 package com.chiller.bme.posture.tasks;
 
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 
 public class AsyncTaskPostStats extends AsyncTask<String, Void, String> {
 
	public interface UploadStatsCompleteListener {
 		   public void onTaskComplete(String aResult);
 		   public void onError(String aError);
 	}
 	
 	private Context context = null;
 	private ProgressDialog progressDialog = null;
	private UploadStatsCompleteListener listener;
 	private String error = null;
 
	public AsyncTaskPostStats(Context context, UploadStatsCompleteListener aListener) {
 	    this.context = context; 
 	    listener = aListener;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		progressDialog = new ProgressDialog(this.context);
	    progressDialog.setMessage("Please wait...");
 	    progressDialog.show();
 	}
 	
 	@Override
 	protected String doInBackground(String... params) {
 		 byte[] result = null;
 	        String str = "";
 	        HttpClient client = new DefaultHttpClient();
 	        HttpPost post = new HttpPost(params[0]);// in this case, params[0] is URL
 	        try {
 	            // set up post data
 	            ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
 	            nameValuePair.add(new BasicNameValuePair("data", params[1]));
 	            post.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
 	            HttpResponse response = client.execute(post);
 	            StatusLine statusLine = response.getStatusLine();
 	            if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
 	                result = EntityUtils.toByteArray(response.getEntity());
 	                str = new String(result, "UTF-8");
 	            }
 	        }
 	        catch (UnsupportedEncodingException e) {
 	            e.printStackTrace();
 	        }
 	        catch (Exception e) {
 	        }
 	        return str;
 	}
 	
 	@Override
 	protected void onPostExecute(String result) {
 	    progressDialog.dismiss();
 	    if (error != null) {
 	    	listener.onError(error);
 	    }
 	    else {
 	    	listener.onTaskComplete(result);
 	    }
 	} 
 }
