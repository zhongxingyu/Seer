 package com.aethermade.shoplist;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 public class Shoplist extends Activity {
 	
	final String tag = this.getPackageName()+this.getLocalClassName();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     	Log.i(tag, "onCreate");
     }
     
     public void onResume(){
 
     	Log.i(tag, "onResume");
     }
     
     public void onPause(){
 
     	Log.i(tag, "onPause");
     }
     
     public void onStop(){
 
     	Log.i(tag, "onStop");
     }
     
     public void onDestroy(){
     	Log.i(tag, "onDestroy");
     }
 }
