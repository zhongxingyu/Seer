 /**
  * @author Raghav Sood
  */
 package com.appaholics.listviewfont;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 /**
  * The Class MainActivity.
  */
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		ListView listView = (ListView) findViewById(R.id.listView1);
 		String[] values = new String[] { "Android", "iPhone", "WindowsMobile", "Blackberry", "WebOS", "Ubuntu",
				"Windows7", "Mac OS X", "Linux", "OS/2" };
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, R.id.fontView, values);
 
 		listView.setAdapter(adapter);
 	}
 
 }
