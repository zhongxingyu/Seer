 package com.android.apptime;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import com.android.apptime.database.ItemDBAdapter;
 import com.android.apptime.database.MainDBAdapter;
 
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
     }
 }
