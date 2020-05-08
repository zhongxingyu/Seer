 /*
  * Copyright [2012] [Simon Fransson]
 
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
import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.content.Intent;
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
     private Thread threadForSplash;
    private boolean isBackPressed = false;
 
 	/**
 	 * onCreate method that assign the instance variables and create an anonymous Thread which is used as a timer for the splash screen.
 	 */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_startup);
         assignInstances();
         //Use a thread for the splash screen to assign its lifetime.
         threadForSplash = new Thread(){
 
 			public void run (){
             	try{
             		synchronized(this){
             		wait(5000);
             		}
             	}
             	catch(InterruptedException e){
             		e.printStackTrace();
             	}
             	finish();
            	if(!isBackPressed){
         		startActivity(startMenuActivity);
            	}
             }
           };
        
         threadForSplash.start();
        

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
 	protected void onPause() {
 		super.onPause();
 		finish();
 	}
/**
 * If back button is clicked, then finish the activity
 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            isBackPressed = true;
            finish();
        }
        return super.onKeyDown(keyCode, event);

    }
 	
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(event.getAction()==MotionEvent.ACTION_DOWN){
 			synchronized(threadForSplash){
 				threadForSplash.notifyAll();
 			}
 			return true;
 		}
 		return false;
 	}    
 }
