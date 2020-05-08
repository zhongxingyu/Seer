 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.activity.MSLoggerActivity;
 import uk.org.smithfamily.mslogger.comms.CRC32Exception;
 import uk.org.smithfamily.mslogger.comms.ECUConnectionManager;
 import uk.org.smithfamily.mslogger.ecuDef.gen.ECURegistry;
 import uk.org.smithfamily.mslogger.log.DatalogManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.log.FRDLogManager;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegister;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegisterInterface;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Abstract base class for all ECU implementations
  * 
  * @author dgs
  * 
  */
 public class Megasquirt extends Service implements MSControllerInterface
 {
     private static final int MAX_QUEUE_SIZE = 10;
     BlockingQueue<InjectedCommand> injectionQueue = new ArrayBlockingQueue<InjectedCommand>(MAX_QUEUE_SIZE);
     
     private enum State
     {
         DISCONNECTED, CONNECTING, CONNECTED, LOGGING
     };
 
     private NotificationManager notifications;
     private MSECUInterface ecuImplementation;
 
     private boolean simulated = false;
     public static final String CONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
     public static final String DISCONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
     public static final String NEW_DATA = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
     public static final String UNKNOWN_ECU = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.UNKNOWN_ECU";
     public static final String UNKNOWN_ECU_BT = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.UNKNOWN_ECU_BT";
     public static final String PROBE_ECU = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.ECU_PROBED";
     private static final String INJECTED_COMMAND_RESULTS = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.INJECTED_COMMAND_RESULTS";
     private static final String INJECTED_COMMAND_RESULT_ID = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.INJECTED_COMMAND_RESULTS_ID";
     private static final String INJECTED_COMMAND_RESULT_DATA = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.INJECTED_COMMAND_RESULTS_DATA";
     
     private static final String UNKNOWN = "UNKNOWN";
     private static final String LAST_SIG = "LAST_SIG";
     private static final String LAST_PROBE = "LAST_PROBE";
     private static final int NOTIFICATION_ID = 0;
     private static final int BURN_DATA = 10;
 
     private BroadcastReceiver yourReceiver;
 
     private boolean logging;
     private boolean constantsLoaded;
     private String trueSignature = "Unknown";
     private volatile ECUThread ecuThread;
     private volatile boolean running;
     private static volatile ECUThread watch;
 
     public class LocalBinder extends Binder
     {
         public Megasquirt getService()
         {
             return Megasquirt.this;
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         DebugLogManager.INSTANCE.log("Megasquirt Received start id " + startId + ": " + intent, Log.VERBOSE);
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return mBinder;
     }
 
     // This is the object that receives interactions from clients. See
     // RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
 
     @Override
     public void onCreate()
     {
         super.onCreate();
         notifications = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
         final IntentFilter btChangedFilter = new IntentFilter();
         btChangedFilter.addAction(ApplicationSettings.BT_CHANGED);
 
         final IntentFilter injectCommandResultsFilter = new IntentFilter();
         injectCommandResultsFilter.addAction(Megasquirt.INJECTED_COMMAND_RESULTS);
         
         this.yourReceiver = new BroadcastReceiver()
         {
             @Override
             public void onReceive(Context context, Intent intent)
             {
                 String action = intent.getAction();
                 
                 if (action.equals(ApplicationSettings.BT_CHANGED))
                 {
                     DebugLogManager.INSTANCE.log("BT_CHANGED received", Log.VERBOSE);
                     stop();
                     start();
                 }
                 else if (action.equals(Megasquirt.INJECTED_COMMAND_RESULTS))
                 {
                     int resultId = intent.getIntExtra(Megasquirt.INJECTED_COMMAND_RESULT_ID, 0);
                     
                     if (resultId == Megasquirt.BURN_DATA)
                     {
                         // Wait til we get some data and flush it
                         try
                         {
                             Thread.sleep(200);
                         }
                         catch (InterruptedException e) {}
                         
                         try 
                         {
                             ECUConnectionManager.getInstance().flushAll();
                         }
                         catch (IOException e) {}
                     }
                 }
             }
         };
 
         // Registers the receiver so that your service will listen for broadcasts
         this.registerReceiver(this.yourReceiver, btChangedFilter);
         this.registerReceiver(this.yourReceiver, injectCommandResultsFilter);
         
         ApplicationSettings.INSTANCE.setEcu(this);
         // setState(State.DISCONNECTED);
         start();
 
         startForeground(NOTIFICATION_ID, null);
     }
 
     private void setState(State s)
     {
         int msgId;
 
         switch (s)
         {
         case DISCONNECTED:
             msgId = R.string.disconnected_from_ms;
             break;
         case CONNECTING:
             msgId = R.string.connecting_to_ms;
             break;
         case CONNECTED:
             msgId = R.string.connected_to_ms;
             break;
         case LOGGING:
             msgId = R.string.logging;
             break;
         default:
             msgId = R.string.unknown;
             break;
         }
         CharSequence text = getText(R.string.app_name);
         Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MSLoggerActivity.class), 0);
         notification.setLatestEventInfo(this, getText(msgId), text, contentIntent);
         notifications.notify(NOTIFICATION_ID, notification);
     }
 
     @Override
     public void onDestroy()
     {
         super.onDestroy();
         notifications.cancelAll();
         // Do not forget to unregister the receiver!!!
         this.unregisterReceiver(this.yourReceiver);
     }
 
     /**
      * Shortcut function to access data tables. Makes the INI->Java translation a little simpler
      * 
      * @param i1 index into table
      * @param name table name
      * @return value from table
      */
     protected int table(int i1, String name)
     {
         return TableManager.INSTANCE.table(i1, name);
     }
 
     public int table(double d1, String name)
     {
         return table((int) d1, name);
     }
 
     /**
      * Add a command for the ECUThread to process when it can
      * @param command
      */
     public void injectCommand(InjectedCommand command)
     {
         injectionQueue.add(command);
     }
     /**
      * If we're not running, start things up, if we are already running, shut things down
      */
     public synchronized void toggleConnection()
     {
         if (!running)
         {
             ApplicationSettings.INSTANCE.setAutoConnectOverride(true);
             start();
         }
         else
         {
             ApplicationSettings.INSTANCE.setAutoConnectOverride(false);
             stop();
         }
     }
 
     /**
      * 
      * @return
      */
     public boolean isLogging()
     {
         return logging;
     }
 
     /**
      * Temperature unit conversion function
      * 
      * @param t temp in F
      * @return temp in C if CELSIUS is set, in F otherwise
      */
     public double tempCvt(double t)
     {
         if (isSet("CELSIUS"))
         {
             return (t - 32.0) * 5.0 / 9.0;
         }
         else
         {
             return t;
         }
     }
 
     /**
      * Launch the ECU thread
      */
     public synchronized void start()
     {
         DebugLogManager.INSTANCE.log("Megasquirt.start()", Log.INFO);
 
         if (ApplicationSettings.INSTANCE.getECUBluetoothMac().equals(ApplicationSettings.MISSING_VALUE))
         {
             broadcast(UNKNOWN_ECU_BT);
         }
         else
         {
             if (ecuThread != null)
             {
                 ecuThread.halt();
             }
             ecuThread = new ECUThread();
 
             ecuThread.start();
         }
     }
 
     /**
      * Shut down the ECU thread
      */
     public synchronized void stop()
     {
         DebugLogManager.INSTANCE.log("Megasquirt.stop()", Log.INFO);
 
         if (ecuThread != null)
         {
             ecuThread.halt();
             ecuThread = null;
         }
 
         running = false;
 
         setState(State.DISCONNECTED);
         broadcast(DISCONNECTED);
     }
 
     /**
      * Revert to initial state
      */
     public void reset()
     {
         ecuImplementation.refreshFlags();
         constantsLoaded = false;
         running = false;
         notifications.cancelAll();
     }
 
     /**
      * Output the current values to be logged
      */
     private void logValues(byte[] buffer)
     {
         if (!isLogging())
         {
             return;
         }
         try
         {
             FRDLogManager.INSTANCE.write(buffer);
             DatalogManager.INSTANCE.write(ecuImplementation.getLogRow());
 
         }
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
     }
 
     /**
      * Shutdown the data connection to the MS
      */
     private void disconnect()
     {
         if (simulated)
         {
             return;
         }
         
         DebugLogManager.INSTANCE.log("Disconnect", Log.INFO);
 
         ECUConnectionManager.getInstance().disconnect();
         DatalogManager.INSTANCE.mark("Disconnected");
         FRDLogManager.INSTANCE.close();
         DatalogManager.INSTANCE.close();
         broadcast(DISCONNECTED);
     }
 
     /**
      * Send a message to the user
      * 
      * @param msg Message to be sent
      */
     protected void sendMessage(String msg)
     {
         broadcast(ApplicationSettings.GENERAL_MESSAGE, msg);
     }
 
     /**
      * Send a toast message to the user
      * 
      * @param message to be sent
      */
     protected void sendToastMessage(String msg)
     {
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.TOAST);
         broadcast.putExtra(ApplicationSettings.TOAST_MESSAGE, msg);
         sendBroadcast(broadcast);
     }
 
     /**
      * Send the reads per second to be displayed on the screen
      * 
      * @param RPS the current reads per second value
      */
     private void sendRPS(double RPS)
     {
         DecimalFormat decimalFormat = new DecimalFormat("#.0");
 
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.RPS_MESSAGE);
         broadcast.putExtra(ApplicationSettings.RPS, decimalFormat.format(RPS));
         sendBroadcast(broadcast);
     }
 
     /**
      * Send a status update to the rest of the application
      * 
      * @param action
      */
     private void broadcast(String action)
     {
         Intent broadcast = new Intent();
         broadcast.setAction(action);
         sendBroadcast(broadcast);
     }
 
     private void broadcast(String action, String data)
     {
         DebugLogManager.INSTANCE.log("Megasquirt.broadcast(" + action + "," + data + ")", Log.VERBOSE);
         Intent broadcast = new Intent();
         broadcast.setAction(action);
         broadcast.putExtra(ApplicationSettings.MESSAGE, data);
         sendBroadcast(broadcast);
     }
 
     private void broadcast()
     {
         Intent broadcast = new Intent();
         broadcast.setAction(NEW_DATA);
 
         sendBroadcast(broadcast);
 
     }
 
     /**
      * How long have we been running?
      * 
      * @return
      */
     public double timeNow()
     {
         DecimalFormat decimalFormat = new DecimalFormat("#.000");
 
         return Double.parseDouble(decimalFormat.format(((System.currentTimeMillis() - DatalogManager.INSTANCE.getLogStart()) / 1000.0)));
     }
 
     /**
      * Flag the logging process to happen
      */
     public void startLogging()
     {
         logging = true;
         DebugLogManager.INSTANCE.log("startLogging()", Log.INFO);
 
     }
 
     /**
      * Stop the logging process
      */
     public void stopLogging()
     {
         DebugLogManager.INSTANCE.log("stopLogging()", Log.INFO);
         logging = false;
         FRDLogManager.INSTANCE.close();
         DatalogManager.INSTANCE.close();
     }
 
     /**
      * Take a wild stab at what this does.
      * 
      * @param v
      * @return
      */
     public double round(double v)
     {
         return Math.floor(v * 100 + .5) / 100;
     }
 
     /**
      * Returns if a flag has been set in the application
      * 
      * @param name
      * @return
      */
     public boolean isSet(String name)
     {
         return ApplicationSettings.INSTANCE.isSet(name);
     }
 
     /**
      * The thread that handles all communications with the ECU. This must be done in it's own thread as Android gets very picky about unresponsive UI
      * threads
      */
     private class ECUThread extends Thread
     {
         private class CalculationThread extends Thread
         {
             private volatile boolean running = true;
 
             public void halt()
             {
                 DebugLogManager.INSTANCE.log("CalculationThread.halt()", Log.INFO);
 
                 running = false;
             }
 
             public void run()
             {
                 this.setName("CalculationThread");
                 try
                 {
                     while (running)
                     {
                         byte[] buffer = handshake.get();
                         if (ecuImplementation != null)
                         {
                             ecuImplementation.calculate(buffer);
                             logValues(buffer);
                             broadcast();
                         }
                     }
                 }
                 catch (InterruptedException e)
                 {
                     // Swallow, we're on our way out.
                 }
             }
 
         }
 
         class Handshake
         {
             private byte[] buffer;
 
             public void put(byte[] buf)
             {
                 buffer = buf;
                 synchronized (this)
                 {
                     notify();
                 }
             }
 
             public byte[] get() throws InterruptedException
             {
                 synchronized (this)
                 {
                     wait();
                 }
                 return buffer;
             }
         }
 
 
         Handshake handshake = new Handshake();
         CalculationThread calculationThread = new CalculationThread();
 
         /**
          * 
          */
         public ECUThread()
         {
             if (watch != null)
             {
                 DebugLogManager.INSTANCE.log("Attempting to create second connection!", Log.ASSERT);
             }
             watch = this;
             String name = "ECUThread:" + System.currentTimeMillis();
             setName(name);
             DebugLogManager.INSTANCE.log("Creating ECUThread named " + name, Log.VERBOSE);
             calculationThread.start();
 
         }
 
         /**
          * Kick the connection off
          */
         public void initialiseConnection()
         {
             // sendMessage("Launching connection");
 
             // Connection conn = ConnectionFactory.INSTANCE.getConnection();
             String btAddress = ApplicationSettings.INSTANCE.getECUBluetoothMac();
             ECUConnectionManager.getInstance().init(null, btAddress);
         }
 
         /**
          * The main loop of the connection to the ECU
          */
         public void run()
         {
             try
             {
                 setState(Megasquirt.State.CONNECTING);
                 sendMessage("Starting connection");
                 DebugLogManager.INSTANCE.log("BEGIN connectedThread", Log.INFO);
                 initialiseConnection();
 
                 try
                 {
                     Thread.sleep(500);
                 }
                 catch (InterruptedException e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
 
                 running = true;
 
                 try
                 {
                     ECUConnectionManager.getInstance().flushAll();
                     initialiseImplementation();
                     /*
                      * Make sure we have calculated runtime vars at least once before refreshing flags. The reason is that the refreshFlags() function
                      * also trigger the creation of menus/dialogs/tables/curves/etc that use variables such as {clthighlim} in curves that need to
                      * have their value assigned before being used.
                      */
                     try
                     {
                         byte[] bufferRV = getRuntimeVars();
                         ecuImplementation.calculate(bufferRV);
                     }
                     catch (CRC32Exception e)
                     {
                         DebugLogManager.INSTANCE.logException(e);
                     }
 
                     // Make sure everyone agrees on what flags are set
                     ApplicationSettings.INSTANCE.refreshFlags();
                     ecuImplementation.refreshFlags();
 
                     if (!constantsLoaded)
                     {
                         // Only do this once so reconnects are quicker
                         ecuImplementation.loadConstants(simulated);
                         constantsLoaded = true;
 
                     }
                     sendMessage("Connected to " + getTrueSignature());
                     setState(Megasquirt.State.CONNECTED);
                     long lastRpsTime = System.currentTimeMillis();
                     double readCounter = 0;
 
                     // This is the actual work. Outside influences will toggle 'running' when we want this to stop
                     while (running)
                     {
                         try
                         {
                             if (injectionQueue.peek() != null)
                             {
                                 for (InjectedCommand i : injectionQueue)
                                 {
                                     processCommand(i);
                                 }
                                 
                                 injectionQueue.clear();
                             }
                             final byte[] buffer = getRuntimeVars();
                             handshake.put(buffer);
                         }
                         catch (CRC32Exception e)
                         {
                             DatalogManager.INSTANCE.mark(e.getLocalizedMessage());
                             DebugLogManager.INSTANCE.logException(e);
                         }
                         readCounter++;
 
                         long delay = System.currentTimeMillis() - lastRpsTime;
                         if (delay > 1000)
                         {
                             double RPS = readCounter / delay * 1000;
                             readCounter = 0;
                             lastRpsTime = System.currentTimeMillis();
 
                             if (RPS > 0)
                             {
                                 sendRPS(RPS);
                             }
                         }
 
                     }
                 }
                 catch (IOException e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
                 catch (CRC32Exception e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
                 catch (ArithmeticException e)
                 {
                     // If we get a maths error, we probably have loaded duff constants and hit a divide by zero
                     // force the constants to reload in case it was just a bad data read
                     DebugLogManager.INSTANCE.logException(e);
                     constantsLoaded = false;
                 }
                 catch (RuntimeException t)
                 {
                     DebugLogManager.INSTANCE.logException(t);
                     throw (t);
                 }
                 // We're on our way out, so drop the connection
                 disconnect();
             }
             finally
             {
                 calculationThread.halt();
                 calculationThread.interrupt();
                 watch = null;
             }
         }
 
         private void processCommand(InjectedCommand i) throws IOException
         {
             ECUConnectionManager.getInstance().writeCommand(i.getCommand(), i.getDelay(), ecuImplementation.isCRC32Protocol());
             
             // If we want to get the result back
             if (i.isReturnResult())
             {
                 Intent broadcast = new Intent();
                 broadcast.setAction(INJECTED_COMMAND_RESULTS);
                 
                 byte[] result = ECUConnectionManager.getInstance().readBytes();
 
                 broadcast.putExtra(INJECTED_COMMAND_RESULT_ID, i.getResultId());
                 broadcast.putExtra(INJECTED_COMMAND_RESULT_DATA, result);
                 
                 sendBroadcast(broadcast);
             }
         }
 
         private void initialiseImplementation() throws IOException, CRC32Exception
         {
             sendMessage("Checking your ECU");
             String signature = getSignature();
 
             Class<? extends MSECUInterface> ecuClass = ECURegistry.INSTANCE.findEcu(signature);
 
             if (ecuImplementation != null && ecuImplementation.getClass().equals(ecuClass))
             {
                 broadcast(PROBE_ECU);
                 return;
             }
 
             Constructor<? extends MSECUInterface> constructor;
             try
             {
                 constructor = ecuClass.getConstructor(MSControllerInterface.class, MSUtilsInterface.class, GaugeRegisterInterface.class);
 
                 ecuImplementation = constructor.newInstance(Megasquirt.this, MSUtils.INSTANCE, GaugeRegister.INSTANCE);
 
                 if (!signature.equals(ecuImplementation.getSignature()))
                 {
                     trueSignature = ecuImplementation.getSignature();
 
                     String msg = "Got unsupported signature from Megasquirt \"" + trueSignature + "\" but found a similar supported signature \"" + signature + "\"";
 
                     sendToastMessage(msg);
                     DebugLogManager.INSTANCE.log(msg, Log.INFO);
                 }
                 sendMessage("Found " + trueSignature);
 
             }
             catch (Exception e)
             {
                 DebugLogManager.INSTANCE.logException(e);
                 broadcast(UNKNOWN_ECU);
             }
             broadcast(PROBE_ECU);
         }
 
         private String getSignature() throws IOException, CRC32Exception
         {
             byte[] bootCommand = { 'X' };
             String lastSuccessfulProbeCommand = ApplicationSettings.INSTANCE.getPref(LAST_PROBE);
             String lastSig = ApplicationSettings.INSTANCE.getPref(LAST_SIG);
 
             if (lastSuccessfulProbeCommand != null && lastSig != null)
             {
                 byte[] probe = lastSuccessfulProbeCommand.getBytes();
                 // We need to loop as a BT adapter can pump crap into the MS at the start which confuses the poor thing.
                 for (int i = 0; i < 3; i++)
                 {
                     byte[] response = ECUConnectionManager.getInstance().writeAndRead(probe, 50, false);
                     try
                     {
                         String sig = processResponse(response);
                         if (lastSig.equals(sig))
                         {
                             return sig;
                         }
                     }
                     catch (BootException e)
                     {
                         response = ECUConnectionManager.getInstance().writeAndRead(bootCommand, 500, false);
                     }
                 }
             }
             String probeCommand1 = "Q";
             String probeCommand2 = "S";
             String probeUsed;
             int i = 0;
             String sig = UNKNOWN;
 
             // IF we don't get it in 20 goes, we're not talking to a Megasquirt
             while (i++ < 20)
             {
                 probeUsed = probeCommand1;
                 byte[] response = ECUConnectionManager.getInstance().writeAndRead(probeUsed.getBytes(), 500, false);
 
                 try
                 {
                     if (response != null && response.length > 1)
                     {
                         sig = processResponse(response);
                     }
                     else
                     {
                         probeUsed = probeCommand2;
                         response = ECUConnectionManager.getInstance().writeAndRead(probeUsed.getBytes(), 500, false);
                         if (response != null && response.length > 1)
                         {
                             sig = processResponse(response);
                         }
                     }
                     if (!UNKNOWN.equals(sig))
                     {
                         ApplicationSettings.INSTANCE.setPref(LAST_PROBE, probeUsed);
                         ApplicationSettings.INSTANCE.setPref(LAST_SIG, sig);
                         ECUConnectionManager.getInstance().flushAll();
                         break;
                     }
                 }
                 catch (BootException e)
                 {
                     /*
                      * My ECU also occasionally goes to a Boot> prompt on start up (dodgy electrics) so if we see that, force the ECU to start.
                      */
                     response = ECUConnectionManager.getInstance().writeAndRead(bootCommand, 500, false);
                 }
             }
 
             return sig;
         }
 
         /**
          * Attempt to figure out the data we got back from the device
          * 
          * @param response
          * @return
          * @throws BootException
          */
         private String processResponse(byte[] response) throws BootException
         {
             String result = new String(response);
             trueSignature = result;
             if (result.contains("Boot>"))
             {
                 throw new BootException();
             }
 
             if (response == null)
                 return UNKNOWN;
 
             // Early ECUs only respond with one byte
             if (response.length == 1 && response[0] != 20)
                 return UNKNOWN;
 
             if (response.length <= 1)
                 return UNKNOWN;
 
             // Examine the first few bytes and see if it smells of one of the things an MS may say to us.
             if ((response[0] != 'M' && response[0] != 'J') || (response[1] != 'S' && response[1] != 'o' && response[1] != 'i'))
                 return UNKNOWN;
 
             // Looks like we have a Megasquirt
             return result;
         }
 
         /**
          * Called by other threads to stop the comms
          */
         public void halt()
         {
             DebugLogManager.INSTANCE.log("ECUThread.halt()", Log.INFO);
 
             running = false;
         }
 
         /**
          * Get the current variables from the ECU
          * 
          * @throws IOException
          * @throws CRC32Exception
          */
         private byte[] getRuntimeVars() throws IOException, CRC32Exception
         {
             byte[] buffer = new byte[ecuImplementation.getBlockSize()];
             if (simulated)
             {
                 MSSimulator.INSTANCE.getNextRTV(buffer);
                 return buffer;
             }
             
             // Make sure there is nothing in the buffer
             ECUConnectionManager.getInstance().flushAll();
             
             int d = ecuImplementation.getInterWriteDelay();
             ECUConnectionManager.getInstance().writeAndRead(ecuImplementation.getOchCommand(), buffer, d, ecuImplementation.isCRC32Protocol());
             return buffer;
         }
 
         /**
          * Read a page of constants from the ECU into a byte buffer. MS1 uses a select/read combo, MS2 just does a read
          * 
          * @param pageBuffer
          * @param pageSelectCommand
          * @param pageReadCommand
          * @throws IOException
          */
         protected void getPage(byte[] pageBuffer, byte[] pageSelectCommand, byte[] pageReadCommand) throws IOException, CRC32Exception
         {
             ECUConnectionManager.getInstance().flushAll();
             int delay = ecuImplementation.getPageActivationDelay();
             if (pageSelectCommand != null)
             {
                 ECUConnectionManager.getInstance().writeCommand(pageSelectCommand, delay, ecuImplementation.isCRC32Protocol());
             }
             if (pageReadCommand != null)
             {
                 ECUConnectionManager.getInstance().writeCommand(pageReadCommand, delay, ecuImplementation.isCRC32Protocol());
             }
             ECUConnectionManager.getInstance().readBytes(pageBuffer, ecuImplementation.isCRC32Protocol());
         }
     }
 
     /**
      * 
      * @return
      */
     public String getTrueSignature()
     {
         return trueSignature;
     }
 
     /**
      * 
      * @return
      */
     public boolean isRunning()
     {
         return running;
     }
 
     /**
      * helper method for subclasses
      * 
      * @param pageNo
      * @param pageOffset
      * @param pageSize
      * @param select
      * @param read
      * @return
      */
     public byte[] loadPage(int pageNo, int pageOffset, int pageSize, byte[] select, byte[] read)
     {
 
         byte[] buffer = new byte[pageSize];
         try
         {
             sendMessage("Loading constants from page " + pageNo);
             getPage(buffer, select, read);
             savePage(pageNo, buffer);
             sendMessage("Constants loaded from page " + pageNo);
         }
         catch (IOException e)
         {
             e.printStackTrace();
             DebugLogManager.INSTANCE.logException(e);
             sendMessage("Error loading constants from page " + pageNo);
         }
         catch (CRC32Exception e)
         {
             e.printStackTrace();
             DebugLogManager.INSTANCE.logException(e);
             sendMessage("Error loading constants from page " + pageNo);
         }
         return buffer;
     }
 
     /**
      * 
      * @param buffer
      * @param select
      * @param read
      * @throws IOException
      */
     private void getPage(byte[] buffer, byte[] select, byte[] read) throws IOException, CRC32Exception
     {
         ecuThread.getPage(buffer, select, read);
     }
 
     /**
      * Dumps a loaded page to SD card for analysis
      * 
      * @param pageNo
      * @param buffer
      */
     private void savePage(int pageNo, byte[] buffer)
     {
 
         try
         {
             File dir = new File(Environment.getExternalStorageDirectory(), "MSLogger");
 
             if (!dir.exists())
             {
                 boolean mkDirs = dir.mkdirs();
                 if (!mkDirs)
                 {
                     DebugLogManager.INSTANCE.log("Unable to create directory MSLogger at " + Environment.getExternalStorageDirectory(), Log.ERROR);
                 }
             }
 
             String fileName = ecuImplementation.getClass().getName() + ".firmware";
             File outputFile = new File(dir, fileName);
             BufferedOutputStream out = null;
             try
             {
                 boolean append = !(pageNo == 1);
                 out = new BufferedOutputStream(new FileOutputStream(outputFile, append));
                 DebugLogManager.INSTANCE.log("Saving page " + pageNo + " append=" + append, Log.INFO);
                 out.write(buffer);
             }
             finally
             {
                 if (out != null)
                 {
                     out.flush();
                     out.close();
                 }
             }
         }
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
     }
 
     /**
      * Write a constant back to the ECU
      * 
      * @param constant The constant to write
      */
     public void writeConstant(Constant constant)
     {
         List<String> pageIdentifiers = ecuImplementation.getPageIdentifiers();
         List<String> pageValueWrites = ecuImplementation.getPageValueWrites();
 
         // Ex: U08, S16
         String type = constant.getType();
 
         // 8 bits = 1 byte by default
         int size = 1;
         if (type.contains("16"))
         {
             size = 2; // 16 bits = 2 bytes
         }
 
         int pageNo = constant.getPage();
         int offset = constant.getOffset();
 
         double scale = constant.getScale();
         double translate = constant.getTranslate();
 
         int[] msValue = null;
 
         // Constant to write is of type scalar or bits
         if (constant.getClassType().equals("scalar") || constant.getClassType().equals("bits"))
         {
             double userValue = getField(constant.getName());
 
             msValue = new int[1];
             msValue[0] = (int) (userValue / scale - translate);
         }
         // Constant to write to ECU is of type array
         else if (constant.getClassType().equals("array"))
         {
             int shape[] = MSUtilsShared.getArraySize(constant.getShape());
 
             int width = shape[0];
             int height = shape[1];
 
             // Vector
             if (height == -1)
             {
                 size *= width;
 
                 double[] vector = getVector(constant.getName());
                 msValue = new int[vector.length];
 
                 for (int x = 0; x < width; x++)
                 {
                     // Switch from user value to MS value
                     msValue[x] = (int) (vector[x] / scale - translate);
                 }
             }
             // Array
             else
             {
                 double[][] array = getArray(constant.getName());
                 int i = 0;
 
                 size *= width * height;
                 msValue = new int[width * height];
 
                 for (int y = 0; y < height; y++)
                 {
                     for (int x = 0; x < width; x++)
                     {
                         // Switch from user value to MS value
                         msValue[i++] = (int) (array[x][y] / scale - translate);
                     }
                 }
 
             }
         }
 
         // Make sure we have something to send to the MS
         if (msValue != null && msValue.length > 0)
         {
             String writeCommand = pageValueWrites.get(pageNo - 1);
             String command = MSUtilsShared.HexStringToBytes(pageIdentifiers, writeCommand, offset, size, msValue, pageNo);
             byte[] byteCommand = MSUtils.INSTANCE.commandStringtoByteArray(command);
 
             DebugLogManager.INSTANCE.log("Writing to MS: command: " + command + " constant: " + constant.getName() + " msValue: " + Arrays.toString(msValue) + " pageValueWrite: " + writeCommand + " offset: " + offset + " count: "
                     + size + " pageNo: " + pageNo, Log.DEBUG);
 
             List<byte[]> pageActivates = ecuImplementation.getPageActivates();
 
             try
             {
                 int delay = ecuImplementation.getPageActivationDelay();
 
                 // MS1 use page select command
                 if (pageActivates.size() >= pageNo)
                 {
                     byte[] pageSelectCommand = pageActivates.get(pageNo - 1);
                     ECUConnectionManager.getInstance().writeCommand(pageSelectCommand, delay, ecuImplementation.isCRC32Protocol());
                 }
                 
                 InjectedCommand writeToRAM = new InjectedCommand(byteCommand, 300, false, 0);
                 injectCommand(writeToRAM);
 
                 Toast.makeText(this, "Writing constant " + constant.getName() + " to MegaSquirt", Toast.LENGTH_SHORT).show();
             }
             catch (IOException e)
             {
                 DebugLogManager.INSTANCE.logException(e);
             }
 
             burnPage(pageNo);
         }
         // Nothing to send to the MS, maybe unsupported constant type ?
         else
         {
             DebugLogManager.INSTANCE.log("Couldn't find any value to write, maybe unsupported constant type " + constant.getType(), Log.DEBUG);
         }
     }
 
     /**
      * Burn a page from MS RAM to Flash
      * 
      * @param pageNo The page number to burn
      */
     private void burnPage(int pageNo)
     {
         // Convert from page to table index that the ECU understand
         List<String> pageIdentifiers = ecuImplementation.getPageIdentifiers();
 
        String pageIdentifier = pageIdentifiers.get(pageNo - 1).replace("\\$tsCanId\\", "");
 
         byte tblIdx = (byte) MSUtilsShared.HexByteToDec(pageIdentifier);
 
         DebugLogManager.INSTANCE.log("Burning page " + pageNo + " (Page identifier: " + pageIdentifier + " - Table index: " + tblIdx + ")", Log.DEBUG);
 
         // Send "b" command for the tblIdx
         InjectedCommand burnToFlash = new InjectedCommand(new byte[] { 98, 0, tblIdx }, 300, true, Megasquirt.BURN_DATA);
         injectCommand(burnToFlash);
         
         Toast.makeText(this, "Burning page " + pageNo + " to MegaSquirt", Toast.LENGTH_SHORT).show();
     }
     
     /**
      * Get an array from the ECU
      * 
      * @param channelName The variable name to modify
      * @return
      */
     public double[][] getArray(String channelName)
     {
         double[][] value = { { 0 }, { 0 } };
         Class<?> c = ecuImplementation.getClass();
         try
         {
             Field f = c.getDeclaredField(channelName);
             value = (double[][]) f.get(ecuImplementation);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get array value for " + channelName, Log.ERROR);
         }
         return value;
     }
 
     /**
      * Get a vector from the ECU
      * 
      * @param channelName The variable name to modify
      * @return
      */
     public double[] getVector(String channelName)
     {
         double[] value = { 0 };
         Class<?> c = ecuImplementation.getClass();
         try
         {
             Field f = c.getDeclaredField(channelName);
             value = (double[]) f.get(ecuImplementation);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get vector value for " + channelName, Log.ERROR);
         }
         return value;
     }
 
     /**
      * Set a field in the ECU class
      * 
      * @param channelName The variable name to modify
      * @return
      */
     public double getField(String channelName)
     {
         double value = 0;
         Class<?> c = ecuImplementation.getClass();
         try
         {
             Field f = c.getDeclaredField(channelName);
             value = f.getDouble(ecuImplementation);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get value for " + channelName, Log.ERROR);
         }
         return value;
     }
 
     /**
      * 
      * @param channelName
      * @param value
      */
     public void setField(String channelName, double value)
     {
         Class<?> c = ecuImplementation.getClass();
 
         try
         {
             Field f = c.getDeclaredField(channelName);
 
             if (f.getType().toString().equals("int"))
             {
                 f.setInt(ecuImplementation, (int) value);
             }
             else
             {
                 f.setDouble(ecuImplementation, value);
             }
         }
         catch (NoSuchFieldException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", no such field", Log.ERROR);
         }
         catch (IllegalArgumentException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", illegal argument", Log.ERROR);
         }
         catch (IllegalAccessException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", illegal access", Log.ERROR);
         }
     }
 
     /**
      * Set a vector in the ECU class
      * 
      * @param channelName The variable name to modify
      * @param double[]
      * @return
      */
     public void setVector(String channelName, double[] value)
     {
         Class<?> c = ecuImplementation.getClass();
 
         try
         {
             Field f = c.getDeclaredField(channelName);
             f.set(ecuImplementation, value);
         }
         catch (NoSuchFieldException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", no such field", Log.ERROR);
         }
         catch (IllegalArgumentException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", illegal argument", Log.ERROR);
         }
         catch (IllegalAccessException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", illegal access", Log.ERROR);
         }
     }
     
     /**
      * Set an array in the ECU class
      * 
      * @param channelName The variable name to modify
      * @param double[][]
      * @return
      * 
      */
     public void setArray(String channelName, double[][] value)
     {
         Class<?> c = ecuImplementation.getClass();
 
         try
         {
             Field f = c.getDeclaredField(channelName);
             f.set(ecuImplementation, value);
         }
         catch (NoSuchFieldException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", no such field", Log.ERROR);
         }
         catch (IllegalArgumentException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", illegal argument", Log.ERROR);
         }
         catch (IllegalAccessException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName  + ", illegal access", Log.ERROR);
         }
     }
     
     /**
      * Round a double number to a specific number of decimals
      * 
      * @param number The number to round
      * @param decimals The number of decimals to keep
      * 
      * @return The rounded number
      */
     public double roundDouble(double number, int decimals)
     {
         double p = (double) Math.pow(10, decimals);
         number = number * p;
         double tmp = Math.round(number);
         return tmp / p;
     }
 
     /**
      * Load a byte array contained in pageBuffer from the specified offset and width
      * 
      * @param pageBuffer The buffer where the byte array is located
      * @param offset The offset where the byte array is located
      * @param width The width of the byte array
      * @param height The height of the byte array
      * @param scale The scale of the data
      * @param translate The translate of the data
      * @param digits Number of digits of the data
      * @param signed Is the data signed ?
      * 
      * @return
      */
     public double[][] loadByteArray(byte[] pageBuffer, int offset, int width, int height, double scale, double translate, int digits, boolean signed)
     {
         double[][] destination = new double[width][height];
         int index = offset;
         for (int y = 0; y < height; y++)
         {
             for (int x = 0; x < width; x++)
             {
                 double value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index) : MSUtils.INSTANCE.getByte(pageBuffer, index);
                 value = (value + translate) * scale;
                 destination[x][y] = this.roundDouble(value, digits);
                 index = index + 1;
             }
         }
         return destination;
     }
 
     /**
      * Load a byte vector contained in pageBuffer from the specified offset and width
      * 
      * @param pageBuffer The buffer where the byte vector is located
      * @param offset The offset where the byte vector is located
      * @param width The width of the byte vector
      * @param scale The scale of the data
      * @param translate The translate of the data
      * @param digits Number of digits of the data
      * @param signed Is the data signed ?
      * 
      * @return
      */
     public double[] loadByteVector(byte[] pageBuffer, int offset, int width, double scale, double translate, int digits, boolean signed)
     {
         double[] destination = new double[width];
         int index = offset;
         for (int x = 0; x < width; x++)
         {
             double value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index) : MSUtils.INSTANCE.getByte(pageBuffer, index);
             value = (value + translate) * scale;
             destination[x] = this.roundDouble(value, digits);
             index = index + 1;
         }
 
         return destination;
     }
 
     /**
      * Load a word array contained in pageBuffer from the specified offset and width
      * 
      * @param pageBuffer The buffer where the word array is located
      * @param offset The offset where the word array is located
      * @param width The width of the word array
      * @param height The height of the word array
      * @param scale The scale of the data
      * @param translate The translate of the data
      * @param digits Number of digits of the data
      * @param signed Is the data signed ?
      * 
      * @return
      */
     public double[][] loadWordArray(byte[] pageBuffer, int offset, int width, int height, double scale, double translate, int digits, boolean signed)
     {
         double[][] destination = new double[width][height];
         int index = offset;
         for (int y = 0; y < height; y++)
         {
             for (int x = 0; x < width; x++)
             {
                 double value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index) : MSUtils.INSTANCE.getWord(pageBuffer, index);
                 value = (value + translate) * scale;
                 destination[x][y] = this.roundDouble(value, digits);
                 index = index + 2;
             }
         }
 
         return destination;
     }
 
     /**
      * Load a word vector contained in pageBuffer from the specified offset and width
      * 
      * @param pageBuffer The buffer where the word vector is located
      * @param offset The offset where the word vector is located
      * @param width The width of the word vector
      * @param scale The scale of the data
      * @param translate The translate of the data
      * @param digits Number of digits of the data
      * @param signed Is the data signed ?
      * 
      * @return
      */
     public double[] loadWordVector(byte[] pageBuffer, int offset, int width, double scale, double translate, int digits, boolean signed)
     {
         double[] destination = new double[width];
         int index = offset;
         for (int x = 0; x < width; x++)
         {
             double value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index) : MSUtils.INSTANCE.getWord(pageBuffer, index);
             value = (value + translate) * scale;
             destination[x] = this.roundDouble(value, digits);
             index = index + 2;
         }
 
         return destination;
 
     }
 
     /**
      * Helper function to know if a constant name exists
      * 
      * @param name The name of the constant
      * @return true if the constant exists, false otherwise
      */
     public boolean isConstantExists(String name)
     {
         return MSECUInterface.constants.containsKey(name);
     }
 
     /**
      * Get a constant from the ECU class
      * 
      * @param name The name of the constant
      * @return The constant object
      */
     public Constant getConstantByName(String name)
     {
         return MSECUInterface.constants.get(name);
     }
 
     /**
      * Get an output channel from the ECU class
      * 
      * @param name The name of the output channel
      * @return The output channel object
      */
     public OutputChannel getOutputChannelByName(String name)
     {
         return MSECUInterface.outputChannels.get(name);
     }
 
     /**
      * Get a table editor from the ECU class
      * 
      * @param name The name of the table editor object
      * @return The table editor object
      */
     public TableEditor getTableEditorByName(String name)
     {
         return MSECUInterface.tableEditors.get(name);
     }
 
     /**
      * Get a curve editor from the ECU class
      * 
      * @param name The name of the curve editor object
      * @return The curve editor object
      */
     public CurveEditor getCurveEditorByName(String name)
     {
         return MSECUInterface.curveEditors.get(name);
     }
 
     /**
      * Get a list of menus from the ECU class
      * 
      * @param name The name of the menu tree
      * @return A list of menus object
      */
     public List<Menu> getMenusForDialog(String name)
     {
         return MSECUInterface.menus.get(name);
     }
 
     /**
      * Get a dialog from the ECU class
      * 
      * @param name The name of the dialog object
      * @return The dialog object
      */
     public MSDialog getDialogByName(String name)
     {
         return MSECUInterface.dialogs.get(name);
     }
 
     /**
      * Get a visibility flag for a user defined (dialog, field, panel, etc)
      * Used for field in dialog, for example
      * 
      * @param name The name of the user defined flag
      * @return true if visible, false otherwise
      */
     public boolean getUserDefinedVisibilityFlagsByName(String name)
     {
         if (MSECUInterface.userDefinedVisibilityFlags.containsKey(name))
         {
             return MSECUInterface.userDefinedVisibilityFlags.get(name);
         }
 
         return true;
     }
 
     /**
      * Get a visibility flag for a menu
      * 
      * @param name The name of the menu flag
      * @return true if visible, false otherwise
      */
     public boolean getMenuVisibilityFlagsByName(String name)
     {
         return MSECUInterface.menuVisibilityFlags.get(name);
     }
 
     /**
      * Add a dialog to the list of dialogs in the ECU class
      * 
      * @param dialog The dialog object to add
      */
     public void addDialog(MSDialog dialog)
     {
         MSECUInterface.dialogs.put(dialog.getName(), dialog);
     }
 
     /**
      * Add a curve to the list of curves in the ECU class
      * 
      * @param curve The curve object to add
      */
     public void addCurve(CurveEditor curve)
     {
         MSECUInterface.curveEditors.put(curve.getName(), curve);
     }
 
     /**
      * Add a constant to the list of constants in the ECU class
      * 
      * @param constant The constant object to add
      */
     public void addConstant(Constant constant)
     {
         MSECUInterface.constants.put(constant.getName(), constant);
     }
 
     /**
      * Used to get a list of all constants name used in a specific dialog
      * 
      * @param dialog The dialog to get the list of constants name
      * @return A list of constants name
      */
     public List<String> getAllConstantsNamesForDialog(MSDialog dialog)
     {
         List<String> constants = new ArrayList<String>();
         return buildListOfConstants(constants, dialog);
     }
 
     /**
      * Helper function for getAllConstantsNamesForDialog() which builds the array of constants name
      * 
      * @param constants
      * @param dialog
      */
     private List<String> buildListOfConstants(List<String> constants, MSDialog dialog)
     {
         for (DialogField df : dialog.getFieldsList())
         {
             if (!df.getName().equals("null"))
             {
                 constants.add(df.getName());
             }
         }
 
         for (DialogPanel dp : dialog.getPanelsList())
         {
             MSDialog dialogPanel = this.getDialogByName(dp.getName());
 
             if (dialogPanel != null)
             {
                 buildListOfConstants(constants, dialogPanel);
             }
         }
 
         return constants;
     }
 
     /**
      * 
      * @return
      */
     public String[] defaultGauges()
     {
         return ecuImplementation.defaultGauges();
     }
 
     /**
      * 
      * @return
      */
     public int getBlockSize()
     {
         return ecuImplementation.getBlockSize();
     }
 
     /**
      * 
      * @return
      */
     public int getCurrentTPS()
     {
         return ecuImplementation.getCurrentTPS();
     }
 
     /**
      * 
      * @return
      */
     public String getLogHeader()
     {
         return ecuImplementation.getLogHeader();
     }
 
     /**
      * 
      */
     public void initGauges()
     {
         ecuImplementation.initGauges();
     }
 
     /**
      * 
      */
     public void refreshFlags()
     {
         ecuImplementation.refreshFlags();
     }
 
     /**
      * 
      */
     public void setMenuVisibilityFlags()
     {
         ecuImplementation.setMenuVisibilityFlags();
     }
 
     /**
      * 
      */
     public void setUserDefinedVisibilityFlags()
     {
         ecuImplementation.setUserDefinedVisibilityFlags();
 
     }
 
     /**
      * 
      * @return
      */
     public String[] getControlFlags()
     {
         return ecuImplementation.getControlFlags();
     }
 
     /**
      * 
      * @return
      */
     public List<String> getRequiresPowerCycle()
     {
         return ecuImplementation.getRequiresPowerCycle();
     }
 
     public List<SettingGroup> getSettingGroups()
     {
         ecuImplementation.createSettingGroups();
         return ecuImplementation.getSettingGroups();
     }
 
     /**
      * Helper functions to get specific value out of ECU Different MS version have different name for the same thing so get the right one depending on
      * the MS version we're connected to
      */
 
     /**
      * @return Return the current ECU cylinders count
      */
     public int getCylindersCount()
     {
         return (int) (isConstantExists("nCylinders") ? getField("nCylinders") : getField("nCylinders1"));
     }
 
     /**
      * @return Return the current ECU injectors count
      */
     public int getInjectorsCount()
     {
         return (int) (isConstantExists("nInjectors") ? getField("nInjectors") : getField("nInjectors1"));
     }
 
     /**
      * @return Return the current ECU divider
      */
     public int getDivider()
     {
         return (int) (isConstantExists("divider") ? getField("divider") : getField("divider1"));
     }
 
     /**
      * Return the current ECU injector staging
      * 
      * @return 0 = Simultaneous, 1 = Alternating
      */
     public int getInjectorStating()
     {
         return (int) (isConstantExists("alternate") ? getField("alternate") : getField("alternate1"));
     }
 
 }
