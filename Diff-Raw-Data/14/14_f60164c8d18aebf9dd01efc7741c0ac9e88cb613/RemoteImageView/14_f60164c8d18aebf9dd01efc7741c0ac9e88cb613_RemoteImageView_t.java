 package edu.illinois.geosight.maps;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
import edu.illinois.geosight.R;

 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 
 public class RemoteImageView extends LinearLayout {
 	protected ProgressBar bar;
 	protected ImageView image;
 	
 	public RemoteImageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		bar = new ProgressBar(context);
 		bar.setIndeterminate(true);
 		
 		image = new ImageView(context, attrs);
 		image.setVisibility(GONE);
 		this.addView(bar);
 		this.addView(image);
 	}
 	
 	public void setImageUrl(URL url){
 		(new DownloadImageTask()).execute(url);
 	}
 	
 	 private class DownloadImageTask extends AsyncTask<URL, Integer, Bitmap > {
 	     protected Bitmap doInBackground(URL... urls) {
 	    	URL url = urls[0];
 	    	
 	    	try{
 				Log.v("IMaGE", "Setting image: " + url.toExternalForm() );
 		        HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
 		        conn.setDoInput(true);
 		        conn.connect();  
 		        InputStream is = conn.getInputStream();	
 		        Bitmap bm = BitmapFactory.decodeStream(is);  
 		        is.close();  
 		        return bm;
 			} catch (IOException ex){
 				ex.printStackTrace();
 			}
 			
 			return null;
 	     }
 	     
 	     protected void onPostExecute(Bitmap bmp) {
 	    	 bar.setVisibility(GONE);
	    	 image.setVisibility(VISIBLE);
	    	 
	    	 if( bmp == null){
	    		 image.setImageResource(R.drawable.error_icon);
	    	 } else {
	    		 image.setImageBitmap(bmp);
	    	 }
 	     }
 	 }
 
 }
