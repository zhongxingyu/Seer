 /**
  * Copyright (C) 2012-2013 Dell, Inc.
  * See annotations for authorship information
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
 
 package org.dasein.cloud.cloudsigma.compute.vm;
 
 import org.dasein.cloud.cloudsigma.CloudSigmaException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.Tag;
 import org.dasein.cloud.cloudsigma.CloudSigma;
 import org.dasein.cloud.cloudsigma.CloudSigmaConfigurationException;
 import org.dasein.cloud.cloudsigma.CloudSigmaMethod;
 import org.dasein.cloud.cloudsigma.NoContextException;
 import org.dasein.cloud.compute.*;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.uom.storage.Gigabyte;
 import org.dasein.util.uom.storage.Megabyte;
 import org.dasein.util.uom.storage.Storage;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Locale;
 import java.util.Random;
 import java.util.TreeSet;
 
 /**
  * Provides access to virtual machines in CloudSigma.
  * <p>Created by Danielle Mayne: 02/19/13 17:04 PM</p>
  * @author George Reese
  * @author Danielle Mayne
  * @version 2013.02 initial version
  * @since 2013.02
  */
 public class ServerSupport extends AbstractVMSupport {
     static private final Logger logger = CloudSigma.getLogger(ServerSupport.class);
 
     private CloudSigma provider;
 
     public ServerSupport(@Nonnull CloudSigma provider) {
         super(provider);
         this.provider = provider;
     }
 
 
     public void assignIP(@Nonnull String serverId, @Nonnull IpAddress address) throws CloudException, InternalException {
         VirtualMachine vm = null;
 
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         //dmayne 20130218: use JSON Parsing
         try {
             String obj = method.getString(toServerURL(serverId, ""));
             if (obj != null) {
                 vm = toVirtualMachine(new JSONObject(obj));
             }
 
             if (vm == null) {
                 throw new CloudException("No such virtual machine: " + serverId);
             }
 
             JSONObject server = new JSONObject(obj);
             JSONArray nics = server.getJSONArray("nics");
 
             JSONObject newNic = new JSONObject(), newIP = new JSONObject();
             newIP.put("ip", address.getProviderIpAddressId());
             newIP.put("conf", "static");
             newNic.put("ip_v4_conf", newIP);
             nics.put(newNic);
             server.put("nics", nics);
 
             change(vm, server.toString());
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     public void attach(@Nonnull Volume volume, @Nonnull String serverId, @Nonnull String deviceId) throws CloudException, InternalException {
         logger.debug("Device "+deviceId);
         if ( deviceId.contains("/dev/") ) {
             logger.debug("Stripping extra text /dev/: "+deviceId.substring(deviceId.indexOf("/dev/")+5));
             deviceId = deviceId.substring(deviceId.indexOf("/dev/")+5);
         }
         if (volume.getProviderVirtualMachineId() != null) {
             throw new CloudException("Volume is already attached to " + volume.getProviderVirtualMachineId());
         }
 
         VirtualMachine vm = null;
 
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         //dmayne 20130218: use JSON Parsing
         try {
             String obj = (method.getString(toServerURL(serverId, "")));
             if (obj != null) {
                 vm = toVirtualMachine(new JSONObject(obj));
             }
 
             if (vm == null) {
                 throw new CloudException("Virtual machine " + serverId + " does not exist");
             }
 
             JSONObject server = new JSONObject(obj);
             JSONArray drives = server.getJSONArray("drives");
 
             JSONObject newDrive = new JSONObject();
             //todo remove hardcoded values
             newDrive.put("boot_order", drives.length()+1);
             newDrive.put("device", "virtio");
             newDrive.put("dev_channel", deviceId);
             newDrive.put("drive", volume.getProviderVolumeId());
 
             drives.put(newDrive);
             server.put("drives", drives);
 
             change(vm, server.toString());
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private void change(@Nonnull VirtualMachine vm, @Nonnull String body) throws CloudException, InternalException {
         if (logger.isTraceEnabled()) {
             logger.trace("ENTER - " + ServerSupport.class.getName() + ".change(" + vm + "," + body + ")");
         }
         try {
             if (!VmState.STOPPED.equals(vm.getCurrentState())) {
                 throw new CloudException("Server must be stopped before making change");
             }
             VirtualMachine workingVm = vm;
 
             /*if (restart) {
                 if (logger.isInfoEnabled()) {
                     logger.info("Virtual machine " + vm.getProviderVirtualMachineId() + " needs to be stopped prior to change");
                 }
                 stop(vm.getProviderVirtualMachineId());
                 if (logger.isInfoEnabled()) {
                     logger.info("Waiting for " + vm.getProviderVirtualMachineId() + " to fully stop");
                 }
                 workingVm = waitForState(workingVm, CalendarWrapper.MINUTE * 10L, VmState.STOPPED);
                 if (workingVm == null) {
                     logger.info("Virtual machine " + vm.getProviderVirtualMachineId() + " disappared while waiting for stop");
                     throw new CloudException("Virtual machine " + vm.getProviderVirtualMachineId() + " disappeared before attachment could happen");
                 }
                 if (logger.isInfoEnabled()) {
                     logger.info("Done waiting for " + vm.getProviderVirtualMachineId() + ": " + workingVm.getCurrentState());
                 }
             }*/
             CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
             if (logger.isInfoEnabled()) {
                 logger.info("PUTing changes to " + vm.getProviderVirtualMachineId());
             }
 
             if (method.putString(toServerURL(vm.getProviderVirtualMachineId(), ""), body) == null) {
                 throw new CloudException("Unable to locate servers endpoint in CloudSigma");
             }
             if (logger.isInfoEnabled()) {
                 logger.info("Change to " + vm.getProviderVirtualMachineId() + " succeeded");
             }
             /*if (restart) {
                 if (logger.isInfoEnabled()) {
                     logger.info("Restarting " + vm.getProviderVirtualMachineId());
                 }
                 final String id = vm.getProviderVirtualMachineId();
 
                 Thread t = new Thread() {
                     public void run() {
                         try {
                             try {
                                 ServerSupport.this.start(id);
                                 try { Thread.sleep(2000L); }
                                 catch( InterruptedException ignore ) { }
                             } catch (Exception e) {
                                 logger.warn("Failed to start VM post-change: " + e.getMessage());
                             }
                         } finally {
                             provider.release();
                         }
                     }
                 };
 
                 provider.hold();
                 t.setName("Restart CloudSigma VM " + id);
                 t.setDaemon(true);
                 t.start();
             }*/
         } finally {
             if (logger.isTraceEnabled()) {
                 logger.trace("EXIT - " + ServerSupport.class.getName() + ".change()");
             }
         }
     }
 
     @Override
     public VirtualMachine alterVirtualMachine(@Nonnull String vmId, @Nonnull VMScalingOptions options) throws InternalException, CloudException {
         throw new OperationNotSupportedException("VM alteration not yet supported");
     }
 
     @Override
     public @Nonnull VirtualMachine clone(final @Nonnull String vmId, @Nonnull String intoDcId, @Nonnull String name, @Nonnull String description, boolean powerOn, @Nullable String... firewallIds) throws InternalException, CloudException {
         logger.debug("Name: "+name+", description: "+description);
 
         VirtualMachine vm = getVirtualMachine(vmId);
 
         if (vm == null || VmState.TERMINATED.equals(vm.getCurrentState())) {
             throw new CloudException("No such virtual machine to clone: " + vmId);
         }
         long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 20L);
 
         if (!VmState.STOPPED.equals(vm.getCurrentState())) {
             throw new CloudException("Server must be stopped before making clone");
         }
          /*   stop(vmId);
             while (timeout > System.currentTimeMillis()) {
                 if (vm == null || VmState.TERMINATED.equals(vm.getCurrentState())) {
                     throw new CloudException("Virtual machine terminated during stop for cloning");
                 }
                 if (VmState.STOPPED.equals(vm.getCurrentState())) {
                     break;
                 }
                 try {
                     Thread.sleep(30000L);
                 } catch (InterruptedException ignore) {
                 }
                 try {
                     vm = getVirtualMachine(vmId);
                 } catch (Exception ignore) {
                 }
             }
         }  */
         try {
             //dmayne 20130222: api 2.0 uses empty body for server clone
 
             CloudSigmaMethod method = new CloudSigmaMethod(provider);
             vm = null;    // make sure we are looking at the vm in the response
 
             //dmayne 20130218: use JSON Parsing
             JSONObject object = new JSONObject(method.postString(toServerURL(vmId, "action/?do=clone"), ""));
             if (object != null) {
                 vm = toVirtualMachine((JSONObject) object);
             }
             if (vm == null) {
                 throw new CloudException("No virtual machine was provided in the response");
             }
             if (powerOn) {
                 vm = waitForState(vm, CalendarWrapper.MINUTE * 15L, VmState.STOPPED, VmState.RUNNING);
                 if (vm == null) {
                     throw new CloudException("New VM disappeared");
                 }
                 if (!VmState.RUNNING.equals(vm.getCurrentState())) {
                     final String id = vm.getProviderVirtualMachineId();
 
                     Thread t = new Thread() {
                         public void run() {
                             try {
                                 try {
                                     ServerSupport.this.start(id);
                                     try { Thread.sleep(2000L); }
                                     catch( InterruptedException ignore ) { }
                                 } catch (Exception e) {
                                     logger.warn("Failed to start VM post-create: " + e.getMessage());
                                 }
                             } finally {
                                 provider.release();
                             }
                         }
                     };
 
                     provider.hold();
                     t.setName("Start CloudSigma VM " + id);
                     t.setDaemon(true);
                     t.start();
                 }
             }
             return vm;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
         finally {
             provider.hold();
             Thread t = new Thread() {
                 public void run() {
                     try {
                         try {
                             ServerSupport.this.start(vmId);
                             try { Thread.sleep(2000L); }
                             catch( InterruptedException ignore ) { }
                         } catch (Throwable ignore) {
                         }
                     } finally {
                         provider.release();
                     }
                 }
             };
 
             t.setName("CloudSigma Clone Restarted " + vmId);
             t.setDaemon(true);
             t.start();
         }
     }
 
     @Override
     public @Nullable VMScalingCapabilities describeVerticalScalingCapabilities() throws CloudException, InternalException {
         return null;
     }
 
 
     public void detach(@Nonnull Volume volume) throws CloudException, InternalException {
         String serverId = volume.getProviderVirtualMachineId();
 
         if (serverId == null) {
             throw new CloudException("No server is attached to " + volume.getProviderVolumeId());
         }
 
         VirtualMachine vm = null;
 
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         //dmayne 20130218: use JSON Parsing
         try {
             String obj = method.getString(toServerURL(serverId, ""));
             if (obj != null) {
                 vm = toVirtualMachine(new JSONObject(obj));
             }
 
             if (vm == null) {
                 throw new CloudException("No such virtual machine: " + serverId);
             }
 
             String driveId = volume.getProviderVolumeId();
 
             JSONObject json = new JSONObject(obj);
             JSONArray drives = json.getJSONArray("drives");
             JSONArray newArray = new JSONArray();
             int index = 0;
             for (int i = 0; i < drives.length(); i++) {
                 JSONObject drive = drives.getJSONObject(i);
                 JSONObject driveObj = drive.getJSONObject("drive");
                 if (!driveObj.getString("uuid").equals(driveId)) {
                     newArray.put(drives.getJSONObject(i));
                 }
             }
             json.put("drives", newArray);
             String jsonBody = json.toString();
 
             change(vm, jsonBody);
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     @Override
     public void disableAnalytics(String vmId) throws InternalException, CloudException {
         // NO-OP
     }
 
     @Override
     public void enableAnalytics(String vmId) throws InternalException, CloudException {
         // NO-OP
     }
 
     @Override
     public @Nonnull String getConsoleOutput(@Nonnull String vmId) throws InternalException, CloudException {
         return "";
     }
 
     @Override
     public int getCostFactor(@Nonnull VmState state) throws InternalException, CloudException {
         return 100;
     }
 
     public @Nullable String getDeviceId(@Nonnull VirtualMachine vm, @Nonnull String volumeId) throws CloudException, InternalException {
         for (int i = 0; i <= 9; i++) {
             for (int j = 0; j <= 9; j++) {
                 String id = (String) vm.getTag("virtio:" + i+":"+j);     //assume all drives added to virtio
 
                 if (id != null && id.equals(volumeId)) {
                     return String.valueOf(i).concat(":").concat(String.valueOf(j));
                 }
             }
         }
 
         return null;
     }
 
     @Override
     public int getMaximumVirtualMachineCount() throws CloudException, InternalException {
         return -2;
     }
 
     @Override
     public VirtualMachineProduct getProduct(@Nonnull String productId) throws InternalException, CloudException {
         String[] parts = productId.split(":");
         int cpuCount, ramInMb, cpuSpeed;
 
         if (parts.length < 2) {
             return null;
         }
         try {
             if (parts.length == 2) {
                 cpuCount = 1;
             } else {
                 cpuCount = Integer.parseInt(parts[2]);
             }
             ramInMb = Integer.parseInt(parts[0]);
             cpuSpeed = Integer.parseInt(parts[1]);
         } catch (NumberFormatException e) {
             return null;
         }
         VirtualMachineProduct product = new VirtualMachineProduct();
 
         product.setProviderProductId(productId);
         product.setName(ramInMb + "MB - " + cpuCount + "x" + cpuSpeed + "MHz");
         product.setRamSize(new Storage<Megabyte>(ramInMb, Storage.MEGABYTE));
         product.setCpuCount(cpuCount);
         product.setDescription(product.getName());
         product.setRootVolumeSize(new Storage<Gigabyte>(10, Storage.GIGABYTE));
         return product;
     }
 
     @Override
     public @Nonnull String getProviderTermForServer(@Nonnull Locale locale) {
         return "server";
     }
 
     @Override
     public VirtualMachine getVirtualMachine(@Nonnull String vmId) throws InternalException, CloudException {
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         //dmayne 20130218: use JSON Parsing
         try {
             String obj = (method.getString(toServerURL(vmId, "")));
             if (obj != null) {
                 return toVirtualMachine(new JSONObject(obj));
             }
         }
         catch (JSONException e) {
             throw  new InternalException(e);
         }
         return null;
     }
 
     @Override
     public @Nonnull Requirement identifyImageRequirement(@Nonnull ImageClass cls) throws CloudException, InternalException {
         return (cls.equals(ImageClass.MACHINE) ? Requirement.REQUIRED : Requirement.NONE);
     }
 
     @Override
     public @Nonnull Requirement identifyPasswordRequirement(Platform platform) throws CloudException, InternalException {
         return Requirement.OPTIONAL;
     }
 
     @Override
     public @Nonnull Requirement identifyRootVolumeRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyShellKeyRequirement(Platform platform) throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyVlanRequirement() throws CloudException, InternalException {
         return Requirement.OPTIONAL;
     }
 
     @Override
     public boolean isAPITerminationPreventable() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isBasicAnalyticsSupported() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isExtendedAnalyticsSupported() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
        listVirtualMachines();
         return true;
     }
 
     @Override
     public boolean isUserDataSupported() throws CloudException, InternalException {
         return false;
     }
 
     static private final Random random = new Random();
 
     private @Nonnull String generatePassword() {
         int len = 8 + random.nextInt(5);
         StringBuilder password = new StringBuilder();
 
         while( password.length() < len ) {
             char c = (char)random.nextInt(255);
 
             if( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ) {
                 if( c != 'I' && c != 'i' && c != 'o' && c != 'O' && c != 'l' ) {
                     password.append(c);
                 }
             }
             else if( c >= '2' && c <='9' ) {
                 password.append(c);
             }
             else if( c == '%' || c == '@' || c == '#' || c == '$' || c == '[' || c == ']' ) {
                 password.append(c);
             }
         }
         return password.toString();
     }
 
     @Override
     public @Nonnull VirtualMachine launch(@Nonnull VMLaunchOptions withLaunchOptions) throws CloudException, InternalException {
         logger.debug("Name: "+withLaunchOptions.getHostName()+", description: "+withLaunchOptions.getDescription()
                 +"friendly name: "+withLaunchOptions.getFriendlyName());
         if (logger.isTraceEnabled()) {
             logger.trace("ENTER - " + ServerSupport.class.getName() + ".launch(" + withLaunchOptions + ")");
         }
         try {
             MachineImage img = provider.getComputeServices().getImageSupport().getImage(withLaunchOptions.getMachineImageId());
 
             if (img == null) {
                 throw new CloudException("No such machine image: " + withLaunchOptions.getMachineImageId());
             }
 
             MachineImageState state = img.getCurrentState();
             String name = img.getName();
             boolean cloneFound = false;
             if (state.equals(MachineImageState.ACTIVE) && name.contains("clone")) {
                 logger.info("Available 'clone' - will attach directly to new server");
                 cloneFound = true;
             }
             else {
                 logger.info("Image is either mounted or is not a clone - cloning drive from machine image "+ img.getProviderMachineImageId());
             }
 
             String media = img.getTag("media").toString();
 
             String imageDriveId = null;
             //dmayne 20130529: cdrom does not need to be cloned and can be attached directly
             if (!media.equals("cdrom") && !cloneFound) {
                 if (logger.isInfoEnabled()) {
                     logger.info("Cloning drive from machine image " + img.getProviderMachineImageId() + "...");
                 }
 
                 JSONObject drive = provider.getComputeServices().getImageSupport().cloneDrive(withLaunchOptions.getMachineImageId(), withLaunchOptions.getHostName(), null);
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("drive=" + drive);
                 }
                 String driveId = null;
                 try {
                     JSONObject actualDrive = null;
                     if (drive.has("objects")) {
                         JSONArray objects = drive.getJSONArray("objects");
                         actualDrive = (JSONObject) objects.get(0);
                         driveId = actualDrive.getString("uuid");
                     }
 
                     if (driveId == null) {
                         throw new CloudException("No drive was cloned to support the machine launch process");
                     }
                     long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 40L);
                     String status = actualDrive.getString("status");
 
                     if (logger.isInfoEnabled()) {
                         logger.info("Waiting for new drive " + driveId + " to become active...");
                     }
                     while (timeout > System.currentTimeMillis()) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("status.drive." + driveId + "=" + status);
                        }
                         if (status != null && (status.equals("mounted") || status.equals("unmounted"))) {
                             if (logger.isInfoEnabled()) {
                                 logger.info("Drive is now ready for launching");
                             }
                             break;
                         }
                         try {
                             Thread.sleep(20000L);
                         } catch (InterruptedException ignore) {
                         }
                         try {
                             actualDrive = provider.getComputeServices().getImageSupport().getDrive(driveId);
                         } catch (Throwable ignore) {
                         }
                         if (actualDrive == null) {
                             throw new CloudException("Cloned drive has disappeared");
                         }
                         status = actualDrive.getString("status");
                     }
                     imageDriveId = actualDrive.getString("uuid");
                 }
                 catch (JSONException e) {
                     throw new InternalException(e);
                 }
             }
             else {
                 imageDriveId = img.getProviderMachineImageId();
             }
 
             //dmayne 20130529: now we can create server and attach drive
             try {
                 JSONObject newServer = new JSONObject(), newDrive = new JSONObject(), newNic = new JSONObject(), newVlan = new JSONObject();
                 JSONArray drives = new JSONArray(), nics = new JSONArray();
 
                 newServer.put("name", withLaunchOptions.getHostName().replaceAll("\n", " "));
                 String password = withLaunchOptions.getBootstrapPassword();
 
                 if( password == null ) {
                     password = generatePassword();
                 }
                 newServer.put("vnc_password", password);
 
                 newDrive.put("boot_order", 1);
                 newDrive.put("device", "virtio");
                 newDrive.put("dev_channel", "0:0");
                 newDrive.put("drive", imageDriveId);
 
                 drives.put(newDrive);
 
                 newServer.put("drives", drives);
 
                 String productId = withLaunchOptions.getStandardProductId();
                 int cpuCount = 1, cpuSpeed = 1000, ramInMb = 512;
                 long ramInBytes = 536870912;
                 String[] parts = productId.replaceAll("\n", " ").split(":");
                 if (parts.length > 1) {
                     cpuCount = 1;
                     try {
                         ramInMb = Integer.parseInt(parts[0]);
                         ramInBytes = ramInMb * 1024L * 1024L;
                         cpuSpeed = Integer.parseInt(parts[1]);
                         if (parts.length == 3) {
                             cpuCount = Integer.parseInt(parts[2]);
                             // total speed will be cpuCount * perSMPspeed
                             cpuSpeed = cpuSpeed*cpuCount;
                         }
                     } catch (NumberFormatException ignore) {
                         // ignore
                     }
                 }
 
                 newServer.put("cpu", String.valueOf(cpuSpeed));
                 newServer.put("mem", String.valueOf(ramInBytes));
                 newServer.put("smp", String.valueOf(cpuCount));
 
                 if (withLaunchOptions.getVlanId() != null) {
                     newVlan.put("uuid", withLaunchOptions.getVlanId().replaceAll("\n", " "));
                     newNic.put("vlan", newVlan);
                     nics.put(newNic);
                     newServer.put("nics", nics);
                 }
                 else {
                     JSONObject newIP = new JSONObject();
                     newIP.put("conf", "dhcp");
                     newNic.put("ip_v4_conf", newIP);
 
                     //firewall support
                     if (withLaunchOptions.getFirewallIds() != null) {
                         if (withLaunchOptions.getFirewallIds().length == 1) {
                             newNic.put("firewall_policy", withLaunchOptions.getFirewallIds()[0]);
                         }
                         else {
                             logger.warn("Firewall not applied to server as there is more than one - current list has "+withLaunchOptions.getFirewallIds().length);
                         }
                     }
 
                     nics.put(newNic);
                     newServer.put("nics", nics);
                 }
 
                 CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
                 if (logger.isInfoEnabled()) {
                     logger.info("Creating server....");
                 }
                 //dmayne 20130218: use JSON Parsing
                 VirtualMachine vm = null;
                 JSONObject obj = new JSONObject(method.postString("/servers/", newServer.toString()));
 
                 //dmayne 20130227: check value returned and extract created server from the objects array
                 if (obj != null) {
                     JSONObject object = (JSONObject) obj;
                     JSONArray arr = object.getJSONArray("objects");
                     JSONObject server = arr.getJSONObject(0);
                     vm = toVirtualMachine((JSONObject) server);
                 }
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("vm=" + vm);
                 }
                 if (vm == null) {
                     throw new CloudException("No virtual machine was provided in the response");
                 }
                 if (logger.isInfoEnabled()) {
                     logger.info("Waiting for " + vm.getProviderVirtualMachineId() + " to be STOPPED or RUNNING...");
                 }
                 vm = waitForState(vm, CalendarWrapper.MINUTE * 15L, VmState.STOPPED, VmState.RUNNING);
                 if (logger.isDebugEnabled()) {
                     logger.debug("post wait vm=" + vm);
                 }
                 if (vm == null) {
                     throw new CloudException("Virtual machine disappeared waiting for startup state");
                 }
                 if (logger.isDebugEnabled()) {
                     logger.debug("status.vm." + vm.getProviderVirtualMachineId() + "=" + vm.getCurrentState());
                 }
                 if (!VmState.RUNNING.equals(vm.getCurrentState())) {
                     if (logger.isInfoEnabled()) {
                         logger.info("Setting up a separate thread to start " + vm.getProviderVirtualMachineId() + "...");
                     }
                     final String id = vm.getProviderVirtualMachineId();
 
                     Thread t = new Thread() {
                         public void run() {
                             try {
                                 VirtualMachine vm = null;
 
                                 for (int i = 0; i < 5; i++) {
                                     try {
                                         if (vm == null) {
                                             try {
                                                 vm = getVirtualMachine(id);
                                             } catch (Throwable ignore) {
                                                 // ignore
                                             }
                                         }
                                         if (vm != null) {
                                             if (logger.isInfoEnabled()) {
                                                 logger.info("Verifying the state of " + id);
                                             }
                                             vm = waitForState(vm, CalendarWrapper.MINUTE * 15L, VmState.STOPPED, VmState.RUNNING);
                                             if (vm == null || VmState.TERMINATED.equals(vm.getCurrentState()) || VmState.RUNNING.equals(vm.getCurrentState())) {
                                                 if (logger.isInfoEnabled()) {
                                                     logger.info("Pre-emptive return due to non-existence or true running: " + id);
                                                 }
                                                 return;
                                             }
                                         }
                                         if (logger.isInfoEnabled()) {
                                             logger.info("Start attempt " + (i + 1) + " on " + id);
                                         }
                                         ServerSupport.this.start(id);
                                         if (logger.isInfoEnabled()) {
                                             logger.info("VM " + id + " started");
                                         }
                                         try { Thread.sleep(2000L); }
                                         catch( InterruptedException ignore ) { }
                                         return;
                                     } catch (Exception e) {
                                         logger.warn("Failed to start virtual machine " + id + " post-create: " + e.getMessage());
                                     }
                                     try {
                                         Thread.sleep(60000L);
                                     } catch (InterruptedException ignore) {
                                     }
                                 }
                                 if (logger.isInfoEnabled()) {
                                     logger.info("VM " + id + " never started");
                                     if (vm != null) {
                                         logger.debug("status.vm." + id + " (not started)=" + vm.getCurrentState());
                                     }
                                 }
                             } finally {
                                 provider.release();
                             }
                         }
                     };
 
                     provider.hold();
                     t.setName("Start CloudSigma VM " + id);
                     t.setDaemon(true);
                     t.start();
                 }
                 return vm;
             }
 
             catch (JSONException e) {
                 throw new InternalException(e);
             }
         } finally {
             if (logger.isTraceEnabled()) {
                 logger.trace("EXIT - " + ServerSupport.class.getName() + ".launch()");
             }
         }
     }
 
     @Override
     public @Nonnull Iterable<String> listFirewalls(@Nonnull String vmId) throws InternalException, CloudException {
         VirtualMachine vm = getVirtualMachine(vmId);
 
         String[] firewalls = vm.getProviderFirewallIds();
         ArrayList<String> list = new ArrayList<String>();
 
         for (int i= 0; i<firewalls.length; i++) {
            list.add(firewalls[i]);
         }
         return list;
     }
 
     private transient ArrayList<VirtualMachineProduct> cachedProducts;
 
     @Override
     public @Nonnull Iterable<VirtualMachineProduct> listProducts(@Nonnull Architecture architecture) throws InternalException, CloudException {
         ArrayList<VirtualMachineProduct> products = cachedProducts;
 
         if (products == null) {
             products = new ArrayList<VirtualMachineProduct>();
 
             for (int ram : new int[]{1024, 2048, 4096, 8192, 12288, 16384, 20480, 24576, 28668, 32768}) {
                 for (int cpu : new int[]{1000, 1200, 1500, 2000}) {
                     for (int cpuCount : new int[]{1, 2, 4, 8, 12}) {
                         if (cpuCount == 1) {
                             products.add(getProduct(ram + ":" + cpu));
                         } else {
                             products.add(getProduct(ram + ":" + cpu + ":" + cpuCount));
                         }
                     }
                 }
             }
             cachedProducts = products;
         }
         return products;
     }
 
     static private volatile Collection<Architecture> architectures;
 
     @Override
     public Iterable<Architecture> listSupportedArchitectures() {
         if (architectures == null) {
             ArrayList<Architecture> list = new ArrayList<Architecture>();
 
             list.add(Architecture.I64);
             list.add(Architecture.I32);
             architectures = Collections.unmodifiableCollection(list);
         }
         return architectures;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listVirtualMachineStatus() throws InternalException, CloudException {
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
         ArrayList<ResourceStatus> list = new ArrayList<ResourceStatus>();
 
         boolean moreData = true;
         String baseTarget = "/servers";
         String target = "/?fields=uuid,status";
 
         while(moreData)  {
             //dmayne 20130218: JSON Parsing
             target = baseTarget+target;
 
             JSONObject jObject = method.list(target);
 
             if (jObject == null) {
                 throw new CloudException("No servers endpoint found");
             }
 
             try {
                 JSONArray objects = jObject.getJSONArray("objects");
                 for (int i= 0; i < objects.length(); i++) {
                     ResourceStatus vm = toStatus(objects.getJSONObject(i));
 
                     if (vm != null) {
                         list.add(vm);
                     }
                 }
 
                 //dmayne 20130314: check if there are more pages
                 if (jObject.has("meta")) {
                     JSONObject meta = jObject.getJSONObject("meta");
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 }
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
         }
         return list;
     }
 
     @Override
     public @Nonnull Iterable<VirtualMachine> listVirtualMachines() throws InternalException, CloudException {
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
         ArrayList<VirtualMachine> list = new ArrayList<VirtualMachine>();
 
         boolean moreData = true;
         String baseTarget = "/servers/detail/";
         String target = "";
 
         while(moreData)  {
             //dmayne 20130218: JSON Parsing
             target = baseTarget+target;
 
             JSONObject jObject = method.list(target);
 
             if (jObject == null) {
                 throw new CloudException("No servers endpoint found");
             }
 
             try {
                 JSONArray objects = jObject.getJSONArray("objects");
                 for (int i=0; i< objects.length(); i++) {
                     VirtualMachine vm = toVirtualMachine(objects.getJSONObject(i));
 
                     if (vm != null) {
                         list.add(vm);
                     }
                 }
 
                 //dmayne 20130314: check if there are more pages
                 if (jObject.has("meta")) {
                     JSONObject meta = jObject.getJSONObject("meta");
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 }
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
         }
         return list;
     }
 
     @Override
     public void pause(@Nonnull String vmId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("CloudSigma does not support pause/unpause");
     }
 
     @Override
     public void reboot(@Nonnull String vmId) throws CloudException, InternalException {
         VirtualMachine vm = getVirtualMachine(vmId);
 
         if (vm == null) {
             throw new CloudException("No such virtual machine: " + vmId);
         }
         stop(vmId);
 
         long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 20L);
 
         while (timeout > System.currentTimeMillis()) {
             try {
                 vm = getVirtualMachine(vmId);
             } catch (Exception ignore) {
             }
             if (vm == null || VmState.TERMINATED.equals(vm.getCurrentState())) {
                 throw new CloudException("Server disappeared during reboot");
             }
             if (VmState.STOPPED.equals(vm.getCurrentState())) {
                 break;
             }
             try {
                 Thread.sleep(15000L);
             } catch (InterruptedException ignore) {
             }
         }
         start(vmId);
     }
 
     public void releaseIP(@Nonnull IpAddress address) throws CloudException, InternalException {
         String serverId = address.getServerId();
 
         if (serverId == null) {
             throw new CloudException("No server is assigned to " + address.getProviderIpAddressId());
         }
         VirtualMachine vm = null;
 
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         //dmayne 20130218: use JSON Parsing
         try {
             String obj = method.getString(toServerURL(serverId, ""));
             if (obj != null ) {
                 vm = toVirtualMachine(new JSONObject(obj));
             }
 
             if (vm == null) {
                 throw new CloudException("No such virtual machine: " + serverId);
             }
 
             JSONObject json = new JSONObject(obj);
             JSONArray nics = json.getJSONArray("nics");
             JSONArray newArray = new JSONArray();
             int index = 0;
             for (int i = 0; i < nics.length(); i++) {
                 JSONObject nic = (JSONObject) nics.get(i);
                 if (address.getVersion().equals(IPVersion.IPV4)) {
                     JSONObject nicObj = nic.getJSONObject("ip_v4_conf");
                     if (nicObj.isNull("ip") && nicObj.getString("conf").equalsIgnoreCase("dhcp")) {
                          newArray.put(nics.getJSONObject(i));
                     }
                     else if (!nicObj.isNull("ip")) {
                         JSONObject ip = nicObj.getJSONObject("ip");
                         if (!ip.getString("uuid").equals(address.getProviderIpAddressId())) {
                             newArray.put(nics.getJSONObject(i));
                         }
                     }
                 }
             }
             json.put("nics", newArray);
             String jsonBody = json.toString();
 
             change(vm, jsonBody);
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     @Override
     public void resume(@Nonnull String vmId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("CloudSigma does not support suspend/resume");
     }
 
     @Override
     public void start(@Nonnull String vmId) throws InternalException, CloudException {
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         try {
             method.postString(toServerURL(vmId, "action/?do=start"), "");
         }
         catch( CloudSigmaException e ) {
             if( e.getMessage().contains("Cannot start guest in state") ) {
                 return;
             }
             if (e.getHttpCode() == 402) {
                 //dmayne 20130603: if error is payment/billing related check for software licenses
                 VirtualMachine vm = getVirtualMachine(vmId);
                 MachineImage image = provider.getComputeServices().getImageSupport().getImage(vm.getProviderMachineImageId());
                 if (image.getSoftware() != null) {
                     throw new CloudException("Unable to start server - it is associated with a software license which does not have a paid subscription.");
                 }
                 else {
                     throw new CloudException("Unable to start server - payment required./nPlease check your account subscription and balance");
                 }
             }
             throw e;
         }
     }
 
     @Override
     public void stop(@Nonnull String vmId, boolean force) throws InternalException, CloudException {
         if (logger.isTraceEnabled()) {
             logger.trace("ENTER - " + ServerSupport.class.getName() + ".stop(" + vmId + "," + force + ")");
         }
         try {
             CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
             if (force) {
                 method.postString(toServerURL(vmId, "action/?do=stop"), "");
             } else {
                 method.postString(toServerURL(vmId, "action/?do=shutdown"), "");
                 VirtualMachine v = waitForState(getVirtualMachine(vmId),CalendarWrapper.MINUTE * 5L, VmState.STOPPED, VmState.RUNNING);
                 if (!v.getCurrentState().equals(VmState.STOPPED)) {
                     stop(vmId, true);
                 }
             }
         } finally {
             if (logger.isTraceEnabled()) {
                 logger.trace("EXIT - " + ServerSupport.class.getName() + ".stop()");
             }
         }
     }
 
     @Override
     public boolean supportsPauseUnpause(@Nonnull VirtualMachine vm) {
         return false;
     }
 
     @Override
     public boolean supportsStartStop(@Nonnull VirtualMachine vm) {
         return true;
     }
 
     @Override
     public boolean supportsSuspendResume(@Nonnull VirtualMachine vm) {
         return false;
     }
 
     @Override
     public void suspend(@Nonnull String vmId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("CloudSigma does not support suspend/resume");
     }
 
     @Override
     public void terminate(@Nonnull String vmId) throws InternalException, CloudException {
         VirtualMachine vm = getVirtualMachine(vmId);
 
         if (vm == null) {
             throw new CloudException("No such virtual machine: " + vmId);
         }
         if( !vm.getCurrentState().equals(VmState.STOPPED) ) {
             try { stop(vmId, true); }
             catch( Exception ignore ) { }
         }
         long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE* 5L);
 
         while( timeout > System.currentTimeMillis() ) {
             if( vm == null ) {
                 return;
             }
             if( vm.getCurrentState().equals(VmState.STOPPED) ) {
                 break;
             }
             try { Thread.sleep(15000L); }
             catch( InterruptedException ignore ) { }
             try { vm = getVirtualMachine(vmId); }
             catch( Throwable ignore ) { }
         }
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         method.deleteString(toServerURL(vmId, ""), "");
 
         timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 5L);
 
         try {
             vm = getVirtualMachine(vmId);
         } catch (Exception ignore) {
         }
         while (timeout > System.currentTimeMillis()) {
             if (vm == null || VmState.TERMINATED.equals(vm.getCurrentState())) {
                 return;
             }
             try {
                 Thread.sleep(15000L);
             } catch (InterruptedException ignore) {
             }
         }
         logger.warn("System timed out waiting for the VM termination to complete");
     }
 
     @Override
     public void unpause(@Nonnull String vmId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("CloudSigma does not support pause/unpause");
     }
 
     @Override
     public void updateTags(@Nonnull String vmId, @Nonnull Tag... tags) throws CloudException, InternalException {
         // NO-OP
     }
 
     @Override
     public void updateTags(@Nonnull String[] vmIds, @Nonnull Tag... tags) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void removeTags(@Nonnull String vmId, @Nonnull Tag... tags) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void removeTags(@Nonnull String[] vmIds, @Nonnull Tag... tags) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     private boolean isPublic(@Nonnull String ip) {
         if (ip.startsWith("127.0.0.")) {
             return false;
         }
         if (ip.startsWith("10.")) {
             return false;
         }
         if (ip.startsWith("192.168.")) {
             return false;
         }
         if (ip.startsWith("172.")) {
             String[] parts = ip.split("\\.");
 
             if (parts.length != 4) {
                 return true;
             }
             try {
                 int x = Integer.parseInt(parts[1]);
 
                 if (x >= 16 && x < 33) {
                     return false;
                 }
             } catch (NumberFormatException ignore) {
                 // ignore
             }
         }
         return true;
     }
 
     private void setIP(@Nonnull VirtualMachine vm, @Nonnull TreeSet<String> ips) {
         ArrayList<String> pub = new ArrayList<String>();
         ArrayList<String> priv = new ArrayList<String>();
 
         for (String ip : ips) {
             if (isPublic(ip)) {
                 pub.add(ip);
             } else {
                 priv.add(ip);
             }
         }
         vm.setPrivateIpAddresses(priv.toArray(new String[priv.size()]));
         vm.setPublicIpAddresses(pub.toArray(new String[pub.size()]));
     }
 
 
     private @Nullable ResourceStatus toStatus(@Nullable JSONObject object) throws CloudException, InternalException {
         if (object == null) {
             return null;
         }
         ProviderContext ctx = provider.getContext();
 
         if (ctx == null) {
             throw new NoContextException();
         }
         String regionId = ctx.getRegionId();
 
         if (regionId == null) {
             throw new CloudSigmaConfigurationException("No region was specified for this request");
         }
 
         try {
             String id = object.getString("uuid");
             if (id == null || id.equals("")) {
                 return null;
             }
     
             VmState state = VmState.PENDING;
     
             String status = object.getString("status");
             if (status != null) {
                 if (status.equalsIgnoreCase("stopped")) {
                     state = VmState.STOPPED;
                 } else if (status.equalsIgnoreCase("stopping")) {
                     state = VmState.STOPPING;
                 } else if (status.equalsIgnoreCase("started") || status.equalsIgnoreCase("running")) {
                     state = VmState.RUNNING;
                 } else if (status.equalsIgnoreCase("paused")) {
                     state = VmState.PAUSED;
                 } else if (status.equalsIgnoreCase("dead") || status.equalsIgnoreCase("dumped") || status.equalsIgnoreCase("unavailable")) {
                     state = VmState.TERMINATED;
                 } else if (status.startsWith("imaging")) {
                     state = VmState.PENDING;
                 } else {
                     logger.warn("DEBUG: Unknown CloudSigma server status: " + status);
                 }
             }
         return new ResourceStatus(id, state);
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private @Nullable VirtualMachine toVirtualMachine(@Nullable JSONObject object) throws CloudException, InternalException {
         if (object == null) {
             return null;
         }
         ProviderContext ctx = provider.getContext();
 
         if (ctx == null) {
             throw new NoContextException();
         }
         String regionId = ctx.getRegionId();
 
         if (regionId == null) {
             throw new CloudSigmaConfigurationException("No region was specified for this request");
         }
         VirtualMachine vm = new VirtualMachine();
 
         vm.setPersistent(true);
         vm.setCurrentState(VmState.PENDING);
         vm.setImagable(false);
         vm.setPausable(false);
         vm.setRebootable(false);
         vm.setPlatform(Platform.UNKNOWN);
         vm.setProviderDataCenterId(regionId + "-a");
         vm.setProviderRegionId(regionId);
         vm.setArchitecture(Architecture.I64);
         try {
             String id = object.getString("uuid");
 
             vm.setProviderVirtualMachineId(id);
             //dmayne 20130218: use JSON Parsing
             String imageId = "";
             JSONArray drives = null;
             if (object.has("drives")) {
                 drives = object.getJSONArray("drives");
                 for (int i = 0 ; i<drives.length(); i++) {
                     JSONObject jdrive = drives.getJSONObject(i);
                     if (jdrive.has("boot_order")) {
                         String boot_order = jdrive.getString("boot_order");
                         if (boot_order.equals("1")) {
                             JSONObject driveTag = jdrive.getJSONObject("drive");
                             imageId = driveTag.getString("uuid");
                             break;
                         }
                     }
                 }
             }
             if (imageId != null && !imageId.equals("")) {
                 vm.setProviderMachineImageId(imageId);
                 
                 //dmayne 20130524: try to get image os
                 logger.debug("Trying to establish the platform for "+imageId);
                 MachineImage image = provider.getComputeServices().getImageSupport().getImage(imageId);
                 Platform os = image.getPlatform();
                 vm.setPlatform(os);
                 logger.debug("Server os is "+vm.getPlatform());
             }
 
             String vlanId = null;
             JSONArray nics = null;
             if (object.has("nics")) {
                 nics = object.getJSONArray("nics");
                 for (int i = 0; i < nics.length(); i++) {
                     JSONObject jnic = nics.getJSONObject(i);
                     if (jnic.has("vlan") && !jnic.isNull("vlan")) {
                         JSONObject vlan = jnic.getJSONObject("vlan");
                         if (vlan != null) {
                             vlanId = vlan.getString("uuid");
                             break;
                         }
                     }
                 }
             }
             if (vlanId != null) {
                 vm.setProviderVlanId(vlanId);
             }
 
             if (drives != null) {
                 for (int i = 0; i< drives.length(); i++) {
                     JSONObject jDrive = drives.getJSONObject(i);
                     String devChannel = jDrive.getString("dev_channel");
                     JSONObject driveTag = jDrive.getJSONObject("drive");
                     String value = driveTag.getString("uuid");
                     if (value != null) {
                         String key = "virtio" + ":" + devChannel;
                         vm.setTag(key, value);
                     }
                 }
             }
 
             TreeSet<String> allIps = new TreeSet<String>();
             ArrayList<String> firewallIds = new ArrayList<String>();
             if (nics != null) {
                 for (int i=0; i < nics.length(); i++) {
                     //todo:dmayne 20130218: will a server ever have both ipv4 and ipv6?
                     JSONObject jnic = nics.getJSONObject(i);
 
                     if (jnic.has("ip_v4_conf") && !jnic.isNull("ip_v4_conf")) {
                         JSONObject ipv4 = jnic.getJSONObject("ip_v4_conf");
                         if (ipv4 != null) {
                             String ip4 = null;
                             if (ipv4.has("ip") && !ipv4.isNull("ip")) {
                                 JSONObject ipObj = ipv4.getJSONObject("ip");
                                 if (ipObj != null) {
                                     ip4 = ipObj.getString("uuid");
                                     if (ip4 != null && (!ip4.equalsIgnoreCase(""))) {
                                         allIps.add(ip4);
                                     }
                                 }
                             }
                             if (ipv4.has("conf")) {
                                 String conf4 = ipv4.getString("conf");
                                 if (conf4.equalsIgnoreCase("static")) {
                                     if (ip4 != null && !ip4.equals("") && !ip4.equals("auto")) {
                                         if (vm.getProviderAssignedIpAddressId() == null) {
                                             vm.setProviderAssignedIpAddressId(ip4);
                                         }
                                     }
                                 }
                             }
                         }
                     }
 
 
                     if (jnic.has("ip_v6_conf") && !jnic.isNull("ip_v6_conf")) {
                         JSONObject ipv6 = jnic.getJSONObject("ip_v6_conf");
                         if (ipv6 != null) {
                             String ip6 = null;
                             if (ipv6.has("ip") && !ipv6.isNull("ip")) {
                                 JSONObject ip6Obj = ipv6.getJSONObject("ip");
                                 if (ip6Obj != null) {
                                     ip6 = ip6Obj.getString("uuid");
                                     if (ip6 != null && (!ip6.equalsIgnoreCase(""))) {
                                         allIps.add(ip6);
                                     }
                                 }
                             }
                             if (ipv6.has("conf")) {
                                 String conf6 = ipv6.getString("conf");
                                 if (conf6.equalsIgnoreCase("static")) {
                                     if (ip6 != null && !ip6.equals("") && !ip6.equals("auto")) {
                                         if (vm.getProviderAssignedIpAddressId() == null) {
                                             vm.setProviderAssignedIpAddressId(ip6);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                     
                     //dmayne 20130524: check for runtime details to get dhcp ip address
                     logger.debug("Trying to get runtime ip info");
                     if (jnic.has("runtime") && !jnic.isNull("runtime")) {
                         JSONObject jRun = jnic.getJSONObject("runtime");
                         if (jRun.has("ip_v4") && !jRun.isNull("ip_v4")) {
                             JSONObject ipRun = jRun.getJSONObject("ip_v4");
                             String ip = ipRun.getString("uuid");
                             if (ip != null && (!ip.equalsIgnoreCase(""))) {
                                     allIps.add(ip);
                                 }
                         }
                         if (jRun.has("ip_v6") && !jRun.isNull("ip_v6")) {
                             JSONObject ipRun = jRun.getJSONObject("ip_v6");
                             String ip = ipRun.getString("uuid");
                             if (ip != null && (!ip.equalsIgnoreCase(""))) {
                                     allIps.add(ip);
                                 }
                         }
                     }
 
                     //check for firewall policy
                     if (jnic.has("firewall_policy") && !jnic.isNull("firewall_policy")) {
                         JSONObject fw = jnic.getJSONObject("firewall_policy");
                         if (fw.has("uuid") && !fw.isNull("uuid")) {
                             String firewall = fw.getString("uuid");
                             logger.debug("adding firewall policy "+firewall+" to server "+vm.getProviderVirtualMachineId());
                             firewallIds.add(firewall);
                         }
                     }
                 }
             }
             if (!allIps.isEmpty()) {
                 setIP(vm, allIps);
             }
 
             if (!firewallIds.isEmpty()) {
                 String[] vmFirewalls = new String[firewallIds.size()];
                 for (int i = 0; i<firewallIds.size(); i++) {
                     vmFirewalls[i] = firewallIds.get(i);
                 }
                 vm.setProviderFirewallIds(vmFirewalls);
             }
 
             JSONObject owner = object.getJSONObject("owner");
             String user = owner.getString("uuid");
             vm.setProviderOwnerId(user);
 
             String value = object.getString("name");
             vm.setName(value);
 
             JSONObject meta = object.getJSONObject("meta");
             if (meta != null && meta.has("description")) {
                 String description = meta.getString("description");
                 if (description != null && !description.equals("")) {
                     vm.setDescription(description);
                 }
             }
 
             value = object.getString("vnc_password");
             vm.setRootUser("root");
             vm.setRootPassword(value);
 
             String status = object.getString("status");
             if (status != null) {
                 if (status.equalsIgnoreCase("stopped")) {
                     vm.setCurrentState(VmState.STOPPED);
                 } else if (status.equalsIgnoreCase("stopping")) {
                     vm.setCurrentState(VmState.STOPPING);
                 } else if (status.equalsIgnoreCase("started") || status.equalsIgnoreCase("running")) {
                     vm.setCurrentState(VmState.RUNNING);
                 } else if (status.equalsIgnoreCase("paused")) {
                     vm.setCurrentState(VmState.PAUSED);
                 } else if (status.equalsIgnoreCase("dead") || status.equalsIgnoreCase("dumped") || status.equalsIgnoreCase("unavailable")) {
                     vm.setCurrentState(VmState.TERMINATED);
                 } else if (status.startsWith("imaging")) {
                     vm.setCurrentState(VmState.PENDING);
                 } else {
                     logger.warn("DEBUG: Unknown CloudSigma server status: " + status);
                 }
             } else {
                 vm.setCurrentState(VmState.PENDING);
             }
 
             String cpuCount = "1", cpuSpeed = "1000", ramInMB = "512", ramInBytes = "0";
 
             try {
                 String tmp = object.getString("smp");
                 if (tmp != null) {
                     cpuCount = String.valueOf(Integer.parseInt(tmp));
                 }
             } catch (NumberFormatException ignore) {
                 // ignore
             }
             try {
                 String tmp = object.getString("cpu");
                 if (tmp != null) {
                     cpuSpeed = String.valueOf(Integer.parseInt(tmp));
                 }
                 // we will be given total cpu speed but we need cpuPerSMP
                 cpuSpeed = String.valueOf(Integer.parseInt(cpuSpeed)/Integer.parseInt(cpuCount));
             } catch (NumberFormatException ignore) {
                 // ignore
             }
             try {
                 String tmp = object.getString("mem");
                 if (tmp != null) {
                     ramInBytes = String.valueOf(Long.parseLong(tmp));
                     ramInMB = String.valueOf(Long.parseLong(ramInBytes)/1024/1024);
                 }
             } catch (NumberFormatException ignore) {
                 // ignore
             }
             if (cpuCount.equals("1")) {
                 vm.setProductId(ramInMB + ":" + cpuSpeed);
             } else {
                 vm.setProductId(ramInMB + ":" + cpuSpeed + ":" + cpuCount);
             }
             if (vm.getProviderVirtualMachineId() == null) {
                 return null;
             }
             if (vm.getName() == null) {
                 vm.setName(vm.getProviderVirtualMachineId());
             }
             if (vm.getDescription() == null) {
                 vm.setDescription(vm.getName());
             }
             vm.setClonable(VmState.PAUSED.equals(vm.getCurrentState()));
             return vm;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private @Nonnull String toServerURL(@Nonnull String vmId, @Nonnull String action) throws InternalException {
         try {
             return ("/servers/" + URLEncoder.encode(vmId, "utf-8") + "/" + action);
         } catch (UnsupportedEncodingException e) {
             logger.error("UTF-8 not supported: " + e.getMessage());
             throw new InternalException(e);
         }
     }
 
     private @Nullable VirtualMachine waitForState(@Nonnull VirtualMachine vm, long timeoutPeriod, @Nonnull VmState... states) {
         long timeout = System.currentTimeMillis() + timeoutPeriod;
         VirtualMachine newVm = vm;
 
         while (timeout > System.currentTimeMillis()) {
             if (newVm == null) {
                 return null;
             }
             for (VmState state : states) {
                 if (state.equals(newVm.getCurrentState())) {
                     return newVm;
                 }
             }
             try {
                 Thread.sleep(15000L);
             } catch (InterruptedException ignore) {
             }
             try {
                 newVm = getVirtualMachine(vm.getProviderVirtualMachineId());
             } catch (Exception ignore) {
             }
         }
         return newVm;
     }
 }
