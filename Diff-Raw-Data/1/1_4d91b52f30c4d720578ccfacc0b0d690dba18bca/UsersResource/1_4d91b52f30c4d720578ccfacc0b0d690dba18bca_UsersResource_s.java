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
 
 package com.cedarsoft.rest.sample.rest;
 
 import com.cedarsoft.rest.sample.jaxb.User;
 import com.cedarsoft.rest.sample.jaxb.UserMapping;
 import com.google.inject.Inject;
 import com.sun.jersey.api.NotFoundException;
 import org.apache.commons.io.FileUtils;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.UriInfo;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 @Path( UserMapping.PATH_USERS )
 @Produces( {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
 public class UsersResource {
   @NotNull
   @Context
   private UriInfo uriInfo;
   @Context
   private Request request;
   @NotNull
   private final List<com.cedarsoft.rest.sample.User> users;
   @NotNull
   private final UserMapping userMapping;
 
   @Inject
   public UsersResource( @NotNull UserMapping userMapping, @NotNull List<? extends com.cedarsoft.rest.sample.User> users ) throws IOException {
    FileUtils.touch( new File( "/tmp/UsersResources" ) );
     this.userMapping = userMapping;
     this.users = new ArrayList<com.cedarsoft.rest.sample.User>( users );
   }
 
   @GET
   @NotNull
   public List<User.Stub> getUsers( @Context HttpHeaders headers, @QueryParam( "minId" ) int minId, @QueryParam( "max-id" ) int maxId ) throws URISyntaxException {
     return userMapping.getJaxbStubs( users, uriInfo.getBaseUriBuilder() );
   }
 
   @GET
   @Path( UserMapping.PATH_ID )
   @NotNull
   public User.Jaxb getUserResource( @PathParam( "id" ) @NotNull @NonNls String id ) throws URISyntaxException {
     for ( com.cedarsoft.rest.sample.User user : users ) {
       if ( user.getEmail().equals( id ) ) {
         return userMapping.getJaxbObject( user, uriInfo.getBaseUriBuilder() );
       }
     }
 
     throw new NotFoundException();
   }
 
   @GET
   @Path( "test" )
   @NotNull
   public User.Jaxb getUser() throws URISyntaxException {
     com.cedarsoft.rest.sample.User user = new com.cedarsoft.rest.sample.User( "test@test.com", "Test User" );
     user.addFriend( new com.cedarsoft.rest.sample.User( "friend@asdf.de", "A Friend" ) );
     return userMapping.getJaxbObject( user, uriInfo.getBaseUriBuilder() );
   }
 }
