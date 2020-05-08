 /**
  * Flurry is Copyright(c) 2012 by Flurry, Inc.
  *
  * Usage of this module is subject to the Terms of Service agreement of Flurry, Inc.
  *
  * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
  * and licensed under the Apache Public License (version 2)
  *
  * This module is Copyright (c) 2012 Sofi Software LLC
  * and licensed under the Apache Public License (version 2)
  * http://www.apache.org/licenses/LICENSE-2.0.html
  */
 package com.sofisoftwarellc.tiflurry;
 
import java.util.HashMap;

 import org.appcelerator.kroll.KrollModule;
 import org.appcelerator.kroll.annotations.Kroll;
 import org.appcelerator.kroll.common.Log;
 import org.appcelerator.kroll.common.TiConfig;
 import org.appcelerator.titanium.TiApplication;
 import org.appcelerator.titanium.TiContext;
 
 import com.flurry.android.Constants;
 import com.flurry.android.FlurryAgent;
 
 /**
  * This module is an implementation of the Flurry Android API for use with Titanium.
  * It also includes methods for compatibility with the iOS version of the module. This
  * means you can build one Ti project with the same JS source.
  *
  * Some Flurry iOS methods are not implemented in Flurry Android, so they
  * do nothing here. These are:
  * 		setSessionReportsOnCloseEnabled
  * 		setSessionReportsOnPauseEnabled
  * 		setLatitude (should use setReportLocation, currently unimplemented here)
  *
  * Some Flurry Android API functions are not implemented:
  * 		appCircle
  * 		setVersionName
  * 		getAgentVersion
  * 		setReportLocation
  * 		setLogLevel
  *
  * @author Eric Herrmann, Sofi Software LLC
  */
 
 @Kroll.module(name="Tiflurry", id="com.sofisoftwarellc.tiflurry")
 public class TiflurryModule extends KrollModule
 {
 
 	// Standard Debugging variables
 	private static final String LCAT = "TiflurryModule";
 	private static final boolean DBG = TiConfig.LOGD;
 
 	@Kroll.constant public static final byte MALE = Constants.MALE;
   	@Kroll.constant public static final byte FEMALE = Constants.FEMALE;
 
 	public TiflurryModule()
 	{
 		super();
 	}
 
 	@Kroll.onAppCreate
 	public static void onAppCreate(TiApplication app)
 	{
 		Log.d(LCAT, "inside onAppCreate");
 		// put module init code that needs to run when the application is created
 	}
 
 	// Methods
 
 	@Kroll.method
 	public void onStartSession(TiContext context, String apiKey) {
 		FlurryAgent.onStartSession(context.getAndroidContext().getBaseContext(), apiKey);
 	}
 
 	@Kroll.method
 	public void onEndSession(TiContext context) {
 		FlurryAgent.onEndSession(context.getAndroidContext().getBaseContext());
 	}
 
 	@Kroll.method
 	public void setContinueSessionMillis(long milliseconds) {
 		FlurryAgent.setContinueSessionMillis(milliseconds);
 	}
 
 	@Kroll.method
 	public void logEvent(String eventId, @Kroll.argument(optional=true) Object arg2, @Kroll.argument(optional=true) Object arg3) {
 		if (arg2 == null)
 			FlurryAgent.logEvent(eventId);
 		else if (arg3 == null) {
 			if (arg2 instanceof Boolean)
 				FlurryAgent.logEvent(eventId, (Boolean) arg2);
			else if (arg2 instanceof HashMap)
				FlurryAgent.logEvent(eventId, (HashMap) arg2);
 		}
 		else
			FlurryAgent.logEvent(eventId, (HashMap) arg2, (Boolean) arg3);
 	}
 
 	@Kroll.method
 	public void onError(String errorId, String message, String errorClass) {
 		FlurryAgent.onError(errorId, message, errorClass);
 	}
 
 	@Kroll.method
 	public void setUserId(String userId) {
 		FlurryAgent.setUserId(userId);
 	}
 
 	@Kroll.method
 	public void setAge(int age) {
 		FlurryAgent.setAge(age);
 	}
 
 	@Kroll.method
 	public void setGender(int gender) {
 		byte genderByte;
 
 		if (gender == 0)
 			genderByte = Constants.FEMALE;
 		else
 			genderByte = Constants.MALE;
 		FlurryAgent.setGender(genderByte);
 	}
 
 	@Kroll.method
 	public void onPageView() {
 		FlurryAgent.onPageView();
 	}
 
 	@Kroll.method
 	public void setLogEnabled(boolean enabled) {
 		FlurryAgent.setLogEnabled(enabled);
 	}
 
 	@Kroll.method
 	public void setUseHttps(boolean useHttps) {
 		FlurryAgent.setUseHttps(useHttps);
 	}
 
 	@Kroll.method
 	public void setReportLocation(boolean report) {
 		FlurryAgent.setReportLocation(report);
 	}
 
 	// The following methods enable compatibility with iOS API
 
 	@Kroll.method
 	public void startSession(String apiKey) {
 		FlurryAgent.onStartSession(TiApplication.getAppRootOrCurrentActivity(), apiKey);
 	}
 
 	@Kroll.method
 	public void logUncaughtExceptions(boolean b) {
 		FlurryAgent.setCaptureUncaughtExceptions(b);
 	}
 
 
 	@Kroll.method
 	public void setSessionReportsOnCloseEnabled(boolean b) {
 		Log.w(LCAT, "setSessionReportsOnCloseEnabled ignored on Android");
 	}
 
 	@Kroll.method
 	public void setSessionReportsOnPauseEnabled(boolean b) {
 		Log.w(LCAT, "setSessionReportsOnPauseEnabled ignored on Android");
 	}
 
 	@Kroll.method
 	public void setSecureTransportEnabled(boolean useHttps) {
 		FlurryAgent.setUseHttps(useHttps);
 	}
 
 	@Kroll.method
 	public void endTimedEvent(String eventId) {
 		FlurryAgent.endTimedEvent(eventId);
 	}
 
 	@Kroll.method
 	public void logPageView() {
 		FlurryAgent.onPageView();
 	}
 
 	@Kroll.method
 	public void logError(String errorId, String message, String errorClass) {
 		FlurryAgent.onError(errorId, message, errorClass);
 	}
 
 	@Kroll.method
 	public void setGender(String gender) {
 		byte genderByte;
 
 		if (gender == "f")
 			genderByte = Constants.FEMALE;
 		else
 			genderByte = Constants.MALE;
 		FlurryAgent.setGender(genderByte);
 	}
 
 	@Kroll.method
 	public void setLatitude(float lat, float lng, float h, float v) {
 		Log.w(LCAT, "setLatitude ignored on Android");
 	}
 }
 
