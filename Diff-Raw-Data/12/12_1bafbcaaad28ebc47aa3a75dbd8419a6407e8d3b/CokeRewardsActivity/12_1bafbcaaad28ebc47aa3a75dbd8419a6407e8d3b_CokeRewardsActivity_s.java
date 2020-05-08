 package com.brianreber.cokerewards;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerPNames;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.SingleClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 /**
  * Main activity for the app
  * 
  * @author breber
  */
 public class CokeRewardsActivity extends Activity {
 	/**
 	 * SharedPreference name
 	 */
 	public static final String COKE_REWARDS = "CokeRewards";
 
 	/**
 	 * Email address preference key
 	 */
 	public static final String EMAIL_ADDRESS = "emailAddress";
 
 	/**
 	 * Password preference key
 	 */
 	public static final String PASSWORD = "password";
 
 	/**
 	 * Logged in state preference key
 	 */
 	public static final String LOGGED_IN = "loggedIn";
 
 	/**
 	 * Point count preference key
 	 */
 	public static final String POINTS = "points";
 
 	/**
 	 * Screen name preference key
 	 */
 	public static final String SCREEN_NAME = "screenName";
 
 	/**
 	 * Result of latest code entry
 	 */
 	public static final String ENTER_CODE_RESULT = "enterCodeResult";
 
 	/**
 	 * Result of messages
 	 */
 	public static final String MESSAGES = "messagesResult";
 
 	/**
 	 * URL to POST requests to
 	 */
 	private static final String URL = "http://www.mycokerewards.com/xmlrpc";
 
 	/**
 	 * Secure URL to POST requests to
 	 */
 	private static final String SECURE_URL = "https://secure.mycokerewards.com/xmlrpc";
 
 	/**
 	 * Request code for starting our RegisterActivity
 	 */
 	private static final int REGISTER_REQUEST_CODE = 1000;
 
 	/**
 	 * Handler to use for the threads
 	 */
 	private static Handler handler = new Handler();
 
 	/**
 	 * The request response
 	 */
 	private static String mResult;
 
 	/**
 	 * The view where ads with display
 	 */
 	private AdView adView;
 
 	/**
 	 * Google Analytics tracker
 	 */
 	private GoogleAnalyticsTracker tracker;
 
 	/**
 	 * Dialog to display that we are submitting a code
 	 */
 	private ProgressDialog dlg;
 
 	/**
 	 * A Runnable that will update the UI with values stored
 	 * in SharedPreferences
 	 */
 	private Runnable updateUIRunnable = new Runnable() {
 		@Override
 		public void run() {
 			SharedPreferences prefs = getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 
 			TextView tv = (TextView) findViewById(R.id.numPoints);
 			tv.setText("Number of Points: " + prefs.getString(POINTS, ""));
 
 			tv = (TextView) findViewById(R.id.screenName);
 			String name = ("".equals(prefs.getString(SCREEN_NAME, "")) ?
 					prefs.getString(EMAIL_ADDRESS, "") : prefs.getString(SCREEN_NAME, ""));
 
 			tv.setText("Welcome " + name + "!");
 		}
 	};
 
 	/**
 	 * A Runnable that will update the UI with values stored
 	 * in SharedPreferences
 	 */
 	private Runnable codeUpdateRunnable = new Runnable() {
 		@Override
 		public void run() {
 			SharedPreferences prefs = getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 
 			if (prefs.getBoolean(ENTER_CODE_RESULT, false)) {
 				EditText tv = (EditText) findViewById(R.id.code);
 				tv.setText("");
 
 				Toast.makeText(getApplicationContext(), "Code submitted successfully", Toast.LENGTH_SHORT).show();
 				tracker.trackEvent("SuccessfulCode", "SuccessfulCode", "SuccessfulCodeSubmission", 0);
 			} else {
 				String messages = prefs.getString(MESSAGES, "");
 				Toast.makeText(getApplicationContext(), "Code submission failed..." + messages, Toast.LENGTH_SHORT).show();
 
 				tracker.trackEvent("FailedCode", "FailedCode", "Code submission failed: " + messages, 0);
 			}
 
 			getNumberOfPoints();
 		}
 	};
 
 	/**
 	 * A Runnable that will show an error Toast
 	 */
 	private Runnable errorRunnable = new Runnable() {
 		@Override
 		public void run() {
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 
 			Toast.makeText(CokeRewardsActivity.this, "An error has occurred. Please try again.", Toast.LENGTH_SHORT).show();
 		}
 	};
 
 
 	/**
 	 * Set up the basic UI elements
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// Create the adView
 		adView = new AdView(this, AdSize.BANNER, "a14f11d378bdbae");
 
 		// Lookup your LinearLayout assuming it's been given
 		// the attribute android:id="@+id/mainLayout"
 		LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);
 
 		// Add the adView to it
 		layout.addView(adView);
 
 		// Initiate a generic request to load it with an ad
 		adView.loadAd(new AdRequest());
 
 		tracker = GoogleAnalyticsTracker.getInstance();
 
 		// Start the tracker in manual dispatch mode...
 		tracker.startNewSession("UA-3673402-16", 20, this);
 		tracker.setAnonymizeIp(true);
 
 		dlg = new ProgressDialog(this);
 		dlg.setCancelable(false);
 		dlg.setMessage(getResources().getText(R.string.submitting));
 
 		SharedPreferences prefs = getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 		TextView tv = (TextView) findViewById(R.id.numPoints);
 		tv.setText("Number of Points: " + prefs.getString(POINTS, ""));
 
 		tv = (TextView) findViewById(R.id.screenName);
 		String name = ("".equals(prefs.getString(SCREEN_NAME, "")) ?
 				prefs.getString(EMAIL_ADDRESS, "") : prefs.getString(SCREEN_NAME, ""));
 
 		tv.setText("Welcome " + name + "!");
 
 		Button submitCode = (Button) findViewById(R.id.submitCode);
 		submitCode.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				EditText tv = (EditText) findViewById(R.id.code);
 				String code = tv.getText().toString();
 
 				code = code.replace(" ", "");
 				final String finalCode = code.toUpperCase();
 
 				if (finalCode.length() < 10) {
 					Toast.makeText(CokeRewardsActivity.this, "Code not long enough", Toast.LENGTH_SHORT).show();
 					return;
 				}
 
 				dlg.show();
 
 				new Thread(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							tracker.trackEvent("Button", "SubmitCode", "Submit Code button clicked", 0);
 							try {
 								getData(CokeRewardsActivity.this, CokeRewardsRequest.createCodeRequestBody(CokeRewardsActivity.this, finalCode), codeUpdateRunnable, true);
 							} catch (Exception e) {
 								tracker.trackEvent("Exception", "ExceptionSecureSubmitCode", "Submit Code encountered an exception: " + e.getMessage(), 0);
 								e.printStackTrace();
 								getData(CokeRewardsActivity.this, CokeRewardsRequest.createCodeRequestBody(CokeRewardsActivity.this, finalCode), codeUpdateRunnable, false);
 							}
 						} catch (Exception e) {
 							tracker.trackEvent("Exception", "ExceptionSubmitCode", "Submit Code encountered an exception: " + e.getMessage(), 0);
 							handler.post(errorRunnable);
 							e.printStackTrace();
 						}
 					}
 				}).start();
 			}
 		});
 
 		if (isLoggedIn()) {
 			getNumberOfPoints();
 		} else {
 			Intent register = new Intent(this, RegisterActivity.class);
 			startActivityForResult(register, REGISTER_REQUEST_CODE);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		adView.destroy();
 
 		// Stop the tracker when it is no longer needed.
 		tracker.stopSession();
 
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		getNumberOfPoints();
 	}
 
 	/**
 	 * Get the number of points from the MyCokeRewards server
 	 */
 	private void getNumberOfPoints() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					tracker.trackEvent("CountFetch", "CountFetch", "Fetched number of points", 0);
 					try {
 						getData(CokeRewardsActivity.this, CokeRewardsRequest.createLoginRequestBody(CokeRewardsActivity.this), updateUIRunnable, true);
 					} catch (Exception e) {
 						tracker.trackEvent("Exception", "ExceptionSecureGetNumPoints", "Get number of points encountered an exception: " + e.getMessage(), 0);
 						e.printStackTrace();
 						getData(CokeRewardsActivity.this, CokeRewardsRequest.createLoginRequestBody(CokeRewardsActivity.this), updateUIRunnable, false);
 					}
 				} catch (Exception e) {
 					tracker.trackEvent("Exception", "ExceptionGetNumPoints", "Get number of points encountered an exception: " + e.getMessage(), 0);
 					handler.post(errorRunnable);
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * Do we have a logged in user?
 	 * 
 	 * @return whether we have a valid email address, password, and have successfully
 	 * logged in with the most recent request
 	 */
 	private boolean isLoggedIn() {
 		return isLoggedIn(this);
 	}
 
 	/**
 	 * Do we have a logged in user?
 	 * 
 	 * @return whether we have a valid email address, password, and have successfully
 	 * logged in with the most recent request
 	 */
 	public static boolean isLoggedIn(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 		return prefs.contains(EMAIL_ADDRESS) && prefs.contains(PASSWORD) && prefs.getBoolean(LOGGED_IN, false);
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		int menuId = item.getItemId();
 
 		if (menuId == R.id.logout) {
 			SharedPreferences prefs = getSharedPreferences(CokeRewardsActivity.COKE_REWARDS, Context.MODE_WORLD_WRITEABLE);
 			Editor edit = prefs.edit();
 
 			edit.remove(EMAIL_ADDRESS);
 			edit.remove(PASSWORD);
 			edit.remove(LOGGED_IN);
 			edit.commit();
 
 			Intent register = new Intent(this, RegisterActivity.class);
 			startActivityForResult(register, REGISTER_REQUEST_CODE);
 		}
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menus, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (isLoggedIn()) {
 			getNumberOfPoints();
 		} else {
 			finish();
 		}
 	}
 
 	/**
 	 * Create an HTTPClient that supports HTTP and HTTPS.
 	 * 
 	 * @return the HTTPClient
 	 */
 	private static HttpClient createHttpClient() {
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
 
 		HttpParams params = new BasicHttpParams();
 		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
 		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
 		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 
 		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
 		return new DefaultHttpClient(cm, params);
 	}
 
 
 	/**
 	 * Make a server request and update preferences accordingly.
 	 * 
 	 * @param postValue The value to POST to the server
 	 * @param mRunnable The Runnable to start after we are done processing
 	 * @throws IOException
 	 * @throws XPathExpressionException
 	 * @throws SAXException
 	 * @throws ParserConfigurationException
 	 */
 	public static void getData(Context ctx, String postValue, Runnable mRunnable, boolean isSecure) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
 		HttpClient httpClient = createHttpClient();
 		HttpPost httppost = new HttpPost(isSecure ? SECURE_URL : URL);
 
 		StringEntity se = new StringEntity(postValue);
 		se.setContentType("text/xml");
 		httppost.setEntity(se);
 
 		HttpResponse response = httpClient.execute(httppost);
 
 		InputStream input = response.getEntity().getContent();
 
 		mResult = readStreamAsString(input);
 
 		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
 		builderFactory.setNamespaceAware(true);
 		DocumentBuilder builder = builderFactory.newDocumentBuilder();
 		Document document = builder.parse(new InputSource(new StringReader(mResult)));
 		XPath xpath = XPathFactory.newInstance().newXPath();
 
 		SharedPreferences prefs = ctx.getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_WRITEABLE);
 		Editor edit = prefs.edit();
 
 		NodeList nodes = (NodeList) xpath.evaluate("/methodResponse//member/value[../name/text()='POINTS']", document, XPathConstants.NODESET);
 
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node n = nodes.item(i);
 			edit.putString(POINTS, n.getTextContent());
 		}
 
 		nodes = (NodeList) xpath.evaluate("/methodResponse//member/value[../name/text()='LOGIN_RESULT']", document, XPathConstants.NODESET);
 
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node n = nodes.item(i);
 			edit.putBoolean(LOGGED_IN, Boolean.parseBoolean(n.getTextContent()));
 		}
 
 		nodes = (NodeList) xpath.evaluate("/methodResponse//member/value[../name/text()='SCREEN_NAME']", document, XPathConstants.NODESET);
 
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node n = nodes.item(i);
 			String screenName = n.getTextContent();
 			if (!"".equals(screenName)) {
 				edit.putString(SCREEN_NAME, screenName);
 			}
 		}
 
 		nodes = (NodeList) xpath.evaluate("/methodResponse//member/value[../name/text()='ENTER_CODE_RESULT']", document, XPathConstants.NODESET);
 
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node n = nodes.item(i);
 			edit.putBoolean(ENTER_CODE_RESULT, Boolean.parseBoolean(n.getTextContent()));
 		}
 
 		nodes = (NodeList) xpath.evaluate("/methodResponse//member/value[../name/text()='MESSAGES']", document, XPathConstants.NODESET);
 
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node n = nodes.item(i);
 			edit.putString(MESSAGES, n.getTextContent());
 		}
 
 		edit.commit();
 
 		handler.post(mRunnable);
 	}
 
 	/**
 	 * Reads an entire input stream as a String. Closes the input stream.
 	 */
 	public static String readStreamAsString(InputStream in) {
 		try {
 			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
 			byte[] buffer = new byte[1024];
 			int count;
 			do {
 				count = in.read(buffer);
 				if (count > 0) {
 					out.write(buffer, 0, count);
 				}
 			} while (count >= 0);
 			return out.toString("UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("The JVM does not support the compiler's default encoding.", e);
 		} catch (IOException e) {
 			return null;
 		} finally {
 			try {
 				in.close();
 			} catch (IOException ignored) {	}
 		}
 	}
 }
