 package org.orzlabs.android.massage;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioGroup;
 
 public class Massage extends Activity implements OnClickListener {
 	private static final String TAG = Massage.class.getSimpleName();
 
 	// TODO to Enum
 	private static final int RANDOM = 0;
 	private static final int CONTINUOUS = 1;
 
 	private static final int QUIT_ITEM = 0;
 	private boolean isVibrating = false;
 	private int vibMode;
 	private Vibrator vib;
 	private MyVibrator myVibrator;
 
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		Button button = (Button) findViewById(R.id.Massage);
 		button.setOnClickListener(this);
 		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.RadioGroup01);
 		radioGroup.setOnClickListener(this);
 		Context context = getBaseContext();
 		vib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
 		
 	}
 
 	@Override
 	public void onClick(View v) {
 		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.RadioGroup01);
 		int checkedButtonId = radioGroup.getCheckedRadioButtonId();
 		if (checkedButtonId == R.id.RadioButtonRandomRepeat) {
 			vibMode = RANDOM;
 		} else if (checkedButtonId == R.id.RadioButton01) {
 			vibMode = CONTINUOUS;
 		}
 		Log.d(TAG, "vibMode:" + vibMode);
 		
 		if (isVibrating) {
 			if (myVibrator != null) {
 				myVibrator.setToDie();
 			}
 			vib.cancel();
 			isVibrating = false;
 			Button button = (Button) findViewById(R.id.Massage);
 			button.setText(R.string.start);
 			setEnabledRadioButtons(true);
 			Log.d(TAG, "vibrate end.");
 			return;
 		}
 		if (myVibrator != null) {
 			myVibrator.setToDie();
 		}
 		myVibrator = new MyVibrator();
 		myVibrator.setToAlive();
 		Thread myVibratorThread = new Thread(myVibrator);
 		myVibratorThread.start();
 		isVibrating = true;
 		setEnabledRadioButtons(false);
 
 		Log.d(TAG, "vibrate start.");
 		Button button = (Button) findViewById(R.id.Massage);
 		button.setText(R.string.stop);
 	}
 
 	private void setEnabledRadioButtons(boolean isEnabled) {
 		findViewById(R.id.RadioButton01).setEnabled(isEnabled);
 		findViewById(R.id.RadioButtonRandomRepeat).setEnabled(isEnabled);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuItem itemQuit = menu.add(0, QUIT_ITEM, 0, "Quit");
 		itemQuit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		vib.cancel();
		if (myVibrator != null) {
			myVibrator.setToDie();
		}
 		finish();
 		return true;
 	}
 	private class MyVibrator implements Runnable {
 		private boolean isAlive;
 		@Override
 		public void run() {
 			while (isAlive) {
 				long vibratingTime = getVibratingTime();
 				if (isVibrating) {
 					Log.d(TAG, "vibrate start " + vibratingTime + "msec.");
 					vib.vibrate(vibratingTime);
 					Log.d(TAG, "vibrate end " + vibratingTime + "msec.");
 				}
 				sleep(vibratingTime);
 				Log.d(TAG, "vibrating " + vibratingTime + "msec.");
 			}
 		}
 		void setToAlive() {
 			isAlive = true;
 		}
 		void setToDie() {
 			isAlive = false;
 		}
 		private void sleep(long vibratingTime) {
 			try {
 				long sleepBonus = 0;
 				
 				if (vibMode == RANDOM) {
 					sleepBonus = (long) (Math.random() * 200L);
 				} else {
 					sleepBonus = (long) -10L;
 				}
 				long sleepTime = vibratingTime + sleepBonus;
 				Log.d(TAG, "sleep start " + sleepTime + "msec.");
 				Thread.sleep(sleepTime);
 				Log.d(TAG, "sleep end " + sleepTime + "msec.");
 			} catch (InterruptedException e) {
 				Log.e(TAG, e.getMessage(),e);
 			}
 		}
 		private long getVibratingTime() {
 			if (vibMode == RANDOM) {
 				double rand = Math.random();
 				return (long) (rand * 700);
 			}
 			return 1000L;
 		}
 		
 	}
 }
