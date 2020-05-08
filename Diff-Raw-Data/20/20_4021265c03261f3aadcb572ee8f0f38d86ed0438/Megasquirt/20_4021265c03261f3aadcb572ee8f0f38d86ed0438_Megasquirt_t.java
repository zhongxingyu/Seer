 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.*;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 
 import org.acra.ErrorReporter;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.log.*;
 import android.bluetooth.*;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Environment;
 import android.util.Log;
 
 public abstract class Megasquirt
 {
 	static Timer	connectionWatcher	= new Timer("ConnectionWatcher", true);
 
 	public ConnectionState getCurrentState()
 	{
 		return currentState;
 	}
 
 	private boolean	simulated	= false;
 
 	public enum ConnectionState
 	{
 		STATE_NONE, STATE_CONNECTING, STATE_CONNECTED
 	};
 
 	private volatile ConnectionState	currentState	= ConnectionState.STATE_NONE;
 
 	private UUID						RFCOMM_UUID		= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
 	protected BluetoothSocket			sock;
 
 	private BluetoothAdapter			mAdapter;
 	private ConnectThread				mConnectThread;
 	private ConnectedThread				mConnectedThread;
 
 	public static final String			NEW_DATA		= "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
 
 	public static final String			CONNECTED		= "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
 	public static final String			CONNECTION_LOST	= "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTION_LOST";
 	public static final String			DISCONNECTED	= "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
 	public static final String			TAG				= ApplicationSettings.TAG;
 
 	protected Context					context;
 
 	public abstract Set<String> getSignature();
 
 	public abstract byte[] getOchCommand();
 
 	public abstract byte[] getSigCommand();
 
 	public abstract void loadConstants(boolean simulated) throws IOException;
 
 	public abstract void calculate(byte[] ochBuffer) throws IOException;
 
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
 
 	private byte[]	ochBuffer;
 
 	private boolean	logging;
 	private boolean	constantsLoaded;
 	private boolean	signatureChecked;
 	private String	trueSignature	= "Unknown";
 
 	private boolean	running;
 
 	protected int table(int i1, String name)
 	{
 		return TableManager.INSTANCE.table(i1, name);
 	}
 
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
 
 	public void start()
 	{
 		DebugLogManager.INSTANCE.log("Megasquirt.start()");
 
 		TimerTask connectionTask = new TimerTask()
 		{
 			@Override
 			public void run()
 			{
 				ApplicationSettings settings = ApplicationSettings.INSTANCE;
 				boolean autoConnectable = settings.autoConnectable();
 				if (autoConnectable)
 				{
 					if (currentState == ConnectionState.STATE_NONE)
 					{
 						setState(ConnectionState.STATE_CONNECTING);
 						DebugLogManager.INSTANCE.log("Launching connection");
 						initialiseConnection();
 					}
 				}
 			}
 		};
 		connectionWatcher.schedule(connectionTask, 1000, 5000);
 		running = true;
 	}
 
 	public void stop()
 	{
 		connectionWatcher.purge();
 		running = false;
 		DebugLogManager.INSTANCE.log("Megasquirt.stop()");
 		disconnect();
 		sendMessage("");
 		setState(ConnectionState.STATE_NONE);
 	}
 
 	public void initialiseConnection()
 	{
 		setState(ConnectionState.STATE_CONNECTING);
 		ochBuffer = new byte[this.getBlockSize()];
 
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
 		// Cancel any thread attempting to make a connection
 		if (mConnectThread != null)
 		{
 			sendMessage("Cancelling connection thread");
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null)
 		{
 			sendMessage("Cancelling connected thread");
 			mConnectedThread.cancelConnection();
 			mConnectedThread = null;
 		}
 		String btAddr = ApplicationSettings.INSTANCE.getBluetoothMac();
 
 		BluetoothDevice remote = mAdapter.getRemoteDevice(btAddr);
 
 		sendMessage("Launching connection");
 		connect(remote);
 	}
 
 	private void logValues()
 	{
 
 		if (!logging)
 		{
 			return;
 		}
 		try
 		{
 			FRDLogManager.INSTANCE.write(ochBuffer);
 			DatalogManager.INSTANCE.write(getLogRow());
 
 		}
 		catch (IOException e)
 		{
 			// ErrorReporter.getInstance().handleException(e);
 			DebugLogManager.INSTANCE.logException(e);
 
 			Log.e(ApplicationSettings.TAG, "Megasquirt.logValues()", e);
 		}
 	}
 
 	private void disconnect()
 	{
 		if (simulated)
 			return;
 		if (mConnectedThread != null)
 		{
 			mConnectedThread.cancelConnection();
 		}
 		FRDLogManager.INSTANCE.close();
 		DatalogManager.INSTANCE.close();
 
 	}
 
 	protected void sendMessage(String msg)
 	{
 		Intent broadcast = new Intent();
 		broadcast.setAction(ApplicationSettings.GENERAL_MESSAGE);
 		broadcast.putExtra(ApplicationSettings.MESSAGE, msg);
 		context.sendBroadcast(broadcast);
 
 	}
 
 	private void delay(long pauseTime)
 	{
 		try
 		{
 			Thread.sleep(pauseTime);
 		}
 		catch (InterruptedException e)
 		{
 			DebugLogManager.INSTANCE.logException(e);
 			Log.e(ApplicationSettings.TAG, "Megasquirt.delay()", e);
 		}
 
 	}
 
 	private void broadcast(String action)
 	{
 		Intent broadcast = new Intent();
 		broadcast.setAction(action);
 		// broadcast.putExtra(LOCATION, location);
 		context.sendBroadcast(broadcast);
 
 	}
 
 	public Megasquirt(Context c)
 	{
 		this.context = c;
 	}
 
 	protected double timeNow()
 	{
 		return (System.currentTimeMillis() - DatalogManager.INSTANCE.getLogStart()) / 1000.0;
 	}
 
 	public double getValue(String channel)
 	{
 
 		double value = 0;
 		Class<?> c = this.getClass();
 		try
 		{
 			Field f = c.getDeclaredField(channel);
 			value = f.getDouble(this);
 		}
 		catch (Exception e)
 		{
 			DebugLogManager.INSTANCE.log("Failed to get value for " + channel);
 			Log.e(ApplicationSettings.TAG, "Megasquirt.getValue()", e);
 		}
 		return value;
 	}
 
 	public void startLogging()
 	{
 		logging = true;
 		DebugLogManager.INSTANCE.log("startLogging()");
 
 	}
 
 	public void stopLogging()
 	{
 		DebugLogManager.INSTANCE.log("stopLogging()");
 		logging = false;
 		FRDLogManager.INSTANCE.close();
 		DatalogManager.INSTANCE.close();
 	}
 
 	protected double round(double v)
 	{
 		return Math.floor(v * 100 + .5) / 100;
 	}
 
 	protected boolean isSet(String name)
 	{
 		return ApplicationSettings.INSTANCE.isSet(name);
 	}
 
 	/**
 	 * This thread runs while attempting to make an outgoing connection with a
 	 * device. It runs straight through; the connection either succeeds or
 	 * fails.
 	 */
 	private class ConnectThread extends Thread
 	{
 		private final BluetoothSocket	mmSocket;
 		private final BluetoothDevice	mmDevice;
 
 		public ConnectThread(BluetoothDevice device)
 		{
 			mmDevice = device;
 			BluetoothSocket tmp = null;
 			// Get a BluetoothSocket for a connection with the
 			// given BluetoothDevice
 			if (ApplicationSettings.INSTANCE.isBTWorkaround())
 			{
 				try
 				{
 					tmp = createWorkaroundSocket(device);
 				}
 				catch (Exception e)
 				{
 					DebugLogManager.INSTANCE.logException(e);
 				}
 			}
 			else
 			{
 				try
 				{
 					tmp = device.createRfcommSocketToServiceRecord(RFCOMM_UUID);
 				}
 				catch (IOException e)
 				{
 					// That didn't work, let's try the work around
 					DebugLogManager.INSTANCE.logException(e);
 
 					try
 					{
 						tmp = createWorkaroundSocket(device);
 						// That did work, let's make it permanment
 						ApplicationSettings.INSTANCE.setBTWorkaround(true);
 					}
 					catch (Exception e1)
 					{
 						// We're boned, nothing works
 						DebugLogManager.INSTANCE.logException(e);
 					}
 				}
 			}
 			mmSocket = tmp;
 		}
 
 		private BluetoothSocket createWorkaroundSocket(BluetoothDevice device) throws Exception
 		{
 			Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
 			BluetoothSocket tmp = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
 			return tmp;
 		}
 
 		public void run()
 		{
 			if (mmSocket == null)
 			{
 				sendMessage("No connection!");
 				return;
 			}
 			Log.i(TAG, "BEGIN mConnectThread");
 			setName("ConnectThread");
 			sendMessage("Starting connection");
 
 			// Always cancel discovery because it will slow down a connection
 			mAdapter.cancelDiscovery();
 
 			// Make a connection to the BluetoothSocket
 			try
 			{
 				// This is a blocking call and will only return on a
 				// successful connection or an exception
 				mmSocket.connect();
 			}
 			catch (IOException e)
 			{
 				// ErrorReporter.getInstance().handleException(e);
 				DebugLogManager.INSTANCE.logException(e);
 				try
 				{
 					Thread.sleep(1000);
 				}
 				catch (InterruptedException e1)
 				{
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 				// Close the socket
 				try
 				{
 					mmSocket.close();
 				}
 				catch (IOException e2)
 				{
 					// ErrorReporter.getInstance().handleException(e2);
 					Log.e(TAG, "unable to close() socket during connection failure", e2);
 				}
 				// Start the service over to restart listening mode
 				connectionFailed();
 				return;
 			}
 
 			// Reset the ConnectThread because we're done
 			synchronized (Megasquirt.this)
 			{
 				mConnectThread = null;
 			}
 
 			// Start the connected thread
 			connected(mmSocket, mmDevice);
 		}
 
 		public void cancel()
 		{
 			try
 			{
 				mmSocket.close();
 			}
 			catch (IOException e)
 			{
 				// ErrorReporter.getInstance().handleException(e);
 
 				DebugLogManager.INSTANCE.logException(e);
 				Log.e(TAG, "close() of connect socket failed", e);
 			}
 		}
 	}
 
 	private void setState(ConnectionState state)
 	{
 		this.currentState = state;
 	}
 
 	synchronized boolean connect(BluetoothDevice device)
 	{
 		// Cancel any thread attempting to make a connection
 		if (currentState == ConnectionState.STATE_CONNECTING)
 		{
 			if (mConnectThread != null)
 			{
 				sendMessage("Cancelling connect thread");
 				mConnectThread.cancel();
 				mConnectThread = null;
 			}
 		}
 
 		// Start the thread to connect with the given device
 		mConnectThread = new ConnectThread(device);
 		mConnectThread.start();
 		setState(ConnectionState.STATE_CONNECTING);
 		return true;
 	}
 
 	private void connected(BluetoothSocket socket, BluetoothDevice device)
 	{
 
 		// Cancel the thread that completed the connection
 		if (mConnectThread != null)
 		{
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		// Cancel any thread currently running a connection
 
 		if (mConnectedThread != null)
 		{
 			mConnectedThread.cancelConnection();
 			mConnectedThread = null;
 		}
 
 		// Start the thread to manage the connection and perform transmissions
 		mConnectedThread = new ConnectedThread(socket);
 		sendMessage("Starting connected thread");
 		mConnectedThread.start();
 
 		// Send the name of the connected device back to the UI Activity
 		sendMessage("Connected to " + device.getName());
 
 		setState(ConnectionState.STATE_CONNECTED);
 	}
 
 	/**
 	 * Indicate that the connection attempt failed and notify the UI Activity.
 	 */
 	private void connectionFailed()
 	{
 		setState(ConnectionState.STATE_NONE);
 
 		// Send a failure message back to the Activity
 		sendMessage("Unable to connect device");
 		broadcast(CONNECTION_LOST);
 	}
 
 	/**
 	 * Indicate that the connection was lost and notify the UI Activity.
 	 */
 	private void connectionLost()
 	{
 		setState(ConnectionState.STATE_NONE);
 
 		// Send a failure message back to the Activity
 
 		sendMessage("Device connection was lost");
 		broadcast(CONNECTION_LOST);
 	}
 
 	/**
 	 * This thread runs during a connection with a remote device. It handles all
 	 * incoming and outgoing transmissions.
 	 */
 	private class ConnectedThread extends Thread
 	{
 		private final BluetoothSocket	mmSocket;
 		private final InputStream		mmInStream;
 		private final OutputStream		mmOutStream;
 		Timer							t	= new Timer("IOTimer", true);
 		private boolean					timerTriggered;
 
 		public ConnectedThread(BluetoothSocket socket)
 		{
 			Log.d(TAG, "create ConnectedThread");
 			mmSocket = socket;
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 			sendMessage("Establishing connection");
 
 			// Get the BluetoothSocket input and output streams
 			try
 			{
 				tmpIn = socket.getInputStream();
 				tmpOut = socket.getOutputStream();
 			}
 			catch (IOException e)
 			{
 				DebugLogManager.INSTANCE.logException(e);
 				Log.e(TAG, "temp sockets not created", e);
 			}
 
 			mmInStream = tmpIn;
 			mmOutStream = tmpOut;
 		}
 
 		public void run()
 		{
 			sendMessage("Starting run()");
 			Log.i(TAG, "BEGIN mConnectedThread");
 			try
 			{
 				Thread.sleep(500);
 			}
 			catch (InterruptedException e1)
 			{
 				e1.printStackTrace();
 			}
 			try
 			{
 				flush();
 
 				if (!verifySignature())
 				{
 					connectionLost();
 					return;
 				}
 				if (!constantsLoaded)
 				{
 					loadConstants(simulated);
 					constantsLoaded = true;
 				}
 				while (running)
 				{
 					getRuntimeVars();
 					calculateValues();
 					logValues();
 					broadcast(NEW_DATA);
 				}
 			}
 			catch (IOException e)
 			{
 				DebugLogManager.INSTANCE.logException(e);
 				Log.e(TAG, "disconnected", e);
 				connectionLost();
 			}
			catch(ArithmeticException e)
			{
                DebugLogManager.INSTANCE.logException(e);
                constantsLoaded = false;
                connectionLost();
			}
 			catch (RuntimeException t)
 			{
 				ErrorReporter.getInstance().handleException(t);
 				DebugLogManager.INSTANCE.logException(t);
 				throw (t);
 			}
 			if (t != null)
 			{
 				t.cancel();
 				t = null;
 			}
 		}
 
 		private void flush() throws IOException
 		{
 			mmOutStream.flush();
 			try
 			{
 				Thread.sleep(getPageActivationDelay());
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			while (mmInStream.available() > 0)
 			{
 				mmInStream.read();
 			}
 		}
 
 		private boolean verifySignature() throws IOException
 		{
 			boolean verified = false;
 			String msSig = null;
 			if (simulated || signatureChecked)
 			{
 				verified = true;
 			}
 			else
 			{
 				byte[] sigCommand = getSigCommand();
 				sendMessage("Verifying MS");
 				Set<String> signatures = Megasquirt.this.getSignature();
 
 				msSig = getSignature(sigCommand, Megasquirt.this.getSigSize());
 				verified = signatures.contains(msSig);
 				if (verified)
 				{
 					trueSignature = msSig;
 				}
 			}
 			if (verified)
 			{
 				sendMessage("Connected to " + trueSignature);
 
 				signatureChecked = true;
 				broadcast(CONNECTED);
 			}
 			else
 			{
 				sendMessage("Signature error! " + msSig);
 			}
 			return verified;
 		}
 
 		private void getRuntimeVars() throws IOException
 		{
 			// Debug.startMethodTracing("getRuntimeVars");
 			if (simulated)
 			{
 				MSSimulator.INSTANCE.getNextRTV(ochBuffer);
 				return;
 			}
 			write(getOchCommand());
 			read(ochBuffer);
 			// Debug.stopMethodTracing();
 		}
 
 		private void calculateValues() throws IOException
 		{
 			calculate(ochBuffer);
 		}
 
 		private void write(byte[] command) throws IOException
 		{
 			this.mmOutStream.write(command);
 			try
 			{
 				Thread.sleep(getInterWriteDelay());
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 		}
 
 		private void read(byte[] bytes) throws IOException
 		{
 			timerTriggered = false;
 			TimerTask cancelTask = new TimerTask()
 			{
 				@Override
 				public void run()
 				{
 					timerTriggered = true;
 					cancelConnection();
 				}
 			};
 
 			t.schedule(cancelTask, 1000);
 
 			int nBytes = bytes.length;
 			int bytesRead = 0;
 			byte[] buffer = new byte[bytes.length];
 			while (bytesRead < nBytes)
 			{
 
 				try
 				{
 					int result = mmInStream.read(buffer, bytesRead, nBytes - bytesRead);
 					if (result == -1)
 						break;
 
 					bytesRead += result;
 				}
 				catch (IOException e)
 				{
 					if (timerTriggered)
 					{
 						DebugLogManager.INSTANCE.log("read timeout occured : read " + bytesRead + " : expected " + nBytes);
 					}
 					throw e;
 				}
 			}
 
 			synchronized (bytes)
 			{
 				System.arraycopy(buffer, 0, bytes, 0, bytes.length);
 			}
 			cancelTask.cancel();
 			t.purge();
 		}
 
 		private String getSignature(byte[] sigCommand, int i) throws IOException
 		{
 			String sig1 = "NoSigReadYet";
 			String sig2 = "Not here";
 			byte[] buf = new byte[i];
 			do
 			{
 				write(sigCommand);
 
 				sig1 = sig2;
 				read(buf);
 				sig2 = new String(buf);
 				flush();
 			}
 			while (!sig1.equals(sig2));
 			return sig1;
 		}
 
 		public void cancelConnection()
 		{
 			try
 			{
 				mmSocket.close();
 			}
 			catch (IOException e)
 			{
 				// ErrorReporter.getInstance().handleException(e);
 				DebugLogManager.INSTANCE.logException(e);
 				Log.e(TAG, "close() of connect socket failed", e);
 			}
 		}
 	}
 
 	protected void getPage(byte[] pageBuffer, byte[] pageSelectCommand, byte[] pageReadCommand) throws IOException
 	{
 		if (mConnectedThread == null)
 		{
 			throw new IOException("Not connected!");
 		}
 
		mConnectedThread.flush();
		if(pageSelectCommand != null)
		{    
		    mConnectedThread.write(pageSelectCommand);
		}
 		delay(getPageActivationDelay());
 		if (pageReadCommand != null)
 		{
 			mConnectedThread.write(pageReadCommand);
 		}
 		delay(getPageActivationDelay());
 		mConnectedThread.read(pageBuffer);
 	}
 
 	public String getTrueSignature()
 	{
 		return trueSignature;
 	}
 
 	public boolean isRunning()
 	{
 		return running;
 	}
 
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
 
 	private void savePage(int pageNo, byte[] buffer)
 	{
 
 		try
 		{
 			File dir = new File(Environment.getExternalStorageDirectory(), "MSLogger");
 			dir.mkdirs();
 
 			String fileName = this.getClass().getName()+".firmware";
 			File outputFile = new File(dir, fileName);
 			BufferedOutputStream out = null;
 			try
 			{
				boolean append = !(pageNo == 1);
                out = new BufferedOutputStream(new FileOutputStream(outputFile, append));
                DebugLogManager.INSTANCE.log("Saving page "+pageNo+" append="+append);
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
 }
