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
		String text = pref.getText();
		
		if(text == null || text.length() == 0)  {
 			text = "Not yet";
 		}
 		else {
			long timeAgo = Long.valueOf(text);
 			text = TimeAgo.toFriendlyString(timeAgo);
 		}
 		
 		pref.setSummary(text);
 		pref.setEnabled(false);
 	}
 }
