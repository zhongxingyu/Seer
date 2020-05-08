 package autograph;
 import java.io.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Graph Helper class used to manipulate graph objects
  *
  * @author Ben List
  * @version 1.0
  */
 public class GraphHelper {
 
    /**
     * Draws the graph object in the most efficent way possible
     *
     * Note: Probably going to have a Java drawing return type, not sure on details of implementation yet.
     *
     * @param   graph    The Graph to be drawn
     * @return           Returns an integer based error code or 0 for success 
     * @see     Graph
     */
    public static int mDrawGraph(Graph graph) {
       int returnVal = ErrorHandler.NOERROR;
       return returnVal;
    }
    
    /**
     * Saves the graph object as a serialized object to the given file location
     *
     * @param   graph       The graph to be saved
     * @param   fileName    The file name of the graph to be saved
     * @param   fileLoc     The location to save the graph to
     * @see     Graph
     */
    public static void mSaveGraphObject(Graph graph, String fileName, String fileLoc) {
       try {
          // Open filestream to write graph object to
          FileOutputStream fileOut = 
             new FileOutputStream(fileLoc + "/" + fileName + ".ser");
          ObjectOutputStream out = new ObjectOutputStream(fileOut);
          // Write graph object to file
          out.writeObject(graph);
          out.close();
          fileOut.close();
       } catch (IOException e) { // Saving the file failed
          // IO EXCEPTION TEMP ERROR CODE
       }
    }
    
    /**
     * Loads the serialized graph object from the file location given
     *
     * @param   fileName    The file name of the graph to be loaded (with .ser extension)
     * @param   fileLoc     The location to load the graph from
     * @return              The deserialized graph object
     * @see     Graph
     */
    public static Graph mLoadGraphObject(String fileName, String fileLoc) {
      Graph graph = new Graph();
       try {
          // Open file to read in graph object
          FileInputStream fileIn = 
             new FileInputStream(fileLoc + "/" + fileName);
          ObjectInputStream in = new ObjectInputStream(fileIn);
          // Read in graph object from file
          graph = (Graph) in.readObject();
          in.close();
          fileIn.close();
       } catch(IOException e) { // Reading the file failed
          // IO EXCEPTION TEMP ERROR CODE
       } catch (ClassNotFoundException e) { 
          // Class Not Found Exception
       }
       // Loading the graph object was successful
       return graph;
    }
    
    /**
     * Exports a graph to an xml file at the given location
     *
     * @param   graph       The graph to be exported
     * @param   filePath    The filepath to be saved (with .xml extension)
     * @see     Graph
     */
    public static void mExportGraphToXML(Graph graph, String filePath) {
    }
    
    /**
     * Imports a graph from xml file at the given location
     *
     * @param   filePath    The filepath to be saved (with .xml extension)
     * @return              The graph loaded from the xml file
     * @see     Graph
     */
    public static Graph mImportGraphFromXML(String filePath) {
      Graph graph = new Graph();
       ArrayList<Node> nodeArrayList;
       nodeArrayList = new ArrayList<Node>();
       ArrayList<Edge> edgeArrayList;
       edgeArrayList = new ArrayList<Edge>();
       Document dom;
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			// Using factory get an instance of document builder
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			// Parse using builder to get DOM representation of the XML file
 			dom = db.parse(filePath);
 			// Get Root Element
 			Element docEle = dom.getDocumentElement();
          // Get a NodeList of <Node> elements
          NodeList nl = docEle.getElementsByTagName("Node");
          // If there are Node elements
          if(nl != null && nl.getLength() > 0) {
             // Loop through each Node Element and load into Node object
             for(int i = 0 ; i < nl.getLength();i++) {
                Element el = (Element)nl.item(i);
                Node node = getNodeElement(el);
               System.out.println(node.mGetName()+" added");
                nodeArrayList.add(node);
 			   }
 		   } else { // No Nodes
 		   }
 		} catch(ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch(SAXException e) {
 			e.printStackTrace();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
       return graph;
    }
    
    /**
     * Exports a graph to a plaintext document in our Graphing Language
     *
     * @param   graph       The graph to be exported
     * @param   fileName    The file name to be saved (with .agl extension)
     * @param   fileLoc     The location to save the agl file to
     * @see     Graph
     */
    public static void mExportGraphToLanguage(Graph graph, String fileName, String fileLoc) {
    }
    
    /**
     * Imports a graph from a plaintext document in our Graphing Language
     *
     * @param   fileName    The file name to be loaded (with .agl extension)
     * @param   fileLoc     The location to load the agl file from
     * @return              The graph loaded from the agl file
     * @see     Graph
     */
    public static Graph mImportGraphFromLanguage(String fileName, String fileLoc) {
      Graph graph = new Graph();
       return graph;
    }
    
    /**
     * Gets Node object from xml element
     * 
     * @param   el          The xml node element
     * @return              The Node object
     */
    private static Node getNodeElement(Element el) {
       Node node;
       String id = el.getAttribute("id");
       if (id != null) {
          node = new Node(id, null, null, null);
       }
       else {
          node = new Node(null, null, null, null);
       }
       return node;
    }
 
    /**
     * Gets Edge object from xml element
     * 
     * @param   el          The xml edge element
     * @param   graph       The current graph with the nodes 
     * @return              The edge object
     */
    private static Edge getEdgeElement(Element el, Graph graph) {
       Edge edge;
       String id = el.getAttribute("id");
      String targetNode
       if (id != null) {
         edge = new Edge(id, null, null, null);
       }
       else {
         edge = new Edge(null, null, null, null);
       }
       return edge;
    }
 }
