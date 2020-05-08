 package co.joyatwork.accelerometer.sandbox;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 
 public class Main extends Activity {
 
 	private static final String TAG = "Main";
 
 	private final class AccelerometerUpdateReciever extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.d(TAG, "AccelerometerUpdateReciever.onReceive " + intent.getAction());
 			/*
 			if (intent.hasExtra(getResources().getString(R.string.acc_x_value))) {
 				TextView xValueTextView = (TextView) findViewById(R.id.accXValTextView);
 				xValueTextView.setText(
 						intent.getExtras().getCharSequence(getResources().getString(R.string.acc_x_value)));
 			}
 			if (intent.hasExtra(getResources().getString(R.string.acc_y_value))) {
 				TextView xValueTextView = (TextView) findViewById(id.accYValTextView);
 				xValueTextView.setText(
 						intent.getExtras().getCharSequence(getResources().getString(R.string.acc_y_value)));
 			}
 			if (intent.hasExtra(getResources().getString(R.string.acc_z_value))) {
 				TextView xValueTextView = (TextView) findViewById(id.accZValTextView);
 				xValueTextView.setText(
 						intent.getExtras().getCharSequence(getResources().getString(R.string.acc_z_value)));
 			}
 			*/
 		}
 		
 	}
 	private BroadcastReceiver accelerometerUpdateReceiver;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.main);
 		
 		Button quitButton = (Button) findViewById(R.id.quitButton);
 		quitButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		CheckBox loggingCheckBox = (CheckBox) findViewById(R.id.loggingCheckBox);
 		loggingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		accelerometerUpdateReceiver = new AccelerometerUpdateReciever();
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
 		lbm.registerReceiver(accelerometerUpdateReceiver, 
				new IntentFilter(getResources().getString(R.string.step_count_update_action)));
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
 		lbm.unregisterReceiver(accelerometerUpdateReceiver);
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 	}
 
 }
