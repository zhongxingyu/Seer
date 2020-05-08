 package com.github.ironjan.photodrop;
 
import android.support.v4.app.*;

import com.actionbarsherlock.app.*;
import com.googlecode.androidannotations.annotations.*;
 
 @EActivity(R.layout.act_pref)
 public class PrefActivity extends SherlockFragmentActivity {
	@AfterViews
	void setHomeAsUp() {
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	@OptionsItem
	void homeSelected() {
		NavUtils.navigateUpTo(this, getParentActivityIntent());
	}
 }
