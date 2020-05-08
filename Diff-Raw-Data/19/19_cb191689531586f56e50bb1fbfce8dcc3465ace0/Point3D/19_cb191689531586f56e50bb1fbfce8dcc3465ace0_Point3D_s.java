 /*
  * GLMotes is licensed under the FreeBSD (also known as the "Simplified BSD
  * License", which is the BSD license that omits the final "no-endoresment"
  * clause. The original BSD License can be found at
  * http://www.opensource.org/licenses/bsd-license.php with the
  * modified/simplified version found at
  * http://www.freebsd.org/copyright/freebsd-license.html
  *
  * Copyright (c) 2010, Elijah C. Menifee
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are
  * those of the authors and should not be interpreted as representing official
  * policies, either expressed or implied, of Elijah C. Menifee
  */
 
 package org.tonoplace.glmotes.geometry;
 
 import java.io.Serializable;
 import org.tonoplace.glmotes.utility.FloatingPointComparison;
 
 /**
  * The Point3D class defines a point representing a location in (x, y, z)
  * coordinate space.  This class uses double precision for storing the
  * coordinates.  This class is immutable, and therefore thread safe, without
  * requiring any special synchronization or locking.
  *
  * @author Elijah C. Menifee
  * @date Nov 6, 2010 4:10:21 PM
  */
 public class Point3D implements Serializable
 {
   /**
    * Value used in equals operation to determine if a point is the same allowing
    * for the fuzziness that can result in precision errors in floating point
    * math.
    * @see FloatingPointComparison.AlmostEqual2sComplement(double a, double b, long maxULPS)
    */
   public static final long EPSILON_ULPS = 128;
   
   /**
    * The X coordinate of this Point3D.
    */
   private double x_;
 
   /**
    * The Y coordinate of this Point3D.
    */
   private double y_;
 
   /**
    * The Z coordinate of this Point3D.
    */
   private double z_;
 
   /**
    * Constructs and initializes a Point3D with coordinates(0,0,0).
    */
   Point3D()
   {
     //this(0, 0, 0);
   }
 
   /**
    * Constructs and initializes a Point3D with the specified coordinates.
    * @param x the X coordinate of the newly constructed Point3D
    * @param y the Y coordinate of the newly constructed Point3D
    * @param z the Z coordinate of the newly constructed Point3D
    */
   Point3D(double x, double y, double z)
   {
     //x_ = x;
     //y_ = y;
     //z_ = z;
   }
   
   /**
    * Constructs and initializes a Point3D using the double values found in the
    * array starting at position 0 for x, position 1 for y, position 2 for z. 
    * @param doubles an array of doubles to use for the coordinates
    * @throws NullPointerException if doubles is NULL
    * @throws IllegalArgumentException if the length of the array is less than 3.
    * @see Point3D(double[] doubles, int offset)
    */
   Point3D(double[] doubles)
     throws NullPointerException, IllegalArgumentException
   {
     this(doubles, 0);
   }
 
   /**
    * Constructs and initializes a Point3D using the double values found in the
    * array.  Starts at offset for x, offset+1 for y, and offset+2 for z.
    * @param doubles an array of doubles to use for the coordinates.
    * @param offset the position in the array to start with for x value
    * @throws NullPointerException if doubles is NULL
    * @throws IllegalArgumentException if the length of the array is less
    * than offset + 3.
    */
   Point3D(double[] doubles, int offset)
     throws NullPointerException, IllegalArgumentException
   {
 
   }
   /**
    * Returns the X coordinate of this Point3D in double precision.
    * @return the X coordinate of this Point3D
    */
   public double getX()
   {
     return 0d;
   }
 
   /**
    * Returns the Y coordinate of this Point3D in double precision.
    * @return the Y coordinate of this Point3D
    */
   public double getY()
   {
     return 0d;
   }
 
   /**
    * Returns the Z coordinate of this Point3D in double precision.
    * @return the Z coordinate of this Point3D
    */
   public double getZ()
   {
     return 0d;
   }
 
   /**
    * Returns a String that represents the value of this Point3D.
    * @return a string representation of this Point3D
    */
   @Override
   public String toString()
   {
     return "";
   }
 
   /**
    * Returns a hash code for this Point3D.  Because the equals method fudges the
    * comparison so that extremely close Point3Ds are considered equal, a hash
    * algorithm that would always produce the same hashCode for any two Points
    * that are considered equal has not been devised, therefore the hashCode for
    * all values are 0 to ensure the hashCode is compliant with the equals method
    * @return a hashCode of 0.
    */
   @Override
   public int hashCode()
   {
     // becuase we allow an almost equal double calculation for equals, only safe
     // way to make sure the hash codes are equal for objects that are equal is
     // to always return the same hashCode, Point3Ds will not be efficiant as
     // keys in a int hash based algorithm.
     return 0;
   }
 
   /**
    * Compares this object against the specified object.  This method will only
    * report true if obj is not null and is a Point3d.  If obj is a valid Point3D
    * than each of its coordinates are compared using a nearly equal algorithm so
    * that two points that are extremely close are considered equal.  This is to
    * take into account the problem of precision of floating point calculations
    * where a change in the order of operations can affect the comparison of
    * float values. Due to the nature of this nearly equal comparison given 3
    * points A, B, and C: The method may report A==B and B==C but A!=C.
    *
    * @see FloatingPointComparison.AlmostEqual2sComplement(double a, double b, long maxULPS)
    * @param obj the object to compare with.
    * @return true if the points represent the same point, within the accepted
    * amount of floating point error.
    */
   @Override
   public boolean equals(Object obj)
   {
     /**
     if (obj==null)
       return false;
     if (obj instanceof Point3D)
     {
       Point3D b = (Point3D) obj;
       if (trulyEquals(b))
         return true;
       // might be more accurate to see how close the distance between the points
       // is to zero, and use that to determine if the points are close enough
       // to be considered as the same point, however distance requires sqrt calc
       // which can be slow, so just do simpler faster bit based comparision to
       // determine if all individual coordinates are close to one another.
       long doublePrecisionULPS = 128;
       boolean sameX = FloatingPointComparison.AlmostEqual2sComplement(x_, b.x_, doublePrecisionULPS);
       if (!sameX)
         return false;
       boolean sameY = FloatingPointComparison.AlmostEqual2sComplement(y_, b.y_, doublePrecisionULPS);
       if (!sameY)
         return false;
       boolean sameZ = FloatingPointComparison.AlmostEqual2sComplement(z_, b.z_, doublePrecisionULPS);
       return sameZ;
     }
     return false;
      **/
     return false;
   }
 
   /**
    * Compares this object against the specified object for exact value.  Using
    * this to compare can lead to points not being reported as equal that would
    * be considered equal if not for floating point epsilon errors.  This method
    * is the equivalent call of equals that most objects use to define equals.
    * @param otherPoint the object to compare with.
    * @return true if otherPoint is not null and the X,Y, and Z coordinates are
    * exactly the same.
    */
   public boolean trulyEquals(Point3D otherPoint)
   {
 //    if (otherPoint==null)
 //      return false;
 //    return x_==otherPoint.x_ && y_==otherPoint.y_ && z_==otherPoint.z_;
     return false;
   }
 
   /**
    * Calculates the distance between this Point3D and the specified Point3D.
    * For 3 points A, B, and C: This method may report A.distance(B)==0
    * B.distance(C)==0 but A.distance(C)!=0.  This is due to fuzzy equals used
    * in comparing doubles.
    *
    * @param otherPoint the Point3D to determine the distance from.
    * @return The distance between the two points.
    * @throws NullPointerException if otherPoint is null.
   * @see equals(Object o)
    */
   public double distance(Point3D otherPoint) throws NullPointerException
   {
     return 0.0d;
   }
 
   /**
    * Calculates the square of the distance between this Point3D and the
   * specified Point3D.
    * @param otherPoint the Point3D to determine the square of the distance from.
    * @return The square of the distance between the two points.
    * @throws NullPointerException if otherPoint is null.
    */
   public double distanceSq(Point3D otherPoint) throws NullPointerException
   {
     return 0.0d;
   }
 
 }
