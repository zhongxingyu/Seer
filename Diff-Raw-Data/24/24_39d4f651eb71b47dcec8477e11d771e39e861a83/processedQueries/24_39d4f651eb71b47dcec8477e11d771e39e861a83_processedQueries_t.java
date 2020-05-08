 package net.ednovak.nearby;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class processedQueries extends ListActivity {
 	
 	private static ArrayList<String> data = new ArrayList<String>();
 	
 	// Should be called when activity is newly opened
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_processed_queries);
 		
		Intent i = getIntent();		
 		String lat = i.getStringExtra("lat");
 		String lon = i.getStringExtra("lon");
		String name = i.getStringExtra("name");		
 		
 		ListView list = (ListView)findViewById(android.R.id.list);
 		//data.add(name + ": " + lat + ", " + lon);
 		data.add(0, name + ": " + lat + ", " + lon);
 		ArrayAdapter<String> AA = new ArrayAdapter<String>(this, R.layout.smallfont, data);
 		list.setAdapter(AA);
 		
 		// Short click to open with browser
 		list.setOnItemClickListener(new OnItemClickListener() {
 			
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int pos, long id){
 				String s = ((TextView) v).getText().toString();
 				s = s.split(":")[1];
 				
 				//http://maps.google.com/?f=q&q=37.2700,+-76.7116
 				String url = "http://maps.google.com/?f=q&q=" + s;
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(url));
 				startActivity(i);
 			}		
 		});
 		
 		// Long click to open with maps app
 		list.setOnItemLongClickListener(new OnItemLongClickListener() {
 			
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id){
 				String s = ((TextView) v).getText().toString();
 				s = s.split(":")[1];
 				
 				String url = "geo:" + s;
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(url));
 				try{
 					startActivity(i);
 				}
 				catch (ActivityNotFoundException e){
 					Toast.makeText(getApplicationContext(), "You don't have a maps app!", Toast.LENGTH_SHORT).show();
 				}
 				return true;
 			}
 		});
 	}
 }
