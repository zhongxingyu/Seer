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
 
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.model.CdRom;
 import com.redhat.rhevm.api.model.CdRoms;
 import com.redhat.rhevm.api.model.Floppy;
 import com.redhat.rhevm.api.model.Floppies;
 import com.redhat.rhevm.api.model.Disk;
 import com.redhat.rhevm.api.model.Disks;
 import com.redhat.rhevm.api.model.DiskType;
 import com.redhat.rhevm.api.model.File;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.NIC;
 import com.redhat.rhevm.api.model.Nics;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
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
 public class PowerShellDevicesResourceTest
     extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {
 
     private static final String VM_NAME = "1234";
     private static final String VM_ID = Integer.toString(VM_NAME.hashCode());
     private static final String DISK_NAME = "3456";
     private static final String DISK_ID = Integer.toString(DISK_NAME.hashCode());
     private static final String NIC_NAME = "eth11";
     private static final String NIC_ID = Integer.toString(NIC_NAME.hashCode());
     private static final String NETWORK_NAME = "net1";
     private static final String NETWORK_ID = Integer.toString(NETWORK_NAME.hashCode());
     private static final String DATA_CENTER_ID = "999";
     private static final String ISO_NAME = PowerShellVmsResourceTest.ISO_NAME;
     private static final String VFD_NAME = PowerShellVmsResourceTest.VFD_NAME;
 
     private static final String CDROM_ID = Integer.toString("cdrom".hashCode());
     private static final String FLOPPY_ID = Integer.toString("floppy".hashCode());
 
     private static final String GET_CDROMS_CMD = "get-vm \"" + VM_ID + "\"";
    private static final String UPDATE_CDROM_CMD = "$v = get-vm \"{0}\";$v.cdisopath = \"{1}\";update-vm -vmobject $v";
 
     private static final String GET_FLOPPIES_CMD = "get-vm \"" + VM_ID + "\"";
     private static final String UPDATE_FLOPPY_CMD = "$v = get-vm \"{0}\";$v.floppypath = \"{1}\";update-vm -vmobject $v";
 
     private static final long DISK_SIZE = 10;
     private static final long DISK_SIZE_BYTES = DISK_SIZE * 1024 * 1024 * 1024;
 
     private static final String[] diskArgs = new String[] { Long.toString(DISK_SIZE_BYTES), "", "", "", "" };
     private static final String[] networkArgs = new String[] { DATA_CENTER_ID};
     private static final String[] nicArgs = new String[] { NETWORK_NAME };
 
     private static final String GET_DISKS_CMD = "$v = get-vm \"" + VM_ID + "\";$v.GetDiskImages()";
 
     private static final String ADD_DISK_COMMAND = "$d = new-disk -disksize {0} -disktype System;$v = get-vm \"{1}\";add-disk -diskobject $d -vmobject $v";
     private static final String REMOVE_DISK_COMMAND = "remove-disk -vmid \"{0}\" -diskids \"{1}\"";
 
     private static final String GET_NICS_CMD = "$v = get-vm \"" + VM_ID + "\";$v.GetNetworkAdapters()";
 
     public static final String LOOKUP_NETWORK_ID_COMMAND = "$n = get-networks;foreach ($i in $n) {  if ($i.name -eq \"" + NETWORK_NAME + "\") {    $i  }}";
 
     private static final String ADD_NIC_COMMAND = "$v = get-vm \"{0}\";foreach ($i in get-networks) '{'  if ($i.networkid -eq \"{1}\") '{    $n = $i  }}'add-networkadapter -vmobject $v -interfacename \"{2}\" -networkname $n.name";
     private static final String REMOVE_NIC_COMMAND = "$v = get-vm \"{0}\";foreach ($i in $v.GetNetworkAdapters()) '{'  if ($i.id -eq \"{1}\") '{    $n = $i  }}'remove-networkadapter -vmobject $v -networkadapterobject $n";
 
     protected PowerShellVmResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
         return new PowerShellVmResource(VM_ID, executor, uriProvider, poolMap, parser);
     }
 
     protected String formatVm(String name) {
         return formatXmlReturn("vm",
                                new String[] { name },
                                new String[] { "" },
                                PowerShellVmsResourceTest.extraArgs);
     }
 
     protected String formatDisk(String name) {
         return formatXmlReturn("disk",
                                new String[] { name },
                                new String[] { "" },
                                diskArgs);
     }
 
     protected String formatNetwork(String name) {
         return formatXmlReturn("network",
                                new String[] { name },
                                new String[] { "" },
                                networkArgs);
     }
 
     protected String formatNic(String name) {
         return formatXmlReturn("nic",
                                new String[] { name },
                                new String[] { "" },
                                nicArgs);
     }
 
     @Test
     public void testCdRomGet() throws Exception {
         PowerShellCdRomsResource parent = new PowerShellCdRomsResource(VM_ID,
                                                                        poolMap,
                                                                        resource.new CdRomQuery(VM_ID),
                                                                        uriProvider);
         PowerShellDeviceResource<CdRom, CdRoms> cdromResource =
             new PowerShellDeviceResource<CdRom, CdRoms>(parent, CDROM_ID);
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(GET_CDROMS_CMD, formatVm(VM_NAME));
         verifyCdRom(cdromResource.get());
     }
 
     @Test
     public void testCdRomList() throws Exception {
         PowerShellCdRomsResource cdromResource = new PowerShellCdRomsResource(VM_ID,
                                                                          poolMap,
                                                                          resource.new CdRomQuery(VM_ID),
                                                                          uriProvider);
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(GET_CDROMS_CMD, formatVm(VM_NAME));
 
         verifyCdRoms(cdromResource.list());
     }
 
     @Test
     public void testCdRomAdd() throws Exception {
         PowerShellCdRomsResource cdromResource = new PowerShellCdRomsResource(VM_ID,
                                                                               poolMap,
                                                                               resource.new CdRomQuery(VM_ID),
                                                                               uriProvider);
 
         CdRom cdrom = new CdRom();
         cdrom.setFile(new File());
         cdrom.getFile().setId(ISO_NAME);
 
         String command = MessageFormat.format(UPDATE_CDROM_CMD, VM_ID, ISO_NAME);
 
         setUriInfo(setUpCmdExpectations(command, "", "cdroms", CDROM_ID));
 
         verifyCdRom((CdRom)cdromResource.add(cdrom).getEntity());
     }
 
     @Test
     public void testCdRomUpdate() throws Exception {
         PowerShellCdRomsResource cdromsResource = new PowerShellCdRomsResource(VM_ID,
                                                                                poolMap,
                                                                                resource.new CdRomQuery(VM_ID),
                                                                                uriProvider);
 
         PowerShellDeviceResource<CdRom, CdRoms> cdromResource = cdromsResource.getDeviceSubResource(CDROM_ID);
 
         CdRom cdrom = new CdRom();
         cdrom.setFile(new File());
         cdrom.getFile().setId(ISO_NAME);
 
         String command = MessageFormat.format(UPDATE_CDROM_CMD, VM_ID, ISO_NAME);
 
         setUriInfo(setUpCmdExpectations(new String[]{command}, new String[]{""}, "cdroms", CDROM_ID, false));
 
         verifyCdRom(cdromResource.update(cdrom));
     }
 
     @Test
     public void testCdRomRemove() throws Exception {
         PowerShellCdRomsResource cdromResource = new PowerShellCdRomsResource(VM_ID,
                                                                               poolMap,
                                                                               resource.new CdRomQuery(VM_ID),
                                                                               uriProvider);
 
         String command = MessageFormat.format(UPDATE_CDROM_CMD, VM_ID, "");
 
         setUpCmdExpectations(command, "");
 
         cdromResource.remove(CDROM_ID);
     }
 
     @Test
     public void testFloppyGet() throws Exception {
         PowerShellFloppiesResource parent = new PowerShellFloppiesResource(VM_ID,
                                                                            poolMap,
                                                                            resource.new FloppyQuery(VM_ID),
                                                                            uriProvider);
         PowerShellDeviceResource<Floppy, Floppies> floppyResource =
             new PowerShellDeviceResource<Floppy, Floppies>(parent, FLOPPY_ID);
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(GET_FLOPPIES_CMD, formatVm(VM_NAME));
         verifyFloppy(floppyResource.get());
     }
 
     @Test
     public void testFloppyList() throws Exception {
         PowerShellFloppiesResource floppyResource = new PowerShellFloppiesResource(VM_ID,
                                                                                    poolMap,
                                                                                    resource.new FloppyQuery(VM_ID),
                                                                                    uriProvider);
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(GET_FLOPPIES_CMD, formatVm(VM_NAME));
 
         verifyFloppies(floppyResource.list());
     }
 
     @Test
     public void testFloppyAdd() throws Exception {
         PowerShellFloppiesResource floppyResource = new PowerShellFloppiesResource(VM_ID,
                                                                                    poolMap,
                                                                                    resource.new FloppyQuery(VM_ID),
                                                                                    uriProvider);
 
         Floppy floppy = new Floppy();
         floppy.setFile(new File());
         floppy.getFile().setId(VFD_NAME);
 
         String command = MessageFormat.format(UPDATE_FLOPPY_CMD, VM_ID, VFD_NAME);
 
         setUriInfo(setUpCmdExpectations(command, "", "floppies", FLOPPY_ID));
 
         verifyFloppy((Floppy)floppyResource.add(floppy).getEntity());
     }
 
     @Test
     public void testFloppyUpdate() throws Exception {
         PowerShellFloppiesResource floppiesResource = new PowerShellFloppiesResource(VM_ID,
                                                                                      poolMap,
                                                                                      resource.new FloppyQuery(VM_ID),
                                                                                      uriProvider);
 
         PowerShellDeviceResource<Floppy, Floppies> floppyResource = floppiesResource.getDeviceSubResource(FLOPPY_ID);
 
         Floppy floppy = new Floppy();
         floppy.setFile(new File());
         floppy.getFile().setId(VFD_NAME);
 
         String command = MessageFormat.format(UPDATE_FLOPPY_CMD, VM_ID, VFD_NAME);
 
         setUriInfo(setUpCmdExpectations(new String[]{command}, new String[]{""}, "floppies", FLOPPY_ID, false));
 
         verifyFloppy(floppyResource.update(floppy));
     }
 
     @Test
     public void testFloppyRemove() throws Exception {
         PowerShellFloppiesResource floppyResource = new PowerShellFloppiesResource(VM_ID,
                                                                                    poolMap,
                                                                                    resource.new FloppyQuery(VM_ID),
                                                                                    uriProvider);
 
         String command = MessageFormat.format(UPDATE_FLOPPY_CMD, VM_ID, "");
 
         setUpCmdExpectations(command, "");
 
         floppyResource.remove(FLOPPY_ID);
     }
 
     @Test
     public void testDiskGet() throws Exception {
         PowerShellDisksResource parent = new PowerShellDisksResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
         PowerShellDeviceResource<Disk, Disks> diskResource =
             new PowerShellDeviceResource<Disk, Disks>(parent, DISK_ID);
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(GET_DISKS_CMD, formatDisk(DISK_NAME));
         verifyDisk(diskResource.get());
     }
 
     @Test
     public void testDiskList() throws Exception {
         PowerShellDisksResource diskResource = new PowerShellDisksResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(GET_DISKS_CMD, formatDisk(DISK_NAME));
 
         verifyDisks(diskResource.list());
     }
 
     @Test
     public void testDiskAdd() throws Exception {
         PowerShellDisksResource diskResource = new PowerShellDisksResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
 
         Disk disk = new Disk();
         disk.setType(DiskType.SYSTEM);
         disk.setSize(DISK_SIZE_BYTES);
 
         String command = MessageFormat.format(ADD_DISK_COMMAND, DISK_SIZE, VM_ID);
 
         setUriInfo(setUpCmdExpectations(command, formatDisk(DISK_NAME), "disks", DISK_ID));
 
         verifyDisk((Disk)diskResource.add(disk).getEntity());
     }
 
     @Test
     public void testDiskRemove() throws Exception {
         PowerShellDisksResource diskResource = new PowerShellDisksResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
 
         String command = MessageFormat.format(REMOVE_DISK_COMMAND, VM_ID, DISK_ID);
 
         setUpCmdExpectations(command, "");
 
         diskResource.remove(DISK_ID);
     }
 
     @Test
     public void testNicGet() throws Exception {
         PowerShellNicsResource parent = new PowerShellNicsResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
         PowerShellDeviceResource<NIC, Nics> nicResource =
             new PowerShellDeviceResource<NIC, Nics>(parent, NIC_ID);
 
         String [] commands = { GET_NICS_CMD, LOOKUP_NETWORK_ID_COMMAND };
         String [] returns = { formatNic(NIC_NAME), formatNetwork(NETWORK_NAME) };
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(commands, returns);
 
         verifyNic(nicResource.get());
     }
 
     @Test
     public void testNicList() throws Exception {
         PowerShellNicsResource nicResource = new PowerShellNicsResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
 
         String [] commands = { GET_NICS_CMD, LOOKUP_NETWORK_ID_COMMAND };
         String [] returns = { formatNic(NIC_NAME), formatNetwork(NETWORK_NAME) };
 
         setUriInfo(setUpBasicUriExpectations());
         setUpCmdExpectations(commands, returns);
 
         verifyNics(nicResource.list());
     }
 
     @Test
     public void testNicAdd() throws Exception {
         PowerShellNicsResource nicResource = new PowerShellNicsResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
 
         NIC nic = new NIC();
         nic.setName(NIC_NAME);
         nic.setNetwork(new Network());
         nic.getNetwork().setId(NETWORK_ID);
 
         String [] commands = {
             MessageFormat.format(ADD_NIC_COMMAND, VM_ID, NETWORK_ID, NIC_NAME),
             LOOKUP_NETWORK_ID_COMMAND
         };
 
         String [] returns = { formatNic(NIC_NAME), formatNetwork(NETWORK_NAME) };
 
         setUriInfo(setUpCmdExpectations(commands, returns, "nics", NIC_ID));
 
         verifyNic((NIC)nicResource.add(nic).getEntity());
     }
 
     @Test
     public void testNicRemove() throws Exception {
         PowerShellNicsResource nicResource = new PowerShellNicsResource(VM_ID, poolMap, parser, "get-vm", uriProvider);
 
         String command = MessageFormat.format(REMOVE_NIC_COMMAND, VM_ID, NIC_ID);
 
         setUpCmdExpectations(command, "");
 
         nicResource.remove(NIC_ID);
     }
 
 
     private UriInfo setUpCmdExpectations(String[] commands, String[] returns, String collectionType, String newId) throws Exception {
         return setUpCmdExpectations(commands, returns, collectionType, newId, true);
     }
 
     private UriInfo setUpCmdExpectations(String[] commands, String[] returns, String collectionType, String newId, boolean build) throws Exception {
         mockStatic(PowerShellCmd.class);
         for (int i = 0 ; i < Math.min(commands.length, returns.length) ; i++) {
             if (commands[i] != null) {
                 expect(PowerShellCmd.runCommand(setUpPoolExpectations(), commands[i])).andReturn(returns[i]);
             }
         }
 
         UriInfo uriInfo = null;
         if (collectionType != null && newId != null) {
             uriInfo = setUpBasicUriExpectations();
             if (build) {
                 UriBuilder uriBuilder = createMock(UriBuilder.class);
                 expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
                 expect(uriBuilder.path(newId)).andReturn(uriBuilder);
                 expect(uriBuilder.build()).andReturn(new URI("vms/" + VM_ID + "/" + collectionType + "/" + newId)).anyTimes();
             }
         }
 
         replayAll();
 
         return uriInfo;
     }
 
     protected PowerShellPool setUpPoolExpectations() {
         PowerShellPool pool = createMock(PowerShellPool.class);
         expect(poolMap.get()).andReturn(pool);
         return pool;
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
         verifyLinks(cdrom);
     }
 
     private void verifyCdRoms(CdRoms cdroms) {
         assertNotNull(cdroms);
         assertEquals(cdroms.getCdRoms().size(), 1);
         verifyCdRom(cdroms.getCdRoms().get(0));
     }
 
     private void verifyFloppy(Floppy floppy) {
         assertNotNull(floppy);
         assertEquals(floppy.getId(), FLOPPY_ID);
         assertNotNull(floppy.getVm());
         assertEquals(floppy.getVm().getId(), VM_ID);
         verifyLinks(floppy);
     }
 
     private void verifyFloppies(Floppies floppies) {
         assertNotNull(floppies);
         assertEquals(floppies.getFloppies().size(), 1);
         verifyFloppy(floppies.getFloppies().get(0));
     }
 
     private void verifyDisk(Disk disk) {
         assertNotNull(disk);
         assertEquals(disk.getId(), DISK_ID);
         assertNotNull(disk.getVm());
         assertEquals(disk.getVm().getId(), VM_ID);
         verifyLinks(disk);
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
         verifyLinks(nic);
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
