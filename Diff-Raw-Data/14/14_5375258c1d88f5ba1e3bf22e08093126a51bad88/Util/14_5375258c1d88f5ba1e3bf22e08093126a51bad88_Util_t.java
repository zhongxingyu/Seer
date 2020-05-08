 package icm.netgen;
 
 import icm.toplschema.Begin;
 import icm.toplschema.DataSources;
 import icm.toplschema.DisplayPosition;
 import icm.toplschema.End;
 import icm.toplschema.Fd;
 import icm.toplschema.Input;
 import icm.toplschema.Inputs;
 import icm.toplschema.Link;
 import icm.toplschema.LinkList;
 import icm.toplschema.Links;
 import icm.toplschema.Node;
 import icm.toplschema.NodeList;
 import icm.toplschema.Output;
 import icm.toplschema.Outputs;
 import icm.toplschema.Parameter;
 import icm.toplschema.Parameters;
 import icm.toplschema.Point;
 import icm.toplschema.Position;
 import icm.toplschema.Sensor;
 import icm.toplschema.SensorList;
 import icm.toplschema.Source;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import netconfig.ModelGraphLink;
 import netconfig.ModelGraphNode;
 import netconfig.NavteqLink;
 import netconfig.NetconfigException;
 import netconfig.TrafficFlowSink;
 import netconfig.TrafficFlowSource;
 import core.Monitor;
 
 public class Util {
 
 	/** a dashed line to delineate debug messages */
 	public static String LINE = "-------------------------------------";
 
 	public static String SCENARIO_ID = "null";
 	public static String SCENARIO_NAME = "null";
 	public static String SCENARIO_SCHEMA_VERSION = "1.0.20";
 
 	public static Boolean NETWORK_ML_CONTROL = false;
 	public static Boolean NETWORK_Q_CONTROL = false;
 	/** the simulation time step, step to 6 seconds according to MM */
 	public static BigDecimal NETWORK_DT = new BigDecimal(6);
 	/** id of the network in TOPL schema */
 	public static String NETWORK_ID = "1";
 	/** name of the network in TOPL schema */
 	public static String NETWORK_NAME = "US101 CSMP network with ramps and PeMS stations and links subdivided and truncated";
 
 	/** divide long links into smaller links (cells) when flag = true */
 	public static boolean flagSubdivisionLink = true;
 
 	/** divide the network into north and south directions when flag = true */
 	public static boolean flagSubdivisionNetwork = true;
 
 	/** the nid of the Highway network to obtain links and nodes */
 	// nid = 227 for the US-101 CSMP network (Highway version)
	// nid = 266 for the US-101 CSMP network (Highway version) (truncated at University due to sensor issue)
	public static int nidHighway = 266;
 	/** the nid of the Hybrid network to obtain ramps */
 	// nid = 228 for the US-101 CSMP network (Hybrid version)
 	public static int nidHybrid = 228;
 	/** output file name */
 	public static String OUTPUT_FILENAME = "US101_CSMP_ramp_PeMS_subdivisionlink_truncated";
 
 	/**
 	 * 
 	 * @param modelGraphLinks
 	 *            from the model graph based network
 	 * @param modelGraphNodes
 	 *            from the model graph based network
 	 * @return true if startNode and endNode of all links are in the list of
 	 *         modelGraphNodes
 	 */
 	public static boolean sanityCheck1(ModelGraphLink[] modelGraphLinks,
 			ModelGraphNode[] modelGraphNodes) {
 
 		Monitor.out(Util.LINE);
 
 		// generate a list of id for all modelGraphNodes
 		ArrayList<Integer> nodeID = new ArrayList<Integer>();
 		for (ModelGraphNode node : modelGraphNodes) {
 			nodeID.add(node.id);
 		}
 		for (ModelGraphLink link : modelGraphLinks) {
 			if (!nodeID.contains(link.startNodeIndex)
 					|| !nodeID.contains(link.endNodeIndex)) {
 				Monitor.err("link with unidentified node: link id = " + link.id);
 				Monitor.out("Sanity check (no missing link or node): " + false);
 				return false;
 			}
 		}
 		Monitor.out("Sanity check (no missing link or node): " + true);
 		return true;
 
 	}
 
 	/**
 	 * 
 	 * @param modelGraphLinks
 	 *            from the model graph based network
 	 * @param modelGraphNodes
 	 *            from the model graph based network
 	 * @param netHighway
 	 *            the Highway type model graph network
 	 * @return true if there is no hanging modelGraphLinks
 	 * @throws NetconfigException
 	 */
 	public static boolean sanityCheck2(ModelGraphLink[] modelGraphLinks,
 			ModelGraphNode[] modelGraphNodes, netconfig.Network netHighway)
 			throws NetconfigException {
 
 		Monitor.out(Util.LINE);
 		Monitor.out("Number of MM model_graph nodes: " + modelGraphNodes.length);
 
 		HashSet<Integer> nodeIdAll = new HashSet<Integer>();
 		for (ModelGraphNode node : modelGraphNodes) {
 			nodeIdAll.add(node.id);
 		}
 
 		// counter for # of connected groups of nodes
 		int counter = 0;
 
 		while (!nodeIdAll.isEmpty()) {
 
 			counter++;
 
 			// nodeIdSet for the collection of node id of connected nodes
 			HashSet<Integer> nodeIdSet = new HashSet<Integer>();
 			Integer nodeIdSize = nodeIdSet.size();
 
 			// take one element out of nodeIdAll and add to nodeIdSet
 			for (Integer i : nodeIdAll) {
 				nodeIdSet.add(i);
 				break;
 			}
 			// Monitor.out("size of nodeIdSet: " + nodeIdSet.size());
 
 			// nodeIdSize is the size before adding extra nodes
 			while (nodeIdSet.size() > nodeIdSize) {
 
 				// nodeIdToAdd is to collect the nodes to be added to nodeIdSet
 				// in each iteration
 				HashSet<Integer> nodeIdToAdd = new HashSet<Integer>();
 
 				// update nodeIdSize
 				nodeIdSize = nodeIdSet.size();
 
 				for (Integer id : nodeIdSet) {
 					ModelGraphLink[] inLinks = netHighway.getNodeWithID(id)
 							.getInLinks();
 					for (ModelGraphLink l : inLinks) {
 						nodeIdToAdd.add(l.startNodeIndex);
 					}
 					ModelGraphLink[] outLinks = netHighway.getNodeWithID(id)
 							.getOutLinks();
 					for (ModelGraphLink l : outLinks) {
 						nodeIdToAdd.add(l.endNodeIndex);
 					}
 				}
 				nodeIdSet.addAll(nodeIdToAdd);
 				// Monitor.out("size of nodeIdSet: " + nodeIdSet.size());
 			}
 
 			Monitor.out("Node group " + counter + ", size: " + nodeIdSet.size());
 			Monitor.out("Node id in group: " + nodeIdSet);
 
 			// remove nodes in nodeIdSet (connected nodes) from nodeIdAll
 			nodeIdAll.removeAll(nodeIdSet);
 		}
 
 		if (counter <= 2) {
 			Monitor.out("Sanity check (no hanging link): " + true);
 			return true;
 		} else {
 			Monitor.out("Sanity check (no hanging link): " + false);
 			return false;
 		}
 
 	}
 
 	public static boolean sanityCheck3(ModelGraphLink[] modelGraphLinks) {
 
 		Monitor.out(Util.LINE);
 		boolean flag = true;
 		float dt = 6.0f;
 
 		for (ModelGraphLink l : modelGraphLinks) {
 			float speedLimit = l.getAverageSpeedLimit();
 			float minLinkLength = speedLimit * dt;
 			float linkLength = l.getLength();
 			if (linkLength < minLinkLength) {
 				Monitor.err("Short link (MM): link = " + l.id + ", speed = "
 						+ speedLimit + ", length = " + linkLength
 						+ ", minLength = " + minLinkLength);
 				flag = false;
 			}
 		}
 
 		Monitor.out("Sanity check (short links): " + flag);
 		return flag;
 	}
 
 	/**
 	 * <p>
 	 * The criterion for a terminal node is when a node does not have any
 	 * incoming link (source) or when it does not have any outgoing link (sink).
 	 * </p>
 	 * 
 	 * <p>
 	 * There is also debug messages with node id, # inLinks, # outLinks, lat,
 	 * lon.
 	 * </p>
 	 * 
 	 * @param modelGraphLinks
 	 *            from the model graph based network
 	 * @param modelGraphNodes
 	 *            from the model graph based network
 	 * @return an ArrayList of the id of terminal nodes (sources and sinks).
 	 * 
 	 * @throws NetconfigException
 	 */
 	public static ArrayList<Integer> findTerminalNodesID(
 			ModelGraphLink[] modelGraphLinks, ModelGraphNode[] modelGraphNodes)
 			throws NetconfigException {
 
 		Monitor.out(Util.LINE);
 		Monitor.out("This is for the debug of terminal nodes. Format:");
 		Monitor.out("node id, # inLinks, # outLinks, lat, lon");
 		ArrayList<Integer> terminalNodesID = new ArrayList<Integer>();
 		for (ModelGraphNode node : modelGraphNodes) {
 			ModelGraphLink[] inLinks = node.getInLinks();
 			ModelGraphLink[] outLinks = node.getOutLinks();
 			if (inLinks.length == 0) { // source node
 				if (outLinks.length == 0) { // sink node
 					// isolated node, this should not happen!
 					Monitor.err("isolated node identified: node id = "
 							+ node.id);
 				} else { // not sink node
 					// source node
 					terminalNodesID.add(node.id);
 					Monitor.out(node.id
 							+ ", "
 							+ inLinks.length
 							+ ", "
 							+ outLinks.length
 							+ ", "
 							+ outLinks[0].getGeoMultiLine()
 									.getFirstCoordinate().lat
 							+ ", "
 							+ outLinks[0].getGeoMultiLine()
 									.getFirstCoordinate().lon);
 				}
 			} else { // not source node
 				if (node.getOutLinks().length == 0) { // sink node
 					// sink node
 					terminalNodesID.add(node.id);
 					Monitor.out(node.id
 							+ ", "
 							+ inLinks.length
 							+ ", "
 							+ outLinks.length
 							+ ", "
 							+ inLinks[0].getGeoMultiLine().getLastCoordinate().lat
 							+ ", "
 							+ inLinks[0].getGeoMultiLine().getLastCoordinate().lon);
 				} else { // not sink node
 					// intermediate node
 				}
 			}
 		}
 		return terminalNodesID;
 
 	}
 
 	/**
 	 * <p>
 	 * The criterion for a signal node is when any incoming link of a node has a
 	 * signal at the end of the link.
 	 * </p>
 	 * 
 	 * <p>
 	 * There is also debug messages with node id, # inLinks, # inLinks signal,
 	 * lat, lon.
 	 * </p>
 	 * 
 	 * <p>
 	 * Originally Ethan thinks the criterion should be when all incoming links
 	 * (# of links > 0) of a node have signal at the end of the link. He tested
 	 * some nodes (NavteqNode id 4674 and 32741). At node 4674, 4 of the 5
 	 * incoming links say the node has signal, 1 disagree; verification with
 	 * google street view indicate there is a signal. At node 32741, 2 of the 4
 	 * incoming links say the node has a signal, 2 disagree; verification again
 	 * show signal. The criterion is therefore changed.
 	 * </p>
 	 * 
 	 * @param modelGraphLinks
 	 *            from the model graph based network
 	 * @param modelGraphNodes
 	 *            from the model graph based network
 	 * @return an ArrayList of the id of signal nodes.
 	 * @throws NetconfigException
 	 */
 	public static ArrayList<Integer> findSignalNodesID(
 			ModelGraphLink[] modelGraphLinks, ModelGraphNode[] modelGraphNodes)
 			throws NetconfigException {
 
 		Monitor.out(Util.LINE);
 		Monitor.out("This is for the debug of signal nodes. Format:");
 		Monitor.out("node id, # inLinks, # inLinks signal, lat, lon");
 		ArrayList<Integer> signalNodesID = new ArrayList<Integer>();
 		for (ModelGraphNode node : modelGraphNodes) { // for each ModelGraphNode
 			ModelGraphLink[] inLinks = node.getInLinks();
 			int countSignal = 0;
 			NavteqLink[] ntLinks = null;
 			for (int i = 0; i < inLinks.length; i++) {
 				// for each of its incoming ModelGraphLink
 				ntLinks = inLinks[i].getNavteqLinks();
 				// if the last NavteqLink has a signal at the end of the link
 				if (ntLinks[ntLinks.length - 1].signal) {
 					countSignal++;
 				}
 			}
 			// condition for signal node: if any incoming links has a signal at
 			// the end of link
 			if (countSignal > 0) {
 				signalNodesID.add(node.id);
 				Monitor.out(node.id + ", " + inLinks.length + ", "
 						+ countSignal + ", "
 						+ inLinks[0].getGeoMultiLine().getLastCoordinate().lat
 						+ ", "
 						+ inLinks[0].getGeoMultiLine().getLastCoordinate().lon);
 			}
 		}
 		return signalNodesID;
 
 	}
 
 	/**
 	 * 
 	 * <p>
 	 * The criterion for a freeway node is when a node ((is a source node) OR
 	 * (has at least one freeway incoming link)) AND ((is a sink node) OR (has
 	 * at least one freeway outgoing link)).
 	 * </p>
 	 * 
 	 * <p>
 	 * The relation between ModelGraphLink type (highway, arterial, ramp) and
 	 * link functional classification (functional class). There seems to be a
 	 * link_type field for link table in the model_graph schema. The small
 	 * network (nid=160) surrounded by 380, 101, 92 and 280 only has links with
 	 * function class 2 to 5. US-101 is class 2, El Camino Real is class 3.
 	 * Therefore, I would assume highway stands for class 1 and 2, and arterial
 	 * stands for class 3 to 5.
 	 * </p>
 	 * 
 	 * @param modelGraphLinks
 	 *            from the model graph based network
 	 * @param modelGraphNodes
 	 *            from the model graph based network
 	 * @return an ArrayList of the id of freeway nodes.
 	 * @throws NetconfigException
 	 */
 	public static ArrayList<Integer> findFreewayNodesID(
 			ModelGraphLink[] modelGraphLinks, ModelGraphNode[] modelGraphNodes)
 			throws NetconfigException {
 
 		ArrayList<Integer> freewayNodesID = new ArrayList<Integer>();
 		for (ModelGraphNode node : modelGraphNodes) { // for each ModelGraphNode
 			boolean flagIn = false;
 			ModelGraphLink[] inLinks = node.getInLinks();
 			if (inLinks.length == 0) {
 				flagIn = true;
 			} else {
 				for (int i = 0; i < inLinks.length; i++) {
 					// for each of its incoming ModelGraphLink
 					NavteqLink[] ntLinks = inLinks[i].getNavteqLinks();
 					// if last NavteqLink is of function class 1 or 2
 					if (ntLinks[ntLinks.length - 1].function_class <= 2) {
 						flagIn = true;
 					}
 				}
 			}
 
 			boolean flagOut = false;
 			ModelGraphLink[] outLinks = node.getOutLinks();
 			if (outLinks.length == 0) {
 				flagOut = true;
 			} else {
 				for (int i = 0; i < outLinks.length; i++) {
 					// for each of its outgoing ModelGraphLink
 					NavteqLink[] ntLinks = outLinks[i].getNavteqLinks();
 					// if first NavteqLink is of function class 1 or 2
 					if (ntLinks[0].function_class <= 2) {
 						flagOut = true;
 					}
 				}
 			}
 
 			if (flagIn && flagOut) {
 				freewayNodesID.add(node.id);
 			}
 		}
 		return freewayNodesID;
 
 	}
 
 	/**
 	 * 
 	 * <p>
 	 * Besides the nodes from modelGraphNodes, ramps are also included, which
 	 * are implicitly accounted for using Traffic Flow Sources and Traffic Flow
 	 * Sinks in MM. Ramps are explicitly added here using information from
 	 * Traffic Flow Sources and Traffic Flow Sinks. The id of Traffic Flow
 	 * Sources and Traffic Flow Sinks actually is the id of the link thru which
 	 * flow comes in. Starting node of the link is added as new terminal nodes
 	 * if current node is not a terminal node.
 	 * </p>
 	 * 
 	 * @param modelGraphLinks
 	 *            from the model graph based network
 	 * @param modelGraphNodes
 	 *            from the model graph based network
 	 * @param sources
 	 *            traffic flow sources from the model graph based network
 	 * @param sinks
 	 *            traffic flow sinks from the model graph based network
 	 * @param netHybrid
 	 *            the hybrid version of the network will provide ramp
 	 *            information
 	 * @param terminalNodesID
 	 *            ArrayList of node ID for terminal nodes
 	 * @param signalNodesID
 	 *            ArrayList of node ID for signal nodes
 	 * @param freewayNodesID
 	 *            ArrayList of node ID for freeway nodes
 	 * @return nodeList for TOPL network
 	 * 
 	 * @throws NetconfigException
 	 */
 	public static NodeList parseModelGraphNodes(
 			ModelGraphLink[] modelGraphLinks, ModelGraphNode[] modelGraphNodes,
 			TrafficFlowSource[] sources, TrafficFlowSink[] sinks,
 			netconfig.Network netHybrid, ArrayList<Integer> terminalNodesID,
 			ArrayList<Integer> signalNodesID,
 			ArrayList<Integer> freewayNodesID, boolean flagSubdivisionLink)
 			throws NetconfigException {
 
 		NodeList nodeList = new NodeList();
 		Node node;
 		ArrayList<Node> nodes;
 
 		for (ModelGraphNode n : modelGraphNodes) {
 
 			node = parseSingleModelGraphNode(n, flagSubdivisionLink);
 
 			// set node type
 			// if node id is within terminalNodesID, then it is a terminal node
 			if (terminalNodesID.contains(n.id)) {
 				node.setType("T");
 			} else if (signalNodesID.contains(n.id)) {
 				node.setType("S");
 			} else if (freewayNodesID.contains(n.id)) {
 				node.setType("F");
 			} else {
 				// temporarily, change the type "O" to "F" to run simulation
 				node.setType("F");
 			}
 
 			// no input/output links for terminal nodes
 			if (node.getType().equals("T")) {
 				Inputs inputs = new Inputs();
 				node.setInputs(inputs);
 				Outputs outputs = new Outputs();
 				node.setOutputs(outputs);
 			}
 
 			// add node to the nodeList
 			nodeList.getNode().add(node);
 		}
 
 		// add newly generated nodes due to subdivision of links
 		for (ModelGraphLink l : modelGraphLinks) {
 			nodes = parseNodesSubdivision(l, flagSubdivisionLink);
 
 			// set type of new node to "F" (freeway)
 			for (Node n : nodes) {
 				n.setType("F");
 			}
 
 			// add node to the nodeList
 			nodeList.getNode().addAll(nodes);
 		}
 
 		for (TrafficFlowSource s : sources) {
 			// add extra terminal nodes only if source node is non-terminal
 			if (!terminalNodesID.contains(s.node.id)) {
 				// get the start node of the traffic flow source
 				ModelGraphLink l = netHybrid.getLinkWithID(s.id);
 				// sometimes ramp links are not in the hybrid network
 				if (l != null) {
 					Integer nodeIndex = l.startNodeIndex;
 					ModelGraphNode n = netHybrid.getNodeWithID(nodeIndex);
 					// for terminal node on ramp, no subdivision of ramp is
 					// needed
 					node = parseSingleModelGraphNode(n, false);
 					// set type to terminal
 					node.setType("T");
 					// no input/output links for terminal nodes
 					Inputs inputs = new Inputs();
 					node.setInputs(inputs);
 					Outputs outputs = new Outputs();
 					node.setOutputs(outputs);
 					// add node to the nodeList
 					nodeList.getNode().add(node);
 				} else {
 					Monitor.out("Ramp with link id " + s.id
 							+ " not found in netHybrid");
 				}
 			}
 		}
 
 		for (TrafficFlowSink s : sinks) {
 			// add extra terminal nodes only if source node is non-terminal
 			if (!terminalNodesID.contains(s.node.id)) {
 				// get the end node of the traffic flow sink
 				ModelGraphLink l = netHybrid.getLinkWithID(s.id);
 				// sometimes ramp links are not in the hybrid network
 				if (l != null) {
 					Integer nodeIndex = l.endNodeIndex;
 					ModelGraphNode n = netHybrid.getNodeWithID(nodeIndex);
 					// for terminal node on ramp, no subdivision of ramp is
 					// needed
 					node = parseSingleModelGraphNode(n, false);
 					// set type to terminal
 					node.setType("T");
 					// no input/output links for terminal nodes
 					Inputs inputs = new Inputs();
 					node.setInputs(inputs);
 					Outputs outputs = new Outputs();
 					node.setOutputs(outputs);
 					// add node to the nodeList
 					nodeList.getNode().add(node);
 				} else {
 					Monitor.out("Ramp with link id " + s.id
 							+ " not found in netHybrid");
 				}
 			}
 		}
 
 		return nodeList;
 	}
 
 	/**
 	 * 
 	 * @param modelGraphNode
 	 *            a node in the model graph network
 	 * @return Node object in TOPL format
 	 * @throws NetconfigException
 	 */
 	public static Node parseSingleModelGraphNode(ModelGraphNode modelGraphNode,
 			boolean flagSubdivisionLink) throws NetconfigException {
 
 		// instantiate a node object
 		Node node = new Node();
 
 		// set node name (to empty string)
 		node.setName("");
 
 		// set node id
 		node.setId(Integer.toString(modelGraphNode.id));
 
 		// set node position (lat and lon)
 		Position position = new Position();
 		Point point = new Point();
 		try {
 			point.setLat(new BigDecimal(modelGraphNode.getNavteqNode().geom.lat));
 			point.setLng(new BigDecimal(modelGraphNode.getNavteqNode().geom.lon));
 		} catch (NetconfigException e) {
 			e.printStackTrace();
 			Monitor.err("Failed to find NavteqNode of the ModelGraphNode!");
 		}
 		position.getPoint().add(point);
 		node.setPosition(position);
 
 		// set output links of the node only for non-terminal nodes
 		Outputs outputs = new Outputs();
 		ModelGraphLink[] outLinks;
 		Output output;
 		int cellID;
 		try {
 			outLinks = modelGraphNode.getOutLinks();
 			for (int j = 0; j < outLinks.length; j++) {
 				output = new Output();
 				if (!flagSubdivisionLink) {
 					output.setLinkId(Integer.toString(outLinks[j].id));
 				} else {
 					// cell id is always 0 or "00" for output links when
 					// subdivide
 					cellID = 0;
 					output.setLinkId(Integer.toString(outLinks[j].id)
 							+ String.format("%02d", cellID));
 				}
 				outputs.getOutput().add(output);
 			}
 		} catch (NetconfigException e) {
 			e.printStackTrace();
 			Monitor.err("Failed to find outLinks Node of the ModelGraphNode!");
 		}
 		node.setOutputs(outputs);
 
 		// set input links of the node only for non-terminal nodes
 		Inputs inputs = new Inputs();
 		ModelGraphLink[] inLinks;
 		Input input;
 		try {
 			inLinks = modelGraphNode.getInLinks();
 			for (int j = 0; j < inLinks.length; j++) {
 				input = new Input();
 				if (!flagSubdivisionLink) {
 					input.setLinkId(Integer.toString(inLinks[j].id));
 				} else {
 					// cell id always takes the maximal number
 					// (nbCells - 1), for input links
 					cellID = inLinks[j].nbCells - 1;
 					input.setLinkId(Integer.toString(inLinks[j].id)
 							+ String.format("%02d", cellID));
 				}
 				inputs.getInput().add(input);
 			}
 		} catch (NetconfigException e) {
 			e.printStackTrace();
 			Monitor.err("Failed to find inLinks Node of the ModelGraphNode!");
 		}
 		node.setInputs(inputs);
 
 		return node;
 	}
 
 	public static ArrayList<Node> parseNodesSubdivision(
 			ModelGraphLink modelGraphLink, boolean flagSubdivisionLink)
 			throws NetconfigException {
 
 		ArrayList<Node> nodes = new ArrayList<Node>();
 
 		if (flagSubdivisionLink) {
 			// the id of extra nodes will be ccccc01, ccccc02, ccccc03
 			// (for 4 cells) if the original link id is ccccc
 			for (int cellID = 1; cellID < modelGraphLink.nbCells; cellID++) {
 				// instantiate a node object
 				Node node = new Node();
 
 				// set node name (to empty string)
 				node.setName("");
 
 				// set node id with link id + cell id
 				node.setId(Integer.toString(modelGraphLink.id)
 						+ String.format("%02d", cellID));
 
 				// set node position (lat and lon)
 				Position position = new Position();
 				Point point = new Point();
 				try {
 					point.setLat(new BigDecimal(modelGraphLink
							.getCoordinate(cellID * modelGraphLink.length
									/ modelGraphLink.nbCells).lat));
 					point.setLng(new BigDecimal(modelGraphLink
							.getCoordinate(cellID * modelGraphLink.length
									/ modelGraphLink.nbCells).lon));
 				} catch (NetconfigException e) {
 					e.printStackTrace();
 					Monitor.err("Failed to find NavteqNode of the ModelGraphNode!");
 				}
 				position.getPoint().add(point);
 				node.setPosition(position);
 
 				// set output links
 				// e.g. the output link of new node ccccc02 is link ccccc02
 				Outputs outputs = new Outputs();
 				Output output = new Output();
 				output.setLinkId(Integer.toString(modelGraphLink.id)
 						+ String.format("%02d", cellID));
 				outputs.getOutput().add(output);
 				node.setOutputs(outputs);
 
 				// set input links
 				// e.g. the input link of new node ccccc02 is link ccccc01
 				Inputs inputs = new Inputs();
 				Input input = new Input();
 				input.setLinkId(Integer.toString(modelGraphLink.id)
 						+ String.format("%02d", cellID - 1));
 				inputs.getInput().add(input);
 				node.setInputs(inputs);
 
 				nodes.add(node);
 			}
 		}
 
 		return nodes;
 	}
 
 	/**
 	 * 
 	 * <p>
 	 * Besides the links from modelGraphLinks, on-ramp links (OR) and off-ramp
 	 * links (FR) are also included. The id of Traffic Flow Sources and Traffic
 	 * Flow Sinks actually is the id of these links. If there are duplicate link
 	 * id, it means there are multiple inputs/outputs are the same location,
 	 * then only one is picked (randomly).
 	 * </p>
 	 * 
 	 * @param modelGraphLinks
 	 *            from the model graph based network
 	 * @param modelGraphNodes
 	 *            from the model graph based network
 	 * @param sources
 	 *            traffic flow sources from the model graph based network
 	 * @param sinks
 	 *            traffic flow sinks from the model graph based network
 	 * @param netHybrid
 	 *            the hybrid version of the network will provide ramp
 	 *            information
 	 * @param terminalNodesID
 	 *            ArrayList of node ID for terminal nodes
 	 * @return linkList for TOPL network
 	 * 
 	 * @throws NetconfigException
 	 */
 	public static LinkList parseModelGraphLinks(
 			ModelGraphLink[] modelGraphLinks, ModelGraphNode[] modelGraphNodes,
 			TrafficFlowSource[] sources, TrafficFlowSink[] sinks,
 			netconfig.Network netHybrid, ArrayList<Integer> terminalNodesID,
 			boolean flagSubdivisionLink) throws NetconfigException {
 
 		LinkList linkList = new LinkList();
 		ArrayList<Link> links;
 
 		Monitor.out(Util.LINE);
 		int count = 0;
 		for (ModelGraphLink l : modelGraphLinks) {
 
 			links = parseSingleModelGraphLink(l, flagSubdivisionLink);
 
 			// add links to the linkList
 			linkList.getLink().addAll(links);
 			count += links.size();
 		}
 		Monitor.out("model graph links added: " + count);
 
 		count = 0;
 		for (TrafficFlowSource s : sources) {
 			if (!terminalNodesID.contains(s.node.id)) {
 				ModelGraphLink l = netHybrid.getLinkWithID(s.id);
 				if (l != null) {
 					// when adding on ramps, no subdivision
 					links = parseSingleModelGraphLink(l, false);
 					// traffic flow sources are equivalent to on-ramps
 					for (Link link : links) {
 						link.setType("OR");
 					}
 					// add link to the linkList
 					linkList.getLink().addAll(links);
 					count += links.size();
 				} else {
 					Monitor.out("Ramp with link id " + s.id
 							+ " not found in netHybrid");
 				}
 			} else {
 				Monitor.out("source is also terminal, node id: " + s.node.id
 						+ ", souce id: " + s.id);
 			}
 		}
 		Monitor.out("on ramp links added: " + count);
 		Monitor.out("Number of sources: " + sources.length);
 
 		count = 0;
 		for (TrafficFlowSink s : sinks) {
 			if (!terminalNodesID.contains(s.node.id)) {
 				ModelGraphLink l = netHybrid.getLinkWithID(s.id);
 				if (l != null) {
 					// when adding off ramps, no subdivision
 					links = parseSingleModelGraphLink(l, false);
 					// traffic flow sinks are equivalent to off-ramps
 					for (Link link : links) {
 						link.setType("FR");
 					}
 					// add link to the linkList
 					linkList.getLink().addAll(links);
 					count += links.size();
 				} else {
 					Monitor.out("Ramp with link id " + s.id
 							+ " not found in netHybrid");
 				}
 			} else {
 				Monitor.out("sink is also terminal, node id: " + s.node.id
 						+ ", souce id: " + s.id);
 			}
 		}
 		Monitor.out("off ramp links added: " + count);
 		Monitor.out("Number of sinks: " + sinks.length);
 
 		return linkList;
 	}
 
 	/**
 	 * 
 	 * @param modelGraphLink
 	 *            a link from the model graph network
 	 * @return Link object in TOPL format
 	 * @throws NetconfigException
 	 */
 	public static ArrayList<Link> parseSingleModelGraphLink(
 			ModelGraphLink modelGraphLink, boolean flagSubdivisionLink)
 			throws NetconfigException {
 
 		// instantiate a Link object
 		ArrayList<Link> links = new ArrayList<Link>();
 
 		// totalCellNumber: number of loops carried out next
 		int totalCellNumber;
 		if (!flagSubdivisionLink) {
 			totalCellNumber = 1;
 		} else {
 			totalCellNumber = modelGraphLink.nbCells;
 		}
 
 		for (int cellID = 0; cellID < totalCellNumber; cellID++) {
 
 			Link link = new Link();
 
 			// set node name (to empty string)
 			link.setName("");
 
 			if (!flagSubdivisionLink) {
 				// set link id
 				link.setId(Integer.toString(modelGraphLink.id));
 
 				// set the begin and end node id
 				Begin begin = new Begin();
 				End end = new End();
 				begin.setNodeId(Integer.toString(modelGraphLink.startNodeIndex));
 				end.setNodeId(Integer.toString(modelGraphLink.endNodeIndex));
 				link.setBegin(begin);
 				link.setEnd(end);
 			} else {
 				// set link id
 				// when subdivide, the new link id will be ccccc00, ccccc01,
 				// ccccc02 (for 3 cells), if the original link id is ccccc
 				link.setId(Integer.toString(modelGraphLink.id)
 						+ String.format("%02d", cellID));
 
 				// set the begin and end node id
 				// when subdivide, the original start and end node will keep
 				// their id
 				// id of the intermediate nodes will be ccccc01, ccccc02
 				// (for 3 cells) if the original link id is ccccc
 				Begin begin = new Begin();
 				End end = new End();
 				if (cellID == 0) {
 					begin.setNodeId(Integer
 							.toString(modelGraphLink.startNodeIndex));
 				} else {
 					begin.setNodeId(Integer.toString(modelGraphLink.id)
 							+ String.format("%02d", cellID));
 				}
 				if (cellID == totalCellNumber - 1) {
 					end.setNodeId(Integer.toString(modelGraphLink.endNodeIndex));
 				} else {
 					end.setNodeId(Integer.toString(modelGraphLink.id)
 							+ String.format("%02d", cellID + 1));
 				}
 				link.setBegin(begin);
 				link.setEnd(end);
 			}
 
 			// set link length
 			link.setLength(new BigDecimal(modelGraphLink.getLength()
 					/ totalCellNumber));
 
 			// set number of lanes on link
 			link.setLanes(new BigDecimal(modelGraphLink.getMax_num_lanes()));
 			// TODO: when number of lanes changes, cut link into small links
 
 			// set the fundamental diagram parameters
 			Fd fd = new Fd();
 			// free flow speed (v): approximated with the average speed limit
 			// (originally in the unit of m/s, now in km/hr)
 			float speedLimit = modelGraphLink.getAverageSpeedLimit() * 3.6f;
 			// jam density (kj): 1/6.7 veh/meter (150 veh/km)
 			float densityJam = 150.0f;
 			// speed of back-propagating wave (w): 20 km/hr
 			float waveSpeed = 20.0f;
 			float densityCritical = densityJam * waveSpeed
 					/ (waveSpeed + speedLimit);
 			float flowMax = densityCritical * speedLimit;
 			// DensityJam unit: veh/km
 			fd.setDensityJam(Float.toString(densityJam));
 			// DensityCritical unit: veh/km
 			fd.setDensityCritical(Float.toString(densityCritical));
 			// FlowMax unit: veh/hr
 			fd.setFlowMax(Float.toString(flowMax));
 			link.setFd(fd);
 
 			// set link type
 			// since link type is only used for consistency test, all links
 			// are set as type "FW" (freeway)
 			link.setType("FW");
 
 			links.add(link);
 		}
 		return links;
 	}
 
 	/**
 	 * 
 	 * @param sensorsPeMS
 	 *            an array of SensorPeMS objects to be parsed into TOPL format
 	 * @return a SensorList object with Sensor objects representing PeMS
 	 *         stations
 	 */
 	public static SensorList parsePeMSSensors(
 			netconfig.SensorPeMS[] sensorsPeMS, boolean flagSubdivisionLink) {
 
 		Monitor.out(Util.LINE);
 		SensorList sensorList = new SensorList();
 		for (int i = 0; i < sensorsPeMS.length; i++) {
 
 			Sensor sensor = parseSinglePeMSSensor(sensorsPeMS[i],
 					flagSubdivisionLink);
 
 			// add sensor to the list
 			sensorList.getSensor().add(sensor);
 		}
 
 		return sensorList;
 	}
 
 	/**
 	 * <p>
 	 * Convert a netconfig.SensorPeMS object into a Sensor object in TOPL
 	 * format, except for which link it is on. This link will be set in either
 	 * parsePeMSSensors or parsePeMSSensorsSubdivide, depending on whether links
 	 * need to be subdivided and renamed.
 	 * </p>
 	 * 
 	 * @param sensorPeMS
 	 *            a netconfig.SensorPeMS object for a single PeMS station
 	 * @return a Sensor object in TOPL format
 	 */
 	public static Sensor parseSinglePeMSSensor(netconfig.SensorPeMS sensorPeMS,
 			boolean flagSubdivisionLink) {
 
 		// instantiate a Sensor object
 		Sensor sensor = new Sensor();
 
 		// set sensor id
 		sensor.setId(Integer.toString(sensorPeMS.ID));
 
 		// set sensor type
 		// other types (radar, camera, sensys) not used here
 		sensor.setType("loop");
 
 		// set link type to "FW" (freeway)
 		sensor.setLinkType("FW");
 
 		// set sensor description
 		sensor.setDescription(sensorPeMS.name);
 
 		// set sensor position (and display position)
 		Position position = new Position();
 		DisplayPosition displayPosition = new DisplayPosition();
 		Point point = new Point();
 		try {
 			point.setLat(new BigDecimal(sensorPeMS.toCoordinate().lat));
 			point.setLng(new BigDecimal(sensorPeMS.toCoordinate().lon));
 		} catch (NetconfigException e) {
 			e.printStackTrace();
 			Monitor.err("failed to find location of PeMS station "
 					+ sensorPeMS.vdsID);
 		}
 		position.getPoint().add(point);
 		sensor.setPosition(position);
 		displayPosition.getPoint().add(point);
 		sensor.setDisplayPosition(displayPosition);
 
 		// set the link id that sensor is on
 		Links links = new Links();
 		if (!flagSubdivisionLink) {
 			links.setContent(Integer.toString(sensorPeMS.link.id));
 		} else {
 			int cellID;
 			// calculating cellID based on offset
 			if (sensorPeMS.offset == sensorPeMS.link.length) {
 				cellID = sensorPeMS.link.nbCells - 1;
 			} else {
 				cellID = (int) Math.floor(sensorPeMS.offset
 						/ sensorPeMS.link.length * sensorPeMS.link.nbCells);
 			}
 			// adding a 2-digit cellID to the end of linkID
 			// is equivalent to (linkid * 100 + cellID)
 			links.setContent(Integer.toString(sensorPeMS.link.id)
 					+ String.format("%02d", cellID));
 		}
 		sensor.setLinks(links);
 
 		// set the parameters
 		Parameters parameters = new Parameters();
 
 		Parameter parameter_hwy_dir = new Parameter();
 		parameter_hwy_dir.setName("hwy_dir");
 		parameter_hwy_dir.setValue(sensorPeMS.freewayDir);
 		parameters.getParameter().add(parameter_hwy_dir);
 
 		Parameter parameter_hwy_name = new Parameter();
 		parameter_hwy_name.setName("hwy_name");
 		parameter_hwy_name.setValue(Integer.toString(sensorPeMS.freewayID));
 		parameters.getParameter().add(parameter_hwy_name);
 
 		Parameter parameter_postmile = new Parameter();
 		parameter_postmile.setName("postmile");
 		parameter_postmile.setValue(Float.toString(sensorPeMS.absPM));
 		parameters.getParameter().add(parameter_postmile);
 
 		Parameter parameter_vds = new Parameter();
 		parameter_vds.setName("vds");
 		parameter_vds.setValue(Integer.toString(sensorPeMS.vdsID));
 		parameters.getParameter().add(parameter_vds);
 		// Monitor.out("VDS: " + sensorsPeMS[i].vdsID);
 
 		Parameter parameter_data_id = new Parameter();
 		parameter_data_id.setName("data_id");
 		parameter_data_id.setValue(Integer.toString(sensorPeMS.vdsID));
 		parameters.getParameter().add(parameter_data_id);
 
 		Parameter parameter_lanes = new Parameter();
 		parameter_lanes.setName("lanes");
 		parameter_lanes.setValue(Integer.toString(sensorPeMS.lanes));
 		int linkLanes = sensorPeMS.link.getMax_num_lanes().intValue();
 		if (sensorPeMS.lanes != linkLanes) {
 			parameter_lanes.setValue(Integer.toString(linkLanes));
 			Monitor.out("Lane number mismatch: vds = " + sensorPeMS.vdsID
 					+ ", linkID = " + sensorPeMS.link.id
 					+ ", setting sensorLanes to " + linkLanes
 					+ " according to linkLanes instead of " + sensorPeMS.lanes);
 		}
 		parameters.getParameter().add(parameter_lanes);
 
 		Parameter parameter_offset_in_link = new Parameter();
 		parameter_offset_in_link.setName("offset_in_link");
 		parameter_offset_in_link.setValue(Float.toString(sensorPeMS.offset));
 		parameters.getParameter().add(parameter_offset_in_link);
 
 		sensor.setParameters(parameters);
 
 		// set data_sources
 		DataSources dataSources = new DataSources();
 		Source source = new Source();
 		// set the dates to Jan 28-29, 2009 for the Dowling data collection
 		source.setUrl("pems:d4, Jan 28, 2009");
 		// dt is always set to 300 seconds
 		source.setDt(new BigDecimal(300));
 		// from "PeMS Data Clearinghouse", instead of "Caltrans DBX" or
 		// "BHL"
 		source.setFormat("PeMS Data Clearinghouse");
 		dataSources.getSource().add(source);
 		sensor.setDataSources(dataSources);
 
 		return sensor;
 	}
 
 	/**
 	 * 
 	 * <p>
 	 * The purpose of the method is because the TOPL simulation right now
 	 * (Aurora) requires each terminal node to be associated with only one link
 	 * </p>
 	 * 
 	 * <p>
 	 * The method looks through the list of terminal nodes and identify those
 	 * that are associated with multiple links. The method does the following:
 	 * (1) duplicate terminal nodes as necessary so that links do not share
 	 * terminal nodes; (2) change begin/end node of corresponding links; (3) add
 	 * duplicate nodes to terminalNodesID. Note that in TOPL simulation,
 	 * terminal nodes cannot have inputs or outputs elements (therefore, nothing
 	 * needs to be done there).
 	 * </p>
 	 * 
 	 * @param nodeList
 	 *            raw info from ModelGraphNode, will be changed, also the output
 	 * @param linkList
 	 *            raw info from ModelGraphLink, will be changed, also the output
 	 * @param netHighway
 	 *            network object from model graph
 	 * @param terminalNodesID
 	 *            an ArrayList of Integers with node id for terminal nodes, will
 	 *            be changed, also the output
 	 * 
 	 * @throws NetconfigException
 	 */
 	public static void oneLinkPerTerminalNode(NodeList nodeList,
 			LinkList linkList, netconfig.Network netHighway,
 			ArrayList<Integer> terminalNodesID) throws NetconfigException {
 
 		int length = terminalNodesID.size();
 		for (int i = 0; i < length; i++) { // loop through all terminal nodes
 			ModelGraphNode node = netHighway.getNodeWithID(terminalNodesID
 					.get(i));
 
 			ModelGraphLink[] inLinks = node.getInLinks();
 			// if a terminal node has multiple inLinks
 			if (inLinks.length > 1) {
 				// identify the node in nodeList (tnode)
 				int tNodeIndex = findNodeInNodeList(nodeList, node.id);
 
 				// run the following (inLinks.length - 1) times for each
 				// duplicate node
 				for (int k = 1; k < inLinks.length; k++) {
 					// duplicate a node in nodeList, and set id for duplicate
 					// node, prefix its id with a pattern: "199", "299", ...
 					Node tNode = cloneNode(nodeList.getNode().get(tNodeIndex));
 					String newNodeID = k + "99" + node.id;
 					Monitor.out("adding node: " + newNodeID);
 					tNode.setId(newNodeID);
 					nodeList.getNode().add(tNode);
 
 					// change nodeID of end node of corresponding links
 					int tLinkIndex = findLinkInLinkList(linkList, inLinks[k].id);
 					linkList.getLink().get(tLinkIndex).getEnd()
 							.setNodeId(newNodeID);
 
 					// add duplicate node to terminalNodesID
 					terminalNodesID.add(Integer.valueOf(newNodeID));
 				}
 			}
 
 			ModelGraphLink[] outLinks = node.getOutLinks();
 			// if a terminal node has multiple outLinks
 			if (outLinks.length > 1) {
 				// identify the node in nodeList (tnode)
 				int tNodeIndex = findNodeInNodeList(nodeList, node.id);
 
 				// run the following (outLinks.length - 1) times for each
 				// duplicate node
 				for (int k = 1; k < outLinks.length; k++) {
 					// duplicate a node in nodeList, and set id for duplicate
 					// node, prefix its id with a pattern: "199", "299", ...
 					Node tNode = cloneNode(nodeList.getNode().get(tNodeIndex));
 					String newNodeID = k + "99" + node.id;
 					Monitor.out("adding node: " + newNodeID);
 					tNode.setId(newNodeID);
 					nodeList.getNode().add(tNode);
 
 					// change nodeID of end node of corresponding links
 					int tLinkIndex = findLinkInLinkList(linkList,
 							outLinks[k].id);
 					linkList.getLink().get(tLinkIndex).getBegin()
 							.setNodeId(newNodeID);
 
 					// add duplicate node to terminalNodesID
 					terminalNodesID.add(Integer.valueOf(newNodeID));
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param nodeList
 	 *            to search from
 	 * @param nodeID
 	 *            the id of the desired node
 	 * @return the index of the node (nodeIndex) so that
 	 *         nodeList.getNode().get(nodeIndex).getId()
 	 *         .equals(Integer.toString(nodeID)).
 	 */
 	public static int findNodeInNodeList(NodeList nodeList, int nodeID) {
 		int nodeIndex = -1;
 		for (int i = 0; i < nodeList.getNode().size(); i++) {
 			if (nodeList.getNode().get(i).getId()
 					.equals(Integer.toString(nodeID))) {
 				nodeIndex = i;
 				break;
 			}
 		}
 		return nodeIndex;
 	}
 
 	/**
 	 * 
 	 * @param linkList
 	 *            to search from
 	 * @param linkID
 	 *            the id of the desired link
 	 * @return the index of the link (linkIndex) so that
 	 *         linkList.getLink().get(linkIndex).getId()
 	 *         .equals(Integer.toString(linkID)).
 	 */
 	public static int findLinkInLinkList(LinkList linkList, int linkID) {
 		int linkIndex = -1;
 		for (int i = 0; i < linkList.getLink().size(); i++) {
 			if (linkList.getLink().get(i).getId()
 					.equals(Integer.toString(linkID))) {
 				linkIndex = i;
 				break;
 			}
 		}
 		return linkIndex;
 
 	}
 
 	/**
 	 * 
 	 * @param node
 	 *            a node from nodeList
 	 * @return a cloned node (not pointing to the same memory)
 	 */
 	public static Node cloneNode(Node node) {
 		Node node2 = new Node();
 		node2.setDescription(node.getDescription());
 		node2.setId(node.getId());
 		node2.setInputs(node.getInputs());
 		node2.setName(node.getName());
 		node2.setOutputs(node.getOutputs());
 		node2.setPosition(node.getPosition());
 		node2.setPostmile(node.getPostmile());
 		node2.setType(node.getType());
 		return node2;
 	}
 
 	public static boolean sanityCheckTOPLConnectivity(NodeList nodeList,
 			LinkList linkList) {
 
 		boolean flag = true;
 
 		for (Node n : nodeList.getNode()) {
 			for (Input input : n.getInputs().getInput()) {
 				int index = findLinkInLinkList(linkList,
 						Integer.parseInt(input.getLinkId()));
 				if (!linkList.getLink().get(index).getEnd().getNodeId()
 						.equals(n.getId())) {
 					Monitor.err("Link-Node mismatch -- node: " + n.getId()
 							+ ", link: "
 							+ linkList.getLink().get(index).getId());
 					flag = false;
 				}
 			}
 			for (Output output : n.getOutputs().getOutput()) {
 				int index = findLinkInLinkList(linkList,
 						Integer.parseInt(output.getLinkId()));
 				if (!linkList.getLink().get(index).getBegin().getNodeId()
 						.equals(n.getId())) {
 					Monitor.err("Link-Node mismatch -- node: " + n.getId()
 							+ ", link: "
 							+ linkList.getLink().get(index).getId());
 					flag = false;
 				}
 			}
 		}
 
 		return flag;
 
 	}
 
 	public static boolean sanityCheckTOPLSensors(LinkList linkList,
 			SensorList sensorList) {
 
 		boolean flag = true;
 
 		for (Sensor s : sensorList.getSensor()) {
 			int index = findLinkInLinkList(linkList,
 					Integer.parseInt(s.getLinks().getContent()));
 			if (index == -1) {
 				Monitor.err("Sensor-Link mismatch -- link: "
 						+ linkList.getLink().get(index).getId() + ", sensor: "
 						+ s.getId());
 				flag = false;
 			}
 		}
 
 		return flag;
 	}
 
 	public static boolean sanityCheckTOPLNodeUnique(NodeList nodeList) {
 
 		HashSet<Integer> nodesID = new HashSet<Integer>();
 		for (Node n : nodeList.getNode()) {
 			int s = nodesID.size();
 			nodesID.add(Integer.parseInt(n.getId()));
 			if (nodesID.size() == s) {
 				Monitor.err("Duplicate node id: " + n.getId());
 			}
 		}
 
 		if (nodeList.getNode().size() == nodesID.size()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean sanityCheckTOPLLinkUnique(LinkList linkList) {
 
 		HashSet<Integer> linksID = new HashSet<Integer>();
 		for (Link l : linkList.getLink()) {
 			int s = linksID.size();
 			linksID.add(Integer.parseInt(l.getId()));
 			if (linksID.size() == s) {
 				Monitor.err("Duplicate link id: " + l.getId());
 			}
 		}
 
 		if (linkList.getLink().size() == linksID.size()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean sanityCheckTOPLLaneNumberMatch(LinkList linkList,
 			SensorList sensorList) {
 
 		boolean flag = true;
 
 		for (Sensor s : sensorList.getSensor()) {
 			int sensorLanes = 0;
 			int vdsID = 0;
 			for (Parameter p : s.getParameters().getParameter()) {
 				if (p.getName().equalsIgnoreCase("lanes")) {
 					sensorLanes = Integer.parseInt(p.getValue());
 				}
 				if (p.getName().equalsIgnoreCase("vds")) {
 					vdsID = Integer.parseInt(p.getValue());
 				}
 			}
 			int linkID = Integer.parseInt(s.getLinks().getContent());
 			int index = findLinkInLinkList(linkList, linkID);
 			int linkLanes = linkList.getLink().get(index).getLanes().intValue();
 			if (sensorLanes != linkLanes) {
 				Monitor.err("Lane number does not match: link = " + linkID
 						+ ", PeMS VDS = " + vdsID + ", linkLanes = "
 						+ linkLanes + ", sensorLanes = " + sensorLanes);
 				flag = false;
 			}
 		}
 
 		return flag;
 	}
 
 	public static boolean sanityCheckTOPLCFLCondition(LinkList linkList,
 			BigDecimal dt) {
 
 		Monitor.out(Util.LINE);
 
 		boolean flag = true;
 
 		for (Link link : linkList.getLink()) {
 			float flowMax = Float.parseFloat(link.getFd().getFlowMax());
 			float densityCritical = Float.parseFloat(link.getFd()
 					.getDensityCritical());
 			float speedLimit = flowMax / densityCritical;
 			float minLinkLength = speedLimit / 3.6f * dt.floatValue();
 			if (link.getLength().floatValue() < minLinkLength) {
 				Monitor.err("Short link: link = " + link.getId() + ", type = "
 						+ link.getType() + ", length = " + link.getLength()
 						+ ", min length = " + minLinkLength);
 				flag = false;
 			}
 		}
 		return flag;
 	}
 
 	/**
 	 * When insert terminal nodes due to ramps, there are sometimes nodes
 	 * connected to both on-ramp to the freeway and also off-ramp from the
 	 * freeway. Also, the node appears twice in the nodeList. The solution is to
 	 * rename the one of the node id, and also update the link begin/end node.
 	 * 
 	 * @param nodeList
 	 */
 	public static void renameDuplicateNodes(NodeList nodeList, LinkList linkList) {
 
 		HashSet<Integer> nodesID = new HashSet<Integer>();
 		for (int i = 0; i < nodeList.getNode().size(); i++) {
 			int s = nodesID.size();
 			String nodeID = nodeList.getNode().get(i).getId();
 			nodesID.add(Integer.parseInt(nodeID));
 			if (nodesID.size() == s) {
 				nodeList.getNode().get(i).setId("199" + nodeID);
 				int linkID = Util.findListInLinkListByEndNode(linkList,
 						Integer.parseInt(nodeID));
 				End end = new End();
 				end.setNodeId("199" + nodeID);
 				linkList.getLink().get(linkID).setEnd(end);
 			}
 		}
 	}
 
 	public static int findListInLinkListByBeginNode(LinkList linkList,
 			int nodeID) {
 
 		for (int i = 0; i < linkList.getLink().size(); i++) {
 			if (Integer.parseInt(linkList.getLink().get(i).getBegin()
 					.getNodeId()) == nodeID) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public static int findListInLinkListByEndNode(LinkList linkList, int nodeID) {
 
 		for (int i = 0; i < linkList.getLink().size(); i++) {
 			if (Integer
 					.parseInt(linkList.getLink().get(i).getEnd().getNodeId()) == nodeID) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * For all the on-ramps and off-ramps just added, also need to update the
 	 * input and output links of existing nodes on freeway
 	 * 
 	 * @param nodeList
 	 * @param linkList
 	 */
 	public static void updateExistingNodesInputOutput(NodeList nodeList,
 			LinkList linkList) {
 
 		for (Link link : linkList.getLink()) {
 			if (link.getType().equals("OR")) {
 				int nodeID = Integer.parseInt(link.getEnd().getNodeId());
 				int nodeIndex = Util.findNodeInNodeList(nodeList, nodeID);
 				Input input = new Input();
 				input.setLinkId(link.getId());
 				nodeList.getNode().get(nodeIndex).getInputs().getInput()
 						.add(input);
 			} else if (link.getType().equals("FR")) {
 				int nodeID = Integer.parseInt(link.getBegin().getNodeId());
 				int nodeIndex = Util.findNodeInNodeList(nodeList, nodeID);
 				Output output = new Output();
 				output.setLinkId(link.getId());
 				nodeList.getNode().get(nodeIndex).getOutputs().getOutput()
 						.add(output);
 			}
 		}
 	}
 }
