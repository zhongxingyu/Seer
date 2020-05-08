 package ru.ezhoff.geolocation.geoexplorer;
 
 import android.telephony.*;
 import android.telephony.cdma.CdmaCellLocation;
 import android.telephony.gsm.GsmCellLocation;
 
import java.util.List;

 /**
  * @author e.ezhov
  * @version 1.0 05.06.13
  */
 public class CellMonitor extends PhoneStateListener implements Monitor, Runnable {
     /**
      * Main template for module messaging.
      */
     private static final String MONITORING_MESSAGE = "CellMonitor::%s";
 
     /**
      * The logger.
      */
     private FileLogger logger = FileLogger.getInstance();
 
     /**
      * The period of checking location in milliseconds.
      * if equals <code>0</code> the periodically checks is switched off.
      */
     private long period;
 
     /**
      * The flag of monitoring is on.
      */
     private volatile boolean isStarted = false;
 
     /**
      * The thread for checking by period.
      */
     private Thread thread;
 
     /**
      * The telephony manager.
      */
     private TelephonyManager telephonyManager;
 
     @Override
     public void onServiceStateChanged(ServiceState serviceState) {
         super.onServiceStateChanged(serviceState);
         logger.info(formatModuleMessage(String.format(
                 "Listener::Service state changed - Operator Long: %s, Operator Short: %s, Operator Numeric: %s, " +
                         "State value: %s, Describe contents: %s, is roaming: %s",
                 serviceState.getOperatorAlphaLong(),
                 serviceState.getOperatorAlphaShort(),
                 serviceState.getOperatorNumeric(),
                 serviceState.getState(),
                 serviceState.describeContents(),
                 serviceState.getRoaming()
         )));
     }
 
     @Override
     public void onCellLocationChanged(CellLocation location) {
         super.onCellLocationChanged(location);
         if (location instanceof GsmCellLocation) {
             GsmCellLocation loc = (GsmCellLocation) location;
             logger.info(formatModuleMessage(String.format(
                    "Listener::Cell location changed{GSM} - CID: %s, LAC: %s, PSC: %s",
                     loc.getCid(),
                    loc.getLac(),
                    loc.getPsc()
             )));
         } else if (location instanceof CdmaCellLocation) {
             CdmaCellLocation loc = (CdmaCellLocation) location;
             logger.info(formatModuleMessage(String.format(
                     "Listener::Cell location changed{CDMA} - Station ID: %s, Latitude: %s, Longitude: %s, " +
                             "Network ID: %s, System ID: %s",
                     loc.getBaseStationId(),
                     loc.getBaseStationLatitude(),
                     loc.getBaseStationLongitude(),
                     loc.getNetworkId(),
                     loc.getSystemId()
             )));
         }
     }
 
     @Override
     public void onCallStateChanged(int state, String incomingNumber) {
         super.onCallStateChanged(state, incomingNumber);
         logger.info(formatModuleMessage(String.format(
                 "Listener::Cell state changed - State: %s, Incoming number: %s",
                 state,
                 incomingNumber
         )));
     }
 
     @Override
     public void onDataConnectionStateChanged(int state, int networkType) {
         super.onDataConnectionStateChanged(state, networkType);
         logger.info(formatModuleMessage(String.format(
                 "Listener::Data connection state changed - State: %s, Network type: %s",
                 state,
                 networkType
         )));
     }
 
     @Override
     public void onSignalStrengthChanged(int asu) {
         super.onSignalStrengthChanged(asu);
         logger.info(formatModuleMessage(String.format(
                 "Listener::Signal strength changed - ASU: %s",
                 asu
         )));
     }
 
     @Override
     public void onSignalStrengthsChanged(SignalStrength signalStrength) {
         super.onSignalStrengthsChanged(signalStrength);
         logger.info(formatModuleMessage(String.format(
                 "Listener::Signal strengths changed - is GSM: %s, CDMA DBM: %s, CDMA ERIO: %s, EVDO DBM: %s, " +
                         "EVDO ERIO: %s, EVDO SNR: %s, GSM BER: %s, GSM SS: %s, Describe contents: %s",
                 signalStrength.isGsm(),
                 signalStrength.getCdmaDbm(),
                 signalStrength.getCdmaEcio(),
                 signalStrength.getEvdoDbm(),
                 signalStrength.getEvdoEcio(),
                 signalStrength.getEvdoSnr(),
                 signalStrength.getGsmBitErrorRate(),
                 signalStrength.getGsmSignalStrength(),
                 signalStrength.describeContents()
         )));
     }
 
     @Override
     public void onDataConnectionStateChanged(int state) {
         super.onDataConnectionStateChanged(state);
         logger.info(formatModuleMessage(String.format(
                 "Listener::Data connection state changed - State: %s",
                 state
         )));
     }
 
     @Override
     public void onDataActivity(int direction) {
         super.onDataActivity(direction);
         logger.info(formatModuleMessage(String.format(
                 "Listener::Data activity - Direction: %s",
                 direction
         )));
     }
 
     @Override
     public void start() {
         if (telephonyManager == null) {
             throw new IllegalMonitorStateException("Monitor is not initialized");
         }
         telephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
         telephonyManager.listen(this, PhoneStateListener.LISTEN_CELL_LOCATION);
         telephonyManager.listen(this, PhoneStateListener.LISTEN_DATA_ACTIVITY);
         telephonyManager.listen(this, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
         telephonyManager.listen(this, PhoneStateListener.LISTEN_SERVICE_STATE);
         telephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
         logger.info(formatModuleMessage("Cell listener is added."));
         if (period > 0) {
             isStarted = true;
             if (thread == null) {
                 thread = new Thread(this);
             }
             thread.start();
             logger.info(formatModuleMessage("Cell monitor is started."));
         }
     }
 
     @Override
     public void stop() {
         isStarted = false;
         logger.info(formatModuleMessage("Signal to stopping for cell monitor is sent."));
         telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
         logger.info(formatModuleMessage("Cell listener is removed."));
     }
 
     @Override
     public void run() {
         while (isStarted) {
             if (Thread.interrupted()) {
                 return;
             }
             for (NeighboringCellInfo neighboringCellInfo: telephonyManager.getNeighboringCellInfo()) {
                 logger.info(formatModuleMessage(String.format(
                         "Monitor::Neighboring cell info: CID: %s, LAC: %s, Network type: %s, PSC: %s, RSSI: %s, " +
                                 "Describe contents: %s",
                         neighboringCellInfo.getCid(),
                         neighboringCellInfo.getLac(),
                         neighboringCellInfo.getNetworkType(),
                         neighboringCellInfo.getPsc(),
                         neighboringCellInfo.getRssi(),
                         neighboringCellInfo.describeContents()
                 )));
             }
             try {
                 Thread.sleep(period);
             } catch (InterruptedException e) {
                 logger.error(formatModuleMessage(e.getMessage()));
             }
         }
         logger.info(formatModuleMessage("Cell monitor is stopped."));
     }
 
     /**
      * Sets telephony manager and returns self object for easy init.
      * @param telephonyManager  telephony manager
      * @return                  self object
      */
     public CellMonitor setTelephonyManager(TelephonyManager telephonyManager) {
         this.telephonyManager = telephonyManager;
         return this;
     }
 
     /**
      * Sets period of checks and returns self object for easy init.
      * @param period            period, in milliseconds
      * @return                  self object
      */
     public CellMonitor setPeriod(int period) {
         this.period = period;
         return this;
     }
 
     private String formatModuleMessage(String message) {
         return String.format(MONITORING_MESSAGE, message);
     }
 }
