 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.*;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.text.DecimalFormat;
 import java.util.*;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import uk.org.smithfamily.mslogger.*;
 import uk.org.smithfamily.mslogger.activity.MSLoggerActivity;
 import uk.org.smithfamily.mslogger.comms.CRC32Exception;
 import uk.org.smithfamily.mslogger.comms.ECUConnectionManager;
 import uk.org.smithfamily.mslogger.ecuDef.gen.ECURegistry;
 import uk.org.smithfamily.mslogger.log.*;
 import android.app.*;
 import android.content.*;
 import android.os.*;
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
 
     private volatile State currentState = State.DISCONNECTED;
 
     private NotificationManager notifications;
     private MSECUInterface ecuImplementation;
 
     private final boolean simulated = false;
     public static final String CONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
     public static final String DISCONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
     public static final String NEW_DATA = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
     public static final String UNKNOWN_ECU = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.UNKNOWN_ECU";
     public static final String UNKNOWN_ECU_BT = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.UNKNOWN_ECU_BT";
     public static final String PROBE_ECU = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.ECU_PROBED";
     public static final String INJECTED_COMMAND_RESULTS = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.INJECTED_COMMAND_RESULTS";
     public static final String INJECTED_COMMAND_RESULT_ID = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.INJECTED_COMMAND_RESULTS_ID";
     public static final String INJECTED_COMMAND_RESULT_DATA = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.INJECTED_COMMAND_RESULTS_DATA";
 
     private static final String UNKNOWN = "UNKNOWN";
     private static final String LAST_SIG = "LAST_SIG";
     private static final String LAST_PROBE = "LAST_PROBE";
     private static final int NOTIFICATION_ID = 0;
 
     final DecimalFormat decimalFormat3dp = new DecimalFormat("#.000");
     final DecimalFormat decimalFormat1dp = new DecimalFormat("#.0");
 
     private BroadcastReceiver yourReceiver;
 
     // Used by broadcast receiver
     public static final int CONTROLLER_COMMAND = 1;
 
     public static final int BURN_DATA = 10;
 
     public static final int MS3_SD_CARD_STATUS_WRITE = 50;
     public static final int MS3_SD_CARD_STATUS_READ = 51;
     public static final int MS3_SD_CARD_RESET_AND_GO = 52;
     public static final int MS3_SD_CARD_RESET_AND_WAIT = 53;
     public static final int MS3_SD_CARD_STOP_LOGGING = 54;
     public static final int MS3_SD_CARD_START_LOGGING = 55;
     public static final int MS3_SD_CARD_REINITIALISE_CARD = 56;
     public static final int MS3_SD_CARD_READ_DIRECTORY_WRITE = 57;
     public static final int MS3_SD_CARD_READ_DIRECTORY_READ = 58;
     public static final int MS3_SD_CARD_READ_STREAM = 59;
     public static final int MS3_SD_CARD_READ_RTC_WRITE = 60;
     public static final int MS3_SD_CARD_READ_RTC_READ = 61;
 
     private boolean constantsLoaded;
     private String trueSignature = "Unknown";
     private volatile ECUThread ecuThread;
     private static volatile ECUThread watch;
     private long logStart = 0;
 
     public class LocalBinder extends Binder
     {
         public Megasquirt getService()
         {
             return Megasquirt.this;
         }
     }
 
     @Override
     public int onStartCommand(final Intent intent, final int flags, final int startId)
     {
         DebugLogManager.INSTANCE.log("Megasquirt Received start id " + startId + ": " + intent, Log.VERBOSE);
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     @Override
     public IBinder onBind(final Intent intent)
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
             public void onReceive(final Context context, final Intent intent)
             {
                 final String action = intent.getAction();
 
                 if (action.equals(ApplicationSettings.BT_CHANGED))
                 {
                     DebugLogManager.INSTANCE.log("BT_CHANGED received", Log.VERBOSE);
                     stop();
                     start();
                 }
                 else if (action.equals(Megasquirt.INJECTED_COMMAND_RESULTS))
                 {
                     final int resultId = intent.getIntExtra(Megasquirt.INJECTED_COMMAND_RESULT_ID, 0);
 
                     switch (resultId)
                     {
                     case Megasquirt.BURN_DATA:
                         // Wait til we get some data and flush it
                         try
                         {
                             Thread.sleep(200);
                         }
                         catch (final InterruptedException e)
                         {
                         }
 
                         break;
                     default:
                         break;
                     }
                 }
             }
         };
 
         // Registers the receiver so that your service will listen for broadcasts
         this.registerReceiver(this.yourReceiver, btChangedFilter);
         this.registerReceiver(this.yourReceiver, injectCommandResultsFilter);
 
         final String lastSig = ApplicationSettings.INSTANCE.getPref(LAST_SIG);
         if (lastSig != null)
         {
             setImplementation(lastSig);
         }
 
         ApplicationSettings.INSTANCE.setEcu(this);
         start();
 
         startForeground(NOTIFICATION_ID, null);
     }
 
     private void setState(final State s)
     {
         currentState = s;
         int msgId = R.string.disconnected_from_ms;
         boolean removeNotification = false;
         switch (currentState)
         {
         case DISCONNECTED:
             removeNotification = true;
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
 
         if (removeNotification)
         {
             notifications.cancelAll();
         }
         else
         {
             final CharSequence text = getText(R.string.app_name);
             final Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
             final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MSLoggerActivity.class), 0);
             notification.setLatestEventInfo(this, getText(msgId), text, contentIntent);
             notifications.notify(NOTIFICATION_ID, notification);
         }
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
     protected int table(final int i1, final String name)
     {
         return TableManager.INSTANCE.table(i1, name);
     }
 
     @Override
     public int table(final double d1, final String name)
     {
         return table((int) d1, name);
     }
 
     /**
      * Add a command for the ECUThread to process when it can
      * 
      * @param command
      */
     public void injectCommand(final InjectedCommand command)
     {
         injectionQueue.add(command);
     }
 
     /**
      * @return true if we're connected to an ECU, false otherwise
      */
     public boolean isConnected()
     {
         return (currentState == State.CONNECTED) || (currentState == State.LOGGING);
     }
 
     /**
      * @return true if we're data logging the ECU realtime stream, false otherwise
      */
     public boolean isLogging()
     {
         return currentState == State.LOGGING;
     }
 
     /**
      * Temperature unit conversion function
      * 
      * @param t temp in F
      * @return temp in C if CELSIUS is set, in F otherwise
      */
     @Override
     public double tempCvt(final double t)
     {
         if (isSet("CELSIUS"))
         {
             return ((t - 32.0) * 5.0) / 9.0;
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
             setState(State.DISCONNECTED);
             ecuThread = new ECUThread();
             ecuThread.setPriority(Thread.MAX_PRIORITY);
             ecuThread.start();
         }
     }
 
     /**
      * Shut down the ECU thread
      */
     public synchronized void stop()
     {
         DebugLogManager.INSTANCE.log("Megasquirt.stop()", Log.INFO);
 
         ecuThread = null;
 
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
         notifications.cancelAll();
     }
 
     /**
      * Output the current values to be logged
      */
     private void logValues(final byte[] buffer)
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
         catch (final IOException e)
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
     protected void sendMessage(final String msg)
     {
         broadcast(ApplicationSettings.GENERAL_MESSAGE, msg);
     }
 
     /**
      * Send a toast message to the user
      * 
      * @param message to be sent
      */
     protected void sendToastMessage(final String msg)
     {
         final Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.TOAST);
         broadcast.putExtra(ApplicationSettings.TOAST_MESSAGE, msg);
         sendBroadcast(broadcast);
     }
 
     /**
      * Send the reads per second to be displayed on the screen
      * 
      * @param RPS the current reads per second value
      */
     private void sendRPS(final double RPS)
     {
 
         final Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.RPS_MESSAGE);
         broadcast.putExtra(ApplicationSettings.RPS, decimalFormat1dp.format(RPS));
         sendBroadcast(broadcast);
     }
 
     /**
      * Send a status update to the rest of the application
      * 
      * @param action
      */
     private void broadcast(final String action)
     {
         final Intent broadcast = new Intent();
         broadcast.setAction(action);
         sendBroadcast(broadcast);
     }
 
     private void broadcast(final String action, final String data)
     {
         DebugLogManager.INSTANCE.log("Megasquirt.broadcast(" + action + "," + data + ")", Log.VERBOSE);
         final Intent broadcast = new Intent();
         broadcast.setAction(action);
         broadcast.putExtra(ApplicationSettings.MESSAGE, data);
         sendBroadcast(broadcast);
     }
 
     private void broadcast()
     {
         final Intent broadcast = new Intent();
         broadcast.setAction(NEW_DATA);
 
         sendBroadcast(broadcast);
 
     }
 
     /**
      * How long have we been running?
      * 
      * @return
      */
     @Override
     public double timeNow()
     {
         if (logStart == 0)
         {
             logStart = DatalogManager.INSTANCE.getLogStart();
         }
         final long runTime = System.currentTimeMillis() - logStart;
         final double timeNow = runTime / 1000.0;
 
         return timeNow;
     }
 
     /**
      * Flag the logging process to happen
      */
     public void startLogging()
     {
         if (currentState == State.CONNECTED)
         {
             currentState = State.LOGGING;
             DebugLogManager.INSTANCE.log("startLogging()", Log.INFO);
         }
     }
 
     /**
      * Stop the logging process
      */
     public void stopLogging()
     {
         if (currentState == State.LOGGING)
         {
             DebugLogManager.INSTANCE.log("stopLogging()", Log.INFO);
             currentState = State.CONNECTED;
             FRDLogManager.INSTANCE.close();
             DatalogManager.INSTANCE.close();
         }
     }
 
     /**
      * Take a wild stab at what this does.
      * 
      * @param v
      * @return
      */
     @Override
     public double round(final double v)
     {
         return Math.floor((v * 100) + .5) / 100;
     }
 
     /**
      * Returns if a flag has been set in the application
      * 
      * @param name
      * @return
      */
     @Override
     public boolean isSet(final String name)
     {
         return ApplicationSettings.INSTANCE.isSet(name);
     }
 
     /**
      * The thread that handles all communications with the ECU. This must be done in it's own thread as Android gets very picky about unresponsive UI
      * threads
      */
     private class ECUThread extends Thread
     {
         int interWriteDelay = 0;
 
         private class CalculationThread extends Thread
         {
             private volatile boolean running = true;
 
             public void halt()
             {
                 DebugLogManager.INSTANCE.log("CalculationThread.halt()", Log.INFO);
 
                 running = false;
             }
 
             @Override
             public void run()
             {
                 this.setName("CalculationThread");
                 try
                 {
                     while (running)
                     {
                         final byte[] buffer = handshake.get();
                         if (ecuImplementation != null)
                         {
                             ecuImplementation.calculate(buffer);
                             logValues(buffer);
                             broadcast();
                         }
                     }
                 }
                 catch (final InterruptedException e)
                 {
                     // Swallow, we're on our way out.
                 }
             }
 
         }
 
         class Handshake
         {
             private byte[] buffer;
 
             public void put(final byte[] buf)
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
             final String name = "ECUThread:" + System.currentTimeMillis();
             setName(name);
             DebugLogManager.INSTANCE.log("Creating ECUThread named " + name, Log.VERBOSE);
 
             interWriteDelay = Integer.parseInt(ApplicationSettings.INSTANCE.getOrSetPref("iwd", "0"));
             calculationThread.start();
 
         }
 
         /**
          * Kick the connection off
          */
         public void initialiseConnection()
         {
             // sendMessage("Launching connection");
 
             // Connection conn = ConnectionFactory.INSTANCE.getConnection();
             final String btAddress = ApplicationSettings.INSTANCE.getECUBluetoothMac();
             ECUConnectionManager.getInstance().init(null, btAddress);
         }
 
         /**
          * The main loop of the connection to the ECU
          */
         @Override
         public void run()
         {
             try
             {
                 setState(Megasquirt.State.CONNECTING);
                 sendMessage("Starting connection");
                 DebugLogManager.INSTANCE.log("BEGIN connectedThread", Log.INFO);
                 initialiseConnection();
 
                 delay(500);
 
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
                         final byte[] bufferRV = getRuntimeVars();
                         ecuImplementation.calculate(bufferRV);
                     }
                     catch (final CRC32Exception e)
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
                     while ((currentState == Megasquirt.State.CONNECTED) || (currentState == Megasquirt.State.LOGGING))
                     {
                         try
                         {
                             if (injectionQueue.peek() != null)
                             {
                                 for (final InjectedCommand i : injectionQueue)
                                 {
                                     processCommand(i);
                                 }
 
                                 injectionQueue.clear();
                             }
                             final byte[] buffer = getRuntimeVars();
                             handshake.put(buffer);
                         }
                         catch (final CRC32Exception e)
                         {
                             DatalogManager.INSTANCE.mark(e.getLocalizedMessage());
                             DebugLogManager.INSTANCE.logException(e);
                         }
                         readCounter++;
 
                         final long delay = System.currentTimeMillis() - lastRpsTime;
                         if (delay > 1000)
                         {
                             final double RPS = (readCounter / delay) * 1000;
                             readCounter = 0;
                             lastRpsTime = System.currentTimeMillis();
 
                             if (RPS > 0)
                             {
                                 sendRPS(RPS);
                             }
                         }
 
                     }
                 }
                 catch (final IOException e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
                 catch (final CRC32Exception e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
                 catch (final ArithmeticException e)
                 {
                     // If we get a maths error, we probably have loaded duff constants and hit a divide by zero
                     // force the constants to reload in case it was just a bad data read
                     DebugLogManager.INSTANCE.logException(e);
                     constantsLoaded = false;
                 }
                 catch (final RuntimeException t)
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
 
         private void processCommand(final InjectedCommand i) throws IOException
         {
             ECUConnectionManager.getInstance().writeCommand(i.getCommand(), i.getDelay(), ecuImplementation.isCRC32Protocol());
 
             // If we want to get the result back
             if (i.isReturnResult())
             {
                 final Intent broadcast = new Intent();
                 broadcast.setAction(INJECTED_COMMAND_RESULTS);
 
                 final byte[] result = ECUConnectionManager.getInstance().readBytes();
 
                 broadcast.putExtra(INJECTED_COMMAND_RESULT_ID, i.getResultId());
                 broadcast.putExtra(INJECTED_COMMAND_RESULT_DATA, result);
 
                 sendBroadcast(broadcast);
             }
         }
 
         private void initialiseImplementation() throws IOException, CRC32Exception
         {
             sendMessage("Checking your ECU");
             final String signature = getSignature();
 
             setImplementation(signature);
         }
 
         private String getSignature() throws IOException, CRC32Exception
         {
             final byte[] bootCommand = { 'X' };
             final String lastSuccessfulProbeCommand = ApplicationSettings.INSTANCE.getPref(LAST_PROBE);
             final String lastSig = ApplicationSettings.INSTANCE.getPref(LAST_SIG);
 
             if ((lastSuccessfulProbeCommand != null) && (lastSig != null))
             {
                 final byte[] probe = lastSuccessfulProbeCommand.getBytes();
                 // We need to loop as a BT adapter can pump crap into the MS at the start which confuses the poor thing.
                 for (int i = 0; i < 3; i++)
                 {
                     byte[] response = ECUConnectionManager.getInstance().writeAndRead(probe, 50, false);
                     try
                     {
                         final String sig = processResponse(response);
                         if (lastSig.equals(sig))
                         {
                             return sig;
                         }
                     }
                     catch (final BootException e)
                     {
                         response = ECUConnectionManager.getInstance().writeAndRead(bootCommand, 500, false);
                     }
                 }
             }
             final String probeCommand1 = "Q";
             final String probeCommand2 = "S";
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
                     if ((response != null) && (response.length > 1))
                     {
                         sig = processResponse(response);
                     }
                     else
                     {
                         probeUsed = probeCommand2;
                         response = ECUConnectionManager.getInstance().writeAndRead(probeUsed.getBytes(), 500, false);
                         if ((response != null) && (response.length > 1))
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
                 catch (final BootException e)
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
         private String processResponse(final byte[] response) throws BootException
         {
             final String result = new String(response);
             trueSignature = result;
             if (result.contains("Boot>"))
             {
                 throw new BootException();
             }
 
             if (response == null)
             {
                 return UNKNOWN;
             }
 
             // Early ECUs only respond with one byte
             if ((response.length == 1) && (response[0] != 20))
             {
                 return UNKNOWN;
             }
 
             if (response.length <= 1)
             {
                 return UNKNOWN;
             }
 
             // Examine the first few bytes and see if it smells of one of the things an MS may say to us.
             if (((response[0] != 'M') && (response[0] != 'J')) || ((response[1] != 'S') && (response[1] != 'o') && (response[1] != 'i')))
             {
                 return UNKNOWN;
             }
 
             // Looks like we have a Megasquirt
             return result;
         }
 
         /**
          * Get the current variables from the ECU
          * 
          * @throws IOException
          * @throws CRC32Exception
          */
         private byte[] getRuntimeVars() throws IOException, CRC32Exception
         {
             if (interWriteDelay > 0)
             {
                 delay(interWriteDelay);
             }
             final byte[] buffer = new byte[ecuImplementation.getBlockSize()];
             if (simulated)
             {
                 MSSimulator.INSTANCE.getNextRTV(buffer);
                 return buffer;
             }
 
             final int delay = interWriteDelay == 0 ? ecuImplementation.getInterWriteDelay() : interWriteDelay;
             ECUConnectionManager.getInstance().writeAndRead(ecuImplementation.getOchCommand(), buffer, delay, ecuImplementation.isCRC32Protocol());
             return buffer;
         }
 
         private void delay(final int delay)
         {
             try
             {
                 Thread.sleep(delay);
             }
             catch (final InterruptedException e)
             {
                 DebugLogManager.INSTANCE.logException(e);
             }
         }
 
         /**
          * Read a page of constants from the ECU into a byte buffer. MS1 uses a select/read combo, MS2 just does a read
          * 
          * @param pageBuffer
          * @param pageSelectCommand
          * @param pageReadCommand
          * @throws IOException
          */
         protected void getPage(final byte[] pageBuffer, final byte[] pageSelectCommand, final byte[] pageReadCommand) throws IOException, CRC32Exception
         {
             ECUConnectionManager.getInstance().flushAll();
             final int delay = ecuImplementation.getPageActivationDelay();
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
      * helper method for subclasses
      * 
      * @param pageNo
      * @param pageOffset
      * @param pageSize
      * @param select
      * @param read
      * @return
      */
     @Override
     public byte[] loadPage(final int pageNo, final int pageOffset, final int pageSize, final byte[] select, final byte[] read)
     {
 
         final byte[] buffer = new byte[pageSize];
         try
         {
             sendMessage("Loading constants from page " + pageNo);
             getPage(buffer, select, read);
             savePage(pageNo, buffer);
             sendMessage("Constants loaded from page " + pageNo);
         }
         catch (final IOException e)
         {
             e.printStackTrace();
             DebugLogManager.INSTANCE.logException(e);
             sendMessage("Error loading constants from page " + pageNo);
         }
         catch (final CRC32Exception e)
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
     private void getPage(final byte[] buffer, final byte[] select, final byte[] read) throws IOException, CRC32Exception
     {
         ecuThread.getPage(buffer, select, read);
     }
 
     /**
      * Dumps a loaded page to SD card for analysis
      * 
      * @param pageNo
      * @param buffer
      */
     private void savePage(final int pageNo, final byte[] buffer)
     {
 
         try
         {
             final File dir = new File(Environment.getExternalStorageDirectory(), "MSLogger");
 
             if (!dir.exists())
             {
                 final boolean mkDirs = dir.mkdirs();
                 if (!mkDirs)
                 {
                     DebugLogManager.INSTANCE.log("Unable to create directory MSLogger at " + Environment.getExternalStorageDirectory(), Log.ERROR);
                 }
             }
 
             final String fileName = ecuImplementation.getClass().getName() + ".firmware";
             final File outputFile = new File(dir, fileName);
             BufferedOutputStream out = null;
             try
             {
                 final boolean append = !(pageNo == 1);
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
         catch (final IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
     }
 
     /**
      * Write a constant back to the ECU
      * 
      * @param constant The constant to write
      */
     public void writeConstant(final Constant constant)
     {
         final List<String> pageIdentifiers = ecuImplementation.getPageIdentifiers();
         final List<String> pageValueWrites = ecuImplementation.getPageValueWrites();
 
         // Ex: U08, S16
         final String type = constant.getType();
 
         // 8 bits = 1 byte by default
         int size = 1;
         if (type.contains("16"))
         {
             size = 2; // 16 bits = 2 bytes
         }
 
         final int pageNo = constant.getPage();
         final int offset = constant.getOffset();
 
         int[] msValue = null;
 
         // Constant to write is of type scalar or bits
         if (constant.getClassType().equals("scalar") || constant.getClassType().equals("bits"))
         {
             msValue = new int[1];
             msValue[0] = (int) getField(constant.getName());
         }
         // Constant to write to ECU is of type array
         else if (constant.getClassType().equals("array"))
         {
             final int shape[] = MSUtilsShared.getArraySize(constant.getShape());
 
             final int width = shape[0];
             final int height = shape[1];
 
             // Vector
             if (height == -1)
             {
                 size *= width;
                 msValue = getVector(constant.getName());
             }
             // Array
             else
             {
                 // Flatten array into msValue
                 final int[][] array = getArray(constant.getName());
                 int i = 0;
 
                 size *= width * height;
                 msValue = new int[width * height];
 
                 for (int y = 0; y < height; y++)
                 {
                     for (int x = 0; x < width; x++)
                     {
                         msValue[i++] = array[x][y];
                     }
                 }
 
             }
         }
 
         // Make sure we have something to send to the MS
         if ((msValue != null) && (msValue.length > 0))
         {
             final String writeCommand = pageValueWrites.get(pageNo - 1);
             final String command = MSUtilsShared.HexStringToBytes(pageIdentifiers, writeCommand, offset, size, msValue, pageNo);
             final byte[] byteCommand = MSUtils.INSTANCE.commandStringtoByteArray(command);
 
             DebugLogManager.INSTANCE.log("Writing to MS: command: " + command + " constant: " + constant.getName() + " msValue: " + Arrays.toString(msValue) + " pageValueWrite: " + writeCommand + " offset: " + offset + " count: " + size
                     + " pageNo: " + pageNo, Log.DEBUG);
 
             final List<byte[]> pageActivates = ecuImplementation.getPageActivates();
 
             try
             {
                 final int delay = ecuImplementation.getPageActivationDelay();
 
                 // MS1 use page select command
                 if (pageActivates.size() >= pageNo)
                 {
                     final byte[] pageSelectCommand = pageActivates.get(pageNo - 1);
                     ECUConnectionManager.getInstance().writeCommand(pageSelectCommand, delay, ecuImplementation.isCRC32Protocol());
                 }
 
                 final InjectedCommand writeToRAM = new InjectedCommand(byteCommand, 300, false, 0);
                 injectCommand(writeToRAM);
 
                 Toast.makeText(this, "Writing constant " + constant.getName() + " to MegaSquirt", Toast.LENGTH_SHORT).show();
             }
             catch (final IOException e)
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
     private void burnPage(final int pageNo)
     {
         // Convert from page to table index that the ECU understand
         final List<String> pageIdentifiers = ecuImplementation.getPageIdentifiers();
 
         final String pageIdentifier = pageIdentifiers.get(pageNo - 1).replace("\\$tsCanId\\", "");
 
         final byte tblIdx = (byte) MSUtilsShared.HexByteToDec(pageIdentifier);
 
         DebugLogManager.INSTANCE.log("Burning page " + pageNo + " (Page identifier: " + pageIdentifier + " - Table index: " + tblIdx + ")", Log.DEBUG);
 
         // Send "b" command for the tblIdx
         final InjectedCommand burnToFlash = new InjectedCommand(new byte[] { 98, 0, tblIdx }, 300, true, Megasquirt.BURN_DATA);
         injectCommand(burnToFlash);
 
         Toast.makeText(this, "Burning page " + pageNo + " to MegaSquirt", Toast.LENGTH_SHORT).show();
     }
 
     /**
      * Get an array from the ECU
      * 
      * @param channelName The variable name to modify
      * @return
      */
     public int[][] getArray(final String channelName)
     {
         int[][] value = { { 0 }, { 0 } };
         final Class<?> c = ecuImplementation.getClass();
         try
         {
             final Field f = c.getDeclaredField(channelName);
             value = (int[][]) f.get(ecuImplementation);
         }
         catch (final Exception e)
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
     public int[] getVector(final String channelName)
     {
         int[] value = { 0 };
         final Class<?> c = ecuImplementation.getClass();
         try
         {
             final Field f = c.getDeclaredField(channelName);
             value = (int[]) f.get(ecuImplementation);
         }
         catch (final Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get vector value for " + channelName, Log.ERROR);
         }
         return value;
     }
 
     /**
      * 
      * @param channelName
      * @param value
      */
     public void setField(final String channelName, final int value)
     {
         final Class<?> c = ecuImplementation.getClass();
 
         try
         {
             final Field f = c.getDeclaredField(channelName);
             f.setInt(ecuImplementation, value);
         }
         catch (final NoSuchFieldException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName + ", no such field", Log.ERROR);
         }
         catch (final IllegalArgumentException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName + ", illegal argument", Log.ERROR);
         }
         catch (final IllegalAccessException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName + ", illegal access", Log.ERROR);
         }
     }
 
     /**
      * Set a vector in the ECU class
      * 
      * @param channelName The variable name to modify
      * @param double[]
      * @return
      */
     public void setVector(final String channelName, final int[] xBins)
     {
         final Class<?> c = ecuImplementation.getClass();
 
         try
         {
             final Field f = c.getDeclaredField(channelName);
             f.set(ecuImplementation, xBins);
         }
         catch (final NoSuchFieldException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + xBins + " for " + channelName + ", no such field", Log.ERROR);
         }
         catch (final IllegalArgumentException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + xBins + " for " + channelName + ", illegal argument", Log.ERROR);
         }
         catch (final IllegalAccessException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + xBins + " for " + channelName + ", illegal access", Log.ERROR);
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
     public void setArray(final String channelName, final int[][] zBins)
     {
         final Class<?> c = ecuImplementation.getClass();
 
         try
         {
             final Field f = c.getDeclaredField(channelName);
             f.set(ecuImplementation, zBins);
         }
         catch (final NoSuchFieldException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + zBins + " for " + channelName + ", no such field", Log.ERROR);
         }
         catch (final IllegalArgumentException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + zBins + " for " + channelName + ", illegal argument", Log.ERROR);
         }
         catch (final IllegalAccessException e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + zBins + " for " + channelName + ", illegal access", Log.ERROR);
         }
     }
 
     /**
      * Load a byte array contained in pageBuffer from the specified offset and width
      * 
      * @param pageBuffer The buffer where the byte array is located
      * @param offset The offset where the byte array is located
      * @param width The width of the byte array
      * @param height The height of the byte array
      * @param signed Is the data signed ?
      * 
      * @return
      */
     @Override
     public int[][] loadByteArray(final byte[] pageBuffer, final int offset, final int width, final int height, final boolean signed)
     {
         final int[][] destination = new int[width][height];
         int index = offset;
         for (int y = 0; y < height; y++)
         {
             for (int x = 0; x < width; x++)
             {
                 final int value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index) : MSUtils.INSTANCE.getByte(pageBuffer, index);
                 destination[x][y] = value;
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
      * @param signed Is the data signed ?
      * 
      * @return
      */
     @Override
     public int[] loadByteVector(final byte[] pageBuffer, final int offset, final int width, final boolean signed)
     {
         final int[] destination = new int[width];
         int index = offset;
         for (int x = 0; x < width; x++)
         {
             final int value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index) : MSUtils.INSTANCE.getByte(pageBuffer, index);
             destination[x] = value;
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
      * @param signed Is the data signed ?
      * 
      * @return
      */
     @Override
     public int[][] loadWordArray(final byte[] pageBuffer, final int offset, final int width, final int height, final boolean signed)
     {
         final int[][] destination = new int[width][height];
         int index = offset;
         for (int y = 0; y < height; y++)
         {
             for (int x = 0; x < width; x++)
             {
                 final int value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index) : MSUtils.INSTANCE.getWord(pageBuffer, index);
                 destination[x][y] = value;
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
      * @param signed Is the data signed ?
      * 
      * @return
      */
     @Override
     public int[] loadWordVector(final byte[] pageBuffer, final int offset, final int width, final boolean signed)
     {
         final int[] destination = new int[width];
         int index = offset;
         for (int x = 0; x < width; x++)
         {
             final int value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index) : MSUtils.INSTANCE.getWord(pageBuffer, index);
             destination[x] = value;
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
     public boolean isConstantExists(final String name)
     {
         return MSECUInterface.constants.containsKey(name);
     }
 
     /**
      * Get a constant from the ECU class
      * 
      * @param name The name of the constant
      * @return The constant object
      */
     public Constant getConstantByName(final String name)
     {
         return MSECUInterface.constants.get(name);
     }
 
     /**
      * Get an output channel from the ECU class
      * 
      * @param name The name of the output channel
      * @return The output channel object
      */
     public OutputChannel getOutputChannelByName(final String name)
     {
         return MSECUInterface.outputChannels.get(name);
     }
 
     /**
      * Get a table editor from the ECU class
      * 
      * @param name The name of the table editor object
      * @return The table editor object
      */
     public TableEditor getTableEditorByName(final String name)
     {
         return MSECUInterface.tableEditors.get(name);
     }
 
     /**
      * Get a curve editor from the ECU class
      * 
      * @param name The name of the curve editor object
      * @return The curve editor object
      */
     public CurveEditor getCurveEditorByName(final String name)
     {
         return MSECUInterface.curveEditors.get(name);
     }
 
     /**
      * Get a list of menus from the ECU class
      * 
      * @param name The name of the menu tree
      * @return A list of menus object
      */
     public List<Menu> getMenusForDialog(final String name)
     {
         return MSECUInterface.menus.get(name);
     }
 
     /**
      * Get a dialog from the ECU class
      * 
      * @param name The name of the dialog object
      * @return The dialog object
      */
     public MSDialog getDialogByName(final String name)
     {
         return MSECUInterface.dialogs.get(name);
     }
 
     /**
      * Get a visibility flag for a user defined (dialog, field, panel, etc) Used for field in dialog, for example
      * 
      * @param name The name of the user defined flag
      * @return true if visible, false otherwise
      */
     public boolean getUserDefinedVisibilityFlagsByName(final String name)
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
     public boolean getMenuVisibilityFlagsByName(final String name)
     {
         if (MSECUInterface.menuVisibilityFlags.containsKey(name))
         {
             return MSECUInterface.menuVisibilityFlags.get(name);
         }
         
         return true;
     }
 
     /**
      * Add a dialog to the list of dialogs in the ECU class
      * 
      * @param dialog The dialog object to add
      */
     public void addDialog(final MSDialog dialog)
     {
         MSECUInterface.dialogs.put(dialog.getName(), dialog);
     }
 
     /**
      * Add a curve to the list of curves in the ECU class
      * 
      * @param curve The curve object to add
      */
     public void addCurve(final CurveEditor curve)
     {
         MSECUInterface.curveEditors.put(curve.getName(), curve);
     }
 
     /**
      * Add a constant to the list of constants in the ECU class
      * 
      * @param constant The constant object to add
      */
     public void addConstant(final Constant constant)
     {
         MSECUInterface.constants.put(constant.getName(), constant);
     }
 
     /**
      * Used to get a list of all constants name used in a specific dialog
      * 
      * @param dialog The dialog to get the list of constants name
      * @return A list of constants name
      */
     public List<String> getAllConstantsNamesForDialog(final MSDialog dialog)
     {
         final List<String> constants = new ArrayList<String>();
         return buildListOfConstants(constants, dialog);
     }
 
     /**
      * Helper function for getAllConstantsNamesForDialog() which builds the array of constants name
      * 
      * @param constants
      * @param dialog
      */
     private List<String> buildListOfConstants(final List<String> constants, final MSDialog dialog)
     {
         for (final DialogField df : dialog.getFieldsList())
         {
             if (!df.getName().equals("null"))
             {
                 constants.add(df.getName());
             }
         }
 
         for (final DialogPanel dp : dialog.getPanelsList())
         {
             final MSDialog dialogPanel = this.getDialogByName(dp.getName());
 
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
 
     public Map<String, String> getControllerCommands()
     {
         ecuImplementation.createControllerCommands();
         return ecuImplementation.getControllerCommands();
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
 
     public double getField(final String channelName)
     {
        return DataManager.getInstance().getField(channelName);
     }
 
     private void setImplementation(final String signature)
     {
         final Class<? extends MSECUInterface> ecuClass = ECURegistry.INSTANCE.findEcu(signature);
 
         if ((ecuImplementation != null) && ecuImplementation.getClass().equals(ecuClass))
         {
             broadcast(PROBE_ECU);
             return;
         }
 
         Constructor<? extends MSECUInterface> constructor;
         try
         {
             constructor = ecuClass.getConstructor(MSControllerInterface.class, MSUtilsInterface.class);
 
             ecuImplementation = constructor.newInstance(Megasquirt.this, MSUtils.INSTANCE);
 
             if (!signature.equals(ecuImplementation.getSignature()))
             {
                 trueSignature = ecuImplementation.getSignature();
 
                 final String msg = "Got unsupported signature from Megasquirt \"" + trueSignature + "\" but found a similar supported signature \"" + signature + "\"";
 
                 sendToastMessage(msg);
                 DebugLogManager.INSTANCE.log(msg, Log.INFO);
             }
             sendMessage("Found " + trueSignature);
 
         }
         catch (final Exception e)
         {
             DebugLogManager.INSTANCE.logException(e);
             broadcast(UNKNOWN_ECU);
         }
         broadcast(PROBE_ECU);
     }
 
     @Override
     public void registerOutputChannel(final OutputChannel o)
     {
         DataManager.getInstance().addOutputChannel(o);
 
     }
 
 }
