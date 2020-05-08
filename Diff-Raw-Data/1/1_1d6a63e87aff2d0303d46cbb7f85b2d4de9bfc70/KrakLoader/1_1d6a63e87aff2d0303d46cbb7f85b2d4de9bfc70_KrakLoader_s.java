 package krakLoader;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryMXBean;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.PriorityQueue;
 
 import Part1.Edge;
 import Part1.Node;
 import Part1.RoadSegment;
 import Part1.Window;
 import QuadTree.Point;
 import QuadTree.QuadTree;
 
 import Part1.Graph;
 
 /**
  * This class can load the data files from krak into a graph representation
  * 
  * @author Peter Tiedemann petert@itu.dk
  */
 public class KrakLoader {
 	private static KrakLoader loader;
 	private ArrayList<Node> nodes;
 	private ArrayList<Edge> longestRoads;
 	private final String nodeFile, edgeFile;
 	private static double maxX = 0, maxY = 0, minX = -1, minY = -1, maxLength = 0;
 
 	/**
 	 * Constructor for the KrakLoader class. The constructor creates an
 	 * ArrayList containing all nodes which are represented within the "<br>
 	 * kdv_node_unload.txt</br>" file.
 	 * 
 	 * @param nodeFile
 	 *            The path which leads to the "<br>
 	 *            kdv_node_unload.txt</br>" file.
 	 * @param edgeFile
 	 *            The path which leads to the "<br>
 	 *            kdv_unload.txt</br>" file.
 	 */
 	private KrakLoader(String nodeFile, String edgeFile) {
 		this.nodeFile = nodeFile;
 		this.edgeFile = edgeFile;
 	}
 	
 	public static KrakLoader use(String nodeFile, String edgeFile) {
 		if(loader == null) return new KrakLoader(nodeFile, edgeFile);
 		else return loader;
 	}
 
 	public void createNodeList(){
 		// open the file containing the list of nodes
 		try {
 		BufferedReader br = new BufferedReader(new FileReader(nodeFile));
 		
 		br.readLine(); // discard names of columns which is the first line
 
 		String line = br.readLine();
 
 		// An array list containing the nodes we find in the file
 		nodes = new ArrayList<Node>();
 		nodes.add(null);
 		while (line != null) {
 			// Splits "line" by ',' and parses the id, x and y values to
 			// KrakNode
 			String[] lineArray = line.split(",");
 			double x = Double.parseDouble(lineArray[3]);
 			if (maxX < x)
 				maxX = x;
 			double y = Double.parseDouble(lineArray[4]);
 			if (maxY < y)
 				maxY = y;
 			int id = Integer.parseInt(lineArray[1]);
 			if (minX > x || minX == -1)
 				minX = x;
 			if (minY > y || minY == -1)
 				minY = y;
 
 			nodes.add(new Node(x, y, id));
 			line = br.readLine();
 		}
 		
 		// The coordinates of every node is corrected for the offset
 		Node.setXOffset(minX);
 		Node.setYOffset(minY);
 		System.out.println("Width of map: " + (maxX-minX));
 		System.out.println("Height of map: " + (maxY-minY));
 		br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	public Graph createGraphAndLongestRoadsList(int longestRoadsFloor) throws IOException {
 
 		System.out.println("Adding " + (nodes.size()-1) + " nodes to graph");
 
 		// Create a graph on the nodes
 		Graph graph = new Graph(nodes.size());
 
 		// Reads the "kdv_unload.txt" file into the buffer.
 		BufferedReader br = new BufferedReader(new FileReader(edgeFile));
 
 		br.readLine(); // again discarding column names
 		String line = br.readLine();
 		
 		while (line != null) {
 			String[] lineArray = line.split(",(?! |[a-zA-ZæÆøØåÅ])"); // Regex matches ',' not followed by space of letters.
 			Node fromNode = nodes.get(Integer.parseInt(lineArray[0]));
 			Node toNode = nodes.get(Integer.parseInt(lineArray[1]));
 			double length = Double.parseDouble(lineArray[2]);
 			int type = Integer.parseInt(lineArray[5]);
 			Edge edge = new Edge(fromNode, toNode, length, type); // Creates an edge.
 			if (length > longestRoadsFloor) longestRoads.add(edge);
 			graph.addEdge(edge); // Adds the newly created edge object to the graph.
 			line = br.readLine();
 		}
 		br.close();
 		System.out.println("Max length: " + maxLength);
 		
 		return graph;
 	}
 
 	/**
 	 * Creates and 
 	 * 
 	 * @return A QuadTree based on the nodes arrayList.
 	 */
 	public QuadTree createQuadTree() {
 		if(nodes.size() == 0) createNodeList();
 		// Create QuadTree
 		QuadTree QT = new QuadTree(3, maxX - minX, maxY - minY);
 		for (int i = 1; i < nodes.size(); i++) { //For loop start at index 1 because index 0 is null.
 			QT.insert(nodes.get(i));
 		}
 
 		return QT;
 	}
 
 	public static double getMaxX() {
 		return maxX;
 	}
 
 	public static double getMaxY() {
 		return maxY;
 	}
 
 	public static double getMinX() {
 		return minX;
 	}
 
 	public static double getMinY() {
 		return minY;
 	}
 	
 	public List<Edge> getLongestRoads() {
 		return longestRoads;
 	}
 
 	public static void main(String[] args) throws IOException {
 		Long startTime = System.currentTimeMillis();
 		KrakLoader krakLoader = KrakLoader.use("kdv_node_unload.txt",
 				"kdv_unload.txt");
 		krakLoader.createNodeList();
 		Graph graph = krakLoader.createGraphAndLongestRoadsList(10000);
 		QuadTree QT = krakLoader.createQuadTree();
 		krakLoader = null;
 		Long endTime = System.currentTimeMillis();
 		Long duration = endTime - startTime;
 		System.out.println("Time to create Nodelist, Graph and QuadTree: " + duration/1000.0);
 		startTime = System.currentTimeMillis();
 		List<Node> list = QT.query(0, 0, maxX-minX, maxY-minY);
 		System.out.println("Length of the result from full query: " + list.size());
 		for (Node n : list) {
 			Iterable<Edge> edges = graph.adjOut(n.getKdvID());
 			for (Edge e : edges) {
 				double x1 = n.getXCord();
 				double y1 = n.getYCord();
 				double x2 = e.getToNode().getXCord();
 				double y2 = e.getToNode().getYCord();
 			}
 		}
 		endTime = System.currentTimeMillis();
 		duration = endTime - startTime;
 		System.out.println("Time to query all nodes and find their neighbours: " + duration/1000.0);
 		System.out.printf("Graph has %d edges%n", graph.getE());
 		MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
 		System.out.printf("Heap memory usage: %d MB%n", mxbean
 				.getHeapMemoryUsage().getUsed() / (1000000));
 	}
 
 }
