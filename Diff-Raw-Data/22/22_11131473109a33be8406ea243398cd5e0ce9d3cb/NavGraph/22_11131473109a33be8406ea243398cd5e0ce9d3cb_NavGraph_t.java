 package ohm.roth.lbs;
 
 import com.vividsolutions.jts.geom.LineString;
 
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 
 public class NavGraph implements Serializable {
     public String name;
     public HashMap<String, Node> nodeList;
     public HashMap<String, LineString> geoList;
     public List<Edge> edgeList;
 
     public NavGraph(String name) {
         nodeList = new HashMap<String, Node>();
         geoList = new HashMap<String, LineString>();
         edgeList = new ArrayList<Edge>();
         this.name = name;
     }
 
     public NavGraph() {
         super();
         nodeList = new HashMap<String, Node>();
         geoList = new HashMap<String, LineString>();
         edgeList = new ArrayList<Edge>();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     //geoList
     public void addGeo(String id, LineString line) {
         if (!this.geoList.containsKey(id))
             this.geoList.put(id, line);
     }
 
     public LineString getGeo(String id) {
         return this.geoList.get(id);
     }
 
     public HashMap<String, LineString> getGeoList() {
         return geoList;
     }
 
     public void setGeoList(HashMap<String, LineString> geoList) {
         this.geoList = geoList;
     }
 
     // nodelist
     public void addNode(Node n) {
         if (!this.nodeList.containsKey(n.getName()))
             this.nodeList.put(n.getName(), n);
     }
 
     public Node getNode(String id) {
         return this.nodeList.get(id);
     }
 
     public HashMap<String, Node> getNodeList() {
         return nodeList;
     }
 
     public void setNodeList(HashMap<String, Node> nodeList) {
         this.nodeList = nodeList;
     }
 
     // edgelist
     public void addEdge(Edge e) {
         this.edgeList.add(e);
     }
 
     public void removeEdge(Edge e) {
         this.edgeList.remove(e);
     }
 
     public List<Edge> getEdgeList() {
         return edgeList;
     }
 
     public void setEdgeList(List<Edge> edgeList) {
         this.edgeList = edgeList;
     }
 
     //TODO: findClosest überarbeiten (zur not Nodes einführen)
 
     public Node findClosest(Position p) {
         double dis = 999999999;
         Node closest = null;
         for (Node node : this.nodeList.values()) {
             if (distance.calcDist(node.getPosition(), p) < dis) {
                 dis = distance.calcDist(node.getPosition(), p);
                 closest = node;
             }
         }
         return closest;
     }
 
     public Node findClosest2(Position p) {
         double dis = 999999999;
         boolean node = false;
         Node bNode = null;
         Edge bEdge = null;
 
         for (Edge edge : this.edgeList) {
            //TODO: Abstand auf Geometry
             Node nodeTo = this.nodeList.get(edge.getTo());
             Node nodeFrom = this.nodeList.get(edge.getFrom());
             if (nodeTo.getPosition().getLat() == p.getLat() && nodeTo.getPosition().getLon() == p.getLon()) return nodeTo;
             if (nodeFrom.getPosition().getLat() == p.getLat() && nodeFrom.getPosition().getLon() == p.getLon()) return nodeFrom;
 
             double xDelta = nodeTo.getPosition().getLon() - nodeFrom.getPosition().getLon();
             double yDelta = nodeTo.getPosition().getLat() - nodeFrom.getPosition().getLat();
             double u = ((p.getLon() - nodeFrom.getPosition().getLon()) * xDelta) + ((p.getLat() - nodeFrom.getPosition().getLat()) * yDelta);
             u = u / (xDelta * xDelta + yDelta * yDelta);
             if (u < 0) {
                 if (distance.calcDist(nodeFrom, p) < dis) {
                     bNode = nodeFrom;
                     dis = distance.calcDist(nodeFrom, p);
                     node = true;
                 }
             } else if (u > 1) {
                 if (distance.calcDist(nodeTo, p) < dis) {
                     bNode = nodeTo;
                     dis = distance.calcDist(nodeTo, p);
                     node = true;
                 }
             } else {
                 Node tmpNode = new Node("INSERTD"+ UUID.randomUUID().toString(), new Position ((nodeFrom.getPosition().getLon() + u * xDelta), (nodeFrom.getPosition().getLat() + u * yDelta)));
                 if (distance.calcDist(tmpNode, p) < dis) {
                     bNode = tmpNode;
                     bEdge = edge;
                     dis = distance.calcDist(tmpNode, p);
                     node = false;
                 }
             }
         }
         if (!node) {
             // Node einfügen
             // Todo: Kosten der einzufügenden Kante berechnen
             addNode(bNode);
             addConnection(bNode.getName(), bEdge.getFrom(), 0, bEdge.gao_id, bEdge.from_node, bEdge.to_node);
             addConnection(bEdge.getFrom(), bNode.getName(), 0, bEdge.gao_id, bEdge.from_node, bEdge.to_node);
 			addConnection(bNode.getName(), bEdge.getTo(), 0, bEdge.gao_id, bEdge.from_node, bEdge.to_node);
             addConnection(bEdge.getTo(), bNode.getName(), 0, bEdge.gao_id, bEdge.from_node, bEdge.to_node);
         }
 
 
         return bNode;
     }
 
     public void addConnection(String firstId, String secondId, double c, String gao_id, int from_node, int to_node) {
         Node node1 = this.getNode(firstId);
         Node node2 = this.getNode(secondId);
         if (node1 != null && node2 != null) {
             Edge edge1 = new Edge(node1.getName(), node2.getName(), c, gao_id, from_node, to_node);
             node1.addNeighbore(edge1);
             this.addEdge(edge1);
         }
     }
 }
