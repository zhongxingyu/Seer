 package tn.insat.androcope.thread;
 
 import java.io.IOException;
 import java.util.UUID;
 import tn.insat.androcope.BluetoothCommandService;
 import tn.insat.androcope.MessageHandler;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class ConnectThread extends Thread {
 	private static final String TAG = "ConnectedThread";
 	private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
 	
 	BluetoothCommandService commandService;
 	Handler handler;
 	private final BluetoothAdapter adapter;
     private final BluetoothSocket socket;
     private final BluetoothDevice device;
 
     public ConnectThread(BluetoothDevice device, BluetoothCommandService commandService, Handler handler) {
         this.device = device;
         this.commandService = commandService;
         this.handler = handler;
         this.adapter = BluetoothAdapter.getDefaultAdapter();
         BluetoothSocket tmp = null;
 
         // Get a BluetoothSocket for a connection with the
         // given BluetoothDevice
         try {
             tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
         } catch (IOException e) {
             Log.e(TAG, "create() failed", e);
         }
         socket = tmp;
     }
 
     public void run() {
         Log.i(TAG, "BEGIN mConnectThread");
         setName("ConnectThread");
 
         // Always cancel discovery because it will slow down a connection
         adapter.cancelDiscovery();
 
         // Make a connection to the BluetoothSocket
         try {
             // This is a blocking call and will only return on a
             // successful connection or an exception
             socket.connect();
         } catch (IOException e) {
             connectionFailed();
             // Close the socket
             try {
                 socket.close();
             } catch (IOException e2) {
                 Log.e(TAG, "unable to close() socket during connection failure", e2);
             }
             // Start the service over to restart listening mode
            commandService.getConnectThread().start();             // TO DO !! when connecting to unavailable device
             return;
         }
 
         // Reset the ConnectThread because we're done
         synchronized (this) {
             commandService.setConnectThread(null);
         }
 
         // Start the connected thread
         connected(socket, device);
     }
     
     private void connectionFailed() {
         commandService.setState(commandService.STATE_LISTEN);
 
         // Send a failure message back to the Activity
         Message msg = handler.obtainMessage(MessageHandler.MESSAGE_TOAST);
         Bundle bundle = new Bundle();
         bundle.putString(MessageHandler.TOAST, "Unable to connect device");
         msg.setData(bundle);
         handler.sendMessage(msg);
     }
 
     public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
         Log.d(TAG, "connected");
 
         // Cancel the thread that completed the connection
         if (commandService.getConnectThread() != null) {commandService.getConnectThread().cancel(); commandService.setConnectThread(null);}
 
         // Cancel any thread currently running a connection
         if (commandService.getTransmissionThread() != null) {commandService.getTransmissionThread().cancel(); commandService.setTransmissionThread(null);}
 
         // Start the thread to manage the connection and perform transmissions
         commandService.setTransmissionThread(new TransmissionThread(socket, commandService));
         commandService.getTransmissionThread().start();
 
         // Send the name of the connected device back to the UI Activity
         Message msg = handler.obtainMessage(MessageHandler.MESSAGE_DEVICE_NAME);
         Bundle bundle = new Bundle();
         bundle.putString(MessageHandler.DEVICE_NAME, device.getName());
         msg.setData(bundle);
         handler.sendMessage(msg);
         
         commandService.setState(commandService.STATE_CONNECTED);
     }
     
     public void cancel() {
         try {
             socket.close();
         } catch (IOException e) {
             Log.e(TAG, "close() of connect socket failed", e);
         }
     }
 }
