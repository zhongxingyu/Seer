 package uk.org.smithfamily.mslogger.comms;
 
 import java.io.*;
 import java.util.*;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.MSLoggerApplication;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 
 import android.bluetooth.*;
 import android.os.*;
 import android.util.Log;
 
 /**
  * Main connection class that wrap all the Bluetooth stuff that communicate with the Megasquirt
  */
 public enum Connection
 {
     INSTANCE;
 
     /**
      * Class that is used to time out the Bluetooth communication
      */
     static class Reaper extends TimerTask
     {
 		Connection parent;
 		Reaper(Connection p)
 		{
 			this.parent = p;
 		}
         @Override
         public void run()
         {
             parent.timerTriggered = true;
             parent.tearDown();
         }
 
     }
 
     private String            btAddr;
     private BluetoothAdapter  adapter;
     private BluetoothSocket   socket;
     private InputStream       mmInStream;
     private OutputStream      mmOutStream;
     private BluetoothDevice   remote;
     private Handler           handler;
     private static final long IO_TIMEOUT     = 5000;
     volatile boolean          timerTriggered = false;
     Timer                     t              = new Timer();
 
     public enum ConnectionState
     {
         STATE_DISCONNECTED, STATE_CONNECTING, STATE_CONNECTED
     };
 
     private volatile ConnectionState currentState = ConnectionState.STATE_DISCONNECTED;
     private Thread                   ownerThread;
 
     /**
      * Get the current Bluetooth connection state
      * 
      * @return
      */
     public ConnectionState getCurrentState()
     {
         return currentState;
     }
 
     /**
      * Get the handler
      * 
      * @return
      */
     public Handler getHandler()
     {
         return handler;
     }
 
     /**
      * Set the handler 
      * 
      * @param handler
      */
     public void setHandler(Handler handler)
     {
         this.handler = handler;
     }
 
     /**
      * Initialise the Bluetooth connection
      * 
      * @param btAddr
      * @param adapter
      * @param h
      */
     public synchronized void init(String btAddr, BluetoothAdapter adapter, Handler h)
     {
         if (currentState != ConnectionState.STATE_DISCONNECTED && !btAddr.equals(this.btAddr))
         {
             tearDown();
             setState(ConnectionState.STATE_DISCONNECTED);
         }
         this.btAddr = btAddr;
         this.adapter = adapter;
         this.handler = h;
         this.ownerThread = Thread.currentThread();
         timerTriggered = false;
     }
 
     /**
      * Set the current state of the Bluetooth connection
      * 
      * @param state
      */
     private void setState(ConnectionState state)
     {
         currentState = state;
         DebugLogManager.INSTANCE.log("Set state to " + state, Log.INFO);
     }
 
     /**
      * Connect to the Bluetooth device
      *  
      * 
      * @throws IOException
      */
     public synchronized void connect() throws IOException
     {
     	DebugLogManager.INSTANCE.log("connect() : Current state "+currentState, Log.DEBUG);
         
         if (currentState != ConnectionState.STATE_DISCONNECTED)
         {
             return;
         }
         setState(ConnectionState.STATE_CONNECTING);
         DebugLogManager.INSTANCE.log("connect() : Current state "+currentState, Log.DEBUG);
         remote = adapter.getRemoteDevice(btAddr);
         getSocket(remote);
 
         adapter.cancelDiscovery();
 
         try
         {
         	DebugLogManager.INSTANCE.log("connect() : Attempting connection", Log.DEBUG);
             
             socket.connect();
         }
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
             delay(1000);
 
             try
             {
                 socket.close();
             }
             catch (Exception e1)
             {
                 DebugLogManager.INSTANCE.logException(e1);
             }
             ApplicationSettings.INSTANCE.setBTWorkaround(!ApplicationSettings.INSTANCE.isBTWorkaround());
             getSocket(remote);
             
             try {
                 socket.connect();
             }
             catch (Exception e1)
             {
                 DebugLogManager.INSTANCE.logException(e1);
             }
         }
         InputStream tmpIn = null;
         OutputStream tmpOut = null;
         DebugLogManager.INSTANCE.log("Establishing connection", Log.DEBUG);
 
         // Get the BluetoothSocket input and output streams
         try
         {
             tmpIn = socket.getInputStream();
             tmpOut = socket.getOutputStream();
         }
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
 
         mmInStream = tmpIn;
         mmOutStream = tmpOut;
         if (mmInStream != null && mmOutStream != null)
         {
             setState(ConnectionState.STATE_CONNECTED);
             DebugLogManager.INSTANCE.log("connect() : Current state "+currentState, Log.DEBUG);
             
         }
         else
         {
             DebugLogManager.INSTANCE.log("Failed to complete connection", Log.ERROR);
             setState(ConnectionState.STATE_DISCONNECTED);
             tearDown();
         }
         return;
     }
 
     /**
      * Get socket to the Bluetooth device
      * 
      * @param remote Remote Bluetooth device
      * @throws IOException
      */
     private void getSocket(BluetoothDevice remote) throws IOException
     {
         try
         {
             socket = BTSocketFactory.getSocket(remote);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get a socket!", Log.ERROR);
             throw new IOException(e.getLocalizedMessage());
         }
     }
 
     /**
      * Check if the application should auto-connect and automatically connect if it should
      * 
      * @throws IOException
      */
     private synchronized void checkConnection() throws IOException
     {
     	DebugLogManager.INSTANCE.log("checkConnection()", Log.DEBUG);
         
         if (currentState == ConnectionState.STATE_DISCONNECTED)
         {
             boolean autoConnect = ApplicationSettings.INSTANCE.autoConnectable();
             if (autoConnect)
             {
                 connect();
             }
             else
             {
                 throw new IOException("Autoconnection not allowed");
             }
         }
         if (ownerThread != Thread.currentThread())
         {
             throw new IOException("Attempt to use from thread'" + Thread.currentThread().getName() + "' when owned by thread '" + ownerThread.getName());
         }
     }
 
     /**
      * Make the connection thread sleep
      * 
      * @param d Delay
      */
     private void delay(int d)
     {
         DebugLogManager.INSTANCE.log("Sleeping for "+d+"ms", Log.DEBUG);
     	try
         {
             Thread.sleep(d);
         }
         catch (InterruptedException e)
         {
         	DebugLogManager.INSTANCE.log("sleep was interrupted", Log.DEBUG);
         }
     }
 
     /**
      * @return Return the remote Bluetooth device
      */
     public BluetoothDevice getRemote()
     {
         return remote;
     }
 
     /**
      * Close input and output Bluetooth streams and socket
      */
     public synchronized void tearDown()
     {
     	DebugLogManager.INSTANCE.log("tearDown()", Log.DEBUG);
         if (mmInStream != null)
         {
             try
             {
                 mmInStream.close();
             }
             catch (IOException e)
             {
             }
             mmInStream = null;
         }
         if (mmOutStream != null)
         {
             try
             {
                 mmOutStream.close();
             }
             catch (IOException e)
             {
             }
             mmOutStream = null;
         }
 
         if (socket != null)
         {
             try
             {
                 DebugLogManager.INSTANCE.log("ECUFingerprint teardown socket close()", Log.DEBUG);
 
                 socket.close();
             }
             catch (IOException e)
             {
             }
             socket = null;
         }
         setState(ConnectionState.STATE_DISCONNECTED);
     }
 
     /**
      * Write a command to the Bluetooth stream
      * 
      * @param cmd       Command to be send
      * @param d         Delay to wait after sending command
      * @throws IOException
      */
     public synchronized void writeCommand(byte[] command, int d,boolean isCRC32) throws IOException
     {
        if (isCRC32)
         {
         	command = CRC32ProtocolHandler.wrap(command);
         }
         DebugLogManager.INSTANCE.log("Writing", command, Log.DEBUG);
         this.mmOutStream.write(command);
         this.mmOutStream.flush();
         delay(d);
     }
 
     /**
      * Write a command to the Bluetooth stream and return the result
      * 
      * @param cmd       Command to be send
      * @param d         Delay to wait after sending command
      * @return
      * @throws IOException
      */
     public byte[] writeAndRead(byte[] cmd, int d,boolean isCRC32) throws IOException
     {
         checkConnection();
        writeCommand(cmd, d, isCRC32);
 
         byte[] result = readBytes(isCRC32);
         return result;
     }
 
     /**
      * Write a command to the Bluetooth stream and read the result
      * 
      * @param cmd       Command to be send
      * @param result    Result of the command sent by the Megasquirt
      * @param d         Delay to wait after sending command
      * @throws IOException
      */
     public void writeAndRead(byte[] cmd, byte[] result, int d,boolean isCRC32) throws IOException
     {
         checkConnection();
         writeCommand(cmd, d,isCRC32);
 
         readBytes(result,isCRC32);
     }
 
     /**
      * Read bytes available on Bluetooth stream
      * 
      * @param bytes
      * @throws IOException
      */
     public void readBytes(byte[] bytes,boolean isCRC32) throws IOException
     {
         TimerTask reaper = new Reaper(this);
         t.schedule(reaper, IO_TIMEOUT);
         
         int target = bytes.length;
         if (isCRC32)
         {
         	target += 7;
         }
         
         byte[] buffer = new byte[target];
         int read = 0;
         try
         {
            synchronized(this)
            {
                 while (read < target)
                 {
                     int numRead = mmInStream.read(buffer, read, target - read);
                     if (numRead == -1)
                     {
                         throw new IOException("end of stream attempting to read");
                     }
                     read += numRead;
                     DebugLogManager.INSTANCE.log("readBytes[] : target = "+target+" read so far :" +read, Log.DEBUG);
                 }
             }
             reaper.cancel();
             DebugLogManager.INSTANCE.log("readBytes[]",buffer, Log.DEBUG);
             if (isCRC32)
             {
             	if (!CRC32ProtocolHandler.check(buffer))
             	{
             		throw new IOException("CRC32 check failed");
             	}
             	
             	byte[] actual = CRC32ProtocolHandler.unwrap(buffer);
             	System.arraycopy(actual, 0, bytes, 0, bytes.length);
             }
             else
             {
             	System.arraycopy(buffer, 0, bytes, 0, bytes.length);
             }
         }
         catch (IOException e)
         {
             if (timerTriggered)
             {
                 DebugLogManager.INSTANCE.log("Time out reading from stream", Log.ERROR);
                 throw e;
             }
         }
     }
 
     /**
      * Read bytes available on Bluetooth stream and return the resulting array
      * 
      * @return Array of bytes read from Bluetooth stream
      * @throws IOException
      */
     public byte[] readBytes(boolean isCRC32) throws IOException
     {
         List<Byte> read = new ArrayList<Byte>();
 
        synchronized(this)
        {
             while (mmInStream.available() > 0)
             {
                 byte b = (byte) mmInStream.read();
                 read.add(b);
             }
         }
         
         byte[] result = new byte[read.size()];
         int i = 0;
         for (Byte b : read)
         {
             result[i++] = b;
         }
                 
         DebugLogManager.INSTANCE.log("readBytes", result, Log.DEBUG);
         
         if (isCRC32)
         {
         	if (!CRC32ProtocolHandler.check(result))
         	{
         		throw new IOException("CRC32 check failed");
         	}
         	result = CRC32ProtocolHandler.unwrap(result);
         }
         
         return result;
     }
 
     /**
      * Flush all data on the Bluetooth stream
      * 
      * @throws IOException
      */
     public synchronized void flushAll() throws IOException
     {
     	DebugLogManager.INSTANCE.log("flushAll()", Log.DEBUG);
         checkConnection();
 
         mmOutStream.flush();
         while (mmInStream.available() > 0)
         {
             mmInStream.read();
         }
     }
 
     /**
      * Send status to the application that appear in the status bar
      * 
      * @param msgStr Message to be broadcasted
      */
     public void sendStatus(String msgStr)
     {
         DebugLogManager.INSTANCE.log(msgStr, Log.INFO);
 
         if (handler != null)
         {
             Message msg = handler.obtainMessage(MSLoggerApplication.MESSAGE_TOAST);
             Bundle bundle = new Bundle();
             bundle.putString(MSLoggerApplication.MSG_ID, msgStr);
             msg.setData(bundle);
             handler.sendMessage(msg);
         }
     }
 
     /**
      * Disconnect the current Bluetooth connection with the Megasquirt ECU
      */
     public synchronized void disconnect()
     {
     	DebugLogManager.INSTANCE.log("disconnect()", Log.DEBUG);
         
         tearDown();
     }
 }
