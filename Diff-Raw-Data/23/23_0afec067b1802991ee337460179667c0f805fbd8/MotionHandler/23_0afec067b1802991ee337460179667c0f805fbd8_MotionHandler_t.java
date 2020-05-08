 package org.pa5;
 
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class MotionHandler extends Handler
 {
     public interface CountdownListener
     {
         public void countdownDone();
     }
 
     public static final int DEFAULT_START_NUMBER = 10;
     public static final int MINIMUM_NUMBER = 0;
    public static final int MESSAGE_START_NUMBER = 1;
    public static final int MESSAGE_UPDATE_NUMBER = 2;
     public static final int TIME_FOR_ONE_NUMBER_MS = 1000;
     private RelativeTextView mNumberView;
     private RelativeLayout mParent;
     private int mCurrentNumber;
     private CountdownListener mListener;
 
     /**
      * The containing parent has to be a RelativeLayout. This is because it will make updating
      * the motion handler with new functionality in the future much easier.
      * 
      */
     public MotionHandler(TextView theNumber, RelativeLayout containingParent, CountdownListener listener)
     {
         mNumberView = new RelativeTextView(theNumber);
         mCurrentNumber = DEFAULT_START_NUMBER;
         mParent = containingParent;
         mListener = listener;
     }
 
     /**
      * This method can be used to start the count down from a number other than the default,
      * or to set the number to a known value at any point, for example after the listener is fired.
      * 
      */
     public void setCurrentNumber(int currentNumber)
     {
         mCurrentNumber = currentNumber;
     }
 
     @Override
     public void handleMessage(Message msg)
     {
         switch (msg.what)
         {
            case MESSAGE_START_NUMBER:
                 updateNumberView();
                 makeNumberVisible();
                 moveNumberTo(0);
                 Animation animation = new TranslateAnimation(0, mParent.getWidth(), 0, 0);
                 animation.setDuration(TIME_FOR_ONE_NUMBER_MS);
                 animation.setFillAfter(true);
                 mNumberView.startAnimation(animation);
                msg = obtainMessage(MESSAGE_UPDATE_NUMBER);
                 sendMessageDelayed(msg, TIME_FOR_ONE_NUMBER_MS);
                 break;
            case MESSAGE_UPDATE_NUMBER:
                 hideNumber();
                 decrementNumber();
                 if (mCurrentNumber < MINIMUM_NUMBER)
                 {
                     setCurrentNumber(DEFAULT_START_NUMBER);
                     mListener.countdownDone();
                 }
                 else
                 {
                    sendEmptyMessage(MESSAGE_START_NUMBER);
                 }
         }
     }
 
     private void updateNumberView()
     {
         mNumberView.setText("" + mCurrentNumber);
     }
 
     private void decrementNumber()
     {
         --mCurrentNumber;
     }
 
     public void moveNumberTo(int leftMargin)
     {
         mNumberView.setLeftMargin(leftMargin);
     }
 
     private void makeNumberVisible()
     {
         mNumberView.setVisibility(View.VISIBLE);
     }
 
     private void hideNumber()
     {
         mNumberView.setVisibility(View.GONE);
     }
 }
