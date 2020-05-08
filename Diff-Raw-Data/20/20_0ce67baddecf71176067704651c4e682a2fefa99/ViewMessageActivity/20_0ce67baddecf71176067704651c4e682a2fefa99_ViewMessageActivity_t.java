 package com.spiteful.cipher.ui;
 
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import net.minidev.json.JSONObject;
 import net.minidev.json.JSONValue;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.HttpClient;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.BufferedHttpEntity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.ImageView;
import android.widget.Toast;
 
 import com.spiteful.cipher.android.R;
 import com.spiteful.cipher.service.GetMessageService;
 import com.spiteful.cipher.service.WebActionCallback;
 import com.spiteful.cipher.Constants;
 import com.spiteful.cipher.android.R;
 import com.spiteful.cipher.service.VerifyService;
 import com.spiteful.cipher.service.WebActionCallback;
 
 public class ViewMessageActivity extends Activity{
 	private static final String TAG = ViewMessageActivity.class.toString();
 	private ImageView messageImage;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_message);
 
 		messageImage = (ImageView) this.findViewById(R.id.img_message);
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		int id = Integer.parseInt(prefs.getString(Constants.TEAMID_PREF, "0"));
 		int pin = Integer.parseInt(prefs.getString(Constants.TEAMPIN_PREF, "0"));
		
 		GetMessageService service = new GetMessageService(messageHandler, id, pin);
 		service.performAction();
 	}
 
 	private WebActionCallback messageHandler = new WebActionCallback() {
 		@Override
 		public void onCompleted(JSONObject json) {
			if(json == null) {
				Toast.makeText(ViewMessageActivity.this, "Unable to contact server", Toast.LENGTH_LONG).show();
				return;
			}

 			if(json.containsKey(Constants.ERROR_KEY)) {
				Log.e(TAG, "Couldn't get message: Error " + json.get(Constants.ERROR_KEY));		
				Toast.makeText(ViewMessageActivity.this, "Couldn't get QR Code. Error " + json.get(Constants.ERROR_KEY), Toast.LENGTH_LONG).show();
 				return;
 			}
 
 			String url = (String) json.get(Constants.URL_KEY);
 			new DownloadImageTask(messageImage).execute(new String[] {url});
 		}
 	};
 
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 	    ImageView bmImage;
 
 	    public DownloadImageTask(ImageView bmImage) {
 	        this.bmImage = bmImage;
 	    }
 
 	    protected Bitmap doInBackground(String... urls) {
 			try {
 				URL url = new URL(urls[0]);
 	            HttpGet httpRequest = null;
 
 	            httpRequest = new HttpGet(url.toURI());
 
 	            HttpClient httpclient = new DefaultHttpClient();
 	            HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
 
 	            HttpEntity entity = response.getEntity();
 	            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
 	            InputStream input = bufHttpEntity.getContent();
 
 	            Bitmap bitmap = BitmapFactory.decodeStream(input);
 	           	return bitmap;
 	        } catch (MalformedURLException e) {
 	            Log.e(TAG, "bad url", e);
 	        } catch (Exception e) {
 	            Log.e(TAG, "io error", e);
 	        }
 	    	return null;
 	    }
 
 	    protected void onPostExecute(Bitmap result) {
 	        bmImage.setImageBitmap(result);
 	    }
 	}
 }
