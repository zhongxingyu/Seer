 package com.synaptian.smoketracker.habits;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.app.ListActivity;
 import android.os.Bundle;
 
 public class HabitTimeListActivity extends ListActivity {
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.event_list);
 
         List habitTimes = new ArrayList();
        habitTimes.add(new HabitTime("MSFT", 24));
        habitTimes.add(new HabitTime("ORCL", 34));
        habitTimes.add(new HabitTime("AMZN", 180));
        habitTimes.add(new HabitTime("ERTS", 19));
 
         setListAdapter(new HabitTimeAdapter(this, habitTimes));
     }
 }
