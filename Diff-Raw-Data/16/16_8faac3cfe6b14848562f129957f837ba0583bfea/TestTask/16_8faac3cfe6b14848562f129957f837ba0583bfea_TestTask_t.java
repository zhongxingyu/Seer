 package com.example.samplesqlsync.tasks;
 
 import java.util.Random;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.example.samplesqlsync.SampleActivity;
 import com.example.samplesqlsync.provider.SampleDatabaseHelper;
 import com.nexusray.sqlsync.settings.SyncSettings;
 import com.nexusray.sqlsync.sync.SyncExecutor;
 
 /**
  * Tests synchronization functionality
  *
  */
 public class TestTask extends AsyncTask<Void, Void, Void> {
 	
 	private static final String logTag = "TestTask";
 	private String displayText = "";
 	private SampleActivity callerActivity;
 	private ContentResolver cr;
 	
 	private void addText(String text) {
 		displayText += text + "\n";    		
 	}
 	
 	public TestTask(SampleActivity callerActivity, ContentResolver cr) {
 		this.callerActivity = callerActivity;
 		this.cr = cr;
 	}
 	
 	@Override
 	protected void onProgressUpdate(Void... values) {
 		
 		callerActivity.displayText(displayText);	
 		displayText = "";
 		
 		super.onProgressUpdate(values);
 	}
 	
 	@Override
 	protected void onPostExecute(Void result) {
 		callerActivity.displayText(displayText);	
 		displayText = "";
 		
 		super.onPostExecute(result);
 	}	
 	
 	@Override
 	protected Void doInBackground(Void... params) {
 		
 		
 			
 		if (httpExe(SampleActivity.SCRIPT_BENCH_URL + "?a=generate_init") == null) {
 			return null;
 		}
     	
     	Random rnd = new Random();
     	ContentValues values;
     	
     	for (int i = 0; i < 10; i++) {					
 		
 	    	values = new ContentValues();
 	        values.put("name", "lampa");
 	        values.put("count", rnd.nextInt(99)+1);
 	        values.put("price", rnd.nextInt(999)+1);
 	        
 	        cr.insert(SampleDatabaseHelper.tableProducts, values);
     	}
     	
     	for (int no = 0; no < 10; no++) {	
     		
     		// Concurrent synchronizations are database modifications
     		SyncExecutor.requestSync();
     		
 	    	for (int i = 0; i < 10; i++) {	
 		        Cursor c = cr.query(SampleDatabaseHelper.tableProducts, new String[] {"_syncID"}, null, null, "random()");
 		        if (c.moveToFirst()) {
 		    	
 		        	// Update random row
 		        	values = new ContentValues();
 		            values.put("count", rnd.nextInt(99)+1);
 		            values.put("price", rnd.nextInt(999)+1);
 			        cr.update(SampleDatabaseHelper.tableProducts, values, "_ID=?", new String[] {c.getString(0)});
 			        c.close();
 			        
 			        if (httpExe(SampleActivity.SCRIPT_BENCH_URL + "?a=generate") == null) {
 						return null;
 					}
 			        
 			        // Delete random row
 			        c = cr.query(SampleDatabaseHelper.tableProducts, new String[] {"_syncID"}, null, null, "random()");
 			        c.moveToFirst();
 			        values = new ContentValues();
 			    	cr.delete(SampleDatabaseHelper.tableProducts, "_syncID=?", new String[] {c.getString(0)});
 			    	
 			    	// Insert a row
 			    	values = new ContentValues();
 			        values.put("name", "lampa");
 			        values.put("count", rnd.nextInt(99)+1);
 			        values.put("price", rnd.nextInt(999)+1);
 			        
 			        cr.insert(SampleDatabaseHelper.tableProducts, values);
 		        }
 		    	c.close();	
 	    	}
     	}
     	
     	try {
			Thread.sleep(10000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
     	
     	
     	//Next phase
    	SampleActivity.testPhase.drainPermits();
 		SyncExecutor.requestSync();			
 		try {
 			SampleActivity.testPhase.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
     	// Compares databases
     	String mobileEcho = "START\n";    	
     	Cursor c = cr.query(SampleDatabaseHelper.tableProducts, 
     			new String[] {"_syncID", "name", "count", "price"},  null, null, "_syncID");
         while (c.moveToNext())
         	mobileEcho += c.getString(0) + "," + c.getString(1) +
         			"," + c.getString(2) + "," + c.getString(3) + "\n";
         mobileEcho += "DONE\n\n";    		
 		c.close();
         
 		String serverEcho = httpExe(SampleActivity.SCRIPT_BENCH_URL + "?a=echo");
 		if (serverEcho == null) {
 			return null;
 		}
 		
         boolean compareResult = mobileEcho.equals(serverEcho); 		
 	    
 	    if (compareResult) addText("Test SUCCEEDED");
 	    else {
 	    	addText("Test FAILED");
 	    	Log.w(logTag, "Compare result M/S:\n"+ mobileEcho + serverEcho);
 	    }
 		
 	    callerActivity.resetOperationInProgress();
 	
 		return null;
 	}
 
 	/**
 	 * Executes HTTP request, must be done in worker thread
 	 * @param url
 	 * @return
 	 */
 	private String httpExe(String url) {
 		HttpParams params = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(params, SyncSettings.HTTP_TIMEOUT);
 		HttpConnectionParams.setSoTimeout(params, SyncSettings.HTTP_TIMEOUT);
 		ConnManagerParams.setTimeout(params, SyncSettings.HTTP_TIMEOUT);
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 	    HttpClient httpclient = new DefaultHttpClient(params);
 	    HttpResponse response;
 	    
 	    try {
 	        response = httpclient.execute(new HttpGet(url));
 	        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 	        	return EntityUtils.toString(response.getEntity());
 	        }
 	        else {
 	        	Log.e(logTag, "Http request has FAILED!");
 	        }
 	    } 
 	    catch (Exception e) {
 	    	Log.e(logTag, "Http request has FAILED!");
 	    	e.printStackTrace();
 	    } 
 	    
 	    addText("HTTP request failed, URL must be incorrect");
 	    return null;
 	}
 }
