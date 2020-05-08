 package com.jboss.demo.mrg.messaging.graphics;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.swing.JPanel;
 
 import com.jboss.demo.mrg.messaging.handler.CommandHandler;
 
 /**
  * Line graph panel.
  * Mike Darretta
  */
 public class LineGraph extends JPanel { 
 
 	/** Serial version UID */
 	private static final long serialVersionUID = -1978038232845014444L;
 
 	/** The points for the line graph */
 	protected Collection<GraphPoints> points;
 	
 	/** The optional collections of command handlers that this graph is bound to */
 	protected Collection<CommandHandler> handlers;
 
 	/** Padding from the panel edge for rendering chart */
 	protected final int PAD = 30;
     
 	/**
 	 * Default constructor. This version creates an empty set of graph points.
 	 */
     public LineGraph() {
     	this(new ArrayList<GraphPoints> ());
     }
 
     /**
      * Constructor for a set of graph points.
      * @param graphPoints The graph points.
      */  	
     public LineGraph(GraphPoints graphPoints) {
     	Collection<GraphPoints> points = new ArrayList<GraphPoints> ();
     	if (graphPoints != null) {
     	    points.add(graphPoints);
     	}
         this.points = points;
     }
     
     /**
      * Constructor for a collection of graph points (for a multi-line chart).
      * @param points The collection of graph points.
      */
     public LineGraph(Collection<GraphPoints> points) {
     	this.points = points;
     }
     
     /**
      * Returns the collection of graph points.
      * @return The graph points.
      */
     public Collection<GraphPoints> getPoints() {
     	return points;
     }
     
     /**
      * Sets the graph points.
      * @param points The graph points.
      */
     public void setPoints(Collection<GraphPoints> points) {
     	this.points = points;
     }
 
     /**
      * Adds a new set of points to the points collection.
      * @param points The points to add to the collection.
      */
     public void addPoints(GraphPoints points) {
         this.points.add(points);
     }
     
     /**
      * Returns the optional command handlers.
      * @return The optional command handlers.
      */
     public Collection<CommandHandler> getHandlers() {
 		return handlers;
 	}
 
     /**
      * Sets the optional command handlers.
      * @param handlers The command handlers.
      */
 	public void setHandlers(Collection<CommandHandler> handlers) {
 		this.handlers = handlers;
 	}
 	
 	/**
 	 * Adds a command handler to the handler list.
 	 * @param handler The command handler to add.
 	 */
 	public void addHandler(CommandHandler handler) {
 		if (handlers == null) {
 			handlers = new ArrayList<CommandHandler> ();
 		}
 		
 		handlers.add(handler);
 	}
 
 	/**
      * Paints the component.
      * @param g The graphics object.
      */
     protected void paintComponent(Graphics g) { 
 	
         super.paintComponent(g); 
          
         Graphics2D g2 = (Graphics2D) g; 
 
         g2.setRenderingHint(
             RenderingHints.KEY_ANTIALIASING, 
             RenderingHints.VALUE_ANTIALIAS_ON); 
 
         int height = getHeight(); 
         int width = getWidth(); 
 
         GraphUtils.renderYAxis(g2, height, PAD, null);
         GraphUtils.renderXAxis(g2, height, width, PAD, "Samples");
 
         AxisCoordinates xAxisCoordinates = new XAxisCoordinates(0, points.size(), 0.0, 10);
         AxisCoordinates yAxisCoordinates = new YAxisCoordinates(getMaxDataValue());
                 
         GraphUtils.renderXCoordinates(g2, xAxisCoordinates,
         		new Coordinate(PAD, this.getHeight()-PAD), new Coordinate(width-PAD, height-PAD));
         GraphUtils.renderYCoordinates(g2, yAxisCoordinates, 
         		new Coordinate(PAD, PAD), new Coordinate(PAD, height-PAD));
         
         Iterator<GraphPoints> i = points.iterator();
         int x=0;
         while (i.hasNext()) {
         	Integer[] data = i.next().getData();
         	double scale = (double) (height - PAD) / yAxisCoordinates.getMaxCoordinate();
         	GraphUtils.renderLines(g2, height, width, PAD, scale, data, GraphUtils.colors[x++], false);
         }
         
        GraphUtils.renderLegend(g2, getWidth()-PAD-60, getHeight()-PAD-30, points);
     }
     
     /**
      * Destroys any existing owning command vias the optional command handlers.
      */
     public void exit() {
     	if (handlers != null) {
     		Iterator<CommandHandler> i = handlers.iterator();
     		while (i.hasNext()) {
     			i.next().destroyProcess();
     		}
     	}
     }
     
     /**
      * Returns the maximum data value within the collection of graph points.
      * @return The maximum data value.
      */
     private int getMaxDataValue() { 
     	int maxValue = 0;
         Iterator<GraphPoints> i = points.iterator();
         while (i.hasNext()) {
         	int dataMaxValue = getMaxDataValue(i.next().getData());
         	if (maxValue < dataMaxValue) {
         		maxValue = dataMaxValue;
         	}
         }
         
         return maxValue;
     }
     
     /**
      * Returns the maximum data value for a particular set of graph data points.
      * @param data The graph data points.
      * @return The maximum data value.
      */
     private int getMaxDataValue(Integer[] data) { 
         int max = Integer.MIN_VALUE;
         for (int i = 0; i < data.length; i++) { 
         	int value = data[i].intValue();
             if (max < value) { 
                 max = value;
             } 
         } 
         return max; 
     }
 }
