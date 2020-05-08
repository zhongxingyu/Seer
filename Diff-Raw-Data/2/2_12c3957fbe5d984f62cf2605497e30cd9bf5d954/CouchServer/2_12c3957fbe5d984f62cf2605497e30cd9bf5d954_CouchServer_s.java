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
 
 import com.cedarsoft.serialization.jackson.ListSerializer;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.ClientFilter;
 import com.sun.jersey.client.apache4.ApacheHttpClient4;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.List;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class CouchServer {
   @Nonnull
   public static final String ALL_DBS = "_all_dbs";
 
   @Nonnull
   private final Client client;
   @Nonnull
   private final ClientFilter[] clientFilters;
   @Nonnull
   private final WebResource root;
 
 
   public CouchServer( @Nonnull URI uri, @Nullable ClientFilter... filters ) {
     //    client = Client.create();
     client = ApacheHttpClient4.create();
     if ( filters != null ) {
       for ( ClientFilter filter : filters ) {
         if ( filter != null ) {
           client.addFilter( filter );
         }
       }
     }
     this.clientFilters = filters == null ? new ClientFilter[0] : filters.clone();
     root = client.resource( uri );
   }
 
   public void deleteDatabase( @Nonnull String dbName ) throws ActionFailedException {
     ClientResponse response = root.path( dbName ).delete( ClientResponse.class );
     try {
       ActionResponse.verifyNoError( response );
    } catch ( ActionFailedException e ) {
       response.close();
     }
   }
 
   public boolean createDatabase( @Nonnull String dbName ) throws ActionFailedException {
     ClientResponse response = root.path( dbName ).put( ClientResponse.class );
     try {
       if ( response.getStatus( ) == 201 ) {
         return true;
       }
 
       ActionResponse.verifyNoError( response );
     } finally {
       response.close();
     }
     return false;
   }
 
   @Nonnull
   public List<? extends String> listDatabases( ) throws IOException {
     InputStream in = root.path( ALL_DBS ).get( InputStream.class );
 
     try {
       ListSerializer listSerializer = new ListSerializer( );
       List<? extends Object> dbs = listSerializer.deserialize( in );
 
       return ( List<? extends String> ) dbs;
     } finally {
       in.close();
     }
   }
 
   @Nonnull
   public ClientResponse get( @Nonnull String uri ) {
     return root.path( uri ).get( ClientResponse.class );
   }
 
   @Nonnull
   public ClientResponse put( @Nonnull String uri, @Nonnull byte[] bytes, @Nonnull String mediaType ) {
     return root.path( uri ).type( mediaType ).put( ClientResponse.class, bytes );
   }
 }
