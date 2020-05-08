 package com.example.formidable;
 
 import android.test.InstrumentationTestCase;
 import android.util.Log;
 import com.couchbase.touchdb.TDDatabase;
 import com.couchbase.touchdb.TDServer;
 import com.couchbase.touchdb.TDView;
 import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
 import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
import com.couchbase.touchdb.support.FileDirUtils;

 import org.ektorp.CouchDbConnector;
 import org.ektorp.CouchDbInstance;
 import org.ektorp.impl.StdCouchDbInstance;
 
import java.io.File;
 import java.io.IOException;
 
 abstract class FormidableTestCase extends InstrumentationTestCase {
 
     public static final String TAG = "FormidableTestCase";
     private TDServer localServer = null;
     private TDDatabase database = null;
     protected CouchDbConnector events;
 
     @Override
     protected void setUp() throws Exception {
         Log.v(TAG, "setUp");
         super.setUp();
 
         //for some reason a traditional static initializer causes junit to die
         TDURLStreamHandlerFactory.registerSelfIgnoreError();
 
         startServer();
         startClient();
     }
 
     private String getFilesDir() {
         //return getInstrumentation().getContext().getFilesDir().getAbsolutePath();
         return "/data/data/com.example.formidable/files";
     }
 
 	private void startServer() {
         try {        		
            String files = getFilesDir();
            FileDirUtils.deleteRecursive(new File(files));
			localServer = new TDServer(files);
             startViews();
         } catch (IOException e) {
             Log.e(TAG, "Error starting TouchDB Server.", e);
         }
 	}
   
 	private void startViews() {
 		TDDatabase db = localServer.getDatabaseNamed("events");
 		TDView view = db.getViewNamed("records/latest");
 		new CurrentState().setMapReduceBlocksFor(view);
 	}
 	
 	private void startClient() {
 		CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(localServer));
         events = client.createConnector("events", true);
 	}
 }
