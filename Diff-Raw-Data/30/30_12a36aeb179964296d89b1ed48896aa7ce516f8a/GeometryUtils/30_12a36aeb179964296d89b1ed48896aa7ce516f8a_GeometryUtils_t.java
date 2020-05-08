 package sofia.graphics.internal;
 
 import java.util.Collections;
 import java.util.Set;

import sofia.graphics.CopyableGeometry;
 import sofia.graphics.ResolvableGeometry;
 import sofia.graphics.Shape;
import sofia.graphics.SizeF;
import android.graphics.PointF;
import android.graphics.RectF;
 
 // -------------------------------------------------------------------------
 /**
  * TODO Document
  *
  * @author  Tony Allevato
  * @version 2011.11.25
  */
 public class GeometryUtils
 {
     // ----------------------------------------------------------
     private GeometryUtils()
     {
         // Do nothing.
     }
 
 
     // ----------------------------------------------------------
     public static Set<Shape> getShapeDependencies(Object object)
     {
         if (object instanceof ResolvableGeometry)
         {
            return ((ResolvableGeometry<?>) object).getShapeDependencies();
         }
         else
         {
             return Collections.<Shape>emptySet();
         }
     }
 
 
     // ----------------------------------------------------------
     public static boolean isGeometryResolved(Object object)
     {
         if (object instanceof ResolvableGeometry)
         {
            return ((ResolvableGeometry<?>) object).isGeometryResolved();
         }
         else
         {
             return true;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Resolves a geometric object, if possible. This method checks the
      * argument to determine if it implements the {@link ResolvableGeometry} interface,
      * and if so, calls the {@link ResolvableGeometry#resolveGeometry()} method to fill in its
      * fields. Otherwise, it does nothing.
      *
      * @param object the geometric object to resolve
      */
     public static void resolveGeometry(Object object, Shape shape)
     {
         if (object instanceof ResolvableGeometry)
         {
            ((ResolvableGeometry<?>) object).resolveGeometry(shape);
         }
     }
 
 
     // ----------------------------------------------------------
     @SuppressWarnings("unchecked")
     public static <T> T copy(T object)
     {
         if (object instanceof CopyableGeometry)
         {
             return ((CopyableGeometry<T>) object).copy();
         }
         else if (object instanceof PointF)
         {
             PointF point = (PointF) object;
             return (T) new PointF(point.x, point.y);
         }
         else if (object instanceof SizeF)
         {
             SizeF size = (SizeF) object;
             return (T) new SizeF(size.width, size.height);
         }
         else if (object instanceof RectF)
         {
             RectF rect = (RectF) object;
             return (T) new RectF(rect);
         }
         else
         {
             return null;
         }
     }
 }
