 package com.android.tecla.keyboard;
 
 import ca.idi.tecla.sdk.SwitchEvent;
 import ca.idrc.tecla.framework.TeclaStatic;
 
 import com.android.inputmethod.keyboard.KeyboardSwitcher;
 import android.content.Intent;
 import com.android.inputmethod.keyboard.KeyboardView;
 
 import android.inputmethodservice.InputMethodService;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.os.Message;
 import android.view.inputmethod.EditorInfo;
 
 public class TeclaIME extends InputMethodService {
 
 	/**
 	 * Tag used for logging in the whole framework
 	 */
 	public static final String CLASS_TAG = "TeclaIME";
 
 	private static final int IMESCAN_SETUP = 0x2244;
 	private static final int SHIELDEVENT_TIMEOUT = 0x4466;
 	
 	private int[] mKeyBuff = new int[6];
 	private int mKeyCount = 0;
 	
 	private Handler mHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			if(msg.what == IMESCAN_SETUP) {
 				KeyboardView kbv = KeyboardSwitcher.getInstance().getKeyboardView();
 				boolean kb_ready = IMEAdapter.setKeyboardView(kbv);
 				if(!kb_ready) {
 					++ msg.arg1;
 					if(msg.arg1 < 10) {
 						mHandler.sendMessageDelayed(msg, 250);
 					}
 				} else {
 				}
 			} else if(msg.what == SHIELDEVENT_TIMEOUT) {
 				if(mKeyCount == 1) 
 					TeclaApp.ime.keyDownUp(mKeyBuff[0]);
 				mKeyCount = 0;
 				
 			}
 			super.handleMessage(msg);
 		}
 		
 	};
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();	
 		TeclaApp.setIMEInstance(this);
 	}
 	
 	@Override
 	public void onStartInputView(EditorInfo info, boolean restarting) {
 		Message msg = new Message();
 		msg.what = IMESCAN_SETUP;
 		msg.arg1 = 0;
 		mHandler.sendMessageDelayed(msg, 250);
 		super.onStartInputView(info, restarting);
 		TeclaApp.persistence.setIMEShowing(true);
 		TeclaAccessibilityService.getInstance().mTeclaHUDController.mAutoScanHandler.removeMessages(0);
 		TeclaAccessibilityService.getInstance().mTeclaHUDController.hide();
 	}
 
 	@Override
 	public void onFinishInputView(boolean finishingInput) {
 		IMEAdapter.setKeyboardView(null);
 		TeclaApp.persistence.setIMEShowing(false);
 		if(!TeclaApp.persistence.isHUDCancelled()) {
 			TeclaAccessibilityService.getInstance().mTeclaHUDController.show();
 			TeclaAccessibilityService.getInstance().mTeclaHUDController.mAutoScanHandler.sleep(TeclaApp.persistence.getScanDelay());
 		}
 		super.onFinishInputView(finishingInput);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.inputmethodservice.InputMethodService#onKeyDown(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		TeclaStatic.logD(CLASS_TAG, "Key " + keyCode + " down!");
 		if(mKeyCount == 0) {
 			Message msg = new Message();
 			msg.what = SHIELDEVENT_TIMEOUT;
 			msg.arg1 = 0;
 			mHandler.sendMessageDelayed(msg, 200);
 		}
 		mKeyBuff[mKeyCount++] = keyCode;
 		if(mKeyCount == 6) {
 			checkAndSendTeclaSwitchEvent();
 		}		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.inputmethodservice.InputMethodService#onKeyUp(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		TeclaStatic.logD(CLASS_TAG, "Key " + keyCode + " up!");
 		mKeyBuff[mKeyCount++] = keyCode;
 		if(mKeyCount == 6) {
 			checkAndSendTeclaSwitchEvent();			
 		}
 		return true;
 	}
 	
 	private void checkAndSendTeclaSwitchEvent() {
 		mHandler.removeMessages(SHIELDEVENT_TIMEOUT);
 		mKeyCount = 0;
 		if(mKeyBuff[0] != 59) return;
 		if(mKeyBuff[1] != 10) return;
 		if(mKeyBuff[2] != 59) return;
 		if(mKeyBuff[3] != 10) return;
 		
 		if(mKeyBuff[4] == 124 && mKeyBuff[5] == 124) {
 			// switch E1 down
 			TeclaAccessibilityService.getInstance().injectSwitchEvent(
 					new SwitchEvent(SwitchEvent.MASK_SWITCH_E1, 0)); //Primary switch pressed
 		} else if(mKeyBuff[4] == 122 && mKeyBuff[5] == 122) {
 			// switch E2 down
 			TeclaAccessibilityService.getInstance().injectSwitchEvent(
 					new SwitchEvent(SwitchEvent.MASK_SWITCH_E2, 0)); //Primary switch pressed
 		} else if(mKeyBuff[4] == 7 && mKeyBuff[5] == 7) {
 			// switch up
 			TeclaAccessibilityService.getInstance().injectSwitchEvent(
 					new SwitchEvent(0,0)); //Switches released			
 		} // TODO: write detection for J1 to J4 here
 	
		
		

 	}
 
 	public void pressHomeKey() {
 		Intent home = new Intent(Intent.ACTION_MAIN);
 		home.addCategory(Intent.CATEGORY_HOME);
 		home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		getApplicationContext().startActivity(home);
 	}
 	
 	public void pressBackKey() {
 		keyDownUp(KeyEvent.KEYCODE_BACK);
 	}
 	
 	/**
 	 * Helper to send a key down / key up pair to the current editor.
 	 */
 	private void keyDownUp(int keyEventCode) {
 		getCurrentInputConnection().sendKeyEvent(
 				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
 		getCurrentInputConnection().sendKeyEvent(
 				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
 	}	
 
 }
