 /*
  * Licensed under the MIT license
  * http://htmlpreview.github.com/?https://github.com/tapfortap/Documentation/blob/master/License.html
  * Copyright (c) 2013 Tap for Tap
  */
 
 package com.tapfortap.ti;
 
 import org.appcelerator.kroll.KrollProxy;
 import org.appcelerator.kroll.annotations.Kroll;
 
 import android.location.Location;
 
 import com.tapfortap.TapForTap;
 import com.tapfortap.TapForTap.Gender;
 
 @Kroll.proxy(creatableInModule = TapForTapTitaniumModule.class)
 public class TapForTapProxy extends KrollProxy {
 
 	@Kroll.method
 	public void initializeWithApiKey(String apiKey) {
 		TapForTap.plugin = "titanium";
        TapForTap.pluginVersion = "1.1.0";
 		TapForTap.initialize(getActivity(), apiKey);
 	}
 
 	@Kroll.method
 	public void setYearOfBirth(int yearOfBirth) {
 		TapForTap.setYearOfBirth(yearOfBirth);
 	}
 
 	@Kroll.method
 	public void setGender(int gender) {
 		switch (gender) {
 		case 0:
 			TapForTap.setGender(Gender.MALE);
 		case 1:
 			TapForTap.setGender(Gender.FEMALE);
 		default:
 			TapForTap.setGender(Gender.NONE);
 		}
 	}
 
 	@Kroll.method
 	public void setLocation(double latitude, double longitude) {
 		Location location = new Location("Tap for Tap");
 		location.setLatitude(latitude);
 		location.setLongitude(longitude);
 		TapForTap.setLocation(location);
 	}
 
 	@Kroll.method
 	public void setUserAccountId(String accountId) {
 		TapForTap.setUserAccountId(accountId);
 	}
 	
 	@Kroll.method
 	public void setEnvironment(String environment) {
 		TapForTap.environment = environment;
 	}
 }
