 package org.babysuper.phonegap.plugin;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.DhcpInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 import com.phonegap.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.*;
 
 /**
  * Created by Bodil.
  * User: Bodil
  * Date: 12-5-7
  * Time: 下午4:45
  */
 public class NetDroid extends Plugin {
 
     private static String ACTION_NAME_GETCONFIG = "getConfig";
     private static String ACTION_NAME_GETSSIDS = "getSSIDs";
     private List<ScanResult> wifiResultList;
     private HashMap<String, String> ssidHashMap = new HashMap<String, String>();
     private WifiManager wifiManger;
     private static Boolean isRegSSIDScan = false;
 
     public PluginResult execute(String actionName, JSONArray jsonParams, String callBack) {
         if (!ACTION_NAME_GETCONFIG.equals(actionName) && !ACTION_NAME_GETSSIDS.equals(actionName))
             return new PluginResult(PluginResult.Status.INVALID_ACTION, "The action name must is:" + ACTION_NAME_GETCONFIG
                     + "or " + ACTION_NAME_GETSSIDS);
         PluginResult result;
         try {
             JSONObject configJson;
            if (ACTION_NAME_GETCONFIG.equals(actionName)) {
                 configJson = getDHCPJson();
                 configJson.put("ip", getLocalIpAddress());
            } else
                 configJson = getSSIDJson();
 
             result = new PluginResult(PluginResult.Status.OK, configJson);
             return result;
         } catch (JSONException ex) {
             Log.e("NetDroid Error", String.format("Info:%s", ex.getMessage()));
         }
         return new PluginResult(PluginResult.Status.NO_RESULT, "What Error?I don't know.");
     }
 
     private String getLocalIpAddress() {
         try {
             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                 NetworkInterface netInterface = en.nextElement();
                 for (Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                     InetAddress inetAddress = inetAddresses.nextElement();
                     if (!inetAddress.isLoopbackAddress()) {
                         return inetAddress.getHostAddress().toString();
                     }
                 }
             }
         } catch (SocketException ex) {
             Log.e("WifiPreference IpAddress", ex.toString());
         }
         return null;
     }
 
     private JSONObject getDHCPJson() {
         wifiManger = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
         DhcpInfo dhcpInfo = wifiManger.getDhcpInfo();
         WifiInfo wifiInfo = wifiManger.getConnectionInfo();
         JSONObject dhcpJson = new JSONObject();
         try {
             dhcpJson.put("staticIp", intToIp(dhcpInfo.ipAddress));
             dhcpJson.put("wifiAddress", intToIp(wifiInfo.getIpAddress()));
             dhcpJson.put("netmask", intToIp(dhcpInfo.netmask));
             dhcpJson.put("gateway", intToIp(dhcpInfo.gateway));
             dhcpJson.put("dns1", intToIp(dhcpInfo.dns1));
             dhcpJson.put("dns2", intToIp(dhcpInfo.dns2));
             dhcpJson.put("serverAddress", intToIp(dhcpInfo.serverAddress));
             dhcpJson.put("mac", String.valueOf(wifiInfo.getMacAddress()));
 
         } catch (JSONException ex) {
             Log.e("getDHCPInfo Error", "JSON Error:" + ex.getMessage());
         } finally {
             return dhcpJson;
         }
     }
 
     private String intToIp(int ipInt) {
         return (ipInt & 0xFF) + "." + ((ipInt >> 8) & 0xFF) + "." + ((ipInt >> 16) & 0xFF) + "." + ((ipInt >> 24) & 0xFF);
     }
 
     private JSONObject getSSIDJson() {
         regSSIDListener();
         ssidHashMap.clear();
         wifiManger.startScan();
         try {
             int recordSize = wifiResultList.size();
             while (recordSize >= 0) {
                 ScanResult recordItem = wifiResultList.get(recordSize);
                 ssidHashMap.put(recordItem.SSID, recordItem.capabilities + "|" + recordItem.frequency + "|" + recordItem.level);
                 recordSize--;
             }
         } catch (Exception ex) {
            Log.e("get wifi ssid error", "get wifi ssid error:" + ex.getMessage());
         }
         JSONObject ssidJsonObj = new JSONObject();
         try {
             Iterator iterator = ssidHashMap.entrySet().iterator();
             while (iterator.hasNext()) {
                 Map.Entry<String, String> ssidEntry = (Map.Entry<String, String>) iterator.next();
                 ssidJsonObj.put(ssidEntry.getKey(), ssidEntry.getValue());
             }
         } catch (JSONException ex) {
            Log.e("get wifi ssid parse to json Error", "ssid parse error:" + ex.getMessage());
         }
         return ssidJsonObj;
     }
 
    private void regSSIDListener() {
         if (wifiManger == null)
             wifiManger = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
         if (!isRegSSIDScan) {
            ctx.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     wifiResultList = wifiManger.getScanResults();
                 }
             }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
             isRegSSIDScan = true;
         }
     }
 }
