 /*******************************************************************************
  * Copyright 2011 John Casey
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.commonjava.couch.change;
 
 import static org.commonjava.couch.util.UrlUtils.buildUrl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.enterprise.inject.Alternative;
 import javax.inject.Named;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.commonjava.couch.change.dispatch.CouchChangeDispatcher;
 import org.commonjava.couch.conf.CouchDBConfiguration;
 import org.commonjava.couch.db.CouchDBException;
 import org.commonjava.couch.db.CouchManager;
 import org.commonjava.couch.io.CouchHttpClient;
 import org.commonjava.couch.io.Serializer;
 import org.commonjava.couch.model.AbstractCouchDocument;
 import org.commonjava.couch.model.CouchDocRef;
 import org.commonjava.util.logging.Logger;
 
 import com.google.gson.annotations.SerializedName;
 
 @Named( "dont-use-directly" )
 @Alternative
 public class CouchChangeListener
     implements Runnable
 {
 
     private static final String CHANGE_LISTENER_DOCID = "change-listener-metadata";
 
     private static final String CHANGES_SERVICE = "_changes";
 
     private final Logger logger = new Logger( getClass() );
 
     private final CouchChangeDispatcher dispatcher;
 
     private final CouchDBConfiguration config;
 
     private final CouchHttpClient http;
 
     private final CouchManager couch;
 
     private final Serializer serializer;
 
     private ChangeListenerMetadata metadata;
 
     private Thread listenerThread;
 
     private boolean running = false;
 
     private final Object internalLock = new Object();
 
     public CouchChangeListener( final CouchChangeDispatcher dispatcher, final CouchHttpClient http,
                                 final CouchDBConfiguration config, final CouchManager couch, final Serializer serializer )
     {
         this.dispatcher = dispatcher;
         this.http = http;
         this.config = config;
         this.couch = couch;
         this.serializer = serializer;
     }
 
     public void startup()
         throws CouchDBException
     {
         startup( true );
     }
 
     public void startup( final boolean wait )
         throws CouchDBException
     {
         if ( running )
         {
             return;
         }
 
         metadata = couch.getDocument( new CouchDocRef( CHANGE_LISTENER_DOCID ), ChangeListenerMetadata.class );
         if ( metadata == null )
         {
             metadata = new ChangeListenerMetadata();
         }
 
         logger.info( "starting change-listener thread..." );
         listenerThread = new Thread( this );
         listenerThread.setDaemon( true );
         listenerThread.start();
 
         if ( wait )
         {
             synchronized ( internalLock )
             {
                 while ( !running )
                 {
                     logger.info( "Waiting for change listener to startup..." );
                     try
                     {
                         internalLock.wait( 100 );
                     }
                     catch ( final InterruptedException e )
                     {
                         logger.info( "Interrupted..." );
                         break;
                     }
                 }
 
                 logger.info( "change-listener is running." );
             }
         }
     }
 
     public void shutdown()
         throws CouchDBException
     {
         if ( listenerThread != null )
         {
             listenerThread.interrupt();
 
             while ( listenerThread.isAlive() )
             {
                 logger.info( "Waiting for change-listener shutdown..." );
                 synchronized ( internalLock )
                 {
                     try
                     {
                         internalLock.wait( 2000 );
                     }
                     catch ( final InterruptedException e )
                     {
                         break;
                     }
                 }
             }
         }
 
         if ( metadata != null )
         {
            if ( metadata.getLastProcessedSequenceId() > 0 )
             {
                 couch.store( metadata, false );
             }
         }
 
         running = false;
 
         synchronized ( this )
         {
             notifyAll();
         }
     }
 
     public boolean isRunning()
     {
         return running;
     }
 
     @Override
     public void run()
     {
         final CouchDocChangeDeserializer docDeserializer = new CouchDocChangeDeserializer();
 
         all: while ( !Thread.interrupted() )
         {
             HttpGet get;
             try
             {
                 final String url = buildUrl( config.getDatabaseUrl(), metadata.getUrlParameters(), CHANGES_SERVICE );
 
                 get = new HttpGet( url );
             }
             catch ( final MalformedURLException e )
             {
                 logger.error( "Failed to construct changes URL for db: %s. Reason: %s", e, config.getDatabaseUrl(),
                               e.getMessage() );
                 break;
             }
 
             String encoding = null;
             try
             {
                 // logger.info( "requesting changes..." );
 
                 final HttpResponse response = http.executeHttpWithResponse( get, "Failed to open changes stream." );
 
                 if ( response.getEntity() == null )
                 {
                     logger.error( "Changes stream did not return a response body." );
                 }
                 else
                 {
                     final Header encodingHeader = response.getEntity()
                                                           .getContentEncoding();
                     if ( encodingHeader == null )
                     {
                         encoding = "UTF-8";
                     }
                     else
                     {
                         encoding = encodingHeader.getValue();
                     }
 
                     final InputStream stream = response.getEntity()
                                                        .getContent();
 
                     running = true;
                     synchronized ( internalLock )
                     {
                         internalLock.notifyAll();
                     }
 
                     final CouchDocChangeList changes =
                         serializer.fromJson( stream, encoding, CouchDocChangeList.class, docDeserializer );
 
                     for ( final CouchDocChange change : changes )
                     {
                         logger.info( "Processing change: %s", change.getId() );
 
                         if ( !change.getId()
                                     .equals( CHANGE_LISTENER_DOCID ) )
                         {
                             metadata.setLastProcessedSequenceId( change.getSequence() );
                             dispatcher.documentChanged( change );
                         }
                     }
                 }
             }
             catch ( final CouchDBException e )
             {
                 logger.error( "Failed to read changes stream for db: %s. Reason: %s", e, config.getDatabaseUrl(),
                               e.getMessage() );
                 break;
             }
             catch ( final UnsupportedEncodingException e )
             {
                 logger.error( "Invalid content encoding for changes response: %s. Reason: %s", e, encoding,
                               e.getMessage() );
                 break;
             }
             catch ( final IOException e )
             {
                 logger.error( "Error reading changes response content. Reason: %s", e, e.getMessage() );
                 break;
             }
             finally
             {
                 http.cleanup( get );
             }
 
             try
             {
                 Thread.sleep( 2000 );
             }
             catch ( final InterruptedException e )
             {
                 break all;
             }
         }
 
         synchronized ( internalLock )
         {
             internalLock.notifyAll();
         }
     }
 
     static final class ChangeListenerMetadata
         extends AbstractCouchDocument
     {
 
         @SerializedName( "last_seq" )
         private int lastProcessedSequenceId;
 
         ChangeListenerMetadata()
         {
             setCouchDocId( CHANGE_LISTENER_DOCID );
         }
 
         public Map<String, String> getUrlParameters()
         {
             final Map<String, String> params = new HashMap<String, String>();
             // params.put( "feed", "continuous" );
             if ( lastProcessedSequenceId > 0 )
             {
                 params.put( "since", Integer.toString( lastProcessedSequenceId ) );
             }
 
             return params;
         }
 
         int getLastProcessedSequenceId()
         {
             return lastProcessedSequenceId;
         }
 
         void setLastProcessedSequenceId( final int lastProcessedSequenceId )
         {
             this.lastProcessedSequenceId = lastProcessedSequenceId;
         }
 
     }
 
 }
