 package com.pomometer.PomometerApp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Vector;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 public class ResultListActivity extends Activity {
 	
 	String task_id = "";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_list_layout);	
 
 		//the project_id sent by the Task list
 		
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			//task_id = ((Integer) extras.getInt("id")).toString();
 			task_id = extras.getString("id");
 		}
 		
 		//change title to correspond to task
 		TextView page_title = (TextView) findViewById(R.id.page_title);
 		page_title.setText("Result List");
 		
 		final int MAX_LINES_RESULT_NAME = 2;
 		
 		//new table layout which will load the table that the data will be placed in
 		TableLayout result_list_content = (TableLayout) findViewById(R.id.page_list_content);
 		
 		//Frame layout to add the START POMODORO button
 		FrameLayout options_frame = (FrameLayout) findViewById(R.id.page_list_options_frame);
 		//a separation line for distinction from table
 		LinearLayout a_separator = new LinearLayout(this);
 		a_separator.setBackgroundColor(getResources().getColor(R.color.red_background));
 		a_separator.setMinimumHeight(2); //set a thin line separator
 		options_frame.addView(a_separator);
 		
 		//button to add to bottom frame
 		Button start_button = new Button(this);
 		start_button.setText("Start Pomodoro");
 		start_button.setTextColor(getResources().getColor(R.color.white_text));
 		start_button.setBackgroundColor(getResources().getColor(R.color.red_foreground));
 		start_button.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		start_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
     	    	Intent i = new Intent(getApplicationContext(), PomometerOptionsActivity.class);
     	    	i.putExtra("task_id", task_id);
     	    	startActivity(i);
             }
         });
 		options_frame.addView(start_button);
 		
 		//list of Tasks, to be retrieved through GSON
 		Vector<Result> list_of_results = new Vector<Result>();
 		list_of_results = populate();
 		
 		if (list_of_results.size() == 0){
 			TableRow a_row_to_add = new TableRow(this);
 			a_row_to_add.setBackgroundColor(getResources().getColor(R.color.red_foreground));
 			TextView notice = new TextView(this);
 			notice.setText("There are no Results for this Task.");
 			notice.setMaxLines(MAX_LINES_RESULT_NAME);
 			notice.setTextColor(getResources().getColor(R.color.white_text));
 			notice.setTextSize(20);
 			notice.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.66f));
 			a_row_to_add.addView(notice);
 			result_list_content.addView(a_row_to_add);
 		}else{
 			for(int i=0;i<list_of_results.size();i++)
 			{
 				//New row to be added to table
 				TableRow a_row_to_add = new TableRow(this);
 				//set background colour of the row
 				a_row_to_add.setBackgroundColor(getResources().getColor(R.color.red_foreground));
 				
 				//Row contains three buttons
 				//Task name button, will link to task page for this Task
 				Button result_name = new Button(this);
 
 				//set button attributes
 				result_name.setText(list_of_results.get(i).getGoal());
 				result_name.setMaxLines(MAX_LINES_RESULT_NAME);
 				result_name.setTag("task_" + list_of_results.get(i).getId());
 				result_name.setTextColor(getResources().getColor(R.color.white_text));
 				result_name.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 							
 				//add the buttons to the current row, then add the row to the table
 				a_row_to_add.addView(result_name);
 				result_list_content.addView(a_row_to_add);			
 			}
 		}
 		
 		
 	}
 	
 	private Vector<Result> populate() {
 	    Vector<Result> results = new Vector<Result>();
 	    JSONObject obj = null;
 	    
 	        String jsonUrl = "http://pomometer.herokuapp.com/tasks/" + task_id + ".json";
 	        try {
 				obj = new Read().execute(jsonUrl).get();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	        
 	        	JSONArray ja = null;
 				try {
 					ja = obj.getJSONArray("results");
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        	for (int i = 0; i < ja.length(); i++){
 	        		JSONObject jo = null;
 	      
 					try {
 						jo = (JSONObject) ja.get(i);
						Locale defaultLocale = Locale.getDefault();
 						String goal = jo.getString("goal");
 						String notes = jo.getString("notes");
 						Date started_at = null;
 						Date ended_at = null;
 						try {
							//Calendar myCalendar = Calendar.getInstance();
							//System.out.println(jo.getString("started_at"));
							//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.Z", Locale.ENGLISH);
							started_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", defaultLocale).parse(jo.getString("started_at"));
							System.out.println("This is the started at date: ");
							ended_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", defaultLocale).parse(jo.getString("ended_at"));
 							
 						} catch (ParseException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						int duration = Integer.valueOf(jo.getString("duration"));
 						int task = Integer.valueOf(task_id);
 						int id = Integer.valueOf(jo.getString("id"));
 						
 						Result tempResult = new Result(id, goal, notes, duration, started_at, ended_at, task);
 						results.add(tempResult);
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 	        	}
 	        
         return results;
     }
 	
 	public class Read extends AsyncTask<String, Integer, JSONObject>{
 
 		@Override
 		protected JSONObject doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			URL jsonUrl = null;
 			URLConnection transport = null;
 			InputStreamReader isr = null;
 			String line = null;
 			JSONObject tempObj = null;
 			
 			
 			try {
 				jsonUrl = new URL(params[0]);
 			} catch (MalformedURLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
 			
 			try {
 				transport = jsonUrl.openConnection();
 				isr = new InputStreamReader(transport.getInputStream());
 				BufferedReader in = new BufferedReader(isr);
 				line = in.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			
 			try {
 				tempObj = new JSONObject(line);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return tempObj;
 			//set button attributes			
 		}
 		
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.project_list, menu);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    //respond to menu item selection
 		switch (item.getItemId()) {
 			case R.id.durationAndBreakLength:
 				startActivity(new Intent(this, DurationAndBreak.class));
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 }
