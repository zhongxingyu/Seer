 package com.because.wimote;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.inputmethod.InputMethodManager;
 
 public class KeyboardActivity extends Activity {
 	private final int KEYCODE_CAPS_LOCK = 115;
 	private final int KEYCODE_ESCAPE = 111;
 
 	private boolean bAccel = false;
 	private List<Sensor> sensors;
 	private Sensor mAccelerometer;
 	private SensorManager mSensorManager;
 	private WiMoteUtil util;
 
 	private final SensorEventListener mySensorListener = new SensorEventListener() {
 		public void onSensorChanged(SensorEvent event) {
 			if (bAccel) {
 				float[] delta = new float[2];
 				delta = util.processAccel(-event.values[1], event.values[0], event.values[2]);
 				util.sendString("MOUSE_DELTA " + Float.toString(delta[0]) + " " + Float.toString(delta[1]));
 				try {
 					Thread.sleep(0, 10000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		util = new WiMoteUtil(this);
 
 		util.setKeyboard();
 
 		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 		sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		if (sensors.size() > 0)
 			mAccelerometer = sensors.get(0);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.keyboard_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_keyboard_accel:
 				bAccel = !bAccel;
 				return true;
 			case R.id.menu_keyboard_soft: {
 				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 				if (imm != null)
 					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 				return true;
 			}
 			case R.id.menu_keyboard_gaming:
 				bAccel = util.toggleGaming();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onResume(){
 		super.onResume();
 		mSensorManager.registerListener(mySensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
 	}
 
 	@Override
	protected void onPause(){
 		mSensorManager.unregisterListener(mySensorListener);
		super.onPause();
 	}
 
 	private void processKeys(String szKeyAction, int keyCode, int character) {
 		String szKeyName = new String();
 		switch (keyCode) {
 			case KEYCODE_ESCAPE:
 				szKeyName = util.getKeyName(R.id.Esc);
 				break;
 			case KeyEvent.KEYCODE_DEL:
 				szKeyName = util.getKeyName(R.id.Backspace);
 				break;
 			case KeyEvent.KEYCODE_TAB:
 				szKeyName = util.getKeyName(R.id.Tab);
 				break;
 			case KEYCODE_CAPS_LOCK:
 				szKeyName = util.getKeyName(R.id.CapsLock);
 				break;
 			case KeyEvent.KEYCODE_ENTER:
 				szKeyName = util.getKeyName(R.id.Enter);
 				break;
 			case KeyEvent.KEYCODE_SPACE:
 				szKeyName = util.getKeyName(R.id.Space);
 				break;
 			case KeyEvent.KEYCODE_DPAD_UP:
 				szKeyName = util.getKeyName(R.id.Up);
 				break;
 			case KeyEvent.KEYCODE_DPAD_DOWN:
 				szKeyName = util.getKeyName(R.id.Down);
 				break;
 			case KeyEvent.KEYCODE_DPAD_LEFT:
 				szKeyName = util.getKeyName(R.id.Left);
 				break;
 			case KeyEvent.KEYCODE_DPAD_RIGHT:
 				szKeyName = util.getKeyName(R.id.Right);
 				break;
 			default:
 				if (character < 32 || character > 126)
 					return;
 				szKeyName = Character.toString((char)character);
 				break;
 		}
 		util.processKeys(szKeyAction, szKeyName, true);
 	}
 
 	// These are for built-in Android soft keyboard or physical keyboard.
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		processKeys(WiMoteUtil.KEY_DOWN, keyCode, event.getUnicodeChar());
 		return super.onKeyDown(keyCode, event);
 	}
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		processKeys(WiMoteUtil.KEY_UP, keyCode, event.getUnicodeChar());
 		return super.onKeyUp(keyCode, event);
 	}
 }
