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
 
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.Test;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import org.jboss.resteasy.client.ClientResponse;
 import com.redhat.rhevm.api.model.API;
 import com.redhat.rhevm.api.model.LinkHeader;
 import com.redhat.rhevm.api.model.Link;
 
 public class MockApiResourceTest extends MockTestBase {
 
     private static final String[] relationships = {
         "capabilities",
         "clusters",
         "clusters/search",
         "datacenters",
         "datacenters/search",
         "hosts",
         "hosts/search",
         "networks",
         "roles",
         "storagedomains",
         "storagedomains/search",
        "tags",
         "templates",
         "templates/search",
         "users",
         "users/search",
         "vmpools",
         "vmpools/search",
         "vms",
         "vms/search",
     };
 
     private void testResponse(ClientResponse<? extends Object> response) {
         assertEquals(Response.Status.Family.SUCCESSFUL, response.getResponseStatus().getFamily());
         assertEquals(Response.Status.OK, response.getResponseStatus());
 
         MultivaluedMap<String, String> headers = response.getHeaders();
 
         List<String> linkHeaders = headers.get("Link");
 
         assertNotNull(linkHeaders);
 
         List<Link> links = new ArrayList<Link>();
 
         for (String s : linkHeaders) {
             for (String t : s.split(",")) {
                 Link l = LinkHeader.parse(t);
                 assertEquals(t, LinkHeader.format(l));
                 links.add(l);
             }
         }
 
         assertEquals(relationships.length, links.size());
         for (int i = 0; i < relationships.length; i++) {
             assertEquals(relationships[i], links.get(i).getRel());
         }
     }
 
     @Test
     public void testEntryPointHead() throws Exception {
         ClientResponse<Object> response = api.head();
 
         testResponse(response);
     }
 
     @Test
     public void testEntryPointGet() throws Exception {
         ClientResponse<API> response = api.get();
 
         testResponse(response);
 
         API api = response.getEntity();
 
         assertEquals(relationships.length, api.getLinks().size());
         for (int i = 0; i < relationships.length; i++) {
             assertEquals(relationships[i], api.getLinks().get(i).getRel());
         }
     }
 }
