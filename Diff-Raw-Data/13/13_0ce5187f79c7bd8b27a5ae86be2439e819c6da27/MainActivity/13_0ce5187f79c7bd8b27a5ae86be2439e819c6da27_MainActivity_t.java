 package br.eng.moretto.a2turbo;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	
 	public static final String LOG_TAG = "A2Turbo";
 	
 	  // Intent request codes
     private static final int REQUEST_CONNECT_DEVICE = 1;
     private static final int REQUEST_ENABLE_BT = 2;
     
 	 // Message types sent from the BluetoothReadService Handler
     public static final int MESSAGE_STATE_CHANGE = 1;
     public static final int MESSAGE_READ = 2;
     public static final int MESSAGE_WRITE = 3;
     public static final int MESSAGE_DEVICE_NAME = 4;
     public static final int MESSAGE_TOAST = 5;	
 
     // Key names received from the BluetoothChatService Handler
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
 
 	
     private static TextView mTitle;
     private MenuItem mMenuItemConnect;
     private String mConnectedDeviceName = null;
     
 	
 	 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         setContentView(R.layout.main);    
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
           
         // Set up the custom title
         mTitle = (TextView) findViewById(R.id.title_left_text);
         mTitle.setText(R.string.app_name);
         mTitle = (TextView) findViewById(R.id.title_right_text);       
        
         
         if(MainApplication.get().getBluetoothAdapter() == null){
 	    
 		    //Bluetooth init
         	MainApplication.get().setBluetoothAdapter(BluetoothAdapter.getDefaultAdapter());
 	
 			if (MainApplication.get().getBluetoothAdapter() == null) {
 	            finishDialogNoBluetooth();
 				return;
 			}
 			
 			MainApplication.get().setSerialService(new BluetoothSerialService(this, mHandlerBT));	
 	    }
         
     }
     
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	//Closing bluetooth connection
        if( keyCode == KeyEvent.KEYCODE_BACK ){     
        	MainApplication.get().getSerialService().stop();
        	this.finish();
            return true;
        }
        return false;
    }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       setContentView(R.layout.main);
     }
         
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.option_menu, menu);
         mMenuItemConnect = menu.getItem(0);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.connect:
         	
         	if (getConnectionState() == BluetoothSerialService.STATE_NONE) {
         		// Launch the DeviceListActivity to see devices and do scan
         		Intent serverIntent = new Intent(this, DeviceListActivity.class);
         		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
         	}
         	else
             	if (getConnectionState() == BluetoothSerialService.STATE_CONNECTED) {
             		MainApplication.get().getSerialService().stop();
             		MainApplication.get().getSerialService().start();
             	}
             return true;
             
         case R.id.fuelmap:
         	
         	Intent i = new Intent(this, FuelMapper.class);
         	startActivity(i);
         	
             return true;
             /*
         case R.id.menu_special_keys:
             //doDocumentKeys();
             return true;
             */
         }
         return false;
     }
     
     public int getConnectionState() {
 		return MainApplication.get().getSerialService().getState();
 	}
     
     /**
      * Callback from Activity (onOptionsItemSelected @ MainActivity)
      */
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         
         switch (requestCode) {
         
         case REQUEST_CONNECT_DEVICE:
 
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
                 // Get the device MAC address
                 String address = data.getExtras()
                                      .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                
                 // Get the BluetoothDevice object
                 BluetoothDevice device = MainApplication.get().getBluetoothAdapter().getRemoteDevice(address);
                 // Attempt to connect to the device
                 MainApplication.get().getSerialService().connect(device);                
             }
             break;
 
         case REQUEST_ENABLE_BT:
             // When the request to enable Bluetooth returns
             if (resultCode == Activity.RESULT_OK) {
                 Log.d(LOG_TAG, "BT not enabled");                
                 finishDialogNoBluetooth();                
             }
         }
     }
     
     public void finishDialogNoBluetooth() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.alert_dialog_no_bt)
         .setIcon(android.R.drawable.ic_dialog_info)
         .setTitle(R.string.app_name)
         .setCancelable( false )
         .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //finish();            	
                 	   }
                });
         AlertDialog alert = builder.create();
         alert.show(); 
     }
     
     
     /**
      * BT Handler
      * Here, we have to deal with message from/to BT
      */
     private final Handler mHandlerBT = new Handler() {
     	
         @Override
         public void handleMessage(Message msg) {        	
             switch (msg.what) {
             case MESSAGE_STATE_CHANGE:
                 
                 switch (msg.arg1) {
                 case BluetoothSerialService.STATE_CONNECTED:
                 	
                 	if (mMenuItemConnect != null) {
                 		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
                 		mMenuItemConnect.setTitle(R.string.disconnect);
                 	} 
                     mTitle.setText("Conectado com ");
                     mTitle.append(mConnectedDeviceName);
                     break;
                     
                 case BluetoothSerialService.STATE_CONNECTING:
                     mTitle.setText("Conectando...");
                     break;
                     
                 case BluetoothSerialService.STATE_LISTEN:
                 case BluetoothSerialService.STATE_NONE:
                 	
                 	if (mMenuItemConnect != null) {
                 		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_search);
                 		mMenuItemConnect.setTitle(R.string.connect);
                 	}
             		mTitle.setText("Não conectado");
 
                     break;
                 }
                 break;
             case MESSAGE_WRITE:
             	
             	//byte[] writeBuf = (byte[]) msg.obj;
             	//mEmulatorView.write(writeBuf, msg.arg1);
             	
                 break;
                 
             case MESSAGE_READ:
                 String readBuf = (String) msg.obj;              
                 
                 TurboGauge t = (TurboGauge) findViewById(R.id.turbogauge);                
                 
                 try{
                 	t.setHandTarget(Float.parseFloat(readBuf));                
                 }catch (Exception e) {
                 	Log.e("tag", "nao deu");
 				}
                 //Toast.makeText(getApplicationContext(), new String(readBuf), Toast.LENGTH_SHORT).show();
                 TextView text = (TextView) findViewById(R.id.text_view);
                 text.setText(new String(readBuf));
                 
                 break;
                 
             case MESSAGE_DEVICE_NAME:
                 // save the connected device's name
                 mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                 Toast.makeText(getApplicationContext(), "Conectado com "
                 		+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                 break;
                 
             case MESSAGE_TOAST:
                 Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                 break;
             }
         }
     }; 
 }
