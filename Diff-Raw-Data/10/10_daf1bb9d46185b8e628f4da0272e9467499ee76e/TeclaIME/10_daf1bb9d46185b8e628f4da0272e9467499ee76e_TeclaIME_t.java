 package com.android.tecla.keyboard;
 
 import com.android.inputmethod.keyboard.KeyboardSwitcher;
 import android.content.Intent;
 import com.android.inputmethod.keyboard.KeyboardView;
 
 import android.inputmethodservice.InputMethodService;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.os.Message;
 import android.view.inputmethod.EditorInfo;
 
 public class TeclaIME extends InputMethodService {
 
 	private static final int IMESCAN_SETUP = 0x2244;
 	
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
		TeclaAccessibilityService.getInstance().mTeclaHUDController.hide();
 	}
 
 	@Override
 	public void onFinishInputView(boolean finishingInput) {
 		IMEAdapter.setKeyboardView(null);
 		TeclaApp.persistence.setIMEShowing(false);
		TeclaAccessibilityService.getInstance().mTeclaHUDController.show();
 		super.onFinishInputView(finishingInput);
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
