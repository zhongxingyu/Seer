 package edu.sou.rover2013.activities;
 
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
import android.widget.EditText;
 import android.widget.TextView;
 import edu.sou.rover2013.BaseActivity;
 import edu.sou.rover2013.R;
 
 public class ControlWebActivity extends BaseActivity {
 
 	// UI Elements
 	private static WebView webView;
 	private static Button button1;
 	private static Button button2;
 	private static Button button3;
 	private static TextView textView;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.web_activity);
 
 		// *******************************
 		// Assigning UI Elements
 		// *******************************
 		webView = (WebView) findViewById(R.id.webView1);
 		button1 = (Button) findViewById(R.id.button1);
 		button2 = (Button) findViewById(R.id.button2);
 		button3 = (Button) findViewById(R.id.button3);
 		textView = (TextView) findViewById(R.id.textView1);
 		
 		// *******************************
 		// Button Listeners
 		// *******************************
 		button1.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				textView.append("Button Pushed\n");
 			}
 		});
 		button2.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				textView.setText("");
 			}
 		});
 		button3.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				webView.loadUrl("http://slashdot.org/");
 			}
 		});
 		
 
 		//************** Web Stuff
 		// http://developer.android.com/reference/android/webkit/WebView.html
 
 		// Jeff, here's something useful on getting webview content
 		// http://stackoverflow.com/questions/8200945/how-to-get-html-content-from-a-webview
 		// Alternatively, you might be able to directly connect to the server to
 		// get scripts, rather than parsing through html
 
 		// Simplest usage: note that an exception will NOT be thrown
 		// if there is an error loading this page (see below).
 		webView.loadUrl("http://webpages.sou.edu/~rogo/");
 
 		// OR, you can also load from an HTML string:
 		// String summary =
 		// "<html><body>You scored <b>192</b> points.</body></html>";
 		// webView.loadData(summary, "text/html", null);
 		// ... although note that there are restrictions on what this HTML can
 		// do.
 		// See the JavaDocs for loadData() and loadDataWithBaseURL() for more
 		// info.
 
 	}
 
 }
