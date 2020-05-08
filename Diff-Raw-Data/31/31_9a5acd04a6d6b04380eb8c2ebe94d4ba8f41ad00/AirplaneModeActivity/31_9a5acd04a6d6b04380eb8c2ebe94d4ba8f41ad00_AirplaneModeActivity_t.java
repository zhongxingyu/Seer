 
 package jp.co.c_lis.glassradiosdisabler.ui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 
 import com.google.android.glass.app.Card;
 import com.google.android.glass.touchpad.Gesture;
 import com.google.android.glass.touchpad.GestureDetector;
 
 import jp.co.c_lis.glassradiosdisabler.Configuration;
 import jp.co.c_lis.glassradiosdisabler.R;
 import jp.co.c_lis.glassradiosdisabler.Setting;
 
 public class AirplaneModeActivity extends Activity {
     private static final String TAG = "AirplaneModeActivity";
 
     private static final int REQUEST_LOCK = 0x0;
     private static final int REQUEST_UNLOCK = 0x1;
 
     private Setting mSetting = null;
 
     private GestureDetector mGestureDetector = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mSetting = Setting.getInstance(this);
         mGestureDetector = new GestureDetector(this);
 
         Card card = new Card(this);
         card.setText(R.string.tap_to_turn_radios_off);
         card.setFootnote(R.string.long_tap_to_turn_radios_off_with_pattern);
         mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
             @Override
             public boolean onGesture(Gesture gesture) {
                 if (gesture == Gesture.TAP) {
                     Configuration.setAirplaneMode(AirplaneModeActivity.this, true);
                    mSetting.clearPattern();
                     finishOnMessage(R.string.all_radios_turned_off);
                     return true;
                 } else if (gesture == Gesture.LONG_PRESS) {
                     displayPatternActivity(REQUEST_LOCK);
                     return true;
                 }
                 return false;
             }
         });
 
         if (Configuration.getAirplaneModeState(this)) {
             card.setText(R.string.would_you_like_to_turn_radios_on);
 
            if (mSetting.hasPattern()) {
                 card.setFootnote(R.string.long_tap_to_enter_pattern);
                 mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
                     @Override
                     public boolean onGesture(Gesture gesture) {
                         if (gesture == Gesture.LONG_PRESS) {
                             displayPatternActivity(REQUEST_UNLOCK);
                             return true;
                         }
                         return false;
                     }
                 });
             } else {
                 card.setFootnote(R.string.long_tap_to_turn_radios_on);
                 mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
                     @Override
                     public boolean onGesture(Gesture gesture) {
                         if (gesture == Gesture.LONG_PRESS) {
                             Configuration.setAirplaneMode(AirplaneModeActivity.this, false);
                             finishOnMessage(R.string.all_radios_turned_on);
                             return true;
                         }
                         return false;
                     }
                 });
             }
         }
 
         setContentView(card.toView());
     }
 
     @Override
     public boolean onGenericMotionEvent(MotionEvent event) {
         return mGestureDetector.onMotionEvent(event);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         if (resultCode == RESULT_OK) {
             String pattern = data != null ? data.getStringExtra(PatternActivity.KEY_PATTERN) : null;
             if (requestCode == REQUEST_LOCK) {
                mSetting.setPattern(pattern);
                 Configuration.setAirplaneMode(this, true);
                 finishOnMessage(R.string.all_radios_turned_off_with_pattern);
             } else if (requestCode == REQUEST_UNLOCK) {
                 String p = mSetting.getPattern();
                 if (p == null || p.equals(pattern)) {
                     Configuration.setAirplaneMode(this, false);
                     finishOnMessage(R.string.all_radios_turned_on);
                 } else {
                     Card card = new Card(this);
                     card.setText(R.string.pattern_does_not_match);
                     card.setFootnote(R.string.long_tap_to_reenter_pattern);
                     mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
                         @Override
                         public boolean onGesture(Gesture gesture) {
                             if (gesture == Gesture.LONG_PRESS) {
                                 displayPatternActivity(REQUEST_UNLOCK);
                                 return true;
                             }
                             return false;
                         }
                     });
                     setContentView(card.toView());
                 }
             }
         }
 
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     private void displayPatternActivity(int code) {
         Intent intent = new Intent(this, PatternActivity.class);
         startActivityForResult(intent, code);
     }
 
     private void finishOnMessage(int resId) {
         finishOnMessage(getString(resId));
     }
 
     private void finishOnMessage(String message) {
         Intent intent = new Intent(this, MessageActivity.class);
         intent.putExtra(MessageActivity.BUNDLE_KEY_MESSAGE, message);
         intent.putExtra(MessageActivity.BUNDLE_KEY_DELAY_IN_MILISEC, 1500L);
         startActivity(intent);
         finish();
     }
 }
