 package com.robonobo.core.service;
 
 import java.net.*;
 import java.util.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.*;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 import org.bitlet.weupnp.*;
 
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.common.util.CodeUtil;
 import com.robonobo.common.util.NetUtil;
 import com.robonobo.core.api.config.RobonoboConfig;
 import com.robonobo.mina.external.MinaConfig;
 
 public class GatewayService extends AbstractService {
 	private static final int PORT_MAPPING_ATTEMPTS = 3;
 	private InetSocketAddress publicDetails;
 	private GatewayDevice gateway;
 	Log log = LogFactory.getLog(getClass());
 
 	public GatewayService() {
 		addHardDependency("core.http");
 	}
 
 	public String getName() {
 		return "Gateway Service";
 	}
 
 	public void startup() throws Exception {
 		RobonoboConfig roboCfg = rbnb.getConfig();
 		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
 		InetAddress localAddr = chooseLocalIP();
 		String cfgMode = roboCfg.getGatewayCfgMode();
 		if (cfgMode.equals("off")) {
 			minaCfg.setGatewayAddress(null);
 			roboCfg.setGatewayCfgResult("Off");
 		} else if (cfgMode.equals("auto")) {
 			if (localAddr.isSiteLocalAddress())
 				configureUPnP();
 			else {
 				minaCfg.setGatewayAddress(null);
 				roboCfg.setGatewayCfgResult("OK :)");
 				log.info("Local IP address is public, assuming gateway not present");
 			}
 		} else {
 			roboCfg.setGatewayCfgResult("");
 			configureGatewayFromConfig(Integer.parseInt(cfgMode));
 		}
 		getRobonobo().saveConfig();
 	}
 
 	private InetAddress chooseLocalIP() {
 		// Check that the local-address is one of the addresses on this host
 		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
 		InetAddress myAddr = null;
 		String configAddrStr = minaCfg.getLocalAddress();
 		Set<InetAddress> localAddrs = NetUtil.getLocalInetAddresses(false);
 		if (configAddrStr != null) {
 			try {
 				InetAddress configAddr = InetAddress.getByName(configAddrStr);
 				if (!configAddr.isLoopbackAddress() && localAddrs.contains(configAddr))
 					myAddr = configAddr;
 				else {
 					log.info("Local address in config (" + configAddrStr + ") not valid for this host, picking new one");
 					// If the user manually picked an ip address, it's no longer valid
 					getRobonobo().getConfig().setUserSpecifiedLocalAddr(false);
 				}
 			} catch (UnknownHostException ignore) {
 			}
 		}
 		if (myAddr == null) {
 			// Select an address from our local ones
 			myAddr = NetUtil.getFirstPublicInet4Address();
 			// TODO IPv6
 			if (myAddr == null)
 				myAddr = NetUtil.getFirstNonLoopbackInet4Address();
 			if (myAddr == null) {
 				try {
 					// TODO - Poll and restart if an interface appears
 					myAddr = InetAddress.getByName("127.0.0.1");
 				} catch (UnknownHostException e) {
 					throw new SeekInnerCalmException();
 				}
 			}
 		}
 		minaCfg.setLocalAddress(myAddr.getHostAddress());
 		log.info("Configured local address: " + myAddr.toString());
 		return myAddr;
 	}
 
 	private void configureUPnP() {
 		RobonoboConfig roboCfg = rbnb.getConfig();
 		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
 		roboCfg.setGatewayCfgResult("Checking...");
 		minaCfg.setGatewayAddress(null);
 		// Get our list of available gateways
 		GatewayDiscover gd = new GatewayDiscover();
 		Map<InetAddress, GatewayDevice> devices;
 		try {
 			devices = gd.discover();
 		} catch (Exception e) {
 			log.error("Caught " + CodeUtil.shortClassName(e.getClass()) + " discovering gateways");
 			roboCfg.setGatewayCfgResult("Failed :(");
 			return;
 		}
 		if (devices.size() == 0) {
 			log.info("No gateways available :(");
 			roboCfg.setGatewayCfgResult("Failed :(");
 			return;
 		}
 		// Figure out any discrepancies between the available gateways and our configured local ip address
 		InetAddress localAddr;
 		try {
 			localAddr = InetAddress.getByName(minaCfg.getLocalAddress());
 		} catch (UnknownHostException e) {
 			// You arsed up the config, you cretin
 			log.error("Invalid local ip address " + minaCfg.getLocalAddress());
 			roboCfg.setGatewayCfgResult("Failed :(");
 			return;
 		}
 		if (devices.containsKey(localAddr)) {
 			log.info("Our configured ip address " + localAddr.getHostAddress() + " has a gateway on it, huzzah");
 			gateway = devices.get(localAddr);
 		} else {
 			// If the user specified the ip address themselves, respect their choice
 			if (roboCfg.getUserSpecifiedLocalAddr()) {
 				log.info("User specified address " + localAddr.getHostAddress() + ", respecting their choice and not using any of the " + devices.size() + " gateway(s) available");
 				roboCfg.setGatewayCfgResult("Overruled!");
 				return;
 			} else {
 				// Change our configured local ip to one with a gateway
 				localAddr = devices.keySet().iterator().next();
 				minaCfg.setLocalAddress(localAddr.getHostAddress());
 				log.info("Changing my local ip to " + localAddr.getHostAddress() + " as that has a gateway on it");
 				gateway = devices.get(localAddr);
 			}
 		}
 		// Figure out our gateway's external ip address
 		InetAddress gatewayIp;
 		try {
 			gatewayIp = InetAddress.getByName(gateway.getExternalIPAddress());
 		} catch (Exception e) {
 			gatewayIp = null;
 		}
 		if (gatewayIp == null) {
 			// Oops - this probably means our port mapping is going to fail, but just in case this is a screwy router
 			// (and there are many), look up the IP from sonar
 			try {
 				gatewayIp = getPublicIpFromSonar();
 			} catch (Exception e) {
 				log.error("Caught exception asking sonar for my public ip", e);
 				log.error("Not configuring port forwarding - cannot find my public ip address");
 				roboCfg.setGatewayCfgResult("Failed :(");
 				return;
 			}
 		}
 		// Get the existing port mappings on the gateway to make sure we don't conflict - don't draw tcp/udp distinction
 		// here
 		Set<Integer> usedPorts = new HashSet<Integer>();
 		for (int i = 0;; i++) {
 			PortMappingEntry entry = new PortMappingEntry();
 			try {
 				if (!gateway.getGenericPortMappingEntry(i, entry))
 					break;
 			} catch (Exception e) {
 				// Oops - this probably means our port mapping will fail, but carry on in case of screwy router
 				break;
 			}
			// Is this a rbnb mapping from a previous crashed/kill9d run?
			if (entry.getInternalClient().equals(localAddr.getHostAddress()) && entry.getInternalPort() == minaCfg.getListenUdpPort()
					&& entry.getProtocol().equalsIgnoreCase("udp")) {
				publicDetails = new InetSocketAddress(gatewayIp, entry.getExternalPort());
				minaCfg.setGatewayAddress(publicDetails.getAddress().getHostAddress());
				minaCfg.setGatewayUdpPort(entry.getExternalPort());
				log.info("Using existing UPnP port mapping with details " + publicDetails);
				roboCfg.setGatewayCfgResult("OK :)");
				return;
			}
 			int externalPort = entry.getExternalPort();
 			usedPorts.add(externalPort);
 			log.debug("Found existing port mapping: " + entry.getProtocol() + " port " + externalPort + " to " + entry.getInternalClient() + ":" + entry.getInternalPort()
 					+ " for service '" + entry.getPortMappingDescription() + "'");
 		}
 		String myName = "robonobo v" + rbnb.getVersion();
 		Random rand = new Random();
 		// Try a couple of times just for completeness' sake (might be someone else configuring ports)
 		int tried = 0;
 		do {
 			try {
 				int port;
 				do {
 					port = 1025 + rand.nextInt(65535 - 1025);
 				} while (usedPorts.contains(port));
 				if (gateway.getSpecificPortMappingEntry(port, "UDP", new PortMappingEntry()))
 					log.info("Port mapping already exists for port " + port);
 				else {
 					if (gateway.addPortMapping(port, minaCfg.getListenUdpPort(), localAddr.getHostAddress(), "UDP", myName)) {
 						publicDetails = new InetSocketAddress(gatewayIp, port);
 						minaCfg.setGatewayAddress(publicDetails.getAddress().getHostAddress());
 						minaCfg.setGatewayUdpPort(port);
 						log.info("Successfully configured UPnP, using external details " + publicDetails);
 						roboCfg.setGatewayCfgResult("OK :)");
 						return;
 					} else
 						log.info("Port mapping failure on port " + port);
 				}
 			} catch (Exception e) {
 				log.error("Caught " + CodeUtil.shortClassName(e.getClass()) + " attempting to create port mapping");
 				// Keep trying, just in case...
 			}
 		} while (++tried < PORT_MAPPING_ATTEMPTS);
 		log.info("Failed to create UPnP port mapping");
 		roboCfg.setGatewayCfgResult("Failed :(");
 	}
 
 	private void configureGatewayFromConfig(int publicPort) {
 		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
 		// Port is specified manually
 		minaCfg.setGatewayUdpPort(publicPort);
 		// If lookupGatewayIP is false, we just use whatever IP is in minaconfig.gatewayAddress
 		if (getRobonobo().getConfig().getLookupGatewayIP()) {
 			try {
 				minaCfg.setGatewayAddress(getPublicIpFromSonar().getHostAddress());
 			} catch (Exception e) {
 				log.error("Caught exception asking sonar for my ip", e);
 				minaCfg.setGatewayAddress(null);
 			}
 		}
 	}
 
 	/** Figure out our public ip by pinging sonar */
 	private InetAddress getPublicIpFromSonar() throws Exception {
 		String ipDetectUrl = rbnb.getConfig().getSonarUrl() + "ipdetect";
 		HttpGet get = new HttpGet(ipDetectUrl);
 		HttpClient client = rbnb.getHttpService().getClient();
 		HttpEntity body = null;
 		HttpResponse resp = client.execute(get);
 		try {
 			body = resp.getEntity();
 			int status = resp.getStatusLine().getStatusCode();
 			if (status != HttpStatus.SC_OK)
 				return null;
 			String ipStr = EntityUtils.toString(body);
 			return InetAddress.getByName(ipStr);
 		} finally {
 			if (body != null)
 				EntityUtils.consume(body);
 		}
 	}
 
 	public void shutdown() {
 		if (gateway != null && publicDetails != null) {
 			try {
 				gateway.deletePortMapping(publicDetails.getPort(), "UDP");
 				log.info("Deleted gateway port mapping");
 			} catch (Exception e) {
 				log.error("Caught " + CodeUtil.shortClassName(e.getClass()) + " deleting gateway port mapping");
 			}
 		}
 	}
 
 	public String getProvides() {
 		return "core.gateway";
 	}
 
 	public InetSocketAddress getPublicDetails() {
 		return publicDetails;
 	}
 }
