 package se.kth.infosys.login.couchbase;
 
 /*
  * Copyright (C) 2013 KTH, Kungliga tekniska hogskolan, http://www.kth.se
  *
  * This file is part of cas-server-integration-couchbase.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 
 import javax.validation.constraints.NotNull;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.couchbase.client.CouchbaseClient;
 import com.couchbase.client.protocol.views.DesignDocument;
 import com.couchbase.client.protocol.views.InvalidViewException;
 import com.couchbase.client.protocol.views.ViewDesign;
 
 /**
  * A factory class which produces a client for a particular Couchbase bucket.
  * A design consideration was that we want the server to start even if Couchbase
  * is unavailable, picking up the connection when Couchbase comes online. Hence
  * the creation of the client is made using a scheduled task which is repeated
  * until successful connection is made.
  */
 public class CouchbaseClientFactory extends TimerTask {
     private final Logger logger = LoggerFactory.getLogger(CouchbaseClientFactory.class);
     private static final int RETRY_INTERVAL = 10; // seconds.
     private final Timer timer = new Timer();
 
     private CouchbaseClient client;
 
     @NotNull
     private List<URI> uris;
 
     /* The name of the bucket, will use the default bucket unless otherwise specified. */
     private String bucket = "default";
 
     /* NOTE: username is currently not used in Couchbase 2.0, may be in the future. */
     private String username = "";
 
     /* Password for the bucket if any. */
     private String password = "";
 
     /* Design document and views to create in the bucket, if any. */
     private String designDocument;
     private List<ViewDesign> views;
 
 
     /**
      * Default constructor. 
      */
     public CouchbaseClientFactory() {}
 
 
     /**
      * Start initializing the client. This will schedule a task that retries
      * connection until successful.
      */
     public void initialize() {
         timer.scheduleAtFixedRate(this, new Date(), TimeUnit.SECONDS.toMillis(RETRY_INTERVAL));
     }
 
 
     /**
      * Inverse of initialize, shuts down the client, cancelling connection
      * task if not completed.
      * 
      * @throws Exception on errors.
      */
     public void shutdown() throws Exception {
         timer.cancel();
         timer.purge();
         if (client != null) {
             client.shutdown();
         }
     }
 
 
     /**
      * Fetch a client for the database.
      * 
      * @return the client if available.
      * @throws RuntimeException if client is not initialized yet.
      */
     public CouchbaseClient getClient() {
         if (client != null) {
             return client;
         } else {
             throw new RuntimeException("Conncetion to bucket " + bucket + " not initialized yet.");
         }
     }
 
 
     /**
      * Register indexes to ensure in the bucket when the client is initialized.
      * 
      * @param documentName name of the Couchbase design document.
      * @param views the list of Couchbase views (i.e. indexes) to create in the document.
      */
     public void ensureIndexes(final String documentName, final List<ViewDesign> views) {
         this.designDocument = documentName;
         this.views = views;
     }
 
 
     /**
      * Ensures that all views exists in the database.
      * 
      * @param documentName the name of the design document.
      * @param views the views to ensure exists in the database.
      */
    @SuppressWarnings("unchecked")
     private void doEnsureIndexes(final String documentName, final List<ViewDesign> views) {
        DesignDocument<ViewDesign> document;
         try {
             document = client.getDesignDocument(documentName);
             List<ViewDesign> oldViews = document.getViews();
 
             for (ViewDesign view : views) {
                 if (!isViewInList(view, oldViews)) {
                     throw new InvalidViewException("Missing view: " + view.getName());
                 }
             }
             logger.info("All views are already created for bucket " + bucket);
         } catch (final InvalidViewException e) {
             logger.warn("Missing indexes in database for document " + documentName + ", creating new.");
            document = new DesignDocument<ViewDesign>(documentName);
             for (ViewDesign view : views) {
                 document.getViews().add(view);
                 if (!client.createDesignDoc(document)) {
                     throw new InvalidViewException("Failed to create views.");
                 }
             }
         }
     }
 
 
     /**
      * @param needle the view design to look for
      * @param stack the list of view designs to look in
      * @return true if needle exists in stack
      */
     private static boolean isViewInList(final ViewDesign needle, final List<ViewDesign> stack) {
         for (ViewDesign view : stack) {
             if (equals(needle, view)) {
                 return true;
             }
         }
         return false;
     }
 
 
     /**
      * @param d1 a view design.
      * @param d2 another view design.
      * @return true if designs are equal.
      */
     private static boolean equals(final ViewDesign d1, final ViewDesign d2) {
         return (d1.getName().equals(d2.getName())
                 && d1.getMap().equals(d2.getMap())
                 && d1.getReduce().equals(d2.getReduce()));
     }
 
 
     /**
      * Task to initialize the Couchbase client.
      */
     public void run() {
         try {
             logger.info("Trying to connect to couchbase bucket " + bucket);
             client = new CouchbaseClient(uris, bucket, username, password);
             timer.cancel();
             if (views != null) {
                 doEnsureIndexes(designDocument, views);
             }
         } catch (final Exception e) {
             logger.error("Failed to connect to Couchbase bucket " + bucket + ", retrying...");
         }
     }
 
     public void setUris(final List<URI> uris) {
         this.uris = uris;
     }
 
     public void setBucket(final String bucket) {
         this.bucket = bucket;
     }
 
     public void setUsername(final String username) {
         this.username = username;
     }
 
     public void setPassword(final String password) {
         this.password = password;
     }
 }
