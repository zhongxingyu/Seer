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
 
     private static final String RESOURCE = "/images/background/";
     private static final long serialVersionUID = 1L;
     private static final int FIRST_TURTLE = 0;
     private static final int GRID_VALUE = 100;
     private Color trailColor = Color.BLACK;
     private static final Color GRID_COLOR = Color.BLACK;
     private static final int GRID_LABEL_OFFSET = 20;
     private boolean myToggledOn = true;
     private boolean toggledOn = true;
     private boolean dashed = true;
     private boolean penIsDown = true;
     private Trail myTrail;
     private Canvas myView;
     private Map<Integer, Turtle> myTurtles;
     private Set<Turtle> isActive;
     private Set<Integer> unpaintedTrails;
     private List<Turtle> lastEdited; 
     private List<Turtle> lastUndid; 
     private List<Stamp> myStamps; 
     private java.awt.Image myBackgroundImage;
     private int penWidth=4;  
     private Map <Integer, Color> colorPalette; 
     private int currentColorIndex=0; 
 
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
         unpaintedTrails = new HashSet<Integer>();
         isActive = new HashSet<Turtle>();
         lastEdited= new ArrayList<Turtle>(); 
         lastUndid= new ArrayList<Turtle>();
         myStamps= new ArrayList<Stamp>();
         isActive.add(myTurtles.get(FIRST_TURTLE));
         colorPalette=new HashMap<Integer,Color>(); 
 
         setVisible(true);
 
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
             if (isActive.contains(t)) {
                 t.changeTurtleImage("turtle2.gif");
             }
             t.paint(pen);
             lastEdited.add(t);
             if (penIsDown) {
                 t.addTrail();
             }
             
             if (!penIsDown) {
                 unpaintedTrails.add(myTrail.getTrails().size());
             }
         }
     }
     
     /*
      * Paints all stamps
      */
     private void paintStamps (Graphics2D pen) {
 
         for (Stamp s: myStamps){
         	s.paint(pen);
         }
     }
 
     /**
      * 
      * Paints all trails that the turtle has traveled
      */
     private void paintTrails (Graphics2D pen) {
         pen.setColor(trailColor);
         if (dashed) {
             Stroke drawingStroke =
                     new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                                     new float[] { 9 }, 0);
             pen.setStroke(drawingStroke);
         }
         // Sets to default stroke
         else {
             pen.setStroke(new BasicStroke(penWidth));
         }
         List<Location> trails = myTrail.getTrails();
         if (!(trails.isEmpty())) {
             Location prevLocation = myTrail.getTrails().get(FIRST_TURTLE);
             Location newLocation;
             for (int i = 1; i < trails.size(); i++) {
                 newLocation = trails.get(i);
                 if (!unpaintedTrails.contains(i)) {
                     pen.drawLine((int) prevLocation.getX(), (int) prevLocation.getY(),
                                  (int) newLocation.getX(), (int) newLocation.getY());
                 }
                 prevLocation = newLocation;
             }
         }
     }
 
     private void paintGrid (Graphics2D pen) {
 
         if (myToggledOn) {
             pen.setColor(GRID_COLOR);
             pen.setStroke(new BasicStroke());
             if (toggledOn) {
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
      * 
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
     * @param colorIndex    The Color index for the background color
     */
    public void changeBackgroundColor(int colorIndex) {
        resetBackgroundImage();
        setBackgroundColor(colorIndex);
    }
    
    private void resetBackgroundImage() {
        myBackgroundImage = null;
    }
     
     /**
      * 
      */
     public void setDashed () {
         dashed = true;
         repaint();
     }
 
     public void setSolid () {
         dashed = false;
         repaint();
     }
 
     public void toggleGridOff () {
         toggledOn = false;
         repaint();
     }
 
     public void toggleGridOn () {
         toggledOn = true;
         repaint();
     }
 
     public void penDown () {
         penIsDown = true;
     }
 
     public void penUp () {
         penIsDown = false;
     }
 
     /**
      * 
      * @param color
      */
     public void setTrailColor (int color) {
         switch (color) {
             case 1:
                 trailColor = Color.BLACK;
                 break;
             case 2:
                 trailColor = Color.BLUE;
                 break;
             case 3:
                 trailColor = Color.GREEN;
                 break;
             case 4:
                 trailColor = Color.RED;
                 break;
             case 5:
                 trailColor = Color.YELLOW;
         }
         repaint();
     }
     
     public void undo(){
         Turtle toUndo=lastEdited.get(lastEdited.size()-1);
     	toUndo.undoMove();
     	lastUndid.add(toUndo);
     	repaint();
     }
     public void redo(){
     	Turtle toRedo=lastUndid.get(lastUndid.size()-1);
     	toRedo.redoMove();
     	lastEdited.add(toRedo);
     	repaint();
     }
     public void editPenWidth(int newWidth){
     	penWidth=newWidth;
     	repaint(); 
     }
     
     public void addToColorPalette (int index, float r, float g, float b){
     	colorPalette.put (index, new Color(r, g, b));
     	setPenColor(index);
     }
     public void setPenColor(int index){
     	trailColor= colorPalette.get(index);
     	currentColorIndex=index;
     	repaint();
     }
     
     public int getCurentColorIndex(){
     	return currentColorIndex; 
     }
     
     /*
      * Creates stamp
      */
     public void createStamp(Turtle t){
     	myStamps.add(new Stamp(t));
     	repaint();
     }
     /*
      * Clears stamps
      */
     public void clearStamps(){
     	myStamps.clear();
     	repaint();
     }
     
     
 }
