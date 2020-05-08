 package se.liu.ida.emiva760.tddc69.projekt;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * ==========================
  * File: GraphicalViewer.java
  * ==========================
  *
  * Handles the graphics
  */
 public class GraphicalViewer extends JComponent implements GridListener {
     private static final int ROWS = 540;
     private static final int COLUMNS = 960;
     private static final int BLOCKHEIGHT = ROWS / 20;
     private static final int BLOCKWIDTH = COLUMNS / 20;
 
     private final GameGrid game;
 
     public GraphicalViewer(GameGrid game) {
         this.game = game;
         game.addGridListener(this);
     }
 
     @Override
     public Dimension getPreferredSize() {
        return new Dimension(WIDTH,HEIGHT);
     }
 
     @Override
     public void paintComponent(final Graphics g) {
         final Graphics2D g2 = (Graphics2D) g;
         paintBackground(g2);
         paintObjects(g2);
     }
 
     public void paintBackground(final Graphics2D g2) {
         g2.setColor(Color.WHITE);
        g2.fill(new Rectangle(0,0,WIDTH,HEIGHT));
     }
 
     // =======================================================================
     // Tror att utritningsfelet ligger hos dessa två funktioner. Kolla!
     
     public void paintObjects(final Graphics2D g2) {
         for (int i = 0; i < game.getRows(); i++) {
             for (int j = 0; j < game.getColumns(); j++) {
                if ()
             }
         }
     }

     /*
     public void paintObjects(final Graphics2D g2) {
         for (int i = 0; i < game.getRows(); i++) {
             for (int j = 0; j < game.getColumns(); j++) {
                 g2.setColor(convertToColor(game.getSquareColor(i, j)));
                 int blockY = i*BLOCKHEIGHT;
                 int blockX = j*BLOCKWIDTH;
                 g2.fill(new Rectangle(blockX, blockY, BLOCKWIDTH, BLOCKHEIGHT));
             }
         }
     }
 
     public Color convertToColor(SquareColor color) {
         if (color == null) return Color.WHITE;
         else {
             switch (color) {
                 case BLUE:
                     return Color.BLUE;
                 case RED:
                     return Color.RED;
                 case YELLOW:
                     return Color.YELLOW;
                 default:
                     return Color.WHITE;
             }
         }
     }
     */
     
     // =======================================================================
 
     @Override
     public void gridChanged() {
         this.repaint();
     }
 }
