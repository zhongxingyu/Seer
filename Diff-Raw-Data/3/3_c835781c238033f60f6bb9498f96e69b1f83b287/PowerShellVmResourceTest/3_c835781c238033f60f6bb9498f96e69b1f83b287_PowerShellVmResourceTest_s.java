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
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Status;
 import com.redhat.rhevm.api.model.VM;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellVmResourceTest extends BasePowerShellResourceTest {
 
     private static final String GET_RETURN = "vmid: 12345 \n name: sedna";
     private static final String ACTION_RETURN = "replace with realistic powershell return";
     private static final String UPDATE_COMMAND = "$v = get-vm 12345\n$v.name = \"eris\"\nupdate-vm -vmobject $v";
     private static final String UPDATE_RETURN = "vmid: 12345 \n name: eris";
 
     private PowerShellVmResource resource;
 
     @Before
     public void setUp() {
         resource = new PowerShellVmResource("12345", executor);
     }
 
     @Test
     public void testGet() throws Exception {
         verifyVM(
             resource.get(setUpVmExpectations("get-vm 12345", GET_RETURN, "sedna")),
             "sedna");
     }
 
     @Test
     public void testGoodUpdate() throws Exception {
         verifyVM(
             resource.update(createMock(HttpHeaders.class),
                             setUpVmExpectations(UPDATE_COMMAND, UPDATE_RETURN, "eris"),
                             getVM("eris")),
             "eris");
     }
 
     @Test
     public void testBadUpdate() throws Exception {
         try {
             UriInfo uriInfo = createMock(UriInfo.class);
             resource.update(setUpHeadersExpectation(),
                             uriInfo,
                             getVM("98765", "eris"));
             fail("expected WebApplicationException on bad update");
         } catch (WebApplicationException wae) {
             verifyUpdateException(wae);
         }
     }
 
     @Test
     public void testStart() throws Exception {
         verifyActionResponse(
             resource.start(setUpActionExpectation("start", "start-vm"), getAction()),
             false);
     }
 
     @Test
     public void testStop() throws Exception {
         verifyActionResponse(
             resource.stop(setUpActionExpectation("stop", "stop-vm"), getAction()),
             false);
     }
 
     @Test
     public void testShutdown() throws Exception {
         verifyActionResponse(
             resource.shutdown(setUpActionExpectation("shutdown", "shutdown-vm"), getAction()),
             false);
     }
 
     @Test
     public void testSuspend() throws Exception {
         verifyActionResponse(
             resource.suspend(setUpActionExpectation("suspend", "suspend-vm"), getAction()),
             false);
     }
 
     @Test
     public void testRestore() throws Exception {
         verifyActionResponse(
             resource.restore(setUpActionExpectation("restore", "restore-vm"), getAction()),
             false);
     }
 
     @Test
     public void testStartAsync() throws Exception {
         verifyActionResponse(
             resource.start(setUpActionExpectation("start", "start-vm"), getAction(true)),
             true);
     }
 
     @Test
     public void testStopAsync() throws Exception {
         verifyActionResponse(
             resource.stop(setUpActionExpectation("stop", "stop-vm"), getAction(true)),
             true);
     }
 
     @Test
     public void testShutdownAsync() throws Exception {
         verifyActionResponse(
             resource.shutdown(setUpActionExpectation("shutdown", "shutdown-vm"), getAction(true)),
             true);
     }
 
     @Test
     public void testSuspendAsync() throws Exception {
         verifyActionResponse(
             resource.suspend(setUpActionExpectation("suspend", "suspend-vm"), getAction(true)),
             true);
     }
 
     @Test
     public void testRestoreAsync() throws Exception {
         verifyActionResponse(
             resource.restore(setUpActionExpectation("restore", "restore-vm"), getAction(true)),
             true);
     }
 
     private UriInfo setUpVmExpectations(String command, String ret, String name) throws Exception {
         mockStatic(PowerShellCmd.class);
         expect(PowerShellCmd.runCommand(command)).andReturn(ret);
         UriInfo uriInfo = createMock(UriInfo.class);
         UriBuilder uriBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
         expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/vms/12345")).anyTimes();
         replayAll();
 
         return uriInfo;
     }
 
     private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
         return setUpActionExpectation("/vms/12345/", verb, command + " -vmid 12345", ACTION_RETURN);
     }
 
     private HttpHeaders setUpHeadersExpectation() {
         HttpHeaders headers = createMock(HttpHeaders.class);
         List<MediaType> mediaTypes = new ArrayList<MediaType>();
         mediaTypes.add(MediaType.APPLICATION_XML_TYPE);
         expect(headers.getAcceptableMediaTypes()).andReturn(mediaTypes).anyTimes();
         replayAll();
         return headers;
     }
 
     private VM getVM(String name) {
         return getVM("12345", name);
     }
 
     private VM getVM(String id, String name) {
         VM vm = new VM();
         vm.setId(id);
         vm.setName(name);
         return vm;
     }
 
     private void verifyVM(VM vm, String name) {
         assertNotNull(vm);
         assertEquals(vm.getId(), "12345");
         assertEquals(vm.getName(), name);
     }
 
     private void verifyActionResponse(Response r, boolean async) throws Exception {
         verifyActionResponse(r, "/vms/12345/", async);
     }
 
     private void verifyUpdateException(WebApplicationException wae) {
         assertEquals(409, wae.getResponse().getStatus());
         Fault fault = (Fault)wae.getResponse().getEntity();
         assertNotNull(fault);
         assertEquals("Broken immutability constraint", fault.getReason());
         assertEquals("Attempt to set immutable field: id", fault.getDetail());
     }
 }
