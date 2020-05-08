 package org.spash.intersect;
 
 import org.spash.ROVector2f;
 import org.spash.Shape;
 import org.spash.ray.Ray;
 
 /**
  * Intersects rays and shapes. 
  */
 public interface RayShapeIntersector {
     /**
      * Find intersection point between a ray and shape.
      * 
      * @param ray
      * @param shape
      * @return Intersection point, if any, otherwise null.
      */
  ROVector2f intersect(Ray ray, Shape shape);
 }
