 package com.tware.anexter;
 
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.content.BroadcastReceiver;
 //import android.widget.Toast;
 
 public class bluetooth extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private Button bBlue;
 	private String LOG;
 
 	private TextView btCntV;
 	
 	private BluetoothAdapter btAdapter;
 	private int foundBTNum = 0;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.bluetooth);
         this.setTitle("aNexter - 蓝牙测试");
         
 		btAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (btAdapter == null)
 		{
 			Toast.makeText(getApplicationContext(), "No bluetooth adapter", Toast.LENGTH_LONG).show();
 		}
 		else
 		{
     		Toast.makeText(getApplicationContext(),
 					"开始重新搜索蓝牙设备……",
 					Toast.LENGTH_SHORT).show();
     		
 /*    		IntentFilter intentFilter = new IntentFilter();
     		intentFilter.addAction("android.bluetooth.device.action.FOUND");
     		registerReceiver(bluetoothReciever, intentFilter);
     		btAdapter.startDiscovery();
 */
 		}
 		
 		btCntV = (TextView)findViewById(R.id.btCntView);
 		
         bPass = (Button)findViewById(R.id.btn_pass);     
         bPass.setEnabled(false);
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
 				if (timer != null)
 				{
 					timer.cancel();
 					timer = null;
 					Log.d("BT", "Timer canceled!");
 				}
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "PASS|");
         		i.setClass(bluetooth.this , lightSensor.class);
         		startActivity(i);
         		bluetooth.this.finish();
         	}
         });
         
         bBlue = (Button)findViewById(R.id.btn_bluetooth);
         bBlue.setText("Retest");
         bBlue.setOnClickListener(new OnClickListener(){
         	@Override
         	public void onClick(View v)
         	{
        		if (btAdapter!= null){
        			btAdapter.startDiscovery();
        			foundBTNum = 0;
        			Toast.makeText(getApplicationContext(),
        					"开始重新搜索蓝牙设备……",
        					Toast.LENGTH_SHORT).show();
        		}else{
            		Toast.makeText(getApplicationContext(),
    						"本机没有蓝牙模组存在……",
    						Toast.LENGTH_SHORT).show();        			
        		}
         	}
         });       
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setEnabled(false);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
 				if (timer != null)
 				{
 					timer.cancel();
 					timer = null;
 					Log.d("BT", "Timer canceled!");
 				}
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(bluetooth.this , getResults.class);
         		startActivity(i);
         		bluetooth.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
 				if (timer != null)
 				{
 					timer.cancel();
 					timer = null;
 					Log.d("BT", "Timer canceled!");
 				}
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "N/A|");
         		i.setClass(bluetooth.this , lightSensor.class);
         		startActivity(i);
         		bluetooth.this.finish();
         	}
         });
         
         LOG = this.getIntent().getStringExtra("LOG");
         timer.schedule(task, 1000, 1000);
     }        
     
     Handler uhandle = new Handler();
     Timer timer = new Timer();
     TimerTask task = new TimerTask()
     {
     	@Override
     	public void run()
     	{
     		uhandle.post(new Runnable()
     		{
     			@Override
     			public void run()
     			{
    				if (btAdapter == null) return ;
    				
     				if (!btAdapter.isEnabled())
     				{
     					btAdapter.enable();
     				}else{
     					btAdapter.startDiscovery();
     				}
     			}
     		});
     	}
     };
     
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(bluetoothReciever);
 	}
     
 	@Override
 	protected void onResume() { 
 		super.onResume();
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction("android.bluetooth.device.action.FOUND");
 		registerReceiver(bluetoothReciever, intentFilter);
		if (btAdapter!= null){
			btAdapter.startDiscovery();
		}
 	}
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		bBlue.setEnabled(true);
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private BroadcastReceiver bluetoothReciever = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context arg0, Intent intent) {
 			String action = intent.getAction();
 			if (action.equals("android.bluetooth.device.action.FOUND")) {
 				Log.e("BT", "Found Bluetooth devices!");
 				foundBTNum ++;
 				btCntV.setText("已搜到第 " + foundBTNum + " 蓝牙设备!");
 				if (foundBTNum >= 3) 
 				{
 					if (timer != null)
 					{
 						timer.cancel();
 						timer = null;
 						Log.d("BT", "Timer canceled!");
 					}
 	        		Intent i = new Intent();
 	        		i.putExtra("LOG", LOG + "PASS|");
 	        		i.setClass(bluetooth.this , lightSensor.class);
 	        		startActivity(i);
 	        		bluetooth.this.finish();
 				}
 			}
 		}
 	};
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, 0, 0, "Bluetooth Settings");        
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch(item.getItemId())
         {
     		case 0:
     			try{
     				Intent i = new Intent();
     				i.setClassName("com.android.IvtBluetooth", "com.android.IvtBluetooth.IVTBluetooth");
     				startActivity(i);
     				bPass.setEnabled(true);
     				bFail.setEnabled(true);
     			}catch(ActivityNotFoundException e)
     			{
   					Log.d("aNext", "No IVT Devices found, and now starting default Bluetooth Settings");
   					startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
   					bPass.setEnabled(true);
   					bFail.setEnabled(true);
     			}
     		break;
     	}
         return true;
     }
 	
 }
