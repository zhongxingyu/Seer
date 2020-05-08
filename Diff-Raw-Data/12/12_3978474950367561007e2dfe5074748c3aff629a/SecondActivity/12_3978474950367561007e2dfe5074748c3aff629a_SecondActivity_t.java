package ru.sixteenfourteen.translator;
 
 import java.io.InputStream;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 @SuppressLint("NewApi")
 public class SecondActivity extends Activity {
 	Context context;
 	private ImageView imgView;
 	PicturesFinder finder = new PicturesFinder();
 	String url, fun = "http://airs-design.ru/meme/imgs/efdf2591.jpeg";
 
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(SecondActivity.this, MainActivity.class);
 		startActivity(intent);
 		finish();
 		return;
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
 				InputStream in = new java.net.URL(urldisplay).openStream();
 				mIcon11 = BitmapFactory.decodeStream(in);
 			} catch (Exception e) {
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 				picSet(bmImage);
 			}
 			if (mIcon11 == null) {
 				picSet(bmImage);
 			}
 			return mIcon11;
 		}
 
 		protected void onPostExecute(Bitmap result) {
 			bmImage.setImageBitmap(result);
 		}
 	}
 
 	public void picSet (ImageView tmpPic){
 		url = finder.nextURL();
 		if (url == null)
 			url = fun;
 		new DownloadImageTask(tmpPic).execute(url);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.second);
 		context = getApplicationContext();
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 				.permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 
 		// Translation
 		String engWord = getIntent().getExtras().getString("engWord");
 		String ruWord = engWord;
 		TextView initalWord = (TextView) findViewById(R.id.initalWord);
 		initalWord.setText(engWord);
 		try {
			ruWord = new Translator().getTranslate(engWord);
 		} catch (Exception e) {
 			e.printStackTrace();
 			ruWord = "Check your Internet connection";
 		}
 		TextView rusWord = (TextView) findViewById(R.id.rusWord);
 		rusWord.setText(ruWord);
 
 		// image from URL
 		// get images function request
 		// String urls[] = new String[10];
 		// String urls[] = new PicturesFinder().getURLs(engWord, 10);
 
 		finder.setTranslatingWord(engWord);
 
 		imgView = (ImageView) findViewById(R.id.img0);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img1);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img2);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img3);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img4);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img5);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img6);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img7);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img8);
 		picSet(imgView);
 
 		imgView = (ImageView) findViewById(R.id.img9);
 		picSet(imgView);
 	}
 }
