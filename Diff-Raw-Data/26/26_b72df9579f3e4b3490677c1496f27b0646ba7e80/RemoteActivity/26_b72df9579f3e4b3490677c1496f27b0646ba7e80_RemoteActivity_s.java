 package de.questmaster.fatremote;
 
 import java.net.ConnectException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.questmaster.fatremote.FatRemoteSettings.Settings;
 
 public class RemoteActivity extends Activity {
 
 	protected static final String INTENT_ALLFATS = "de.questmaster.fatremote.fats";
 
 	private static final int INTENT_SELECT_FAT = 0;
 	public static final int ON_SETTINGS_CHANGE = 1;
 
 	private Settings mSettings = new FatRemoteSettings.Settings();
 	private AudioManager audioManager;
 
 	private boolean showDebugView = false;
 	private Activity c = this;
 	private short keyCode;
 	private short keyModifier;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.remote);
 
 		/* start audio */
 		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		audioManager.loadSoundEffects();
 
 		/* read IP */
 		mSettings.ReadSettings(this);
 
 		// check if WIFI available
 		if (!NetworkUtil.getInstance(this).isWifiEnabled()) {
 			Toast.makeText(this, R.string.app_err_enwifi, Toast.LENGTH_LONG).show();
 		}
 
 		if (mSettings.m_sFatIP.equals("")) {
 			AlertDialog.Builder notification = new Builder(this);
 			notification.setIcon(android.R.drawable.ic_menu_manage);
 			notification.setTitle(R.string.dialog_noip_title);
 			notification.setMessage(R.string.dialog_noip_text);
 			notification.show();
 		}
 	}
 
 	/**
 	 * Add menu items
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 
 		// For testing purpose
 		if (StartActivity.onEmulator) {
 			MenuItem mi = menu.findItem(R.id.MENU_ITEM_DEBUG);
 			mi.setVisible(true);
 		}
 
 		return true;
 	}
 
 	/**
 	 * Define menu action
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.MENU_ITEM_SELECTFAT:
 			// // TODO: show FAT selection
 			// String ips[] = discoverFAT();
 			//
 			// if (ips != null) {
 			// Intent selectFAT = new Intent(Intent.ACTION_PICK);
 			// selectFAT.putExtra(INTENT_ALLFATS, ips);
 			//
 			// new SelectFAT().startActivityForResult(selectFAT,
 			// INTENT_SELECT_FAT);
 			// }
 
 			Intent iSettings = new Intent();
 			iSettings.setClass(this, FatRemoteSettings.class);
 			startActivityForResult(iSettings, ON_SETTINGS_CHANGE);
 
 			break;
 		case R.id.MENU_ITEM_DEBUG:
 			if (showDebugView) {
 				setContentView(R.layout.main);
 			} else
 				setContentView(R.layout.debug);
 			break;
 		case R.id.MENU_ITEM_EXIT:
 			// FIXME: exit is not working
 			System.exit(0);
 			break;
 		default:
 			// should not happen
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case INTENT_SELECT_FAT: {
 			// if (resultCode == Activity.RESULT_OK) {
 			// try {
 			// mFATip =
 			// InetAddress.getByName(data.getStringExtra(INTENT_ALLFATS));
 			// } catch (UnknownHostException e) {
 			// e.printStackTrace();
 			// }
 			// } else {
 			// mFATip = null;
 			// }
 
 			break;
 		}
 		case ON_SETTINGS_CHANGE: {
 			mSettings.ReadSettings(this);
 			break;
 		}
 		}
 	}
 
 	public void onDebugButton(View v) {
 //		short in1 = Short.decode(((TextView) findViewById(R.id.pos1)).getText().toString());
 //		short in2 = Short.decode(((TextView) findViewById(R.id.pos2)).getText().toString());
 		short in3 = Short.decode(((TextView) findViewById(R.id.pos3)).getText().toString());
 		short in4 = Short.decode(((TextView) findViewById(R.id.pos4)).getText().toString());
 
 		keyCode = in3;
 		keyModifier = in4;
 
 		// send keyCode
 		invokeSend();
 	}
 
 	public boolean onKeyDown(int keyId, KeyEvent event) {
 		short key = 0;
 		int unicode = 0;
 
 		// get key
 		unicode = event.getUnicodeChar();
 		key = (short) unicode;
 		Log.i(StartActivity.LOG_TAG, "KeyEvent: " + keyId + ", unicode: " + unicode);
 
 		// WORKAROUND for some keys
 		if (key == 0) {
 			if (keyId == 67 ) key = 8; // Backspace
 			
 		}
 		
 		// FIXME: Some key are not working, e.g. öäü (extended ASCII)...
 		// Send Character
 		if (key != 0) {
 			keyCode = 0xf9;
 			keyModifier = key;
 
 			// send keyCode
 			invokeSend();
 			return true;
 		} else
 			return super.onKeyDown(keyId, event);
 	}
 
 	private void showKeyboard() {
 		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		mgr.toggleSoftInput(0, 0);
 	}
 
 	public void getKeyCode(View v) {
 
 		// solve to code
 		if (v instanceof ImageView) {
 			ImageView bv = (ImageView) v;
 			String t = (String) bv.getTag();
 
 			if (t.equals("power"))
 				keyCode = 0x38;
 			else if (t.equals("home"))
 				keyCode = 0x39;
 			else if (t.equals("up"))
 				keyCode = 0x40;
 			else if (t.equals("left"))
 				keyCode = 0x07;
 			else if (t.equals("ok"))
 				keyCode = 0x0E;
 			else if (t.equals("right"))
 				keyCode = 0x06;
 			else if (t.equals("back"))
 				keyCode = 0x1B;
 			else if (t.equals("down"))
 				keyCode = 0x41;
 			else if (t.equals("info"))
 				keyCode = 0x47;
 			else if (t.equals("text")) {
 				showKeyboard();
 				return;
 			} else if (t.equals("rew"))
 				keyCode = 0x52;
 			else if (t.equals("menu"))
 				keyCode = 0x1A;
 			else if (t.equals("ff"))
 				keyCode = 0x51;
 			else if (t.equals("prev"))
 				keyCode = 0x31;
 			else if (t.equals("playpause"))
 				keyCode = 0x4F;
 			else if (t.equals("next"))
 				keyCode = 0x30;
 			else if (t.equals("volup"))
 				keyCode = 0x32;
 			else if (t.equals("stop"))
 				keyCode = 0x54;
 			else if (t.equals("zoomin"))
 				keyCode = 0x35;
 			else if (t.equals("voldown"))
 				keyCode = 0x33;
 			else if (t.equals("mute"))
 				keyCode = 0x59;
 			else if (t.equals("zoomout"))
 				keyCode = 0x08;
 		}
 		keyModifier = 0x00;
 
 		// send keyCode
 		invokeSend();
 	}
 
 	private void invokeSend() {
 		// ring / vibrate
 		if ((!mSettings.m_bOverride && audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) != 0) || (mSettings.m_bOverride && mSettings.m_bTone)) {
 			audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
 		}
 
 		if (mSettings.m_bVibrate) {
 			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 			vibrator.vibrate(25);
 		}
 
		// FIXME: findbyid returns null, why?!
 		ImageView sending = (ImageView) this.findViewById(R.id.sendLED);
 		sending.setImageResource(R.drawable.light_highlight);
 		sending.invalidate();
 
 		// start sending in new thread
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					NetworkUtil.getInstance(c).sendCode(mSettings.m_sFatIP, new short[] { 0x48, 0x12, keyCode, keyModifier });
 				} catch (final ConnectException e) {
 					c.runOnUiThread(new Runnable() {
 						public void run() {
 							Toast.makeText(c, e.getMessage(), Toast.LENGTH_LONG).show();
 						}
 					});
 				}
 
 				c.runOnUiThread(new Runnable() {
 					public void run() {
 						// reset send image
 						ImageView sending = (ImageView) findViewById(R.id.sendLED);
 						sending.setImageResource(R.drawable.light_normal);
 					}
 				});
 			}
 		}).start();
 	}
 }
