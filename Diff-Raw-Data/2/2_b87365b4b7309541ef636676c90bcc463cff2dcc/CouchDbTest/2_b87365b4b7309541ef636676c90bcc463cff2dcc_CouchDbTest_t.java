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
 
 package com.cedarsoft.couchdb;
 
 import com.cedarsoft.CanceledException;
 import org.jcouchdb.db.Database;
 import org.jcouchdb.db.Server;
 import org.jcouchdb.db.ServerImpl;
 import org.jcouchdb.exception.CouchDBException;
 import org.jcouchdb.util.CouchDBUpdater;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public abstract class CouchDbTest {
   @NotNull
   protected final URI serverURI;
 
   protected CouchDbTest() {
     try {
       serverURI = new URI( "http://localhost:5984" );
     } catch ( URISyntaxException e ) {
       throw new RuntimeException( e );
     }
   }
 
   protected CouchDatabase db;
   private Server server;
 
   @Before
   public void setupDb() throws IOException, URISyntaxException {
     server = new ServerImpl( serverURI.getHost(), serverURI.getPort() );
     db = createDb( getTestDbName() );
 
     //publish views
     publishViews();
   }
 
   @NotNull
   @NonNls
   protected String getTestDbName() {
     return "couch_unit_test";
   }
 
   @NotNull
   protected CouchDatabase createDb( @NotNull @NonNls String dbName ) {
     try {
       server.deleteDatabase( dbName );
     } catch ( CouchDBException ignore ) {
     }
     assertTrue( server.createDatabase( dbName ) );
 
     return new CouchDatabase( serverURI, dbName );
   }
 
   protected void publishViews() throws URISyntaxException, IOException {
     CouchDBUpdater updater = new CouchDBUpdater();
     updater.setCreateDatabase( false );
     updater.setDatabase( new Database( server, db.getDbName() ) );
 
     try {
       URL resource = getViewResource();
       assertNotNull( resource );
       File file = new File( resource.toURI() );
       File viewsDir = file.getParentFile().getParentFile();
 
       assertTrue( viewsDir.isDirectory() );
       updater.setDesignDocumentDir( viewsDir );

      updater.updateDesignDocuments();
     } catch ( CanceledException ignore ) {
     }
   }
 
   /**
    * Returns one view resource that is used to find the base dir for all views
    *
    * @return one view resource
    *
    * @throws CanceledException if no views shall be uploaded for the test
    */
   @NotNull
   protected URL getViewResource() throws CanceledException {
     throw new CanceledException();
   }
 }
