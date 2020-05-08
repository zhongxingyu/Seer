 package com.example.shopping_list;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
     public static String mt = "main_tag";
     public static int LISTS_REQUEST = 10;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         final Button lists_button = (Button) findViewById(R.id.lists_button);
        Log.d(mt, "First Screen");
         lists_button.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
                 // start a new intent
                 Intent i = new Intent(getApplicationContext(), ListsActivity.class);
                 startActivityForResult(i, LISTS_REQUEST);
         }
         });
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == Activity.RESULT_OK) {
                 Log.d(mt, "Returned successfully from Lists");
         }
     }
 }
