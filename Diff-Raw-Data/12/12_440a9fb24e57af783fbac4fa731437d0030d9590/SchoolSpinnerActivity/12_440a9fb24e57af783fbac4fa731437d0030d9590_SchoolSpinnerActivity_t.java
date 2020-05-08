 package edu.upenn.cis350.project;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
 
 public class SchoolSpinnerActivity extends Activity implements OnItemSelectedListener{
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_start);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_start, menu);
         return true;
     }
 
     public void onItemSelected(AdapterView<?> parent, View view, 
             int pos, long id) {
         // An item was selected. You can retrieve the selected item using
         // parent.getItemAtPosition(pos)
     }
 
     public void onNothingSelected(AdapterView<?> parent) {
         // Another interface callback
     }
     
    //code to deal with the spinner
    Spinner spinner = (Spinner) findViewById(R.id.school);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.school_list, android.R.layout.simple_spinner_item);
    
    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    //spinner.setAdapter(adapter);
    
 }
