 package pl.jacbar.runner;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 @SuppressLint({ "NewApi", "NewApi" })
 public class WorkoutDetails extends Activity {
 	private long workoutId = 0;
 	private Button removeBtn;
 	private Button syncBtn;
 	private Button mapBtn;
 	private SharedPreferences preferences;
 	private DatabaseHandler dh;
 	private Workout workout;
 	
 
 	@TargetApi(9)
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
         setContentView(R.layout.workoutdetail);
         preferences = getSharedPreferences(Config.RUNNER_PREFERENCES, Activity.MODE_PRIVATE);
         if(preferences.getString("username", "").equals("") || preferences.getString("password", "").equals("")){
         	Intent loginIntent = new Intent(getApplicationContext(), Login.class);
 			startActivity(loginIntent);
         }
         
 	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 	    StrictMode.setThreadPolicy(policy); 
         
         workoutId = Long.parseLong(getIntent().getStringExtra("workoutid"));
         TextView startTx = (TextView) findViewById(R.id.start);
         TextView endTx = (TextView) findViewById(R.id.end);
         TextView typeTx = (TextView) findViewById(R.id.type);
         TextView durationTx = (TextView) findViewById(R.id.durationTV);
         TextView distanceTx = (TextView) findViewById(R.id.distance);
         
         dh = new DatabaseHandler(this);
         workout = dh.getWorkout(workoutId);
         
         startTx.setText("Start : " + workout.getStartString());
         endTx.setText("End : " + workout.getEndString());
         typeTx.setText("Type : " + Config.getType(workout.getType()));
        distanceTx.setText(String.format("Distance : %.0f m", workout.getDistance()));
         
         int h = workout.getDuration() / 3600;
         int m = (workout.getDuration() - (h * 3600)) / 60;
         int s = workout.getDuration() % 60;
         
         durationTx.setText(String.format("Duration : %02d:%02d:%02d",h,m,s));
 
         
         ListView lv = (ListView)findViewById(R.id.workoutParts);
         ArrayAdapter<WorkoutPart> adapter = new ArrayAdapter<WorkoutPart>(this, android.R.layout.simple_list_item_1, workout.getWorkoutParts());
         lv.setAdapter(adapter);
         
         mapBtn = (Button) findViewById(R.id.mapBtn);
         mapBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent mapIntent = new Intent(getApplicationContext(), Map.class);
 				mapIntent.putExtra("workoutid", Long.toString(workout.getId()));
 				startActivity(mapIntent);
 			}
 		});
         
         syncBtn = (Button) findViewById(R.id.syncBtn);
         if(workout.getIsSynchronized()){
         	syncBtn.setVisibility(View.GONE);
         }
         
         if(workout.getWorkoutParts().isEmpty()){
         	mapBtn.setVisibility(View.GONE);
         }
         
         removeBtn = (Button) findViewById(R.id.deleteBtn);
         removeBtn.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				dh.removeWorkout(workout.getId());
 				finish();
 			}
 		});
         
         syncBtn.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				 System.out.println("Im in workout details sync");
 				 System.out.println(preferences.getString("token", ""));
 				 try {
 					WorkoutResult result = WorkoutSychHelper.SyncWorkout(workout, preferences.getString("token", ""));
 					if(result.getHttpStatus() == 401){	
 						LoginResult loginResult =  LoginHelper.Login(preferences.getString("username", ""), preferences.getString("password", ""));
 						
 						if(loginResult.getHttpStatus() != 200){
 							goToLogin();
 						} 
 						
 						SharedPreferences.Editor preferencesEditor = preferences.edit();
 						preferencesEditor.putString("token", loginResult.getToken());
 						preferencesEditor.commit();
 						
 						result = WorkoutSychHelper.SyncWorkout(workout, preferences.getString("token", ""));
 						
 					} else if(result.getHttpStatus() == 400){
 						return;
 					}
 					
 					workout.setOriginalId(result.getWorkoutId());
 					if(!workout.getWorkoutParts().isEmpty()){
 						RequestResult res =  WorkoutSychHelper.SyncWorkoutParts(workout, preferences.getString("token", ""));
 						if(res.getHttpStatus() != 200) return;
 					}
 					
 					dh.setAsSynchronized(workout);
 					syncBtn.setVisibility(View.GONE);
 					
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		});
         
 	}
 	
 	private void goToLogin(){
 		SharedPreferences.Editor preferencesEditor = preferences.edit();
 		preferencesEditor.clear();
 		preferencesEditor.commit();
     	Intent loginIntent = new Intent(getApplicationContext(), Login.class);
 		startActivity(loginIntent);
 	}
 }
