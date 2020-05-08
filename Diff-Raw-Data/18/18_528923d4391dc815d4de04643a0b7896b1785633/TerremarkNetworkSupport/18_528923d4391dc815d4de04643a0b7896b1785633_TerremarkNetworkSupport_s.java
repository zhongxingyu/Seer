 package org.dasein.cloud.terremark.network;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Locale;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.cloud.network.NICCreateOptions;
 import org.dasein.cloud.network.NICState;
 import org.dasein.cloud.network.NetworkInterface;
 import org.dasein.cloud.network.Networkable;
 import org.dasein.cloud.network.RawAddress;
 import org.dasein.cloud.network.RoutingTable;
 import org.dasein.cloud.network.Subnet;
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.network.VLANState;
 import org.dasein.cloud.network.VLANSupport;
 import org.dasein.cloud.terremark.EnvironmentsAndComputePools;
 import org.dasein.cloud.terremark.Terremark;
 import org.dasein.cloud.terremark.TerremarkException;
 import org.dasein.cloud.terremark.TerremarkMethod;
 import org.dasein.cloud.terremark.TerremarkMethod.HttpMethodName;
 import org.dasein.cloud.terremark.compute.VMSupport;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class TerremarkNetworkSupport  implements VLANSupport {
 
 	// API Calls
 	public final static String NETWORKS       = "networks";
 	public final static String NETWORK_HOSTS  = "networkHosts";
 
 	// Response Tags
 	public final static String NETWORKS_TAG      = "Networks";
 	public final static String NETWORK_TAG       = "Network";
 	public final static String NETWORK_HOST_TAG  = "NetworkHost";
 
 	// Types
 	public final static String NETWORK_TYPE       = "application/vnd.tmrk.cloud.network";
 	public final static String NETWORK_IPV6_TYPE  = "application/vnd.tmrk.cloud.network.ipv6";
 	public final static String NETWORK_HOST_TYPE  = "application/vnd.tmrk.cloud.networkHost";
 
 	static Logger logger = Terremark.getLogger(TerremarkNetworkSupport.class);
 
 	private Terremark provider;
 
 	TerremarkNetworkSupport(Terremark provider) { this.provider = provider; }
 
 	/**
 	 * Adds the specified route to the specified routing table.
 	 * @param toRoutingTableId the routing table to which the route will be added
 	 * @param version ipv4 or ipv6
 	 * @param destinationCidr the destination IP address or CIDR, or null if setting the default route
 	 * @param address the IP address to which the traffic is being routed
 	 * @throws CloudException an error occurred in the cloud while adding the route to the routing table
 	 * @throws InternalException a local error occurred processing the request to add the route
 	 */
 	@Override
 	public void addRouteToAddress(String toRoutingTableId, IPVersion version, String destinationCidr, String address) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Adds the specified route to the specified routing table.
 	 * @param toRoutingTableId the routing table to which the route will be added
 	 * @param version ipv4 or ipv6
 	 * @param destinationCidr the destination IP address or CIDR, or null if setting the default route
 	 * @param gatewayId the ID of a known gateway
 	 * @throws CloudException an error occurred in the cloud while adding the route to the routing table
 	 * @throws InternalException a local error occurred processing the request to add the route
 	 */
 	@Override
 	public void addRouteToGateway(String toRoutingTableId, IPVersion version, String destinationCidr, String gatewayId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Adds the specified route to the specified routing table.
 	 * @param toRoutingTableId the routing table to which the route will be added
 	 * @param version ipv4 or ipv6
 	 * @param destinationCidr the destination IP address or CIDR, or null if setting the default route
 	 * @param nicId the ID of a known network interface
 	 * @throws CloudException an error occurred in the cloud while adding the route to the routing table
 	 * @throws InternalException a local error occurred processing the request to add the route
 	 */
 	@Override
 	public void addRouteToNetworkInterface(String toRoutingTableId, IPVersion version, String destinationCidr, String nicId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Adds the specified route to the specified routing table.
 	 * @param toRoutingTableId the routing table to which the route will be added
 	 * @param version ipv4 or ipv6
 	 * @param destinationCidr the destination IP address or CIDR, or null if setting the default route
 	 * @param vmId the unique ID of the virtual machine to which traffic is being routed
 	 * @throws CloudException an error occurred in the cloud while adding the route to the routing table
 	 * @throws InternalException a local error occurred processing the request to add the route
 	 */
 	@Override
 	public void addRouteToVirtualMachine(String toRoutingTableId, IPVersion version, String destinationCidr, String vmId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Indicates that users may self-provision network interfaces. If false, either network interfaces are not supported
 	 * or they cannot be self-provisioned
 	 * @return true if users can self-provision network interfaces
 	 * @throws CloudException an error occurred checking with the cloud if network interfaces may be self provisioned
 	 * @throws InternalException an error occurred in the Dasein Cloud implementation determining support
 	 */
 	@Override
 	public boolean allowsNewNetworkInterfaceCreation() throws CloudException, InternalException {
 		return false;
 	}
 
 	@Override
 	public boolean allowsNewSubnetCreation() throws CloudException, InternalException{
 		return false;
 	}
 
 	@Override
 	public boolean allowsNewVlanCreation() throws CloudException, InternalException {
 		return false;
 	}
 
 	/**
 	 * Assigns the specified routing table to the target subnet.
 	 * @param subnetId the unique ID of the subnet being assigned the routing table
 	 * @param routingTableId the routing table to which the subnet is being assigned
 	 * @throws CloudException an error occurred with the cloud provider assigning the routing table
 	 * @throws InternalException a local error occurred while processing the request
 	 */
 	@Override
 	public void assignRoutingTableToSubnet(String subnetId, String routingTableId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Assigns the specified routing table to the target VLAN (or makes it the main routing table among the routing tables)
 	 * @param vlanId the VLAN to which the routing table is being assigned
 	 * @param routingTableId the unique ID of the routing table being assigned
 	 * @throws CloudException an error occurred with the cloud provider assigning the routing table
 	 * @throws InternalException a local error occurred while processing the request
 	 */
 	@Override
 	public void assignRoutingTableToVlan(String vlanId, String routingTableId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Attaches a network interface to an existing virtual machine.
 	 * @param nicId the unique ID of the network interface to attach
 	 * @param vmId the virtual machine to which the network interface should be attached
 	 * @param index the 1-based index (-1 meaning at the end) for the attached interface 
 	 * @throws CloudException an error occurred with the cloud provider attaching the interface                                                           
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation attaching the interface
 	 */
 	@Override
 	public void attachNetworkInterface(String nicId, String vmId, int index) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Attaching a network host is not supported.");
 	}
 
 	/**
 	 * Creates an Internet gateway for the specified VLAN. This method makes sense only if the cloud supports enabling Internet routing from VLANs.
 	 * @param forVlanId the unique ID of the VLAN to create an Internet gateway for
 	 * @return an ID of the newly created gateway in clouds that allow gateway tracking, or null if gateways are not tracked
 	 * @throws CloudException an error occurred in the cloud while setting up the Internet gateway
 	 * @throws InternalException a local error occurred while setting up the Internet gateway
 	 * @throws OperationNotSupportedException this cloud does not allow enabling Internet routing from VLANs either because all VLANs are automatically routed or are never routed
 	 */
 	@Override
 	public String createInternetGateway(String forVlanId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Creating internet gateways not supported.");
 	}
 
 	/**
 	 * Provisions a new network interface in accordance with the specified create options. 
 	 * @param options the options to be used in creating the network interface
 	 * @return the newly provisioned network interface
 	 * @throws CloudException an error occurred in the cloud while provisioning the interface
 	 * @throws InternalException a local error occurred during the provisoning of the interface
 	 * @throws OperationNotSupportedException if {@link #allowsNewNetworkInterfaceCreation()} is false
 	 */
 	@Override
 	public NetworkInterface createNetworkInterface(NICCreateOptions options) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Creating network interfaces is not supported.");
 	}
 
 	/**
 	 * Creates a new routing table for the target VLAN.
 	 * @param forVlanId the VLAN for which a routing table is being created
 	 * @param name the name of the new routing table
 	 * @param description a description for the new routing table
 	 * @return a unique ID within the cloud for the specified routing table
 	 * @throws CloudException an error occurred with the cloud provider while creating the routing table
 	 * @throws InternalException a local error occurred creating the routing table
 	 */
 	@Override
 	public String createRoutingTable(String forVlanId, String name, String description) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	@Override
 	public Subnet createSubnet(String cidr, String inProviderVlanId, String name, String description) throws CloudException, InternalException{
 		throw new OperationNotSupportedException("Subnets not supported.");
 	}
 
 	@Override
 	public VLAN createVlan(String cidr, String name, String description, String domainName, String[] dnsServers, String[] ntpServers) throws CloudException, InternalException{
 		throw new OperationNotSupportedException("Network provisioning is not supported");
 	}
 
 	/**
 	 * Detaches the specified network interface from any virtual machine it might be attached to.
 	 * @param nicId the unique ID of the network interface to be detached
 	 * @throws CloudException an error occurred with the cloud provider while detaching the network interface
 	 * @throws InternalException a local error occurred while detaching the network interface
 	 */
 	@Override
 	public void detachNetworkInterface(String nicId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Detaching a network host is not supported.");
 	}
 
 	/**
 	 * Specifies the maximum number of network interfaces that may be provisioned.
 	 * @return the maximum number of network interfaces that may be provisioned or -1 for no limit or -2 for unknown
 	 * @throws CloudException an error occurred requesting the limit from the cloud provider
 	 * @throws InternalException a local error occurred figuring out the limit
 	 */
 	@Override
 	public int getMaxNetworkInterfaceCount() throws CloudException, InternalException {
 		return -2;
 	}
 
 	@Override
 	public int getMaxVlanCount() throws CloudException, InternalException{
 		return -2;
 	}
 
 	/**
 	 * Fetches the network interfaced specified by the unique network interface ID.
 	 * @param nicId the unique ID of the desired network interface
 	 * @return the network interface that matches the specified ID
 	 * @throws CloudException an error occurred in the cloud provider fetching the desired network interface
 	 * @throws InternalException a local error occurred while fetching the desired network interface
 	 */
 	public @Nonnull NetworkInterface getNetworkInterface(@Nonnull String networkHostId) throws CloudException, InternalException {
 		NetworkInterface networkInterface = null;
		String url = "/" + NETWORK_HOSTS + "/" + networkHostId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		}
 		catch (CloudException e) {
 			logger.debug("Did not find requested network interface");
 		}
 		catch (InternalException i) {
 			logger.debug("Did not find requested network interface");
 		}
 		if (doc != null) {
 			Node networkHostNode = doc.getElementsByTagName(NETWORK_HOST_TAG).item(0);
 			networkInterface = toNetworkInterface(networkHostNode);
 		}
 		return networkInterface;
 	}
 
 	/**
 	 * Identifies the provider term for a network interface.
 	 * @param locale the locale in which the term should be provided
 	 * @return a localized term for "network interface" specific to this cloud provider
 	 */
 	@Override
 	public String getProviderTermForNetworkInterface(Locale locale){
 		return "network host";
 	}
 
 	@Override
 	public String getProviderTermForSubnet(Locale locale){
 		return "";
 	}
 
 	@Override
 	public String getProviderTermForVlan(Locale locale){
 		return "network";
 	}
 
 	/**
 	 * Identifies the routing table that contains the routes for the subnet.
 	 * @param subnetId the unique ID of the subnet for which you are attempting to identify a routing table
 	 * @return the matching routing table or <code>null</code> if the cloud doesn't support routing tables
 	 * @throws CloudException an error occurred loading the routing table for the specified subnet
 	 * @throws InternalException a local error occurred identifying the routing table
 	 * @throws OperationNotSupportedException the cloud does not support subnets
 	 */
 	@Override
 	public RoutingTable getRoutingTableForSubnet(String subnetId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables and subnets not supported.");
 	}
 
 	/**
 	 * Identifies the routing table that supports the routes for the VLAN (when subnets are not supported) or the
 	 * main/default routing table for subnets within the VLAN (when subnets are supported).
 	 * @param vlanId the VLAN ID of the VLAN whose routing table is being sought
 	 * @return the matching routing table or <code>null</code> if the cloud doesn't support routing tables
 	 * @throws CloudException an error occurred loading the routing table for the specified VLAN
 	 * @throws InternalException a local error occurred identifying the routing table
 	 */
 	@Override
 	public RoutingTable getRoutingTableForVlan(String vlanId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Indicates whether or not you may or must manage routing tables for your VLANs/subnets.
 	 * @return the level of routing table management that is required
 	 * @throws CloudException an error occurred in the cloud provider determining support
 	 * @throws InternalException a local error occurred processing the request
 	 */
 	@Override
 	public Requirement getRoutingTableSupport() throws CloudException, InternalException {
 		return Requirement.NONE;
 	}
 
 	@Override
 	public @Nullable Subnet getSubnet(@Nonnull String subnetId) throws CloudException, InternalException{
 		return null;
 	}
 
 	/**
 	 * Indicates whether subnets in VLANs are required, optional, or not supported.
 	 * @return the level of support for subnets in this cloud
 	 * @throws CloudException an error occurred in the cloud while determining the support level
 	 * @throws InternalException a local error occurred determining subnet support level
 	 */
 	@Override
 	public Requirement getSubnetSupport() throws CloudException, InternalException {
 		return Requirement.NONE;
 	}
 
 	@Override
 	public @Nullable VLAN getVlan(@Nonnull String vlanId) throws CloudException, InternalException{
 		VLAN network = null;
 		String url = "/" + NETWORKS + "/" + vlanId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		} catch (TerremarkException e) {
 			logger.warn("Failed to get network " + vlanId);
 		}
 		catch (CloudException e) {
 			logger.warn("Failed to get network " + vlanId);
 		}
 		catch (InternalException e) {
 			logger.warn("Failed to get network " + vlanId);
 		}
 		if (doc != null){
 			Node networkNode = doc.getElementsByTagName(NETWORK_TAG).item(0);
 			if (networkNode != null) {
 				network = toVLAN(networkNode);
 			}
 		}
 		return network;
 	}
 
 	/**
 	 * Indicates whether or not this cloud included the concept of network interfaces in its networking support.
 	 * @return true if this cloud supports network interfaces as part of its networking concepts
 	 * @throws CloudException an error occurred with the cloud provider determining support for network interfaces
 	 * @throws InternalException a local error occurred determining support for network interfaces
 	 */
 	@Override
 	public boolean isNetworkInterfaceSupportEnabled() throws CloudException, InternalException {
 		return true;
 	}
 
 	@Override
 	public boolean isSubnetDataCenterConstrained() throws CloudException, InternalException{
 		return false;
 	}
 
 	@Override
 	public boolean isSubscribed() throws CloudException, InternalException {
 		return true;
 	}
 
 	@Override
 	public boolean isVlanDataCenterConstrained() throws CloudException, InternalException{
 		return false;
 	}
 
 	/**
 	 * Lists the IDs of the firewalls protecting the specified network interface.
 	 * @param nicId the network interface ID of the desired network interface
 	 * @return the firewall/security group IDs of all firewalls supporting this network interface
 	 * @throws CloudException an error occurred with the cloud providing fetching the firewall IDs
 	 * @throws InternalException a local error occurred while attempting to communicate with the cloud
 	 */
 	@Override
 	public Collection<String> listFirewallIdsForNIC(String nicId) throws CloudException, InternalException {
 		Collection<String> firewallIds = new ArrayList<String>();
 		String url = "/" + NETWORK_HOSTS + "/" + nicId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList acls = doc.getElementsByTagName(FirewallRule.FIREWALL_ACL_TAG);
 			for (int i=0; i<acls.getLength(); i++) {
 				String aclId = Terremark.hrefToFirewallRuleId(acls.item(i).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 				firewallIds.add(aclId);
 			}
 		}
 		return firewallIds;
 	}
 
 	/**
 	 * Lists all network interfaces currently provisioned in the current region.
 	 * @return a list of all provisioned network interfaces in the current region
 	 * @throws CloudException an error occurred with the cloud provider fetching the network interfaces
 	 * @throws InternalException a local error occurred fetching the network interfaces
 	 */
 	@Override
 	public Iterable<NetworkInterface> listNetworkInterfaces() throws CloudException, InternalException {
 		Collection<NetworkInterface> networkHosts = new ArrayList<NetworkInterface>();
 		String url = "/" + NETWORK_HOSTS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId();
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null){
 			NodeList networkHostNodes = doc.getElementsByTagName("Host");
 			for (int i=0; i<networkHostNodes.getLength(); i++) {
 				String nicId = Terremark.hrefToId(networkHostNodes.item(i).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 				NetworkInterface nic = getNetworkInterface(nicId);
 				if (nic != null) {
 					networkHosts.add(nic);	
 				}
 			}
 		}
 		return networkHosts;
 	}
 
 	/**
 	 * Lists all network interfaces attached to a specific virtual machine.
 	 * @param forVmId the virtual machine whose network interfaces you want listed
 	 * @return the network interfaces attached to the specified virtual machine
 	 * @throws CloudException an error occurred with the cloud provider determining the attached network interfaces
 	 * @throws InternalException a local error occurred listing the network interfaces attached to the specified virtual machine
 	 */
 	@Override
 	public Iterable<NetworkInterface> listNetworkInterfacesForVM(String forVmId) throws CloudException, InternalException {
 		logger.trace("enter - listNetworkInterfacesForVM(" + forVmId + ")");
 		Collection<NetworkInterface> netowrkHost = new ArrayList<NetworkInterface>();
 		String url = "/" + VMSupport.VIRTUAL_MACHINES + "/" + forVmId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null){
 			Node networkHostNode = doc.getElementsByTagName(NETWORK_HOST_TAG).item(0);
 			String nicId = Terremark.hrefToId(networkHostNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 			netowrkHost.add(getNetworkInterface(nicId));
 		}
 		logger.trace("exit - listNetworkInterfacesForVM(" + forVmId + ")");
 		return netowrkHost;
 	}
 
 	/**
 	 * Lists all network interfaces connected to a specific subnet. Valid only if the cloud provider supports subnets.
 	 * @param subnetId the subnet ID for the subnet in which you are searching
 	 * @return all interfaces within the specified subnet
 	 * @throws CloudException an error occurred in the cloud identifying the matching network interfaces
 	 * @throws InternalException a local error occurred constructing the cloud query
 	 */
 	@Override
 	public Iterable<NetworkInterface> listNetworkInterfacesInSubnet(String subnetId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Subnets not supported.");
 	}
 
 	/**
 	 * Lists all network interfaces connected to a specific VLAN. Valid only if the cloud provider supports VLANs.
 	 * @param vlanId the VLAN ID for the VLAN in which you are searching
 	 * @return all interfaces within the specified VLAN
 	 * @throws CloudException an error occurred in the cloud identifying the matching network interfaces
 	 * @throws InternalException a local error occurred constructing the cloud query
 	 */
 	@Override
 	public Iterable<NetworkInterface> listNetworkInterfacesInVLAN(String vlanId) throws CloudException, InternalException {
 		Collection<NetworkInterface> vlanNics = new ArrayList<NetworkInterface>();
 		for (NetworkInterface nic : listNetworkInterfaces()) {
 			if (nic.getProviderVlanId().equals(vlanId)) {
 				vlanNics.add(nic);
 			}
 		}
 		return vlanNics;
 	}
 
 	/**
 	 * Lists the status of all network interfaces currently provisioned in the current region.
 	 * @return a list of status for all provisioned network interfaces in the current region
 	 * @throws CloudException an error occurred with the cloud provider fetching the network interfaces
 	 * @throws InternalException a local error occurred fetching the network interfaces
 	 */
 	@Override
 	public Iterable<ResourceStatus> listNetworkInterfaceStatus() throws CloudException, InternalException {
 		Collection<ResourceStatus> networkHostsStatus = new ArrayList<ResourceStatus>();
 		String url = "/" + NETWORK_HOSTS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId();
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null){
 			NodeList networkHostNodes = doc.getElementsByTagName("Host");
 			for (int i=0; i<networkHostNodes.getLength(); i++) {
 				String nicId = Terremark.hrefToId(networkHostNodes.item(i).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 				if (nicId != null) {
 					networkHostsStatus.add(new ResourceStatus(nicId, NICState.AVAILABLE));	
 				}
 			}
 		}
 		return networkHostsStatus;
 	}
 
 	/**
 	 * Lists all resources associated with the specified VLAN. In many clouds, this is a very expensive operation. So
 	 * call this method with care.
 	 * @param inVlanId the VLAN for whom you are seeking the resource list
 	 * @return a list of resources associated with the specified VLAN
 	 * @throws CloudException an error occurred in the cloud identifying the matching resources
 	 * @throws InternalException a local error occurred constructing the cloud query
 	 */
 	@Override
 	public Iterable<Networkable> listResources(String inVlanId) throws CloudException, InternalException {
 		logger.trace("enter - listResources(" + inVlanId + ")");
 		Collection<Networkable> resouces = new ArrayList<Networkable>();
 		for (IpAddress ip : provider.getNetworkServices().getIpAddressSupport().listPrivateIps(inVlanId, false, false, true)) {
 			resouces.add(ip);
 		}
 		logger.trace("exit - listResources(" + inVlanId + ")");
 		return resouces;
 	}
 
 	/**
 	 * Lists all routing tables associated with the specified VLAN. 
 	 * @param inVlanId the VLAN ID whose routing tables are being sought
 	 * @return a list of routing tables for the specified VLAN
 	 * @throws CloudException an error occurred fetching the routing tables from the cloud provider
 	 * @throws InternalException a local error occurred processing the routing tables
 	 */
 	@Override
 	public Iterable<RoutingTable> listRoutingTables(String inVlanId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	@Override
 	public @Nonnull Iterable<Subnet> listSubnets(@Nonnull String inVlanId) throws CloudException, InternalException{
 		return Collections.emptyList();
 	}
 
 	/**
 	 * Lists all IP protocol versions supported for VLANs in this cloud.
 	 * @return a list of supported versions
 	 * @throws CloudException an error occurred checking support for IP versions with the cloud provider
 	 * @throws InternalException a local error occurred preparing the supported version
 	 */
 	@Override
 	public Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
 		Collection<IPVersion> versions = new ArrayList<IPVersion>();
 		versions.add(IPVersion.IPV4);
 		versions.add(IPVersion.IPV6);
 		return versions;
 	}
 
 	/**
 	 * Lists all VLANs in the current region.
 	 * @return all VLANs in the current region
 	 * @throws CloudException an error occurred communicating with the cloud provider
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation
 	 */
 	@Override
 	public @Nonnull Iterable<VLAN> listVlans() throws CloudException, InternalException{
 		Collection<VLAN> vlans = new ArrayList<VLAN>();
 		String url = "/" + NETWORKS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId();
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		} 
 		catch (TerremarkException e) {
 			throw new CloudException("Failed to list networks " + e.getMessage());
 		}
 		if (doc != null){
 			NodeList networks = doc.getElementsByTagName(NETWORK_TAG);
 			for (int i=0; i<networks.getLength(); i++) {
 				VLAN vlan = toVLAN(networks.item(i));
 				if (vlan != null) {
 					vlans.add(vlan);
 				}
 			}
 		}
 		return vlans;
 	}
 
 	/**
 	 * Lists the status of all VLANs in the current region.
 	 * @return the status of all VLANs in the current region
 	 * @throws CloudException an error occurred communicating with the cloud provider
 	 * @throws InternalException an error occurred within the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<ResourceStatus> listVlanStatus() throws CloudException, InternalException {
 		Collection<ResourceStatus> vlans = new ArrayList<ResourceStatus>();
 		String url = "/" + NETWORKS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId();
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		} 
 		catch (TerremarkException e) {
 			throw new CloudException("Failed to list networks " + e.getMessage());
 		}
 		if (doc != null){
 			NodeList networks = doc.getElementsByTagName(NETWORK_TAG);
 			for (int i=0; i<networks.getLength(); i++) {
 				ResourceStatus vlanStatus = toVLANStatus(networks.item(i));
 				vlans.add(vlanStatus);
 			}
 		}
 		return vlans;
 	}
 
 	@Override
 	public String[] mapServiceAction(ServiceAction action) {
 		return new String[0];
 	}
 
 	/**
 	 * Disconnects the specified VLAN from it's gateway and deletes it if no other VLANs are attached to it.
 	 * @param forVlanId the VLAN to disconnect
 	 * @throws CloudException an error occurred with the cloud provider while removing the gateway
 	 * @throws InternalException a local error occurred while removing the gateway
 	 */
 	@Override
 	public void removeInternetGateway(String forVlanId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Removing internet gateways not supported.");
 	}
 
 	/**
 	 * De-provisions the specified network interface.
 	 * @param nicId the network interface to be de-provisioned
 	 * @throws CloudException an error occurred with the cloud provider while de-provisioning the network interface
 	 * @throws InternalException a local error occurred while de-provisioning the network interface
 	 */
 	@Override
 	public void removeNetworkInterface(String nicId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("De-provisioning network interfaces is not supported.");
 	}
 
 	/**
 	 * Removes any routing to the specified destination from the specified routing table.
 	 * @param inRoutingTableId the routing table from which the route is being removed
 	 * @param destinationCidr the destination CIDR for the traffic being routed
 	 * @throws CloudException an error occurred in the cloud while removing the route
 	 * @throws InternalException a local error occurred processing the request to remove the route
 	 */
 	@Override
 	public void removeRoute(String inRoutingTableId, String destinationCidr) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	/**
 	 * Removes the specified routing table from the cloud.
 	 * @param routingTableId the unique ID of the routing table to be removed
 	 * @throws CloudException an error occurred in the cloud removing the routing table
 	 * @throws InternalException a local error occurred while processing the request to remove the routing table
 	 */
 	@Override
 	public void removeRoutingTable(String routingTableId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Routing tables not supported.");
 	}
 
 	@Override
 	public void removeSubnet(String providerSubnetId) throws CloudException, InternalException{
 		throw new OperationNotSupportedException("Subnets not supported");
 	}
 
 	@Override
 	public void removeVlan(String vlanId) throws CloudException, InternalException{
 		throw new OperationNotSupportedException("Network removal is not supported");
 	}
 
 	/**
 	 * Indicates whether or not this cloud allows enabling of internet gateways for VLANs. This is not relevant if all VLANs are Internet
 	 * routable or if they simply cannot be made routable.
 	 * @return true if this cloud supports the optional enablement of Internet gateways for VLANS, false if all VLANs are either always or never Internet routable
 	 * @throws CloudException an error occurred determining this capability from the cloud provider
 	 * @throws InternalException a local error occurred determining this capability
 	 */
 	@Override
 	public boolean supportsInternetGatewayCreation() throws CloudException, InternalException {
 		return false;
 	}
 
 	/**
 	 * Indicates whether you can specify a raw IP address as a target for your routing table.
 	 * @return true if you can specify raw addresses, false if you need to specify other resources
 	 * @throws CloudException an error occurred identifying support
 	 * @throws InternalException a local error occurred identifying support
 	 */
 	@Override
 	public boolean supportsRawAddressRouting() throws CloudException, InternalException {
 		return false;
 	}
 
 	private NetworkInterface toNetworkInterface(Node networkHostNode) {
 		NetworkInterface networkInterface = new NetworkInterface();
 		String id = Terremark.hrefToId(networkHostNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 		networkInterface.setProviderNetworkInterfaceId(id);
 		String name = Terremark.hrefToId(networkHostNode.getAttributes().getNamedItem(Terremark.NAME).getNodeValue());
 		networkInterface.setName(name);
 		networkInterface.setProviderRegionId(provider.getContext().getRegionId());
 		ArrayList<RawAddress> addresses = new ArrayList<RawAddress>();
 		NodeList nodes = networkHostNode.getChildNodes();
 		for (int i=0; i < nodes.getLength(); i++) {
 			Node hostChild = nodes.item(i);
 			if (hostChild.getNodeName().equals("Device")){
 				String vmId = Terremark.hrefToId(hostChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 				logger.debug("toNetworkInterface(): network host " + networkInterface.getProviderNetworkInterfaceId() + " vm id = " + vmId);
 				networkInterface.setProviderVirtualMachineId(vmId);
 			}
 			else if (hostChild.getNodeName().equals(NETWORKS_TAG)){
 				NodeList networksChildren = hostChild.getChildNodes();
 				for (int j=0; j < networksChildren.getLength(); j++) {
 					Node networkNode = networksChildren.item(j);
 					networkInterface.setProviderVlanId(Terremark.hrefToNetworkId(networkNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue()));
 					NodeList networkChildren = networkNode.getChildNodes();
 					for (int k=0; k<networkChildren.getLength(); k++) {
 						if (networkChildren.item(k).getNodeName().equals(TerremarkIpAddressSupport.IP_ADDRESS_TAG)) {
 							NodeList ipAddressNodes = networkChildren.item(k).getChildNodes();
 							for (int l=0; l<ipAddressNodes.getLength(); l++) {
 								Node ipNode = ipAddressNodes.item(l);
 								String ipAddress = ipNode.getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 								String type = ipNode.getAttributes().getNamedItem(Terremark.TYPE).getNodeValue();
 								IPVersion version = null;
 								if (type.equals(TerremarkIpAddressSupport.IP_ADDRESS_TYPE)) {
 									version = IPVersion.IPV4;
 								}
 								else if (type.equals(TerremarkIpAddressSupport.IP_ADDRESS_IPV6_TYPE)) {
 									version = IPVersion.IPV6;
 								}
 								addresses.add(new RawAddress(ipAddress, version));
 							}
 						}
 					}
 				}
 			}
 		}
 
 		networkInterface.setIpAddresses(addresses.toArray(new RawAddress[addresses.size()]));
 		networkInterface.setCurrentState(NICState.AVAILABLE);
 
 
 		return networkInterface;
 	}
 
 	private VLAN toVLAN(Node networkNode) throws CloudException, InternalException {
 		VLAN network = new VLAN();
 		network.setProviderOwnerId(provider.getContext().getAccountNumber());
 		NamedNodeMap networkAttrs = networkNode.getAttributes();
 		String vlanId = Terremark.hrefToNetworkId(networkAttrs.getNamedItem(Terremark.HREF).getTextContent());
 		network.setProviderVlanId(vlanId);
 		logger.debug("toVLAN(): VLAN ID = " + network.getProviderVlanId());
 		String vlanName = networkAttrs.getNamedItem(Terremark.NAME).getTextContent();
 		network.setName(vlanName);
 		logger.debug("toVLAN(): VLAN ID = " + network.getProviderVlanId() + " Name = " + network.getName());
 		network.setCidr(vlanName);
 		logger.debug("toVLAN(): VLAN ID = " + network.getProviderVlanId() + " CIDR = " + network.getCidr());
 		network.setProviderRegionId(provider.getContext().getRegionId());
 		logger.debug("toVLAN(): VLAN ID = " + network.getProviderVlanId() + " Region = " + network.getProviderRegionId());
 		network.setCurrentState(VLANState.AVAILABLE);
 		NodeList networkChildren = networkNode.getChildNodes();
 		for (int i=0; i<networkChildren.getLength(); i++) {
 			Node networkChild = networkChildren.item(i);
 			if (networkChild.getNodeName().equals("NetworkType")) {
 				String type = networkChild.getTextContent();
 				boolean ipv6 = networkAttrs.getNamedItem(Terremark.TYPE).getTextContent().equals(NETWORK_IPV6_TYPE);
 				if (ipv6) {
 					type = type + " IPv6";
 				}
 				else {
 					type = type + " IPv4";
 				}
 				network.setNetworkType(type);
 				network.setDescription(type);
 				logger.debug("toVLAN(): VLAN ID = " + network.getProviderVlanId() + " Description = " + network.getDescription());
 				break;
 			}
 		}
 		network.setDnsServers(null);
 		network.setDomainName(null);
 		network.setNtpServers(null);
 		network.setProviderDataCenterId(null);
 		network.setTags(null);
 
 		return network;
 	}
 
 	private ResourceStatus toVLANStatus(Node networkNode) throws CloudException, InternalException {
 		NamedNodeMap networkAttrs = networkNode.getAttributes();
 		String vlanId = Terremark.hrefToId(networkAttrs.getNamedItem(Terremark.HREF).getTextContent());
 		return new ResourceStatus(vlanId, VLANState.AVAILABLE);
 	}
 
 }
