 package net.solajpafistoj.tag.client;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class SplashActivity extends Activity{
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
        
  	
         setContentView(R.layout.activity_splash);
 	
         // Creates a thread that lets the splash run for 5 seconds
 	    Thread logoTimer = new Thread(){
 	    	public void run(){
 	    		try{
 	    			int logoTimer = 0;
 	    			while( logoTimer < 5000 ){
 	    				sleep(100);
 	    				logoTimer = logoTimer + 100;
 	    			}
	    			startActivity( new Intent ("net.solajpafistoj.tag.client.CLEARSCREEN"));
 	    		} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	    		
 	    		finally{
 	    			finish();
 	    		}
 	    	}
 	    	
 	    };
 	    
 	    logoTimer.start();
 	}
 }
 
