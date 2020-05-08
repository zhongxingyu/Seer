 package com.innovalley.bt;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Set;
 import java.util.UUID;
 
 import android.app.AlertDialog;
 import android.app.Application;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.DialogInterface;
 
 // Sin Comentarios
 //Ahora sigue sin comentarios
 public class Bluetooth extends Thread {
 	private String address;
 	private BluetoothAdapter mBluetoothAdapter = null;
     private BluetoothSocket btSocket = null;
     private OutputStream outStream = null;
     private boolean connected;
     private Application application;
     
     private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
     
   
 	public boolean isConnected() {
 		return connected;
 	}
 	
 	
 	public void setConnected(boolean connected) {
 		this.connected = connected;
 	}
 
 	public Bluetooth(Application application) throws Exception{
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		this.connected = false;
 		this.application = application;
 	}
 
 	public boolean isEnabled(){
 		return this.mBluetoothAdapter.isEnabled();
 	}
 	
 	public void init(String address){
 		this.address = address;
 		this.run();
 	}
 	
 	public void run(){
 		BluetoothDevice device =mBluetoothAdapter.getRemoteDevice(address);
         try{
 		if(device == null){
         	throw new Exception("It couldn't be connected with the device"+this.address);
         }
         btSocket =device.createRfcommSocketToServiceRecord(MY_UUID);
         
         mBluetoothAdapter.cancelDiscovery();
         }catch(Exception e){
         	
         }
 	}
 	
 	public void connect() throws Exception{
 		try{
         	btSocket.connect();
         	this.connected=true;
         }
         catch(Exception e){
         	try{
         		btSocket.close();
         	}
         	catch(Exception e2){
         		throw new Exception("ON RESUME: Unable to close socket during connection failure"+e2.getMessage());
         	}
         }
         
         try {
             outStream = btSocket.getOutputStream();
         } catch (IOException e) {
         	throw new Exception("BLUETOOTH CONSTRUCTOR:Output stream creation failed."+e.getMessage());
         }
 	}
 	
 	
 	public void sendMessage(String message) throws Exception{
 		try{
 			byte[] msgBuffer = message.getBytes();
 			outStream.write(msgBuffer);
 			outStream.flush();
 		}catch(Exception e){
 			throw new Exception("BLUETOOTH SEND MESSAGE:Exception during write."+e.getMessage());
 		}
 	}
 	
 	public void disconnect() throws Exception{
 		try{
 			this.btSocket.close();
 			this.outStream.close();
 			this.connected = false;
     	}
     	catch(Exception e2){
     		throw new Exception("ON RESUME: Unable to close socket during connection failure"+e2.getMessage());
     	}
 	}
 
 
 	public void pair() {
 		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
 		//ArrayAdapter<String> mArrayAdapter = new ArrayAdapter(GeoVibe.getInstance().getAppContext(), R.id.message);
 		AlertDialog.Builder ab=new AlertDialog.Builder(this.application.getApplicationContext());
 		ab.setTitle("Pairment");
 		String[] data = new String[pairedDevices.size()];
 		
 		int i = 0;
 		// If there are paired devices
 		if (pairedDevices.size() > 0) {
 		    // Loop through paired devices
 		    for (BluetoothDevice device : pairedDevices) {
 		        // Add the name and address to an array adapter to show in a ListView
 		        //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
 		    	data[i] = device.getName() + "\n" + device.getAddress();
 		    	i++;
 		    }
 		}
 		
 		ab.setSingleChoiceItems(data, 0,new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				
 			}
 		}).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			 public void onClick(DialogInterface dialog, int whichButton) {
 				 //showMessage("select:"+GeoVibe.getInstance().getDestAddressGP(option).toString());
 				 
 			 }
 		 }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			 public void onClick(DialogInterface dialog, int whichButton) {
 				 // on cancel button action
 			 }
 		 });
 		ab.show();
 		
 		
 		
 	}
 }
