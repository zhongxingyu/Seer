 package net.redlinesoft.app.iampetdo;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import android.app.AlertDialog;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import net.redlinesoft.app.iampetdo.R;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.ads.*;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
 	private ProgressDialog mProgressDialog;
 	String feedurl = "http://query.yahooapis.com/v1/public/yql?q=select%20%20*%20from%20feed%20where%20url%3D%22http%3A%2F%2Fwww.iampetdo.com%2Frss.xml%22%20%20%20";
 
 	public ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
 	HashMap<String, String> map;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// check network connection
 		if (isNetworkAvailable()) {
 			// load admob
 			// Create the adView
 			AdView adView = new AdView(this, AdSize.BANNER, "a14fdcb18476694");
 			// Lookup your LinearLayout assuming it’s been given
 			// the attribute android:id="@+id/mainLayout"
 			LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
 			// Add the adView to it
 			layout.addView(adView);
 			// Initiate a generic request to load it with an ad
 			adView.loadAd(new AdRequest());
 			
 			// load content
 			startDownload();
 			parseContent();
 			loadContent();
 		} else {
 			AlertDialog.Builder alertAdb = new AlertDialog.Builder(
 					MainActivity.this);
 			alertAdb.setTitle("Error");
 			alertAdb.setMessage("No Internet connection, please connect to internet and try again.");
 			alertAdb.setNegativeButton("OK", null);
 			alertAdb.show();
 			Log.d("Network", "No network connection");
 		}
 
 	}
 
 	ListView listItem;
 	LazyAdapter adapter;
 
 	private void loadContent() {
 
 		Log.d("XML", String.valueOf(MyArrList.size()));
 		listItem = (ListView) findViewById(R.id.listItem);
 		LazyAdapter adapter = new LazyAdapter(this, MyArrList); 		
 		listItem.setAdapter(adapter);
 		listItem.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				// Toast.makeText(MainActivity.this,
 				// MyArrList.get(arg2).get("ImageTitle") ,
 				// Toast.LENGTH_SHORT).show();
 				// intent activity and show item
 				Intent newActivity = new Intent(MainActivity.this,
 						ItemActivity.class);
 				newActivity.putExtra("ItemTitle",
 						MyArrList.get(arg2).get("ImageTitle"));
 				newActivity.putExtra("ItemURL",
 						MyArrList.get(arg2).get("ImagePath"));
 				startActivity(newActivity);
 			}
 
 		});
 
 	}
 
 	private void startDownload() {
 		new DownloadFileAsync().execute(feedurl);
 	}
 
 	class DownloadFileAsync extends AsyncTask<String, String, String> {
 
 		@Override
 		protected String doInBackground(String... aurl) {
 			int count;
 			Log.d("DOWNLOAD", feedurl);
 			try {
 
 				URL url = new URL(aurl[0]);
 				URLConnection conexion = url.openConnection();
 				conexion.connect();
 
 				int lenghtOfFile = conexion.getContentLength();
 				Log.d("DOWNLOAD", "Lenght of file: " + lenghtOfFile);
 
 				InputStream input = new BufferedInputStream(url.openStream());
 
 				// Get File Name from URL
 				// String fileName = URLDownload.substring(
 				// URLDownload.lastIndexOf('/')+1, URLDownload.length() );
 
 				OutputStream output = new FileOutputStream(Environment
 						.getExternalStorageDirectory().getPath()
 						+ "/iampetdo.feed.xml");
 
 				Log.d("FILE", Environment.getExternalStorageDirectory()
 						.getPath() + "/iampetdo.feed.xml");
 
 				byte data[] = new byte[1024];
 
 				long total = 0;
 
 				while ((count = input.read(data)) != -1) {
 					total += count;
 					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
 					output.write(data, 0, count);
 				}
 
 				output.flush();
 				output.close();
 				input.close();
 
 			} catch (Exception e) {
 				Log.d("DOWNLOAD", "Error download file");
 			}
 
 			return null;
 
 		}
 
 		protected void onProgressUpdate(String... progress) {
 			// Log.d("DOWNLOAD", progress[0]);
 			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
 		}
 
 		@SuppressWarnings("deprecation")
 		protected void onPostExecute(String unused) {
 			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
 			removeDialog(DIALOG_DOWNLOAD_PROGRESS);
 		}
 
 		@SuppressWarnings("deprecation")
 		protected void onPreExecute() {
 			super.onPreExecute();
 			showDialog(DIALOG_DOWNLOAD_PROGRESS);
 		}
 
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_DOWNLOAD_PROGRESS:
 			mProgressDialog = new ProgressDialog(this);
 			mProgressDialog.setMessage("Downloading...");
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			mProgressDialog.setCancelable(false);
 			mProgressDialog.show();
 			return mProgressDialog;
 		default:
 			return null;
 		}
 	}
 
 	private void parseContent() {
 		// TODO Auto-generated method stub
 
 		try {
 
 			File fXmlFile = new File(Environment.getExternalStorageDirectory()
 					.getPath() + "/iampetdo.feed.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(fXmlFile);
 			doc.getDocumentElement().normalize();
 
 			Log.d("XML", doc.getDocumentElement().getNodeName());
 			NodeList nList = doc.getElementsByTagName("item");
 
 			Log.d("XML", String.valueOf(nList.getLength()));
			
			MyArrList.clear();
 
 			for (int temp = 0; temp < nList.getLength(); temp++) {
 				Node nNode = nList.item(temp);
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element eElement = (Element) nNode;
 
 					// Log.d("XML", getTagValue("title", eElement));
 					// Log.d("XML", getTagValue("description", eElement));
 
 					String title = getTagValue("title", eElement);
 					String descrition = getTagValue("description", eElement);
 
 					Pattern p = Pattern.compile(".*<img[^>]*src=\"([^\"]*)",
 							Pattern.CASE_INSENSITIVE);
 					Matcher m = p.matcher(descrition);
 
 					String strImage = "";
 					while (m.find()) {
 						String word0 = m.group(1);
 						// Log.d("XML", word0.toString());
 						strImage += word0.toString() + ",";
 					}
 
 					// Log.d("XML", strImage);
 					map = new HashMap<String, String>();
 					map.put("ImageID", String.valueOf((temp + 1)));
 					map.put("ImageTitle", title);
 					map.put("ImagePath", strImage);
 					MyArrList.add(map);
 
 				}
 
 			}
 			Log.d("XML", String.valueOf(MyArrList.size()));
 
 		} catch (Exception e) {
 
 			Log.d("XML", e.getMessage());
 		}
 
 	}
 
 	private String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
 				.getChildNodes();
 		Node nValue = (Node) nlList.item(0);
 		return nValue.getNodeValue().trim();
 	}
 
 	private boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_share:
 			Log.d("MENU", "select menu share");
 			Intent sharingIntent = new Intent(
 					android.content.Intent.ACTION_SEND);
 			sharingIntent.setType("text/*");
 			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 					getString(R.string.text_share_subject));
 			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
 					getString(R.string.text_share_body)
 							+ getApplicationContext().getPackageName());
 			// startActivity(Intent.createChooser(sharingIntent,getString(R.string.menu_share)));
 			startActivity(sharingIntent);
 			break;
 		case R.id.menu_update:
 			Log.d("MENU", "select menu update");
			// load content
			startDownload();
			parseContent();
			loadContent();
 			break;		
 		case R.id.menu_refresh:
 			Log.d("MENU", "select menu refresh");
			loadContent();
 			break;
 		}
 		
 		return false;
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 }
