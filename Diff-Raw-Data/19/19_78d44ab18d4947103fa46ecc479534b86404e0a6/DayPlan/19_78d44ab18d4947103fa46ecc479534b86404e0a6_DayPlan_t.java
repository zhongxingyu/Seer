 /**
  * 
  */
 package com.kangaroo.gui;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.TextView;
 
import com.android.kangaroo.R;
import com.kangaroo.calendar.CalendarEvent;
import com.kangaroo.calendar.CalendarLibrary;

 /**
  * @author mrtazz
  *
  */
 public class DayPlan extends ListActivity {
 
 	  private ArrayList<CalendarEvent> eventlist = null;
 	  private CalendarAdapter calendarAdapter;
 	
 	  @Override
 	  public void onCreate(Bundle savedInstanceState) 
 	  {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.dayplan);
 	        CalendarLibrary cl = new CalendarLibrary(this);
 	        TextView tv = (TextView)findViewById(R.id.DayTitle);
 	        tv.setText("Today");
 	        eventlist = new ArrayList<CalendarEvent>(cl.getTodaysEvents("1").values());
 		    // Bind the ListView to an ArrayList of strings.
 	        calendarAdapter = new CalendarAdapter(this, R.layout.row, eventlist);
 	        calendarAdapter.notifyDataSetChanged();
	        int size = eventlist.size();
	        for (int i=0; i < size;i++)
 	        {
 	        	calendarAdapter.add(eventlist.get(i));
 	        }
 	        calendarAdapter.notifyDataSetChanged();
 	  } 
 
 }
