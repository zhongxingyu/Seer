 package at.tell.todolist;
 
 import org.apache.cordova.DroidGap;
 
 import android.os.Bundle;
 import android.view.View;
 
 public class PhoneGapActivity extends DroidGap {
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.setIntegerProperty("loadUrlTimeoutValue", 10000); 
        super.loadUrl("file:///android_asset/www/index.html");
         
         // Display vertical scrollbar and hide horizontal scrollBar
         super.appView.setVerticalScrollBarEnabled(true);
         super.appView.setHorizontalScrollBarEnabled(false);
         // set scrollbar style
         super.appView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
     }
     
 }
