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
 
 import com.cedarsoft.couchdb.io.CouchSerializerWrapper;
 import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
 import com.cedarsoft.serialization.jackson.JacksonSupport;
 import com.cedarsoft.serialization.jackson.test.compatible.JacksonParserWrapper;
 import com.cedarsoft.version.Version;
 import com.cedarsoft.version.VersionException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.JsonToken;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.ws.rs.HEAD;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Updates the design documents
  *
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class DesignDocumentsUpdater {
   @Nonnull
   private static final Logger log = Logger.getLogger( DesignDocumentsUpdater.class.getName() );
   @Nonnull
   private final CouchDatabase database;
 
   /**
    * Creates a new updater for the given database
    *
    * @param database the database
    */
   public DesignDocumentsUpdater( @Nonnull CouchDatabase database ) {
     this.database = database;
   }
 
   /**
    * Uploads the given design documents to the database
    *
    * @param designDocuments the design documents
    * @throws IOException
    * @throws ActionFailedException
    */
   public void update( @Nonnull Iterable<? extends DesignDocument> designDocuments ) throws IOException, ActionFailedException {
     for ( DesignDocument designDocument : designDocuments ) {
       if ( !designDocument.hasViews() ) {
         continue;
       }
 
      log.info( "Updating db <" + designDocument.getId() + ">:" );
 
       String path = designDocument.getDesignDocumentPath();
       WebResource resource = database.getDbRoot().path( path );
 
 
       @Nullable Revision currentRevision = getRevision( resource );
 
       log.fine( "PUT: " + resource.toString() );
       ClientResponse response = resource.put( ClientResponse.class, designDocument.createJson(currentRevision) );
       ActionResponse.verifyNoError( response );
     }
   }
 
   /**
    * Returns the current revision (if there is one) or null
    *
    * @param path the path
    * @return the revision or null if there is no revision
    */
   @Nullable
   private Revision getRevision( @Nonnull WebResource path ) throws ActionFailedException, IOException {
     log.fine( "HEAD: " + path.toString() );
     ClientResponse response = path.get( ClientResponse.class );
     log.fine( "\tStatus: " + response.getStatus() );
     if ( response.getStatus() == 404 ) {
       return null;
     }
 
     ActionResponse.verifyNoError( response );
 
     if ( response.getStatus() != 200 ) {
       throw new IllegalStateException( "Invalid response: " + response.getStatus() + ": " + response.getEntity( String.class ) );
     }
 
     JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
     InputStream entityInputStream = response.getEntityInputStream();
     try {
       JsonParser parser = jsonFactory.createJsonParser( entityInputStream );
       JacksonParserWrapper wrapper = new JacksonParserWrapper( parser );
 
       wrapper.nextToken( JsonToken.START_OBJECT );
 
       wrapper.nextFieldValue( "_id" );
       wrapper.nextFieldValue( "_rev" );
       return new Revision( wrapper.getText() );
     } finally {
       entityInputStream.close();
     }
   }
 }
