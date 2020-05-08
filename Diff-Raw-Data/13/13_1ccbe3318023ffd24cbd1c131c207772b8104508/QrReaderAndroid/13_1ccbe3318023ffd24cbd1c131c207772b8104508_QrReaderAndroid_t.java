 package com.qrreader.android;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class QrReaderAndroid extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         TextView  tv = new TextView(this);
        if (testMe() == 12) {
         	tv.setText( "YES" );
         } else {
         	tv.setText( "SHIT" );
        }
         setContentView(tv);
     }
     
     public native int testMe();
 
    static {
        System.loadLibrary("JNIBarcodesLibrary");
    }
 }
