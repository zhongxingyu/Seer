 package view;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.swing.ImageIcon;
 import object.Stamp;
 import object.Trail;
 import object.Turtle;
 import util.Location;
 
 
 /**
  * 
  * @author Leonard and Alan
  * 
  */
 public class TurtleArea extends Window {
 
     /**
      * 
      */
     public static final int NO_KEY_PRESSED = -1;
     private static final int LIMIT = 9;
     private static final int PENWIDTH = 4;
     private static final String RESOURCE = "/images/background/";
     private static final long serialVersionUID = 1L;
     private static final int FIRST_TURTLE = 0;
     private static final int GRID_VALUE = 100;
     private static final Color GRID_COLOR = Color.BLACK;
     private static final int GRID_LABEL_OFFSET = 20;
     private boolean myToggledOn = true;
     private boolean myDashed = true;
     private boolean myPenIsDown = true;
     private Color myTrailColor = Color.BLACK;
     private Trail myTrail;
     private Canvas myView;
     private Map<Integer, Turtle> myTurtles;
     private Set<Turtle> myIsActive;
     private Set<Integer> myUnpaintedTrails;
     private List<Turtle> myLastEdited;
     private List<Turtle> myLastUndid;
     private List<Stamp> myStamps;
     private java.awt.Image myBackgroundImage;
     private int myPenWidth = PENWIDTH;
     private Map<Integer, Color> myColorPalette;
     private int myCurrentColorIndex = 0;
 
     /**
      * 
      * @param size
      *        size of display area
      * @param myTurtle
      *        pen image
      * @param canvas
      *        view
      *        Constructs TurtleArea
      */
     public TurtleArea (Dimension size, Map<Integer, Turtle> myTurtle, Canvas canvas) {
         super(size, "English");
         setFocusable(true);
 
         myBackgroundImage =
                 new ImageIcon(getClass().getResource(RESOURCE + "dukeblue.gif")).getImage();
 
         myView = canvas;
         myTurtles = myTurtle;
         myTrail = myTurtles.get(FIRST_TURTLE).getTrail();
         myUnpaintedTrails = new HashSet<Integer>();
         myIsActive = new HashSet<Turtle>();
         myLastEdited = new ArrayList<Turtle>();
         myLastUndid = new ArrayList<Turtle>();
         myStamps = new ArrayList<Stamp>();
         myIsActive.add(myTurtles.get(FIRST_TURTLE));
         myColorPalette = new HashMap<Integer, Color>();
 
         setVisible(true);
 
     }
 
     /**
      * 
      * Return map of turtles
      */
     public Map<Integer, Turtle> getMyTurtles () {
         return myTurtles;
     }
 
     /**
      * 
      * @param index
      *        Index of turtle you are searching for
      * @return
      *         Returns requested Turtle based on index
      */
     public Turtle getMyTurtle (int index) {
        System.out.println("test");
         return myTurtles.get(index);
     }
 
     /**
      * Paints turtle and trail
      * 
      * @param pen
      *        Graphics pen for drawing
      */
     @Override
     public void paint (Graphics pen) {
         super.paintComponent(pen);
 
         if (myBackgroundImage != null) {
             pen.drawImage(myBackgroundImage, 0, 0, null);
         }
 
         paintTurtle((Graphics2D) pen);
         paintTrails((Graphics2D) pen);
         paintGrid((Graphics2D) pen);
         paintStamps((Graphics2D) pen);
 
         if (myTurtles.get(FIRST_TURTLE).getAngle() != 0.0) {
             rotateImage((Graphics2D) pen);
         }
 
         Toolkit.getDefaultToolkit().sync();
         pen.dispose();
     }
 
     /**
      * Paints rotated image
      * 
      * @param pen
      *        pen for painting
      */
     private void rotateImage (Graphics2D pen) {
         for (Turtle t : myTurtles.values()) {
             t.paint(pen, t.getCenter(), t.getSize(), t.getAngle());
         }
         myView.update();
     }
 
     /**
      * Updates turtles' trails and their location
      */
     public void update () {
         myView.update();
 
     }
 
     /**
      * paints all the turtles and updates their trails
      */
     private void paintTurtle (Graphics2D pen) {
 
         for (Turtle t : myTurtles.values()) {
 //            if (myIsActive.contains(t)) {
 //                t.changeTurtleImage("turtle2.gif");
 //            }
             t.paint(pen);
             myLastEdited.add(t);
             if (myPenIsDown) {
                 t.addTrail();
             }
 
             if (!myPenIsDown) {
                 myUnpaintedTrails.add(myTrail.getTrails().size());
             }
         }
     }
 
     /**
      * Paints all stamps
      */
     private void paintStamps (Graphics2D pen) {
 
         for (Stamp s : myStamps) {
             s.paint(pen);
         }
     }
 
     /**
      * 
      * Paints all trails that the turtle has traveled
      */
     private void paintTrails (Graphics2D pen) {
         pen.setColor(myTrailColor);
         if (myDashed) {
             Stroke drawingStroke =
                     new BasicStroke(myPenWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                                     new float[] {LIMIT}, 0);
             pen.setStroke(drawingStroke);
         }
         // Sets to default stroke
         else {
             pen.setStroke(new BasicStroke(myPenWidth));
         }
         List<Location> trails = myTrail.getTrails();
         if (!(trails.isEmpty())) {
             Location prevLocation = myTrail.getTrails().get(FIRST_TURTLE);
             Location newLocation;
             for (int i = 1; i < trails.size(); i++) {
                 newLocation = trails.get(i);
                 if (!myUnpaintedTrails.contains(i)) {
                     pen.drawLine((int) prevLocation.getX(), (int) prevLocation.getY(),
                                  (int) newLocation.getX(), (int) newLocation.getY());
                 }
                 prevLocation = newLocation;
             }
         }
     }
 
     /**
      * 
      * Paints grid for every interval of 100
      */
     private void paintGrid (Graphics2D pen) {
         if (myToggledOn) {
             pen.setColor(GRID_COLOR);
             pen.setStroke(new BasicStroke());
             if (myToggledOn) {
                 for (int i = 0; i < getWidth(); i += GRID_VALUE) {
                     pen.drawLine(i, 0, i, getHeight());
                     pen.drawString(Integer.toString(i), i, GRID_LABEL_OFFSET);
                 }
                 for (int i = 0; i < getWidth(); i += GRID_VALUE) {
                     pen.drawLine(0, i, getWidth(), i);
                     pen.drawString(Integer.toString(i), 0, i);
                 }
             }
 
         }
     }
 
     /**
      * @param filename
      *        name of new background
      */
     public void changeBackgroundImage (String filename) {
         myBackgroundImage =
                 new ImageIcon(getClass().getResource(RESOURCE + filename)).getImage();
         repaint();
     }
 
     /**
      * Removes the background image and sets a new background color
      * 
      * @param colorIndex The Color index for the background color
      */
     public void changeBackgroundColor (int colorIndex) {
         resetBackgroundImage();
         setBackgroundColor(colorIndex);
     }
 
     private void resetBackgroundImage () {
         myBackgroundImage = null;
     }
 
     /**
      * sets pen to paint dashed lines for trails
      */
     public void setDashed () {
         myDashed = true;
         repaint();
     }
 
     /**
      * sets pen to paint solid lines for trails
      */
     public void setSolid () {
         myDashed = false;
         repaint();
     }
 
     /**
      * Tells pen not to paint grid
      */
     public void toggleGridOff () {
         myToggledOn = false;
         repaint();
     }
 
     /**
      * Tells pen to paint grid
      */
     public void toggleGridOn () {
         myToggledOn = true;
         repaint();
     }
 
     /**
      * Sets the pen to leave a trail
      */
     public void penDown () {
         myPenIsDown = true;
     }
 
     /**
      * Sets the pen not to leave a trail
      */
     public void penUp () {
         myPenIsDown = false;
     }
 
     /**
      * Undoes previous move and removes it from list of lastEdited
      * locations and adds it to list of undone locations
      */
     public void undo () {
         Turtle toUndo = myLastEdited.get(myLastEdited.size() - 1);
         toUndo.undoMove();
         myLastUndid.add(toUndo);
         repaint();
     }
 
     /**
      * Redoes previous move and removes it from list of undone
      * locations and puts in lastEdited locations
      */
     public void redo () {
         Turtle toRedo = myLastUndid.get(myLastUndid.size() - 1);
         toRedo.redoMove();
         myLastEdited.add(toRedo);
         repaint();
     }
 
     /**
      * 
      * @param newWidth
      *        Sets pen width to newWidth
      */
     public void editPenWidth (int newWidth) {
         myPenWidth = newWidth;
         repaint();
     }
 
     /**
      * 
      * @param index
      *        Index that correlates to color value, created by user command
      * @param r
      *        red color value
      * @param g
      *        green color value
      * @param b
      *        blue color value
      */
     public void addToColorPalette (int index, float r, float g, float b) {
         myColorPalette.put(index, new Color(r, g, b));
         setPenColor(index);
     }
 
     /**
      * 
      * @param index
      *        returns color based on index set by user
      */
     public void setPenColor (int index) {
         if (myColorPalette.containsKey(index)) {
             myTrailColor = myColorPalette.get(index);
             myCurrentColorIndex = index;
             repaint();
         }
     }
 
     /**
      * return the current color index
      */
     public int getCurrentColorIndex () {
         return myCurrentColorIndex;
     }
 
     /**
      * 
      * @param t
      *        Turtle to create a stamp of
      * 
      *        Creates a stamp of the turtle
      */
     public void createStamp (Turtle t) {
         myStamps.add(new Stamp(t));
         repaint();
     }
 
     /**
      * Clears stamps
      */
     public void clearStamps () {
         myStamps.clear();
         repaint();
     }
 
 }
