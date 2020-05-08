 package org.dasein.cloud.vcloud.network;
 
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.dc.DataCenter;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.network.VLANState;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.cloud.vcloud.vCloud;
 import org.dasein.cloud.vcloud.vCloudMethod;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 
 /**
  * Implements support for vCloud networking.
  * <p>Created by George Reese: 9/17/12 10:59 AM</p>
  * @author George Reese
  * @version 2013.04 initial version
  * @since 2013.04
  */
 public class HybridVLANSupport extends DefunctVLAN {
     HybridVLANSupport(@Nonnull vCloud provider) {
         super(provider);
     }
 
     @Override
     public boolean allowsNewVlanCreation() throws CloudException, InternalException {
         // TODO: change me when implemented
         return false;
     }
 
     @Override
     public @Nonnull VLAN createVlan(@Nonnull String cidr, @Nonnull String name, @Nonnull String description, @Nonnull String domainName, @Nonnull String[] dnsServers, @Nonnull String[] ntpServers) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Cannot create VLANs");
     }
 
     @Override
     public int getMaxVlanCount() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "getMaxVlanCount");
         try {
             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
 
             return method.getNetworkQuota();
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForNetworkInterface(@Nonnull Locale locale) {
         return "network interface";
     }
 
     @Override
     public @Nonnull String getProviderTermForSubnet(@Nonnull Locale locale) {
         return "subnet";
     }
 
     @Override
     public @Nonnull String getProviderTermForVlan(@Nonnull Locale locale) {
         return "network";
     }
 
     @Override
     public VLAN getVlan(@Nonnull String vlanId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "getVlan");
         try {
             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
 
             for( DataCenter dc : method.listDataCenters() ) {
                 VLAN vlan = toVlan(dc.getProviderDataCenterId(), vlanId);
 
                 if( vlan != null ) {
                     return vlan;
                 }
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "isSubscribedVLAN");
         try {
             return (getProvider().testContext() != null);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean isVlanDataCenterConstrained() throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public @Nonnull Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
         return Collections.singletonList(IPVersion.IPV4);
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listVlanStatus() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "listVlanStatus");
         try {
             ArrayList<ResourceStatus> status = new ArrayList<ResourceStatus>();
 
             for( VLAN vlan : listVlans() ) {
                 status.add(new ResourceStatus(vlan.getProviderVlanId(), vlan.getCurrentState()));
             }
             return status;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<VLAN> listVlans() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "listVlans");
         try {
             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
             ArrayList<VLAN> vlans = new ArrayList<VLAN>();
 
             for( DataCenter dc : method.listDataCenters() ) {
                 String xml = method.get("vdc", dc.getProviderDataCenterId());
 
                 if( xml != null && !xml.equals("") ) {
                     NodeList vdcs = method.parseXML(xml).getElementsByTagName("Vdc");
 
                     if( vdcs.getLength() > 0 ) {
                         NodeList attributes = vdcs.item(0).getChildNodes();
 
                         for( int i=0; i<attributes.getLength(); i++ ) {
                             Node attribute = attributes.item(i);
 
                             if( attribute.getNodeName().equalsIgnoreCase("AvailableNetworks") && attribute.hasChildNodes() ) {
                                 NodeList resources = attribute.getChildNodes();
 
                                 for( int j=0; j<resources.getLength(); j++ ) {
                                     Node resource = resources.item(j);
 
                                     if( resource.getNodeName().equalsIgnoreCase("Network") && resource.hasAttributes() ) {
                                         Node href = resource.getAttributes().getNamedItem("href");
 
                                         VLAN vlan = toVlan(dc.getProviderDataCenterId(), ((vCloud) getProvider()).toID(href.getNodeValue().trim()));
 
                                         if( vlan != null ) {
                                             vlans.add(vlan);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             return vlans;
         }
         finally {
             APITrace.end();
         }
     }
 
     private @Nullable VLAN toVlan(@Nonnull String vdcId, @Nonnull String id) throws InternalException, CloudException {
         vCloudMethod method = new vCloudMethod((vCloud)getProvider());
 
         String xml = method.get("network", id);
 
         if( xml == null || xml.equals("") ) {
             return null;
         }
         Document doc = method.parseXML(xml);
         NodeList nets = doc.getElementsByTagName("OrgVdcNetwork");
 
         if( nets.getLength() < 1 ) {
             nets = doc.getElementsByTagName("OrgNetwork");
             if( nets.getLength() < 1 ) {
                 nets = doc.getElementsByTagName("Network");
                 if( nets.getLength() < 1 ) {
                     return null;
                 }
             }
         }
         Node netNode = nets.item(0);
         NodeList attributes = netNode.getChildNodes();
         VLAN vlan = new VLAN();
 
         vlan.setProviderVlanId(id);
         vlan.setProviderDataCenterId(vdcId);
         vlan.setProviderRegionId(getContext().getRegionId());
         vlan.setProviderOwnerId(getContext().getAccountNumber());
         vlan.setSupportedTraffic(new IPVersion[] { IPVersion.IPV4 });
         vlan.setCurrentState(VLANState.AVAILABLE);
 
         Node n;
 
         /*
         n = netNode.getAttributes().getNamedItem("status");
         if( n == null ) {
             vlan.setCurrentState(VLANState.AVAILABLE);
         }
         else {
             vlan.setCurrentState(toState(n.getNodeValue().trim()));
         }
         */
         n = netNode.getAttributes().getNamedItem("name");
         if( n != null ) {
             vlan.setName(n.getNodeValue().trim());
             vlan.setDescription(n.getNodeValue().trim());
         }
 
         HashMap<String,String> tags = new HashMap<String, String>();
         String gateway = null;
         String netmask = null;
         boolean shared = false;
         String fenceMode = null;
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
 
             if( attribute.getNodeName().equals("Description") && attribute.hasChildNodes() ) {
                 shared = attribute.getFirstChild().getNodeValue().trim().equalsIgnoreCase("true");
             }
             else if( attribute.getNodeName().equals("IsShared") && attribute.hasChildNodes() ) {
                 vlan.setDescription(attribute.getFirstChild().getNodeValue().trim());
             }
             else if( attribute.getNodeName().equals("Features") && attribute.hasChildNodes() ) {
                 NodeList list = attribute.getChildNodes();
 
                 for( int j=0; j<list.getLength(); j++ ) {
                     Node feature = list.item(j);
 
                     if( feature.getNodeName().equalsIgnoreCase("FenceMode") && feature.hasChildNodes() ) {
                         fenceMode = feature.getFirstChild().getNodeValue().trim();
                     }
                 }
             }
             else if( attribute.getNodeName().equals("Configuration") && attribute.hasChildNodes() ) {
                 NodeList scopesList = attribute.getChildNodes();
                 String[] dns = new String[10];
                 String ipStart = null;
                 String ipEnd = null;
                 String domain = null;
                 Boolean enabled = null;
 
                 for( int j=0; j<scopesList.getLength(); j++ ) {
                     Node scopesNode = scopesList.item(j);
 
                     if( scopesNode.getNodeName().equalsIgnoreCase("FenceMode") && scopesNode.hasChildNodes() ) {
                         fenceMode = scopesNode.getFirstChild().getNodeValue().trim();
                     }
                     else if( (scopesNode.getNodeName().equalsIgnoreCase("IpScope") || scopesNode.getNodeName().equalsIgnoreCase("IpScopes")) && scopesNode.hasChildNodes() ) {
                         Node scope = null;
 
                         if( scopesNode.getNodeName().equalsIgnoreCase("IpScope") ) {
                             scope = scopesNode;
                         }
                         else {
                             NodeList scopes = scopesNode.getChildNodes();
 
                             for( int k=0; k<scopes.getLength(); k++ ) {
                                 Node node = scopes.item(k);
 
                                 if( node.getNodeName().equalsIgnoreCase("IpScope") ) {
                                     scope = node;
                                     break;
                                 }
                             }
                         }
                         if( scope != null ) {
                             NodeList saList = scope.getChildNodes();
 
                             for( int l=0; l<saList.getLength(); l++ ) {
                                 Node sa = saList.item(l);
 
                                 if( sa.getNodeName().equalsIgnoreCase("Gateway") && sa.hasChildNodes() ) {
                                     gateway = sa.getFirstChild().getNodeValue().trim();
                                 }
                                 else if( sa.getNodeName().equalsIgnoreCase("Netmask") && sa.hasChildNodes() ) {
                                     netmask = sa.getFirstChild().getNodeValue().trim();
                                 }
                                 else if( sa.getNodeName().equalsIgnoreCase("DnsSuffix") && sa.hasChildNodes() ) {
                                     domain = sa.getFirstChild().getNodeValue().trim();
                                 }
                                 else if( sa.getNodeName().startsWith("Dns") && sa.hasChildNodes() ) {
                                     String ns = sa.getFirstChild().getNodeValue().trim();
 
                                     if( sa.getNodeName().equals("Dns") ) {
                                         dns[0] = ns;
                                     }
                                     else {
                                         try {
                                             int idx = Integer.parseInt(sa.getNodeName().substring(3));
 
                                             dns[idx] = ns;
                                         }
                                         catch( NumberFormatException e ) {
                                             for(int z=0; i<dns.length; z++ ) {
                                                 if( dns[z] == null ) {
                                                     dns[z] = ns;
                                                     break;
                                                 }
                                             }
                                         }
                                     }
                                 }
                                 else if( sa.getNodeName().equalsIgnoreCase("IsEnabled") && sa.hasChildNodes() ) {
                                     enabled = sa.getFirstChild().getNodeValue().trim().equalsIgnoreCase("true");
                                 }
                                 else if( sa.getNodeName().equalsIgnoreCase("IpRanges") && sa.hasChildNodes() ) {
                                     NodeList rangesList = sa.getChildNodes();
 
                                     for( int m=0; m<rangesList.getLength(); m++ ) {
                                         Node ranges = rangesList.item(m);
 
                                         if( ranges.getNodeName().equalsIgnoreCase("IpRanges") && ranges.hasChildNodes() ) {
                                             NodeList rangeList = ranges.getChildNodes();
 
                                             for( int o=0; o<rangeList.getLength(); o++ ) {
                                                 Node range = rangeList.item(o);
 
                                                 if( range.getNodeName().equalsIgnoreCase("IpRange") && range.hasChildNodes() ) {
                                                     NodeList addresses = range.getChildNodes();
 
                                                     for( int p=0; p<addresses.getLength(); p++ ) {
                                                         Node address = addresses.item(p);
 
                                                         if( address.getNodeName().equalsIgnoreCase("StartAddress") && address.hasChildNodes() ) {
                                                             ipStart = address.getFirstChild().getNodeValue().trim();
                                                         }
                                                         else if( address.getNodeName().equalsIgnoreCase("EndAddress") && address.hasChildNodes() ) {
                                                             ipEnd = address.getFirstChild().getNodeValue().trim();
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                     else if( attribute.getNodeName().equalsIgnoreCase("Gateway") && attribute.hasChildNodes() ) {
                         gateway = attribute.getFirstChild().getNodeValue().trim();
                     }
                     else if( attribute.getNodeName().equalsIgnoreCase("Netmask") && attribute.hasChildNodes() ) {
                         netmask = attribute.getFirstChild().getNodeValue().trim();
                     }
                 }
                 ArrayList<String> dnsServers = new ArrayList<String>();
 
                 for( String ns : dns ) {
                     if( ns != null ) {
                         dnsServers.add(ns);
                     }
                 }
                 vlan.setDnsServers(dnsServers.toArray(new String[dnsServers.size()]));
                 vlan.setCurrentState(enabled == null || enabled ? VLANState.AVAILABLE : VLANState.PENDING);
                 if( domain != null ) {
                     vlan.setDomainName(domain);
                 }
                 if( ipStart != null ) {
                     tags.put("ipStart", ipStart);
                 }
                 if( ipEnd != null ) {
                     tags.put("ipEnd", ipEnd);
                 }
             }
         }
         if( fenceMode != null ) {
             // isolated
             // bridged
             // natRouted
             tags.put("fenceMode", fenceMode);
         }
         if( gateway != null ) {
             tags.put("gateway", gateway);
         }
         if( netmask != null ) {
             tags.put("netmask", netmask);
         }
         if( netmask != null && gateway != null ) {
            vlan.setCidr(netmask, gateway);
         }
         tags.put("shared", String.valueOf(shared));
         if( vlan.getName() == null ) {
             vlan.setName(vlan.getProviderVlanId());
         }
         if( vlan.getDescription() == null ) {
             vlan.setDescription(vlan.getName());
         }
         vlan.setTags(tags);
         return vlan;
     }
 
     @Override
     public void removeVlan(String vlanId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "removeVlan");
         try {
             throw new OperationNotSupportedException("VLAN deletion not yet supported");
         }
         finally {
             APITrace.end();
         }
     }
 
     public void setCidr(@Nonnull VLAN vlan, @Nonnull String netmask, @Nonnull String anAddress) {
         String[] dots = netmask.split("\\.");
         int cidr = 0;
 
         for( String item : dots ) {
             int x = Integer.parseInt(item);
 
             for( ; x > 0 ; x = (x<<1)%256 ) {
                 cidr++;
             }
         }
         StringBuilder network = new StringBuilder();
 
         dots = anAddress.split("\\.");
         int start = 0;
 
         for( String item : dots ) {
             if( ((start+8) < cidr) || cidr == 0 ) {
                 network.append(item);
             }
             else {
                 int addresses = (int)Math.pow(2, (start+8)-cidr);
                 int subnets = 256/addresses;
                 int gw = Integer.parseInt(item);
 
                 for( int i=0; i<subnets; i++ ) {
                     int base = i*addresses;
                     int top = ((i+1)*addresses);
 
                     if( gw >= base && gw < top ) {
                         network.append(String.valueOf(base));
                         break;
                     }
                 }
             }
             start += 8;
             if( start < 32 ) {
                 network.append(".");
             }
         }
         network.append("/");
         network.append(String.valueOf(cidr));
         vlan.setCidr(network.toString());
     }
 }
