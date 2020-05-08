 package edu.cui.wineapp;
 
import java.io.IOException;
 import java.io.InputStream;
import java.net.URL;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class WineInfo extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_wine_info);
 		
 		Intent i = getIntent();
 		Wine currentWine = (Wine)i.getSerializableExtra("passedWine");
 		
 		currentWine.stripJSON();
 		
 		TextView name = (TextView) findViewById(R.id.textView1);
 		name.setText(currentWine.getName());	
 		
 		TextView varietal = (TextView) findViewById(R.id.TextView03);
 		varietal.setText(currentWine.getVarietal());		
 		
 		//TextView label_url = (TextView) findViewById(R.id.TextView02);
 		//label_url.setText(currentWine.getLabel_URL());	
 		
 		TextView avin = (TextView) findViewById(R.id.TextView05);
 		avin.setText(currentWine.getAvin());	
 		
 		TextView rating = (TextView) findViewById(R.id.TextView04);
 		rating.setText(currentWine.getRating());	
 		
 		TextView country = (TextView) findViewById(R.id.TextView01);
 		country.setText(currentWine.getCountry());
 		
 		new DownloadImageTask((ImageView) findViewById(R.id.image))
         .execute(currentWine.getLabel_URL());
		
		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.wine_info, menu);
 		
 		return true;
 	}
 
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 		  ImageView bmImage;
 
		  public DownloadImageTask(ImageView bmImage) {this.bmImage = bmImage;}
 
 		  protected Bitmap doInBackground(String... urls) {
 		      String urldisplay = urls[0];
 		      Bitmap mIcon11 = null;
		      
 		      try {
		    	  InputStream in = new URL(urldisplay).openStream();
			      mIcon11 = BitmapFactory.decodeStream(in);} catch (Exception e) {
			    	  try {
			    		   	InputStream in = getAssets().open("gillsp.jpg");
			    		   	mIcon11 = BitmapFactory.decodeStream(in);} catch (IOException e1){
			    	  		   	e1.printStackTrace();}
		          			Log.e("Error", e.getMessage());
		          			e.printStackTrace();
 		      }
		      
 		      return mIcon11;
 		  }
 
 		  protected void onPostExecute(Bitmap result) {
 		      bmImage.setImageBitmap(result);
 		  }
 		}
 
 }
