 package ayelix.stocks;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.app.Service;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 //TODO: Null/empty check in search() doesn't work.
 
 public class MainActivity extends Activity {
 	private static final String TAG = "MainActivity";
 
 	/** URL to which a stock query is appended. */
 	private static final String BASE_URL = "http://www.google.com/finance?q=";
 	
 	/** Refresh interval in milliseconds. */
 	private static final int REFRESH_DELAY = 10000;
 
 	/** Property name for the chart image URL */
 	private static final String IMAGE_PROPERTY = "imageUrl";
 
 	/** EditText for search query input. */
 	private EditText m_searchEditText;
 	/** Button to start a search. */
 	private Button m_goButton;
 	/** ImageView for chart image. */
 	private ImageView m_chartImageView;
 	/** LinearLayout for parsed results. */
 	private LinearLayout m_resultsLayout;
 
 	/** List of properties to find and display. */
 	private final List<StockProperty> m_propertyList = Arrays.asList(
 	/** Company name */
 	new StockProperty("name", "Name", this),
 	/** Stock exchange for this stock */
 	new StockProperty("exchange", "Exchange", this),
 	/** Stock price */
 	new StockProperty("price", "Price", this),
 	/** Currency being used */
 	new StockProperty("priceCurrency", "Currency", this),
 	/** Price change in currency */
 	new StockProperty("priceChange", "Change", this),
 	/** Price change in percent */
 	new StockProperty("priceChangePercent", "Change (%)", this),
 	/** Source of provided data */
 	new StockProperty("dataSource", "Data Source", this));
 
 	/** Latest search string. */
 	private String m_lastSearch = "";
 
 	/** Handler for refresh task. */
 	private Handler m_handler;
 	/** Runnable refresh task. */
 	private Runnable m_refreshRunnable;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Get the pre-created views
 		m_searchEditText = (EditText) findViewById(R.id.searchEditText);
 		m_goButton = (Button) findViewById(R.id.goButton);
 		m_resultsLayout = (LinearLayout) findViewById(R.id.resultsLayout);
 		m_chartImageView = (ImageView) findViewById(R.id.chartImageView);
 
 		// Create and add the views for each property
 		createAndAddViews();
 
 		// Create the listener for the search box "Go" action
 		m_searchEditText
 				.setOnEditorActionListener(new OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView v, int actionId,
 							KeyEvent event) {
 						boolean handled = false;
 						// The GO action will start a search
 						if (EditorInfo.IME_ACTION_GO == actionId) {
 							search();
 							handled = true;
 						}
 						return handled;
 					}
 				});
 
 		// Create the listener for the Go button
 		m_goButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				search();
 			}
 		});
 
 		// Create the refresh task
 		m_handler = new Handler();
 		m_refreshRunnable = new Runnable() {
 			@Override
 			public void run() {
 				Log.d(TAG, "Refreshing results.");
 				search();
 			}
 		};
 
 	} // End method onCreate()
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		// Remove the refresh if it's scheduled
 		m_handler.removeCallbacks(m_refreshRunnable);
 	}
 
 	/**
 	 * Starts a search for the given string.
 	 * 
 	 * @param searchString
 	 *            The string to search for (stock symbol or company name).
 	 */
 	private void search() {
 		String searchString = m_searchEditText.getText().toString();
 
 		// Remove the refresh if it's scheduled
 		m_handler.removeCallbacks(m_refreshRunnable);
 		
		if ((searchString != null) && (searchString != "")) {
 			Log.d(TAG, "Searching string: \"" + searchString + "\"");
 
 			// Save the search string
 			m_lastSearch = searchString;
 
 			// Disable the Go button to show that the search is in progress
 			m_goButton.setEnabled(false);
 
 			// Remove the keyboard to better show results
 			((InputMethodManager) this
 					.getSystemService(Service.INPUT_METHOD_SERVICE))
 					.hideSoftInputFromWindow(m_searchEditText.getWindowToken(),
 							0);
 
 			// Start the search task
 			new HTTPTask().execute(searchString);
 			
 			// Schedule the refresh
 			m_handler.postDelayed(m_refreshRunnable, REFRESH_DELAY);
 		} else {
 			Log.d(TAG, "Ignoring null or empty search string.");
 		}
 	} // End method search()
 
 	/**
 	 * Creates and adds rows in the UI for each parameter (label and value).
 	 */
 	private void createAndAddViews() {
 		// Create the layout parameters for each field
 		final LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
 				0, LayoutParams.WRAP_CONTENT, 0.3f);
 		labelParams.gravity = Gravity.RIGHT;
 		labelParams.setMargins(0, 25, 0, 0);
 		final LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
 				0, LayoutParams.WRAP_CONTENT, 0.7f);
 		valueParams.gravity = Gravity.LEFT;
 		valueParams.setMargins(0, 25, 0, 0);
 
 		// Add a layout and text views for each property
 		for (final StockProperty property : m_propertyList) {
 			Log.d(TAG, "Adding row for property: " + property.getPropertyName());
 
 			// Create a horizontal layout for the label and value
 			final LinearLayout layout = new LinearLayout(this);
 			layout.setLayoutParams(new LinearLayout.LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 
 			// Create a TextView for the label
 			final TextView label = new TextView(this);
 			label.setLayoutParams(labelParams);
 			label.setText(property.getLabelText());
 			label.setTextAppearance(this, android.R.style.TextAppearance_Medium);
 			layout.addView(label);
 
 			// Configure and add the value TextView (created when the property
 			// was constructed)
 			final TextView value = property.getView();
 			value.setLayoutParams(valueParams);
 			value.setHint("None");
 			value.setTextAppearance(this, android.R.style.TextAppearance_Medium);
 			value.setTypeface(null, Typeface.BOLD);
 			layout.addView(value);
 
 			// Add the row to the main layout
 			m_resultsLayout.addView(layout);
 		}
 	}
 
 	/**
 	 * AsyncTask to execute the HTTP operations.
 	 */
 	private class HTTPTask extends AsyncTask<String, Void, String> {
 
 		@Override
 		protected String doInBackground(String... params) {
 
 			String retVal = null;
 
 			// Build the properly-formatted URI
 			String formattedUri = BASE_URL + Uri.encode(params[0]);
 			Log.d(TAG, "HTTPTask requesting " + formattedUri);
 
 			// Get the HTTP client
 			final HttpClient client = new DefaultHttpClient();
 
 			// Get the HTTP request
 			final HttpGet request = new HttpGet(formattedUri);
 
 			try {
 				// Get the response
 				HttpResponse response = client.execute(request);
 				BufferedReader responseReader = new BufferedReader(
 						new InputStreamReader(response.getEntity().getContent()));
 
 				// Get the results as a string
 				String line = new String();
 				StringBuilder sb = new StringBuilder();
 				while ((line = responseReader.readLine()) != null) {
 					sb.append(line + "\n");
 				}
 				retVal = sb.toString();
 
 				responseReader.close();
 
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Return the response
 			return retVal;
 
 		} // End method doInBackground()
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (result != null) {
 				// Start the parser and image threads with the results
 				if (!result.equals("") && (null != result)) {
 					new ParserTask().execute(result);
 					new ImageTask().execute(result);
 					Log.d(TAG, "Request complete, Parsing tasks started.");
 				} else {
 					Log.e(TAG,
 							"Parsing tasks not started: null or empty string.");
 				}
 			}
 		} // End method onPostExecute()
 
 	} // End class HTTPTask
 
 	/**
 	 * AsyncTask to parse HTTP results.
 	 */
 	private class ParserTask extends AsyncTask<String, StockProperty, Void> {
 		@Override
 		protected Void doInBackground(String... params) {
 			Log.d(TAG, "ParserTask parsing results.");
 
 			// Get a Jsoup document for the string
 			final Document doc = Jsoup.parse(params[0]);
 
 			// Get all the meta elements from the document
 			final Elements metaElements = doc.getElementsByTag("meta");
 
 			// String to be displayed for the first error
 			String errorValue = "Not found";
 
 			// Parse the value for each property
 			for (final StockProperty property : m_propertyList) {
 				// Select the Element(s) containing the current property
 				final Elements currentElements = metaElements
 						.select("[itemprop=" + property.getPropertyName() + "]");
 
 				// Make sure there is at least one result
 				if (!currentElements.isEmpty()) {
 					// Get the value from the first element (we'll just assume
 					// the first one is the right one).
 					final String value = currentElements.first()
 							.attr("content");
 
 					// Set the StockProperty value to the parsed value
 					property.setNextValue(value);
 
 				} else {
 					// Set the value to something indicating an error
 					property.setNextValue(errorValue);
 					errorValue = "";
 				}
 
 				// Publish the values as they are parsed
 				publishProgress(property);
 			}
 
 			return null;
 		} // End method doInBackground()
 
 		@Override
 		protected void onProgressUpdate(StockProperty... properties) {
 			// Update the value for any properties provided
 			for (final StockProperty property : properties) {
 				property.updateValue();
 			}
 		}
 
 	} // End class ParserTask
 
 	/**
 	 * AsyncTask to handle downloading and updating the chart image.
 	 */
 	private class ImageTask extends AsyncTask<String, Void, Bitmap> {
 
 		@Override
 		protected Bitmap doInBackground(String... params) {
 			Log.d(TAG, "ImageTask downloading image.");
 
 			Bitmap chart = null;
 
 			// Get a Jsoup document for the string
 			final Document doc = Jsoup.parse(params[0]);
 
 			// Get all the meta elements from the document
 			final Elements metaElements = doc.getElementsByTag("meta");
 
 			// Select the Element(s) containing the image URL property
 			final Elements currentElements = metaElements.select("[itemprop="
 					+ IMAGE_PROPERTY + "]");
 
 			// Make sure there is at least one result
 			if (!currentElements.isEmpty()) {
 				// Get the value from the first element (we'll just assume
 				// the first one is the right one).
 				final String url = currentElements.first().attr("content");
 
 				try {
 					// Read the image from the URL
 					chart = BitmapFactory.decodeStream(new URL(url)
 							.openStream());
 
 					Log.d(TAG,
 							"ImageTask done downloading image, scaling image now.");
 
 					// Scale the bitmap to fit the ImageView width
 					double scale = m_chartImageView.getWidth()
 							/ chart.getWidth();
 					int newWidth = (int) (chart.getWidth() * scale);
 					int newHeight = (int) (chart.getHeight() * scale);
 					chart = Bitmap.createScaledBitmap(chart, newWidth,
 							newHeight, false);
 
 					Log.d(TAG, "ImageTask done scaling image.");
 
 				} catch (MalformedURLException e) {
 					Log.e(TAG, "Malformed URL parsed in ImageTask: " + url);
 				} catch (IOException e) {
 					Log.e(TAG, "Error opening stream from URL: " + url);
 				}
 
 			} else {
 				Log.d(TAG, "ImageTask unable to parse image URL from HTML.");
 			}
 
 			return chart;
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap result) {
 			// Update the image with the resulting bitmap
 			m_chartImageView.setImageBitmap(result);
 			if (null == result) {
 				Log.d(TAG, "ImageView cleared due to null Bitmap.");
 			}
 
 			// Enable to Go button to show the search is complete
 			m_goButton.setEnabled(true);
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
