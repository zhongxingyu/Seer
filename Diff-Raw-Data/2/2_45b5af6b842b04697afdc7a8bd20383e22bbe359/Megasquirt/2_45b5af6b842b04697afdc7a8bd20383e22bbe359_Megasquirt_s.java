 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.MSLoggerApplication;
 import uk.org.smithfamily.mslogger.comms.CRC32Exception;
 import uk.org.smithfamily.mslogger.comms.ConnectionManager;
 import uk.org.smithfamily.mslogger.log.DatalogManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.log.FRDLogManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * Abstract base class for all ECU implementations
  * 
  * @author dgs
  * 
  */
 public class Megasquirt implements MSControllerInterface
 {
     private MSECUInterface     ecuImplementation;
     
     static Timer               connectionWatcher = new Timer("ConnectionWatcher", true);
 
     private boolean            simulated         = false;
 
     public static final String CONNECTED         = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
     public static final String DISCONNECTED      = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
     public static final String NEW_DATA          = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
     
     protected Context          context;
 
     
     private boolean            logging;
     private boolean            constantsLoaded;
     private String             trueSignature = "Unknown";
     private volatile ECUThread ecuThread;
     private volatile boolean   running;
     private static volatile ECUThread watch;
 
     // protected byte[] ochBuffer;
 
     private RebroadcastHandler handler;
 
     /**
      * Shortcut function to access data tables. Makes the INI->Java translation a little simpler
      * 
      * @param i1
      *            index into table
      * @param name
      *            table name
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
      * 
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
      * @param t
      *            temp in F
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
 
         if (ecuThread == null)
         {
             ecuThread = new ECUThread();
             ecuThread.start();
         }
     }
 
     /**
      * Shut down the ECU thread
      */
     public synchronized void stop()
     {
         if (ecuThread != null)
         {
             ecuThread.halt();
             ecuThread = null;
         }
 
         running = false;
 
         DebugLogManager.INSTANCE.log("Megasquirt.stop()", Log.INFO);
 
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
             // ErrorReporter.getInstance().handleException(e);
             DebugLogManager.INSTANCE.logException(e);
 
             Log.e(ApplicationSettings.TAG, "Megasquirt.logValues()", e);
         }
     }
 
     /**
      * Shutdown the data connection to the MS
      */
     private void disconnect()
     {
         if (simulated)
             return;
         DebugLogManager.INSTANCE.log("Disconnect", Log.INFO);
 
         ConnectionManager.INSTANCE.disconnect();
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
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.GENERAL_MESSAGE);
         broadcast.putExtra(ApplicationSettings.MESSAGE, msg);
         context.sendBroadcast(broadcast);
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
         context.sendBroadcast(broadcast);
     }
 
     /**
      * Send the reads per second to be displayed on the screen
      * 
      * @param RPS
      *            the current reads per second value
      */
     private void sendRPS(double RPS)
     {
         DecimalFormat decimalFormat = new DecimalFormat("#.0");
 
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.RPS_MESSAGE);
         broadcast.putExtra(ApplicationSettings.RPS, decimalFormat.format(RPS));
         context.sendBroadcast(broadcast);
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
         context.sendBroadcast(broadcast);
     }
 
     private void broadcast()
     {
         Intent broadcast = new Intent();
         broadcast.setAction(NEW_DATA);
 
         context.sendBroadcast(broadcast);
 
     }
 
     /**
      * 
      * @param c
      */
     public Megasquirt(Context c)
     {
         this.context = c;
         handler = new RebroadcastHandler(this);
     }
 
     /**
      * How long have we been running?
      * 
      * @return
      */
     public int timeNow()
     {
         return (int) ((System.currentTimeMillis() - DatalogManager.INSTANCE.getLogStart()) / 1000.0);
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
      *
      */
     private static class RebroadcastHandler extends Handler
     {
         private Megasquirt ecu;
 
         /**
          * 
          * @param ecu
          */
         public RebroadcastHandler(Megasquirt ecu)
         {
             this.ecu = ecu;
         }
 
         /**
          * 
          * @param m
          */
         @Override
         public void handleMessage(Message m)
         {
             super.handleMessage(m);
             Bundle b = m.getData();
             String msg = b.getString(MSLoggerApplication.MSG_ID);
             ecu.sendMessage(msg);
         }
     }
 
     /**
      * The thread that handles all communications with the ECU. This must be done in it's own thread as Android gets very picky about unresponsive UI threads
      */
     private class ECUThread extends Thread
     {
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
 
         Handshake handshake         = new Handshake();
         Thread    calculationThread = new CalculationThread();
 
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
             String sig = ecuImplementation.getSignature();
             setName("ECUThread:" + sig + ":" + System.currentTimeMillis());
             calculationThread.start();
         }
 
         private class CalculationThread extends Thread
         {
             public void run()
             {
                 this.setName("CalculationThread");
                 try
                 {
                     while (true)
                     {
                         byte[] buffer = handshake.get();
                         ecuImplementation.calculate(buffer);
                         logValues(buffer);
                         broadcast();
                     }
                 }
                 catch (InterruptedException e)
                 {
                     // Swallow, we're on our way out.
                 }
             }
         }
 
         /**
          * Kick the connection off
          */
         public void initialiseConnection()
         {
             sendMessage("Launching connection");
             ConnectionManager.INSTANCE.init(handler);
         }
 
         /**
          * The main loop of the connection to the ECU
          */
         public void run()
         {
             try
             {
                 sendMessage("");
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
                     ConnectionManager.INSTANCE.connect();
                     ConnectionManager.INSTANCE.flushAll();
 
                     if (!verifySignature())
                     {
                         DebugLogManager.INSTANCE.log("!verifySignature()", Log.DEBUG);
 
                         ConnectionManager.INSTANCE.disconnect();
                         return;
                     }
                     
                     /*
                      * Make sure we have calculated runtime vars at least once before refreshing flags.
                      * The reason is that the refreshFlags() function also trigger the creation of menus/dialogs/tables/curves/etc
                      * that use variables such as {clthighlim} in curves that need to have their value assigned before
                      * being used.
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
 
                         sendMessage("Connected to " + getTrueSignature());
                     }
 
                     long lastRpsTime = System.currentTimeMillis();
                     double readCounter = 0;
 
                     // This is the actual work. Outside influences will toggle 'running' when we want this to stop
                     while (running)
                     {
                         try
                         {
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
                 calculationThread.interrupt();
                 watch = null;
             }
         }
 
         /**
          * Called by other threads to stop the comms
          */
         public void halt()
         {
             running = false;
         }
 
         /**
          * Checks that the signature returned by the ECU is what we are expecting
          * 
          * @return
          * @throws IOException
          */
         private boolean verifySignature() throws IOException
         {
             boolean verified = false;
             String msSig = null;
             if (simulated)
             {
                 verified = true;
             }
             else
             {
                 byte[] sigCommand = ecuImplementation.getSigCommand();
                 sendMessage("Verifying MS");
                 String signature = ecuImplementation.getSignature();
 
                 msSig = getSignature(sigCommand);
                 verified = signature.equals(msSig);
                 if (verified)
                 {
                     trueSignature = msSig;
                 }
                 else
                 {
                     // We are going to try to remove characters from the MS signature to see
                     // if we support something similar that could match
                     for (int i = msSig.length() - 1; i > msSig.length() / 2 && i > 3
                             && !verified; i--)
                     {
                         String fuzzySig = msSig.substring(0, i);
                         
                         // We have a match!
                         if (signature.startsWith(fuzzySig))
                         {
                             verified = true;
                             trueSignature = msSig;
                             
                             String msg = "Got unsupported signature from Megasquirt \"" + msSig + "\" but found a similar supported signature \"" + signature + "\"";
                             
                             sendToastMessage(msg);                            
                             DebugLogManager.INSTANCE.log(msg, Log.INFO);
                             
                             break;
                         }
                     }
 
                 }
             }
             if (verified)
             {
                 sendMessage("Connected to " + trueSignature);
                 broadcast(CONNECTED);
             }
             else
             {
                 sendMessage("Signature error! " + msSig);
             }
             return verified;
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
             int d = ecuImplementation.getInterWriteDelay();
             ConnectionManager.INSTANCE.writeAndRead(ecuImplementation.getOchCommand(), buffer, d, ecuImplementation.isCRC32Protocol());
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
         protected void getPage(byte[] pageBuffer, byte[] pageSelectCommand, byte[] pageReadCommand) throws IOException,
                 CRC32Exception
         {
             ConnectionManager.INSTANCE.flushAll();
             int delay = ecuImplementation.getPageActivationDelay();
             if (pageSelectCommand != null)
             {
                 ConnectionManager.INSTANCE.writeCommand(pageSelectCommand, delay, ecuImplementation.isCRC32Protocol());
             }
             if (pageReadCommand != null)
             {
                 ConnectionManager.INSTANCE.writeCommand(pageReadCommand, delay, ecuImplementation.isCRC32Protocol());
             }
             ConnectionManager.INSTANCE.readBytes(pageBuffer, ecuImplementation.isCRC32Protocol());
         }
 
         /**
          * Gets the signature from the ECU
          * 
          * @param sigCommand
          * @return
          * @throws IOException
          */
         private String getSignature(byte[] sigCommand) throws IOException
         {
             String signatureFromMS = "";
             int d = Math.max(ecuImplementation.getInterWriteDelay(), 300);
             ConnectionManager.INSTANCE.flushAll();
 
             DebugLogManager.INSTANCE.log("getSignature()", Log.DEBUG);
 
             /*
              * We need to loop around until we get a valid result. When a BT module connects, it can feed an initial 'CONNECT xyz' string into the ECU which confuses the hell out of it, and the first
              * few interactions return garbage
              */
             do
             {
 
                 byte[] buf;
                 try
                 {
                     buf = ConnectionManager.INSTANCE.writeAndRead(sigCommand, d, ecuImplementation.isCRC32Protocol());
 
                     try
                     {
                         signatureFromMS = ECUFingerprint.processResponse(buf);
                     }
                     catch (BootException e)
                     {
                         return "ECU needs a reboot!";
                     }
                 }
                 catch (CRC32Exception e1)
                 {
                     DebugLogManager.INSTANCE.logException(e1);
                 }
 
                 DebugLogManager.INSTANCE.log("Got a signature of " + signatureFromMS, Log.INFO);
 
                 ConnectionManager.INSTANCE.flushAll();
             }
             // We loop until we get a valid signature
             while (signatureFromMS.equals(ECUFingerprint.UNKNOWN));
 
             // Notify the user of the signature we got
             ConnectionManager.INSTANCE.sendStatus("Recieved '" + signatureFromMS + "'");
 
             return signatureFromMS;
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
 
     public double[] getVector(String channelName)
     {
         double[] value = {0};
         Class<?> c = ecuImplementation.getClass();
         try
         {
             Field f = c.getDeclaredField(channelName);
             value = (double[]) f.get(ecuImplementation);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get value for " + channelName, Log.ERROR);
             Log.e(ApplicationSettings.TAG, "Megasquirt.getVector()", e);
         }
         return value;
     }
     
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
             Log.e(ApplicationSettings.TAG, "Megasquirt.getField()", e);
         }
         return value;
     }
     
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
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName, Log.ERROR);
             Log.e(ApplicationSettings.TAG, "Megasquirt.setFeidl()", e);
         }
     }
 
     public double roundDouble(double number, int decimals)
     {
         double p = (double) Math.pow(10,decimals);
         number = number * p;
         double tmp = Math.round(number);
         return tmp / p;
     }
     
     public double[][] loadByteArray(byte[] pageBuffer, int offset, int width, int height, double scale, double translate, int digits, boolean signed)
     {
         double[][] destination = new double[width][height];
         int index = offset;
         for (int y = 0 ; y < height ; y++)
         {
             for (int x = 0; x < width ; x++)
             {
                 double value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index): MSUtils.INSTANCE.getByte(pageBuffer, index);
                 value = (value + translate) * scale;
                 destination[x][y] = this.roundDouble(value, digits);
                 index = index + 1;
             }
         }
         return destination;
     }
     public double[] loadByteVector(byte[] pageBuffer, int offset, int width, double scale, double translate, int digits, boolean signed)
     {
         double[] destination = new double[width];
         int index = offset;
         for (int x = 0; x < width ; x++)
         {
             double value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index): MSUtils.INSTANCE.getByte(pageBuffer, index);
             value = (value + translate) * scale;
             destination[x] = this.roundDouble(value, digits);
             index = index + 1;
         }
         
         return destination;
     }
 
     public double[][] loadWordArray(byte[] pageBuffer, int offset, int width, int height, double scale, double translate, int digits, boolean signed)
     {
         double[][] destination = new double[width][height];
         int index = offset;
         for (int y = 0 ; y < height ; y++)
         {
             for (int x = 0; x < width ; x++)
             {
                 double value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index): MSUtils.INSTANCE.getWord(pageBuffer, index);
                 value = (value + translate) * scale;
                 destination[x][y] = this.roundDouble(value, digits);
                 index = index + 2;
             }
         }
 
         return destination;
     }
     public double[] loadWordVector(byte[] pageBuffer, int offset, int width, double scale, double translate, int digits, boolean signed)
     {
         double[] destination = new double[width];
         int index = offset;
         for (int x = 0; x < width ; x++)
         {
             double value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index): MSUtils.INSTANCE.getWord(pageBuffer, index);
             value = (value + translate) * scale;
             destination[x] = this.roundDouble(value, digits);
             index = index + 2;
         }
         
         return destination;
 
     }
     
     public boolean isConstantExists(String name)
     {
         return MSECUInterface.constants.containsKey(name);
     }
     
     public Constant getConstantByName(String name)
     {
         return MSECUInterface.constants.get(name);
     }
 
     public OutputChannel getOutputChannelByName(String name)
     {
         return MSECUInterface.outputChannels.get(name);
     }
     
     public TableEditor getTableEditorByName(String name) 
     {
         return MSECUInterface.tableEditors.get(name);
     }
     
     public CurveEditor getCurveEditorByName(String name) 
     {
         return MSECUInterface.curveEditors.get(name);
     }
     
     public List<Menu> getMenusForDialog(String name)
     {
         return MSECUInterface.menus.get(name);
     }
     
     public MSDialog getDialogByName(String name)
     {
         return MSECUInterface.dialogs.get(name);
     }
     
     public boolean getUserDefinedVisibilityFlagsByName(String name)
     {
         if (MSECUInterface.userDefinedVisibilityFlags.containsKey(name))
         {
             return MSECUInterface.userDefinedVisibilityFlags.get(name);
         }
         
         return true;
     }
   
     public boolean getMenuVisibilityFlagsByName(String name)
     {
         return MSECUInterface.menuVisibilityFlags.get(name);
     }
     
     public void addDialog(MSDialog dialog)
     {
         MSECUInterface.dialogs.put(dialog.getName(), dialog);
     }
     
     public void addCurve(CurveEditor curve)
     {
         MSECUInterface.curveEditors.put(curve.getName(), curve);
     }
     
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
 
     public String[] defaultGauges()
     {
         return ecuImplementation.defaultGauges();
     }
 
     public int getBlockSize()
     {
         return ecuImplementation.getBlockSize();
     }
 
     public int getCurrentTPS()
     {
         return ecuImplementation.getCurrentTPS();
     }
 
     public String getLogHeader()
     {
         return ecuImplementation.getLogHeader();
     }
 
     public void initGauges()
     {
         ecuImplementation.initGauges();
     }
 
     public void refreshFlags()
     {
         ecuImplementation.refreshFlags();
     }
 
     public void setMenuVisibilityFlags()
     {
         ecuImplementation.setMenuVisibilityFlags();
     }
 
     public void setUserDefinedVisibilityFlags()
     {
         ecuImplementation.setUserDefinedVisibilityFlags();
         
     }
 
     public String[] getControlFlags()
     {
         return ecuImplementation.getControlFlags();
     }
     
     public boolean hasImplementation(Class<? extends MSECUInterface> cls)
     {
         return (ecuImplementation != null && ecuImplementation.getClass().equals(cls));
     }
 
     @Override
     public void setImplementation(MSECUInterface i)
     {
         this.ecuImplementation = i;
     }
     
     public List<SettingGroup> getSettingGroups()
     {
         ecuImplementation.createSettingGroups();
        return ecuImplementation.settingGroups;
     }
     /**
      * Helper functions to get specific value out of ECU
      * Different MS version have different name for the same thing so get the right one depending on the MS version we're connected to
      */
     
     /**
      * @return Return the current ECU cylinders count
      */
     public int getCylindersCount()
     {
         return  (int) (isConstantExists("nCylinders") ? getField("nCylinders") : getField("nCylinders1"));
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
