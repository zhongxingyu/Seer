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
 package com.redhat.rhevm.api.powershell.resource;
 
 import java.net.URI;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.API;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellTestUtils;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 public class PowerShellApiResourceTest
     extends AbstractPowerShellResourceTest<API, PowerShellApiResource> {
 
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
 
     private static final String[] hrefs = {
         BASE_PATH + "/capabilities/",
         BASE_PATH + "/clusters/",
         BASE_PATH + "/clusters/?search={query}",
         BASE_PATH + "/datacenters/",
         BASE_PATH + "/datacenters/?search={query}",
         BASE_PATH + "/hosts/",
         BASE_PATH + "/hosts/?search={query}",
         BASE_PATH + "/networks/",
         BASE_PATH + "/roles/",
         BASE_PATH + "/storagedomains/",
         BASE_PATH + "/storagedomains/?search={query}",
         BASE_PATH + "/tags/",
         BASE_PATH + "/templates/",
         BASE_PATH + "/templates/?search={query}",
         BASE_PATH + "/users/",
         BASE_PATH + "/users/?search={query}",
         BASE_PATH + "/vmpools/",
         BASE_PATH + "/vmpools/?search={query}",
         BASE_PATH + "/vms/",
         BASE_PATH + "/vms/?search={query}",
     };
 
     private static String GET_SYSTEM_STATS_COMMAND = "get-systemstatistics";
 
     private String formatSystemStats() {
         return PowerShellTestUtils.readClassPathFile("systemstats.xml");
     }
 
     protected PowerShellApiResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
         PowerShellApiResource resource = new PowerShellApiResource();
         resource.setExecutor(executor);
         resource.setParser(parser);
         resource.setPowerShellPoolMap(poolMap);
         return resource;
     }
 
     @Test
     public void testGet() throws Exception {
         setUpSystemStatsExpectations(GET_SYSTEM_STATS_COMMAND, formatSystemStats());
         resource.setUriInfo(setUpUriInfo());
         verifyResponse(resource.get());
     }
 
     private void setUpSystemStatsExpectations(String command, String ret) throws Exception {
         mockStatic(PowerShellCmd.class);
         expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
     }
 
     private UriInfo setUpUriInfo() {
         UriBuilder uriBuilder = createMock(UriBuilder.class);
         expect(uriBuilder.clone()).andReturn(uriBuilder).anyTimes();
 
         for (String rel : relationships) {
             UriBuilder colUriBuilder = createMock(UriBuilder.class);
             expect(colUriBuilder.build()).andReturn(URI.create(URI_ROOT + SLASH + rel + "/")).anyTimes();
             if (rel.endsWith("/search")) {
                expect(uriBuilder.path((BASE_PATH + "/" + rel).replace("/search", ""))).andReturn(colUriBuilder);
             } else {
                expect(uriBuilder.path(BASE_PATH + "/" + rel + SLASH)).andReturn(colUriBuilder);
             }
         }
 
         UriInfo uriInfo = setUpBasicUriExpectations();
         expect(uriInfo.getBaseUriBuilder()).andReturn(uriBuilder);
 
         replayAll();
 
         return uriInfo;
     }
 
     private void verifyResponse(Response response) {
         assertEquals(200, response.getStatus());
         assertTrue(response.getEntity() instanceof API);
         verifyApi((API)response.getEntity());
     }
 
     private void verifyApi(API api) {
         assertNotNull(api);
         assertNotNull(api.getLinks());
 
         assertEquals(relationships.length, api.getLinks().size());
         for (int i = 0; i < relationships.length; i++) {
             Link l = api.getLinks().get(i);
             assertNotNull(l);
             assertEquals(relationships[i], l.getRel());
             assertEquals(hrefs[i], l.getHref());
         }
 
         assertNotNull(api.getSummary());
         assertEquals(1L, api.getSummary().getVMs().getTotal());
         assertEquals(0L, api.getSummary().getVMs().getActive());
         assertEquals(1L, api.getSummary().getHosts().getTotal());
         assertEquals(0L, api.getSummary().getHosts().getActive());
         assertEquals(1L, api.getSummary().getUsers().getTotal());
         assertEquals(0L, api.getSummary().getUsers().getActive());
         assertEquals(2L, api.getSummary().getStorageDomains().getTotal());
         assertEquals(0L, api.getSummary().getStorageDomains().getActive());
     }
 
     private static void assertEquals(long expected, Long actual) {
         assertEquals(expected, actual.longValue());
     }
 }
