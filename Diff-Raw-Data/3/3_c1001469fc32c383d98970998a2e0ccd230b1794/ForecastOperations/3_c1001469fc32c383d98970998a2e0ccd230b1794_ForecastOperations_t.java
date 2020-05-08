 /*
  * project		WiseWeather
  * package		TweetBuilder
  * author		Russell Carlin
  * date			Apr 22, 2013
  */
 package forcastBuilder;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.recarlin.wiseweather.MainActivity;
 import com.recarlin.wiseweather.R;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ForecastOperations {
 	
 	public static Bitmap myBitmap;
 	
 	public static Context context = MainActivity.getAppContext();
 //This reads the response from the site, goes through the JSONArray, picks out what info I want,
 //builds views for the images and text, then puts them into an array.
 	public static ArrayList<View> readForcastJSON(JSONArray results) {
 		ArrayList<View> post = new ArrayList<View>();
 		try {
 			for(int i = 0; i < results.length(); i++){
 				String title, forcast, precipitation, iconURL;
 				JSONObject currentPeriod = results.getJSONObject(i);
 				title = currentPeriod.getString("title");
 			    forcast = currentPeriod.getString("fcttext");
 			    precipitation = currentPeriod.getString("pop");
 			    iconURL = currentPeriod.getString("icon_url");
 			    ImageView imageV = new ImageView(context);
 			    ForecastOperations.getImage gi = new ForecastOperations.getImage();
 			    gi.execute(iconURL);
 			    
 			    imageV.setImageBitmap(myBitmap);
 			    TextView textV = newTextView((title + " - " + precipitation + "% Chance of Precipitation\r\n" + forcast + "\r\n"), context);
 			    
 			    imageV.setMinimumWidth(100);
 			    imageV.setMinimumHeight(100);
 			    
 			    textV.setTextAppearance(context, R.style.Forecast);
			    textV.setWidth(1000);
 			    
 			    post.add(imageV);
 				post.add(textV);
 			}
 		} catch (JSONException e) {
 			Log.e("READJSON", "Error Reading JSON!");
 		}
 		return post;
 	}
 //This builds a new ImageView for the bitmaps.
 	public static ImageView newImageView(Bitmap post, Context context) {
 		ImageView imageView = new ImageView(context);
 		imageView.setImageBitmap(post);
 		return imageView;
 	}
 //This builds a new TextView for the strings.
 	public static TextView newTextView(String post, Context context) {
 		TextView textView = new TextView(context);
 		textView.setText(post);
 		return textView;
 	}
 //This is used to get the images from the site in as a AsyncTask, because you can't do these calls on the main thread.
 	private static class getImage extends AsyncTask<String, Void, Bitmap>{
 		@Override
 		protected Bitmap doInBackground(String...strings) {
 			Bitmap response = null;
 			for (String string: strings) {
 				try {
 					URL url = new URL(string);
 		            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		            connection.setDoInput(true);
 		            connection.connect();
 		            InputStream input = connection.getInputStream();
 		            response = BitmapFactory.decodeStream(input);
 				} catch (IOException e) {
 					Log.e("IOException", "ERMAGERD");
 				}
 				
 			}
 			return response;
 		}
 		@Override
 		protected void onPostExecute(Bitmap result) {
 			myBitmap = result;
 		}
 	}
 }
