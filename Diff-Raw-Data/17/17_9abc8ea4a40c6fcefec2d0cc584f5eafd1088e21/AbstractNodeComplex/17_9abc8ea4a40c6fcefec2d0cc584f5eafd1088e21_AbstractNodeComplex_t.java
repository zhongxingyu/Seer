 /**
  * @(#)AbstractNodeComplex.java
  */
 
 package aurora;
 
 import java.io.*;
 import java.util.*;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.*;
 
 
 /**
  * Base class for Network Nodes.
  * @author Alex Kurzhanskiy
  * @version $Id: AbstractNodeComplex.java 135 2010-06-05 00:57:31Z akurzhan $
  */
 public abstract class AbstractNodeComplex extends AbstractNode {
 	private static final long serialVersionUID = -6880500889959625206L;
 	
 	protected double tp = 1.0/3600;  // sampling time
 	//protected Vector<Double> timeH = new Vector<Double>();
 	protected int simNo = 0; // simulation number
 	protected int maxTimeStep = 100000; // maximum simulation step
 	protected boolean top = false;
 	protected boolean controlled = true;
 	protected DataStorage database = null;
 	protected boolean verbose = false;
 	
 	protected int tsV = 0;
 
 	protected Vector<AbstractSensor> sensors = new Vector<AbstractSensor>();
 	protected Vector<AbstractMonitor> monitors = new Vector<AbstractMonitor>();
 	protected Vector<AbstractNodeComplex> networks = new Vector<AbstractNodeComplex>();
 	protected Vector<AbstractNodeSimple> nodes = new Vector<AbstractNodeSimple>();
 	protected Vector<AbstractLink> links = new Vector<AbstractLink>();
 	protected Vector<OD> odList = new Vector<OD>();
 	protected Vector<ErrorConfiguration> cfgErrors = new Vector<ErrorConfiguration>();
 	
 	protected int nodesToSave = 0;
 	protected int linksToSave = 0;
 	
 	protected AbstractContainer container = null;
 	
 	protected Vector<AbstractControllerComplex> controllers = new Vector<AbstractControllerComplex>();
 	
 	private Vector<Node> domnodes = new Vector<Node>();
 	
 	/**
 	 * Initialize Network list from the DOM structure.
 	 */
 	public boolean initNetworkListFromDOM(Node p) throws Exception {
 		boolean res = true;
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp2 = p.getChildNodes();
 			for (int j = 0; j < pp2.getLength(); j++) {
 				if (pp2.item(j).getNodeName().equals("network")) {
 					Class c = Class.forName(container.neType2Classname("N"));
 					AbstractNodeComplex net = (AbstractNodeComplex)c.newInstance();
 					net.setMyNetwork(this);
 					res &= net.initFromDOM(pp2.item(j));
 					addNetwork(net);
 					domnodes.add(pp2.item(j));
 				}
 				if (pp2.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					if (doc.hasChildNodes())
 						res &= initNetworkListFromDOM(doc.getChildNodes().item(0));
 				}
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Initialize Node list from the DOM structure.
 	 */
 	public boolean initNodeListFromDOM(Node p) throws Exception {
 		boolean res = true;
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp2 = p.getChildNodes();
 			for (int j = 0; j < pp2.getLength(); j++) {
 				if (pp2.item(j).getNodeName().equals("network")) {
 					Class c = Class.forName(container.neType2Classname("N"));
 					AbstractNodeComplex net = (AbstractNodeComplex)c.newInstance();
 					net.setMyNetwork(this);
 					res &= net.initFromDOM(pp2.item(j));
 					addNetwork(net);
 					domnodes.add(pp2.item(j));
 				}
 				if (pp2.item(j).getNodeName().equals("node")) {
 					Node type_attr = pp2.item(j).getAttributes().getNamedItem("type");
 					String class_name = null;
 					if (type_attr != null)
 						class_name = container.neType2Classname(type_attr.getNodeValue());
 					else
 						class_name = pp2.item(j).getAttributes().getNamedItem("class").getNodeValue();
 					Class c = Class.forName(class_name);
 					AbstractNodeSimple nd = (AbstractNodeSimple)c.newInstance();
 					nd.setMyNetwork(this);
 					res &= nd.initFromDOM(pp2.item(j));
 					addNode(nd);
					if (nd.getType() != AbstractTypes.NODE_TERMINAL)
						domnodes.add(pp2.item(j));
 				}
 				if (pp2.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					if (doc.hasChildNodes())
 						res &= initNodeListFromDOM(doc.getChildNodes().item(0));
 				}
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Initialize Link list from the DOM structure.
 	 */
 	private boolean initLinkListFromDOM(Node p) throws Exception {
 		boolean res = true;
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp2 = p.getChildNodes();
 			for (int j = 0; j < pp2.getLength(); j++) {
 				if (pp2.item(j).getNodeName().equals("link")) {
 					Node type_attr = pp2.item(j).getAttributes().getNamedItem("type");
 					String class_name = null;
 					if (type_attr != null)
 						class_name = container.neType2Classname(type_attr.getNodeValue());
 					else
 						class_name = pp2.item(j).getAttributes().getNamedItem("class").getNodeValue();
 					Class c = Class.forName(class_name);
 					AbstractLink lk = (AbstractLink)c.newInstance();
 					lk.setMyNetwork(this);
 					res &= lk.initFromDOM(pp2.item(j));
 					addLink(lk);
 				}
 				if (pp2.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					if (doc.hasChildNodes())
 						res &= initLinkListFromDOM(doc.getChildNodes().item(0));
 				}
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Initialize Sensor list from the DOM structure.
 	 */
 	private boolean initSensorListFromDOM(Node p) throws Exception {
 		boolean res = true;
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp2 = p.getChildNodes();
 			for (int j = 0; j < pp2.getLength(); j++) {
 				if (pp2.item(j).getNodeName().equals("sensor")) {
 					Node type_attr = pp2.item(j).getAttributes().getNamedItem("type");
 					String class_name = null;
 					if (type_attr != null)
 						class_name = container.neType2Classname(type_attr.getNodeValue());
 					else
 						class_name = pp2.item(j).getAttributes().getNamedItem("class").getNodeValue();
 					Class c = Class.forName(class_name);
 					AbstractSensor sns = (AbstractSensor)c.newInstance();
 					sns.setMyNetwork(this);
 					res &= sns.initFromDOM(pp2.item(j));
 					addSensor(sns);
 				}
 				if (pp2.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					if (doc.hasChildNodes())
 						res &= initSensorListFromDOM(doc.getChildNodes().item(0));
 				}
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Initialize Monitor list from the DOM structure.
 	 */
 	private boolean initMonitorListFromDOM(Node p) throws Exception {
 		boolean res = true;
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp2 = p.getChildNodes();
 			for (int j = 0; j < pp2.getLength(); j++) {
 				if (pp2.item(j).getNodeName().equals("monitor")) {
 					Node type_attr = pp2.item(j).getAttributes().getNamedItem("type");
 					String class_name = null;
 					if (type_attr != null)
 						class_name = container.neType2Classname(type_attr.getNodeValue());
 					else
 						class_name = pp2.item(j).getAttributes().getNamedItem("class").getNodeValue();
 					Class c = Class.forName(class_name);
 					AbstractMonitor mon = (AbstractMonitor)c.newInstance();
 					mon.setMyNetwork(this);
 					res &= mon.initFromDOM(pp2.item(j));
 					addMonitor(mon);
 				}
 				if (pp2.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					if (doc.hasChildNodes())
 						res &= initMonitorListFromDOM(doc.getChildNodes().item(0));
 				}
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Initialize OD list from the DOM structure.
 	 */
 	protected abstract boolean initODListFromDOM(Node p) throws Exception;
 	
 	/**
 	 * Initializes the complex Node from given DOM structure.
 	 * @param p DOM node.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration
 	 */
 	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = true;
 		if (p == null)
 			return !res;
 		if (initialized)
 			return res;
 		if (myNetwork == null) {
 			myNetwork = this;
 			top = true;
 		}
 		else {
 			top = false;
 			container = myNetwork.getContainer();
 		}
 		try  {
 			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
 			Node dt_attr = p.getAttributes().getNamedItem("dt");
 			if (dt_attr == null)
 				dt_attr = p.getAttributes().getNamedItem("tp");
 			tp = Double.parseDouble(dt_attr.getNodeValue());
 			if (tp >= 0.1) // sampling period is in seconds
 				tp = tp/3600;
 			name = p.getAttributes().getNamedItem("name").getNodeValue();
 			if (p.hasChildNodes()) {
 				domnodes.clear();
 				int i;
 				NodeList pp = p.getChildNodes();
 				for (i = 0; i < pp.getLength(); i++) {
 					if (pp.item(i).getNodeName().equals("description")) {
 						description = pp.item(i).getTextContent();
 						if (description.equals("null"))
 							description = "";
 					}
 					if (pp.item(i).getNodeName().equals("position")) {
 						position = new PositionNode();
 						res &= position.initFromDOM(pp.item(i));
 					}
 					if (pp.item(i).getNodeName().equals("NetworkList")) { // 0: Networks
 						res &= initNetworkListFromDOM(pp.item(i));
 					}
 					if (pp.item(i).getNodeName().equals("NodeList")) { // 1: Nodes
 						res &= initNodeListFromDOM(pp.item(i));
 					}
 				}
 				for (i = 0; i < pp.getLength(); i++) // 2: Links
 					if (pp.item(i).getNodeName().equals("LinkList"))
 						res &= initLinkListFromDOM(pp.item(i));
 				for (i = 0; i < nodes.size(); i++) // 3: Nodes again
 					res &= nodes.get(i).initFromDOM(domnodes.get(i));
 				domnodes.clear();
 				for (i = 0; i < pp.getLength(); i++) // 4: Sensors
 					if (pp.item(i).getNodeName().equals("SensorList"))
 						res &= initSensorListFromDOM(pp.item(i));
 				for (i = 0; i < pp.getLength(); i++) // 5: Monitors
 					if (pp.item(i).getNodeName().equals("MonitorList"))
 						res &= initMonitorListFromDOM(pp.item(i));
 				for (i = 0; i < pp.getLength(); i++) // 6: ODs
 					if (pp.item(i).getNodeName().equals("ODList"))
 						res &= initODListFromDOM(pp.item(i));
 			}
 			else
 				if (top)
 					res = false;
 			
 		}
 		catch(Exception e) {
 			res = false;
 			throw new ExceptionConfiguration(e.getMessage());
 		}
 		initialized = true;
 		return res;
 	}
 	
 	/**
 	 * Generates XML description of the complex Node.<br>
 	 * If the print stream is specified, then XML buffer is written to the stream.
 	 * @param out print stream.
 	 * @throws IOException
 	 */
 	public void xmlDump(PrintStream out) throws IOException {
 		int i;
 		if (out == null)
 			out = System.out;
 		out.print("<description>" + description + "</description>\n");
 		position.xmlDump(out);
 		out.print("\n\n<MonitorList>\n");
 		for (i = 0; i < monitors.size(); i++)
 			monitors.get(i).xmlDump(out);
 		out.print("</MonitorList>\n");
 		out.print("\n<NetworkList>\n");
 		for (i = 0; i < networks.size(); i++)
 			networks.get(i).xmlDump(out);
 		out.print("</NetworkList>\n");
 		out.print("\n<NodeList>\n");
 		for (i = 0; i < nodes.size(); i++)
 			nodes.get(i).xmlDump(out);
 		for (i = 0; i < links.size(); i++)
 			links.get(i).xmlDumpTerminalNode(out);
 		out.print("</NodeList>\n");
 		out.print("\n<LinkList>\n");
 		for (i = 0; i < links.size(); i++)
 			links.get(i).xmlDump(out);
 		out.print("</LinkList>\n");
 		out.print("\n<ODList>\n");
 		for (i = 0; i < odList.size(); i++)
 			odList.get(i).xmlDump(out);
 		out.print("</ODList>\n");
 		out.print("\n<SensorList>\n");
 		for (i = 0; i < sensors.size(); i++)
 			sensors.get(i).xmlDump(out);
 		out.print("</SensorList>\n");
 		return;
 	}
 	
 	/**
 	 * Updates all sensors within this network.
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	public synchronized boolean sensorDataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		if (simNo != myNetwork.getSimNo())
 			throw new ExceptionSimulation(this, "Simulation number inconsistency (" + Integer.toString(simNo) + "," + Integer.toString(myNetwork.getSimNo())+ ").");
 		boolean res = true;
 		if (ts < 1)
 			throw new ExceptionSimulation(this, "Nonpositive time step (" + Integer.toString(ts) + ").");
 		int period = (int)Math.round((double)(myNetwork.getTP()/getTop().getTP()));
 		if (period == 0)
 			period = 1;
 		if ((this.ts > 0) && ((ts - this.ts) < period))
 			res = false;
 		this.ts = ts;
 		PrintStream os = null;
 		int initTS = Math.max(myNetwork.getContainer().getMySettings().getTSInitial(), (int)(myNetwork.getContainer().getMySettings().getTimeInitial()/myNetwork.getTop().getTP()));
 		if ((ts - initTS == 1) || (((ts - tsV) * getTop().getTP()) >= container.getMySettings().getDisplayTP())) {
 			os = container.getMySettings().getTmpDataOutput();
 			tsV = ts;
 		}
 		if (os != null) {
 			if (top)
 				os.print("\n" + (ts-1));
 			if (!res)
 				for (int i = 0; i < linksToSave; i++)
 					os.print(", *");
 		}
 		if (!res)
 			return res;
 		for (int i = 0; i < sensors.size(); i++)
 			res &= sensors.get(i).dataUpdate(ts);
 		for (int i = 0; i < networks.size(); i++)
 			res &= networks.get(i).sensorDataUpdate(ts);
 		return res;
 	}
 	
 	/**
 	 * Updates all monitors within this network.
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	public synchronized boolean monitorDataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		boolean res = (ts == this.ts);
 		for (int i = 0; i < monitors.size(); i++)
 			res &= monitors.get(i).dataUpdate(ts);
 		for (int i = 0; i < networks.size(); i++)
 			res &= networks.get(i).monitorDataUpdate(ts);
 		return res;
 	}
 	
 	/**
 	 * Updates all nodes within this network.
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	public synchronized boolean nodeDataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		boolean res = (ts == this.ts);
 		for (int i = 0; i < networks.size(); i++)
 			res &= networks.get(i).nodeDataUpdate(ts);
 		for (int i = 0; i < nodes.size(); i++)
 				res &= nodes.get(i).dataUpdate(ts);
 		return res;
 	}
 	
 	/**
 	 * Updates all links within this network.
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	public synchronized boolean linkDataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		boolean res = (ts == this.ts);
 		for (int i = 0; i < links.size(); i++)
 			res &= links.get(i).dataUpdate(ts);
 		for (int i = 0; i < networks.size(); i++)
 			res &= networks.get(i).linkDataUpdate(ts);
 		return res;
 	}
 	
 	/**
 	 * Updates Network data.<br>
 	 * Initiates data update on all Monitors, Nodes and Links that belong to this Node.
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		if (!sensorDataUpdate(ts))
 			return false;
 		if (!monitorDataUpdate(ts))
 			return false;
 		if (!nodeDataUpdate(ts))
 			return false;
 		if (!monitorDataUpdate(ts))
 			return false;
 		if (!linkDataUpdate(ts))
 			return false;
 		if (!monitorDataUpdate(ts))
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Validates Node configuration.<br>
 	 * Initiates validation of all Monitors, Nodes and Links that belong to this node.
 	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration
 	 */
 	public boolean validate() throws ExceptionConfiguration {
 		int i;
 		cfgErrors.clear();
 		boolean res = super.validate();
 		for (i = 0; i < networks.size(); i++) {
 			res &= networks.get(i).validate();
 			Vector<ErrorConfiguration> errs = networks.get(i).getConfigurationErrors();
 			for (int j = 0; j < errs.size(); j++)
 				cfgErrors.add(errs.get(j));
 		}
 		for (i = 0; i < sensors.size(); i++)
 			res &= sensors.get(i).validate();
 		for (i = 0; i < monitors.size(); i++)
 			res &= monitors.get(i).validate();
 		for (i = 0; i < nodes.size(); i++)
 			res &= nodes.get(i).validate();
 		for (i = 0; i < links.size(); i++)
 			res &= links.get(i).validate();
 		for (i = 0; i < odList.size(); i++)
 			res &= odList.get(i).validate();
 		return res;
 	}
 	
 	/**
 	 * Updates configuration summary.
 	 * @param cs configuration summary.
 	 */
 	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
 		if (cs == null)
 			return;
 		cs.incrementNetworks();
 		int i;
 		for (i = 0; i < sensors.size(); i++)
 			sensors.get(i).updateConfigurationSummary(cs);
 		for (i = 0; i < monitors.size(); i++)
 			monitors.get(i).updateConfigurationSummary(cs);
 		for (i = 0; i < nodes.size(); i++)
 			nodes.get(i).updateConfigurationSummary(cs);
 		for (i = 0; i < links.size(); i++)
 			links.get(i).updateConfigurationSummary(cs);
 		return;
 	}
 
 	/**
 	 * Returns <code>false</code> indicating that it is a complex node.
 	 */
 	public final boolean isSimple() {
 		return false;
 	}
 	
 	/**
 	 * Returns <code>true</code> if it is a top node, <code>false</code> - otherwise.
 	 */
 	public final boolean isTop() {
 		return top;
 	}
 	
 	/**
 	 * Returns <code>true</code> if control is on, <code>false</code> - otherwise.
 	 */
 	public final boolean isControlled() {
 		return controlled;
 	}
 	
 	/**
 	 * Returns container to which it belongs.
 	 */
 	public AbstractContainer getContainer() {
 		return container;
 	}
 	
 	/**
 	 * Returns sampling time of the simulation.
 	 */
 	public final double getTP() {
 		return tp;
 	}
 	
 	/**
 	 * Returns simulation time.
 	 */
 	public final double getSimTime() {
 		return getTop().getTS() * getTop().getTP();
 	}
 	
 	/**
 	 * Returns simulation number.
 	 */
 	public final int getSimNo() {
 		return simNo;
 	}
 	
 	/**
 	 * Returns number of nodes in this network whose state must be saved.
 	 */
 	public final int totalNodesToSave() {
 		return nodesToSave;
 	}
 
 	/**
 	 * Returns number of links in this network whose state must be saved.
 	 */
 	public final int totalLinksToSave() {
 		return linksToSave;
 	}
 	
 	/**
 	 * Returns maximum simulation step.
 	 */
 	public final int getMaxTimeStep() {
 		return maxTimeStep;
 	}
 	
 	/**
 	 * Returns verbosity mode.
 	 */
 	public final boolean getVerbose() {
 		return verbose;
 	}
 	
 	/**
 	 * Returns vector of Sensors.
 	 */
 	public final Vector<AbstractSensor> getSensors() {
 		Vector<AbstractSensor> ss = new Vector<AbstractSensor>();
 		ss.addAll(sensors);
 		for (int i = 0; i < networks.size(); i++)
 			ss.addAll(networks.get(i).getSensors());
 		return ss;
 	}
 	
 	/**
 	 * Returns vector of Monitors.
 	 */
 	public final Vector<AbstractMonitor> getMonitors() {
 		Vector<AbstractMonitor> mm = new Vector<AbstractMonitor>();
 		mm.addAll(monitors);
 		for (int i = 0; i < networks.size(); i++)
 			mm.addAll(networks.get(i).getMonitors());
 		return mm;
 	}
 	
 	/**
 	 * Returns vector of Complex Controllers.
 	 */
 	public final Vector<AbstractControllerComplex> getControllers() {
 		Vector<AbstractControllerComplex> cc = new Vector<AbstractControllerComplex>();
 		cc.addAll(controllers);
 		for (int i = 0; i < networks.size(); i++)
 			cc.addAll(networks.get(i).getControllers());
 		return cc;
 	}
 	
 	/**
 	 * Returns vector of Networks.
 	 */
 	public final Vector<AbstractNodeComplex> getNetworks() {
 		Vector<AbstractNodeComplex> tt = new Vector<AbstractNodeComplex>();
 		tt.addAll(networks);
 		for (int i = 0; i < networks.size(); i++)
 			tt.addAll(networks.get(i).getNetworks());
 		return tt;
 	}
 	
 	/**
 	 * Returns vector of Nodes.
 	 */
 	public final Vector<AbstractNodeSimple> getNodes() {
 		Vector<AbstractNodeSimple> nn = new Vector<AbstractNodeSimple>();
 		nn.addAll(nodes);
 		for (int i = 0; i < networks.size(); i++)
 			nn.addAll(networks.get(i).getNodes());
 		return nn;
 	}
 	
 	/**
 	 * Returns vector of Links.
 	 */
 	public final Vector<AbstractLink> getLinks() {
 		Vector<AbstractLink> ll = new Vector<AbstractLink>();
 		ll.addAll(links);
 		for (int i = 0; i < networks.size(); i++)
 			ll.addAll(networks.get(i).getLinks());
 		return ll;
 	}
 	
 	/**
 	 * Returns vector of source Links.
 	 */
 	public final Vector<AbstractLink> getSourceLinks() {
 		Vector<AbstractLink> sources = new Vector<AbstractLink>();
 		for (int i = 0; i < links.size(); i++)
 			if (links.get(i).getBeginNode() == null)
 				sources.add(links.get(i));
 		for (int i = 0; i < networks.size(); i++)
 			sources.addAll(((AbstractNodeComplex)networks.get(i)).getSourceLinks());
 		return sources;
 	}
 	
 	/**
 	 * Returns vector of destination Links.
 	 */
 	public final Vector<AbstractLink> getDestinationLinks() {
 		Vector<AbstractLink> destinations = new Vector<AbstractLink>();
 		for (int i = 0; i < links.size(); i++)
 			if (links.get(i).getEndNode() == null)
 				destinations.add(links.get(i));
 		for (int i = 0; i < networks.size(); i++)
 			destinations.addAll(networks.get(i).getDestinationLinks());
 		return destinations;
 	}
 	
 	/**
 	 * Returns vector of Links to be saved.
 	 */
 	public final Vector<AbstractLink> getLinksToBeSaved() {
 		Vector<AbstractLink> tobesaved = new Vector<AbstractLink>();
 		for (int i = 0; i < links.size(); i++)
 			if (links.get(i).toSave())
 				tobesaved.add(links.get(i));
 		for (int i = 0; i < networks.size(); i++)
 			tobesaved.addAll(networks.get(i).getLinksToBeSaved());
 		return tobesaved;
 	}
 	
 	/**
 	 * Returns vector of Links to be saved.
 	 */
 	public final Vector<Path> getPaths() {
 		Vector<Path> paths = new Vector<Path>();
 		for (int i = 0; i < odList.size(); i++)
 			paths.addAll(odList.get(i).getPathList());
 		for (int i = 0; i < networks.size(); i++)
 				paths.addAll(networks.get(i).getPaths());
 		return paths;
 	}
 	
 	/**
 	 * Returns list of ODs.
 	 */
 	public final Vector<OD> getODList() {
 		Vector<OD> odl = new Vector<OD>();
 		odl.addAll(odList);
 		for (int i = 0; i < networks.size(); i++)
 			odl.addAll(networks.get(i).getODList());
 		return odl;
 	}
 	
 	/**
 	 * Returns list of configuration errors.
 	 */
 	public final Vector<ErrorConfiguration> getConfigurationErrors() {
 		return cfgErrors;
 	}
 	
 	/**
 	 * Finds and returns Sensor specified by its identifier.
 	 * @param id Sensor identifier.
 	 * @return Sensor, <code>null</code> if Sensor was not found.
 	 */
 	public final AbstractSensor getSensorById(int id) {
 		int i;
 		for (i = 0; i < sensors.size(); i++)
 			if (id == sensors.get(i).getId())
 				return sensors.get(i);
 		AbstractSensor sen = null;
 		for (i = 0; i < networks.size(); i++) {
 				sen = networks.get(i).getSensorById(id);
 			if (sen != null)
 				return sen;
 		}
 		return sen;
 	}
 	
 	/**
 	 * Finds and returns Sensor specified by the identifier of its current link.
 	 * @param id Sensor identifier.
 	 * @return Sensor, <code>null</code> if Sensor was not found.
 	 */
 	public final AbstractSensor getSensorByLinkId(int id) {
 		int i;
 		for (i = 0; i < sensors.size(); i++)
 			if (id == sensors.get(i).getLink().getId())
 				return sensors.get(i);
 		AbstractSensor sen = null;
 		for (i = 0; i < networks.size(); i++) {
 				sen = networks.get(i).getSensorByLinkId(id);
 			if (sen != null)
 				return sen;
 		}
 		return sen;
 	}
 	
 	/**
 	 * Finds and returns Monitor specified by its identifier.
 	 * @param id Monitor identifier.
 	 * @return Monitor, <code>null</code> if Monitor was not found.
 	 */
 	public final AbstractMonitor getMonitorById(int id) {
 		int i;
 		for (i = 0; i < monitors.size(); i++)
 			if (id == monitors.get(i).getId())
 				return monitors.get(i);
 		AbstractMonitor mon = null;
 		for (i = 0; i < networks.size(); i++) {
 				mon = networks.get(i).getMonitorById(id);
 			if (mon != null)
 				return mon;
 		}
 		return mon;
 	}
 	
 	/**
 	 * Finds and returns Network specified by its identifier.
 	 * @param id Network identifier.
 	 * @return Network, <code>null</code> if Network was not found.
 	 */
 	public final AbstractNodeComplex getNetworkById(int id) {
 		int i;
 		if (this.id == id)
 			return this;
 		for (i = 0; i < networks.size(); i++)
 			if (id == networks.get(i).getId())
 				return networks.get(i);
 			else {
 				AbstractNodeComplex net = networks.get(i).getNetworkById(i);
 				if (net != null)
 					return net;
 			}
 		return null;
 	}
 	
 	/**
 	 * Finds and returns Node specified by its identifier.
 	 * @param id Node identifier.
 	 * @return Node, <code>null</code> if Node was not found.
 	 */
 	public final AbstractNodeSimple getNodeById(int id) {
 		int i;
 		for (i = 0; i < nodes.size(); i++)
 			if (id == nodes.get(i).getId())
 				return nodes.get(i);
 		AbstractNodeSimple nd = null;
 		for (i = 0; i < networks.size(); i++) {
 				nd = networks.get(i).getNodeById(id);
 			if (nd != null)
 				return nd;
 		}
 		return nd;
 	}
 	
 	/**
 	 * Finds and returns Link specified by its identifier.
 	 * @param id Link identifier.
 	 * @return Link, <code>null</code> if Link was not found.
 	 */
 	public final AbstractLink getLinkById(int id) {
 		int i;
 		for (i = 0; i < links.size(); i++)
 			if (id == links.get(i).getId())
 				return links.get(i);
 		AbstractLink lk = null;
 		for (i = 0; i < networks.size(); i++) {
 				lk = networks.get(i).getLinkById(id);
 			if (lk != null)
 				return lk;
 		}
 		return lk;
 	}
 	
 	/**
 	 * Sets container to which the network belongs.
 	 * @param x container object.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setContainer(AbstractContainer x) {
 		if (x == null)
 			return false;
 		container = x;
 		return true;
 	}
 	
 	/**
 	 * Sets top flag.
 	 * @param x top flag.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setTop(boolean x) {
 		top = x;
 		return true;
 	}
 	
 	/**
 	 * Sets simulation sampling period.<br>
 	 * Checks that new sampling period is consistent with upper and lower level nodes.
 	 * @param x sampling time.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setTP(double x) {
 		boolean res = true;
 		if ((x < 0) || ((!top) && (getMyNetwork().getTP() > x)))
 			return false;
 		tp = x;
 		for (int i = 0; i < nodes.size(); i++) {
 			if (nodes.get(i).getTP() < x)
 				res &= nodes.get(i).setTP(x);
 		}
 		for (int i = 0; i < monitors.size(); i++) {
 			if ((monitors.get(i).getType() & AbstractTypes.MASK_MONITOR_CONTROLLER) > 0) {
 				AbstractMonitorController mn = (AbstractMonitorController)monitors.get(i);
 				if (mn.getTP() < x)
 					res &= mn.setTP(x);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Sets verbosity mode on/off.
 	 * @param val true to turn verbosity mode on, false - off.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setVerbose(boolean val) {
 		boolean res = true;
 		verbose = val;
 		for (int i = 0; i < networks.size(); i++)
 			res &= networks.get(i).setVerbose(val);
 		return res;
 	}
 	
 	/**
 	 * Additional initialization.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration, ExceptionDatabase
 	 */
 	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
 		boolean res = super.initialize();
 		simNo++;
 		tsV = ts;
 		if (!top) {
 			maxTimeStep = (int)Math.floor((getTop().getTP()*getTop().getMaxTimeStep())/tp);
 		}
 		if (top) {
 			PrintStream os = container.getMySettings().getTmpDataOutput();
 			if (os != null) {
 				Vector<AbstractLink> tobesaved = getLinksToBeSaved();
 				Vector<Path> pathlist = getPaths();
 				os.print("\nSampling Period, Time Units\n" + tp + ", hours\n" + tp*60 + ", minutes\n" + tp*3600 + ", seconds\n\n");
 				os.print("Description\n" + description + "\n\n\nRoutes");
 				for (int i = 0; i < pathlist.size(); i++) {
 					Vector<AbstractLink> pll = pathlist.get(i).getLinkVector();
 					os.print("\n" + pathlist.get(i).getName());
 					for (int j = 0; j < pll.size(); j++)
 						os.print(", " + pll.get(j).getId());
 				}
 				os.print("\n\n\n");
 				String buf_id = "Link ID";
 				String buf_name = "Link Name";
 				String buf_type = "Link Type";
 				String buf_length = "Link Length";
 				String buf_width = "Link Width";
 				String buf_src = "Source";
 				for (int i = 0; i < tobesaved.size(); i++) {
 					buf_id += ", " + tobesaved.get(i).getId();
 					buf_name += ", " + tobesaved.get(i);
 					buf_type += ", " + tobesaved.get(i).getTypeString();
 					buf_length += ", " + tobesaved.get(i).getLength();
 					buf_width += ", " + tobesaved.get(i).getWidth();
 					buf_src += ", ";
 					if (tobesaved.get(i).getBeginNode() == null)
 						buf_src += "Yes";
 					else
 						buf_src += "No";
 				}
 				os.println(buf_id);
 				os.println(buf_name);
 				os.println(buf_type);
 				os.println(buf_length);
 				os.println(buf_width);
 				os.println(buf_src);
 				os.print("Time Step");
 			}
 		}
 		for (int i = 0; i < sensors.size(); i++)
 			res &= sensors.get(i).initialize();
 		for (int i = 0; i < monitors.size(); i++)
 			res &= monitors.get(i).initialize();
 		nodesToSave = 0;
 		linksToSave = 0;
 		for (int i = 0; i < networks.size(); i++) {
 			AbstractNodeComplex net = networks.get(i);
 			res &= net.initialize();
 			nodesToSave += net.totalNodesToSave();
 			linksToSave += net.totalLinksToSave();
 		}
 		for (int i = 0; i < nodes.size(); i++) {
 				res &= nodes.get(i).initialize();
 				if (nodes.get(i).toSave())
 					nodesToSave++;
 		}
 		for (int i = 0; i < links.size(); i++) {
 			res &= links.get(i).initialize();
 			if (links.get(i).toSave())
 				linksToSave++;
 		}
 		return res;
 	}
 	
 	/**
 	 * Sets maximum simulation step.<br>
 	 * @param x maximum time step.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setMaxTimeStep(int x) {
 		if (x < 1)
 			return false;
 		maxTimeStep = x;
 		return true;
 	}
 	
 	/**
 	 * Sets control mode On/Off.<br>
 	 * @param x true/false value.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setControlled(boolean x) {
 		boolean res = true;
 		controlled = x;
 		for (int i = 0; i < networks.size(); i++) {
 			res &= networks.get(i).setControlled(x);
 		}
 		return res;
 	}
 
 	/**
 	 * Adds a Sensor to the list.
 	 * @param x Sensor.
 	 * @return idx index of the added Sensor, <code>-1</code> - if the Sensor could not be added.
 	 */
 	public synchronized int addSensor(AbstractSensor x) {
 		int idx = -1;
 		if ((x != null) && (sensors.add(x)))
 			idx = sensors.size() - 1;
 		return idx;
 	}
 	
 	/**
 	 * Adds a Monitor to the list.
 	 * @param x Monitor.
 	 * @return idx index of the added Monitor, <code>-1</code> - if the Monitor could not be added.
 	 */
 	public synchronized int addMonitor(AbstractMonitor x) {
 		int idx = -1;
 		if ((x != null) && (monitors.add(x)))
 			idx = monitors.size() - 1;
 		return idx;
 	}
 	
 	/**
 	 * Adds a Complex Controller to the list.
 	 * @param x Complex Controller.
 	 * @return idx index of the added Controller, <code>-1</code> - if the Controller could not be added.
 	 */
 	public synchronized int addController(AbstractControllerComplex x) {
 		int idx = -1;
 		if ((x != null) && (controllers.add(x)))
 			idx = controllers.size() - 1;
 		return idx;
 	}
 	
 	/**
 	 * Adds a Network to the list.
 	 * @param x Network.
 	 * @return idx index of the added Network, <code>-1</code> - if the Network could not be added.
 	 */
 	public synchronized int addNetwork(AbstractNodeComplex x) {
 		int idx = -1;
 		if ((x != null) && (networks.add(x)))
 			idx = networks.size() - 1;
 		if (verbose)
 			System.out.println("* " + this + " *: adding Network '" + x + "'");
 		return idx;
 	}
 	
 	/**
 	 * Adds a Node to the list.
 	 * @param x Node.
 	 * @return idx index of the added Node, <code>-1</code> - if the Node could not be added.
 	 */
 	public synchronized int addNode(AbstractNodeSimple x) {
 		int idx = -1;
 		if ((x != null) && (nodes.add(x)))
 			idx = nodes.size() - 1;
 		if (verbose)
 			System.out.println("* " + this + " *: adding Node '" + x + "'");
 		return idx;
 	}
 	
 	/**
 	 * Adds a Link to the list.
 	 * @param x Link.
 	 * @return idx index of the added Link, <code>-1</code> - if the Link could not be added.
 	 */
 	public synchronized int addLink(AbstractLink x) {
 		int idx = -1;
 		if ((x != null) && (links.add(x)))
 			idx = links.size() - 1;
 		if (verbose)
 			System.out.println("* " + this + " *: adding Link '" + x + "'");
 		return idx;
 	}
 	
 	/**
 	 * Adds OD to the list.
 	 * @param x OD.
 	 * @return idx index of the added OD, <code>-1</code> - if the OD could not be added.
 	 */
 	public synchronized int addOD(OD x) {
 		int idx = -1;
 		if ((x != null) && (odList.add(x)))
 			idx = odList.size() - 1;
 		return idx;
 	}
 	
 	/**
 	 * Adds an error to the configuration error list.
 	 * @param x configuration error.
 	 * @return idx index of the added OD, <code>-1</code> - if the OD could not be added.
 	 */
 	public synchronized int addConfigurationError(ErrorConfiguration err) {
 		int idx = -1;
 		if ((err != null) && (err.getSource() != null) && (err.getMessage() != null) && (cfgErrors.add(err)))
 			idx = cfgErrors.size() - 1;
 		return idx;
 	}
 	
 	/**
 	 * Deletes specified NetworkElement from the list.
 	 * @param x Network Element to be deleted.
 	 * @return idx index of deleted Network Element, <code>-1</code> - if such Network Element was not found.
 	 */
 	public synchronized int deleteNetworkElement(AbstractNetworkElement x) {
 		int idx = -1;
 		if (x != null) {
 			if ((x.getType() & AbstractTypes.MASK_MONITOR) > 0)
 				idx = deleteMonitor((AbstractMonitor)x);
 			else if ((x.getType() & AbstractTypes.MASK_SENSOR) > 0)
 				idx = deleteSensor((AbstractSensor)x);
 			else if ((x.getType() & AbstractTypes.MASK_NETWORK) > 0)
 				idx = deleteNetwork((AbstractNodeComplex)x);
 			else if ((x.getType() & AbstractTypes.MASK_NODE) > 0)
 				idx = deleteNode((AbstractNodeSimple)x);
 			else
 				idx = deleteLink((AbstractLink)x);
 		}
 		return idx;
 	}
 	
 	/**
 	 * Deletes specified Monitor from the list. 
 	 * @param x Monitor to be deleted.
 	 * @return idx index of deleted Monitor, <code>-1</code> - if such Monitor was not found.
 	 */
 	public synchronized int deleteMonitor(AbstractMonitor x) {
 		int idx = monitors.indexOf(x);
 		if (idx >= 0) {
 			monitors.remove(idx);
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				idx = networks.get(i).deleteMonitor(x);
 				if (idx >= 0)
 					break;
 			}
 		}
 		if (idx >= 0) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).deletePredecessor(x);
 				monitors.get(i).deleteSuccessor(x);
 			}
 		}
 		return idx;
 	}
 	
 	/**
 	 * Deletes specified Sensor from the list. 
 	 * @param x Sensor to be deleted.
 	 * @return idx index of deleted Sensor, <code>-1</code> - if such Sensor was not found.
 	 */
 	public synchronized int deleteSensor(AbstractSensor x) {
 		int idx = sensors.indexOf(x);
 		if (idx >= 0) {
 			sensors.remove(idx);
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				idx = networks.get(i).deleteSensor(x);
 				if (idx >= 0)
 					break;
 			}
 		}
 		if (idx >= 0) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).deletePredecessor(x);
 				monitors.get(i).deleteSuccessor(x);
 			}
 		}
 		return idx;
 	}
 
 	/**
 	 * Deletes specified Network from the list. 
 	 * @param x Network to be deleted.
 	 * @return idx index of deleted Network, <code>-1</code> - if such Network was not found.
 	 */
 	public synchronized int deleteNetwork(AbstractNodeComplex x) {
 		int idx = networks.indexOf(x);
 		if (idx >= 0) {
 			if (verbose)
 				System.out.println("* " + this + " *: deleting Network '" + x + "'");
 			networks.remove(idx);
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				idx = networks.get(i).deleteNetwork(x);
 				if (idx >= 0)
 					break;
 			}
 		}
 		if (idx >= 0) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).deletePredecessor(x);
 				monitors.get(i).deleteSuccessor(x);
 			}
 		}
 		for (int i = 0; i < monitors.size(); i++)
 			monitors.get(i).deleteDeadNeighbors();
 		return idx;
 	}
 	
 	/**
 	 * Deletes specified Node from the list. 
 	 * @param x Node to be deleted.
 	 * @return idx index of deleted Node, <code>-1</code> - if such Node was not found.
 	 */
 	public synchronized int deleteNode(AbstractNodeSimple x) {
 		int idx = nodes.indexOf(x);
 		if (idx >= 0) {
 			if (verbose)
 				System.out.println("* " + this + " *: deleting Node '" + x + "'");
 			nodes.remove(idx);
 			Vector<AbstractNetworkElement> links = x.getPredecessors();
 			for (int i = 0; i < links.size(); i++)
 				links.get(i).deleteSuccessor(x);
 			links = x.getSuccessors();
 			for (int i = 0; i < links.size(); i++)
 				links.get(i).deletePredecessor(x);
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				idx = networks.get(i).deleteNode(x);
 				if (idx >= 0)
 					break;
 			}
 		}
 		if (idx >= 0) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).deletePredecessor(x);
 				monitors.get(i).deleteSuccessor(x);
 			}
 		}
 		return idx;
 	}
 	
 	/**
 	 * Deletes specified Link from the list. 
 	 * @param x Link to be deleted.
 	 * @return idx index of deleted Link, <code>-1</code> - if such Link was not found.
 	 */
 	public synchronized int deleteLink(AbstractLink x) {
 		int idx = links.indexOf(x);
 		if (idx >= 0) {
 			if (verbose)
 				System.out.println("* " + this + " *: deleting Link '" + x + "'");
 			links.remove(idx);
 			AbstractNetworkElement nd = x.getBeginNode();
 			if (nd != null)
 				nd.deleteSuccessor(x);
 			nd = x.getEndNode();
 			if (nd != null)
 				nd.deletePredecessor(x);
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				idx = networks.get(i).deleteLink(x);
 				if (idx >= 0)
 					break;
 			}
 		}
 		if (idx >= 0) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).deletePredecessor(x);
 				monitors.get(i).deleteSuccessor(x);
 			}
 		}
 		return idx;
 	}
 	
 	/**
 	 * Replaces given Sensor with the new one.
 	 * @param oldm Sensor to be replaced.
 	 * @param newm new Sensor.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	protected synchronized boolean replaceSensor(AbstractSensor olds, AbstractSensor news) {
 		boolean res = false;
 		int idx = sensors.indexOf(olds);
 		if (idx >= 0) {
 			sensors.remove(idx);
 			sensors.add(idx, news);
 			res = true;
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				res = networks.get(i).replaceSensor(olds, news);
 				if (res)
 					break;
 			}
 		}
 		if (res) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).replacePredecessor(olds, news);
 				monitors.get(i).replaceSuccessor(olds, news);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Replaces given Monitor with the new one.
 	 * @param oldm Monitor to be replaced.
 	 * @param newm new Monitor.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	protected synchronized boolean replaceMonitor(AbstractMonitor oldm, AbstractMonitor newm) {
 		boolean res = false;
 		int idx = monitors.indexOf(oldm);
 		if (idx >= 0) {
 			monitors.remove(idx);
 			monitors.add(idx, newm);
 			res = true;
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				res = networks.get(i).replaceMonitor(oldm, newm);
 				if (res)
 					break;
 			}
 		}
 		if (res) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).replacePredecessor(oldm, newm);
 				monitors.get(i).replaceSuccessor(oldm, newm);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Replaces given Network with the new one.
 	 * @param oldn Network to be replaced.
 	 * @param newn new Network.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	protected synchronized boolean replaceNetwork(AbstractNodeComplex oldn, AbstractNodeComplex newn) {
 		boolean res = false;
 		int idx = networks.indexOf(oldn);
 		if (idx >= 0) {
 			networks.remove(idx);
 			networks.add(idx, newn);
 			res = true;
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				res = networks.get(i).replaceNetwork(oldn, newn);
 				if (res)
 					break;
 			}
 		}
 		if (res) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).replacePredecessor(oldn, newn);
 				monitors.get(i).replaceSuccessor(oldn, newn);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Replaces given Node with the new one.
 	 * @param oldn Node to be replaced.
 	 * @param newn new Node.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	protected synchronized boolean replaceNode(AbstractNodeSimple oldn, AbstractNodeSimple newn) {
 		boolean res = false;
 		int idx = nodes.indexOf(oldn);
 		if (idx >= 0) {
 			nodes.remove(idx);
 			nodes.add(idx, newn);
 			res = true;
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				res = networks.get(i).replaceNode(oldn, newn);
 				if (res)
 					break;
 			}
 		}
 		if (res) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).replacePredecessor(oldn, newn);
 				monitors.get(i).replaceSuccessor(oldn, newn);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Replaces given Link with the new one.
 	 * @param oldl Link to be replaced.
 	 * @param newl new Link.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	protected synchronized boolean replaceLink(AbstractLink oldl, AbstractLink newl) {
 		boolean res = false;
 		int idx = links.indexOf(oldl);
 		if (idx >= 0) {
 			links.remove(idx);
 			links.add(idx, newl);
 			res = true;
 		}
 		else {
 			for (int i = 0; i < networks.size(); i++) {
 				res = networks.get(i).replaceLink(oldl, newl);
 				if (res)
 					break;
 			}
 		}
 		if (res) {
 			for (int i = 0; i < monitors.size(); i++) {
 				monitors.get(i).replacePredecessor(oldl, newl);
 				monitors.get(i).replaceSuccessor(oldl, newl);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Replaces given Network Element with the new one.
 	 * @param oldne Network Element to be replaced.
 	 * @param newne new Network Element.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean replaceNetworkElement(AbstractNetworkElement oldne, AbstractNetworkElement newne) {
 		if ((oldne == null) || (newne == null))
 			return false;
 		boolean res = false;
 		if (((oldne.getType() & AbstractTypes.MASK_MONITOR) > 0) && ((newne.getType() & AbstractTypes.MASK_MONITOR) > 0))
 			res = replaceMonitor((AbstractMonitor)oldne, (AbstractMonitor)newne);
 		if (((oldne.getType() & AbstractTypes.MASK_SENSOR) > 0) && ((newne.getType() & AbstractTypes.MASK_SENSOR) > 0))
 			res = replaceSensor((AbstractSensor)oldne, (AbstractSensor)newne);
 		if (((oldne.getType() & AbstractTypes.MASK_NETWORK) > 0) && ((newne.getType() & AbstractTypes.MASK_NETWORK) > 0))
 			res = replaceNetwork((AbstractNodeComplex)oldne, (AbstractNodeComplex)newne);
 		if (((oldne.getType() & AbstractTypes.MASK_NODE) > 0) && ((newne.getType() & AbstractTypes.MASK_NODE) > 0))
 			res = replaceNode((AbstractNodeSimple)oldne, (AbstractNodeSimple)newne);
 		if (((oldne.getType() & AbstractTypes.MASK_LINK) > 0) && ((newne.getType() & AbstractTypes.MASK_LINK) > 0))
 			res = replaceLink((AbstractLink)oldne, (AbstractLink)newne);
 		if (res) {
 			int i;
 			Vector<AbstractNetworkElement> nelist = oldne.getPredecessors();
 			for (i = 0; i < nelist.size(); i++)
 				nelist.get(i).replaceSuccessor(oldne, newne);
 			nelist = oldne.getSuccessors();
 			for (i = 0; i < nelist.size(); i++)
 				nelist.get(i).replacePredecessor(oldne, newne);
 		}
 		return res;
 	}
 	
 	/**
 	 * Clears the OD list.
 	 */
 	public synchronized void clearMyODList() {
 		odList.clear();
 		return;
 	}
 	
 	/**
 	 * Copies data from given Network to a current one.
 	 * @param x given Network Element.
 	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean copyData(AbstractNetworkElement x) {
 		boolean res = super.copyData(x);
 		if ((!res) || ((x.getType() & AbstractTypes.MASK_NETWORK) == 0))
 			return false;
 		AbstractNodeComplex ntwk = (AbstractNodeComplex)x;
 		container = ntwk.getContainer();
 		controlled = ntwk.isControlled();
 		top = ntwk.isTop();
 		simNo = ntwk.getSimNo();
 		tp = ntwk.getTP();
 		maxTimeStep = ntwk.getMaxTimeStep();
 		sensors = ntwk.getSensors();
 		monitors = ntwk.getMonitors();
 		controllers = ntwk.getControllers();
 		nodes = ntwk.getNodes();
 		links = ntwk.getLinks();
 		odList = ntwk.getODList();
 		verbose = ntwk.getVerbose();
 		return res;
 	}
 	
 }
