 package object;
 
 import java.awt.Dimension;
 import util.Animal;
 import util.Location;
 import util.Pixmap;
 import view.Canvas;
 
 
 /**
  * 
  * @author Richard Yang
  * @author Leonard
  */
 
 public class Turtle extends Animal {
 
     /**
      * Default dimension size
      */
     public static final Dimension DEFAULT_SIZE = new Dimension(50, 50);
 
     private static final Pixmap TURTLE_IMAGE = new Pixmap("turtle.gif");
     private double myAngle;
     private boolean myLeftTrail;
     private Trail myTrail;
     private Trail undoneTrails; 
     private boolean myVisible;
     private String myTurtleName = "turtle.gif";
     private static int myGlobalTurtleID = -1;
     private int myTurtleID;
 
     /**
      * Constructs turtle object
      * 
      * @param myLocation myLocation
      * @param angle myAngle
      */
     public Turtle (Location myLocation, double angle) {
         super(TURTLE_IMAGE, myLocation, DEFAULT_SIZE);
         myAngle = angle;
         myTrail = new Trail();
         undoneTrails=new Trail(); 
         myGlobalTurtleID++;
         myTurtleID = myGlobalTurtleID;
     }
 
     /**
      * Constructs turtle object with image
      */
     public Turtle () {
         super(TURTLE_IMAGE, new Location(Canvas.TURTLE_AREA_SIZE.width / 2,
                                          Canvas.TURTLE_AREA_SIZE.height / 2), DEFAULT_SIZE);
         myAngle = 0;
         myTrail = new Trail();
     }
     
     public int getID() {
         return myTurtleID;
     }
     
     /**
      * initialize turtle at center
      */
     public void initialize () {
         super.setCenter(new Location(0, 0));
     }
 
     /**
      * add a trail
      */
     public void addTrail () {
         myTrail.addTrail(new Location(getX(), getY()));
     }
 
     /**
      * Are there trails left?
      */
     public void leftTrail () {
         myLeftTrail = true;
     }
 
     /**
      * avoid trail
      */
     public void avoidTrail () {
         myLeftTrail = false;
     }
 
     /**
      * clear all trails
      */
     public void clearTrail () {
         myTrail.clearTrail();
     }
 
     /**
      * set visible
      */
     public void setVisible () {
         myVisible = true;
     }
 
     /**
      * set to invisible
      */
     public void setInvisible () {
         myVisible = false;
     }
 
     /**
      * get angle
      * 
      * @return myAngle
      */
     public double getAngle () {
         return myAngle;
     }
 
     /**
      * Set angle
      * 
      * @param angle angle to set to
      */
     public void setAngle (double angle) {
         myAngle = angle;
     }
 
     /**
      * Does it leave a trail?
      * 
      * @return leftTrail
      */
     public boolean isLeaveTrail () {
         return myLeftTrail;
     }
 
     /**
      * is it visible?
      * 
      * @return my visible
      */
     public boolean isVisible () {
         return myVisible;
     }
 
     /**
      * double to string
      * 
      * @param num number
      * @return to string
      */
     public String toString (double num) {
         return Double.toString(num);
     }
 
     /**
      * Return trail
      * 
      * @return
      */
     public Trail getTrail () {
         return myTrail;
     }
 
     /**
      * 
      * @return size of Image
      */
     public Dimension getSize () {
         return DEFAULT_SIZE;
     }
 
     /**
      * 
      * @param image Turtle's image
      */
     public void changeTurtleImage (String image) {
         setView(new Pixmap(image));
         myTurtleName = image;
     }
     
     public void undoMove(){
         System.out.println(myTrail.getTrails().size());
     	if (myTrail.getTrails().size()>1){
 	    	Location lastTrail=myTrail.getTrails().get(myTrail.getTrails().size()-1);
 	    	undoneTrails.addTrail(lastTrail);
 	    	myTrail.removeTrail(lastTrail);
 	    	System.out.println(myTrail.getTrails().get(myTrail.getTrails().size()-1));
 	    	setCenter(myTrail.getTrails().get(myTrail.getTrails().size()-1));
     	}
     }
     
     public void redoMove(){
     	if (undoneTrails.getTrails().size()>0){
 	    	Location lastUndoneTrail=undoneTrails.getTrails().get(undoneTrails.getTrails().size()-1);
 	    	myTrail.addTrail(lastUndoneTrail);
 	    	undoneTrails.removeTrail(lastUndoneTrail);
 	    	setCenter(lastUndoneTrail);
     	}
     }
 }
