 package com.strollimo.android.util;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 import com.strollimo.android.StrollimoApplication;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.view.MainActivity;
 
 public class DebugModeController {
 
     private final View mDebugView;
     private final Context mContext;
     private final Handler mHandler;
     private Runnable mDebugModeRunnable = new Runnable() {
 
         @Override
         public void run() {
             StrollimoApplication.getService(StrollimoPreferences.class).setDebugModeOn(true);
             Toast.makeText(mContext, "You're in debug mode", Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(mContext, MainActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             mContext.startActivity(intent);
         }
     };
 
     public DebugModeController(View view, Context context) {
         mDebugView = view;
         mContext = context;
         mHandler = new Handler();
         mDebugView.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 int action = event.getAction() & MotionEvent.ACTION_MASK;
                if (event.getPointerCount() >= 3 && action == MotionEvent.ACTION_POINTER_DOWN) {
                     mHandler.postDelayed(mDebugModeRunnable, 5000);
                     return true;
                } else if (action == MotionEvent.ACTION_POINTER_UP) {
                     mHandler.removeCallbacks(mDebugModeRunnable);
                     return true;
                 }
                return true;
             }
         });
     }
 
 }
