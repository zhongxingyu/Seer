 package com.schema.bro.share;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
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
        byte[] buffer = new byte[1024];  // buffer store for the stream
         int bytes; // bytes returned from read()
 
         // Keep listening to the InputStream until an exception occurs
         while (true) {
             try {
             	// Read from the InputStream
                 bytes = mmInStream.read(buffer);
                 byte[] readableBytes = new byte[bytes];
                 System.arraycopy(buffer, 0, readableBytes, 0, bytes);
                 String data = new String(readableBytes);
                
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
     public void write(byte[] bytes) {
         try {
            mmOutStream.write(bytes);
             mmOutStream.flush();
         } catch (IOException e) { }
     }
  
     /* Call this from the main activity to shutdown the connection */
     public void cancel() {
         try {
             mmSocket.close();
         } catch (IOException e) { }
     }
 }
