 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.blacktie.administration;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.ejb.ActivationConfigProperty;
 import javax.ejb.MessageDriven;
 import javax.jms.Destination;
 import javax.jms.Queue;
 import javax.management.Attribute;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jboss.blacktie.administration.core.AdministrationProxy;
 import org.jboss.blacktie.jatmibroker.core.conf.ConfigurationException;
 import org.jboss.blacktie.jatmibroker.core.conf.XMLEnvHandler;
 import org.jboss.blacktie.jatmibroker.core.conf.XMLParser;
 import org.jboss.blacktie.jatmibroker.xatmi.Connection;
 import org.jboss.blacktie.jatmibroker.xatmi.ConnectionException;
 import org.jboss.blacktie.jatmibroker.xatmi.Response;
 import org.jboss.blacktie.jatmibroker.xatmi.TPSVCINFO;
 import org.jboss.blacktie.jatmibroker.xatmi.X_OCTET;
 import org.jboss.blacktie.jatmibroker.xatmi.mdb.MDBBlacktieService;
 import org.jboss.ejb3.annotation.Depends;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 
 @MessageDriven(activationConfig = {
 		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
 		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/BTStompAdmin") })
 @Depends("jboss.messaging.destination:service=Queue,name=BTStompAdmin")
 @javax.ejb.TransactionAttribute(javax.ejb.TransactionAttributeType.NOT_SUPPORTED)
 public class BlacktieStompAdministrationService extends MDBBlacktieService
 		implements javax.jms.MessageListener {
 	private static final Logger log = LogManager
 			.getLogger(BlacktieStompAdministrationService.class);
 
 	private MBeanServerConnection beanServerConnection;
 	private Properties prop = new Properties();
 
 	public static Hashtable<String, Long> QUEUE_CREATION_TIMES = new Hashtable<String, Long>();
 
 	public BlacktieStompAdministrationService() throws IOException,
 			ConfigurationException, ConnectionException {
 		super("BTStompAdmin");
 
 		XMLEnvHandler handler = new XMLEnvHandler(prop);
 		XMLParser xmlenv = new XMLParser(handler, "Environment.xsd");
 		xmlenv.parse("Environment.xml", true);
 		JMXServiceURL u = new JMXServiceURL((String) prop.get("JMXURL"));
 		JMXConnector c = JMXConnectorFactory.connect(u);
 		beanServerConnection = c.getMBeanServerConnection();
 	}
 
 	boolean isDeployQueue(ObjectName objName, String serviceName)
 			throws Exception {
 		HashSet dests = (HashSet) beanServerConnection.getAttribute(objName,
 				"Destinations");
 
 		Iterator<Destination> it = dests.iterator();
 		while (it.hasNext()) {
 			Destination dest = it.next();
 			if (dest instanceof Queue) {
 				String qname = ((Queue) dest).getQueueName();
 				log.debug("destination is " + qname);
 				if (qname.equals(serviceName)) {
 					log.trace("find serviceName " + serviceName);
 					return true;
 				}
 			}
 		}
 		log.trace("did not find serviceName " + serviceName);
 		return false;
 	}
 
 	int consumerCount(String serviceName) throws Exception {
 		ObjectName objName = new ObjectName(
 				"jboss.messaging.destination:service=Queue,name=" + serviceName);
 		Integer count = (Integer) beanServerConnection.getAttribute(objName,
 				"ConsumerCount");
 		Element security = (Element) beanServerConnection.getAttribute(objName,
 				"SecurityConfig");
 		log.debug(serviceName + " security config is " + printNode(security));
 		return count.intValue();
 	}
 
 	Element stringToElement(String s) throws Exception {
 		StringReader sreader = new StringReader(s);
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder parser = factory.newDocumentBuilder();
 		Document doc = parser.parse(new InputSource(sreader));
 		return doc.getDocumentElement();
 	}
 
 	String printNode(Node node) {
 		try {
 			// Set up the output transformer
 			TransformerFactory transfac = TransformerFactory.newInstance();
 			Transformer trans = transfac.newTransformer();
 			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
 			// Print the DOM node
 
 			StringWriter sw = new StringWriter();
 			StreamResult result = new StreamResult(sw);
 			DOMSource source = new DOMSource(node);
 			trans.transform(source, result);
 			String xmlString = sw.toString();
 
 			return xmlString;
 		} catch (TransformerException e) {
 			log.error(e);
 		}
 		return null;
 	}
 
 	void setSecurityConfig(ObjectName objName, String serviceName) {
 		log.debug("Get security configuration from xml");
 		String roleList = (String) prop.getProperty("blacktie." + serviceName
 				+ ".security");
 		if (roleList == null) {
 			log.debug("Will use servers default security if present");
 			String server = (String) prop.getProperty("blacktie." + serviceName
 					+ ".server");
 			if (server == null && serviceName.indexOf("_ADMIN_") > 0) {
 				server = serviceName.substring(0, serviceName
 						.indexOf("_ADMIN_"));
 				log.trace("Using server name of: " + server);
 			}
 			roleList = (String) prop.getProperty("blacktie." + server
 					+ ".security");
 			if (roleList == null) {
 				log.warn("No security set for service: " + serviceName);
 				roleList = "";
 			}
 		}
 
 		if (roleList.length() > 0) {
 			String security = "<security>\n";
 			String[] roles = roleList.split(",");
 			for (int i = 0; i < roles.length; i++) {
 				String[] details = roles[i].split(":");
 				security += "<role name=\"" + details[0] + "\" read=\""
 						+ details[1] + "\" write=\"" + details[2] + "\"/>\n";
 			}
 			security += "</security>";
 
 			log.trace("access security is " + security);
 			try {
 				Element element = stringToElement(security);
 				Attribute attr = new Attribute("SecurityConfig", element);
 				beanServerConnection.setAttribute(objName, attr);
 			} catch (Throwable t) {
 				log.error("Could not set security config " + t);
 			}
 		}
 	}
 
 	int deployQueue(String serviceName, String version) {
 		log.trace("deployQueue: " + serviceName + " version: " + version);
 		
 		if(version == null || !version.equals(prop.getProperty("blacktie.domain.version"))) {
 			log.warn("Blacktie Domain version " + 
 					  prop.getProperty("blacktie.domain.version") + 
 					  " not match server " + version);
 			return 4;
 		}
 		
 		int result = 0;
 		Long currentTime = QUEUE_CREATION_TIMES.get(serviceName);
 
 		try {
 			ObjectName objName = new ObjectName(
 					"jboss.messaging:service=ServerPeer");
 			boolean queue = false;
 
 			queue = isDeployQueue(objName, serviceName);
 			if (queue == false) {
 				synchronized (QUEUE_CREATION_TIMES) {
 					QUEUE_CREATION_TIMES.put(serviceName, System
 							.currentTimeMillis());
 					beanServerConnection.invoke(objName, "deployQueue",
 							new Object[] { serviceName, null }, new String[] {
 									"java.lang.String", "java.lang.String" });
 					ObjectName queueName = new ObjectName(
 							"jboss.messaging.destination:service=Queue,name="
 									+ serviceName);
 					setSecurityConfig(queueName, serviceName);
 				}
 			}
 
 			if (queue == false || !serviceName.contains("_ADMIN_")) {
 				result = 1;
 				if (AdministrationProxy.isDomainPause
 						&& serviceName.contains("_ADMIN_")) {
 					log.info("Domain is pause");
 					result = 3;
 				}
 			} else if (consumerCount(serviceName) > 0) {
 				log.warn("can not advertise ADMIN with same id");
 				result = 2;
 			} else if (AdministrationProxy.isDomainPause) {
 				log.info("Domain is pause");
 				result = 3;
 			} else {
 				result = 1;
 			}
 		} catch (Throwable t) {
 			log.error("Could not deploy queue of " + serviceName, t);
 			QUEUE_CREATION_TIMES.put(serviceName, currentTime);
 		}
 
 		return result;
 	}
 
 	int undeployQueue(String serviceName) {
 		int result = 0;
 
 		try {
 			ObjectName objName = new ObjectName(
 					"jboss.messaging:service=ServerPeer");
 			if (isDeployQueue(objName, serviceName)) {
 				beanServerConnection.invoke(objName, "undeployQueue",
 						new Object[] { serviceName },
 						new String[] { "java.lang.String" });
 			}
 			result = 1;
 		} catch (Throwable t) {
 			log.error("Could not undeploy queue of " + serviceName, t);
 		}
 
 		return result;
 	}
 
 	int decrementConsumer(String serviceName) {
 		int consumerCounts;
 		int result = 0;
 
 		try {
 			consumerCounts = consumerCount(serviceName);
 			log.debug("Service " + serviceName + " has " + consumerCounts
 					+ " consumers");
 			if (consumerCounts == 1) {
 				result = undeployQueue(serviceName);
 			} else {
 				// THERE ARE OTHER SERVERS STILL ALIVE
 				result = 1;
 			}
 		} catch (Throwable t) {
 			log.error("Could not get consumer counts of " + serviceName, t);
 		}
 		return result;
 	}
 
 	public Response tpservice(TPSVCINFO svcinfo) {
 		log.debug("Message received");
 		X_OCTET recv = (X_OCTET) svcinfo.getBuffer();
 		String string = new String(recv.getByteArray());
 		StringTokenizer st = new StringTokenizer(string, ",", false);
 		String operation = st.nextToken();
 		String serverName = st.nextToken();
 		String serviceName = st.nextToken();
		
 		byte[] success = new byte[1];
 		String server = null;
 		int k = -1;
 
 		try {
 			Properties prop = new Properties();
 			XMLEnvHandler handler = new XMLEnvHandler(prop);
 			XMLParser xmlenv = new XMLParser(handler, "Environment.xsd");
 			xmlenv.parse("Environment.xml", true);
 
 			if ((k = serviceName.indexOf("_ADMIN_")) > 0) {
 				server = serviceName.substring(0, k);
 				List<String> servers = (List<String>) prop
 						.get("blacktie.domain.servers");
 				if (servers.contains(server) == false) {
 					log.warn("Could not find the server to advertise for: "
 							+ server);
 					server = null;
 				} else {
 					log.trace("Located server: " + server);
 				}
 			} else {
 				server = (String) prop.get("blacktie." + serviceName
 						+ ".server");
 			}
 
 			if (server != null && server.equals(serverName)) {
 				log.trace("Service " + serviceName + " exists for server: "
 						+ server);
 				if (operation.equals("tpunadvertise")) {
 					log.trace("Unadvertising: " + serviceName);
 					success[0] = (byte) undeployQueue(serviceName);
 				} else if (operation.equals("tpadvertise")) {
 					log.trace("Advertising: " + serviceName);
					String version = st.nextToken();
 					success[0] = (byte) deployQueue(serviceName, version);
 				} else if (operation.equals("decrementconsumer")) {
 					log.trace("Decrement consumer: " + serviceName);
 					success[0] = (byte) decrementConsumer(serviceName);
 				} else {
 					log.error("Unknow operation " + operation);
 					success[0] = 0;
 				}
 			} else {
 				log.error("Service " + serviceName
 						+ " cannot be located for server");
 				success[0] = 0;
 			}
 
 			X_OCTET buffer = (X_OCTET) svcinfo.tpalloc("X_OCTET", null);
 			buffer.setByteArray(success);
 			log.debug("Responding");
 			return new Response(Connection.TPSUCCESS, 0, buffer, 1, 0);
 		} catch (ConnectionException e) {
 			return new Response(Connection.TPFAIL, 0, null, 0, 0);
 		} catch (ConfigurationException e) {
 			return new Response(Connection.TPFAIL, 0, null, 0, 0);
 		}
 	}
 }
