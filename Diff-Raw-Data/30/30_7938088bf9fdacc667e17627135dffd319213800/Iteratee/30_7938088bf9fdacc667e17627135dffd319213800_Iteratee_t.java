 /*
     Copyright 2012 Georgia Tech Research Institute
 
     Author: lance.gatlin@gtri.gatech.edu
 
     This file is part of org.gtri.util.iteratee library.
 
     org.gtri.util.iteratee library is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     org.gtri.util.iteratee library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with org.gtri.util.iteratee library. If not, see <http://www.gnu.org/licenses/>.
 
 */
 
 package org.gtri.util.iteratee.api;
 
import scala.collection.immutable.Traversable;
 
 /**
  * An interface that represents the immutable state of a loop. An Iteratee is 
  * advanced to the next state by calling the apply method with a buffer of 
  * input. The apply method returns an immutable result object that contains a 
  * list of issues that occurred during processing, any input that was not used 
  * by the Iteratee and the next immutable Iteratee. The Iteratee's state method 
  * may be called to retrieve the state of the loop after each application of 
  * input. An Iteratee signals completion (or the presence of errors) by way of 
  * the status method. Further calls to the apply method after an Iteratee's 
  * status method has indicated completion are a noop and will result in all 
  * applied input being returned as overflow in the result. Additionally, once 
  * all input has been applied to an Iteratee, the endOfInput method should be 
  * called to signal to the Iteratee to move to a completion status (either 
  * SUCCESS or FATAL_ERROR).
  * 
  * Note1: Iteratee implementations should not throw exceptions. Instead,
  * exceptions should be returned as issues in the result and the 
  * status of the next Iteratee set to FATAL_ERROR (should processing stop) or 
  * RECOVERABLE_ERROR (if processing may continue).
  * 
  * @author Lance
  */
 public interface Iteratee<A,S> {
   /**
    * Get the status of the iteratee
    * @return status of the iteratee
    */
   StatusCode status();
 
   /**
    * Get the immutable state of the iteratee
    * @return the immutable state of the iteratee
    */
   S state();
   
   /**
    * The immutable result of applying input to an iteratee
    * 
    * @param <A> the output type
    * @param <S> the state type
    */
   public interface Result<A,S> {
     /**
      * Get the next iteratee
      * @return the next iteratee
      */
     Iteratee<A,S> next();
     /**
      * Get the issues that occurred during processing this result
      * @return the issues that occurred during processing this result
      */
     Traversable<Issue> issues();
     /**
      * Get any remaining input that was not used. Only occurs if iteratee
      * completes on part of the applied input.
      * @return any remaining input that was not used
      */
     Traversable<A> overflow();
   }
   
   /**
    * Apply input to the iteratee and return an immutable result object that 
    * contains a list of issues that occurred during processing, 
    * any input that was not used and the next immutable iteratee
    * @param input a buffer of input items
    * @return an immutable result object that contains a list of issues that 
    * occurred during processing, any input that was not used and the next 
    * immutable iteratee
    */
   Result<A,S> apply(Traversable<A> input);
   /**
    * Signal to the iteratee that no further input will occur. Implementations
    * should ensure that the status of the next Iteratee is SUCCESS or 
    * FATAL_ERROR.
    * @return an immutable result object that contains the next immutable 
    * iteratee and a list of issues that occurred during processing and any 
    * cached input from previous applications that was not used
    */
   Result<A,S> endOfInput();
 }
