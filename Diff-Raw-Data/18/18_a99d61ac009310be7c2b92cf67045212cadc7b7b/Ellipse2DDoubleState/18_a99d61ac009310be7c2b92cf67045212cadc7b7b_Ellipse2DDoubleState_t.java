 package ecologylab.xml.library.geom;
 
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import ecologylab.xml.xml_inherit;
 
 /**
  * Encapsulates a Ellipse2D.Double for use in translating to/from XML.
  * 
  * ***WARNING!!!***
  * 
  * Performing transformations (such as setFrame()) on the result of shape() will cause this object
  * to become out of synch with its underlying Ellipse2D.Double object. While this will have no bad
  * ramifications for this, it MAY have ramifications for what you INTENDED to do. Essentially,
  * changes to the returned Shape will NOT be reflected in the XML produced by this.
  * 
  * If other transformation methods are required, either notify me, or implement them yourself. :D
  * 
  * Accessor methods (such as contains()) on the result of getEllipse() are fine.
  * 
  * @author Zachary O. Toups (toupsz@cs.tamu.edu)
  */
 public @xml_inherit class Ellipse2DDoubleState extends RectangularShape
 {
     protected Ellipse2D.Double shape = null;
 
     public Ellipse2DDoubleState()
     {
         super();
     }
     
     public Ellipse2DDoubleState(double x, double y, double width, double height)
     {
         super(x, y, width, height);
     }
 
     /**
      * Returns an Ellipse2D object represented by this.
      */
    @Override public Ellipse2D.Double shape()
     {
         if (shape == null)
         {
             shape = new Ellipse2D.Double(x, y, w, h);
         }
         else if (shape.x != x || shape.y != y || shape.height != h || shape.width != w)
         {
             shape.setFrame(x, y, w, h);
         }
 
         return shape;
     }
 
     public boolean contains(Point2D p)
     {
         return shape().contains(p);
     }
 
     public boolean contains(Rectangle2D r)
     {
         return shape().contains(r);
     }
 
     public boolean contains(double x, double y)
     {
         return shape().contains(x, y);
     }
 
     public boolean contains(double x, double y, double w, double h)
     {
         return shape().contains(x, y, w, h);
     }
 
     public Rectangle getBounds()
     {
         return shape().getBounds();
     }
 
     public Rectangle2D getBounds2D()
     {
         return shape().getBounds2D();
     }
 
     public PathIterator getPathIterator(AffineTransform at)
     {
         return shape().getPathIterator(at);
     }
 
     public PathIterator getPathIterator(AffineTransform at, double flatness)
     {
         return shape().getPathIterator(at, flatness);
     }
 
     public boolean intersects(Rectangle2D r)
     {
         return shape().intersects(r);
     }
 
     public boolean intersects(double x, double y, double w, double h)
     {
         return shape().intersects(x, y, w, h);
     }
 }
