 package com.shining.bookmanager;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo.State;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.os.StrictMode;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScanBook extends Activity implements OnClickListener{
 	
 	 protected static final int GUIUPDATEIDENTIFIER = 0x101;   
 	
 	
 //	private static String APIKey="003afe0642e755f700b0fa12c8b601e5";
 //	private static String URL = "http://api.douban.com/book/subject/isbn/";
 //	private static String PATH_COVER = Environment.getExternalStorageDirectory() + "/BookMangerData/";   
 	//private String PATH_COVER = getApplicationContext().getFilesDir().getAbsolutePath()+/BookMangerData/;
 	
 	private String isbn;
 	
 	private Bitmap bitmap;
 	private Button button_scan;
 	private TextView textview_isbn;
 	private TextView textview_name;
 	private TextView textview_author;
 	private TextView textview_summary;
 	private ImageView imageview_book;
 	private WebView resultWeb;
 	
 	//private BookInfo bookInfo;
 	
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
     	if (android.os.Build.VERSION.SDK_INT >= 9) {
     	      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
     	      StrictMode.setThreadPolicy(policy);
     	    }
     	
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.tab_scan);
         
         button_scan=(Button)findViewById(R.id.button_scan);
         button_scan.setOnClickListener(this);
         
         
         
         textview_isbn=(TextView)findViewById(R.id.text_isbn);
         
         /*
         textview_name=(TextView)findViewById(R.id.text_name);
         textview_author=(TextView)findViewById(R.id.text_author);
         textview_summary=(TextView)findViewById(R.id.text_summary);
         
         imageview_book=(ImageView)findViewById(R.id.image_book);
         */
         
         resultWeb=(WebView)findViewById(R.id.resultWeb);
         
         
         
         
       
 		
        
     }
     
 	public void onClick(View v){
 		
 		 Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 	     intent.putExtra("SCAN_MODE", "ONE_D_MODE");
 	     startActivityForResult(intent, 0);
 	    
 	    
 	}
 	
 	
 
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  	
		  if (null == data) 
			  return;
 	
 		if (requestCode != 0) {
 			return;
 		}
 		
 		
 	
 	   
 	   isbn=data.getStringExtra("SCAN_RESULT");
 	  
 	   textview_isbn.setText("ISBN:"+isbn);
 	   
 	   Intent intent = new Intent();
 		intent.setClass(this, SearchBookActivity.class);
 		
 		intent.putExtra("ISBN", isbn);
 		this.startActivity(intent);
 	   
 	//   System.out.println(isbn);
 	   
 	  /*
 	   
 	   new Thread(getBookInfoRun).start(); 
 	   
 	   
 	   
 	   resultWeb.getSettings().setSupportZoom(false);
 		resultWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(
 				true);
 		resultWeb.getSettings().setJavaScriptEnabled(true);
 
 		resultWeb.loadUrl("file:///android_asset/results.html");
 
 		resultWeb.addJavascriptInterface(new Object() {
 			
 			public String getBookName() {
 				return bookInfo.getName();
 			}
 
 			public String getBookSummary() {
 				return bookInfo.getSummary();
 			}
 
 			public String getBookImageUrl() {
 				return bookInfo.getImageUrl();
 			}
 
 			public String getBookAuthor() {
 				return bookInfo.getAuthor();
 			}
 		}, "searchResult");
 
 		resultWeb.addJavascriptInterface(new Object() {
 			public void save(String count) {
 				finish();
 			}
 
 		}, "saveOrderCount");
 	   
 	   
 	   
 	  
 		
 			
 	  
 		   
 		 
 	
 	//   System.out.println(bookInfo.getName().toString());
 	   
 	   /*
 	 
 	   
 	   textview_name.setText(bookInfo.getName());
 	   textview_author.setText(bookInfo.getAuthor());
 	   textview_summary.setText(bookInfo.getSummary());
 	   */
 	   
 	   
 	   
 	   
 	
 	   /*
 	   byte[] imageData;
 	   try {
 		   
 		   imageData = downImage(bookInfo.getImageUrl());
 	
 		   if(imageData!=null){         
            
 			   bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);        
 			   imageview_book.setImageBitmap(bitmap);
 		   }
 	   
 		   saveFile(bitmap, isbn+".jpg");
 		   Toast.makeText(this, "汣ɹ", Toast.LENGTH_SHORT).show();   
 	   } catch (Exception e) {
 		   
 		   Toast.makeText(this, "汣ʧܣ", Toast.LENGTH_SHORT).show();   
 		   e.printStackTrace();
 	   }  
 		   */
 	}   
 	
 
 
 	   
 
 
 
 		/*
 		try{
 		
 		URL url = new URL(URL+isbn+"?apikey="+APIKey);      
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();      
 		conn.setConnectTimeout(5 * 1000);      
 		conn.setRequestMethod("GET");      
 		InputStream inStream = conn.getInputStream(); 
 		
 		dbook = getBookInfo(inStream);
 		}catch (Exception e) {  
 		e.printStackTrace();  
 		}  
 		
 		
 		return dbook;
 		
 		*/
 		
 
 
 
 	   
 	  
    
 	
 	
 	 
 			
 			
 
  
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_scan, menu);
         return true;
     }
     
     
 	  
 }
