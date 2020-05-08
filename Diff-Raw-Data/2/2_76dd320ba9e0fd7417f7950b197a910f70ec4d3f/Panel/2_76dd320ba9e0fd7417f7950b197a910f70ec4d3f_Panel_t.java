 package gui.graphical;
 
 // Default Libraries
 import java.io.IOException;
 import java.lang.reflect.Field;
 
 // Graphical Libraries (AWT)
 import java.awt.*;
 import java.awt.TexturePaint;
 import java.awt.image.BufferedImage;
 
 // Graphical Libraries (Swing)
 import javax.swing.*;
 import javax.imageio.ImageIO;
 import javax.swing.SwingUtilities;
 
 // Libraries
 import arena.Map;
 import parameters.*;
 
 public class Panel extends JPanel 
     implements Game
 {
     int MAP_SIZE = 16;
     private Cell[][] cell = new Cell[MAP_SIZE][MAP_SIZE];
     private Map map;
     
     Panel(int R, int width, int height, Map map) 
     {
         this.map = map;
         
         // Preferences
         setBackground(Color.black);
         setPreferredSize(new Dimension(width, height));
         
         int Dx = (int) ( 2*R * Math.sin(Math.PI/3) ); 
         int Dy = 3 * R/2;
        
         // Put images in the screen
         int Δ  = 0;
         for (int i = 0; i < MAP_SIZE; i++)
             for (int j = 0; j < MAP_SIZE; j++) 
             {
                 cell[i][j] = new Cell(
                    Δ + R + i*Dx, R + j*Dy, R, map.map[j][i]
                 ); 
                 Δ = (Δ == 0) ? Dx/2 : 0;
             }
     }
     
     /**
      * Paint hexagons on the screen.<br>
      * At each step, repaint the cells
      * as they need changes.
      * @param g Graphic object with all context 
      *          needed to render the image
      */
     public void paintComponent(Graphics g) 
     {
         super.paintComponent(g);
         Graphics2D g2d = (Graphics2D) g;
         for (int i = 0; i < MAP_SIZE; i++) 
             for (int j = 0; j < MAP_SIZE; j++)
                 cell[i][j].draw(g); 
     }
 }
