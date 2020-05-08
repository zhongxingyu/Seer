 package com.pineapple.valentine;
 
 import android.app.Application;
import com.scoreloop.client.android.core.model.*;
 
 public class BaseApplication extends Application{
 
 	@Override
 	public void onCreate(){
		super.onCreate();
 		// initialize the client using the context and game secret
 		Client.init(this, "rytSbvqboE/CABKJRZypE2HrATHBqPBUJQCox6UPpxKJIRGpQhtu1Q==", null);
		
 	}
 	
 }
