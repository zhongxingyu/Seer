 /**
  * Copyright (C) 2009-2013 Dell, Inc.
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
 
 package org.dasein.cloud.openstack.nova.os.compute;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.net.util.SubnetUtils;
 import org.apache.http.HttpStatus;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.compute.AbstractVMSupport;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.VMLaunchOptions;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VirtualMachineProduct;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.identity.IdentityServices;
 import org.dasein.cloud.identity.ShellKeySupport;
 import org.dasein.cloud.network.Firewall;
 import org.dasein.cloud.network.FirewallSupport;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.cloud.network.IpAddressSupport;
 import org.dasein.cloud.network.NetworkServices;
 import org.dasein.cloud.network.RawAddress;
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.network.VLANSupport;
 import org.dasein.cloud.network.Subnet;
 import org.dasein.cloud.openstack.nova.os.NovaException;
 import org.dasein.cloud.openstack.nova.os.NovaMethod;
 import org.dasein.cloud.openstack.nova.os.NovaOpenStack;
 import org.dasein.cloud.openstack.nova.os.OpenStackProvider;
 import org.dasein.cloud.openstack.nova.os.network.NovaNetworkServices;
 import org.dasein.cloud.openstack.nova.os.network.Quantum;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.cloud.util.Cache;
 import org.dasein.cloud.util.CacheLevel;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.uom.storage.Gigabyte;
 import org.dasein.util.uom.storage.Megabyte;
 import org.dasein.util.uom.storage.Storage;
 import org.dasein.util.uom.time.Day;
 import org.dasein.util.uom.time.TimePeriod;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Implements services supporting interaction with cloud virtual machines.
  * @author George Reese (george.reese@imaginary.com)
  * @version 2012.09 addressed issue with alternate security group lookup in some OpenStack environments (issue #1)
  * @version 2013.02 implemented setting kernel/ramdisk image IDs (see issue #40 in dasein-cloud-core)
  * @version 2013.02 updated with support for Dasein Cloud 2013.02 model
  * @version 2013.02 added support for fetching shell keys (issue #4)
  * @since unknown
  */
 public class NovaServer extends AbstractVMSupport<NovaOpenStack> {
     static private final Logger logger = NovaOpenStack.getLogger(NovaServer.class, "std");
 
     static public final String SERVICE = "compute";
 
     NovaServer(NovaOpenStack provider) {
         super(provider);
     }
 
     private @Nonnull String getTenantId() throws CloudException, InternalException {
         return ((NovaOpenStack)getProvider()).getAuthenticationContext().getTenantId();
     }
 
     @Override
     public @Nonnull String getConsoleOutput(@Nonnull String vmId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.getConsoleOutput");
         try {
             VirtualMachine vm = getVirtualMachine(vmId);
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             HashMap<String,Object> json = new HashMap<String,Object>();
 
             json.put("os-getConsoleOutput", new HashMap<String,Object>());
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             String console = method.postServersForString("/servers", vmId, new JSONObject(json), true);
 
             return (console == null ? "" : console);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nullable VirtualMachineProduct getProduct(@Nonnull String productId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.getProduct");
         try {
             for( VirtualMachineProduct product : listProducts(Architecture.I64) ) {
                 if( product.getProviderProductId().equals(productId) ) {
                     return product;
                 }
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForServer(@Nonnull Locale locale) {
         return "server";
     }
 
     @Override
     public @Nullable VirtualMachine getVirtualMachine(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.getVirtualMachine");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/servers", vmId, true);
 
             if( ob == null ) {
                 return null;
             }
             Iterable<IpAddress> ipv4, ipv6;
             Iterable<VLAN> networks;
 
             NetworkServices services = getProvider().getNetworkServices();
 
             if( services != null ) {
                 IpAddressSupport support = services.getIpAddressSupport();
 
                 if( support != null ) {
                     ipv4 = support.listIpPool(IPVersion.IPV4, false);
                     ipv6 = support.listIpPool(IPVersion.IPV6, false);
                 }
                 else {
                     ipv4 = ipv6 = Collections.emptyList();
                 }
 
                 VLANSupport vs = services.getVlanSupport();
 
                 if( vs != null ) {
                     networks = vs.listVlans();
                 }
                 else {
                     networks = Collections.emptyList();
                 }
             }
             else {
                 ipv4 = ipv6 = Collections.emptyList();
                 networks = Collections.emptyList();
             }
             try {
                 if( ob.has("server") ) {
                     JSONObject server = ob.getJSONObject("server");
                     VirtualMachine vm = toVirtualMachine(server, ipv4, ipv6, networks);
                         
                     if( vm != null ) {
                         return vm;
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("getVirtualMachine(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for servers");
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Requirement identifyImageRequirement(@Nonnull ImageClass cls) throws CloudException, InternalException {
         return (cls.equals(ImageClass.MACHINE) ? Requirement.REQUIRED : Requirement.OPTIONAL);
     }
 
     @Override
     public @Nonnull Requirement identifyPasswordRequirement(Platform platform) throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyRootVolumeRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyShellKeyRequirement(Platform platform) throws CloudException, InternalException {
         IdentityServices services = getProvider().getIdentityServices();
 
         if( services == null ) {
             return Requirement.NONE;
         }
         ShellKeySupport support = services.getShellKeySupport();
         if( support == null ) {
             return Requirement.NONE;
         }
         return Requirement.OPTIONAL;
     }
 
     @Override
     public @Nonnull Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyVlanRequirement() throws CloudException, InternalException {
         NetworkServices services = getProvider().getNetworkServices();
 
         if( services == null ) {
             return Requirement.NONE;
         }
         VLANSupport support = services.getVlanSupport();
 
         if( support == null || !support.isSubscribed() ) {
             return Requirement.NONE;
         }
         return Requirement.OPTIONAL;
     }
 
     @Override
     public boolean isAPITerminationPreventable() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isBasicAnalyticsSupported() throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public boolean isExtendedAnalyticsSupported() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.isSubscribed");
         try {
             return (getProvider().testContext() != null);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean isUserDataSupported() throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public @Nonnull VirtualMachine launch(@Nonnull VMLaunchOptions options) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.launch");
         try {
             MachineImage targetImage = getProvider().getComputeServices().getImageSupport().getImage(options.getMachineImageId());
 
             if( targetImage == null ) {
                 throw new CloudException("No such machine image: " + options.getMachineImageId());
             }
             HashMap<String,Object> wrapper = new HashMap<String,Object>();
             HashMap<String,Object> json = new HashMap<String,Object>();
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             json.put("name", options.getHostName());
             if( options.getUserData() != null ) {
                 try {
                     json.put("user_data", Base64.encodeBase64String(options.getUserData().getBytes("utf-8")));
                 }
                 catch( UnsupportedEncodingException e ) {
                     throw new InternalException(e);
                 }
             }
             if( ((NovaOpenStack)getProvider()).getMinorVersion() == 0 && ((NovaOpenStack)getProvider()).getMajorVersion() == 1 ) {
                 json.put("imageId", String.valueOf(options.getMachineImageId()));
                 json.put("flavorId", options.getStandardProductId());
             }
             else {
                 if( getProvider().getProviderName().equals("HP") ) {
                     json.put("imageRef", options.getMachineImageId());
                 }
                 else {
                     json.put("imageRef", ((NovaOpenStack)getProvider()).getComputeServices().getImageSupport().getImageRef(options.getMachineImageId()));
                 }
                 json.put("flavorRef", getFlavorRef(options.getStandardProductId()));
             }
             if( options.getVlanId() != null && ((NovaOpenStack)getProvider()).isRackspace() ) {
                 ArrayList<Map<String,Object>> vlans = new ArrayList<Map<String, Object>>();
                 HashMap<String,Object> vlan = new HashMap<String, Object>();
 
                 vlan.put("uuid", options.getVlanId());
                 vlans.add(vlan);
                 json.put("networks", vlans);
             }
             else {
                 if( options.getVlanId() != null && !((NovaOpenStack)getProvider()).isRackspace() ) {
                     NovaNetworkServices services = ((NovaOpenStack)getProvider()).getNetworkServices();
 
                     if( services != null ) {
                         Quantum support = services.getVlanSupport();
 
                         if( support != null ) {
                             ArrayList<Map<String,Object>> vlans = new ArrayList<Map<String, Object>>();
                             HashMap<String,Object> vlan = new HashMap<String, Object>();
 
                             try {
                                 vlan.put("port", support.createPort(options.getVlanId(), options.getHostName()));
                                 vlans.add(vlan);
                                 json.put("networks", vlans);
                             }
                             catch (CloudException e) {
                                 if (e.getHttpCode() != 403) {
                                     throw new CloudException(e.getMessage());
                                 }
 
                                 logger.warn("Unable to create port - trying to launch into general network");
                                 Subnet subnet = support.getSubnet(options.getVlanId());
 
                                 vlan.put("uuid", subnet.getProviderVlanId());
                                 vlans.add(vlan);
                                 json.put("networks", vlans);
                             }
                         }
                     }
                 }
 
             }
             if( options.getBootstrapKey() != null ) {
                 json.put("key_name", options.getBootstrapKey());
             }
             if( options.getFirewallIds().length > 0 ) {
                 ArrayList<HashMap<String,Object>> firewalls = new ArrayList<HashMap<String,Object>>();
 
                 for( String id : options.getFirewallIds() ) {
                     NetworkServices services = getProvider().getNetworkServices();
                     Firewall firewall = null;
 
                     if( services != null ) {
                         FirewallSupport support = services.getFirewallSupport();
 
                         if( support != null ) {
                             firewall = support.getFirewall(id);
                         }
                     }
                     if( firewall != null ) {
                         HashMap<String,Object> fw = new HashMap<String, Object>();
 
                         fw.put("name", firewall.getName());
                         firewalls.add(fw);
                     }
                 }
                 json.put("security_groups", firewalls);
             }
             if( !targetImage.getPlatform().equals(Platform.UNKNOWN) ) {
                 options.withMetaData("org.dasein.platform", targetImage.getPlatform().name());
             }
             options.withMetaData("org.dasein.description", options.getDescription());
             json.put("metadata", options.getMetaData());
             wrapper.put("server", json);
             JSONObject result = method.postServers("/servers", null, new JSONObject(wrapper), true);
 
             if( result.has("server") ) {
                 try {
                     Collection<IpAddress> ips = Collections.emptyList();
                     Collection<VLAN> nets = Collections.emptyList();
 
                     JSONObject server = result.getJSONObject("server");
                     VirtualMachine vm = toVirtualMachine(server, ips, ips, nets);
 
                     if( vm != null ) {
                         return vm;
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("launch(): Unable to understand launch response: " + e.getMessage());
                     if( logger.isTraceEnabled() ) {
                         e.printStackTrace();
                     }
                     throw new CloudException(e);
                 }
             }
             logger.error("launch(): No server was created by the launch attempt, and no error was returned");
             throw new CloudException("No virtual machine was launched");
 
         }
         finally {
             APITrace.end();
         }
     }
 
     private @Nonnull Iterable<String> listFirewalls(@Nonnull String vmId, @Nonnull JSONObject server) throws InternalException, CloudException {
         try {
             if( server.has("security_groups") ) {
                 NetworkServices services = getProvider().getNetworkServices();
                 Collection<Firewall> firewalls = null;
 
                 if( services != null ) {
                     FirewallSupport support = services.getFirewallSupport();
 
                     if( support != null ) {
                         firewalls = support.list();
                     }
                 }
                 if( firewalls == null ) {
                     firewalls = Collections.emptyList();
                 }
                 JSONArray groups = server.getJSONArray("security_groups");
                 ArrayList<String> results = new ArrayList<String>();
 
                 for( int i=0; i<groups.length(); i++ ) {
                     JSONObject group = groups.getJSONObject(i);
                     String id = group.has("id") ? group.getString("id") : null;
                     String name = group.has("name") ? group.getString("name") : null;
 
                     if( id != null || name != null  ) {
                         for( Firewall fw : firewalls ) {
                             if( id != null ) {
                                 if( id.equals(fw.getProviderFirewallId()) ) {
                                     results.add(id);
                                 }
                             }
                             else if( name.equals(fw.getName()) ) {
                                 results.add(fw.getProviderFirewallId());
                             }
                         }
                     }
                 }
                 return results;
             }
             else {
                 ArrayList<String> results = new ArrayList<String>();
 
                 NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
                JSONObject ob = method.getServers("/servers", vmId + "/os-security-groups", true);
 
                 if( ob != null ) {
 
                     if( ob.has("security_groups") ) {
                         JSONArray groups = ob.getJSONArray("security_groups");
 
                         for( int i=0; i<groups.length(); i++ ) {
                             JSONObject group = groups.getJSONObject(i);
 
                             if( group.has("id") ) {
                                 results.add(group.getString("id"));
                             }
                         }
                     }
                 }
                 return results;
             }
         }
         catch( JSONException e ) {
             throw new CloudException(e);
         }
     }
 
     @Override
     public @Nonnull Iterable<String> listFirewalls(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.listFirewalls");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/servers", vmId, true);
 
             if( ob == null ) {
                 return Collections.emptyList();
             }
             try {
                 if( ob.has("server") ) {
                     JSONObject server = ob.getJSONObject("server");
 
                     return listFirewalls(vmId, server);
                 }
                 throw new CloudException("No such server: " + vmId);
             }
             catch( JSONException e ) {
                 logger.error("listFirewalls(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for servers");
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     static public class FlavorRef {
         public String id;
         public String[][] links;
         VirtualMachineProduct product;
 
         public String toString() { return (id + " -> " + product); }
     }
 
     private @Nonnull Iterable<FlavorRef> listFlavors() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.listFlavors");
         try {
             Cache<FlavorRef> cache = Cache.getInstance(getProvider(), "flavorRefs", FlavorRef.class, CacheLevel.REGION_ACCOUNT, new TimePeriod<Day>(1, TimePeriod.DAY));
             Iterable<FlavorRef> refs = cache.get(getContext());
 
             if( refs != null ) {
                 return refs;
             }
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/flavors", null, true);
             ArrayList<FlavorRef> flavors = new ArrayList<FlavorRef>();
 
             try {
                 if( ob != null && ob.has("flavors") ) {
                     JSONArray list = ob.getJSONArray("flavors");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject p = list.getJSONObject(i);
                         FlavorRef ref = new FlavorRef();
 
                         if( p.has("id") ) {
                             ref.id = p.getString("id");
                         }
                         else {
                             continue;
                         }
                         if( p.has("links") ) {
                             JSONArray links = p.getJSONArray("links");
 
                             ref.links = new String[links.length()][];
                             for( int j=0; j<links.length(); j++ ) {
                                 JSONObject link = links.getJSONObject(j);
 
                                 ref.links[j] = new String[2];
                                 if( link.has("rel") ) {
                                     ref.links[j][0] = link.getString("rel");
                                 }
                                 if( link.has("href") ) {
                                     ref.links[j][1] = link.getString("href");
                                 }
                             }
                         }
                         else {
                             ref.links = new String[0][];
                         }
                         ref.product = toProduct(p);
                         if( ref.product != null ) {
                             flavors.add(ref);
                         }
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("listProducts(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for flavors: " + e.getMessage());
             }
             cache.put(getContext(), flavors);
             return flavors;
         }
         finally {
             APITrace.end();
         }
     }
 
     public @Nullable String getFlavorRef(@Nonnull String flavorId) throws InternalException, CloudException {
         for( FlavorRef ref : listFlavors() ) {
             if( ref.id.equals(flavorId) ) {
                 String def = null;
 
                 for( String[] link : ref.links ) {
                     if( link[0] != null && link[0].equals("self") && link[1] != null ) {
                         return link[1];
                     }
                     else if( def == null && link[1] != null ) {
                         def = link[1];
                     }
                 }
                 return def;
             }
         }
         return null;
     }
     
     @Override
     public @Nonnull Iterable<VirtualMachineProduct> listProducts(@Nonnull Architecture architecture) throws InternalException, CloudException {
         if( !architecture.equals(Architecture.I32) && !architecture.equals(Architecture.I64) ) {
             return Collections.emptyList();
         }
         APITrace.begin(getProvider(), "VM.listProducts");
         try {
             ArrayList<VirtualMachineProduct> products = new ArrayList<VirtualMachineProduct>();
 
             for( FlavorRef flavor : listFlavors() ) {
                 products.add(flavor.product);
             }
             return products;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public Iterable<Architecture> listSupportedArchitectures() throws InternalException, CloudException {
         ArrayList<Architecture> architectures = new ArrayList<Architecture>();
 
         architectures.add(Architecture.I32);
         architectures.add(Architecture.I64);
         return architectures;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listVirtualMachineStatus() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.listVirtualMachineStatus");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/servers", null, true);
             ArrayList<ResourceStatus> servers = new ArrayList<ResourceStatus>();
 
             try {
                 if( ob != null && ob.has("servers") ) {
                     JSONArray list = ob.getJSONArray("servers");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject server = list.getJSONObject(i);
                         ResourceStatus vm = toStatus(server);
 
                         if( vm != null ) {
                             servers.add(vm);
                         }
 
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("listVirtualMachines(): Unable to identify expected values in JSON: " + e.getMessage());                e.printStackTrace();
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for servers in " + ob.toString());
             }
             return servers;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<VirtualMachine> listVirtualMachines() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.listVirtualMachines");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/servers", null, true);
             ArrayList<VirtualMachine> servers = new ArrayList<VirtualMachine>();
 
             Iterable<IpAddress> ipv4 = Collections.emptyList(), ipv6 = Collections.emptyList();
             Iterable<VLAN> nets = Collections.emptyList();
             NetworkServices services = getProvider().getNetworkServices();
 
             if( services != null ) {
                 IpAddressSupport support = services.getIpAddressSupport();
 
                 if( support != null ) {
                     ipv4 = support.listIpPool(IPVersion.IPV4, false);
                     ipv6 = support.listIpPool(IPVersion.IPV6, false);
                 }
 
                 VLANSupport vs = services.getVlanSupport();
 
                 if( vs != null ) {
                     nets = vs.listVlans();
                 }
             }
             try {
                 if( ob != null && ob.has("servers") ) {
                     JSONArray list = ob.getJSONArray("servers");
                     
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject server = list.getJSONObject(i);
                         VirtualMachine vm = toVirtualMachine(server, ipv4, ipv6, nets);
                         
                         if( vm != null ) {
                             servers.add(vm);
                         }
                         
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("listVirtualMachines(): Unable to identify expected values in JSON: " + e.getMessage());                e.printStackTrace();
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for servers in " + ob.toString());
             }
             return servers;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void pause(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.pause");
         try {
             VirtualMachine vm = getVirtualMachine(vmId);
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             if( !supportsPauseUnpause(vm) ) {
                 throw new OperationNotSupportedException("Pause/unpause is not supported in " + getProvider().getCloudName());
             }
             HashMap<String,Object> json = new HashMap<String,Object>();
 
             json.put("pause", null);
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             method.postServers("/servers", vmId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void resume(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.resume");
         try {
             VirtualMachine vm = getVirtualMachine(vmId);
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             if( !supportsSuspendResume(vm) ) {
                 throw new OperationNotSupportedException("Suspend/resume is not supported in " + getProvider().getCloudName());
             }
             HashMap<String,Object> json = new HashMap<String,Object>();
 
             json.put("resume", null);
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             method.postServers("/servers", vmId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void start(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.start");
         try {
             VirtualMachine vm = getVirtualMachine(vmId);
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             if( !supportsStartStop(vm) ) {
                 throw new OperationNotSupportedException("Start/stop is not supported in " + getProvider().getCloudName());
             }
             HashMap<String,Object> json = new HashMap<String,Object>();
 
             json.put("os-start", null);
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             method.postServers("/servers", vmId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void stop(@Nonnull String vmId, boolean force) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.stop");
         try {
             VirtualMachine vm = getVirtualMachine(vmId);
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             if( !supportsStartStop(vm) ) {
                 throw new OperationNotSupportedException("Start/stop is not supported in " + getProvider().getCloudName());
             }
             HashMap<String,Object> json = new HashMap<String,Object>();
 
             json.put("os-stop", null);
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             method.postServers("/servers", vmId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void suspend(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.suspend");
         try {
             VirtualMachine vm = getVirtualMachine(vmId);
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             if( !supportsSuspendResume(vm) ) {
                 throw new OperationNotSupportedException("Suspend/resume is not supported in " + getProvider().getCloudName());
             }
             HashMap<String,Object> json = new HashMap<String,Object>();
 
             json.put("suspend", null);
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             method.postServers("/servers", vmId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void unpause(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.unpause");
         try {
             VirtualMachine vm = getVirtualMachine(vmId);
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             if( !supportsPauseUnpause(vm) ) {
                 throw new OperationNotSupportedException("Pause/unpause is not supported in " + getProvider().getCloudName());
             }
             HashMap<String,Object> json = new HashMap<String,Object>();
 
             json.put("unpause", null);
 
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             method.postServers("/servers", vmId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void reboot(@Nonnull String vmId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.reboot");
         try {
             HashMap<String,Object> json = new HashMap<String,Object>();
             HashMap<String,Object> action = new HashMap<String,Object>();
             
             action.put("type", "HARD");
             json.put("reboot", action);
 
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
             
             method.postServers("/servers", vmId, new JSONObject(json), true);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean supportsPauseUnpause(@Nonnull VirtualMachine vm) {
         return ((NovaOpenStack)getProvider()).getCloudProvider().supportsPauseUnpause(vm);
     }
 
     @Override
     public boolean supportsStartStop(@Nonnull VirtualMachine vm) {
         return ((NovaOpenStack)getProvider()).getCloudProvider().supportsStartStop(vm);
     }
 
     @Override
     public boolean supportsSuspendResume(@Nonnull VirtualMachine vm) {
         return ((NovaOpenStack)getProvider()).getCloudProvider().supportsSuspendResume(vm);
     }
 
     @Override
     public void terminate(@Nonnull String vmId, @Nullable String explanation) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.terminate");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             long timeout = System.currentTimeMillis() + CalendarWrapper.HOUR;
 
             do {
                 try {
                     method.deleteServers("/servers", vmId);
                     return;
                 }
                 catch( NovaException e ) {
                     if( e.getHttpCode() != HttpStatus.SC_CONFLICT ) {
                         throw e;
                     }
                 }
                 try { Thread.sleep(CalendarWrapper.MINUTE); }
                 catch( InterruptedException e ) { /* ignore */ }
             } while( System.currentTimeMillis() < timeout );
         }
         finally {
             APITrace.end();
         }
     }
     
     private @Nullable VirtualMachineProduct toProduct(@Nullable JSONObject json) throws JSONException, InternalException, CloudException {
         if( json == null ) {
             return null;
         }
         VirtualMachineProduct product = new VirtualMachineProduct();
 
         if( json.has("id") ) {
             product.setProviderProductId(json.getString("id"));
         }
         if( json.has("name") ) {
             product.setName(json.getString("name"));
         }
         if( json.has("description") ) {
             product.setDescription(json.getString("description"));
         }
         if( json.has("ram") ) {
             product.setRamSize(new Storage<Megabyte>(json.getInt("ram"), Storage.MEGABYTE));
         }
         if( json.has("disk") ) {
             product.setRootVolumeSize(new Storage<Gigabyte>(json.getInt("disk"), Storage.GIGABYTE));
         }
         product.setCpuCount(1);
         if( product.getProviderProductId() == null ) {
             return null;
         }
         if( product.getName() == null ) {
             product.setName(product.getProviderProductId());
         }
         if( product.getDescription() == null ) {
             product.setDescription(product.getName());
         }
         return product;
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable JSONObject server) throws JSONException, InternalException, CloudException {
         if( server == null ) {
             return null;
         }
         String serverId = null;
 
         if( server.has("id") ) {
             serverId = server.getString("id");
         }
         if( serverId == null ) {
             return null;
         }
 
         VmState state = VmState.PENDING;
 
         if( server.has("status") ) {
             String s = server.getString("status").toLowerCase();
 
             if( s.equals("active") ) {
                 state = VmState.RUNNING;
             }
             else if( s.equals("build") ) {
                 state = VmState.PENDING;
             }
             else if( s.equals("deleted") ) {
                 state = VmState.TERMINATED;
             }
             else if( s.equals("suspended") ) {
                 state = VmState.SUSPENDED;
             }
             else if( s.equalsIgnoreCase("paused") ) {
                 state = VmState.PAUSED;
             }
             else if( s.equalsIgnoreCase("stopped") || s.equalsIgnoreCase("shutoff")) {
                 state = VmState.STOPPED;
             }
             else if( s.equalsIgnoreCase("stopping") ) {
                 state = VmState.STOPPING;
             }
             else if( s.equalsIgnoreCase("pausing") ) {
                 state = VmState.PAUSING;
             }
             else if( s.equalsIgnoreCase("suspending") ) {
                 state = VmState.SUSPENDING;
             }
             else if( s.equals("error") ) {
                 return null;
             }
             else if( s.equals("reboot") || s.equals("hard_reboot") ) {
                 state = VmState.REBOOTING;
             }
             else {
                 logger.warn("toVirtualMachine(): Unknown server state: " + s);
                 state = VmState.PENDING;
             }
         }
         return new ResourceStatus(serverId, state);
     }
 
     private @Nullable VirtualMachine toVirtualMachine(@Nullable JSONObject server, @Nonnull Iterable<IpAddress> ipv4, @Nonnull Iterable<IpAddress> ipv6, @Nonnull Iterable<VLAN> networks) throws JSONException, InternalException, CloudException {
         if( server == null ) {
             return null;
         }
         VirtualMachine vm = new VirtualMachine();
         String description = null;
 
         vm.setCurrentState(VmState.RUNNING);
         vm.setArchitecture(Architecture.I64);
         vm.setClonable(false);
         vm.setCreationTimestamp(-1L);
         vm.setImagable(false);
         vm.setLastBootTimestamp(-1L);
         vm.setLastPauseTimestamp(-1L);
         vm.setPausable(false);
         vm.setPersistent(true);
         vm.setPlatform(Platform.UNKNOWN);
         vm.setRebootable(true);
         vm.setProviderOwnerId(getTenantId());
 
         if (getProvider().getCloudProvider().equals(OpenStackProvider.RACKSPACE) || getProvider().getCloudProvider().equals(OpenStackProvider.HP)) {
             vm.setPersistent(false);
         }
         if( server.has("id") ) {
             vm.setProviderVirtualMachineId(server.getString("id"));
         }
         if( server.has("name") ) {
             vm.setName(server.getString("name"));
         }
         if( server.has("description") && !server.isNull("description") ) {
             description = server.getString("description");
         }
         if( server.has("kernel_id") ) {
             vm.setProviderKernelImageId(server.getString("kernel_id"));
         }
         if( server.has("ramdisk_id") ) {
             vm.setProviderRamdiskImageId(server.getString("ramdisk_id"));
         }
         JSONObject md = (server.has("metadata") && !server.isNull("metadata")) ? server.getJSONObject("metadata") : null;
 
         HashMap<String,String> map = new HashMap<String,String>();
         boolean imaging = false;
 
         if( md != null ) {
             if( md.has("org.dasein.description") && vm.getDescription() == null ) {
                     description = md.getString("org.dasein.description");
             }
             else if( md.has("Server Label") ) {
                 description = md.getString("Server Label");
             }
             if( md.has("org.dasein.platform") ) {
                 try {
                     vm.setPlatform(Platform.valueOf(md.getString("org.dasein.platform")));
                 }
                 catch( Throwable ignore ) {
                     // ignore
                 }
             }
             String[] keys = JSONObject.getNames(md);
 
             if( keys != null ) {
                 for( String key : keys ) {
                     String value = md.getString(key);
 
                     if( value != null ) {
                         map.put(key, value);
                     }
                 }
             }
         }
         if( server.has("OS-EXT-STS:task_state") && !server.isNull("OS-EXT-STS:task_state") ) {
             String t = server.getString("OS-EXT-STS:task_state");
 
             map.put("OS-EXT-STS:task_state", t);
             imaging = t.equalsIgnoreCase("image_snapshot");
         }
         if( description == null ) {
             if( vm.getName() == null ) {
                 vm.setName(vm.getProviderVirtualMachineId());
             }
             vm.setDescription(vm.getName());
         }
         else {
             vm.setDescription(description);
         }
         if( server.has("hostId") ) {
             map.put("host", server.getString("hostId"));
         }
         vm.setTags(map);
         if( server.has("image") ) {
             JSONObject img = server.getJSONObject("image");
 
             if( img.has("id") ) {
                 vm.setProviderMachineImageId(img.getString("id"));
             }
         }
         if( server.has("flavor") ) {
             JSONObject f = server.getJSONObject("flavor");
 
             if( f.has("id") ) {
                 vm.setProductId(f.getString("id"));
             }
         }
         else if( server.has("flavorId") ) {
             vm.setProductId(server.getString("flavorId"));
         }
         if( server.has("adminPass") ) {
             vm.setRootPassword(server.getString("adminPass"));
         }
         if( server.has("key_name") ) {
             vm.setProviderShellKeyIds(server.getString("key_name"));
         }
         if( server.has("status") ) {
             String s = server.getString("status").toLowerCase();
 
             if( s.equals("active") ) {
                 vm.setCurrentState(VmState.RUNNING);
             }
             else if( s.startsWith("build") ) {
                 vm.setCurrentState(VmState.PENDING);
             }
             else if( s.equals("deleted") ) {
                 vm.setCurrentState(VmState.TERMINATED);
             }
             else if( s.equals("suspended") ) {
                 vm.setCurrentState(VmState.SUSPENDED);
             }
             else if( s.equalsIgnoreCase("paused") ) {
                 vm.setCurrentState(VmState.PAUSED);
             }
             else if( s.equalsIgnoreCase("stopped") || s.equalsIgnoreCase("shutoff")) {
                 vm.setCurrentState(VmState.STOPPED);
             }
             else if( s.equalsIgnoreCase("stopping") ) {
                 vm.setCurrentState(VmState.STOPPING);
             }
             else if( s.equalsIgnoreCase("pausing") ) {
                 vm.setCurrentState(VmState.PAUSING);
             }
             else if( s.equalsIgnoreCase("suspending") ) {
                 vm.setCurrentState(VmState.SUSPENDING);
             }
             else if( s.equals("error") ) {
                 return null;
             }
             else if( s.equals("reboot") || s.equals("hard_reboot") ) {
                 vm.setCurrentState(VmState.REBOOTING);
             }
             else {
                 logger.warn("toVirtualMachine(): Unknown server state: " + s);
                 vm.setCurrentState(VmState.PENDING);
             }
         }
         if( vm.getCurrentState().equals(VmState.RUNNING) && imaging ) {
             vm.setCurrentState(VmState.PENDING);
         }
         if( server.has("created") ) {
             vm.setCreationTimestamp(((NovaOpenStack)getProvider()).parseTimestamp(server.getString("created")));
         }
         if( server.has("addresses") ) {
             JSONObject addrs = server.getJSONObject("addresses");
             String[] names = JSONObject.getNames(addrs);
 
             if( names != null && names.length > 0 ) {
                 ArrayList<RawAddress> pub = new ArrayList<RawAddress>();
                 ArrayList<RawAddress> priv = new ArrayList<RawAddress>();
 
                 for( String name : names ) {
                     JSONArray arr = addrs.getJSONArray(name);
 
                     String subnet = null;
                     for( int i=0; i<arr.length(); i++ ) {
                         RawAddress addr = null;
 
                         if( ((NovaOpenStack)getProvider()).getMinorVersion() == 0 && ((NovaOpenStack)getProvider()).getMajorVersion() == 1 ) {
                             addr = new RawAddress(arr.getString(i).trim(), IPVersion.IPV4);
                         }
                         else {
                             JSONObject a = arr.getJSONObject(i);
 
                             if( a.has("version") && a.getInt("version") == 4 && a.has("addr") ) {
                                 subnet = a.getString("addr");
                                 addr = new RawAddress(a.getString("addr"), IPVersion.IPV4);
                             }
                             else if( a.has("version") && a.getInt("version") == 6 && a.has("addr") ) {
                                 subnet = a.getString("addr");
                                 addr = new RawAddress(a.getString("addr"), IPVersion.IPV6);
                             }
                         }
                         if( addr != null ) {
                             if( addr.isPublicIpAddress() ) {
                                 pub.add(addr);
                             }
                             else {
                                 priv.add(addr);
                             }
                         }
                     }
                     if( vm.getProviderVlanId() == null && !name.equals("public") && !name.equals("private") && !name.equals("nova_fixed") ) {
                         for( VLAN network : networks ) {
                             if( network.getName().equals(name) ) {
                                 vm.setProviderVlanId(network.getProviderVlanId());
                                 //get subnet
                                 NetworkServices services = getProvider().getNetworkServices();
                                 VLANSupport support = services.getVlanSupport();
                                 Iterable<Subnet> subnets = support.listSubnets(network.getProviderVlanId());
                                 for (Subnet sub : subnets) {
                                     SubnetUtils utils = new SubnetUtils(sub.getCidr());
 
                                     if (utils.getInfo().isInRange(subnet)) {
                                         vm.setProviderSubnetId(sub.getProviderSubnetId());
                                         break;
                                     }
                                 }
                                 break;
                             }
                         }
                     }
                 }
                 vm.setPublicAddresses(pub.toArray(new RawAddress[pub.size()]));
                 vm.setPrivateAddresses(priv.toArray(new RawAddress[priv.size()]));
             }
             RawAddress[] raw = vm.getPublicAddresses();
 
             if( raw != null ) {
                 for( RawAddress addr : vm.getPublicAddresses() ) {
                     if( addr.getVersion().equals(IPVersion.IPV4) ) {
                         for( IpAddress a : ipv4 ) {
                             if( a.getRawAddress().getIpAddress().equals(addr.getIpAddress()) ) {
                                 vm.setProviderAssignedIpAddressId(a.getProviderIpAddressId());
                                 break;
                             }
                         }
                     }
                     else if( addr.getVersion().equals(IPVersion.IPV6) ) {
                         for( IpAddress a : ipv6 ) {
                             if( a.getRawAddress().getIpAddress().equals(addr.getIpAddress()) ) {
                                 vm.setProviderAssignedIpAddressId(a.getProviderIpAddressId());
                                 break;
                             }
                         }
                     }
                 }
             }
             if( vm.getProviderAssignedIpAddressId() == null ) {
                 for( IpAddress addr : ipv4 ) {
                     String serverId = addr.getServerId();
 
                     if( serverId != null && serverId.equals(vm.getProviderVirtualMachineId()) ) {
                         vm.setProviderAssignedIpAddressId(addr.getProviderIpAddressId());
                         break;
                     }
                 }
                 if( vm.getProviderAssignedIpAddressId() == null ) {
                     for( IpAddress addr : ipv6 ) {
                         String serverId = addr.getServerId();
 
                         if( serverId != null && addr.getServerId().equals(vm.getProviderVirtualMachineId()) ) {
                             vm.setProviderAssignedIpAddressId(addr.getProviderIpAddressId());
                             break;
                         }
                     }
                 }
             }
             vm.setProviderRegionId(getContext().getRegionId());
             vm.setProviderDataCenterId(vm.getProviderRegionId() + "-a");
             vm.setTerminationTimestamp(-1L);
             if( vm.getProviderVirtualMachineId() == null ) {
                 return null;
             }
             if( vm.getProviderAssignedIpAddressId() == null ) {
                 for( IpAddress addr : ipv6 ) {
                     if( addr.getServerId().equals(vm.getProviderVirtualMachineId()) ) {
                         vm.setProviderAssignedIpAddressId(addr.getProviderIpAddressId());
                         break;
                     }
                 }
             }
         }
         vm.setProviderRegionId(getContext().getRegionId());
         vm.setProviderDataCenterId(vm.getProviderRegionId() + "-a");
         vm.setTerminationTimestamp(-1L);
         if( vm.getProviderVirtualMachineId() == null ) {
             return null;
         }
         if( vm.getName() == null ) {
             vm.setName(vm.getProviderVirtualMachineId());
         }
         if( vm.getDescription() == null ) {
             vm.setDescription(vm.getName());
         }
         vm.setImagable(vm.getCurrentState().equals(VmState.RUNNING));
         vm.setRebootable(vm.getCurrentState().equals(VmState.RUNNING));
         if( vm.getPlatform().equals(Platform.UNKNOWN) ) {
             Platform p = Platform.guess(vm.getName() + " " + vm.getDescription());
 
             if( p.equals(Platform.UNKNOWN) ) {
                 MachineImage img = getProvider().getComputeServices().getImageSupport().getImage(vm.getProviderMachineImageId());
 
                 if( img != null ) {
                     p = img.getPlatform();
                 }
             }
             vm.setPlatform(p);
         }
         if (getProvider().getProviderName().equalsIgnoreCase("RACKSPACE")){
             //Rackspace does not support the concept for firewalls in servers
         	vm.setProviderFirewallIds(null);
         }
         else{
             Iterable<String> fwIds = listFirewalls(vm.getProviderVirtualMachineId(), server);
             int count = 0;
 
             //noinspection UnusedDeclaration
             for( String id : fwIds ) {
                 count++;
             }
             String[] ids = new String[count];
             int i = 0;
 
             for( String id : fwIds ) {
                 ids[i++] = id;
             }
             vm.setProviderFirewallIds(ids);
         }
         return vm;
     }
 
 }
