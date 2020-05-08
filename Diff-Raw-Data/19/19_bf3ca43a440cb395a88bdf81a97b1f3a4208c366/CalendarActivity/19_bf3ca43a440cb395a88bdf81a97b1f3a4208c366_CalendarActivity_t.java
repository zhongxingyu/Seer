 package com.ISU.shoppingsidekick;
 
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.Database.API.Account;
 import com.Database.API.CalendarItem;
 import com.Database.API.DatabaseAPI;
 
 public class CalendarActivity extends Activity {
 	public Calendar month;
 	public CalendarAdapter adapter;
 	public Handler handler;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		final Account a = (Account) getIntent().getExtras().get("account");
 		
 		super.onCreate(savedInstanceState);
 	    setContentView(R.layout.calendar);
 	    month = Calendar.getInstance();
 	    adapter = new CalendarAdapter(this, month);
 	    
 	    GridView gridview = (GridView) findViewById(R.id.gridview);
 	    gridview.setAdapter(adapter);
 	    
 	    handler = new Handler();
 	    handler.post(calendarUpdater);
 	    
 	    TextView title  = (TextView) findViewById(R.id.title);
 	    title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
 	    
 	    TextView previous  = (TextView) findViewById(R.id.previous);
 	    previous.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(month.get(Calendar.MONTH)== month.getActualMinimum(Calendar.MONTH)) {				
 					month.set((month.get(Calendar.YEAR)-1),month.getActualMaximum(Calendar.MONTH),1);
 				} else {
 					month.set(Calendar.MONTH,month.get(Calendar.MONTH)-1);
 				}
 				refreshCalendar();
 			}
 		});
 	
 	
 	TextView next  = (TextView) findViewById(R.id.next);
     next.setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			if(month.get(Calendar.MONTH)== month.getActualMaximum(Calendar.MONTH)) {				
 				month.set((month.get(Calendar.YEAR)+1),month.getActualMinimum(Calendar.MONTH),1);
 			} else {
 				month.set(Calendar.MONTH,month.get(Calendar.MONTH)+1);
 			}
 			refreshCalendar();
 			
 		}
 	});
     
 	gridview.setOnItemClickListener(new OnItemClickListener() {
 	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 	    	TextView date = (TextView)v.findViewById(R.id.date);
 	        if(date instanceof TextView && !date.getText().equals("")) {
 	        	
 	        	Intent intent = new Intent();
 	        	String day = date.getText().toString();
 	        	if(day.length()==1) {
 	        		day = "0"+day;
 	        	}
 	        	// return chosen date as string format 
 	        	intent.putExtra("date", android.text.format.DateFormat.format("yyyy-MM", month)+"-"+day);
 	        	setResult(RESULT_OK, intent);
 	        	Intent i = new Intent(CalendarActivity.this, CalendarInfo.class);
 	        	i.putExtras(intent);
 	        	i.putExtra("account", a);       	
 				startActivity(i);
 			}
 	        	finish();
 	        }
 	});
 }
 
 public void refreshCalendar()
 {
 	TextView title  = (TextView) findViewById(R.id.title);
 	adapter.refreshDays();
 	adapter.notifyDataSetChanged();				
 	handler.post(calendarUpdater); 
 	
 	title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
 }
 
 public void onNewIntent(Intent intent) {
 	String date = intent.getStringExtra("date");
 	String[] dateArr = date.split("-"); // date format is yyyy-mm-dd
 	month.set(Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]));
 }
 Boolean flag = true;
 ArrayList<CalendarItem> itemsList;
 public Runnable calendarUpdater = new Runnable() {
 	
 	
 	@Override
 	public void run() {
 		Thread thread = new Thread()
 		{
 			@Override
 	        public void run()
 			{
 				synchronized(this)
 				{
 										
 					DatabaseAPI api = new DatabaseAPI();
 					final Account a = (Account) getIntent().getExtras().get("account");
 					itemsList = (ArrayList<CalendarItem>) api.getUsersItems(a.getUserID());
 					flag = false;
 				};
 			}
 		};
 		
 		thread.start();
 		while(flag);
 		adapter.setItems(itemsList);
 		adapter.notifyDataSetChanged();
 	}
 };
 
  
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.calendar, menu);
 		return true;
 	}
 
 }
