 package Part1;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryMXBean;
 import java.util.List;
 
 import krakLoader.KrakLoader;
 import QuadTree.QuadTree;
 
 public class WindowHandler {
 	static List<Node> nodes;
 	static QuadTree QT;
 	static Graph graph;
 	static Window window;
 
 	/**
 	 * Calculates the pixel coordinate of a given geo coordinate, and returns a new coordinate set with int values
 	 * 
 	 * @param geoCoord  The geo coordinate
 	 * @return 			A new Coordinate set of int values (a pixel coordinate set)
 	 */
 //	private Coordinate geoToPixel(Coordinate geoCoord) {
 //		double x = geoCoord.getXcoordDouble();
 //		double y = geoCoord.getYcoordDouble();
 //		
 //		int intX = (int) Math.round(( (x - 450000 + Window.origoX) * (Window.windowSize / 200) * Window.zoomFactor) / 1000);
 //		int intY = (int) Math.round(((6200000 + y + Window.origoY) * (Window.windowSize / 200) * Window.zoomFactor) / 1000);
 //		
 //		return new Coordinate(intX, intY);
 //	}
 	
 	/**
 	 * Calculates the UMT coordinate of a given on-screen pixel coordinate, and returns a new coordinate set with double values
 	 * 
 	 * @param pixelCoord 	The coordinate of the pixel
 	 * @return 				The UMT coordinate of said pixel
 	 */	
 //	private Coordinate pixelToGeo(Coordinate pixelCoord) {
 //		int x = pixelCoord.getXcoordInt();
 //		int y = pixelCoord.getYcoordInt();
 //		
 //		double doubleX = (double) (( (x + 450000 - Window.origoX) / (Window.windowSize / 200) / Window.zoomFactor) * 1000);
 //		double doubleY = (double) (((6200000 + y - Window.origoY) / (Window.windowSize / 200) / Window.zoomFactor) * 1000);
 //		
 //		return new Coordinate(doubleX, doubleY);
 //	}
 	
 	// Converts X-coordinates to pixel-values
 	private static int geoXToPixel(double geoX) {
 		return (int) Math.round(( (geoX + Window.offsetX) * (Window.windowSize / 3)
 									* Window.use().getZoomFactor()) / 100000);
 	}
 	
 	
 	// Converts Y-coordinates to pixel-values
 	private static int geoYToPixel(double geoY) {
 		return (int) Math.round((( geoY + Window.offsetY) * (Window.windowSize / 3)
 									* Window.use().getZoomFactor()) / 100000);
 	}
 	
 	//Adds road segments to arrayList within Map class
 	public static void calculatePixels() {
 		Long startTime = System.currentTimeMillis();
 		for (Node n: nodes) {
 			Iterable<Edge> edges = graph.adjOut(n.getKdvID());
 			for (Edge e : edges) {
 				if (e.getType() != 5) break;
 				double x1 = n.getXCord();
 				double y1 = n.getYCord();
 				double x2 = e.getToNode().getXCord();
 				double y2 = e.getToNode().getYCord();
 				int pixelX1 = geoXToPixel(x1);
 				int pixelX2 = geoXToPixel(x2);
 				int pixelY1 = window.getHeight() - geoYToPixel(y1);
 				int pixelY2 = window.getHeight() - geoYToPixel(y2);
 				Map.use().addRoadSegment(new RoadSegment(pixelX1, pixelY1, pixelX2, pixelY2, getRoadSegmentColor(e.getType()), selectRoadWidth()));
 			}
 		}
 		Long endTime = System.currentTimeMillis();
 		Long duration = endTime - startTime;
 		System.out.println("Time to calculate pixels: " + (duration/1000.0) + "s");
 	}
 	
 	// determine what color the drawn line has
 	private static Color getRoadSegmentColor(int TYP){
 		switch(TYP) {
 		case 1 : return Color.red;		// Motor ways 	
 		case 2 : return Color.red;		// Motor traffic road
 		case 3 : return Color.blue; 	// Primary roads > 6 m 
 		case 4 : return Color.blue;		// Secondary roads > 6 m
 		case 5 : return Color.black;	// Roads between 3-6 meters
 		case 8 : return Color.green;	// paths
 		default : return Color.pink; 	// everything else
 		}
 	}
 	
 	// determine road width by zoom factor
 	private static int selectRoadWidth(){
 		int roadWidth = 1;
 		if(Window.zoomFactor > 1.5) roadWidth = 10;
 		if(Window.zoomFactor > 3)   roadWidth = 10;
 		if(Window.zoomFactor > 5)   roadWidth = 10;
 		return roadWidth;
 	}
 	
 
 	public static void main(String[] args) throws IOException {
 		//Initializing of data from KrakLoader
 		
 		Long startTime = System.currentTimeMillis();
 		KrakLoader krakLoader = KrakLoader.use("kdv_node_unload.txt",
 				"kdv_unload.txt");
 		krakLoader.createNodeList(); 				//ArraylList with Nodes
		graph = krakLoader.createGraph();			//Makes graph object
 		QT = krakLoader.createQuadTree();			//Makes and returns a quadTree
 		krakLoader = null;							//Avoid loithering
 		Long endTime = System.currentTimeMillis();
 		Long duration = endTime - startTime;
 		System.out.println("Time to create Nodelist, Graph and QuadTree: " + duration/1000.0 + " s");
 		
 		//
 		
 		startTime = System.currentTimeMillis();
 		nodes = QT.query(0, 0, KrakLoader.getMaxX()-KrakLoader.getMinX(), 
 							KrakLoader.getMaxY()-KrakLoader.getMinY());
 		endTime = System.currentTimeMillis();
 		duration = endTime - startTime;
 		System.out.println("Time to query all nodes and find their neighbours: " 
 														+ duration/1000.0 + " s");
 		System.out.println("Length of the result from full query: " + nodes.size());
 		window = Window.use();
 		
 		calculatePixels();
 		
 		System.out.printf("Graph has %d edges%n", graph.getE());
 		//MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
 		//System.out.printf("Heap memory usage: %d MB%n", mxbean
 		//		.getHeapMemoryUsage().getUsed() / (1000000));
 	}
 	
 }
