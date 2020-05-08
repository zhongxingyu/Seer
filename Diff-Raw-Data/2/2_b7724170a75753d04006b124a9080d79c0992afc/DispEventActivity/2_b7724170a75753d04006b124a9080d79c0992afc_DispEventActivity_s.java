 package com.example.handsoncentralohio;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 
 public class DispEventActivity extends Activity {
 
 	public static ArrayList<EventData> parseArrayData(String [][] arrayData) {
 		ArrayList <EventData> list = new ArrayList <EventData>();
 		for(int i = 0; i < arrayData.length; i++) {
 			EventData ed = new EventData();
 			ed.setAll(Integer.parseInt(arrayData[i][0]), arrayData[i][2], arrayData[i][1],
 					arrayData[i][3]);
 			list.add(ed);
 		}
 		return list;
 		
 	}
 
     public static ArrayList<EventData> searchData(String keyword, ArrayList<EventData> events){
         ArrayList<EventData> results = new ArrayList<EventData>();
         for (EventData e: events){
             if (e.getName().contains(keyword) || e.getDescr().contains(keyword)
                     || e.getStartDate().contains(keyword)){
                 results.add(e);
             }
         }
         return results;
     }
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dispevent);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		// Text View setting for table...
 		ArrayList <EventData> list = parseArrayData(RawData.data);
 		int numEvents = list.size();
 		int curElementOnPage = 0;
 		
 		TextView eventDate;
 		TextView eventName;
 		TextView eventDesc;
 		
 		for (int i = 0; i < numEvents; i++)
 		{
 			if (i == 0)
 			{
 				eventDate = (TextView) findViewById(R.id.event1date);
 				eventDate.setText(list.get(i).getStartDate().toString());
 				eventName = (TextView) findViewById(R.id.event1name);
 				eventName.setText(list.get(i).getName().toString());
 				eventDesc = (TextView) findViewById(R.id.event1date);
 				eventDesc.setText(list.get(i).getDescr().toString());
 			}
 			else if (i == 1)
 			{
 				eventDate = (TextView) findViewById(R.id.event2date);
 				eventDate.setText(list.get(i).getStartDate().toString());
 				eventName = (TextView) findViewById(R.id.event2name);
 				eventName.setText(list.get(i).getName().toString());
 				eventDesc = (TextView) findViewById(R.id.event2date);
 				eventDesc.setText(list.get(i).getDescr().toString());
 			}
 			else if (i == 2)
 			{
 				eventDate = (TextView) findViewById(R.id.event3date);
 				eventDate.setText(list.get(i).getStartDate().toString());
 				eventName = (TextView) findViewById(R.id.event3name);
 				eventName.setText(list.get(i).getName().toString());
 				eventDesc = (TextView) findViewById(R.id.event3date);
 				eventDesc.setText(list.get(i).getDescr().toString());
 			}
 			else if (i == 3)
 			{
 				eventDate = (TextView) findViewById(R.id.event4date);
 				eventDate.setText(list.get(i).getStartDate().toString());
 				eventName = (TextView) findViewById(R.id.event4name);
 				eventName.setText(list.get(i).getName().toString());
 				eventDesc = (TextView) findViewById(R.id.event4date);
 				eventDesc.setText(list.get(i).getDescr().toString());
 			}
 			else if (i == 4)
 			{
 				eventDate = (TextView) findViewById(R.id.event5date);
 				eventDate.setText(list.get(i).getStartDate().toString());
 				eventName = (TextView) findViewById(R.id.event5name);
 				eventName.setText(list.get(i).getName().toString());
 				eventDesc = (TextView) findViewById(R.id.event5date);
 				eventDesc.setText(list.get(i).getDescr().toString());
 			}
 		}
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.disp_event, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void expandEvent(View view)
 	{
		if (view.getId() == R.id.event2date)
 		{
 			Intent intent = new Intent(this, SelectedEventActivity.class);
 			startActivity(intent);
 		}
 	}
 
 }
