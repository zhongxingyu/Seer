 package com.mro.bluetooth;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 import android.bluetooth.BluetoothSocket;
 import android.os.Handler;
 import android.util.Log;
 
public class BluetoothConnection extends Thread implements
		BluetoothSocketHandler {
 	public final static String TAG = BluetoothConnection.class.getSimpleName();
 
 	private BufferedReader reader;
 	private BufferedWriter writer;
 	private boolean shouldContinue;
 	private BluetoothSocket socket;
 	private Handler handler;
 
 	public BluetoothConnection(BluetoothSocket socket, Handler handler) {
 		this.handler = handler;
 		this.shouldContinue = true;
 
 		handleSocket(socket);
 	}
 
 	public BluetoothConnection(Handler handler) {
 		this.handler = handler;
 		this.shouldContinue = true;
 	}
 
 	@Override
 	public void handleSocket(BluetoothSocket socket) {
 		Log.d(TAG, "Handling socket!");
 
 		this.socket = socket;
 
 		BufferedInputStream inStream = null;
 		OutputStream outStream = null;
 
 		try {
 			inStream = new BufferedInputStream(socket.getInputStream());
 			outStream = new BufferedOutputStream(socket.getOutputStream());
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to create connection streams!", e);
 		}
 
 		reader = new BufferedReader(new InputStreamReader(inStream));
 		writer = new BufferedWriter(new OutputStreamWriter(outStream));
 	}
 
 	public void run() {
 		String line;
 
 		try {
 			while (shouldContinue) {
 
				if (socket != null && reader != null) {
 					if ((line = reader.readLine()) != null) {
 						handler.obtainMessage(MAX_PRIORITY, line)
 								.sendToTarget();
 					}
 				}
 
 				Thread.sleep(200);
 			}
 		} catch (IOException e) {
 			Log.e(TAG,
 					"Failed to read from Bluetooth socket! Have fun debugging!",
 					e);
 		} catch (InterruptedException e) {
 			Log.d(TAG, "I am out of my slumbering sleep!");
 		}
 	}
 
 	public void write(String output) {
 		try {
			if (writer != null) {
				writer.write(output + "\n");
 
				// Should we try to flush it right away?
				// not really sure here, I would imagine so
				writer.flush();
			}
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to write to stream!", e);
 		}
 	}
 
 	public void stopConnection() {
 		shouldContinue = false;
 	}
 }
