 package com.mainapp.mynoppaapp;
 
 
 import java.util.ArrayList;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import datastructures.Course;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class DisplaySearchResultsActivity extends Activity {
 	private ArrayAdapter<String> mAdapter;
 	final ArrayList<String> list = new ArrayList<String>();
 	final ArrayList<Course> courselist = new ArrayList<Course>();
 	public final static String EXTRA_MESSAGE = "com.mainapp.mynoppaapp.MESSAGE";
 
 	private final String key = "key=cdda4ae4833c0114005de5b5c4371bb8";
 	private final String host = "http://noppa-api-dev.aalto.fi/api/v1/";
 
 	private String message;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_search_results);
 		Intent intent = getIntent();
 		message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
 		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
 				list);
 		final ListView listview = (ListView) findViewById(R.id.listview);
 		listview.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				openCourseDetails(position);
 			}
 		});
 		listview.setAdapter(mAdapter);
 		getSearchResults();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_display_search_results, menu);
 		return true;
 	}
 
 	private void openCourseDetails(int position) {
 		Intent intent = new Intent(this, DisplayCourseDetailsActivity.class);
 		final Course course = courselist.get(position);
 		intent.putExtra(EXTRA_MESSAGE, course);
 		startActivity(intent);
 	}
 
 	private void getSearchResults() {
 
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					String url = host + "courses?" + key + "&search=" + message;
 					final String r = MainActivity.CONNECTOR.getResults(url);
 					final ListView listview = (ListView) findViewById(R.id.listview);
 					listview.post(new Runnable() {
 						public void run() {
 
 							JSONTokener jsontokener = new JSONTokener(r);
 
 							try {
 								JSONArray jsonarray = new JSONArray(jsontokener);
 								for (int i = 0; i < jsonarray.length(); i++) {
 									JSONObject obj = (JSONObject) jsonarray
 											.get(i);
 									String course_id = (String) obj
 											.get("course_id");
 								
 									String name = (String) obj.get("name");
 									Course c = new Course(
 											course_id, name);
 									courselist.add(c);
 									mAdapter.add(name);
 								}
 
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 					});
 
 					// }
 				} catch (Exception e) {
 					e.printStackTrace();
 
 				}
 			}
 		}).start();
 
 	}
 
 }
