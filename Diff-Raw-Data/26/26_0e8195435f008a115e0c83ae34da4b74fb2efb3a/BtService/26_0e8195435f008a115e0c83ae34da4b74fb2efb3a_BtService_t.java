 package remote.control.android;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import android.app.IntentService;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 
 import common.files.android.Constants;
 
 //import on.hover.android.Command.DriveSignals;
 //import on.hover.android.Command.Engines;
 
 public class BtService extends IntentService
 {
 	String TOGGLE_BLUETOOTH_STATE = "toggleBluetooth";
 	String FIND_BLUETOOTH_DEVICES = "findDevices";
 	String CHOOSE_BLUETOOTH_DEVICE = "chooseDevice";
 	String CONNECT_WITH_BLUETOOTH_DEVICE = "connectDevice";
 	String DISCONNECT_BLUETOOTH_DEVICE = "disconnectDevice";
 	String TOGGLE_BT_BUTTON_TEXT = "toggleBtButtonText";
 	String BT_STATUS = "btStatus";
 	
 	private static String TAG = "JM";
 	protected static final int REQUEST_ENABLE_BT = 1;
 	
 	public List<String> devicesFound = new ArrayList<String>();
 	
 	boolean bluetoothSocketUp = false;
 	boolean listenOnBtInputstream = false;
 
 	String deviceAdress = null;
 	String deviceName = null;
 	int lengthOfDeviceArray = 0;
 
 	BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
 	private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 	private BluetoothSocket mmSocket;
 	private OutputStream mmOutStream;
 	
 	private InputStream mInputStream;
 
 	public BtService() 
 	{
 		super("BtService");
 	}
 
 	@Override
 	public void onCreate()
 	{
 		if(bluetooth.isEnabled())
 		{
 			updateBtButtonText(true);
 		}
 		else
 		{
 			updateBtButtonText(false);
 		}
 
 		super.onCreate();
 		Log.d(TAG,"BtService: start BtService");
 	}
 
 	void updateBtButtonText(boolean status)
 	{
 		Intent toggle = new Intent(TOGGLE_BT_BUTTON_TEXT);
 		toggle.putExtra(BT_STATUS, status);
 		sendBroadcast(toggle);
 	}
 	
 	void toggleBtOnOff()
 	{
 		if(bluetooth.isEnabled())
 		{
 			bluetooth.disable();
 			updateBtButtonText(false);
 		}
 		else
 		{
 			bluetooth.enable();
 			updateBtButtonText(true);
 		}	
 	}
 	
 	@Override
 	protected void onHandleIntent(Intent arg0)
 	{
 		Log.d(TAG, "BTService started");
 		initReceiver();
 
 		while ( true )
 		{
 			try
 			{
 				Thread.sleep(10);
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			if( listenOnBtInputstream )
 			{
 				//TODO implement checkInput here
 				checkInput();
 			}
 		}
 	}
 
 	private void initReceiver()
 	{
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BluetoothDevice.ACTION_FOUND);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.ACTION);
 		filter.addAction("callFunction");
 		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_US_DATA);
 		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_ACC_BRAIN_DATA);
		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_STOP_ACC_BRAIN_DATA);
 		registerReceiver(BtRemoteServiceReciever, filter);
 	}
 	
 	private void findBluetoothDevices()
 	{
 		//Clear list of devices
 		devicesFound.clear();
 
 		if(bluetooth.startDiscovery())	
 		{
 			sendBroadcastInfo("Starts searching...");
 		}
 		else
 		{
 			sendBroadcastInfo("Failed to start search...");
 		}
 	}
 
 	private void printBluetoothDevicesFound()
 	{
 		sendBroadcastInfo("Search finished...");
 		Iterator<String> it = devicesFound.iterator();
 		String devs = "";
 		
 		while(it.hasNext())
 		{
 				devs += (String)it.next() + "\n";
 		}
 		
 		sendBroadcastInfo("Devices found:\n\n" + devs);
 	}
 
 	int i = 0;
 	private void chooseFoundBluetoothDevice()
 	{
 		bluetooth.cancelDiscovery();
 
 		lengthOfDeviceArray = devicesFound.size(); 
 
 		if( lengthOfDeviceArray > 0 )
 		{
 			if( i < ( lengthOfDeviceArray - 2 ) )
 			{
 				//Save adress to selected device
 				deviceAdress = devicesFound.get( 1 + i );
 				deviceName = devicesFound.get( 0  + i );
 				
 				sendBroadcastInfo("Selected device:" + "\n\n" + devicesFound.get( 0  + i )
 									+ "\n" + devicesFound.get(1 + i ));
 				
 
 				i += 2;
 			}
 			else if (i == (lengthOfDeviceArray - 2 ) )
 			{
 				//Save adress to selected device
 				deviceAdress = devicesFound.get( 1 + i );
 				deviceName = devicesFound.get(0  + i );
 				
 				sendBroadcastInfo("Selected device:" + "\n\n" + devicesFound.get( 0  + i )
 									+ "\n" + devicesFound.get(1 + i ));
 				i = 0;
 			}
 			else
 			{
 				i = 0;
 			}
 		}
 		else
 		{
 			sendBroadcastInfo("No devices found...");
 		}
 	}
 
 	private void connectDevice() throws IOException 
 	{
 		if( !bluetoothSocketUp )
 		{
 			BluetoothDevice device = null;
 			BluetoothSocket temp = null;
 			mmSocket = null;
 
 
 			if( deviceAdress != null )
 			{
 				device = bluetooth.getRemoteDevice(deviceAdress);
 
 				try
 				{
 					// MY_UUID is the app's UUID string, also used by the server code
 					temp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
 					mmSocket = temp;
 					bluetoothSocketUp = true;
 
 				}
 				catch (IOException e)
 				{
 					mmSocket = null;
 					bluetoothSocketUp = false;
 					sendBroadcastInfo("Failed to create socket...");
 				}
 
 				if( bluetoothSocketUp )
 				{
 					try
 					{
 						mmSocket.connect();
 						sendBroadcastInfo("Connected to device...");
 						startReadingBluetoothInputstream();
 					}
 					catch (IOException connectException)  
 					{
 						btConnectionLost("Connection failed...");
 					}
 				}
 			}
 			else
 			{
 				sendBroadcastInfo("No selected device...");
 			}
 		}	
 	}
 	
 	private void startReadingBluetoothInputstream()
 	{
 		if( bluetoothSocketUp )
 		{
 			try
 			{
 				mInputStream = mmSocket.getInputStream();
 				sendBroadcastInfo("Connected to: " + deviceName + "\n" + "Input stream open...");
 				listenOnBtInputstream = true;
 			}
 			catch (IOException e)
 			{
 				btConnectionLost("Connection lost...");
 				sendBroadcastInfo("Failed to open input stream...");
 				listenOnBtInputstream = false;
 				return;
 			}
 		}
 	}
 	
 	void checkInput()
 	{
 		byte[] bufferInfo = new byte[3];
 		try
 		{
 			bufferInfo[0] = (byte) mInputStream.read(); // Command
 			bufferInfo[1] = (byte) mInputStream.read(); // Target
 			bufferInfo[2] = (byte) mInputStream.read(); // Message length
 		} 
 		catch (IOException e1)
 		{
 			btConnectionLost("Lost connection...");
 			Log.d(TAG,"BtService: BufferInfo read failed");
 			return;
 		}
 		
 		byte[] bufferMessage = new byte[(int) bufferInfo[2]];
 		try
 		{
 			mInputStream.read(bufferMessage, 0, (int) bufferInfo[2]);
 		} 
 		catch (IOException e1) 
 		{
 			btConnectionLost("Lost connection...");
 			Log.d(TAG,"BtService: BufferMessage read failed");
 			return;
 		}
 
 		Log.d(TAG, "bufferInfo[0]"+bufferInfo[0]);
 		Log.d(TAG, "bufferInfo[1]"+bufferInfo[1]);
 		Log.d(TAG, "bufferInfo[2]"+bufferInfo[2]);
 
 		// commands from ADK to this device
 		if(Constants.TARGET_BRAIN == bufferInfo[1])
 		{
 			//handleBrainCommands(bufferInfo, bufferMessage);
 		}
 		// commands from remote to remote. Never used?
 		else if(Constants.TARGET_REMOTE == bufferInfo[1])
 		{
 			if(Constants.LOG_US_SENSOR_COMMAND == bufferInfo[0])
 			{
 				sendBroadcastMessage("Message received:\n" + String.valueOf(bufferInfo[0]));
 				
 				Intent usSensors = new Intent(Constants.Broadcast.LogService.Actions.ADK_US_RESPONSE);
 				usSensors.putExtra(Constants.Broadcast.LogService.Actions.Intent.BYTES, bufferMessage);
 				sendBroadcast(usSensors);
 			}
			else if(Constants.LOG_ACC_BRAIN_SENSOR_COMMAND == bufferInfo[0])
			{
				sendBroadcastMessage("Message received:\n" + String.valueOf(bufferInfo[0]));
			}
 						
 			//sendCommand(bufferInfo[0], bufferInfo[1], bufferMessage);
 		}
 		// commands from remote to ADK.
 		else if(Constants.TARGET_ADK == bufferInfo[1])
 		{
 			//broadcastBufferToUSBService(bufferInfo, bufferMessage);
 		}
 	}
 
 	private void sendData(byte[] data)
 	{	
 		try 
 		{
 			mmOutStream = mmSocket.getOutputStream();
 		} 
 		catch (IOException e) 
 		{
 			btConnectionLost("Connection lost...");
 		}
 
 		try 
 		{
 			mmOutStream.write(data);
 		} 
 		catch (IOException e) 
 		{
 			btConnectionLost("Connection lost...");
 		}
 	}
 
 	private void btConnectionLost(String message)
 	{
 		listenOnBtInputstream = false;
 		
 		if(bluetoothSocketUp) //ADDED THIS??? check if lost connection does not work
 		{
 			try
 			{
 				mmSocket.close();
 				bluetoothSocketUp = false;
 			}
 			catch (IOException e)
 			{ 
 				
 			}
 		}
 
 		//Disable transmission
 		Intent disableMS = new Intent(Constants.Broadcast.MotorSignals.Remote.DISABLE_TRANSMISSION);
 		sendBroadcast(disableMS);
 		sendBroadcastInfo(message);
 	}
 
 	private void sendBroadcastInfo(String message)
 	{
 		Intent i = new Intent("printMessage");
 		i.putExtra("message", message);
 		sendBroadcast(i);
 	}
 	
 	private void sendBroadcastMessage(String message)
 	{
 		Intent i = new Intent("printMessage");
 		i.putExtra("coordinates", message);
 		sendBroadcast(i);
 	}
 	
 	private final BroadcastReceiver BtRemoteServiceReciever = new BroadcastReceiver()
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			String action = intent.getAction();
 
 			// When discovery finds a device
 			if (BluetoothDevice.ACTION_FOUND.equals(action))
 			{
 				// Get the BluetoothDevice object from the Intent
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 
 				if(!devicesFound.contains(device.getName()))
 				{
 					devicesFound.add(device.getName());
 					devicesFound.add(device.getAddress());
 					sendBroadcastInfo("Found device:\n\n" + device.getName() + "\n" + device.getAddress());
 				}
 			}
 
 			//When discovery is finished, change the Activity title
 			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
 			{
 				//Print list of found devices
 				printBluetoothDevicesFound();
 			}
 
 			if(action.equalsIgnoreCase("callFunction"))
 			{
 				if(intent.hasExtra(TOGGLE_BLUETOOTH_STATE))
 				{
 					toggleBtOnOff();
 				}
 			
 				if(intent.hasExtra(FIND_BLUETOOTH_DEVICES))
 				{
 					findBluetoothDevices();
 				}
 
 				if(intent.hasExtra(CHOOSE_BLUETOOTH_DEVICE))
 				{
 					chooseFoundBluetoothDevice();
 				}
 				
 				if(intent.hasExtra(DISCONNECT_BLUETOOTH_DEVICE))
 				{
 					if( bluetoothSocketUp )
 						btConnectionLost("Connection lost...");
 				}
 
 				if(intent.hasExtra(CONNECT_WITH_BLUETOOTH_DEVICE))
 				{
 					if( !bluetoothSocketUp && deviceAdress != null)
 					{
 						try
 						{
 							connectDevice();
 						}
 						catch (IOException e)
 						{
 							sendBroadcastInfo("Failed to connect...");
 						}
 					}
 				}
 			}
 			
 			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.ACTION))
 			{
 				byte command = intent
 					.getByteExtra(
 						Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.COMMAND,
 						(byte) 0);
 				byte target = intent
 					.getByteExtra(
 						Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET,
 						(byte) 0);
 				byte[] bytes = intent
 					.getByteArrayExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.BYTES);
 				sendProtocol(command, target, bytes);
 			}
 			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_US_DATA))
 			{
 				byte[] requestUsAdk = new byte[1];
 				requestUsAdk[0] = Constants.TARGET_REMOTE;
 				
 				if( bluetoothSocketUp )
 					sendProtocol(Constants.US_SENSOR_REQ_COMMAND,Constants.TARGET_ADK,requestUsAdk);
 			}
 			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_ACC_BRAIN_DATA))
 			{
 				byte[] requestAccBrain = new byte[1];
 				requestAccBrain[0] = Constants.TARGET_REMOTE;
 				
 				if( bluetoothSocketUp )
 					sendProtocol(Constants.ACC_BRAIN_SENSOR_REQ_COMMAND,Constants.TARGET_BRAIN,requestAccBrain);
 			}
			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_STOP_ACC_BRAIN_DATA))
			{
				byte[] requestAccBrain = new byte[1];
				requestAccBrain[0] = Constants.TARGET_REMOTE;
				
				if( bluetoothSocketUp )
					sendProtocol(Constants.ACC_BRAIN_SENSOR_STOP_REQ_COMMAND,Constants.TARGET_BRAIN, requestAccBrain);
			}
 		}
 	};
 
 	// TODO: Should it give any status as return?
 	public void sendProtocol(byte command, byte target, byte[] message)
 	{
 		int messageLength = message.length;
 		byte[] dataTransmitt = new byte[3 + messageLength];
 		dataTransmitt[0] = command;
 		dataTransmitt[1] = target;
 		dataTransmitt[2] = (byte) messageLength;
 		System.arraycopy(message, 0, dataTransmitt, 3, messageLength);
 		sendData(dataTransmitt);
 	}
 	
 	//THIS happens when phone locks, wanted??
 	@Override
 	public void onDestroy()
 	{
 		Log.d(TAG, "IntentService onDestroy");
 		btConnectionLost("Destroy..");
 		unregisterReceiver(BtRemoteServiceReciever);
 	}
 }
