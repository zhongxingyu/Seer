 package com.android.demo.trial;
 
 import com.android.demo.db.DataProvider;
 import com.android.demo.db.DemoDatabase;
 import com.android.demo.util.Util;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Window;
 
 public class SplashScreen extends Activity
 {
 
     /**
      * The thread to process splash screen events
      */
     private Thread mSplashThread;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
 
         // Splash screen view
         this.requestWindowFeature( Window.FEATURE_NO_TITLE );
         setContentView( R.layout.splash );
 
         Util.setAppContext( this );
        Util.setNetworkStatus(this);
 
         MarketsTab.fetchIndicesData();
         showSplash( this, null );
     }
 
     /**
      * Processes splash screen touch events
      */
 
     private void showSplash( final Context context, final Intent intentShortCut )
     {
         // The thread to wait for splash screen events
         mSplashThread = new Thread()
         {
             @Override
             public void run()
             {
                 try
                 {
                     synchronized( this )
                     {
                         // Wait given period of time or exit on touch
                         wait( 2000 );
                     }
                 }
                 catch( InterruptedException ex )
                 {
                     ex.printStackTrace();
                 }
 
                 Intent intent = new Intent();
                 intent.setClass( context, DemoProjectActivity.class );
                 startActivity( intent );
                 finish();
             }
         };
 
         mSplashThread.start();
     }
 
     @Override
     public boolean onTouchEvent( MotionEvent evt )
     {
         if( evt.getAction() == MotionEvent.ACTION_DOWN )
         {
             synchronized( mSplashThread )
             {
                 mSplashThread.notifyAll();
             }
         }
         return true;
     }
 
 }
