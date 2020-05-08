 package com.kau.jonathan.umdschedulesharer;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Picture;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.webkit.JavascriptInterface;
 import android.webkit.WebView;
 import android.webkit.WebView.PictureListener;
 import android.webkit.WebViewClient;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.UiLifecycleHelper;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.ProfilePictureView;
 
 public class SignInActivity extends Activity {
 	private static final int REAUTH_ACTIVITY_CODE = 100;
 	public static final String headerString = "<center>";
 	ProgressDialog loggingIn;
 	ProgressDialog umdLoginDialog;
 	ProfilePictureView profilePictureView;
 	TextView username;
 	TextView title;
 	String fb_id;
 
 	private UiLifecycleHelper uiHelper;
 	private Session.StatusCallback callback = new Session.StatusCallback() {
 		@Override
 		public void call(final Session session, final SessionState state, final Exception exception) {
 			onSessionStateChange(session, state, exception);
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sign_in);
 
 		Intent intent = getIntent();
 
 		// Generate typefaces		
 		final Typeface face=Typeface.createFromAsset(this.getAssets(),
 				"fonts/Lato-Reg.ttf");
 		final Typeface boldface=Typeface.createFromAsset(this.getAssets(),
 				"fonts/Lato-Bol.ttf");
 
 
 		// Populate Spinner choices
 		Spinner spinner = (Spinner) findViewById(R.id.semester_spinner);
 		// Create an ArrayAdapter using the string array and a default spinner layout
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item){
 
 			public View getView(int position, View convertView, ViewGroup parent) {
 				View v = super.getView(position, convertView, parent);				
 				((TextView) v).setTypeface(face);
 				return v;
 			}
 
 
 			public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
 				View v =super.getDropDownView(position, convertView, parent);
 				((TextView) v).setTypeface(face);
 				return v;
 			}
 		};
 
 		String[] semesters = getResources().getStringArray(R.array.semesters_array);
 
 		for(String s: semesters) {
 			adapter.add(s);
 		}
 
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Apply the adapter to the spinner
 		spinner.setAdapter(adapter);
 
 		// Facebook Session
 		uiHelper = new UiLifecycleHelper(this, callback);
 		uiHelper.onCreate(savedInstanceState);
 
 		// Find the user's profile picture custom view
 		profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
 		profilePictureView.setCropped(true);
 
 		// Change typeface for all text
 		title = (TextView)findViewById(R.id.main_title);
 		username = (TextView)findViewById(R.id.selection_user_name);
 
 		EditText umd_username = (EditText) findViewById(R.id.umd_username);
 		EditText umd_password = (EditText) findViewById(R.id.umd_password);
 		Button umd_login = (Button) findViewById(R.id.umd_login);
 
 		title.setTypeface(face);
 		username.setTypeface(face);
 		umd_username.setTypeface(face);
 		umd_password.setTypeface(face);
 		umd_login.setTypeface(boldface);
 
 		// Get user data
 		Session session = Session.getActiveSession();
 		if (session != null && session.isOpened()) {
 			loggingIn = ProgressDialog.show(
					SignInActivity.this, "", "Logging into Facebook", true);
 			makeMeRequest(session);
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.sign_in, menu);
 		return true;
 	}
 
 	private void makeMeRequest(final Session session) {
 		// Make an API call to get user data and define a 
 		// new callback to handle the response.
 		Request request = Request.newMeRequest(session, 
 				new Request.GraphUserCallback() {
 			@Override
 			public void onCompleted(GraphUser user, Response response) {
 				// If the response is successful
 				loggingIn.dismiss();
 
 				// Show views
 				//profilePictureView.setVisibility(View.VISIBLE);
 				username.setVisibility(View.VISIBLE);
 				title.setVisibility(View.VISIBLE);
 				findViewById(R.id.login_box).setVisibility(View.VISIBLE);
 				findViewById(R.id.picture_frame).setVisibility(View.VISIBLE);
 
 				if (session == Session.getActiveSession()) {
 					if (user != null) {
 						// Set the id for the ProfilePictureView
 						// view that in turn displays the profile picture.
 						profilePictureView.setProfileId(user.getId());
 						// Set the Textview's text to the user's name.
 						username.setText(user.getName());
 
 						// Get Facebook number ID
 						fb_id = user.getId();
 
 
 						// Playing with image stuff
 						String imageUrl= "http://graph.facebook.com/" + fb_id + "/picture?type=large";
 						new RetrieveImgTask().execute(imageUrl);
 					}
 				}
 				if (response.getError() != null) {
 					// Handle errors, will do so later.
 				}
 			}
 		});
 		request.executeAsync();
 	} 
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == REAUTH_ACTIVITY_CODE) {
 			uiHelper.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 
 	}
 
 	// FB State Managers
 
 	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
 		if (session != null && session.isOpened()) {
 			// Get the user's data.
 			makeMeRequest(session);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		uiHelper.onResume();	
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle bundle) {
 		super.onSaveInstanceState(bundle);
 		uiHelper.onSaveInstanceState(bundle);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		uiHelper.onPause();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		uiHelper.onDestroy();
 	}
 
 	// For circular profile image
 
 	class RetrieveImgTask extends AsyncTask<String, Void, Void> {
 		private Bitmap bitmap;
 		private Bitmap output;
 		private Exception exception;
 
 		protected Void doInBackground(String... urls) {
 			try {
 				String imageUrl = urls[0];
 
 				URL newurl = null;
 				try {
 					newurl = new URL(imageUrl);
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 				bitmap = null;
 				try {
 					bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				return null;
 			} catch (Exception e) {
 				this.exception = e;
 				return null;
 			}
 		}
 
 		protected void onPostExecute(Void v) {
 
 
 			if (bitmap != null) {
 				Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
 						bitmap.getHeight(), Config.ARGB_8888);
 				Canvas canvas = new Canvas(output);
 
 				final int color = 0xff424242;
 				final Paint paint = new Paint();
 				final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
 
 				paint.setAntiAlias(true);
 				canvas.drawARGB(0, 0, 0, 0);
 				paint.setColor(color);
 				canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
 						bitmap.getWidth() / 2, paint);
 				paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
 				canvas.drawBitmap(bitmap, rect, rect, paint);
 
 				((ImageView)findViewById(R.id.test_prof_pic)).setImageBitmap(output);
 			}
 		}
 	}
 
 	// When user presses sign in	
 	public void umdSignInAction(View v) {
 //		WebView view = (WebView) findViewById(R.id.login_page);
 //		LayoutParams lp = view.getLayoutParams();    
 //		lp.width=1000;   
 //		lp.height=1000;   
 //		view.setLayoutParams(lp);
		InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
	    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
 		umdLoginDialog = ProgressDialog.show(
 				SignInActivity.this, "", "Signing In", true);
 		loginProcess();
 	}
 
 	//////////////////////////////////////////////////////////////////////////////////////
 	// Capturing Schedule Code
 
 
 	@SuppressLint({ "JavascriptInterface", "NewApi" })
 	public void loginProcess() {
 
 		String loadUrl = "http://mobilemy.umd.edu/";
 
 		// Initialize the WebView and edit settings
 		WebView view = (WebView) findViewById(R.id.login_page);
 		view.getSettings().setJavaScriptEnabled(true);
 		view.getSettings().setBuiltInZoomControls(true);
 		view.getSettings().setDomStorageEnabled(true);
 		view.getSettings().setLoadWithOverviewMode(true);
 		view.getSettings().setUseWideViewPort(true);
 		view.getSettings().setAllowUniversalAccessFromFileURLs(true);
 		view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
 		view.getSettings().setSavePassword(false);
 		view.getSettings().setSaveFormData(false);
 		view.clearCache(true);
 		view.clearHistory();
 		view.clearSslPreferences();
 		view.clearFormData();
 
 		CookieSyncManager.createInstance(this);
 		CookieSyncManager.getInstance().startSync();
 		CookieManager.getInstance().setAcceptCookie(true);
 		CookieManager.getInstance().removeAllCookie();
 
 
 		/* Register a new JavaScript interface called HTMLOUT */
 		view.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");
 
 		// Sets the webview client for loading and accessing the HTML source of the schedule
 		view.setWebViewClient(new WebViewClient() {
 			int count = 0;
 
 			@Override  
 			public boolean shouldOverrideUrlLoading(WebView view, String url)  
 			{  
 				return false; 
 			}  
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				if (count == 0) {
 					count++;
 					// Sets the webview client for loading and accessing the HTML source of the schedule
 					view.setPictureListener(new PictureListener() {  
 						int count = 0;
 
 						public void onNewPicture(WebView view, Picture picture) {
 
 							if (count == 0) {
 								count++;
 
 								//Toast.makeText(SignInActivity.this, "Signed into my.umd.edu!", Toast.LENGTH_SHORT).show();
 
 								// Wait for completed login using UID       
 								CookieManager manager = CookieManager.getInstance();
 								//Toast.makeText(SignInActivity.this, Boolean.toString(manager.hasCookies()), Toast.LENGTH_SHORT).show();
 								//Toast.makeText(SignInActivity.this, "Cookie URL: " + view.getUrl(), Toast.LENGTH_SHORT).show();
 
 								if((manager.getCookie(view.getUrl()) != null && manager.getCookie(view.getUrl()).contains("true")) || !view.getUrl().contains("0")) {
 									// Sets the webview client for loading and accessing the HTML source of the schedule
 									view.setWebViewClient(new WebViewClient() {
 										int count = 0;
 										@Override  
 										public boolean shouldOverrideUrlLoading(WebView view, String url)  
 										{  
 											return false; 
 										}  
 
 										@Override
 										public void onPageFinished(WebView view, String url) {
 											if(count == 0) {
 												//Toast.makeText(SignInActivity.this, "Loaded schedule page!", Toast.LENGTH_SHORT).show();
 												count++;
 
 
 												// Load the actual schedule page
 												view.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
 											}
 
 											//Toast.makeText(SignInActivity.this, "Herro!", Toast.LENGTH_SHORT).show();
 										}
 									});
 
 									view.loadUrl("https://mobilemy.umd.edu/portal/server.pt/gateway/PTARGS_0_340574_368_211_0_43/https%3B/www.sis.umd.edu/testudo/studentSched?term=201401");
 
 								}
 							}
 
 						}
 					});
 
 					String umd_username = ((EditText) findViewById(R.id.umd_username)).getText().toString();
 					String umd_password = ((EditText) findViewById(R.id.umd_password)).getText().toString();
 
 					view.loadUrl("javascript:(function() { " +  
 							"document.LoginPortletForm.in_tx_username.value='" + umd_username + "'; " +  
 							"document.LoginPortletForm.in_pw_userpass.value='" + umd_password + "'; " +
 							"document.LoginPortletForm.submit(); " +
 							"})()");
 				}
 
 				//Toast.makeText(SignInActivity.this, url, Toast.LENGTH_LONG).show();
 
 				// Wait for completed login using UID       
 				CookieManager manager = CookieManager.getInstance();
 				//Toast.makeText(SignInActivity.this, Boolean.toString(manager.hasCookies()), Toast.LENGTH_SHORT).show();
 				//Toast.makeText(SignInActivity.this, "Cookie URL: " + view.getUrl(), Toast.LENGTH_SHORT).show();
 
 				if((manager.getCookie(view.getUrl()) != null && manager.getCookie(view.getUrl()).contains("true")) || !view.getUrl().contains("0")) {
 					if(!view.getUrl().contains("0")) { // Incorrect login
 						Toast.makeText(SignInActivity.this, "Incorrect login", Toast.LENGTH_SHORT).show();
 						umdLoginDialog.dismiss();
 					} else { // Correct login
 						// Sets the webview client for loading and accessing the HTML source of the schedule
 						view.setWebViewClient(new WebViewClient() {
 							int count = 0;
 							@Override  
 							public boolean shouldOverrideUrlLoading(WebView view, String url)  
 							{  
 								return false; 
 							}  
 
 							@Override
 							public void onPageFinished(WebView view, String url) {
 								if(count == 0) {
 									//Toast.makeText(SignInActivity.this, "Loaded schedule page!", Toast.LENGTH_SHORT).show();
 									count++;
 
 
 									// Load the actual schedule page
 									view.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
 								}
 
 								//Toast.makeText(SignInActivity.this, "Herro!", Toast.LENGTH_SHORT).show();
 							}
 						});
 
 						view.loadUrl("https://mobilemy.umd.edu/portal/server.pt/gateway/PTARGS_0_340574_368_211_0_43/https%3B/www.sis.umd.edu/testudo/studentSched?term=201401");
 
 					}
 				}
 
 			}
 		});
 
 		// Load the actual schedule page
 		//view.loadUrl("https://mobilemy.umd.edu/portal/server.pt/gateway/PTARGS_0_340574_368_211_0_43/https%3B/www.sis.umd.edu/testudo/studentSched?term=201401");
 		view.loadUrl("https://mobilemy.umd.edu/portal/server.pt/mypage/home_page/0");
 	}
 
 
 	// Captures the HTML source of the schedule webpage
 	class MyJavaScriptInterface
 	{
 		int count = 0;
 		Context mContext;
 
 		MyJavaScriptInterface(Context c) {
 			mContext = c;
 		}
 
 		@SuppressLint("NewApi")
 		@SuppressWarnings("deprecation")
 		@JavascriptInterface
 		public void processHTML(String html)
 		{
 			//Toast.makeText(SignInActivity.this, "Herro anybody" + html, Toast.LENGTH_SHORT).show();
			//WebView scheduleBrowser = (WebView) findViewById(R.id.screenshot_page);
 			//scheduleBrowser.removeJavascriptInterface("HTMLOUT");
 
 			if(count == 0) {
 				count++;
 
 				//Toast.makeText(SignInActivity.this, "Processing HTML!" + html, Toast.LENGTH_SHORT).show();
 
 				// Determines the beginning and end of just the schedule
 				int beginIndex = html.indexOf(headerString);
 				int endIndex = html.indexOf("</table>", beginIndex) + 7;
 				if(beginIndex == -1) beginIndex = 0;
 				//Toast.makeText(SignInActivity.this, "Indices found" + beginIndex + (endIndex), Toast.LENGTH_SHORT).show();
 
 				// Crops the substring of HTML source
 				final String scheduleTable = html.substring(beginIndex, endIndex + 1);
 				Log.v("FUCK THIS SHIT", scheduleTable);
 
 				//Toast.makeText(SignInActivity.this, "Cropped", Toast.LENGTH_SHORT).show();
 				//Toast.makeText(SignInActivity.this, "Schedule: " + scheduleTable, Toast.LENGTH_SHORT).show();
 
 
				final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><head><meta name='viewport' content='target-densityDpi=device-dpi, initial-scale = 1.2, minimum-scale=1.2'/></head>";
 
 				umdLoginDialog.dismiss();
 				
 				// Take bitmap and send with intent to Schedule Activity
 				Intent intent = new Intent(SignInActivity.this, ScheduleActivity.class);
 
 				// Attach source code
 				intent.putExtra("SOURCE_CODE", header + scheduleTable);			
 
 
 				// Start activity
 				startActivity(intent);
 
 				// Take screenshot of Webview and convert to Bitmap
 				//scheduleBrowser.setVisibility(View.GONE);
 
 
 				// Sets the WebView which will just hold the schedule itself
 				//				scheduleBrowser.setPictureListener(new PictureListener() {  
 				//					int count = 0;
 				//
 				//					public void onNewPicture(WebView view, Picture picture) {
 				//						if(count == 0) {
 				//							count++;							
 
 
 				//							Picture screenshot = view.capturePicture();
 				//							Toast.makeText(SignInActivity.this, "Height: " + screenshot.getHeight() + " Width: " + screenshot.getWidth(), Toast.LENGTH_SHORT).show();
 				//
 				//							PictureDrawable pictureDrawable = new PictureDrawable(screenshot);
 				//							Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(),pictureDrawable.getIntrinsicHeight(), Config.ARGB_8888);
 				//							Canvas canvas = new Canvas(bitmap);
 				//							canvas.drawPicture(pictureDrawable.getPicture());
 				//
 				//							// Crop bitmap by calling function		
 				//							Bitmap cropped = bitmap;
 				//
 				//
 				//							//Convert to byte array
 				//							ByteArrayOutputStream stream = new ByteArrayOutputStream();
 				//							cropped.compress(Bitmap.CompressFormat.PNG, 100, stream);
 				//							byte[] byteArray = stream.toByteArray();
 				//							intent.putExtra("image",byteArray);
 
 				//						
 				//						}
 				//
 				//					}
 				//				});
 
 
 				// Loads HTML source for just the schedule
 				//scheduleBrowser.loadData(header + scheduleTable, "text/html", null);
 
 			}    
 		}
 	}
 
 
 
 	// This function takes the WebView screenshot and crops away a large portion of the white space,
 	// returning the cropped Bitmap file.
 	public Bitmap cropBitmap(Bitmap original) {
 		int x = 0, y = 0, width = 0, height = 0;
 
 		// Calculate lower margin
 		int tempX = original.getWidth() / 2;
 		int tempY = original.getHeight() - 1;
 
 		while(original.getPixel(tempX, tempY) == Color.WHITE) {
 			tempY--;
 		}
 
 		height = tempY + 10;
 
 		// Calculate side margin
 		tempX = 0;
 		tempY = original.getHeight() / 5;
 
 		while(original.getPixel(tempX, tempY) == Color.WHITE) {
 			tempX++;
 		}
 
 		x = tempX - 10;
 		width = original.getWidth() - 2 * tempX + 20;
 
 		// Crop bitmap
 		return Bitmap.createBitmap(original, x, y, width, height, null, false);
 	}
 
 
 	//
 	//////////////////////////////////////////////////////////////////////////////////////
 
 
 }
