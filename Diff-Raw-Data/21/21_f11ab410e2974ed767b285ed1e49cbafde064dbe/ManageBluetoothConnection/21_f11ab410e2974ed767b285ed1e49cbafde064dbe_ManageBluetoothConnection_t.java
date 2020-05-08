 package com.schema.bro.share;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.util.Log;
 
 import com.schema.bro.ks.Schedule;
 
 public class ManageBluetoothConnection extends Thread {
     
 	private final BluetoothSocket mmSocket;
     private final InputStream mmInStream;
     private final OutputStream mmOutStream;
     private Context context;
     
     public ManageBluetoothConnection(Context context, BluetoothSocket socket) {
         mmSocket = socket;
         InputStream tmpIn = null;
         OutputStream tmpOut = null;
  
         this.context = context;
         
         // Get the input and output streams, using temp objects because
         // member streams are final
         try {
             tmpIn = socket.getInputStream();
             tmpOut = socket.getOutputStream();
         } catch (IOException e) { }
  
         mmInStream = tmpIn;
         mmOutStream = tmpOut;
     }
  
     public void run() {
     	byte[] buffer = new byte[1024]; 
         int bytes, totalBytes = 0; // bytes returned from read()
         
         // Keep listening to the InputStream until an exception occurs
         while (true) {
             try {
             	// Read from the InputStream
                 bytes = mmInStream.read(buffer);
                Log.e("Bluetooth", bytes + " bytes to receive");
                 byte[] byteArray = new byte[bytes];
                 System.arraycopy(buffer, 0, byteArray, 0, bytes);
                 String trans = new String(byteArray);
                Log.e("Bluetooth", trans + " transactions to receive");
                 int transactions = Integer.parseInt(trans);
                 Log.e("Bluetooth", transactions + " transactions to receive");
                 
                 String data = "";
                 while(transactions > 0){
                 	bytes = mmInStream.read(buffer);
                 	totalBytes += bytes;
                 	byte[] readableBytes = new byte[bytes];
                     System.arraycopy(buffer, 0, readableBytes, 0, bytes);
                     String tempData = new String(readableBytes);
                     data += tempData;
                     transactions--;
                 }
                 Log.e("Bluetooth", totalBytes + " bytes received");
                 
                 Schedule database = new Schedule(context);
                 database.addSchedule(data);
                 
                 break;
             } catch (IOException e) {
                 break;
             }
         }
         
         this.cancel();
     }
  
     /* Call this from the main activity to send data to the remote device */
     public void write(byte[] data) {
         try {
             int bytes = data.length;
             double dBytes = bytes;
         	int transactions = (int) Math.ceil(dBytes/1024);
         	Log.e("Bluetooth", transactions + " transactions to send.");
         	String trans = String.valueOf(transactions);
         	mmOutStream.write(trans.getBytes());
             mmOutStream.flush();
         	
            Thread.sleep(55);
             
             int sent = 0;
             while(transactions > sent){
             	if(bytes - 1024*sent >= 1024)
             		mmOutStream.write(data, 1024*sent, 1024);
             	else
             		mmOutStream.write(data, 1024*sent, bytes - 1024*sent);
             	mmOutStream.flush();
             	sent++;
             	Thread.sleep(10);
             }
             Log.e("Bluetooth", bytes + " bytes sent.");
             Log.e("Bluetooth", sent + " transactions sent.");
             
         } catch (IOException e) { } catch (InterruptedException e) {
 			e.printStackTrace();
 		}
     }
  
     /* Call this from the main activity to shutdown the connection */
     public void cancel() {
         try {
             mmSocket.close();
         } catch (IOException e) { }
     }
 }
