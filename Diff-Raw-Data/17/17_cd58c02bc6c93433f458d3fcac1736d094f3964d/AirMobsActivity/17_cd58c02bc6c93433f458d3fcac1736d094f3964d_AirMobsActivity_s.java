 package edu.mit.media.airmobs;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class AirMobsActivity extends Activity implements ServiceBinder.BoundCallback{
 	
 	private static final String LOG_TAG = "AirMobsActivity";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         // ui handlers
         AutoCompleteTextView tx = (AutoCompleteTextView)findViewById(R.id.peerIP);
         tx.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,int count) {
 				AddressManager.instance().setProviderAddress(s.toString());
 			}
         });
         // checkbox handlers
         CheckBox sms  = (CheckBox) findViewById(R.id.sms_ok);
         CheckBox call = (CheckBox) findViewById(R.id.call_ok);
         CheckBox inet = (CheckBox) findViewById(R.id.inet_ok);
         sms.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				// TODO Auto-generated method stub
				if (isChecked) {
					smsEnabled = !smsEnabled;
 					Log.i(LOG_TAG, "Sms status: " + smsEnabled);
				}
 				
 			}        	
         });
         call.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				// TODO Auto-generated method stub
				if (isChecked) {
					callEnabled = !callEnabled;
 					Log.i(LOG_TAG, "Call status: " + callEnabled);
				}
 				
 			}        	
         });
         inet.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				// TODO Auto-generated method stub
				if (isChecked) {
					inetEnabled = !inetEnabled;
 					Log.i(LOG_TAG, "Internet status: " + inetEnabled);
				}
 				
 			}        	
         });
        // starting  service and binding
        started = false;
        mBinder = new ServiceBinder();
        started = mBinder.isServiceStarted(MonitorService.class.getName(),this);
        if (!started){
 			startService(new Intent(MonitorService.class.getName()));
 			started = true;
         }
         mBinder.bindToService(MonitorService.class.getName(), this, this);
         mListener = new SampleMonitorListener(this);
     }
     
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu, menu);
 	    return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    if (item.getItemId() == R.id.exit_srv) {
 	    	try {
 				api.exit();
 			} catch (RemoteException e) {
 				Log.e(LOG_TAG,"api exit call failed");
 			}
 	    	mBinder.unbindToService(MonitorService.class.getName(), this);
 	    	stopService(new Intent(MonitorService.class.getName()));
 	    	started = false;
 	    }	 
 	    else if (item.getItemId() == R.id.tst1_srv){
 	    	try {
 	    		api.test(1);
 	    	} catch (RemoteException e) {
 	    		Log.e(LOG_TAG,"api test(1) call failed");
 	    	}
 	    }
 	    else if (item.getItemId() == R.id.tst2_srv){
 	    	try {
 	    		api.test(2);
 	    	} catch (RemoteException e) {
 	    		Log.e(LOG_TAG,"api test(2) call failed");
 	    	}
 	    } 
 	    else if (item.getItemId() == R.id.tst3_srv){
 	    	try {
 	    		api.test(3);
 	    	} catch (RemoteException e) {
 	    		Log.e(LOG_TAG,"api test(2) call failed");
 	    	}
 	    } 
 	    // start settings activities
 	    return true;
 	}
     
     
 	@Override
 	public void serviceBound(IBinder service) {
 		api = MonitorApi.Stub.asInterface(service);
 		// TODO implement whatever is needed to get the API object
 		try {
 			api.addMonitorListener(mListener);
 			Log.i(LOG_TAG,"AirMobsActivity has bound to service"+(new Integer(api.getStatus()).toString()));
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			Log.e(LOG_TAG,"api call failed");
 			e.printStackTrace();
 		}
 	}    
 	
 	private MonitorApi api;
     private ServiceBinder mBinder;
     private boolean started;
     private SampleMonitorListener mListener;
     private boolean smsEnabled = false;
     private boolean callEnabled = false;
     private boolean inetEnabled = false;
     
 }
