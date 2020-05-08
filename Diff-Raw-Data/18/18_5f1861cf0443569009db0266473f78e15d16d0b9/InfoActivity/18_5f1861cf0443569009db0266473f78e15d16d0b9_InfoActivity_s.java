 package com.cscapstone.protone;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class InfoActivity extends ListActivity {
 
 	private ArrayAdapter<String> mInfoAdapter;
 	private ListView factsListView;
 	private InfoActivity infoActivityInstance = null;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         infoActivityInstance = this;
        
         /*
         // Create a progress bar to display while the list loads
         ProgressBar progressBar = new ProgressBar(this);
         progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.WRAP_CONTENT, Gravity.CENTER));
         progressBar.setIndeterminate(true);
         getListView().setEmptyView(progressBar);
         
         // Must add the progress bar to the root of the layout
         ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
         root.addView(progressBar);
         */
         
         PointOfInterestTask downloadTask = new PointOfInterestTask();
         downloadTask.execute("Morken");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_info, menu);
         return true;
     }
     
     
     private class PointOfInterestTask extends AsyncTask<String, Integer, ArrayList>{
 
     	
     	
     	private ArrayList sendPOIData(String poi) throws IOException{
             
     		BufferedReader rd = null; 
             StringBuilder sb = null; 
             String line = null; 
             ArrayList<String> data = new ArrayList<String>();
             try{
                 URL url = new URL(getString(R.string.poi_url));
                 /** Creating an http connection to communicate with url */
                 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                 /** Allow inputs */
                 urlConnection.setDoInput(true);
                 /** Connecting to url */
                 urlConnection.connect();
                 
                 /** Sending data to the server */
                 
                 //DataOutputStream dos = new DataOutputStream( urlConnection.getOutputStream() );
                 
                 //dos.writeBytes("point of interest:" + poi + "\r\n");
                 
                 //dos.flush();  
                 rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); 
                 sb = new StringBuilder(); 
 
                 while ((line = rd.readLine()) != null) { 
                 	data.add(line);
                 } 
                 System.out.println("body=" + data.get(1).toString()); 
                 
                 return data;
      
             }catch(Exception e){
                 Log.d("Exception while downloading url", e.toString());
                 return null;
             }
             
         }
     	
    	
    	
     	@Override
     	protected void onPreExecute(){
     		
     	}
     	
 		@Override
 		protected ArrayList doInBackground(String... poi) {
 			ArrayList data = null;
 			try{
 				data = sendPOIData(poi[0]);
 			}
 			catch(Exception e){
 				Log.d("Background Task", e.toString());
 			}
			
			return data;
 		}
     	
 		 protected void onPostExecute(ArrayList data) {
 	         /** Load passed in ArrayList into a simple list adapter */
 			 mInfoAdapter = new ArrayAdapter<String>(infoActivityInstance, R.layout.row, data);
 	 
 	         /** Set the ArrayAdapter to this instance of InfoActivity */
 	         infoActivityInstance.setListAdapter(mInfoAdapter);
 	 
 	         /** Showing a message, on completion of download process */
 	         Toast.makeText(getBaseContext(), "POI data downloaded successfully", Toast.LENGTH_SHORT).show();
 	     }
     }
     
     
     
     
     
 }
