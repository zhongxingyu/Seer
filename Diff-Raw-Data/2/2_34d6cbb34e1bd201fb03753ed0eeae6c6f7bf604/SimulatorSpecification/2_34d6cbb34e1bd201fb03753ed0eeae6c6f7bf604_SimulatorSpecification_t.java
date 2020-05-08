 package netproj.controller;
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import netproj.hosts.Pinger;
 import netproj.hosts.ProbFlowSender;
 import netproj.hosts.SimpleHost;
 import netproj.hosts.UDPSender;
 import netproj.hosts.tcp.FastTcpHost;
 import netproj.hosts.tcp.TcpTahoeHost;
 
 import netproj.routers.BFRouter;
 import netproj.routers.Router;
 import netproj.skeleton.Device;
 import netproj.skeleton.Link;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class SimulatorSpecification {
 	public List<Device> devices;
 	public List<Router> routers;
 	public List<Link> links;
 	private NetworkWatcher bw;
 	
 	public SimulatorSpecification(List<Device> devices, List<Router> routers,
 			List<Link> links, NetworkWatcher bw) {
 		super();
 		this.devices = devices;
 		this.routers = routers;
 		this.links = links;
 		this.bw = bw;
 	}
 	
 	public SimulatorSpecification() {
 		devices = new LinkedList<Device>();
 		routers = new LinkedList<Router>();
 		links = new LinkedList<Link>();
 		this.bw = new NetworkWatcher(devices, routers, links);
 	}
 	
 	public List<Device> getDevices() {
 		return devices;
 	}
 	
 	public Device getDevice(int address) {
 		for (Device d : devices) {
 			if (d.getAddress() == address) {
 				return d;
 			}
 		}
 		for (Router r : routers) {
 			if (r.getAddress() == address) {
 				return r;
 			}
 		}
 		return null;
 	}
 	
 	public void setDevices(List<Device> devices) {
 		this.devices = devices;
 	}
 	
 	public List<Router> getRouters() {
 		return routers;
 	}
 	
 	public void setRouters(List<Router> routers) {
 		this.routers = routers;
 	}
 	
 	public List<Link> getLinks() {
 		return links;
 	}
 	
 	public void setLinks(List<Link> links) {
 		this.links = links;
 	}
 	
 	public void addFastTcp(int i_buf, int o_buf, int ip) {
 		Device d = new FastTcpHost(i_buf, o_buf, ip);
 		d.start();
 		devices.add(d);
 	}
 	
 	public void addTcpTahoe(int i_buf, int o_buf, int ip) {
 		Device d = new TcpTahoeHost(i_buf, o_buf, ip);
 		d.start();
 		devices.add(d);
 	}
 	
 	
 	public void start() {
 		for(Device d : devices){
 			d.start();
 		}
 		for(Device d : routers) {
 			d.start();
 		}
 		for(Link l : links) {
 			l.start();
 		}
 		bw.start();
 	}
 	
 	/*
 	 * loadXML loads in a simulator spec based on an xml file of the form
 	 * <simulator>
 	 * <router inputBuffSize="1024000" outputBuffSize="1024000" address="0x100" />
 	 * <device inputBuffSize="1024000" outputBuffSize="1024000" address="0x106" />
 	 * <device inputBuffSize="1024000" outputBuffSize="1024000" address="0x105" />
 	 * <router inputBuffSize="1024000" outputBuffSize="1024000" address="0x200" />
 	 * <host   inputBuffSize="1024000" outputBuffSize="1024000" address="0x201" />
 	 * <link bps="1024000" delayms="10">
 	 *      <connect address="foo" />
 	 *      <connect address="bar" />
 	 * </link>
 	 * </simulator>
 	 */
 	public static SimulatorSpecification loadXML(String fileName) {
 		try {
 			Logger log = Logger.getLogger("loadXML");
 			log.setParent(Logger.getLogger("netproj"));
 			log.setLevel(null);
 
 			SimulatorSpecification simulator = new SimulatorSpecification();
 			HashMap<Integer, Device> ipTodevice = new HashMap<Integer, Device>();
 			
 			File file = new File(fileName);
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document doc = db.parse(file);
 			doc.getDocumentElement().normalize();
 			if(doc.getDocumentElement().getNodeName() != "simulator") {
 				// invalid xml file
 				throw new RuntimeException();
 			}
 			NodeList routerList = doc.getElementsByTagName("router");
 			// enumerate routers
 			for (int s = 0; s < routerList.getLength(); s++) {
 				Node router = routerList.item(s);
 				if (router.getNodeType() == Node.ELEMENT_NODE) {
 					Element routerElement = (Element) router;
 					int inputBuffSize = 
 						Integer.parseInt(routerElement.getAttribute("inputBuffSize"));
 					int address = Integer.decode(routerElement.getAttribute("address"));
 					int outputBuffSize = 
 						Integer.parseInt(routerElement.getAttribute("outputBuffSize"));
 					Router r = new Router(inputBuffSize, outputBuffSize, address);
 					ipTodevice.put(address, r);
 					simulator.routers.add(r);
 					log.fine("importing router " + Integer.toHexString(address));
 				}
 			}
 			NodeList bfrouterList = doc.getElementsByTagName("bfrouter");
 			// enumerate routers
 			for (int s = 0; s < bfrouterList.getLength(); s++) {
 				Node router = bfrouterList.item(s);
 				if (router.getNodeType() == Node.ELEMENT_NODE) {
 					Element routerElement = (Element) router;
 					int inputBuffSize = 
 						Integer.parseInt(routerElement.getAttribute("inputBuffSize"));
 					int address = Integer.decode(routerElement.getAttribute("address"));
 					int outputBuffSize = 
 						Integer.parseInt(routerElement.getAttribute("outputBuffSize"));
 					Router r = new BFRouter(inputBuffSize, outputBuffSize, address);
 					ipTodevice.put(address, r);
 					simulator.routers.add(r);
 					log.fine("importing router " + Integer.toHexString(address));
 				}
 			}
 			
 			NodeList pingList = doc.getElementsByTagName("pinger");
 			for (int d = 0; d < pingList.getLength(); d++) {
 				Node host = pingList.item(d);
 				if (host.getNodeType() == Node.ELEMENT_NODE) {
 					Element hostElement = (Element) host;
 					int address = Integer.decode(hostElement.getAttribute("address"));
 					// TODO: output buffer size
 					Device h = new Pinger(1000, 1000, address); 
 					ipTodevice.put(address, h);					
 					log.fine("importing host " + Integer.toHexString(address));
 					simulator.devices.add(h);
 				}
 			}
 			
 			NodeList fastList = doc.getElementsByTagName("FastTCP");
 			for (int d = 0; d < fastList.getLength(); d++) {
 				Node host = fastList.item(d);
 				if (host.getNodeType() == Node.ELEMENT_NODE) {
 					Element hostElement = (Element) host;
 					int address = Integer.decode(hostElement.getAttribute("address"));
 					int inpBuff = Integer.decode(hostElement.getAttribute("inputBuffSize"));
 					int outBuff = Integer.decode(hostElement.getAttribute("outputBuffSize"));
 					// TODO: output buffer size
 					Device h = new FastTcpHost(inpBuff, outBuff, address); 
 					ipTodevice.put(address, h);					
 					log.fine("importing fastTcp " + Integer.toHexString(address));
 					simulator.devices.add(h);
 				}
 			}
 			
 			NodeList udpList = doc.getElementsByTagName("UDPSender");
 			for (int d = 0; d < udpList.getLength(); d++) {
 				Node host = udpList.item(d);
 				if (host.getNodeType() == Node.ELEMENT_NODE) {
 					Element hostElement = (Element) host;
 					int address = Integer.decode(hostElement.getAttribute("address"));
 					// TODO: output buffer size
 					Device h = new UDPSender(1000, 1000, address, 1000); 
 					ipTodevice.put(address, h);					
 					log.fine("importing udpsender " + Integer.toHexString(address));
 					simulator.devices.add(h);
 				}
 			}
 			NodeList tcpList = doc.getElementsByTagName("TCPTahoe");
 			for (int d = 0; d < tcpList.getLength(); d++) {
 				Node host = tcpList.item(d);
 				if (host.getNodeType() == Node.ELEMENT_NODE) {
 					Element hostElement = (Element) host;
 					int address = Integer.decode(hostElement.getAttribute("address"));
 					int inpBuff = Integer.decode(hostElement.getAttribute("inputBuffSize"));
 					int outBuff = Integer.decode(hostElement.getAttribute("outputBuffSize"));
 					Device h = new TcpTahoeHost(inpBuff, outBuff, address); 
 					ipTodevice.put(address, h);					
 					log.fine("importing tcptahoe " + Integer.toHexString(address));
 					simulator.devices.add(h);
 				}
 			}			
 			NodeList probList = doc.getElementsByTagName("ProbFlowSender");
 			for (int d = 0; d < probList.getLength(); d++) {
 				Node host = probList.item(d);
 				if (host.getNodeType() == Node.ELEMENT_NODE) {
 					Element hostElement = (Element) host;
 					int address = Integer.decode(hostElement.getAttribute("address"));
 					int inpBuff = Integer.decode(hostElement.getAttribute("inputBuffSize"));
 					int outBuff = Integer.decode(hostElement.getAttribute("outputBuffSize"));
 					Device h = new ProbFlowSender(inpBuff, outBuff, address); 
 					ipTodevice.put(address, h);					
					log.fine("importing probflowsender " + Integer.toHexString(address));
 					simulator.devices.add(h);
 				}
 			}			
 			NodeList hostList = doc.getElementsByTagName("device");
 			for (int d = 0; d < hostList.getLength(); d++) {
 				Node host = hostList.item(d);
 				if (host.getNodeType() == Node.ELEMENT_NODE) {
 					Element hostElement = (Element) host;
 					int address = Integer.decode(hostElement.getAttribute("address"));
 					// TODO: output buffer size
 					Device h = new SimpleHost(address, 1000000); 
 					ipTodevice.put(address, h);					
 					log.fine("importing host " + Integer.toHexString(address));
 					simulator.devices.add(h);
 				}
 			}
 			NodeList linkList = doc.getElementsByTagName("link");
 			for (int l = 0; l < linkList.getLength(); l++) {
 				Node linkNode = linkList.item(l);
 				if (linkNode.getNodeType() == Node.ELEMENT_NODE) {
 					Element linkElement = (Element) linkNode;
 					int bps = Integer.parseInt(linkElement.getAttribute("bps"));
 					int delayms = Integer.parseInt(linkElement.getAttribute("delayms"));
 
 					NodeList connects = linkElement.getElementsByTagName("connect");
 					LinkedList<Device> devices = new LinkedList<Device>();
 					for(int c = 0; c < connects.getLength(); c++) {
 						Node connect = connects.item(c);
 						if (connect.getNodeType() == Node.ELEMENT_NODE) {
 							Element connectElement = (Element) connect;
 							int address = Integer.decode(connectElement.getAttribute("address"));
 							devices.add(ipTodevice.get(address));
 						}
 					}
 					Link link = new Link(bps, delayms, devices);
 					for (Device d: devices) {
 						d.addLink(link);
 					}
 					simulator.links.add(link);
 				}
 			}
 			NodeList hostlinkList = doc.getElementsByTagName("hostlink");
 			for (int l = 0; l < hostlinkList.getLength(); l++) {
 				Node linkNode = hostlinkList.item(l);
 				if (linkNode.getNodeType() == Node. ELEMENT_NODE) {
 					Element linkElement = (Element) linkNode;
 					int bps = Integer.parseInt(linkElement.getAttribute("bps"));
 					int delayms = Integer.parseInt(linkElement.getAttribute("delayms"));
 					int host = Integer.decode(linkElement.getAttribute("host"));
 					int router = Integer.decode(linkElement.getAttribute("router"));
 
 					LinkedList<Device> devices = new LinkedList<Device>();
 					
 					devices.add(ipTodevice.get(host));
 					devices.add(ipTodevice.get(router));
 					
 					Link link = new Link(bps, delayms, devices);
 					Router r = (Router)ipTodevice.get(router);
 					r.addRoutingTableEntry(host, 32, r.addLink(link));
 					ipTodevice.get(host).addLink(link);
 					
 					simulator.links.add(link);
 				}
 			}
 			return simulator;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
