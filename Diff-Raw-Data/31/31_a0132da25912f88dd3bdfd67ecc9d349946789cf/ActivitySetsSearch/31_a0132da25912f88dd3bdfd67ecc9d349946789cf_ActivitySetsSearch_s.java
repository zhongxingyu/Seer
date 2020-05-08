 package com.lollapaloozer.ui;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.lollapaloozer.R;
 import com.lollapaloozer.util.LollapaloozerHelper;
 
 public class ActivitySetsSearch extends Activity implements OnClickListener {
 
   private String _daySelected;
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     LollapaloozerHelper.debug(this, "Search Sets Activity Launched");
     this.initializeApp();
   }
 
   public void initializeApp() {
     setContentView(R.layout.sets_search);
 
     // Setup day spinner
     Spinner daySpinner = (Spinner) this.findViewById(R.id.search_spinner_day);
     LollapaloozerHelper.populateSpinnerWithArray(daySpinner, R.array.names_day);
     daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
       @Override
       public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         LollapaloozerHelper.debug(parent.getContext(), "Day Selected: " + parent.getSelectedItem());
         _daySelected = parent.getSelectedItem().toString();
 
       }
 
       @Override
       public void onNothingSelected(AdapterView<?> parent) {
         LollapaloozerHelper.debug(parent.getContext(), "No Day Selected - Unexpected condition");
       }
     });
 
     Calendar cal = Calendar.getInstance();
     int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
     if (dayOfWeek == Calendar.FRIDAY) {
       daySpinner.setSelection(0);
     } else if (dayOfWeek == Calendar.SATURDAY) {
       daySpinner.setSelection(1);
     } else if (dayOfWeek == Calendar.SUNDAY) {
       daySpinner.setSelection(2);
     } else {
       // Select Friday
       daySpinner.setSelection(0);
     }
 
     // Register "this" as the onClick listener for search button
     Button searchButton = (Button) this.findViewById(R.id.sets_search_button_search);
     searchButton.setOnClickListener(this);
   }
 
   /**
    * Fires when the Search ("Go!") button is pressed, this object is the OnClick
    * listener.
    * 
    * @param v
    */
 
   public void onClick(View v) {
     if (v.getId() == R.id.sets_search_button_search) {
       LollapaloozerHelper.debug(this, "Search button pressed");
 
       try {
         Intent intent = new Intent();
         Bundle bun = new Bundle();
 
         bun.putString("DAY", _daySelected);
 
         intent.setClass(this, LollapaloozerActivity.class);
 
         intent.putExtras(bun);
         startActivity(intent);
       } catch (Exception e) {
         e.printStackTrace();
         Toast exceptionText = Toast.makeText(this, "Exception Launching Activity", 5);
         exceptionText.show();
       }
     } else {
       LollapaloozerHelper.debug(this, "An unknown button was pressed - Unexpected");
     }
 
   }
 
 }
