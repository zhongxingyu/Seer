 package sofia.graphics;
 
 import static java.lang.Float.isNaN;
 
 import sofia.app.ShapeScreen;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.graphics.RectF;
 
 //-------------------------------------------------------------------------
 /**
  * <p>
  * Allows the user to modify the coordinate system of a {@link ShapeView} (or
  * a {@link ShapeScreen}).
  * </p><p>
  * You cannot create instances of this class. Instead, you should call
  * {@link ShapeScreen#getCoordinateSystem()} or
  * {@link ShapeView#getCoordinateSystem()} to retrieve the current coordinate
  * system and then chain method calls to it to apply modifications to the
  * system. For example,
  * </p>
  * <pre>
  *     getCoordinateSystem().origin(Anchor.BOTTOM_LEFT).flipY().width(400);</pre>
  * <p>
  * will set the origin (0, 0) at the bottom-left corner of the view, flip the
  * y-axis so that it grows upward in the positive direction, and fixes the
  * width of the view to be 400 units, scaling it to fit the actual pixel size
  * of the view.
  * </p>
  *
  * @author  Tony Allevato
  * @version 2012.09.16
  */
 public class CoordinateSystem
 {
     //~ Fields ................................................................
 
     private ShapeView owner;
     private Anchor origin;
     private boolean flipX;
     private boolean flipY;
     private float width;
     private float height;
     private Matrix matrix;
 
 
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Creates a new {@code CoordinateSystem} for the specified
      * {@code ShapeView}.
      *
      * @param owner the {@code ShapeView} that owns this coordinate system
      */
     protected CoordinateSystem(ShapeView owner)
     {
         this.owner = owner;
         reset();
     }
 
 
     //~ Public methods ........................................................
 
     // ----------------------------------------------------------
     /**
      * Resets the coordinate system so that the origin is in the top-left
      * corner of the view, the x-axis goes right in the positive direction,
      * the y-axis goes down in the positive direction, and one unit is equal
      * to one pixel.
      */
     public void reset()
     {
         origin = Anchor.TOP_LEFT;
         width = height = Float.NaN;
         flipX = false;
         flipY = false;
 
         updateTransform();
         owner.repaint();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the location on the view where the origin (0, 0) is located. Note
      * that if you set the origin to be the right and/or bottom edges of the
      * view, you should make sure to call {@link #flipX()} and/or
      * {@link #flipY()} to ensure that the coordinate system has the correct
      * orientation.
      *
      * @param anchor the {@link Anchor} representing the origin of the
      *     coordinate system with respect to the view
      *
      * @return this coordinate system, for chaining method calls
      */
     public CoordinateSystem origin(Anchor anchor)
     {
         origin = anchor;
 
        updateTransform();
        owner.repaint();

         return this;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Flips the x-axis of the coordinate system so that it higher values on
      * the x-axis are to the left of lower values.
      *
      * @return this coordinate system, for chaining method calls
      */
     public CoordinateSystem flipX()
     {
         flipX = true;
 
        updateTransform();
        owner.repaint();

         return this;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a value indicating whether the x-axis of this coordinate system is
      * flipped (that is, higher values of x are to the left lower values).
      *
      * @return true if the coordinate system's x-axis is flipped, otherwise
      *     false
      */
     public boolean isFlippedX()
     {
         return flipX;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Flips the y-axis of the coordinate system so that it higher values on
      * the y-axis are above lower values.
      *
      * @return this coordinate system, for chaining method calls
      */
     public CoordinateSystem flipY()
     {
         flipY = true;
 
        updateTransform();
        owner.repaint();

         return this;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a value indicating whether the y-axis of this coordinate system is
      * flipped (that is, higher values of y are above lower values).
      *
      * @return true if the coordinate system's y-axis is flipped, otherwise
      *     false
      */
     public boolean isFlippedY()
     {
         return flipY;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the number of horizontal units that the view should occupy. The
      * view will be scaled so that it always occupies this many units; that is,
      * if the width of the coordinate system is 400 units and the view is 800
      * pixels wide, each unit will be 2 pixels wide. Likewise, if the view is
      * 200 pixels wide, each unit will be 0.5 pixels wide.
      *
      * @param units the number of horizontal units that the view should occupy
      *
      * @return this coordinate system, for chaining method calls
      */
     public CoordinateSystem width(float units)
     {
         width = units;
 
         updateTransform();
         owner.repaint();
 
         return this;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the number of vertical units that the view should occupy. The view
      * will be scaled so that it always occupies this many units; that is, if
      * the height of the coordinate system is 400 units and the view is 800
      * pixels tall, each unit will be 2 pixels tall. Likewise, if the view is
      * 200 pixels tall, each unit will be 0.5 pixels tall.
      *
      * @param units the number of vertical units that the view should occupy
      *
      * @return this coordinate system, for chaining method calls
      */
     public CoordinateSystem height(float units)
     {
         height = units;
 
         updateTransform();
         owner.repaint();
 
         return this;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Transforms a point from device coordinates (pixels on the view/screen)
      * to local coordinates.
      *
      * @param x the x-coordinate
      * @param y the y-coordinate
      * @return the local coordinates in this coordinate system corresponding to
      *     the specified device coordinates
      */
     public PointF deviceToLocal(float x, float y)
     {
         if (matrix != null)
         {
             float[] ptArray = { x, y };
 
             Matrix inverse = new Matrix();
             matrix.invert(inverse);
             inverse.mapPoints(ptArray);
 
             return new PointF(ptArray[0], ptArray[1]);
         }
         else
         {
             return new PointF(x, y);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Transforms a point from device coordinates (pixels on the view/screen)
      * to local coordinates.
      *
      * @param pt the device coordinates to transform
      * @return the local coordinates in this coordinate system corresponding to
      *     the specified device coordinates
      */
     public PointF deviceToLocal(PointF pt)
     {
         return deviceToLocal(pt.x, pt.y);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Called internally to update the {@code AffineTransform} that will be
      * used to transform this view.
      */
     protected void updateTransform()
     {
         if (origin == Anchor.TOP_LEFT && isNaN(width) && isNaN(height))
         {
             matrix = null;
         }
         else
         {
             matrix = new Matrix();
 
             int viewWidth = owner.getWidth();
             int viewHeight = owner.getHeight();
 
             float xScale = 1;
             float yScale = 1;
 
             if (!isNaN(width) && !isNaN(height))
             {
                 xScale = viewWidth / width;
                 yScale = viewHeight / height;
             }
            else if (!isNaN(height))
             {
                 xScale = viewHeight / height;
                 yScale = viewHeight / height;
             }
            else if (!isNaN(width))
             {
                 xScale = viewWidth / width;
                 yScale = viewWidth / width;
             }
 
             RectF bounds = new RectF(0, 0, viewWidth, viewHeight);
             PointF originPt = origin.getPoint(bounds);
 
             if (flipX)
             {
                 xScale *= -1;
             }
 
             if (flipY)
             {
                 yScale *= -1;
             }
 
             matrix.preScale(xScale, yScale);
             matrix.preTranslate(
                     originPt.x / xScale, originPt.y / yScale);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Applies the transformation represented by this coordinate system to the
      * specified {@code Graphics2D} object.
      *
      * @param g the {@code Graphics2D} object that the transformation should be
      *     applied to
      */
     protected void applyTransform(Canvas canvas)
     {
         if (matrix != null)
         {
             canvas.concat(matrix);
         }
     }
 }
