 package edu.hawaii.achriste.ocdutils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.Namespace;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * This class contains methods that convert between GraphML and .pairs files.
  *
  * @author Anthony Christe
  */
 public class Converter {
 
     private static final String E_DELIM = " ";
     private static final String V_DELIM = ",";
     private static final Namespace ns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
 
     /**
      * Converts GraphML files to .pairs files using a streaming parser.
      *
      * @param graphMlFile The GraphML file being converted.
      * @param pairsFile The resulting pairs file name.
      * @param weighted true if the GraphML file is weighted, false otherwise.
      */
     public static void convertGraphMlToPairs(File graphMlFile, File pairsFile, boolean weighted) {
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(pairsFile));
             SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
             DefaultHandler handler = new GraphMlHandler(out, weighted);
             saxParser.parse(graphMlFile, handler);
         } catch (ParserConfigurationException | SAXException | IOException ex) {
             System.err.println("Problem occured with conversion");
             System.err.println(ex.getMessage());
         }
     }
 
     public static void convertClustersToGraphML(File inGraphMlFile, File clustersFile, File outGraphMlFile) throws JDOMException, IOException {
         Document graphMl;
         List<String> communities;
         Map<Integer, Set<String>> communityToNodes;
         Map<String, Set<Integer>> nodeToCommunities;
         Map<String, Integer> edgeToCommunity;
         int lastIndex;
 
         OutputStream out = new FileOutputStream(outGraphMlFile);
         
         communities = IOUtil.getFileAsLines(clustersFile);
         communityToNodes = new HashMap<>();
         nodeToCommunities = new HashMap<>();
         edgeToCommunity = new HashMap<>();
 
         initData(communities, communityToNodes, nodeToCommunities, edgeToCommunity);
         graphMl = loadDocument(inGraphMlFile);
         
         // Modify and update DOM
         addNewKeys(graphMl);
         addCommunityNodes(graphMl, communityToNodes);
         lastIndex = updateNodeData(graphMl, nodeToCommunities);
         declareCommunityEdges(graphMl, communityToNodes, lastIndex + 1);
         declareOtherEdges(graphMl, edgeToCommunity);
         
         // Write results to file
         XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
         xout.output(graphMl, out);
     }
 
     private static void initData(List<String> communities,
             Map<Integer, Set<String>> communityToNodes,
             Map<String, Set<Integer>> nodeToCommunities,
             Map<String, Integer> edgeToCommunity) {
 
         String[] edges;
         String[] nodes;
         String node;
         int comm = 0;
 
         for (String community : communities) {
             edges = community.trim().split(E_DELIM);
             communityToNodes.put(comm, new HashSet<String>());
 
             for (String edge : edges) {
                 edgeToCommunity.put(edge, comm);
                 nodes = edge.trim().split(V_DELIM);
 
                 if (!communityToNodes.get(comm).contains(nodes[0])) {
                     communityToNodes.get(comm).add(nodes[0]);
                 }
 
                 if (!communityToNodes.get(comm).contains(nodes[1])) {
                     communityToNodes.get(comm).add(nodes[1]);
                 }
 
                 if (!nodeToCommunities.containsKey(nodes[0])) {
                     nodeToCommunities.put(nodes[0], new TreeSet<Integer>());
                 }
 
                 if (!nodeToCommunities.containsKey(nodes[1])) {
                     nodeToCommunities.put(nodes[1], new TreeSet<Integer>());
                 }
 
                 nodeToCommunities.get(nodes[0]).add(comm);
                 nodeToCommunities.get(nodes[1]).add(comm);
             }
             comm++;
         }
     }
     
     private static void addNewKeys(Document doc) {
         Element root = doc.getRootElement();
         Element key;
         
         key = new Element("key", ns);
         key.setAttribute("attr.name", "ecomm");
         key.setAttribute("attr.type", "integer");
         key.setAttribute("for", "edge");
         key.setAttribute("id", "ecomm");
         root.addContent(0, key);
         
         key = new Element("key", ns);
         key.setAttribute("attr.name", "ncomm");
         key.setAttribute("attr.type", "integer");
         key.setAttribute("for", "node");
         key.setAttribute("id", "ncomm");
         root.addContent(1, key);
         
         key = new Element("key", ns);
         key.setAttribute("attr.name", "commlist");
         key.setAttribute("attr.type", "listint");
         key.setAttribute("for", "node");
         key.setAttribute("id", "commlist");
         root.addContent(2, key);
     }
     
     private static void addCommunityNodes(Document doc, Map<Integer, Set<String>> communityToNodes) {
         Element root;
         Element graph;
         Element node;
         Element data;
 
         int idx = 0;
 
         // First find the graph element
         root = doc.getRootElement();
         graph = null;
 
         for (Element e : root.getChildren()) {
             if (e.getName().equals("graph")) {
                 graph = e;
                 break;
             }
         }
 
         for (Integer i : communityToNodes.keySet()) {
             node = new Element("node", ns);
             node.setAttribute("id", "C_" + i);
             data = new Element("data", ns);
             data.setAttribute("key", "entity");
             data.addContent("COMM");
 
             node.addContent(data);
             graph.addContent(idx++, node);
         }
     }
 
     private static int updateNodeData(Document doc, Map<String, Set<Integer>> nodeToCommunities) {
 
         Element root = doc.getRootElement();
         Element graph = null;
         Element data;
         int lastIndex = -1;
         String id;
 
         for (Element e : root.getChildren()) {
             if (e.getName().equals("graph")) {
                 graph = e;
                 break;
             }
         }
 
         // Search for nodes where id does not begin with C_
         for (Element e : graph.getChildren()) {
             if (e.getName().equals("node")) {
                 if (!(e.getAttributeValue("id").startsWith("C_"))) {
                     // Handle data ncomm
                     data = new Element("data", ns);
                     data.setAttribute("key", "ncomm");
                     id = e.getAttributeValue("id");
                     if (id.contains(".")) {
                         id = id.split("\\.")[0];
                     }
                     
                     if(nodeToCommunities.containsKey(id)) {
                         data.addContent(Integer.toString(nodeToCommunities.get(id).size()));
                         e.addContent(0, data);
                     }
 
                     // Handle data commlist
                     String comms;
                     data = new Element("data", ns);
                     data.setAttribute("key", "commlist");
                     
                     if (nodeToCommunities.containsKey(id)) {
                         comms = nodeToCommunities.get(id).toString();
                         comms = comms.substring(1, comms.length() - 1);
                         comms = comms.replaceAll(" ", "");
                         data.addContent(comms);
                         e.addContent(1, data);
                     }
                     lastIndex = graph.indexOf(e);
                 }
             }
         }
         return lastIndex;
     }
 
     private static int declareCommunityEdges(Document doc, Map<Integer, Set<String>> communityToNodes, int firstIndex) {
         Element root = doc.getRootElement();
         Element graph = null;
         Element edge;
         int idx = 0;
         int lastIndex = -1;
 
         for (Element e : root.getChildren()) {
             if (e.getName().equals("graph")) {
                 graph = e;
                 break;
             }
         }
 
         for (Integer i : communityToNodes.keySet()) {
             for (String n : communityToNodes.get(i)) {
                 edge = new Element("edge", ns);
                 edge.setAttribute("id", "C_" + i.toString() + "," + n);
                 edge.setAttribute("source", "C_" + i.toString());
                 edge.setAttribute("target", n);
                 graph.addContent(firstIndex + idx++, edge);
                 lastIndex = graph.indexOf(edge);
             }
         }
         return lastIndex;
     }
 
     private static void declareOtherEdges(Document doc,
             Map<String, Integer> edgeToCommunity) {
 
         Element root = doc.getRootElement();
         Element graph = null;
         Element data;
         String ecomm;
         String source;
         String target;
 
         for (Element e : root.getChildren()) {
             if (e.getName().equals("graph")) {
                 graph = e;
                 break;
             }
         }
 
         // Search for nodes where id does not exists
         for (Element e : graph.getChildren()) {
             if (e.getName().equals("edge")) {
                 if (e.getAttributeValue("id") ==  null) {
                     // Handle data ecomm
                     data = new Element("data", ns);
                     data.setAttribute("key", "ecomm");
                     
                     source = e.getAttributeValue("source");
                     target = e.getAttributeValue("target");
                     
                     if(source.contains(".")) {
                         source = source.split("\\.")[0];
                     }
                     
                     if(target.contains(".")) {
                         target = target.split("\\.")[0];
                     }
                    ecomm = edgeToCommunity.get(target + "," + source).toString();
                     
                     data.addContent(ecomm);
                     e.addContent(1, data);
                 }
             }
         }
     }
 
     private static Document loadDocument(File graphMlFile) throws JDOMException, IOException {
         return new SAXBuilder().build(graphMlFile);
     }
 }
