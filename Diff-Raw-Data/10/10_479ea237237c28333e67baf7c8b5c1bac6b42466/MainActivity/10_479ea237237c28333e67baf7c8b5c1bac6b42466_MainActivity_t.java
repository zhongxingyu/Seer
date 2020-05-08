 package de.karg.wavecamp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Display;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private EditText text1;
 	private TextView text2;
 	private WebView webview;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         //text2 = (TextView) findViewById(R.id.textView1);
         //text2.setText("Hallo Hans!");
     	Display display = getWindowManager().getDefaultDisplay();
     	int height = display.getHeight();
         get_news_content();
         webview = (WebView) findViewById(R.id.WebView1);
     	//webview.loadData("<html><head></head><body><img align='center' height='"+(height-20)+"px' src='../android_asset/wavecamp_title.jpg' /></body></html>", "text/html", "UTF-8");
     	webview.loadUrl("file:///android_asset/logo.html");
     }
     
     public void get_webcam_content() {
     	String str;
     	str ="";
     	String webcamText="";
     	try {
     	    // Create a URL for the desired page
     	    URL url = new URL("http://wavecamp.eu/webcam/webcam_android.php");
     	    
     	    // Read all the text returned by the server
     	    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
     	    
     	    while ((str = in.readLine()) != null) {
     	        // str is one line of text; readLine() strips the newline character(s)
     	    	webcamText+=str;
     	    }
     	    in.close();
     	    //System.out.println(str);
     	    Button button2 = (Button) findViewById(R.id.button2);
             button2.setText("Refresh");
 
     	} catch (MalformedURLException e) {
     		webcamText="MalformedURL motherfucker";
     		
     	} catch (IOException e) {
     		webcamText="Du hasch glaub koi Internet du Seggl";
     	}
     	int width;
     	Display display = getWindowManager().getDefaultDisplay();
     	width=display.getWidth();
     	webview = (WebView) findViewById(R.id.WebView1);
    	webview.getSettings().setLoadWithOverviewMode(true);
    	webview.getSettings().setUseWideViewPort(true);
    	webview.getSettings().setBuiltInZoomControls(true);
    	webview.getSettings().setSupportZoom(true); 
    	//webview.setInitialScale(100);
    	//webview.loadData("<html><head></head><body><img align='center' width='"+(width-5)+"px' src='http://wavecamp.eu/webcam/webcam.jpg' />"+webcamText+"</body></html>", "text/html", "UTF-8");
    	webview.loadData("<div style='align:center; background-color: FFFFFF;'><img align='center' style='horizontal-align:center;' src='http://wavecamp.eu/webcam/webcam.jpg' />"+webcamText+"</div>", "text/html", "UTF-8");
     	
     }
     
     public void get_news_content() {
     	String str;
     	str ="";
     	String newsText="<body background='file:///android_asset/news_gb.jpg'><b>News vom Lift:</b><br/><br/>";
     	try {
     	    // Create a URL for the desired page
     	    URL url = new URL("http://wavecamp.eu/news_de.php");
     	    
     	    // Read all the text returned by the server
     	    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
     	    
     	    while ((str = in.readLine()) != null) {
     	        // str is one line of text; readLine() strips the newline character(s)
     	    	newsText+=str;
     	    }
     	    in.close();
     	    //System.out.println(str);
     	    newsText+="</body>";
     	    Button button2 = (Button) findViewById(R.id.button2);
             button2.setText("Webcam");
 
     	} catch (MalformedURLException e) {
     		newsText="MalformedURL motherfucker";
     		
     	} catch (IOException e) {
     		newsText="Du hasch glaub koi Internet du Seggl";
     	}
     	
     	webview = (WebView) findViewById(R.id.WebView1);
     	webview.loadData(newsText, "text/html", "UTF-8");
     	//text2 = (TextView) findViewById(R.id.textView1);
         //text2.setText(str);
     }
     
     public void webcam_handler(View view) {
     	get_webcam_content();
       }
     
     public void news_handler(View view) {
     	get_news_content();
       }
     /*
     public void webcam_handler(Bundle savedInstanceState) {
         //super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         text2 = (TextView) findViewById(R.id.textView1);
         text2.setText("Hallo Webcam!");
        
 
         // load something on startup
     }*/
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
