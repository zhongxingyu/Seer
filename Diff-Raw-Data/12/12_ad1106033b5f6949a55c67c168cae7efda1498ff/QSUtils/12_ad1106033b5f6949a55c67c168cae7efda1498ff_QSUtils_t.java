 package com.android.internal.util.cm;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.res.Resources;
 import android.hardware.Camera;
 import android.hardware.display.DisplayManager;
 import android.hardware.display.WifiDisplayStatus;
 import android.net.ConnectivityManager;
 import android.nfc.NfcAdapter;
 import android.os.UserHandle;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 
 import com.android.internal.telephony.PhoneConstants;
 
 public class QSUtils {
         public static boolean deviceSupportsUsbTether(Context ctx) {
             ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
             return (cm.getTetherableUsbRegexs().length != 0);
         }
 
         public static boolean deviceSupportsWifiDisplay(Context ctx) {
             DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
             return (dm.getWifiDisplayStatus().getFeatureState() != WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE);
         }
 
         public static boolean deviceSupportsMobileData(Context ctx) {
             ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
             return cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
         }
 
         public static boolean deviceSupportsBluetooth() {
             return (BluetoothAdapter.getDefaultAdapter() != null);
         }
 
        public static boolean expandedDesktopEnabled(ContentResolver resolver) {
            return (Settings.System.getIntForUser(resolver, Settings.System.EXPANDED_DESKTOP_STYLE, 0,
                    UserHandle.USER_CURRENT_OR_SELF) != 0);
        }

         public static boolean deviceSupportsNfc(Context ctx) {
             return NfcAdapter.getDefaultAdapter(ctx) != null;
         }
 
         public static boolean deviceSupportsCamera() {
             return Camera.getNumberOfCameras() > 0;
         }
 }
