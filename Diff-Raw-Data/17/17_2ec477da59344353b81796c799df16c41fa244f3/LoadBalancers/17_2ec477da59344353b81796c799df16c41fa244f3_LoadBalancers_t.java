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
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeSet;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.cloudstack.CSCloud;
 import org.dasein.cloud.cloudstack.CSException;
 import org.dasein.cloud.cloudstack.CSMethod;
 import org.dasein.cloud.cloudstack.CSTopology;
 import org.dasein.cloud.cloudstack.Param;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.dc.DataCenter;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.AbstractLoadBalancerSupport;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.LbAlgorithm;
 import org.dasein.cloud.network.LbEndpointState;
 import org.dasein.cloud.network.LbEndpointType;
 import org.dasein.cloud.network.LbListener;
 import org.dasein.cloud.network.LbPersistence;
 import org.dasein.cloud.network.LbProtocol;
 import org.dasein.cloud.network.LoadBalancer;
 import org.dasein.cloud.network.LoadBalancerAddressType;
 import org.dasein.cloud.network.LoadBalancerCreateOptions;
 import org.dasein.cloud.network.LoadBalancerEndpoint;
 import org.dasein.cloud.network.LoadBalancerState;
 import org.dasein.cloud.util.APITrace;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class LoadBalancers extends AbstractLoadBalancerSupport<CSCloud> {
     static public final String ASSIGN_TO_LOAD_BALANCER_RULE       = "assignToLoadBalancerRule";
     static public final String CREATE_LOAD_BALANCER_RULE          = "createLoadBalancerRule";
     static public final String DELETE_LOAD_BALANCER_RULE          = "deleteLoadBalancerRule";
     static public final String LIST_LOAD_BALANCER_RULES           = "listLoadBalancerRules";
     static public final String LIST_LOAD_BALANCER_RULE_INSTANCES  = "listLoadBalancerRuleInstances";
     static public final String REMOVE_FROM_LOAD_BALANCER_RULE     = "removeFromLoadBalancerRule";
     
     private CSCloud provider;
     
     LoadBalancers(CSCloud provider) {
         super(provider);
         this.provider = provider;
     }
 
     @Override
     public void addServers(@Nonnull String toLoadBalancerId, @Nonnull String ... serverIds) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.addServers");
         try {
             try {
                 LoadBalancer lb = getLoadBalancer(toLoadBalancerId);
 
                 if( lb == null ) {
                     throw new CloudException("No such load balancer: " + toLoadBalancerId);
                 }
                 if( serverIds == null || serverIds.length < 1 ) {
                     return;
                 }
                 for( LbListener listener : lb.getListeners() ) {
                     String ruleId = getVmOpsRuleId(listener.getAlgorithm(), toLoadBalancerId, listener.getPublicPort(), listener.getPrivatePort());
                     StringBuilder str = new StringBuilder();
 
                     for( int i=0; i<serverIds.length; i++ ) {
                         str.append(serverIds[i]);
                         if( i < serverIds.length-1 ) {
                             str.append(",");
                         }
                     }
                     CSMethod method = new CSMethod(provider);
                     Document doc = method.get(method.buildUrl(ASSIGN_TO_LOAD_BALANCER_RULE, new Param("id", ruleId), new Param("virtualMachineIds", str.toString())), ASSIGN_TO_LOAD_BALANCER_RULE);
 
                     provider.waitForJob(doc, "Add Server");
                 }
             }
             catch( RuntimeException e ) {
                 throw new InternalException(e);
             }
             catch( Error e ) {
                 throw new InternalException(e);
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String createLoadBalancer(@Nonnull LoadBalancerCreateOptions options) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.create");
         try {
             @SuppressWarnings("ConstantConditions") org.dasein.cloud.network.IpAddress publicAddress = provider.getNetworkServices().getIpAddressSupport().getIpAddress(options.getProviderIpAddressId());
 
             if( publicAddress == null ) {
                 throw new CloudException("You must specify the IP address for your load balancer.");
             }
             for( LbListener listener : options.getListeners() ) {
                 if( !isId(options.getProviderIpAddressId()) ) {
                     createVmOpsRule(options.getName(), listener.getAlgorithm(), options.getProviderIpAddressId(), listener.getPublicPort(), listener.getPrivatePort());
                 }
                 else {
                     createCloudstack22Rule(options.getName(), listener.getAlgorithm(), options.getProviderIpAddressId(), listener.getPublicPort(), listener.getPrivatePort());
                 }
             }
             for( LoadBalancerEndpoint endpoint : options.getEndpoints() ) {
                 if( endpoint.getEndpointType().equals(LbEndpointType.VM) ) {
                     addServers(publicAddress.getRawAddress().getIpAddress(), endpoint.getEndpointValue());
                 }
             }
             return publicAddress.getRawAddress().getIpAddress();
         }
         finally {
             APITrace.end();
         }
     }
 
     @SuppressWarnings("deprecation")
     @Override
     @Deprecated
     public @Nonnull String create(@Nonnull String name, @Nonnull String description, @Nullable String addressId, @Nullable String[] zoneIds, @Nullable LbListener[] listeners, @Nullable String[] serverIds) throws CloudException, InternalException {
         if( addressId == null ) {
             throw new CloudException("You must specify an IP address for load balancer creation");
         }
         LoadBalancerCreateOptions options = LoadBalancerCreateOptions.getInstance(name, description, addressId);
 
         if( zoneIds != null && zoneIds.length > 0 ) {
             options.limitedTo(zoneIds);
         }
         if( listeners != null && listeners.length > 0 ) {
             options.havingListeners(listeners);
         }
         if( serverIds != null && serverIds.length > 0 ) {
             options.withVirtualMachines(serverIds);
         }
         return createLoadBalancer(options);
     }
 
     private void createVmOpsRule(String lbName, LbAlgorithm algorithm, String publicIp, int publicPort, int privatePort) throws CloudException, InternalException {
         String id = getVmOpsRuleId(algorithm, publicIp, publicPort, privatePort);
         
         if( id != null ) {
             return;
         }
         Param[] params = new Param[6];
         String algor;
         
         switch( algorithm ) {
             case ROUND_ROBIN: algor = "roundrobin"; break;
             case LEAST_CONN: algor = "leastconn"; break;
             case SOURCE: algor = "source"; break;
             default: algor = "roundrobin"; break;
         }
         params[0] = new Param("publicIp", publicIp);
         params[1] = new Param("publicPort", String.valueOf(publicPort));
         params[2] = new Param("privatePort", String.valueOf(privatePort));
         params[3] = new Param("algorithm", algor);
         if (lbName != null && !lbName.equals("")) {
             params[4] = new Param("name", lbName);
         }
         else {
             params[4] = new Param("name", "dsnlb_" + publicIp + "_" + publicPort + "_" + privatePort);
         }
         params[5] = new Param("description", "dsnlb_" + publicIp + "_" + publicPort + "_" + privatePort);
         
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(CREATE_LOAD_BALANCER_RULE, params), CREATE_LOAD_BALANCER_RULE);
         NodeList matches = doc.getElementsByTagName("loadbalancerrule"); // v2.1
         
         for( int i=0; i<matches.getLength(); i++ ) {
             HashMap<String,LoadBalancer> current = new HashMap<String,LoadBalancer>();
             Collection<LoadBalancer> lbs;
             Node node = matches.item(i);
             
             toRule(node, current);
             lbs = current.values();
             if( lbs.size() > 0 ) {
                 return;
             }
         }
         matches = doc.getElementsByTagName("loadbalancer"); // v2.2
         for( int i=0; i<matches.getLength(); i++ ) {
             HashMap<String,LoadBalancer> current = new HashMap<String,LoadBalancer>();
             Collection<LoadBalancer> lbs;
             Node node = matches.item(i);
             
             toRule(node, current);
             lbs = current.values();
             if( lbs.size() > 0 ) {
                 return;
             }
         }
         throw new CloudException("Failed to add load balancer rule (2).");
     }
     
     private void createCloudstack22Rule(String lbName, LbAlgorithm algorithm, String publicIpId, int publicPort, int privatePort) throws CloudException, InternalException {
         String id = getVmOpsRuleId(algorithm, publicIpId, publicPort, privatePort);
         
         if( id != null ) {
             return;
         }
         
         Param[] params = new Param[6];
         String algor;
         
         switch( algorithm ) {
             case ROUND_ROBIN: algor = "roundrobin"; break;
             case LEAST_CONN: algor = "leastconn"; break;
             case SOURCE: algor = "source"; break;
             default: algor = "roundrobin"; break;
         }
         params[0] = new Param("publicIpId", publicIpId);
         params[1] = new Param("publicPort", String.valueOf(publicPort));
         params[2] = new Param("privatePort", String.valueOf(privatePort));
         params[3] = new Param("algorithm", algor);
         if (lbName != null && !lbName.equals("")) {
             params[4] = new Param("name", lbName);
         }
         else {
             params[4] = new Param("name", "dsnlb_" + publicIpId + "_" + publicPort + "_" + privatePort);
         }
         params[5] = new Param("description", "dsnlb_" + publicIpId + "_" + publicPort + "_" + privatePort);
 
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(CREATE_LOAD_BALANCER_RULE, params), CREATE_LOAD_BALANCER_RULE);
         NodeList matches = doc.getElementsByTagName("loadbalancerrule"); // v2.1
         
         for( int i=0; i<matches.getLength(); i++ ) {
             HashMap<String,LoadBalancer> current = new HashMap<String,LoadBalancer>();
             Collection<LoadBalancer> lbs;
             Node node = matches.item(i);
             
             toRule(node, current);
             lbs = current.values();
             if( lbs.size() > 0 ) {
                 return;
             }
         }
         matches = doc.getElementsByTagName("loadbalancer"); // v2.2.0 - v2.2.10
         for( int i=0; i<matches.getLength(); i++ ) {
             HashMap<String,LoadBalancer> current = new HashMap<String,LoadBalancer>();
             Collection<LoadBalancer> lbs;
             Node node = matches.item(i);
             
             toRule(node, current);
             lbs = current.values();
             if( lbs.size() > 0 ) {
                 return;
             }
         }
         
         matches = doc.getElementsByTagName("jobid"); // v2.2.11 - v2.2.13
         if (matches.getLength() > 0) {
         	provider.waitForJob(doc, "Create Load Balancer Rule");
         	return;
         }
         
         throw new CloudException("Failed to add load balancer rule (2).");
     }
     
     @Override
     public @Nonnull LoadBalancerAddressType getAddressType() throws CloudException, InternalException {
         return LoadBalancerAddressType.IP;
     }
     
     private boolean isId(String ipAddressIdCandidate) {
         String[] parts = ipAddressIdCandidate.split("\\.");
         
         return (parts == null || parts.length != 4 );        
     }
     
     @Override
     public @Nullable LoadBalancer getLoadBalancer(@Nonnull String loadBalancerId) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.getLoadBalancer");
         try {
             try {
                 HashMap<String,LoadBalancer> matches = new HashMap<String,LoadBalancer>();
                 boolean isId = isId(loadBalancerId);
                 String key = (isId ? "publicIpId" : "publicIp");
 
                 CSMethod method = new CSMethod(provider);
                 Document doc = method.get(method.buildUrl(LIST_LOAD_BALANCER_RULES, new Param(key, loadBalancerId)), LIST_LOAD_BALANCER_RULES);
                 NodeList rules = doc.getElementsByTagName("loadbalancerrule");
 
                 for( int i=0; i<rules.getLength(); i++ ) {
                     Node node = rules.item(i);
 
                     toRule(node, matches);
                 }
                 return matches.get(loadBalancerId);
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
     public int getMaxPublicPorts() throws CloudException, InternalException {
         return 0;
     }
 
     @Override
     public @Nonnull String getProviderTermForLoadBalancer(@Nonnull Locale locale) {
         return "load balancer";
     }
 
     @Override
     public @Nonnull Requirement identifyEndpointsOnCreateRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyListenersOnCreateRequirement() throws CloudException, InternalException {
         return Requirement.REQUIRED;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listLoadBalancerStatus() throws CloudException, InternalException {
         APITrace.begin(provider, "LB.listLoadBalancerStatus");
         try {
             HashMap<String,LoadBalancer> matches = new HashMap<String,LoadBalancer>();
             CSMethod method = new CSMethod(provider);
 
             try {
                 Document doc = method.get(method.buildUrl(LIST_LOAD_BALANCER_RULES), LIST_LOAD_BALANCER_RULES);
                 NodeList rules = doc.getElementsByTagName("loadbalancerrule");
 
                 for( int i=0; i<rules.getLength(); i++ ) {
                     Node node = rules.item(i);
 
                     toRule(node, matches);
                 }
                 ArrayList<ResourceStatus> results = new ArrayList<ResourceStatus>();
 
                 for( LoadBalancer lb : matches.values() ) {
                     if( matchesRegion(lb.getProviderLoadBalancerId()) ) {
                         results.add(new ResourceStatus(lb.getProviderLoadBalancerId(), lb.getCurrentState()));
                     }
                 }
                 return results;
             }
             catch( CloudException e ) {
                 if( e.getHttpCode() == HttpServletResponse.SC_NOT_FOUND ) {
                     return Collections.emptyList();
                 }
                 e.printStackTrace();
                 throw e;
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     static private volatile List<LbAlgorithm> algorithms = null;
     
     @Override
     public @Nonnull Iterable<LbAlgorithm> listSupportedAlgorithms() {
         List<LbAlgorithm> list = algorithms;
         
         if( list == null ) {
             list = new ArrayList<LbAlgorithm>();
             list.add(LbAlgorithm.ROUND_ROBIN);
             list.add(LbAlgorithm.LEAST_CONN);
             list.add(LbAlgorithm.SOURCE);
             algorithms = Collections.unmodifiableList(list);
         }
         return algorithms;
     }
 
     @Override
     public @Nonnull Iterable<LbEndpointType> listSupportedEndpointTypes() throws CloudException, InternalException {
         return Collections.singletonList(LbEndpointType.VM);
     }
 
     @Override
     public @Nonnull Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
         return Collections.singletonList(IPVersion.IPV4);
     }
 
     @Override
     public @Nonnull Iterable<LbPersistence> listSupportedPersistenceOptions() throws CloudException, InternalException {
         return Collections.singletonList(LbPersistence.NONE);
     }
 
     static private volatile List<LbProtocol> protocols = null;
     
     @Override
     public @Nonnull Iterable<LbProtocol> listSupportedProtocols() {
         if( protocols == null ) {
             List<LbProtocol> list = new ArrayList<LbProtocol>();
 
             list.add(LbProtocol.RAW_TCP);
             protocols = Collections.unmodifiableList(list);
         }
         return protocols;
     }
     
     private @Nonnull Collection<String> getServersAt(String ruleId) throws InternalException, CloudException {
         ArrayList<String> ids = new ArrayList<String>();
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(LIST_LOAD_BALANCER_RULE_INSTANCES, new Param("id", ruleId)), LIST_LOAD_BALANCER_RULE_INSTANCES);
         NodeList instances = doc.getElementsByTagName("loadbalancerruleinstance");     
         
         for( int i=0; i<instances.getLength(); i++ ) {
             Node node = instances.item(i);
             NodeList attributes = node.getChildNodes();
             
             for( int j=0; j<attributes.getLength(); j++ ) {
                 Node n = attributes.item(j);
                 
                 if( n.getNodeName().equals("id") ) {
                     ids.add(n.getFirstChild().getNodeValue());
                 }
             }
         }
         return ids;
     }
     
     private @Nullable String getVmOpsRuleId(@Nonnull LbAlgorithm lbAlgorithm, @Nonnull String publicIp, int publicPort, int privatePort) throws CloudException, InternalException {
         String ruleId = null;
         String algorithm;
 
         switch( lbAlgorithm ) {
             case ROUND_ROBIN: algorithm = "roundrobin"; break;
             case LEAST_CONN: algorithm = "leastconn"; break;
             case SOURCE: algorithm = "source"; break;
             default: algorithm = "roundrobin"; break;
         }
         boolean isId = isId(publicIp);
         String key = (isId ? "publicIpId" : "publicIp");
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(LIST_LOAD_BALANCER_RULES, new Param(key, publicIp)), LIST_LOAD_BALANCER_RULES);
         NodeList rules = doc.getElementsByTagName("loadbalancerrule");
         
         for( int i=0; i<rules.getLength(); i++ ) {
             Node node = rules.item(i);
             NodeList attributes = node.getChildNodes();
             boolean isIt = true;
             String id = null;
             
             for( int j=0; j<attributes.getLength(); j++ ) {
                 Node n = attributes.item(j);
                 String name = n.getNodeName().toLowerCase();
                 String value;
                 
                 if( n.getChildNodes().getLength() > 0 ) {
                     value = n.getFirstChild().getNodeValue();
                 }
                 else {
                     value = null;
                 }
                 if( name.equals("publicip") ) {
                     if( value != null && !value.equals(publicIp) ) {
                         isIt = false;
                         break;
                     }
                 }
                 else if( name.equals("publicport") ) {
                     if( value == null || publicPort != Integer.parseInt(value) ) {
                         isIt = false;
                         break;
                     }
                 }
                 else if( name.equals("privateport") ) {
                     if( value == null || privatePort != Integer.parseInt(value) ) {
                         isIt = false;
                         break;
                     }
                 }
                 else if( name.equals("algorithm") ) {
                     if( value == null || !value.equals(algorithm) ) {
                         isIt = false;
                         break;
                     }
                 }
                 else if( name.equals("id") ) {
                     id = value;
                 }
             }
             if( isIt ) {
                 ruleId = id;
                 break;
             }
         }
         return ruleId;
     }
 
     @Override
     public boolean isAddressAssignedByProvider() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isDataCenterLimited() {
         return false;
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(provider, "LB.isSubscribed");
         try {
             CSMethod method = new CSMethod(provider);
 
             try {
                 method.get(method.buildUrl(LIST_LOAD_BALANCER_RULES), LIST_LOAD_BALANCER_RULES);
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
     public @Nonnull Iterable<LoadBalancerEndpoint> listEndpoints(@Nonnull String loadBalancerId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "LB.listEndpoints");
         try {
             LoadBalancer lb = getLoadBalancer(loadBalancerId);
 
             if( lb == null ) {
                 return Collections.emptyList();
             }
             ArrayList<LoadBalancerEndpoint> endpoints = new ArrayList<LoadBalancerEndpoint>();
 
             //noinspection deprecation
             for( String serverId : lb.getProviderServerIds() ) {
                 VirtualMachine vm = getProvider().getComputeServices().getVirtualMachineSupport().getVirtualMachine(serverId);
 
                 endpoints.add(LoadBalancerEndpoint.getInstance(LbEndpointType.VM, serverId, vm != null && vm.getCurrentState().equals(VmState.RUNNING) ? LbEndpointState.ACTIVE : LbEndpointState.INACTIVE));
             }
             return endpoints;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean supportsMonitoring() {
         return false;
     }
 
     @Override
     public boolean supportsMultipleTrafficTypes() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public @Nonnull Iterable<LoadBalancer> listLoadBalancers() throws CloudException, InternalException {
         APITrace.begin(provider, "LB.listLoadBalancers");
         try {
             HashMap<String,LoadBalancer> matches = new HashMap<String,LoadBalancer>();
             CSMethod method = new CSMethod(provider);
 
             try {
                 Document doc = method.get(method.buildUrl(LIST_LOAD_BALANCER_RULES), LIST_LOAD_BALANCER_RULES);
                 NodeList rules = doc.getElementsByTagName("loadbalancerrule");
 
                 for( int i=0; i<rules.getLength(); i++ ) {
                     Node node = rules.item(i);
 
                     toRule(node, matches);
                 }
                 ArrayList<LoadBalancer> results = new ArrayList<LoadBalancer>();
 
                 for( LoadBalancer lb : matches.values() ) {
                     if( matchesRegion(lb.getProviderLoadBalancerId()) ) {
                         results.add(lb);
                     }
                 }
                 return results;
             }
             catch( CloudException e ) {
                 if( e.getHttpCode() == HttpServletResponse.SC_NOT_FOUND ) {
                     return Collections.emptyList();
                 }
                 e.printStackTrace();
                 throw e;
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     boolean matchesRegion(@Nonnull String addressId) throws InternalException, CloudException {
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl("listPublicIpAddresses", new Param(isId(addressId) ? "ipAddressId" : "ipAddress", addressId)), "listPublicIpAddresses");
 
         NodeList matches = doc.getElementsByTagName("publicipaddress");
         for( int i=0; i<matches.getLength(); i++ ) {
             Node n = matches.item(i);
             NodeList attributes = n.getChildNodes();
             
             for( int j=0; j<attributes.getLength(); j++ ) {
                 Node child = attributes.item(j);
                 String name = child.getNodeName().toLowerCase();
                 String value;
                 
                 if( child.getChildNodes().getLength() > 0 ) {
                     value = child.getFirstChild().getNodeValue();
                 }
                 else {
                     value = null;
                 }
                 if( name.equalsIgnoreCase("zoneid") ) {
                     return (value != null && value.equalsIgnoreCase(getContext().getRegionId()));
                 }
             }
         }
         return false;
     }
     
     @SuppressWarnings("deprecation")
     @Override
     @Deprecated
     public void remove(@Nonnull String loadBalancerId) throws CloudException, InternalException {
         removeLoadBalancer(loadBalancerId);
     }
 
     @Override
     public void removeLoadBalancer(@Nonnull String loadBalancerId) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.remove");
         try {
             LoadBalancer lb = getLoadBalancer(loadBalancerId);
 
             if( lb == null || lb.getListeners().length < 1 ) {
                 return;
             }
             for( LbListener listener : lb.getListeners() ) {
                 String ruleId = getVmOpsRuleId(listener.getAlgorithm(), lb.getAddress(), listener.getPublicPort(), listener.getPrivatePort());
 
                 if( ruleId != null ) {
                     removeVmOpsRule(ruleId);
                 }
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void removeServers(@Nonnull String toLoadBalancerId, @Nonnull String ... serverIds) throws CloudException, InternalException {
         APITrace.begin(provider, "LB.removeServers");
         try {
             try {
                 LoadBalancer lb = getLoadBalancer(toLoadBalancerId);
 
                 if( lb == null ) {
                     throw new CloudException("No such load balancer: " + toLoadBalancerId);
                 }
                 StringBuilder ids = new StringBuilder();
 
                 for( int i=0; i<serverIds.length; i++ ) {
                     ids.append(serverIds[i]);
                     if( i < serverIds.length-1 ) {
                         ids.append(",");
                     }
                 }
                 for( LbListener listener : lb.getListeners() ) {
                     String ruleId = getVmOpsRuleId(listener.getAlgorithm(), toLoadBalancerId, listener.getPublicPort(), listener.getPrivatePort());
                     CSMethod method = new CSMethod(provider);
                     Document doc = method.get(method.buildUrl(REMOVE_FROM_LOAD_BALANCER_RULE, new Param("id", ruleId), new Param("virtualMachineIds", ids.toString())), REMOVE_FROM_LOAD_BALANCER_RULE);
 
                     provider.waitForJob(doc, "Remove Server");
                 }
             }
             catch( RuntimeException e ) {
                 throw new InternalException(e);
             }
             catch( Error e ) {
                 throw new InternalException(e);
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean supportsAddingEndpoints() throws CloudException, InternalException {
         return true;
     }
 
     private void removeVmOpsRule(@Nonnull String ruleId) throws CloudException, InternalException {
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(DELETE_LOAD_BALANCER_RULE, new Param("id", ruleId)), DELETE_LOAD_BALANCER_RULE);
 
         provider.waitForJob(doc, "Remove Load Balancer Rule");
     }
     
     private void toRule(@Nullable Node node, @Nonnull Map<String,LoadBalancer> current) throws InternalException, CloudException {
         NodeList attributes = node.getChildNodes();
         int publicPort = -1, privatePort = -1;
         LbAlgorithm algorithm = null;
         String publicIp = null;
         String ruleId = null;
        String lbName = null;
        String lbDesc = null;
         
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
             if( name.equals("publicip") ) {
                 publicIp = value;
             }
             else if( name.equals("id") ) {
                 ruleId = value;
             }
             else if( name.equals("publicport") && value != null ) {
                 publicPort = Integer.parseInt(value);
             }
             else if( name.equals("privateport") && value != null ) {
                 privatePort = Integer.parseInt(value);
             }
             else if( name.equals("algorithm") ) {
                 if( value == null || value.equals("roundrobin") ) {
                     algorithm = LbAlgorithm.ROUND_ROBIN;
                 }
                 else if( value.equals("leastconn") ) {
                     algorithm = LbAlgorithm.LEAST_CONN;
                 }
                 else if( value.equals("") ) {
                     algorithm = LbAlgorithm.SOURCE;
                 }
                 else {
                     algorithm = LbAlgorithm.ROUND_ROBIN;            
                 }
             }
            else if (name.equals("name")) {
                lbName = value;
            }
            else if (name.equals("description")) {
                lbDesc = value;
            }
         }
         LbListener listener = LbListener.getInstance(algorithm, LbPersistence.NONE, LbProtocol.RAW_TCP, publicPort, privatePort);
         Collection<String> serverIds = getServersAt(ruleId);
 
         if( current.containsKey(publicIp) ) {
             LoadBalancer lb = current.get(publicIp);
 
             @SuppressWarnings("deprecation") String[] currentIds = lb.getProviderServerIds();
             LbListener[] listeners = lb.getListeners();
             TreeSet<Integer> ports = new TreeSet<Integer>();
 
             for( int port : lb.getPublicPorts() ) {
                 ports.add(port);
             }
             ports.add(publicPort);
             
             int[] portList = new int[ports.size()];
             int i = 0;
             
             for( Integer p : ports ) {
                 portList[i++] = p;
             }
             //noinspection deprecation
             lb.setPublicPorts(portList);
 
             boolean there = false;
             
             for( LbListener l : listeners ) {
                 if( l.getAlgorithm().equals(listener.getAlgorithm()) ) {
                     if( l.getNetworkProtocol().equals(listener.getNetworkProtocol()) ) {
                         if( l.getPublicPort() == listener.getPublicPort() ) {
                             if( l.getPrivatePort() == listener.getPrivatePort() ) {
                                 there = true;
                                 break;
                             }
                         }
                     }
                 }
             }
             if( !there ) {
                 lb.withListeners(listener);
             }
             
             TreeSet<String> newIds = new TreeSet<String>();
 
             Collections.addAll(newIds, currentIds);
             for( String id : serverIds ) {
                 newIds.add(id);
             }
             //noinspection deprecation
             lb.setProviderServerIds(newIds.toArray(new String[newIds.size()]));
            //noinspection deprecation
            lb.setName(lbName);
            //noinspection deprecation
            lb.setDescription(lbDesc);
         }
         else {
             Collection<DataCenter> dcs = provider.getDataCenterServices().listDataCenters(provider.getContext().getRegionId());
             String[] ids = new String[dcs.size()];
             int i =0;
             
             for( DataCenter dc : dcs ) {
                 ids[i++] = dc.getProviderDataCenterId();
             }
 
            LoadBalancer lb = LoadBalancer.getInstance(getContext().getAccountNumber(), getContext().getRegionId(), publicIp, LoadBalancerState.ACTIVE, lbName, lbDesc, LoadBalancerAddressType.IP, publicIp, publicPort).withListeners(listener).operatingIn(ids);
 
             //noinspection deprecation
             lb.setProviderServerIds(serverIds.toArray(new String[serverIds.size()]));
             current.put(publicIp, lb);
         }
     }
 
     /*
     public @Nullable String getLoadBalancerForAddress(@Nonnull String address) throws InternalException, CloudException {
         APITrace.begin(provider, "LB.getLoadBalancerForAddress");
         try {
             boolean isId = isId(address);
             String key = (isId ? "publicIpId" : "publicIp");
             CSMethod method = new CSMethod(provider);
 
             Document doc = method.get(method.buildUrl(LIST_LOAD_BALANCER_RULES, new Param[] { new Param(key, address) }), LIST_LOAD_BALANCER_RULES);
             NodeList rules = doc.getElementsByTagName("loadbalancerrule");
 
             if( rules.getLength() > 0 ) {
                 return address;
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
     */
 }
