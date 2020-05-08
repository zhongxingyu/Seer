 package com.danielpecos.gtdtm.activities.tasks;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.danielpecos.gtdtm.R;
 import com.danielpecos.gtdtm.model.TaskManager;
 import com.danielpecos.gtdtm.model.beans.Context;
 import com.danielpecos.gtdtm.model.beans.Project;
 import com.danielpecos.gtdtm.model.beans.Task;
 import com.google.android.maps.GeoPoint;
 
 public class CreateDemoDataAsyncTask extends AsyncTask<Object, Integer, Void>{
 
 	private Activity activity;
 
 	ProgressDialog progressDialog;
 
 	private OnFinishedListener onFinishedListener;
 
 	public CreateDemoDataAsyncTask(Activity activity) {
 		this.activity = activity;
 	}
 
 	@Override
 	protected Void doInBackground(Object... params) {
 		final TaskManager taskManager = TaskManager.getInstance(activity);
 		
 		Context ctx = taskManager.createContext(activity, "Context 1");
 		Project prj = ctx.createProject(activity, "Project 1.1", "Project 1.1 description");
 		prj.createTask(activity, "Task 1", "Task number 1.1.1", Task.Priority.Critical).setStatus(Task.Status.Discarded).store(activity);
 		prj.createTask(activity, "Task 2", "Task number 1.1.2", Task.Priority.Important);
 		prj.createTask(activity, "Task 3", "Task number 1.1.3", Task.Priority.Low).setStatus(Task.Status.Discarded_Completed).store(activity);
 		prj.createTask(activity, "Task 4", "Task number 1.1.4", Task.Priority.Important).setStatus(Task.Status.Completed).store(activity);
 		prj.createTask(activity, "Task 5", "Task number 1.1.5", Task.Priority.Critical);
 
 		ctx.createTask(activity, "Task 1", "Task number 1.0.1", Task.Priority.Critical).setStatus(Task.Status.Completed).store(activity);
 		ctx.createTask(activity, "Task 2", "Task number 1.0.2", Task.Priority.Important);
 		ctx.createTask(activity, "Task 3", "Task number 1.0.3", Task.Priority.Normal).setLocation(new GeoPoint(40000000, 0)).store(activity);
 		ctx.createTask(activity, "Task 4", "Task number 1.0.4", Task.Priority.Low);
 
 		prj = ctx.createProject(activity, "Project 1.2", "Project 1.2 description");
 		prj.createTask(activity, "Task 1", "Task number 1.2.1", Task.Priority.Critical);
 		prj.createTask(activity, "Task 2", "Task number 1.2.2", Task.Priority.Important);
 		prj.createTask(activity, "Task 3", "Task number 1.2.3", Task.Priority.Low).setStatus(Task.Status.Completed).store(activity);
 		prj.createTask(activity, "Task 4", "Task number 1.2.4", Task.Priority.Important);
 		prj.createTask(activity, "Task 5", "Task number 1.2.5", Task.Priority.Critical);
 
 		taskManager.createContext(activity, "Context 2");
 		taskManager.createContext(activity, "Context 3");
 		
 		return null;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.context_loadDemo_creating), true);
 	}
 
 	@Override
 	protected void onPostExecute(Void response) {
 		super.onPostExecute(response);
 		Log.i(TaskManager.TAG, "Demo data created succesffully");
		progressDialog.dismiss();
 		if (this.onFinishedListener != null) {
 			onFinishedListener.onFinish(null);
 		}
 	}
 	
 	public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
 		this.onFinishedListener = onFinishedListener;
 	}
 }
