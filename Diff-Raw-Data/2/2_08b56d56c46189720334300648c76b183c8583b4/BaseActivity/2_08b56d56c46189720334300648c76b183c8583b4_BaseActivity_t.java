 package com.shahanp.invisibletouch.internal;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 import com.shahanp.invisibletouch.R;
 import com.shahanp.invisibletouch.utils.Log;
 import com.shahanp.invisibletouch.utils.Log.Level;
 import com.shahanp.invisibletouch.view.MainMenuActivity;
 
 public abstract class BaseActivity extends Activity implements IGestures,
         IBrailleKeyboard, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
 
     protected final static int GESTURE_TAP = 0;
     protected final static int GESTURE_LONGTAP = 1;
     protected final static int GESTURE_SWIPE = 2;
     protected final static int SWIPE_LEFT = 10;
     protected final static int SWIPE_RIGHT = 11;
     protected final static int SWIPE_UP = 12;
     protected final static int SWIPE_DOWN = 13;
     private final static int FLING_THRESHOLD = 100;
     protected boolean mStoppedFromNewScreen = false;
     protected int mCurrentGesture = -1;
     private boolean isFinishing = false;
     private int mLastSwipeDirection = -1;
     private GestureDetector mGestureDetector;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mGestureDetector = new GestureDetector(this, this);
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 
         if (getIntent().hasExtra(PluginManager.FLAG_REQUEST_FOREGROUND))
             InvisibleTouchApplication.getInstance().getPluginManager().requestForeground();
 
         init();
     }
 
     @Override
     public boolean dispatchTouchEvent(MotionEvent ev) {
         if (!mGestureDetector.onTouchEvent(ev))
             return super.dispatchTouchEvent(ev);
         else
             return true;
     }
 
     @Override
     public void onClick(View v) {
         int _id = v.getId();
         Log.announce("" + mCurrentGesture, Level.INFO);
         switch (_id) {
             case R.id.item_one_one:
                 if (mCurrentGesture == GESTURE_TAP)
                     onKeyOne();
                 else if (mCurrentGesture == GESTURE_LONGTAP)
                    onLongKeyOne();
                 break;
             case R.id.item_one_two:
                 if (mCurrentGesture == GESTURE_TAP)
                     onKeyTwo();
                 else if (mCurrentGesture == GESTURE_LONGTAP)
                     onLongKeyTwo();
                 break;
             case R.id.item_one_three:
                 if (mCurrentGesture == GESTURE_TAP)
                     onKeyThree();
                 else if (mCurrentGesture == GESTURE_LONGTAP)
                     onLongKeyThree();
                 break;
             case R.id.item_two_one:
                 if (mCurrentGesture == GESTURE_TAP)
                     onKeyFour();
                 else if (mCurrentGesture == GESTURE_LONGTAP)
                     onLongKeyFour();
                 break;
             case R.id.item_two_two:
                 if (mCurrentGesture == GESTURE_TAP)
                     onKeyFive();
                 else if (mCurrentGesture == GESTURE_LONGTAP)
                     onLongKeyFive();
                 break;
             case R.id.item_two_three:
                 if (mCurrentGesture == GESTURE_TAP)
                     onKeySix();
                 else if (mCurrentGesture == GESTURE_LONGTAP)
                     onLongKeySix();
                 break;
 
             default:
                 break;
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         Log.announce("KeyCode : " + keyCode, Level.INFO);
         if (!event.isTracking()) {
             event.startTracking();
             switch (keyCode) {
                 case KeyEvent.KEYCODE_VOLUME_DOWN:
                     onVolumeDownKeyShortPress();
                     return true;
                 case KeyEvent.KEYCODE_VOLUME_UP:
                     onVolumeUpKeyShortPress();
                     return true;
                 case KeyEvent.KEYCODE_POWER:
                     onPowerKeyShortPress();
                     return true;
                 case KeyEvent.KEYCODE_CAMERA:
                     onCameraKeyShortPress();
                     return true;
                 case KeyEvent.KEYCODE_HEADSETHOOK:
                     Toast.makeText(this, "Headset", Toast.LENGTH_SHORT).show();
                     return true;
                 default:
                     break;
             }
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onKeyLongPress(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_VOLUME_DOWN:
                 onVolumeDownKeyLongPress();
                 return true;
             case KeyEvent.KEYCODE_VOLUME_UP:
                 onVolumeUpKeyLongPress();
                 return true;
             case KeyEvent.KEYCODE_POWER:
                 onPowerKeyLongPress();
                 return true;
             case KeyEvent.KEYCODE_CAMERA:
                 onCameraKeyLongPress();
                 return true;
             default:
                 break;
         }
         return super.onKeyLongPress(keyCode, event);
     }
 
     @Override
     public void startActivity(Intent intent) {
         mStoppedFromNewScreen = true;
         super.startActivity(intent);
     }
 
     @Override
     public void startActivityForResult(Intent intent, int requestCode) {
         mStoppedFromNewScreen = true;
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         super.startActivityForResult(intent, requestCode);
     }
 
     public void setActivityResult(int result) {
         InvisibleTouchApplication.getInstance().setResult(result);
         setResult(result);
     }
 
     @Override
     protected void onResume() {
         if (InvisibleTouchApplication.getInstance().shouldKillApp())
             this.finish();
         super.onResume();
     }
 
     @Override
     protected void onPause() {
         if (InvisibleTouchApplication.getInstance().getCallManager().isCallScreenPrioritized())
             mStoppedFromNewScreen = true;
         super.onPause();
     }
 
     @Override
     public void onBackPressed() {
     }
 
     @Override
     public void finish() {
         isFinishing = true;
         super.finish();
     }
 
     @Override
     public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
         mCurrentGesture = GESTURE_TAP;
         return false;
     }
 
     @Override
     public boolean onDoubleTap(MotionEvent motionEvent) {
         return false;
     }
 
     @Override
     public boolean onDoubleTapEvent(MotionEvent motionEvent) {
         return false;
     }
 
     @Override
     public boolean onDown(MotionEvent motionEvent) {
         return false;
     }
 
     @Override
     public void onShowPress(MotionEvent motionEvent) {
 
     }
 
     @Override
     public boolean onSingleTapUp(MotionEvent motionEvent) {
         mCurrentGesture = GESTURE_TAP;
         mLastSwipeDirection = -1;
         return false;
     }
 
     @Override
     public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
         return false;
     }
 
     @Override
     public void onLongPress(MotionEvent motionEvent) {
         mCurrentGesture = GESTURE_LONGTAP;
         mLastSwipeDirection = -1;
         onScreenLongPress();
     }
 
     @Override
     public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float velocityX, float velocityY) {
         mCurrentGesture = GESTURE_SWIPE;
         int direction = getFlingDirection(motionEvent, motionEvent2);
         boolean isValidFling = validateFling(motionEvent, motionEvent2);
         if (isValidFling) {
             switch (direction) {
                 case SWIPE_DOWN:
                     if (mLastSwipeDirection == SWIPE_DOWN)
                         onDoubleSwipeDown();
                     else
                         onSwipeDown();
                     break;
                 case SWIPE_LEFT:
                     if (mLastSwipeDirection == SWIPE_LEFT)
                         onDoubleSwipeLeft();
                     else
                         onSwipeLeft();
                     break;
                 case SWIPE_RIGHT:
                     if (mLastSwipeDirection == SWIPE_RIGHT)
                         onDoubleSwipeRight();
                     else
                         onSwipeRight();
                     break;
                 case SWIPE_UP:
                     if (mLastSwipeDirection == SWIPE_UP)
                         onDoubleSwipeUp();
                     else
                         onSwipeUp();
                     break;
                 default:
                     mLastSwipeDirection = -1;
                     return false;
             }
             mLastSwipeDirection = direction;
             return true;
         } else {
             mLastSwipeDirection = -1;
             return false;
         }
 
     }
 
     private boolean validateFling(MotionEvent motionEvent, MotionEvent motionEvent2) {
         return Math.abs(motionEvent.getX() - motionEvent2.getX()) > FLING_THRESHOLD || Math.abs(motionEvent.getY() - motionEvent2.getY()) > FLING_THRESHOLD;
     }
 
     private int getFlingDirection(MotionEvent event1, MotionEvent event2) {
         float oX = event1.getX();
         float oy = event1.getY();
 
         float nX = event2.getX();
         float nY = event2.getY();
 
         float xT = nX - oX;
         float yT = nY - oy;
 
         if (Math.abs(xT) > Math.abs(yT)) {
             if (xT > 0)
                 return SWIPE_RIGHT;
             else
                 return SWIPE_LEFT;
         } else {
             if (yT > 0)
                 return SWIPE_DOWN;
             else
                 return SWIPE_UP;
         }
     }
 
     protected abstract void init();
 
     protected abstract void onAttachView(int id, View view);
 }
