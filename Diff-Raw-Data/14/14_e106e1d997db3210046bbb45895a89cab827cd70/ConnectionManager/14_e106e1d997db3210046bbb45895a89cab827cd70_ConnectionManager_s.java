 package com.example.Bluetooth;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.bluetooth.BluetoothSocket;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class ConnectionManager extends AsyncTask<Void, String, Void>
 {
 	private BluetoothSocket mSocket;
 	private DataInputStream mInput;
 	private DataOutputStream mOutput;
 	private final UILink mLink;
 	private boolean mIsRunning;
 	private boolean mIsConnected;
 	private String mDeviceName;
 
 	public ConnectionManager(BluetoothSocket socket, UILink updater)
 	{
 		mSocket = socket;
 		mLink = updater;
 		mIsConnected = true;
 		InputStream tmpInput = null;
 		OutputStream tmpOutput = null;
 		try
 		{
 			tmpInput = mSocket.getInputStream();
 			tmpOutput = mSocket.getOutputStream();
 		} catch (Exception er)
 		{
 			Log.d("BLT", "Couldn't obtain the streams from socket!");
 		}
 		mInput = new DataInputStream(tmpInput);
 		mOutput = new DataOutputStream(tmpOutput);
 		mDeviceName = mSocket.getRemoteDevice().getName();
 	}
 
 	protected synchronized void onPreExecute()
 	{
 		Log.d("BLT", "Connection Manager for the device " + mDeviceName + " is starting!");
 	}
 
 	public Void doInBackground(Void... params)
 	{
 		mIsRunning = true;
 
 		boolean receivedExpectedPing = false;
		double timePassedInSeconds = 0;
 
 		while (mIsRunning && mIsConnected)
 		{
 			try
 			{
 				if (mInput.available() > 0)
 				{
 					// this is not safe
 					String readString = mInput.readUTF();
 					if (readString != null)
 					{
 						if (readString.equals("!Ping!"))
 						{
 							receivedExpectedPing = true;
							timePassedInSeconds = 0.0;
 							Log.d("BLT", "Received ping from " + mDeviceName + "!");
 						}
 						this.publishProgress(readString);
 					}
 				}
 
 				if (receivedExpectedPing)
 				{
					if (timePassedInSeconds == 1.0)
 					{
 						write("!Ping!");
 						receivedExpectedPing = false;
 						timePassedInSeconds = 0;
 						Log.d("BLT", "Sending ping to " + mDeviceName + "!");
 					}
 				} else
 				{
					if (timePassedInSeconds > 2.0)
 					{
 						mIsConnected = false;
 						Log.d("BLT", "Device " + mDeviceName + " is gone!");
 					}
 				}
 
				timePassedInSeconds += 0.05;
 				Thread.sleep(20);
 			} catch (Exception er)
 			{
 				Log.d("BLT", er.getMessage());
 				break;
 			}
 		}
 		return null;
 	}
 
 	protected synchronized void onProgressUpdate(String... strings)
 	{
 		if (mLink != null)
 			mLink.useData(strings);
 	}
 
 	protected synchronized void onPostExecute(Void result)
 	{
 		Log.d("BLT", "Connection Manager stopped successfully!");
 
 	}
 
 	public synchronized void write(String data)
 	{
 		try
 		{
 			mOutput.writeUTF(data);
 			mOutput.flush();
 		} catch (Exception er)
 		{
 		}
 	}
 
 	public synchronized void stop()
 	{
 		mIsRunning = false;
 		try
 		{
 			mInput.close();
 
 		} catch (Exception e)
 		{
 		}
 
 		try
 		{
 			mOutput.close();
 
 		} catch (Exception e)
 		{
 		}
 		try
 		{
 			mSocket.close();
 		} catch (Exception e)
 		{
 		}
 		mIsConnected = false;
 
 	}
 }
