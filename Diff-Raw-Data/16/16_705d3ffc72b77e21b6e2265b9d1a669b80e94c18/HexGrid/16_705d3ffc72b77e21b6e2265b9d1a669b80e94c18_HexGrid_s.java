 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package AntAttack_Group9;
 
 import java.awt.*;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class HexGrid {
 
     final static Color COLOURBLACK = Color.BLACK;
     final static Color COLOURRED = Color.RED;
     final static Color COLOURGREY = Color.DARK_GRAY;
     final static Color COLOURCELL = Color.ORANGE;
     final static Color COLOURGRID = Color.BLACK;
     final static Color COLOURONE = new Color(255, 255, 255, 200);
     final static Color COLOURONETXT = Color.BLUE;
     final static Color COLOURTWO = new Color(0, 0, 0, 200);
     final static Color COLOURTWOTXT = new Color(255, 100, 255);
     final static int EMPTY = 0;
    final static int BSIZE = 12; //board size.
    final static int HEXSIZE = 60;	//hex size in pixels
     final static int BORDERS = 15;
     final static int SCRSIZE = HEXSIZE * (BSIZE + 1) + BORDERS * 3; //screen size (vertical dimension).
     public final static boolean orFLAT = true;
     public final static boolean orPOINT = false;
     public static boolean ORIENT = orFLAT; //this is not used. We're never going to do pointy orientation
     public static boolean XYVertex = true;	//true: x,y are the co-ords of the first vertex.
 //false: x,y are the co-ords of the top left rect. co-ord.
     private static int s = 0;	// length of one side
     private static int t = 0;	// short side of 30o triangle outside of each hex
     private static int r = 0;	// radius of inscribed circle (centre to middle of each side). r= h/2
     private static int h = 0;	// height. Distance between centres of two adjacent hexes. Distance between two opposite sides in a hex.
     int[][] board = new int[BSIZE][BSIZE];
 
     private World world;
     
     void initGame(World w) {
 
         world = w;
         setXYasVertex(false); //RECOMMENDED: leave this as FALSE.
 
         setHeight(HEXSIZE); //Either setHeight or setSize must be run to initialize the hex
 
         for (int i = 0; i < BSIZE; i++) {
             for (int j = 0; j < BSIZE; j++) {
                 board[i][j] = EMPTY;
             }
         }
         
         //set up board here
         board[0][1] = (int) 'A';
         board[3][3] = (int) 'A';
         board[4][3] = (int) 'Q';
         board[4][4] = -(int) 'B';
     }
 
     public void createAndShowGUI() {
         DrawingPanel panel = new DrawingPanel();
 
 
         //JFrame.setDefaultLookAndFeelDecorated(true);
         JFrame frame = new JFrame("Hex Testing 4");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Container content = frame.getContentPane();
         content.add(panel);
         //this.add(panel); -- cannot be done in a static context
         //for hexes in the FLAT orientation, the height of a 10x10 grid is 1.1764 * the width. (from h / (s+t))
         frame.setSize((int)(SCRSIZE / 1.05), (int)(SCRSIZE / 1.2));
         frame.setResizable(false);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 
     class DrawingPanel extends JPanel {
         //mouse variables here
         //Point mPt = new Point(0,0);
 
         public DrawingPanel() {
             setBackground(Color.WHITE);
         }
 
         @Override
         public void paintComponent(Graphics g) {
             Graphics2D g2 = (Graphics2D) g;
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
             super.paintComponent(g2);
 //            //draw grid
 //            for (int i = 0; i < BSIZE; i++) {
 //                for (int j = 0; j < BSIZE; j++) {
 //                    drawHex(i, j, g2);
 //                }
 //            }
 //            //fill in hexes
 //            for (int i = 0; i < BSIZE; i++) {
 //                for (int j = 0; j < BSIZE; j++) {
 //                    //if (board[i][j] < 0) hexmech.fillHex(i,j,COLOURONE,-board[i][j],g2);
 //                    //if (board[i][j] > 0) hexmech.fillHex(i,j,COLOURTWO, board[i][j],g2);
 //                    fillHex(i, j, board[i][j], g2);
 //                }
 //            }
             
             for (int i = 0; i < world.height; i++) {
                 for (int j = 0; j < world.width; j++) {
                     drawHex(i, j, g2);
                     Cell c = world.cells[i][j];
                     if(c.isEmpty()) {
                         fillHex(i, j, 0, g2);
                     } else if (c.rock) {
                         fillHex(i, j, 99, g2);
                     } else if (c.anthill != null) {
                         if(c.anthill.equalsIgnoreCase("black")) {
                             fillHex(i, j, 30, g2);
                         } else if(c.anthill.equalsIgnoreCase("red")) {
                             fillHex(i, j, 31, g2);
                         }
                     } else if (c.ant != null) {
                         if(c.ant.getColour()) {
                             fillHex(i, j, 6, g2);
                         } else {
                             fillHex(i, j, 7, g2);
                         }
                     } else if (c.food > 0) {
                         fillHex(i, j, c.food, g2);
                     }
                     
                 }
             }
 
             //g.setColor(Color.RED);
             //g.drawLine(mPt.x,mPt.y, mPt.x,mPt.y);
         }
     } // end of DrawingPanel class
 
     public static void setXYasVertex(boolean b) {
         XYVertex = b;
     }
 
     /**
      * This functions takes the Side length in pixels and uses that as the basic
      * dimension of the hex. It calculates all other needed constants from this
      * dimension.
      */
     public static void setSide(int side) {
         s = side;
         t = (int) (s / 2);	//t = s sin(30) = (int) CalculateH(s);
         r = (int) (s * 0.8660254037844);	//r = s cos(30) = (int) CalculateR(s);
         h = 2 * r;
     }
 
     public static void setHeight(int height) {
         h = height;	// h = basic dimension: height (distance between two adj centresr aka size)
         r = h / 2;	// r = radius of inscribed circle
         s = (int) (h / 1.73205);	// s = (h/2)/cos(30)= (h/2) / (sqrt(3)/2) = h / sqrt(3)
         t = (int) (r / 1.73205);	// t = (h/2) tan30 = (h/2) 1/sqrt(3) = h / (2 sqrt(3)) = r / sqrt(3)
     }
 
     /**
      * *******************************************************
      * Name: hex() Parameters: (x0,y0) This point is normally the top left
      * corner of the rectangle enclosing the hexagon. However, if XYVertex is
      * true then (x0,y0) is the vertex of the top left corner of the hexagon.
      * Returns: a polygon containing the six points. Called from: drawHex(),
      * fillhex() Purpose: This function takes two points that describe a hexagon
      * and calculates all six of the points in the hexagon.
      * *******************************************************
      */
     public static Polygon hex(int x0, int y0) {
 
         int y = y0 + BORDERS;
         int x = x0 + BORDERS; // + (XYVertex ? t : 0); //Fix added for XYVertex = true.
         // NO! Done below in cx= section
         if (s == 0 || h == 0) {
             System.out.println("ERROR: size of hex has not been set");
             return new Polygon();
         }
 
         int[] cx, cy;
 
         //I think that this XYvertex stuff is taken care of in the int x line above. Why is it here twice?
         if (XYVertex) {
             cx = new int[]{x, x + s, x + s + t, x + s, x, x - t}; //this is for the top left vertex being at x,y. Which means that some of the hex is cutoff.
         } else {
             //cx = new int[]{x + t, x + s + t, x + s + t + t, x + s + t, x + t, x};	//this is for the whole hexagon to be below and to the right of this point
             cx = new int[]{x + r, x + h, x + h, x + r, x, x};	//this is for the whole hexagon to be below and to the right of this point
         }
         //cy = new int[]{y, y, y + r, y + r + r, y + r + r, y + r};
         cy = new int[]{y, y + t, y + t + s, y + 2*t + s, y + t + s, y + t};
         
         Polygon rotated = new Polygon(cx, cy, 6);
         
         
         return rotated;
 
         /*
          * x=200; poly = new Polygon(); poly.addPoint(x,y);
          * poly.addPoint(x+s,y); poly.addPoint(x+s+t,y+r);
          * poly.addPoint(x+s,y+r+r); poly.addPoint(x,y+r+r);
          * poly.addPoint(x-t,y+r);
          */
     }
 
     /**
      * ******************************************************************
      * Name: drawHex() Parameters: (i,j) : the x,y coordinates of the initial
      * point of the hexagon g2: the Graphics2D object to draw on. Returns: void
      * Calls: hex() Purpose: This function draws a hexagon based on the initial
      * point (x,y). The hexagon is drawn in the colour specified in
      * hexgame.COLOURELL.
      * *******************************************************************
      */
     public static void drawHex(int i, int j, Graphics2D g2) {
         //int x = i * (s + t);
         //int y = j * h + (i % 2) * h / 2;
         int x;
         if(i % 2 != 0){
            x = j * h;
        } else {
             x = j * h + r;
         }
         int y = i * (h - (t / 2));
         Polygon poly = hex(x, y);
         g2.setColor(COLOURCELL);
         g2.fillPolygon(poly);
         g2.setColor(COLOURGRID);
         g2.drawPolygon(poly);
     }
 
     /**
      * *************************************************************************
      * Name: fillHex() Parameters: (i,j) : the x,y coordinates of the initial
      * point of the hexagon n : an integer number to indicate a letter to draw
      * in the hex g2 : the graphics context to draw on Return: void Called from:
      * Calls: hex() Purpose: This draws a filled in polygon based on the
      * coordinates of the hexagon. The colour depends on whether n is negative
      * or positive. The colour is set by hexgame.COLOURONE and
      * hexgame.COLOURTWO. The value of n is converted to letter and drawn in the
      * hexagon.
      * ***************************************************************************
      */
     public static void fillHex(int i, int j, int n, Graphics2D g2) {
         char c = 'o';
         //int x = i * (s + t);
         //int y = j * h + (i % 2) * h / 2;
         int x;
         if(i % 2 != 0){
            x = j * h;
        } else {
             x = j * h + r;
         }
         int y = i * (h - (t / 2));
         
         switch(n) {
             // BLANK
             case 0:
                 g2.setColor(COLOURCELL);
                 g2.fillPolygon(hex(x, y));
                 break;
             // FOOD
             case 1:
                 g2.setColor(COLOURCELL);
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURONETXT);
                 g2.drawString("" + n, x + t + BORDERS, y + r + BORDERS + 4);
                 break;
             case 2:
                 g2.setColor(COLOURCELL);
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURONETXT);
                 g2.drawString("" + n, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 3:
                 g2.setColor(COLOURCELL);
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURONETXT);
                 g2.drawString("" + n, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 4:
                 g2.setColor(COLOURCELL);
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURONETXT);
                 g2.drawString("" + n, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 5:
                 g2.setColor(COLOURCELL);
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURONETXT);
                 g2.drawString("" + n, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             // END FOOD
             // BLACK ANT
             case 6:
                 g2.setColor(COLOURBLACK);
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURONETXT);
                 break;
             // RED ANT
             case 7:
                 g2.setColor(COLOURRED);
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURONETXT);
                 break;
             // BLACK MARKERS
             case 10:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURBLACK);
                 g2.drawString("" + 1, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 11:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURBLACK);
                 g2.drawString("" + 2, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 12:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURBLACK);
                 g2.drawString("" + 3, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 13:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURBLACK);
                 g2.drawString("" + 4, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 14:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURBLACK);
                 g2.drawString("" + 5, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 15:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURBLACK);
                 g2.drawString("" + 6, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             // END BLACK MARKERS
             // RED MARKERS
             case 20:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURRED);
                 g2.drawString("" + 1, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 21:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURRED);
                 g2.drawString("" + 2, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 22:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURRED);
                 g2.drawString("" + 3, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 23:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURRED);
                 g2.drawString("" + 4, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 24:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURRED);
                 g2.drawString("" + 5, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             case 25:
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURRED);
                 g2.drawString("" + 6, x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             // END RED MARKERS
             // BLACK ANTHILL
             case 30: 
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURBLACK);
                 g2.drawString("H", x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             // RED ANTHILL
             case 31: 
                 g2.fillPolygon(hex(x, y));
                 g2.setColor(COLOURRED);
                 g2.drawString("H", x + r + BORDERS, y + r + BORDERS + 4);
                 break;
             // ROCK
             case 99:
                 g2.setColor(COLOURGREY);
                 g2.fillPolygon(hex(x, y));
                 break;
         }
 //        if (n < 0) {
 //            g2.setColor(COLOURONE);
 //            g2.fillPolygon(hex(x, y));
 //            g2.setColor(COLOURONETXT);
 //            c = (char) (-n);
 //            g2.drawString("" + c, x + r + BORDERS, y + r + BORDERS + 4); //FIXME: handle XYVertex
 //            //g2.drawString(x+","+y, x+r+BORDERS, y+r+BORDERS+4);
 //        }
 //        if (n > 0) {
 //            g2.setColor(COLOURTWO);
 //            g2.fillPolygon(hex(x, y));
 //            g2.setColor(COLOURTWOTXT);
 //            c = (char) n;
 //            g2.drawString("" + c, x + r + BORDERS, y + r + BORDERS + 4); //FIXME handle XYVertex
 //            //g2.drawString(i+","+j, x+r+BORDERS, y+r+BORDERS+4);
 //        }
     }
 }
