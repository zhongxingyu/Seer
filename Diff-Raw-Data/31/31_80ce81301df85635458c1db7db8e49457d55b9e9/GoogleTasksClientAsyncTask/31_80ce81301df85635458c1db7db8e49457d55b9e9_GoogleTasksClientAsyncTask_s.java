 package com.danielpecos.gtdtm.activities.tasks;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.danielpecos.gtdtm.R;
 import com.danielpecos.gtdtm.model.TaskManager;
 import com.danielpecos.gtdtm.model.beans.Context;
 import com.danielpecos.gtdtm.model.beans.Task;
 import com.danielpecos.gtdtm.model.persistence.GoogleTasksHelper;
 import com.danielpecos.gtdtm.utils.google.GoogleClient;
 import com.danielpecos.gtdtm.utils.google.GoogleClient.AuthCallback;
 import com.danielpecos.gtdtm.utils.google.GoogleTasksClient;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.HttpResponseException;
 
 public class GoogleTasksClientAsyncTask extends AsyncTask<Object, Integer, String> {
 	public static final String GTASKS_ACTION_SYNCHRONIZE = "gTasks_synchronization";
 	public static final String GTASKS_ACTION_DELETE_TASK = "gTasks_deleteTask";
 
 	public static final String RESPONSE_OK = "OK";
 
 	private Activity activity;
 	private Collection<Context> contexts = null;
 	private Task task = null;
 	private GoogleClient googleClient;
 
 	ProgressDialog progressDialog;
 
 	public GoogleTasksClientAsyncTask(Activity activity) {
 		this.activity = activity;
 		this.googleClient = new GoogleClient();
 	}
 	
 	int retries = 1;
 
 	@Override
 	protected String doInBackground(Object... params) {
 		final String action = (String)params[0];
 
 		if (action.equalsIgnoreCase(GTASKS_ACTION_SYNCHRONIZE)) {
 			this.contexts = (Collection<Context>) params[1];
 		} else if (action.equalsIgnoreCase(GTASKS_ACTION_DELETE_TASK)) {
 			this.task = (Task) params[1];
 		}
 
 		final StringBuilder message = new StringBuilder();
 		
 		googleClient.selectGoogleAccount(activity, new AuthCallback() {
 			@Override
 			public void onAuthResult(final String authToken) {
 				googleClient.login(activity, new AuthCallback() {
 					@Override
 					public void onAuthResult(String authToken) {
 						if (authToken != null) {
 							GoogleTasksClient googleTasksClient = new GoogleTasksClient(activity, authToken);
 
 							message.append(processAction(action, googleTasksClient, contexts, task, this));
 						}
 					}
 				});
 			}
 		});
 
 		return message.toString();
 
 		//return GoogleTasksHelper.doInGTasks(activity, GoogleTasksHelper.GTASKS_ACTION_SYNCHRONIZE, contexts, null, null);
 
 	}
 
 	private String processAction(String action, GoogleTasksClient googleTasksClient, Collection<Context> contexts, Task task, AuthCallback authCallback) {
 		Log.i(TaskManager.TAG, "GTasks: Synchronizing with Google Tasks...");
 
 		try {
 			if (action.equalsIgnoreCase(GTASKS_ACTION_SYNCHRONIZE)) {
 				StringBuilder message = new StringBuilder(); 
 				for (Context context: contexts) {
 					message.append(GoogleTasksHelper.gTasksSynchronization(activity, googleTasksClient, context));
 				}
 				return message.toString();
 			} else if (action.equalsIgnoreCase(GTASKS_ACTION_DELETE_TASK)) {
 				return GoogleTasksHelper.gTasksDeleteTask(activity, googleTasksClient, task);
 			} else {
 				return null;
 			}
 		} catch (Exception e) {
 			String message = this.handleException(e, activity, authCallback);
 			if (message == null && this.retries > 0) {
 				// it was a 401
 				Log.i(TaskManager.TAG, "GTasks: retrying action...");
 				this.retries--;
 				this.googleClient.login(activity, authCallback);
 			}
 			return message;
 		}
 	}
 
 	private String handleException(Exception e, Activity activity, AuthCallback authCallback) {
 		Log.e(TaskManager.TAG, e.getMessage(), e);
 		String message = null;
 		if (e instanceof HttpResponseException) {
 			HttpResponse response = ((HttpResponseException) e).response;
 			int statusCode = response.statusCode;
 			try {
 				response.ignore();
 			} catch (IOException ioe) {
 				Log.e(TaskManager.TAG, "GoogleClient: error", ioe);
 			}
 			if (statusCode == 400) {
 				Log.e(TaskManager.TAG, "GTasks: error in request " + e.getMessage(), e);
 				message = activity.getString(R.string.gtasks_errorInRequest);
 			} else if (statusCode == 401) {
 				Log.w(TaskManager.TAG, "GoogleClient: authToken invalid! " + statusCode);
 			} else {
 				Log.e(TaskManager.TAG, "GTasks: error in communication", e);
 				message = activity.getString(R.string.gtasks_errorInCommunication);
 			}
 		} else {
 			Log.e(TaskManager.TAG, "GTasks: unknown error: " + e.getMessage(), e);
 			message = activity.getString(R.string.error_unknown) + ": " + e.getMessage();
 		}
 		return message;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.gtasks_synchronizing), true);
 	}
 
 	@Override
 	protected void onPostExecute(String response) {
 		super.onPostExecute(response);
		progressDialog.dismiss();
 		if (response != null) {
 			if (response.equalsIgnoreCase(RESPONSE_OK)) {
 				Toast.makeText(activity, activity.getString(R.string.gtasks_synchronizationFinished), Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
 			}
 		} 
 //		activity.initializeUI();
 	}
 }
