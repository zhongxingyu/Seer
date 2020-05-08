 package com.example.plottercontrol;
 
import java.io.BufferedOutputStream;
 import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.nio.ByteBuffer;
 import java.util.UUID;
 
import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BluetoothController {
 	private static final String TAG = MainActivity.class.getSimpleName();
 
 	private BluetoothSocket mBluetoothSocket;
 	private static final String UUID_NXT = "00001101-0000-1000-8000-00805F9B34FB";
 
 	private PipedInputStream inDataStream;
	private BufferedOutputStream mBTOutputStream;
 
 	public BluetoothController(PipedOutputStream dataStream) {
 		try {
 			inDataStream = new PipedInputStream(dataStream);
 		} catch (IOException e) {
 			Log.wtf(TAG, "Stream is already connected!?");
 		}
 	}
 
 	public boolean connectToBluetoothDevice(BluetoothDevice bluetoothDevice) {
 		try {
 			mBluetoothSocket = bluetoothDevice
 					.createRfcommSocketToServiceRecord(UUID
 							.fromString(UUID_NXT));
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
			mBluetoothSocket.connect();
			mBTOutputStream = new BufferedOutputStream(mBluetoothSocket.getOutputStream());
 		} catch (IOException e) {
 			Log.w(TAG, "Unable to create a socket!");
 			return false;
 		}
 		return true;
 	}
 
 	public void send(Context con) {
 		try {
 			byte[] numberOfLinesByte = new byte[4];
 			inDataStream.read(numberOfLinesByte);
 			int numberOfLines = ByteBuffer.wrap(numberOfLinesByte).getInt();
 
 			for (int a = 0; a < numberOfLines; a++) {
 				Toast.makeText(con, "Points in Line " + a, Toast.LENGTH_SHORT)
 						.show();
 				byte[] lengthBytes = new byte[4];
 				inDataStream.read(lengthBytes);
 				int length = ByteBuffer.wrap(lengthBytes).getInt();
 
 				for (int i = 0; i < length; i++) {
					byte[] buffer = new byte[8];
					inDataStream.read(buffer);
					ByteBuffer bb = ByteBuffer.wrap(buffer);
					int x = bb.getInt();
					int y = bb.getInt();
					Toast.makeText(con, x + " " + y,
 							Toast.LENGTH_SHORT).show();
					mBTOutputStream.write(buffer);
					mBTOutputStream.flush();
 				}
 			}
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to send Data!");
 		}
 	}
 }
