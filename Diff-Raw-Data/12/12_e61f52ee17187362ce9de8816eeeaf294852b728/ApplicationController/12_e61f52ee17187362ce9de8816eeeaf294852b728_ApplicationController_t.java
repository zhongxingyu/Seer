 package at.roadrunner.android;
 
 import android.app.Application;
 
 public class ApplicationController extends Application {
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		// start the services
 		//new ServiceController().startAllServices(this);
 	}
 }
