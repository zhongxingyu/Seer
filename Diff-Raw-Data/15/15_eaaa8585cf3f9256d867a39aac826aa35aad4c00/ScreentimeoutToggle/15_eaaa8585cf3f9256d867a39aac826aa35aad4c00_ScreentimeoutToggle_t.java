  
 package com.android.systemui.statusbar.toggles;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.provider.Settings.System;
 import android.view.View;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import com.android.systemui.R;
 import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
 import android.app.Activity;
 
 public class ScreentimeoutToggle extends Toggle {
 
         private int sc;
         private boolean tripped = false;
 	    private int timeout;
 	    private int value;
 	
      public ScreentimeoutToggle(Context context) {
         super(context);
         setLabel(R.string.toggle_screentimeout);
 	    tripped = false;
         loadToggles();
         }
  
      private void setScreenTimeout(int sc) {
 	 
         ContentResolver resolver = mContext.getContentResolver();
         Settings.System.putInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT, sc);
         }
 
      public int getScreenTimeout() {
 		ContentResolver cr = mContext.getContentResolver();
         return timeout = android.provider.Settings.System.getInt(cr, SCREEN_OFF_TIMEOUT, 0);
         }
         
      @Override
      protected boolean updateInternalToggleState() {
         
 	if (tripped == true) { 	 
 	    int ScreenTimeout = getScreenTimeout();
 		if (ScreenTimeout == -1) {
             setIcon(R.drawable.toggle_30s);
 				 value = 30000; 
         } else if (ScreenTimeout == 30000) {
             setIcon(R.drawable.toggle_1m);
 			     value = 60000;
         } else if (ScreenTimeout == 60000) {
             setIcon(R.drawable.toggle_2m);
 				 value = 120000;
         } else if (ScreenTimeout == 120000) {
            setIcon(R.drawable.toggle_5m);
 				 value = 300000;
 		} else if (ScreenTimeout == 300000) {
             setIcon(R.drawable.toggle_10m);
 				 value = 600000;
         } else if (ScreenTimeout == 600000) {
             setIcon(R.drawable.toggle_on);
 				 value = -1;
         } else {
             setIcon(R.drawable.toggle_1m);
 			     value = 60000;
         }
 		
         
 //				 	int sc = value;
                  setScreenTimeout(value);
 				 
        } else {
	        	 tripped = true;
 	}
     
 
   
        return tripped;
 			
    }
 	
   
   
   private void loadToggles() {
         int timeoutToggle = getScreenTimeout();
 		mToggle.setChecked(true);
 		if (timeoutToggle == -1) {
             setIcon(R.drawable.toggle_on);
         } else if (timeoutToggle == 30000) {
             setIcon(R.drawable.toggle_30s);
         } else if (timeoutToggle == 60000) {
             setIcon(R.drawable.toggle_1m);
         } else if (timeoutToggle == 120000) {
             setIcon(R.drawable.toggle_2m);
 		} else if (timeoutToggle == 300000) {
             setIcon(R.drawable.toggle_5m);
         } else if (timeoutToggle == 600000) {
             setIcon(R.drawable.toggle_10m);
         } else {
             setIcon(R.drawable.toggle_settings);
         }
 		
 
   tripped = false;
   
 		
   }
   
   @Override
   protected void onCheckChanged(boolean isChecked) {
         updateState();
     }
 	
     @Override
     protected boolean onLongPress() {
         if (mContext != null) {
             Intent intent = new Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             mContext.startActivity(intent);
         }
         return true;
     }
 }
