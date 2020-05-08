 package com.smartcommunities.xdengue.net;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 import com.smartcommunities.xdengue.R;
 import com.smartcommunities.xdengue.XdenguePreferences;
 
 public class RegisterTask extends AsyncTask<String, Void, String> {
 	private Context context;
 	private Activity callingActivity;
 	private ProgressDialog dialog;
 	private UrlEncodedFormEntity registerData;
 
 	public RegisterTask(Context context, Activity callingActivity, UrlEncodedFormEntity registerData) {
 		this.context = context;
 		this.callingActivity = callingActivity;
 		this.dialog = new ProgressDialog(context);
 		this.registerData = registerData;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		this.dialog.setMessage("Registering");
 		this.dialog.show();
 	}
 
 	@Override
 	protected String doInBackground(String... urls) {
 		String response = "";
 		for (String url : urls) {
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpPost httpPost = new HttpPost(url);
 			httpPost.setEntity(registerData);
 			try {
 				HttpResponse execute = client.execute(httpPost);
 				InputStream content = execute.getEntity().getContent();
 
 				BufferedReader buffer = new BufferedReader(
 						new InputStreamReader(content));
 				String s = "";
 				while ((s = buffer.readLine()) != null) {
 					response += s;
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return response;
 	}
 
 	@Override
 	protected void onPostExecute(String result) {
 		if (this.dialog.isShowing()) {
 			this.dialog.dismiss();
 		}
 		System.out.println(result);
 		try {
 			JSONObject json = new JSONObject(result);
 			if (json.getBoolean("HasValidationErrors")) {
				XdenguePreferences.getEditor(context).clear().commit();
 				Toast.makeText(context, R.string.validation_error,
 						Toast.LENGTH_LONG).show();
 			} else {
 				int isnewRegister = json.getInt("RegistrationStatus");
 		        if(isnewRegister==0){
 		        	System.out.println("NEw Registration");
 		        	
 		        	String emailAddress = XdenguePreferences.readString(context, XdenguePreferences.EMAIL, "");
 		    		String passwordString = XdenguePreferences.readString(context, XdenguePreferences.PASS, "");
 		    		
 		    		if(emailAddress.length()!=0 && passwordString.length()!=0){
 		    	        List<NameValuePair> params = new LinkedList<NameValuePair>();
 		    			String url = "http://www.x-dengue.com/mobilev1/FullCustomerData";
 		    			params.add(new BasicNameValuePair("emailAddress", emailAddress));
 		    			params.add(new BasicNameValuePair("password", passwordString));
 		    			String paramString = URLEncodedUtils.format(params, "utf-8");
 		    			url += "?" + paramString;
 		    			LoginTask loginTask = new LoginTask(context,callingActivity);
 		    			loginTask.execute(url);
 		    		}
 		        }
 		        else if(isnewRegister == 1){
		        	XdenguePreferences.getEditor(context).clear().commit();
 		        	System.out.println(R.string.oldUser_error);
 		        	Toast.makeText(context, R.string.oldUser_error,
 							Toast.LENGTH_LONG).show();
 		        }
 			}
 		} catch (JSONException e) {
			XdenguePreferences.getEditor(context).clear().commit();
 			e.printStackTrace();
 		}
 	}
 
 }
