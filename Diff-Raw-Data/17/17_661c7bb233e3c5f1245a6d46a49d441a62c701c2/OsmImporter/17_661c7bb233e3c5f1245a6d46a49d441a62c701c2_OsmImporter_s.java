 package edu.uaskl.cpp.importer;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeMap;
 
 import edu.uaskl.cpp.map.meta.WayNodeOSM;
 import edu.uaskl.cpp.map.meta.WayOSM;
 import edu.uaskl.cpp.model.edge.EdgeCppOSM;
 import edu.uaskl.cpp.model.edge.EdgeCppOSMDirected;
 import edu.uaskl.cpp.model.graph.GraphDirected;
 import edu.uaskl.cpp.model.graph.GraphUndirected;
 import edu.uaskl.cpp.model.node.NodeCppOSM;
 import edu.uaskl.cpp.model.node.NodeCppOSMDirected;
 import edu.uaskl.cpp.model.path.PathExtended;
 
 import javax.xml.stream.*;
 
 public class OsmImporter {
 
 	/**
 	 * Returns the distance between the two waypoints in meters base on the
 	 * Spherical Law of Cosines.
 	 * www.movable-type.co.uk/scripts/latlong.html
 	 * @param a a waypoint
 	 * @param b another waypoint
 	 * @return distance in meter
 	 */
 	private static double getDistance(final WayNodeOSM a, final WayNodeOSM b) {
 		double latA = a.getLatitude()/180*Math.PI;
 		double latB = b.getLatitude()/180*Math.PI;
 		double lonDif = (b.getLongitude() - a.getLongitude())/180*Math.PI;
 		return (Math.acos((Math.sin(latA) * Math.sin(latB)) + (Math.cos(latA) * Math.cos(latB) * Math.cos(lonDif))) * 6367500);
 	}
 	private static double getDistance(final WayNodeOSM a, final NodeCppOSM b) {
 		double latA = a.getLatitude()/180*Math.PI;
 		double latB = b.lat/180*Math.PI;
 		double lonDif = (b.lon - a.getLongitude())/180*Math.PI;
 		return (Math.acos((Math.sin(latA) * Math.sin(latB)) + (Math.cos(latA) * Math.cos(latB) * Math.cos(lonDif))) * 6367500);
 	}
 	
 	
 
 	/**
 	 * Returns the potentially unconnected graph based on the specified osm file.
 	 * @param filename filename including the path to the osm file
 	 * @return potentially unconnected graph
 	 */
 	public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importOsmUndirected(final String filename) {
 		// setting up
 		GraphUndirected<NodeCppOSM, EdgeCppOSM> osmGraph = new GraphUndirected<>();
 		FileReader fis = null;
 		HashMap<Long, WayNodeOSM> osmNodes = new HashMap<>();
 		try {
 			// create a StAX XML parser
 			fis = new FileReader(filename);
 			XMLInputFactory factory = XMLInputFactory.newInstance();
 			XMLStreamReader parser = factory.createXMLStreamReader(fis);
 			// go through all events
 			for (int event = parser.next();  
 					event != XMLStreamConstants.END_DOCUMENT;
 					event = parser.next()){
 				if (event == XMLStreamConstants.START_ELEMENT) {
 					//we are only interested in node|way elements
 					if (parser.getLocalName().equals("node")) {
 						// get the node and store it
 						WayNodeOSM node = processNode(parser);
 						osmNodes.put(node.getID(), node);
 					}
 					else {
 						if (parser.getLocalName().equals("way")) {
 							// get the way and add it to the graph
 							// this also creates all the included nodes
 							OSMWay way = processWay(parser,osmNodes);
 							addWay(osmGraph,osmNodes,way);
 						}
 					}
 				}
 			}
 			// tear down the parser
 			parser.close();
 			parser = null;
 			factory = null;
 		}
 		catch (IOException e){
 			System.out.println("IOExcpetions");
 			System.exit(0);
 		} catch (XMLStreamException e) {
 			System.out.println("XMLStreamExcpetions");
 			System.exit(0);
 		}
 		if(fis!=null){
 			try {
 				fis.close();
 			} catch (IOException e) {
 				System.exit(0);
 			} 
 		}
 		// reduce the number of nodes
 		return osmGraph;
 	}
 
 	/**
 	 * Returns the potentially unconnected directed graph based on the specified osm file.
 	 * @param filename filename including the path to the osm file
 	 * @return potentially unconnected directed graph
 	 */
 	public static GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> importOsmDirected(final String filename) {
 		// setting up
 		GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> osmGraph = new GraphDirected<>();
 		FileReader fis = null;
 		HashMap<Long, WayNodeOSM> osmNodes = new HashMap<>();
 		try {
 			// create a StAX XML parser
 			fis = new FileReader(filename);
 			XMLInputFactory factory = XMLInputFactory.newInstance();
 			XMLStreamReader parser = factory.createXMLStreamReader(fis);
 			// go through all events
 			for (int event = parser.next();  
 					event != XMLStreamConstants.END_DOCUMENT;
 					event = parser.next()){
 				if (event == XMLStreamConstants.START_ELEMENT) {
 					//we are only interested in node|way elements
 					if (parser.getLocalName().equals("node")) {
 						// get the node and store it
 						WayNodeOSM node = processNode(parser);
 						osmNodes.put(node.getID(), node);
 					}
 					else {
 						if (parser.getLocalName().equals("way")) {
 							// get the way and add it to the graph
 							// this also creates all the included nodes
 							OSMWay way = processWay(parser,osmNodes);
 							addWay(osmGraph,osmNodes,way);
 						}
 					}
 				}
 			}
 			// tear down the parser
 			parser.close();
 			parser = null;
 			factory = null;
 		}
 		catch (IOException e){
 			System.out.println("IOExcpetions");
 			System.exit(0);
 		} catch (XMLStreamException e) {
 			System.out.println("XMLStreamExcpetions");
 			System.exit(0);
 		}
 		if(fis!=null){
 			try {
 				fis.close();
 			} catch (IOException e) {
 				System.exit(0);
 			} 
 		}
 		// reduce the number of nodes
 		simplify(osmGraph);
 		return osmGraph;
 	}
 
 	/**
 	 * Simplifies the graph in place by removing all nodes with degree 2 and adding them as metanodes to the edges.
 	 * @param osmGraph the graph to modify
 	 */
 	private static void simplify (GraphUndirected<NodeCppOSM, EdgeCppOSM> osmGraph) {
 		final Iterator<NodeCppOSM> iteratorNodes = osmGraph.getNodes().iterator();
 		while (iteratorNodes.hasNext()) {
 			final NodeCppOSM node = iteratorNodes.next();
 			if (node.getDegree() == 2) {
 				// get the ids of the two connected nodes
 				final Long currentNodeId = node.getId();
 				final List<EdgeCppOSM> edges = node.getEdges();
 				final EdgeCppOSM edge1 = edges.get(0);
 				final EdgeCppOSM edge2 = edges.get(1);
 				final Long node1id = edge1.getNode1().getId() == (currentNodeId) ? edge1.getNode2().getId() : edge1.getNode1().getId();
 				final Long node2id = edge2.getNode1().getId() == (currentNodeId) ? edge2.getNode2().getId() : edge2.getNode1().getId();
 				if (currentNodeId == node1id){
 					// we are in a loop and do not erase ourself
 					continue;
 				}
 				// concat the list in the right way
 				List<WayNodeOSM> newMetaNodes = concatMetanodes(edge1.getMetadata().getNodes(), edge2.getMetadata().getNodes(), currentNodeId);
 				// add a new edge
 				// TODO: Handle way properly - what would name be in this context?
 				// TODO: names
 				osmGraph.getNode(node1id).connectWithNodeWeigthAndMeta(osmGraph.getNode(node2id), edge1.getWeight() + edge2.getWeight(),
						new WayOSM(0, WayOSM.WayType.UNSPECIFIED, edge1.getMetadata().getName()+edge2.getMetadata().getName(), newMetaNodes));
 				// remove the old node
 				// do this manually because we otherwise corrupt the iterator
 				node.removeAllEdges();
 				iteratorNodes.remove();
 			}
 		}
 	}

 	/**
 	 * Simplifies the graph in place by removing all nodes with one edge incoming and one edge outgoing and adding them as metanodes to the edges.
 	 * @param osmGraph the graph to modify
 	 */
 	private static void simplify (GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> osmGraph) {
 		final Iterator<NodeCppOSMDirected> iteratorNodes = osmGraph.getNodes().iterator();
 		while (iteratorNodes.hasNext()) {
 			final NodeCppOSMDirected node = iteratorNodes.next();
 			// one in one out
 			if (node.getInDegree() == 1 && node.getOutDegree() == 1) {
 				// get the ids of the two connected nodes
 				final Long currentNodeId = node.getId();
 				final List<EdgeCppOSMDirected> edges = node.getEdges();
 				final EdgeCppOSMDirected edge1 = edges.get(0);
 				final EdgeCppOSMDirected edge2 = edges.get(1);
 				final Long node1id = edge1.getNode1().getId() == (currentNodeId) ? edge2.getNode1().getId() : edge1.getNode1().getId();
 				final Long node2id = edge1.getNode1().getId() == (currentNodeId) ? edge1.getNode2().getId() : edge2.getNode2().getId();
 				if (node2id == node1id){
 					// we are in a deadend and do not erase ourself
 					continue;
 				}
 				// concat the list in the right way
 				List<WayNodeOSM> newMetaNodes = concatMetanodes(edge1.getMetadata().getNodes(), edge2.getMetadata().getNodes(), currentNodeId);
 				// add a new edge
 				// TODO: Handle way properly - what would name be in this context?
 				osmGraph.getNode(node1id).connectWithNodeWeigthAndMeta(osmGraph.getNode(node2id), edge1.getWeight() + edge2.getWeight(),
 						new WayOSM(0, WayOSM.WayType.UNSPECIFIED, "unknown", newMetaNodes));
 				// remove the old node
 				// do this manually because we otherwise corrupt the iterator
 				node.removeAllEdges();
 				iteratorNodes.remove();
 				continue;
 			}
 			// two in two out
 			if (node.getInDegree() == 2 && node.getOutDegree() == 2) {
 				final long currentNodeId = node.getId();
 				//check the connections
 				ArrayList<EdgeCppOSMDirected> incomingEdges = new ArrayList<>();
 				ArrayList<EdgeCppOSMDirected> outgoingEdges = new ArrayList<>();
 				for(EdgeCppOSMDirected edge : node.getEdges()) {
 					if(edge.getNode1().getId() == currentNodeId){
 						outgoingEdges.add(edge);
 					}
 					else {
 						incomingEdges.add(edge);
 					}
 				}
 				//TODO make this condition better
 				if(outgoingEdges.get(0).getNode2().equals(incomingEdges.get(0).getNode1())){
 					//out0 = in0
 					if(!outgoingEdges.get(1).getNode2().equals(incomingEdges.get(1).getNode1())){
 						continue;
 					}
 					if(outgoingEdges.get(0).getWeight() != incomingEdges.get(0).getWeight()){
 						continue;
 					}
 					if(outgoingEdges.get(1).getWeight() != incomingEdges.get(1).getWeight()){
 						continue;
 					}
 				}
 				else {
 					//out0 should be in1
 					//therefore out1 = in0
 					if(!outgoingEdges.get(0).getNode2().equals(incomingEdges.get(1).getNode1())){
 						continue;
 					}
 					if(!outgoingEdges.get(1).getNode2().equals(incomingEdges.get(0).getNode1())){
 						continue;
 					}
 					if(outgoingEdges.get(0).getWeight() != incomingEdges.get(1).getWeight()){
 						continue;
 					}
 					if(outgoingEdges.get(1).getWeight() != incomingEdges.get(0).getWeight()){
 						continue;
 					}
 				}
 
 				// get the ids of the two connected nodes
 				final NodeCppOSMDirected node1 = incomingEdges.get(0).getNode1();
 				final NodeCppOSMDirected node2 = incomingEdges.get(1).getNode1();
 				//    			if (node1.equals(node2)){
 				//    				// we are in a loop and do not erase ourself
 				//    				continue;
 				//    			}
 				// concat the list in the right way
 				List<WayNodeOSM> metaNodes1 = incomingEdges.get(0).getMetadata().getNodes();
 				List<WayNodeOSM> metaNodes2 = incomingEdges.get(1).getMetadata().getNodes();
 				double weight = incomingEdges.get(0).getWeight() +incomingEdges.get(1).getWeight();
 				List<WayNodeOSM> newMetaNodes = concatMetanodes(metaNodes1, metaNodes2, currentNodeId);
 				// add a new edge
 				// TODO: Handle way properly - what would name be in this context?
 				node1.connectWithNodeWeigthAndMeta(node2, weight,
 						new WayOSM(0, WayOSM.WayType.UNSPECIFIED, "unknown", newMetaNodes));
 				node2.connectWithNodeWeigthAndMeta(node1, weight,
 						new WayOSM(0, WayOSM.WayType.UNSPECIFIED, "unknown", newMetaNodes));
 				// remove the old node
 				// do this manually because we otherwise corrupt the iterator
 				node.removeAllEdges();
 				iteratorNodes.remove();
 			}
 		}
 	}
 
 	/**
 	 * Concats the meta nodes of the two edged to be merged.
 	 * @param metaNodes1 meta nodes from the first edge
 	 * @param metaNodes2 meta nodes from the second edge
 	 * @param duplicateNode the node id of the node connected to both edges
 	 * @return the meta nodes of the new edge
 	 */
 	private static List<WayNodeOSM> concatMetanodes(List<WayNodeOSM>metaNodes1, List<WayNodeOSM>metaNodes2, long duplicateNode) {
 		// turn the lists the right way round
 		if ( metaNodes1.get(0).getID() == duplicateNode){
 			Collections.reverse(metaNodes1);
 		}
 		if (!( metaNodes2.get(0).getID() == duplicateNode)) {
 			Collections.reverse(metaNodes2);
 		}
 		// remove the duplicate, then concat
 		metaNodes1.remove(metaNodes1.size() - 1);
 		metaNodes1.addAll(metaNodes2);
 		return metaNodes1;
 	}
 
 	/**
 	 * Adds the way to the graph by connecting all nodes specified in the way.
 	 * @param osmGraph graph to modify
 	 * @param osmNodes hashmap of id->node
 	 * @param way a way carrying a list of ids
 	 */
 	private static void addWay(GraphUndirected<NodeCppOSM, EdgeCppOSM> osmGraph,HashMap<Long, WayNodeOSM> osmNodes, OSMWay way) {
 		for (int j = 1; j < way.nodes.size(); ++j) {
 			// get the star and end node of this segment of the way 
 			final Long startNodeId = way.nodes.get(j - 1);
 			final Long endNodeId = way.nodes.get(j);
 			NodeCppOSM node1 = createNodeIfNonexisting(osmGraph, startNodeId);
 			node1.lat = osmNodes.get(startNodeId).getLatitude();
 			node1.lon = osmNodes.get(startNodeId).getLongitude();
 			NodeCppOSM node2 = createNodeIfNonexisting(osmGraph, endNodeId);
 			node2.lat = osmNodes.get(endNodeId).getLatitude();
 			node2.lon = osmNodes.get(endNodeId).getLongitude();
 			// create the meta nodes for this segment
 			final List<WayNodeOSM> metaNodes = new LinkedList<>();
 			metaNodes.add(osmNodes.get(startNodeId));
 			metaNodes.add(osmNodes.get(endNodeId));
 			// for roundabouts use distance 0, so we can run around in circles at no cost
 //			double distance = way.roundabout ? 0 : getDistance(osmNodes.get(startNodeId),osmNodes.get(endNodeId));
 			double distance = getDistance(osmNodes.get(startNodeId),osmNodes.get(endNodeId));
 			osmGraph.getNode(startNodeId).connectWithNodeWeigthAndMeta(osmGraph.getNode(endNodeId), distance,
 					new WayOSM(0, WayOSM.WayType.UNSPECIFIED, way.name, metaNodes));
 		}
 	}
 
 	/**
 	 * Adds the way to the graph by connecting all nodes specified in the way.
 	 * @param osmGraph graph to modify
 	 * @param osmNodes hashmap of id->node
 	 * @param way a way carrying a list of ids
 	 */
 	private static void addWay(GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> osmGraph,HashMap<Long, WayNodeOSM> osmNodes, OSMWay way) {
 		for (int j = 1; j < way.nodes.size(); ++j) {
 			// get the star and end node of this segment of the way 
 			final Long startNodeId = way.nodes.get(j - 1);
 			final Long endNodeId = way.nodes.get(j);
 			createNodeIfNonexisting(osmGraph, startNodeId);
 			createNodeIfNonexisting(osmGraph, endNodeId);
 			// create the meta nodes for this segment
 			final List<WayNodeOSM> metaNodes = new LinkedList<>();
 			metaNodes.add(osmNodes.get(startNodeId));
 			metaNodes.add(osmNodes.get(endNodeId));
 			// for roundabouts use distance 0, so we can run around in circles at no cost
 			double distance = way.roundabout ? 0 : getDistance(osmNodes.get(startNodeId),osmNodes.get(endNodeId));
 			osmGraph.getNode(startNodeId).connectWithNodeWeigthAndMeta(osmGraph.getNode(endNodeId), distance,
 					new WayOSM(0, WayOSM.WayType.UNSPECIFIED, way.name, metaNodes));
 			if(!way.directed && (startNodeId != endNodeId)) {
 				//a loop must not be doubled, no need for it
 				osmGraph.getNode(endNodeId).connectWithNodeWeigthAndMeta(osmGraph.getNode(startNodeId), distance,
 						new WayOSM(0, WayOSM.WayType.UNSPECIFIED, way.name, metaNodes));
 			}
 		}
 	}
 
 	/**
 	 * Makes sure that the specified node is existing.
 	 * @param osmGraph graph to be modified
 	 * @param nodeId id of the node to possibly create
 	 * @return 
 	 */
 	private static NodeCppOSM createNodeIfNonexisting(
 			GraphUndirected<NodeCppOSM, EdgeCppOSM> osmGraph,
 			final Long nodeId) {
 		if (osmGraph.getNode(nodeId) == null) {
 			final NodeCppOSM newNode = new NodeCppOSM(nodeId);
 			newNode.setName(""+nodeId);
 			osmGraph.addNode(newNode);
 		}
 		return osmGraph.getNode(nodeId);
 	}
 
 	/**
 	 * Makes sure that the specified node is existing.
 	 * @param osmGraph graph to be modified
 	 * @param nodeId id of the node to possibly create
 	 */
 	private static void createNodeIfNonexisting(
 			GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> osmGraph,
 			final Long nodeId) {
 		if (osmGraph.getNode(nodeId) == null) {
 			final NodeCppOSMDirected newNode = new NodeCppOSMDirected(nodeId);
 			newNode.setName(""+nodeId);
 			osmGraph.addNode(newNode);
 		}
 	}
 
 	/**
 	 * During parsing, processes a way element by adding all nodes, the name and whether it is a roundabout or directed to a OSMWay.
 	 * @param parser XML parser
 	 * @param osmNodes hashmap of id->node
 	 * @return the OSMWay (list of ids) specified in this XML element
 	 * @throws XMLStreamException
 	 */
 	private static OSMWay processWay(XMLStreamReader parser,HashMap<Long, WayNodeOSM>  osmNodes) throws XMLStreamException {
 		List<Long> nodeIds = new ArrayList<>();
 		boolean roundabout = false;
 		boolean directed = false;
 		String name = "";
 		while(true){
 			int event = parser.next();
 			if ( event == XMLStreamConstants.END_ELEMENT && parser.getLocalName().equals("way")) {
 				// the XML way elements ends, so create the OSMWay and let the caller resume
 				break;
 			}
 			if (event == XMLStreamConstants.START_ELEMENT){
 				Long id;
 				switch(parser.getLocalName()){
 				case "nd":
 					// get the referenced node and if it exists in the data set add it to the way
 					id = getNdId(parser);
 					if(osmNodes.containsKey(id)){
 						nodeIds.add(id);
 					}
 					break;
 				case "tag":
 					// search the tag
 					boolean j = false; // no junction 
 					boolean n = false; // no name
 					boolean d = false; // not directed
 					for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
 						if (parser.getAttributeLocalName( i ).equals("k")) {
 							// check whether the key is either junction|name
 							if (parser.getAttributeValue(i).equals("junction")){
 								j = true;
 							}
 							if (parser.getAttributeValue(i).equals("name")) {
 								n = true;
 							}
 							if (parser.getAttributeValue(i).equals("oneway")) {
 								d = true;
 							}
 						}
 						// if the key was junction|name, get the value
 						if (j) {
 							if (parser.getAttributeLocalName( i ).equals("v")) {
 								roundabout = parser.getAttributeValue(i) == "roundabout";
 								directed = true;
 								break;
 							}
 						}
 						if (n) {
 							if (parser.getAttributeLocalName( i ).equals("v")) {
 								name = parser.getAttributeValue(i);
 								break;
 							}
 						}
 						if (d) {
 							if (parser.getAttributeLocalName( i ).equals("v")) {
 								directed = parser.getAttributeValue(i).equals("yes");
 								break;
 							}
 						}
 					}
 					break;
 				}
 			}
 		}   	
 
 		return new OSMWay(nodeIds, roundabout,name,directed);
 	}
 
 	/**
 	 * During parsing, in a nd element get the referenced node id. 
 	 * @param parser XML parser
 	 * @return the referenced node id
 	 */
 	private static Long getNdId(XMLStreamReader parser) {
 		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
 			if(parser.getAttributeLocalName( i ).equals("ref")){	
 				return Long.parseLong(parser.getAttributeValue(i), 10);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Parses a node element in the xml file.
 	 * @param parser XML parser
 	 * @return WayNodeOsm with id,lat,lon
 	 * @throws XMLStreamException
 	 */
 	private static WayNodeOSM processNode(XMLStreamReader parser) throws XMLStreamException {
 		long id=0l;
 		double lat=0, lon=0;
 		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
 			switch(parser.getAttributeLocalName( i )){
 			case "id":
 				id = Long.parseLong(parser.getAttributeValue(i), 10);
 				break;
 			case "lat":
 				lat = Double.parseDouble(parser.getAttributeValue(i));
 				break;
 			case "lon":
 				lon = Double.parseDouble(parser.getAttributeValue(i));
 				break;
 			}
 		}
 		return new WayNodeOSM(lat, lon, id);
 	}
 
 	/**
 	 * @param nodeId a nodeId from the connected subgraph
 	 * @param fileName file name including path to the osm file to import
 	 * @return a connected subgraph imported from the osm file including the specified node
 	 */
 	public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importClean(long nodeId,String fileName){
 		GraphUndirected<NodeCppOSM, EdgeCppOSM> graph = importOsmUndirected(fileName);
 		simplify(graph);
 		// mark all nodes connected to the specified node
 		graph.getAlgorithms().visitAllNodesFromStartNode(graph.getNode(nodeId));
 		// remove all unmarked=unconnected nodes
 		Iterator<NodeCppOSM> iter = graph.getNodes().iterator();
 		while(iter.hasNext()){
 			NodeCppOSM node = iter.next();
 			if(!node.isVisited()) {
 				node.removeAllEdges();
 				iter.remove();
 			}
 		}
 		return graph;
 	}
 
 
 	public static List<OSMWay> getWaysFromOSM(final String filename) {
 		// setting up
 		ArrayList<OSMWay> wayList = new ArrayList<>();
 		FileReader fis = null;
 		HashMap<Long, WayNodeOSM> osmNodes = new HashMap<>();
 		try {
 			// create a StAX XML parser
 			fis = new FileReader(filename);
 			XMLInputFactory factory = XMLInputFactory.newInstance();
 			XMLStreamReader parser = factory.createXMLStreamReader(fis);
 			// go through all events
 			for (int event = parser.next();  
 					event != XMLStreamConstants.END_DOCUMENT;
 					event = parser.next()){
 				if (event == XMLStreamConstants.START_ELEMENT) {
 					//we are only interested in node|way elements
 					if (parser.getLocalName().equals("node")) {
 						// get the node and store it
 						WayNodeOSM node = processNode(parser);
 						osmNodes.put(node.getID(), node);
 					}
 					else {
 						if (parser.getLocalName().equals("way")) {
 							// get the way and add it to the graph
 							// this also creates all the included nodes
 							OSMWay way = processWay(parser,osmNodes);
 							wayList.add(way);
 							//    	    				addWay(osmGraph,osmNodes,way);
 						}
 					}
 				}
 			}
 			// tear down the parser
 			parser.close();
 			parser = null;
 			factory = null;
 		}
 		catch (IOException e){
 			System.out.println("IOExcpetions");
 			System.exit(0);
 		} catch (XMLStreamException e) {
 			System.out.println("XMLStreamExcpetions");
 			System.exit(0);
 		}
 		if(fis!=null){
 			try {
 				fis.close();
 			} catch (IOException e) {
 				System.exit(0);
 			} 
 		}
 		// reduce the number of nodes
 		return wayList;
 	}
 
 	public static HashMap<Long, WayNodeOSM> getNodesFromOSM(final String filename) {
 		// setting up
 		FileReader fis = null;
 		HashMap<Long, WayNodeOSM> osmNodes = new HashMap<>();
 		try {
 			// create a StAX XML parser
 			fis = new FileReader(filename);
 			XMLInputFactory factory = XMLInputFactory.newInstance();
 			XMLStreamReader parser = factory.createXMLStreamReader(fis);
 			// go through all events
 			for (int event = parser.next();  
 					event != XMLStreamConstants.END_DOCUMENT;
 					event = parser.next()){
 				if (event == XMLStreamConstants.START_ELEMENT) {
 					//we are only interested in node|way elements
 					if (parser.getLocalName().equals("node")) {
 						// get the node and store it
 						WayNodeOSM node = processNode(parser);
 						osmNodes.put(node.getID(), node);
 					}
 					else {
 						if (parser.getLocalName().equals("way")) {
 							break;
 						}
 					}
 				}
 			}
 			// tear down the parser
 			parser.close();
 			parser = null;
 			factory = null;
 		}
 		catch (IOException e){
 			System.out.println("IOExcpetions");
 			System.exit(0);
 		} catch (XMLStreamException e) {
 			System.out.println("XMLStreamExcpetions");
 			System.exit(0);
 		}
 		if(fis!=null){
 			try {
 				fis.close();
 			} catch (IOException e) {
 				System.exit(0);
 			} 
 		}
 		// reduce the number of nodes
 		return osmNodes;
 	}
 
 
 	/**
 	 * @param nodeId a nodeId from the connected subgraph
 	 * @param fileNameGraph file name including path to the osm file to import
 	 * @param fileNameTrack file name to osm file from UBZ
 	 * @return a connected subgraph imported from the osm file including the specified node
 	 */
 	public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importUBZ(long nodeId,String fileNameGraph, String fileNameTrack){
 		GraphUndirected<NodeCppOSM, EdgeCppOSM> graph = importOsmUndirected(fileNameGraph);
 		//    	simplify(graph);
 		// mark all nodes connected to the specified node
 		graph.getAlgorithms().visitAllNodesFromStartNode(graph.getNode(nodeId));
 		// remove all unmarked=unconnected nodes
 		{
 			Iterator<NodeCppOSM> iter = graph.getNodes().iterator();
 			while(iter.hasNext()){
 				NodeCppOSM node = iter.next();
 				if(!node.isVisited()) {
 					node.removeAllEdges();
 					iter.remove();
 				}
 			}
 			graph.resetStates();
 		}
 		// for each point in the track find the nearest node in OSM
 		boolean first = true;
 		NodeCppOSM previous = null;
 		NodeCppOSM current;
 		List<OSMWay> wayList = getWaysFromOSM(fileNameTrack);
 		HashMap<Long, WayNodeOSM> osmNodes = getNodesFromOSM(fileNameTrack);
 		// right now just use the first way
 		Collection<NodeCppOSM> graphNodes = graph.getNodes();
 		for(int i = 0; i< wayList.size();i++) { // < wayList.size()
 			// for each way
 			System.out.println("processing track "+(i+1)+" of "+wayList.size());
 			first = true;
 			List<Long> wayNodeIDs = wayList.get(i).nodes;
 			
 			System.out.println(wayNodeIDs.size() +" waypoints to process");
 			for(int j = 0; j< wayNodeIDs.size();++j) {
 				
 				if(j%250==0 && j>0){
 					System.out.println("waypoint "+j);
 				}
 				if(osmNodes.get(wayNodeIDs.get(j)) == null) {
 					System.out.println("we missed one");
 					continue;
 				}
 				// go through the waypoints
 				if(first) {
 					first = false;
 					WayNodeOSM wayPoint = osmNodes.get(wayNodeIDs.get(j));
 					previous = getNearestPoint(wayPoint,graphNodes);
 					previous.setVisited();
 					continue;
 				}
 				
 				if(wayNodeIDs.get(j) == wayNodeIDs.get(j-1)) {
 					continue;
 				}
 				
 				current = getNearestPoint(osmNodes.get(wayNodeIDs.get(j)),graphNodes,graph,previous);
 				// mark that point and mark the shortest path connecting the nodes
 				current.setVisited();
 				PathExtended<NodeCppOSM> path = graph.getAlgorithms().getShortestPathAStar(previous, current);
 				for(NodeCppOSM node1 : path.getNodes()) {
 					//TODO check whether we have to mark edges and remove unvisited ones
 					node1.setVisited();
 				}
 				
 				previous = current;
 			}
 
 		}
 
 		// remove unmarked nodes
 		{
 			Iterator<NodeCppOSM> iter = graph.getNodes().iterator();
 			while(iter.hasNext()){
 				NodeCppOSM node = iter.next();
 				if(!node.isVisited()) {
 					node.removeAllEdges();
 					iter.remove();
 				}
 			}
 		}
 		simplify(graph);
 		return graph;
 	}
 
 	
 	private static NodeCppOSM getNearestPoint(WayNodeOSM wayPoint,
 			Collection<NodeCppOSM> graphNodes, GraphUndirected<NodeCppOSM, EdgeCppOSM> graph,NodeCppOSM previous) {
 		if(graphNodes.isEmpty()) {
 			throw new IllegalStateException();
 		}
 		// calculate distances
 		Iterator<NodeCppOSM> iter = graphNodes.iterator();
 		TreeMap<Double, NodeCppOSM> directDistance = new TreeMap<Double,NodeCppOSM>();
 		while (iter.hasNext()) {
 			NodeCppOSM node = (NodeCppOSM) iter.next();
 			double currentDistance = getDistance(wayPoint, node);
 			directDistance.put(currentDistance, node);
 		}
 		
 		int counter = 0;
 		iter = directDistance.values().iterator();
 		double shortestPath = Double.POSITIVE_INFINITY;
 		int numberOfCheckedNodes = 10;
 		double threshold = 1.4;
 		// find the shortest path of the k nearest nodes
 		while (iter.hasNext() && counter < numberOfCheckedNodes) {
 			NodeCppOSM node = (NodeCppOSM) iter.next();
 			if(node == previous) {
 				continue;
 			}
 			double currentPath = graph.getAlgorithms().getShortestPathAStar(previous,node).getDistance();
 			if(currentPath == 0.){
 				continue;
 			}
 			if (currentPath < shortestPath) {
 				shortestPath = currentPath;
 			}
 			++counter;
 		}
 		
 		iter = directDistance.values().iterator();
 		while (iter.hasNext()) {
 			NodeCppOSM node = (NodeCppOSM) iter.next();
 			if(threshold * shortestPath > graph.getAlgorithms().getShortestPathAStar(previous,node).getDistance()) {
 				return node;
 			}	
 		}
 		
 		return null;
 //		return nodeNearest;
 	}
 
 	private static NodeCppOSM getNearestPoint(WayNodeOSM wayPoint,
 			Collection<NodeCppOSM> graphNodes) {
 		if(graphNodes.isEmpty()) {
 			throw new IllegalStateException();
 		}
 		Iterator<NodeCppOSM> iter = graphNodes.iterator();
 		NodeCppOSM nearestNode = null;
 		double minDistance = Double.POSITIVE_INFINITY; 
 		while (iter.hasNext()) {
 			NodeCppOSM node = (NodeCppOSM) iter.next();
 			double currentDistance = getDistance(wayPoint, node);
 			if(currentDistance < minDistance) {
 				minDistance = currentDistance;
 				nearestNode = node;
 			}
 		}
 		
 		return nearestNode;
 	}
 
 	/**
 	 * @param nodeId a nodeId from the connected subgraph
 	 * @param fileName file name including path to the osm file to import
 	 * @return a connected subgraph imported from the osm file including the specified node
 	 */
 	public static GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> importCleanDirected(long nodeId,String fileName){
 		GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> graph = importOsmDirected(fileName);
 		// mark all nodes connected to the specified node
 		graph.getAlgorithms().visitAllNodesFromStartNode(graph.getNode(nodeId));
 		// remove all unmarked=unconnected nodes
 		Iterator<NodeCppOSMDirected> iter = graph.getNodes().iterator();
 		while(iter.hasNext()){
 			NodeCppOSMDirected node = iter.next();
 			if(!node.isVisited()) {
 				node.removeAllEdges();
 				iter.remove();
 			}
 		}
 		return graph;
 	}
 
 	/**
 	 * @return connected graph of Zweibrücken
 	 */
 	public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importZW(){
 		return importClean(260070555l,"src/test/resources/edu/uaskl/cpp/zweibruecken_way_no_meta.osm");
 	}
 	
 	
 	public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importZWUBZ(){
 		return importUBZ(260070555l,"src/test/resources/edu/uaskl/cpp/zweibruecken_way_no_meta_service.osm", "src/test/resources/edu/uaskl/cpp/2013-11-25_Entsorgung_ZW-EB_515_Tour_934 - Linie.osm");
 	}
 
 	/**
 	 * @return connected graph of Zweibrücken
 	 */
 	public static GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> importZWDirected(){
 		return importCleanDirected(260070555l,"src/test/resources/edu/uaskl/cpp/zweibruecken_way_no_meta.osm");
 	}
 
 	/**
 	 * @return  connected graph of the FH area
 	 */
 	public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importFH(){
 		return importClean(260070555l,"src/test/resources/edu/uaskl/cpp/fh_way_no_meta.osm");
 	}
 
 	/**
 	 * @return  connected graph of the FH area
 	 */
 	public static GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> importFHDirected(){
 		return importCleanDirected(260070555l,"src/test/resources/edu/uaskl/cpp/fh_way_no_meta.osm");
 	}
 
 
 	/**
 	 * @return connected graph of Kaiserslautern
 	 */
 	public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importKL(){
 		return importClean(281170640l,"src/test/resources/edu/uaskl/cpp/kl.osm");
 	} 
 
 	/**
 	 * @return connected graph of Kaiserslautern
 	 */
 	public static GraphDirected<NodeCppOSMDirected, EdgeCppOSMDirected> importKLDirected(){
 		return importCleanDirected(281170640l,"src/test/resources/edu/uaskl/cpp/kl.osm");
 	} 
 }
