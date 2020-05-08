 package com.laithnurie.baka;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Color;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class NationalTrains extends Activity {
 
 	String fromStation;
 	String toStation;
 	String trainUrlDate;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_national_trains);
         
         Bundle extras = getIntent().getExtras();
         fromStation = extras.getString("from");
         toStation = extras.getString("to");
 
        trainUrlDate = "http://ojp.nationalrail.co.uk/service/ldb/liveTrainsJson?departing=true&liveTrainsFrom="+fromStation+"&liveTrainsTo=&serviceId=";
         new loadTubeJson().execute();
     }
 
     public class loadTubeJson extends AsyncTask<String, Integer, String> {
       	
       	ProgressDialog pd;
 
       	@Override
       	protected void onPreExecute() {
       		
       		pd = new ProgressDialog(NationalTrains.this);
       		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
       		pd.setCanceledOnTouchOutside(false);
       		pd.setMax(100);
       		pd.show();
       		
       	}
       	
       	@Override
       	protected String doInBackground(final String... params) {
       		StringBuilder builder = new StringBuilder();
       	    HttpClient client = new DefaultHttpClient();
       	    HttpGet httpGet = new HttpGet(trainUrlDate);
       	    
       	    try {
       	    	HttpResponse response = client.execute(httpGet);
       	    	StatusLine statusLine = response.getStatusLine();
       	    	int statusCode = statusLine.getStatusCode();
       	    	if (statusCode == 200) {
     	  	        HttpEntity entity = response.getEntity();
     	  	        InputStream content = entity.getContent();
     	  	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
     	  	        String line;
     	  	        
     	  	        while ((line = reader.readLine()) != null) {
     	  	          builder.append(line);
     	  	        }
       	    	} 
       	    	else {
       	    		Log.e(NationalTrains.class.toString(), "Failed to download file");
       	    	}
       	    } 
       	    catch (ClientProtocolException e) {
       	      e.printStackTrace();
       	    } 
       	    catch (IOException e) {
       	      e.printStackTrace();
       	    }
       	    return builder.toString();
       	} 
       	
       	
       	@Override
       	protected void onProgressUpdate(Integer... progress) {
       		pd.incrementProgressBy(progress[0]);	
       	}
       	
       	@Override
       	protected void onPostExecute(String result) {
       		
       		TableRow  tr1;  
 	      	//TableRow  tr2;    
 	      	TextView statusTV;
 	      	TextView timeTV;
 	      	TextView destTV;
 	
 	      	TableLayout tl = (TableLayout)findViewById(R.id.trainsTable);
       		
 	      	try {
       			
       			JSONObject trainJson = new JSONObject(result);
       	        JSONArray linesList = trainJson.getJSONArray("trains");
     	        Log.v("trains", "lineList:" +linesList.length());
 
       	      	for (int i = 0; i < linesList.length(); i++) {
       	    	  
 	      	        JSONArray line = linesList.getJSONArray(i);
 	      	        String time = line.getString(1);
 	      	        String dest = line.getString(2).replace("amp;", "");
 	      	        String status = line.getString(3).replace("&lt;br/&gt;", "");
 	      	        
 	      	        int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 1, getResources().getDisplayMetrics());
 	    	        
 	    	        tr1 = (TableRow) new TableRow(getApplicationContext());
 	    	        tr1.setGravity(Gravity.CENTER);
 	    	        tr1.setBackgroundColor(getResources().getColor(R.color.status));
 	    	        
 	    	        timeTV=new TextView(getApplicationContext());
 	    	        timeTV.setPadding(10*dip, 10*dip, 10*dip, 10*dip);
 	    	        timeTV.setText(time);
 	    	        timeTV.setTextColor(getResources().getColor(R.color.piccadily));
 	    	        timeTV.setWidth(100*dip);
 	    	        
 	    	        statusTV=new TextView(getApplicationContext());
 	    	        statusTV.setPadding(10*dip, 10*dip, 10*dip, 10*dip);
 	    	        statusTV.setText(status);
 	    	        statusTV.setTextColor(getResources().getColor(R.color.piccadily));
 	    	        statusTV.setWidth(100*dip);
 	    	            	        
 	    	        destTV=new TextView(getApplicationContext());
 	    	        destTV.setPadding(10*dip, 10*dip, 10*dip, 10*dip);
 	    	        destTV.setText(dest);
 	    	        destTV.setTextColor(getResources().getColor(R.color.piccadily));
 	    	        destTV.setWidth(100*dip);
 	    	        
 	    	        tr1.addView(timeTV);
 	    	        tr1.addView(statusTV);
 	    	        tr1.addView(destTV);
 	    	        
 	    	        View v = new View(getApplicationContext());
 	    	        
 	    	        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
 	    	        v.setBackgroundColor(Color.rgb(51, 51, 51));
 	    	        
 	    	        tl.addView(tr1);
 	    	        tl.addView(v);
       	      	}
 	      	} 
       		catch (Exception e) {
       			e.printStackTrace();
 	      	}
 	      	pd.dismiss();
       	}
     }
 }
