 package com.example.taximap.db;
 
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
 import org.json.JSONObject;
 
 import com.example.taximap.map.MapViewActivity;
 import com.example.taximap.map.TabLayoutActivity;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.sax.StartElementListener;
 import android.util.Log;
 import android.widget.Toast;
 
 
 public class QueryDatabaseLogin  extends AsyncTask<String, Void, Integer[]>{
 	int puid=-1;
 	Context context;
 	
 	public QueryDatabaseLogin(Context context) {
         this.context = context;
     }
 	
 	protected Integer[] doInBackground(String... username) {
 		Integer[] return_result = new Integer[2];
 		String line;
 		//the data to send
 		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("username",username[0]));
 		nameValuePairs.add(new BasicNameValuePair("password",username[1]));
 		 
 		//try connecting to the server
 		try{
 		        HttpClient httpclient = new DefaultHttpClient();
 		        HttpPost httppost = new HttpPost("http://ec2-23-22-121-122.compute-1.amazonaws.com/login.php");
 		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 		        HttpResponse response = httpclient.execute(httppost);		  
 		        HttpEntity entity = response.getEntity();
 		        InputStream is = entity.getContent();
 		        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
 		        line = reader.readLine();
 		        JSONObject json_login = new JSONObject(line);
 		        String  uid_string = json_login.getString("uid");		       
 		        String type_string = json_login.getString("type");
 		        return_result[0] = Integer.valueOf(uid_string);
		        return_result[1] = Integer.valueOf(type_string);		        
 		}catch(Exception e){
 		        Log.e("log_tag", "Error in http connection "+e.toString());
 		        return_result[0] = -5;
 		}		
 		return return_result;
 	}
 	
 	protected void onPostExecute(Integer[] result) {
 		if(result[0]>0){ 
 			 MapViewActivity.uID=result[0].toString();			//wei added
			 if(result[1].toString()=="0"){		//customer login
 		        	MapViewActivity.markerType="driver";
		        }
		        else if(result[1].toString()=="1"){
 		        	MapViewActivity.markerType="customer";
		        }else{
		        	Toast.makeText(context, "Invalid user type", Toast.LENGTH_SHORT).show();
		        }
 	            context.startActivity(new Intent(context,TabLayoutActivity.class));          
         } else if(result[0]==-5){ 
         	new AlertDialog.Builder(context)
     		.setTitle("Error")
     		.setMessage("Trouble Connecting To Database, Check Network Connection")
     		.setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
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
