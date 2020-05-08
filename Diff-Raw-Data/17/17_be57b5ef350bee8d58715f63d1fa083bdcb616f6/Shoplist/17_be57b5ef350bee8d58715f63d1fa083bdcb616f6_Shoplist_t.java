 package com.aethermade.shoplist;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 public class Shoplist extends Activity {
 	
 	final String tag = "com.aethermade.shoplist.Shoplist";
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     	Log.i(tag, "onCreate");
     }
     
     public void onResume(){
    	super.onResume();
     	Log.i(tag, "onResume");
     }
     
     public void onPause(){
    	super.onPause();
     	Log.i(tag, "onPause");
     }
     
     public void onStop(){
    	super.onStop();
     	Log.i(tag, "onStop");
     }
     
     public void onDestroy(){
    	super.onDestroy();
     	Log.i(tag, "onDestroy");
     }
 }
