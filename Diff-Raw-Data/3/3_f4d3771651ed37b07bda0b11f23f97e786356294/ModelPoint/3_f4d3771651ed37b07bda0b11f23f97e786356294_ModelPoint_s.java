 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.modelstate;
 
 import static cz.cuni.mff.peckam.java.origamist.math.MathHelper.EPSILON;
 
 import javax.vecmath.Point2d;
 import javax.vecmath.Point3d;
 
 /**
  * A point on the model.
  * 
  * @author Martin Pecka
  */
 public class ModelPoint extends Point3d implements Cloneable
 {
     /** */
     private static final long serialVersionUID  = -2994355263756836249L;
 
     /** The point on the paper referenced by this point. */
     protected Point2d         original;
 
     /** The segment that contains this point. */
     protected ModelSegment    containingSegment = null;
 
     public ModelPoint(Point3d point, Point2d original)
     {
         this(point, original, null);
     }
 
     public ModelPoint(Point3d point, Point2d original, ModelSegment containingSegment)
     {
         super(point);
         this.original = original;
         this.containingSegment = containingSegment;
     }
 
     /**
      * @return The point on the paper referenced by this point.
      */
     public Point2d getOriginal()
     {
         return original;
     }
 
     /**
      * @param original The point on the paper referenced by this point.
      */
     public void setOriginal(Point2d original)
     {
         this.original = original;
     }
 
     /**
      * @return The segment that contains this point. This is an optional property.
      */
     public ModelSegment getContainingSegment()
     {
         return containingSegment;
     }
 
     /**
      * Return true if this point is almost equal to the given one.
      * 
      * @param point The point to compare.
      * @return true if this point is almost equal to the given one.
      */
     public boolean epsilonEquals(ModelPoint point)
     {
         return super.epsilonEquals(point, EPSILON) && original.epsilonEquals(point.original, EPSILON);
     }
 
     @Override
     public String toString()
     {
         return "ModelPoint [point=" + super.toString() + ", original=" + original + "]";
     }
 
     @Override
     public int hashCode()
     {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((original == null) ? 0 : original.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         if (this == obj)
             return true;
         if (!super.equals(obj))
             return false;
         if (getClass() != obj.getClass())
             return false;
         ModelPoint other = (ModelPoint) obj;
         if (original == null) {
             if (other.original != null)
                 return false;
         } else if (!original.equals(other.original))
             return false;
         return true;
     }
 
     @Override
     public ModelPoint clone()
     {
        return new ModelPoint((Point3d) super.clone(), (Point2d) original.clone(), containingSegment.clone());
     }
 }
