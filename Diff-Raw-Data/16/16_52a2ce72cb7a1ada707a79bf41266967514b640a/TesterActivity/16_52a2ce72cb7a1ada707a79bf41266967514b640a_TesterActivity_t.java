package com.aethermade.shoplist;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class TesterActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         try {
 			super.clearWallpaper();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 }
