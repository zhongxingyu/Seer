 package it.giacomos.android.osmer.pro.widgets.map.report.network;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class ReportUpdateTask extends AsyncTask<String, Integer, String> 
 {
 	private static final String CLI = "afe0983der38819073rxc1900lksjd";
 	private String mErrorMsg;
 	private ReportUpdateTaskListener mReportUpdateTaskListener;
 	Location mLastLocation;
 	String mAndroidId;
 	
 	public ReportUpdateTask(ReportUpdateTaskListener reportUpdateTaskListener,
 			Location l, String androidId)
 	{
 		super();
 		mReportUpdateTaskListener = reportUpdateTaskListener;
 		mLastLocation = l;
 		mAndroidId = androidId;
 	}
 	
 	@Override
 	public void onPostExecute(String doc)
 	{
 		mReportUpdateTaskListener.onReportUpdateTaskComplete(!mErrorMsg.isEmpty(), doc);
 	}
 	
 	@Override
 	public void onCancelled(String doc)
 	{
 		
 	}
 	
 	public String getError()
 	{
 		return mErrorMsg;
 	}
 	
 	@Override
 	protected String doInBackground(String... urls) 
 	{
 		String document = "";
 		Location l = mLastLocation;
 		if(l == null){
 			mErrorMsg = "Location unavailable";
 			return "";
 		}
 		mErrorMsg = "";
 		HttpClient httpClient = new DefaultHttpClient();
         HttpPost request = new HttpPost(urls[0]);
         List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
         postParameters.add(new BasicNameValuePair("cli", CLI));
         postParameters.add(new BasicNameValuePair("d", mAndroidId));
         postParameters.add(new BasicNameValuePair("la", String.valueOf(l.getLatitude())));
         postParameters.add(new BasicNameValuePair("lo", String.valueOf(l.getLongitude())));
        // postParameters.add(new BasicNameValuePair("loc", mLocality));
         UrlEncodedFormEntity form;
 		try {
 			form = new UrlEncodedFormEntity(postParameters);
 	        request.setEntity(form);
 	        HttpResponse response = httpClient.execute(request);
 	        StatusLine statusLine = response.getStatusLine();
 	        if(statusLine.getStatusCode() < 200 || statusLine.getStatusCode() >= 300)
 	        	mErrorMsg = statusLine.getReasonPhrase();
 	        else if(statusLine.getStatusCode() < 0)
 	        	mErrorMsg = "Server error";
 	        /* check the echo result */
 	        HttpEntity entity = response.getEntity();
 	        document = EntityUtils.toString(entity);
 		} 
 		catch (UnsupportedEncodingException e) 
 		{
 			mErrorMsg = e.getLocalizedMessage();
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			mErrorMsg = e.getLocalizedMessage();
 			e.printStackTrace();
 		} catch (IOException e) {
 			mErrorMsg = e.getLocalizedMessage();
 			e.printStackTrace();
 		}
 		return document;
 	}
 
 	
 }
