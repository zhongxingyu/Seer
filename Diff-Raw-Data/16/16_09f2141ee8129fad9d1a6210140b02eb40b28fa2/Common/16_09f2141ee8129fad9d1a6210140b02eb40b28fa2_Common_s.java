 package com.pilot51.lclock;
 
 import android.app.Activity;
 import android.util.Log;
 import android.view.WindowManager.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 import com.adwhirl.AdWhirlLayout;
 
 public class Common {
 
 	void ad(Activity a) {
 		LinearLayout layout = (LinearLayout) a.findViewById(R.id.layoutAd);
 		// Disable ads based on preference or availability
 		if (layout == null) {
 			Log.e("AdWhirl", "Layout is null!");
 			return;
 		}
 		float density = a.getResources().getDisplayMetrics().density;
 		// int width = (int) (320 * density);
 		int height = (int) (52 * density);
 		/*
 		AdWhirlTargeting.setAge(23);
 		AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
 		AdWhirlTargeting.setKeywords("online games gaming");
 		AdWhirlTargeting.setPostalCode("94123");
 		AdWhirlTargeting.setTestMode(false);
 		AdWhirlAdapter.setGoogleAdSenseAppName("PrediSat");
 		AdWhirlAdapter.setGoogleAdSenseCompanyName("Pilot_51");
 		*/
 		// layout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
 		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		layout.addView(new AdWhirlLayout(a, "08a2a4f33a2e465eb5d6f899fcc000a8"), lp);
 		layout.invalidate();
 	}
 }
