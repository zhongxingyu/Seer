 /**
  * Copyright (C) 2009-2012 enStratus Networks Inc
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.cloudstack.compute;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.cloudstack.CSCloud;
 import org.dasein.cloud.cloudstack.CSException;
 import org.dasein.cloud.cloudstack.CSMethod;
 import org.dasein.cloud.cloudstack.Param;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.Snapshot;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.compute.Volume;
 import org.dasein.cloud.compute.VolumeCreateOptions;
 import org.dasein.cloud.compute.VolumeFormat;
 import org.dasein.cloud.compute.VolumeProduct;
 import org.dasein.cloud.compute.VolumeState;
 import org.dasein.cloud.compute.VolumeSupport;
 import org.dasein.cloud.compute.VolumeType;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.uom.storage.Gigabyte;
 import org.dasein.util.uom.storage.Storage;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Volumes implements VolumeSupport {
     static private final Logger logger = Logger.getLogger(Volumes.class);
     
     static private final String ATTACH_VOLUME = "attachVolume";
     static private final String CREATE_VOLUME = "createVolume";
     static private final String DELETE_VOLUME = "deleteVolume";
     static private final String DETACH_VOLUME = "detachVolume";
     static private final String LIST_DISK_OFFERINGS     = "listDiskOfferings";
 
     static private final String LIST_VOLUMES  = "listVolumes";
 
     static public class DiskOffering {
         public String id;
         public long diskSize;
         public String name;
         public String description;
 
         public String toString() {return "DiskOffering ["+id+"] of size "+diskSize;}
     }
     
     private CSCloud provider;
     
     Volumes(CSCloud provider) {
         this.provider = provider;
     }
     
     @Override
     public void attach(@Nonnull String volumeId, @Nonnull String serverId, @Nullable String deviceId) throws InternalException, CloudException {
         Param[] params;
         
         if( logger.isInfoEnabled() ) {
             logger.info("attaching " + volumeId + " to " + serverId + " as " + deviceId);
         }
         VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(serverId);
 
         if( vm == null ) {
             throw new CloudException("No such virtual machine: " + serverId);
         }
         long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 10L);
 
         while( timeout > System.currentTimeMillis() ) {
             if( VmState.RUNNING.equals(vm.getCurrentState()) || VmState.STOPPED.equals(vm.getCurrentState()) ) {
                 break;
             }
             try { Thread.sleep(15000L); }
             catch( InterruptedException ignore ) { }
             try { vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(serverId); }
             catch( Throwable ignore ) { }
             if( vm == null ) {
                 throw new CloudException("Virtual machine " + serverId + " disappeared waiting for it to enter an attachable state");
             }
         }
         if( deviceId == null ) {
             params = new Param[] { new Param("id", volumeId), new Param("virtualMachineId", serverId) }; 
         }
         else {
             deviceId = toDeviceNumber(deviceId);
             if( logger.isDebugEnabled() ) {
                 logger.debug("Device mapping is: " + deviceId);
             }
             params = new Param[] { new Param("id", volumeId), new Param("virtualMachineId", serverId), new Param("deviceId", deviceId) };
         }
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(ATTACH_VOLUME, params));
 
         if( doc == null ) {
             throw new CloudException("No such volume or server");
         }
         provider.waitForJob(doc, "Attach Volume");
     }
 
     @Override
     @Deprecated
     public @Nonnull String create(@Nullable String snapshotId, @Nonnegative int size, @Nullable String zoneId) throws InternalException, CloudException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was provided for this request");
         }
         String name = "vol_" + System.currentTimeMillis();
         VolumeCreateOptions options;
 
         if( size < 0 ) {
             size = 0;
         }
         if( snapshotId == null ) {
             options = VolumeCreateOptions.getInstance(new Storage<Gigabyte>(size, Storage.GIGABYTE), name, name);
         }
         else {
             options = VolumeCreateOptions.getInstanceForSnapshot(snapshotId, new Storage<Gigabyte>(size, Storage.GIGABYTE), name, name);
         }
         if( zoneId != null ) {
             options = options.inDataCenter(zoneId);
         }
         return createVolume(options);
     }
 
     @Override
     public @Nonnull String createVolume(@Nonnull VolumeCreateOptions options) throws InternalException, CloudException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was provided for this request");
         }
         String snapshotId = options.getSnapshotId();
         String productId = options.getVolumeProductId();
         VolumeProduct product = null;
 
         if( productId != null ) {
             for( VolumeProduct prd : listVolumeProducts() ) {
                 if( productId.equals(prd.getProviderProductId()) ) {
                     product = prd;
                     break;
                 }
             }
         }
         Storage<Gigabyte> size;
 
         if( snapshotId == null ) {
             if( product == null ) {
                 size = options.getVolumeSize();
                 if( size.intValue() < getMinimumVolumeSize().intValue() ) {
                     size = getMinimumVolumeSize();
                 }
                 Iterable<VolumeProduct> products = listVolumeProducts();
                 VolumeProduct best = null;
 
                 for( VolumeProduct p : products ) {
                     Storage<Gigabyte> s = p.getVolumeSize();
 
                     if( s  == null || s.intValue() == 0 ) {
                         product = p;
                         break;
                     }
                     long currentSize = s.getQuantity().longValue();
 
                     s = (best == null ? null : best.getVolumeSize());
 
                     long bestSize = (s == null ? 0L : s.getQuantity().longValue());
 
                     if( size.longValue() > 0L && size.longValue() == currentSize ) {
                         product = p;
                         break;
                     }
                     if( best == null ) {
                         best = p;
                     }
                     else if( bestSize > 0L || currentSize > 0L ) {
                         if( size.longValue() > 0L ) {
                             if( bestSize < size.longValue() && bestSize >0L && currentSize > size.longValue() ) {
                                 best = p;
                             }
                             else if( bestSize > size.longValue() && currentSize > size.longValue() && currentSize < bestSize ) {
                                 best = p;
                             }
                         }
                         else if( currentSize > 0L && currentSize < bestSize ) {
                             best = p;
                         }
                     }
                 }
                 if( product == null ) {
                     product = best;
                 }
             }
             else {
                 size = product.getVolumeSize();
                 if( size == null || size.intValue() < 1 ) {
                     size = options.getVolumeSize();
                 }
             }
             if( product == null && size.longValue() < 1L ) {
                 throw new CloudException("No offering matching " + options.getVolumeProductId());
             }
         }
         else {
             Snapshot snapshot = provider.getComputeServices().getSnapshotSupport().getSnapshot(snapshotId);
 
             if( snapshot == null ) {
                 throw new CloudException("No such snapshot: " + snapshotId);
             }
             int s = snapshot.getSizeInGb();
 
             if( s < 1 || s < getMinimumVolumeSize().intValue() ) {
                 size = getMinimumVolumeSize();
             }
             else {
                 size = new Storage<Gigabyte>(s, Storage.GIGABYTE);
             }
         }
         Param[] params;
 
         if( product == null && snapshotId == null ) {
             params = new Param[] {
                     new Param("name", options.getName()),
                     new Param("zoneId", ctx.getRegionId()),
                     new Param("size", String.valueOf(size.longValue()))
             };
         }
         else if( snapshotId != null ) {
             params = new Param[] {
                     new Param("name", options.getName()),
                     new Param("zoneId", ctx.getRegionId()),
                     new Param("snapshotId", snapshotId),
                     new Param("size", String.valueOf(size.longValue()))
             };
         }
         else {
             Storage<Gigabyte> s = product.getVolumeSize();
 
             if( s == null || s.intValue() < 1 ) {
                 params = new Param[] {
                         new Param("name", options.getName()),
                         new Param("zoneId", ctx.getRegionId()),
                         new Param("diskOfferingId", product.getProviderProductId()),
                         new Param("size", String.valueOf(size.longValue()))
                 };
             }
             else {
                 params = new Param[] {
                         new Param("name", options.getName()),
                         new Param("zoneId", ctx.getRegionId()),
                         new Param("diskOfferingId", product.getProviderProductId())
                 };
             }
         }
 
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(CREATE_VOLUME, params));
         NodeList matches = doc.getElementsByTagName("volumeid"); // v2.1
         String volumeId = null;
 
         if( matches.getLength() > 0 ) {
             volumeId = matches.item(0).getFirstChild().getNodeValue();
         }
         if( volumeId == null ) {
             matches = doc.getElementsByTagName("id"); // v2.2
             if( matches.getLength() > 0 ) {
                 volumeId = matches.item(0).getFirstChild().getNodeValue();
             }
         }
         if( volumeId == null ) {
             throw new CloudException("Failed to create volume");
         }
         provider.waitForJob(doc, "Create Volume");
         return volumeId;
     }
 
     @Override
     public void detach(@Nonnull String volumeId) throws InternalException, CloudException {
         detach(volumeId, false);
     }
 
     @Override
     public void detach(@Nonnull String volumeId, boolean force) throws InternalException, CloudException {
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(DETACH_VOLUME, new Param("id", volumeId)));
 
         provider.waitForJob(doc, "Detach Volume");
     }
 
     @Override
     public int getMaximumVolumeCount() throws InternalException, CloudException {
         return -2;
     }
 
     @Override
     public @Nonnull Storage<Gigabyte> getMaximumVolumeSize() throws InternalException, CloudException {
         return new Storage<Gigabyte>(5000, Storage.GIGABYTE);
     }
 
     @Override
     public @Nonnull Storage<Gigabyte> getMinimumVolumeSize() throws InternalException, CloudException {
         return new Storage<Gigabyte>(1, Storage.GIGABYTE);
     }
 
     @Nonnull Collection<DiskOffering> getDiskOfferings() throws InternalException, CloudException {
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(LIST_DISK_OFFERINGS));
         ArrayList<DiskOffering> offerings = new ArrayList<DiskOffering>();
         NodeList matches = doc.getElementsByTagName("diskoffering");
         
         for( int i=0; i<matches.getLength(); i++ ) {
             DiskOffering offering = new DiskOffering();
             Node node = matches.item(i);
             NodeList attributes;
             
             attributes = node.getChildNodes();
             for( int j=0; j<attributes.getLength(); j++ ) {
                 Node n = attributes.item(j);
                 String value;
 
                 if( n.getChildNodes().getLength() > 0 ) {
                     value = n.getFirstChild().getNodeValue();
                 }
                 else {
                     value = null;
                 }
                 if( n.getNodeName().equals("id") ) {
                     offering.id = value;
                 }
                 else if( n.getNodeName().equals("disksize") ) {
                     offering.diskSize = Long.parseLong(value);
                 }
                 else if( n.getNodeName().equalsIgnoreCase("name") ) {
                     offering.name = value;
                 }
                 else if( n.getNodeName().equalsIgnoreCase("displayText") ) {
                     offering.description = value;
                 }
             }
             if( offering.id != null ) {
                 if( offering.name == null ) {
                     if( offering.diskSize > 0 ) {
                         offering.name = offering.diskSize + " GB";
                     }
                     else {
                         offering.name = "Custom #" + offering.id;
                     }
                 }
                 if( offering.description == null ) {
                     offering.description = offering.name;
                 }
                 offerings.add(offering);
             }
         }
         return offerings;
     }
     
     @Override
     public @Nonnull String getProviderTermForVolume(@Nonnull Locale locale) {
         return "volume";
     }
 
     @Nullable String getRootVolumeId(@Nonnull String serverId) throws InternalException, CloudException {
         Volume volume = getRootVolume(serverId);
         
         return (volume == null ? null : volume.getProviderVolumeId());
     }
 
     /*
     @Nonnull String getRootVolumeOffering(@Nonnull String serverId) throws InternalException, CloudException {
         return getRootVolume(serverId).offeringId;
     }
     */
 
     private @Nullable Volume getRootVolume(@Nonnull String serverId) throws InternalException, CloudException {
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(LIST_VOLUMES, new Param("virtualMachineId", serverId)));
         NodeList matches = doc.getElementsByTagName("volume");
         
         for( int i=0; i<matches.getLength(); i++ ) {
             Node v = matches.item(i);
 
             if( v != null ) {
                 Volume volume = toVolume(v, true);
                 
                 if( volume != null ) {
                     return volume;
                 }
             }
         }
         return null;
     }
     
     @Override
     public @Nullable Volume getVolume(@Nonnull String volumeId) throws InternalException, CloudException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was configured for this request");
         }
         try {
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_VOLUMES, new Param("id", volumeId), new Param("zoneId", ctx.getRegionId())));
             NodeList matches = doc.getElementsByTagName("volume");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node v = matches.item(i);
 
                 if( v != null ) {
                     return toVolume(v, false);
                 }
             }
             return null;
         }
         catch( CSException e ) {
             if( e.getHttpCode() == 431 ) {
                 return null;
             }
             throw e;
         }
     }
 
     @Override
     public @Nonnull Requirement getVolumeProductRequirement() throws InternalException, CloudException {
         return Requirement.REQUIRED;
     }
 
     @Override
     public boolean isVolumeSizeDeterminedByProduct() throws InternalException, CloudException {
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         return provider.getComputeServices().getVirtualMachineSupport().isSubscribed();
     }
 
     private List<String> unixDeviceIdList    = null;
     private List<String> windowsDeviceIdList = null;
     
     @Override
     public @Nonnull Iterable<String> listPossibleDeviceIds(@Nonnull Platform platform) throws InternalException, CloudException {
         if( platform.isWindows() ) {
             if( windowsDeviceIdList == null ) {
                 ArrayList<String> list = new ArrayList<String>();
                 
                 list.add("hde");
                 list.add("hdf");
                 list.add("hdg");
                 list.add("hdh");
                 list.add("hdi");
                 list.add("hdj");
                 windowsDeviceIdList = Collections.unmodifiableList(list);
             }
             return windowsDeviceIdList;            
         }
         else {
             if( unixDeviceIdList == null ) {
                 ArrayList<String> list = new ArrayList<String>();
                 
                 list.add("/dev/xvdc");
                 list.add("/dev/xvde");
                 list.add("/dev/xvdf");
                 list.add("/dev/xvdg");
                 list.add("/dev/xvdh");
                 list.add("/dev/xvdi");
                 list.add("/dev/xvdj");
                 unixDeviceIdList = Collections.unmodifiableList(list);
             }
             return unixDeviceIdList;
         }
     }
 
     @Override
     public @Nonnull Iterable<VolumeFormat> listSupportedFormats() throws InternalException, CloudException {
         return Collections.singletonList(VolumeFormat.BLOCK);
     }
 
     private transient Collection<VolumeProduct> products;
 
     @Override
     public @Nonnull Iterable<VolumeProduct> listVolumeProducts() throws InternalException, CloudException {
         if( products == null ) {
             ArrayList<VolumeProduct> list = new ArrayList<VolumeProduct>();
 
             for( DiskOffering offering : getDiskOfferings() ) {
                 VolumeProduct p = toProduct(offering);
 
                 if( p != null ) {
                     list.add(p);
                 }
             }
             products = Collections.unmodifiableList(list);
         }
         return products;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listVolumeStatus() throws InternalException, CloudException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was specified for this request");
         }
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(LIST_VOLUMES, new Param("zoneId", ctx.getRegionId())));
         ArrayList<ResourceStatus> volumes = new ArrayList<ResourceStatus>();
         NodeList matches = doc.getElementsByTagName("volume");
 
         for( int i=0; i<matches.getLength(); i++ ) {
             Node v = matches.item(i);
 
             if( v != null ) {
                 ResourceStatus volume = toStatus(v);
 
                 if( volume != null ) {
                     volumes.add(volume);
                 }
             }
         }
         return volumes;
     }
 
     @Override
     public @Nonnull Iterable<Volume> listVolumes() throws InternalException, CloudException {
         return listVolumes(false);
     }
      
     private @Nonnull Collection<Volume> listVolumes(boolean rootOnly) throws InternalException, CloudException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was specified for this request");
         }
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(LIST_VOLUMES, new Param("zoneId", ctx.getRegionId())));
         ArrayList<Volume> volumes = new ArrayList<Volume>();
         NodeList matches = doc.getElementsByTagName("volume");
         
         for( int i=0; i<matches.getLength(); i++ ) {
             Node v = matches.item(i);
 
             if( v != null ) {
                 Volume volume = toVolume(v, rootOnly);
                 
                 if( volume != null ) {
                     volumes.add(volume);
                 }
             }
         }
         return volumes;
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     @Override
     public void remove(@Nonnull String volumeId) throws InternalException, CloudException {
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(DELETE_VOLUME, new Param("id", volumeId)));
 
         provider.waitForJob(doc, "Delete Volume");
     }
     
     private @Nonnull String toDeviceNumber(@Nonnull String deviceId) {
         if( !deviceId.startsWith("/dev/") && !deviceId.startsWith("hd") ) {
             deviceId = "/dev/" + deviceId;
         }
         if( deviceId.equals("/dev/xvda") ) { return "0"; }
         else if( deviceId.equals("/dev/xvdb") ) { return "1"; }
         else if( deviceId.equals("/dev/xvdc") ) { return "2"; }
         else if( deviceId.equals("/dev/xvde") ) { return "4"; }
         else if( deviceId.equals("/dev/xvdf") ) { return "5"; }
         else if( deviceId.equals("/dev/xvdg") ) { return "6"; }
         else if( deviceId.equals("/dev/xvdh") ) { return "7"; }
         else if( deviceId.equals("/dev/xvdi") ) { return "8"; }
         else if( deviceId.equals("/dev/xvdj") ) { return "9"; }
         else if( deviceId.equals("hda") ) { return "0"; }
         else if( deviceId.equals("hdb") ) { return "1"; }
         else if( deviceId.equals("hdc") ) { return "2"; }
         else if( deviceId.equals("hdd") ) { return "3"; }
         else if( deviceId.equals("hde") ) { return "4"; }
         else if( deviceId.equals("hdf") ) { return "5"; }
         else if( deviceId.equals("hdg") ) { return "6"; }
         else if( deviceId.equals("hdh") ) { return "7"; }
         else if( deviceId.equals("hdi") ) { return "8"; }
         else if( deviceId.equals("hdj") ) { return "9"; }
         else { return "9"; }
     }
 
     private @Nullable VolumeProduct toProduct(@Nullable DiskOffering offering) throws InternalException, CloudException {
         if( offering == null ) {
             return null;
         }
         if( offering.diskSize < 1 ) {
             return VolumeProduct.getInstance(offering.id, offering.name, offering.description, VolumeType.HDD);
         }
         else {
             return VolumeProduct.getInstance(offering.id, offering.name, offering.description, VolumeType.HDD, new Storage<Gigabyte>(offering.diskSize, Storage.GIGABYTE));
         }
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable Node node) throws InternalException, CloudException {
         if( node == null ) {
             return null;
         }
         NodeList attributes = node.getChildNodes();
         VolumeState volumeState = null;
         String volumeId = null;
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
 
             if( attribute != null ) {
                 String name = attribute.getNodeName();
 
                 if( name.equals("id") ) {
                     volumeId = attribute.getFirstChild().getNodeValue().trim();
                 }
                 else if( name.equals("state") && attribute.hasChildNodes() ) {
                     String state = attribute.getFirstChild().getNodeValue();
 
                     if( state == null ) {
                         volumeState = VolumeState.PENDING;
                     }
                     else if( state.equalsIgnoreCase("created") || state.equalsIgnoreCase("ready") || state.equalsIgnoreCase("allocated") ) {
                         volumeState = VolumeState.AVAILABLE;
                     }
                     else {
                         logger.warn("DEBUG: Unknown state for CloudStack volume: " + state);
                         volumeState = VolumeState.PENDING;
                     }
                 }
                 if( volumeId != null && volumeState != null ) {
                     break;
                 }
             }
         }
         if( volumeId == null ) {
             return null;
         }
         if( volumeState == null ) {
             volumeState = VolumeState.PENDING;
         }
         return new ResourceStatus(volumeId, volumeState);
     }
 
     private @Nullable Volume toVolume(@Nullable Node node, boolean rootOnly) throws InternalException, CloudException {
         if( node == null ) {
             return null;
         }
         
         Volume volume = new Volume();
         String offeringId = null;
         NodeList attributes = node.getChildNodes();
         String volumeName = null, description = null;
         boolean root = false;
 
         volume.setFormat(VolumeFormat.BLOCK);
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             
             if( attribute != null ) {
                 String name = attribute.getNodeName();
                 
                 if( name.equals("id") ) {
                     volume.setProviderVolumeId(attribute.getFirstChild().getNodeValue().trim());
                 }
                 if( name.equals("zoneid") ) {
                     String zid = attribute.getFirstChild().getNodeValue().trim();
                     if( !provider.getContext().getRegionId().equals(zid) ) {
                         System.out.println("Zone mismatch: " + provider.getContext().getRegionId());
                         System.out.println("               " + zid);
                         return null;
                     }
                 }
                 else if( name.equals("type") && attribute.hasChildNodes() ) {
                     if( attribute.getFirstChild().getNodeValue().equalsIgnoreCase("root") ) {
                         root = true;
                     }
                 }
                 else if( name.equals("diskofferingid") && attribute.hasChildNodes() ) {
                     offeringId = attribute.getFirstChild().getNodeValue().trim();
                 }
                 else if( name.equals("name") && attribute.hasChildNodes() ) {
                     volumeName = attribute.getFirstChild().getNodeValue().trim();
                 }
                 else if( name.equalsIgnoreCase("virtualmachineid") && attribute.hasChildNodes() ) {
                     volume.setProviderVirtualMachineId(attribute.getFirstChild().getNodeValue());
                 }
                 else if( name.equals("displayname") && attribute.hasChildNodes() ) {
                     description = attribute.getFirstChild().getNodeValue().trim();
                 }
                 else if( name.equals("size") && attribute.hasChildNodes() ) {
                     long size = (Long.parseLong(attribute.getFirstChild().getNodeValue())/1024000000L);
 
                     volume.setSize(new Storage<Gigabyte>(size, Storage.GIGABYTE));
                 }
                 else if( name.equals("state") && attribute.hasChildNodes() ) {
                     String state = attribute.getFirstChild().getNodeValue();
 
                    if( state == null ) {
                         volume.setCurrentState(VolumeState.PENDING);
                     }
                    else if( state.equalsIgnoreCase("created") || state.equalsIgnoreCase("ready") || state.equalsIgnoreCase("allocated") ) {
                         volume.setCurrentState(VolumeState.AVAILABLE);
                     }
                     else {
                         logger.warn("DEBUG: Unknown state for CloudStack volume: " + state);
                         volume.setCurrentState(VolumeState.PENDING);
                     }
                 }
                 else if( name.equals("created") && attribute.hasChildNodes() ) {
                     String date = attribute.getFirstChild().getNodeValue();
                     DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); //2009-02-03T05:26:32.612278
                     
                     try {
                         volume.setCreationTimestamp(df.parse(date).getTime());
                     }
                     catch( ParseException e ) {
                         volume.setCreationTimestamp(0L);
                     }
                 }
             }
         }
         if( !root && rootOnly ) {
             return null;
         }
         if( volume.getProviderVolumeId() == null ) {
             return null;
         }
         if( volumeName == null ) {
             volume.setName(volume.getProviderVolumeId());
         }
         else {
             volume.setName(volumeName);
         }
         if( description == null ) {
             volume.setDescription(volume.getName());
         }
         else {
             volume.setDescription(description);
         }
         if( offeringId != null ) {
             volume.setProviderProductId(offeringId);
         }
         volume.setProviderRegionId(provider.getContext().getRegionId());
         volume.setProviderDataCenterId(provider.getContext().getRegionId());
         if( volume.getProviderVirtualMachineId() != null ) {
             if( root ) {
                 volume.setDeviceId("/dev/xvda2");
             }
         }
         volume.setRootVolume(root);
         volume.setType(VolumeType.HDD);
         return volume;
     }
 }
