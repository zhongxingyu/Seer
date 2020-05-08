 package com.TimeStat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MainActivity extends Activity {
     private EventsAccess eventData;
 
     // ArrayList of strings for the events, get it from the database later
     List<String> events = new ArrayList<String>();
 
     // How we will display the event names on the ListView
     private ArrayAdapter<String> adapter;
 
     private ListView listview;
 
 
     // Counter, delete when we are actually connecting to the database
     private int count = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, events);
 
         listview = (ListView) findViewById(R.id.listview);
 
         listview.setAdapter(adapter);
 
         listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Context context = getApplicationContext();
                 CharSequence text = "You pressed list position number: " + position;
                 int duration = Toast.LENGTH_SHORT;
 
                 Toast toast = Toast.makeText(context, text, duration);
                 toast.show();
             }
         });
 
         eventData = new EventsAccess(this);
 
         eventData.open();
         eventData.close();
     }
 
     /* There might be a better way to do this but this allows the ListView
      * to stay when we rotate the screen.
      */
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
 
         listview.setAdapter(adapter);
     }
 
     /* Gets called when the user presses the add more activities button */
     public void onClick(View view) {
 
         // Have the user enter the name of the event in an AlertDialog
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
         alert.setTitle("Add a new activity");
 
         // Set an EditText view to get user input
         final EditText input = new EditText(this);
         alert.setView(input);
 
         alert.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 String value = input.getText().toString();
 
                 // Add the text to the Listview
                 adapter.add(value);
                 listview.setAdapter(adapter);
             }
         });
 
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 // Do nothing
             }
         });
 
        alert.show();                                    */
     }
 }
