 package cs437.som.visualization;
 
 import cs437.som.SelfOrganizingMap;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Visualization for SOM with a 2-dimensional input.
  */
 public class SOM2dPlotter extends JFrame {
     private static final long serialVersionUID = 0L;
     
     public static final int WIDTH = 400;
     public static final int HEIGHT = 400;
 
     SelfOrganizingMap som = null;
     private int neuronCount = 0;
 
     /**
      * Create and setup a dot plot for a 2D input SOM.
      * @param map The SOM to plot.
      */
     public SOM2dPlotter(SelfOrganizingMap map) {
         super("SOM Plot");
         if (map.getInputLength() != 2) {
            throw new IllegalArgumentException("SOM does not som 2d inputs");
         }
 
         this.som = map;
         neuronCount = map.getNeuronCount();
         setSize(WIDTH, HEIGHT);
         setVisible(true);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         createBufferStrategy(2);
     }
 
     /**
      * Draw the current "locations" of the SOM's neurons.
      */
     public void draw() {
         Graphics g = getBufferStrategy().getDrawGraphics();
         g.setColor(Color.white);
         g.fillRect(0, 0, getWidth(), getHeight());
         g.setColor(Color.black);
         
         double[][] points = new double[neuronCount][2];
 
         // find the x- and y-axis spread
         double xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE;
         double ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE;
         for (int i = 0; i < neuronCount; i++) {
             points[i][0] = som.getWeight(i, 0);
             points[i][1] = som.getWeight(i, 1);
             xmin = Math.min(xmin, points[i][0]);
             ymin = Math.min(ymin, points[i][1]);
             xmax = Math.max(xmax, points[i][0]);
             ymax = Math.max(ymax, points[i][1]);
         }
 
         // compute the scaling factor
         double dx = getWidth() / (xmax - xmin);
         double dy = getHeight() / (ymax - ymin);
 
         for (double[] pt : points) {
             // zero-base the coordinates and scale them to window size
             int x = (int) Math.round((pt[0] - xmin) * dx);
             int y = (int) Math.round((pt[1] - ymin) * dy);
             g.drawOval(x - 2, y - 2, 4, 4);
         }
 
         getBufferStrategy().show();
     }
 
     @Override
     public String toString() {
         return "SOM2dPlotter{som=" + som + '}';
     }
 }
