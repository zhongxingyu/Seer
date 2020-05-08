 package org.dasein.cloud.terremark.network;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Locale;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.Direction;
 import org.dasein.cloud.network.Firewall;
 import org.dasein.cloud.network.FirewallSupport;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.cloud.network.Permission;
 import org.dasein.cloud.network.Protocol;
 import org.dasein.cloud.network.RuleTarget;
 import org.dasein.cloud.network.RuleTargetType;
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.terremark.EnvironmentsAndComputePools;
 import org.dasein.cloud.terremark.Terremark;
 import org.dasein.cloud.terremark.TerremarkMethod;
 import org.dasein.cloud.terremark.TerremarkMethod.HttpMethodName;
 import org.dasein.util.CalendarWrapper;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class FirewallRule implements FirewallSupport {
 
 	private Terremark provider = null;
 
 	static private final Logger logger = Logger.getLogger(FirewallRule.class);
 
 	// API Calls
 	public final static String FIREWALL_ACLS            = "firewallAcls";
 
 	public final static String CREATE_FIREWALL_ACL      = "createFirewallAcl";
 
 	// Response Tags
 	public final static String FIREWALL_ACL_TAG         = "FirewallAcl";
 
 	public final static String DELETE_FW_RULE_OPERATION = "Delete Firewall Rule";
 
 	public final static long DEFAULT_SLEEP              = CalendarWrapper.SECOND * 10;
 	public final static long DEFAULT_TIMEOUT            = CalendarWrapper.MINUTE * 20;
 
 	FirewallRule(@Nonnull Terremark provider) {
 		this.provider = provider;
 	}
 
 	/**
 	 * Provides positive authorization for the specified firewall rule with the specified precedence. Any call to this method should
 	 * result in an override of any previous revocations.
 	 * @param firewallId the unique, cloud-specific ID for the firewall being targeted by the new rule
 	 * @param direction the direction of the traffic governing the rule
 	 * @param permission ALLOW or DENY
 	 * @param sourceEndpoint the source endpoint for this rule
 	 * @param protocol the protocol (tcp/udp/icmp) supported by this rule
 	 * @param destinationEndpoint the destination endpoint to specify for this rule
 	 * @param beginPort the beginning of the port range to be allowed, inclusive
 	 * @param endPort the end of the port range to be allowed, inclusive
 	 * @param precedence the precedence of this rule with respect to others
 	 * @return the provider ID of the new rule
 	 * @throws CloudException an error occurred with the cloud provider establishing the rule
 	 * @throws InternalException an error occurred locally trying to establish the rule
 	 * @throws OperationNotSupportedException the specified direction, target, or permission are not supported
 	 */
 	@Override
 	public String authorize(String firewallId, Direction direction, Permission permission, RuleTarget sourceEndpoint, Protocol protocol, RuleTarget destinationEndpoint, int beginPort, int endPort, int precedence) throws CloudException, InternalException {
 		String providerRuleId = "";
 		String id = provider.getContext().getRegionId();
 		String permissionString;
 		if (permission.equals(Permission.ALLOW)) {
 			permissionString  = "allow";
 		}
 		else {
 			permissionString = "deny";
 		}
 		RuleTargetType sourceType = sourceEndpoint.getRuleTargetType();
 		RuleTargetType destinationType = destinationEndpoint.getRuleTargetType();
 
 
 		String url = "/" + FIREWALL_ACLS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + id + "/" + Terremark.ACTION + "/" + CREATE_FIREWALL_ACL;
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			// root element
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("CreateFirewallAcl");
 
 			Element permissionElement = doc.createElement("Permission");
 			permissionElement.appendChild(doc.createTextNode(permissionString));
 			rootElement.appendChild(permissionElement);
 
 			String protocolString = "TCP";
 			switch (protocol) {
 			case TCP   : protocolString = "TCP"; break;
 			case UDP   : protocolString = "UDP"; break;
 			case ANY   : protocolString = "Any"; break;
 			case IPSEC : protocolString = "Any"; break;
 			case ICMP  : protocolString = "Any"; break;
 			}
 
 			Element protocolElement = doc.createElement("Protocol");
 			protocolElement.appendChild(doc.createTextNode(protocolString));
 			rootElement.appendChild(protocolElement);
 
 			Element sourceElement = doc.createElement("Source");
 			Element sourceTypeElement = doc.createElement("Type");
 			if (sourceType.equals(RuleTargetType.CIDR)) {
 				String cidr = sourceEndpoint.getCidr();
 				String networkSize = cidr.substring(cidr.lastIndexOf("/")+1);
 				String address = cidr.substring(0, cidr.lastIndexOf("/"));
 
 				if (permission.equals(Permission.DENY)) {
 					if (networkSize.equals("32")) {
 						sourceTypeElement.appendChild(doc.createTextNode("ExternalIp"));
 						sourceElement.appendChild(sourceTypeElement);
 
 						Element externalIpAddressElement = doc.createElement("ExternalIpAddress");
 						externalIpAddressElement.appendChild(doc.createTextNode(address));
 						sourceElement.appendChild(externalIpAddressElement);
 					}
 					else {
 						sourceTypeElement.appendChild(doc.createTextNode("ExternalNetwork"));
 						sourceElement.appendChild(sourceTypeElement);
 
 						Element externalNetworkElement = doc.createElement("ExternalNetwork");
 
 						Element addressElement = doc.createElement("Address");
 						addressElement.appendChild(doc.createTextNode(address));
 						externalNetworkElement.appendChild(addressElement);
 
 						Element sizeElement = doc.createElement("Size");
 						sizeElement.appendChild(doc.createTextNode(networkSize));
 						externalNetworkElement.appendChild(sizeElement);
 
 						sourceElement.appendChild(externalNetworkElement);
 					}
 				}
 				else {
 					if (!networkSize.equals("32")) {
 						throw new InternalException("Firewall allow rules with a source CIDR must have a CIDR ending in /32");
 					}
 					Iterable<IpAddress> ips = provider.getNetworkServices().getIpAddressSupport().lisPrivatetIpPool();
 					IpAddress ipAddress = null;
 					for (IpAddress ip : ips) {
 						if (ip.getRawAddress().getIpAddress().equals(address)) {
 							ipAddress = ip;
 							break;
 						}
 					}
 					if (ipAddress == null) {
 						throw new InternalException("Failed to find an ip address matching the source CIDR " + cidr);
 					}
 					sourceTypeElement.appendChild(doc.createTextNode(TerremarkIpAddressSupport.IP_ADDRESS_TAG));
 					sourceElement.appendChild(sourceTypeElement);
 					
 					Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_TAG);
 					ipAddressElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + TerremarkIpAddressSupport.IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + ipAddress.getProviderIpAddressId());
 					sourceElement.appendChild(ipAddressElement);
 				}
 			}
 			else if (sourceType.equals(RuleTargetType.VM)) {
 				throw new InternalException("VM based firewall rule sources are not permitted in Terremark.");
 			}
 			else if (sourceType.equals(RuleTargetType.VLAN)) {
 				String vlanId = sourceEndpoint.getProviderVlanId();
 				VLAN vlan = provider.getNetworkServices().getVlanSupport().getVlan(vlanId);
 				if (vlan == null) {
 					throw new InternalException("Failed to find the network: " + vlanId);
 				}
 				
 				sourceTypeElement.appendChild(doc.createTextNode(TerremarkNetworkSupport.NETWORK_TAG));
 				sourceElement.appendChild(sourceTypeElement);
 				
 				Element networkElement = doc.createElement(TerremarkNetworkSupport.NETWORK_TAG);
 				networkElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + TerremarkNetworkSupport.NETWORKS + "/" + vlanId);
 				networkElement.setAttribute(Terremark.NAME, vlan.getName());
 				sourceElement.appendChild(networkElement);
 			}
 			else if (sourceType.equals(RuleTargetType.GLOBAL)) {
 				throw new InternalException("Creating firewall rules with a global source can only be done by creating port forwarding rules.");
 			}
 
 
 			rootElement.appendChild(sourceElement);
 
 			Element destinationElement = doc.createElement("Destination");
 			Element destinationTypeElement = doc.createElement("Type");
 			if (destinationType.equals(RuleTargetType.CIDR)) {
 				String cidr = destinationEndpoint.getCidr();
 				String networkSize = cidr.substring(cidr.lastIndexOf("/")+1);
 				String address = cidr.substring(0, cidr.lastIndexOf("/"));
 
 				if (direction.equals(Direction.EGRESS)) {
 					if (networkSize.equals("32")) {
 						destinationTypeElement.appendChild(doc.createTextNode("ExternalIp"));
 						destinationElement.appendChild(destinationTypeElement);
 
 						Element externalIpAddressElement = doc.createElement("ExternalIpAddress");
 						externalIpAddressElement.appendChild(doc.createTextNode(address));
 						destinationElement.appendChild(externalIpAddressElement);
 					}
 					else {
 						destinationTypeElement.appendChild(doc.createTextNode("ExternalNetwork"));
 						destinationElement.appendChild(destinationTypeElement);
 
 						Element externalNetworkElement = doc.createElement("ExternalNetwork");
 
 						Element addressElement = doc.createElement("Address");
 						addressElement.appendChild(doc.createTextNode(address));
 						externalNetworkElement.appendChild(addressElement);
 
 						Element sizeElement = doc.createElement("Size");
 						sizeElement.appendChild(doc.createTextNode(networkSize));
 						externalNetworkElement.appendChild(sizeElement);
 
 						destinationElement.appendChild(externalNetworkElement);
 					}
 				}
 				else {
 					if (!networkSize.equals("32")) {
 						throw new InternalException("Ingress firewall rules with a destination CIDR must have a CIDR ending in /32");
 					}
 					Iterable<IpAddress> ips = provider.getNetworkServices().getIpAddressSupport().lisPrivatetIpPool();
 					IpAddress ipAddress = null;
 					for (IpAddress ip : ips) {
 						if (ip.getRawAddress().getIpAddress().equals(address)) {
 							ipAddress = ip;
 							break;
 						}
 					}
 					if (ipAddress == null) {
 						throw new InternalException("Failed to find an ip address matching the destination CIDR " + cidr);
 					}
 					destinationTypeElement.appendChild(doc.createTextNode(TerremarkIpAddressSupport.IP_ADDRESS_TAG));
 					destinationElement.appendChild(destinationTypeElement);
 					
 					Element ipAddressElement = doc.createElement(TerremarkIpAddressSupport.IP_ADDRESS_TAG);
 					ipAddressElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + TerremarkIpAddressSupport.IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + ipAddress.getProviderIpAddressId());
 					destinationElement.appendChild(ipAddressElement);
 				}
 			}
 			else if (destinationType.equals(RuleTargetType.VM)) {
 				throw new InternalException("VM based firewall rule destinations are not permitted in Terremark.");
 			}
 			else if (destinationType.equals(RuleTargetType.VLAN)) {
 				String vlanId = destinationEndpoint.getProviderVlanId();
 				VLAN vlan = provider.getNetworkServices().getVlanSupport().getVlan(vlanId);
 				if (vlan == null) {
 					throw new InternalException("Failed to find the network: " + vlanId);
 				}
 				
 				destinationTypeElement.appendChild(doc.createTextNode(TerremarkNetworkSupport.NETWORK_TAG));
 				destinationElement.appendChild(destinationTypeElement);
 				
 				Element networkElement = doc.createElement(TerremarkNetworkSupport.NETWORK_TAG);
 				networkElement.setAttribute(Terremark.HREF, Terremark.DEFAULT_URI_PATH + "/" + TerremarkNetworkSupport.NETWORKS + "/" + vlanId);
 				networkElement.setAttribute(Terremark.NAME, vlan.getName());
 				destinationElement.appendChild(networkElement);
 			}
 			else if (destinationType.equals(RuleTargetType.GLOBAL)) {
 				destinationTypeElement.appendChild(doc.createTextNode("Any"));
 				destinationElement.appendChild(destinationTypeElement);
 			}
 
 			rootElement.appendChild(destinationElement);
 
			Element portRangeElement = doc.createElement("PortRange");

			Element addressElement = doc.createElement("Start");
			addressElement.appendChild(doc.createTextNode(Integer.toString(beginPort)));
			portRangeElement.appendChild(addressElement);

			Element sizeElement = doc.createElement("End");
			sizeElement.appendChild(doc.createTextNode(Integer.toString(endPort)));
			portRangeElement.appendChild(sizeElement);
 
			rootElement.appendChild(portRangeElement);
 
 			doc.appendChild(rootElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, body);
 		Document doc = method.invoke();
 
 		if (doc != null) {
 			String ruleHref = doc.getElementsByTagName(FIREWALL_ACL_TAG).item(0).getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 			providerRuleId = Terremark.hrefToFirewallRuleId(ruleHref);
 		}
 
 		return providerRuleId;
 	}
 
 	/**
 	 * Provides positive authorization for the specified firewall rule to global destinations. Any call to this method should
 	 * result in an override of any previous revocations.
 	 * @param firewallId the unique, cloud-specific ID for the firewall being targeted by the new rule
 	 * @param direction the direction of the traffic governing the rule
 	 * @param permission ALLOW or DENY
 	 * @param source the source CIDR (http://en.wikipedia.org/wiki/CIDR) or provider firewall ID for the CIDR or other firewall being set
 	 * @param protocol the protocol (tcp/udp/icmp) supported by this rule
 	 * @param beginPort the beginning of the port range to be allowed, inclusive
 	 * @param endPort the end of the port range to be allowed, inclusive
 	 * @return the provider ID of the new rule
 	 * @throws CloudException an error occurred with the cloud provider establishing the rule
 	 * @throws InternalException an error occurred locally trying to establish the rule
 	 * @throws OperationNotSupportedException the specified direction or permission are not supported or global destinations are not supported
 	 * @deprecated Use {@link #authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)}
 	 */
 	@Deprecated
 	@Override
 	public String authorize(String firewallId, Direction direction, Permission permission, String source, Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
 		if (permission.equals(Permission.ALLOW) || direction.equals(Direction.EGRESS)) {
 			throw new OperationNotSupportedException("CIDR source with Allow permission or Egress direction not supported. Use authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)");
 		}
 		RuleTarget sourceTarget = RuleTarget.getCIDR(source);
 		RuleTarget destinationTarget = RuleTarget.getGlobal(provider.getContext().getRegionId());
 		return authorize(firewallId, direction, permission, sourceTarget, protocol, destinationTarget, beginPort, endPort, -1);
 	}
 
 	/**
 	 * Provides positive authorization for the specified firewall rule. Any call to this method should
 	 * result in an override of any previous revocations.
 	 * @param firewallId the unique, cloud-specific ID for the firewall being targeted by the new rule
 	 * @param direction the direction of the traffic governing the rule
 	 * @param permission ALLOW or DENY
 	 * @param source the source CIDR (http://en.wikipedia.org/wiki/CIDR) or provider firewall ID for the CIDR or other firewall being set
 	 * @param protocol the protocol (tcp/udp/icmp) supported by this rule
 	 * @param target the target to specify for this rule
 	 * @param beginPort the beginning of the port range to be allowed, inclusive
 	 * @param endPort the end of the port range to be allowed, inclusive
 	 * @return the provider ID of the new rule
 	 * @throws CloudException an error occurred with the cloud provider establishing the rule
 	 * @throws InternalException an error occurred locally trying to establish the rule
 	 * @throws OperationNotSupportedException the specified direction, target, or permission are not supported
 	 * @deprecated Use {@link #authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)}
 	 */
 	@Deprecated
 	@Override
 	public String authorize(String firewallId, Direction direction, Permission permission, String source, Protocol protocol, RuleTarget target, int beginPort, int endPort) throws CloudException, InternalException {
 		if (permission.equals(Permission.ALLOW)) {
 			throw new OperationNotSupportedException("CIDR source with Allow permission not supported. Use authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)");
 		}
 		RuleTarget sourceTarget = RuleTarget.getCIDR(source);
 		return authorize(firewallId, direction, permission, sourceTarget, protocol, target, beginPort, endPort, -1);
 	}
 
 	/**
 	 * Provides positive authorization to all destinations behind this firewall for the specified rule.
 	 * Any call to this method should result in an override of any previous revocations.
 	 * @param firewallId the unique, cloud-specific ID for the firewall being targeted by the new rule
 	 * @param direction the direction of the traffic governing the rule                  
 	 * @param source the source CIDR (http://en.wikipedia.org/wiki/CIDR) or provider firewall ID for the CIDR or other firewall being set
 	 * @param protocol the protocol (tcp/udp/icmp) supported by this rule
 	 * @param beginPort the beginning of the port range to be allowed, inclusive
 	 * @param endPort the end of the port range to be allowed, inclusive
 	 * @return the provider ID of the new rule
 	 * @throws CloudException an error occurred with the cloud provider establishing the rule
 	 * @throws InternalException an error occurred locally trying to establish the rule
 	 * @throws OperationNotSupportedException the specified direction, ALLOW rules, or global destinations are not supported
 	 * @deprecated Use {@link #authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)}
 	 */
 	@Deprecated
 	@Override
 	public String authorize(String firewallId, Direction direction, String source, Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("CIDR source with Allow permission not supported. Use authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)");
 	}
 
 
 	/**
 	 * Provides positive authorization for the specified firewall rule. Any call to this method should
 	 * result in an override of any previous revocations.
 	 * @param firewallId the unique, cloud-specific ID for the firewall being targeted by the new rule
 	 * @param cidr the source CIDR (http://en.wikipedia.org/wiki/CIDR) for the allowed traffic
 	 * @param protocol the protocol (tcp/udp/icmp) supported by this rule
 	 * @param beginPort the beginning of the port range to be allowed, inclusive
 	 * @param endPort the end of the port range to be allowed, inclusive
 	 * @return the provider ID of the new rule
 	 * @throws CloudException an error occurred with the cloud provider establishing the rule
 	 * @throws InternalException an error occurred locally trying to establish the rule
 	 * @deprecated Use {@link #authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)}
 	 */
 	@Deprecated
 	@Override
 	public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("CIDR source with Allow permission not supported. Use authorize(String, Direction, Permission, RuleTarget, Protocol, RuleTarget, int, int, int)");
 	}
 
 	/**
 	 * Not supported in Terremark.
 	 */
 	@Override
 	public @Nonnull String create(@Nonnull String name, @Nonnull String description) throws InternalException, CloudException {
 		throw new OperationNotSupportedException("Not supported.");
 	}
 
 	/**
 	 * Not supported in Terremark.
 	 */
 	@Override
 	public @Nonnull String createInVLAN(@Nonnull String name, @Nonnull String description, @Nonnull String providerVlanId) throws InternalException, CloudException {
 		throw new OperationNotSupportedException("Not supported");
 	}
 
 	/**
 	 * Not supported in Terremark.
 	 */
 	@Override
 	public void delete(@Nonnull String firewallId) throws InternalException, CloudException {
 
 	}
 
 	/**
 	 * Provides negative authorization for the specified firewall rule. Any call to this method should
 	 * result in an override of any previous revocations.
 	 * @param firewallId the unique, cloud-specific ID for the firewall being targeted by the new rule
 	 * @param cidr the source CIDR (http://en.wikipedia.org/wiki/CIDR) for the denied traffic
 	 * @param protocol the protocol (tcp/udp/icmp) supported by this rule
 	 * @param beginPort the beginning of the port range to be allowed, inclusive
 	 * @param endPort the end of the port range to be allowed, inclusive
 	 * @return the provider ID of the new rule
 	 * @throws CloudException an error occurred with the cloud provider establishing the rule
 	 * @throws InternalException an error occurred locally trying to establish the rule
 	 */
 	public @Nonnull String deny(@Nonnull String firewallId, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
 		String providerRuleId = "";
 		String id = provider.getContext().getRegionId();
 		String url = "/" + FIREWALL_ACLS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + id + "/" + Terremark.ACTION + "/" + CREATE_FIREWALL_ACL;
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			// root element
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("CreateFirewallAcl");
 
 			Element permissionElement = doc.createElement("Permission");
 			permissionElement.appendChild(doc.createTextNode("deny"));
 			rootElement.appendChild(permissionElement);
 
 			String protocolString = "TCP";
 			switch (protocol) {
 			case TCP  : protocolString = "TCP"; break;
 			case UDP  : protocolString = "UDP"; break;
 			case ICMP : protocolString = "Any"; break;
 			}
 
 			Element protocolElement = doc.createElement("Protocol");
 			protocolElement.appendChild(doc.createTextNode(protocolString));
 			rootElement.appendChild(protocolElement);
 
 			Element sourceElement = doc.createElement("Source");
 			String networkSize = cidr.substring(cidr.lastIndexOf("/")+1);
 			String address = cidr.substring(0, cidr.lastIndexOf("/"));
 			Element sourceTypeElement = doc.createElement("Type");
 			if (networkSize.equals("32")) {
 				sourceTypeElement.appendChild(doc.createTextNode("ExternalIp"));
 				sourceElement.appendChild(sourceTypeElement);
 
 				Element externalIpAddressElement = doc.createElement("ExternalIpAddress");
 				externalIpAddressElement.appendChild(doc.createTextNode(address));
 				sourceElement.appendChild(externalIpAddressElement);
 			}
 			else {
 				sourceTypeElement.appendChild(doc.createTextNode("ExternalNetwork"));
 				sourceElement.appendChild(sourceTypeElement);
 
 				Element externalNetworkElement = doc.createElement("ExternalNetwork");
 
 				Element addressElement = doc.createElement("Address");
 				addressElement.appendChild(doc.createTextNode(address));
 				externalNetworkElement.appendChild(addressElement);
 
 				Element sizeElement = doc.createElement("Size");
 				sizeElement.appendChild(doc.createTextNode(networkSize));
 				externalNetworkElement.appendChild(sizeElement);
 
 				sourceElement.appendChild(externalNetworkElement);
 			}
 			rootElement.appendChild(sourceElement);
 
 			Element destinationElement = doc.createElement("Destination");
 			Element destinationTypeElement = doc.createElement("Type");
 			destinationTypeElement.appendChild(doc.createTextNode("Any"));
 			destinationElement.appendChild(destinationTypeElement);
 			rootElement.appendChild(destinationElement);
 
 			Element portRangeElement = doc.createElement("PortRange");
 
 			Element addressElement = doc.createElement("Start");
 			addressElement.appendChild(doc.createTextNode(Integer.toString(beginPort)));
 			portRangeElement.appendChild(addressElement);
 
 			Element sizeElement = doc.createElement("End");
 			sizeElement.appendChild(doc.createTextNode(Integer.toString(endPort)));
 			portRangeElement.appendChild(sizeElement);
 
 			rootElement.appendChild(portRangeElement);
 
 			doc.appendChild(rootElement);
 
 			StringWriter stw = new StringWriter(); 
 			Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 			serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 			body = stw.toString();
 
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, body);
 		Document doc = method.invoke();
 
 		if (doc != null) {
 			String ruleHref = doc.getElementsByTagName(FIREWALL_ACL_TAG).item(0).getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 			providerRuleId = Terremark.hrefToFirewallRuleId(ruleHref);
 		}
 
 		return providerRuleId;
 	}
 
 	/**
 	 * Provides the full firewall data for the specified firewall.
 	 * @param firewallId the unique ID of the desired firewall
 	 * @return the firewall state for the specified firewall instance
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public @Nullable Firewall getFirewall(@Nonnull String firewallId) throws InternalException, CloudException {
 		Firewall firewall = null;
 		String id = provider.getContext().getRegionId();
 		if (firewallId.equals(id)){
 			firewall = new Firewall();
 			firewall.setActive(true);
 			firewall.setAvailable(true);
 			String regionName = provider.getDataCenterServices().getRegion(id).getName();
 			firewall.setDescription("The " + regionName + " firewall");
 			firewall.setName(regionName + " Firewall");
 			firewall.setProviderFirewallId(id);
 			firewall.setRegionId(id);
 		}
 		return firewall;
 	}
 
 	/**
 	 * Provides the firewall terminology for the concept of a firewall. For example, AWS calls a 
 	 * firewall a "security group".
 	 * @param locale the locale for which you should translate the firewall term
 	 * @return the translated term for firewall with the target cloud provider
 	 */
 	@Override
 	public @Nonnull String getProviderTermForFirewall(@Nonnull Locale locale) {
 		return "Firewall rule";
 	}
 
 	/**
 	 * Provides the affirmative rules supported by the named firewall.
 	 * @param firewallId the unique ID of the firewall being queried
 	 * @return all rules supported by the target firewall
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public @Nonnull Collection<org.dasein.cloud.network.FirewallRule> getRules(@Nonnull String firewallId) throws InternalException, CloudException {
 		Collection<org.dasein.cloud.network.FirewallRule> rules = new ArrayList<org.dasein.cloud.network.FirewallRule>();
 		String id = provider.getContext().getRegionId();
 		if (firewallId.equals(id)){
 			String url = "/" + FIREWALL_ACLS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + id;
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 			Document doc = method.invoke();
 			if (doc != null) {
 				NodeList firewallAclNodes = doc.getElementsByTagName(FIREWALL_ACL_TAG);
 				for (int i=0; i < firewallAclNodes.getLength(); i++) {
 					org.dasein.cloud.network.FirewallRule rule = toFirewallRule(firewallAclNodes.item(i));
 					if (rule != null) {
 						rules.add(rule);
 					}
 				}
 
 			}
 		}
 		else {
 			rules = Collections.emptyList(); 
 		}
 		return rules;
 	}
 
 	/**
 	 * Indicates the degree to which authorizations expect precedence of rules to be established.
 	 * @param inVlan whether or not you are checking for VLAN firewalls or regular ones
 	 * @return the degree to which precedence is required
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public Requirement identifyPrecedenceRequirement(boolean inVlan) throws InternalException, CloudException {
 		return Requirement.NONE;
 	}
 
 	/**
 	 * Identifies whether or not the current account is subscribed to firewall services in the current region.
 	 * @return true if the current account is subscribed to firewall services for the current region
 	 * @throws CloudException an error occurred with the cloud provider while determining subscription status
 	 * @throws InternalException an error occurred in the Dasein Cloud implementation while determining subscription status
 	 */
 	@Override
 	public boolean isSubscribed() throws CloudException, InternalException {
 		boolean subscribed = false;
 		try {
 			getRules(provider.getContext().getRegionId());
 			subscribed = true;
 		}
 		catch (Exception e) {
 			logger.warn("");
 		}
 		return subscribed;
 	}
 
 	/**
 	 * Indicates whether the highest precedence comes from low numbers. If true, 0 is the highest precedence a rule
 	 * can have. If false, 0 is the lowest precedence.
 	 * @return true if 0 is the highest precedence for a rule
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public boolean isZeroPrecedenceHighest() throws InternalException, CloudException {
 		throw new OperationNotSupportedException("Firewall rule precedence is not supported.");
 	}
 
 	/**
 	 * Lists all firewalls in the current provider context.
 	 * @return a list of all firewalls in the current provider context
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public @Nonnull Collection<Firewall> list() throws InternalException, CloudException {
 		Collection<Firewall> firewalls = new ArrayList<Firewall>();
 		firewalls.add(getFirewall(provider.getContext().getRegionId()));
 		return firewalls;
 	}
 
 	/**
 	 * Lists the status for all firewalls in the current provider context.
 	 * @return the status for all firewalls in the current account
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public Iterable<ResourceStatus> listFirewallStatus() throws InternalException, CloudException {
 		Collection<ResourceStatus> firewallStatus = new ArrayList<ResourceStatus>();
 		firewallStatus.add(new ResourceStatus(provider.getContext().getRegionId(), true));
 		return firewallStatus;
 	}
 
 	/**
 	 * Describes what kinds of destinations may be named. A cloud must support at least one, but may support more
 	 * than one.
 	 * @param inVlan whether or not you are testing capabilities for VLAN firewalls
 	 * @return a list of supported destinations
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public Iterable<RuleTargetType> listSupportedDestinationTypes(boolean inVlan) throws InternalException, CloudException {
 		Collection<RuleTargetType> destTypes = new ArrayList<RuleTargetType>();
 		if (!inVlan) {
 			destTypes.add(RuleTargetType.VLAN);
 			destTypes.add(RuleTargetType.CIDR);
 			destTypes.add(RuleTargetType.GLOBAL);
 		}
 		return destTypes;
 	}
 
 	/**
 	 * Lists the supported traffic directions for rules behind this kind of firewall.
 	 * @param inVlan whether or not you are interested in VLAN firewalls
 	 * @return a list of supported directions
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public Iterable<Direction> listSupportedDirections(boolean inVlan) throws InternalException, CloudException {
 		Collection<Direction> directions = new ArrayList<Direction>();
 		if (!inVlan) {
 			directions.add(Direction.EGRESS);
 			directions.add(Direction.INGRESS);
 		}
 		return directions;
 	}
 
 	/**
 	 * Lists the types of permissions that one may authorize for a firewall rule.
 	 * @param inVlan whether or not you are interested in VLAN firewalls or general ones
 	 * @return a list of supported permissions
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public Iterable<Permission> listSupportedPermissions(boolean inVlan) throws InternalException, CloudException {
 		Collection<Permission> permissions = new ArrayList<Permission>();
 		if (!inVlan) {
 			permissions.add(Permission.ALLOW);
 			permissions.add(Permission.DENY);
 		}
 		return permissions;
 	}
 
 	/**
 	 * Describes what kinds of source endpoints may be named. A cloud must support at least one, but may support more
 	 * than one.
 	 * @param inVlan whether or not you are testing capabilities for VLAN firewalls
 	 * @return a list of supported source endpoints
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public Iterable<RuleTargetType> listSupportedSourceTypes(boolean inVlan) throws InternalException, CloudException {
 		Collection<RuleTargetType> sourceTypes = new ArrayList<RuleTargetType>();
 		if (!inVlan) {
 			sourceTypes.add(RuleTargetType.CIDR);
 			sourceTypes.add(RuleTargetType.VLAN);
 			//Note: RuleTargetType.GLOBAL sources are created automatically when forwarding rules are created, but they cannot be created or removed manually.
 		}
 		return sourceTypes;
 	}
 
 	@Override
 	public String[] mapServiceAction(ServiceAction action) {
 		return new String[0];
 	}
 
 	/**
 	 * Revokes the uniquely identified firewall rule.
 	 * @param providerFirewallRuleId the unique ID of the firewall.
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 */
 	@Override
 	public void revoke(String providerFirewallRuleId) throws InternalException, CloudException {
 		if (providerFirewallRuleId.contains("nodeServices")) {
 			throw new InternalException("You cannot manually revoke node service firewall rules. Delete the forwarding rule using the stopForward method and the associated node service firewall rule will be removed.");
 		}
 		String url = "/" + FIREWALL_ACLS + "/" + providerFirewallRuleId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.DELETE, url, null, "");
 		Document doc = method.invoke();
 
 		String taskHref = Terremark.getTaskHref(doc, DELETE_FW_RULE_OPERATION);
 		provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 	}
 
 	/**
 	 * Revokes the specified access from the named firewall.
 	 * @param firewallId the firewall from which the rule is being revoked
 	 * @param direction the direction of the traffic being revoked
 	 * @param permission ALLOW or DENY
 	 * @param source the source CIDR (http://en.wikipedia.org/wiki/CIDR) or provider firewall ID for the CIDR or other firewall being set
 	 * @param protocol the protocol (tcp/icmp/udp) of the rule being removed
 	 * @param beginPort the initial port of the rule being removed
 	 * @param endPort the end port of the rule being removed
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 * @deprecated Use {@link #revoke(String)}
 	 */
 	@Deprecated
 	@Override
 	public void revoke(String firewallId, Direction direction, Permission permission, String source, Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Not supported. Use revoke(String providerFirewallRuleId) instead.");
 	}
 
 	/**
 	 * Revokes the specified access from the named firewall.
 	 * @param firewallId the firewall from which the rule is being revoked
 	 * @param direction the direction of the traffic being revoked
 	 * @param permission ALLOW or DENY
 	 * @param source the source CIDR (http://en.wikipedia.org/wiki/CIDR) or provider firewall ID for the CIDR or other firewall being set
 	 * @param protocol the protocol (tcp/icmp/udp) of the rule being removed
 	 * @param target the target for traffic matching this rule
 	 * @param beginPort the initial port of the rule being removed
 	 * @param endPort the end port of the rule being removed
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 * @deprecated Use {@link #revoke(String)}
 	 */
 	@Deprecated
 	@Override
 	public void revoke(String firewallId, Direction direction, Permission permission, String source, Protocol protocol, RuleTarget target, int beginPort, int endPort) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Not supported. Use revoke(String providerFirewallRuleId) instead.");
 	}
 
 	/**
 	 * Revokes the specified ALLOW access from the named firewall.
 	 * @param firewallId the firewall from which the rule is being revoked
 	 * @param direction the direction of the traffic being revoked                  
 	 * @param source the source CIDR (http://en.wikipedia.org/wiki/CIDR) or provider firewall ID for the CIDR or other firewall being set
 	 * @param protocol the protocol (tcp/icmp/udp) of the rule being removed
 	 * @param beginPort the initial port of the rule being removed
 	 * @param endPort the end port of the rule being removed
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 * @deprecated Use {@link #revoke(String)}
 	 */
 	@Deprecated
 	@Override
 	public void revoke(String firewallId, Direction direction, String source, Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Not supported. Use revoke(String providerFirewallRuleId) instead.");
 	}
 
 	/**
 	 * Revokes the specified INGRESS + ALLOW access from the named firewall.
 	 * @param firewallId the firewall from which the rule is being revoked
 	 * @param source the source CIDR (http://en.wikipedia.org/wiki/CIDR) or provider firewall ID for the CIDR or other firewall being set
 	 * @param protocol the protocol (tcp/icmp/udp) of the rule being removed
 	 * @param beginPort the initial port of the rule being removed
 	 * @param endPort the end port of the rule being removed
 	 * @throws InternalException an error occurred locally independent of any events in the cloud
 	 * @throws CloudException an error occurred with the cloud provider while performing the operation
 	 * @deprecated Use {@link #revoke(String)}
 	 */
 	@Deprecated
 	@Override
 	public void revoke(@Nonnull String firewallId, @Nonnull String source, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Not supported. Use revoke(String providerFirewallRuleId) instead.");
 	}
 
 	/**
 	 * Indicates whether or not the sources you specify in your rules may be other firewalls (security group behavior).
 	 * @return true if the sources may be other firewalls
 	 * @throws CloudException an error occurred with the cloud provider while checking for support
 	 * @throws InternalException a local error occurred while checking for support
 	 */
 	@Override
 	public boolean supportsFirewallSources() throws CloudException, InternalException {
 		return false;
 	}
 
 	/**
 	 * Indicates whether firewalls of the specified type (VLAN or flat network) support rules over the direction specified.
 	 * @param direction the direction of the traffic
 	 * @param permission the type of permission
 	 * @param inVlan whether or not you are looking for support for VLAN or flat network traffic
 	 * @return true if the cloud supports the creation of firewall rules in the direction specfied for the type of network specified
 	 * @throws CloudException an error occurred with the cloud provider while checking for support
 	 * @throws InternalException a local error occurred while checking for support
 	 */
 	@Override
 	public boolean supportsRules(Direction direction, Permission permission, boolean inVlan) throws CloudException, InternalException {
 		boolean support = false;
 		if (inVlan == false) {
 			if (permission.equals(Permission.DENY)) {
 				if (direction.equals(Direction.INGRESS)) {
 					support = true;
 				}
 			}
 			else if (permission.equals(Permission.ALLOW)) {
 				support = true;
 			}
 		}
 		return support;
 	}
 
 	private org.dasein.cloud.network.FirewallRule toFirewallRule(Node firewallAcl) throws InternalException, CloudException {
 		org.dasein.cloud.network.FirewallRule rule = null;
 		String firewallRuleId = null;
 		String providerFirewallId = provider.getContext().getRegionId();
 		RuleTarget source = null;
 		Direction direction = Direction.INGRESS;
 		Protocol protocol = null;
 		Permission permission = null;
 		RuleTarget destination = null;
 		int startPort = -1;
 		int endPort = -1;
 
 		String href = firewallAcl.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 		firewallRuleId = Terremark.hrefToFirewallRuleId(href);
 
 		String sourceType = null;
 		String destinationType = null;
 
 		NodeList firewallRuleChildren = firewallAcl.getChildNodes();
 		for (int i=0; i < firewallRuleChildren.getLength(); i++) {
 			Node firewallChild = firewallRuleChildren.item(i);
 			if (firewallChild.getNodeName().equals("Permission")) {
 				if (firewallChild.getTextContent().equals("allow")){
 					permission = Permission.ALLOW;
 				}
 				else if (firewallChild.getTextContent().equals("deny")){
 					permission = Permission.DENY;
 				}
 			}
 			else if (firewallChild.getNodeName().equals("PortType")) {
 				if (firewallChild.getTextContent().equals("Any")){
 					startPort = 0;
 					endPort = 65535;
 				}
 			}
 			else if (firewallChild.getNodeName().equals("Protocol")) {
 				logger.debug("Protocol = " + firewallChild.getTextContent());
 				if (firewallChild.getTextContent().equals("UDP")){
 					protocol = Protocol.UDP;
 				}
 				else if (firewallChild.getTextContent().equals("IPSEC")){
 					protocol = Protocol.IPSEC;
 				}
 				else if (firewallChild.getTextContent().equals("TCP")){
 					protocol = Protocol.TCP;
 				}
 				else { // Any, HTTP, HTTPS, FTP
 					protocol = Protocol.ANY;
 				}
 				logger.debug("Rule protocol = " + protocol);
 			}
 			else if (firewallChild.getNodeName().equals("Source")) {
 				Node sourceTypeNode = firewallChild.getFirstChild();
 				sourceType = sourceTypeNode.getTextContent();
 				Node sourceNode = sourceTypeNode.getNextSibling();
 
 				if (sourceType.equals("Any")){
 					source = RuleTarget.getGlobal(providerFirewallId);
 				}
 				else if (sourceType.equals(TerremarkIpAddressSupport.IP_ADDRESS_TAG)){
 					source = RuleTarget.getCIDR(sourceNode.getAttributes().getNamedItem(Terremark.NAME).getNodeValue() + "/32");
 				}
 				else if (sourceType.equals(TerremarkNetworkSupport.NETWORK_TAG)){
 					String vlanId = Terremark.hrefToNetworkId(sourceNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 					source = RuleTarget.getVlan(vlanId);
 				}
 				else if (sourceType.equals("ExternalIp")){
 					source = RuleTarget.getCIDR(sourceNode.getTextContent() + "/32");
 				}
 				else if (sourceType.equals("ExternalNetwork")){
 					Node networkAddressNode = sourceNode.getFirstChild();
 					String networkSize = networkAddressNode.getNextSibling().getTextContent();
 					source = RuleTarget.getCIDR(networkAddressNode.getTextContent() + "/" + networkSize);
 				}
 				else if (sourceType.equals("TrustedNetworkGroup")){
 					break;
 				}
 			}
 			else if (firewallChild.getNodeName().equals("Destination")) {
 				Node destinationTypeNode = firewallChild.getFirstChild();
 				destinationType = destinationTypeNode.getTextContent();
 				Node destinationNode = destinationTypeNode.getNextSibling();
 
 				if (destinationType.equals("Any")){
 					destination = RuleTarget.getGlobal(providerFirewallId);
 				}
 				else if (destinationType.equals(TerremarkIpAddressSupport.IP_ADDRESS_TAG)){
 					destination = RuleTarget.getCIDR(destinationNode.getAttributes().getNamedItem(Terremark.NAME).getNodeValue() + "/32");
 				}
 				else if (destinationType.equals(TerremarkNetworkSupport.NETWORK_TAG)){
 					String vlanId = Terremark.hrefToNetworkId(destinationNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 					destination = RuleTarget.getVlan(vlanId);
 				}
 				else if (sourceType.equals("ExternalIp")){
 					destination = RuleTarget.getCIDR(destinationNode.getTextContent() + "/32");
 					direction = Direction.EGRESS;
 				}
 				else if (destinationType.equals("ExternalNetwork")){
 					Node networkAddressNode = destinationNode.getFirstChild();
 					String networkSize = networkAddressNode.getNextSibling().getTextContent();
 					destination = RuleTarget.getCIDR(networkAddressNode.getTextContent() + "/" + networkSize);
 					direction = Direction.EGRESS;
 				}
 				else if (destinationType.equals("TrustedNetworkGroup")){
 					break;
 				}
 			}
 			else if (firewallChild.getNodeName().equals("PortRange")) {
 				startPort = Integer.parseInt(firewallChild.getFirstChild().getTextContent());
 				endPort = Integer.parseInt(firewallChild.getLastChild().getTextContent());
 			}
 
 		}
 		if (source != null && destination != null) {
 			rule = org.dasein.cloud.network.FirewallRule.getInstance(firewallRuleId, providerFirewallId, source, direction, protocol, permission, destination, startPort, endPort);
 		}
 		return rule;
 	}
 
 }
