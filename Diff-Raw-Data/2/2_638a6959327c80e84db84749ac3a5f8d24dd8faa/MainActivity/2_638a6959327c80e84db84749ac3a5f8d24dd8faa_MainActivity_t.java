 package com.rubikssolutions.cultureshockme;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private static final String API_URL = "http://culture-shock.me/ajax/?act=get_stories_more";
 	private static final String TAG = "MainActivity";
 	private static final Float FLAG_SCALE = 1.5f;
 
 	TextView[] textViews;
 	TextView[] authorTextViews;
 	ImageView[] flagImageViews;
 	ImageView[] backgroundImageViews;
 
 	int counter = 0;
 
 	/*
 	 * pulls the first x stories and displays them.
 	 * Higher values throw an OutOfMemoryError on my crappy phone,
 	 * but work fine on the Genymotion emulator.
 	 * 
 	 * Potential max is 12 for now I think.
 	 */
	int amountToLoad = 3; 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		addViews();
 		
 		new StoryLoader().execute();
 		new AuthorLoader().execute();
 		new FlagLoader().execute();
 		
 		// Note - takes a while and is probably horribly inefficient. 
 		new BackgroundLoader().execute();
 	}
 	
 	private void addViews () {
 		View myLayout = findViewById(R.id.scrollingLinearView);
 		
 		// Create the arrays
 		textViews = new TextView[amountToLoad];
 		authorTextViews = new TextView[amountToLoad];
 		flagImageViews = new ImageView[amountToLoad];
 		backgroundImageViews = new ImageView[amountToLoad];
 
 		// Set up the LayoutParams
 		LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		LinearLayout.LayoutParams layoutParamsBottomPadding = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		layoutParamsBottomPadding.setMargins(0, 0, 0, 15);
 		LinearLayout.LayoutParams layoutParamsNoPadding = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		
 		// Add all the views
 		for (int i = 0; i < amountToLoad; i++) {
 			// Create the new viws
 			TextView text = new TextView(this);
 			TextView author = new TextView(this);
 			ImageView flag = new ImageView(this);
 			ImageView background = new ImageView(this);
 			
 			// Add them to the arrays
 			textViews[i] = text;
 			authorTextViews[i] = author;
 			flagImageViews[i] = flag;
 			backgroundImageViews[i] = background; 
 			
 			// Set the loading text
 			text.setText("Loading stories...");
 			text.setTextSize(20f);
 			author.setText("Loading authors...");
 			
 			// Set the layout parameters
 			author.setLayoutParams(layoutParamsNoPadding);
 			flag.setLayoutParams(layoutParamsNoPadding);
 			flag.setPadding(0, 0, 0, 5);
 			background.setLayoutParams(backgroundParams);
 			background.setAdjustViewBounds(true);			
 			text.setLayoutParams(layoutParamsBottomPadding);
 
 			// Set the background color
 			author.setBackgroundColor(Color.WHITE);
 			background.setBackgroundColor(Color.WHITE);
 			text.setBackgroundColor(Color.WHITE);
 
 			// The order in which to display the items
 			((LinearLayout) myLayout).addView(flag);
 			((LinearLayout) myLayout).addView(author);
 			((LinearLayout) myLayout).addView(background);
 			((LinearLayout) myLayout).addView(text);
 		}
 	}
 
 	class BackgroundLoader extends AsyncTask<String, Void, Bitmap[]> {
 		@Override
 		protected Bitmap[] doInBackground(String... params) {	
 			Bitmap[] backgroundArray = new Bitmap[amountToLoad];
 			String[] backgroundURLArray = new String[amountToLoad];
 			
 			try {
 				Document doc = Jsoup.connect(API_URL).get();
 				int backgroundCounter = 0;
 				for (int i = 0; i < (amountToLoad * 2); i++) {
 					try {
 						String uRLString = doc.select("[style^=background-image:url(']").get(i) + " a";
 						uRLString = uRLString.substring(73);
 						if (uRLString.startsWith("http")) {
 							uRLString = uRLString.substring(0, uRLString.length() - 12);
 							backgroundURLArray[backgroundCounter] = uRLString;
 							backgroundCounter++;
 						} else if (uRLString.startsWith("'")) {
 							backgroundCounter++;
 						}
 					} catch (Exception e) {
 						Log.e(TAG, "error fetching BACKGROUND from server", e);
 					}
 				}
 			} catch (Exception e) {
 				Log.e(TAG, "error connecting to server", e);
 			}
 			
 			for (int i = 0; i < backgroundArray.length; i++) {
 				try {
 					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(backgroundURLArray[i]).getContent());
 					backgroundArray[i] = bitmap; 
 				} catch (Exception e) {
 					Log.e(TAG, "error fetching BACKGROUND from URL", e);
 				}
 			}
 			return backgroundArray;
 		}
 		
 		@Override
 		protected void onPostExecute(Bitmap[] backgroundArray) {
 			for (int i = 0; i < backgroundImageViews.length; i++) {
 				try {
 					backgroundArray[i] = scaleBitmapToDevice(backgroundArray[i]);
 					backgroundImageViews[i].setImageBitmap(backgroundArray[i]);
 				} catch (Exception e) {
 					Log.e(TAG, "Can't scale Bitmap, probable cause: picture missing from the story!", e);
 				}
 			}
 		}
 	}
 
 	class StoryLoader extends AsyncTask<String, Void, String[]> {
 		protected void onPreExecute() {
 			textViews[0].setTextSize(25f);
 			textViews[0].setText("Loading stories...");
 		}
 		
 		protected String[] doInBackground(String... urls) {
 			try {
 				Document doc = Jsoup.connect(API_URL).get();
 				String[] textArray = new String[amountToLoad];
 
 				for (int i = 0; i < textArray.length; i++) {
 					Element textElement = doc.select("H3").get(i);
 					textArray[i] = textElement.text();
 				}
 
 				return textArray;
 			} catch (Exception e) {
 				Log.e(TAG, "error fetching TEXT from server", e);
 				return null;
 			}
 		}
 
 		protected void onPostExecute(String[] result) {
 			if (result != null) {
 				for (int i = 0; i < textViews.length; i++) {
 					textViews[i].setText(result[i]);
 					textViews[i].setTextSize(17f);
 				}
 			}
 		}
 	}
 
 	class AuthorLoader extends AsyncTask<String, Void, String[]> {
 		protected String[] doInBackground(String... urls) {
 			try {
 				Document doc = Jsoup.connect(API_URL).get();
 				String[] authorArray = new String[amountToLoad];
 
 				for (int i = 0; i < authorArray.length; i++) {							
 					Element authorElement = doc.select("[class=user_link]").get(i);
 					Element countryElement = doc.select("[class=browse_story_location with_countryflag_icon]").get(i);
 					authorArray[i] = authorElement.text() + " - " +  countryElement.text();
 				}
 				return authorArray;
 			} catch (Exception e) {
 				Log.e(TAG, "error fetching AUTHOR from server", e);
 				return null;
 			}
 		}
 
 		protected void onPostExecute(String[] result) {
 			if (result != null) {
 				for (int i = 0; i < authorTextViews.length; i++) {
 					authorTextViews[i].setText(result[i]);
 					authorTextViews[i].setTypeface(null, Typeface.ITALIC);
 					authorTextViews[i].setTextSize(15f);
 				}
 			}
 		}
 	}
 
 	class FlagLoader extends AsyncTask<String, Integer, Bitmap[]> {
 		protected Bitmap[] doInBackground(String... params) {
 			try {
 				Document doc = Jsoup.connect(API_URL).get();
 				Bitmap[] flagArray = new Bitmap[amountToLoad];
 
 				for (int i = 0; i < flagArray.length; i++) {
 					Element imageElement = doc.select("[style*=flags/mini]").get(i);
 					String imageCode = imageElement.toString().substring(125, 131);
 
 					URL imageUrl = new URL("http://culture-shock.me/img/icons/flags/mini/" + imageCode);
 					URLConnection conn = imageUrl.openConnection();
 					conn.connect();
 
 					InputStream is = conn.getInputStream();
 					BufferedInputStream bis = new BufferedInputStream(is);
 					flagArray[i] = BitmapFactory.decodeStream(bis);
 					bis.close();
 					is.close();
 				}
 				return flagArray;
 			} catch (Exception e) {
 				Log.e(TAG, "error fetching FLAG from server", e);
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Bitmap[] result) {
 			if (result != null) {
 				for (int i = 0; i < flagImageViews.length; i++) {
 					flagImageViews[i].setImageBitmap(result[i]);
 					flagImageViews[i].setScaleX(FLAG_SCALE);
 					flagImageViews[i].setScaleY(FLAG_SCALE);
 				}
 			}
 		}
 	}
 
 	private Bitmap scaleBitmapToDevice(Bitmap inputBitmap) { 
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		int deviceWidth = dm.widthPixels;
 		
 		float width = inputBitmap.getWidth();
 		float height = inputBitmap.getHeight();
 		float ratio = (width / height);
 		
 		return Bitmap.createScaledBitmap(inputBitmap, deviceWidth, (int)(deviceWidth / ratio), false);
 	}
 }
