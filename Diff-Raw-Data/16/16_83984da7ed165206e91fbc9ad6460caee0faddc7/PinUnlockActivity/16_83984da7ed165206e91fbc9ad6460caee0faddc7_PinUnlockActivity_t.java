 package com.androidhuman.putmedown.activity;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.androidhuman.putmedown.R;
 import com.androidhuman.putmedown.service.IProtectionService;
 import com.androidhuman.putmedown.service.ProtectionService;
 import com.androidhuman.putmedown.util.Util.Security;
 import com.androidhuman.putmedown.util.Util.Security.PinType;
 
 public class PinUnlockActivity extends Activity {
 	
 	EditText edtPinInput;
 	Button btnDone;
 	
 	private IProtectionService mService;
 	private ServiceConnection conn = new ServiceConnection(){
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			mService = IProtectionService.Stub.asInterface(service);
 			
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 
 		}
 		
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_pin_unlock);
 	    
 	    edtPinInput = (EditText)findViewById(R.id.edt_activity_pin_unlock_input);
 	    btnDone = (Button)findViewById(R.id.btn_activity_pin_unlock_done);
	    
 	    btnDone.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				String pin = Security.getUnlockPIN(getApplicationContext(), PinType.PIN);
 				if(pin.equals(edtPinInput.getText().toString())){
 					try{
             			mService.dismissAlarm();
             			mService.disableService();
             			finish();
             		}catch(RemoteException e){
             			e.printStackTrace();
             		}
 				}else{
 					Toast.makeText(getApplicationContext(), "Invalid PIN.", Toast.LENGTH_SHORT).show();
 				}
 			}
 	    	
 	    });
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		bindService(new Intent(this, ProtectionService.class), conn, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		if(mService!=null){
 			unbindService(conn);
 		}
 	}
 
 }
