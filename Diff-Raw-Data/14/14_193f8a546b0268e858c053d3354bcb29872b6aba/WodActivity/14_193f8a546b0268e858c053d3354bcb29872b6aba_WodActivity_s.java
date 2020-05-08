 package com.vorsk.crossfitr;
 
 import java.util.ArrayList;
 
 import com.vorsk.crossfitr.models.WODModel;
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.SQLException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class WodActivity extends Activity  implements OnItemClickListener
 {
 	private ListView listView;
 	protected ProgressDialog pd;
 	ArrayAdapter<WorkoutRow> adapter;
 	private static String TAG = "WODActivity";
 	
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.workout_list);
 		
 		listView = (ListView) findViewById(R.id.workout_list_view);
 		
 		WODModel WODmodel = new WODModel(this);
 
 		listView.setOnItemClickListener(this);
 		
 		DownloadWOD downloadTask = new DownloadWOD(WODmodel,this);
 		startLoadingScreen(downloadTask);
 		downloadTask.execute(0);
 	}
 	
 	protected void startLoadingScreen(final AsyncTask task){
 		 pd = ProgressDialog.show(this, "Loading...", "Retrieving Workouts", true, true,
 				 new DialogInterface.OnCancelListener(){
              public void onCancel(DialogInterface dialog) {
                  task.cancel(true);
                  finish();
              }
          }
 		);
 	}
 	
 	protected void stopLoadingScreen(){
 		if (pd != null){
 			pd.dismiss();
 		}
 	}
 	
 	/**
 	 * ASync task for loading the RSS
 	 * @author Ian
 	 */
 	 private class DownloadWOD extends AsyncTask<Integer, Integer, ArrayList<WorkoutRow>> {
 		 WODModel model;
 		 Activity context;
 		 public DownloadWOD(WODModel model,Activity parent){
 			 this.model = model;
 			 this.context = parent;
 		 }
 	     protected ArrayList<WorkoutRow> doInBackground(Integer... models) {
 	    	 model.fetchAll();
 	         //publishProgress((int) ((i / (float) count) * 100));
 	         return model.getWodRows();
 	     }
 
 	     protected void onProgressUpdate(Integer... progress) {
 	         //setProgressPercent(progress[0]);
 	     }
 
 	     protected void onPostExecute(ArrayList<WorkoutRow> results) {
 			
 			adapter = new ArrayAdapter<WorkoutRow>(context,
 					android.R.layout.simple_list_item_1, android.R.id.text1,results);
 			stopLoadingScreen();
 	 		listView.setAdapter(adapter);
 
 	     }
 	 }
 
 	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
 		//pass the ID of the workout into the WorkoutProfileActivity
 		WorkoutRow workout = adapter.getItem(position);
 		
 		//add the selected workout to the DB
 		WorkoutModel model = new WorkoutModel(this);
 		model.open();
 		
 		long entry_id = model.getIDFromName(workout.name);
 
 		if (entry_id == -1){
 			Log.d(TAG,"WOD not in DB, inserting");
 			try {
 				//entry_id = model.insert(workout);
 				
 				//Log.d(TAG,"WODTypeID: "+workout.record_type_id);
 				entry_id = model.insert(workout.name, workout.description, (int)workout.workout_type_id,
 														(int)workout.record_type_id, workout.record);
 			} catch (SQLException e) {
 				Log.e(TAG,"derp on wod insert");
 				model.close();
 				return;
 			}
 
 		}
 		model.close();
 		if (entry_id == -1){
 			Log.e(TAG,"could not insert WOD into DB, unknown error");
 			return;
 		}
 		
 		Intent x = new Intent(this, WorkoutProfileActivity.class);
 		x.putExtra("ID", entry_id); 
 		startActivity(x);
 	}
 
 }
