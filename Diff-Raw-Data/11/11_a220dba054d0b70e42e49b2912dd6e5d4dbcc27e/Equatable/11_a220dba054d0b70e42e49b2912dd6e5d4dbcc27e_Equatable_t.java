 package de.skuzzle.polly.tools;
 
 /**
 * <p>Interface which helps to override the {@link Object#equals(Object)} method 
 * according to its specified contract. This interface is based on the article 
  * <a href="http://www.drdobbs.com/jvm/java-qa-how-do-i-correctly-implement-th/184405053">
  * How Do I Correctly Implement the equals() Method?</a> by Tal Cohen (May 01, 2002) and
  * its related <a href="http://tal.forum2.org/equals">discussion page</a>.</p>
  *  
  * <p>Classes the implement this interfaces, can implement the 
  * equals method using {@link EqualsHelper}:</p>
  * 
  * <pre>
  * &#64;Override
  * public boolean equals(Object obj) {
  *     return EqualsHelper.testEquality(this, obj);
  * }
  * </pre>
  * 
  * This is a sample implementation, which would not allow the implementing class to be 
  * equal to an instance of any of its superclasses:
  * 
  * <pre>
  * public class Point implements Equatable {
  *     private final x;
  *     private final y;
  *     
  *     // ...
  *     
  *     &#64;Override
  *     public final boolean equals(Object o) {
  *         return EqualsHelper.testEquality(this, obj);
  *     }
  *     
  *     &#64;Override
  *     public Class<?> getEquivalenceClass() {
  *         return Point.class;
  *     }
  *     
  *     &#64;Override
  *     public boolean actualEquals(Equatable o) {
  *         final Point other = (Point) o;
  *         return this.x == other.x && this.y == other.y;
  *     }
  * }
  * </pre>
  * 
  * <p>Now consider the following implementation of a subclass which extends the Point to
  * carry a new field named 'color'. In this case, instances of ColorPoint can only
  * be equal to other instances of ColorPoint (as by the return value of 
  * {@link #getEquivalenceClass()}):</p>
  * 
  * <pre>
  * public class ColorPoint extends Point {
  *
  *     private final Color color;
  *     
  *     // ...
  *
  *     &#64;Override
  *     public Class<?> getEquivalenceClass() {
  *         return ColorPoint.class;
  *     }
  *     
  *     &#64;Override
  *     public boolean actualEquals(Equatable o) {
  *         final ColorPoint other = (ColorPoint) o;
  *         return super.actualEquals(o) && this.color.equals(other.color);
  *     }
  * }
  * </pre>
  * 
  * If instances of ColorPoint shall also be considered equal to a instances of Point, 
  * disregarding the 'color' attribute, no further work would be required. Implementation 
  * of ColorPoint would just look like the following:
  * 
  * <pre>
  * public class ColorPoint extends Point {
  *
  *     private final Color color;
  *     
  *     // ...
  * }
  * </pre>
  * 
  * @author Simon Taddiken
  * @see EqualsHelper
  */
 public interface Equatable {
     
     /**
      * <p>This method must return a class which is assignment compatible with this one. 
      * That is, it must be either the class of the implementing class itself or any class 
      * which is on a higher level in this class' type hierarchy.</p>
      * 
      * <p>In short: this method must return a class to which the implementing class can
      * be casted.</p>
      * 
      * <p>When testing for equality using 
      * {@link EqualsHelper#testEquality(Equatable, Object)}, two objects o1 and o2 can 
      * only be considered equal, if both implement {@link Equatable} and
      * <code>o1.getEquivalenceClass().equals(o2.getEquivalenceClass())</code>
      * </p>
      * 
      * @return The most specific super class (or this class itself) to which the 
      *          implementing class be equal.
      * @see EqualsHelper
      */
     public Class<?> getEquivalenceClass();
     
     
     
     /**
      * <p>Performs actual test for equality. You may cast the passed object to the type 
      * returned by {@link #getEquivalenceClass()} or any of that types super types.</p>
      * 
      * <p>After casting, you may perform equality checks for the attributes of your 
      * class.</p>
      * @param o The object to compare this one with. Note that this is guaranteed to be 
      *          <code> != null</code> when called by 
      *          {@link EqualsHelper#testEquality(Equatable, Object)}
      * @return Whether both objects are equal.
      * @see EqualsHelper
      */
     public boolean actualEquals(Equatable o);
 }
