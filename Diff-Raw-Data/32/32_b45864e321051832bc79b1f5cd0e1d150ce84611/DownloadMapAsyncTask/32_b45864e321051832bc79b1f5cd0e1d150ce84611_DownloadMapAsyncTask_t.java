 package com.hackathon.fshow.module;
 
 import java.io.ByteArrayOutputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.hackathon.fshow.ObjectDraggingRenderer;
 
 public class DownloadMapAsyncTask extends AsyncTask<String, Integer, String> {
 	private static final String TAG = "DownloadMapAsyncTask";
 	private ObjectDraggingRenderer mRenderer;
 	private ProgressDialog dialog = null;
 	private Context mContext = null;
 	//@see http://techbooster.org/android/application/1645/
 	private String mMapAccessUrl = "http://133.242.168.69/team_h/fshow/develop/fashion/MapAccess.php";
 	
 	public DownloadMapAsyncTask(Context context, ObjectDraggingRenderer renderer) {
 		this.mRenderer = renderer;
 		this.mContext = context;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		dialog = new ProgressDialog(mContext);
 		dialog.setMessage("Loading...");
 		dialog.show();
 	}
 	
 	@Override
 	protected void onPostExecute(String result) {
 		if (result != null) {
 			dialog.setMessage("loading completed");
 		} else {
 			dialog.setMessage("loading failure");
 		}
 		dialog.dismiss();
 		super.onPostExecute(result);
 	}
 	
 	@Override
 	protected String doInBackground(String... params) {
		String url = mMapAccessUrl;
 		if (params != null && params.length == 1) {
 			Log.v(TAG, "@@@ userid="+params[0]);
			url = mMapAccessUrl + "?userid="+params[0];
 		}
 		String jsonResult = null;
 		HttpClient httpClient = new DefaultHttpClient();
 		 
		StringBuilder uri = new StringBuilder(url);
 		HttpGet request = new HttpGet(uri.toString());
 		HttpResponse httpResponse = null;
 		 
 		try {
 		    httpResponse = httpClient.execute(request);
 		} catch (Exception e) {
 		    Log.d("JSONSampleActivity", "Error Execute");
 		    return jsonResult;
 		}
 		 
 		int status = httpResponse.getStatusLine().getStatusCode();
 		 
 		if (HttpStatus.SC_OK == status) {
 		    try {
 		        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		        httpResponse.getEntity().writeTo(outputStream);
 		        jsonResult = outputStream.toString(); // JSONデータ
 		        if (jsonResult != null) {
 			        //Log.v(TAG, "@@@ jsonresult:"+jsonResult);
 			        Log.v(TAG, "@@@ jsonresult OK");
 			        JSONObject rootObject = new JSONObject(jsonResult);
 					JSONArray items = rootObject.getJSONArray("items");
 					mRenderer.setData(items);
					mRenderer.refresh();
 		        } 
 		    } catch (Exception e) {
 		          Log.d("JSONSampleActivity", "Error");
 		          mRenderer.refresh();
 		    }
 		} else {
 		    Log.d("JSONSampleActivity", "Status" + status);
 		    return jsonResult;
 		}
 		return jsonResult;
 	}
 
 }
