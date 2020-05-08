 package eu.liveandgov.sensorcollectorv3;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 import java.io.File;
 
 import eu.liveandgov.sensorcollectorv3.Configuration.IntentAPI;
 import eu.liveandgov.sensorcollectorv3.Connector.ConnectorThread;
 import eu.liveandgov.sensorcollectorv3.Monitor.MonitorThread;
 import eu.liveandgov.sensorcollectorv3.Persistence.Persistor;
 import eu.liveandgov.sensorcollectorv3.Persistence.ZmqStreamer;
 import eu.liveandgov.sensorcollectorv3.SensorQueue.LinkedSensorQueue;
 import eu.liveandgov.sensorcollectorv3.SensorQueue.SensorQueue;
 import eu.liveandgov.sensorcollectorv3.Sensors.SensorParser;
 import eu.liveandgov.sensorcollectorv3.Sensors.SensorThread;
 import eu.liveandgov.sensorcollectorv3.Transfer.TransferManager;
 import eu.liveandgov.sensorcollectorv3.Transfer.TransferThreadPost;
 
 public class ServiceSensorControl extends Service {
     static final String LOG_TAG =  "SCS";
     public static final String STAGE_FILENAME = "sensor.stage.ssf";
     public static final String SENSOR_FILENAME = "sensor.ssf";
 
     public boolean isRecording = false;
     public boolean isTransferring = false;
 
     public ServiceSensorControl() {}
 
     // Thread objects
     // Rem: SensorThread is static
     public ConnectorThread connectorThread;
     public TransferManager transferManager;
 
     // Communication Channels
     public SensorQueue sensorQueue;
     public Persistor persistor;
 
 
     public ServiceSensorControl context;
 
 
     /* ANDROID LIFECYCLE */
     @Override
     public void onCreate() {
         Log.i(LOG_TAG, "Creating ServiceSensorControl }");
 
         // Setup static variables
         GlobalContext.set(this);
 
         // setup communication Channels
         persistor   = new FilePersistor(new File(GlobalContext.context.getFilesDir(), SENSOR_FILENAME));
        persistor   = new ZmqStreamer();
         sensorQueue = new LinkedSensorQueue();
 
         // Start sensor thread
         SensorThread.setup(sensorQueue);
         SensorThread.start();
 
         // Connect sensorQueue to Persistor
         connectorThread = new ConnectorThread(sensorQueue, persistor);
         connectorThread.start();
 
         // setup sensor manager
         transferManager = new TransferThreadPost(persistor, new File(GlobalContext.context.getFilesDir(), STAGE_FILENAME));
 
         // Start transfer thread
         // TransferThreadZMQ.setup();
         // TransferThreadZMQ.getInstance().start();
 
         // Start monitoring thread
         // MonitorThread.addQueue(sensorQueue)
         // MonitorThread.addPersistor(persistor)
         MonitorThread m = new MonitorThread();
         m.registerMonitorable(connectorThread, "SampleCount");
         m.registerMonitorable(persistor, "Persitor");
         m.registerMonitorable(transferManager, "Transfer");
         m.registerMonitorable(sensorQueue, "Queue");
         m.start();
 
         super.onCreate();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     /* INTENT API */
 
     /**
      * Dispatches incoming intents
      */
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (intent == null) {
             Log.i(LOG_TAG, "No intent received.");
             return START_STICKY;
         }
 
         String action = intent.getAction();
         Log.i(LOG_TAG, "Received intent with action " + action);
 
         if (action == null) return START_STICKY;
 
         // Dispatch IntentAPI
         if (action.equals(IntentAPI.RECORDING_ENABLE)) {
             doEnableRecording();
             doSendStatus();
         } else if (action.equals(IntentAPI.RECORDING_DISABLE)) {
             doDisableRecording();
             doSendStatus();
         } else if (action.equals(IntentAPI.TRANSFER_SAMPLES)) {
             doTransferSamples();
             doSendStatus();
         } else if (action.equals(IntentAPI.ANNOTATE)) {
             doAnnotate(intent.getStringExtra(IntentAPI.FIELD_ANNOTATION));
         } else if (action.equals(IntentAPI.GET_STATUS)) {
             doSendStatus();
         } else {
             Log.i(LOG_TAG, "Received unknown action " + action);
         }
 
         return START_STICKY;
     }
 
     private void doAnnotate(String tag) {
         String msg = SensorParser.parse(tag);
         sensorQueue.push(msg);
     }
 
     private void doTransferSamples() {
         transferManager.doTransfer();
         isTransferring = true;
     }
 
     private void doDisableRecording() {
         SensorThread.stopAllRecording();
         isRecording = false;
     }
 
     private void doEnableRecording() {
         SensorThread.startAllRecording();
         isRecording = true;
     }
 
     public void doSendStatus() {
         isTransferring = transferManager.isTransferring();
 
         Intent intent = new Intent(IntentAPI.RETURN_STATUS);
         intent.putExtra(IntentAPI.FIELD_SAMPLING, isRecording);
         intent.putExtra(IntentAPI.FIELD_TRANSFERRING, isTransferring);
         sendBroadcast(intent);
     }
 
 }
