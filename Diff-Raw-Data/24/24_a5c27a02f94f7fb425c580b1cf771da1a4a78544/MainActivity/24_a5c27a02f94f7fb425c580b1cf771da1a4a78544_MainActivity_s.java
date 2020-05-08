 package com.ds.itimer;
 
 import java.util.Observable;
 import java.util.Observer;
 
import android.media.Ringtone;
import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Vibrator;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity implements View.OnClickListener, Observer {
 
     
     private Button mButton[];
     private ImageButton mImageButton[];
     private ButtonState mState;
     private TextView mInput;
     private TextView mOutput;
     private ToggleButton mToggle;
    private Ringtone mRing;
     private Vibrator mVib;
     private boolean mIsSr;
     private Preferences mPref;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         mPref = new Preferences(this);
         mState = new ButtonState();
         mState.addObserver(this);
         mButton = new Button[ButtonState.BUTTON_TOTAL];
         mImageButton = new ImageButton[ButtonState.BUTTON_TOTAL];
         LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = layoutInflater.inflate(R.layout.activity_main, null);
         mButton[ButtonState.BUTTON_0] = (Button)view.findViewById(R.id.button0);
         mButton[ButtonState.BUTTON_1] = (Button)view.findViewById(R.id.button1);
         mButton[ButtonState.BUTTON_2] = (Button)view.findViewById(R.id.button2);
         mButton[ButtonState.BUTTON_3] = (Button)view.findViewById(R.id.button3);
         mButton[ButtonState.BUTTON_4] = (Button)view.findViewById(R.id.button4);
         mButton[ButtonState.BUTTON_5] = (Button)view.findViewById(R.id.button5);
         mButton[ButtonState.BUTTON_6] = (Button)view.findViewById(R.id.button6);
         mButton[ButtonState.BUTTON_7] = (Button)view.findViewById(R.id.button7);
         mButton[ButtonState.BUTTON_8] = (Button)view.findViewById(R.id.button8);
         mButton[ButtonState.BUTTON_9] = (Button)view.findViewById(R.id.button9);
         mImageButton[ButtonState.BUTTON_STOP]  = (ImageButton)view.findViewById(R.id.buttonStop);
         mImageButton[ButtonState.BUTTON_START] = (ImageButton)view.findViewById(R.id.buttonStart);
         mInput = (TextView)view.findViewById(R.id.textView2);
         mOutput = (TextView)view.findViewById(R.id.textView4);
         mToggle = (ToggleButton)view.findViewById(R.id.toggleButton1);
         mToggle.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (((ToggleButton) v).isChecked()) {
                     mIsSr = true;
                 }
                 else {
                     mIsSr = false;
                 }
                 mState.setStandardRate(mIsSr); 
             }
         });
         
         for(int button = ButtonState.BUTTON_0; button <= ButtonState.BUTTON_9; button++) {
             mButton[button].setOnClickListener(this);
         }
         for(int button = ButtonState.BUTTON_STOP; button < ButtonState.BUTTON_TOTAL; button++) {
             mImageButton[button].setOnClickListener(this);
         }
         
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRing = RingtoneManager.getRingtone(getApplicationContext(), notification);
         mVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         mIsSr = false;
 
         setContentView(view);        
     }
 
     /**
      * 
      */
     @Override 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if(KeyEvent.KEYCODE_MENU == keyCode) {
             /*
              * Exclude menu from all keys
              */
             return false;
         }
         if(mPref.useAnyKey()) {
             mState.runState(ButtonState.TOGGLE);
             enableButtons(mState.isStopped());
             return true;
         }
         if(KeyEvent.KEYCODE_BACK == keyCode) {
             /*
              * Special handling for back key
              */
             super.onBackPressed();
             return true;
         }
         return false;        
     }
         
     /*
      * For being on tab this activity discards back to main activity
      * (non-Javadoc)
      * @see android.app.Activity#onBackPressed()
      */
     
     /* (non-Javadoc)
      * @see android.app.Activity#onResume()
      */
     @Override
     public void onResume() {
         super.onResume();
         Helper.setOrientationAndOn(this);
     }
 
     /* (non-Javadoc)
      * @see android.app.Activity#onResume()
      */
     @Override
     public void onPause() {
         super.onPause();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     /* (non-Javadoc)
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         switch(item.getItemId()) {
         
             case R.id.menu_settings:
                 /*
                  * Bring up preferences
                  */
                 startActivity(new Intent(this, PrefActivity.class));
                 break;
                 
             case R.id.menu_help:    
                 String url = "http://apps4av.com";
                 Intent it = new Intent(Intent.ACTION_VIEW);
                 it.setData(Uri.parse(url));
                 startActivity(it);
                 break;
         }
         return true;
     }
 
     @Override
     public void onClick(View v) {
         int event = -1;
         switch(v.getId()) {
             case R.id.button0:
                 event = ButtonState.BUTTON_0;
                 break;
             case R.id.button1:
                 event = ButtonState.BUTTON_1;
                 break;
             case R.id.button2:
                 event = ButtonState.BUTTON_2;
                 break;
             case R.id.button3:
                 event = ButtonState.BUTTON_3;
                 break;
             case R.id.button4:
                 event = ButtonState.BUTTON_4;
                 break;
             case R.id.button5:
                 event = ButtonState.BUTTON_5;
                 break;
             case R.id.button6:
                 event = ButtonState.BUTTON_6;
                 break;
             case R.id.button7:
                 event = ButtonState.BUTTON_7;
                 break;
             case R.id.button8:
                 event = ButtonState.BUTTON_8;
                 break;
             case R.id.button9:
                 event = ButtonState.BUTTON_9;
                 break;
             case R.id.buttonStart:
                 event = ButtonState.BUTTON_START;
                 break;
             case R.id.buttonStop:
                 event = ButtonState.BUTTON_STOP;
                 break;
         }
         
         mState.runState(event);
         mInput.setText(mState.getTimeString());
         /*
          * Disable some buttons when running
          */
         enableButtons(mState.isStopped());
     }
 
     /**
      * 
      */
     private synchronized void enableButtons(boolean state) {
         mToggle.setEnabled(state);
         for(int button = ButtonState.BUTTON_0; button <= ButtonState.BUTTON_9; button++) {
             mButton[button].setEnabled(state);
         }
     }
 
     /**
      * Use default notification
      */
     @Override
     public void update(Observable arg0, Object arg1) {
         int timeLeft = (Integer)arg1;
         
         Message msg = mHandler.obtainMessage();
         
         msg.what = (Integer)timeLeft;
         mHandler.sendMessage(msg);
     }
     
     /**
      * This leak warning is not an issue if we do not post delayed messages, which is true here.
      */
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             int timeLeft = msg.what;
             if(timeLeft >= 0) {
                 if(!mIsSr) {
                     mOutput.setText(Helper.timeToFour(timeLeft));
                 } 
                 else {
                     mOutput.setText(Helper.degreesToFour(timeLeft));                    
                 }
             }
         
             
             /*
              * Expired?
              */
             if(timeLeft <= 0) {
                 /*
                  * Feedback to stop timer after a run
                  */
                 try {
                    mRing.play();
                     if(mPref.useVibrator()) {
                         mVib.vibrate(1000);
                     }
                 }
                 catch (Exception e) {    
                 }
             }
             
             /*
              * Enable buttons when stopped
              */
             enableButtons(mState.isStopped());
         }
     };
 
 }
 
