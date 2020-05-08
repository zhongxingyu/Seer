 package com.ampvita.paybaq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class ViewRemindersActivity extends ListActivity {
 
 	PayBaqAdapter adapter;
 	public ArrayList<String> values = new ArrayList<String>();
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_reminders);
 
 		// listView = (ListView) findViewById(R.id.list);
 
 		try {
 			values = new ArrayList<String>();
 			BufferedReader inputReader = new BufferedReader(new InputStreamReader(
 					openFileInput("ReminderList")));
 			String inputString;               
 			while ((inputString = inputReader.readLine()) != null) {
 				values.add(inputString);
 			}
 			adapter = new PayBaqAdapter(this, R.layout.debtitem, values);
 			setListAdapter(adapter);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void onPause() {
 		super.onPause();
 
 		try {
 			new File(getFilesDir(), "ReminderList").delete();
 
 			FileOutputStream fos = openFileOutput("ReminderList", Context.MODE_APPEND);
 
 			int size = adapter.getCount();
 			for (int i = 0; i <= size; i++) {
 				String[] parts = adapter.getItem(i).split("\\t");
 
 				if (parts.length >= 4) {
 					fos.write((parts[0] + "\t").getBytes()); // Name
 					fos.write((parts[1] + "\t").getBytes()); // Number
 					fos.write((parts[2] + "\t").getBytes()); // Reason
 					fos.write((parts[3] + "\t").getBytes()); // Amount
 					fos.write(parts[4].getBytes()); // Tier
 					fos.write(("\n").getBytes());
 				}
 			}
 			fos.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void onBackPressed() {
 		startActivity(new Intent(this, StartActivity.class));
		finish();
 	}
 }
