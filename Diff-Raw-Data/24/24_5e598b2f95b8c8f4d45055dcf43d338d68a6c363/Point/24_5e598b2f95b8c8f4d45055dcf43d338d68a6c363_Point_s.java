 package drawing.lines;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.Ellipse2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import util.Location;
 
 
 /**
  * Represents a 2d line or variable thickness and color.
  * 
  * @author Scott Valentine
  * @author Ryan Fishel
  * @author Ellango Jothimurugesan
  */
 public class Point {
    private Location myStart;
     private Color myColor;
    private double myThickness;
     private Collection<Ellipse2D> myPoints;
 
     /**
      * Creates a new Point based on coordinates, thickness, and a color.
      * 
     * @param start is the coordinate of the point.
      * @param thickness is the thickness of the line.
      * @param color is the color of the line.
      */
    public Point(Location start, double thickness, Color color) {
        myStart = start;
         myColor = color;
        myThickness = thickness;
         
         myPoints = new ArrayList<Ellipse2D>();
         
         
        initializeCircle();
         
     }
 
     /**
      * Initializes the circle shape based on the parameters of this point.
      */
    private void initializeCircle() {
        double x = myStart.getX() - myThickness / 2;
        double y = myStart.getY() - myThickness / 2;
        myPoints.add(new Ellipse2D.Double(x, y, myThickness, myThickness));
     }
 
     /**
      * Paints the line using the given pen.
      * 
      * @param pen is the graphic representation that will represent the line.
      */
     public void paint(Graphics2D pen) {
         pen.setColor(myColor);
         
         for (Ellipse2D point: myPoints) {          
             pen.draw(point);
             pen.fill(point);
         }
     }
 }
