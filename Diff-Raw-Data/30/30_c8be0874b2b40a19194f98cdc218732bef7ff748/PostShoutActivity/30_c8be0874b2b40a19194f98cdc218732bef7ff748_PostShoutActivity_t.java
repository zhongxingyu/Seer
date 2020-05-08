 package com.example.soapbox;
 
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.example.soapbox.PostShoutTask.PostShoutCallbackInterface;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
import android.widget.Button;
 import android.widget.EditText;
 
 public class PostShoutActivity extends Activity implements PostShoutCallbackInterface{
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_post_shout);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.post_shout, menu);
 		return true;
 	}
 	//called when cancel button is pressed
 	public void cancel(View view)
 	{
 		this.finish();
 	}
 	
 	//called when the post button is pressed
 	public void postShout(View view)
 	{
 		
 		String url ="http://acx0.dyndns.org:3000/shouts.json";
 		
 		SharedPreferences prefs = this.getSharedPreferences(
 			      "com.example.soapbox", Context.MODE_PRIVATE);
 
 		//url += "?auth_token="+prefs.getString(LoginTask.AUTH, "");
 		
 		String name = (prefs.getString(LoginTask.NAME, ""));
 		
 		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 		
 		
 		EditText editText = (EditText) findViewById(R.id.shoutbox);
 		//params.add(new BasicNameValuePair("?auth_token", prefs.getString(LoginTask.AUTH,"")));
 		System.out.println(name);
 		//params.add(new BasicNameValuePair("auth_token", prefs.getString(LoginTask.AUTH, "")));
 
 		
 		String message = editText.getText().toString();
 		
 		if (message.isEmpty())
 		{
 			AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
 			
 			dlgAlert.setTitle("Invalid Shout");
 			dlgAlert.setMessage("Shout cannot be empty.");
 			dlgAlert.setPositiveButton("Ok",
 				    new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int which) {
 				          //dismiss the dialog  
 				        }
 				    });
 			dlgAlert.setCancelable(true);
 			dlgAlert.create().show();
 			return;
 		} 
 		else if (message.length() >= 140)
 		{
 			AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
 			
 			dlgAlert.setTitle("Invalid Shout");
 			dlgAlert.setMessage("Shout cannot be greater than 140 characters.");
 			dlgAlert.setPositiveButton("Ok",
 				    new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int which) {
 				          //dismiss the dialog  
 				        }
 				    });
 			dlgAlert.setCancelable(true);
 			dlgAlert.create().show();
 			return;
 		}
 		params.add(new BasicNameValuePair(PostShoutTask.AUTH, prefs.getString(LoginTask.AUTH, "")));
 		params.add(new BasicNameValuePair(PostShoutTask.MESSAGE, editText.getText().toString()));
 		params.add(new BasicNameValuePair(PostShoutTask.NAME, name));
 		params.add(new BasicNameValuePair(PostShoutTask.TAG,"toronto"));
 		params.add(new BasicNameValuePair(PostShoutTask.NAME, name));
 		
 		
 		PostShoutTask task = new PostShoutTask(url, PostShoutTask.POST, params, this, this);
 		task.execute();
 		
 	}
 
 	@Override
 	public void onRequestComplete(JSONObject result) {
 		// TODO Auto-generated method stub
 		System.out.println(result);
 	}
 }
