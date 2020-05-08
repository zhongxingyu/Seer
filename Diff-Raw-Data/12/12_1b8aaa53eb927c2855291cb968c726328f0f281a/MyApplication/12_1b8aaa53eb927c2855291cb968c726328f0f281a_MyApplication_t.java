 package uk.co.mpkdashx.CBTT;
 
 import com.bugsense.trace.BugSenseHandler;
 
 import android.app.Application;
 
 public class MyApplication extends Application {
 
 	private static MyApplication app;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
		BugSenseHandler.setup(this, "d71aad6d");
 
 	}
 
 	public static MyApplication getApp() {
 		return app;
 	}
 
 }
