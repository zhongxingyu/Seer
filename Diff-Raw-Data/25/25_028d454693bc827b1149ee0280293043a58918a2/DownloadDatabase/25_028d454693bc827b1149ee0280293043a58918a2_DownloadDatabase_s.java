 package com.thebitisland.campamentosdiaper.auxClasses;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import com.thebitisland.campamentosdiaper.R;
 
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class DownloadDatabase extends AsyncTask<Void, Void, Void> {
 	
 	private final String DATABASE_URL = "http://camps.thebitisland.com/CampsDB.db";
 	private final String database_folder = "/data/data/com.thebitisland.campamentosdiaper/databases/";
 	private final String database_version = "http://camps.thebitisland.com/DBversion";
 	SharedPreferences prefs;
 	Context context;
 	ProgressDialog pDialog;
 	
 	public DownloadDatabase(SharedPreferences prefs, Context context) {
 		this.prefs = prefs;
 		this.context = context;
 		pDialog = new ProgressDialog(context);
 		pDialog.setIndeterminate(false);
 		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCancelable(true);
         pDialog.setMessage(context.getResources().getString(R.string.download_db));
 
 	}
 	
 	@Override
 	protected void onProgressUpdate(Void... values) {
 		// TODO Auto-generated method stub
 		pDialog.show();
 	}
 	
 	@Override
 	protected Void doInBackground(Void... arg0) {
 		
 		String filename = "CampsDB.db";
 		
 		try {
 			if(!checkDatabaseVersion()){
 				pDialog.dismiss();
 				return null;
 			}
 		} catch (ParseException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         
         try {
         	//pDialog.show();
         publishProgress();
         URL input = new URL(DATABASE_URL);
         URLConnection conn = input.openConnection();
         int contentLength = conn.getContentLength();
         
         DataInputStream stream = new DataInputStream(input.openStream());
         byte[] buffer = new byte[contentLength];
         stream.readFully(buffer);
         stream.close();
         
         DataOutputStream output = new DataOutputStream(new FileOutputStream(database_folder + filename));
         output.write(buffer);
         output.flush();
         output.close();
         //pDialog.dismiss();
         } catch(FileNotFoundException e) {
         } catch (IOException e) {
         }
 		return null;
 	}
 	
 	private boolean checkDatabaseVersion() throws ParseException, IOException {
 		
         int databaseVersion = prefs.getInt("DBVersion", 0);
         Log.d("databaseVersion", "The current DB is v"+databaseVersion);
         
         HttpClient httpClient = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(database_version);
         
         try {
         	HttpResponse response = httpClient.execute(httpGet);
         	int responseCode = response.getStatusLine().getStatusCode();
         	if(responseCode == 200) {
         		HttpEntity entity = response.getEntity();
         		String responseContent = EntityUtils.toString(entity);
         		int version = Integer.parseInt(responseContent);
         		Log.d("checkDatabaseVersion","Version = "+version);
         		if(version > databaseVersion || databaseVersion == 0) {
         			SharedPreferences.Editor editor = prefs.edit();
         			editor.putInt("DBVersion", version);
         			editor.commit();
         			return true;
         		}
         		
         	}
         } catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
         
 	}
 
 	
 	@Override
 	protected void onPostExecute(Void result) {
 		// TODO Auto-generated method stub
 		super.onPostExecute(result);
 		pDialog.dismiss();
 	}
 	
 
 
 }
