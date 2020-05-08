 package de.gymbuetz.gsgbapp.classes;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import de.gymbuetz.gsgbapp.R;
 
 // Implementation of AsyncTask used to download XML feed from
 // stackoverflow.com.
 public class DownloadXmlTask extends AsyncTask<String, Void, String> {
 
 	Context myContext;
 	String url;
 	String saveFile;
 	ProgressDialog myProgessDialog;
 
 	public DownloadXmlTask(Context context, String url, String saveFile, ProgressDialog pd) {
 		myContext = context;
 		this.url = url;
 		this.saveFile = saveFile;
 		myProgessDialog = pd;
 	}
 
 	@Override
 	protected String doInBackground(String... args) {
 		try {
 			return downloadXml(url);
 		} catch (IOException e) {
 			return myContext.getResources().getString(R.string.connection_error);
 		}
 	}
 
 	@Override
 	protected void onPostExecute(String result) {
 		try {
 			safeXmlFile(result);
			notifyAll();
 		} catch (IOException e) {
 			e.printStackTrace();
 			// TODO: What should happen, when the file can't be saved?
 		}
 
 		myProgessDialog.cancel();
 	}
 
 	private String downloadXml(String urlString) throws IOException {
 		myProgessDialog.setProgress(25);
 		InputStream stream = null;
 		String xml = "";
 
 		try {
 			ConnectivityManager connMgr = (ConnectivityManager) myContext
 					.getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 			if (networkInfo != null && networkInfo.isConnected()) {
 				xml = downloadUrl(url);
 			}
 		} finally {
 			if (stream != null) {
 				stream.close();
 			}
 		}
 
 		return xml;
 	}
 
 	private String downloadUrl(String myurl) throws IOException {
 		InputStream is = null;
 		// Only display the first 500 characters of the retrieved
 		// web page content.
 
 		try {
 			URL url = new URL(myurl);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setReadTimeout(10000 /* milliseconds */);
 			conn.setConnectTimeout(15000 /* milliseconds */);
 			conn.setRequestMethod("GET");
 			conn.setDoInput(true);
 			// Starts the query
 			conn.connect();
 			is = conn.getInputStream();
 			// Convert the InputStream into a string
 			String contentAsString = convertStreamToString(is);
 			myProgessDialog.setProgress(50);
 			return contentAsString;
 
 			// Makes sure that the InputStream is closed after the app is
 			// finished using it.
 		} finally {
 			if (is != null) {
 				is.close();
 			}
 		}
 	}
 
 	private void safeXmlFile(String xml) throws IOException {
 		FileOutputStream fouts;
 		try {
 			fouts = myContext.openFileOutput(saveFile, Context.MODE_PRIVATE);
 			fouts.write(xml.getBytes());
 			fouts.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String convertStreamToString(InputStream is) {
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return sb.toString();
 	}
 }
