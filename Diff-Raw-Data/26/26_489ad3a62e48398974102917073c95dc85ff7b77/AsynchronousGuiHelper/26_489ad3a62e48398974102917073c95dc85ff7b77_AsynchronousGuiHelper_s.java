 package com.bryanestrada.androidutils;
 
 import android.content.Context;
 import android.os.Handler;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.TextView;
 
 import com.bryanestrada.waitforit.R;
 
 public final class AsynchronousGuiHelper
 {
     private static AsynchronousGuiHelper Instance = null;
     public static final AsynchronousGuiHelper getInstance()
     {
        if (null == Instance)
            Instance = new AsynchronousGuiHelper();
        
         return Instance;
     }
    
     
     private Handler _guiThread;
     
    private AsynchronousGuiHelper()
     {
        _guiThread = new Handler();
     }
     
     
     public void guiSetVisibility(final Context context, final View view, final int visibility)
     {
        _guiThread.post(new Runnable()
        {
           @Override
           public void run()
           {
              view.setVisibility(visibility);
              if (View.VISIBLE == visibility)
              {
                 // if this is an "appear" visibility shift, then do a fade-in
                 Animation appear = AnimationUtils.loadAnimation(context, R.anim.appear);
                 view.startAnimation(appear);
              }
              else if (View.GONE == visibility || View.INVISIBLE == visibility)
              {
                 Animation disappear = AnimationUtils.loadAnimation(context, R.anim.disappear);
                 view.startAnimation(disappear);
              }
           }
        });
     }
     
     public void guiSetText(final Context context, final TextView view, final String text, final int style)
     {
        _guiThread.post(new Runnable()
        {
           @Override
           public void run()
           {
              view.setTextAppearance(context, style);
              view.setText(text);
           }
        });
     }
 }
