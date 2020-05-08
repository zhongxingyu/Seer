 package com.example.sprint;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.compuware.apm.uem.mobile.android.CompuwareUEM;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 
 public class JobListJSONTask extends AsyncTask<Context, Void, String> {
 	private JobListFragment jobListFragment;
 	ProgressDialog pd;
 	ArrayList<Job> jobs = new ArrayList<Job>();
 	
 	// Constructor for the JobListJSON Task
 	public JobListJSONTask(JobListFragment jobListFragment) {
 		this.jobListFragment = jobListFragment;
 	}
 	
 	@Override
 	protected String doInBackground(Context... params) {
 				
 		try{
 		    // Create a new HTTP Client and setup the POST
 		    DefaultHttpClient defaultClient = new DefaultHttpClient();
 		    HttpPost httpPostRequest = new HttpPost("http://10.24.16.122/show_active_jobs.php");
 		    
 		    // Set timeout for the connection
 		    final HttpParams httpParameters = defaultClient.getParams();
 		    HttpConnectionParams.setConnectionTimeout(httpParameters, 6000);
 		    HttpConnectionParams.setSoTimeout(httpParameters, 6000);
 		    
 		    // Grab the username from preferences
 		    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(jobListFragment.getActivity());
 		    String prefsUserName = preferences.getString("pref_sprintUsername","");
 		    		
 		    // Add the username to the POST Data
 	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 	        nameValuePairs.add(new BasicNameValuePair("user_name", prefsUserName));
 	        httpPostRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 	        
 		    // Execute the request in the client
 		    HttpResponse httpResponse = defaultClient.execute(httpPostRequest);
 		    
 		    // Check if the response was OK
 		    if(httpResponse.getStatusLine().getStatusCode() == 200)
 		    {		    	
 		    	// Convert the httpResponse into a JSONArray
 		    	HttpEntity entity = httpResponse.getEntity();
 			    InputStream ist = entity.getContent();
 			    ByteArrayOutputStream content = new ByteArrayOutputStream();
 			    int readCount = 0;
 			    byte[] buffer = new byte[1024];
 			    while ((readCount = ist.read(buffer)) != -1)
 			    {
 			    	content.write(buffer, 0, readCount);
 			    }
 			    String retVal = new String(content.toByteArray());
 			    
 			    // Attempt to convert the return value to a JSON Array
 			    try {
 			    	JSONArray jsonArray = new JSONArray(retVal);
 			    	
 			    	 // List of jobs
 				    ArrayList<Job> jobList = new ArrayList<Job>();
 				    
 				    // Loop through the jsonArray and get the user,title,id,type
 				    for (int i = 0; i < jsonArray.length(); i++) {
 				        JSONObject row = jsonArray.getJSONObject(i);
 				        String user = row.getString("user_name").trim();
 				        String name = row.getString("job_title").trim();
 				        String id = row.getString("job_id").trim();
 				        String type = row.getString("file_type").trim().toLowerCase(Locale.getDefault());
 				        
 				        jobList.add(new Job(id, name, user, type));
 				    }
 				    
 				    // Assign the jobs
 				    jobs = jobList;
 				    
 			        return "Success";
 			    } catch (Exception e) {
 			    	e.printStackTrace();
 			    }
 
 			   return "Failed";
 		    }
 
 	        return "Failed";
 
 		} catch(java.net.SocketTimeoutException e) {
 			return "Timeout";
 		} catch(Exception e) {
 			e.printStackTrace();  
 		}
 		
         return "Failed";
 	}
 	
 	@Override
 	protected void onPostExecute(String result) {
 		
 		if(result.equals("Success"))
 		{
 			// Dynatrace Job List Load Leave
 			CompuwareUEM.leaveAction("Job List Load");
 		}
 		else if(result.equals("Timeout"))
 		{
 			// Timeout tell the user to check if they are connected to Compuware and retry
			Toast.makeText(jobListFragment.getActivity(), "Timeout - Check WiFi Setting", Toast.LENGTH_LONG).show();
 		}
 		else
 		{
 			// Error occurred most likely on the back end
			Toast.makeText(jobListFragment.getActivity(), "Error - Try Again Momentarily", Toast.LENGTH_LONG).show();
 		}
 		
 		// Set the jobs and show the list and dismiss the progress dialog
 		jobListFragment.setJobs(jobs);
 		jobListFragment.showList();
 		pd.dismiss();		
 		jobListFragment.notifyJobLoadComplete();
 	}
 	
 	@Override
 	protected void onPreExecute() {
 
 		// Dynatrace Job List Load Enter
 		CompuwareUEM.enterAction("Job List Load");		
 		
 		// Clear the list and start a progress dialog for loading jobs
 		jobListFragment.clearList();
 		pd = new ProgressDialog(jobListFragment.getActivity());
 		pd.setMessage("Loading Jobs ...");
 		pd.show();
 		final AsyncTask<Context, Void, String> task = this;
 		pd.setOnDismissListener(new OnDismissListener() {
 			
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				// Allows canceling the task
 				task.cancel(true);
 			}
 		});
 	}	
 }
