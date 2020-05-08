 package autograph;
 import java.awt.*;
 import autograph.GraphPanel;
 import java.awt.image.*;
 import java.io.*;
 
 import autograph.Edge.Direction;
 import autograph.Edge.EdgeStyle;
 import autograph.Node.NodeShape;
 import autograph.exception.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Stack;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Attr;
 import org.xml.sax.SAXException;
 
 /**
  * Graph Helper class used to manipulate graph objects
  *
  * @author Ben List
  * @version 1.0
  */
 public class GraphHelper {
 
 
 
 	private enum nodeAttributes{
 		ID,
 		LABEL,
 		SHAPE,
 		STYLE,
 		FILLCOLOR,
 		BORDERCOLOR,
 		LABELCOLOR,
 		FONT,
 		WIDTH,
 		HEIGHT
 	}
 	private static enum edgeAttributes{
 		ID,
 		LABEL,
 		STARTNODE,
 		ENDNODE,
 		DIRECTION,
 		STYLE,
 		EDGECOLOR,
 		LABELCOLOR,
 		FONT
 	}
 
 	/**
 	 * GetOreferredImageWidth - dynamically calculates the width of the image we are creating (in pixels) so we know what width to set
 	 *                          for the JPanel.
 	 * @param graph - the graph that we will be drawing.
 	 * @return - the width of the image.
 	 */
 	public static int mGetPreferredImageWidth(Graph graph){
 		int imageWidth = 0;
 		ArrayList<Node> nodes = graph.mGetNodeList();
 		int maxNodeWidth = 0;
 		int numNodes = nodes.size();
 		for(int i = 0; i < numNodes; i++){
 			if(nodes.get(i).mGetWidth() == 0){
 				nodes.get(i).mSetWidth(50);
 				nodes.get(i).mSetHeight(50);
 			}
 			if(nodes.get(i).mGetWidth() > maxNodeWidth){
 				maxNodeWidth = nodes.get(i).mGetWidth();
 			}
 		}
 		//KMW Note: We'll make the circle the width of putting half the nodes in one line. This _should_ be enough
 		//to prevent node overlapping, but it may run into problems with edges overlapping nodes. We will
 		//tweak this as necessary
 		imageWidth = (numNodes/2)*maxNodeWidth+500;
 		return imageWidth;
 	}
 
 	/**
 	 * Use the circle drawing algorithm to layout the locations for the nodes/edges
 	 * @param graph - the graph object to draw
 	 * @param g - the graphics element to use for drawing
 	 * @param panel - the panel to draw on
 	 */
 	public static void mDrawGraphInCircle(GraphPanel panel){
 		Graph graph = panel.mGetGraph();
 		ArrayList<Node> nodes = graph.mGetNodeList();
 		int numNodes = nodes.size();
 		double currentAngle = 0;
 		double angleIncrement = 360/numNodes;
 
 		//use the center of the JPanel for the center of our circle.
 		int centerX = panel.getWidth()/2;
 		int centerY = panel.getHeight()/2;
 		//the circumference of the circle will be bounded by the panel, with 30 pixels of padding
 		int radius = Math.min(centerX, centerY) - 30;
 
 		for(int i = 0; i < numNodes; i++){
 			int nodeX;
 			int nodeY;
 			if(currentAngle <= 90 || currentAngle >= 270){
 				nodeX = (int)(Math.cos(currentAngle)*radius) + centerX;
 			}
 			else{
 				nodeX = centerX - (int)(Math.cos(currentAngle)*radius);
 			}
 
 			if(currentAngle <= 180){
 				nodeY = centerY - (int)(Math.sin(currentAngle)*radius);
 			}
 			else{
 				nodeY = (int)(Math.sin(currentAngle)*radius) + centerY;
 			}
 			//nodes.get(i).mSetCenterLocation(nodeX, nodeY);
 			//mDrawNode(g, nodes.get(i));
 			currentAngle = currentAngle+angleIncrement;
 		}
 	}
 
 	/**
 	 *  This is the primary graph drawing method. Implements the force-directed graph
 	 *  drawing algorithm. No animation between timesteps at this time.
 	 *  
 	 *  Reference:
 	 *  http://www.cs.brown.edu/~rt/gdhandbook/chapters/force-directed.pdf
 	 * 
 	 * @author Jeff Farris
 	 * 
 	 * @param graph - the graph object being drawn
 	 * @param g - the graphics object where the graph will be drawn
 	 * @param panel - the panel holding the graphics
 	 * 
 	 */
 	public static void mDrawForceDirectedGraph(GraphPanel panel) {
 		Graph graph = panel.mGetGraph();
 		ArrayList<Node> nodes = graph.mGetNodeList();
 		ArrayList<Edge> edges = graph.mGetEdgeList();
 
 		int numNodes = nodes.size();
 		int numEdges = edges.size();
 
		int width = GraphHelper.mGetPreferredImageWidth(panel.mGetGraph());
		int height = GraphHelper.mGetPreferredImageWidth(panel.mGetGraph());
 		int area = width * height;
 		System.out.println(width);
 		
 		/*
 		// Set each node at a random location
 		// within the center 60% of the screen
 		Random generator = new Random();
 		for(int i = 0; i < numNodes; i++) {
 			Node v = nodes.get(i);
 			double randX = generator.nextDouble() * (width - (width / 5)) + (width / 5);
 			double randY = generator.nextDouble() * (height - (height / 5)) + (height / 5);
 			v.mSetCenterLocation((int)randX, (int)randY);   
 		} */
 
 		// Value for the ideal distance between nodes
 		double k = Math.sqrt(area / (numNodes + numEdges));
 
 		// This value is used to make sure nodes don't move too much
 		// as well as makes sure the graph is slowly coming to rest.
 		double temp = width / 10;
 		double tempOrig = temp;
 
 		// Values used for making sure the graph stays centered on the screen
 		int totalX = 0;
 		int totalY = 0;
 		int centerScreenX = width / 2;
 		int centerScreenY = height / 2;
 
 		// Values for calculating displacement
 		GraphVector diff = new GraphVector();
 		double displace;
 
 		// While the graph has not "cooled off"
 		while(temp > 1) {
 
 			diff.mSetXCor(0.0);
 			diff.mSetYCor(0.0);
 
 			/**
 			 * Calculate the repulsive forces 
 			 */
 			for(int i = 0; i < numNodes; i++) {
 				// The current node
 				Node v = nodes.get(i);
 
 				v.mSetDispX(0.0);
 				v.mSetDispY(0.0);
 
 				/**
 				 * Look at the rest of the nodes
 				 */
 				for(int j = 0; j < numNodes; j++) {
 					Node u = nodes.get(j);
 
 					// if the two nodes are not the same
 					if (!v.equals(u)) {
 						// Set the difference vector
 						diff.mSetXCor(v.mGetCenterX() - u.mGetCenterX());
 						diff.mSetYCor(v.mGetCenterY() - u.mGetCenterY());
 
 						// If the nodes are not on top of each other
 						if(diff.mGetDistance() != 0) {
 							// Add the repulsive forces
 							v.mSetDispX(v.mGetDispX() + (diff.mGetXCor() / diff.mGetDistance()) * diff.mCalcRepulsive(k, tempOrig));
 							v.mSetDispY(v.mGetDispY() + (diff.mGetYCor() / diff.mGetDistance()) * diff.mCalcRepulsive(k, tempOrig));
 						}
 						else {
 							// otherwise force the nodes apart!
 							v.mSetDispX(-1 * width / 4);
 							v.mSetDispY(-1 * width / 4);
 							u.mSetDispX(width / 4);
 							u.mSetDispY(width / 4);
 						}
 					}
 				}
 			}  
 			/**
 			 * Calculate the attractive forces
 			 */
 			for(int a = 0; a < numEdges; a++) {
 				// The current edge
 				Edge e = edges.get(a);
 
 				// Set the difference vector
 				diff.mSetXCor(e.mGetStartNode().mGetCenterX() - e.mGetEndNode().mGetCenterX());
 				diff.mSetYCor(e.mGetStartNode().mGetCenterY() - e.mGetEndNode().mGetCenterY());
 
 				// Only displace if they're not on top of each other
 				if(diff.mGetDistance() != 0.0) {
 					// Displace the first node
 					e.mGetStartNode().mSetDispX(e.mGetStartNode().mGetDispX() - (diff.mGetXCor() / diff.mGetDistance()) * diff.mCalcAttractive(k));
 					e.mGetStartNode().mSetDispY(e.mGetStartNode().mGetDispY() - (diff.mGetYCor() / diff.mGetDistance()) * diff.mCalcAttractive(k));
 
 					// Displace the second node
 					e.mGetEndNode().mSetDispX(e.mGetEndNode().mGetDispX() + (diff.mGetXCor() / diff.mGetDistance()) * diff.mCalcAttractive(k));
 					e.mGetEndNode().mSetDispY(e.mGetEndNode().mGetDispY() + (diff.mGetYCor() / diff.mGetDistance()) * diff.mCalcAttractive(k));
 				}
 			}
 			/**
 			 * Limit max displacement to temp and prevent from displacement outside panel
 			 */
 			for(int b = 0; b < numNodes; b++) {
 				// the current node
 				Node v = nodes.get(b);
 
 				// Make sure the displacement isn't too much
 				if(Math.abs(v.mGetDispDistance()) > temp)
 					displace = temp;
 				else
 					displace = v.mGetDispDistance();
 
 				// Reposition the node
 				v.mSetCenterLocation((int)(v.mGetCenterX() + (v.mGetDispX() / v.mGetDispDistance()) * displace), 
 						(int)(v.mGetCenterY() + (v.mGetDispY() / v.mGetDispDistance()) * displace));
 
 				// Make sure none of the nodes are off the screen
 				v.mSetCenterLocation((int)(Math.min(width - v.mGetWidth(), Math.max(v.mGetWidth(), v.mGetCenterX()))), 
 						(int)(Math.min(height - v.mGetHeight(), Math.max(v.mGetHeight(), v.mGetCenterY()))));
 			}
 
 			/**
 			 * Cool the temperature as the graph is taking
 			 * shape (ideally)
 			 */
 			temp -= 10;
 		}
 
 		/**
 		 * Shift the entire graph back to the middle
 		 * of the screen by calculating the center of
 		 * the graph the dispersing each node back
 		 * toward the center of the screen.
 		 */
 		for(int p = 0; p < numNodes; p++) {
 			Node v = nodes.get(p);
 			// Get the totals for calculating the center of the graph
 			totalX += v.mGetCenterX();
 			totalY += v.mGetCenterY();
 		}
 
 		// Make the total values = the average values
 		totalX /= numNodes;
 		totalY /= numNodes;
 
 		// Make the total values = the amount to displace all nodes
 		totalX -= centerScreenX;
 		totalY -= centerScreenY;
 
 		// Change each node's position
 		for(int q = 0; q < numNodes; q++) {
 			Node v = nodes.get(q);
 			v.mSetCenterLocation(v.mGetCenterX() - totalX, v.mGetCenterY() - totalY);
 		}
 	}
 
 	/**
 	 * Draws the graph object in the most efficient way possible
 	 *
 	 * Note: Probably going to have a Java drawing return type, not sure on details of implementation yet.
 	 *
 	 * @param   graph    The Graph to be drawn
 	 * @see     Graph
 	 */
 	public static void mDrawGraph(Graph graph, Graphics g, GraphPanel panel) {
 		// TODO: This is a lot slower than it needs to be.. You should use a hash map with the node ID
 		// TODO: have user dynamically set width/height rather than hard coding it here. 
 		//      (Or dynamically calculate height/width based on label size)
 		//      At the momemnt the node size will be updated if the label does not 
 		//      fit in the node within the mDrawNode function.
 
 		// Set background color of graphics object to white
 		g.setColor(Color.WHITE);
 
 		mDrawGraphInCircle(panel);
 
 		// Draw edges
 		ArrayList<Edge> edges = graph.mGetEdgeList();
 		for (Edge edge : edges) {
 			mDrawEdge(g, edge);
 		}
 	}
 
 	/**
 	 * Saves the graph object as a serialized object to the given file location
 	 *
 	 * @param   graph       The graph to be saved
 	 * @param   filePath    The filepath to be saved (with .xml extension)
 	 * @see     Graph
 	 */
 	public static void mSaveGraphObject(Graph graph, String filePath) {
 		try {
 			// Open filestream to write graph object to
 			FileOutputStream fileOut = 
 					new FileOutputStream(filePath);
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
 	 * @param   filePath    The filepath to be saved (with .xml extension)
 	 * @return              The deserialized graph object
 	 * @see     Graph
 	 */
 	public static Graph mLoadGraphObject(String filePath) {
 		Graph graph = new Graph("");
 		try {
 			// Open file to read in graph object
 			FileInputStream fileIn = 
 					new FileInputStream(filePath);
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
 		// Get Node and Edge list and the number of nodes and edges
 		ArrayList<Node> nodes = graph.mGetNodeList();
 		ArrayList<Edge> edges = graph.mGetEdgeList();
 		int numNodes = nodes.size();
 		int numEdges = edges.size();
 
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
 			// Create and add root element to document
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("Autograph");
 			doc.appendChild(rootElement);
 			rootElement.setAttribute("title", graph.mGetTitle());
 
 			// Nodes element
 			Element nodesElement = doc.createElement("Nodes");
 			// Append Nodes element to root
 			rootElement.appendChild(nodesElement);
 			// Edges element
 			Element edgesElement = doc.createElement("Edges");
 			// Append Edges element to root
 			rootElement.appendChild(edgesElement);
 
 			// Loop through each node, add it as a child element to the Nodes Element
 			// The grab each attribute for it and write the attribute to that element
 			for (int i = 0; i < numNodes; i++) {
 				Node node = nodes.get(i);
 				// Create new node element
 				Element nodeElement = doc.createElement("Node");
 				// Append node element to Parent (Nodes)
 				nodesElement.appendChild(nodeElement);
 				for (nodeAttributes na : nodeAttributes.values() ) {
 					// Get String Value of Attribute
 					String value = mGetNodeAttributeValue(node, na);
 					// Add attribute and associated value
 					switch (na) {
 					case LABEL: // The label element can be empty, we want to ignore it if this is the case
 						if (value != "") 
 							nodeElement.setAttribute(na.name().toLowerCase(), value);
 						break;
 					case FILLCOLOR: //Because XML is case sensitive
 						nodeElement.setAttribute("fillColor", value);
 						break;
 					case BORDERCOLOR: //Because XML is case sensitive
 						nodeElement.setAttribute("borderColor", value);
 						break;
 					case LABELCOLOR: //Because XML is case sensitive
 						nodeElement.setAttribute("labelColor", value);
 						break;
 					case WIDTH: // Width can be empty (defaults to 0), we want to ignore it if this is the case
 						if (!value.equals("0")) {
 							nodeElement.setAttribute(na.name().toLowerCase(), value);
 						}
 						break;
 					case HEIGHT: // Height can be empty (defaults to 0), we want to ignore it if this is the case
 						if (!value.equals("0")) {
 							nodeElement.setAttribute(na.name().toLowerCase(), value);
 						}
 						break;
 					default: // Add attribute to current edge
 						nodeElement.setAttribute(na.name().toLowerCase(), value);
 						break;
 					}
 				}
 			}
 			// Loop through each edge, add it as a child element to the Edges Element
 			// The grab each attribute for it and write the attribute to that element
 			for (int i = 0; i < numEdges; i++) {
 				// Get next edge
 				Edge edge = edges.get(i);
 				// Create new edge element
 				Element edgeElement = doc.createElement("Edge");
 				// Append edge element to Parrent (Edges)
 				edgesElement.appendChild(edgeElement);
 				for (edgeAttributes ea : edgeAttributes.values() ) {
 					// Get string value of associated attribute
 					String value = mGetEdgeAttributeValue(edge, ea);
 					// Add attribute ans associated value
 					switch (ea) {
 					case LABEL: // The label element can be empty, we want to ignore it if this is the case
 						if (value != "")
 							edgeElement.setAttribute(ea.name().toLowerCase(), value);
 						break;
 					case STARTNODE: //Because XML is case sensitive
 						edgeElement.setAttribute("startNode", value);
 						break;
 					case ENDNODE: //Because XML is case sensitive
 						edgeElement.setAttribute("endNode", value);
 						break;
 					case EDGECOLOR: //Because XML is case sensitive
 						edgeElement.setAttribute("edgeColor", value);
 						break;
 					case LABELCOLOR: //Because XML is case sensitive
 						edgeElement.setAttribute("labelColor", value);
 						break;
 					default: // Add attribute to current edge
 						edgeElement.setAttribute(ea.name().toLowerCase(), value);
 						break;
 					}
 				}
 			}
 			// write the content into xml file
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			StreamResult result = new StreamResult(new File(filePath));
 			transformer.transform(source, result);
 
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		} catch (TransformerException tfe) {
 			tfe.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Imports a graph from xml file at the given location
 	 *
 	 * @param   filePath    The filepath to be saved (with .xml extension)
 	 * @return              The graph loaded from the xml file
 	 * @see     Graph
 	 */
 	public static Graph mImportGraphFromXML(String filePath) {
 		Graph graph = null;
 		Document dom;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			// Using factory get an instance of document builder
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			// Parse using builder to get DOM representation of the XML file
 			dom = db.parse(filePath);
 			// Get Root Element
 			Element docEle = dom.getDocumentElement();
 			// Get Graph Title
 			graph = new Graph(docEle.getAttribute("title"));
 			// Get a NodeList of <Node> elements
 			NodeList nl = docEle.getElementsByTagName("Node");
 			// If there are Node elements
 			if(nl != null && nl.getLength() > 0) {
 				// Loop through each Node Element and load into Node object
 				for(int i = 0; i < nl.getLength(); i++) {
 					Element el = (Element)nl.item(i);
 					try {
 						Node node = mGetNodeElement(el);
 						graph.mAddNode(node);
 					} catch (InvalidXMLException e) {
 						e.getMessage();
 						e.getCause();
 					} catch (CannotAddNodeException e) {
 						e.getMessage();
 						e.getCause();
 					}
 				}
 			} else {
 				// TODO: THROW INVALID XML EXCEPTION
 			}
 			nl = docEle.getElementsByTagName("Edge");
 			// If there are Edge elements
 			if(nl != null && nl.getLength() > 0) {
 				// Loop through each Edge Element and load into Edge object
 				for(int i = 0; i < nl.getLength(); i++) {
 					Element el = (Element)nl.item(i);
 					try {
 						Edge edge = mGetEdgeElement(el, graph);
 						graph.mAddEdge(edge);
 					} catch (InvalidXMLException e) {
 						e.getMessage();
 						e.getCause();
 					} catch (CannotAddEdgeException e) {
 						e.getMessage();
 						e.getCause();
 					}
 				}
 			} else { 
 				// TODO: THROW INVALID XML EXCEPTION
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
 	public static void mExportGraphToGML(Graph graph, String filePath) throws IOException{
 		StringBuilder gml = new StringBuilder();
 		GMLBuilder gmlBuilder = new GMLBuilder(gml);
 
 		//create the gml string
 		gmlBuilder.mBuildGML(graph);
 
 		FileWriter file = new FileWriter(filePath);
 		BufferedWriter out = new BufferedWriter(file);
 
 		out.write(gmlBuilder.mGetGML().toString());
 		out.close();
 	}
 
 
 
 	/**
 	 * Imports a graph from a plaintext document in our Graphing Language
 	 *
 	 * @param   fileName    The file name to be loaded (with .agl extension)
 	 * @param   fileLoc     The location to load the agl file from
 	 * @return              The graph loaded from the agl file
 	 * @throws CannotLoadGraph, IOException
 	 * @see     Graph
 	 */
 	public static Graph mImportGraphFromGML(String fileName, String fileLoc){
 
 		Stack<String> graphLocation = new Stack<String>();
 		Graph graph = new Graph("");
 		if(!fileName.isEmpty()){
 			String fullFilePath;
 			if(fileLoc != null){
 				fullFilePath = fileLoc + fileName;
 			}
 			else{
 				fullFilePath = fileName;
 			}
 			Scanner scanner = null;
 			try {
 				scanner = new Scanner(new FileInputStream(fullFilePath));
 			} catch (FileNotFoundException e1) {
 				// Report failures to user
 				e1.printStackTrace();
 			}
 			StringBuilder fileText = new StringBuilder();
 			//String NL = System.getProperty("line.separator");
 
 			if(scanner != null){
 				//Read in all of the text into a StringBuilder.
 				try{
 					while(scanner.hasNextLine()){
 						fileText.append(scanner.nextLine());
 					}
 				}
 				finally{
 					scanner.close();
 				}
 			}
 
 			//Begin parsing the text into a graph.
 			if(fileText.indexOf("graph")!= -1){
 				int currentIndex = fileText.indexOf("graph") + "graph".length();
 				GMLParser parser = new GMLParser(fileText, currentIndex);
 				String word = parser.mGetNextWord();
 				if(word.equals("[")){
 					graphLocation.push("graph");
 				}
 				if(graphLocation.peek()=="graph"){
 					try {
 						parser.mGetGraphAttributesGML(graph, graphLocation);
 					} catch (CannotLoadGraph e) {
 						// TODO report failures to user
 						e.printStackTrace();
 					}
 				}
 
 			}
 			else{
 				//throw new CannotLoadGraph("Invalid GML syntax");
 			}
 
 		}
 		else{
 			//throw new CannotLoadGraph("Invalid GML file");
 		}
 		return graph;
 	}
 	/**
 	 * Returns string value of requested attribute given a node object
 	 * 
 	 * @param   node        The node object
 	 * @param   attribute   The nodeAttribute
 	 * @return              The value of the attribute as a string
 	 */
 	private static String mGetNodeAttributeValue(Node node, nodeAttributes attribute) {
 		String value = "";
 		try {
 			switch (attribute) {
 			case ID:
 				value = node.mGetId();
 				break;
 			case LABEL:
 				value = node.mGetLabel();
 				break;
 			case SHAPE:
 				value = node.mGetShape().name();
 				break;
 			case STYLE:
 				value = node.mGetStyle().name();
 				break;
 			case FILLCOLOR:;
 			// get hax value of color as a string
 			value = Integer.toHexString(node.mGetFillColor().getRGB());
 			// Remove ff from begining of value, add a # symbol and make all caps
 			value = "#" + value.substring(2, value.length()).toUpperCase();
 			break;
 			case BORDERCOLOR:
 				// get hax value of color as a string
 				value = Integer.toHexString(node.mGetBorderColor().getRGB());
 				// Remove ff from begining of value, add a # symbol and make all caps
 				value = "#" + value.substring(2, value.length()).toUpperCase();
 				break;
 			case LABELCOLOR:
 				// get hax value of color as a string
 				value = Integer.toHexString(node.mGetLabelColor().getRGB());
 				// Remove ff from begining of value, add a # symbol and make all caps
 				value = "#" + value.substring(2, value.length()).toUpperCase();
 				break;
 			case FONT:
 				Font f = node.mGetFont();
 				value = f.getFontName() + " " + f.getStyle() + " " + f.getSize();
 				break;
 			case WIDTH:
 				value = String.valueOf(node.mGetWidth());
 				break;
 			case HEIGHT:
 				value = String.valueOf(node.mGetHeight());
 				break;
 			default: break;
 			}
 		} catch (IllegalArgumentException e) {
 			// TODO: Print out warning (log) for invalid attribute
 			// This function is only ever called by code, and the associated enums
 			// are from an existing graph object and therefore should never happen.
 		}
 		return value;
 	}
 
 	/**
 	 * Returns string value of requested attribute given an edge object
 	 * 
 	 * @param   edge        The edge object
 	 * @param   attribute   The edgeAttribute
 	 * @return              The value of the attribute as a string
 	 */
 	private static String mGetEdgeAttributeValue(Edge edge, edgeAttributes attribute) {
 		String value = "";
 		try {
 			switch (attribute) {
 			case ID:
 				value = edge.mGetId();
 				break;
 			case LABEL:
 				value = edge.mGetLabel();
 				break;
 			case STARTNODE:
 				value = edge.mGetStartNode().mGetId();
 				break;
 			case ENDNODE:
 				value = edge.mGetEndNode().mGetId();
 				break;
 			case DIRECTION:
 				value = edge.mGetDirection().name();
 				break;
 			case STYLE:
 				value = edge.mGetEdgeStyle().name();
 				break;
 			case EDGECOLOR:;
 			// get hax value of color as a string
 			value = Integer.toHexString(edge.mGetEdgeColor().getRGB());
 			// Remove ff from begining of value, add a # symbol and make all caps
 			value = "#" + value.substring(2, value.length()).toUpperCase();
 			break;
 			case LABELCOLOR:
 				// get hax value of color as a string
 				value = Integer.toHexString(edge.mGetLabelColor().getRGB());
 				// Remove ff from begining of value, add a # symbol and make all caps
 				value = "#" + value.substring(2, value.length()).toUpperCase();
 				break;
 			case FONT:
 				Font f = edge.mGetFont();
 				value = f.getFontName() + " " + f.getStyle() + " " + f.getSize();
 				break;
 			default: break;
 			}
 		} catch (IllegalArgumentException e) {
 			// TODO: Print out warning (log) for invalid attribute
 			// This function is only ever called by code, and the associated enums
 			// are from an existing graph object and therefore should never happen.
 		}
 		return value;
 	}
 	/**
 	 * Creates Node object from xml element
 	 * 
 	 * @param   el          The xml node element
 	 * @return              The Node object
 	 */
 	private static Node mGetNodeElement(Element el) throws InvalidXMLException {
 		// TODO: Get additional elements from XML
 
 		// Create Node Object
 		Node node = null;
 		String id = el.getAttribute("id");
 		if (!id.isEmpty()) {
 			node = new Node(id, el.getAttribute("label"), 
 					el.getAttribute("shape"), el.getAttribute("style"));
 			// Get the rest of the attributes
 			NamedNodeMap attributes = el.getAttributes();
 			for (int i = 0; i < attributes.getLength(); i++) {
 				// Get Current Attribute
 				Attr a = (Attr)attributes.item(i);
 				// Set values for valid attributes
 				try {
 					switch (nodeAttributes.valueOf( a.getName().toUpperCase() )) {
 					case FILLCOLOR:
 						// TODO: Check for # and allow textual representation of colors ie "red"
 						node.mSetFillColor(Color.decode( a.getValue() ));
 						break;
 					case BORDERCOLOR:
 						// TODO: Check for # and allow textual representation of colors ie "red"
 						node.mSetBorderColor(Color.decode( a.getValue() ));
 						break;
 					case LABELCOLOR:
 						// TODO: Check for # and allow textual representation of colors ie "red"
 						node.mSetLabelColor(Color.decode( a.getValue() ));
 						break;
 					case FONT:
 						// TODO: Handle FONT ATTRIBUTE;
 						break;
 					case WIDTH:
 						// TODO: Handle Width Attribute;
 						break;
 					case HEIGHT:
 						// TODO: Handle Width Attribute;
 						break;
 					default: break;
 					}
 				} catch (NumberFormatException e) {
 					// COLOR VALUE ERROR
 					switch (nodeAttributes.valueOf( a.getName().toUpperCase() )) {
 					case FILLCOLOR:
 						// TODO: Print out warning (log) for invalid fill color
 						break;
 					case BORDERCOLOR:
 						// TODO: Print out warning (log) for invalid border color
 						break;
 					case LABELCOLOR:
 						// TODO: Print out warning (log) for invalid label color
 						break;
 					default: 
 						//Print out Unknown Number Format Exception warning (log)
 						break;
 					}
 				} catch (IllegalArgumentException e) {
 					// TODO: Print out warning (log) for invalid attribute
 				}
 			}
 		} else { // Invalid XML for a node
 			throw new InvalidXMLException("Node Element is missing the required ID attribute");
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
 	private static Edge mGetEdgeElement(Element el, Graph graph) throws InvalidXMLException {
 		// TODO: Get additional elements from XML
 		Edge edge = null;
 		String id = el.getAttribute("id");
 		Node startNode = graph.mGetNodeById(el.getAttribute("startNode"));
 		Node endNode = graph.mGetNodeById(el.getAttribute("endNode"));
 		if (!id.isEmpty() && startNode != null && endNode != null) {
 			edge = new Edge(id, el.getAttribute("label"), startNode, endNode, 
 					el.getAttribute("direction"), el.getAttribute("edgeStyle"), false);
 			// Get the rest of the attributes
 			NamedNodeMap attributes = el.getAttributes();
 			for (int i = 0; i < attributes.getLength(); i++) {
 				// Get Current Attribute
 				Attr a = (Attr)attributes.item(i);
 				// Set values for valid attributes
 				try {
 					switch (edgeAttributes.valueOf( a.getName().toUpperCase() )) {
 					case EDGECOLOR:
 						// TODO: Check for # and allow textual representation of colors ie "red"
 						edge.mSetEdgeColor(Color.decode( a.getValue() ));
 						break;
 					case LABELCOLOR:
 						// TODO: Check for # and allow textual representation of colors ie "red"
 						edge.mSetLabelColor(Color.decode( a.getValue() ));
 						break;
 					case FONT:
 						// TODO: Handle FONT ATTRIBUTE;
 						break;
 					default: break;
 					}
 				} catch (NumberFormatException e) {
 					// COLOR VALUE ERROR
 					switch (edgeAttributes.valueOf( a.getName().toUpperCase() )) {
 					case EDGECOLOR:
 						// TODO: Print out warning (log) for invalid fill color
 						break;
 					case LABELCOLOR:
 						// TODO: Print out warning (log) for invalid label color
 						break;
 					default: 
 						//Print out Unknown Number Format Exception warning (log)
 						break;
 					}
 				} catch (IllegalArgumentException e) {
 					// TODO: Print out warning (log) for invalid attribute
 				}
 			}
 		}
 		else { // Invalid XML for an edge
 			throw new InvalidXMLException("Edge Element is missing one or more of the following " + 
 					"required attributes: ID, startNode, endNode");
 		}
 		return edge;
 	}
 
 	/**
 	 * DrawNode - renders the node given the node's position and the graphics object g.
 	 *
 	 * @param   g        The Graphics object the node is drawn on
 	 * @param   n        The node object to draw on g
 	 * @see     Node
 	 */
 	public static void mDrawNode(Graphics g, Node n){
 		// make the assumption that no object's width should be 0
 		if(n.mGetWidth() != 0){
 			int upperLeftX = n.mGetUpperLeftX();
 			int upperLeftY = n.mGetUpperLeftY();
 
 			// KMW Note: The Graphics2D library draws strings using the lower left part of the string 
 			//           as the beginning coordinates of the graph.
 			// We now need to calculate the optimal position of the lower left portion of the string 
 			// so that the string is centered within the bounds of the circle.
 			g.setFont(n.mGetFont());
 			FontMetrics fm = g.getFontMetrics();
 			int labelWidth = fm.stringWidth(n.mGetLabel());
 			int labelHeight = fm.getHeight();
 			int widthDifference = n.mGetWidth() - labelWidth;
 			if(widthDifference <= 0){
 				//dynamically resize the node so the label fits. (because it is a circle resize both height and width)
 				n.mSetWidth(labelWidth + 20);
 				n.mSetHeight(labelWidth + 20);
 
 				widthDifference = n.mGetWidth() - labelWidth;
 			}
 
 			int heightDifference = n.mGetHeight() - labelHeight;
 			if(heightDifference <= 0){
 				//dynamically resize the node so the label fits. (because it is a circle resize both height and width)
 				n.mSetWidth(labelHeight + 20);
 				n.mSetHeight(labelHeight + 20);
 				widthDifference = n.mGetWidth() - labelWidth;
 			}
 			//x coordinate of the lower left position of the string (for centering the string in the node)
 			int labelLeftX = upperLeftX + widthDifference/2; 
 			//y coordinate of the lower left position of the string (for centering the string in the node)
 			int labelLeftY = upperLeftY + heightDifference/2 + labelHeight/2; 
 
 			switch (n.mGetShape()){
 			case CIRCLE:
 			case OVAL:
 				//KMW Note: Graphics2D has antialiasing that renders the pictures much clearer.
 				Graphics2D g2 = (Graphics2D)g;
 				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 				//draw shape first so label is not overwritten
 				g2.setColor(n.mGetFillColor());
 				g2.fillOval(upperLeftX, upperLeftY, n.mGetWidth(), n.mGetHeight());
 				g2.setColor(n.mGetBorderColor());
 				g2.drawOval(upperLeftX, upperLeftY, n.mGetWidth(), n.mGetHeight());
 
 				//draw the label
 				//KMW Note: the text still looks kind of bad. It has a separate key/value combo of
 				//          KEY_TEXT_ANTIALIASING and VALUE_TEXT_ANTIALIAS_ON, but that didn't seem
 				//          to change the look of the text. Not sure if we can fix this.
 				g2.setColor(n.mGetLabelColor());
 				g2.drawString(n.mGetLabel(), labelLeftX, labelLeftY);
 
 				break;
 			case SQUARE:
 			case RECTANGLE:
 				//KMW Note: don't need to antialias for squares/rectangles. They look fine as is.
 				//draw shape first so label is not overwritten
 				g.setColor(n.mGetFillColor());
 				g.fillRect(upperLeftX, upperLeftY, n.mGetWidth(), n.mGetHeight());
 				g.setColor(n.mGetBorderColor());
 				g.drawRect(upperLeftX, upperLeftY, n.mGetWidth(), n.mGetHeight());
 
 				//draw the label
 				g.setColor(n.mGetLabelColor());
 				g.drawString(n.mGetLabel(), labelLeftX, labelLeftY);
 				break;
 			case TRIANGLE:
 				//TODO: recalculate label location to fit in the triangle.
 				int x[] = new int[3];
 				x[0] = upperLeftX; // left point x coordinte
 				x[1] = upperLeftX + n.mGetWidth(); //right point x coordinate
 				x[2] = upperLeftX + n.mGetWidth()/2; //middle point x coordinate
 
 				int y[] = new int[3];
 				y[0] = upperLeftY + n.mGetHeight(); //left point y coordinate
 				y[1] = upperLeftY + n.mGetHeight(); //right point y coordinate
 				y[2] = upperLeftY; //middle point y coordinate
 
 				g.setColor(n.mGetFillColor());
 				g.fillPolygon(x, y, 3);
 				g.setColor(n.mGetBorderColor());
 				g.drawPolygon(x, y, 3);
 
 				g.setColor(n.mGetLabelColor());
 				g.drawString(n.mGetLabel(), labelLeftX, labelLeftY);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * DrawEdge - Draws an edge between two nodes. The edge will dynamically choose one of 4 points on each
 	 *            node as the start/end point
 	 *
 	 * @param   g        The Graphics object the edge is drawn on
 	 * @param   e        The edge object to draw on g
 	 * @see     Edge
 	 */
 	public static void mDrawEdge(Graphics g, Edge e) {
 		//TODO: implement for non-straight edges
 		//TODO: implement for different edge styles/directions.
 		//TODO: implement label placement.
 		//TODO: figure out how we want to handle the case where the edge label is longer than the edge 
 		//      (if we need to recalculate node position etc.)
 		//TODO: account for triangle nodes (edges currently do not intersect at correct locations for triangles)
 
 		g.setColor(e.mGetEdgeColor());
 
 		int startX;
 		int startY;
 		int endX;
 		int endY;
 		int differenceX;
 		int differenceY;
 
 		Node startNode = e.mGetStartNode();
 		Node endNode = e.mGetEndNode();
 
 		int startNodeCenterX = startNode.mGetCenterX();
 		int startNodeCenterY = startNode.mGetCenterY();
 		int endNodeCenterX = endNode.mGetCenterX();
 		int endNodeCenterY = endNode.mGetCenterY();
 
 		//first thing we do is calculate which of the 4 points we will draw the edge from on each node
 		if(startNodeCenterX - endNodeCenterX > 0){
 			//we will either use the left, top, or bottom point
 			differenceX = startNodeCenterX - endNodeCenterX;
 			if(startNodeCenterY - endNodeCenterY > 0){
 				//we will either user the left point or the top point
 				differenceY = startNodeCenterY - endNodeCenterY;
 				if (differenceX > differenceY){ 
 					//startNodeX > endNodeX && startNodeY > endNodeY && x difference is bigger.
 
 					//use the left point on the start node and right point on the end node
 					startX = startNodeCenterX - startNode.mGetWidth()/2;
 					startY = startNodeCenterY;
 					endX = endNodeCenterX + endNode.mGetWidth()/2;
 					endY = endNodeCenterY;
 				}
 				else { // startNodex > endNodeX && startNodeY > endNodeY && y difference is bigger
 					//use the top point on the start node and bottom point on the end node
 					startX = startNodeCenterX;
 					startY = startNodeCenterY - startNode.mGetHeight()/2;
 					endX = endNodeCenterX;
 					endY = endNodeCenterY + endNode.mGetHeight()/2;
 				}
 			}
 			else{ // startNodeX > endNodeX && endNodeY >= startNodeY
 				//we will either use the left point or the bottom point
 				differenceY = endNodeCenterY - startNodeCenterY;
 				if (differenceX > differenceY) {
 					//startNodeX > endNodeX && endNodeY >= startNodeY && differenceX > differenceY 
 
 					//use the left point on the start node and the right point on the end node
 					startX = startNodeCenterX - startNode.mGetWidth()/2;
 					startY = startNodeCenterY;
 					endX =  endNodeCenterX + endNode.mGetWidth()/2;
 					endY = endNodeCenterY;
 				}
 				else{
 					//use the bottom point on the start node and the top point on the end node
 					startX = startNodeCenterX;
 					startY = startNodeCenterY + startNode.mGetHeight()/2;
 					endX = endNodeCenterX;
 					endY = endNodeCenterY - endNode.mGetHeight()/2;
 				}
 			}
 		}
 		else{
 			//we will either use the right, top, or bottom point on the start node
 			differenceX = endNodeCenterX - startNodeCenterX;
 			if(startNodeCenterY - endNodeCenterY > 0){
 				//use either right point or top point
 				differenceY = startNodeCenterY - endNodeCenterY;
 				if(differenceX > differenceY){
 					//use the right point on the start node and the left point on the end node
 					startX = startNodeCenterX + startNode.mGetWidth()/2;
 					startY = startNodeCenterY;
 					endX = endNodeCenterX - endNode.mGetWidth()/2;
 					endY = endNodeCenterY;
 				}
 				else{
 					//use the top point on the start node and the bottom point on the end node
 					startX = startNodeCenterX;
 					startY = startNodeCenterY - startNode.mGetHeight()/2;
 					endX = endNodeCenterX;
 					endY = endNodeCenterY + endNode.mGetWidth()/2;
 				}
 			}
 			else{
 				//use either right point or bottom point
 				differenceY = endNodeCenterY - startNodeCenterY;
 				if(differenceX > differenceY){
 					//use right point on the start node and the left point on the end node
 					startX = startNodeCenterX + startNode.mGetWidth()/2;
 					startY = startNodeCenterY;
 					endX = endNodeCenterX - endNode.mGetWidth()/2;
 					endY = endNodeCenterY;
 				}
 				else{
 					//use bottom point on the start node and the top point on the end node
 					startX = startNodeCenterX;
 					startY = startNodeCenterY + startNode.mGetHeight()/2;
 					endX = endNodeCenterX;
 					endY = endNodeCenterY - endNode.mGetHeight()/2;
 				}
 			}
 		}
 
 		int labelX = (startX + endX)/2 + 5;
 		int labelY = (startY +endY)/2 - 5;
 
 		switch (e.mGetEdgeStyle()){
 		case DOTTED:
 			BasicStroke dotted =  new BasicStroke(
 					1f, 
 					BasicStroke.CAP_ROUND, 
 					BasicStroke.JOIN_ROUND, 
 					1f, 
 					new float[] {2f}, 
 					0f);
 			Graphics2D g2 = (Graphics2D)g;
 			g2.setStroke(dotted);
 			g2.drawLine(startX, startY, endX, endY);
 			break;
 		case DASHED:
 			float dash1[] = {10.0f};
 			BasicStroke dashed =
 					new BasicStroke(1.0f,
 							BasicStroke.CAP_BUTT,
 							BasicStroke.JOIN_MITER,
 							10.0f, dash1, 0.0f);
 			Graphics2D gr2 = (Graphics2D)g;
 			gr2.setStroke(dashed);
 			gr2.drawLine(startX, startY, endX, endY);
 			break;
 		case SOLID:
 		default:
 			g.drawLine(startX, startY, endX, endY);
 			break;
 		}
 
 		mDrawEdgeLabel(g, e, startX, startY, endX, endY);
 
 		//KMW Note: for now we will only support one style of arrow. (a filled in triangle)
 		//          at some point we will need to support the other types.
 		switch(e.mGetDirection()){
 		case NODIRECTION:
 			//we are done. It will work
 			break;
 		case STARTDIRECTION:
 			//KMW Note: I'm having difficulty coming up with good code to do this.
 			//          I know we can use sin/cos/tan to calculate points to draw a
 			//          triangle, but every way I think of to do this we would need to
 			//          break it into 4 cases for each combination of node placements in
 			//          relation to each other, and this seems unnecessary to me.
 			/*int x[] = new int[3];
             int y[] = new int[3];
             x[0] = startX;
             y[0] = startY;
 
             x[1] = ;
             y[1] = ;
 
             x[3] = ;
             y[3] = ;
             g.drawPolygon(x, y, 3);*/
 			break;
 		case ENDDIRECTION:
 
 			break;
 		case DOUBLEDIRECTION:
 
 			break;
 		}
 
 	}
 
 	/**
 	 * Calculates the edge label position and draws it to the panel.
 	 * @param g - the graphics object doing the drawing.
 	 * @param e - the Edge to calculate the label position for.
 	 * @param startX - the start point of the edge's x coordinate
 	 * @param startY - the start point of the edge's y coordinate
 	 * @param endX - the end point of the edge's x coordinate
 	 * @param endY - the end point of the edge's y coordinate
 	 */
 	public static void mDrawEdgeLabel(Graphics g, Edge e, int startX, int startY, int endX, int endY){
 		g.setColor(e.mGetLabelColor());
 
 		//calculate the label position.
 		//KMW Note: this is not working correctly yet. Somtimes the edge lables are going throught the middle
 		//          of the edge.
 		int labelX;
 		int labelY;
 
 		if(startX < endX){
 			labelX = (startX + endX)/2 + 5;
 			if(startY > endY){
 				labelY = (startY + endY)/2 + 5;
 			}
 			else{
 				labelY = (startY + endY)/2 - 5;
 			}
 		}
 		else{
 			labelX = (startX + endX)/2 - 5;
 			if(startY > endY){
 				labelY = (startY + endY)/2 + 5;
 			}
 			else{
 				labelY = (startY + endY)/2 - 5;
 			}
 		}
 		g.drawString(e.mGetLabel(), labelX, labelY);
 	}
 
 	/**
 	 * mDrawSelectedNode - Draws a Cyan Bounding Box arround a node object
 	 *                     to indicate that the object is selected
 	 *
 	 * @param   g        The Graphics object the selection is drawn on
 	 * @param   e        The node object to draw the selection arround
 	 * @see     Node
 	 * @see     SelectedItems
 	 */
 	public static void mDrawSelectedNode(Graphics g, Node n) {
 		Graphics2D g2d = (Graphics2D)g;
 		float dash1[] = {10.0f};
 		BasicStroke dashed =
 				new BasicStroke(1.0f,
 						BasicStroke.CAP_BUTT,
 						BasicStroke.JOIN_MITER,
 						10.0f, dash1, 0.0f);
 		g2d.setStroke(dashed);
 		g2d.setColor(Color.cyan);
 		g2d.drawRect(n.mGetUpperLeftX()-5, n.mGetUpperLeftY()-5, n.mGetWidth()+10, n.mGetHeight()+10);
 	}
 
 	/**
 	 * savePNG - Save a graph as a .png file
 	 * 
 	 * @param panel - the object holding the graph drawing
 	 * @param path - location to save the image
 	 */
 	public static void mSavePNG(GraphPanel panel, String path) {
 		Dimension size = panel.getSize();  //panel == JPanel
 		BufferedImage myImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = myImage.createGraphics();
 		panel.paint(g2);
 		g2.dispose();
 		try
 		{
 			ImageIO.write(myImage, "png", new File(path));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * saveJPG - Save a graph as a .jpg file
 	 * 
 	 * @param panel - the object holding the graph drawing
 	 * @param path - location to save the image
 	 */
 	public static void mSaveJPG(GraphPanel panel, String path) {
 		Dimension size = panel.getSize();  //panel == JPanel
 		BufferedImage myImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = myImage.createGraphics();
 		panel.paint(g2);
 		g2.dispose();
 		try
 		{
 			ImageIO.write(myImage, "jpg", new File(path));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * saveBMP - Save a graph as a .bmp file
 	 * 
 	 * @param panel - the object holding the graph drawing
 	 * @param path - location to save the image
 	 */
 	public static void mSaveBMP(GraphPanel panel, String path) {
 		Dimension size = panel.getSize();  //panel == JPanel
 		BufferedImage myImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = myImage.createGraphics();
 		panel.paint(g2);
 		g2.dispose();
 		try
 		{
 			// This file location only works for Jeff's Mac laptop...we can make a filechooser later
 			ImageIO.write(myImage, "bmp", new File(path));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * saveGIF - save a graph as a .gif file
 	 * 
 	 * @param panel - the object holding the graph drawing
 	 * @param path - location to save the image
 	 */
 	public static void mSaveGIF(GraphPanel panel, String path) {
 		Dimension size = panel.getSize();  //panel == JPanel
 		BufferedImage myImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = myImage.createGraphics();
 		panel.paint(g2);
 		g2.dispose();
 		try
 		{
 			// This file location only works for Jeff's Mac laptop...we can make a filechooser later
 			ImageIO.write(myImage, "gif", new File(path));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
