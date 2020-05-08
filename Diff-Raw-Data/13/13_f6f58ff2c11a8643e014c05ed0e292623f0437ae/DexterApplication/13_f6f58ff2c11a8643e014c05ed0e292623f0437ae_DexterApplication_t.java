 package uk.ac.cam.db538.dexter.aux;
 
 import android.app.Application;
 import android.content.Context;
 
public class DexterApplication extends Application {
 
 	public static Context DexterContext;
 	
 	public DexterApplication() {
 		super();
 		DexterContext = this;
 	}
 	
 }
