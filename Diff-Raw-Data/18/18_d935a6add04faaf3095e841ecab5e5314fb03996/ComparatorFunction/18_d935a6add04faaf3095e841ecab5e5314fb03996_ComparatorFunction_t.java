 package org.datasetdiff;
 
 /**
  * Function which can compare 2 values.
  *
 * @author agustafson
  *
  * @param <T> The type to be compared
  */
 public interface ComparatorFunction<T> {
     /**
      * Compare 2 values and return true if the 2 values can be compared equal.
      *
      * @param left The left value to compare
      * @param right The right value to compare
      * @return true if the 2 values can be compared equal
      */
     boolean compare(T left, T right);
 }
