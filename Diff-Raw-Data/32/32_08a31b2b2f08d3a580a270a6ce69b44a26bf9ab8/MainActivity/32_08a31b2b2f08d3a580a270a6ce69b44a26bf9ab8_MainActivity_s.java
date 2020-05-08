 package com.mertlebeach.fungifind;
 import com.parse.Parse;
import com.parse.ParseAnalytics;
 import com.parse.ParseObject;
 
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
 
 public class MainActivity extends Activity {

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         Parse.initialize(this, "f9nuFRqE5CjHeXL5DxtKourEaDLM62sDkiYGjLzV", "JSCyGA0ovUs894LhEnqEijDCZgK2kgezoWLghk4T");
         ParseObject testObject = new ParseObject("TestObject");
         testObject.put("foo", "bar");
         testObject.saveInBackground();
    }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
        
         return true;
     }
     
 }
