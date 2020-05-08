 package com.crankcode.application;
 
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

 import android.app.Application;
 
@ReportsCrashes(formKey = "dENJRGczTmdGdy1SbXVnV2pGNXNWdWc6MQ")
 public class JustPlay extends Application {
 
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
	}
 }
