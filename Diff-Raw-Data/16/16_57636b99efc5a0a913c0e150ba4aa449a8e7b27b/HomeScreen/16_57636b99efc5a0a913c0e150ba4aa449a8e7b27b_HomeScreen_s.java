 package com.example.ucrinstagram;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.example.ucrinstagram.Models.Photo;
 import com.example.ucrinstagram.Models.User;
 
 public class HomeScreen extends Activity {
 	String caption=null;
 	String link1="";
 	String link2="";
 	public static String username= Login.username.toLowerCase().replaceAll("\\s","");
 	InputStream is;
     User user1;
 
     ImageView[] image;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_screen);
 		user1 = new User(username);
 		Photo[] allThisUserPhotos = user1.getPhotos();
 		image = new ImageView[allThisUserPhotos.length];
 		for (int i = allThisUserPhotos.length-1; i >=0;  i --){
 			image[i] = new ImageView(this);
 	        image[i].setImageResource(R.drawable.ic_launcher);
	        image[i].setAdjustViewBounds(true);
 			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,600); 
	        image[i].setLayoutParams(lp);
 	        LinearLayout linlay = (LinearLayout) findViewById(R.id.linearLayoutWithLotofContent);
 	        linlay.addView(image[i]);
 	        
 			System.out.println(allThisUserPhotos[i].path+'/'+allThisUserPhotos[i].filename);
 			new DownloadImageTask(image[i]).execute(allThisUserPhotos[i].path+'/'+allThisUserPhotos[i].filename);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_home_screen, menu);
 		return true;
 	}
 
     public void explore(View view){
     	Intent intent = new Intent(this, Explore.class);
     	startActivity(intent);
     }
 
     public void camera(View view){
     	Intent intent = new Intent(this, Camera.class);
     	startActivity(intent);
     }
 
     public void profile(View view){
     	Intent intent = new Intent(this, Profile.class);
     	startActivity(intent);
     }
 
  
 
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 		  ImageView bmImage;
 
 		  public DownloadImageTask(ImageView bmImage) {
 		      this.bmImage = bmImage;
 		  }
 
 		  protected Bitmap doInBackground(String... urls) {
 		      String urldisplay = urls[0];
 		      Bitmap mIcon11 = null;
 		      try {
 		  		BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inSampleSize = 3;
 		        InputStream in = new java.net.URL(urldisplay).openStream();
 		        mIcon11 = BitmapFactory.decodeStream(in,null,options); 
 		      } catch (Exception e) {
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
