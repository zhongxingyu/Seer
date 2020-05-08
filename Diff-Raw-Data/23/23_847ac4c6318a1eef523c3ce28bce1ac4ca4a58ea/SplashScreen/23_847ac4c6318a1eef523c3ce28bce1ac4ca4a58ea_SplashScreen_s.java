 	//sg
 package com.bvp.miniproject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ImageView;
 
 public class SplashScreen extends Activity {
 	Long delay=3000l;
 	@Override
 	protected void onCreate(Bundle savedInstanceState)  {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splashscreen);
 		
 	ImageView iv=(ImageView)findViewById(R.id.imageViewSplash);
 	
 	iv.setImageResource(R.drawable.splash);
 		Thread splashTread = new Thread() {
 	        @Override
 	        public void run() {
 	            try {
 	                sleep(delay);
 	                }
 	             catch(InterruptedException e) {
 	                // do nothing
 	            	 }
 	             finally {
 	                finish();
 	               
 	            }
 	        }
 	    };
 	    splashTread.start();
 	    /*The following won't work as it will block current thread
         try {
             Thread t=Thread.currentThread();
         	Thread.sleep(3000);
             }
          catch(InterruptedException e) {
             // do nothing
         	 }
          finally {
             finish();
            
         }
 */
 	}
 }
