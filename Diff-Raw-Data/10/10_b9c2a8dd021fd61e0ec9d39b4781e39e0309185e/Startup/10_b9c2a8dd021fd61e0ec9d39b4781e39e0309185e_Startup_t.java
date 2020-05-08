 /*
  * Copyright [2012] []
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
    */
 
 package com.chalmers.schmaps;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 
 /**
  * A Startup activity for showing a splash screen, uses a thread to determine the length of the splash screen.
  * @author Frolle
  * @version 1.1
  */
 public class Startup extends Activity {
 
     private ImageView myView;
 	private Animation fadeInAnimation;
     private Intent startMenuActivity;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_startup);
         
         //Use a thread for the splash screen to assign its lifetime.
         assignInstances();
         Thread timer = new Thread(){
 
 			public void run (){
             	try{
             		sleep(5000);
             		startActivity(startMenuActivity);
             	}
             	catch(InterruptedException e){
             		e.printStackTrace();
             	}
             }
           };
 
         timer.start();
     }
 	/**
 	 * Method to assign the instance variables.
 	 */
     private void assignInstances() {
                myView = (ImageView) findViewById(R.id.imageView1);
                fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.appear);
                myView.startAnimation(fadeInAnimation);
               startMenuActivity = new Intent("android.intent.action.MENUACTIVITY");
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_startup, menu);
         return true;
     }
     
 	@Override
 	protected void onPause() {
 		super.onPause();
 		finish();
 	}
 	
 	public Intent getIntentForMenuActivity(){
 		return startMenuActivity;
 	}
     
 }
