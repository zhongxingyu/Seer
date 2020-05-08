 package ru.serjik.arduinopixels;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.UUID;
 
 import ru.serjik.arduinopixels.BluetoothDeviceReciever.OnBluetoothDeviceListener;
 import android.app.Application;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.IntentFilter;
 import android.util.Log;
 
 public class app extends Application implements OnBluetoothDeviceListener
 {
 	public static cfg cfg;
 
 	private BluetoothDeviceReciever bluetoothDeviceReciever = new BluetoothDeviceReciever(this);
 
 	private static BluetoothAdapter btAdapter = null;
 	private static BluetoothSocket btSocket = null;
 	private static OutputStream outStream = null;
 	private static InputStream inStream = null;
 
 	private static BluetoothState bluetoothState = BluetoothState.DICONNECTED;
 
 	@Override
 	public void onCreate()
 	{
 		cfg = new cfg(this, "settings.txt");
 		registerReceiver(bluetoothDeviceReciever, new IntentFilter(BluetoothDevicePicker.ACTION_DEVICE_SELECTED));
 		super.onCreate();
 	}
 
 	@Override
 	public void onTerminate()
 	{
 		unregisterReceiver(bluetoothDeviceReciever);
 		super.onTerminate();
 	}
 
 	@Override
 	public void onBluetoothDevice(BluetoothDevice device)
 	{
 		cfg.set(cfg.DEVICE_MAC_ADDRESS, device.getAddress());
 		Log.v("app", "onBluetoothDevice " + device.getAddress());
 	}
 
 	@Override
 	public void onBluetoothDisconnected()
 	{
 		Log.v("app", "onBluetoothDisconnected");
 		bluetoothState = BluetoothState.DICONNECTED;
 	}
 
 	@Override
 	public void onBluetoothDisconnectRequest()
 	{
 		Log.v("app", "onBluetoothDisconnectRequest");
 		bluetoothState = BluetoothState.DICONNECTED;
 	}
 
 	public static void connect()
 	{
 		try
 		{
 			if (bluetoothState == BluetoothState.DICONNECTED)
 			{
 				final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 				btAdapter = BluetoothAdapter.getDefaultAdapter();
 				BluetoothDevice device = btAdapter.getRemoteDevice(cfg.get(cfg.DEVICE_MAC_ADDRESS));
 
 				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
 				btSocket.connect();
 				outStream = btSocket.getOutputStream();
 				inStream = btSocket.getInputStream();
 				bluetoothState = BluetoothState.CONNECTED;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void send(byte[] data)
 	{
 		Log.v("ArduinoPixels", "start sending. len = " + data.length);
 		try
 		{
 			if (bluetoothState == BluetoothState.CONNECTED)
 			{
 				bluetoothState = BluetoothState.SENDING;
 
 				int position = 0;
 				int maxBlockSize = 32;
 
 				while (position < data.length)
 				{
 					int blockSize = data.length - position;
 
 					if (blockSize > maxBlockSize)
 					{
 						blockSize = maxBlockSize;
 					}
 
 					outStream.write(blockSize);
 
 					Log.v("ArduinoPixels", "sended block size = " + blockSize);
 
 					int xorValueForBlock = 0;
 
 					for (int i = 0; i < blockSize; i++)
 					{
 						xorValueForBlock = (xorValueForBlock ^ (int) (data[position] & 0xff));
 						outStream.write(data[position]);
 						position++;
 					}
 
 					Log.v("ArduinoPixels", "sended block data");
 
 					// int t= inStream.read();
 
 					if (xorValueForBlock != (int) (inStream.read() & 0xff))
 					{
 						throw new IOException("incorrect xor value");
 					}
					Log.v("ArduinoPixels", "recieved correct xor byte");
 
 				}
 				outStream.write(0);
 				Log.v("ArduinoPixels", "sended end block");
 
 				bluetoothState = BluetoothState.CONNECTED;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void disconnect()
 	{
 		try
 		{
 			if (bluetoothState != BluetoothState.DICONNECTED)
 			{
 				outStream.close();
 				btSocket.close();
 				bluetoothState = BluetoothState.DICONNECTED;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private enum BluetoothState
 	{
 		DICONNECTED, CONNECTED, SENDING;
 	}
 
 }
