 package com.prototype.cruise;
 
 import java.io.IOException;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.InputType;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 
 public class BeforeDriveActivity extends FragmentActivity {
 
 	// Declare & initialize logging variable.
 	private static final String TAG = "BeforeDriveActivity";
 
 	// Declare fragments.
 	BeforeDriveFragmentCurrentStatus beforeDriveFragmentCurrentStatus;
 
 	// Declare ViewPager & Adapter variables.
 	private MyAdapter mAdapter;
 	private ViewPager mPager;
 
 	// Declare & initialize preference variables and set defaults.
 	public static final String PREFS_NAME = "MyPrefsFile";
 	public static int defaultRange = 120;
 
 	// Declare & initialize data variables.
 	public static final String DATA_NAME = "MyDataFile";
 	public static int currentRange = 120;
 	public static int driveLength = 0;
 	public static int rangeUsed = 0;
 	public static int accMistakes = 0;
 	public static int speedMistakes = 0;
 	public static int brakeMistakes = 0;
 	public static int routeFail = 0;
 	public static int chargedRange = 0;
 
 	// Declare & initialize date setting.
 	public static final String DATE_NAME = "MyDateFile";
 	public static long lastTime = 0;
 
 	// Declare temporary calculation variables.
 	public static long timeDifference;
 	public static boolean firstTime;
 	public static double relativeRange;
 	public static int previousChargedRange;
 	public static double previousRelativeRange;
 	
 	// Declare & initialize bluetooth variables.
 	private BluetoothBackEnd bt = new BluetoothBackEnd();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.viewpager);
 
 		mAdapter = new MyAdapter(getSupportFragmentManager());
 
 		mPager = (ViewPager) findViewById(R.id.pager);
 		mPager.setAdapter(mAdapter);
 
 		beforeDriveFragmentCurrentStatus = (BeforeDriveFragmentCurrentStatus) mAdapter
 				.getItem(0);
 
 		loadSettings();
 		loadData();
 		loadDate();
 		calc();
 		btConnect();
 	}
 
 	public static class MyAdapter extends FragmentPagerAdapter {
 
 		public MyAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public int getCount() {
 			return 1;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			switch (position) {
 			case 0:
 				return new BeforeDriveFragmentCurrentStatus();
 			default:
 				return null;
 			}
 		}
 	}
 
 	public void calc() {
 		previousRelativeRange = (double) ((double) currentRange / (double) defaultRange);
 		// calculate range and charge time
 		timeDifference = System.currentTimeMillis() - lastTime;
 		if (lastTime == 0) {
 			firstTime = true;
 			currentRange = defaultRange;
 		} else {
 			previousChargedRange = chargedRange;
 			// chargedRange = (int) (double) (timeDifference / 300000);
 			chargedRange = (int) (double) (timeDifference / 3000);
 			if (chargedRange > rangeUsed) {
 				chargedRange = rangeUsed;
 			}
 			if (currentRange + chargedRange >= defaultRange) {
 				currentRange = defaultRange;
 			} else {
 				currentRange = currentRange
 						+ (chargedRange - previousChargedRange);
 			}
 		}
 		relativeRange = (double) currentRange / (double) defaultRange;
 		saveData();
 	}
 	
 	public void btConnect() {
 		bt.findBT(true, this);
 		try {
 			bt.openBT(true, this);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadSettings() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		defaultRange = settings.getInt("defaultRange", defaultRange);
 	}
 
 	public void loadData() {
 		SharedPreferences data = getSharedPreferences(DATA_NAME, 0);
 		currentRange = data.getInt("currentRange", currentRange);
 		driveLength = data.getInt("driveLength", driveLength);
 		rangeUsed = data.getInt("rangeUsed", rangeUsed);
 		accMistakes = data.getInt("accMistakes", accMistakes);
 		speedMistakes = data.getInt("speedMistakes", speedMistakes);
 		brakeMistakes = data.getInt("brakeMistakes", brakeMistakes);
 		routeFail = data.getInt("routeFail", routeFail);
 		chargedRange = data.getInt("chargedRange", chargedRange);
 	}
 
 	public void loadDate() {
 		SharedPreferences date = getSharedPreferences(DATE_NAME, 0);
 		lastTime = date.getLong("lastTime", lastTime);
 	}
 
 	public void saveSettings() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putInt("defaultRange", defaultRange);
 		editor.commit();
 	}
 
 	public void saveData() {
 		SharedPreferences data = getSharedPreferences(DATA_NAME, 0);
 		SharedPreferences.Editor editor = data.edit();
 		editor.putInt("currentRange", currentRange);
 		editor.putInt("driveLength", driveLength);
 		editor.putInt("rangeUsed", rangeUsed);
 		editor.putInt("accMistakes", accMistakes);
 		editor.putInt("speedMistakes", speedMistakes);
 		editor.putInt("brakeMistakes", brakeMistakes);
 		editor.putInt("routeFail", routeFail);
 		editor.putInt("chargedRange", chargedRange);
 		editor.commit();
 	}
 
 	public void saveDate() {
 		SharedPreferences date = getSharedPreferences(DATE_NAME, 0);
 		SharedPreferences.Editor editor = date.edit();
 		editor.putLong("lastTime", lastTime);
 		editor.commit();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.drive, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_defaultrange:
 			setDefaultRange();
 			return true;
 		case R.id.action_reset:
 			reset();
 			return true;
 		case R.id.action_connect_bt:
 			btConnect();
			return true;
 		case R.id.action_close_bt:
 			try {
 				bt.closeBT(true, this);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void setDefaultRange() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Enter default range in km");
 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 		input.setText("" + defaultRange + "");
 		input.setInputType(InputType.TYPE_CLASS_PHONE);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				defaultRange = Integer.parseInt(input.getText().toString());
 				saveSettings();
 				Intent intent = getIntent();
 				finish();
 				startActivity(intent);
 			}
 		});
 
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 					}
 				});
 
 		alert.show();
 	}
 
 	public void reset() {
 		// Reset all defaults and restart activity.
 		defaultRange = 120;
 		lastTime = 0;
 		currentRange = defaultRange;
 		chargedRange = 0;
 		saveSettings();
 		saveData();
 		saveDate();
 		Intent intent = getIntent();
 		finish();
 		startActivity(intent);
 	}
 }
