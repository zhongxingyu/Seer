 /*
  * This is a utility project for wide range of applications
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.rest.atom.resources;
 
 import com.smartitengineering.util.rest.atom.resources.domain.SomeDomain;
 import java.net.URI;
 import java.util.Date;
 import java.util.UUID;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.HEAD;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 import org.apache.abdera.Abdera;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.model.Link;
 
 /**
  *
  * @author imyousuf
  */
 @Path("/")
 public class SomeDomainResource {
 
  public static final String COUNT = "count";
  public static final String STARTINDEX = "startIndex";
  public static final int DOMAIN_SIZE = 100;
  public static final SomeDomain[] DOMAIN_DATA;
 
   static {
     DOMAIN_DATA = new SomeDomain[DOMAIN_SIZE];
     for (int i = 0; i < DOMAIN_SIZE; ++i) {
       DOMAIN_DATA[i] = new SomeDomain();
       DOMAIN_DATA[i].setTestName(UUID.randomUUID().toString());
     }
   }
   @Context
   public UriInfo uriInfo;
   protected final Factory abderaFactory = Abdera.getNewFactory();
 
   protected UriBuilder setBaseUri(final UriBuilder builder) throws IllegalArgumentException {
     final URI baseUri = uriInfo.getBaseUri();
     UriBuilder result = UriBuilder.fromUri(baseUri);
     final URI uri = builder.build();
     result.path(uri.getPath());
     return result;
   }
 
   protected Feed getFeed(String title, Date updated) {
     return getFeed(uriInfo.getRequestUri().toString(), title, updated);
   }
 
   protected Feed getFeed(String id, String title, Date updated) {
     Feed feed = getFeed();
     feed.setId(id);
     feed.setTitle(title);
     feed.setUpdated(updated);
     return feed;
   }
 
   protected Feed getFeed() {
     Feed feed = abderaFactory.newFeed();
     feed.addLink(getSelfLink());
     feed.addAuthor("author");     ///error in adding getDefaultAuthor();
     return feed;
   }
 
   protected Link getSelfLink() {
     Link selfLink = abderaFactory.newLink();
     selfLink.setHref(uriInfo.getRequestUri().toString());
     selfLink.setRel(Link.REL_SELF);
     selfLink.setMimeType(MediaType.APPLICATION_ATOM_XML);
     return selfLink;
   }
 
   @HEAD
   @Produces(MediaType.APPLICATION_ATOM_XML)
   public Response get() {
     return Response.noContent().build();
   }
 
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/domain/{index}")
   public SomeDomain getDomain(@PathParam("index") int index) {
     return DOMAIN_DATA[index];
   }
 
   @GET
   @Produces(MediaType.APPLICATION_ATOM_XML)
   @Path("feed")
   public Response getFeed(@QueryParam(STARTINDEX) @DefaultValue("0") final int startIndex,
                           @QueryParam(COUNT) @DefaultValue("5") final int count) {
     final Feed feed = getFeed("Feed!", new Date());
     final UriBuilder builder = uriInfo.getAbsolutePathBuilder();
     final int nextIndex = startIndex + count;
     if (nextIndex < DOMAIN_SIZE) {
       builder.queryParam(STARTINDEX, nextIndex);
       builder.queryParam(COUNT, count);
       Link link = abderaFactory.newLink();
       link.setRel(Link.REL_NEXT);
       link.setMimeType(MediaType.APPLICATION_ATOM_XML);
      link.setHref(builder.build().toString());
       feed.addLink(link);
     }
     final int previousIndex = startIndex - count;
     if (previousIndex > 0) {
       builder.queryParam(STARTINDEX, previousIndex);
       builder.queryParam(COUNT, count);
       Link link = abderaFactory.newLink();
       link.setRel(Link.REL_PREVIOUS);
       link.setMimeType(MediaType.APPLICATION_ATOM_XML);
      link.setHref(builder.build().toString());
       feed.addLink(link);
     }
     final int toIndex;
     final int probableToIndex = startIndex + count - 1;
     if (probableToIndex >= DOMAIN_SIZE) {
       toIndex = DOMAIN_SIZE - 1;
     }
     else {
       toIndex = probableToIndex;
     }
     for (int i = startIndex; i <= toIndex; ++i) {
       UriBuilder uriBuilder = UriBuilder.fromPath("/domain/" + i);
       uriBuilder = setBaseUri(uriBuilder);
       Entry entry = abderaFactory.newEntry();
       entry.setId(Integer.toString(i));
       entry.setTitle("Domain " + Integer.toString(i));
       entry.setUpdated(new Date());
       Link link = entry.addLink(uriBuilder.build().toString(), Link.REL_ALTERNATE);
       link.setMimeType(MediaType.APPLICATION_JSON);
       feed.addEntry(entry);
     }
     final ResponseBuilder responseBuilder = Response.ok(feed);
     return responseBuilder.build();
   }
 }
