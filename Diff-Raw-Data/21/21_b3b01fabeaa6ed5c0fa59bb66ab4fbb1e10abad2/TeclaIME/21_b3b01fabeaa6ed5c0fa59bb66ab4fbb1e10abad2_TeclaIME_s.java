 package com.android.tecla.keyboard;
 
 import com.android.inputmethod.keyboard.KeyboardSwitcher;
 import com.android.inputmethod.keyboard.KeyboardView;
 
 import android.inputmethodservice.InputMethodService;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 
 public class TeclaIME extends InputMethodService {
 
 	private static final int IMESCAN_SETUP = 0x2244;
 	
 	private static TeclaIME sInstance;
 
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
 				}
				
 			}
 			super.handleMessage(msg);
 		}
 		
 	};
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();		
 		sInstance = this;
 	}
 	
 	public static TeclaIME getInstance() {
 		return sInstance;
 	}
 
 	@Override
 	public void onStartInputView(EditorInfo info, boolean restarting) {
 		Message msg = new Message();
 		msg.what = IMESCAN_SETUP;
 		msg.arg1 = 0;
 		mHandler.sendMessageDelayed(msg, 250);
 		super.onStartInputView(info, restarting);
 	}
 
 	@Override
 	public void onFinishInputView(boolean finishingInput) {
 		IMEAdapter.setKeyboardView(null);
 		super.onFinishInputView(finishingInput);
 	}
 	
 	
 }
