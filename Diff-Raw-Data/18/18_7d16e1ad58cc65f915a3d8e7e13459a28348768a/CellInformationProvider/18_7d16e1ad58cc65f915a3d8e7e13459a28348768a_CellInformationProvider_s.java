 package  com.protocollabs.android.cellperf;
 
 
 import android.content.Context;
 import android.provider.Settings.Secure;
 import android.telephony.TelephonyManager;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.telephony.NeighboringCellInfo;
 import android.telephony.PhoneStateListener;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.telephony.gsm.GsmCellLocation;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 
 import android.os.Parcelable;
 import android.os.Parcel;
 import android.widget.Toast;
 
 // Info about Cell Information:
 // https://developer.android.com/reference/android/telephony/TelephonyManager.html
 
 
 public class CellInformationProvider {
 
     public String measurementDate;
     public String coordinateLangitude;
     public String coordinateLongitude;
 
     public int dbm;
 
     private TelephonyManager telephonyManager;
 
     private Context context;
     private CellInformation mCellInformation;
     private PhoneStateListener mPhoneStateListener;
 
 
     public CellInformationProvider(Context context, CellInformation cellInformation) {
         this.context = context;
         mCellInformation = cellInformation;
     }
 
 
     public void activate() {
         initNetwork();
     }
 
     
     public void deactivate() {
         assert telephonyManager != null;
         assert mPhoneStateListener != null;
 
         // stop listening
         telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
     }
 
 
     private synchronized void initNetwork() {
 
         final int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
 
         if (telephonyManager == null) {
             mPhoneStateListener = new SignalStrengthChangeListener();
             telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
             telephonyManager.listen(mPhoneStateListener, events);
         }
     }
 
 
     private static final String[] NETWORK_TYPES = {
         "UNKNOWN",  // 0  - NETWORK_TYPE_UNKNOWN
         "GPRS",     // 1  - NETWORK_TYPE_GPRS
         "EDGE",     // 2  - NETWORK_TYPE_EDGE
         "UMTS",     // 3  - NETWORK_TYPE_UMTS
         "CDMA",     // 4  - NETWORK_TYPE_CDMA
         "EVDO_0",   // 5  - NETWORK_TYPE_EVDO_0
         "EVDO_A",   // 6  - NETWORK_TYPE_EVDO_A
         "1xRTT",    // 7  - NETWORK_TYPE_1xRTT
         "HSDPA",    // 8  - NETWORK_TYPE_HSDPA
         "HSUPA",    // 9  - NETWORK_TYPE_HSUPA
         "HSPA",     // 10 - NETWORK_TYPE_HSPA
         "IDEN",     // 11 - NETWORK_TYPE_IDEN
         "EVDO_B",   // 12 - NETWORK_TYPE_EVDO_B
         "LTE",      // 13 - NETWORK_TYPE_LTE
         "EHRPD",    // 14 - NETWORK_TYPE_EHRPD
         "HSPA+",    // 15 - NETWORK_TYPE_HSPAP
     };
 
 
     public synchronized void setCurrentRssi(int rssi) {
         mCellInformation.setCurrentRssi(rssi);
     }
 
 
     private void cellInfo() {
         //CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
         //CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
         //cellSignalStrengthGsm.getDbm();
     }
 
 
     private void update() {
         mCellInformation.setTelephonyNetworkType(getTelephonyNetworkType());
         mCellInformation.setSimOperator(telephonyManager.getSimOperator());
         mCellInformation.setSimOperatorName(telephonyManager.getSimOperatorName());
 
         mCellInformation.setNetworkOperatorName(telephonyManager.getNetworkOperatorName());
         mCellInformation.setNetworkOperator(telephonyManager.getNetworkOperator());
         mCellInformation.setNetworkCountryIso(telephonyManager.getNetworkCountryIso());
 
         mCellInformation.setDeviceId(telephonyManager.getDeviceId());
 
         GsmCellLocation gsmCellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
        mCellInformation.setCid(String.valueOf(gsmCellLocation.getCid()));
        mCellInformation.setLac(String.valueOf(gsmCellLocation.getLac()));
 
 
         mCellInformation.update();
     }
 
 
     public String getNetwork() {
         return getTelephonyNetworkType();
     }
 
 
     private String getTelephonyNetworkType() {
 
         int networkType = telephonyManager.getNetworkType();
 
         if (networkType < NETWORK_TYPES.length)
             return NETWORK_TYPES[telephonyManager.getNetworkType()];
 
         return "Unrecognized: " + networkType;
     }
 
     private String getTelephonyPhoneType() {
         switch (telephonyManager.getPhoneType()) {
             case TelephonyManager.PHONE_TYPE_CDMA:
                 return "CDMA";
             case TelephonyManager.PHONE_TYPE_GSM:
                 return "GSM";
             case TelephonyManager.PHONE_TYPE_NONE:
                 return "None";
         }
         return "Unknown";
     }
 
 
     private class SignalStrengthChangeListener extends PhoneStateListener {
 
         @Override
         public void onSignalStrengthsChanged(SignalStrength signalStrength) {
 
             super.onSignalStrengthsChanged(signalStrength);
 
             if (getNetwork().compareTo(NETWORK_TYPES[TelephonyManager.NETWORK_TYPE_CDMA]) == 0) {
                 setCurrentRssi(signalStrength.getCdmaDbm());
             } else if (getNetwork().compareTo(NETWORK_TYPES[TelephonyManager.NETWORK_TYPE_EVDO_0]) == 0 ||
                     getNetwork().compareTo(NETWORK_TYPES[TelephonyManager.NETWORK_TYPE_EVDO_A]) == 0 ||
                     getNetwork().compareTo(NETWORK_TYPES[TelephonyManager.NETWORK_TYPE_EVDO_B]) == 0) {
                 setCurrentRssi(signalStrength.getEvdoDbm());
             } else if (signalStrength.isGsm()) {
                 if (signalStrength.getGsmSignalStrength() != 99)
                     setCurrentRssi(signalStrength.getGsmSignalStrength() * 2 - 113);
                 else
                     setCurrentRssi(signalStrength.getGsmSignalStrength());
             }
 
             update();
         }
     }
 }
