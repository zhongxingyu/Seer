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
 
 package org.dasein.cloud.cloudstack.network;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.cloudstack.CSCloud;
 import org.dasein.cloud.cloudstack.CSException;
 import org.dasein.cloud.cloudstack.CSMethod;
 import org.dasein.cloud.cloudstack.CSTopology;
 import org.dasein.cloud.cloudstack.CSVersion;
 import org.dasein.cloud.cloudstack.Param;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.AddressType;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.IpAddressSupport;
 import org.dasein.cloud.network.IpForwardingRule;
 import org.dasein.cloud.network.LoadBalancer;
 import org.dasein.cloud.network.LoadBalancerSupport;
 import org.dasein.cloud.network.Protocol;
 import org.dasein.cloud.network.RawAddress;
 import org.dasein.cloud.util.APITrace;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class IpAddress implements IpAddressSupport {
     static private final Logger logger = CSCloud.getLogger(IpAddress.class, "std");
 
     static private final String ASSOCIATE_IP_ADDRESS                 = "associateIpAddress";
     static private final String CREATE_PORT_FORWARDING_RULE          = "createPortForwardingRule";
     static private final String DISASSOCIATE_IP_ADDRESS              = "disassociateIpAddress";
     static private final String LIST_PORT_FORWARDING_RULES           = "listPortForwardingRules";
     static private final String LIST_PUBLIC_IP_ADDRESSES             = "listPublicIpAddresses";
     static private final String STOP_FORWARD                         = "deletePortForwardingRule";
     
     private CSCloud provider;
     
     public IpAddress(CSCloud provider) {
         this.provider = provider;
     }
     
     public void assign(@Nonnull String addressId, @Nonnull String toServerId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("Address assignment is not supported");
     }
 
     @Override
     public void assignToNetworkInterface(@Nonnull String addressId, @Nonnull String nicId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("Network interfaces are not supported");
     }
 
     @Override
     public @Nonnull String forward(@Nonnull String addressId, int publicPort, @Nonnull Protocol protocol, int privatePort, @Nonnull String onServerId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.forward");
         try {
             VirtualMachine server = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(onServerId);
             RawAddress privateIpAddress = null;
                 
             if( server == null ) {
                 throw new CloudException("No such server: " + onServerId);
             }
             RawAddress[] addresses = server.getPrivateAddresses();
 
             if( addresses != null && addresses.length > 0 ) {
                 privateIpAddress = addresses[0];
             }
             if( privateIpAddress == null ) {
                 throw new CloudException("Could not determine a private IP address for " + onServerId);
             }
             Param[] params;
                 
             if( isId() ) {
                 params = new Param[] { new Param("ipAddressId", addressId), new Param("publicPort", String.valueOf(publicPort)), new Param("privatePort", String.valueOf(privatePort)), new Param("protocol", protocol.name()), new Param("virtualMachineId", onServerId)};
             }
             else {
                 params = new Param[] { new Param("ipAddress", addressId), new Param("publicPort", String.valueOf(publicPort)), new Param("privatePort", String.valueOf(privatePort)), new Param("protocol", protocol.name()), new Param("virtualMachineId", onServerId)};
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(CREATE_PORT_FORWARDING_RULE, params), CREATE_PORT_FORWARDING_RULE);
     
             provider.waitForJob(doc, "Assigning forwarding rule");
             for( IpForwardingRule rule : listRules(addressId) ) {
                 if( rule.getPublicPort() == publicPort && rule.getPrivatePort() == privatePort && onServerId.equals(rule.getServerId()) && protocol.equals(rule.getProtocol()) && addressId.equals(rule.getAddressId()) ) {
                     return rule.getProviderRuleId();
                 }
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
     
     private boolean isId() {
         return provider.getVersion().greaterThan(CSVersion.CS21);
     }
 
     @Override
     public @Nullable org.dasein.cloud.network.IpAddress getIpAddress(@Nonnull String addressId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.getIpAddress");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             try {
                 CSMethod method = new CSMethod(provider);
                 Document doc = method.get(method.buildUrl(LIST_PUBLIC_IP_ADDRESSES, new Param(isId() ? "ipAddressId" : "ipAddress", addressId)), LIST_PUBLIC_IP_ADDRESSES);
                 HashMap<String,LoadBalancer> loadBalancers = new HashMap<String,LoadBalancer>();
                 LoadBalancerSupport support = provider.getNetworkServices().getLoadBalancerSupport();
                 LoadBalancer lb = (support == null ? null : support.getLoadBalancer(addressId));
 
                 loadBalancers.put(addressId, lb);
                 NodeList matches = doc.getElementsByTagName("publicipaddress");
                 for( int i=0; i<matches.getLength(); i++ ) {
                     org.dasein.cloud.network.IpAddress addr = toAddress(matches.item(i), ctx, loadBalancers);
 
                     if( addr != null ) {
                         if( addr.getProviderIpAddressId().equals(addressId) ) {
                             return addr;
                         }
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
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForIpAddress(@Nonnull Locale locale) {
         return "IP address";
     }
 
     @Override
     public @Nonnull Requirement identifyVlanForVlanIPRequirement() throws CloudException, InternalException {
         return Requirement.REQUIRED;
     }
 
     @Override
     public boolean isAssigned(@Nonnull AddressType type) {
         return false;
     }
 
     @Override
     public boolean isAssigned(@Nonnull IPVersion version) throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isAssignablePostLaunch(@Nonnull IPVersion version) throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isForwarding() {
         return true;
     }
 
     @Override
     public boolean isForwarding(@Nonnull IPVersion version) throws CloudException, InternalException {
         return version.equals(IPVersion.IPV4);
     }
 
     @Override
     public boolean isRequestable(@Nonnull AddressType type) {
         return type.equals(AddressType.PUBLIC);
     }
 
     @Override
     public boolean isRequestable(@Nonnull IPVersion version) throws CloudException, InternalException {
         return version.equals(IPVersion.IPV4);
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(provider, "IpAddress.isSubscribed");
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
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     @Deprecated
     public @Nonnull Iterable<org.dasein.cloud.network.IpAddress> listPrivateIpPool(boolean unassignedOnly) throws InternalException, CloudException {
         return Collections.emptyList();
     }
 
     @Override
     @Deprecated
     public @Nonnull Iterable<org.dasein.cloud.network.IpAddress> listPublicIpPool(boolean unassignedOnly) throws InternalException, CloudException {
         return listIpPool(IPVersion.IPV4, unassignedOnly);
     }
 
     @Override
     public @Nonnull Iterable<org.dasein.cloud.network.IpAddress> listIpPool(@Nonnull IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.listIpPool");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( version.equals(IPVersion.IPV4) ) {
                 HashMap<String,LoadBalancer> loadBalancers = new HashMap<String,LoadBalancer>();
                 LoadBalancerSupport support = provider.getNetworkServices().getLoadBalancerSupport();
 
                 if( support != null ) {
                     for( LoadBalancer lb : support.listLoadBalancers() ) {
                         loadBalancers.put(lb.getProviderLoadBalancerId(), lb);
                     }
                 }
                 CSMethod method = new CSMethod(provider);
                 Document doc = method.get(method.buildUrl(LIST_PUBLIC_IP_ADDRESSES, new Param("zoneId", ctx.getRegionId())), LIST_PUBLIC_IP_ADDRESSES);
                 ArrayList<org.dasein.cloud.network.IpAddress> addresses = new ArrayList<org.dasein.cloud.network.IpAddress>();
                 NodeList matches = doc.getElementsByTagName("publicipaddress");
 
                 for( int i=0; i<matches.getLength(); i++ ) {
                     org.dasein.cloud.network.IpAddress addr = toAddress(matches.item(i), ctx, loadBalancers);
 
                     if( addr != null && (!unassignedOnly || !addr.isAssigned()) ) {
                         addresses.add(addr);
                     }
                 }
                 return addresses;
             }
             return Collections.emptyList();
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listIpPoolStatus(@Nonnull IPVersion version) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.listIpPoolStatus");
         try {
             if( !IPVersion.IPV4.equals(version) ) {
                 return Collections.emptyList();
             }
             HashMap<String,LoadBalancer> loadBalancers = new HashMap<String,LoadBalancer>();
             LoadBalancerSupport support = provider.getNetworkServices().getLoadBalancerSupport();
 
             if( support != null ) {
                 for( LoadBalancer lb : support.listLoadBalancers() ) {
                     loadBalancers.put(lb.getProviderLoadBalancerId(), lb);
                 }
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_PUBLIC_IP_ADDRESSES, new Param("zoneId", ctx.getRegionId())), LIST_PUBLIC_IP_ADDRESSES);
             ArrayList<ResourceStatus> addresses = new ArrayList<ResourceStatus>();
             NodeList matches = doc.getElementsByTagName("publicipaddress");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 ResourceStatus addr = toStatus(matches.item(i), loadBalancers);
 
                 if( addr != null ) {
                     addresses.add(addr);
                 }
             }
             return addresses;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Collection<IpForwardingRule> listRules(@Nonnull String addressId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.listRules");
         try {
             ArrayList<IpForwardingRule> rules = new ArrayList<IpForwardingRule>();
             Param[] params;
             
             if( provider.getVersion().greaterThan(CSVersion.CS21) ) {
                 params = new Param[] { new Param("ipaddressid", addressId) };                    
             }
             else {
                 params = new Param[] { new Param("ipAddress", addressId) };
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_PORT_FORWARDING_RULES, params), LIST_PORT_FORWARDING_RULES);
             
             if( doc == null ) {
                 throw new CloudException("No such IP address: " + addressId);
             }
             NodeList matches = doc.getElementsByTagName("portforwardingrule");
             
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);
                 
                 if( node != null ) {
                     IpForwardingRule rule = new IpForwardingRule();
                     NodeList list = node.getChildNodes();
     
                     rule.setAddressId(addressId);
                     for( int j=0; j<list.getLength(); j++ ) {
                         Node attr = list.item(j);
                         
                         if( attr.getNodeName().equals("publicport") ) {
                             rule.setPublicPort(Integer.parseInt(attr.getFirstChild().getNodeValue()));                        
                         }
                         else if( attr.getNodeName().equals("privateport") ) {
                             rule.setPrivatePort(Integer.parseInt(attr.getFirstChild().getNodeValue()));                        
                         }
                         else if( attr.getNodeName().equals("protocol") ) {
                             rule.setProtocol(Protocol.valueOf(attr.getFirstChild().getNodeValue().toUpperCase()));
                         } 
                         else if( attr.getNodeName().equals("id") ) {
                             rule.setProviderRuleId(attr.getFirstChild().getNodeValue());
                         } 
                         else if( attr.getNodeName().equals("virtualmachineid") ) {
                             rule.setServerId(attr.getFirstChild().getNodeValue());
                         } 
                     }
                     if( logger.isDebugEnabled() ) {
                         logger.debug("listRules(): * " + rule);
                     }
                     rules.add(rule);
                 }
             }
             return rules;
         }
         catch( RuntimeException e ) {
             logger.error("listRules(): Runtime exception listing rules for " + addressId + ": " + e.getMessage());
             e.printStackTrace();
             throw new InternalException(e);
         }
         catch( Error e ) {
             logger.error("listRules(): Error listing rules for " + addressId + ": " + e.getMessage());
             e.printStackTrace();
             throw new InternalException(e);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
         return Collections.singletonList(IPVersion.IPV4);
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     @Override
     public void releaseFromPool(@Nonnull String addressId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.releaseFromPool");
         try {
             CSMethod method = new CSMethod(provider);
         
             method.get(method.buildUrl(DISASSOCIATE_IP_ADDRESS, new Param(isId() ? "id" : "ipaddress", addressId)), DISASSOCIATE_IP_ADDRESS);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void releaseFromServer(@Nonnull String addressId) throws InternalException, CloudException {
         throw new OperationNotSupportedException();
     }
 
     @Override
     public @Nonnull String request(@Nonnull AddressType typeOfAddress) throws InternalException, CloudException {
         if( typeOfAddress.equals(AddressType.PUBLIC) ) {
             return request(IPVersion.IPV4);
         }
         throw new OperationNotSupportedException("Private IP requests are not supported");
     }
 
     @Override
     public @Nonnull String request(@Nonnull IPVersion version) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.request");
         try {
             if( !version.equals(IPVersion.IPV4) ) {
                 throw new OperationNotSupportedException("Only IPv4 is currently supported");
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             CSMethod method = new CSMethod(provider);
             Document doc;
 
             //if( isBasic() ) {
             doc = method.get(method.buildUrl(ASSOCIATE_IP_ADDRESS,  new Param("zoneId", ctx.getRegionId())), ASSOCIATE_IP_ADDRESS);
             // }
             //else {
             //  throw new
             //doc = method.get(method.buildUrl(ASSOCIATE_IP_ADDRESS,  new Param[] { new Param("zoneId", provider.getContext().getRegionId())  }))
             //}
             NodeList matches;
 
             if( provider.getVersion().greaterThan(CSVersion.CS21) ) {
                 matches = doc.getElementsByTagName("id");
                 if (matches.getLength() == 0) {
                     matches = doc.getElementsByTagName("jobid"); //4.1
                 }
             }
             else {
                 matches = doc.getElementsByTagName("ipaddress");
             }
             String id = null;
             if( matches.getLength() > 0 ) {
                 id = matches.item(0).getFirstChild().getNodeValue();
             }
             if( id == null ) {
                 throw new CloudException("Failed to request an IP address without error");
             }
             Document responseDoc = provider.waitForJob(doc, ASSOCIATE_IP_ADDRESS);
             if (responseDoc != null) {
                 NodeList nodeList = responseDoc.getElementsByTagName("ipaddress");
                 if (nodeList.getLength() > 0) {
                     Node ipAddress = nodeList.item(0);
                     NodeList attributes = ipAddress.getChildNodes();
                     for (int i = 0; i<attributes.getLength(); i++) {
                         Node attribute = attributes.item(i);
                         String tmpname = attribute.getNodeName().toLowerCase();
                         String value;
 
                         if( attribute.getChildNodes().getLength() > 0 ) {
                             value = attribute.getFirstChild().getNodeValue();
                         }
                         else {
                             value = null;
                         }
                         if (tmpname.equalsIgnoreCase("id")) {
                             id = value;
                             break;
                         }
                     }
                 }
             }
             return id;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String requestForVLAN(@Nonnull IPVersion version) throws InternalException, CloudException {
         throw new OperationNotSupportedException("Not yet supported since CloudStack requires you to name the network");
     }
 
     @Override
     public @Nonnull String requestForVLAN(@Nonnull IPVersion version, @Nonnull String vlanId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.requestForVLAN");
         try {
             if( !version.equals(IPVersion.IPV4) ) {
                 throw new OperationNotSupportedException("Only IPv4 is currently supported");
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(ASSOCIATE_IP_ADDRESS,  new Param("zoneId", ctx.getRegionId()), new Param("networkId", vlanId)), ASSOCIATE_IP_ADDRESS);
             NodeList matches;
 
             if( provider.getVersion().greaterThan(CSVersion.CS21) ) {
                 matches = doc.getElementsByTagName("id");
                if (matches.getLength() == 0) {
                    matches = doc.getElementsByTagName("jobid"); //4.1
                }
             }
             else {
                 matches = doc.getElementsByTagName("ipaddress");
             }
             String id = null;
             if( matches.getLength() > 0 ) {
                 id = matches.item(0).getFirstChild().getNodeValue();
             }
             if( id == null ) {
                 throw new CloudException("Failed to request an IP address without error");
             }
            Document responseDoc = provider.waitForJob(doc, ASSOCIATE_IP_ADDRESS);
            if (responseDoc != null) {
                NodeList nodeList = responseDoc.getElementsByTagName("ipaddress");
                if (nodeList.getLength() > 0) {
                    Node ipAddress = nodeList.item(0);
                    NodeList attributes = ipAddress.getChildNodes();
                    for (int i = 0; i<attributes.getLength(); i++) {
                        Node attribute = attributes.item(i);
                        String tmpname = attribute.getNodeName().toLowerCase();
                        String value;

                        if( attribute.getChildNodes().getLength() > 0 ) {
                            value = attribute.getFirstChild().getNodeValue();
                        }
                        else {
                            value = null;
                        }
                        if (tmpname.equalsIgnoreCase("id")) {
                            id = value;
                            break;
                        }
                    }
                }
            }
             return id;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void stopForward(@Nonnull String ruleId) throws InternalException, CloudException {
         APITrace.begin(provider, "IpAddress.stopForward");
         try {
             Logger logger = CSCloud.getLogger(IpAddress.class, "std");
 
             if( logger.isTraceEnabled() ) {
                 logger.trace("enter - " + IpAddress.class.getName() + ".stopForward(" + ruleId + ")");
             }
             try {
                 CSMethod method = new CSMethod(provider);
                 Document doc = method.get(method.buildUrl(STOP_FORWARD, new Param("id", ruleId)), STOP_FORWARD);
 
                 provider.waitForJob(doc, STOP_FORWARD);
             }
             finally {
                 if( logger.isTraceEnabled() ) {
                     logger.trace("exit - " + IpAddress.class.getName() + ".stopForward()");
                 }
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean supportsVLANAddresses(@Nonnull IPVersion ofVersion) throws InternalException, CloudException {
         return IPVersion.IPV4.equals(ofVersion);
     }
 
     private @Nullable org.dasein.cloud.network.IpAddress toAddress(@Nullable Node node, @Nonnull ProviderContext ctx, @Nonnull Map<String,LoadBalancer> loadBalancers) throws InternalException, CloudException {
         if( node == null ) {
             return null;
         }
         String regionId = ctx.getRegionId();
 
         if( regionId == null ) {
             throw new CloudException("No region was set for this request");
         }
         org.dasein.cloud.network.IpAddress address = new org.dasein.cloud.network.IpAddress();
         NodeList attributes = node.getChildNodes();
         
         address.setRegionId(regionId);
         address.setServerId(null);
         address.setProviderLoadBalancerId(null);
         address.setAddressType(AddressType.PUBLIC);
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node n = attributes.item(i);
             String name = n.getNodeName().toLowerCase();
             String value;
             
             if( n.getChildNodes().getLength() > 0 ) {
                 value = n.getFirstChild().getNodeValue();
             }
             else {
                 value = null;
             }
             if( name.equalsIgnoreCase("id") && value != null ) {
                 address.setIpAddressId(value);
             }
             else if( name.equalsIgnoreCase("ipaddress") && value != null ) {
                 //noinspection ConstantConditions
                 if( address.getProviderIpAddressId() == null ) { // 2.1
                     address.setIpAddressId(value);
                 }
                 address.setAddress(value);
             }
             else if( name.equalsIgnoreCase("zoneid") && value != null ) {
                 address.setRegionId(value);
             }
             else if( name.equalsIgnoreCase("virtualmachineid") ) {
                 address.setServerId(value);
             }
             else if( name.equalsIgnoreCase("state") ) {
                 if( value != null && !value.equalsIgnoreCase("allocated") ) {
                     return null;
                 }
             }
             else if( name.equalsIgnoreCase("associatednetworkid") ) {
                 if( value != null ) {
                     address.setForVlan(true);
                 }
             }
         }
         LoadBalancer lb = loadBalancers.get(address.getRawAddress().getIpAddress());
             
         if( lb != null ) {
             address.setProviderLoadBalancerId(lb.getProviderLoadBalancerId());
         }
         if( address.getServerId() == null ) {
             for( VirtualMachine vm : provider.getComputeServices().getVirtualMachineSupport().listVirtualMachines() ) {
                 RawAddress[] addrs = vm.getPublicAddresses();
                 
                 for( RawAddress addr : addrs ) {
                     if( addr.getIpAddress().equals(address.getRawAddress().getIpAddress()) ) {
                         address.setServerId(vm.getProviderVirtualMachineId());
                     }
                 }
             }
         }
         return address;
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable Node node, @Nonnull Map<String,LoadBalancer> loadBalancers) throws InternalException, CloudException {
         if( node == null ) {
             return null;
         }
         NodeList attributes = node.getChildNodes();
         String addressId = null, address = null;
         Boolean available = null;
         boolean hasState = false;
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node n = attributes.item(i);
             String name = n.getNodeName().toLowerCase();
             String value;
 
             if( n.getChildNodes().getLength() > 0 ) {
                 value = n.getFirstChild().getNodeValue();
             }
             else {
                 value = null;
             }
             if( name.equalsIgnoreCase("id") && value != null ) {
                 addressId = value;
             }
             else if( name.equalsIgnoreCase("ipaddress") && value != null ) {
                 address = value;
             }
             else if( name.equalsIgnoreCase("virtualmachineid") ) {
                 available = (value != null && !value.equals(""));
             }
             else if( name.equalsIgnoreCase("state") ) {
                 if( value != null && !value.equalsIgnoreCase("allocated") ) {
                     return null;
                 }
                 hasState = true;
             }
             if( addressId != null && address != null && available != null && hasState ) {
                 break;
             }
         }
         if( addressId == null ) {
             return null;
         }
         if( address != null ) {
             LoadBalancer lb = loadBalancers.get(address);
 
             if( lb != null ) {
                 available = false;
             }
         }
         if( available == null ) {
             available = true;
         }
         return new ResourceStatus(addressId, available);
     }
 }
