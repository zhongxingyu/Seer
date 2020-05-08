 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.*;
 import student.world.World;
 
 public class GridPanel extends JPanel {
 
     public int xSTART = 0;
     public int ySTART = 0;
     private int HEXSIZE = 100;
     public World WORLD;
     public GridPanel(World world) {
         WORLD = world;
     }
     
     /**
      * Calculates the dimensions and coordinates of the grid based off of
      * the panel size then paints the display.
      *
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */
     public void paintComponent(Graphics g) {
         /*
          * HEXSIZE = 
          * xSTART = 
          * ySTART =
          */
         /*        
          *   
          *
          //IMAGEWIDTH = (int)(this.getSize().getWidth()*9.0/10.0); //calculates the width of the images being displayed
          //IMAGEHEIGHT= (int)(this.getSize().getHeight()*9.0/10.0);
          
          //STARTX=(int)(this.getSize().getWidth()-IMAGEWIDTH)/2;
          //STARTY=(int)(this.getSize().getHeight()-IMAGEHEIGHT)/2;*/
 
         update(g);
     }
 
     /**
      *
      * @see javax.swing.JComponent#update(java.awt.Graphics)
      */
     public void update(Graphics g)//overrides update method to prevent continuous uneccessary repainting
     {
         drawGrid(HEXSIZE, g);
     }
 
     /**
      * Draws the entire grid of hexagons
      *
      * @param length the number of hexagons that make of a side length of the
      * hexagon grid
      * @param hexsize the size of each hexagon
      */
    /* public void drawGrid(int length, int hexsize, Graphics g) {
         int numrows = length * 2 - 1;
         int numcols = length;
         int medianrow = length - 1;
         int[] rowstartpoint = new int[]{(hexsize - 1) * (length - 1), 0};
         for (int i = 0; i < numrows; i++) {
             for (int j = 0; j < numcols; j++) {
                 //int[] point = point(i, j, rowstartpoint, hexsize);
                 drawHexagon(hexCoordinates(pnX(i, j) + xSTART, pnY(i, j) + ySTART, hexsize), g);
                 //drawHexagon(hexCoordinates(point[0]+xSTART, point[1]+ySTART, hexsize));
             }
             if (i > medianrow) {
                 rowstartpoint[0] = 0;
                 rowstartpoint[1] = rowstartpoint[1] + hexsize;
                 numcols--;
             } else {
                 rowstartpoint[0] = 0;
                 rowstartpoint[1] = rowstartpoint[1] + hexsize;
                 numcols++;
             }
 
         }
     }*/
 
     /**
      * Draws a hexagon of the given coordinates
      *
      * @param hexCoordinates the given coordinates
      * @param g graphics
      */
     public void drawHexagon(int[][] hexCoordinates, Graphics g) {
         for (int i = 0; i < hexCoordinates.length - 1; i++) {
             g.setColor(Color.GREEN);
             g.drawLine(hexCoordinates[i][0], hexCoordinates[i][1], hexCoordinates[i + 1][0], hexCoordinates[i + 1][1]);
         }
         g.drawLine(hexCoordinates[hexCoordinates.length - 1][0], hexCoordinates[hexCoordinates.length - 1][1], hexCoordinates[0][0], hexCoordinates[0][1]);
     }
 
     public int pnX(int row, int col) {
         return col * HEXSIZE * 2;
     }
 
     public int pnY(int row, int col) {
         return row * HEXSIZE * 2
                 + col
                 % 2 == 0
                 ? HEXSIZE
                 / 2 : 0;
     }
 
     /**
      * Returns the coordinates (in the form [x, y]) of a single hexagon drawn
      * starting from the given coordinates Precondition: length is divisible by
      * 4
      *
      * @param startX the given start x coordinate
      * @param startY the given start y coordinate
      * @param size the side length of square the hexagon will be contained in
      * @return
      */
     public int[][] hexCoordinates(int startX, int startY, int size) {
         int[][] hexCoord = new int[6][2]; //[Xj, Yj] for j= 1,2,3,4,5,6
         hexCoord[0] = new int[]{size / 4, 0};
         hexCoord[1] = new int[]{size * 3 / 4, 0};
         hexCoord[2] = new int[]{size, size / 2};
         hexCoord[3] = new int[]{size * 3 / 4, size};
         hexCoord[4] = new int[]{size / 4, size};
         hexCoord[5] = new int[]{0, size / 2};
         /*hexCoord[0] = new int[]{startX, B};
          hexCoord[1] = new int[]{sidelength/2, startY};
          hexCoord[2] = new int[]{A+sidelength, startY};
          hexCoord[3] = new int[]{2*sidelength, B};
          hexCoord[4] = new int[]{A+sidelength, 2*B};
          hexCoord[5] = new int[]{A, 2*B};*/
         return hexCoord;
     }
    /**
      * Draws the entire grid of hexagons
      */
     //@Deprecated
     public void drawGrid(int hexsize, Graphics gp) {//HWHdrawGrid(int hxsz, Graphics gp) {
         for (int c = 0; c < WORLD.width(); c++) {
             int x = pnX(0, c);
             for (int r = 0; r < WORLD.height(); r++) {
                 int y = pnY(r, c);
                 drawHexagon(hexCoordinates(x, y, hexsize), gp);
             }
         }
     }
     
 
 }
