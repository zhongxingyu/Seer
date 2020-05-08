 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.mock.resource;
 
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.API;
 import com.redhat.rhevm.api.model.LinkHeader;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.ObjectFactory;
 import com.redhat.rhevm.api.resource.ApiResource;
 import com.redhat.rhevm.api.common.util.JAXBHelper;
 
 public class MockApiResource implements ApiResource {
 
     private static final String SEARCH_RELATION = "/search";
     private static final String SEARCH_TEMPLATE = "?search={query}";
 
     protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
 
     protected UriInfo ui;
 
     public UriInfo getUriInfo() {
         return ui;
     }
 
     @Context
     public void setUriInfo(UriInfo uriInfo) {
         ui = uriInfo;
     }
 
     private API addLinks(API api) {
         addLink(api, "capabilities", false);
         addLink(api, "clusters");
         addLink(api, "datacenters");
         addLink(api, "hosts");
         addLink(api, "networks", false); // powershell has no select-network command
         addLink(api, "storagedomains");
         addLink(api, "templates");
         addLink(api, "vms");
         return api;
     }
 
     private void addLink(API api, String rel, boolean searchable) {
         Link link = new Link();
         link.setRel(rel);
         link.setHref(combine(getUriInfo().getBaseUri().getPath(), rel) + "/");
         api.getLinks().add(link);
 
         if (searchable) {
             link = new Link();
             link.setRel(rel + SEARCH_RELATION);
             link.setHref(combine(getUriInfo().getBaseUri().getPath(), rel) + "/" + SEARCH_TEMPLATE);
             api.getLinks().add(link);
         }
     }
 
     private void addLink(API api, String rel) {
         addLink(api, rel, true);
     }
 
     private String addPath(UriBuilder uriBuilder, Link link) {
         String query = "";
        String path = link.getHref();
 
         // otherwise UriBuilder.build() will substitute {query}
         if (path.contains("?")) {
             query = path.substring(path.indexOf("?"));
             path = path.substring(0, path.indexOf("?") - 1);
         }
 
         link = JAXBHelper.clone(OBJECT_FACTORY.createLink(link));
         link.setHref(uriBuilder.clone().path(path).build() + query);
 
         return LinkHeader.format(link);
     }
 
     private void addHeader(API api, Response.ResponseBuilder responseBuilder, UriBuilder uriBuilder) {
         // concantenate links in a single header with a comma-separated value,
         // which is the canonical form according to:
         // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
         //
         StringBuffer header = new StringBuffer();
 
         for (Link l : api.getLinks()) {
             header.append(addPath(uriBuilder, l)).append(",");
         }
 
         header.setLength(header.length() - 1);
 
         responseBuilder.header("Link", header);
     }
 
     private Response.ResponseBuilder getResponseBuilder(API api) {
         UriBuilder uriBuilder = getUriInfo().getBaseUriBuilder();
 
         Response.ResponseBuilder responseBuilder = Response.ok();
 
         addHeader(api, responseBuilder, uriBuilder);
 
         return responseBuilder;
     }
 
     @Override
     public Response head() {
         API api = addLinks(new API());
         return getResponseBuilder(api).build();
     }
 
     @Override
     public Response get() {
         API api = addLinks(new API());
         return getResponseBuilder(api).entity(api).build();
     }
 
     private String combine(String head, String tail) {
         if (head.endsWith("/")) {
             head = head.substring(0, head.length() - 1);
         }
         if (tail.startsWith("/")) {
             tail = tail.substring(1);
         }
         return head + "/" + tail;
     }
 }
