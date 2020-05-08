 package com.sgs.hotelguru;
 
 import android.app.Activity;
import android.content.SharedPreferences;
 
 public class firstUseSetup extends Activity{
 	
 	public static final String PREFS_NAME = "HotelGuru_Prefs_File";
	public void firstuse(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
		
		if(settings.getBoolean("firstBoot", true))
				{
			//FIRST BOOT DETECTED, IMPORT DATABASE AND QUERY FOR LOGIN INFORMATION
				}
		else
		{
			//NORMAL MODE CONTINUE TO MAIN ACTIVITY
		}
	}
 	
		
}
