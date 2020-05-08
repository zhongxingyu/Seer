 package com.alephapps.apps.chordkeyboard.server;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import android.content.*;
 import android.inputmethodservice.InputMethodService;
 import android.inputmethodservice.Keyboard;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 
 import com.alephapps.apps.chord.ChordImpl;
 import com.alephapps.apps.chord.ChordService;
 import com.alephapps.chordapps.chordkeyboard.core.*;
 
 import static com.alephapps.apps.chord.util.LogUtils.LOGD;
 import static com.alephapps.apps.chord.util.LogUtils.LOGI;
 import static com.alephapps.apps.chord.util.LogUtils.makeLogTag;
 
 
 /**
  * The class represent the keyboard part that is presented to the user on the client.
  *
  *  Send to the client:
  *
  *  1) OnCreateInputView -
  *      If connected show connected screen
  *      If not connected show pair button and pair screen
  *  2) On Create candidate view, return null.
  *  3) On Start input view:
  *      a) If not connected show the join button. Perofrm the join seq
  *      b) if connected show the small connection view, and send the event to the client
  *
  *   4) Reciver receive key event, and use the input method to update the app field.
  *
  */
 public class ServerKeyboardService extends InputMethodService  {
     private static final String TAG = makeLogTag(ServerKeyboardService.class);
 
     private InputMethodManager mInputMethodManager;
 
     boolean mChordBound = false;
     boolean mKeyboardBound = false;
 
 
 
     private KeyboardService   mKeyboardService;
     private ChordService      mChordService;
 
     private ServerKeyboardUI    mView;
 
     /**
      * Binding actions
      */
     private void bindKeyboardService() {
         LOGD(TAG, "KEYBOARD SERVICE BIND");
         Intent intent = new Intent(this, KeyboardService.class);
         bindService(intent, mKeyboardConnection, Context.BIND_AUTO_CREATE);
     }
 
     private void unbindKeyboardService() {
         LOGD(TAG, "KEYBOARD SERVICE UNBIND");
         if (mChordBound) {
             unbindService(mKeyboardConnection);
             mChordBound = false;
         }
     }
 
 
     private void bindChordService() {
         LOGD(TAG, "CHORD SERVICE BIND");
         Intent intent = new Intent(this, ChordService.class);
         bindService(intent, mChordConnection, Context.BIND_AUTO_CREATE);
     }
 
     private void unbindChordService() {
         LOGD(TAG, "CHORD SERVICE UNBIND");
         if (mChordBound) {
             unbindService(mChordConnection);
             mChordBound = false;
         }
     }
 
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mKeyboardConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                                        IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             KeyboardService.LocalBinder binder = (KeyboardService.LocalBinder) service;
             mKeyboardService = binder.getService();
             mKeyboardBound = true;
             mView.setKeyboardActions(mKeyboardService);
             LOGD(TAG, "BOUND KEYBOARD SERVICE ");
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mKeyboardBound = false;
         }
     };
 
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mChordConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                                        IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             ChordService.LocalBinder binder = (ChordService.LocalBinder) service;
             mChordService = binder.getService();
             mChordBound = true;
             mView.setChord(mChordService);
             LOGD(TAG, "BOUND CHORD SERVICE ");
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mChordBound = false;
         }
     };
 
 
     @Override
     public void onCreate() {
         LOGI(TAG,"onCreate()");
         super.onCreate();
         mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
         LOGI(TAG,"started Method Manager");
     }
 
     /**
      * Called by the framework when your view for creating input needs to
      * be generated.  This will be called the first time your input method
      * is displayed, and every time it needs to be re-created such as due to
      * a configuration change.
      */
     public View onCreateInputView() {
         LOGD(TAG,"onCreateInputView()");
         mView = new ServerKeyboardUI();
         final View result = mView.create(getLayoutInflater(),this);
         
      // start the keyboard service
         Intent ic = new Intent(getApplicationContext(),ChordService.class);
         LOGD(TAG,"starting chord service");
         startService(ic);
         LOGD(TAG,"bind chord service");
         bindChordService();
 
         // start the keyboard service
         Intent ik = new Intent(getApplicationContext(),KeyboardService.class);
         LOGD(TAG,"starting keyboard service");
         startService(ik);
         LOGD(TAG,"bind keyboard service");
         bindKeyboardService();
        
         mView.listen(this);
         mView.setInputMethod(this);
         
         return result;
     }
 
 
     @Override
     public void onStartInput(EditorInfo attribute, boolean restarting) {
         LOGI(TAG,"onStartInput");
         super.onStartInput(attribute, restarting);
         // send this intent to the state machine, which should send it to the client
         Intent startInputIntent = DomainFactory.makeStartInputIntent(this,attribute);
         LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(startInputIntent);
     }
     
     private void postKeyEvent(int keyEventCode) {
         getCurrentInputConnection().sendKeyEvent(
                 new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
         getCurrentInputConnection().sendKeyEvent(
                 new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
     }
     
 
     @Override
     public void onFinishInput() {
     	LOGI(TAG,"onFinishInput");
         requestHideSelf(0);
         if(mView != null) mView.hideKeyboard();
         super.onFinishInput();
 
         Intent finishInputIntent = DomainFactory.makeFinishInputIntent(this);
         LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(finishInputIntent);
 
 
     }
    
     public void onClientKeyPressed(int keyCode) {
         LOGD(TAG,"OnClientKeyPressed " +keyCode);
 
         if (keyCode == KeyEvent.KEYCODE_BACK) {
         	LOGD(TAG,"KeyEvent.KEYCODE_BACK");
         	postKeyEvent(KeyEvent.KEYCODE_BACK);
         }
        else if (keyCode == KeyEvent.KEYCODE_DEL) {
         	LOGD(TAG,"KeyEvent.KEYCODE_DEL");
         	postKeyEvent(KeyEvent.KEYCODE_DEL);
         }
 
        else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == '\n') {
          	LOGD(TAG,"KeyEvent.KEYCODE_ENDER");
         	postKeyEvent(KeyEvent.KEYCODE_ENTER);
         }
         
        else if (keyCode >= '0' && keyCode <= '9') {
         	getCurrentInputConnection().commitText(String.valueOf((char) keyCode - '0' + KeyEvent.KEYCODE_0), 1);
         }
        else getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
      }
 
 
     @Override
     public void onDestroy() {
         unbindChordService();
         unbindKeyboardService();
         super.onDestroy();
     }
 }
