 package com.numbers.domain;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 
 public class ImageService extends AsyncTask<URL, Void, Bitmap> {
 
 	
 	private URL serverURL;
 	private ImageView view;
 
 	public ImageService(String serverURL, ImageView view) {
 		try {
 			this.serverURL = new URL(serverURL);
 		} catch (MalformedURLException e) {
 			throw new IllegalArgumentException("Malformed server URL.");
 		}
 		this.view = view; 
 	}
 
 	
 	@Override
 	protected Bitmap doInBackground(URL... params) {
 		Log.d("Numbers", "Language: Background loading");
 		
 		if(params == null) {
 			throw new IllegalArgumentException("URL can not be null.");
 		}
 		
 		Bitmap image = null;
 		try {
 			BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inJustDecodeBounds = true;
 			InputStream is = params[0].openStream();
 			if(is == null)
 				Log.d("Numbers", "is is Null");
 			
			image = BitmapFactory.decodeStream(is);
 			
 			if(image == null) {
 				Log.d("Numbers", "image is null.. why????");
 			}
 			
 		} catch (MalformedURLException e) {
 			Log.d("Numbers", "malformed url");
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.d("Numbers", "IOExc;etop");
 			e.printStackTrace();
 		}
 
 		return image;
 	}
 
 	public void loadImage() {
 		Log.d("Numbers", "loading image");
 		this.execute(serverURL);
 	}
 	
 	protected void onPostExecute (Bitmap result) {
 		Log.d("Numbers", "post result called ");

		if(result != null) {
			view.setImageBitmap(result);
		} else {
 			Log.d("Numbers", "result foi NULO PORRA");
 		}
 	}
 	
 	public static ImageService newInstance(String serverURL, ImageView view) {
 		return new ImageService(serverURL, view);
 	}
 
 }
