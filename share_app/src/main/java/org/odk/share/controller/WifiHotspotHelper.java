package org.odk.share.controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import org.odk.share.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * Created by laksh on 5/17/2018.
 */

public class WifiHotspotHelper {

    private Method getWifiApConfig;
    private Method setWifiApEnable;
    private Method setWifiApConfig;
    private Method isWifiApEnabled;
    private Method getWifiApState;
    private WifiManager wifiManager;
    private WifiConfiguration lastConfig;
    private WifiConfiguration currConfig;
    private Context context;
    public static final String ssid = "ODK-Share";
    private int port;

    public WifiConfiguration getCurrConfig() {
        return currConfig;
    }

    public void setCurrConfig(WifiConfiguration currConfig) {
        this.currConfig = currConfig;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static WifiHotspotHelper wifiHotspotHelper;

    private WifiHotspotHelper(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        for (Method method : wifiManager.getClass().getMethods()) {
            switch (method.getName()) {
                case "isWifiApEnabled" :
                    isWifiApEnabled = method;
                    break;
                case "setWifiApEnabled":
                    setWifiApEnable = method;
                    break;
                case "getWifiApState":
                    getWifiApState = method;
                    break;
                case "getWifiApConfiguration":
                    getWifiApConfig = method;
                    break;
                case "setWifiApConfiguration":
                    setWifiApConfig = method;
            }
        }
    }

    public static synchronized WifiHotspotHelper getInstance(Context context) {
        if (wifiHotspotHelper == null) {
            wifiHotspotHelper = new WifiHotspotHelper(context);
        }
        return wifiHotspotHelper;
    }

    public boolean isSupported() {
        return isWifiApEnabled != null && setWifiApEnable != null && getWifiApState != null &&
                getWifiApConfig != null && setWifiApConfig != null;
    }

    public WifiConfiguration getWifiConfig() {
        Object obj = null;
        try {
            obj = getWifiApConfig.invoke(wifiManager, null);
            if (obj != null) {
                return (WifiConfiguration) obj;
            }
        } catch (IllegalAccessException e) {
            Timber.e(e);
        } catch (InvocationTargetException e) {
            Timber.e(e);
        }
        return null;
    }

    public Boolean isHotspotEnabled() {
        Object obj = null;
        try {
            obj = isWifiApEnabled.invoke(wifiManager);
            if (obj == null) {
                return false;
            }
        } catch (IllegalAccessException e) {
            Timber.e(e);
        } catch (InvocationTargetException e) {
            Timber.e(e);
        }
        return (Boolean) obj;
    }

    public Object setWifiConfig(WifiConfiguration configuration) {
        Object obj = null;
        try {
            obj = setWifiApConfig.invoke(wifiManager, configuration);
            if (obj != null) {
                return obj;
            }
        } catch (IllegalAccessException e) {
            Timber.e(e);
        } catch (InvocationTargetException e) {
            Timber.e(e);
        }
        return null;
    }

    public void saveLastConfig() {
        lastConfig = getWifiConfig();
    }

    public boolean enableHotspot() {
        saveLastConfig();

        currConfig = createNewConfig(ssid + context.getString(R.string.hotspot_name_suffix));
        return toggleHotspot(currConfig, true);
    }

    public boolean disableHotspot() {
        setWifiConfig(lastConfig);
        return toggleHotspot(lastConfig, false);
    }

    private boolean toggleHotspot(WifiConfiguration configuration, boolean enable) {
        try {
            Object obj = setWifiApEnable.invoke(wifiManager, configuration, enable);
            if (obj != null) {
                return (Boolean) obj;
            }
        } catch (IllegalAccessException e) {
            Timber.e(e);
        } catch (InvocationTargetException e) {
            Timber.e(e);
        }
        return false;
    }

    public WifiConfiguration createNewConfig(String ssid) {
        WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.SSID = ssid;
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(wifiConf);
        wifiManager.saveConfiguration();
        return wifiConf;
    }

    public static boolean isMobileDataEnabled(Context context) {
        boolean enabled = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            enabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            Timber.e(e);
        }
        return enabled;
    }
}
