 package org.bxmy.shiftclock;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.GridView;
 import android.widget.SimpleAdapter;
 
 public class DutyListActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.duty_list);
        
         GridView grid = (GridView) findViewById(R.id.grid_duty_list);
        SimpleAdapter adapter = new SimpleAdapter(this, addTestDuties(), R.layout.duty_list_item, new String[] { "1", "2", "3"}, new int[] {R.id.text_dutyName, R.id.time_dutyStart, R.id.time_dutyEnd});
         grid.setAdapter(adapter);
     }
    
     private ArrayList<HashMap<String, Object>> addTestDuties() {
         ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
         HashMap<String, Object> map = new HashMap<String, Object>();
         map.put("1", "白班");
         map.put("2", new Date(0, 0, 0, 9, 30, 0));
         map.put("3", new Date(0, 0, 0, 18, 30, 0));
         items.add(map);
         return items;
     }
 }
