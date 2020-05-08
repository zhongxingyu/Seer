 package com.prototype.cruise;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Set;
 import java.util.UUID;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.Toast;
 
 public class BluetoothBackEnd {
 
 	private BluetoothAdapter mBluetoothAdapter;
 	private BluetoothSocket mmSocket;
 	private BluetoothDevice mmDevice;
 	private OutputStream mmOutputStream;
 	private InputStream mmInputStream;
 	private Thread workerThread;
 	private byte[] readBuffer;
 	private int readBufferPosition;
 	private volatile boolean stopWorker;
 	private String data, findStatus, openStatus;
 	static JSONObject jObj = null;
 
 	StringBuilder sb = new StringBuilder();
 
 	public BluetoothBackEnd() {
 	}
 
 	public String getFindStatus() {
 		return findStatus;
 	}
 
 	public String getOpenStatus() {
 		return openStatus;
 	}
 
 	public String getData() {
 		return data;
 	}
 
 	public void findBT(boolean show, Context c) {
 
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (mBluetoothAdapter == null) {
 			findStatus = "No bluetooth adapter available";
 		}
 
 		if (!mBluetoothAdapter.isEnabled()) {
 			Intent enableBluetooth = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			((Activity) c).startActivityForResult(enableBluetooth, 0);
 		}
 
 		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
 				.getBondedDevices();
 		if (pairedDevices.size() > 0) {
 			for (BluetoothDevice device : pairedDevices) {
 				if (device.getName().equals("RN42-DF28")) {
 					mmDevice = device;
 					findStatus = "Bluetooth Device Found";
					try {
						openBT(show, c);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
 					break;
 				}
 
 			}
 		}
 		if (show)
 			Toast.makeText(c.getApplicationContext(), findStatus,
 					Toast.LENGTH_SHORT).show();
 
 	}
 
 	public void openBT(boolean show, Context c) throws IOException {
 		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard
 																				// SerialPortService
 																				// ID
 		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
 		mmSocket.connect();
 		mmOutputStream = mmSocket.getOutputStream();
 		mmInputStream = mmSocket.getInputStream();
 
 		beginListenForData(c);
 
 		openStatus = "Bluetooth Opened";
 		if (show)
 			Toast.makeText(c.getApplicationContext(), openStatus,
 					Toast.LENGTH_SHORT).show();
 	}
 
 	void beginListenForData(final Context c) {
 		final byte delimiter = 10; // This is the ASCII code for a newline
 									// character
 
 		stopWorker = false;
 		readBufferPosition = 0;
 		readBuffer = new byte[1024];
 		workerThread = new Thread(new Runnable() {
 			public void run() {
 				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
 					try {
 						int bytesAvailable = mmInputStream.available();
 						if (bytesAvailable > 0) {
 							byte[] packetBytes = new byte[bytesAvailable];
 							mmInputStream.read(packetBytes);
 							for (int i = 0; i < bytesAvailable; i++) {
 								byte b = packetBytes[i];
 								if (b == delimiter) {
 									byte[] encodedBytes = new byte[readBufferPosition];
 									System.arraycopy(readBuffer, 0,
 											encodedBytes, 0,
 											encodedBytes.length);
 									final String data = new String(
 											encodedBytes, "US-ASCII");
 									readBufferPosition = 0;
 									sb.append(data);
 									
 									//Check if last line, then open afteractivity with the string from stringbuilder
 									if (i + 1 == bytesAvailable) {
 										Intent in = new Intent(c.getApplicationContext(),AfterDriveActivity.class);
 										in.putExtra("btdata", sb.toString());
 										c.startActivity(in);
 									}
 
 								} else {
 									readBuffer[readBufferPosition++] = b;
 								}
 							}
 						}
 					} catch (IOException ex) {
 						stopWorker = true;
 					}
 				}
 			}
 
 		});
 
 		workerThread.start();
 	}
 
 
 	public void closeBT(boolean show, Context c) throws IOException {
 		stopWorker = true;
 		mmOutputStream.close();
 		mmInputStream.close();
 		mmSocket.close();
 		openStatus = "Bluetooth Closed";
 		if (show)
 			Toast.makeText(c.getApplicationContext(), openStatus,
 					Toast.LENGTH_SHORT).show();
 
 	}
 }
