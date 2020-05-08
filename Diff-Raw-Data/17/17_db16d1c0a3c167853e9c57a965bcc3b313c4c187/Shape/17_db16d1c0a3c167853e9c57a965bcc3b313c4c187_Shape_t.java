 package sofia.graphics;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import sofia.graphics.internal.GeometryUtils;
 import sofia.graphics.internal.animation.AlphaTransformer;
 import sofia.graphics.internal.animation.AnimationState;
 import sofia.graphics.internal.animation.BoundsTransformer;
 import sofia.graphics.internal.animation.ColorTransformer;
 import sofia.graphics.internal.animation.MotionStepTransformer;
 import sofia.graphics.internal.animation.PositionTransformer;
 import sofia.graphics.internal.animation.RotationTransformer;
 import sofia.graphics.internal.animation.XTransformer;
 import sofia.graphics.internal.animation.YTransformer;
 import sofia.internal.events.EventDispatcher;
 import sofia.internal.events.OptionalEventDispatcher;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.view.animation.Interpolator;
 
 // -------------------------------------------------------------------------
 /**
  * The base class for all types of shapes that can be drawn on a
  * {@link ShapeView}. This class maintains all of the properties that are
  * common to every type of shape, such as its bounds (position and size on the
  * canvas), visibility, color (though some subclasses define multiple kinds of
  * colors), and rotation. Animation support is also provided through this
  * class.
  *
  * @author  Tony Allevato
  * @author  Last changed by $Author: edwards $
  * @version $Date: 2012/08/04 16:32 $
  */
 public abstract class Shape
 {
     //~ Fields ................................................................
 
     private RectF bounds;
     private int zIndex;
     private ShapeParent parent;
     private boolean visible;
     private Color color;
     private float rotation;
     private PointF rotationPivot;
     private PointF positionAnchor;
     private Matrix transform;
     private Matrix inverseTransform;
     private float[][] rotatedCorners;
 
     // Collision checker hook, for future location-based query extensions
     @SuppressWarnings("unused")
     private sofia.graphics.collision.CollisionChecker collisionChecker;
     private sofia.graphics.collision.ShapeNode node;
 
 
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Creates a new shape with default bounds (located at the center of the
      * screen and 100 pixels wide by 100 pixels tall).
      */
     public Shape()
     {
         this(Anchor.CENTER.anchoredAt(Anchor.CENTER.ofView()).sized(100, 100));
     }
 
 
     // ----------------------------------------------------------
     /**
      * Creates a new shape with the specified bounds.
      *
      * @param bounds The bounds of the shape.
      */
     public Shape(RectF bounds)
     {
         this.zIndex = 0;
         this.bounds = GeometryUtils.copy(bounds);
         this.visible = true;
         this.color = Color.white;
         this.positionAnchor = new PointF(0, 0);
     }
 
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     /**
      * Add another shape to the same view (or composite shape) containing this
      * shape. This method is a convenience shortcut for
      * {@code getShapeParent().add(newShape)}, but with the added benefit that
      * it does nothing if the receiving shape is not added to a parent (and
      * thus the parent is null).
      *
      * @param newShape The other shape to add.
      */
     public void addOther(Shape newShape)
     {
         ShapeParent parent = getShapeParent();
 
         if (parent != null)
         {
             parent.add(newShape);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Remove this shape from its view (or parent).
      */
     public void remove()
     {
         ShapeParent parent = getShapeParent();
 
         if (parent != null)
         {
             parent.remove(this);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the bounding rectangle of the shape. The top-left corner of the
      * bounding rectangle is the shape's origin, and the bottom-right corner
      * is the shape's extent.
      *
      * @return The bounding rectangle of the shape.
      */
     public RectF getBounds()
     {
         return bounds;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the bounding rectangle of the shape. The bounding rectangle passed
      * to this method is copied, so changes to it after this method is called
      * will not be reflected by the shape.
      *
      * @param newBounds The new bounding rectangle of the shape.
      */
     public void setBounds(RectF newBounds)
     {
         this.bounds = GeometryUtils.copy(newBounds);
 
         if (getShapeParent() != null)
         {
             GeometryUtils.resolveGeometry(this.bounds, this);
         }
 
         notifyParentOfPositionChange();
         conditionallyRepaint();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Get the current position anchor, which is an offset relative to the
      * upper left corner of the shape that is used as the shape's "origin"
      * for the purposes of getting/setting x-y positions.  The default
      * anchor is the top-left corner (0, 0) unless it has been explicitly set.
      *
      * @return The current position anchor.
      */
     public PointF getPositionAnchor()
     {
         return positionAnchor;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Set the position anchor, which is an offset relative to the
      * upper left corner of the shape that is used as the shape's "origin"
      * for the purposes of getting/setting x-y positions.  This does not
      * change the shape's current position, but will change the behavior
      * of future calls to setX()/setY()/setPosition() and
      * getX()/getY()/getPosition().
      *
      * @param anchor The new position anchor.
      */
     public void setPositionAnchor(PointF anchor)
     {
         positionAnchor = new PointF(anchor.x, anchor.y);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Set the position anchor, which is an offset relative to the
      * upper left corner of the shape that is used as the shape's "origin"
      * for the purposes of getting/setting x-y positions.  This does not
      * change the shape's current position, but will change the behavior
      * of future calls to setX()/setY()/setPosition() and
      * getX()/getY()/getPosition().
      *
      * @param anchor The new position anchor.
      */
     public void setPositionAnchor(Anchor anchor)
     {
         positionAnchor = anchor.getPoint(getBounds());
         positionAnchor.offset(-getBounds().left, -getBounds().top);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the current angle of rotation of the shape, in degrees clockwise.
      *
      * @return The current angle of rotation of the shape.
      */
     public float getRotation()
     {
         return rotation;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the point around which the shape's rotation will pivot. By
      * default, the center of the shape's bounding box is used.
      *
      * @return The point around which the shape's rotation will pivot.
      */
     public PointF getRotationPivot()
     {
         return rotationPivot;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the angle of rotation of the shape in degrees clockwise, using
      * the center of the shape's bounding box as the pivot point.
      *
      * @param newRotation The new angle of rotation of the shape.
      */
     public void setRotation(float newRotation)
     {
         setRotation(newRotation, Anchor.CENTER.of(this));
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the angle of rotation of the shape in degrees clockwise, using
      * the specified point as the pivot point.
      *
      * @param newRotation The new angle of rotation of the shape.
      * @param newPivot    The point around which the rotation will pivot.
      */
     public void setRotation(float newRotation, PointF newPivot)
     {
         this.rotation = newRotation;
         this.rotationPivot = newPivot;
 
         updateTransform();
         notifyParentOfPositionChange();
         conditionallyRepaint();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Increments the shape's rotation by the specified number of degrees,
      * around the same pivot point that was used previously (or the center
      * of the shape if no other pivot has been previously used).
      *
      * @param angleDelta The number of degrees to add to the shape's rotation.
      */
     public void rotateBy(float angleDelta)
     {
         this.rotation += angleDelta;
 
         updateTransform();
         notifyParentOfPositionChange();
         conditionallyRepaint();
     }
 
 
     // ----------------------------------------------------------
     private void updateTransform()
     {
         if (rotation == 0)
         {
             transform = null;
         }
         else
         {
             transform = new Matrix();
 
             PointF pivot;
 
             if (rotationPivot == null)
             {
                 pivot = Anchor.CENTER.of(this);
             }
             else
             {
                 pivot = rotationPivot;
             }
 
             GeometryUtils.resolveGeometry(pivot, this);
             // This places the pivot in absolute coords, but we want
             // to make it relative to the bounding box.
             pivot.offset(-getBounds().left, -getBounds().top);
 
             // The rotation transform is to offset the shape from the
             // pivot so that the pivot becomes the origin, then rotate,
             // then move the shape back to its original location.
             // Note that the pivot is now in bounding-box-relative coords.
             transform.postTranslate(-pivot.x, -pivot.y);
             transform.postRotate(rotation);
             transform.postTranslate(pivot.x, pivot.y);
         }
         inverseTransform = null;
         rotatedCorners = null;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the current linear transformation that will be applied to the
      * shape when it is drawn. Currently, the matrix only contains rotation
      * information.
      *
      * @return The current linear transformation that is applied to the shape.
      */
     public Matrix getTransform()
     {
         return transform;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the x-coordinate of the anchor point of the shape's bounding
      * box (the top-left corner, by default).
      *
      * @return The x-coordinate of the anchor point of the shape's
      *         bounding box.
      * @see #setPositionAnchor(Anchor)
      */
     public float getX()
     {
         return getBounds().left + positionAnchor.x;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the x-coordinate of the anchor point of the shape's bounding
      * box (the top-left corner, by default). This moves the shape, so
      * calling this method also causes the extent of the shape to change,
      * keeping with width the same.
      *
      * @param x The x-coordinate of the anchor point of the shape's
      *          bounding box.
      * @see #setPositionAnchor(Anchor)
      */
     public void setX(float x)
     {
         getBounds().offsetTo(x - positionAnchor.x, getBounds().top);
         notifyParentOfPositionChange();
         conditionallyRelayout();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the y-coordinate of the anchor point of the shape's bounding
      * box (the top-left corner, by default).
      *
      * @return The y-coordinate of the anchor point of the shape's
      *         bounding box.
      * @see #setPositionAnchor(Anchor)
      */
     public float getY()
     {
         return getBounds().top + positionAnchor.y;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the y-coordinate of the anchor point of the shape's bounding
      * box (the top-left corner, by default). This moves the shape, so
      * calling this method also causes the extent of the shape to change,
      * keeping with height the same.
      *
      * @param y The y-coordinate of the anchor point of the shape's
      *          bounding box.
      * @see #setPositionAnchor(Anchor)
      */
     public void setY(float y)
     {
         getBounds().offsetTo(getBounds().left, y - positionAnchor.y);
         notifyParentOfPositionChange();
         conditionallyRelayout();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the width of the shape, in pixels.
      *
      * @return The width of the shape.
      */
     public float getWidth()
     {
         return getBounds().width();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the height of the shape, in pixels.
      *
      * @return The height of the shape.
      */
     public float getHeight()
     {
         return getBounds().height();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the origin (top-left corner) of the receiver. Be aware that the
      * {@link PointF#x} and {@link PointF#y} fields of the returned point may
      * not be valid if layout of the shapes has not yet occurred.
      *
      * @return A {@link PointF} object describing the origin of the shape.
      */
     public PointF getPosition()
     {
         return new PointF(
             bounds.left + positionAnchor.x,
             bounds.top + positionAnchor.y);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the origin (top-left corner) of the receiver.
      *
      * @param x the x-coordinate of the desired origin of the shape
      * @param y the y-coordinate of the desired origin of the shape
      */
     public void setPosition(float x, float y)
     {
         bounds.offsetTo(
             x - positionAnchor.x,
             y - positionAnchor.y);
         notifyParentOfPositionChange();
         conditionallyRelayout();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the origin (top-left corner) of the receiver.
      *
      * @param position A {@link PointF} object describing the origin of the
      *                 shape.
      */
     public void setPosition(PointF position)
     {
         setPosition(position.x, position.y);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the position of the receiver based on the specified point and
      * anchor, leaving its size unchanged.
      *
      * @param pointAndAnchor A {@link PointAndAnchor} object describing the
      *                       position of the shape.
      */
     public void setPosition(PointAndAnchor pointAndAnchor)
     {
         bounds = pointAndAnchor.sized(bounds.width(), bounds.height());
         bounds.offset(-positionAnchor.x, -positionAnchor.y);
         notifyParentOfPositionChange();
         conditionallyRelayout();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Moves the receiver by the specified horizontal and vertical distance.
      * Positive values move the shape to the right or down, and negative
      * values move it to the left or up.
      *
      * @param dx The number of pixels to move the shape horizontally.
      * @param dy The number of pixels to move the shape vertically.
      */
     public void move(float dx, float dy)
     {
         bounds.offset(dx, dy);
         notifyParentOfPositionChange();
         conditionallyRelayout();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Transforms a point on the screen into the original bounds of a shape,
      * pre-rotation. This method is mainly meant to be used by subclasses
      * that need to provide their own {@link #contains(float, float)}
      * implementation, so that those methods return the correct values when
      * a rotation is applied to the shape.
      *
      * @param x The x-coordinate in the view.
      * @param y The y-coordinate in the view.
      * @return A two-element float array that contains the x- and
      *     y-coordinates that the inputs would map to before the rotation
      *     was applied.
      */
     protected float[] inverseTransformPoint(float x, float y)
     {
         float[] point = { x, y };
         Matrix xform = getTransform();
 
         if (xform != null)
         {
             if (inverseTransform == null)
             {
                 inverseTransform = new Matrix();
                 xform.invert(inverseTransform);
             }
             inverseTransform.mapPoints(point);
         }
 
         return point;
     }
 
 
     // ----------------------------------------------------------
     /**
      * <p>
      * Gets a value indicating whether the specified pixel location is
      * contained in the receiver.
      * </p><p>
      * By default, this method checks to see whether the point is located
      * within the bounding rectangle of the shape. Shapes where this would
      * produce incorrect results, such as ovals or lines, override this method
      * accordingly.
      * </p><p>
      * This method <strong>does</strong> take the shape's rotation into
      * account. This means that if you subclass {@code Shape} and need to
      * provide logic that is different from the default bounding box behavior,
      * then you may need to undo the rotation of the incoming point before
      * testing it against your shape's bounds. The
      * {@link #inverseTransformPoint(float, float)} method has been provided
      * to simplify this.
      * </p>
      *
      * @param x The x-coordinate.
      * @param y The y-coordinate.
      * @return True if the shape contains the point, otherwise false.
      */
     public boolean contains(float x, float y)
     {
         float[] point = inverseTransformPoint(x, y);
         return getBounds().contains(point[0], point[1]);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a value indicating whether the specified pixel location is
      * contained in the receiver.
      *
      * @param point The point.
      * @return True if the shape contains the point, otherwise false.
      */
     public boolean contains(PointF point)
     {
         return contains(point.x, point.y);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Called when the bounds of the shape have been resolved. Subclasses may
      * want to override this if they need to update their state when this
      * occurs.
      */
     public void onBoundsResolved()
     {
         updateTransform();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the z-index of the receiver. A shape with a higher z-index will be
      * drawn on top of a shape with a lower z-index. Among shapes that have the
      * same z-index, shapes added later will be drawn above shapes added
      * earlier.
      *
      * By default, shapes are created with a z-index of 0.
      *
      * @return The z-index of the shape.
      */
     public int getZIndex()
     {
         return zIndex;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the z-index of the receiver.
      *
      * @param newZIndex The new z-index of the shape.
      */
     public void setZIndex(int newZIndex)
     {
         zIndex = newZIndex;
 
         if (parent != null)
         {
             parent.onZIndexChanged(this);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Returns true if this shape is drawn in front of (later than) the
      * other shape.
      * @param other The shape to check against.
      * @return True if this shape is drawn in front of (later than) the other.
      */
     public boolean isInFrontOf(Shape other)
     {
         return parent != null && parent.isInFrontOf(this, other);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the parent of the receiver.
      *
      * @return The parent of the receiver.
      */
     public final ShapeParent getShapeParent()
     {
         return parent;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the parent of the receiver.
      *
      * @return The parent of the receiver.
      */
     public final ShapeView getParentView()
     {
         ShapeParent ancestor = getShapeParent();
 
         while (ancestor != null)
         {
             if (ancestor instanceof ShapeView)
             {
                 return (ShapeView) ancestor;
             }
 
             ancestor = ancestor.getShapeParent();
         }
 
         return null;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the parent of the receiver. Used internally.
      *
      * @param newParent The new parent.
      */
     final void setParent(ShapeParent newParent)
     {
         this.parent = newParent;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the color of the receiver.
      *
      * @return The color of the receiver.
      */
     public Color getColor()
     {
         return color;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the color of the receiver.
      *
      * @param newColor The new color of the receiver.
      */
     public void setColor(Color newColor)
     {
         this.color = newColor;
         conditionallyRepaint();
     }
 
 
     // ----------------------------------------------------------
     /**
      * A convenience method that gets the alpha (opacity) component of the
      * shape's color.
      *
      * @return The alpha component of the shape's color, where 0 means that
      *         the color is fully transparent and 255 means that it is fully
      *         opaque.
      */
     public int getAlpha()
     {
         return getColor().alpha();
     }
 
 
     // ----------------------------------------------------------
     /**
      * A convenience method that sets the alpha (opacity) component of the
      * shape's color without changing the other color components.
      *
      * @param newAlpha The new alpha component of the shape's color, where 0
      *                 means that the color is fully transparent and 255
      *                 means that it is fully opaque.
      */
     public void setAlpha(int newAlpha)
     {
         setColor(getColor().withAlpha(newAlpha));
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a value indicating whether the receiver is visible (drawn on the
      * screen). Invisible shapes also do not receive touch events.
      *
      * @return True if the shape is visible, otherwise false.
      */
     public boolean isVisible()
     {
         return visible;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets a value indicating whether the receiver is visible (drawn on the
      * screen).
      *
      * @param newVisible True if the shape should be visible, otherwise false.
      */
     public void setVisible(boolean newVisible)
     {
         this.visible = newVisible;
         conditionallyRepaint();
     }
 
 
     // ----------------------------------------------------------
     /**
      * <p>
      * Gets an <em>animator</em> object that lets the user animate properties
      * of the receiving shape.
      * </p><p>
      * For ease of use, the description of the animation desired can be
      * chained directly to the result of this method. For example, the
      * following code fragment would create an animation that runs for 2
      * seconds, gradually changing the shape's color to red, its position
      * to the top-right corner of the view, and then starts the animation
      * after a delay of 1 second after the method is called:
      * <pre>
      *     shape.animate(2000)
      *          .delay(1000)
      *          .color(Color.red)
      *          .position(Anchor.TOP_RIGHT.ofView())
      *          .play();</pre>
      * </p>
      *
      * @param duration The length of the animation in milliseconds.
      * @return A {@link ShapeAnimator} that lets the user animate properties
      *         of the receiving shape.
      */
     @SuppressWarnings("rawtypes")
     public Animator<?> animate(long duration)
     {
         return new Animator(duration);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Stops the current animation for this shape, if there is one.
      */
     public void stopAnimation()
     {
         ShapeView view = getParentView();
 
         if (view != null)
         {
             view.getAnimationManager().stop(this);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Subclasses must implement this method to define how the shape is to be
      * drawn on the canvas. Users should never call this method directly; it is
      * called as part of the repaint cycle by the {@link ShapeView} that
      * contains the shape.
      *
      * @param canvas The {@link Canvas} on which to draw the shape.
      */
     public abstract void draw(Canvas canvas);
 
 
     // ----------------------------------------------------------
     /**
      * Gets the {@code Paint} object that describes how this shape should be
      * drawn. By default, the {@code Paint}'s style is set to {@code STROKE}
      * and the color to the value returned by {@link #getColor()}. Subclasses
      * can override this method to add their own attributes; they should call
      * the superclass implementation and then add their own styles to the
      * returned object.
      *
      * @return A Paint object describing how the shape should be drawn.
      */
     protected Paint getPaint()
     {
         Paint paint = new Paint();
         paint.setStyle(Paint.Style.STROKE);
         paint.setColor(getColor().toRawColor());
         return paint;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Called to indicate that the shape needs to be repainted on the screen.
      * Most users will not need to call this method, because modifying a
      * property of the shape such as its color will repaint it automatically.
      */
     protected void conditionallyRepaint()
     {
         ShapeView view = getParentView();
 
         if (view != null)
         {
             view.conditionallyRepaint();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Called to recalculate the layout of the shape on the screen. Most users
      * will not need to call this method, because modifying a property of the
      * shape such as its position and size will cause the layout to be
      * recalculated automatically.
      */
     protected void conditionallyRelayout()
     {
         ShapeView view = getParentView();
 
         if (view != null)
         {
             view.conditionallyRelayout();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Called to recalculate the layout of the shape on the screen. Most users
      * will not need to call this method, because modifying a property of the
      * shape such as its position and size will cause the layout to be
      * recalculated automatically.
      */
     protected void notifyParentOfPositionChange()
     {
         rotatedCorners = null;
         ShapeView view = getParentView();
 
         if (view != null)
         {
             view.onPositionChanged(this);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Returns a human-readable string representation of the shape.
      *
      * @return A human-readable string representation of the shape.
      */
     @Override
     public String toString()
     {
         return getClass().getSimpleName() + ": "
             + "bounds=" + bounds + ", isVisible=" + visible
             + ", color=" + color;
     }
 
 
     // ----------------------------------------------------------
     private void updateRotatedCorners()
     {
         if (rotatedCorners == null)
         {
             RectF bounds = getBounds();
             rotatedCorners = new float[][] {
                 { bounds.left,  bounds.top    },
                 { bounds.right, bounds.top    },
                 { bounds.left,  bounds.bottom },
                 { bounds.right, bounds.bottom }
             };
 
             Matrix xform = getTransform();
             if (xform != null)
             {
                 for (float[] pt : rotatedCorners)
                 {
                     xform.mapPoints(pt);
                 }
             }
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Check whether all of the vertexes in the "other" rotated rectangle
      * are on the outside of any one of the edges in "my" rotated rectangle.
      *
      * @param myBox    The rotated corners of "my" bounding box.
      * @param otherBox The rotated corners of the "other" bounding box.
      *
      * @return True if all corners of the "other" rectangle are on the
      *         outside of any of  the edges of "my" rectangle.
      */
     private static boolean checkOutside(float [][] myBox, float[][] otherBox)
     {
         vloop:
         for (int v = 0; v < 4; v++)
         {
             int v1 = (v + 1) & 3; // wrap at 4 back to 0
             float edgeX = myBox[v][0] - myBox[v1][0];
             float edgeY = myBox[v][1] - myBox[v1][1];
             float reX = -edgeY;
             float reY = edgeX;
 
             if (reX == 0.0 && reY == 0.0)
             {
                 continue vloop;
             }
 
             for (int e = 0; e < 4; e++)
             {
                 float scalar = reX * (otherBox[e][0] - myBox[v1][0])
                     + reY * (otherBox[e][1] - myBox[v1][1]);
                 if (scalar < 0)
                 {
                     continue vloop;
                 }
             }
 
             // If we got here, we have an edge with all vertexes from the
             // other rect on the outside:
             return true;
         }
 
         return false;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Determine whether this shape intersects another, based on their
      * bounding boxes.
      *
      * @param otherShape The other shape to check against.
      * @return True if this shape and the other shape intersect.
      */
     public boolean intersects(Shape otherShape)
     {
         if (rotation == 0.0 && otherShape.rotation == 0.0)
         {
             return RectF.intersects(getBounds(), otherShape.getBounds());
         }
         else
         {
             updateRotatedCorners();
             otherShape.updateRotatedCorners();
             if (checkOutside(rotatedCorners, otherShape.rotatedCorners))
             {
                 return false;
             }
             if (checkOutside(otherShape.rotatedCorners, rotatedCorners))
             {
                 return false;
             }
             return true;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Determine whether any part of this shape extends outside the given
      * rectangle.
      *
      * @param bounds The rectangle to check against.
      * @return A ViewEdges object indicating whether any part of this shape
      * extends outside the top, bottom, left, or right side of the bounds.
      */
     public ViewEdges extendsOutside(RectF bounds)
     {
         boolean left   = false;
         boolean top    = false;
         boolean right  = false;
         boolean bottom = false;
 
         updateRotatedCorners();
         for (int i = 0; i < 4; i++)
         {
             left   = left   || (rotatedCorners[i][0] <  bounds.left  );
             top    = top    || (rotatedCorners[i][1] <  bounds.top   );
             right  = right  || (rotatedCorners[i][0] >= bounds.right );
             bottom = bottom || (rotatedCorners[i][1] >= bounds.bottom);
         }
 
         return new ViewEdges(left, top, right, bottom);
     }
 
 
     // ----------------------------------------------------------
     /* package */ sofia.graphics.collision.ShapeNode getShapeNode()
     {
         return node;
     }
 
 
     // ----------------------------------------------------------
     /* package */ void setShapeNode(sofia.graphics.collision.ShapeNode node)
     {
         this.node = node;
     }
 
 
     // ----------------------------------------------------------
     /* package */ void setCollisionChecker(
         sofia.graphics.collision.CollisionChecker collisionChecker)
     {
         this.collisionChecker = collisionChecker;
     }
 
 
     //~ Animation support classes .............................................
 
     // -------------------------------------------------------------------------
     /**
      * Provides animation support for shapes. Most uses of this class will not
      * need to reference it directly; for example, an animation can be
      * constructed and played by chaining method calls directly:
      *
      * <pre>
      *     shape.animate(500).color(Color.blue).alpha(128).play();</pre>
      *
      * In situations where the type of the class must be referenced directly
      * (for example, when one is passed to an event handler like
      * {@code onAnimationDone}), referring to the name of that type can be
      * somewhat awkward due to the use of some Java generics tricks to ensure
      * that the methods chain properly. In nearly all cases, it is reasonable
      * to use a "?" wildcard in place of the generic parameter:
      *
      * <pre>
      *     Shape.Animator&lt;?&gt; anim = shape.animate(500).color(Color.blue);
      *     anim.play();</pre>
      *
      * @param <AnimatorType> the concrete type of the animator
      *
      * @author  Tony Allevato
      * @version 2011.12.11
      */
     public class Animator<AnimatorType extends Animator<AnimatorType>>
     {
         //~ Fields ............................................................
 
         private long duration;
         private Interpolator interpolator;
         private long startTime;
         private long delay;
         private String name;
         private RepeatMode repeatMode;
         private boolean removeWhenComplete;
         private AnimationState state;
         private long lastTime;
 
         private OptionalEventDispatcher animationStarted;
         private OptionalEventDispatcher animationEnded;
         private OptionalEventDispatcher animationRepeated;
 
         private Set<PropertyTransformer> transformers;
 
 
         //~ Constructors ......................................................
 
         // ----------------------------------------------------------
         /**
          * Creates a new animator for the specified shape. Users cannot call
          * call this constructor directly; instead, they need to use the
          * {@link Shape#animate(long)} method to get an animator object.
          *
          * @param shape the shape to animate
          * @param duration the length of one pass of the animation, in
          *     milliseconds
          */
         protected Animator(long duration)
         {
             this.duration = duration;
             this.delay = 0;
             this.interpolator = Timings.easeInOut();
             this.repeatMode = RepeatMode.NONE;
             this.removeWhenComplete = false;
             this.state = AnimationState.WAITING;
 
             transformers = new HashSet<PropertyTransformer>();
         }
 
 
         //~ Methods ...........................................................
 
         // ----------------------------------------------------------
         /**
          * Gets the shape that the receiver is animating.
          *
          * @return the shape that the receiver is animating
          */
         public Shape getShape()
         {
             return Shape.this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Gets the delay, in milliseconds, that this animation will wait (or
          * did wait) before starting.
          *
          * @return the delay, in milliseconds, that this animation will (or
          *     did) wait before starting
          */
         public long getDelay()
         {
             return delay;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Gets the duration of this animation in milliseconds.
          *
          * @return the duration of this animation in milliseconds
          */
         public long getDuration()
         {
             return duration;
         }
 
 
         // ----------------------------------------------------------
         /**
          * <p>
          * Sets the name of this animation. You should set the name of an
          * animation if you wish to be notified about events related to that
          * animation. This name is used to determine the name of the handler
          * method to call on the shape or the controller when animation begins,
          * repeats, or ends.
          * </p><p>
          * For example, if the animation has the name "bounce", then the
          * following notifications will be sent to the shape and the
          * controller, if those methods exist:
          * </p>
          * <ul>
          * <li>{@code bounceAnimationStarted(Shape.Animator<?>)}</li>
          * <li>{@code bounceAnimationStarted()}</li>
          * <li>{@code bounceAnimationRepeated(Shape.Animator<?>)}</li>
          * <li>{@code bounceAnimationStarted()}</li>
          * <li>{@code bounceAnimationEnded(Shape.Animator<?>)}</li>
          * <li>{@code bounceAnimationEnded()}</li>
          * </ul>
          * <p>
          * Therefore, the name of an animation should be a valid Java
          * identifier, preferably starting with a lowercase letter.
          * </p><p>
          * The name of the animation <strong>must be set</strong> in order to
          * receive notifications about it.
          * </p>
          *
          * @param newName the name of the animation
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType name(String newName)
         {
             this.name = newName;
 
             animationStarted =
                     new OptionalEventDispatcher(name + "AnimationStarted");
             animationRepeated =
                     new OptionalEventDispatcher(name + "AnimationRepeated");
             animationEnded =
                     new OptionalEventDispatcher(name + "AnimationEnded");
 
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the timing function (interpolator) that determines how the
          * animation behaves during execution. A number of pre-written timing
          * functions can be found as static methods in the {@link Timings} class.
          *
          * @param newInterpolator the timing function (interpolator) that
          *     determines how the animation behaves during execution
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType timing(Interpolator newInterpolator)
         {
             this.interpolator = newInterpolator;
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the delay, in milliseconds, that the animation will wait after the
          * {@link #play()} method is called until it actually starts.
          *
          * @param newDelay the delay, in milliseconds, before the animation starts
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType delay(long newDelay)
         {
             this.delay = newDelay;
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final position of the shape when the animation ends.
          *
          * @param x the final x-coordinate of the shape when the animation ends
          * @param y the final y-coordinate of the shape when the animation ends
          * @return this animator, for method chaining
          */
         public AnimatorType position(float x, float y)
         {
             return position(new PointF(x, y));
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final position of the shape when the animation ends.
          *
          * @param point the final position of the shape when the animation ends
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType position(PointF point)
         {
             addTransformer(new PositionTransformer(getShape(), point));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final position of the shape when the animation ends as a
          * relative shift from the shape's position when the animation starts.
          *
          * @param dx the horizontal amount to have shifted the shape when the
          *     animation ends
          * @param dy the vertical amount to have shifted the shape when the
          *     animation ends
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType moveBy(float dx, float dy)
         {
             addTransformer(new MotionStepTransformer(getShape(),
                     MotionStep.constantVelocity(dx, dy)));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         @SuppressWarnings("unchecked")
         public AnimatorType moveBy(float dx, float dy, float ax, float ay)
         {
             addTransformer(new MotionStepTransformer(getShape(),
                     MotionStep.constantAcceleration(dx, dy, ax, ay)));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         @SuppressWarnings("unchecked")
         public AnimatorType moveBy(MotionStep motionStep)
         {
             addTransformer(new MotionStepTransformer(getShape(), motionStep));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final x-coordinate of the shape when the animation ends.
          *
          * @param y the final x-coordinate of the shape when the animation ends
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType x(float x)
         {
             addTransformer(new XTransformer(getShape(), x));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final y-coordinate of the shape when the animation ends.
          *
          * @param y the final y-coordinate of the shape when the animation ends
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType y(float y)
         {
             addTransformer(new YTransformer(getShape(), y));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final bounds of the shape when the animation ends.
          *
          * @param bounds the final bounds of the shape when the animation ends
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType bounds(RectF bounds)
         {
             addTransformer(new BoundsTransformer(getShape(), bounds));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final color of the shape when the animation ends.
          *
          * @param color the final color of the shape when the animation ends
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType color(Color color)
         {
             addTransformer(new ColorTransformer(getShape(), color));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the final alpha (opacity) of the shape when the animation ends.
          *
          * @param alpha the final alpha (opacity) of the shape when the animation
          *     ends, from 0 (fully transparent) to 255 (fully opaque)
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType alpha(int alpha)
         {
             addTransformer(new AlphaTransformer(getShape(), alpha));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * <p>
          * Sets the final rotation, in degrees clockwise, of the shape when the
          * animation ends. Negative values will create a counter-clockwise
          * rotation.
          * </p><p>
          * A shape can be made to rotate completely multiple times by
          * providing values higher than 360 to this method. For example,
          * passing 360 would cause the shape to make one full rotation over the
          * duration of the animation, passing 720 would cause it to make two
          * full rotations, and so forth.
          * </p>
          *
          * @param rotation the final rotation, in degrees clockwise (negative
          *     values will rotate counter-clockwise)
          * @return this animator, for method chaining
          */
         @SuppressWarnings("unchecked")
         public AnimatorType rotation(float rotation)
         {
             addTransformer(new RotationTransformer(getShape(), rotation));
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Causes the animation to repeat until stopped. This method is
          * provided as shorthand, equivalent to
          * {@code repeatMode(RepeatMode.REPEAT)}.
          *
          * @return this animator, for chaining method calls
          */
         public AnimatorType repeat()
         {
             return repeatMode(RepeatMode.REPEAT);
         }
 
 
         // ----------------------------------------------------------
         /**
          * Causes the animation to oscillate (from start to end and back to
          * start) until stopped. This method is provided as shorthand,
          * equivalent to {@code repeatMode(RepeatMode.OSCILLATE)}.
          *
          * @return this animator, for chaining method calls
          */
         public AnimatorType oscillate()
         {
             return repeatMode(RepeatMode.OSCILLATE);
         }
 
 
         // ----------------------------------------------------------
         /**
          * Sets the repeat mode for this animation. See the {@link RepeatMode}
          * enumeration for possible values.
          *
          * @param mode the repeat mode for the animation
          * @return this animator, for chaining method calls
          */
         @SuppressWarnings("unchecked")
         public AnimatorType repeatMode(RepeatMode mode)
         {
             repeatMode = mode;
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * <p>
          * Causes the shape to be automatically removed from its view when the
          * animation completes. This is useful for animations that cause a shape to
          * fade out, where you want it to disappear for good when done.
          * </p><p>
          * Note that the shape will only be removed if the animation ends on its
          * own when its time expires; it will not be removed if you end the
          * animation prematurely by calling {@link Shape#stopAnimation()}. This
          * also means that this method will have no effect if the animation is
          * repeating or oscillating.
          * </p>
          *
          * @return this animator, for chaining method calls
          */
         @SuppressWarnings("unchecked")
         public AnimatorType removeWhenComplete()
         {
             removeWhenComplete = true;
             return (AnimatorType) this;
         }
 
 
         // ----------------------------------------------------------
         /**
          * Adds a property transformer to the list of those that will be applied
          * each time the animation advances.
          *
          * @param transformer the property transformer
          */
         protected void addTransformer(PropertyTransformer transformer)
         {
             transformers.add(transformer);
         }
 
 
         // ----------------------------------------------------------
         /**
          * Starts the animation.
          */
         public void play()
         {
             for (PropertyTransformer transformer : transformers)
             {
                 transformer.onStart();
             }
 
             startTime = System.currentTimeMillis() + delay;
             getShape().getParentView().getAnimationManager().enqueue(this);
         }
 
 
         // ----------------------------------------------------------
         /**
          * Gets a value indicating whether the animation is currently playing,
          * either forward or backward.
          *
          * @return true if the animation is playing, otherwise false
          */
         public boolean isPlaying()
         {
             return (state == AnimationState.FORWARD
                     || state == AnimationState.BACKWARD);
         }
 
 
         // ----------------------------------------------------------
         /**
          * Gets a value indicating whether the animation is playing forward.
          *
          * @return true if the animation is playing forward, otherwise false
          */
         public boolean isForward()
         {
             return (state == AnimationState.FORWARD);
         }
 
 
         // ----------------------------------------------------------
         /**
          * Gets a value indicating whether the animation is playing backward.
          *
          * @return true if the animation is playing backward, otherwise false
          */
         public boolean isBackward()
         {
             return (state == AnimationState.BACKWARD);
         }
 
 
         // ----------------------------------------------------------
         /**
          * Stops the animation.
          */
         public void stop()
         {
             state = AnimationState.STOPPED;
         }
 
 
         // ----------------------------------------------------------
         /**
          * This method is intended for internal use.
          */
         public boolean advanceTo(long time)
         {
             if (time < startTime)
             {
                 return false;
             }
             else if (state == AnimationState.STOPPED)
             {
                 return true;
             }
             else if (state == AnimationState.WAITING)
             {
                 state = AnimationState.FORWARD;
                 dispatchAnimationEvent(animationStarted);
             }
 
             float t = 0;
             long scaledTime = time;
             boolean ended = false;
 
             switch (repeatMode)
             {
                 case NONE:
                     ended = (time >= startTime + duration);
                     t = ended ? 1.0f :
                         (float) ((double) (time - startTime) / duration);
                     break;
 
                 case REPEAT:
                     state = AnimationState.FORWARD;
                     scaledTime = (time - startTime) % duration;
                     t = (float) ((double) scaledTime / duration);
 
                     if (scaledTime < lastTime)
                     {
                         dispatchAnimationEvent(animationRepeated);
                     }
 
                     break;
 
                 case OSCILLATE:
                     scaledTime = (time - startTime) % (2 * duration);
 
                     if (scaledTime < duration)
                     {
                         t = (float) ((double) scaledTime / duration);
                     }
                     else
                     {
                         t = 1 - (float) ((double) (
                             scaledTime - duration) / duration);
                     }
 
                     if (state == AnimationState.FORWARD
                             && scaledTime > duration)
                     {
                         state = AnimationState.BACKWARD;
                         dispatchAnimationEvent(animationRepeated);
                     }
                     else if (state == AnimationState.BACKWARD
                             && scaledTime < duration)
                     {
                         state = AnimationState.FORWARD;
                         dispatchAnimationEvent(animationRepeated);
                     }
 
                     break;
             }
 
             float y = interpolator.getInterpolation(t);
 
             for (PropertyTransformer transformer : transformers)
             {
                 transformer.transform(y);
             }
 
             if (ended)
             {
                 if (removeWhenComplete)
                 {
                     getShape().remove();
                 }
 
                 dispatchAnimationEvent(animationEnded);
             }
 
             lastTime = scaledTime;
 
             return ended;
         }
 
 
         // ----------------------------------------------------------
         private void dispatchAnimationEvent(final EventDispatcher event)
         {
             if (event == null)
             {
                 return;
             }
 
             final ShapeView view = getShape().getParentView();
             if (view != null)
             {
                 view.post(new Runnable() {
                     public void run()
                     {
                        @SuppressWarnings("unused")
                        boolean result =
                                   event.dispatch(getShape(), Animator.this)
                                || event.dispatch(view, Animator.this)
                                || event.dispatch(
                                        view.getContext(), Animator.this);
                     }
                 });
             }
         }
     }
 }
