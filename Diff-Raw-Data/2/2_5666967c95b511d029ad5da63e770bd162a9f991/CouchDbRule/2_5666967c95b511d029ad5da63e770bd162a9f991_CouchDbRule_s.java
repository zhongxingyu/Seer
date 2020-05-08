 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.couchdb.test.utils;
 
 import com.cedarsoft.couchdb.CouchDatabase;
 import com.cedarsoft.couchdb.CouchServer;
 import com.cedarsoft.couchdb.core.ActionFailedException;
 import com.cedarsoft.couchdb.core.CouchDbException;
 import com.cedarsoft.couchdb.core.DesignDocument;
 import com.cedarsoft.couchdb.update.DesignDocumentsProvider;
 import com.cedarsoft.couchdb.update.DesignDocumentsUpdateService;
 import com.cedarsoft.exceptions.CanceledException;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.client.apache4.ApacheHttpClient4;
 import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
 import org.apache.http.impl.conn.AbstractPooledConnAdapter;
 import org.apache.http.impl.conn.SingleClientConnManager;
 import org.fest.assertions.Assertions;
 import org.fest.reflect.core.Reflection;
 import org.junit.rules.*;
 import org.junit.runners.model.*;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  * @noinspection UseOfSystemOutOrSystemErr
  */
 public class CouchDbRule implements MethodRule {
 
   public static final String KEY_DB_NAME = "couchdb.unittests.db.name";
 
   public static final String KEY_SERVER_URI = "couchdb.unittests.server.uri";
 
   public static final String KEY_SKIP_DELETE_DB = "couchdb.unittests.skip.deletion";
 
 
   public static final String KEY_USER = "couchdb.unittests.username";
 
   public static final String KEY_PASS = "couchdb.unittests.password";
   public static final int DEFAULT_PORT = 80;
   public static final String COUCH_UNIT_TEST_PREFIX = "couch_unit_test";
 
   @Nullable
   protected CouchDatabase db;
 
   /**
    * Contains all dbs that are currently active
    */
   private final Set<CouchDatabase> dbs = new HashSet<>();
 
   @Nullable
   protected Client client;
 
   @Nullable
   protected URI serverURI;
   @Nullable
   private CouchServer server;
   @Nullable
   private final DesignDocumentsProvider designDocumentsProvider;
   @Nullable
 
   private final String dbBaseName;
 
 
   public CouchDbRule() {
     this( null );
   }
 
   /**
    * The view resource will be used to detect the views that are copied to the server
    *
    * @param designDocumentsProvider the optional design documents provider
    */
   public CouchDbRule( @Nullable DesignDocumentsProvider designDocumentsProvider ) {
     this( designDocumentsProvider, null );
   }
 
   public CouchDbRule( @Nullable DesignDocumentsProvider designDocumentsProvider, @Nullable String dbBaseName ) {
     this.designDocumentsProvider = designDocumentsProvider;
     this.dbBaseName = dbBaseName;
 
     LAST_INSTANCE = this;
   }
 
   @Nullable
   private static CouchDbRule LAST_INSTANCE;
 
   @Nullable
   public static CouchDbRule getLastInstance() {
     return LAST_INSTANCE;
   }
 
   public void before() throws IOException, URISyntaxException, CouchDbException {
     client = ApacheHttpClient4.create();
 
     @Nullable HTTPBasicAuthFilter authFilter = getAuthFilter( );
     if ( authFilter != null ) {
       assert client != null;
       client.addFilter( authFilter );
     }
 
     createServer();
     db = createDb( createNewTestDbName() );
   }
 
   public void createServer() throws URISyntaxException {
     URI currentUri = getServerUri();
     serverURI = currentUri;
 
     assert client != null;
     this.server = new CouchServer(client.resource( currentUri ));
 
     //Verify the CouchDB version
     ClientResponse response = server.get( "" );
     try {
       String content = response.getEntity( String.class );
      Assertions.assertThat( content.trim() ).isEqualTo( "{\"couchdb\":\"Welcome\",\"version\":\"1.2.0\"}" );
     } finally {
       response.close();
     }
   }
 
   @Nullable
   private HTTPBasicAuthFilter getAuthFilter( ) {
     @Nullable String username = getUsername( );
     @Nullable String password = getPassword( );
 
     if ( username == null && password == null ) {
       return null;
     }
 
     if ( username == null || password == null ) {
       throw new IllegalStateException( "You need both password *and* user name" );
     }
 
     return new HTTPBasicAuthFilter( username, password );
   }
 
   @Nullable
   protected String getUsername() {
     return System.getProperty( KEY_USER );
   }
 
   @Nullable
   protected String getPassword() {
     return System.getProperty( KEY_PASS );
   }
 
   public void after() throws ActionFailedException {
     ensureConnectionsClosed();
 
     deleteDatabases();
     client = null;
   }
 
   public void ensureConnectionsClosed() {
     assert client != null;
     try {
       //Ensure client is empty
       ApacheHttpClient4Handler clientHandler = ( ( ApacheHttpClient4 ) client ).getClientHandler();
       SingleClientConnManager connectionManager = ( SingleClientConnManager ) clientHandler.getHttpClient().getConnectionManager();
       @Nullable AbstractPooledConnAdapter managedConn = ( AbstractPooledConnAdapter ) Reflection.field( "managedConn" ).ofType( Class.forName( "org.apache.http.impl.conn.SingleClientConnManager$ConnAdapter" ) ).in( connectionManager ).get();
       if ( managedConn != null ) {
         throw new IllegalStateException( "Connection not closed properly: " + managedConn.getRoute() );
       }
     } catch ( ClassNotFoundException e ) {
       throw new RuntimeException( e );
     }
   }
 
   protected void deleteDatabases() throws ActionFailedException {
     if ( Boolean.parseBoolean( System.getProperty( KEY_SKIP_DELETE_DB ) ) ) {
       System.out.println( "----------------------------" );
       System.out.println( "Skipping deletion of " + getCurrentDb().getDbName() );
       System.out.println( "----------------------------" );
       return;
     }
 
     CouchServer currentServer = server;
     if ( currentServer != null ) {
       for ( Iterator<CouchDatabase> iterator = dbs.iterator(); iterator.hasNext(); ) {
         CouchDatabase couchDatabase = iterator.next();
         currentServer.deleteDatabase( couchDatabase.getDbName() );
         iterator.remove();
       }
     }
     db = null;
   }
 
   @Nonnull
   public CouchDatabase createDb( @Nonnull String dbName ) throws IOException, URISyntaxException, CouchDbException {
     assert server != null;
     try {
       server.deleteDatabase( dbName );
     } catch ( Exception ignore ) {
     }
 
     assertTrue( server.createDatabase( dbName ) );
 
     CouchDatabase couchDatabase = getCouchDatabaseObject( dbName );
 
     publishViews( couchDatabase );
 
     this.dbs.add( couchDatabase );
     return couchDatabase;
   }
 
   public void deleteDb( @Nonnull String dbName ) throws ActionFailedException {
     CouchServer currentServer = server;
     if ( currentServer == null ) {
       throw new IllegalArgumentException( "Invalid state - server is null" );
     }
     currentServer.deleteDatabase( dbName );
   }
 
   /**
    * Creates  a new database object - but does *not* create anything on the server
    *
    * @param dbName the db name
    * @return the couch database object
    */
   @Nonnull
   public CouchDatabase getCouchDatabaseObject( @Nonnull String dbName ) {
     URI uri = serverURI;
     assert uri != null;
     assert client != null;
 
     @Nullable String username = getUsername();
     @Nullable String password = getPassword();
     if ( username != null && password != null ) {
       client.addFilter( new HTTPBasicAuthFilter( username, password ) );
     }
 
     return CouchDatabase.create( client, uri, dbName );
   }
 
   public void publishViews( @Nonnull CouchDatabase couchDatabase ) throws IOException, ActionFailedException {
     try {
       DesignDocumentsProvider provider = getDesignDocumentsProvider();
       if ( provider == null ) {
         return;
       }
 
       List<? extends DesignDocument> designDocuments = provider.getDesignDocuments();
       if ( designDocuments.isEmpty() ) {
         return;
       }
 
       DesignDocumentsUpdateService designDocumentsUpdateService = new DesignDocumentsUpdateService( couchDatabase );
       designDocumentsUpdateService.updateIfNecessary( provider );
     } catch ( CanceledException ignore ) {
     }
   }
 
   /**
    * Returns the design documents provider - if there are any
    *
    * @return the provider or null
    */
   @Nullable
   public DesignDocumentsProvider getDesignDocumentsProvider() {
     return designDocumentsProvider;
   }
 
   @Override
   public Statement apply( final Statement base, FrameworkMethod method, Object target ) {
     return new Statement() {
       @Override
       public void evaluate() throws Throwable {
         before();
         try {
           base.evaluate();
         } finally {
           after();
         }
       }
     };
   }
 
   @Nonnull
   private final Random random = new Random();
 
   @Nonnull
   public String createNewTestDbName() {
     //noinspection MagicNumber
     return getTestDbBaseName() + "_" + System.currentTimeMillis() + "_" + random.nextInt( 100000 );
   }
 
   @Nonnull
   public String getTestDbBaseName() {
     if ( dbBaseName != null ) {
       return dbBaseName;
     }
     return System.getProperty( KEY_DB_NAME, COUCH_UNIT_TEST_PREFIX );
   }
 
   @Nonnull
   public URI getServerUri() throws URISyntaxException {
     return new URI( System.getProperty( KEY_SERVER_URI, "http://localhost:5984" ) );
   }
 
   @Nonnull
   public CouchDatabase getCurrentDb() {
     if ( db == null ) {
       throw new IllegalStateException( "No db available" );
     }
     return db;
   }
 
   @Nonnull
   public URI getCurrentServerURI() {
     if ( serverURI == null ) {
       throw new IllegalStateException( "No server uri" );
     }
     return serverURI;
   }
 
   @Nonnull
   public CouchServer getCurrentServer( ) {
     if ( server == null ) {
       throw new IllegalStateException( "No server " );
     }
     return server;
   }
 }
