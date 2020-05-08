 package com.anderspersson.xbmcwidget.configuration;
 
 import android.preference.EditTextPreference;
 
 import com.anderspersson.xbmcwidget.common.TimeAgo;
 
 public class RecentTvRefreshPrefUpdater {
 
 	private EditTextPreference pref;
 
 	public RecentTvRefreshPrefUpdater(EditTextPreference pref) {
 		this.pref = pref;
 		setValues();
 	}
 
 	private void setValues() {
		String timeAgoStr = pref.getSharedPreferences().getString("recenttv_last_refresh", "0");
		String text = "";
		if(timeAgoStr == null || timeAgoStr.length() == 0)  {
 			text = "Not yet";
 		}
 		else {
			long timeAgo = Long.valueOf(timeAgoStr);
 			text = TimeAgo.toFriendlyString(timeAgo);
 		}
 		
 		pref.setSummary(text);
 		pref.setEnabled(false);
 	}
 }
