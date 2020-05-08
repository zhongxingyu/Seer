 package com.example.nxtdriver;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.TextView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.util.Log;
 
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 public class MainActivity extends Activity implements OnClickListener{
     
     TextView status;
     BluetoothAdapter mBluetoothAdapter;
     BluetoothSocket mmSocket;
     BluetoothDevice mmDevice;
     DataOutputStream mmOutputStream;
     InputStream mmInputStream;
     ScheduledExecutorService sendNXT;
     ScheduledExecutorService readNXT;
     ScheduledExecutorService sendServer;
     ScheduledExecutorService background;
     TextView sensor;
     byte motora = 0;
     byte motorb = 0;
     byte motorc = 0;
     byte motora_o = 0;
     byte motorb_o = 0;
     byte motorc_o = 0;
     byte[] received = new byte[3];
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         sendNXT = Executors.newSingleThreadScheduledExecutor();
         readNXT = Executors.newSingleThreadScheduledExecutor();
         sendServer = Executors.newSingleThreadScheduledExecutor();
         background = Executors.newSingleThreadScheduledExecutor();
         Button forwardButton = (Button)findViewById(R.id.forward);
         Button reverseButton = (Button)findViewById(R.id.reverse);
         Button leftButton = (Button)findViewById(R.id.left);
         Button rightButton = (Button)findViewById(R.id.right);
         Button connectButton = (Button)findViewById(R.id.connect);
         Button stopButton = (Button)findViewById(R.id.stop);
         Button readButton = (Button)findViewById(R.id.read);
         
         status = (TextView)findViewById(R.id.status);
         sensor = (TextView)findViewById(R.id.sensor);
         
         //ConnectButton
         connectButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 try
                 {
                     findBT();
                     connectBT();
                 } catch(Exception e) {
                 }
                 
             }
         });
         
         readButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 readNXT.scheduleAtFixedRate(new Runnable(){public void run(){new readSensor().execute();}}, 0, 10, TimeUnit.MILLISECONDS);
             }
         });
         
         forwardButton.setOnClickListener(this);
         reverseButton.setOnClickListener(this);
         leftButton.setOnClickListener(this);
         rightButton.setOnClickListener(this);
         stopButton.setOnClickListener(this);
     }
     
     @Override
     public void onClick(final View v){
        new readSensor();
     }
     
     public void sendCmd(View v){
         try{
             switch(v.getId())
             {
                 case R.id.forward:
                 {
                     motora = 127;
                     motorb = 127;
                     break;
                 }
                     
                 case R.id.reverse:
                 {
                     motora = -127;
                     motorb = -127;
                     break;
                 }
                 case R.id.left:
                 {
                     motora = 40;
                     motorb = -40;
                     break;
                 }
                 case R.id.right:
                 {
                     motora = -40;
                     motorb = 40;
                     break;
                 }
                 case R.id.stop:
                 {
                     motora = 0;
                     motorb = 0;
                     break;
                 } 
             } send();
             }catch(Exception e){}
     }
     
     void findBT() throws Exception
     {
         
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if(mBluetoothAdapter == null) {
             status.setText("NO NXT In Range");
         }
         if(!mBluetoothAdapter.isEnabled())
         {
             Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBluetooth, 0);
         }
         
         Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
         
         if(pairedDevices.size() > 0);
         {
             for(BluetoothDevice device : pairedDevices)
             {
                 if(device.getName().equals("NXT"))
                 {
                     mmDevice = device;
                     status.setText("NXT Found");
                     break;
                 }
             }
         }
     }
     
     void connectBT() throws Exception
     {
         UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
         mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
         mmSocket.connect();
         mmOutputStream = new DataOutputStream(mmSocket.getOutputStream());
         mmInputStream = mmSocket.getInputStream();
         status.setText("Connection Established");
     }
     
     void send() throws Exception
     {
         mmOutputStream.writeByte(motora);
         mmOutputStream.writeByte(motorb);
         mmOutputStream.writeByte(motorc);        
     }
     private class readSensor extends AsyncTask<Void, Void, String> {
     	@Override
     	protected String doInBackground(Void... params) {
         	try {
                 mmInputStream.read(received);
             } catch (Exception e) {
                 String error = e.getMessage();
                 Log.v("nxtdriver", error);
                 sensor.setText("Failed to read");
             }
             return String.valueOf(received[2]);
         }
 
     	@Override
         protected void onPostExecute(String distance) {
         	sensor.setText("Received: " + distance);
         }
 
     }
 }
