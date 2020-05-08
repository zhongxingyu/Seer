 package com.bbaumgar.signalstrengthplotter;
 
 import org.acra.*;
 import org.acra.annotation.*;
 
 import android.app.Application;
 
  @ReportsCrashes(formKey = "dFF5NkRIME1lWWJqM1RxUUtFb2JuWmc6MQ") 
   public class ReporterApplication extends Application {
 	  @Override
 	  public void onCreate() {
 	      super.onCreate();
 	      // The following line triggers the initialization of ACRA
 	      ACRA.init(this);
 	  }
   }
