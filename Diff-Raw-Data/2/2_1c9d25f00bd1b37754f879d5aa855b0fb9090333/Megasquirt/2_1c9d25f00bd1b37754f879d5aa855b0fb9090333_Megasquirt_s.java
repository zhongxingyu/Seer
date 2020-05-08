 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.text.DecimalFormat;
 import java.util.Timer;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.MSLoggerApplication;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.comms.Connection;
 import uk.org.smithfamily.mslogger.log.DatalogManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.log.FRDLogManager;
 import android.bluetooth.BluetoothAdapter;
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
 public abstract class Megasquirt
 {
     static Timer               connectionWatcher = new Timer("ConnectionWatcher", true);
 
     private boolean            simulated         = false;
 
     private BluetoothAdapter   mAdapter;
 
     public static final String NEW_DATA          = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
 
     public static final String CONNECTED         = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
     public static final String DISCONNECTED      = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
     public static final String TAG               = ApplicationSettings.TAG;
 
     protected Context          context;
 
     public abstract String getSignature();
 
     public abstract byte[] getOchCommand();
 
     public abstract byte[] getSigCommand();
 
     public abstract void loadConstants(boolean simulated) throws IOException;
 
     public abstract void calculate(byte[] ochBuffer);
 
     public abstract String getLogHeader();
 
     public abstract String getLogRow();
 
     public abstract int getBlockSize();
 
     public abstract int getSigSize();
 
     public abstract int getPageActivationDelay();
 
     public abstract int getInterWriteDelay();
 
     public abstract int getCurrentTPS();
 
     public abstract void initGauges();
 
     public abstract String[] defaultGauges();
 
     public abstract void refreshFlags();
 
     public abstract boolean isCRC32Protocol();
 
     private boolean            logging;
     private boolean            constantsLoaded;
     private String             trueSignature = "Unknown";
     private ECUThread          ecuThread;
     private volatile boolean   running;
 
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
 
     protected int table(double d1, String name)
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
     protected double tempCvt(int t)
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
 
         sendMessage(context.getString(R.string.disconnected_from_ms));
     }
 
     /**
      * Revert to initial state
      */
     public void reset()
     {
         refreshFlags();
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
             DatalogManager.INSTANCE.write(getLogRow());
 
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
 
         Connection.INSTANCE.disconnect();
         DatalogManager.INSTANCE.mark("Disconnected");
         FRDLogManager.INSTANCE.close();
         DatalogManager.INSTANCE.close();
         broadcast(DISCONNECTED);
     }
 
     /**
      * Send a message to the user
      * 
      * @param msg
      */
     protected void sendMessage(String msg)
     {
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.GENERAL_MESSAGE);
         broadcast.putExtra(ApplicationSettings.MESSAGE, msg);
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
         // broadcast.putExtra(LOCATION, location);
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
     protected int timeNow()
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
     protected double round(double v)
     {
         return Math.floor(v * 100 + .5) / 100;
     }
 
     /**
      * Returns if a flag has been set in the application
      * 
      * @param name
      * @return
      */
     protected boolean isSet(String name)
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
                 synchronized(this)
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
             String sig = Megasquirt.this.getSignature();
             setName("ECUThread:" + sig);
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
 //                        DebugLogManager.INSTANCE.log("Got a buffer", Log.INFO);
 //                        long start = System.currentTimeMillis();
                         calculate(buffer);
 //                        long calc = System.currentTimeMillis();
                         logValues(buffer);
 //                        long log = System.currentTimeMillis();
 //                        long gen = System.currentTimeMillis();
                         broadcast();
 //                        long b = System.currentTimeMillis();
 //                        DebugLogManager.INSTANCE.log("Calculations done and broadcast : " + (calc - start) + "," + (log - calc) + "," + (gen - log) + ","
 //                                + (b - gen) + "," + (b - start), Log.INFO);
                     }
                 }
                 catch (InterruptedException e)
                 {
                     //Swallow, we're on our way out.
                 }
             }
         }
 
         /**
          * Kick the connection off
          */
         public void initialiseConnection()
         {
             if (!ApplicationSettings.INSTANCE.btDeviceSelected())
             {
                 sendMessage("Bluetooth device not selected");
                 return;
             }
             mAdapter = ApplicationSettings.INSTANCE.getDefaultAdapter();
             if (mAdapter == null)
             {
                 sendMessage("Could not find the default Bluetooth adapter!");
                 return;
             }
             String btAddr = ApplicationSettings.INSTANCE.getBluetoothMac();
 
             sendMessage("Launching connection");
             Connection.INSTANCE.init(btAddr, mAdapter, handler);
 
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
                     Connection.INSTANCE.connect();
                     Connection.INSTANCE.flushAll();
 
                     if (!verifySignature())
                     {
                         DebugLogManager.INSTANCE.log("!verifySignature()", Log.DEBUG);
 
                         Connection.INSTANCE.disconnect();
                         return;
                     }
 
                     // Make sure everyone agrees on what flags are set
                     ApplicationSettings.INSTANCE.refreshFlags();
                     refreshFlags();
                     if (!constantsLoaded)
                     {
                         // Only do this once so reconnects are quicker
                         loadConstants(simulated);
                         constantsLoaded = true;
 
                         sendMessage("Connected to " + getTrueSignature());
                     }
 
                     long lastRpsTime = 0;
                     double readCounter = 0;
 
                     // This is the actual work. Outside influences will toggle 'running' when we want this to stop
                     while (running)
                     {
 //                        DebugLogManager.INSTANCE.log("** START **", Log.INFO);
 //                        long start = System.currentTimeMillis();
                         final byte[] buffer = getRuntimeVars();
 //                        long comms = System.currentTimeMillis();
 //                        DebugLogManager.INSTANCE.log("Comms took :" + (comms - start), Log.INFO);
                         handshake.put(buffer);
 //                        long rt = System.currentTimeMillis();
 //                        DebugLogManager.INSTANCE.log("Sent RTVars for calculation : " + (comms - start) + "," + (rt - comms) + "," + (rt - start), Log.INFO);
 
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
                 byte[] sigCommand = getSigCommand();
                 sendMessage("Verifying MS");
                 String signature = Megasquirt.this.getSignature();
 
                 msSig = getSignature(sigCommand);
                 verified = signature.equals(msSig);
                 if (verified)
                 {
                     trueSignature = msSig;
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
          */
         private byte[] getRuntimeVars() throws IOException
         {
             byte[] buffer = new byte[Megasquirt.this.getBlockSize()];
             if (simulated)
             {
                 MSSimulator.INSTANCE.getNextRTV(buffer);
                 return buffer;
             }
             int d = getInterWriteDelay();
             Connection.INSTANCE.writeAndRead(getOchCommand(), buffer, d, isCRC32Protocol());
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
         protected void getPage(byte[] pageBuffer, byte[] pageSelectCommand, byte[] pageReadCommand) throws IOException
         {
 
             Connection.INSTANCE.flushAll();
             int delay = getPageActivationDelay();
             if (pageSelectCommand != null)
             {
                 Connection.INSTANCE.writeCommand(pageSelectCommand, delay, isCRC32Protocol());
             }
             if (pageReadCommand != null)
             {
                 Connection.INSTANCE.writeCommand(pageReadCommand, delay, isCRC32Protocol());
             }
             Connection.INSTANCE.readBytes(pageBuffer, isCRC32Protocol());
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
            int d = Math.max(getInterWriteDelay(), 100);
             Connection.INSTANCE.flushAll();
 
             DebugLogManager.INSTANCE.log("getSignature()", Log.DEBUG);
 
             /*
              * We need to loop around until we get a valid result. When a BT module connects, it can feed an initial 'CONNECT xyz' string into the ECU which confuses the hell out of it, and the first
              * few interactions return garbage
              */
             do
             {
                 byte[] buf = Connection.INSTANCE.writeAndRead(sigCommand, d, isCRC32Protocol());
 
                 try
                 {
                     signatureFromMS = ECUFingerprint.processResponse(buf);
                 }
                 catch (BootException e)
                 {
                     return "ECU needs a reboot!";
                 }
 
                 DebugLogManager.INSTANCE.log("Got a signature of " + signatureFromMS, Log.INFO);
 
                 Connection.INSTANCE.flushAll();
             }
             // We loop until we get a valid signature
             while (signatureFromMS.equals(ECUFingerprint.UNKNOWN));
 
             // Notify the user of the signature we got
             Connection.INSTANCE.sendStatus("Recieved '" + signatureFromMS + "'");
 
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
     protected byte[] loadPage(int pageNo, int pageOffset, int pageSize, byte[] select, byte[] read)
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
         return buffer;
     }
 
     /**
      * 
      * @param buffer
      * @param select
      * @param read
      * @throws IOException
      */
     private void getPage(byte[] buffer, byte[] select, byte[] read) throws IOException
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
             dir.mkdirs();
 
             String fileName = this.getClass().getName() + ".firmware";
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
 
     public double getField(String channelName)
     {
         {
             double value = 0;
             Class<?> c = this.getClass();
             try
             {
                 Field f = c.getDeclaredField(channelName);
                 value = f.getDouble(this);
             }
             catch (Exception e)
             {
                 DebugLogManager.INSTANCE.log("Failed to get value for " + channelName, Log.ERROR);
                 Log.e(ApplicationSettings.TAG, "Megasquirt.getValue()", e);
             }
             return value;
         }
 
     }
 
 }
