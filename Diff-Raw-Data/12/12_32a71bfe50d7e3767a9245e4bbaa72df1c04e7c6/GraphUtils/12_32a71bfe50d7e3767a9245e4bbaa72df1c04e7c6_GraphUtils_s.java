 package com.jboss.demo.mrg.messaging.graphics;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.util.Collection;
 import java.util.Iterator;
 
 import com.jboss.demo.mrg.messaging.graphics.AxisLabel.Orientation;
 
 /**
  * Utility class for rendering graph elements.
  * @author Mike Darretta
  */
 public class GraphUtils {
 	
 	/** Colors array for rendering multiple line in a single chart */
 	protected static final Color[] colors = new Color[] {
 	    Color.BLUE,
 	    Color.MAGENTA,
 	    Color.DARK_GRAY,
 	    Color.GREEN.darker(),
 	    Color.YELLOW.darker(),
 	    Color.CYAN.darker(),
 	    Color.ORANGE,
 	    Color.RED,
 	    Color.PINK,
 	    Color.BLACK
 	};
 	
 	/**
 	 * Constructor made private to prevent instantiation.
 	 */
 	private GraphUtils() {}
 
     /**
      * Renders an X-axis line.
 	 * @param g2 The graphics object.
 	 * @param chartHeight The chart height.
 	 * @param chartWidth The chart width.
 	 * @param chartPadding The chart padding.
 	 * @param label Axis label.
      */
     public static void renderXAxis(Graphics2D g2, int chartHeight, int chartWidth, int chartPadding, String label) {
         new Axis(new Coordinate(chartPadding, chartHeight - chartPadding), 
         		 new Coordinate(chartWidth - chartPadding, chartHeight - chartPadding)).render(g2);
         if (label != null && label.length() > 0) {
             new AxisLabel(
         		label, 
         		Orientation.HORIZONTAL, 
         		AxisLabel.calculateStartingXPos(g2, label, Orientation.HORIZONTAL, chartWidth, chartPadding), 
         		AxisLabel.calculateStartingYPos(g2, label, Orientation.HORIZONTAL, chartHeight, chartPadding)).render(g2);
         }
     }
 	
 	/**
 	 * Renders a Y-axis line.
 	 * @param g2 The graphics object.
 	 * @param chartHeight The chart height.
 	 * @param chartPadding The chart padding.
 	 * @param label Axis label.
 	 */
     public static void renderYAxis(Graphics2D g2, int chartHeight, int chartPadding, String label) {
         new Axis(new Coordinate(chartPadding, chartPadding),
         		 new Coordinate(chartPadding, chartHeight - chartPadding)).render(g2); 
         if (label != null && label.length() > 0) {
             new AxisLabel(
         		label, 
         		Orientation.VERTICAL, 
         		AxisLabel.calculateStartingXPos(g2, label, Orientation.VERTICAL, 0, chartPadding), 
         		AxisLabel.calculateStartingYPos(g2, label, Orientation.VERTICAL, chartHeight, chartPadding)).render(g2);
         }
     }
 		
     /**
      * Renders X-axis coordinates.
      * @param g2 The graphics object.
      * @param axisCoordinates The axis coordinates.
      * @param start The starting axis value.
      * @param end The ending axis value.
      */
 	public static void renderXCoordinates(Graphics2D g2, AxisCoordinates axisCoordinates, Coordinate start, Coordinate end) {
 		Iterator<Integer> i = axisCoordinates.getCoordinates().iterator();
 		int numCoordinates = axisCoordinates.getNumCoordinates();
 
 		double currPos = 0.0;
 		double interval = (end.getXPos()-start.getXPos())/(double) (numCoordinates-1);
 		
 		while (i.hasNext()) {
 			new AxisCoordinateSegment(
 					new Coordinate(start.getXPos()+currPos,
 							start.getYPos()-3),
 					new Coordinate(start.getXPos()+currPos,
 							start.getYPos()+3),
 					null,
 					Color.BLACK).render(g2);
 			i.next();
 			currPos += interval;
 		}
 	}
 	
     /**
      * Renders Y-axis coordinates.
      * @param g2 The graphics object.
      * @param axisCoordinates The axis coordinates.
      * @param start The starting axis value.
      * @param end The ending axis value.
      */
 	public static void renderYCoordinates(Graphics2D g2, AxisCoordinates axisCoordinates, Coordinate start, Coordinate end) {
 		Iterator<Integer> i = axisCoordinates.getCoordinates().iterator();
 		int numCoordinates = axisCoordinates.getNumCoordinates();
 		
 		double currPos = 0.0;
 		double interval = (end.getYPos()-start.getYPos())/(double) (numCoordinates-1);
 		
 		while (i.hasNext()) {
 			new AxisCoordinateSegment(
 					new Coordinate(start.getXPos()-3.0,
 							end.getYPos()-currPos),
 					new Coordinate(start.getXPos()+3.0,
 							end.getYPos()-currPos),
 					"" + i.next().intValue(),
 					Color.BLACK).render(g2);
 			currPos += interval;
 		}
 	}
     
     /**
      * Renders graph lines.
      * @param g2 The graphics object.
      * @param chartHeight The chart height.
      * @param chartWidth The chart width.
      * @param chartPadding The chart padding.
      * @param scale The scale of the graph lines.
      * @param data The graph data.
      * @param lineColor The line color.
      */
     public static void renderLines(Graphics2D g2, int chartHeight, int chartWidth, 
     		int chartPadding, double scale, Integer[] data, Paint lineColor, boolean doLabel) {
         double increment = (double) (chartWidth - 2 * chartPadding) / (data.length - 1); 
         for (int i = 0; i < data.length - 1; i++) { 
 			if (!(data[i].equals(Integer.MIN_VALUE))) {
 				int value = data[i].intValue();
 				new LineSegment(
 						new Coordinate(
 								chartPadding + (i * increment),
								chartHeight - chartPadding - (scale * data[i].intValue())),
 						new Coordinate(
 								chartPadding + ((i + 1) * increment),
								chartHeight - chartPadding - (scale * data[i+1].intValue())),
 						value,
 						lineColor,
 						doLabel).render(g2);
 			}
         }
     }
 	
     /**
      * Renders a chart legend.
      * @param g2 The graphics object.
      * @param x1 The starting X position.
      * @param y1 The starting Y position.
      * @param points The number of elements to define in the legend.
      */
 	public static void renderLegend(Graphics2D g2, int x1, int y1, Collection<GraphPoints> points) {
     	Iterator<GraphPoints> i = points.iterator();
     	int x=0;
     	while (i.hasNext()) {
     		new LegendSegment(new Coordinate(x1, y1), i.next().getName(), colors[x++]).render(g2);
     		y1 -= 20;
     	}
 	}
 }
