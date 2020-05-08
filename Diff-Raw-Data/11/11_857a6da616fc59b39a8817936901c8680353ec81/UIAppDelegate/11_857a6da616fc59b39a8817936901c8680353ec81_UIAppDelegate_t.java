 /*
  * Copyright (C) 2012 Wu Tong
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.cocoa4android.ui;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.cocoa4android.R;
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.ns.NSString;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.WindowManager;
 
 public abstract class UIAppDelegate extends Activity implements AppDelegate{
 	protected static final boolean YES = true;
 	protected static final boolean NO = false;
 	
 	protected UIWindow window;
 	
 	protected static void NSLog(String format,Object...args){
 		Log.i("Cocoa4Android",NSString.stringWithFormat(format, args));
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		//work before onCreate
 		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
         super.onCreate(savedInstanceState);
         UIApplication.sharedApplication().setContext(this);
         UIApplication.sharedApplication().setDelegate(this);
         
         
         
         DisplayMetrics dm = new DisplayMetrics();   
         getWindowManager().getDefaultDisplay().getMetrics(dm); 
         UIScreen.mainScreen().setDisplayMetrics(dm);
         
         window = new UIWindow();
         UIApplication.sharedApplication().setWindow(window);
         this.setContentView(window.getView());
         
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
     }
 
 	@Override
 	public void onBackPressed(){
 		if(window!=null){
         	UIViewController viewController = window.rootViewController();
        	if (viewController!=null) {
        		boolean handled = viewController.onBackPressed();
            	if(!handled){
            		System.exit(0);
            	}
			}
        	
     	}
 	}
 	public void launchApplication(){
 		UIApplication.sharedApplication().setApplicationLaunched(YES);
 		final UIImageView imageView = new UIImageView();
         imageView.setImage(new UIImage(R.drawable.zz_c4a_default));
         window.addSubview(imageView);
         new Timer().schedule(new TimerTask() {
 			@Override
 			public void run() {
 				UIAppDelegate.this.runOnUiThread(new Runnable() {
 					
 					@Override
 					public void run() {
 						imageView.removeFromSuperView();
 						UIAppDelegate.this.applicationDidFinishLaunching();
 					}
 				});
 			}
 		}, 3000);
 	}
 }
