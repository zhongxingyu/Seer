 package com.example.zonedhobbitsportfolio;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.ListView;
 
 public class MainActivity extends Activity {
 	
 	ListView test;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         test = (ListView) findViewById(R.id.listView1);
         
         Person[] arraypersons = new Person[3];
         
         // Just for test purposes
         
         arraypersons[0] = new Person("Albin", null, null, null, null, null, null, null);
         arraypersons[1] = new Person("Fredrik", null, null, null, null, null, null, null);
         arraypersons[1] = new Person("Martin", null, null, null, null, null, null, null);
         
         CustomAdapter test1 = new CustomAdapter(this, test.getId(), arraypersons);
         
         test.setAdapter(test1);
         
        
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
