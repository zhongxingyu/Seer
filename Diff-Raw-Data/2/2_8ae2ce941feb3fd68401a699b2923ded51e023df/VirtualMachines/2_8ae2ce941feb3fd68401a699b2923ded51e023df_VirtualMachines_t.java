 /**
  * Copyright (C) 2009-2013 enstratius, Inc.
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
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.Callable;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.Tag;
 import org.dasein.cloud.cloudstack.CSCloud;
 import org.dasein.cloud.cloudstack.CSException;
 import org.dasein.cloud.cloudstack.CSMethod;
 import org.dasein.cloud.cloudstack.CSServiceProvider;
 import org.dasein.cloud.cloudstack.CSTopology;
 import org.dasein.cloud.cloudstack.CSVersion;
 import org.dasein.cloud.cloudstack.Param;
 import org.dasein.cloud.cloudstack.network.Network;
 import org.dasein.cloud.cloudstack.network.SecurityGroup;
 import org.dasein.cloud.compute.*;
 import org.dasein.cloud.network.RawAddress;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.uom.storage.Gigabyte;
 import org.dasein.util.uom.storage.Megabyte;
 import org.dasein.util.uom.storage.Storage;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.logicblaze.lingo.util.DefaultTimeoutMap;
 
 public class VirtualMachines extends AbstractVMSupport {
     static public final Logger logger = Logger.getLogger(VirtualMachines.class);
     
     static private final String DEPLOY_VIRTUAL_MACHINE  = "deployVirtualMachine";
     static private final String DESTROY_VIRTUAL_MACHINE = "destroyVirtualMachine";
     static private final String GET_VIRTUAL_MACHINE_PASSWORD = "getVMPassword";
     static private final String LIST_VIRTUAL_MACHINES   = "listVirtualMachines";
     static private final String LIST_SERVICE_OFFERINGS  = "listServiceOfferings";
     static private final String REBOOT_VIRTUAL_MACHINE  = "rebootVirtualMachine";
     static private final String RESET_VIRTUAL_MACHINE_PASSWORD = "resetPasswordForVirtualMachine";
     static private final String START_VIRTUAL_MACHINE   = "startVirtualMachine";
     static private final String STOP_VIRTUAL_MACHINE    = "stopVirtualMachine";
     
     static private Properties                              cloudMappings;
     static private Map<String,Map<String,String>>          customNetworkMappings;
     static private Map<String,Map<String,Set<String>>>     customServiceMappings; 
     
     static private DefaultTimeoutMap productCache = new DefaultTimeoutMap();
     
     private CSCloud provider;
     
     public VirtualMachines(CSCloud provider) {
         super(provider);
         this.provider = provider;
     }
 
     @Override
     public int getCostFactor(@Nonnull VmState state) throws InternalException, CloudException {
         return 100;
     }
 
     @Override
     public int getMaximumVirtualMachineCount() throws CloudException, InternalException {
         return -2;
     }
 
     @Override 
     public @Nullable VirtualMachineProduct getProduct(@Nonnull String productId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.getProduct");
         try {
             for( Architecture architecture : Architecture.values() ) {
                 for( VirtualMachineProduct product : listProducts(architecture) ) {
                     if( product.getProviderProductId().equals(productId) ) {
                         return product;
                     }
                 }
             }
             if( logger.isDebugEnabled() ) {
                 logger.debug("Unknown product ID for cloud.com: " + productId);
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
     
     @Override
     public @Nonnull String getProviderTermForServer(@Nonnull Locale locale) {
         return "virtual machine";
     }
 
     private String getRootPassword(@Nonnull String serverId) throws CloudException, InternalException {
        APITrace.begin(getProvider(), "VM.getPassword");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was specified for this request");
             }
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(GET_VIRTUAL_MACHINE_PASSWORD, new Param("id", serverId)), GET_VIRTUAL_MACHINE_PASSWORD);
 
             if (doc != null){
                 NodeList matches = doc.getElementsByTagName("getvmpasswordresponse");
 
                 for( int i=0; i<matches.getLength(); i++ ) {
                     Node node = matches.item(i);
 
                     if( node != null ) {
                         NodeList attributes = node.getChildNodes();
                         for( int j=0; j<attributes.getLength(); j++ ) {
                             Node attribute = attributes.item(j);
                             String name = attribute.getNodeName().toLowerCase();
                             String value;
 
                             if( attribute.getChildNodes().getLength() > 0 ) {
                                 value = attribute.getFirstChild().getNodeValue();
                             }
                             else {
                                 value = null;
                             }
                             if( name.equals("password") ) {
                                 NodeList nodes = attribute.getChildNodes();
                                 for( int k=0; k<nodes.getLength(); k++ ) {
                                     Node password = nodes.item(k);
                                     name = password.getNodeName().toLowerCase();
 
                                     if( password.getChildNodes().getLength() > 0 ) {
                                         value = password.getFirstChild().getNodeValue();
                                     }
                                     else {
                                         value = null;
                                     }
                                     if( name.equals("encryptedpassword") ) {
                                         return value;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             logger.warn("Unable to find password for vm with id "+serverId);
             return null;
         }
         catch (CSException e) {
             if (e.getHttpCode() == 431) {
                 logger.warn("No password found for vm "+serverId);
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nullable VirtualMachine getVirtualMachine(@Nonnull String serverId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.getVirtualMachine");
         try {
 
             CSMethod method = new CSMethod(provider);
 
             try {
                 Document doc = method.get(method.buildUrl(LIST_VIRTUAL_MACHINES, new Param("id", serverId)), LIST_VIRTUAL_MACHINES);
                 NodeList matches = doc.getElementsByTagName("virtualmachine");
 
                 if( matches.getLength() < 1 ) {
                     return null;
                 }
                 for( int i=0; i<matches.getLength(); i++ ) {
                     VirtualMachine s = toVirtualMachine(matches.item(i));
 
                     if( s != null && s.getProviderVirtualMachineId().equals(serverId) ) {
                         return s;
                     }
                 }
             }
             catch( CloudException e ) {
                 if( e.getMessage().contains("does not exist") ) {
                     return null;
                 }
                 throw e;
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Requirement identifyImageRequirement(@Nonnull ImageClass cls) throws CloudException, InternalException {
         return (cls.equals(ImageClass.MACHINE) ? Requirement.REQUIRED : Requirement.NONE);
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
         return Requirement.OPTIONAL;
     }
 
     @Override
     public @Nonnull Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyVlanRequirement() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.identifyVlanRequirement");
         try {
             if( provider.getServiceProvider().equals(CSServiceProvider.DATAPIPE) ) {
                 return Requirement.NONE;
             }
             if( provider.getVersion().greaterThan(CSVersion.CS21) ) {
                 ProviderContext ctx = provider.getContext();
 
                 if( ctx == null ) {
                      throw new CloudException("No context was set for this request");
                 }
                 String regionId = ctx.getRegionId();
 
                 if( regionId == null ) {
                     throw new CloudException("No region was set for this request");
                 }
                 return (provider.getDataCenterServices().requiresNetwork(regionId) ? Requirement.REQUIRED : Requirement.OPTIONAL);
             }
             return Requirement.OPTIONAL;
         }
         finally {
             APITrace.end();
         }
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
         APITrace.begin(getProvider(), "VM.isSubscribed");
         try {
             CSMethod method = new CSMethod(provider);
 
             try {
                 method.get(method.buildUrl(CSTopology.LIST_ZONES, new Param("available", "true")), CSTopology.LIST_ZONES);
                 return true;
             }
             catch( CSException e ) {
                 int code = e.getHttpCode();
 
                 if( code == HttpServletResponse.SC_FORBIDDEN || code == 401 || code == 531 ) {
                     return false;
                 }
                 throw e;
             }
             catch( CloudException e ) {
                 int code = e.getHttpCode();
 
                 if( code == HttpServletResponse.SC_FORBIDDEN || code == HttpServletResponse.SC_UNAUTHORIZED ) {
                     return false;
                 }
                 throw e;
             }
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
     public @Nonnull VirtualMachine launch(@Nonnull VMLaunchOptions withLaunchOptions) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.launch");
         try {
             String id = withLaunchOptions.getStandardProductId();
 
             VirtualMachineProduct product = getProduct(id);
 
             if( product == null ) {
                 throw new CloudException("Invalid product ID: " + id);
             }
             if( provider.getVersion().greaterThan(CSVersion.CS21) ) {
                 return launch22(withLaunchOptions.getMachineImageId(), product,  withLaunchOptions.getDataCenterId(), withLaunchOptions.getFriendlyName(), withLaunchOptions.getBootstrapKey(), withLaunchOptions.getVlanId(), withLaunchOptions.getFirewallIds(), withLaunchOptions.getUserData());
             }
             else {
                 return launch21(withLaunchOptions.getMachineImageId(), product, withLaunchOptions.getDataCenterId(), withLaunchOptions.getFriendlyName());
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     @Deprecated
     @SuppressWarnings("deprecation")
     public @Nonnull VirtualMachine launch(@Nonnull String imageId, @Nonnull VirtualMachineProduct product, @Nonnull String inZoneId, @Nonnull String name, @Nonnull String description, @Nullable String usingKey, @Nullable String withVlanId, boolean withMonitoring, boolean asSandbox, @Nullable String[] protectedByFirewalls, @Nullable Tag ... tags) throws InternalException, CloudException {
         if( provider.getVersion().greaterThan(CSVersion.CS21) ) {
             StringBuilder userData = new StringBuilder();
             
             if( tags != null && tags.length > 0 ) {
                 for( Tag tag : tags ) {
                     userData.append(tag.getKey());
                     userData.append("=");
                     userData.append(tag.getValue());
                     userData.append("\n");
                 }
             }
             else {
                 userData.append("created=Dasein Cloud\n");
             }
             return launch22(imageId, product, inZoneId, name, usingKey, withVlanId, protectedByFirewalls, userData.toString());
         }
         else {
             return launch21(imageId, product, inZoneId, name);
         }
     }
     
     private VirtualMachine launch21(String imageId, VirtualMachineProduct product, String inZoneId, String name) throws InternalException, CloudException {
         CSMethod method = new CSMethod(provider);
         
         return launch(method.get(method.buildUrl(DEPLOY_VIRTUAL_MACHINE, new Param("zoneId", getContext().getRegionId()), new Param("serviceOfferingId", product.getProviderProductId()), new Param("templateId", imageId), new Param("displayName", name) ), DEPLOY_VIRTUAL_MACHINE));
     }
     
     private void load() {
         try {
             InputStream input = VirtualMachines.class.getResourceAsStream("/cloudMappings.cfg");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             Properties properties = new Properties();
             String line;
             
             while( (line = reader.readLine()) != null ) {
                 if( line.startsWith("#") ) {
                     continue;
                 }
                 int idx = line.indexOf('=');
                 if( idx < 0 || line.endsWith("=") ) {
                     continue;
                 }
                 String cloudUrl = line.substring(0, idx);
                 String cloudId = line.substring(idx+1);
                 properties.put(cloudUrl, cloudId);
             }
             cloudMappings = properties;
         }
         catch( Throwable ignore ) {
             // ignore
         }        
         try {
             InputStream input = VirtualMachines.class.getResourceAsStream("/customNetworkMappings.cfg");
             HashMap<String,Map<String,String>> mapping = new HashMap<String,Map<String,String>>(); 
             Properties properties = new Properties();
             
             properties.load(input);
             for( Object key : properties.keySet() ) {
                 String[] trueKey = ((String)key).split(",");
                 Map<String,String> current = mapping.get(trueKey[0]);
                 
                 if( current == null ) {
                     current = new HashMap<String,String>();
                     mapping.put(trueKey[0], current);
                 }
                 current.put(trueKey[1], (String)properties.get(key));
             }
             customNetworkMappings = mapping;
         }
         catch( Throwable ignore ) {
             // ignore
         }
         try {
             InputStream input = VirtualMachines.class.getResourceAsStream("/customServiceMappings.cfg");
             HashMap<String,Map<String,Set<String>>> mapping = new HashMap<String,Map<String,Set<String>>>();
             Properties properties = new Properties();
             
             properties.load(input);
             
             for( Object key : properties.keySet() ) {
                 String value = (String)properties.get(key);
                 
                 if( value != null ) {
                     String[] trueKey = ((String)key).split(",");
                     Map<String,Set<String>> tmp = mapping.get(trueKey[0]);
                     
                     if( tmp == null ) {
                         tmp =new HashMap<String,Set<String>>();
                         mapping.put(trueKey[0], tmp);
                     }
                     TreeSet<String> m = new TreeSet<String>();
                     String[] offerings = value.split(",");
                     
                     if( offerings == null || offerings.length < 1 ) {
                         m.add(value);
                     }
                     else {
                         Collections.addAll(m, offerings);
                     }
                     tmp.put(trueKey[1], m);
                 }
             }
             customServiceMappings = mapping;
         }
         catch( Throwable ignore ) {
             // ignore
         }
     }
     
     private @Nonnull VirtualMachine launch22(@Nonnull String imageId, @Nonnull VirtualMachineProduct product, @Nullable String inZoneId, @Nonnull String name, @Nullable String withKeypair, @Nullable String targetVlanId, @Nullable String[] protectedByFirewalls, @Nullable String userData) throws InternalException, CloudException {
         ProviderContext ctx = provider.getContext();
         List<String> vlans = null;
 
         if( ctx == null ) {
             throw new InternalException("No context was provided for this request");
         }
         String regionId = ctx.getRegionId();
 
         if( regionId == null ) {
             throw new InternalException("No region is established for this request");
         }
         String prdId = product.getProviderProductId();
 
         if( customNetworkMappings == null ) {
             load();
         }
         if( customNetworkMappings != null ) {
             String cloudId = cloudMappings.getProperty(ctx.getEndpoint());
             
             if( cloudId != null ) {
                 Map<String,String> map = customNetworkMappings.get(cloudId);
                 
                 if( map != null ) {
                     String id = map.get(prdId);
                     
                     if( id != null ) {
                         targetVlanId = id;
                     }
                 }
             }
         }
         if( targetVlanId != null && targetVlanId.length() < 1 ) {
             targetVlanId = null;
         }
         if( userData == null ) {
             userData = "";
         }
         String securityGroupIds = null;
         Param[] params;
         
         if( protectedByFirewalls != null && protectedByFirewalls.length > 0 ) {
             StringBuilder str = new StringBuilder();
             int idx = 0;
             
             for( String fw : protectedByFirewalls ) {
                 fw = fw.trim();
                 if( !fw.equals("") ) {
                     str.append(fw);
                     if( (idx++) < protectedByFirewalls.length-1 ) {
                         str.append(",");
                     }
                 }
             }
             securityGroupIds = str.toString();
         }
         int count = 4;
 
         if( userData != null && userData.length() > 0 ) {
             count++;
         }
         if( withKeypair != null ) {
             count++;
         }
         if( targetVlanId == null ) {
             Network vlan = provider.getNetworkServices().getVlanSupport();
             
             if( vlan != null && vlan.isSubscribed() ) {
                 if( provider.getDataCenterServices().requiresNetwork(regionId) ) {
                     vlans = vlan.findFreeNetworks();
                 }
             }
         }
         else {
             vlans = new ArrayList<String>();
             vlans.add(targetVlanId);
         }
         if( vlans != null && vlans.size() > 0 ) {
             count++;
         }
         if( securityGroupIds != null && securityGroupIds.length() > 0 ) {
             if( !provider.getServiceProvider().equals(CSServiceProvider.DATAPIPE) && !provider.getDataCenterServices().supportsSecurityGroups(regionId, vlans == null || vlans.size() < 1) ) {
                 securityGroupIds = null;
             }
             else {
                 count++;
             }
         }
         else if( provider.getDataCenterServices().supportsSecurityGroups(regionId, vlans == null || vlans.size() < 1) ) {
             /*
             String sgId = null;
             
             if( withVlanId == null ) {
                 Collection<Firewall> firewalls = provider.getNetworkServices().getFirewallSupport().list();
                 
                 for( Firewall fw : firewalls ) {
                     if( fw.getName().equalsIgnoreCase("default") && fw.getProviderVlanId() == null ) {
                         sgId = fw.getProviderFirewallId();
                         break;
                     }
                 }
                 if( sgId == null ) {
                     try {
                         sgId = provider.getNetworkServices().getFirewallSupport().create("default", "Default security group");
                     }
                     catch( Throwable t ) {
                         logger.warn("Unable to create a default security group, gonna try anyways: " + t.getMessage());
                     }
                 }
                 if( sgId != null ) {
                     securityGroupIds = sgId;
                 }
             }
             else {
                 Collection<Firewall> firewalls = provider.getNetworkServices().getFirewallSupport().list();
                 
                 for( Firewall fw : firewalls ) {
                     if( (fw.getName().equalsIgnoreCase("default") || fw.getName().equalsIgnoreCase("default-" + withVlanId)) && withVlanId.equals(fw.getProviderVlanId()) ) {
                         sgId = fw.getProviderFirewallId();
                         break;
                     }
                 }
                 if( sgId == null ) {
                     try {
                         sgId = provider.getNetworkServices().getFirewallSupport().createInVLAN("default-" + withVlanId, "Default " + withVlanId + " security group", withVlanId);
                     }
                     catch( Throwable t ) {
                         logger.warn("Unable to create a default security group, gonna try anyways: " + t.getMessage());
                     }
                 }
             }
             if( sgId != null ) {
                 securityGroupIds = sgId;
                 count++;
             }    
             */            
         }
         params = new Param[count];
         params[0] = new Param("zoneId", getContext().getRegionId());
         params[1] = new Param("serviceOfferingId", prdId);
         params[2] = new Param("templateId", imageId);
         params[3] = new Param("displayName", name);
         int i = 4;
         if( userData != null && userData.length() > 0 ) {
             try {
                 params[i++] = new Param("userdata", new String(Base64.encodeBase64(userData.getBytes("utf-8")), "utf-8"));
             }
             catch( UnsupportedEncodingException e ) {
                 e.printStackTrace();
             }
         }
         if( withKeypair != null ) {
             params[i++] = new Param("keypair", withKeypair);
         }
         if( securityGroupIds != null && securityGroupIds.length() > 0 ) {
             params[i++] = new Param("securitygroupids", securityGroupIds);
         }
         if( vlans != null && vlans.size() > 0 ) {
             CloudException lastError = null;
 
             for( String withVlanId : vlans ) {
                 params[i] = new Param("networkIds", withVlanId);
 
                 try {
                     CSMethod method = new CSMethod(provider);
 
                     return launch(method.get(method.buildUrl(DEPLOY_VIRTUAL_MACHINE, params), DEPLOY_VIRTUAL_MACHINE));
                 }
                 catch( CloudException e ) {
                     if( e.getMessage().contains("sufficient address capacity") ) {
                         lastError = e;
                         continue;
                     }
                     throw e;
                 }
             }
             if( lastError == null ) {
                 throw lastError;
             }
             throw new CloudException("Unable to identify a network into which a VM can be launched");
         }
         else {
             CSMethod method = new CSMethod(provider);
 
             return launch(method.get(method.buildUrl(DEPLOY_VIRTUAL_MACHINE, params), DEPLOY_VIRTUAL_MACHINE));
         }
     }
     
     private @Nonnull VirtualMachine launch(@Nonnull Document doc) throws InternalException, CloudException {
         NodeList matches = doc.getElementsByTagName("deployvirtualmachineresponse");
         String serverId = null;
         String jobId = null;
         
         for( int i=0; i<matches.getLength(); i++ ) {
             NodeList attrs = matches.item(i).getChildNodes();
             
             for( int j=0; j<attrs.getLength(); j++ ) {
                 Node node = attrs.item(j);
                 
                 if( node != null && (node.getNodeName().equalsIgnoreCase("virtualmachineid") || node.getNodeName().equalsIgnoreCase("id")) ) {
                     serverId = node.getFirstChild().getNodeValue();
                     break;
                 }
                 else if (node != null && node.getNodeName().equalsIgnoreCase("jobid") ) {
                     jobId = node.getFirstChild().getNodeValue();
                 }
             }
             if( serverId != null ) {
                 break;
             }
         }
         if( serverId == null && jobId == null ) {
             throw new CloudException("Could not launch server");
         }
         // TODO: very odd logic below; figure out what it thinks it is doing
         
         VirtualMachine vm = null;
         
         if (serverId == null) {
             //only wait for job if we don't already have the resource id
             Document responseDoc = provider.waitForJob(doc, "Launch Server");
         
             //parse vm from job completion response to capture vm passwords on initial launch.
             if (responseDoc != null){
                 NodeList nodeList = responseDoc.getElementsByTagName("virtualmachine");
                 if (nodeList.getLength() > 0) {
                     Node virtualMachine = nodeList.item(0);
                     vm = toVirtualMachine(virtualMachine);
                     if( vm != null ) {
                         return vm;
                     }
                 }
             }
         }
         
         if (vm == null){
             long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE*20);
 	        while( System.currentTimeMillis() < timeout ) {
 	            try { vm = getVirtualMachine(serverId); }
 	            catch( Throwable ignore ) {  }
 	            if( vm != null ) {
                     return vm;
 	            }
 	            try { Thread.sleep(5000L); }
 	            catch( InterruptedException ignore ) { }
 	        }
         }
         vm = getVirtualMachine(serverId);
         if( vm == null ) {
             throw new CloudException("No virtual machine provided: " + serverId);
         }
         return vm;
     }
 
     @Override
     public @Nonnull Iterable<String> listFirewalls(@Nonnull String vmId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.listFirewalls");
         try {
             SecurityGroup support = provider.getNetworkServices().getFirewallSupport();
 
             if( support == null ) {
                 return Collections.emptyList();
             }
             return support.listFirewallsForVM(vmId);
         }
         finally {
             APITrace.end();
         }
     }
 
     private void setFirewalls(@Nonnull VirtualMachine vm) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.setFirewalls");
         try {
             SecurityGroup support = provider.getNetworkServices().getFirewallSupport();
 
             if( support == null ) {
                 return;
             }
             ArrayList<String> ids = new ArrayList<String>();
 
             Iterable<String> firewalls;
             try {
                 firewalls = support.listFirewallsForVM(vm.getProviderVirtualMachineId());
             } catch (Throwable t) {
                 logger.error("Problem listing firewalls (listSecurityGroups) for '" + vm.getProviderVirtualMachineId() + "': " + t.getMessage());
                 return;
             }
 
             for( String id : firewalls ) {
                 ids.add(id);
             }
             vm.setProviderFirewallIds(ids.toArray(new String[ids.size()]));
         }
         finally {
             APITrace.end();
         }
     }
 
 
     @Override
     public @Nonnull Iterable<VirtualMachineProduct> listProducts(@Nonnull Architecture architecture) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.listProducts");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was configured for this request");
             }
             Map<Architecture,Collection<VirtualMachineProduct>> cached;
             String endpoint = provider.getContext().getEndpoint();
             String accountId = provider.getContext().getAccountNumber();
             String regionId = provider.getContext().getRegionId();
 
             productCache.purge();
             cached = (HashMap<Architecture, Collection<VirtualMachineProduct>>) productCache.get(endpoint+"_"+accountId+"_"+regionId);
             if (cached != null && !cached.isEmpty()) {
                 if( cached.containsKey(architecture) ) {
                     Collection<VirtualMachineProduct> products = cached.get(architecture);
 
                     if( products != null ) {
                         return products;
                     }
                 }
             }
             else {
                 cached = new HashMap<Architecture, Collection<VirtualMachineProduct>>();
                 productCache.put(endpoint+"_"+accountId+"_"+regionId, cached, CalendarWrapper.HOUR * 4);
             }
             List<VirtualMachineProduct> products;
             Set<String> mapping = null;
 
             if( customServiceMappings == null ) {
                 load();
             }
             if( customServiceMappings != null ) {
                 String cloudId = cloudMappings.getProperty(provider.getContext().getEndpoint());
 
                 if( cloudId != null ) {
                     Map<String,Set<String>> map = customServiceMappings.get(cloudId);
 
                     if( map != null ) {
                         mapping = map.get(provider.getContext().getRegionId());
                     }
                 }
             }
             products = new ArrayList<VirtualMachineProduct>();
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_SERVICE_OFFERINGS, new Param("zoneId", ctx.getRegionId())), LIST_SERVICE_OFFERINGS);
             NodeList matches = doc.getElementsByTagName("serviceoffering");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 String id = null, name = null;
                 Node node = matches.item(i);
                 NodeList attributes;
                 int memory = 0;
                 int cpu = 0;
 
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
                         id = value;
                     }
                     else if( n.getNodeName().equals("name") ) {
                         name = value;
                     }
                     else if( n.getNodeName().equals("cpunumber") ) {
                         cpu = Integer.parseInt(value);
                     }
                     else if( n.getNodeName().equals("memory") ) {
                         memory = Integer.parseInt(value);
                     }
                     if( id != null && name != null && cpu > 0 && memory > 0 ) {
                         break;
                     }
                 }
                 if( id != null ) {
                     if( mapping == null || mapping.contains(id) ) {
                         VirtualMachineProduct product;
 
                         product = new VirtualMachineProduct();
                         product.setProviderProductId(id);
                         product.setName(name + " (" + cpu + " CPU/" + memory + "MB RAM)");
                         product.setDescription(name + " (" + cpu + " CPU/" + memory + "MB RAM)");
                         product.setRamSize(new Storage<Megabyte>(memory, Storage.MEGABYTE));
                         product.setCpuCount(cpu);
                         product.setRootVolumeSize(new Storage<Gigabyte>(1, Storage.GIGABYTE));
                         products.add(product);
                     }
                 }
             }
             cached.put(architecture, products);
             return products;
         }
         finally {
             APITrace.end();
         }
     }
 
     private transient Collection<Architecture> architectures;
 
     @Override
     public Iterable<Architecture> listSupportedArchitectures() throws InternalException, CloudException {
         if( architectures == null ) {
             ArrayList<Architecture> a = new ArrayList<Architecture>();
 
             a.add(Architecture.I32);
             a.add(Architecture.I64);
             architectures = Collections.unmodifiableList(a);
         }
         return architectures;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listVirtualMachineStatus() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.listVirtualMachineStatus");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was specified for this request");
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_VIRTUAL_MACHINES, new Param("zoneId", ctx.getRegionId())), LIST_VIRTUAL_MACHINES);
             ArrayList<ResourceStatus> servers = new ArrayList<ResourceStatus>();
             NodeList matches = doc.getElementsByTagName("virtualmachine");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);
 
                 if( node != null ) {
                     ResourceStatus vm = toStatus(node);
 
                     if( vm != null ) {
                         servers.add(vm);
                     }
                 }
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
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was specified for this request");
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_VIRTUAL_MACHINES, new Param("zoneId", ctx.getRegionId())), LIST_VIRTUAL_MACHINES);
             ArrayList<VirtualMachine> servers = new ArrayList<VirtualMachine>();
             NodeList matches = doc.getElementsByTagName("virtualmachine");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);
 
                 if( node != null ) {
                     try {
                         VirtualMachine vm = toVirtualMachine(node);
 
                         if( vm != null ) {
                             servers.add(vm);
                         }
                     } catch (Throwable t) {
                         logger.error("Problem discovering a virtual machine: " + t.getMessage());
                     }
                 }
             }
             return servers;
         }
         finally {
             APITrace.end();
         }
     }
 
     private String resetPassword(@Nonnull String serverId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.resetPassword");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was specified for this request");
             }
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(RESET_VIRTUAL_MACHINE_PASSWORD, new Param("id", serverId)), RESET_VIRTUAL_MACHINE_PASSWORD);
 
             Document responseDoc = provider.waitForJob(doc, "reset vm password");
 
             if (responseDoc != null){
                 NodeList matches = responseDoc.getElementsByTagName("virtualmachine");
 
                 for( int i=0; i<matches.getLength(); i++ ) {
                     Node node = matches.item(i);
 
                     if( node != null ) {
                         NodeList attributes = node.getChildNodes();
                         for( int j=0; j<attributes.getLength(); j++ ) {
                             Node attribute = attributes.item(j);
                             String name = attribute.getNodeName().toLowerCase();
                             String value;
 
                             if( attribute.getChildNodes().getLength() > 0 ) {
                                 value = attribute.getFirstChild().getNodeValue();
                             }
                             else {
                                 value = null;
                             }
                             if( name.equals("password") ) {
                                 return value;
                             }
                         }
                     }
                 }
             }
 
 
             logger.warn("Unable to find password for vm with id "+serverId);
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void reboot(@Nonnull String serverId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "VM.reboot");
         try {
             CSMethod method = new CSMethod(provider);
         
             method.get(method.buildUrl(REBOOT_VIRTUAL_MACHINE, new Param("id", serverId)), REBOOT_VIRTUAL_MACHINE);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void start(@Nonnull String serverId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.start");
         try {
             CSMethod method = new CSMethod(provider);
 
             method.get(method.buildUrl(START_VIRTUAL_MACHINE, new Param("id", serverId)), START_VIRTUAL_MACHINE);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void stop(@Nonnull String vmId, boolean force) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.stop");
         try {
             CSMethod method = new CSMethod(provider);
 
             method.get(method.buildUrl(STOP_VIRTUAL_MACHINE, new Param("id", vmId), new Param("forced", String.valueOf(force))), STOP_VIRTUAL_MACHINE);
         }
         finally {
             APITrace.end();
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
     public void terminate(@Nonnull String serverId, @Nullable String explanation) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "VM.terminate");
         try {
             CSMethod method = new CSMethod(provider);
         
             method.get(method.buildUrl(DESTROY_VIRTUAL_MACHINE, new Param("id", serverId)), DESTROY_VIRTUAL_MACHINE);
         }
         finally {
             APITrace.end();
         }
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable Node node) throws CloudException, InternalException {
         if( node == null ) {
             return null;
         }
         NodeList attributes = node.getChildNodes();
         VmState state = null;
         String serverId = null;
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             String name = attribute.getNodeName().toLowerCase();
             String value;
 
             if( attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();
             }
             else {
                 value = null;
             }
             if( name.equals("virtualmachineid") || name.equals("id") ) {
                 serverId = value;
             }
             else if( name.equals("state") ) {
                 if( value == null ) {
                     state = VmState.PENDING;
                 }
                 else if( value.equalsIgnoreCase("stopped") ) {
                     state = VmState.STOPPED;
                 }
                 else if( value.equalsIgnoreCase("running") ) {
                     state = VmState.RUNNING;
                 }
                 else if( value.equalsIgnoreCase("stopping") ) {
                     state = VmState.STOPPING;
                 }
                 else if( value.equalsIgnoreCase("starting") ) {
                     state = VmState.PENDING;
                 }
                 else if( value.equalsIgnoreCase("creating") ) {
                     state = VmState.PENDING;
                 }
                 else if( value.equalsIgnoreCase("migrating") ) {
                     state = VmState.REBOOTING;
                 }
                 else if( value.equalsIgnoreCase("destroyed") ) {
                     state = VmState.TERMINATED;
                 }
                 else if( value.equalsIgnoreCase("error") ) {
                     logger.warn("VM is in an error state.");
                     return null;
                 }
                 else if( value.equalsIgnoreCase("expunging") ) {
                     state = VmState.TERMINATED;
                 }
                 else if( value.equalsIgnoreCase("ha") ) {
                     state = VmState.REBOOTING;
                 }
                 else {
                     throw new CloudException("Unexpected server state: " + value);
                 }
             }
             if( serverId != null && state != null ) {
                 break;
             }
         }
         if( serverId == null ) {
             return null;
         }
         if( state == null ) {
             state = VmState.PENDING;
         }
         return new ResourceStatus(serverId, state);
     }
 
     private @Nullable VirtualMachine toVirtualMachine(@Nullable Node node) throws CloudException, InternalException {
         if( node == null ) {
             return null;
         }
         HashMap<String,String> properties = new HashMap<String,String>();
         VirtualMachine server = new VirtualMachine();
         NodeList attributes = node.getChildNodes();
         String productId = null;
         
         server.setTags(properties);
         server.setArchitecture(Architecture.I64); 
         server.setProviderOwnerId(provider.getContext().getAccountNumber());
         server.setClonable(false);
         server.setImagable(false);
         server.setPausable(true);
         server.setPersistent(true);
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             String name = attribute.getNodeName().toLowerCase();
             String value;
             
             if( attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();                
             }
             else {
                 value = null;
             }
             if( name.equals("virtualmachineid") || name.equals("id") ) {
                 server.setProviderVirtualMachineId(value);
                 logger.info("Processing VM id '" + value + "'");
             }
             else if( name.equals("name") ) {
                 server.setDescription(value);
             }
             /*
             else if( name.equals("haenable") ) {
                 server.setPersistent(value != null && value.equalsIgnoreCase("true"));
             }
             */
             else if( name.equals("displayname") ) {
                 server.setName(value);
             }
             else if( name.equals("ipaddress") ) { // v2.1
                 if( value != null ) {
                     server.setPrivateAddresses(new RawAddress(value));
                 }
                 server.setPrivateDnsAddress(value);
             }
             else if( name.equals("password") ) {
                 server.setRootPassword(value);
             }
             else if( name.equals("nic") ) { // v2.2
                 if( attribute.hasChildNodes() ) {                    
                     NodeList parts = attribute.getChildNodes();
                     String addr = null;
                     
                     for( int j=0; j<parts.getLength(); j++ ) {
                         Node part = parts.item(j); 
                         
                         if( part.getNodeName().equalsIgnoreCase("ipaddress") ) {
                             if( part.hasChildNodes() ) {
                                 addr = part.getFirstChild().getNodeValue();
                                 if( addr != null ) {
                                     addr = addr.trim();
                                 }
                             }
                         }
                         else if( part.getNodeName().equalsIgnoreCase("networkid") ) {
                             server.setProviderVlanId(part.getFirstChild().getNodeValue().trim());
                         }
                     }
                     if( addr != null ) {
                         boolean pub = false;
                         
                         if( !addr.startsWith("10.") && !addr.startsWith("192.168.") ) {
                             if( addr.startsWith("172.") ) {
                                 String[] nums = addr.split("\\.");
                                 
                                 if( nums.length != 4 ) {
                                     pub = true;
                                 }
                                 else {
                                     try {
                                         int x = Integer.parseInt(nums[1]);
                                         
                                         if( x < 16 || x > 31 ) {
                                             pub = true;
                                         }
                                     }
                                     catch( NumberFormatException ignore ) {
                                         // ignore
                                     }
                                 }
                             }
                             else {
                                 pub = true;
                             }
                         }
                         if( pub ) {
                             server.setPublicAddresses(new RawAddress(addr));
                             if( server.getPublicDnsAddress() == null ) {
                                 server.setPublicDnsAddress(addr);
                             }
                         }
                         else {
                             server.setPrivateAddresses(new RawAddress(addr));
                             if( server.getPrivateDnsAddress() == null ) {
                                 server.setPrivateDnsAddress(addr);
                             }
                         }
                     }
                 }
             }
             else if( name.equals("osarchitecture") ) {
                 if( value != null && value.equals("32") ) {
                     server.setArchitecture(Architecture.I32);
                 }
                 else {
                     server.setArchitecture(Architecture.I64);                  
                 }
             }
             else if( name.equals("created") ) {
                 DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); //2009-02-03T05:26:32.612278
                 
                 try {
                     server.setCreationTimestamp(df.parse(value).getTime());
                 }
                 catch( ParseException e ) {
                     logger.warn("Invalid date: " + value);
                     server.setLastBootTimestamp(0L);
                 }
             }
             else if( name.equals("state") ) {
                 VmState state;
 
                 //(Running, Stopped, Stopping, Starting, Creating, Migrating, HA).
                 if( value.equalsIgnoreCase("stopped") ) {
                     state = VmState.STOPPED;
                     server.setImagable(true);
                 }
                 else if( value.equalsIgnoreCase("running") ) {
                     state = VmState.RUNNING;
                 }
                 else if( value.equalsIgnoreCase("stopping") ) {
                     state = VmState.STOPPING;
                 }
                 else if( value.equalsIgnoreCase("starting") ) {
                     state = VmState.PENDING;
                 }
                 else if( value.equalsIgnoreCase("creating") ) {
                     state = VmState.PENDING;
                 }
                 else if( value.equalsIgnoreCase("migrating") ) {
                     state = VmState.REBOOTING;
                 }
                 else if( value.equalsIgnoreCase("destroyed") ) {
                     state = VmState.TERMINATED;
                 }
                 else if( value.equalsIgnoreCase("error") ) {
                     logger.warn("VM is in an error state.");
                 	return null;
                 }
                 else if( value.equalsIgnoreCase("expunging") ) {
                     state = VmState.TERMINATED;
                 }
                 else if( value.equalsIgnoreCase("ha") ) {
                     state = VmState.REBOOTING;
                 }
                 else {
                     throw new CloudException("Unexpected server state: " + value);
                 }
                 server.setCurrentState(state);                
             }
             else if( name.equals("zoneid") ) {
                 server.setProviderRegionId(value);
                 server.setProviderDataCenterId(value);
             }
             else if( name.equals("templateid") ) {
                 server.setProviderMachineImageId(value);
             }
             else if( name.equals("templatename") ) {
                 Platform platform = Platform.guess(value);
                 if (platform.equals(Platform.UNKNOWN)){
                     platform = guessForWindows(value);
                 }
                 server.setPlatform(platform);
             }
             else if( name.equals("serviceofferingid") ) {
                 productId = value;
             }
             else if( value != null ) {
                 properties.put(name, value);
             }
         }
         if( server.getName() == null ) {
             server.setName(server.getProviderVirtualMachineId());
         }
         if( server.getDescription() == null ) {
             server.setDescription(server.getName());
         }
         server.setProviderAssignedIpAddressId(null);
         if( server.getProviderRegionId() == null ) {
             server.setProviderRegionId(provider.getContext().getRegionId());
         }
         if( server.getProviderDataCenterId() == null ) {
             server.setProviderDataCenterId(provider.getContext().getRegionId());
         }
         if( productId != null ) {
             server.setProductId(productId);
         }
         if (server.getPlatform().equals(Platform.UNKNOWN)){
             Templates support = provider.getComputeServices().getImageSupport();
             if (support != null){
                 MachineImage image =support.getImage(server.getProviderMachineImageId());
                 if (image != null){
                     server.setPlatform(image.getPlatform());
                 }
             }
         }
 
         setFirewalls(server);
         final String finalServerId = server.getProviderVirtualMachineId();
         server.setPasswordCallback(new Callable<String>() {
             @Override
             public String call() throws Exception {
                 return getRootPassword(finalServerId);
             }
         }
         );
         return server;
     }
 
     private Platform guessForWindows(String name){
         if (name == null){
             return Platform.UNKNOWN;
         }
         String platform = name.toLowerCase();
         if (platform.contains("windows") || platform.contains("win") ){
             return Platform.WINDOWS;
         }
         return Platform.UNKNOWN;
     }
 }
