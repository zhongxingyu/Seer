 	//sg
    /*
    "Copyright 2011 Aman Madaan <madaan.amanmadaan@gmail.com>"
    
     This file is part of Tarang.

    Tarang is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Tarang is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Tarang.  If not, see <http://www.gnu.org/licenses/>.*/
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
