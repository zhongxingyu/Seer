 package sofia.graphics;
 
import java.util.Collections;
import java.util.Set;
 import android.graphics.PointF;
 import android.graphics.RectF;
 
 // -------------------------------------------------------------------------
 /**
  * <p>
  * An {@code Anchor} represents a point inside or on a bounding rectangle that
  * is used to position a shape relative to another shape.
  * </p><p>
  * Pre-existing anchors are provided for the nine common anchor points that
  * most people will want to use: {@link #TOP_LEFT}, {@link #TOP},
  * {@link #TOP_RIGHT}; {@link #LEFT}, {@link #CENTER}, {@link #RIGHT};
  * and {@link #BOTTOM_LEFT}, {@link #BOTTOM}, {@link #BOTTOM_RIGHT}.
  * </p><p>
  * Since {@code Anchor} is an abstract class, users can easily implement their
  * own custom anchors for different types of shapes. For example, a circle
  * might provide anchors that lie on the curve itself rather than on its
  * bounding box.
  * </p>
  *
  * @author  Tony Allevato
  * @version 2011.10.28
  */
 public abstract class Anchor
 {
     //~ Constants .............................................................
 
     // ----------------------------------------------------------
     /**
      * Represents the top-left point of a bounding rectangle.
      */
     public static final Anchor TOP_LEFT = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.left, bounds.top);
         }
 
         @Override
         public String toString()
         {
             return "TOP_LEFT";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the top-center point of a bounding rectangle.
      */
     public static final Anchor TOP = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.centerX(), bounds.top);
         }
 
         @Override
         public String toString()
         {
             return "TOP";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the top-right point of a bounding rectangle.
      */
     public static final Anchor TOP_RIGHT = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.right, bounds.top);
         }
 
         @Override
         public String toString()
         {
             return "TOP_RIGHT";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the middle-left point of a bounding rectangle.
      */
     public static final Anchor LEFT = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.left, bounds.centerY());
         }
 
         @Override
         public String toString()
         {
             return "LEFT";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the middle-center point of a bounding rectangle.
      */
     public static final Anchor CENTER = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.centerX(), bounds.centerY());
         }
 
         @Override
         public String toString()
         {
             return "CENTER";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the middle-right point of a bounding rectangle.
      */
     public static final Anchor RIGHT = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.right, bounds.centerY());
         }
 
         @Override
         public String toString()
         {
             return "RIGHT";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the bottom-left point of a bounding rectangle.
      */
     public static final Anchor BOTTOM_LEFT = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.left, bounds.bottom);
         }
 
         @Override
         public String toString()
         {
             return "BOTTOM_LEFT";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the bottom-center point of a bounding rectangle.
      */
     public static final Anchor BOTTOM = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.centerX(), bounds.bottom);
         }
 
         @Override
         public String toString()
         {
             return "BOTTOM";
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Represents the bottom-right point of a bounding rectangle.
      */
     public static final Anchor BOTTOM_RIGHT = new Anchor()
     {
         @Override
         public PointF getPoint(RectF bounds)
         {
             return new PointF(bounds.right, bounds.bottom);
         }
 
         @Override
         public String toString()
         {
             return "BOTTOM_RIGHT";
         }
     };
 
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     /**
      * Gets the point in the specified rectangle that the receiving anchor
      * represents.
      *
      * @param bounds the bounding rectangle
      * @return a PointF object that represents the location of the anchor
      */
     public abstract PointF getPoint(RectF bounds);
 
 
     // ----------------------------------------------------------
     /**
      * Gets a {@link PointAndAnchor} object that represents the notion of
      * positioning a shape such that the receiving anchor point on that shape
      * is located at the specified point.
      *
      * @param point the point
      * @return a {@link PointAndAnchor} object
      */
     public final PointAndAnchor anchoredAt(PointF point)
     {
         return new PointAndAnchor(point, this);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a {@link PointAndAnchor} object that represents the notion of
      * positioning a shape such that the receiving anchor point on that shape
      * is located at the specified point.
      *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return a {@link PointAndAnchor} object
      */
     public final PointAndAnchor anchoredAt(float x, float y)
     {
         return anchoredAt(new PointF(x, y));
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a point that represents the location of the receiver on the
      * specified shape.
      *
     * @param shape the shape
     * @return a {@link PointD} object that represents the location of the
     *     receiver on the shape
      */
     public final RelativePoint of(Shape shape)
     {
         return new RelativePoint(this, shape);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a point that represents the location of the receiver on the
      * specified canvas.
      *
     * @param canvas the canvas
     * @return a {@link PointD} object that represents the location of the
     *     receiver on the shape canvas
      */
     public final RelativePoint ofView()
     {
         return new RelativePoint(this, null);
     }
 }
