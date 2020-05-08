 package com.worthwhilegames.cardgames.shared;
 
 import android.content.Context;
 import android.content.SharedPreferences;
import android.os.Build;
 import android.util.Log;
 import com.worthwhilegames.cardgames.BuildConfig;
 import org.apache.http.conn.util.InetAddressUtils;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 
 import static com.worthwhilegames.cardgames.shared.Constants.PREF_CHEATER_MODE;
 
 /**
  * A Utility class that contains common generic methods relevant
  * to any object in the application
  */
 public class Util {
 
     public static final String TAG_GENERIC = Util.class.getName();
 
     /**
      * Should we try and use Android's NSD library instead of
      * the jmDns library we package with the app?
      */
    public static final boolean shouldUseNsd = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
 
     /**
      * Represents whether this device is acting as the gameboard
      */
     private static boolean isGameboard = false;
 
     /**
      * Are we in a unit test?
      */
     public static boolean isTestSuite = false;
 
     /**
      * Checks whether this is a debug build or not.
      *
      * This toggles different logging capabilities
      *
      * @return whether this is a production build or not
      */
     public static boolean isDebugBuild() {
         return !isTestSuite && BuildConfig.DEBUG;
     }
 
     /**
      * Returns whether this device is acting as the gameboard
      *
      * @return whether this device is acting as the gameboard
      */
     public static boolean isGameboard() {
         return Util.isGameboard;
     }
 
     /**
      * Checks whether cheater mode is enabled
      *
      * This displays all player hands
      *
      * @return whether cheater mode is enabled
      */
     public static boolean isCheaterMode(Context ctx) {
         SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREFERENCES, 0);
 
         return prefs.getBoolean(PREF_CHEATER_MODE, false);
     }
 
     /**
      * Returns whether this device is a Google TV
      *
      * @param ctx
      * @return whether this device is a Google TV
      */
     public static boolean isGoogleTv(Context ctx) {
         return ctx.getPackageManager().hasSystemFeature("com.google.android.tv");
     }
 
     /**
      * Set whether this device is acting as the gameboard
      *
      * @param isGameboard whether the device is acting as the gameboard
      */
     public static void setIsGameboard(boolean isGameboard) {
         Util.isGameboard = isGameboard;
     }
 
     /**
      * Get this device's IP address
      *
      * @return the ip address of this device
      */
     public static InetAddress getLocalIpAddress() {
         try {
             String addrToReturn;
 
             // Check for IPv4 address
             for (Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces(); inter.hasMoreElements();) {
                 NetworkInterface intf = inter.nextElement();
                 for (Enumeration<InetAddress> enumIP = intf.getInetAddresses(); enumIP.hasMoreElements();) {
                     InetAddress inet = enumIP.nextElement();
                     addrToReturn = inet.getHostAddress();
 
                     if (Util.isDebugBuild()) {
                         Log.d(TAG_GENERIC, "Address: " + addrToReturn);
                         Log.d(TAG_GENERIC, "isIPv4Address: " + InetAddressUtils.isIPv4Address(addrToReturn));
                     }
 
                     if (!inet.isLoopbackAddress() && InetAddressUtils.isIPv4Address(addrToReturn)) {
                         return inet;
                     }
                 }
             }
 
             // Check for IPv6 address
             for (Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces(); inter.hasMoreElements();) {
                 NetworkInterface intf = inter.nextElement();
                 for (Enumeration<InetAddress> enumIP = intf.getInetAddresses(); enumIP.hasMoreElements();) {
                     InetAddress inet = enumIP.nextElement();
                     addrToReturn = inet.getHostAddress();
 
                     int indexOfPercent = addrToReturn.indexOf('%');
                     if (indexOfPercent > 0) {
                         addrToReturn = addrToReturn.substring(0, indexOfPercent);
                     }
 
                     if (Util.isDebugBuild()) {
                         Log.d(TAG_GENERIC, "Address: " + addrToReturn);
                         Log.d(TAG_GENERIC, "isIPv6Address: " + InetAddressUtils.isIPv6Address(addrToReturn));
                         Log.d(TAG_GENERIC, "isIPv6HexCompressedAddress: " + InetAddressUtils.isIPv6HexCompressedAddress(addrToReturn));
                         Log.d(TAG_GENERIC, "isIPv6StdAddress: " + InetAddressUtils.isIPv6StdAddress(addrToReturn));
                     }
 
                     if (!inet.isLoopbackAddress() && (InetAddressUtils.isIPv6Address(addrToReturn) || InetAddressUtils.isIPv6HexCompressedAddress(addrToReturn)
                             || InetAddressUtils.isIPv6StdAddress(addrToReturn))) {
                         return inet;
                     }
                 }
             }
 
         } catch (SocketException ex) {
             Log.e(TAG_GENERIC, ex.toString());
         }
 
         return null;
     }
 }
