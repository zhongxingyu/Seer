 package com.rubikssolutions.cultureshockme;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private static String API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit=0";
 	private static final String TAG = "MainActivity";
 	private static final String LOADING = "Currently loading...";
 	private static final String DONE_LOADING = "Show me more stories!";
 
 	TextView[] storyViews;
 	TextView[] authorTextViews;
 	ImageView[] flagImageViews;
 	ImageView[] backgroundImageViews;
 	ImageView[] profileImageViews;
 
 	int counter = 0;
 	
 	int deviceWidth = 240;
 
 	int amountToDisplayAtOnce = 4; 
 	int amountToGetTotal = 12;
 	
 	String[] allStories;
 	String[] allAuthors;
 	Bitmap[] allBackgrounds;
 	Bitmap[] allFlags;
 	Bitmap[] allProfiles;
 	
 	int currentPage = 0;
 	
 	boolean loading = true;
 	boolean loadMoreButtonIsEnabled = false;
 	Button loadMoreButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		// Create the arrays
 		storyViews = new TextView[amountToDisplayAtOnce];
 		authorTextViews = new TextView[amountToDisplayAtOnce];
 		flagImageViews = new ImageView[amountToDisplayAtOnce];
 		backgroundImageViews = new ImageView[amountToDisplayAtOnce];
 		profileImageViews = new ImageView[amountToDisplayAtOnce];
 
 		// Create the arrays to hold ALL
 		allStories = new String[amountToDisplayAtOnce];
 		allAuthors = new String[amountToDisplayAtOnce];
 		allBackgrounds = new Bitmap[amountToDisplayAtOnce];
 		allFlags = new Bitmap[amountToDisplayAtOnce];
 		allProfiles = new Bitmap[amountToDisplayAtOnce];
 		
 		ImageButton infoButton = (ImageButton) findViewById(R.id.button_info);
 		infoButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent infoScreenIntent = new Intent(MainActivity.this, InfoScreen.class);
 				MainActivity.this.startActivity(infoScreenIntent);
 			}
 		}); 	 
 		
 		// Configure the button
 		loadMoreButton = (Button) findViewById(R.id.buttonLoadMoreStories);
 		loadMoreButton.setText(LOADING);
 		loadMoreButton.setBackgroundColor(Color.LTGRAY);
 		loadMoreButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if (loadMoreButtonIsEnabled) {
 					loadMoreButtonIsEnabled = false;
 					loadMoreButton.setText(LOADING);
 					loadMoreButton.setBackgroundColor(Color.LTGRAY);
 					API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit=" + currentPage;
 					Log.i(TAG, currentPage + "== current page");
 					Log.i(TAG, API_URL);
 					loadMoreStories(true, true, true, true, true);
 					addPage(true, true, true, true, true);
 				}
 			}
 		});
 		
 		addViews();
 		loadMoreStories(true, true, true, true, true);	
 	
 		loading = false;
 	}
 	
 	private void addViews () {		
 		View myLayout = findViewById(R.id.mainBottomView);
 
 		// Set up the LayoutParams
 		LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		LinearLayout.LayoutParams layoutParamsBottomPadding = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		layoutParamsBottomPadding.setMargins(0, 0, 0, 15);
 		LinearLayout.LayoutParams layoutParamsNoPadding = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		LinearLayout.LayoutParams layoutParamsProfile = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		
 		// Add all the views
 		for (int i = 0; i < 1; i++) {
 			// Create the new views
 			TextView text = new TextView(this);
 			TextView author = new TextView(this);
 			ImageView flag = new ImageView(this);
 			ImageView background = new ImageView(this);
 			ImageView profile = new ImageView(this);
 			
 			// Add them to the arrays
 			storyViews[i] = text;
 			authorTextViews[i] = author;
 			flagImageViews[i] = flag;
 			backgroundImageViews[i] = background; 
 			profileImageViews[i] = profile; 
 			
 			// Set the layout parameters
 			profile.setLayoutParams(layoutParamsProfile);
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
 			((LinearLayout) myLayout).addView(text);
 		}
 	}
 	
 	private void addPage(boolean profile, boolean flag, boolean author,
 			boolean background, boolean text) {
 		View myLayout = findViewById(R.id.mainBottomView);
 		
 		// Set up the LayoutParams
 		LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		LinearLayout.LayoutParams layoutParamsBottomPadding = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		layoutParamsBottomPadding.setMargins(0, 0, 0, 15);
 		LinearLayout.LayoutParams layoutParamsNoPadding = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		LinearLayout.LayoutParams layoutParamsProfile = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		
 		TextView[] textViews = new TextView[amountToDisplayAtOnce];
 		ImageView[] imageViews = new ImageView[amountToDisplayAtOnce];
 
 		for (int i = 0; i < amountToDisplayAtOnce; i++) {
 			if (profile) {
 				ImageView view = new ImageView(this);
 				view.setLayoutParams(layoutParamsProfile);
 				view.setImageBitmap(allProfiles[i]);
 				imageViews[i] = view;
 				((LinearLayout) myLayout).addView(view);
 			}
 			if (flag) {
 				ImageView view = new ImageView(this);
 				view.setLayoutParams(layoutParamsNoPadding);
 				view.setImageBitmap(allFlags[i]);
 				imageViews[i] = view;
 				((LinearLayout) myLayout).addView(view);
 			}
 			if (author) {
 				TextView view = new TextView(this);
 				view.setLayoutParams(layoutParamsNoPadding);
 				view.setText(Html.fromHtml(allAuthors[i]));
 				view.setTextSize(15);
 				view.setBackgroundColor(Color.WHITE);
 				view.setPadding(10, 0, 10, 0);
 				textViews[i] = view;
 				((LinearLayout) myLayout).addView(view);
 			}
 			if (background) {
 				ImageView view = new ImageView(this);
 				view.setLayoutParams(backgroundParams);
 				view.setImageBitmap(allBackgrounds[i]);
 				imageViews[i] = view;
 				((LinearLayout) myLayout).addView(view);
 			}
 			if (text) {
 				TextView view = new TextView(this);
 				view.setLayoutParams(layoutParamsBottomPadding);
 				view.setText(allStories[i]);
 				view.setTextSize(17);
 				view.setBackgroundColor(Color.WHITE);
 				view.setPadding(10, 0, 10, 0);
 				textViews[i] = view;
 				((LinearLayout) myLayout).addView(view);
 			}
 		}
 		currentPage += amountToDisplayAtOnce;
 	}
 	
 	public void loadMoreStories(final boolean profile, final boolean flag, final boolean author,
 			final boolean background, final boolean text) {
 		final Handler handler = new Handler();
 		Timer timer = new Timer();
 		TimerTask doAsynchronousTask = new TimerTask() {
 			@Override
 			public void run() {
 				handler.post(new Runnable() {
 					public void run() {
 						try {
 							if (profile) {new ProfilePictureLoader().execute();}
 							if (flag) {new FlagLoader().execute();} 
 							if (author) {new AuthorLoader().execute();}
 							if (background) {new BackgroundLoader().execute();}
 							if (text){new StoryLoader().execute();}
 						} catch (Exception e) {
 							Log.e(TAG, "Could not execute storyloader", e);
 						}
 					}
 				});
 			}
 		};
 		timer.schedule(doAsynchronousTask, 0);
 	}
 	
 	class BackgroundLoader extends AsyncTask<String, Void, Bitmap[]> {
 		@Override
 		protected Bitmap[] doInBackground(String... params) {	
 			Bitmap[] backgroundArray = new Bitmap[amountToDisplayAtOnce];
 			String[] backgroundURLArray = new String[amountToDisplayAtOnce];
 			
 			try {
 				Document doc = Jsoup.connect(API_URL).get();
 				Elements uRLElements = doc.select("[style^=background-image:url(']");
 				int backgroundCounter = 0;
 				for (int i = 0; i < (amountToDisplayAtOnce * 2); i++) {
 					try {
 						String uRlString = uRLElements.get(i).toString();
 						uRlString = uRlString.substring(73);
 						if (uRlString.startsWith("http")) {
 							uRlString = uRlString.substring(0, uRlString.length() - 10);
 							backgroundURLArray[backgroundCounter] = uRlString;
 							backgroundCounter++;
 						} else if (uRlString.startsWith("'")) {
 							backgroundCounter++;
 						}
 					} catch (Exception e) {
 						Log.e(TAG, "error fetching BACKGROUND from server", e);
 					}
 				}
 			} catch (Exception e) {
 				Log.e(TAG, "error connecting to server", e);
 			}
 			
 			BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inSampleSize = 4;
 			for (int i = 0; i < amountToDisplayAtOnce; i++) {
 				try {
 					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(backgroundURLArray[i]).getContent(), null, options);
 					backgroundArray[i] = scaleBitmapToDevice(bitmap); 
 					allBackgrounds[i] = backgroundArray[i]; 
 				} catch (Exception e) { 
 					Log.e(TAG, "error fetching BACKGROUND from URL -" + i, e);
 				}
 			}
 			return backgroundArray;
 		}
 	}
 	
 	class ProfilePictureLoader extends AsyncTask<String, Void, Bitmap[]> {
 		@Override
 		protected Bitmap[] doInBackground(String... params) {	
 			Bitmap[] profiles = new Bitmap[amountToDisplayAtOnce];
 			String[] profileURLs = new String[amountToDisplayAtOnce];
 			try {
 				Elements pics = Jsoup.connect(API_URL).get().select("img");
 				for (int i = 0; i < amountToDisplayAtOnce; i++) {
 					String url = pics.get(i).absUrl("src");
 					profileURLs[i] = url; 
 				}
 			} catch (Exception e) {
 				Log.e(TAG, "error connecting to server", e);
 			}
 			
 			for (int i = 0; i < amountToDisplayAtOnce; i++) {
 				try {
 					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(profileURLs[i]).getContent());
 					profiles[i] = scaleProfileBitmap(bitmap); 
 					allProfiles[i] = profiles[i]; 
 				} catch (Exception e) {
 					Log.e(TAG, "error fetching BACKGROUND from URL -" + i, e);
 				}
 			}
 			return profiles;
 		}
 	}
 
 	class StoryLoader extends AsyncTask<String, Void, String[]> {
 		protected void onPreExecute() {
 			storyViews[0].setTextSize(25f);
 			storyViews[0].setText("Loading stories...");
 		}
 		
 		protected String[] doInBackground(String... urls) {
 			try {
 				String[] textArray = new String[amountToDisplayAtOnce];
 				Elements textElements = Jsoup.connect(API_URL).get().select("H3");
 				for (int i = 0; i < amountToDisplayAtOnce; i++) {
 					textArray[i] = textElements.get(i).text();
 					allStories[i] = textArray[i];
 				}
 				return textArray;
 			} catch (Exception e) {
 				Log.e(TAG, "error fetching TEXT from server", e);
 				return null;
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(String[] result) {
 			loadMoreButtonIsEnabled = true;
 			loadMoreButton.setText(DONE_LOADING);
 			loadMoreButton.setBackgroundColor(Color.YELLOW);
 		}
 	}
 
 	class AuthorLoader extends AsyncTask<String, Void, String[]> {
 		protected void onPreExecute() {
 			authorTextViews[0].setText("Loading authors...");
 		}
 		
 		protected String[] doInBackground(String... urls) {
 			try {
 				String[] authorArray = new String[amountToDisplayAtOnce];
 				Elements authorElements = Jsoup.connect(API_URL).get().select("[class=user_link]");
 				Elements countryElements = Jsoup.connect(API_URL).get().select("[class=browse_story_location with_countryflag_icon]");
 				for (int i = 0; i < amountToDisplayAtOnce; i++) {
 					authorArray[i] = "<big>" + "<i>"
 							+ authorElements.get(i).text() 
 							+ "</i>" + "</big>\n" + "<br />"
 							+ countryElements.get(i).text();
 					allAuthors[i] = authorArray[i];
 				}
 				return authorArray;
 			} catch (Exception e) {
 				Log.e(TAG, "error fetching AUTHOR from server", e);
 				return null;
 			}
 		}
 	}
 
 	class FlagLoader extends AsyncTask<String, Integer, Bitmap[]> {
 		protected Bitmap[] doInBackground(String... params) {
 			try {
 				Bitmap[] flagArray = new Bitmap[amountToDisplayAtOnce];
 				Elements imageElements = Jsoup.connect(API_URL).get().select("[style*=flags/mini]");
 				for (int i = 0; i < amountToDisplayAtOnce; i++) {
 					String imageCode = imageElements.get(i).toString().substring(125, 131);
 					URL imageUrl = new URL("http://culture-shock.me/img/icons/flags/mini/" + imageCode);
 					URLConnection conn = imageUrl.openConnection();
 					conn.connect();
 
 					InputStream is = conn.getInputStream();
 					BufferedInputStream bis = new BufferedInputStream(is);
 					flagArray[i] = BitmapFactory.decodeStream(bis);
 					allFlags[i] = flagArray[i];
 					
 					bis.close();
 					is.close();
 				}
 				return flagArray;
 			} catch (Exception e) {
 				Log.e(TAG, "error fetching FLAG from server", e);
 			}
 			return null;
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
 	
 	private Bitmap scaleProfileBitmap(Bitmap inputBitmap) {
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		int profilePicWidth = (int) (dm.widthPixels/10f);
 		
 		float width = inputBitmap.getWidth();
 		float height = inputBitmap.getHeight();
 		float ratio = (width / height);
 		
 		return Bitmap.createScaledBitmap(inputBitmap, profilePicWidth, (int)(profilePicWidth / ratio), false);
 	}
 
 }
