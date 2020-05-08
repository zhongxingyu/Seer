 package com.pomometer.PomometerApp;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class PomometerFinishActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_pomometer_finish);
 		
 		Bundle extras = getIntent().getExtras();
 		
 		final String sent_goal = extras.getString("goal");
 		
 		final Long sent_duration = extras.getLong("elapsed_duration"); //in ms
 		double decimal_duration = sent_duration/1000; //converts to seconds
 		decimal_duration /= 60; //converts to minutes
 		
 		//round down to nearest minute. if 0, make it 1
 		int temp_duration=0;
 		if ((int)Math.floor(decimal_duration) == 0)
 		{
 			temp_duration = 1;
 		}
 		else
 		{
 			temp_duration = (int)Math.floor(decimal_duration);
 		}
 		
 		final int calculated_duration = temp_duration;
 		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
 		Date endPomometer = new Date();
 		Date startPomometer = new Date();
 		//since declaring starts a new date NOW (finished timer) we want to set that to end
 		//and calculate backwards to find start time.  
 		
 		//convert duration to milliseconds and substract to get start time
 		startPomometer.setTime(endPomometer.getTime() - (calculated_duration*60*1000));
 		
 		final String formattedStartDate = dateFormatter.format(startPomometer);
 		final String formattedEndDate = dateFormatter.format(endPomometer);
 		
 		final int task_id = Integer.parseInt(extras.getString("task_id"));
 		
 		//set title to Complete: goal.  No strings.xml entry as this is dynamic
 		((TextView) findViewById(R.id.finish_title)).setText("Complete: " + sent_goal);
 		
 		Button confirm_button = (Button) findViewById(R.id.final_submit_button);
 		confirm_button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				//json to commit to webserver
 				final String notes = ((EditText) findViewById(R.id.notes_edit_text)).getText().toString();
 				
 				JSONObject result = new JSONObject();
 				
 				try {
 					//result.put("id", 99);
 					
 					result.put("goal", sent_goal);
 					result.put("notes", notes);
 					result.put("duration", calculated_duration);
 					result.put("started_at", formattedStartDate);
 					result.put("ended_at", formattedEndDate);
 					result.put("task_id", task_id);
 					
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				new Write().execute(result);
 				
 				/*
 				 * sent_goal
 				 * (EditText) findViewById(R.id.notes_edit_text)
 				 * calculated_duation
 				 * formattedStartDate
 				 * formattedEndDate
 				 * task_id
 				 */
 				
         		//Toast.makeText(getBaseContext(), ((Integer)calculated_duration).toString(), Toast.LENGTH_SHORT).show();
 
 				Intent i = new Intent(getApplicationContext(), ResultListActivity.class);
 				i.putExtra("task_id", task_id); 
 				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         		startActivity(i);
 			}			
 		});
 	}
 	
 	public class Write extends AsyncTask<JSONObject, Integer, Boolean>{
 
 		@Override
 		protected Boolean doInBackground(JSONObject... params) {
 			// TODO Auto-generated method stub
 			
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpPost httpPost = new HttpPost("http://pomometer.herokuapp.com/results.json");
 			//HttpPost  httpPost = new HttpPost("localhost:3000")
 			try {
 				StringEntity JSONResult = new StringEntity(params[0].toString());
 				httpPost.setEntity(JSONResult);
 				httpPost.setHeader("Content-Type","application/json");
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			HttpResponse response = null;
 			
 			try {
 		        response = client.execute(httpPost);
 		    } catch (ClientProtocolException e) {
 		        e.printStackTrace();
 		    } catch (IOException e) {
 		        e.printStackTrace();
 		    }	
 			
 			return true;
 		}
 		
 	}
 }
