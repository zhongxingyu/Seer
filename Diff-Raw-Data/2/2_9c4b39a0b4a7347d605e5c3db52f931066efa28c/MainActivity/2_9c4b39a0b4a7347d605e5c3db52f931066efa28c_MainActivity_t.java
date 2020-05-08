 package com.brent.feedreader;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.brent.feedreader.util.SystemUiHider;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  * 
  * @see SystemUiHider
  */
 public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {
 	/**
 	 * Whether or not the system UI should be auto-hidden after
 	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
 	 */
 	private static final boolean AUTO_HIDE = true;
 
 	/**
 	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
 	 * user interaction before hiding the system UI.
 	 */
 	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
 
 	/**
 	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
 	 * will show the system UI visibility upon interaction.
 	 */
 	private static final boolean TOGGLE_ON_CLICK = true;
 
 	/**
 	 * The flags to pass to {@link SystemUiHider#getInstance}.
 	 */
 	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;	
 
 	/**
 	 * The instance of the {@link SystemUiHider} for this activity.
 	 */
 	private SystemUiHider mSystemUiHider;
 
 	public static final String TAG = "FeedReader";	
 	private Spinner titleSpinner;
 	WebView articleBodyView;
 	private ArrayList<String> articleTitles;
 	private ArrayList<String> articleLinks;
 	public ArrayList<String> articleTimeStamps;
 	private ArrayList<String> articleBodies;	
 	private String appTitle;	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_PROGRESS);
 		setContentView(R.layout.activity_main);
 		titleSpinner = (Spinner)findViewById(R.id.title_spinner); 
 		titleSpinner.setOnItemSelectedListener(this);
 
 		final View controlsView = findViewById(R.id.fullscreen_content_controls);
 		articleBodyView = (WebView) findViewById(R.id.article_body_webview);
 		articleBodyView.getSettings().setSupportZoom(true);	//this line and next allow zooming in and out of view
 		articleBodyView.getSettings().setBuiltInZoomControls(true);
 		articleBodyView.getSettings().setJavaScriptEnabled(true);	//needed for progress indication
 		articleBodyView.setWebChromeClient(new WebChromeClient() { 
 			//Show progress when loading page, since it takes a little while
 			public void onProgressChanged(WebView view, int progress) {
 				//				articleBodyView.loadData("Loading page...", "text/html", "UTF-8");
 				MainActivity.this.setTitle("Loading page...");
 				MainActivity.this.setProgress(progress * 100);
 				if(progress == 100) {
 					MainActivity.this.setTitle(appTitle);
 					//					articleBodyView.loadData(articleBodies.get(0), "text/html", "UTF-8");
 				}
 			}
 		});
 		
 		// Get the articles. Network "stuff" needs to be done outside of the UI thread:
 		(new FetchArticlesTask()).execute("http://feeds2.feedburner.com/TheTechnologyEdge");
 
 		// Code from here to "end" generated automatically when project created,
 		// to hide and show title and status bars (i.e., run app full screen)
 
 		// Set up an instance of SystemUiHider to control the system UI for
 		// this activity.
 		mSystemUiHider = SystemUiHider.getInstance(this, articleBodyView, HIDER_FLAGS);
 		mSystemUiHider.setup();
 		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
 			// Cached values.
 			int mControlsHeight;
 			int mShortAnimTime;
 
 			@Override
 			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 			public void onVisibilityChange(boolean visible) {
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 					// If the ViewPropertyAnimator API is available
 					// (Honeycomb MR2 and later), use it to animate the
 					// in-layout UI controls at the bottom of the
 					// screen.
 					if (mControlsHeight == 0) {
 						mControlsHeight = controlsView.getHeight();
 					}
 					if (mShortAnimTime == 0) {
 						mShortAnimTime = getResources().getInteger(
 								android.R.integer.config_shortAnimTime);
 					}
 					controlsView
 					.animate()
 					.translationY(visible ? 0 : mControlsHeight)
 					.setDuration(mShortAnimTime);
 				} else {
 					// If the ViewPropertyAnimator APIs aren't
 					// available, simply show or hide the in-layout UI
 					// controls.
 					controlsView.setVisibility(visible ? View.VISIBLE
 							: View.GONE);
 				}
 
 				if (visible && AUTO_HIDE) {
 					// Schedule a hide().
 					delayedHide(AUTO_HIDE_DELAY_MILLIS);
 				}
 			}
 		});
 
 		// Set up the user interaction to manually show or hide the system UI.
 		articleBodyView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (TOGGLE_ON_CLICK) {
 					mSystemUiHider.toggle();
 				} else {
 					mSystemUiHider.show();
 				}
 			}
 		});
 
 		// Upon interacting with UI controls, delay any scheduled hide()
 		// operations to prevent the jarring behavior of controls going away
 		// while interacting with the UI.
 		findViewById(R.id.title_spinner).setOnTouchListener(
 				mDelayHideTouchListener);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 
 		// Trigger the initial hide() shortly after the activity has been
 		// created, to briefly hint to the user that UI controls
 		// are available.
 		delayedHide(100);
 	}
 
 	/**
 	 * Touch listener to use for in-layout UI controls to delay hiding the
 	 * system UI. This is to prevent the jarring behavior of controls going away
 	 * while interacting with activity UI.
 	 */
 	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
 		@Override
 		public boolean onTouch(View view, MotionEvent motionEvent) {
 			if (AUTO_HIDE) {
 				delayedHide(AUTO_HIDE_DELAY_MILLIS);
 			}
 			return false;
 		}
 	};
 
 	Handler mHideHandler = new Handler();
 	Runnable mHideRunnable = new Runnable() {
 		@Override
 		public void run() {
 			mSystemUiHider.hide();
 		}
 	};
 
 	/**
 	 * Schedules a call to hide() in [delay] milliseconds, canceling any
 	 * previously scheduled calls.
 	 */
 	private void delayedHide(int delayMillis) {
 		mHideHandler.removeCallbacks(mHideRunnable);
 		mHideHandler.postDelayed(mHideRunnable, delayMillis);
 	}
 
 	// End automatically generated code to run app full screen
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	private class FetchArticlesTask extends AsyncTask<String, Void, String> {
 
 		@Override
 		protected String doInBackground(String... urls) {
 			BufferedReader reader=null;
 			String rawXML = null;
 
 			// Make the connection to the URL and get the xml as one big string
 			try {
 				URL url = new URL(urls[0]);
 				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 				connection.setRequestMethod("GET");
 				connection.setReadTimeout(15000);
 				connection.connect();
 
 				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 				StringBuilder stringBuilder = new StringBuilder();
 				String line = null;
 
 				while ((line=reader.readLine()) != null) {
 					stringBuilder.append(line + "\n");
 				}
 				rawXML = stringBuilder.toString();
 
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			finally {
 				if (reader != null) {
 					try {
 						reader.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			// Parse the xml and create 3 lists holding article titles, contents (bodies), and external links
 			articleTitles = new ArrayList<String>();
 			articleBodies = new ArrayList<String>();
 			articleLinks = new ArrayList<String>();
 			articleTimeStamps = new ArrayList<String>();
 			
 			articleTitles.add("Reader Instructions");
 			articleBodies.add("Touch the screen to display the article-selection list at the bottom.");
 			articleLinks.add("");
 			articleTimeStamps.add("");
 			DocumentBuilder builder;
 			Document doc = null;
 			try {
 				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 				doc = builder.parse(new InputSource(new StringReader(rawXML)));
 			} catch (ParserConfigurationException e) {				
 				e.printStackTrace();
 			} catch (SAXException e) {				
 				e.printStackTrace();
 			} catch (IOException e) {				
 				e.printStackTrace();
 			}			
 
 			NodeList titles = doc.getElementsByTagName("title");
 			for (int i = 0; i < titles.getLength(); i++) {
 				Element title = (Element)titles.item(i);
 				if (i == 0) {
 					appTitle = title.getFirstChild().getNodeValue();
 				}
 				else articleTitles.add(title.getFirstChild().getNodeValue());
 			}
 
 			NodeList bodies = doc.getElementsByTagName("content");
 			for (int i = 0; i < bodies.getLength(); i++) {
 				Element body = (Element)bodies.item(i);
 				articleBodies.add(body.getFirstChild().getNodeValue());
 			}
 
 			NodeList links = doc.getElementsByTagName("link");			
 			for (int i = 0; i < links.getLength(); i++) {
 				Element link = (Element)links.item(i);
 				if (link == null) Log.i(TAG, i + " is null");
 				if (link.getAttribute("rel").equals("alternate") && link.getAttribute("title").length() > 0) {
 					String articleLink = ("<a href=" + "`" + link.getAttribute("href") + "`" + "target=" + "`"+ "_blank" + "`" + ">" + link.getAttribute("title") + "</a>").replace('`', '"');
 					if (i == 0 || i == 1 || i == 2) Log.i(TAG, i + " " + articleLink);
 					articleLinks.add(articleLink);
 				}
 			}
 			
 			NodeList timeStamps = doc.getElementsByTagName("updated");
 			Log.i(TAG, "timestamps length: " + timeStamps.getLength());
			for (int i = 1; i < timeStamps.getLength(); i++) {
 				Element timeStamp = (Element)timeStamps.item(i);
 				articleTimeStamps.add(timeStamp.getFirstChild().getNodeValue());
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String string) {
 			// Populate the spinner with the article titles
 			ArrayAdapter<String> aa = new ArrayAdapter<String>(MainActivity.this, 
 					android.R.layout.simple_spinner_item, articleTitles);
 			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			titleSpinner.setAdapter(aa);
 			MainActivity.this.setTitle(appTitle);
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 		// Load the article bodies and external links into the WebView for display
 		String article = "<br>" + "<br>" + articleLinks.get(position) + "<br>" + articleTimeStamps.get(position)+ "<p>" + articleBodies.get(position);
 		articleBodyView.loadData(article, "text/html", "UTF-8");
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 
 	}
 }
