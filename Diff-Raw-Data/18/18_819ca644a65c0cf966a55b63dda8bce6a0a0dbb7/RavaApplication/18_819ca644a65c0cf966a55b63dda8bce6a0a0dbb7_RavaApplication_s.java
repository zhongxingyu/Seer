 package com.rava.voting;
 
 import com.facebook.SessionDefaultAudience;
 import com.sromku.simple.fb.Permission;
 import com.sromku.simple.fb.SimpleFacebook;
 import com.sromku.simple.fb.SimpleFacebookConfiguration;
 import com.sromku.simple.fb.utils.Logger;
 
 import android.app.Application;
 
 public class RavaApplication extends Application {
 	private static final String APP_ID = "254740274727830";
 	private static final String APP_NAMESPACE = "ravavoting";
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		// set log to true
 		Logger.DEBUG_WITH_STACKTRACE = true;
 
 		// initialize facebook configuration
 		Permission[] permissions = new Permission[] { Permission.BASIC_INFO,
				Permission.USER_GROUPS, Permission.PUBLISH_STREAM };
 
 		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
 				.setAppId(APP_ID).setNamespace(APP_NAMESPACE)
 				.setPermissions(permissions)
 				.setDefaultAudience(SessionDefaultAudience.FRIENDS)
 				.setAskForAllPermissionsAtOnce(false).build();
 
 		SimpleFacebook.setConfiguration(configuration);
 	}
 
 }
