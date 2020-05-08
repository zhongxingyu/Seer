 package Part1;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 
 
 public class Map extends JPanel {
 	/**
 	 * Map is a JPanel with the lines drawn
 	 */
 	private ArrayList<RoadSegment> segments, borderSegments;
 	private ArrayList<DrawableItem> path;
 	private static ArrayList<CoastPoint[]> coast, lake, island, border; 
 	private static Map instance = null;
 	private ArrayList<Flag> flags;
 	private Image offScreen = null;
 	private Graphics offgc;
 	
 	private double pathLength, driveTime;
 	
 	/**
 	 * Constructor of the map class.
 	 */
 	private Map() {
 	}
 
 	/**
 	 * Method to ensure that only one instance of the map class
 	 * will exist at any time. 
 	 * An new instance will only be initialized if and only if 
 	 * the instance field has the null.
 	 * 
 	 * @return The current used instance of the map class.
 	 */
 	public static Map use() {
 		if(instance == null) {
 			instance = new Map();
 			instance.path = new ArrayList<DrawableItem>();
 			instance.flags = new ArrayList<Flag>();
 		}
 		return instance;
 	}
 
 	/**
 	 * This method flips the buffered image onto the screen
 	 * based on its current displacement.
 	 */
 	public void paint(Graphics g) {
 		g.drawImage(offScreen, Window.use().getMousePanX(), Window.use().getMousePanY(), this);
 	}
 
 	/**
 	 * PaintComponent method used to draw all needed components
 	 * on the map object.
 	 */
 	public void paintComponent(Graphics g) {
 		// Draw coast line, lakes, islands, and borders.
 		long time = System.currentTimeMillis();
 		drawShore(coast, UIManager.getColor("Panel.background"), g);
 		drawShore(lake, Window.use().getBackground(), g);
 		drawShore(island, UIManager.getColor("Panel.background"), g);
 		drawBorder(border, Color.RED, g);
 		
 		// Draw road borders
 		for (RoadSegment b : borderSegments) {
 			if (b == null)
 				continue;
 			b.paintComponent(g);
 		}
 		// Draw roads
 		for (RoadSegment r : segments) {
 			if (r == null)
 				continue;
 			r.paintComponent(g);
 		}
 		System.out.println("draw road :"+(System.currentTimeMillis() - time));
 		// Draw the path
 		for (DrawableItem r : path)
 			r.paintComponent(g);
 		for (Flag f : flags)
 			f.paintComponent(g);
 	}
 
 	/**
 	 * Draws the coastline of the map based on specified inputs.
 	 * 
 	 * @param arg An array list of CoastPoint's defining the shape of the polygon.
 	 * @param c A java.awt.Color object specifying the color of the polygons.
 	 * @param g A java.awt.Graphics object used to draw the polygons.
 	 */
 	private void drawShore(ArrayList<CoastPoint[]> arg, Color c, Graphics g) {		
 		ArrayList<Polygon> poly = new ArrayList<Polygon>();
 		Polygon current = new Polygon();
 		for(CoastPoint[] cp : arg) {
 			current = new Polygon();
 			for(int i = 0; i < cp.length; i++) {
 				current.addPoint(Equation.calcPixelX(cp[i].getX()-DataReader.getMinX()), Equation.calcPixelY(cp[i].getY()-DataReader.getMinY()));
 			}
 			//Only add polygons if they fulfill any of three things.
 			// - The polygon intersects with the bounds of the view area.
 			// - The polygon is inside the bounds of the view area. 
 			// - The view area is inside the bounds of the polygon.
 			if (Map.use().getBounds().contains(current.getBounds()) || current.getBounds().contains(Map.use().getBounds()) || current.intersects(Map.use().getBounds())) {
 				poly.add(current);
 			}
 		}
 		g.setColor(c);
 		for(Polygon fp : poly) {
 			g.fillPolygon(fp);
 		}
 		Graphics2D D2 = (Graphics2D) g;
 		D2.setColor(Color.GRAY);
 		//Checks whether antialiasing should be used or not.
 		if (RoadSegment.getZoomLevel() > 3) {
 			D2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
 			D2.setStroke(new BasicStroke(1.5f));
 		}
 		for(Polygon op : poly) {
 			D2.drawPolygon(op);
 		}
 	}
 
 	/**
 	 * Creates and draws the polygons used to shape the coastline.
 	 * 
 	 * @param arrBorder ArratList of CostPoint arrays containing the points of the polygons.
 	 * @param c	The color used to fill the polygon.
 	 * @param g	The Graphics object used to draw the components. 
 	 */
 	private void drawBorder(ArrayList<CoastPoint[]> arrBorder, Color c, Graphics g) {
 
 		Graphics2D D2 = (Graphics2D) g;
 		// enable anti-aliasing
 		D2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
 		// set road color
 		D2.setColor(c);
 		// setting stroke type
 		D2.setStroke(new BasicStroke(borderWidth(RoadSegment.getZoomLevel()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 
 		for (int i = 0; i < arrBorder.size(); i++) {
 			for (int j = 1; j < arrBorder.get(i).length; j++) {
 				D2.drawLine(
 						Equation.calcPixelX(arrBorder.get(i)[j-1].getX()-DataReader.getMinX()),
 						Equation.calcPixelY(arrBorder.get(i)[j-1].getY()-DataReader.getMinY()),
 						Equation.calcPixelX(arrBorder.get(i)[j].getX()-DataReader.getMinX()),
 						Equation.calcPixelY(arrBorder.get(i)[j].getY()-DataReader.getMinY()));
 			}
 		}
 	}
 
 	/**
 	 * Method used retrieve the width of the border segments.
 	 * @param zoomLevel The current zoom-level of the map. 
 	 * @return The width corresponding to the given zoom-level.
 	 */
 	private float borderWidth(int zoomLevel) {
 		switch(zoomLevel) {
 		case 1 : return 2.5f;
 		case 2 : return 3.0f;
 		case 3 : return 3.2f;
 		case 4 : return 3.6f;
 		case 5 : return 5.0f;
 		default: return 2.5f;
 		}
 	}
 
 
 
 	/**
 	 * Creates an blank off-screen image, which Graphics object is then used to draw an
 	 * image to be flipped in later.
 	 */
 	public void createBufferImage() {
 		offScreen = createImage(getWidth(), getHeight());		// Creates a new empty Image object and saves it to the buffer.
 		offgc = offScreen.getGraphics();						// The Graphics object of this Image is extracted,
 		paintComponent(offgc);									// and the paintComponent() method is called using this Graphics object,
 	}															// thus 'flipping' the new map into view.
 
 	/**
 	 * Getter method for the segments field.
 	 * 
 	 * @return Returns the current value of the segment field. (ArrayList\<RoadSegment\>)
 	 */
 	public ArrayList<RoadSegment> getRoadSegments() {
 		return segments;
 	}
 
 	public ArrayList<RoadSegment> getBorderSegments() {
 		return borderSegments;
 	}
 	
 	/**
 	 * Changes the segments-filed ArrayList to a new empty one. 
 	 */
 	public void newArrayList() {
 		segments = new ArrayList<RoadSegment>();
 		borderSegments = new ArrayList<RoadSegment>();
 	}
 
 	/**
 	 * Adds a single roadSegment to the map.
 	 * 
 	 * @param roadSegment The roadSegment which are to be added to the map.
 	 */
 	public void addRoadSegment(RoadSegment roadSegment) {
 		segments.add(roadSegment);
 	}
 
 	/**
 	 * Recalculate the position of each roadSegment
 	 * within the map.
 	 */
 	public void updatePix(){
 		for(RoadSegment b: borderSegments){
 			if(b == null) continue;
 			b.updatePosition();
 		}
 		for(RoadSegment r: segments){
 			if(r == null) continue;
 			r.updatePosition();
 		}
 		for(DrawableItem r : path) r.updatePosition();
 		for (Flag f : flags) f.updatePosition();
 	}
 
 	/**
 	 * Recalculate position of path
 	 */
 	public void updatePath() {
 		for (DrawableItem r : path) r.updatePosition();
 		for (Flag f : flags) f.updatePosition();
 	}
 
 	/**
 	 * Adds the result of the users path-finding including the length
 	 * of the path and the driving time, to the map instance.
 	 * 
 	 * @param path	An ArrayList of DrawableItem defining the path. 
 	 * @param pathLength	The length of the path in meters.
 	 * @param driveTime	The estimated drive time of the path in minutes.
 	 */
 	public void setPath(ArrayList<DrawableItem> path, double pathLength, double driveTime) {
 		this.path = path;
 		this.pathLength = pathLength;
 		this.driveTime = driveTime;
 	}
 
 	/**
 	 * Removes the path from the map.
 	 */
 	public void resetPath() {
 		path = new ArrayList<DrawableItem>();
 		flags = new ArrayList<Flag>();
 		driveTime = -1;
 		pathLength = -1;
 	}
 
 	/**
 	 * Adds a given flag to the flags array if it does
 	 * not already exists in the array.
 	 * 
 	 * @param f The flag to be added.
 	 */
 	public void addFlag(Flag f) {
 		if (!flags.contains(f))
 			flags.add(f);
 	}
 
 	/**
 	 * Stores the coast, lake and island date to the map instance.
 	 * ##Should only be used once at the initializing 
 	 * since the data is stored in a static field##
 	 * 
 	 * @param argCoast An ArrayList of CoastPoint arrays describing the coast polygons.
 	 * @param argLake An ArrayList of CoastPoint arrays describing the lake polygons.
 	 * @param argIsland An ArrayList of CoastPoint arrays describing the island polygons.
 	 */
 	public static void setCoast(ArrayList<CoastPoint[]> argCoast, ArrayList<CoastPoint[]> argLake, ArrayList<CoastPoint[]> argIsland) {
 		coast = argCoast;
 		lake = argLake;
 		island = argIsland;
 	}
 
 	/**
 	 * Stores the border data to the map instance.
 	 * ##Should only be used once at the initializing 
 	 * since the data is stored in a static field##
 	 * 
 	 * @param argBorder An ArrayList of CoastPoint arrays describing the broderlines.
 	 */
 	public static void setBorder(ArrayList<CoastPoint[]> argBorder) {
 		border = argBorder;
 	}
 	
 	/**
 	 * Getter for the pathLength field.
 	 * 
 	 * @return The lenght of the current stored path.
 	 */
 	public double getPathLengt() {
 		return pathLength;
 	}
 
 	/**
 	 * Getter method for the drive time of the current path.
 	 * 
 	 * @return the driveTime The drive time in minutes. 
 	 */
 	public double getDriveTime() {
 		return driveTime;
 	}
 
 	/**
 	 * Adds a RoadSegment to he borderSegments ArrayList.
 	 * @param borderSegment The RoadSegments which should be added.
 	 */
 	public void addBorderSegment(RoadSegment borderSegment) {
 		borderSegments.add(borderSegment);
 	}
 }
