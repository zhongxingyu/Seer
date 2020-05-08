 package org.dasein.cloud.terremark.network;
 
 import java.io.StringWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
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
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VirtualMachineSupport;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.network.AddressType;
 import org.dasein.cloud.network.IPVersion;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.cloud.network.IpAddressSupport;
 import org.dasein.cloud.network.IpForwardingRule;
 import org.dasein.cloud.network.NetworkInterface;
 import org.dasein.cloud.network.Protocol;
 import org.dasein.cloud.network.RawAddress;
 import org.dasein.cloud.network.VLAN;
 import org.dasein.cloud.network.VLANSupport;
 import org.dasein.cloud.terremark.EnvironmentsAndComputePools;
 import org.dasein.cloud.terremark.Terremark;
 import org.dasein.cloud.terremark.TerremarkException;
 import org.dasein.cloud.terremark.TerremarkMethod;
 import org.dasein.cloud.terremark.TerremarkMethod.HttpMethodName;
 import org.dasein.util.CalendarWrapper;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class TerremarkIpAddressSupport  implements IpAddressSupport {
 
 	// API Calls
 	public final static String IP_ADDRESSES      = "ipAddresses";
 	public final static String PUBLIC_IPS        = "publicIps";
 	public final static String INTERNET_SERVICES = "internetServices";
 	public final static String NODE_SERVICES     = "nodeServices";
 
 	// Response Tags
 	public final static String IP_ADDRESS_TAG              = "IpAddress";
 	public final static String IP_ADDRESSES_TAG            = "IpAddresses";
 	public final static String IP_ADDRESS_V6_TAG           = "IpAddressV6";
 	public final static String PUBLIC_IP_TAG               = "PublicIp";
 	public final static String HOST_TAG                    = "Host";
 	public final static String DETECTED_ON_TAG             = "DetectedOn";
 	public final static String INTERNET_SERVICE_TAG        = "InternetService";
 	public final static String NODE_SERVICE_TAG            = "NodeService";
 	public final static String RESERVED_TAG                = "Reserved";
 	public final static String TRUSTED_NETWORK_GROUP_TAG   = "TrustedNetworkGroup";
 	public final static String BACKUP_INTERNET_SERVICE_TAG = "BackupInternetService";
 
 
 	// Types
 	public final static String IP_ADDRESS_TYPE              = "application/vnd.tmrk.cloud.ipAddress";
 	public final static String IP_ADDRESS_IPV6_TYPE         = "application/vnd.tmrk.cloud.ipAddress.ipv6";
 	public final static String TRUSTED_NETWORK_GROUP_TYPE   = "application/vnd.tmrk.cloud.trustedNetworkGroup";
 	public final static String BACKUP_INTERNET_SERVICE_TYPE = "application/vnd.tmrk.cloud.backupInternetService";
 
 	//Operation Names
 	public final static String CONFIGURE_INTERNET_SERVICE_OPERATION = "Configure Internet Service";
 	public final static String CONFIGURE_NODE_SERVICE_OPERATION     = "Configure Node Service";
 	public final static String DELETE_PUBLIC_IP_OPERATION           = "Delete Public IP";
 	public final static String REMOVE_NODE_SERVICE_OPERATION        = "Remove Node Service";
 	public final static String REMOVE_INTERNET_SERVICE_OPERATION    = "Remove Internet Service";
 
 	public final static long DEFAULT_SLEEP             = CalendarWrapper.SECOND * 15;
 	public final static long DEFAULT_TIMEOUT           = CalendarWrapper.MINUTE * 20;
 
 	static Logger logger = Terremark.getLogger(TerremarkIpAddressSupport.class);
 
 	private Terremark provider = null;
 
 	TerremarkIpAddressSupport(@Nonnull Terremark provider) {
 		this.provider = provider;
 	}
 
 	private InternetService addInternetServiceToExistingIp(String addressId, String name, String protocolString, int publicPort) throws CloudException, InternalException {
 		InternetService service = null;
 		String url = "/" + INTERNET_SERVICES + "/" + PUBLIC_IPS + "/" + addressId + "/action/createInternetService";
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("CreateInternetService");
 			rootElement.setAttribute(Terremark.NAME, name);
 
 			Element protocolElement = doc.createElement("Protocol");
 			protocolElement.appendChild(doc.createTextNode(protocolString));
 			rootElement.appendChild(protocolElement);
 
 			Element portElement = doc.createElement("Port");
 			portElement.appendChild(doc.createTextNode(Integer.toString(publicPort)));
 			rootElement.appendChild(portElement);
 
 			Element enabledElement = doc.createElement("Enabled");
 			enabledElement.appendChild(doc.createTextNode("true"));
 			rootElement.appendChild(enabledElement);
 
 			Element persistenceElement = doc.createElement("Persistence");
 			Element persistenceTypeElement = doc.createElement("Type");
 			persistenceTypeElement.appendChild(doc.createTextNode("None"));
 			persistenceElement.appendChild(persistenceTypeElement);
 			rootElement.appendChild(persistenceElement);
 
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
 			Node internetServiceNode = doc.getElementsByTagName(INTERNET_SERVICE_TAG).item(0);
 			service = toInternetService(internetServiceNode);
 		}
 		return service;
 	}
 
 	private NodeService addNode(String internetServiceId, String privateAddressId, String name, int privatePort) throws CloudException, InternalException {
 		NodeService service = null;
 		String url = "/" + NODE_SERVICES + "/" + INTERNET_SERVICES + "/" + internetServiceId + "/action/createNodeService";
 		String body = "";
 		String[] ipAddressIds = privateAddressId.split("/");
 		String networkId;
 		String privateAddress;
 		if (ipAddressIds.length == 2) {
 			networkId = ipAddressIds[0];
 			privateAddress = ipAddressIds[1];
 		}
 		else {
 			throw new InternalException("Invalid private ip address id " + privateAddressId);
 		}
 		
 		boolean ipv6 = privateAddress.contains(":");
 		String type = "";
 		if (ipv6) {
 			type = IP_ADDRESS_IPV6_TYPE;
 		}
 		else {
 			type = IP_ADDRESS_TYPE;
 		}
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("CreateNodeService");
 			rootElement.setAttribute(Terremark.NAME, name);
 
 			Element ipAddressElement = doc.createElement("IpAddress");
 			String addressHref = Terremark.DEFAULT_URI_PATH + "/" + IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + networkId + "/" + privateAddress;
 			ipAddressElement.setAttribute(Terremark.HREF, addressHref);
 			ipAddressElement.setAttribute(Terremark.NAME, privateAddress);
 			ipAddressElement.setAttribute(Terremark.TYPE, type);
 			rootElement.appendChild(ipAddressElement);
 
 			Element portElement = doc.createElement("Port");
 			portElement.appendChild(doc.createTextNode(Integer.toString(privatePort)));
 			rootElement.appendChild(portElement);
 
 			Element enabledElement = doc.createElement("Enabled");
 			enabledElement.appendChild(doc.createTextNode("true"));
 			rootElement.appendChild(enabledElement);
 
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
 			Node nodeServiceNode = doc.getElementsByTagName(NODE_SERVICE_TAG).item(0);
 			service = toNodeService(nodeServiceNode, internetServiceId);
 		}
 		return service;
 	}
 
 	/**
 	 * Assigns the specified address to the target server. This method should be called only if
 	 * {@link #isAssigned(AddressType)} for the specified address's address type is <code>true</code>.
 	 * If it is not, you will see the {@link RuntimeException} {@link org.dasein.cloud.OperationNotSupportedException}
 	 * thrown.
 	 * @param addressId the unique identifier of the address to be assigned
 	 * @param serverId the unique ID of the server to which the address is being assigned
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws org.dasein.cloud.OperationNotSupportedException this cloud provider does not support address assignment of the specified address type
 	 */
 	@Override
 	public void assign(@Nonnull String addressId, @Nonnull String serverId) throws InternalException, CloudException {
 		throw new OperationNotSupportedException("Not supported.");
 	}
 
 	/**
 	 * Assigns the specified address to the specified network interface.
 	 * @param addressId the unique ID of the IP address to assign
 	 * @param nicId the unique ID of the network interface to which the address is being assigned
 	 * @throws InternalException an error occurred locally while performing the assignment
 	 * @throws CloudException an error occurred in the cloud provider while performing the assignment
 	 */
 	@Override
 	public void assignToNetworkInterface(String addressId, String nicId) throws InternalException, CloudException {
 		throw new OperationNotSupportedException("Not supported.");
 	}
 
 	private void enableInternetService(InternetService service) throws CloudException, InternalException {
 		String url = "/" + INTERNET_SERVICES + "/" + service.getId();
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement(INTERNET_SERVICE_TAG);
 			rootElement.setAttribute(Terremark.NAME, service.getName());
 
 			Element enabledElement = doc.createElement("Enabled");
 			enabledElement.appendChild(doc.createTextNode("true"));
 			rootElement.appendChild(enabledElement);
 
 			Element descriptionElement = doc.createElement("Description");
 			descriptionElement.appendChild(doc.createTextNode(service.getDescription()));
 			rootElement.appendChild(descriptionElement);
 
 			Element persistenceElement = doc.createElement("Persistence");
 			Element persistenceTypeElement = doc.createElement("Type");
 			persistenceTypeElement.appendChild(doc.createTextNode(service.getPersistenceType()));
 			persistenceElement.appendChild(persistenceTypeElement);
 			if (service.getPersistenceTimeout() != null) {
 				Element persistenceTimeoutElement = doc.createElement("Timeout");
 				persistenceTimeoutElement.appendChild(doc.createTextNode(service.getPersistenceTimeout()));
 				persistenceElement.appendChild(persistenceTimeoutElement);
 			}
 			rootElement.appendChild(persistenceElement);
 
 			if (service.getTrustedNetworkGroupHref() != null) {
 				Element trustedNetworkGroupElement = doc.createElement(TRUSTED_NETWORK_GROUP_TAG);
 				trustedNetworkGroupElement.setAttribute(Terremark.HREF, service.getTrustedNetworkGroupHref());
 				trustedNetworkGroupElement.setAttribute(Terremark.NAME, service.getTrustedNetworkGroupName());
 				trustedNetworkGroupElement.setAttribute(Terremark.TYPE, TRUSTED_NETWORK_GROUP_TYPE);
 				rootElement.appendChild(trustedNetworkGroupElement);
 			}
 
 			if (service.getBackupInternetServiceHref() != null) {
 				Element backupInternetServiceElement = doc.createElement(BACKUP_INTERNET_SERVICE_TAG);
 				backupInternetServiceElement.setAttribute(Terremark.HREF, service.getBackupInternetServiceHref());
 				backupInternetServiceElement.setAttribute(Terremark.NAME, service.getBackupInternetServiceName());
 				backupInternetServiceElement.setAttribute(Terremark.TYPE, BACKUP_INTERNET_SERVICE_TYPE);
 				rootElement.appendChild(backupInternetServiceElement);
 			}
 
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
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.PUT, url, null, body);
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, CONFIGURE_INTERNET_SERVICE_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 	}
 
 	private void enableNodeService(NodeService nodeService) throws CloudException, InternalException {
 		String url = "/" + NODE_SERVICES + "/" + nodeService.getId();
 		String body = "";
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement(NODE_SERVICE_TAG);
 			rootElement.setAttribute(Terremark.NAME, nodeService.getName());
 
 			Element enabledElement = doc.createElement("Enabled");
 			enabledElement.appendChild(doc.createTextNode("true"));
 			rootElement.appendChild(enabledElement);
 
 			Element descriptionElement = doc.createElement("Description");
 			descriptionElement.appendChild(doc.createTextNode(nodeService.getDescription()));
 			rootElement.appendChild(descriptionElement);
 
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
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.PUT, url, null, body);
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, CONFIGURE_NODE_SERVICE_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 	}
 
 	/**
 	 * Forwards the specified public IP address traffic on the specified public port over to the
 	 * specified private port on the specified server. If the server goes away, you will generally 
 	 * still have traffic being forwarded to the private IP formally associated with the server, so
 	 * it is best to stop forwarding before terminating a server.
 	 * <p>
 	 * You should check {@link #isForwarding()} before calling this method. The implementation should
 	 * throw a {@link org.dasein.cloud.OperationNotSupportedException} {@link RuntimeException} if the underlying
 	 * cloud does not support IP address forwarding.
 	 * </p>
 	 * @param addressId the unique ID of the public IP address to be forwarded
 	 * @param publicPort the public port of traffic to be forwarded
 	 * @param protocol the network protocol being forwarded (not all clouds support ICMP)
 	 * @param privatePort the private port on the server to which traffic should be forwarded
 	 * @param onServerId the unique ID of the server to which traffic is to be forwarded
 	 * @return the rule ID for the forwarding rule that is created
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws org.dasein.cloud.OperationNotSupportedException this cloud provider does not support address forwarding
 	 */
 	@Override
 	public @Nonnull String forward(@Nonnull String addressId, int publicPort, @Nonnull Protocol protocol, int privatePort, @Nonnull String onServerId) throws InternalException, CloudException {
 		logger.trace("enter - forward('" + addressId + "'," + publicPort + "," + protocol + "," + privatePort + ",'" + onServerId + "')");
 		String ruleId = "";
 		String protocolString;
 		String privateAddressId = null;
 		VirtualMachine server;
 		try {
 			server = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(onServerId);
 			RawAddress[] addresses = server.getPrivateAddresses();
 
 			if( addresses != null && addresses.length > 0 ) {
				privateAddressId = server.getProviderVlanId() + "/" + addresses[0].getIpAddress();
 			}
 			if( logger.isDebugEnabled() ) {
 				logger.debug("forward(): privateAddressId=" + privateAddressId);
 			}
 			if( privateAddressId == null ) {
 				logger.error("forward(): No private address exists for " + server.getProviderVirtualMachineId() + "/" + server.getName());
 				throw new CloudException("No private address exists for " + server.getProviderVirtualMachineId() + "/" + server.getName());
 			}
 			switch( protocol ) {
 			case TCP: protocolString = "TCP"; break;
 			case UDP: protocolString = "UDP"; break;
 			default: throw new CloudException("Terremark does not support ICMP forwarding.");
 			}
 
 			Collection<InternetService> services = Collections.emptyList();
 			try {
 				if( logger.isInfoEnabled() ) {
 					logger.info("forward(): Looking up internet services for " + addressId);
 				}
 				services = getInternetServicesOnPublicIp(addressId);
 			}
 			catch(CloudException e) {
 				logger.error("forward(): Could not load services on IP " + addressId);
 				if( logger.isDebugEnabled() ) {
 					e.printStackTrace();
 				}
 				throw new CloudException(e);
 			}
 
 			InternetService svc = null;
 			NodeService node = null;
 
 			for( InternetService service : services ) {
 				if( logger.isDebugEnabled() ) {
 					logger.info("forward(): Checking " + service.getPort() + " against " + publicPort);
 				}
 				if( (service.getPort() == publicPort) && (service.getProtocol() == protocol)) {
 					if (!service.isEnabled()) {
 						try {
 							enableInternetService(service);
 						}
 						catch(CloudException e) {
 							logger.error("forward(): Could not enable service " + service.getId());
 							if( logger.isDebugEnabled() ) {
 								e.printStackTrace();
 							}
 							throw new CloudException(e);
 						}
 					}
 					svc = service;
 					break;
 				}
 			}
 			if( logger.isDebugEnabled() ) {
 				logger.debug("forward(): service=" + svc);
 			}
 			if( svc == null ) {
 				try { 
 					if( logger.isInfoEnabled() ) {
 						logger.info("forward(): Adding internet service on " + publicPort + " to " + addressId);
 					}
 					String name = "CUSTOM-" + System.currentTimeMillis();
 					svc = addInternetServiceToExistingIp(addressId, name, protocolString, publicPort);
 				}
 				catch( CloudException e ) {
 					logger.error("forward(): Could not create internet service for IP address " + addressId + ": " + e.getMessage());
 					if( logger.isDebugEnabled() ) {
 						e.printStackTrace();
 					}
 					throw new CloudException(e);
 				}
 			}
 			else {
 				if( logger.isDebugEnabled() ) {
 					logger.debug("forward(): service.getId()=" + svc.getId());
 				}
 
 				Collection<NodeService> nodeServices = Collections.emptyList();
 				try {
 					if( logger.isInfoEnabled() ) {
 						logger.info("forward(): Looking up internet services for " + addressId);
 					}
 					nodeServices = getNodeServicesOnInternetService(svc.getId());
 				}
 				catch(CloudException e) {
 					logger.error("forward(): Could not load node services on internet service " + svc.getId());
 					if( logger.isDebugEnabled() ) {
 						e.printStackTrace();
 					}
 					throw new CloudException(e);
 				}
 
 				for (NodeService nodeService : nodeServices) {
 					if( logger.isDebugEnabled() ) {
 						logger.debug("forward(): Checking private port from node " + nodeService.getPort() + " against " + privatePort);
 					}
 					if( nodeService.getPort() == privatePort ) {
 						if (nodeService.getPrivateIpAddressId().equals(privateAddressId)) {
 							if (nodeService.isEnabled()) {
 								node = nodeService;
 								break;
 							}
 							else {
 								try {
 									enableNodeService(nodeService);
 								}
 								catch(CloudException e) {
 									logger.error("forward(): Could not enable node service " + nodeService.getId());
 									if( logger.isDebugEnabled() ) {
 										e.printStackTrace();
 									}
 									throw new CloudException(e);
 								}
 								node = nodeService;
 								break;
 							}
 						}
 						else {
 							logger.info("forward(): Adding a node service that will load balance to multiple servers over port " + privatePort);
 						}
 					}
 				}
 
 			}
 
 			if (node == null) {
 				try {
 					if( logger.isInfoEnabled() ) {
 						logger.info("forward(): Adding node " + privatePort + " to " + addressId + " against " + svc.getId());
 					}
 					String nodeServiceName = "Forward-" + System.currentTimeMillis();
 					node = addNode(svc.getId(), privateAddressId, nodeServiceName, privatePort);
 				}
 				catch( CloudException e ) {
 					logger.error("forward(): Failed to add node to internet service: " + e.getMessage());
 					if( logger.isInfoEnabled() ) {
 						e.printStackTrace();
 					}
 					throw new CloudException(e);
 				}
 			}
 			if( logger.isInfoEnabled() ) {
 				logger.info("forward(): New node is " + node.getId());
 			}
 
 			ruleId = svc.getId() + ":" + node.getId();
 
 		}
 		catch( RuntimeException e ) {
 			logger.error("forward(): Runtime exception while executing method: " + e.getMessage());
 			e.printStackTrace();
 			throw new InternalException(e);
 		}
 		catch( Error e ) {
 			logger.error("forward(): Error while executing method: " + e.getMessage());
 			e.printStackTrace();
 			throw new InternalException(e);
 		}
 		finally {
 			logger.debug("exit - forward()");
 		}
 		return ruleId;
 	}
 
 	private Collection<InternetService> getInternetServicesOnPublicIp(String addressId) throws CloudException, InternalException {
 		Collection<InternetService> internetServices = new ArrayList<InternetService>();
 		String url = "/" + PUBLIC_IPS + "/" + addressId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList internetServiceNodes = doc.getElementsByTagName(INTERNET_SERVICE_TAG);
 			for (int i=0; i<internetServiceNodes.getLength(); i++) {
 				Node internetService = internetServiceNodes.item(i);
 				InternetService service = toInternetService(internetService);
 				internetServices.add(service);
 			}
 		}
 		return internetServices;
 	}
 
 	/**
 	 * Provides the {@link IpAddress} identified by the specified unique address ID.
 	 * @param addressId the unique ID of the IP address being requested
 	 * @return the matching {@link IpAddress}
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 */
 	@Override
 	public @Nullable IpAddress getIpAddress(@Nonnull String addressId) throws InternalException, CloudException {
 		IpAddress ip = null;
 		boolean publicIp = !addressId.contains("/");
 		if (publicIp) {
 			String url = "/" + PUBLIC_IPS + "/" + addressId;
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 			Document doc = null;
 			try {
 				doc = method.invoke();
 			}
 			catch (TerremarkException e) {
 				logger.warn("Failed to get public ip address " + addressId);
 			}
 			catch (CloudException e) {
 				logger.warn("Failed to get public ip address " + addressId);
 			}
 			catch (InternalException e) {
 				logger.warn("Failed to get public ip address " + addressId);
 			}
 			if (doc != null) {
 				NodeList publicIps = doc.getElementsByTagName(PUBLIC_IP_TAG);
 				ip = toPublicIp(publicIps.item(0));
 			}
 		}
 		else {
 			String url = "/" + IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + addressId;
 
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 			Document doc = null;
 			try {
 				doc = method.invoke();
 			}
 			catch (TerremarkException e) {
 				logger.warn("Failed to get private ip address " + addressId);
 			}
 			catch (CloudException e) {
 				logger.warn("Failed to get private ip address " + addressId);
 			}
 			catch (InternalException e) {
 				logger.warn("Failed to get private ip address " + addressId);
 			}
 			if (doc != null) {
 				NodeList privateIps = doc.getElementsByTagName(IP_ADDRESS_TAG);
 				ip = toPrivateIp(privateIps.item(0), true);
 			}
 		}
 		return ip;
 	}
 
 	private Collection<IpAddress> getIpAddresses(Document doc, String networkId, boolean unassignedOnly, boolean reservableOnly) throws CloudException, InternalException {
 		logger.trace("enter - getIpAddresses(" + doc + ", " + networkId + ", " + unassignedOnly + ", " + reservableOnly + ")");
 		Collection<IpAddress> ips = new ArrayList<IpAddress>();
 		NodeList ipAddresses = doc.getElementsByTagName(IP_ADDRESS_TAG);
 		logger.debug("Found " + ipAddresses.getLength() + " ip addresses in network " + networkId);
 		for (int i = 0; i < ipAddresses.getLength(); i++) {
 			Node ipAddress = ipAddresses.item(i);
 			NodeList ipAddressChildren = ipAddress.getChildNodes();
 			boolean available = true;
 			boolean reservable = true;
 			boolean reserved = false;
 			String networkHostId = null;
 			boolean hostUnavailable = false;
 			for (int j = 0; j < ipAddressChildren.getLength(); j++) {
 				Node ipChild = ipAddressChildren.item(j);
 				String ipChildName = ipChild.getNodeName();
 				if (reservableOnly && ipChildName.equalsIgnoreCase("Actions")) {
 					NodeList actions = ipChild.getChildNodes();
 					for (int k=0; k<actions.getLength(); k++) {
 						NamedNodeMap actionAttrs = actions.item(k).getAttributes();
 						if (actionAttrs.getNamedItem(Terremark.NAME).getNodeValue().equals("reserve")) {
 							if (actionAttrs.getNamedItem("actionDisabled") != null) {
 								reservable = false;
 							}
 						}
 					}
 				}
 				if (ipChildName.equalsIgnoreCase(HOST_TAG) || ipChildName.equalsIgnoreCase(DETECTED_ON_TAG)) {
 					String hostHref = ipChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 					Node accessible = ipChild.getAttributes().getNamedItem(Terremark.ACCESSIBLE);
 					if (accessible != null) {
 						hostUnavailable = accessible.getNodeValue().equals("false");
 					}
 					networkHostId = Terremark.hrefToId(hostHref);
 					available = false;
 				}
 				else if (ipChildName.equalsIgnoreCase("Reserved")) {
 					reserved = ipChild.getTextContent().equalsIgnoreCase("true");
 				}
 			}
 			if (available) {
 				String address = ipAddress.getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 				if ((reservableOnly && reservable) || !reservableOnly) {
 					IpAddress ip = new IpAddress();
 					ip.setAddress(address);
 					ip.setIpAddressId(networkId + "/" + address);
 					ip.setProviderVlanId(networkId);
 					ip.setForVlan(true);
 					ip.setAddressType(AddressType.PRIVATE);
 					if (networkId.contains("ipv6")) {
 						ip.setVersion(IPVersion.IPV6);
 					}
 					else {
 						ip.setVersion(IPVersion.IPV4);
 					}
 					ip.setRegionId(provider.getContext().getRegionId());
 					ip.setReserved(reserved);
 					logger.debug("getIpAddresses(): Adding ip: " + ip);
 					ips.add(ip);
 				}
 				else {
 					logger.debug("getIpAddresses(): Skipping ip: " + address);
 				}
 			}
 			else {
 				logger.debug("getIpAddresses(): Ip unavailable");
 				if (!unassignedOnly) {
 					IpAddress ip = new IpAddress();
 					String address = ipAddress.getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 					ip.setAddress(address);
 					ip.setIpAddressId(networkId + "/" + address);
 					ip.setProviderVlanId(networkId);
 					ip.setForVlan(true);
 					ip.setAddressType(AddressType.PRIVATE);
 					if (networkId.contains("ipv6")) {
 						ip.setVersion(IPVersion.IPV6);
 					}
 					else {
 						ip.setVersion(IPVersion.IPV4);
 					}
 					ip.setRegionId(provider.getContext().getRegionId());
 					NetworkInterface host = null;
 					if (hostUnavailable == false) {
 						host = provider.getNetworkServices().getVlanSupport().getNetworkInterface(networkHostId);
 					}
 					if (host != null) {
 						ip.setServerId(host.getProviderVirtualMachineId());
 					}
 					ip.setReserved(reserved);
 					logger.debug("getIpAddresses(): Adding ip: " + ip);
 					ips.add(ip);
 				}
 			}
 		}
 		logger.debug("getIpAddresses(): Found " + ips.size() + " ips matching the request.");
 		logger.trace("exit - getIpAddresses(" + doc + ", " + networkId + ", " + unassignedOnly + ", " + reservableOnly + ")");
 		return ips;
 	}
 
 	private Collection<NodeService> getNodeServicesOnInternetService(String internetServiceId) throws CloudException, InternalException {
 		Collection<NodeService> nodeServices = new ArrayList<NodeService>();
 		String url = "/" + INTERNET_SERVICES + "/" + internetServiceId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList nodeServiceNodes = doc.getElementsByTagName(NODE_SERVICE_TAG);
 			for (int i=0; i<nodeServiceNodes.getLength(); i++) {
 				Node nodeService = nodeServiceNodes.item(i);
 				NodeService service = toNodeService(nodeService, internetServiceId);
 				nodeServices.add(service);
 			}
 		}
 		return nodeServices;
 	}
 
 	/**
 	 * The cloud provider-specific term for an IP address. It's hard to fathom what other
 	 * than "IP address" anyone could use.
 	 * @param locale the locale into which the term should be translated
 	 * @return the cloud provider-specific term for an IP address
 	 */
 	@Override
 	public @Nonnull String getProviderTermForIpAddress(@Nonnull Locale locale) {
 		return "IP address";
 	}
 
 	private Collection<IpAddress> getReservedIpAddresses(Document doc, String networkId) throws CloudException, InternalException {
 		logger.trace("enter - getReservedIpAddresses(" + doc + ", " + networkId + ")");
 		Collection<IpAddress> ips = new ArrayList<IpAddress>();
 		NodeList ipAddresses = doc.getElementsByTagName(IP_ADDRESS_TAG);
 		logger.debug("getReservedIpAddresses(): Found " + ipAddresses.getLength() + " ip addresses in network " + networkId);
 		for (int i = 0; i < ipAddresses.getLength(); i++) {
 			Node ipAddress = ipAddresses.item(i);
 			NodeList ipAddressChildren = ipAddress.getChildNodes();
 			boolean reserved = true;
 			String networkHostId = null;
 			for (int j = 0; j < ipAddressChildren.getLength(); j++) {
 				Node ipChild = ipAddressChildren.item(j);
 				String ipChildName = ipChild.getNodeName();
 				if (ipChildName.equalsIgnoreCase(HOST_TAG) || ipChildName.equalsIgnoreCase(DETECTED_ON_TAG)) {
 					String hostHref = ipChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 					networkHostId = Terremark.hrefToId(hostHref);
 				}
 				else if (ipChildName.equalsIgnoreCase("Reserved")) {
 					reserved = ipChild.getTextContent().equalsIgnoreCase("true");
 				}
 			}
 			if (reserved) {
 				IpAddress ip = new IpAddress();
 				String address = ipAddress.getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 				ip.setAddress(address);
 				ip.setReserved(reserved);
 				ip.setIpAddressId(networkId + "/" + address);
 				ip.setProviderVlanId(networkId);
 				ip.setForVlan(true);
 				ip.setAddressType(AddressType.PRIVATE);
 				if (networkId.contains("ipv6")) {
 					ip.setVersion(IPVersion.IPV6);
 				}
 				else {
 					ip.setVersion(IPVersion.IPV4);
 				}
 				ip.setRegionId(provider.getContext().getRegionId());
 				if (networkHostId != null) {
 					NetworkInterface host = provider.getNetworkServices().getVlanSupport().getNetworkInterface(networkHostId);
 					ip.setServerId(host.getProviderVirtualMachineId());
 				}
 				logger.debug("getReservedIpAddresses(): Adding ip: " + ip);
 				ips.add(ip);
 			}
 		}
 		logger.debug("getReservedIpAddresses(): Found " + ips.size() + " ips matching the request.");
 		logger.trace("exit - getReservedIpAddresses(" + doc + ", " + networkId + ")");
 		return ips;
 	}
 
 	private Collection<ResourceStatus> getReservedIpAddressesStatus(Document doc, String networkId) throws CloudException, InternalException {
 		Collection<ResourceStatus> ips = new ArrayList<ResourceStatus>();
 		NodeList ipAddresses = doc.getElementsByTagName(IP_ADDRESS_TAG);
 		logger.debug("getReservedIpAddressesStatus(): Found " + ipAddresses.getLength() + " ip addresses in network " + networkId);
 		for (int i = 0; i < ipAddresses.getLength(); i++) {
 			Node ipAddress = ipAddresses.item(i);
 			NodeList ipAddressChildren = ipAddress.getChildNodes();
 			boolean reserved = true;
 			String networkHostId = null;
 			for (int j = 0; j < ipAddressChildren.getLength(); j++) {
 				Node ipChild = ipAddressChildren.item(j);
 				String ipChildName = ipChild.getNodeName();
 				if (ipChildName.equalsIgnoreCase(HOST_TAG) || ipChildName.equalsIgnoreCase(DETECTED_ON_TAG)) {
 					String hostHref = ipChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 					networkHostId = Terremark.hrefToId(hostHref);
 				}
 				else if (ipChildName.equalsIgnoreCase("Reserved")) {
 					reserved = ipChild.getTextContent().equalsIgnoreCase("true");
 				}
 			}
 			if (reserved) {
 				String address = ipAddress.getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 				String id = networkId + "/" + address;
 				logger.debug("getReservedIpAddressesStatus(): Adding ip: " + id);
 				ips.add(new ResourceStatus(id, networkHostId == null));
 			}
 		}
 		return ips;
 	}
 	
 	/**
 	 * Finds a private ip address that is not tied to a server and unreserves it if it is reserved.
 	 * @param networkId the id of the network containing the private ips being sought
 	 * @param true if you want the ip address to be reserved, false if you want it to be unreserved
 	 * @return Available private IP addresses from the network in the reserved/unreserved state requested
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 */
 	@Nonnull
 	public String getUnreservedAvailablePrivateIp(String networkId) throws InternalException, CloudException {
 		logger.trace("enter - getUnreservedAvailablePrivateIp(" + networkId + ")");
 		IpAddress availableIp = null;
 		Iterator<IpAddress> privateIps = null;
 		if (networkId.contains("ipv6")) {
 			privateIps = getNextAvailableIpv6Address(networkId).iterator();
 		}
 		else {
 			privateIps = listPrivateIps(networkId, true, false, false).iterator();
 		}
 		String availableIpId = null;
 		String availableIpAddress = null;
 		boolean reserved = false;
 		if (privateIps.hasNext()) {
 			availableIp = privateIps.next();
 			availableIpId = availableIp.getProviderIpAddressId();
 			availableIpAddress = availableIp.getRawAddress().getIpAddress();
 			reserved = availableIp.isReserved();
 		}
 		else {
 			logger.warn("getUnreservedAvailablePrivateIp(): Failed to find available ip");
 		}
 
 		if (availableIp != null && reserved) {
 			unreserveIp(availableIpId);
 		}
 		logger.trace("exit - getUnreservedAvailablePrivateIp(" + networkId + ")");
 		return availableIpAddress;
 	}
 
 	private Collection<IpAddress> getNextAvailableIpv6Address(String networkId) throws CloudException {
 		logger.trace("enter - getNextAvailableIpv6Address(" + networkId + ")");
 		Collection<IpAddress> addresses = new ArrayList<IpAddress>();
 		String url = "/" + TerremarkNetworkSupport.NETWORKS + "/" + networkId;
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		} catch (TerremarkException e) {
 			logger.warn("Failed to get network " + networkId);
 		} catch (CloudException e) {
 			logger.warn("Failed to get network " + networkId);
 		} catch (InternalException e) {
 			logger.warn("Failed to get network " + networkId);
 		}
 		if (doc != null){
 			String ipv6Address = doc.getElementsByTagName(IP_ADDRESSES_TAG).item(0).getLastChild().getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 			try {
 				InetAddress lastUsedIp = InetAddress.getByName(ipv6Address);
 				InetAddress nextIp = com.google.common.net.InetAddresses.increment(lastUsedIp);
 				IpAddress ip = new IpAddress();
 				ip.setAddress(nextIp.getHostAddress());
 				ip.setAddressType(AddressType.PRIVATE);
 				ip.setVersion(IPVersion.IPV6);
 				ip.setReserved(false);
 				ip.setForVlan(true);
 				ip.setProviderVlanId(networkId);
 				ip.setRegionId(provider.getContext().getRegionId());
 				logger.debug("getNextAvailableIpv6Address(): Next ip is " + ip.getRawAddress().getIpAddress());
 				addresses.add(ip);
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			}
 		}
 		logger.trace("exit - getNextAvailableIpv6Address(" + networkId + ")");
 		return addresses;
 	}
 
 	/**
 	 * Indicates whether you need to specify which VLAN you are tying a static IP address to when creating an
 	 * IP address for use in a VLAN. REQUIRED means you must specify the VLAN, OPTIONAL means you may, and NONE
 	 * means you do not specify a VLAN.
 	 * @return the level of requirement for specifying a VLAN when creating a VLAN IP address
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 */
 	@Override
 	public Requirement identifyVlanForVlanIPRequirement() throws CloudException, InternalException {
 		return Requirement.REQUIRED;
 	}
 
 	/**
      * When addresses are assignable, they may be assigned at launch, post-launch, or both.
      * {@link VirtualMachineSupport#identifyStaticIPRequirement()} will tell you what must be done
      * at launch time. This method indicates whether or not assignable IPs may be assigned after launch. This
      * method should never return true when {@link #isAssigned(IPVersion)} returns false.
      * @param version the IP version being checked
      * @return true if IP addresses of the specified version can be assigned post launch
      * @throws CloudException an error occurred with the cloud provider determining support
      * @throws InternalException a local error occurred determining support
      */
 	@Override
 	public boolean isAssignablePostLaunch(IPVersion version) throws CloudException, InternalException {
 		return false;
 	}
 
 	/**
 	 * Indicates whether the underlying cloud supports the assignment of addresses of the specified
 	 * type.
 	 * @param type the type of address being checked (public or private)
 	 * @return <code>true</code> if addresses of the specified type are assignable to servers
 	 * @deprecated use {@link #isAssigned(IPVersion)}
 	 */
 	@Override
 	public boolean isAssigned(@Nonnull AddressType type) {
 		return false;
 	}
 
 
 	/**
 	 * Indicates whether the underlying cloud supports the assignment of addresses of the specified version
 	 * @param version the IP version being checked
 	 * @return true if the addresses of the specified version are assignable to cloud resources for public routing
 	 * @throws CloudException an error occurred with the cloud provider determining support
 	 * @throws InternalException a local error occurred determining support
 	 */
 	@Override
 	public boolean isAssigned(IPVersion version) throws CloudException, InternalException {
 		return false;
 	}
 
 	/**
 	 * Indicates whether the underlying cloud supports the forwarding individual port traffic on 
 	 * public IP addresses to hosts private IPs. These addresses may also be used for load
 	 * balancers in some clouds as well.
 	 * @return <code>true</code> if public IPs may be forwarded on to private IPs
 	 * @deprecated use {@link #isForwarding(IPVersion)}
 	 */
 	@Override
 	public boolean isForwarding() {
 		return true;
 	}
 
 	/**
 	 * Indicates whether the underlying cloud supports the forwarding of traffic on individual ports
 	 * targeted to addresses of the specified version on to resources in the cloud.
 	 * @param version the IP version being checked
 	 * @return true if forwarding is supported
 	 * @throws CloudException an error occurred with the cloud provider determining support
 	 * @throws InternalException a local error occurred determining support
 	 */
 	@Override
 	public boolean isForwarding(IPVersion version) throws CloudException, InternalException {
 		return true;
 	}
 
 	/**
 	 * Indicates whether the underlying cloud allows you to make programmatic requests for
 	 * new IP addresses of the specified type
 	 * @param type the type of address being checked (public or private)
 	 * @return <code>true</code> if there are programmatic mechanisms for allocating new IPs of the specified type
 	 * @deprecated use {@link #isRequestable(IPVersion)}
 	 */
 	@Override
 	public boolean isRequestable(@Nonnull AddressType type){
 		return true;
 	}
 
 	/**
 	 * Indicates whether or not you can request static IP addresses of the specified Internet Protocol version.
 	 * @param version the IP version you may want to request
 	 * @return true if you can make requests from the cloud provider to add addresses of this version to your pool
 	 * @throws CloudException an error occurred with the cloud provider while determining if your account has support
 	 * @throws InternalException a local exception occurred while determining support
 	 */
 	@Override
 	public boolean isRequestable(IPVersion version) throws CloudException, InternalException {
 		return true;
 	}
 
 	/**
 	 * Indicates whether this account is subscribed to leverage IP address services in the
 	 * target cloud.
 	 * @return <code>true</code> if the account holder is subscribed
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 */
 	@Override
 	public boolean isSubscribed() throws CloudException, InternalException {
 		return true;
 	}
 
 	/**
 	 * Lists all IP addresses of the specified IP version that are allocated to the account holder's IP address pool. If
 	 * the specified version is not supported, an empty list should be returned.
 	 * @param version the version of the IP protocol for which you are looking for IP addresses
 	 * @param unassignedOnly show only IP addresses that have yet to be assigned to cloud resources
 	 * @return all matching IP addresses from the IP address pool
 	 * @throws InternalException a local error occurred loading the IP addresses
 	 * @throws CloudException an error occurred with the cloud provider while requesting the IP addresses
 	 */
 	@Override
 	public Iterable<IpAddress> listIpPool(IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
 		logger.trace("enter - listIpPool(" + version + ", " + unassignedOnly + ")");
 		Collection<IpAddress> ips = new ArrayList<IpAddress>();
 
 		// List private addresses
 		Iterable<VLAN> networks = provider.getNetworkServices().getVlanSupport().listVlans();
 		for (VLAN network : networks) {
 			String networkId = network.getProviderVlanId();
 			if ((networkId.contains("ipv6") && version.equals(IPVersion.IPV6)) || (!networkId.contains("ipv6") && version.equals(IPVersion.IPV4))) {
 				Iterable<IpAddress> networkIps = listPrivateIps(networkId, unassignedOnly, false, true);
 				for (IpAddress networkIp : networkIps) {
 					ips.add(networkIp);
 				}
 			}
 		}
 
 		// List public addresses
 		String url = "/" + PUBLIC_IPS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId();
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList publicIps = doc.getElementsByTagName(PUBLIC_IP_TAG);
 			for (int i=0; i < publicIps.getLength(); i++) {
 				IpAddress publicIp = toPublicIp(publicIps.item(i));
 				if (publicIp != null && version.equals(publicIp.getVersion())) {
 					ips.add(publicIp);
 				}
 			}
 		}	
 		logger.trace("exit - listIpPool(" + version + ", " + unassignedOnly + ")");
 		return ips;
 	}
 	
 	/**
 	 * Lists all IP addresses of the specified IP version that are allocated to the account holder's IP address pool. If
 	 * the specified version is not supported, an empty list should be returned.
 	 * @param version the version of the IP protocol for which you are looking for IP addresses
 	 * @param unassignedOnly show only IP addresses that have yet to be assigned to cloud resources
 	 * @return all matching IP addresses from the IP address pool
 	 * @throws InternalException a local error occurred loading the IP addresses
 	 * @throws CloudException an error occurred with the cloud provider while requesting the IP addresses
 	 */
 	protected Iterable<IpAddress> lisPrivatetIpPool() throws InternalException, CloudException {
 		Collection<IpAddress> ips = new ArrayList<IpAddress>();
 		Iterable<VLAN> networks = provider.getNetworkServices().getVlanSupport().listVlans();
 		for (VLAN network : networks) {
 			String networkId = network.getProviderVlanId();
 			Iterable<IpAddress> networkIps = listPrivateIps(networkId, false, false, false);
 			for (IpAddress networkIp : networkIps) {
 				ips.add(networkIp);
 			}
 		}		
 		return ips;
 	}
 
 	/**
 	 * Lists the status of all IP addresses of the specified IP version that are allocated to the account holder's IP
 	 * address pool. If the specified version is not supported, an empty list should be returned.
 	 * @param version the version of the IP protocol for which you are looking for IP addresses
 	 * @return the status of all matching IP addresses from the IP address pool
 	 * @throws InternalException a local error occurred loading the IP addresses
 	 * @throws CloudException an error occurred with the cloud provider while requesting the IP addresses
 	 */
 	@Override
 	public Iterable<ResourceStatus> listIpPoolStatus(IPVersion version) throws InternalException, CloudException {
 		Collection<ResourceStatus> ipsStatus = new ArrayList<ResourceStatus>();
 
 		// List private addresses
 		Iterable<VLAN> networks = provider.getNetworkServices().getVlanSupport().listVlans();
 		for (VLAN network : networks) {
 			String networkId = network.getProviderVlanId();
 			Iterable<ResourceStatus> networkIpsStatus = listPrivateIpsStatus(networkId, version);
 			for (ResourceStatus networkIpStatus : networkIpsStatus) {
 				ipsStatus.add(networkIpStatus);
 			}
 		}
 
 		// List public addresses
 		String url = "/" + PUBLIC_IPS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId();
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList publicIps = doc.getElementsByTagName(PUBLIC_IP_TAG);
 			for (int i=0; i < publicIps.getLength(); i++) {
 				ResourceStatus publicIpStatus = toPublicIpStatus(publicIps.item(i), version);
 				if (publicIpStatus != null) {
 					ipsStatus.add(publicIpStatus);
 				}
 			}
 		}		
 		return ipsStatus;
 	}
 
 	/**
 	 * Lists all (or unassigned) reserved private IP addresses from the account holder's private IP address
 	 * pool. This method is safe to call even if private IP forwarding is not supported. It will
 	 * simply return {@link java.util.Collections#emptyList()}.
 	 * @param unassignedOnly indicates that only unassigned addresses are being sought
 	 * @return all private IP addresses or the unassigned ones from the pool 
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws OperationNotSupportedException the requested version is not supported
 	 * @deprecated private IP pools no longer make sense, use the {@link VLANSupport} class
 	 */
 	@Override
 	public @Nonnull Iterable<IpAddress> listPrivateIpPool(boolean unassignedOnly) throws InternalException, CloudException {
 		logger.trace("enter - listPrivateIpPool(" + unassignedOnly + ")");
 		Collection<IpAddress> ips = new ArrayList<IpAddress>();
 		Iterable<VLAN> networks = provider.getNetworkServices().getVlanSupport().listVlans();
 		for (VLAN network : networks) {
 			String networkId = network.getProviderVlanId();
 			if (!networkId.contains("ipv6")) {
 				Iterable<IpAddress> networkIps = listPrivateIps(networkId, unassignedOnly, false, true);
 				for (IpAddress networkIp : networkIps) {
 					ips.add(networkIp);
 				}
 			}
 		}		
 		logger.trace("exit - listPrivateIpPool(" + unassignedOnly + ")");
 		return ips;
 	}
 	
 	/**
 	 * Lists all (or unassigned, reservable or reserved) private IP addresses within a network.
 	 * @param networkId the id of the network containing the private ips being sought
 	 * @param unassignedOnly indicates that only unassigned addresses are being sought
 	 * @param reservableOnly indicated that only reservable addresses are being sought
 	 * @param reservedOnly indicated that only reserved addresses are being sought
 	 * @return all private IP addresses or the unassigned ones from the network 
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 */
 	protected @Nonnull Iterable<IpAddress> listPrivateIps(String networkId, boolean unassignedOnly, boolean reservableOnly, boolean reservedOnly) throws InternalException, CloudException {
 		logger.trace("enter - listPrivateIps(" + networkId + ", " + unassignedOnly + ", " + reservableOnly + ", " + reservedOnly + ")");
 		Collection<IpAddress> addresses = new ArrayList<IpAddress>();
 		String url = "/" + TerremarkNetworkSupport.NETWORKS + "/" + networkId;
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		} catch (TerremarkException e) {
 			logger.warn("Failed to get network " + networkId);
 		} catch (CloudException e) {
 			logger.warn("Failed to get network " + networkId);
 		} catch (InternalException e) {
 			logger.warn("Failed to get network " + networkId);
 		}
 		if (doc != null){
 			if (reservedOnly) {
 				addresses = getReservedIpAddresses(doc, networkId);
 			}
 			else {
 				addresses = getIpAddresses(doc, networkId, unassignedOnly, reservableOnly);
 			}
 		}
 		logger.trace("exit - listPrivateIps(" + networkId + ", " + unassignedOnly + ", " + reservableOnly + ", " + reservedOnly + ")");
 		return addresses;
 	}
 
 	/**
 	 * Lists all (or unassigned, reservable or reserved) private IP addresses within a network.
 	 * @param networkId the id of the network containing the private ips being sought
 	 * @param unassignedOnly indicates that only unassigned addresses are being sought
 	 * @param reservableOnly indicated that only reservable addresses are being sought
 	 * @param reservedOnly indicated that only reserved addresses are being sought
 	 * @return all private IP addresses or the unassigned ones from the network 
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 */
 	public @Nonnull Iterable<ResourceStatus> listPrivateIpsStatus(String networkId, IPVersion version) throws InternalException, CloudException {
 		Collection<ResourceStatus> addresses = new ArrayList<ResourceStatus>();
 		String url = "/" + TerremarkNetworkSupport.NETWORKS + "/" + networkId;
 		if (version.equals(IPVersion.IPV6)) {
 			url = url + "/ipv6";
 		}
 
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = null;
 		try {
 			doc = method.invoke();
 		} catch (TerremarkException e) {
 			logger.warn("Failed to get network " + networkId);
 		} catch (CloudException e) {
 			logger.warn("Failed to get network " + networkId);
 		} catch (InternalException e) {
 			logger.warn("Failed to get network " + networkId);
 		}
 		if (doc != null){
 				addresses = getReservedIpAddressesStatus(doc, networkId);
 		}
 		return addresses;
 	}
 
 	/**
 	 * Lists all (or unassigned) public IP addresses from the account holder's public IP address
 	 * pool. This method is safe to call even if public IP forwarding is not supported. It will
 	 * simply return {@link java.util.Collections#emptyList()}.
 	 * @param unassignedOnly indicates that only unassigned addresses are being sought
 	 * @return all public IP addresses or the unassigned ones from the pool 
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws OperationNotSupportedException the requested version is not supported
 	 * @deprecated use {@link #listIpPool(IPVersion, boolean)}
 	 */
 	@Override
 	public @Nonnull Iterable<IpAddress> listPublicIpPool(boolean unassignedOnly) throws InternalException, CloudException {
 		Collection<IpAddress> ips = new ArrayList<IpAddress>();
 		String url = "/" + PUBLIC_IPS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId();
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList publicIps = doc.getElementsByTagName(PUBLIC_IP_TAG);
 			for (int i=0; i < publicIps.getLength(); i++) {
 				IpAddress publicIp = toPublicIp(publicIps.item(i));
 				if (publicIp != null) {
 					ips.add(publicIp);
 				}
 			}
 		}
 		return ips;
 	}
 
 	/**
 	 * Lists the IP forwarding rules associated with the specified public IP address. This method
 	 * is safe to call even when requested on a private IP address or when IP forwarding is not supported.
 	 * In those situations, {@link java.util.Collections#emptyList()} will be returned.
 	 * @param addressId the unique ID of the public address whose forwarding rules will be sought
 	 * @return all IP forwarding rules for the specified IP address
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 */
 	@Override
 	public @Nonnull Iterable<IpForwardingRule> listRules(@Nonnull String addressId) throws InternalException, CloudException {
 		Collection<IpForwardingRule> rules = new ArrayList<IpForwardingRule>();
 		String url = "/" + PUBLIC_IPS + "/" + addressId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			rules = toIpForwardingRule(doc);
 		}
 		return rules;
 	}
 
 	/**
 	 * Lists all IP protocol versions supported for static IP addresses in this cloud.
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
 
 	@Override
 	public String[] mapServiceAction(ServiceAction action) {
 		return new String[0];
 	}
 
 
 	// Support for creating, deleting, listing firewall rules for a specific IP address
 
 
 	/**
 	 * When a cloud allows for programmatic requesting of new IP addresses, you may also programmatically
 	 * release them ({@link #isRequestable(AddressType)}). This method will release the specified IP
 	 * address from your pool and you will no longer be able to use it for assignment or forwarding.
 	 * @param addressId the unique ID of the address to be release
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws org.dasein.cloud.OperationNotSupportedException this cloud provider does not support address requests
 	 */
 	@Override
 	public void releaseFromPool(@Nonnull String addressId) throws InternalException, CloudException {
 		boolean publicIp = !addressId.contains("/");
 		if (publicIp && provider.getTerremarkProvider().isVcloudExpress()) {
 			String url = "/" + PUBLIC_IPS + "/" + addressId;
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.DELETE, url, null, "");
 			Document doc = method.invoke();
 			if (doc != null) {
 				String taskHref = Terremark.getTaskHref(doc, DELETE_PUBLIC_IP_OPERATION);
 				provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 			}
 		}
 		else if (publicIp && provider.getTerremarkProvider().isEnterpriseCloud()){
 			throw new OperationNotSupportedException("Not supported"); 
 		}
 		else if (!publicIp) {
 			String url = "/" + IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + addressId + "/" + Terremark.ACTION + "/unreserve";
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 			method.invoke();
 		}
 
 	}
 
 	/**
 	 * Releases an IP address assigned to a server so that it is unassigned in the address pool. 
 	 * You should call this method only when {@link #isAssigned(AddressType)} is <code>true</code>
 	 * for addresses of the target address's type.
 	 * @param addressId the address ID to release from its server assignment
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws org.dasein.cloud.OperationNotSupportedException this cloud provider does not support address assignment for addresses of the specified type
 	 */
 	@Override
 	public void releaseFromServer(@Nonnull String addressId) throws InternalException, CloudException {
 		throw new OperationNotSupportedException("Not supported");
 	}
 
 	/**
 	 * When requests for new IP addresses may be handled programmatically, this method allocates
 	 * a new IP address of the specified type. You should call it only if
 	 * {@link #isRequestable(AddressType)} is <code>true</code> for the address's type.
 	 * @param typeOfAddress the type of address being requested
 	 * @return the newly allocated IP address
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws org.dasein.cloud.OperationNotSupportedException this cloud provider does not support address requests
 	 * @deprecated use {@link #request(IPVersion)}
 	 */
 	@Override
 	public @Nonnull String request(@Nonnull AddressType typeOfAddress) throws InternalException, CloudException {
 		String ipAddressId = "";
 		if (typeOfAddress.equals(AddressType.PUBLIC)) {
 			String url = "/" + PUBLIC_IPS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId() + "/" + Terremark.ACTION + "/activatePublicIp";
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 			Document doc = method.invoke();
 			if (doc != null) {
 				NamedNodeMap attributes = doc.getElementsByTagName(PUBLIC_IP_TAG).item(0).getAttributes();
 				ipAddressId = Terremark.hrefToId(attributes.getNamedItem(Terremark.HREF).getNodeValue());
 			}
 		}
 		else {
 			IpAddress reservableIp = null;
 			Iterable<VLAN> networks = provider.getNetworkServices().getVlanSupport().listVlans();
 			for (VLAN network : networks) {
 				String networkId = network.getProviderVlanId();
 				if (!networkId.contains("ipv6")) {
 					logger.debug("Listing Reservable Ips");
 					Iterable<IpAddress> reservableIps = listPrivateIps(networkId, true, true, false);
 					if (reservableIps.iterator().hasNext()) {
 						reservableIp = reservableIps.iterator().next();
 						break;
 					}
 				}
 			}
 			if (reservableIp != null) {
 				String url = "/" + IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + reservableIp.getProviderIpAddressId() + "/" + Terremark.ACTION + "/reserve";
 				TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 				method.invoke();
 				ipAddressId = reservableIp.getProviderIpAddressId();
 			}
 		}
 		return ipAddressId;
 	}
 
 	/**
 	 * Requests an IP address of the specified version for the flat (non-VLAN) network space.
 	 * @param version the IP version of the address to be requested
 	 * @return the unique ID of the newly provisioned static IP address
 	 * @throws InternalException a local error occurred while preparing the request
 	 * @throws CloudException an error occurred with the cloud while provisioning the new address
 	 */
 	@Override
 	public String request(IPVersion version) throws InternalException, CloudException {
 		String ipAddressId = "";
 		if (version.equals(IPVersion.IPV4)) {
 			String url = "/" + PUBLIC_IPS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId() + "/" + Terremark.ACTION + "/activatePublicIp";
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 			Document doc = method.invoke();
 			if (doc != null) {
 				NamedNodeMap attributes = doc.getElementsByTagName(PUBLIC_IP_TAG).item(0).getAttributes();
 				ipAddressId = Terremark.hrefToId(attributes.getNamedItem(Terremark.HREF).getNodeValue());
 			}
 		}
 		else if (version.equals(IPVersion.IPV6)) {
 			String url = "/" + PUBLIC_IPS + "/" + EnvironmentsAndComputePools.ENVIRONMENTS + "/" + provider.getContext().getRegionId() + "/" + Terremark.ACTION + "/activatePublicIpIpV6";
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 			Document doc = method.invoke();
 			if (doc != null) {
 				NamedNodeMap attributes = doc.getElementsByTagName(PUBLIC_IP_TAG).item(0).getAttributes();
 				ipAddressId = Terremark.hrefToId(attributes.getNamedItem(Terremark.HREF).getNodeValue());
 			}
 		}
 		return ipAddressId;
 	}
 
 	/**
 	 * Requests a public IP address that may be used with a VLAN. This version may be used only when
 	 * {@link #identifyVlanForVlanIPRequirement()} is not {@link Requirement#REQUIRED}.
 	 * @param version the IP version of the address to be requested
 	 * @return the unique ID of a newly provisioned public IP address
 	 * @throws InternalException an error occurred locally while attempting to provision the IP address
 	 * @throws CloudException an error occurred in the cloud provider while provisioning the IP address
 	 * @throws OperationNotSupportedException either VLAN IPs are not supported or they must be explicitly associated with a VLAN
 	 */
 	@Override
 	public String requestForVLAN(IPVersion version) throws InternalException, CloudException {
 		throw new OperationNotSupportedException("This method is not supported. Use requestForVLAN(IPVersion version, String vlanId) instead.");
 	}
 
 	/**
 	 * Requests an IP address that must be used with a specific VLAN. This version may be used only when
 	 * {@link #identifyVlanForVlanIPRequirement()} is not {@link Requirement#NONE}.
 	 * @param version the IP version of the address to be requested
 	 * @param vlanId the unique ID of the VLAN to which the IP address will be assigned
 	 * @return the unique ID of a newly provisioned IP address
 	 * @throws InternalException an error occurred locally while attempting to provision the IP address
 	 * @throws CloudException an error occurred in the cloud provider while provisioning the IP address
 	 * @throws OperationNotSupportedException either VLAN IPs are not supported or they cannot be explicitly associated with a VLAN
 	 */
 	@Override
 	public String requestForVLAN(IPVersion version, String vlanId) throws InternalException, CloudException {
 		if (vlanId.contains("ipv6") && version.equals(IPVersion.IPV4)) {
 			throw new InternalException("The ip version of the vlan requested (IPv6) does not match the ip version requested (IPv4).");
 		}
 		else if (!vlanId.contains("ipv6") && version.equals(IPVersion.IPV6)) {
 			throw new InternalException("The ip version of the vlan requested (IPv4) does not match the ip version requested (IPv6).");
 		}
 		IpAddress availableIp = null;
 		Iterator<IpAddress> privateIps = provider.getNetworkServices().getIpAddressSupport().listPrivateIps(vlanId, true, false, false).iterator();		
 		String availableIpId = null;
 		boolean isReserved = false;
 		if (privateIps.hasNext()) {
 			availableIp = privateIps.next();
 			availableIpId = availableIp.getProviderIpAddressId();
 			isReserved = availableIp.isReserved();
 		}
 		else {
 			logger.warn("requestForVLAN(): Failed to find available ip");
 		}
 
 		logger.debug("requestForVLAN(): Found available ip " + availableIpId);
 		if (!isReserved) {
 			logger.debug("requestForVLAN(): Reserving the ip " + availableIpId);
 			reserveIp(availableIpId);
 		}
 
 		return availableIpId;
 	}
 
 	/**
 	 * Reserves a specified IPv4 address.
 	 * @param ipAddressId The ID of the IP address you want to reserve in the form network_id/ip_address or network_id/ipv6/ip_address.
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 */
 	private void reserveIp(String ipAddressId) throws CloudException, InternalException {
 		logger.debug("enter - reserveIp(" + ipAddressId + ")");
 		String url = "/" + IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + ipAddressId + "/action/reserve";
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 		method.invoke();
 	}
 
 	/**
 	 * Removes the specified forwarding rule from the address with which it is associated.
 	 * @param ruleId the rule to be removed
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws org.dasein.cloud.OperationNotSupportedException this cloud provider does not support address forwarding
 	 */
 	@Override
 	public void stopForward(@Nonnull String ruleId) throws InternalException, CloudException {
 		String[] ids = ruleId.split(":");
 		String internetServiceId = null;
 		String nodeServiceId = null;
 		if (ids.length == 2) {
 			internetServiceId = ids[0];
 			nodeServiceId = ids[1];
 		}
 		else {
 			throw new InternalException("Bad forwarding rule id: " + ruleId);
 		}
 		String url = "/" + NODE_SERVICES + "/" + nodeServiceId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.DELETE, url, null, "");
 		Document doc = method.invoke();
 		if (doc != null) {
 			String taskHref = Terremark.getTaskHref(doc, REMOVE_NODE_SERVICE_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 		}
 
 		Collection<NodeService> nodes = getNodeServicesOnInternetService(internetServiceId);
 		if (nodes.size() == 0) {
 			url = "/" + INTERNET_SERVICES + "/" + internetServiceId;
 			method = new TerremarkMethod(provider, HttpMethodName.DELETE, url, null, "");
 			Document isDoc = method.invoke();
 			if (isDoc != null) {
 				String taskHref = Terremark.getTaskHref(isDoc, REMOVE_INTERNET_SERVICE_OPERATION);
 				provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 			}
 		}
 	}
 
 	/**
 	 * Indicates whether or not IP addresses can be allocated for VLAN use. Only makes sense when the cloud
 	 * actually supports VLANS.
 	 * @param ofVersion the version of public IP address that might be routed to a VLAN resource
 	 * @return true if an IP address may be allocated for use by VLANs
 	 * @throws InternalException a local error occurred determining support
 	 * @throws CloudException an error occurred with the cloud provider in determining support
 	 */
 	@Override
 	public boolean supportsVLANAddresses(IPVersion ofVersion) throws InternalException, CloudException {
 		return true;
 	}
 
 	private InternetService toInternetService(Node internetServiceNode) {
 		InternetService service = new InternetService();
 		NamedNodeMap attrs = internetServiceNode.getAttributes();
 		service.setId(Terremark.hrefToId(attrs.getNamedItem(Terremark.HREF).getNodeValue()));
 		service.setName(attrs.getNamedItem(Terremark.NAME).getNodeValue());
 		NodeList isChildren = internetServiceNode.getChildNodes();
 		for (int i=0; i<isChildren.getLength(); i++) {
 			Node isChild = isChildren.item(i);
 			if (isChild.getNodeName().equals("Protocol")){
 				String protocol = isChild.getTextContent();
 				if (protocol.equals("UDP")){
 					service.setProtocol(Protocol.UDP);
 				}
 				else if (protocol.equals("TCP")){
 					service.setProtocol(Protocol.TCP);
 				}
 				else if (protocol.equals("IPSEC")){
 					service.setProtocol(Protocol.IPSEC);
 				}
 				else {
 					service.setProtocol(Protocol.ANY);
 				}
 			}
 			else if (isChild.getNodeName().equals("Port")){
 				service.setPort(Integer.parseInt(isChild.getTextContent()));
 			}
 			else if (isChild.getNodeName().equals("Enabled")) {
 				service.setEnabled(isChild.getTextContent().equalsIgnoreCase("true"));
 			}
 			else if (isChild.getNodeName().equals("Description")) {
 				service.setDescription(isChild.getTextContent());
 			}
 			else if (isChild.getNodeName().equals("PublicIp")) {
 				String id = Terremark.hrefToId(isChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 				service.setPublicIpId(id);
 			}
 			else if (isChild.getNodeName().equals("Persistence")) {
 				NodeList persistenceChildren = isChild.getChildNodes();
 				for (int j=0; j<persistenceChildren.getLength(); j++) {
 					Node persistenceChild = persistenceChildren.item(j);
 					if (persistenceChild.getNodeName().equals("Type")) {
 						service.setPersistenceType(persistenceChild.getTextContent());
 					}
 					else if (persistenceChild.getNodeName().equals("Timeout")) {
 						service.setPersistenceTimeout(persistenceChild.getTextContent());
 					}
 				}
 
 			}
 			else if (isChild.getNodeName().equals("RedirectUrl")) {
 				service.setRedirectURL(isChild.getTextContent());
 			}
 			else if (isChild.getNodeName().equals(TRUSTED_NETWORK_GROUP_TAG)) {
 				service.setTrustedNetworkGroupHref(isChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 				service.setTrustedNetworkGroupName(isChild.getAttributes().getNamedItem(Terremark.NAME).getNodeValue());
 			}
 			else if (isChild.getNodeName().equals(BACKUP_INTERNET_SERVICE_TAG)) {
 				service.setBackupInternetServiceHref(isChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 				service.setBackupInternetServiceName(isChild.getAttributes().getNamedItem(Terremark.NAME).getNodeValue());
 			}
 		}
 		return service;
 	}
 
 	private Collection<IpForwardingRule> toIpForwardingRule(Document publicIpDoc) throws CloudException, InternalException {
 		Collection<IpForwardingRule> rules = new ArrayList<IpForwardingRule>();
 
 		Node publicIpNode = publicIpDoc.getElementsByTagName(PUBLIC_IP_TAG).item(0);
 		String addressId = Terremark.hrefToId(publicIpNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 		NodeList internetServices = publicIpDoc.getElementsByTagName("InternetService");
 		for (int i=0; i < internetServices.getLength(); i++){
 			Node internetService = internetServices.item(i);
 			NodeList internetServiceChildren = internetService.getChildNodes();
 			int publicPort = -1;
 			for (int j=0; j < internetServiceChildren.getLength(); j++) {
 				Node isChild = internetServiceChildren.item(j);
 				if (isChild.getNodeName().equals("Port")){
 					publicPort = Integer.parseInt(isChild.getTextContent());
 				}
 				else if (isChild.getNodeName().equals("Enabled")){
 					if (isChild.getTextContent().equals("false")){
 						break;
 					}
 				}
 				else if (isChild.getNodeName().equals("NodeServices")){
 					NodeList nodeServiceNodes = isChild.getChildNodes();
 					for (int k=0; k < nodeServiceNodes.getLength(); k++) {
 						IpForwardingRule rule = new IpForwardingRule();
 						rule.setAddressId(addressId);
 						String internetServiceId = Terremark.hrefToId(internetService.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 						String nodeServiceId = Terremark.hrefToId(nodeServiceNodes.item(k).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 						rule.setProviderRuleId(internetServiceId + ":" + nodeServiceId);
 						rule.setPublicPort(publicPort);
 						NodeList nsChildren = nodeServiceNodes.item(k).getChildNodes();
 						for (int l=0; l < nsChildren.getLength(); l++) {
 							Node nsChild = nsChildren.item(l);
 							if (nsChild.getNodeName().equals("IpAddress")) {
 								String networkHostId = Terremark.hrefToId(nsChild.getLastChild().getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 								NetworkInterface nic = provider.getNetworkServices().getVlanSupport().getNetworkInterface(networkHostId);
 								rule.setServerId(nic.getProviderVirtualMachineId());
 							}
 							else if (nsChild.getNodeName().equals("Protocol")){
 								String protocol = nsChild.getTextContent();
 								if (protocol.equals("UDP")){
 									rule.setProtocol(Protocol.UDP);
 								}
 								else if (protocol.equals("TCP")){
 									rule.setProtocol(Protocol.TCP);
 								}
 								else if (protocol.equals("IPSEC")){
 									rule.setProtocol(Protocol.IPSEC);
 								}
 								else {
 									rule.setProtocol(Protocol.ANY);
 								}
 							}
 							else if (nsChild.getNodeName().equals("Port")){
 								rule.setPrivatePort(Integer.parseInt(nsChild.getTextContent()));
 							}
 							else if (nsChild.getNodeName().equals("Enabled")){
 								if (nsChild.getTextContent().equals("false")){
 									continue;
 								}
 							}
 						}
 						logger.debug("toIpForwardingRule(): Adding rule " + rule);
 						rules.add(rule);
 					}
 				}
 			}
 		}
 
 		return rules;
 	}
 
 	private NodeService toNodeService(Node nodeServiceNode, String internetServiceId) {
 		NodeService node = new NodeService();
 		String nodeId = Terremark.hrefToId(nodeServiceNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 		node.setId(nodeId);
 		String name = nodeServiceNode.getAttributes().getNamedItem(Terremark.NAME).getNodeValue();
 		node.setName(name);
 		node.setInternetServiceId(internetServiceId);
 		NodeList childNodes = nodeServiceNode.getChildNodes();
 		for (int i=0; i<childNodes.getLength(); i++) {
 			Node childNode = childNodes.item(i);
 			if (childNode.getNodeName().equals(IP_ADDRESS_TAG)) {
 				String href = childNode.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 				node.setPrivateIpAddressId(Terremark.hrefToNetworkId(href));
 			}
 			else if (childNode.getNodeName().equals("Port")) {
 				node.setPort(Integer.parseInt(childNode.getTextContent()));
 			}
 			else if (childNode.getNodeName().equals("Enabled")) {
 				node.setEnabled(childNode.getTextContent().equals("true"));
 			}
 			else if (childNode.getNodeName().equals("Description")) {
 				node.setDescription(childNode.getTextContent());
 			}
 		}
 		return node;
 	}
 
 	private IpAddress toPrivateIp(Node privateIpNode, boolean reservedOnly) throws CloudException, InternalException {
 		boolean reserved = privateIpNode.getLastChild().getTextContent().equalsIgnoreCase("true");
 
 		IpAddress privateIp = null;
 		if ((reservedOnly && reserved) || !reservedOnly) {
 			privateIp = new IpAddress();
 			privateIp.setForVlan(true);
 			privateIp.setReserved(reserved);
 			NamedNodeMap attributes = privateIpNode.getAttributes();
 			String type = attributes.getNamedItem(Terremark.TYPE).getNodeValue();
 			if (type.equals(IP_ADDRESS_TYPE)) {
 				privateIp.setVersion(IPVersion.IPV4);
 			}
 			else if (type.equals(IP_ADDRESS_IPV6_TYPE)) {
 				privateIp.setVersion(IPVersion.IPV6);
 			}
 			privateIp.setAddress(attributes.getNamedItem(Terremark.NAME).getNodeValue());
 			logger.debug("toPrivateIp(): " + privateIp + " is reserved: " + reserved);
 			privateIp.setAddressType(AddressType.PRIVATE);
 			String href = attributes.getNamedItem(Terremark.HREF).getNodeValue();
 			privateIp.setIpAddressId(Terremark.hrefToNetworkId(href));
 			// Format: network_id/ip_address or network_id/ipv6/ip_address
 			String networkId = privateIp.getProviderIpAddressId().split("/")[0];
 			privateIp.setProviderVlanId(networkId);
 			privateIp.setProviderLoadBalancerId(null);
 			privateIp.setRegionId(provider.getContext().getRegionId());
 			NodeList ipChildren = privateIpNode.getChildNodes();
 			String networkHostId = null;
 			for (int i=0; i<ipChildren.getLength(); i++) {
 				Node ipChild = ipChildren.item(i);
 				String ipChildName = ipChild.getNodeName();
 				if (ipChildName.equalsIgnoreCase(HOST_TAG) || ipChildName.equalsIgnoreCase(DETECTED_ON_TAG)) {
 					String hostHref = ipChild.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 					networkHostId = Terremark.hrefToId(hostHref);
 				}
 			}
 			if (networkHostId != null) {
 				NetworkInterface host = provider.getNetworkServices().getVlanSupport().getNetworkInterface(networkHostId);
 				privateIp.setServerId(host.getProviderVirtualMachineId());
 			}
 		}
 
 		return privateIp;
 	}
 	
 	private IpAddress toPublicIp(Node publicIpNode) {
 		IpAddress publicIp = new IpAddress();
 		NamedNodeMap attributes = publicIpNode.getAttributes();
 		String address = attributes.getNamedItem(Terremark.NAME).getNodeValue();
 		publicIp.setAddress(address);
 		if (address.contains(":")) {
 			publicIp.setVersion(IPVersion.IPV6);
 		}
 		else {
 			publicIp.setVersion(IPVersion.IPV4);
 		}
 		publicIp.setAddressType(AddressType.PUBLIC);
 		String href = attributes.getNamedItem(Terremark.HREF).getNodeValue();
 		publicIp.setIpAddressId(Terremark.hrefToId(href));
 		publicIp.setProviderLoadBalancerId(null);
 		publicIp.setRegionId(provider.getContext().getRegionId());
 		publicIp.setServerId(null);
 		publicIp.setForVlan(false);
 		return publicIp;
 	}
 
 	private ResourceStatus toPublicIpStatus(Node publicIpNode, IPVersion version) {
 		NamedNodeMap attributes = publicIpNode.getAttributes();
 		String address = attributes.getNamedItem(Terremark.NAME).getNodeValue();
 		String href = attributes.getNamedItem(Terremark.HREF).getNodeValue();
 		ResourceStatus status = null;
 		if (address.contains(":") && version.equals(IPVersion.IPV6)) {
 			status = new ResourceStatus(Terremark.hrefToId(href), true);
 		}
 		else if (!address.contains(":") && version.equals(IPVersion.IPV4)){
 			status = new ResourceStatus(Terremark.hrefToId(href), true);
 		}
 		return status;
 	}
 
     /**
 	 * Unreserves a specified IPv4 address.
 	 * @param ipAddressId The ID of the IP address you want to reserve in the form network_id/ip_address or network_id/ipv6/ip_address.
 	 * @throws CloudException an error occurred processing the request in the cloud
 	 * @throws InternalException an internal error occurred inside the Dasein Cloud implementation
 	 */
 	private void unreserveIp(String ipAddressId) throws CloudException, InternalException {
 		logger.debug("enter - reserveIp(" + ipAddressId + ")");
 		String url = "/" + IP_ADDRESSES + "/" + TerremarkNetworkSupport.NETWORKS + "/" + ipAddressId + "/action/unreserve";
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, "");
 		method.invoke();
 	}
 
 }
