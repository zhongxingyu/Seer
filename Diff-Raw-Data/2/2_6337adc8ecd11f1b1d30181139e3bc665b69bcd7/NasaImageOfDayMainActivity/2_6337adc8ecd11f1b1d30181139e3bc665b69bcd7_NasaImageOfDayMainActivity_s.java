 package com.nasaimageofday;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Typeface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.method.LinkMovementMethod;
 import android.text.style.StyleSpan;
 import android.text.style.URLSpan;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /*
  * @ author : ammar.hassan
  */
 public class NasaImageOfDayMainActivity extends FragmentActivity {
 	private static final String URL = "http://www.nasa.gov/rss/image_of_the_day.rss";
 	private Bitmap image;
 	private boolean showLess = true;
 	private StringBuffer fullDescription = new StringBuffer();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_nasa_image_of_day_main);
 		setTextForButton("Show More", fullDescription);
 		setVisibilityForShowMoreButton(View.INVISIBLE);
 		refresh();
 	}
 
 	private void refresh() {
 		if (isInternetAvailable()) {
 			new UpdateRssAsyncTask(this).execute(URL);
 		} else {
 			DialogFragment fragment = new NoInternetConnectionDialogFragment();
 			fragment.show(getSupportFragmentManager(), "");
 		}
 	}
 
 	public void refreshActivity(View view) {
 		refresh();
 	}
 
 	public void setWallpaper(View view) {
 		new UpdateWallpaperAsyncTask().execute();
 	}
 
 	public void setImage(Bitmap image) {
 		this.image = image;
 	}
 
 	private boolean isInternetAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 		return networkInfo != null && networkInfo.isConnected();
 	}
 
 	private void setVisibilityForShowMoreButton(int visibility) {
 		((Button) findViewById(R.id.imageDescriptionMoreOrLess))
 				.setVisibility(visibility);
 	}
 
 	public void showMoreOrLessText(View view) {
 		showLess = !showLess;
 		if (showLess) {
 			setTextForButton("Show More",
 					getSmallerDescription(fullDescription));
 		} else {
 			setTextForButton("Show Less", fullDescription);
 		}
 	}
 
 	private void setTextForButton(String text, StringBuffer imageDescription) {
 		((Button) findViewById(R.id.imageDescriptionMoreOrLess)).setText(text);
 		((TextView) findViewById(R.id.imageDescription))
 				.setText(imageDescription);
 	}
 
 	private StringBuffer getSmallerDescription(StringBuffer description) {
 		if (showLess) {
 			description = new StringBuffer(
					(description == null || description.length() < 20) ? description
 							: description.substring(0, 20));
 		}
 		return description;
 	}
 
 	private class UpdateWallpaperAsyncTask extends
 			AsyncTask<Context, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Context... params) {
 			WallpaperManager wallpaperManager = WallpaperManager
 					.getInstance(getApplicationContext());
 			try {
 				wallpaperManager.setBitmap(image);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		public void onPostExecute(Void obj) {
 			Toast.makeText(getApplicationContext(), "Wallpaper set",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.nasa_image_of_day_main, menu);
 		return true;
 	}
 
 	private class UpdateRssAsyncTask extends
 			AsyncTask<String, Integer, RssHandler> {
 		ProgressDialog progressDialog;
 
 		public UpdateRssAsyncTask(Activity activity) {
 			progressDialog = new ProgressDialog(activity);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setTitle("Loading..");
 			progressDialog.show();
 		}
 
 		@Override
 		protected RssHandler doInBackground(String... params) {
 			RssHandler rssHandler = new RssHandler(params[0]);
 			publishProgress(10, 100);
 			rssHandler.processFeed();
 			publishProgress(70, 100);
 			return rssHandler;
 		}
 
 		@Override
 		public void onProgressUpdate(Integer... progress) {
 			progressDialog.setProgress(progress[0]);
 			progressDialog.setMax(progress[1]);
 		}
 
 		public void onPostExecute(RssHandler rssHandler) {
 			resetViewValues(rssHandler.getTitle(), rssHandler.getDate(),
 					rssHandler.getImage(), rssHandler.getDescription());
 			publishProgress(100, 100);
 			progressDialog.dismiss();
 		}
 
 		private void resetViewValues(final String title, final String date,
 				final Bitmap image, StringBuffer description) {
 			fullDescription = description;
 			setImage(image);
 			description = getSmallerDescription(description);
 			((TextView) findViewById(R.id.title)).setText(title);
 			((TextView) findViewById(R.id.publishDate)).setText(date);
 			((ImageView) findViewById(R.id.imageDisplay)).setImageBitmap(image);
 			((TextView) findViewById(R.id.imageDescription))
 					.setText(description);
 			setVisibilityForShowMoreButton(View.VISIBLE);
 		}
 	}
 }
