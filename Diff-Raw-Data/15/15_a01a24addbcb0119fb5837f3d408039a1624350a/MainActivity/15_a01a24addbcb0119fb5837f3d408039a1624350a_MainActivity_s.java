 package com.android.paragraphselector;
 
 import java.lang.reflect.Method;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 /*
  * Main class for Paragraph Selector
  * @author Sinan Shoukath
  */
 public class MainActivity extends Activity {
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);
     final WebView webView = (WebView)findViewById(R.id.webview);
     final ProgressDialog dialog = ProgressDialog.show(this, "Paragraph Selector", "Loading data");
     webView.loadUrl("file:///android_asset/input_text.txt");
     webView.setWebViewClient(new WebViewClient() {
       @Override
       public void onPageFinished(WebView view, String url) {
         dialog.dismiss();
       }
     });
    final EditText editText = (EditText)findViewById(R.id.edittext);
     ImageButton searchButton = (ImageButton)findViewById(R.id.search);
     searchButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
        searchText(editText.getText().toString(), webView, true, true);
       }
     });
     ImageButton upButton = (ImageButton)findViewById(R.id.search_up);
     upButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
        searchText(editText.getText().toString(), webView, false, false);
       }
     });
     ImageButton downButton = (ImageButton)findViewById(R.id.search_down);
     downButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
        searchText(editText.getText().toString(), webView, true, false);
       }
     });
   }
 
   @SuppressWarnings("deprecation")
   private void searchText(String text, WebView webView, boolean direction, boolean showToast) {
     int count = webView.findAll(text);
     webView.setSelected(true);
     webView.findNext(direction);
     try {
       for(Method m : WebView.class.getDeclaredMethods()) {
         if(m.getName().equals("setFindIsUp")) {
           m.setAccessible(true);
           m.invoke(webView, true);
         }
       }
     }catch (Exception ignored) {
     }
     if(showToast) {
       if(count==0) {
         Toast toast = Toast.makeText(getApplicationContext(), "Not found", Toast.LENGTH_SHORT);
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.show();
      }
      else {
         Toast toast = Toast.makeText(getApplicationContext(),
             text + " found at " + String.valueOf(count) + " locations", Toast.LENGTH_SHORT);
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.show();
       }
     }
   }
 }
