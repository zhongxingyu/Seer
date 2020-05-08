 package edu.msoe.se2800.h4.jplot;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.swing.JPanel;
 
 public class PlotPanel extends JPanel {
 	
 	/**
 	 * Generated serialVersionUID
 	 */
 	private static final long serialVersionUID = 2975690672805786359L;
 	
 	private int activePointIndexHolder;
 	private JPoint activePoint;
 
 	public PlotPanel() {
 		setPreferredSize(new Dimension(Constants.GRID_WIDTH, Constants.GRID_HEIGHT));
 		setBackground(Color.BLACK);
 		setVisible(true);
 		addMouseMotionListener(new PlotMouseMotionListener());
 		addMouseListener(new PlotMouseAdapter());
 		this.addMouseWheelListener(new PlotMouseWheelListener());
 	}
 	
 	
 	
 	/**
 	 * This method returns one of the points in the Grid class that is within 10 pixels of the passed
 	 * in point.  If the passed in point is not within the 10 pixel tolerance, this method returns null.
 	 * @param point
 	 * @return the actual point in the Grid class that is within tolerance of the parameter point, otherwise null
 	 */
 	public JPoint getInterceptedPoint(JPoint point) {
 		JPoint retvalPoint = null;
 		for (JPoint p : Grid.getInstance().getPathPoints()) {
 			JPoint temp = translateToLocation(p);
 			temp.x-=Constants.POINT_RADIUS;
 			temp.y+=Constants.POINT_RADIUS;
 			if ((point.x < temp.x+10 && point.x > temp.x-10) && (point.y < temp.y+10 && point.y > temp.y-10)) {
 				retvalPoint = p;
 			}
 		}
 		return retvalPoint;
 	}
 	
 	/**
 	 * This method draws the dark gray grid lines based on the GRID_DENSITY specified in the constants file.
 	 * @param density the number of lines you would like visible on the screen
 	 * @param g graphics used to draw the line
 	 */
 	public void drawGridLines(int density, Graphics g) {
 		int drawingWidth = (Constants.GRID_WIDTH/density)+Constants.GRID_WIDTH;
 		int drawingHeight = (Constants.GRID_HEIGHT/density)+Constants.GRID_HEIGHT;
 		
 		g.setColor(Color.DARK_GRAY);
 		
 		/** Draws the vertical lines */
 		for (int i=Constants.GRID_OFFSET; i<=drawingWidth;i+=(Constants.GRID_WIDTH/density)) g.drawLine(i, 0, i, drawingHeight);
 		
 		/** Draws the horizontal lines */
 		for (int i=(Constants.GRID_HEIGHT-Constants.GRID_OFFSET); i>=(Constants.GRID_HEIGHT-drawingHeight);i-=(Constants.GRID_HEIGHT/density)) g.drawLine(0, i, drawingWidth, i);
 	}
 	
 	/**
 	 * This method takes in a graphics instance that is used to draw a filled circle at the passed in point with a
 	 * radius (POINT_RADIUS) specified in the constants file
 	 * @param g
 	 * @param p
 	 */
 	public void drawPoint(Graphics g, JPoint p) {
 		if (Grid.getInstance().getHighlightedPoint() != null && Grid.getInstance().getHighlightedPoint().x == p.x && Grid.getInstance().getHighlightedPoint().y == p.y) {
 			g.setColor(Color.ORANGE);
 		}
 		JPoint temp = translateToLocation(p);
 		int x = temp.x;
 		int y = temp.y;
 		x-=Constants.POINT_RADIUS;
 		y+=Constants.POINT_RADIUS;
 		
 		g.fillOval(x, y, Constants.POINT_RADIUS*2, Constants.POINT_RADIUS*2);
 		
 		drawCoordinatesAbovePoint(g, p, temp);
 	}
 	
 	/**
 	 * Draws the x and y values of the passed in coordinates Point above the location Point
 	 * @param g graphics used to draw the x and y values
 	 * @param coordinates the point holding the actual numbers that will be drawn on the screen
 	 * @param location the point holding the location on screen that the coordinates will be drawn
 	 */
 	private void drawCoordinatesAbovePoint(Graphics g, JPoint coordinates, JPoint location) {
 		if (Constants.HOVER_INDEX == Grid.getInstance().getPathPoints().indexOf(coordinates)) {
 			g.setColor(Color.WHITE);
 		} else {
 			g.setColor(Color.LIGHT_GRAY);
 		}
 		g.drawString(""+coordinates.x+","+coordinates.y, location.x, location.y-10);
 	}
 	
 	/**
 	 * This method is usually called if the SNAP_TO_GRID_CORNERS is set to true in the Constants file and it takes a given
 	 * integer and returns the value corresponding to the closest value of a "corner" on the grid.  It will be a multiple of
 	 * STEP_INCREMENT in the constants file
 	 * @param num, the number to round
 	 * @return the rounded number
 	 */
 	public int round(int num) {
		int newNum;
 		
 		boolean keepStepping = true;
 		int highestStep = 0;
 		while (keepStepping) {
 			if (highestStep >= num) {
 				keepStepping = false;
 			} else {
 				highestStep+=Constants.STEP_INCREMENT;
 			}
 		}
 		if ((highestStep-num) < (num-(highestStep-Constants.STEP_INCREMENT))) {
 			newNum = highestStep;
 		} else {
 			newNum = (highestStep-Constants.STEP_INCREMENT);
 		}
 		return newNum;
 	}
 	
 	public void drawLine(Graphics g, JPoint one, JPoint two) {
 		JPoint translated1 = translateToLocation(one);
 		JPoint translated2 = translateToLocation(two);
 		
 		//we must add 10 to the y coordinates because the translate method accounts for the offset for points, not a line
 		g.drawLine(translated1.x, translated1.y+10, translated2.x, translated2.y+10);
 	}
 	
 	public JPoint translateToLocation(JPoint p) {
 		JPoint translated = new JPoint();
 		
 		int x = p.x;
 		int y = p.y;
 		
 		if (Constants.SNAP_TO_GRID_CORNERS) {
 			x = round(x);
 			y = round(y);
 			
 			//need this to update the points in the Grid class after rounding, a reference is passed in so we can just update 
 			//the reference, which still resides in the Grid class so setting the x and y of the passed in point is ok
 			p.x = x;
 			p.y = y;
 		}
 		
 		x = (int)Math.floor(0.5+((Constants.GRID_WIDTH/Grid.getInstance().getGridDensity())*(x/Constants.STEP_INCREMENT)));
 		y = (int)Math.floor(0.5+((Constants.GRID_HEIGHT/Grid.getInstance().getGridDensity())*(y/Constants.STEP_INCREMENT)));
 		y = Constants.GRID_HEIGHT-Constants.GRID_OFFSET-y;
 		x+=Constants.GRID_OFFSET;
 		y-=Constants.GRID_OFFSET;
 		translated.x = x;
 		translated.y = y;
 		
 		return translated;
 	}
 	
 	public JPoint translateToNearestPoint(JPoint p) {
 		JPoint translated = new JPoint();
 		
 		int x = p.x;
 		int y = p.y;
 		
 		x-=Constants.GRID_OFFSET;
 		y+=Constants.GRID_OFFSET;
 		y = Constants.GRID_HEIGHT-Constants.GRID_OFFSET-y;
 		x = (int)Math.floor(0.5+((x*Constants.STEP_INCREMENT)/(Constants.GRID_WIDTH/Grid.getInstance().getGridDensity())));
 		y = (int)Math.floor(0.5+((y*Constants.STEP_INCREMENT)/(Constants.GRID_HEIGHT/Grid.getInstance().getGridDensity())));
 		
 		
 		if (Constants.SNAP_TO_GRID_CORNERS) {
 			x = round(x);
 			y = round(y);
 		}
 		
 		translated.x = x;
 		translated.y = y;
 		
 		return translated;
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		setBackground(Color.BLACK);
 		drawGridLines(Grid.getInstance().getGridDensity(), g);
 		//loop that draws each point, synchronized helps to take care of accessing the same list
 		//from all of these threads the GUI makes
 		synchronized(Grid.getInstance().getPathPoints()) {
 			for (JPoint p : Grid.getInstance().getPathPoints()) {
 				g.setColor(Color.CYAN);
 				if (Grid.getInstance().getPathPoints().indexOf(p) == Constants.DRAGGING_INDEX) {
 					g.setColor(Color.ORANGE);
 				}
 				drawPoint(g, p);
 			}
 		}
 		
 		//loop that draws the lines between each point
 		for (int i=0;i<Grid.getInstance().getPathPoints().size();i++) {
 			if (i+1<Grid.getInstance().getPathPoints().size()) {
 				g.setColor(Color.CYAN);
 				if (Constants.DRAGGING_INDEX == i+1 || Constants.DRAGGING_INDEX == i) {
 					g.setColor(Color.ORANGE);
 				}
 				drawLine(g, Grid.getInstance().getPathPoints().get(i), Grid.getInstance().getPathPoints().get(i+1));
 			}
 		}
 	}
 	
 	private class PlotMouseWheelListener implements MouseWheelListener {
 
 		@Override
 		public void mouseWheelMoved(MouseWheelEvent event) {
 			System.out.println("wheel moved mouseevent");
 			
 			if (event.getWheelRotation() < 0) {
 				Grid.getInstance().zoomIn();
 			} else {
 				Grid.getInstance().zoomOut();
 			}
 			
 		}
 		
 	}
 	
 	/** Listeners and Adapters **/
 	private class PlotMouseAdapter extends MouseAdapter {
 		
 		@Override
 		public void mouseClicked(MouseEvent event) {
 			if (event.getButton() == MouseEvent.BUTTON1) {
 				JPoint point = translateToNearestPoint(new JPoint(event.getX(), event.getY()));
 				boolean found = false;
 				for (JPoint p : Grid.getInstance().getPathPoints()) {
 					if (p.x == point.x && p.y == point.y) {
 						found = true;
 						Grid.getInstance().setHighlightedPoint(Grid.getInstance().getPathPoints().indexOf(p));
 					}
 				}
 				if (found == false) {
 					Grid.getInstance().setHighlightedPoint(-5);
 				}
 				Grid.getInstance().redraw();
 			} else if (event.getButton() == MouseEvent.BUTTON3) {
 				Grid.getInstance().getParent().getParent().dispatchEvent(event);//TODO
 			}
 		}
 		@Override
 		public void mousePressed(MouseEvent event) {
 			JPoint p = new JPoint(event.getX(), event.getY());
 			activePoint = getInterceptedPoint(p);
 			activePointIndexHolder = Grid.getInstance().getPathPoints().indexOf(activePoint);
 			Constants.DRAGGING_INDEX = activePointIndexHolder;
 		}
 		@Override
 		public void mouseReleased(MouseEvent event) {
 			activePoint = null;
 			activePointIndexHolder = -5;
 			Constants.DRAGGING_INDEX = -5;
 			Grid.getInstance().repaint();
 			Constants.HOVER_INDEX = -5;
 		}
 	}
 	
 	private class PlotMouseMotionListener implements MouseMotionListener {
 		@Override
 		public void mouseDragged(MouseEvent event) {
 			if (activePoint != null) {
 				activePoint = translateToNearestPoint(new JPoint(event.getX(),event.getY()));
 				Grid.getInstance().getPathPoints().set(activePointIndexHolder, activePoint);
 				Grid.getInstance().redraw();
 			}
 		}
 		@Override
 		public void mouseMoved(MouseEvent event) {
 			if (activePoint == null) {
 				JPoint p = getInterceptedPoint(new JPoint(event.getX(), event.getY()));
 				if (p != null) {
 					Constants.HOVER_INDEX = Grid.getInstance().getPathPoints().indexOf(p);
 				} else {
 					Constants.HOVER_INDEX = -5;
 				}
 				Grid.getInstance().repaint();
 			}
 		}
 	}
 }
