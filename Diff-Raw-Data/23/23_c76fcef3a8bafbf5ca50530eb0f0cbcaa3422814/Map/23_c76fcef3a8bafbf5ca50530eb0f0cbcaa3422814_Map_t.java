 package core;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.io.File;
 import java.util.ArrayList;
import java.util.HashSet;
 import java.util.List;
 import java.util.Scanner;
import java.util.Set;
 import java.util.Stack;
 
 import util.NodeArray;
 import util.RoadArray;
 import util.TrieTree;
 
 /**
  * A Map contains information on all objects that are used to construct and draw
  * a map
  *
  * @author Oliver Greenaway
  *
  */
 public class Map {
 
 	private double offsetX, offsetY;
 	private double zoomLevel;
 	private double diffX, diffY, minX, minY;
 	private String directory;
 	private NodeArray nodes = new NodeArray();
 	private RoadArray roads = new RoadArray();
 	private List<Polygon> polygons = new ArrayList<Polygon>();
 	private List<Segment> markedPath = new ArrayList<Segment>();
	private Set<Node> articulations = new HashSet<Node>();
 	private boolean polygonsExist;
 	private Road selectedRoad;
 	private Node selectedSourceNode;
 	private Node selectedDestNode;
 	private TrieTree roadNames = new TrieTree();
 
 	/**
 	 * Initializes all Nodes, Roads and Segments into a graphed map using the
 	 * information contained in the directory
 	 *
 	 * @param dir
 	 *            The directory containing the data files
 	 */
 	public Map(String dir) {
 		directory = dir;
 		zoomLevel = 2;
 		polygonsExist = true;
 		initNodes();
 		initRoads();
 		initSegments();
 		initPolygons();
 		buildGraph();
 	}
 
 	/**
 	 * Reads all data from files and creates Node objects
 	 */
 	public void initNodes() {
 		double minX = Double.MAX_VALUE;
 		double maxX = Double.MIN_VALUE;
 		double minY = Double.MAX_VALUE;
 		double maxY = Double.MIN_VALUE;
 		try {
 			File file = new File(directory + "nodeID-lat-lon.tab");
 			if (file.exists()) {
 				Scanner scan = new Scanner(file);
 				while (scan.hasNextLine()) {
 					String line = scan.nextLine();
 					String[] columns = line.split("\t");
 					int ID = Integer.parseInt(columns[0]);
 					double y = latToY(Double.parseDouble(columns[1]));
 					double x = lonToX(Double.parseDouble(columns[2]));
 					if (x < minX) {
 						minX = x;
 					}
 					if (x > maxX) {
 						maxX = x;
 					}
 					if (y < minY) {
 						minY = y;
 					}
 					if (y > maxY) {
 						maxY = y;
 					}
 					nodes.add(new Node(ID, x, y));
 				}
 				nodes.sort();
 				scan.close();
 				diffX = maxX - minX;
 				diffY = maxY - minY;
 				this.minX = minX;
 				this.minY = minY;
 				offsetX = (0.0 - minX);
 				offsetY = (0.0 - minY);
 				zoomLevel = 800 / diffX;
 			} else {
 				Mapper.textArea.setText("File Read Error\n");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Reads all data from files and creates Road objects
 	 */
 	public void initRoads() {
 		try {
 			File file = new File(directory + "roadID-roadInfo.tab");
 			if (file.exists()) {
 				Scanner scan = new Scanner(file);
 				scan.nextLine();
 				while (scan.hasNextLine()) {
 					String line = scan.nextLine();
 					String[] columns = line.split("\t");
 					roads.add(new Road(toInt(columns[0]), toInt(columns[1]),
 							columns[2], columns[3], toInt(columns[4]),
 							toInt(columns[5]), toInt(columns[6]),
 							toInt(columns[7]), toInt(columns[8]),
 							toInt(columns[9])));
 				}
 				roads.sort();
 				scan.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		for (Road r : roads) {
 			roadNames.add(r);
 		}
 	}
 
 	/**
 	 * Reads all data from files and creates Segment objects linking them to
 	 * roads
 	 */
 	public void initSegments() {
 		try {
 			File file = new File(directory
 					+ "roadSeg-roadID-length-nodeID-nodeID-coords.tab");
 			if (file.exists()) {
 				Scanner scan = new Scanner(file);
 				scan.nextLine();
 				while (scan.hasNextLine()) {
 					String line = scan.nextLine();
 					String[] columns = line.split("\t");
 					Road road = roads.get(Integer.parseInt(columns[0]));
 					if (road != null) {
 						Segment s = new Segment(Double.parseDouble(columns[1]),
 								Integer.parseInt(columns[2]),
 								Integer.parseInt(columns[3]), road.isOneWay());
 						for (int i = 4; i < columns.length; i += 2) {
 							double x = lonToX(Double
 									.parseDouble(columns[i + 1]));
 							double y = latToY(Double.parseDouble(columns[i]));
 							s.addPoint(x, y);
 						}
 						road.addSegment(s);
 					} else {
 						System.err.println(columns[0] + " not present");
 					}
 				}
 				scan.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Reads data from the polygon file if one exists, if one one does not exit
 	 * then this is flagged so that polygons are marked as not present
 	 */
 	public void initPolygons() {
 		try {
 			File file = new File(directory + "polygon-shapes.mp");
 			if (file.exists()) {
 				Scanner scan = new Scanner(file);
 				while (scan.hasNextLine()) {
 					List<String> details = new ArrayList<String>();
 					details.add(scan.nextLine());
 					while (!details.get(details.size() - 1).equals("[END]")
 							&& scan.hasNextLine()) {
 						details.add(scan.nextLine());
 					}
 					polygons.add(new Polygon(details));
 				}
 				scan.close();
 			} else {
 				this.polygonsExist = false;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Links all nodes with roads and segments
 	 */
 	public void buildGraph() {
 		for (Road r : roads) {
 			r.connect(nodes);
 		}
 	}
 
 	/**
 	 * Draws all the map components to the graphics object
 	 *
 	 * @param g
 	 *            The graphics object to be drawn to
 	 */
 	public void draw(Graphics2D g) {
 		g.setColor(Color.white);
 		g.fillRect((int) ((minX + offsetX) * zoomLevel),
 				(int) ((minY + offsetY) * zoomLevel),
 				(int) (diffX * zoomLevel), (int) (diffY * zoomLevel));
 		if (polygonsExist) {
 			for (Polygon p : polygons) {
 				p.draw(g, offsetX, offsetY, zoomLevel);
 			}
 		}
 		for (Road r : roads) {
 			r.draw(g, offsetX, offsetY, zoomLevel);
 		}
 
 		for (Node n : nodes) {
 			n.draw(g, offsetX, offsetY, zoomLevel);
 		}
 		for(Node n : articulations){
 			n.drawArticulation(g, offsetX, offsetY, zoomLevel);
 		}
 		Mapper.textArea.setText("");
 		if (selectedRoad != null) {
 			Mapper.textArea.setText("Road Details:\n"
 					+ selectedRoad.getDetails()+"\n\n");
 		}
 		if (selectedSourceNode != null) {
 			Mapper.textArea.append("Source Intersection Details:\n"
 					+ selectedSourceNode.getDetails()+"\n\n");
 		}
 		if (selectedDestNode != null) {
 			Mapper.textArea.append("Destination Intersection Details:\n"
 					+ selectedDestNode.getDetails()+"\n\n");
 		}
 		if(!markedPath.isEmpty()){
 			Mapper.textArea.append("Route Details:"+roadsAndDistance(markedPath));
 		}
 	}
 
 	private String roadsAndDistance(List<Segment> segments) {
 		String details = "";
 		String curName = "";
 		double curLength = 0;
 		for(int i=segments.size()-1; i>=0; i--){
 			Segment cur = segments.get(i);
 			double length = cur.getAccurLength();//Math.round(cur.getAccurLength()*100)/100;
 			if(cur.getName().equals(curName) || curName.equals("")){
 				curName = cur.getName();
 				curLength += length;
 			}else{
 				curLength = ((double)((int)(curLength*100)))/100;
 				details += "\n"+curName+": "+curLength+"km";
 				curName = cur.getName();
 				curLength = length;
 			}
 		}
 		return details;
 	}
 
 	/**
 	 * Adjust the offset of the map by the given x and y positions
 	 *
 	 * @param x
 	 *            The distance to move in the x direction
 	 * @param y
 	 *            The distance to move in the y direction
 	 */
 	public void moveMap(double x, double y) {
 		offsetX -= x / zoomLevel;
 		offsetY -= y / zoomLevel;
 	}
 
 	/**
 	 * Adjust the zoom level by 0.5 per notch rotated
 	 *
 	 * @param notches
 	 *            The number of mouse wheel notches moved
 	 */
 	public void zoom(int notches) {
 		zoomLevel += (double) notches / 2;
 		if (zoomLevel < 1) {
 			zoomLevel = 1;
 		}
 	}
 
 	/**
 	 * Checks all nodes to see which is closest to the click and select the node
 	 *
 	 * @param x
 	 *            The mouseX coordinate
 	 * @param y
 	 *            The mouseY coordinate
 	 */
 	public void clickedSourceNode(int x, int y) {
 		if (selectedSourceNode != null) {
 			selectedSourceNode.setSelect(false);
 		}
 		Node closest = null;
 		double closestDistance = Double.MAX_VALUE;
 		int count = 0;
 		for (Node n : nodes) {
 			double dist = n.getDist(x, y, offsetX, offsetY, zoomLevel);
 			if (dist < closestDistance) {
 				closestDistance = dist;
 				closest = n;
 			}
 		}
 		selectedSourceNode = closest;
 		if (selectedSourceNode != null) {
 			selectedSourceNode.setSelect(true);
 		}
 		updatePath();
 	}
 
 	/**
 	 * Checks all nodes to see which is closest to the click and select the node
 	 *
 	 * @param x
 	 *            The mouseX coordinate
 	 * @param y
 	 *            The mouseY coordinate
 	 */
 	public void clickedDestNode(int x, int y) {
 		if (selectedDestNode != null) {
 			selectedDestNode.setSelect(false);
 		}
 		Node closest = null;
 		double closestDistance = Double.MAX_VALUE;
 		int count = 0;
 		for (Node n : nodes) {
 			double dist = n.getDist(x, y, offsetX, offsetY, zoomLevel);
 			if (dist < closestDistance) {
 				closestDistance = dist;
 				closest = n;
 			}
 		}
 		selectedDestNode = closest;
 		if (selectedDestNode != null) {
 			selectedDestNode.setSelect(true);
 		}
 		updatePath();
 	}
 
 	/**
 	 * Using A* algorithm, the fastest path is found between two intersections
 	 * if two intersections are selected
 	 */
 	private void updatePath() {
 		if (selectedDestNode != null && selectedSourceNode != null) {
 			for (Segment s : markedPath) {
 				s.setSelect(false);
 			}
 			markedPath = new ArrayList<Segment>();
 			List<Node> visited = new ArrayList<Node>();
 			List<Object[]> fringe = new ArrayList<Object[]>(); // [Node,prevNode,pathLength,adjustedLength]
 			fringe.add(new Object[]{selectedSourceNode,null,0,(int)(selectedSourceNode.getPoint().distance(selectedDestNode.getPoint())),null});
 			while(fringe.size() > 0){
 				int indexToRemove = 0;
 				int minLength = Integer.MAX_VALUE;
 				for(int i=0; i<fringe.size(); i++){
 					if((Integer)(fringe.get(i)[3]) < minLength){
 						minLength = (Integer)(fringe.get(i)[3]);
 						indexToRemove = i;
 					}
 				}
 				Object[] working = fringe.remove(indexToRemove);
 				if(!visited.contains(working[0])){
 					visited.add((Node)working[0]);
 					if(working[0] == selectedDestNode){
 						Object[] temp = working;
 						while(temp[4] != null){
 							((Segment)temp[4]).setSelect(true);
 							markedPath.add((Segment)temp[4]);
 							temp = (Object[])temp[1];
 						}
 						return;
 					}else{
 						List<Segment> neighbours = ((Node)working[0]).getNeighbours();
 						for(Segment n : neighbours){
 							Node otherNode = n.getOppositeNode((Node)working[0], true);
 							if(!(otherNode == null || visited.contains(otherNode))){
 								fringe.add(new Object[]{otherNode,working,(Integer)working[2]+n.getLength(),(int)(otherNode.getPoint().distance(selectedDestNode.getPoint())),n});
 							}
 						}
 					}
 				}
 			}
 		} else {
 			return;
 		}
 	}
 
 
 	public void findArticulations(){
 		if(selectedSourceNode != null){
 			this.articulations = getArticulations(selectedSourceNode, 0, selectedSourceNode);
 		}else if(selectedDestNode != null){
 			this.articulations = getArticulations(selectedDestNode, 0, selectedDestNode);
 		}else{
 			Mapper.textArea.setText("Must select a Intersection before finding choke points");
 		}
		System.out.println(articulations.size());
 	}
 
 	/**
 	 * Works through the graph of nodes finding all articulation points
 	 * @param startNode The node to start from
 	 * @param depth The starting depth
 	 * @param root The root node
 	 * @return An arrayList of Nodes that are articulation points
 	 */
	private Set<Node> getArticulations(Node startNode, int depth, Node root){
 		for(Node n : nodes){
 			n.setDepth(Integer.MAX_VALUE);
 		}
		Set<Node> articulations = new HashSet<Node>();
 		Stack<Object[]> stack = new Stack<Object[]>();
 
 		stack.push(new Object[]{startNode, 1, 1, new Object[]{root, 0, 0, null, null}, null}); //[[startNode,nodeDepth],depth,reach,[previousElement,previousDepth],children]
 		while(!stack.isEmpty()){
 			Object[] element = stack.peek();
 			Node curNode = (Node)element[0];
 			if(element[4] == null){
 				//first time
 				curNode.setDepth((Integer)element[1]);
 				element[2] = element[1];
 				element[4] = new ArrayList<Node>();
 				for(Segment s : curNode.getNeighbours()){
 					Node neighbour = s.getOppositeNode(curNode, false);
 					if(neighbour != (Node)((Object[])element[3])[0]){
 						((List<Node>)(element[4])).add(neighbour);
 					}
 				}
 			}else if(!((List<Node>)(element[4])).isEmpty()){
 				//children to process
 				Node child = ((List<Node>)element[4]).remove(0);
 				if(child.getDepth() < Integer.MAX_VALUE){
 					element[2] = Math.min(((Integer)element[2]),child.getDepth());
 				}else{
 					stack.push(new Object[]{child,curNode.getDepth()+1,0,element,null});
 				}
 			}else{
 				//last time
 				if(curNode != startNode){
 					if((Integer)element[2] >= (Integer)((Object[])element[3])[1]){
 						articulations.add((Node)((Object[])element[3])[0]);
 					}
 					((Object[])element[3])[2] = Math.min((Integer)((Object[])element[3])[2], (Integer)element[2]);
 				}
 				stack.pop();
 			}
 		}
 
 		return articulations;
 	}
 
 	/**
 	 * Returns the 10 closest road names to the given string
 	 *
 	 * @param text
 	 *            the string to be searched for
 	 * @return A list of at most 10 roads
 	 */
 	public List<Road> getTen(String text) {
 		return roadNames.getTen(text);
 	}
 
 	/**
 	 * Deselects any currently selected roads and selects the given road
 	 *
 	 * @param r
 	 *            the road to be selected
 	 */
 	public void setSelectedRoad(Road r) {
 		if (selectedRoad != null) {
 			selectedRoad.setSelect(false);
 		}
 		selectedRoad = r;
 		if (selectedRoad != null) {
 			selectedRoad.setSelect(true);
 		}
 	}
 
 	/**
 	 * converts latitude into Y coordinates
 	 *
 	 * @param latitude
 	 *            The degree of latitude
 	 * @return The Y coordinate
 	 */
 	private double latToY(double latitude) {
 		return Math.abs(latitude) * 111.0;
 	}
 
 	/**
 	 * converts longitude into X coordinates
 	 *
 	 * @param longitude
 	 *            The degree of longitude
 	 * @return The X coordinate
 	 */
 	private double lonToX(double longitude) {
 		return Math.abs(longitude) * 88.649;
 	}
 
 	/**
 	 * converts a string to an int
 	 *
 	 * @param s
 	 *            The string to be converted
 	 * @return The int representation of the string, 0 is a conversion error is
 	 *         encountered
 	 */
 	private int toInt(String s) {
 		try {
 			return Integer.parseInt(s);
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 }
