 package org.eclipse.jst.jsf.common.internal.strategy;
 
 /**
  * A strategy that takes an INPUT, performs a calculation on it, and returns
  * an OUTPUT
  * @author cbateman
  *
  * @param <INPUT>
  * @param <OUTPUT>
  */
 public interface ISimpleStrategy<INPUT, OUTPUT>
 {
     /**
      * Perform the algorithm on input and return OUTPUT.  This type of
      * operation should avoid side-effects, but may throw exceptions.
      * 
      * @param input
      * @return the result of the strategy algorithm
      * @throws Exception implementers should narrow what exceptions are thrown.
      */
     public OUTPUT perform(INPUT input) throws Exception;
     
     /**
      * @return the single value that perform will return if it cannot calculate
      * a meaningful result for an input.  The value must be unique, the same
     * for all inputs and must be testable using the '==' operator.
      */
     public OUTPUT getNoResult();
 }
