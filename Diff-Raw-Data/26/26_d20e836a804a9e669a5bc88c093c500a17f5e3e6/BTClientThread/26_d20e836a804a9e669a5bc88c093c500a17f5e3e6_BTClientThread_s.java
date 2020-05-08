 package fib.pec.hovione;
 
 import java.io.IOException;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ParcelUuid;
 
 public class BTClientThread extends Thread {
 	private BluetoothSocket socket;
     private BluetoothDevice remoteDevice;
     private BluetoothAdapter btAdapter;
     private Handler localH;
     
 	public BTClientThread(BluetoothDevice dev, Handler h) {
 		remoteDevice = dev;
 		BluetoothSocket tmp = null;
 		localH = h;
 		
 		ParcelUuid[] proba = remoteDevice.getUuids();
 		try {
			socket = remoteDevice.createRfcommSocketToServiceRecord(proba[0].getUuid());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		socket = tmp;
 	}
 	public void run() {
 		btAdapter = BluetoothAdapter.getDefaultAdapter();
 		btAdapter.cancelDiscovery();
 		
 		try {
 			socket.connect();
 		} catch (IOException e) {
 			try {
 				socket.close();
 			} catch (IOException ee) {
 				ee.printStackTrace();
 			}
 			e.printStackTrace();
 		}
 		
 		Message msg = localH.obtainMessage(0,"APSEOFIAWEF");
 		localH.sendMessage(msg);
 	}
 	public void cancel() {
 		try {
             socket.close();
         } catch (IOException e) { }
 	}
 }
