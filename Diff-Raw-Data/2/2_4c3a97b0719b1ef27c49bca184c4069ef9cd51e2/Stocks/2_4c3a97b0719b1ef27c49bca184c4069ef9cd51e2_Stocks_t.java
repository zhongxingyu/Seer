 package org.metawatch.manager.stocks;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Stocks extends Activity {
 	
 	public static final String TAG = "Stock-MWM";
 	
 	private static String symbols = "GOOG\nAAPL\nFB\nTXN\nSRP.F\nFOSL";
 	private static long refreshInterval = 1000 * 60 * 30;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
 		final SharedPreferences sharedPreferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		
 		symbols = sharedPreferences.getString("symbols", symbols);
         
         final EditText editText = (EditText) findViewById(R.id.editText1);
         editText.setText(symbols);
         
         final TextView textView = (TextView) findViewById(R.id.textview);
         textView.setText("");
 
         Button searchButton = (Button) findViewById(R.id.button2);
         searchButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.yahoo.com/finance"));
 				startActivity(browserIntent);					
 			};
         });      
         
         final Context context = this;
         
         Button refreshButton = (Button) findViewById(R.id.button1);
         refreshButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				symbols = editText.getText().toString();
 				
 				Editor editor = sharedPreferences.edit();
 
 				editor.putString("symbols", symbols);
 				editor.commit();
 				
 				refresh(context, textView);
 									
 			};
         });
         
         refresh(context, textView);
     }
 
     public static void refresh(Context context) {
    	refresh(context, null);
     }
     
     static Thread updateThread = null;
 	private static void refresh(final Context context, final TextView textView) {
 		
 		if (updateThread!=null) {
 			Log.d(Stocks.TAG, "Refresh already running.");
 			return;
 		}
 		
 		if (textView!=null)
 			textView.setText("Refreshing...\n\n");
 		
 		updateThread = new Thread("StocksUpdate") {
 			@Override
 			public void run() {
 		
 				final HashMap<String, List<String>> values = Stocks.get(context);
 		        
 				if (textView!=null) {
 					((Activity)(context)).runOnUiThread(new Runnable() {
 					     public void run() {
 	
 					    	textView.setText("");
 							for (HashMap.Entry<String, List<String>> entry : values.entrySet()) {
 								for (String part : entry.getValue()) {
 									textView.append(part);
 									textView.append(" ");
 								}
 								textView.append("\n");
 							}
 					    }
 					});
 				}
 		        
 		    	if (values!=null)
 		    		Widget.update(context, values);
 				
 				updateThread = null;
 			}
 			
 			
 		};
 		
 		updateThread.start();
 		
 	}
     
     public static HashMap<String, List<String>> get(Context context) {
     	// See: http://www.gummy-stuff.org/Yahoo-data.htm
     	
     	HashMap<String, List<String>> values = null;
     	
     	StringBuilder url = new StringBuilder();
     	url.append("http://finance.yahoo.com/d/quotes.csv?s=");
     	url.append(symbols.replace(" ", "+").replace("\n", "+"));
     	url.append("&f=sl1");
     	
     	values = getCSVfromURL(context, url.toString());
     	
     	return values;
     }
     
     public static HashMap<String, List<String>> getCached(Context context) {
     	
     	File cacheFile = getCacheFile(context);
     	if (!cacheFile.exists()) {
     		return null;
     	}
     	
     	try {
 			FileInputStream is = new FileInputStream(cacheFile);
 			ArrayList<String> lines = readCSV(is);
 			Log.d(Stocks.TAG, "Read from cache.");
 			return parseCSVData(lines);
 		} catch (FileNotFoundException e) {
 			Log.e(Stocks.TAG, "Failed to load cacheFile "+e.toString());
 			return null;
 		}
     	
     }
     
 	private static HashMap<String, List<String>> getCSVfromURL(Context context, String url){
 	
 		//initialize
 		InputStream is = null;
 
 		//http post
 		try{
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(url);
 			HttpResponse response = httpclient.execute(httppost);
 			HttpEntity entity = response.getEntity();
 			is = entity.getContent();
 
 		}catch(Exception e){
 			Log.e(Stocks.TAG, "Error in http connection "+e.toString());
 			
 			return null;
 		}
 		
 		Log.d(Stocks.TAG, "Read from http.");
 		
 		ArrayList<String> lines = readCSV(is);
 		cacheCSVData(context, lines);	
 		return parseCSVData(lines);
 	}
 	
 	public static boolean isCacheOld(final Context context) {
 		final File cache = getCacheFile(context);
 		if (!cache.exists())
 			return true;
 		
 		final long lastMod = cache.lastModified();
 		final long now = System.currentTimeMillis();
 		
 		final long age = now-lastMod;
 		Log.d(Stocks.TAG, "Cache is "+age+ "ms old");
 		
 		return age > refreshInterval;
 	}
 	
 	private static File getCacheFile(Context context) {
 		return new File(context.getCacheDir(), "cachedata.csv");
 	}
 
 	private static void cacheCSVData(Context context, ArrayList<String> lines) {
 		
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(getCacheFile(context)));
 			
 			for (String line : lines) {
 				writer.write(line);
 				writer.newLine();
 			}
 			
 			writer.close();
 		} catch (IOException e) {
 			Log.e(Stocks.TAG, "Failure caching data "+e.toString());
 		}
 
 
 	}
 
 	private static ArrayList<String> readCSV(InputStream is) {
 		ArrayList<String> lines = new ArrayList<String>();
 		if (is != null) {
 			try {
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(is, "iso-8859-1"), 8);
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					lines.add(line);
 				}
 				is.close();
 				
 			} catch (Exception e) {
 				Log.e(Stocks.TAG, "Error converting result " + e.toString());
 			}
 			
 		}
 		else {
 			Log.e(Stocks.TAG, "null result");
 		}	
 		
 		return lines;
 	}
 	
 	private static HashMap<String, List<String>> parseCSVData(ArrayList<String> lines) {
 		String result = "";
 		HashMap<String, List<String>> csvMap = new HashMap<String, List<String>>();
 
 		try {
 
 			for (String line : lines) {
 
 				line = line.replace("\"", "");
 				String parts[] = line.split(",");
 			
 				if (parts.length==2) {					
 					csvMap.put(Widget.id_prefix + parts[0] + Widget.id_suffix, Arrays.asList(parts));
 				}
 			}
 			
 			Log.i(Stocks.TAG, result);
 		} catch (Exception e) {
 			Log.e(Stocks.TAG, "Error parsing " + e.toString());
 		}
 		return csvMap;
 	}
 }
