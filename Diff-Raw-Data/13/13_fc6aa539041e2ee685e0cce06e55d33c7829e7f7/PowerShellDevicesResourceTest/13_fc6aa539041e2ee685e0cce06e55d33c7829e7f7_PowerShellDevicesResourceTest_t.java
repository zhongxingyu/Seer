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
 
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.UriBuilder;
 
 import com.redhat.rhevm.api.model.CdRom;
 import com.redhat.rhevm.api.model.CdRoms;
 import com.redhat.rhevm.api.model.Disk;
 import com.redhat.rhevm.api.model.Disks;
import com.redhat.rhevm.api.model.DiskType;
 import com.redhat.rhevm.api.model.Iso;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.NIC;
 import com.redhat.rhevm.api.model.Nics;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import org.junit.runner.RunWith;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 import static org.powermock.api.easymock.PowerMock.verifyAll;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( { PowerShellCmd.class })
 public class PowerShellDevicesResourceTest extends Assert {
 
     private static final String VM_ID = "1234";
     private static final String DISK_ID = "3456";
     private static final String NIC_ID = "5678";
 
     private static final String CDROM_ID = Integer.toString("cdrom".hashCode());
     private static final String ISO_NAME = "foo.iso";
 
     private static final String GET_CDROMS_CMD = "get-vm \"" + VM_ID + "\"";
     private static final String GET_CDROMS_RETURN = "vmid: " + VM_ID + "\nname: x\nhostclusterid: x\ntemplateid: x\nmemorysize: 1024\ndefaultbootsequence: CDN\nnumofsockets: 2\nnumofcpuspersocket: 4\npoolid: -1\ncdisopath: " + ISO_NAME + "\n";
 
     private static final String UPDATE_CDROM_CMD = "$v = get-vm \"{0}\"\n$v.cdisopath = \"{1}\"\nupdate-vm -vmobject $v";
 
     private static final long DISK_SIZE = 10;
     private static final long DISK_SIZE_BYTES = DISK_SIZE * 1024 * 1024 * 1024;
 
     private static final String GET_DISKS_CMD = "$v = get-vm \"" + VM_ID + "\"\n$v.GetDiskImages()\n";
     private static final String GET_DISKS_RETURN = "snapshotid: " + DISK_ID + "\nactualsizeinbytes: " + Long.toString(DISK_SIZE_BYTES) + "\ndisktype: system\nstatus: ok\ndiskinterface: ide\nvolumeformat: raw\nvolumetype: sparse\nboot: true\nwipeafterdelete: false\npropagateerrors: off\n";
 
    private static final String ADD_DISK_COMMAND = "$d = new-disk -disksize {0} -disktype System\n$v = get-vm \"{1}\"\nadd-disk -diskobject $d -vmobject $v";
     private static final String REMOVE_DISK_COMMAND = "remove-disk -vmid \"{0}\" -diskids \"{1}\"";
 
     private static final String GET_NICS_CMD = "$v = get-vm \"" + VM_ID + "\"\n$v.GetNetworkAdapters()\n";
     private static final String GET_NICS_RETURN = "id: " + NIC_ID + "\nname: eth1\nnetwork: net1\ntype: pv\nmacaddress: 00:1a:4a:16:84:02\naddress: 172.31.0.10\nsubnet: 255.255.255.0\ngateway: 172.31.0.1\n";
 
     public static final String LOOKUP_NETWORK_ID_COMMAND = "$n = get-networks\nforeach ($i in $n) {  if ($i.name -eq \"net1\") {    $i  }}";
     public static final String LOOKUP_NETWORK_ID_RETURN = "networkid: 666\nname: net1\ndatacenterid: 999";
 
     private static final String NIC_NAME = "eth11";
     private static final String NIC_NETWORK = "b4fb4d54-ca44-444c-ba26-d51f18c91998";
     private static final String ADD_NIC_COMMAND = "$v = get-vm \"{0}\"\nforeach ($i in get-networks) '{'  if ($i.networkid -eq \"{1}\") '{    $n = $i  }}'\nadd-networkadapter -vmobject $v -interfacename \"{2}\" -networkname $n.name";
     private static final String REMOVE_NIC_COMMAND = "$v = get-vm \"{0}\"\nforeach ($i in $v.GetNetworkAdapters()) '{'  if ($i.id -eq \"{1}\") '{    $n = $i  }}'\nremove-networkadapter -vmobject $v -networkadapterobject $n";
 
     private PowerShellPoolMap poolMap;
 
     @Before
     public void setUp() {
         poolMap = createMock(PowerShellPoolMap.class);
     }
 
     @After
     public void tearDown() {
         verifyAll();
     }
 
     @Test
     public void testCdRomGet() throws Exception {
         PowerShellCdRomsResource parent = new PowerShellCdRomsResource(VM_ID,
                                                                        poolMap,
                                                                        new PowerShellVmResource.CdRomQuery(VM_ID));
         PowerShellDeviceResource<CdRom, CdRoms> resource =
             new PowerShellDeviceResource<CdRom, CdRoms>(parent, CDROM_ID, poolMap);
 
         setUpCmdExpectations(GET_CDROMS_CMD, GET_CDROMS_RETURN);
         verifyCdRom(resource.get());
     }
 
     @Test
     public void testCdRomList() throws Exception {
         PowerShellCdRomsResource resource = new PowerShellCdRomsResource(VM_ID,
                                                                          poolMap,
                                                                          new PowerShellVmResource.CdRomQuery(VM_ID));
 
         setUpCmdExpectations(GET_CDROMS_CMD, GET_CDROMS_RETURN);
 
         verifyCdRoms(resource.list());
     }
 
     @Test
     public void testCdRomAdd() throws Exception {
         PowerShellCdRomsResource resource = new PowerShellCdRomsResource(VM_ID,
                                                                          poolMap,
                                                                          new PowerShellVmResource.CdRomQuery(VM_ID));
 
         CdRom cdrom = new CdRom();
         cdrom.setIso(new Iso());
         cdrom.getIso().setId(ISO_NAME);
 
         String command = MessageFormat.format(UPDATE_CDROM_CMD, VM_ID, ISO_NAME);
 
         UriInfo uriInfo = setUpCmdExpectations(command, "", "cdroms", CDROM_ID);
 
         verifyCdRom((CdRom)resource.add(uriInfo, cdrom).getEntity());
     }
 
     @Test
     public void testCdRomRemove() throws Exception {
         PowerShellCdRomsResource resource = new PowerShellCdRomsResource(VM_ID,
                                                                          poolMap,
                                                                          new PowerShellVmResource.CdRomQuery(VM_ID));
 
         String command = MessageFormat.format(UPDATE_CDROM_CMD, VM_ID, "");
 
         setUpCmdExpectations(command, "");
 
         resource.remove(CDROM_ID);
     }
 
     @Test
     public void testDiskGet() throws Exception {
         PowerShellDisksResource parent = new PowerShellDisksResource(VM_ID, poolMap, "get-vm");
         PowerShellDeviceResource<Disk, Disks> resource =
             new PowerShellDeviceResource<Disk, Disks>(parent, DISK_ID, poolMap);
 
         setUpCmdExpectations(GET_DISKS_CMD, GET_DISKS_RETURN);
         verifyDisk(resource.get());
     }
 
     @Test
     public void testDiskList() throws Exception {
         PowerShellDisksResource resource = new PowerShellDisksResource(VM_ID, poolMap, "get-vm");
 
         setUpCmdExpectations(GET_DISKS_CMD, GET_DISKS_RETURN);
 
         verifyDisks(resource.list());
     }
 
     @Test
     public void testDiskAdd() throws Exception {
         PowerShellDisksResource resource = new PowerShellDisksResource(VM_ID, poolMap, "get-vm");
 
         Disk disk = new Disk();
        disk.setType(DiskType.SYSTEM);
         disk.setSize(DISK_SIZE_BYTES);
 
         String command = MessageFormat.format(ADD_DISK_COMMAND, DISK_SIZE, VM_ID);
 
         UriInfo uriInfo = setUpCmdExpectations(command, GET_DISKS_RETURN, "disks", DISK_ID);
 
         verifyDisk((Disk)resource.add(uriInfo, disk).getEntity());
     }
 
     @Test
     public void testDiskRemove() throws Exception {
         PowerShellDisksResource resource = new PowerShellDisksResource(VM_ID, poolMap, "get-vm");
 
         String command = MessageFormat.format(REMOVE_DISK_COMMAND, VM_ID, DISK_ID);
 
         setUpCmdExpectations(command, "");
 
         resource.remove(DISK_ID);
     }
 
     @Test
     public void testNicGet() throws Exception {
         PowerShellNicsResource parent = new PowerShellNicsResource(VM_ID, poolMap, "get-vm");
         PowerShellDeviceResource<NIC, Nics> resource =
             new PowerShellDeviceResource<NIC, Nics>(parent, NIC_ID, poolMap);
 
         String [] commands = { GET_NICS_CMD, LOOKUP_NETWORK_ID_COMMAND };
         String [] returns = { GET_NICS_RETURN, LOOKUP_NETWORK_ID_RETURN };
 
         setUpCmdExpectations(commands, returns);
 
         verifyNic(resource.get());
     }
 
     @Test
     public void testNicList() throws Exception {
         PowerShellNicsResource resource = new PowerShellNicsResource(VM_ID, poolMap, "get-vm");
 
         String [] commands = { GET_NICS_CMD, LOOKUP_NETWORK_ID_COMMAND };
         String [] returns = { GET_NICS_RETURN, LOOKUP_NETWORK_ID_RETURN };
 
         setUpCmdExpectations(commands, returns);
 
         verifyNics(resource.list());
     }
 
     @Test
     public void testNicAdd() throws Exception {
         PowerShellNicsResource resource = new PowerShellNicsResource(VM_ID, poolMap, "get-vm");
 
         NIC nic = new NIC();
         nic.setName(NIC_NAME);
         nic.setNetwork(new Network());
         nic.getNetwork().setId(NIC_NETWORK);
 
         String [] commands = {
             MessageFormat.format(ADD_NIC_COMMAND, VM_ID, NIC_NETWORK, NIC_NAME),
             LOOKUP_NETWORK_ID_COMMAND
         };
 
         String [] returns = { GET_NICS_RETURN, LOOKUP_NETWORK_ID_RETURN };
 
         UriInfo uriInfo = setUpCmdExpectations(commands, returns, "nics", NIC_ID);
 
         verifyNic((NIC)resource.add(uriInfo, nic).getEntity());
     }
 
     @Test
     public void testNicRemove() throws Exception {
         PowerShellNicsResource resource = new PowerShellNicsResource(VM_ID, poolMap, "get-vm");
 
         String command = MessageFormat.format(REMOVE_NIC_COMMAND, VM_ID, NIC_ID);
 
         setUpCmdExpectations(command, "");
 
         resource.remove(NIC_ID);
     }
 
     private UriInfo setUpCmdExpectations(String[] commands, String[] returns, String collectionType, String newId) throws Exception {
         mockStatic(PowerShellCmd.class);
         for (int i = 0 ; i < Math.min(commands.length, returns.length) ; i++) {
             if (commands[i] != null) {
                 expect(PowerShellCmd.runCommand(setUpShellExpectations(), commands[i])).andReturn(returns[i]);
             }
         }
 
         UriInfo uriInfo = null;
         if (collectionType != null && newId != null) {
             uriInfo = createMock(UriInfo.class);
             UriBuilder uriBuilder = createMock(UriBuilder.class);
             expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
             expect(uriBuilder.path(newId)).andReturn(uriBuilder);
             expect(uriBuilder.build()).andReturn(new URI("vms/" + VM_ID + "/" + collectionType + "/" + newId)).anyTimes();
         }
 
         replayAll();
 
         return uriInfo;
     }
 
     protected PowerShellCmd setUpShellExpectations() {
         PowerShellPool pool = createMock(PowerShellPool.class);
         PowerShellCmd cmd = createMock(PowerShellCmd.class);
         expect(pool.get()).andReturn(cmd);
         expect(poolMap.get()).andReturn(pool);
         return cmd;
     }
 
     private void setUpCmdExpectations(String[] commands, String[] returns) throws Exception {
         setUpCmdExpectations(commands, returns, null, null);
     }
 
     private UriInfo setUpCmdExpectations(String command, String ret, String collectionType, String newId) throws Exception {
         return setUpCmdExpectations(asArray(command), asArray(ret), collectionType, newId);
     }
 
     private void setUpCmdExpectations(String command, String ret) throws Exception {
         setUpCmdExpectations(command, ret, null, null);
     }
 
     private void verifyCdRom(CdRom cdrom) {
         assertNotNull(cdrom);
         assertEquals(cdrom.getId(), CDROM_ID);
         assertNotNull(cdrom.getVm());
         assertEquals(cdrom.getVm().getId(), VM_ID);
     }
 
     private void verifyCdRoms(CdRoms cdroms) {
         assertNotNull(cdroms);
         assertEquals(cdroms.getCdRoms().size(), 1);
         verifyCdRom(cdroms.getCdRoms().get(0));
     }
 
     private void verifyDisk(Disk disk) {
         assertNotNull(disk);
         assertEquals(disk.getId(), DISK_ID);
         assertNotNull(disk.getVm());
         assertEquals(disk.getVm().getId(), VM_ID);
     }
 
     private void verifyDisks(Disks disks) {
         assertNotNull(disks);
         assertEquals(disks.getDisks().size(), 1);
         verifyDisk(disks.getDisks().get(0));
     }
 
     private void verifyNic(NIC nic) {
         assertNotNull(nic);
         assertEquals(nic.getId(), NIC_ID);
         assertNotNull(nic.getVm());
         assertEquals(nic.getVm().getId(), VM_ID);
     }
 
     private void verifyNics(Nics nics) {
         assertNotNull(nics);
         assertEquals(nics.getNics().size(), 1);
         verifyNic(nics.getNics().get(0));
     }
 
     protected static String[] asArray(String s) {
         return new String[] { s };
     }
 }
