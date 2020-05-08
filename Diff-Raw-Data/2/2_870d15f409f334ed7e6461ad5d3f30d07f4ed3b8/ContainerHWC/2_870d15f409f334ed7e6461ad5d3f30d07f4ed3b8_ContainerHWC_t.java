 /**
  * @(#)ContainerHWC.java
  */
 
 package aurora.hwc;
 
 import java.io.*;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.*;
 
 import aurora.*;
 
 
 /**
  * Top object that contains pointers
  * to all the Aurora system configuration.
  * @author Alex Kurzhanskiy
  * @version $Id: $
  */
 public final class ContainerHWC extends AbstractContainer {
 	private static final long serialVersionUID = 2277054116304494673L;
 
 	private static String schemaVersion = "1.0.0";
 	
 	
 	public ContainerHWC() { }
 	public ContainerHWC(AbstractNodeComplex ntwk) { myNetwork = ntwk; }
 	public ContainerHWC(EventManager emgr) { myEventManager = emgr; }
 	public ContainerHWC(AbstractNodeComplex ntwk, EventManager emgr) {
 		myNetwork = ntwk;
 		myEventManager = emgr;
 	}
 	
 
 	/**
 	 * Initializes demand profile from DOM structure.
 	 */
 	private boolean initDemandProfileSetFromDOM(Node p) throws Exception {
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp = p.getChildNodes();
 			for (int j = 0; j < pp.getLength(); j++) {
 				if (pp.item(j).getNodeName().equals("demand")) {
 					double demandST = 0.0;
 					Node id_attr = pp.item(j).getAttributes().getNamedItem("link_id");
 					if (id_attr == null)
 						id_attr = pp.item(j).getAttributes().getNamedItem("id");
 					int lkid = Integer.parseInt(id_attr.getNodeValue());
 					Node st_attr = pp.item(j).getAttributes().getNamedItem("start_time");
 					if (st_attr != null) {
 						demandST = Double.parseDouble(st_attr.getNodeValue()) / 3600;
 					}
 					Node dt_attr = pp.item(j).getAttributes().getNamedItem("dt");
 					if (dt_attr == null)
 						dt_attr = pp.item(j).getAttributes().getNamedItem("tp");
 					double demandTP = Double.parseDouble(dt_attr.getNodeValue());
 					if (demandTP > 24) // sampling period in seconds
 						demandTP = demandTP/3600;
 					String demandKnob = pp.item(j).getAttributes().getNamedItem("knob").getNodeValue();
 					AbstractLinkHWC lk = (AbstractLinkHWC)myNetwork.getLinkById(lkid);
 					if (lk != null) {
 						lk.setDemandKnobs(demandKnob);
 						lk.setDemandStartTime(demandST);
 						lk.setDemandTP(demandTP);
 						lk.setDemandVector(pp.item(j).getTextContent());
 					}
 				}
 				if (pp.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
 						if (doc.getChildNodes().item(i).getNodeName().equals("DemandProfile") || doc.getChildNodes().item(i).getNodeName().equals("DemandProfileSet"))
 							initDemandProfileSetFromDOM(doc.getChildNodes().item(i));
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Initializes controller set from DOM structure.
 	 */
 	private boolean initControllerSetFromDOM(Node p) throws Exception {
 		if (p == null)
 			return false;
 		boolean res = true;
 		if (p.hasChildNodes()) {
 			NodeList pp = p.getChildNodes();
 			for (int j = 0; j < pp.getLength(); j++) {
 				if (pp.item(j).getNodeName().equals("controller")) {
 					Node type_attr = pp.item(j).getAttributes().getNamedItem("type");
 					String class_name = null;
 					if (type_attr != null)
 						class_name = ctrType2Classname(type_attr.getNodeValue());
 					Class c = Class.forName(class_name);
 					AbstractController ctrl = (AbstractController)c.newInstance();
 					Node id_attr = pp.item(j).getAttributes().getNamedItem("link_id");
 					if (id_attr != null)
 						((AbstractControllerSimple)ctrl).setMyLink(myNetwork.getLinkById(Integer.parseInt(id_attr.getNodeValue())));
 					else {
 						id_attr = pp.item(j).getAttributes().getNamedItem("node_id");
 						if (id_attr != null)
 							((AbstractControllerNode)ctrl).setMyNode(myNetwork.getNodeById(Integer.parseInt(id_attr.getNodeValue())));
 						else {
 							id_attr = pp.item(j).getAttributes().getNamedItem("network_id");
 							if (id_attr != null)
 								((AbstractControllerComplex)ctrl).setMyNetwork(myNetwork.getNetworkById(Integer.parseInt(id_attr.getNodeValue())));
 						}
 					}
 					if (ctrl.getMyNE() != null)
 						res &= ctrl.initFromDOM(pp.item(j));
 				}
 				if (pp.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
 						if (doc.getChildNodes().item(i).getNodeName().equals("ControllerSet"))
 							initControllerSetFromDOM(doc.getChildNodes().item(i));
 				}
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Initializes split ratio profile from DOM structure.
 	 */
 	private boolean initSplitRatioProfileSetFromDOM(Node p) throws Exception {
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp = p.getChildNodes();
 			for (int j = 0; j < pp.getLength(); j++) {
 				if (pp.item(j).getNodeName().equals("splitratios")) {
 					Node id_attr = pp.item(j).getAttributes().getNamedItem("node_id");
 					if (id_attr == null)
 						id_attr = pp.item(j).getAttributes().getNamedItem("id");
 					int nid = Integer.parseInt(id_attr.getNodeValue());
 					AbstractNodeHWC nd = (AbstractNodeHWC)myNetwork.getNodeById(nid);
 					if (nd != null)
 						nd.initSplitRatioProfileFromDOM(pp.item(j));
 				}
 				if (pp.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
 						if (doc.getChildNodes().item(i).getNodeName().equals("SRProfile") || doc.getChildNodes().item(i).getNodeName().equals("SplitRatioProfileSet"))
 							initSplitRatioProfileSetFromDOM(doc.getChildNodes().item(i));
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Initializes capacity profile from DOM structure.
 	 */
 	private boolean initCapacityProfileSetFromDOM(Node p) throws Exception {
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp = p.getChildNodes();
 			for (int j = 0; j < pp.getLength(); j++) {
 				if (pp.item(j).getNodeName().equals("capacity")) {
 					double capacityST = 0.0;
 					Node id_attr = pp.item(j).getAttributes().getNamedItem("link_id");
 					if (id_attr == null)
 						id_attr = pp.item(j).getAttributes().getNamedItem("id");
 					int lkid = Integer.parseInt(id_attr.getNodeValue());
 					Node st_attr = pp.item(j).getAttributes().getNamedItem("start_time");
 					if (st_attr != null) {
 						capacityST = Double.parseDouble(st_attr.getNodeValue()) / 3600;
 					}
 					Node dt_attr = pp.item(j).getAttributes().getNamedItem("dt");
 					if (dt_attr == null)
 						dt_attr = pp.item(j).getAttributes().getNamedItem("tp");
 					double capacityTP = Double.parseDouble(dt_attr.getNodeValue());
 					if (capacityTP > 24) // sampling period in seconds
 						capacityTP = capacityTP/3600;
 					AbstractLinkHWC lk = (AbstractLinkHWC)myNetwork.getLinkById(lkid);
 					if (lk != null) {
 						lk.setCapacityStartTime(capacityST);
 						lk.setCapacityTP(capacityTP);
 						lk.setCapacityVector(pp.item(j).getTextContent());
 					}
 				}
 				if (pp.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
 						if (doc.getChildNodes().item(i).getNodeName().equals("CapacityProfile") || doc.getChildNodes().item(i).getNodeName().equals("CapacityProfileSet"))
 							initCapacityProfileSetFromDOM(doc.getChildNodes().item(i));
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Initializes capacity profile from DOM structure.
 	 */
 	private boolean initInitialDensityProfileFromDOM(Node p) throws Exception {
 		if (p == null)
 			return false;
 		if (p.hasChildNodes()) {
 			NodeList pp = p.getChildNodes();
 			for (int j = 0; j < pp.getLength(); j++) {
 				if (pp.item(j).getNodeName().equals("density")) {
 					Node id_attr = pp.item(j).getAttributes().getNamedItem("link_id");
 					if (id_attr == null)
 						id_attr = pp.item(j).getAttributes().getNamedItem("id");
 					int lkid = Integer.parseInt(id_attr.getNodeValue());
 					AbstractLinkHWC lk = (AbstractLinkHWC)myNetwork.getLinkById(lkid);
 					if (lk != null) {
 						lk.setInitialDensity(pp.item(j).getTextContent());
 					}
 				}
 				if (pp.item(j).getNodeName().equals("include")) {
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
 					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
 						if (doc.getChildNodes().item(i).getNodeName().equals("InitialDensityProfile"))
 							initInitialDensityProfileFromDOM(doc.getChildNodes().item(i));
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Initializes the container contents from given DOM structure.
 	 * @param p top level DOM node.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration
 	 */
 	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = true;
 		mySettings = new SimulationSettingsHWC();
 		defaultTypes2Classnames();
 		if ((p == null) || (!p.hasChildNodes()))
 			return !res;
 		myNetwork = null;
 		myEventManager = new EventManager();
 		res &= myEventManager.setContainer(this);
 		try {
 			for (int i = 0; i < p.getChildNodes().getLength(); i++)
 				if (p.getChildNodes().item(i).getNodeName().equals("settings")) {
 					if (mySettings == null)
 						mySettings = new SimulationSettingsHWC();
 					res &= mySettings.initFromDOM(p.getChildNodes().item(i));
 				}
 			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
 				if (p.getChildNodes().item(i).getNodeName().equals("network")) {
 					if (myStatus != null)
 						myStatus.setSaved(true);
 					myNetwork = new NodeHWCNetwork();
 					res &= myNetwork.setContainer(this);
 					res &= myNetwork.initFromDOM(p.getChildNodes().item(i));
 				}
 			}
 			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
 				if (p.getChildNodes().item(i).getNodeName().equals("EventList") || p.getChildNodes().item(i).getNodeName().equals("EventSet"))
 					res &= myEventManager.initFromDOM(p.getChildNodes().item(i));
 			}
 			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
 				if (p.getChildNodes().item(i).getNodeName().equals("ControllerSet") || p.getChildNodes().item(i).getNodeName().equals("SplitRatioProfileSet"))
 					res &= initControllerSetFromDOM(p.getChildNodes().item(i));
 				if (p.getChildNodes().item(i).getNodeName().equals("SRProfile") || p.getChildNodes().item(i).getNodeName().equals("SplitRatioProfileSet"))
 					res &= initSplitRatioProfileSetFromDOM(p.getChildNodes().item(i));
				if (p.getChildNodes().item(i).getNodeName().equals("DemandProfile") || p.getChildNodes().item(i).getNodeName().equals("DemandProfileSet"))
 					res &= initDemandProfileSetFromDOM(p.getChildNodes().item(i));
 				if (p.getChildNodes().item(i).getNodeName().equals("CapacityProfile") || p.getChildNodes().item(i).getNodeName().equals("CapacityProfileSet"))
 					res &= initCapacityProfileSetFromDOM(p.getChildNodes().item(i));
 				if (p.getChildNodes().item(i).getNodeName().equals("InitialDensityProfile"))
 					res &= initInitialDensityProfileFromDOM(p.getChildNodes().item(i));
 			}
 		}
 		catch(Exception e) {
 			res = false;
 			throw new ExceptionConfiguration(e.getMessage());
 		}
 		if (myNetwork == null)
 			throw new ExceptionConfiguration("No network specified in the configuration file.");
 		if (myStatus == null)
 			myStatus = new SimulationStatus();
 		myStatus.setSaved(true);
 		myStatus.setStopped(true);
 		if (mySettings.getDisplayTP() < myNetwork.getTP())
 			mySettings.setDisplayTP(myNetwork.getTP());
 		return res;
 	}
 	
 	/**
 	 * Generates XML description of the Aurora system configuration.<br>
 	 * If the print stream is specified, then XML buffer is written to the stream.
 	 * @param out print stream.
 	 * @throws IOException
 	 */
 	public void xmlDump(PrintStream out) throws IOException {
 		out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		out.print("<scenario id=\"-2011\" name=\"" + name + "\" schemaVersion=\"" + schemaVersion + "\">\n");
 		super.xmlDump(out);
 		out.print("\n\n<ControllerSet>");
 		((NodeHWCNetwork)myNetwork).xmlDumpControllerSet(out);
 		out.print("\n</ControllerSet>\n");
 		out.print("\n\n<InitialDensityProfile>\n");
 		((NodeHWCNetwork)myNetwork).xmlDumpInitialDensityProfile(out);
 		out.print("</InitialDensityProfile>\n");
 		out.print("\n\n<DemandProfileSet>\n");
 		((NodeHWCNetwork)myNetwork).xmlDumpDemandProfileSet(out);
 		out.print("</DemandProfileSet>\n");
 		out.print("\n\n<SplitRatioProfileSet>\n");
 		((NodeHWCNetwork)myNetwork).xmlDumpSplitRatioProfileSet(out);
 		out.print("</SplitRatioProfileSet>\n");
 		out.print("\n\n<CapacityProfileSet>\n");
 		((NodeHWCNetwork)myNetwork).xmlDumpCapacityProfileSet(out);
 		out.print("</CapacityProfileSet>\n");
 		out.print("\n</scenario>\n");
 		return;
 	}
 	
 	/**
 	 * Updates the state of the Aurora system.
 	 * @param ts new time step.
 	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation, ExceptionEvent
 	 */
 	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation, ExceptionEvent {
 		boolean res = true;
 		// check if we're still below max simulation step and simulation time
 		if ((ts > mySettings.getTSMax()) || ((ts * myNetwork.getTP()) > mySettings.getTimeMax())) {
 			myStatus.setStopped(true);
 			return true;
 		}
 		// activate events that are due
 		myEventManager.activateCurrentEvents(myNetwork, ts * myNetwork.getTP());
 		// make simulation step
 		res &= myNetwork.dataUpdate(ts);
 		return res;
 	}
 	
 	/**
 	 * Resets the state of the Aurora system.
 	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionEvent
 	 */
 	public synchronized boolean initialize() throws ExceptionConfiguration, ExceptionDatabase, ExceptionEvent {
 		boolean res = true;
 		myStatus.setStopped(true);
 		myStatus.setSaved(true);
 		// roll back events
 		myEventManager.deactivateCurrentEvents(myNetwork, 0.0);
 		// set maximum simulation step   FIXME: remove
 		/*double maxTime = Math.min(getMySettings().getTSMax()*myNetwork.getTP(), getMySettings().getTimeMax());
 		myNetwork.setMaxTimeStep((int)Math.floor(maxTime/myNetwork.getTP()));*/
 		// reset network
 		res &= myNetwork.initialize();
 		return res;
 	}
 	
 	/**
 	 * Fills in the default type letter code to class name maps.
 	 */
 	public void defaultTypes2Classnames() {
 		// *** Network Elements ***
 		// networks
 		ne_type2classname.put("N", "aurora.hwc.NodeHWCNetwork");
 		// monitors
 		ne_type2classname.put("C", "aurora.hwc.MonitorControllerHWC");
 		ne_type2classname.put("Z", "aurora.hwc.MonitorZipperHWC");
 		ne_type2classname.put("E", "aurora.hwc.MonitorEventHWC");
 		// nodes
 		ne_type2classname.put("F", "aurora.hwc.NodeFreeway");
 		ne_type2classname.put("H", "aurora.hwc.NodeHighway");
 		ne_type2classname.put("S", "aurora.hwc.NodeUJSignal");
 		ne_type2classname.put("P", "aurora.hwc.NodeUJStop");
 		ne_type2classname.put("O", "aurora.hwc.NodeOther");
 		ne_type2classname.put("T", "aurora.hwc.NodeTerminal");
 		// links
 		ne_type2classname.put("FW", "aurora.hwc.LinkFwML");
 		ne_type2classname.put("HW", "aurora.hwc.LinkHw");
 		ne_type2classname.put("HOV", "aurora.hwc.LinkFwHOV");
 		ne_type2classname.put("HOT", "aurora.hwc.LinkHOT");
 		ne_type2classname.put("HV", "aurora.hwc.LinkHV");
 		ne_type2classname.put("ETC", "aurora.hwc.LinkETC");
 		ne_type2classname.put("OR", "aurora.hwc.LinkOR");
 		ne_type2classname.put("FR", "aurora.hwc.LinkFR");
 		ne_type2classname.put("IC", "aurora.hwc.LinkIC");
 		ne_type2classname.put("ST", "aurora.hwc.LinkStreet");
 		ne_type2classname.put("D", "aurora.hwc.LinkDummy");
 		// sensors
 		//ne_type2classname.put("LD", "aurora.hwc.SensorLoopDetector");
 		ne_type2classname.put("LOOP", "aurora.hwc.SensorLoopDetector");
 		// *** Events ***
 		evt_type2classname.put("FD", "aurora.hwc.EventFD");
 		evt_type2classname.put("DEMAND", "aurora.hwc.EventDemand");
 		evt_type2classname.put("QLIM", "aurora.hwc.EventQueueMax");
 		evt_type2classname.put("SRM", "aurora.hwc.EventSRM");
 		evt_type2classname.put("WFM", "aurora.hwc.EventWFM");
 		evt_type2classname.put("SCONTROL", "aurora.hwc.EventControllerSimple");
 		evt_type2classname.put("NCONTROL", "aurora.hwc.EventControllerNode");
 		evt_type2classname.put("CCONTROL", "aurora.hwc.EventControllerComplex");
 		evt_type2classname.put("TCONTROL", "aurora.hwc.EventNetworkControl");
 		evt_type2classname.put("MONITOR", "aurora.hwc.EventMonitor");
 		// *** Controllers ***
 		ctr_type2classname.put("ALINEA", "aurora.hwc.control.ControllerALINEA");
 		ctr_type2classname.put("TOD", "aurora.hwc.control.ControllerTOD");
 		ctr_type2classname.put("TR", "aurora.hwc.control.ControllerTR");
 		ctr_type2classname.put("VSLTOD", "aurora.hwc.control.ControllerVSLTOD");
 		ctr_type2classname.put("SLAVE", "aurora.hwc.control.ControllerSlave");
 		ctr_type2classname.put("SIMPLESIGNAL", "aurora.hwc.control.ControllerSimpleSignal");
 		ctr_type2classname.put("SWARM", "aurora.hwc.control.ControllerSWARM");
 		ctr_type2classname.put("HERO", "aurora.hwc.control.ControllerHERO");
 		ctr_type2classname.put("PRETIMED", "aurora.hwc.control.signal.ControllerPretimed");
 		ctr_type2classname.put("ACTUATED", "aurora.hwc.control.signal.ControllerActuated");
 		ctr_type2classname.put("COORDINATED", "aurora.hwc.control.signal.ControllerCoordinated");
 		ctr_type2classname.put("QUEUEOVERRIDE", "aurora.hwc.control.QOverride");
 		ctr_type2classname.put("PROPORTIONAL", "aurora.hwc.control.QProportional");
 		ctr_type2classname.put("PI", "aurora.hwc.control.QPI");
 		// *** Models ***
 		mdl_type2classname.put("CTM", "aurora.hwc.DynamicsCTM");
 		return;
 	}
 	
 	/**
 	 * Sets settings object.
 	 * @param st settings object.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
 	 */
 	public synchronized boolean setMySettings(SimulationSettings st) {
 		boolean res = true;
 		if (st != null)
 			res &= ((NodeHWCNetwork)myNetwork).adjustWeightedData(((SimulationSettingsHWC)st).getVehicleWeights());
 		res &= super.setMySettings(st);
 		return res;
 	}
 	
 }
