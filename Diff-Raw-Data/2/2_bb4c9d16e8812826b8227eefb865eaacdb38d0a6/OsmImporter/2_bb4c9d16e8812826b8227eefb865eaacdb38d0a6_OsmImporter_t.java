 package edu.uaskl.cpp.importer;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.uaskl.cpp.map.meta.WayNodeOSM;
 import edu.uaskl.cpp.map.meta.WayOSM;
 import edu.uaskl.cpp.model.edge.EdgeCppOSM;
 import edu.uaskl.cpp.model.graph.GraphUndirected;
 import edu.uaskl.cpp.model.node.NodeCppOSM;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.DocumentBuilder;
 
 import org.xml.sax.SAXException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class OsmImporter {
 
 	// FIXME: Redundant?
     protected static int getDistance(final WayNodeOSM a, final WayNodeOSM b) {
         // long diffLat = a.lat - b.lat;
         // long diffLon = a.lon - b.lon;
         // return (int) Math.sqrt(diffLat*diffLat+diffLon*diffLon);
         // Spherical Law of Cosines
        return (int) (Math.acos((Math.sin(a.getLatitude()) * Math.sin(b.getLatitude())) + (Math.cos(a.getLatitude()) * Math.cos(b.getLatitude()) * Math.cos(b.getLongitude() - a.getLongitude()))) * 6367500);
     }
 
     protected static Document getDomFromFile(final String filename) {
         final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         Document document = null;
         try {
             final DocumentBuilder builder = factory.newDocumentBuilder();
             document = builder.parse(filename);
             document.getDocumentElement().normalize();
         } catch (ParserConfigurationException | SAXException | IOException e) {
             System.out.println("could not open the file");
             e.printStackTrace();
             System.exit(0);
         }
         return document;
     }
 
     protected static long get100NanoDegrees(final String parsed) {
         long value = 1;
         // different amount of decimal places (up to 7)
         final String decimalPlaces = parsed.split("\\.")[1];
         for (int i = 0; i < (7 - decimalPlaces.length()); ++i)
             value *= 10;
         value *= Long.parseLong(parsed.replace(".", ""), 10);
 
         return value;
     }
 
     protected static HashMap<Long, WayNodeOSM> getOsmNodes(final Document dom) {
         final HashMap<Long, WayNodeOSM> osmNodes = new HashMap<>();
         final Element documentElement = dom.getDocumentElement();
         final NodeList nodes = documentElement.getElementsByTagName("node");
         for (int i = 0; i < nodes.getLength(); ++i) {
             final Element node = (Element) nodes.item(i);
             final Long id = Long.parseLong(node.getAttribute("id"),10);
             final long lat = get100NanoDegrees(node.getAttribute("lat"));
             final long lon = get100NanoDegrees(node.getAttribute("lon"));
             final WayNodeOSM osmNode = new WayNodeOSM(lat, lon, id);
             osmNodes.put(id, osmNode);
         }
         return osmNodes;
     }
 
     protected GraphUndirected<NodeCppOSM, EdgeCppOSM> createNaive(final Document osmFile, final HashMap<String, WayNodeOSM> osmNodes) {
         // add all waypoints to the graph
         final GraphUndirected<NodeCppOSM, EdgeCppOSM> osmGraph = new GraphUndirected<NodeCppOSM, EdgeCppOSM>();
         final Collection<WayNodeOSM> wayPoints = osmNodes.values();
         for (final WayNodeOSM wayPoint : wayPoints) {
             final NodeCppOSM newNode = new NodeCppOSM(wayPoint.getID());
             osmGraph.addNode(newNode);
         }
 
         // connect them
         final Element documentElement = osmFile.getDocumentElement();
         final NodeList ways = documentElement.getElementsByTagName("way");
         for (int i = 0; i < ways.getLength(); ++i) {
             final NodeList childNodes = ways.item(i).getChildNodes();
             Long lastWaypoint = null;
             for (int j = 0; j < childNodes.getLength(); ++j) {
                 final Node cNode = childNodes.item(j);
                 if (cNode.getNodeType() == Node.ELEMENT_NODE) {
                     final Element childNode = (Element) cNode;
                     if ((lastWaypoint == null) && (childNode.getNodeName() == "nd")) {
                         lastWaypoint = Long.parseLong(childNode.getAttribute("ref"),10);
                         if (!osmNodes.containsKey(lastWaypoint))
                             lastWaypoint = null;
                     } else if (childNode.getNodeName() == "nd") {
                         final Long nodeId = Long.parseLong(childNode.getAttribute("ref"),10);
                         final NodeCppOSM node = osmGraph.getNode(nodeId);
                         if (!(node == null)) {
                             final int distance = getDistance(osmNodes.get(lastWaypoint), osmNodes.get(childNode.getAttribute("ref")));
                             osmGraph.getNode(lastWaypoint).connectWithNodeAndWeigth(node, distance);
                             lastWaypoint = Long.parseLong(childNode.getAttribute("ref"),10);
                         }
                     }
                     // for non naive: check for roundabout
                 }
             }
         }
 
         return osmGraph;
     }
 
     protected static GraphUndirected<NodeCppOSM, EdgeCppOSM> createFiltered(final Document osmFile, final HashMap<Long, WayNodeOSM> osmNodes) {
         final GraphUndirected<NodeCppOSM, EdgeCppOSM> osmGraph = new GraphUndirected<NodeCppOSM, EdgeCppOSM>();
 
         final Element documentElement = osmFile.getDocumentElement();
         final NodeList ways = documentElement.getElementsByTagName("way");
         for (int i = 0; i < ways.getLength(); ++i) { // for each way
             final NodeList childNodes = ways.item(i).getChildNodes();
             Long lastWaypoint = null;
             final List<Long> metaIds = new LinkedList<>();
             int distance = 0;
             Long currentWaypoint = null;
             boolean roundabout = false;
             String name = "";
             for (int j = 0; j < childNodes.getLength(); ++j) { // go through the nodes
                 final Node cNode = childNodes.item(j);
                 if (cNode.getNodeType() == Node.ELEMENT_NODE) {
                     final Element childNode = (Element) cNode;
                     if (childNode.getNodeName() == "nd") {
                         currentWaypoint = Long.parseLong(childNode.getAttribute("ref"),10);
                         if (osmNodes.containsKey(currentWaypoint)) {
                             if (!(lastWaypoint == null))
                                 distance += getDistance(osmNodes.get(lastWaypoint), osmNodes.get(currentWaypoint));
                             lastWaypoint = currentWaypoint;
                             metaIds.add(currentWaypoint);
 
                         }
                     } else if (childNode.getNodeName() == "tag")
                         if (childNode.hasAttribute("k") && childNode.getAttribute("k").equals("junction") && childNode.getAttribute("v").equals("roundabout")) {
                             roundabout = true;
                         }
                         else {
                         	if (childNode.hasAttribute("k") && childNode.getAttribute("k").equals("name")) {
                                 name = childNode.getAttribute("v");
                             }
                         }
 
                 }
             }
             
             
             if (roundabout)
                 distance = 0;
             
             for (int j = 1; j < metaIds.size(); ++j) {
                 final Long startNodeId = metaIds.get(j - 1);
                 final Long lastNodeId = metaIds.get(j);
                 if (osmGraph.getNode(startNodeId) == null) {
                     final NodeCppOSM newNode = new NodeCppOSM(startNodeId);
                     osmGraph.addNode(newNode);
                 }
                 if (osmGraph.getNode(lastNodeId) == null) {
                     final NodeCppOSM newNode = new NodeCppOSM(lastNodeId);
                     osmGraph.addNode(newNode);
                 }
                 final List<WayNodeOSM> metaNodes = new LinkedList<>();
                 metaNodes.add(osmNodes.get(startNodeId));
                 metaNodes.add(osmNodes.get(lastNodeId));
                 // TODO: Handle way properly.
                 osmGraph.getNode(startNodeId).connectWithNodeWeigthAndMeta(osmGraph.getNode(lastNodeId), distance, new WayOSM(0, WayOSM.WayType.UNSPECIFIED, name, metaNodes));
             }
         }
         // TODO simplify
         final Iterator<NodeCppOSM> iteratorNodes = osmGraph.getNodes().iterator();
         while (iteratorNodes.hasNext()) {
             final NodeCppOSM node = iteratorNodes.next();
             if (node.getDegree() == 2) {
                 final Long currentNodeId = node.getId();
                 final List<EdgeCppOSM> edges = node.getEdges();
                 final EdgeCppOSM edge1 = edges.get(0);
                 final EdgeCppOSM edge2 = edges.get(1);
                 final Long node1id = edge1.getNode1().getId() == (currentNodeId) ? edge1.getNode2().getId() : edge1.getNode1().getId();
                 final Long node2id = edge2.getNode1().getId() == (currentNodeId) ? edge2.getNode2().getId() : edge2.getNode1().getId();
                 if(currentNodeId == node1id){
                 	//we are in a loop and do not erase ourself
                 	continue;
                 }
                 // concat the list in the right way
                 final List<WayNodeOSM> newMetaNodes = edge1.getMetadata().getNodes(), metaNodes2 = edge2.getMetadata().getNodes();
                 // newMetaNodes = metaNodes1.get(0).id==node1id ? metaNodes1 : Collections.reverse(metaNodes1);
                 if (((Long)newMetaNodes.get(0).getID()).equals(currentNodeId)){
                     Collections.reverse(newMetaNodes);
                 }
                 newMetaNodes.remove(newMetaNodes.size()-1);
                 if (!((Long)metaNodes2.get(0).getID()).equals(currentNodeId)) {
                     Collections.reverse(metaNodes2);
                 }
                 newMetaNodes.addAll(metaNodes2);
                 // add a new edge
                 // TODO: Handle way properly - what would name be in this context?
                 osmGraph.getNode(node1id).connectWithNodeWeigthAndMeta(osmGraph.getNode(node2id), edge1.getWeight() + edge2.getWeight(), new WayOSM(0, WayOSM.WayType.UNSPECIFIED, "unknown", newMetaNodes));
                 // remove the old node
                 node.removeAllEdges();
                 iteratorNodes.remove();
             }
         }
 
         return osmGraph;
     }
 
     public static GraphUndirected<NodeCppOSM, EdgeCppOSM> importOsmUndirected(final String filename) {
         final Document osmFile = getDomFromFile(filename);
         final HashMap<Long, WayNodeOSM> osmNodes = getOsmNodes(osmFile);
         final GraphUndirected<NodeCppOSM, EdgeCppOSM> osmGraph = createFiltered(osmFile, osmNodes);
 
         /**
          * TODO create edges
          * for each way-element:
          * create the nodes and connect them but not for roundabout
          * for each way which is a roundabout:
          * find the existing nodes and connect them
          */
 
         return osmGraph;
     }
 
 }
