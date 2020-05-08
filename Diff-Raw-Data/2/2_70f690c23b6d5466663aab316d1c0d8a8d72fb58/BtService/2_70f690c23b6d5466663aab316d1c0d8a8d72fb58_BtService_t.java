 package on.hovercraft.android;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.UUID;
 
 import common.files.android.Constants;
 import common.files.android.Constants.ConnectionState;
 
 import android.annotation.SuppressLint;
 import android.app.IntentService;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 
 public class BtService extends IntentService
 {
 	private static final UUID  MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 	private static final String NAME = "Bluetooth SPP";	
 	
 	public static ConnectionState connectionState = ConnectionState.DISCONNECTED; // BT connection state	
 	
 	private boolean listenOnBtInputstream = false;				/**< */
 	private BluetoothSocket bluetoothSocket;					/**< */
 	private BluetoothServerSocket bluetoothServerSocket;		/**< */
 	private BluetoothAdapter mBluetoothAdapter;					/**< */
 	private InputStream btInputStream;							/**< */
 	private OutputStream btOutStream;							/**< */
 	private int bluetoothConnectionTimeout = 30000;				/**< */
 	private boolean bluetoothServerUp = false;					/**< */
 	private boolean bluetoothSocketUp = false;					/**< */
 	private static String TAG = "JM";							/**< */
 
 	/**
 	* \brief 
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	public BtService() 
 	{
 		super("BtService");
 	}
 
 	/**
 	* \brief Update the Bluetooth connection state
 	*
 	* Description
 	*
 	* @param state 
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private void updateBtConnectionState( ConnectionState state )
 	{
 		if( connectionState != state )
 		{
 			connectionState = state;
 			Intent i = new Intent(Constants.Broadcast.BluetoothService.UPDATE_CONNECTION_STATE);
 			i.putExtra("connectionState", state);
 			sendBroadcast(i);
 		}
 	}	
 
 	/**
 	* \brief 
 	*
 	* Description
 	*
 	*
 	* @param arg0
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	@Override
 	protected void onHandleIntent( Intent arg0 ) 
 	{
 		Log.d(TAG, "BTService started");
 		registerReceiver(BtServiceReciever, new IntentFilter("callFunction"));
 
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
 				checkInput();
 			}
 		}
 	}
 	
 	/**
 	* \brief 
 	*
 	* 
 	*
 	*
 	* @param message
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private void btConnectionLost( String message )
 	{
 		listenOnBtInputstream = false;
 		closeServerSocket();
 
 		if( btInputStream != null )
 		{
 			try 
 			{
 				btInputStream.close();
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		broadcastMessage(message);
 		updateBtConnectionState(ConnectionState.DISCONNECTED);
 	}
 	
 	/**
 	* \brief 
 	*
 	* Desription
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private void closeServerSocket()
 	{
 		if( bluetoothServerUp )
 		{
 			try 
 			{
 				bluetoothServerSocket.close();
 				broadcastMessage("Server down...");
 				bluetoothServerUp = false;
 				bluetoothSocketUp = false;
 			} 
 			catch (IOException e) 
 			{	
 				broadcastMessage("Faild to close server...");
 			}
 		}
 	}
 
 	/**
 	* \brief 
 	*
 	* Description
 	*
 	*
 	*
 	* @return bluetoothServerUp
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private boolean setupServer()
 	{
 		// Use a temporary object that is later assigned to mmServerSocket,
 		// because mmServerSocket is final
 		mBluetoothAdapter = null;
 		BluetoothServerSocket tmp = null;
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		try 
 		{
 			// MY_UUID is the app's UUID string, also used by the client code
 			tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME,  MY_UUID_INSECURE);
 			broadcastMessage("Server up...");
 			bluetoothServerUp = true;
 			updateBtConnectionState(ConnectionState.WAITING);
 		} 
 		catch (IOException e) 
 		{ 
 			btConnectionLost("Failed to create setup server...");
 			bluetoothServerUp = false;
 		}
 
 		bluetoothServerSocket = tmp;
 		return bluetoothServerUp;
 	}
 
 	/**
 	* \brief 
 	*
 	* Description
 	*
 	*
 	*
 	* @return bluetoothSocketUp
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private boolean waitToConnect()
 	{
 		if(bluetoothServerUp)
 		{
 			bluetoothSocket = null;
 			try 
 			{
 				bluetoothSocket = bluetoothServerSocket.accept(bluetoothConnectionTimeout);
 				bluetoothSocketUp = true;
 				broadcastMessage("Socket is up..");
 				updateBtConnectionState(ConnectionState.CONNECTED);
 				
 				listenOnBtInputStream();
 			} 
 			catch (IOException e) 
 			{
 				btConnectionLost("Failed to connect...");
 			}
 		}
 		else
 		{
 			bluetoothSocketUp = false;
 		}
 
 		return bluetoothSocketUp;      
 	}
 
 	/**
 	* \brief 
 	*
 	* Description
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	@SuppressLint("HandlerLeak")
 	private void listenOnBtInputStream()
 	{
 		if( bluetoothSocketUp )
 		{
 			try
 			{
 				btInputStream = bluetoothSocket.getInputStream();
 				broadcastMessage("Input stream open...");
 				listenOnBtInputstream = true;
 			}
 			catch (IOException e)
 			{
 				btConnectionLost("Failed to open input stream...");
 				listenOnBtInputstream = false;
 				return;
 			}
 		}
 	}
 
 	/**
 	* \brief Read the bluetooth input stream
 	*
 	* Description
 	*
 	*
 	*
 	*
 	* \author Jens Moser
 	*
 	*/
 	void checkInput()
 	{
 		byte[] bufferInfo = new byte[3];		
 		try
 		{
 			bufferInfo[0] = (byte) btInputStream.read(); // Command
 			bufferInfo[1] = (byte) btInputStream.read(); // Target
 			bufferInfo[2] = (byte) btInputStream.read(); // Message length
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
 			btInputStream.read(bufferMessage, 0, (int) bufferInfo[2]);
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
 			handleBrainCommands(bufferInfo, bufferMessage);
 		}
 		// commands from remote to remote. Never used?
 		else if(Constants.TARGET_REMOTE == bufferInfo[1])
 		{
 			sendCommand(bufferInfo[0], bufferInfo[1], bufferMessage);
 		}
 		// commands from remote to ADK.
 		else if(Constants.TARGET_ADK == bufferInfo[1])
 		{
 			broadcastBufferToUSBService(bufferInfo, bufferMessage);
 		}
 	}
 	
 	/**
 	* \brief 
 	*
 	* 
 	*
 	*
 	* @param bufferInfo
 	*
 	* @param bufferMessage
 	*
 	* @return Description
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private void broadcastBufferToUSBService( byte[] bufferInfo, byte[] bufferMessage )
 	{
 		Log.d(TAG,"BtService: broadcast command to USB Service");
 		Intent intent = new Intent("handleBTCommands");
 		intent.putExtra("bufferInfo", bufferInfo);
 		intent.putExtra("bufferMessage", bufferMessage);
 		sendBroadcast(intent);
 	}
 
 	/**
 	* \brief 
 	*
 	* 
 	*
 	*
 	* @param bufferInfo
 	*
 	* @param bufferMessage
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private void handleBrainCommands( byte[] bufferInfo, byte[] bufferMessage )
 	{
 		Log.d(TAG,"BtService: handleBrainCommand");
 		switch (bufferInfo[0])
 		{
 			case Constants.MOTOR_SIGNAL_COMMAND:
 				
 				Intent intent = new Intent(Constants.Broadcast.MotorSignals.REMOTE);
 				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.COMMAND, bufferInfo[0]);
 				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.BYTES, bufferMessage);
 				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET, bufferInfo[1]);
 				sendBroadcast(intent);
 				break;
 			
 			case Constants.ACC_BRAIN_SENSOR_REQ_COMMAND:
 				Log.d(TAG, "READY TO GET ACC BRAIN DATA");
 				
 				Intent accBrainIntent = new Intent("accBrainReq");
 				accBrainIntent.putExtra("send", true);
 				sendBroadcast(accBrainIntent);
 				break;
 			
 			case Constants.ACC_BRAIN_SENSOR_STOP_REQ_COMMAND:
 				Log.d(TAG, "READY TO STOP ACC BRAIN DATA");
 				
 				Intent accBrainStopIntent = new Intent("accBrainReq");
 				accBrainStopIntent.putExtra("send", false);
 				sendBroadcast(accBrainStopIntent);
 				break;
 				
 			default:
 				Log.d(TAG, "unknown command: " + bufferInfo[0]);
 				break;
 		}	
 	}
 
 	/**
 	* \brief 
 	*
 	* Description
 	*
 	*
 	* @param command
 	*
 	* @param target
 	* 
 	* @param message
 	*
 	*
 	* \author Jens Moser
 	*
 	*/
 	private void sendCommand(byte command, byte target, byte[] message)
 	{
 		Log.d(TAG,"BtService: SendCommand:" + (int) command);
 		int byteLength = message.length;
 		byte[] buffer = new byte[3+byteLength];
 
 		buffer[0] = command; // command
 		buffer[1] = target; // target
 		buffer[2] = (byte) byteLength; // length
 
 		for (int x = 0; x < byteLength; x++) 
 		{
 			buffer[3 + x] = message[x]; // message
 		}
 
 		Log.d(TAG,"byteLength:"+byteLength);
 		
 		try 
 		{
 			btOutStream = bluetoothSocket.getOutputStream();
 		} 
 		catch (IOException e) 
 		{
 			btConnectionLost("Lost connection...");
 		}		
 		
 		if ( btOutStream != null )
 		{
 			try 
 			{
 				btOutStream.write(buffer);
 			} 
 			catch (IOException e) 
 			{
 				btConnectionLost("Lost connection...");
 				Log.e(TAG, "write failed", e);
 			}
 		}
 	}	
 
 	/**
 	* \brief 
 	*
 	* 
 	*
 	*
 	* @param message
 	*
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private void broadcastMessage(String message)
 	{
 		Intent i = new Intent("printMessage");
 		i.putExtra("message", message);
 		sendBroadcast(i);
 	}
 	
 	/**
 	* \brief BtService BroadcastReceiver
 	*
 	* 
 	*
 	*
 	* @param context 
 	*
 	* @param intent
 	*
 	* \author Johan Gustafsson
 	*
 	*/
 	private final BroadcastReceiver BtServiceReciever = new BroadcastReceiver() 
 	{
 		@Override
 		public void onReceive(Context context, Intent intent) 
 		{
 			String action = intent.getAction();
 
 			//function called
 			if(action.equalsIgnoreCase("callFunction"))
 			{
 				//witch function
 				if(intent.hasExtra("setupServer"))
 				{
 					Log.d(TAG, "BtService: setupServer");
 					if( !bluetoothServerUp )
 						setupServer();
 				}	
 
 				if(intent.hasExtra("waitToConnect"))
 				{
 					if( !bluetoothSocketUp )
 						waitToConnect();
 				}
 				if(intent.hasExtra("sendToRemote"))
 				{
 					if(intent.hasExtra("combinedInfoAndPB"))
 					{
 						byte[] infoAndPB = intent.getByteArrayExtra("combinedInfoAndPB");
 						byte command = infoAndPB[0];
 						byte target = infoAndPB[1];
 						
 						int messageLength = (int)infoAndPB[2];
 						byte[] message = new byte[messageLength];
 						
						for(int i = 0; i < messageLength; i++)
 						{
 							message[i] = infoAndPB[3 + i];
 						}
 						
 						if( bluetoothSocketUp )
 							sendCommand(command, target, message);
 					}
 					else if(intent.hasExtra("onlyMessage"))
 					{
 						byte[] message = intent.getByteArrayExtra("onlyMessage");
 						byte command = Constants.LOG_ACC_BRAIN_SENSOR_COMMAND;
 						byte target = Constants.TARGET_REMOTE;
 						
 						if( bluetoothSocketUp )
 							sendCommand(command, target, message);
 					}
 				}
 			}
 		}
 	};
 }
