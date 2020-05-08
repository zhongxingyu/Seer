 
 package edu.msoe.se2800.h4.jplot.plotPanel;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.Arrays;
 
 import javax.swing.JPanel;
 
 import lejos.robotics.navigation.Waypoint;
 import edu.msoe.se2800.h4.jplot.Constants;
 import edu.msoe.se2800.h4.jplot.JPlotController;
 
 public class PlotPanel extends JPanel implements PlotPanelInterface {
 
     /**
      * Generated serialVersionUID
      */
     private static final long serialVersionUID = 2975690672805786359L;
 
     private int activePointIndexHolder;
     private Waypoint activePoint;
 
     public PlotPanel() {
         setPreferredSize(new Dimension(Constants.GRID_WIDTH(), Constants.GRID_HEIGHT));
         setBackground(Color.BLACK);
         setVisible(true);
         this.addMouseWheelListener(new PlotMouseWheelListener());
     }
 
     @Override
     public Component getComponent() {
         return this;
     }
 
     /**
      * This method returns one of the points in the Grid class that is within 10 pixels of the
      * passed in point. If the passed in point is not within the 10 pixel tolerance, this method
      * returns null.
      * 
      * @param point
      * @return the actual point in the Grid class that is within tolerance of the parameter point,
      *         otherwise null
      */
     @Override
     public Waypoint getInterceptedPoint(Waypoint point) {
         Waypoint retvalPoint = null;
         for (Waypoint p : JPlotController.getInstance().getPath()) {
             Waypoint temp = translateToLocation(p);
             temp.x -= Constants.POINT_RADIUS;
             temp.y += Constants.POINT_RADIUS;
             if ((point.x < temp.x + 10 && point.x > temp.x - 10)
                     && (point.y < temp.y + 10 && point.y > temp.y - 10)) {
                 retvalPoint = p;
             }
         }
         return retvalPoint;
     }
 
     /**
      * This method draws the dark gray grid lines based on the GRID_DENSITY specified in the
      * constants file.
      * 
      * @param density the number of lines you would like visible on the screen
      * @param g graphics used to draw the line
      */
     public void drawGridLines(int density, Graphics g) {
         int drawingWidth = (Constants.GRID_WIDTH() / density) + Constants.GRID_WIDTH();
         int drawingHeight = (Constants.GRID_HEIGHT / density) + Constants.GRID_HEIGHT;
 
         g.setColor(Color.DARK_GRAY);

        /** Draws the vertical lines */
        for (int i = Constants.GRID_OFFSET; i <= drawingWidth; i += (Constants.GRID_WIDTH() / density))
            g.drawLine(i, 0, i, drawingHeight);

        /** Draws the horizontal lines */
        for (int i = (Constants.GRID_HEIGHT - Constants.GRID_OFFSET); i >= (Constants.GRID_HEIGHT - drawingHeight); i -= (Constants.GRID_HEIGHT / density))
            g.drawLine(0, i, drawingWidth, i);
     }
 
     /**
      * This method takes in a graphics instance that is used to draw a filled circle at the passed
      * in point with a radius (POINT_RADIUS) specified in the constants file
      * 
      * @param g
      * @param p
      */
     public void drawPoint(Graphics g, Waypoint p) {
         if (JPlotController.getInstance().getHighlightedPoint() != null
                 && JPlotController.getInstance().getHighlightedPoint().x == p.x
                 && JPlotController.getInstance().getHighlightedPoint().y == p.y) {
             g.setColor(Color.ORANGE);
         }
         Waypoint temp = translateToLocation(p);
         float x = temp.x;
         float y = temp.y;
         x -= Constants.POINT_RADIUS;
         y += Constants.POINT_RADIUS;
 
         g.fillOval((int) x, (int) y, Constants.POINT_RADIUS * 2, Constants.POINT_RADIUS * 2);
 
         drawCoordinatesAbovePoint(g, p, temp);
     }
 
     /**
      * Draws the x and y values of the passed in coordinates Point above the location Point
      * 
      * @param g graphics used to draw the x and y values
      * @param coordinates the point holding the actual numbers that will be drawn on the screen
      * @param location the point holding the location on screen that the coordinates will be drawn
      */
     private void drawCoordinatesAbovePoint(Graphics g, Waypoint coordinates, Waypoint location) {
         if (Constants.HOVER_INDEX == Arrays.asList(JPlotController.getInstance().getPathPoints())
                 .indexOf(coordinates)) {
             g.setColor(Color.WHITE);
         } else {
             g.setColor(Color.LIGHT_GRAY);
         }
         g.drawString("" + coordinates.x + "," + coordinates.y, (int) location.x,
                 (int) location.y - 10);
     }
 
     /**
      * This method is usually called if the SNAP_TO_GRID_CORNERS is set to true in the Constants
      * file and it takes a given integer and returns the value corresponding to the closest value of
      * a "corner" on the grid. It will be a multiple of STEP_INCREMENT in the constants file
      * 
      * @param num, the number to round
      * @return the rounded number
      */
     public float round(float num) {
         float newNum;
 
         boolean keepStepping = true;
         int highestStep = 0;
         while (keepStepping) {
             if (highestStep >= num) {
                 keepStepping = false;
             } else {
                 highestStep += Constants.STEP_INCREMENT;
             }
         }
         if ((highestStep - num) < (num - (highestStep - Constants.STEP_INCREMENT))) {
             newNum = highestStep;
         } else {
             newNum = (highestStep - Constants.STEP_INCREMENT);
         }
         return newNum;
     }
 
     public void drawLine(Graphics g, Waypoint one, Waypoint two) {
         Waypoint translated1 = translateToLocation(one);
         Waypoint translated2 = translateToLocation(two);
 
         // we must add 10 to the y coordinates because the translate method accounts for the offset
         // for points, a line does not need this offset
         g.drawLine((int) translated1.x, (int) translated1.y + 10, (int) translated2.x,
                 (int) translated2.y + 10);
     }
 
     /**
      * This method takes in a Waypoint that has coordinates relative to the grid and translates
      * them to a spot on the screen in coordinates the JVM uses. For example, passing in the Waypoint
      * with coordinates (20,30) may be returned as (130,256).
      * @param p
      * @return
      */
     public Waypoint translateToLocation(Waypoint p) {
 
         float x = p.x;
         System.out.println("x(1-1) = "+x);
         float y = p.y;
         System.out.println("y(1-1) = "+y);
 
         //this next calculation assigns x to the distance from the 0 point it should be
         x = (float)Math.floor(0.5 + ((Constants.GRID_WIDTH() / JPlotController.getInstance()
                 .getGridDensity()) * (x / Constants.STEP_INCREMENT)));
         System.out.println("x(1-2) = "+x);
         x = ( Constants.GRID_OFFSET + ( JPlotController.getInstance().getGridDensity() / 2.0f * ( Constants.GRID_WIDTH() * 1.0f / ( JPlotController.getInstance().getGridDensity() * 1.0f ) ) ) ) + x;
         System.out.println("x(1-3) = "+x);
         //x = x - (Constants.POINT_RADIUS*2);
         y = (float)Math.floor(0.5 + ((Constants.GRID_HEIGHT / JPlotController.getInstance()
                 .getGridDensity()) * (y / Constants.STEP_INCREMENT)));
         System.out.println("y(1-2) = "+y);
         y = (Constants.GRID_OFFSET+(JPlotController.getInstance().getGridDensity()/2*(Constants.GRID_HEIGHT/JPlotController.getInstance().getGridDensity()))) + y;
         System.out.println("y(1-3) = "+y);
         y = Constants.GRID_HEIGHT - Constants.GRID_OFFSET - y;
         System.out.println("y(1-4) = "+y);
         //y = y + (Constants.POINT_RADIUS*2);
         //x += Constants.GRID_OFFSET;
         System.out.println("final x(1-4) = "+x);
         //y -= Constants.GRID_OFFSET;
         System.out.println("final y(1-5) = "+y);
 
         return new Waypoint(x, y);
     }
 
     /**
      * This method takes in a Waypoint with the coordinates of a mouse click and translates them into 
      * coordinates on the Grid.  For example, if the user clicked at coordinates (250, 370), this may
      * translate into the point (-30, 70) on the Grid.
      */
     @Override
     public Waypoint translateToNearestPoint(Waypoint p) {
 
         float x = p.x;
         System.out.println("x(1) = "+x);
         float y = p.y;
         System.out.println("y(1) = "+y);
 
         x -= Constants.GRID_OFFSET;
         System.out.println("x(2) = "+x);
         y += Constants.GRID_OFFSET;
         System.out.println("y(2) = "+y);
         y = Constants.GRID_HEIGHT - Constants.GRID_OFFSET - y;
         System.out.println("y(3) = "+y);
         x = (int) Math
                 .floor(0.5 + ((x * Constants.STEP_INCREMENT) / (Constants.GRID_WIDTH() / JPlotController
                         .getInstance().getGridDensity())));
         System.out.println("x(3) = "+x);
         y = (int) Math
                 .floor(0.5 + ((y * Constants.STEP_INCREMENT) / (Constants.GRID_HEIGHT / JPlotController
                         .getInstance().getGridDensity())));
         System.out.println("y(4) = "+y);
 
         x-=Constants.STEP_INCREMENT*JPlotController.getInstance().getGridDensity()/2;
         y-=Constants.STEP_INCREMENT*JPlotController.getInstance().getGridDensity()/2;
         
         System.out.println("final x = "+x);
         System.out.println("final y = "+y);
         return new Waypoint(x, y);
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         setBackground(Color.BLACK);
         drawGridLines(JPlotController.getInstance().getGridDensity(), g);
         // loop that draws each point, synchronized helps to take care of accessing the same list
         // from all of these threads the GUI makes
         for (Waypoint p : JPlotController.getInstance().getPath()) {
             g.setColor(Color.CYAN);
             if (JPlotController.getInstance().getPath().indexOf(p) == Constants.DRAGGING_INDEX) {
                 g.setColor(Color.ORANGE);
             }
             drawPoint(g, p);
         }
 
         // loop that draws the lines between each point
         for (int i = 0; i < JPlotController.getInstance().getPath().size(); i++) {
             if (i + 1 < JPlotController.getInstance().getPath().size()) {
                 g.setColor(Color.CYAN);
                 if (Constants.DRAGGING_INDEX == i + 1 || Constants.DRAGGING_INDEX == i) {
                     g.setColor(Color.ORANGE);
                 }
                 drawLine(g, JPlotController.getInstance().getPath().get(i), JPlotController
                         .getInstance().getPath().get(i + 1));
             }
         }
     }
 
     private class PlotMouseWheelListener implements MouseWheelListener {
 
         @Override
         public void mouseWheelMoved(MouseWheelEvent event) {
             if (event.getWheelRotation() < 0) {
                 JPlotController.getInstance().zoomIn();
             } else {
                 JPlotController.getInstance().zoomOut();
             }
 
         }
 
     }
 
     @Override
     public Waypoint getActivePoint() {
         return activePoint;
     }
 
     @Override
     public int getActivePointIndexHolder() {
         return activePointIndexHolder;
     }
 
     @Override
     public void setActivePoint(Waypoint p) {
         activePoint = p;
     }
 
     @Override
     public void setActivePointIndexHolder(int index) {
         activePointIndexHolder = index;
     }
 
 }
