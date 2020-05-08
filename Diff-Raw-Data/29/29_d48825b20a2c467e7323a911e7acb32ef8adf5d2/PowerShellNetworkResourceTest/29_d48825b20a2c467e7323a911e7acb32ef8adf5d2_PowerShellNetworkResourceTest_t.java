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
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Fault;
 import com.redhat.rhevm.api.model.Network;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.isA;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellNetworkResourceTest extends AbstractPowerShellResourceTest<Network, PowerShellNetworkResource> {
 
    private static final String GET_COMMAND = "$n = get-networks\nforeach ($i in $n) {  if ($i.networkid -eq '12345') {    $i  }}";
     private static final String GET_RETURN = "networkid: 12345 \nname: sedna\ndatacenterid: 54321";
    private static final String UPDATE_COMMAND = "foreach ($i in $n) {  if ($i.networkid -eq '12345') {    $i.name = 'eris'    update-network -networkobject $i -datacenterid 54321  }}\n";
     private static final String UPDATE_RETURN = "networkid: 12345 \nname: eris\ndatacenterid: 54321";
 
     protected PowerShellNetworkResource getResource() {
         return new PowerShellNetworkResource("12345", executor);
     }
 
     @Test
     public void testGet() throws Exception {
         verifyNetwork(
             resource.get(setUpNetworkExpectations(GET_COMMAND, GET_RETURN, "sedna")),
             "sedna");
     }
 
     @Test
     public void testGoodUpdate() throws Exception {
         verifyNetwork(
             resource.update(createMock(HttpHeaders.class),
                             setUpNetworkExpectations(UPDATE_COMMAND, UPDATE_RETURN, "eris"),
                             getNetwork("eris")),
             "eris");
     }
 
     @Test
     public void testBadUpdate() throws Exception {
         try {
             UriInfo uriInfo = createMock(UriInfo.class);
             resource.update(setUpHeadersExpectation(),
                             uriInfo,
                             getNetwork("98765", "eris"));
             fail("expected WebApplicationException on bad update");
         } catch (WebApplicationException wae) {
             verifyUpdateException(wae);
         }
     }
 
     private UriInfo setUpNetworkExpectations(String command, String ret, String name) throws Exception {
         mockStatic(PowerShellCmd.class);
         expect(PowerShellCmd.runCommand(command)).andReturn(ret);
         UriInfo uriInfo = createMock(UriInfo.class);
         UriBuilder uriBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
         expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/networks/12345")).anyTimes();
         UriBuilder baseBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getBaseUriBuilder()).andReturn(baseBuilder);
         expect(baseBuilder.clone()).andReturn(baseBuilder).anyTimes();
         expect(baseBuilder.path(isA(String.class))).andReturn(baseBuilder).anyTimes();
         expect(baseBuilder.build()).andReturn(new URI(URI_ROOT + "/foo")).anyTimes();
         replayAll();
 
         return uriInfo;
     }
 
     private HttpHeaders setUpHeadersExpectation() {
         HttpHeaders headers = createMock(HttpHeaders.class);
         List<MediaType> mediaTypes = new ArrayList<MediaType>();
         mediaTypes.add(MediaType.APPLICATION_XML_TYPE);
         expect(headers.getAcceptableMediaTypes()).andReturn(mediaTypes).anyTimes();
         replayAll();
         return headers;
     }
 
     private Network getNetwork(String name) {
         return getNetwork("12345", name);
     }
 
     private Network getNetwork(String id, String name) {
         Network network = new Network();
         network.setId(id);
         network.setName(name);
         DataCenter dataCenter = new DataCenter();
         dataCenter.setId("54321");
         network.setDataCenter(dataCenter);
         return network;
     }
 
     private void verifyNetwork(Network network, String name) {
         assertNotNull(network);
         assertEquals(network.getId(), "12345");
         assertEquals(network.getName(), name);
         assertNotNull(network.getDataCenter());
         assertEquals(network.getDataCenter().getId(), "54321");
     }
 
     private void verifyUpdateException(WebApplicationException wae) {
         assertEquals(409, wae.getResponse().getStatus());
         Fault fault = (Fault)wae.getResponse().getEntity();
         assertNotNull(fault);
         assertEquals("Broken immutability constraint", fault.getReason());
         assertEquals("Attempt to set immutable field: id", fault.getDetail());
     }
 }
