 package cs437.som.visualization;
 
 import cs437.som.Dimension;
 import cs437.som.SelfOrganizingMap;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 public class SOMHeatMap extends JFrame {
     // The pixel alpha.
     private static final int ALPHA = 0xFF000000;
     private static final int BYTE_MAX = 255;
     private static final int BYTE_WIDTH = 8;
 
     /**
      * The frame width.
      */
     public static final int WIDTH = 500;
 
     /**
      * The frame height.
      */
     public static final int HEIGHT = 500;
 
     private SelfOrganizingMap som;
     private Dimension dims;
     private BufferedImage img;
     private int neuronCount;
    private double max = 0;
 
     public SOMHeatMap(SelfOrganizingMap map) {
         super("SOM heat map");
 
         som = map;
         dims = som.getGridSize();
         neuronCount = som.getNeuronCount();
         img = new BufferedImage(dims.x, dims.y, BufferedImage.TYPE_INT_ARGB);
 
         setSize(WIDTH, HEIGHT);
         setVisible(true);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         createBufferStrategy(2);
     }
 
     public void draw() {
         Graphics g = getBufferStrategy().getDrawGraphics();
         g.drawImage(img.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT), 0, 0, null);
         getBufferStrategy().show();
     }
 
     public void update(double[] sample) {
         double[] distances = new double[neuronCount];
         int[] pts = new int[neuronCount];
 
         double dist, diff;
        max = Math.max(som.distanceToInput(0, sample), max);
         double min = 0;
 
         for (int i = 1; i < neuronCount; i++) {
             dist = som.distanceToInput(i, sample);
             distances[i] = dist;
             if (dist > max)
                 max = dist;
             else if (dist < min)
                 min = dist;
         }
 
         diff = max - min;
 
         for (int i = 1; i < neuronCount; i++) {
             int value = BYTE_MAX - (int)(BYTE_MAX * ((distances[i] - min) / max));
             pts[i] |= ALPHA;
             pts[i] |= value << (2 * BYTE_WIDTH);
             pts[i] |= value << BYTE_WIDTH;
             pts[i] |= value;
         }
 
         img.setRGB(0,0, dims.x, dims.y, pts, 0, dims.x);
     }
 
 
 }
