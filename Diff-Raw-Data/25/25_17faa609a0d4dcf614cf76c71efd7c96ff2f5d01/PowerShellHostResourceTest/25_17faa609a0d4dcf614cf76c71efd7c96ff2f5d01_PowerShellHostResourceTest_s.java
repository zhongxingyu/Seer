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
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.Fault;
 import com.redhat.rhevm.api.model.Host;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.isA;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellHostResourceTest extends AbstractPowerShellResourceTest<Host, PowerShellHostResource> {
 
     private static final String HOST_NAME = "sedna";
     private static final String HOST_ID = Integer.toString(HOST_NAME.hashCode());
 
     private static final String GET_COMMAND = "rhevmpssnapin\\get-host \"" + HOST_ID + "\"";
     private static final String ACTION_RETURN = "replace with realistic powershell return";
     private static final String UPDATE_COMMAND = "$h = rhevmpssnapin\\get-host \"" + HOST_ID + "\";$h.name = \"eris\";update-host -hostobject $h";
     private static final String INSTALL_PASSWORD = "boldlygoingnowhere";
     private static final String INSTALL_COMMAND = "$h = rhevmpssnapin\\get-host \"" + HOST_ID + "\";update-host -hostobject $h -install -rootpassword \"" + INSTALL_PASSWORD + "\"";
 
     protected PowerShellHostResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser) {
         return new PowerShellHostResource(HOST_ID, executor, poolMap, parser);
     }
 
     protected String formatHost(String name) {
         String ret = formatXmlReturn("host",
                                new String[] { name },
                                new String[] { "" },
                                PowerShellHostsResourceTest.extraArgs);
         System.out.println(ret);
         return ret;
     }
 
     @Test
     public void testGet() throws Exception {
         verifyHost(
             resource.get(setUpHostExpectations(GET_COMMAND,
                                                formatHost(HOST_NAME),
                                                HOST_NAME)),
             HOST_NAME);
     }
 
     @Test
     public void testGoodUpdate() throws Exception {
         verifyHost(
             resource.update(setUpHostExpectations(UPDATE_COMMAND,
                                                   formatHost("eris"),
                                                   "eris"),
                             getHost(HOST_ID, "eris")),
             "eris");
     }
 
     @Test
     public void testBadUpdate() throws Exception {
         try {
             UriInfo uriInfo = createMock(UriInfo.class);
             replayAll();
             resource.update(uriInfo,
                             getHost("98765", "eris"));
             fail("expected WebApplicationException on bad update");
         } catch (WebApplicationException wae) {
             verifyUpdateException(wae);
         }
     }
 
     @Test
     public void testApprove() throws Exception {
         verifyActionResponse(
             resource.approve(setUpActionExpectation("approve", "approve-host"), getAction()),
             false);
     }
 
     @Test
     public void testInstall() throws Exception {
         Action action = getAction();
         action.setRootPassword(INSTALL_PASSWORD);
         verifyActionResponse(
             resource.install(setUpActionExpectation("hosts/" + HOST_ID, "install", INSTALL_COMMAND, ACTION_RETURN), action),
             false);
     }
 
     @Test
     public void testActivate() throws Exception {
         verifyActionResponse(
             resource.activate(setUpActionExpectation("activate", "resume-host"), getAction()),
             false);
     }
 
     @Test
     public void testDeactivate() throws Exception {
         verifyActionResponse(
             resource.deactivate(setUpActionExpectation("deactivate", "suspend-host"), getAction()),
             false);
     }
 
     @Test
     public void testApproveAsync() throws Exception {
         verifyActionResponse(
             resource.approve(setUpActionExpectation("approve", "approve-host"), getAction(true)),
             true);
     }
 
     @Test
     public void testInstallAsync() throws Exception {
         Action action = getAction(true);
         action.setRootPassword(INSTALL_PASSWORD);
         verifyActionResponse(
             resource.install(setUpActionExpectation("hosts/" + HOST_ID, "install", INSTALL_COMMAND, ACTION_RETURN), action),
             true);
     }
 
     @Test
     public void testActivateAsync() throws Exception {
         verifyActionResponse(
             resource.activate(setUpActionExpectation("activate", "resume-host"), getAction(true)),
             true);
     }
 
     @Test
     public void testDeactivateAsync() throws Exception {
         verifyActionResponse(
             resource.deactivate(setUpActionExpectation("deactivate", "suspend-host"), getAction(true)),
             true);
     }
 
     private UriInfo setUpHostExpectations(String command, String ret, String name) throws Exception {
         mockStatic(PowerShellCmd.class);
         expect(PowerShellCmd.runCommand(setUpShellExpectations(), command)).andReturn(ret);
        String href = URI_ROOT + "/hosts/" + Integer.toString(name.hashCode());
         UriInfo uriInfo = createMock(UriInfo.class);
         UriBuilder uriBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
         expect(uriBuilder.build()).andReturn(new URI(href)).anyTimes();
         UriBuilder actionUriBuilder = createMock(UriBuilder.class);
         expect(uriBuilder.clone()).andReturn(actionUriBuilder).anyTimes();
         expect(actionUriBuilder.path(isA(String.class))).andReturn(uriBuilder).anyTimes();
         expect(actionUriBuilder.build()).andReturn(new URI(href + "/action")).anyTimes();
         replayAll();
 
         return uriInfo;
     }
 
     private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
         return setUpActionExpectation("/hosts/"+ HOST_ID + "/",
                                       verb,
                                       command + " -hostid \"" + HOST_ID + "\"",
                                       ACTION_RETURN);
     }
 
     private Host getHost(String name) {
         return getHost(Integer.toString(name.hashCode()), name);
     }
 
     private Host getHost(String id, String name) {
         Host host = new Host();
         host.setId(id);
         host.setName(name);
         return host;
     }
 
     private void verifyHost(Host host, String name) {
         assertNotNull(host);
         assertEquals(Integer.toString(name.hashCode()), host.getId());
         assertEquals(name, host.getName());
     }
 
     private void verifyActionResponse(Response r, boolean async) throws Exception {
         verifyActionResponse(r, "hosts/" + HOST_ID, async);
     }
 
     private void verifyUpdateException(WebApplicationException wae) {
         assertEquals(409, wae.getResponse().getStatus());
         Fault fault = (Fault)wae.getResponse().getEntity();
         assertNotNull(fault);
         assertEquals("Broken immutability constraint", fault.getReason());
         assertEquals("Attempt to set immutable field: id", fault.getDetail());
     }
 }
