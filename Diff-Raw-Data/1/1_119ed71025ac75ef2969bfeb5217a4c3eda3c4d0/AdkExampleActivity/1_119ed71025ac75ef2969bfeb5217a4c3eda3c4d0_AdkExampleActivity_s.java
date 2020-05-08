 package com.tigris.adk.androface;
 
 import com.tigris.adk.androface.R;

 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.CompoundButton;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 
 public class AdkExampleActivity extends Activity {
 	
 	private static final String ACTION_USB_PERMISSION = 
 			"com.tigris.adk.androface.USB_PERMISSION";
 	
 	// USB Ǿ  ̺Ʈ .
 	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (ACTION_USB_PERMISSION.equals(action)) {
 				    // ڿ Android Accessory Protocol   Ǹ
 				    //    ̾α׿     ޴´.
 					synchronized (this) {
 					UsbAccessory accessory = UsbManager.getAccessory(intent);
 					if (intent.getBooleanExtra(
 							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
 						//  
 						showMessage("receiver : USB Host .");
 					} else {
 						Log.d(AdkExampleActivity.class.getName(), 
 								"permission denied for accessory "
 								+ accessory);
 					}
 					
 					openAccessory(accessory);
 					//    ޾ ǥ
 					mPermissionRequestPending = false;
 				}
 			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
 				// Android Accessory Protocol    Ǿ 
 				UsbAccessory accessory = UsbManager.getAccessory(intent);
 				//  ϰ ִ    Ȯ
 				if (accessory != null && accessory.equals(mAccessory)) {
 					showMessage("USB Host  .");
 					closeAccessory();
 				}
 			}
 		}
 	};
 	
 	private TextView txtMsg;
 	private UsbManager mUsbManager;
 	private UsbAccessory mAccessory;
 	private PendingIntent mPermissionIntent;
 	private boolean mPermissionRequestPending;
 	private ToggleButton btnLed;
     private AdkHandler handler;
 
 	
 	/** ƼƼ   ȣ */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         txtMsg = (TextView)this.findViewById(R.id.txtMsg);
         btnLed = (ToggleButton)this.findViewById(R.id.btnLed); // ư
 
         //Android Accessory Protocol   ῡ  εĳƮ ù 
       	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
       	filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
       	registerReceiver(mUsbReceiver, filter);
       	
       	mUsbManager = UsbManager.getInstance(this);
 		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 				ACTION_USB_PERMISSION), 0);
 		
         btnLed.setOnCheckedChangeListener(new OnCheckedChangeListener(){
         	@Override
             public void onCheckedChanged(CompoundButton buttonView,
                                      boolean isChecked) {
                  if(handler != null && handler.isConnected()){
                       handler.write((byte)0x1, (byte)0x0, isChecked ? 1 : 0);
                       showMessage("Printer" + (isChecked ? "Start" : "END"));
                  }
             }});
     }
     
     /** ƼƼ ȭ鿡   ȣ */
     @Override
 	public void onResume() {
 		super.onResume();
 		//  ȭ鿡   ȵ̵  Android Accessory Protocol 
 		//  USB Host Ǿ ִ Ȯ
 		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
 		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
 		if (accessory != null) { // Android Accessory Protocol   ã 
 			if (mUsbManager.hasPermission(accessory)) {
 				showMessage("onresume : USB Host .");
 				openAccessory(accessory);
 			} else {
 				synchronized (mUsbReceiver) {
 					if (!mPermissionRequestPending) {
 						mUsbManager.requestPermission(accessory,
 								mPermissionIntent); // USB    ص Ǵ ڿ 
 						mPermissionRequestPending = true; //   ڵ带  ǥ
 					}
 				}
 			}
 		} else {
 			Log.d(AdkExampleActivity.class.getName(), "mAccessory is null");
 		}
 	}
     
     @Override
     public void onPause() {
          super.onPause();
          closeAccessory();
     }
     // ƼƼ Ҹ  ȣ
  	@Override
  	protected void onDestroy() {
  		// εĳƮ ù 
  		unregisterReceiver(mUsbReceiver);
  		super.onDestroy();
  	}
     
     private void openAccessory(UsbAccessory accessory){
 		mAccessory = accessory;
         if(handler == null)
             handler = new AdkHandler();
 
        handler.open(mUsbManager, mAccessory);
 	}
 	
 	private void closeAccessory(){
         if(handler != null && handler.isConnected())
             handler.close();
         mAccessory = null;
     }
 	
 	private void showMessage(String msg){
 		txtMsg.setText(msg);
 	}
 }
