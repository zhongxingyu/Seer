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
 import java.text.MessageFormat;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.Disk;
 import com.redhat.rhevm.api.model.Fault;
 import com.redhat.rhevm.api.model.Interface;
import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.VM;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.isA;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellVmResourceTest extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {
 
     private static final String VM_ID = "12345";
     private static final String VM_NAME = "sedna";
     private static final String NEW_NAME = "eris";
     private static final String CLUSTER_ID = "3321";
     private static final String BAD_ID = "98765";
 
     private static final String GET_RETURN = "vmid: " + VM_ID + "\nname: " + VM_NAME + "\nhostclusterid: " + CLUSTER_ID + "\n";
     private static final String ACTION_RETURN = "replace with realistic powershell return";
     private static final String UPDATE_COMMAND = "$v = get-vm " + VM_ID + "\n$v.name = \"" + NEW_NAME + "\"\nupdate-vm -vmobject $v";
     private static final String UPDATE_RETURN = "vmid: " + VM_ID + "\n name: " + NEW_NAME + "\nhostclusterid: " + CLUSTER_ID + "\n";
 
     private static final String GET_DISKS_COMMAND = "$v = get-vm {0}\n$v.GetDiskImages()\n";
     private static final String GET_DISKS_RETURN = PowerShellVmsResourceTest.GET_DISKS_RETURN;
 
     private static final String GET_INTERFACES_COMMAND = "$v = get-vm {0}\n$v.GetNetworkAdapters()\n";
     private static final String GET_INTERFACES_RETURN = PowerShellVmsResourceTest.GET_INTERFACES_RETURN;
 
     private static final String LOOKUP_NETWORK_ID_COMMAND = PowerShellVmsResourceTest.LOOKUP_NETWORK_ID_COMMAND;
     private static final String LOOKUP_NETWORK_ID_RETURN = PowerShellVmsResourceTest.LOOKUP_NETWORK_ID_RETURN;
 
     private static final long NEW_DISK_SIZE = 10;
     private static final String ADD_DISK_COMMAND = "$d = new-disk -disksize {0}\n$v = get-vm {1}\nadd-disk -diskobject $d -vmobject $v";
     private static final String REMOVE_DISK_COMMAND = "remove-disk -vmid {0} -diskids 0";
 
     private static final String NEW_INTERFACE_NAME = "eth11";
    private static final String NEW_INTERFACE_NETWORK = "rhevm";
    private static final String ADD_INTERFACE_COMMAND = "$v = get-vm {0}\nadd-networkadapter -vmobject $v -interfacename {1} -networkname {2}";
     private static final String REMOVE_INTERFACE_COMMAND = "$v = get-vm {0}\nforeach ($i in $v.GetNetworkAdapters()) '{  if ($i.id -eq \"0\") {    $n = $i  }}'\nremove-interface -vmobject $v -networkadapterobject $n";
 
     protected PowerShellVmResource getResource() {
         return new PowerShellVmResource(VM_ID);
     }
 
     @Test
     public void testGet() throws Exception {
         String [] commands = { "get-vm " + VM_ID,
                                MessageFormat.format(GET_DISKS_COMMAND, VM_ID),
                                MessageFormat.format(GET_INTERFACES_COMMAND, VM_ID),
                                LOOKUP_NETWORK_ID_COMMAND };
         String [] returns = { GET_RETURN, GET_DISKS_RETURN, GET_INTERFACES_RETURN, LOOKUP_NETWORK_ID_RETURN };
 
         verifyVM(
             resource.get(setUpVmExpectations(commands, returns, VM_NAME)),
             VM_NAME);
     }
 
     @Test
     public void testGoodUpdate() throws Exception {
         String [] commands = { UPDATE_COMMAND,
                                MessageFormat.format(GET_DISKS_COMMAND, VM_ID),
                                MessageFormat.format(GET_INTERFACES_COMMAND, VM_ID) };
         String [] returns = { UPDATE_RETURN, GET_DISKS_RETURN, GET_INTERFACES_COMMAND };
 
         verifyVM(
             resource.update(createMock(HttpHeaders.class),
                             setUpVmExpectations(commands, returns, NEW_NAME),
                             getVM(NEW_NAME)),
             NEW_NAME);
     }
 
     @Test
     public void testBadUpdate() throws Exception {
         try {
             UriInfo uriInfo = createMock(UriInfo.class);
             resource.update(setUpHeadersExpectation(),
                             uriInfo,
                             getVM(BAD_ID, NEW_NAME));
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
 
     @Test
     public void testAddDisk() throws Exception {
         Disk disk = new Disk();
         disk.setSize(NEW_DISK_SIZE * 1024 * 1024 * 1024);
 
         Action action = getAction();
         action.setDisk(disk);
 
         String command = MessageFormat.format(ADD_DISK_COMMAND, NEW_DISK_SIZE, VM_ID);
 
         verifyActionResponse(
             resource.addDevice(setUpActionExpectation("adddevice", command, false), action),
             false);
     }
 
     @Test
     public void testRemoveDisk() throws Exception {
         Disk disk = new Disk();
         disk.setId("0");
 
         Action action = getAction();
         action.setDisk(disk);
 
         String command = MessageFormat.format(REMOVE_DISK_COMMAND, VM_ID);
 
         verifyActionResponse(
             resource.removeDevice(setUpActionExpectation("removedevice", command, false), action),
             false);
     }
 
     @Test
     public void testAddInterface() throws Exception {
         Interface iface = new Interface();
         iface.setName(NEW_INTERFACE_NAME);
 
        Network network = new Network();
        network.setName(NEW_INTERFACE_NETWORK);
        iface.setNetwork(network);

         Action action = getAction();
         action.setInterface(iface);
 
        String command = MessageFormat.format(ADD_INTERFACE_COMMAND, VM_ID, NEW_INTERFACE_NAME, NEW_INTERFACE_NETWORK);
 
         verifyActionResponse(
             resource.addDevice(setUpActionExpectation("adddevice", command, false), action),
             false);
     }
 
     @Test
     public void testRemoveInterface() throws Exception {
         Interface iface = new Interface();
         iface.setId("0");
 
         Action action = getAction();
         action.setInterface(iface);
 
         String command = MessageFormat.format(REMOVE_INTERFACE_COMMAND, VM_ID);
 
         verifyActionResponse(
             resource.removeDevice(setUpActionExpectation("removedevice", command, false), action),
             false);
     }
 
     private UriInfo setUpVmExpectations(String[] commands, String[] rets, String name) throws Exception {
         if (commands != null) {
             mockStatic(PowerShellCmd.class);
             for (int i = 0 ; i < Math.min(commands.length, rets.length) ; i++) {
                 if (commands[i] != null) {
                     expect(PowerShellCmd.runCommand(commands[i])).andReturn(rets[i]);
                 }
             }
         }
         UriInfo uriInfo = createMock(UriInfo.class);
         UriBuilder uriBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
         expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/vms/" + VM_ID)).anyTimes();
         UriBuilder baseBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getBaseUriBuilder()).andReturn(baseBuilder);
         expect(baseBuilder.clone()).andReturn(baseBuilder).anyTimes();
         expect(baseBuilder.path(isA(String.class))).andReturn(baseBuilder).anyTimes();
         expect(baseBuilder.build()).andReturn(new URI(URI_ROOT + "/foo")).anyTimes();
         replayAll();
 
         return uriInfo;
     }
 
     private UriInfo setUpActionExpectation(String verb, String command, boolean appendVmId) throws Exception {
         if (appendVmId) {
             command += " -vmid " + VM_ID;
         }
         return setUpActionExpectation("/vms/" + VM_ID + "/", verb, command, ACTION_RETURN);
     }
 
     private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
         return setUpActionExpectation(verb, command, true);
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
         return getVM(VM_ID, name);
     }
 
     private VM getVM(String id, String name) {
         VM vm = new VM();
         vm.setId(id);
         vm.setName(name);
         return vm;
     }
 
     private void verifyVM(VM vm, String name) {
         assertNotNull(vm);
         assertEquals(vm.getId(), VM_ID);
         assertEquals(vm.getName(), name);
     }
 
     private void verifyActionResponse(Response r, boolean async) throws Exception {
         verifyActionResponse(r, "/vms/" + VM_ID + "/", async);
     }
 
     private void verifyUpdateException(WebApplicationException wae) {
         assertEquals(409, wae.getResponse().getStatus());
         Fault fault = (Fault)wae.getResponse().getEntity();
         assertNotNull(fault);
         assertEquals("Broken immutability constraint", fault.getReason());
         assertEquals("Attempt to set immutable field: id", fault.getDetail());
     }
 
     protected static String[] asArray(String s) {
         return new String[] { s };
     }
 }
