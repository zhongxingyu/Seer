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
 import java.text.MessageFormat;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.UriBuilder;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.Host;
 import com.redhat.rhevm.api.model.HostNIC;
 import com.redhat.rhevm.api.model.HostNics;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.Slaves;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellException;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.Test;
 
 import org.junit.runner.RunWith;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( { PowerShellCmd.class })
 public class PowerShellHostNicsResourceTest
     extends AbstractPowerShellResourceTest<Host, PowerShellHostResource> {
 
     private static final String HOST_NAME = "1234";
 
     private static final String[] NICS = { "eth0", "eth1" };
     private static final String BOND_NAME = "bond0";
     private static final String[] NETS = { "net0", "net1" };
     private static final String[] MACS = { "00:31:20:5F:F9:DD", "00:31:20:5F:F9:EE" };
     private static final String[] IPS = { "192.168.1.7", "192.168.2.10" };
     private static final String[] SUBNETS = { "255.255.255.0", "255.255.255.0" };
 
     private static final String GET_NIC_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $n; break } }";
     private static final String GET_NICS_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; $h.getnetworkadapters()";
 
     public static final String LOOKUP_NETWORK_ID_COMMAND = "$n = get-networks; foreach ($i in $n) '{' if ($i.name -eq \"{0}\") '{' $i; break '} }'";
     public static final String LOOKUP_SLAVE_ID_COMMAND = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in $h.getnetworkadapters()) { if ($n.name -eq \"" + NICS[0] + "\") { $n } if ($n.name -eq \"" + NICS[1] + "\") { $n } }";
 
     private static final String EMPTY_BOND_INTERFACES_XML = "<Property Name=\"BondInterfaces\" Type=\"System.String[]\" />";
     private static final String BOND_INTERFACES_XML = "<Property Name=\"BondInterfaces\" Type=\"System.String[]\">\n      <Property Type=\"System.String\">" + NICS[0] + "</Property>\n      <Property Type=\"System.String\">" + NICS[1] + "</Property>\n    </Property>";
 
     public static final String[][] extraArgs = new String[][] {
         { NETS[0], MACS[0], IPS[0], SUBNETS[0], "False", EMPTY_BOND_INTERFACES_XML },
         { NETS[1], MACS[1], IPS[1], SUBNETS[1], "False", EMPTY_BOND_INTERFACES_XML  },
     };
 
     private static final String[] bondArgs = new String[] {
         NETS[0], MACS[0], IPS[0], SUBNETS[0], "True", BOND_INTERFACES_XML
     };
 
     private static final String DATA_CENTER_ID = "999";
 
     private static final String[] networkArgs = new String[] { DATA_CENTER_ID };
 
     private static final String ATTACH_NETWORK_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.networkid -eq \"" + asId(NETS[0]) + "\") { $net = $n; break } } foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $nic = $n; break } } attach-logicalnetworktonetworkadapter -hostobject $h -network $net -networkadapter $nic";
     private static final String ATTACH_NETWORK_BY_NAME_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.name -eq \"" + NETS[0] + "\") { $net = $n; break } } foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $nic = $n; break } } attach-logicalnetworktonetworkadapter -hostobject $h -network $net -networkadapter $nic";
     private static final String DETACH_NETWORK_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.networkid -eq \"" + asId(NETS[0]) + "\") { $net = $n; break } } foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $nic = $n; break } } detach-logicalnetworkfromnetworkadapter -hostobject $h -network $net -networkadapter $nic";
     private static final String ADD_BOND_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.networkid -eq \"" + asId(NETS[0]) + "\") { $net = $n; break } } $nics = @(); foreach ($nic in $h.getnetworkadapters()) { if ($nic.id -eq \"" + asId(NICS[0]) + "\") { $nics += $nic } if ($nic.id -eq \"" + asId(NICS[1]) + "\") { $nics += $nic } } $h = add-bond -bondname \"" + BOND_NAME + "\" -hostobject $h -network $net -networkadapters $nics; foreach ($nic in $h.getnetworkadapters()) { if ($nic.name -eq \"" + BOND_NAME + "\") { $nic; break } }";
     private static final String REMOVE_BOND_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($nic in $h.getnetworkadapters()) { if ($nic.id -eq \"" + asId(BOND_NAME) + "\") { $bond = $nic; break } } remove-bond -hostobject $h -bondobject $bond";
 
     private static final String NIC_URI = "hosts/" + asId(HOST_NAME) + "/nics/" + asId(NICS[0]);
     private static final String ACTION_RETURN = "replace with realistic powershell return";
     private static final String FAILURE = "replace with realistic powershell failure";
     private static final String REASON = "Powershell command \"" + ATTACH_NETWORK_CMD + "\" failed with " + FAILURE;
     private static final String DETAIL = "at com.redhat.rhevm.api.powershell.util.PowerShellCmd.runCommand(";
 
     protected PowerShellHostResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser) {
         return new PowerShellHostResource(asId(HOST_NAME), executor, poolMap, parser);
     }
 
     protected String formatNics(String[] names, String[][] args) {
         String[] descriptions = new String[names.length];
         return formatXmlReturn("hostnic", names, descriptions, args);
     }
 
     protected String formatNic(String name, String[] args) {
         return formatNics(asArray(name), asArray(args));
     }
 
     protected String formatNetwork(String name) {
         return formatXmlReturn("network",
                                new String[] { name },
                                new String[] { "" },
                                networkArgs);
     }
 
     protected String formatLookupNetworkCmd(int i) {
         return MessageFormat.format(LOOKUP_NETWORK_ID_COMMAND, NETS[i]);
     }
 
     @Test
     public void testGet() {
         PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
         PowerShellHostNicResource resource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent);
 
         String[] commands = { GET_NIC_CMD,
                               formatLookupNetworkCmd(0) };
         String[] returns = { formatNic(NICS[0], extraArgs[0]),
                              formatNetwork(NETS[0]) };
 
         setUpCmdExpectations(commands, returns);
         replayAll();
 
         verifyHostNic(resource.get(), 0);
     }
 
     @Test
     public void testHostNicsList() {
         PowerShellHostNicsResource resource = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
 
         String[] commands = { GET_NICS_CMD,
                               formatLookupNetworkCmd(0),
                               formatLookupNetworkCmd(1) };
         String[] returns = { formatNics(NICS, extraArgs),
                              formatNetwork(NETS[0]),
                              formatNetwork(NETS[1]) };
 
         setUpCmdExpectations(commands, returns);
         replayAll();
 
         verifyHostNics(resource.list());
     }
 
     @Test
     public void testHostNicAdd() throws Exception {
         PowerShellHostNicsResource resource = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
 
         String[] commands = { ADD_BOND_CMD,
                               LOOKUP_SLAVE_ID_COMMAND,
                               formatLookupNetworkCmd(0) };
         String[] returns = { formatNic(BOND_NAME, bondArgs),
                              formatNics(NICS, extraArgs),
                              formatNetwork(NETS[0]) };
 
         setUpCmdExpectations(commands, returns);
         UriInfo uriInfo = setUpUriInfoExpections(asId(BOND_NAME));
         replayAll();
 
         HostNIC bond = new HostNIC();
         bond.setName(BOND_NAME);
         bond.setNetwork(new Network());
         bond.getNetwork().setId(asId(NETS[0]));
         bond.setSlaves(new Slaves());
 
         HostNIC slave = new HostNIC();
         slave.setId(asId(NICS[0]));
         bond.getSlaves().getSlaves().add(slave);
 
         slave = new HostNIC();
         slave.setId(asId(NICS[1]));
         bond.getSlaves().getSlaves().add(slave);
 
         verifyBond((HostNIC)resource.add(uriInfo, bond).getEntity());
     }
 
     @Test
     public void testHostNicRemove() {
         PowerShellHostNicsResource resource = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
 
         setUpCmdExpectations(REMOVE_BOND_CMD, "");
         replayAll();
 
         resource.remove(asId(BOND_NAME));
     }
 
     @Test
     public void testAttach() throws Exception {
         PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
         PowerShellHostNicResource resource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent);
 
         Action action = getAction();
         action.setNetwork(new Network());
         action.getNetwork().setId(asId(NETS[0]));
 
         verifyActionResponse(
             resource.attach(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_CMD, ACTION_RETURN),
                             action),
             NIC_URI,
             false);
     }
 
     @Test
     public void testAttachByName() throws Exception {
         PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
         PowerShellHostNicResource resource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent);
 
         Action action = getAction();
         action.setNetwork(new Network());
         action.getNetwork().setName(NETS[0]);
 
         verifyActionResponse(
             resource.attach(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_BY_NAME_CMD, ACTION_RETURN),
                             action),
             NIC_URI,
             false);
     }
 
     @Test
     public void testDetach() throws Exception {
         PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
         PowerShellHostNicResource resource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent);
 
         Action action = getAction();
         action.setNetwork(new Network());
         action.getNetwork().setId(asId(NETS[0]));
 
         verifyActionResponse(
             resource.detach(setUpActionExpectation(NIC_URI, "detach", DETACH_NETWORK_CMD, ACTION_RETURN),
                             action),
             NIC_URI,
             false);
     }
 
     @Test
     public void testAttachAsync() throws Exception {
         PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
         PowerShellHostNicResource resource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent);
 
         Action action = getAction(true);
         action.setNetwork(new Network());
         action.getNetwork().setId(asId(NETS[0]));
 
         verifyActionResponse(
             resource.attach(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_CMD, ACTION_RETURN),
                             action),
             NIC_URI,
             true);
     }
 
     @Test
     public void testAttachAsyncFailed() throws Exception {
         PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser);
         PowerShellHostNicResource resource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent);
 
         Action action = getAction(true);
         action.setNetwork(new Network());
         action.getNetwork().setId(asId(NETS[0]));
 
         verifyActionResponse(
             resource.attach(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_CMD, new PowerShellException(FAILURE)),
                             action),
             NIC_URI,
             true,
             REASON,
             DETAIL);
     }
 
     private UriInfo setUpUriInfoExpections(String id) throws Exception {
         UriInfo uriInfo = createMock(UriInfo.class);
         UriBuilder uriBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
         expect(uriBuilder.path(id)).andReturn(uriBuilder);
         expect(uriBuilder.build()).andReturn(new URI("hosts/" + asId(HOST_NAME) + "/nics/" + id)).anyTimes();
         return uriInfo;
     }
 
     private void setUpCmdExpectations(String command, String ret) {
         setUpCmdExpectations(asArray(command), asArray(ret));
     }
 
     private void setUpCmdExpectations(String commands[], String returns[]) {
         mockStatic(PowerShellCmd.class);
         for (int i = 0 ; i < Math.min(commands.length, returns.length) ; i++) {
             if (commands[i] != null) {
                 expect(PowerShellCmd.runCommand(setUpPoolExpectations(), commands[i])).andReturn(returns[i]);
             }
         }
     }
 
     protected PowerShellPool setUpPoolExpectations() {
         PowerShellPool pool = createMock(PowerShellPool.class);
         expect(poolMap.get()).andReturn(pool);
         return pool;
     }
 
     private void verifyHostNicDetails(HostNIC nic, int i) {
         assertNotNull(nic.getHost());
         assertEquals(asId(HOST_NAME), nic.getHost().getId());
         assertNotNull(nic.getNetwork());
         assertEquals(asId(NETS[i]), nic.getNetwork().getId());
         assertNotNull(nic.getMac());
         assertEquals(MACS[i], nic.getMac().getAddress());
         assertNotNull(nic.getIp());
         assertEquals(IPS[i], nic.getIp().getAddress());
         assertEquals(SUBNETS[i], nic.getIp().getNetmask());
     }
 
     private void verifyBond(HostNIC nic) {
         assertNotNull(nic);
         assertEquals(asId(BOND_NAME), nic.getId());
         verifyHostNicDetails(nic, 0);
         assertNotNull(nic.getSlaves());
         assertEquals(2, nic.getSlaves().getSlaves().size());
         assertEquals(asId(NICS[0]), nic.getSlaves().getSlaves().get(0).getId());
         assertEquals(asId(NICS[1]), nic.getSlaves().getSlaves().get(1).getId());
     }
 
     private void verifyHostNic(HostNIC nic, int i) {
         assertNotNull(nic);
         assertEquals(asId(NICS[i]), nic.getId());
         verifyHostNicDetails(nic, i);
     }
 
     private void verifyHostNics(HostNics nics) {
         assertNotNull(nics);
         assertEquals(NICS.length, nics.getHostNics().size());
         for (int i = 0; i < NICS.length; i++) {
             verifyHostNic(nics.getHostNics().get(i), i);
         }
     }
 }
