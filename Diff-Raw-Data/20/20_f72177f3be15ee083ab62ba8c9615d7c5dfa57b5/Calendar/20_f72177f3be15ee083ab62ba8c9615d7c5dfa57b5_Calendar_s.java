 /**
  * Calendar
  * 
  * @author Darren Seung Won
  * @Process: In development 
  */
 
 package com.vorsk.crossfitr;
 
 import java.util.ArrayList;
 import java.util.Date;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.TextView;
 
public class Calendar extends Activity implements OnClickListener {
 	/** Called when the activity is first created. */
 	ArrayList<String> mItems;
 	ArrayAdapter<String> adapter;
 	TextView string_year, string_month;
 	int current_year, current_month;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.calendar);
 		
 		string_year = (TextView) findViewById(R.id.current_year);
 		string_month = (TextView) findViewById(R.id.current_month);
 		
 		mItems = new ArrayList<String>();
 
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mItems);
 
 		GridView grid = (GridView) findViewById(R.id.calendar_grid);
 		grid.setAdapter(adapter);
 
 		Date date = new Date();
 		current_year = date.getYear() + 1900;
 		current_month = date.getMonth() + 1;
 	
 
 		string_year.setText(current_year + "");
 		string_month.setText(current_month + "");
 
 		fillDate(current_year, current_month);
 		
 		// pre button control
 		Button b_pre_move = (Button) findViewById(R.id.go_pre_month);
 		b_pre_move.setOnClickListener(this);
 		
 		// post button control 
 		Button b_post_move = (Button) findViewById(R.id.go_post_month);
 		b_post_move.setOnClickListener(this);
 		
 	}
 
 	public void onClick(View view) {
 		// TODO Auto-generated method stub
 		switch (view.getId()) {		
 			
 		case R.id.go_pre_month:
 			current_month -= 1;
 			if(current_month == 0){
 				current_month += 12;
 				current_year -= 1;
 			}
 			//TODO: what about the case, year < 1900?? 
 			fillDate(current_year, current_month);
 			break;
 		
 		case R.id.go_post_month:
 		
 			current_month += 1;
 			if(current_month == 13){
 				current_month = 1;
 				current_year += 1;
 			}
 		//TODO: No limitation of the year going up? 			
 			fillDate(current_year, current_month);
 			break;			
 		}
 	}
 
 	private void fillDate(int year, int mon) {
 		string_year.setText(current_year + "");
 		string_month.setText(current_month + "");
 		mItems.clear();
 
 		mItems.add("Sun");
 		mItems.add("Mon");
 		mItems.add("Tue");
 		mItems.add("Wed");
 		mItems.add("Thu");
 		mItems.add("Fri");
 		mItems.add("Sat");
 
 		Date current = new Date(year - 1900, mon - 1, 1);
 		int day = current.getDay();
 		// find out a start point
 		for (int i = 0; i < day; i++) {
 			mItems.add("");
 		}
 		// commute the last date of months
 		current.setDate(32);
 		int last = 32 - current.getDate();
 		// fill out dates
 		for (int i = 1; i <= last; i++) {
 			mItems.add(i + "");
 		}
 		adapter.notifyDataSetChanged();
 	}
 

 }
