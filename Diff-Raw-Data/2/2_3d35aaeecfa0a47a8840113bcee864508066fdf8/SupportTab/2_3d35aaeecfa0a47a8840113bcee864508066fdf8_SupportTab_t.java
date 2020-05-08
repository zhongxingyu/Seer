 package com.squareup.timessquare.sample;
 
 import com.squareup.timessquare.sample.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class SupportTab extends Activity{
 
 	ListView lv;
 	String[]  supportItems;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.support_tab_layout);
 		
 		lv = (ListView) findViewById(R.id.list);
 		
 		supportItems = getResources().getStringArray(R.array.supportItems);
 		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.label, supportItems));
 		lv.setTextFilterEnabled(true);
 		
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
 				
 				//Contacts
 				if(lv.getItemAtPosition(position) == supportItems[0])
 				{
					Intent i = new Intent(getApplicationContext(), AllContactsActivity.class);
 					startActivity(i);
 				}
 				//Learning Center
 				if(lv.getItemAtPosition(position) == supportItems[1]){
 					Intent i = new Intent(getApplicationContext(), LearningCenter.class);
 					startActivity(i); 
 				}
 				//Videos
 				if(lv.getItemAtPosition(position) == supportItems[2]){
 					Intent i = new Intent(getApplicationContext(), Videos.class);
 					startActivity(i);
 				}
 				//Scholarships
 				if(lv.getItemAtPosition(position) == supportItems[3]){
 					Intent i = new Intent(getApplicationContext(), Scholarships.class);
 					startActivity(i);
 				}
 			}
 		});	
 	}
 	
 
 }
