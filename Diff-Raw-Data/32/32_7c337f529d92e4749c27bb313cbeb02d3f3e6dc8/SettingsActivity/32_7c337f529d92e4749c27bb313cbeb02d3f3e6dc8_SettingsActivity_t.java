 package com.blklb.mpdhd;
 
import android.app.Activity;
 import android.os.Bundle;
 
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
 
import com.blklb.mpdhd.tasks.NetworkAndUITask;
import com.blklb.mpdhd.tools.TimerHelper;
 
 public class SettingsActivity extends PreferenceActivity {

	private String tag = "SettingsActivity";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.prefrences);
	}

	// @Override
	public void selfDestruct(View view) {
		final Activity a = this.getParent();

		// Reset the connection from start
		//TimerHelper.getInstance().scheduleTask(new NetworkAndUITask(a), 100);
		Log.w(tag, "ConnectionReset");

 	}

 }
