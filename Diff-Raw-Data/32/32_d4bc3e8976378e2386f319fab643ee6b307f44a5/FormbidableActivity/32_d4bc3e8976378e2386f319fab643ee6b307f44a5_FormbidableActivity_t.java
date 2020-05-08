 package com.example.formidable;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.UUID;
 
 import org.ektorp.CouchDbConnector;
 import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
 import org.ektorp.impl.StdCouchDbInstance;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 import com.couchbase.touchdb.TDDatabase;
 import com.couchbase.touchdb.TDServer;
 import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
 import com.couchbase.touchdb.replicator.TDReplicator;
 import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
 
 public class FormbidableActivity extends Activity {
 	
 	static { 
 	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
 	}
 	
 	private static final String TAG = "MainActivity";
 	private TDServer server;
	
	@Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
         server = startServer();
         
         CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(server));  
         CouchDbConnector events = client.createConnector("events", true);
         
         String id = newId();
         Event created = new Event();
         created.put("name", "Angshu");
 		events.create(id, created);
 		Event retrieved = events.find(Event.class, id);
 		
		beginReplicating(client);
 		//server.close();
         
         setContentView(R.layout.activity_main);
         WebView myWebView = (WebView) findViewById(R.id.webview);
         WebSettings webSettings = myWebView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         
         webSettings.setDatabaseEnabled(true);
         webSettings.setDomStorageEnabled(true);
         webSettings.setGeolocationDatabasePath("/data/data/com.example.formidable/databases");
         webSettings.setGeolocationEnabled(true);
 
         
         myWebView.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
     }
 
 	private TDServer startServer() {
 		TDServer server = null;
         String filesDir = getFilesDir().getAbsolutePath();
         try {
             server = new TDServer(filesDir);           
         } catch (IOException e) {
             Log.e(TAG, "Error starting TouchDB Server.", e);
         }
         //startReplicating(server);
 		return server;
 	}
 
 	private String newId() {
 		return UUID.randomUUID().toString();
 	}
 	
	private void beginReplicating(CouchDbInstance client) {		
		ReplicationCommand push = new ReplicationCommand.Builder()
		.source("events")
		.target("http://10.4.3.171:5984/events")
		.continuous(true)
		.build();
	
		client.replicate(push);
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
