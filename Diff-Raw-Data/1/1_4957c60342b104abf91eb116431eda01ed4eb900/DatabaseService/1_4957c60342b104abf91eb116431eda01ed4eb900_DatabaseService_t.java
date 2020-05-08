 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.radicaldynamic.groupinform.services;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.ektorp.CouchDbConnector;
 import org.ektorp.CouchDbInstance;
 import org.ektorp.DbAccessException;
 import org.ektorp.ReplicationCommand;
 import org.ektorp.ReplicationStatus;
 import org.ektorp.http.HttpClient;
 import org.ektorp.http.StdHttpClient;
 import org.ektorp.impl.StdCouchDbConnector;
 import org.ektorp.impl.StdCouchDbInstance;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.ConditionVariable;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.database.InformCouchDbConnector;
 import com.radicaldynamic.groupinform.documents.Generic;
 import com.radicaldynamic.groupinform.logic.AccountFolder;
 import com.radicaldynamic.groupinform.repositories.FormDefinitionRepo;
 import com.radicaldynamic.groupinform.repositories.FormInstanceRepo;
 
 /**
  * Database abstraction layer for CouchDB, based on Ektorp.
  * 
  * This does not control the stop/start of the actual DB.
  * See com.couchone.couchdb.CouchService for that.
  */
 public class DatabaseService extends Service {
     private static final String t = "DatabaseService: ";
 
     // Replication modes
     public static final int REPLICATE_PUSH = 0;
     public static final int REPLICATE_PULL = 1;   
     
     // 5 minutes (represented as seconds)
     private static final long TIME_FIVE_MINUTES = 300;
     // 24 hours (represented as milliseconds)
     private static final long TIME_24_HOURS = 86400000;
     
     // Values returned by the Couch service -- we can't connect to the localhost until these are known
     private String mLocalHost = null;
     private int mLocalPort = 0;
     
     private HttpClient mLocalHttpClient = null;
     private CouchDbInstance mLocalDbInstance = null;
     private CouchDbConnector mLocalDbConnector = null;
     
     private HttpClient mRemoteHttpClient = null;
     private CouchDbInstance mRemoteDbInstance = null;
     private CouchDbConnector mRemoteDbConnector = null;
     
     private boolean mInit = false;
     
     private boolean mConnectedToLocal = false;
     private boolean mConnectedToRemote = false;
     
     // This is the object that receives interactions from clients.  
     // See RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
   
     private ConditionVariable mCondition;
     
     // Hash of database-to-last cleanup timestamp (used for controlled purging databases of placeholders)
     private Map<String, Long> mDbLastCleanup = new HashMap<String, Long>();
     
     private Runnable mTask = new Runnable() 
     {
         final String tt = t + "mTask: ";
         
         public void run() {            
             for (int i = 1; i > 0; ++i) {
                 if (mInit == false) {
                     try {
                         mInit = true;
                         
                         if (mLocalDbInstance != null) {
                             performHousekeeping();
                             synchronizeLocalDBs();
                         }
                     } catch (Exception e) {
                         Log.w(Collect.LOGTAG, tt + "error automatically connecting to DB: " + e.toString()); 
                     } finally {
                         mInit = false;
                     }
                 }
                 
                 // Retry connection to CouchDB every 5 minutes
                 if (mCondition.block(300 * 1000))
                     break;
             }
         }
     };
     
     @SuppressWarnings("serial")
     public class DbUnavailableException extends Exception
     {
         DbUnavailableException()
         {
             super();
         }
     }
     
     @SuppressWarnings("serial")
     public class DbUnavailableWhileOfflineException extends DbUnavailableException
     {
         DbUnavailableWhileOfflineException()
         {
             super();
         }
     }
     
     @SuppressWarnings("serial")
     public class DbUnavailableDueToMetadataException extends DbUnavailableException
     {
         DbUnavailableDueToMetadataException(String db)
         {
             super();
             Log.w(Collect.LOGTAG, t + "metadata missing for DB " + db);
         }
     }
       
     @Override
     public void onCreate() 
     {
         Thread persistentConnectionThread = new Thread(null, mTask, "DatabaseService");        
         mCondition = new ConditionVariable(false);
         persistentConnectionThread.start();
     }
     
     @Override
     public void onDestroy() 
     {   
         mCondition.open();
     }
 
     @Override
     public IBinder onBind(Intent intent) 
     {
         return mBinder;
     }
     
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class LocalBinder extends Binder 
     {
         public DatabaseService getService() 
         {
             return DatabaseService.this;
         }
     }
     
     // Convenience method (uses currently selected database)
     public CouchDbConnector getDb() 
     {
         return getDb(Collect.getInstance().getInformOnlineState().getSelectedDatabase());
     }
     
     public CouchDbConnector getDb(String db)
     {
         final String tt = t + "getDb(): ";
         
         AccountFolder folder = Collect.getInstance().getInformOnlineState().getAccountFolders().get(db);
         CouchDbConnector dbConnector;
         
         if (folder.isReplicated()) {
             // Local database
             try {
                 open(db);                
             } catch (DbUnavailableException e) {
                 Log.w(Collect.LOGTAG, tt + "unable to connect to local database server: " + e.toString());
             }
             
             dbConnector = mLocalDbConnector;
         } else {
             // Remote database
             try {
                 open(db);              
             } catch (DbUnavailableException e) {
                 Log.w(Collect.LOGTAG, tt + "unable to connect to remote database server: " + e.toString());
             }
             
             dbConnector = mRemoteDbConnector;
         }
 
         return dbConnector;
     }
     
     public boolean initLocalDb(String db)
     {
         final String tt = t + "initLocalDb(): ";
         
         try {
             ReplicationStatus status = replicate(db, REPLICATE_PULL);
 
             if (status == null)
                 return false;
             else 
                 return status.isOk();
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + "replication pull failed at " + e.toString());
             e.printStackTrace();
             return false;
         }
     }
     
     /*
      * Does a database exist on the local CouchDB instance?
      */
     public boolean isDbLocal(String db)
     {
         final String tt = t + "isDbLocal(): ";
         
         boolean result = false;
         
         try {
             if (mLocalDbInstance == null)
                 connectToLocalServer();
             
             if (mLocalDbInstance.getAllDatabases().indexOf("db_" + db) != -1)
                 result = true;
         } catch (DbAccessException e) {
             Log.w(Collect.LOGTAG, tt + e.toString());
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + "unhandled exception: " + e.toString());
         }
         
         return result;
     }
         
     /**
      * Open a specific database
      * 
      * @param database  Name of database to open
      * @return
      * @throws DbMetadataUnavailableException 
      * @throws DbUnavailableException 
      */
     public void open(String db) throws DbUnavailableException 
     {        
         // If database metadata is not yet available then abort here
         if (db == null || Collect.getInstance().getInformOnlineState().getAccountFolders().get(db) == null) {
             throw new DbUnavailableDueToMetadataException(db);
         }
         
         if (!Collect.getInstance().getIoService().isSignedIn() && !Collect.getInstance().getInformOnlineState().getAccountFolders().get(db).isReplicated())
             throw new DbUnavailableWhileOfflineException();
         
         boolean dbToOpenIsReplicated = Collect.getInstance().getInformOnlineState().getAccountFolders().get(db).isReplicated();        
         
         if (dbToOpenIsReplicated) {
             // Local database
             if (mConnectedToLocal) {
                 if (mLocalDbConnector instanceof StdCouchDbConnector && mLocalDbConnector.getDatabaseName().equals("db_" + db)) {
                     return;
                 }
             } else {
                 connectToLocalServer();
             }
 
             openLocalDb(db);
         } else {
             // Remote database
             if (mConnectedToRemote) {
                 if (mRemoteDbConnector instanceof StdCouchDbConnector && mRemoteDbConnector.getDatabaseName().equals("db_" + db)) {
                     return;
                 }
             } else {
                 connectToRemoteServer();
             }
             
             openRemoteDb(db);
         }
     }
     
     /*
      * Similar in purpose to performHousekeeping(), this method is targeted towards any database 
      */
     public void performHousekeeping(String db) throws DbUnavailableException
     {
         final String tt = t + "performHousekeeping(String): ";
         
         // Determine if this database needs to be cleaned up
         Long lastCleanup = mDbLastCleanup.get(db);
         
         if (lastCleanup == null || System.currentTimeMillis() / 1000 - lastCleanup > TIME_FIVE_MINUTES) {
             Log.i(Collect.LOGTAG, tt + "beginning cleanup for " + db);
             mDbLastCleanup.put(db, new Long(System.currentTimeMillis() / 1000));            
             removePlaceholders(new FormDefinitionRepo(getDb()).getAllPlaceholders());
             removePlaceholders(new FormInstanceRepo(getDb()).getAllPlaceholders());
         }        
     }
     
     /*
      * Remove local database IF final replication push is successful
      */
     public boolean removeLocalDb(String db)
     {
         final String tt = t + "removeLocalDb(): ";
         
         try {
             ReplicationStatus status = replicate(db, REPLICATE_PUSH);
 
             if (status == null)
                 return false;
             else if (status.isOk()) {
                 Log.i(Collect.LOGTAG, tt + "final replication push successful, removing " + db);
                 mLocalDbInstance.deleteDatabase("db_" + db);
             }
             
             return status.isOk();
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + "final replication push failed at " + e.toString());
             e.printStackTrace();
             return false;
         }
     }
     
     synchronized public ReplicationStatus replicate(String db, int mode)
     {
         final String tt = t + "replicate(): ";
         
         Log.d(Collect.LOGTAG, tt + "about to replicate " + db);
 
         // Will not replicate unless signed in
         if (!Collect.getInstance().getIoService().isSignedIn()) {
             Log.w(Collect.LOGTAG, tt + "aborting replication: not signed in");
             return null;
         }
 
         if (Collect.getInstance().getInformOnlineState().isOfflineModeEnabled()) {
             Log.d(Collect.LOGTAG, tt + "aborting replication: offline mode is enabled");
             return null;
         }
         
         /*
          * Lookup master cluster by IP.  Do this instead of relying on Erlang's internal resolver 
          * (and thus Google's public DNS).  Our builds of Erlang for Android do not yet use 
          * Android's native DNS resolver.
          */
         String masterClusterIP = null;
 
         try {
             InetAddress [] clusterInetAddresses = InetAddress.getAllByName(getString(R.string.tf_default_ionline_server));
             masterClusterIP = clusterInetAddresses[new Random().nextInt(clusterInetAddresses.length)].getHostAddress();
         } catch (UnknownHostException e) {
             Log.e(Collect.LOGTAG, tt + "unable to lookup master cluster IP addresses: " + e.toString());
             e.printStackTrace();
         }
 
         // Create local instance of database
         boolean dbCreated = false;
 
         if (mLocalDbInstance.getAllDatabases().indexOf("db_" + db) == -1) {
             mLocalDbInstance.createDatabase("db_" + db);
             dbCreated = true;
         }
 
         // Configure replication direction
         String source = null;
         String target = null;
         
         String deviceId = Collect.getInstance().getInformOnlineState().getDeviceId();
         String deviceKey = Collect.getInstance().getInformOnlineState().getDeviceKey();
         
         switch (mode) {
         case REPLICATE_PUSH:
             source = "http://" + mLocalHost + ":" + mLocalPort + "/db_" + db; 
             target = "https://" + deviceId + ":" + deviceKey + "@" + masterClusterIP + ":6984/db_" + db;
             break;
 
         case REPLICATE_PULL:
             source = "https://" + deviceId + ":" + deviceKey + "@" + masterClusterIP + ":6984/db_" + db;
             target = "http://" + mLocalHost + ":" + mLocalPort + "/db_" + db; 
             break;
         }
 
         ReplicationCommand cmd = new ReplicationCommand.Builder().source(source).target(target).build();
         ReplicationStatus status = null;
 
         try {
             status = mLocalDbInstance.replicate(cmd);
         } catch (Exception e) {
             // Remove a recently created DB if the replication failed
             if (dbCreated) {
                 Log.e(Collect.LOGTAG, t + "replication exception: " + e.toString());
                 e.printStackTrace();
                 
                 mLocalDbInstance.deleteDatabase("db_" + db);
             }
         }
 
         return status;
     }
     
     public void setLocalDatabaseInfo(String host, int port)
     {
         mLocalHost = host;
         mLocalPort = port;
     }
 
     synchronized private void connectToLocalServer() throws DbUnavailableException 
     {
         final String tt = t + "connectToLocalServer(): ";
         
         if (mLocalHost == null || mLocalPort == 0) {
             Log.w(Collect.LOGTAG, tt + "local host information not available; aborting connection");
             mConnectedToLocal = false;
             return;
         }
         
         Log.d(Collect.LOGTAG, tt + "establishing connection to " + mLocalHost + ":" + mLocalPort);
 
         try {          
             /*
              * Socket timeout of 5 minutes is important, otherwise long-running replications
              * will fail.  It is possible that we will need to extend this in the future if
              * it turns out to be insufficient.
              */
             mLocalHttpClient = new StdHttpClient.Builder()
                 .host(mLocalHost)
                 .port(mLocalPort)
                 .socketTimeout(300 * 1000)
                 .build();
             
             mLocalDbInstance = new StdCouchDbInstance(mLocalHttpClient);
             mLocalDbInstance.getAllDatabases();
             
             if (mConnectedToLocal == false)
                 mConnectedToLocal = true;
             
             Log.d(Collect.LOGTAG, tt + "connection to " + mLocalHost + " successful");
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + e.toString());      
             e.printStackTrace();
             mConnectedToLocal = false;            
             throw new DbUnavailableException();
         }
     }
 
     synchronized private void connectToRemoteServer() throws DbUnavailableException 
     {        
         final String tt = t + "connectToRemoteServer(): ";
         
         String host = getString(R.string.tf_default_ionline_server);
         int port = 6984;
         
         Log.d(Collect.LOGTAG, tt + "establishing connection to " + host + ":" + port);
         
         try {                     
             mRemoteHttpClient = new StdHttpClient.Builder()
                 .enableSSL(true)            
                 .host(host)
                 .port(port)
                .socketTimeout(30 * 1000)
                 .username(Collect.getInstance().getInformOnlineState().getDeviceId())
                 .password(Collect.getInstance().getInformOnlineState().getDeviceKey())                    
                 .build();
             
             mRemoteDbInstance = new StdCouchDbInstance(mRemoteHttpClient);
             mRemoteDbInstance.getAllDatabases();
             
             if (mConnectedToRemote == false)
                 mConnectedToRemote = true;
             
             Log.d(Collect.LOGTAG, tt + "connection to " + host + " successful");
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + "while connecting to server " + port + ": " + e.toString());      
             e.printStackTrace();
             mConnectedToRemote = false;            
             throw new DbUnavailableException();
         }
     }
 
     private void openLocalDb(String db) throws DbUnavailableException 
     {
         final String tt = t + "openLocalDb(): ";
         
         try {
             /*
              * We used to create the database if it did not exist HOWEVER this had unintended side effects.
              * 
              * Since local databases are typically initialized on-demand the first time the user selects
              * them for operations, databases that were selected for replication but not yet "switched to" 
              * would be created as empty databases if the user backed out of the folder selection screen 
              * without specifically choosing a database.
              * 
              * Because the database then existed, attempts to "switch to" the database via the folder
              * selection screen (and have it initialized on-demand as expected) would fail.  At least,
              * until the system got around to creating and replicating it automatically.
              */            
             if (mLocalDbInstance.getAllDatabases().indexOf("db_" + db) == -1) {
                 Log.w(Collect.LOGTAG, tt + "database does not exist; failing attempt to open");
                 throw new DbUnavailableException();
             }
             
             Log.d(Collect.LOGTAG, tt + "opening database " + db);
             mLocalDbConnector = new InformCouchDbConnector("db_" + db, mLocalDbInstance);
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + "while opening DB " + db + ": " + e.toString());
             throw new DbUnavailableException();
         }
     }
     
     private void openRemoteDb(String db) throws DbUnavailableException 
     {
         final String tt = t + "openRemoteDb(): ";
         
         try {
             Log.d(Collect.LOGTAG, tt + "opening database " + db);
             mRemoteDbConnector = new InformCouchDbConnector("db_" + db, mRemoteDbInstance);
             
             /* 
              * This should trigger any 401:Unauthorized errors when connecting to a remote DB
              * (better to know about them now then to experience a crash later because we didn't trap something)
              */
             mRemoteDbConnector.getDbInfo();
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + "while opening DB " + db + ": " + e.toString());
             throw new DbUnavailableException();
         }
     }
 
     /*
      * Perform any house keeping (e.g., removing of unused DBs, view compaction & cleanup)
      */
     private void performHousekeeping()
     {   
         final String tt = t + "performHousekeeping(): ";
         
         try {
             List<String> allDatabases = mLocalDbInstance.getAllDatabases();
             Iterator<String> dbs = allDatabases.iterator();
     
             while (dbs.hasNext()) {
                 String db = dbs.next();
                 
                 // Skip special databases
                 if (!db.startsWith("_")) {
                     // Our metadata knows nothing about the db_ prefix
                     db = db.substring(3);
                     
                     AccountFolder folder = Collect.getInstance().getInformOnlineState().getAccountFolders().get(db);
 
                     if (folder == null) {
                         // Remove databases that exist locally but for which we have no metadata
                         Log.i(Collect.LOGTAG, tt + "no metatdata for " + db + " (removing)");
                         mLocalDbInstance.deleteDatabase("db_" + db);
                     } else if (isDbLocal(folder.getId()) && folder.isReplicated() == false) {
                         // Purge any databases that were not zapped at the time of removal from the synchronization list
                         removeLocalDb(folder.getId());
                     }
                 }
             }
         } catch (DbAccessException e) {
             Log.w(Collect.LOGTAG, tt + "database not available " + e.toString());
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, tt + "unhandled exception " + e.toString());
             e.printStackTrace();
         }
     }
     
     /*
      * Evaluate and remove a set of placeholders on the basis of who created it and when it was created
      */
     private void removePlaceholders(HashMap<String, JSONObject> placeholders)
     {        
         final String tt = t + "removePlaceholders(): ";
         
         for (Map.Entry<String, JSONObject> entry : placeholders.entrySet()) {
             if (entry.getValue().optString("createdBy", null) == null || entry.getValue().optString("dateCreated", null) == null) {
                 // Remove old style (unowned) placeholders immediately
                 try {
                     getDb().delete(entry.getKey(), entry.getValue().optString("_rev"));
                     Log.d(Collect.LOGTAG, tt + "removed old-style placeholder " + entry.getKey());
                 } catch (Exception e) {
                     Log.e(Collect.LOGTAG, tt + "unable to remove old-style placeholder");
                 }
             } else if (entry.getValue().optString("createdBy").equals(Collect.getInstance().getInformOnlineState().getDeviceId())) {
                 // Remove placeholders owned by me immediately
                 try {                        
                     getDb().delete(entry.getKey(), entry.getValue().optString("_rev"));
                     Log.d(Collect.LOGTAG, tt + "removed my placeholder " + entry.getKey());
                 } catch (Exception e) {
                     Log.e(Collect.LOGTAG, tt + "unable to remove my placeholder");
                 }                                       
             } else {
                 // Remove placeholders owned by other people if they are stale (older than a day)
                 SimpleDateFormat sdf = new SimpleDateFormat(Generic.DATETIME);
                 Calendar calendar = Calendar.getInstance();
 
                 try {
                     calendar.setTime(sdf.parse(entry.getValue().optString("dateCreated")));
 
                     if (calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() > TIME_24_HOURS) {
                         try {                        
                             getDb().delete(entry.getKey(), entry.getValue().optString("_rev"));
                             Log.d(Collect.LOGTAG, tt + "removed stale placeholder " + entry.getKey());
                         } catch (Exception e) {
                             Log.e(Collect.LOGTAG, tt + "unable to remove stale placeholder");
                         } 
                     }                        
                 } catch (ParseException e1) {
                     Log.e(Collect.LOGTAG, tt + "unable to parse dateCreated: " + e1.toString());            
                 }
             }
         }                
     }
     
     /*
      * Trigger a push/pull replication for each locally replicated database
      */
     private void synchronizeLocalDBs()
     {
         final String tt = t + "synchronizeLocalDBs(): ";
         
         Set<String> folderSet = Collect.getInstance().getInformOnlineState().getAccountFolders().keySet();
         Iterator<String> folderIds = folderSet.iterator();
         
         while (folderIds.hasNext()) {
             AccountFolder folder = Collect.getInstance().getInformOnlineState().getAccountFolders().get(folderIds.next());            
             
             if (folder.isReplicated()) {
                 Log.i(Collect.LOGTAG, tt + "about to begin scheduled replication of " + folder.getName());
                 
                 try {
                     replicate(folder.getId(), REPLICATE_PUSH);
                     replicate(folder.getId(), REPLICATE_PULL);
                 } catch (Exception e) {
                     Log.w(Collect.LOGTAG, tt + "problem replicating " + folder.getId() + ": " + e.toString());
                     e.printStackTrace();
                 }
             }
         }
     }
 }
