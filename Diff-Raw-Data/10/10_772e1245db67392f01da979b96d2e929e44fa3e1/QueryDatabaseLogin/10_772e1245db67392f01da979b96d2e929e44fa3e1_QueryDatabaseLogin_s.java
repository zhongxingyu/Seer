 package com.example.taximap;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.util.Log;
 
 
 public class QueryDatabaseLogin  extends AsyncTask<String, Void, Integer>{
 	int puid=-1;
 	Context context;
 	
 	public QueryDatabaseLogin(Context context) {
         this.context = context;
     }
 	
 	protected Integer doInBackground(String... username) {
 		Integer uid=0;
 		String line;
 		//the data to send
 		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("username",username[0]));
 		nameValuePairs.add(new BasicNameValuePair("password",username[1]));
 		 
 		//http post
 		try{
 		        HttpClient httpclient = new DefaultHttpClient();
 		        HttpPost httppost = new HttpPost("http://ec2-23-22-121-122.compute-1.amazonaws.com/login.php");
 		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 		        HttpResponse response = httpclient.execute(httppost);		        	        
 		        HttpEntity entity = response.getEntity();
 		        InputStream is = entity.getContent();
 		        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
 		        line = reader.readLine();
 		        uid = Integer.parseInt(line);
 		}catch(Exception e){
 		        Log.e("log_tag", "Error in http connection "+e.toString());
 		}
 		
 		return uid;
 	}
 	
 	protected void onPostExecute(Integer result) {
 		if(result>0){           
             new AlertDialog.Builder(context)
     		.setTitle("Message")
     		.setMessage("Login succeeded!")
     		.setNeutralButton("Go back to login", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {}
     		})
     		.show();
             
         } else { 
         	new AlertDialog.Builder(context)
     		.setTitle("Error")
     		.setMessage("Login failed")
     		.setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {}
     		})
     		.show();
         }
     }
 
 }
