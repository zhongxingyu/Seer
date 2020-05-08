 package DroidSocial.Facebook;
 
 import java.net.URLDecoder;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 
 public class FBWebViewLogin extends Activity {
 	
 	private String url = "http://www.facebook.com/login.php?";
 	private WebView fb_login_view = null;
 	private RelativeLayout LoginScreen;
 	private ViewGroup.LayoutParams LoginScreenParams = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, 250);
 	
 	
 	/**
 	 * @Opens a web view allowing the user to log into their facebook account
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// get the bundle
 		Bundle extras = this.getIntent().getExtras();
 		
 		// get the query string
 		String queryStr = extras.getString(Facebook.FB_LOGIN_PARAMS);
 		
 		if (queryStr != null) url += queryStr;
 		
 		// Create the objects we need			
 		this.fb_login_view = new WebView(this);
 		this.LoginScreen = new RelativeLayout(this);
 		this.setContentView(this.LoginScreen);
 		
 		// Set the web view's layout params and the client to intercept url events
 		this.fb_login_view.setLayoutParams(this.LoginScreenParams);
 		this.fb_login_view.setWebViewClient(new FBLoginWebClient(this));
 		
 		// Add the web view to the screen
 		this.LoginScreen.addView(this.fb_login_view);
 		
 		// go to facebook.com/login.php!!
 		this.fb_login_view.loadUrl(url);
 		
 	}
 	
 	private class FBLoginWebClient extends WebViewClient {
 		
 		private Activity activity;
		
		public FBLoginWebClient(Activity a) {
			super();
			this.activity = a;
		}
 		
 		@Override
 		/**
 		 * Tells the system that we want to load the given uri into the facebook login webview.
 		 * When facebook redirects to the success page, the json session data is intercepted here.
 		 */
 	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			int nextIndex = url.indexOf(Facebook.LOGIN_NEXT);
 			if (nextIndex != -1) {
 				// look for the session data, will facebook ever send anything back to us that contains
 				// data after the JSON session string?
 				nextIndex = url.indexOf("session=");
 				// get the json code
 				String json_code = URLDecoder.decode(url.substring(nextIndex));
				nextIndex = json_code.indexOf("=");
				json_code = json_code.substring(nextIndex + 1);
 				
 				this.activity.finish();
 				return true;
 			}
 			view.loadUrl(url);
 	        return true;
 	    }
 	}
 }
