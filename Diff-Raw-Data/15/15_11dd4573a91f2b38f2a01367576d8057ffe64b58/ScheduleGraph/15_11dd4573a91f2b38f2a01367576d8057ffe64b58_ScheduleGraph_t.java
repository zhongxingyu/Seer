 package schedule;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 import java.util.ArrayList;
 import java.util.Random;
 
 import status.StatusPanel;
 
 import network.NetworkGraph;
 
 import domain.Edge;
 import domain.Node;
 
 @SuppressWarnings("serial")
 public class ScheduleGraph extends JPanel {
 
 	//values on gaps for the coordinate system
 	private static final int X_AXIS_GAP = 65;
 	private static final int Y_AXIS_GAP = 40;
 	private static final int HATCH_GAP = 30;
 	private static final int FINAL_GAP = 15;
 	private static final int HATCH_GAP_WIDTH = 10;
 
 	private int STATION_CNT;
 	private int TIME_CNT;
 
 	private ArrayList<Node> stations;
 	private ArrayList<ArrayList<Node>> routes;
 	private ArrayList<Color> colors;
 
 	//special node for 'stop' declarations in the route configuration
 	private static Node stopNode = new Node("stop", "stop");
 
 	/**
 	 * Constructor
 	 * 
 	 * @param stations - station in the railway network system
 	 * @param routes - train routes
 	 */
 	public ScheduleGraph(ArrayList<Node> stations, ArrayList<ArrayList<Node>> routes) {
 		this.stations = stations;
 		this.routes = routes;
 
 		//find length of the longest route declared
 		int longest = 0;
 		for(ArrayList<Node> route : routes){
 			if (longest < route.size()){
 				longest = route.size();
 			}
 		}
 
 		STATION_CNT = stations.size();
 		TIME_CNT = longest;
 
 		//generate random colors for each route
 		colors = new ArrayList<Color>();
 		for(int i = 0; i<routes.size(); i++){
 			colors.add(randomColorGenerator());
 		}
 		addTimeLabels();
 
 		setBorder(new LineBorder(Color.BLACK));
 		setLayout(null);
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		drawXYAxis(g2);
 		drawHatchMarks(g2);
 		drawAxisArrows(g2);
 		drawRoutes(g2);
 
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		int width  = X_AXIS_GAP +(TIME_CNT*HATCH_GAP) + (2 * FINAL_GAP);
 		int height = Y_AXIS_GAP + (STATION_CNT * HATCH_GAP) + (2 * FINAL_GAP);
 
 		return new Dimension(width, height);
 	}
 
 	/**
 	 *  Add clickable time labels to the plot (through the x-axis)
 	 */
 	private void addTimeLabels(){
 		for(int i = 0; i < TIME_CNT; i++) {
 			//calculate the coordinates
 			int x0 = (i * HATCH_GAP) + X_AXIS_GAP + FINAL_GAP;
 			int y0 = Y_AXIS_GAP - (HATCH_GAP_WIDTH/2) - 14; //14 height of the label
 
 			//create and add label
 			JLabel label = new JLabel("t"+i);
 			label.setFont(UIManager.getDefaults().getFont("JLabel.font"));
 			label.setBounds(x0, y0, 20, 14); //standard height and width for label
 			//add mouse listener
 			label.addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseReleased(MouseEvent e) {}
 
 				@Override
 				public void mousePressed(MouseEvent e) {}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					JLabel label = (JLabel) e.getSource();
 					label.setForeground(Color.BLACK);
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					JLabel label = (JLabel) e.getSource();
 					label.setForeground(Color.BLUE);
 					label.setCursor(new Cursor(Cursor.HAND_CURSOR));
 				}
 
 				@Override
 				public synchronized  void mouseClicked(MouseEvent e) {
 					JLabel label = (JLabel) e.getSource();
 					String timeslot = label.getText();
 					//get the time as integer
 					int time = Integer.parseInt(timeslot.substring(1)); 
 					String statusMsg = "Status at time " + time + ":";
 					StatusPanel.addText(statusMsg);
 
 					//look for track collisions
 					if(time > 0){
 						ArrayList<Node> current = trainInfo(time);
 						ArrayList<Node> previous = trainInfo(time-1);
 						ArrayList<Edge> trackCollisions = trackCollision(previous, current);
 						NetworkGraph.updateEdgeColor(trackCollisions);
 					}
 
 					//look for station collision
 					ArrayList<Node> stationCollions = stationCollision(trainInfo(time));
 					NetworkGraph.updateNodeColor(stationCollions);				
 				}
 			});
 			add(label);		
 		}	
 	}
 
 	/**
 	 * Looks for track collisions for previous and current train information
 	 * @param previous - train information (traffic) 1 time unit before than current  
 	 * @param current - current train information (traffic)
 	 * @return
 	 */
 	private ArrayList<Edge> trackCollision(ArrayList<Node> previous, ArrayList<Node> current){
 		//Main algorithm: take two nodes (prev1 and curr1 station), switch them and look for doublets.
 		ArrayList<Edge> collisions = new ArrayList<Edge>();
 		for(int i = 0; i < previous.size(); i++){
 			Node prev1 = previous.get(i);
 			Node curr1 = current.get(i);
 			//the train must have moved before a track collision can occur
 			if(!prev1.equals(curr1)){
 				for(int j = i+1; j < previous.size(); j++){
 					Node prev2 = previous.get(j);
 					Node curr2 = current.get(j);
 					//track collision detected!
 					if(curr1.equals(prev2) && prev1.equals(curr2)){
 						Edge edge = new Edge(prev1, curr1);
 						edge.setColor(Color.RED);
 						collisions.add(edge);
 						//add message to status panel
 						String errorMsg = "Track collision detected between: " + prev1.toString() + " and " + curr1.toString();
 						StatusPanel.addText(errorMsg);
 					}
 				}
 			}
 		}
 		return collisions;
 	}
 
 	/**
 	 * Look for station collisions for a given set of nodes
 	 * @param nodes
 	 * @return
 	 */
 	private ArrayList<Node> stationCollision(ArrayList<Node> nodes){
 		for(int i = 0; i < nodes.size(); i++){
 			Node station1 = nodes.get(i);
 			for(int j = i+1; j < nodes.size(); j++){
 				Node station2 = nodes.get(j);
 				//if a station collision is detected then set the color red
 				if(station1.equals(station2)){
					if((!station1.getId().equals(stopNode.getId()) && !station2.getId().equals(stopNode.getId()))){
						station1.setColor(Color.RED);						
						station2.setColor(Color.RED);
						//add message to status panel
						String errorMsg = "Station collision detected at node/station: " + station1.toString();
						StatusPanel.addText(errorMsg);
					}
 				}
 			}
 		}
 		return nodes;
 	}
 
 	/**
 	 * Find current location (node) for each train for a given time
 	 * @param time
 	 * @return
 	 */
 	private ArrayList<Node> trainInfo(int time){
 		ArrayList<Node> nodes = new ArrayList<Node>();	
 
 		//for each route find the location of each train for a given time
 		for(int i = 0; i < routes.size(); i++){
 			ArrayList<Node> route = routes.get(i);
 			Node station = null;
 			try{
 				station = route.get(time);
 			}catch(IndexOutOfBoundsException e){
 				//some route are not that long so, get the last item
 				station = route.get(route.size()-1);
 			}
 			//the color is set to indicate where the train is
 			station.setColor(colors.get(i));
 			nodes.add(station);
 		}
 		return nodes;		
 	}
 
 	/**
 	 * Generates a random color
 	 * @return a random generated color
 	 */
 	private Color randomColorGenerator(){
 		Random randomGenerator = new Random();
 		int red = randomGenerator.nextInt(255);
 		int green = randomGenerator.nextInt(255);
 		int blue = randomGenerator.nextInt(255);
 		Color randomColour = new Color(red,green,blue);
 
 		return randomColour;
 	}
 
 	/**
 	 * Draws the x and y axis on the graph
 	 * @param g2
 	 */
 	private void drawXYAxis(Graphics2D g2){
 		// create x and y axes 
 		g2.drawLine(X_AXIS_GAP, Y_AXIS_GAP, X_AXIS_GAP +(TIME_CNT*HATCH_GAP), Y_AXIS_GAP);
 		g2.drawLine(X_AXIS_GAP, Y_AXIS_GAP, X_AXIS_GAP, Y_AXIS_GAP + (STATION_CNT * HATCH_GAP));
 	}
 
 	/**
 	 * Draws the hatch marks on the graph
 	 * @param g2
 	 */
 	private void drawHatchMarks(Graphics2D g2){
 
 		// create hatch marks for y axis. 
 		for(int i = 0; i < STATION_CNT; i++) {
 			//calculate coordinates and draw line
 			int x0 = X_AXIS_GAP - (HATCH_GAP_WIDTH/2);
 			int x1 = x0 + HATCH_GAP_WIDTH + (TIME_CNT * HATCH_GAP);
 			int y0 = (i * HATCH_GAP) + Y_AXIS_GAP + FINAL_GAP;
 			int y1 = y0;
 			g2.drawLine(x0, y0, x1, y1);
 
 			//draw the name of the station/node
 			g2.drawString(stations.get(i).toString(), 1, y0);
 		}
 
 		// create hatch marks for x axis.
 		for (int i = 0; i < TIME_CNT; i++) {
 			//calculate coordinates and draw line
 			int x0 = (i * HATCH_GAP) + X_AXIS_GAP + FINAL_GAP;
 			int x1 = x0;
 			int y0 = Y_AXIS_GAP - (HATCH_GAP_WIDTH/2);
 			int y1 = y0 + HATCH_GAP_WIDTH + (STATION_CNT * HATCH_GAP);
 			g2.drawLine(x0, y0, x1, y1);
 		}
 	}
 
 	/**
 	 * Draws the arrows at the end of the x and y axis
 	 * @param g2
 	 */
 	private void drawAxisArrows(Graphics2D g2){
 		// create a triangle for y axis. (calculates by 3 points)
 		int[] xPoints = new int[3];
 		int[] yPoints = new int[3];
 		xPoints[0] = X_AXIS_GAP - (HATCH_GAP_WIDTH/2); //left most x-coordinate
 		xPoints[1] = xPoints[0] + HATCH_GAP_WIDTH; //right most x-coordinate
 		xPoints[2] = X_AXIS_GAP; //middle x-coordinate
 
 		yPoints[0] = Y_AXIS_GAP + (STATION_CNT * HATCH_GAP); //left most y-cooridnate
 		yPoints[1] = yPoints[0]; //right most y-coordinate
 		yPoints[2] = Y_AXIS_GAP + (STATION_CNT * HATCH_GAP) + FINAL_GAP; //middle y-coordinate
 		g2.fillPolygon(xPoints, yPoints, 3);
 
 		// create triangle for x axis.
 		xPoints = new int[3];
 		yPoints = new int[3];
 		xPoints[0] = X_AXIS_GAP + (TIME_CNT * HATCH_GAP); //top x-coordinate
 		xPoints[1] = xPoints[0]; //bottom x-coordinate
 		xPoints[2] = X_AXIS_GAP + (TIME_CNT * HATCH_GAP) + FINAL_GAP; //right-most x-coordinate
 
 		yPoints[0] = Y_AXIS_GAP - (HATCH_GAP_WIDTH/2); //top y-coordinate 	
 		yPoints[1] = yPoints[0] + HATCH_GAP_WIDTH; //bottom y-coordinate
 		yPoints[2] = Y_AXIS_GAP; //right-most y-coordinate
 		g2.fillPolygon(xPoints, yPoints, 3);
 	}
 
 	/**
 	 * Draws the routes on the graph
 	 * @param g2
 	 */
 	private void drawRoutes(Graphics2D g2){
 		//make line thicker
 		g2.setStroke(new BasicStroke(3));
 		for(ArrayList<Node> route : routes){
 			for(int i = 0; i<route.size()-1; i++){
 				//calculate source node coordinates
 				Node station = route.get(i);
 				//don't draw the stop nodes!
				if(!station.getId().equals(stopNode.getId())){
 					int x1 = X_AXIS_GAP + FINAL_GAP + (i*HATCH_GAP);
 					int y1 = Y_AXIS_GAP +(HATCH_GAP * (stations.indexOf(station)+1)) - FINAL_GAP;
 
 					//calculate target node coordinates
 					Node station1 = route.get(i+1);
 					int x2 = X_AXIS_GAP + FINAL_GAP + ((i+1)*HATCH_GAP);
 					int y2 = Y_AXIS_GAP + (HATCH_GAP * (stations.indexOf(station1)+1)) - FINAL_GAP;
 
 					//draw the line 
 					Color c = colors.get(routes.indexOf(route));
 					g2.setColor(c);
 					g2.drawLine(x1, y1, x2, y2);
 
 					//draw the circle (the circles is a bit brighter)
 					Color temp = new Color(c.getRed(), c.getGreen(), c.getBlue(), 200);
 					g2.setColor(temp);
 					g2.fillOval(x1-5, y1-5, 10, 10);
 				}
 			}
 			//paint last circle
 			Node station = route.get(route.size()-1);
 			int x1 = X_AXIS_GAP + FINAL_GAP + (route.size()-1)*HATCH_GAP;
 			int y1 = Y_AXIS_GAP +(HATCH_GAP * (stations.indexOf(station)+1)) - FINAL_GAP;
 			g2.fillOval(x1-5, y1-5, 10, 10);
 
 			//some route might not have same length. Those still have to be drawn
 			//the trains are on the last station/node specified.
 			if(route.size() != TIME_CNT){
 				//calculate coordinates and draw line
 				x1 = X_AXIS_GAP + FINAL_GAP + (route.size()-1)*HATCH_GAP; //start x coordinate
 				y1 = Y_AXIS_GAP +(HATCH_GAP * (stations.indexOf(station)+1)) - FINAL_GAP; //y coordinate 
 				int x2 = x1 + ((TIME_CNT - route.size()) * HATCH_GAP); //end x coordinate
 				//find the color
 				Color c = colors.get(routes.indexOf(route));
 				g2.setColor(c);
 				g2.drawLine(x1, y1, x2, y1);
 
 				//draw the circles (the circles is a bit brighter)
 				Color temp = new Color(c.getRed(), c.getGreen(), c.getBlue(), 200);
 				g2.setColor(temp);
 				//find how many circles to be drawn and paint them
 				int nodes = TIME_CNT - route.size();	
 				for(int i =0; i<nodes; i++){
 					//find the coordinates
 					int x = X_AXIS_GAP + FINAL_GAP + (route.size())*HATCH_GAP + (i * HATCH_GAP);
 					g2.fillOval(x-5, y1-5, 10, 10);
 				}
 			}
 		}
 	}
 }
