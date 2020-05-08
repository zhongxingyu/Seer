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
  * An interface that represents the immutable state of some enumerator. The 
  * state of the enumerator (an Enumeratee) is stepped forward by calling the 
  * step method. The step method returns some enumerated output, any issues 
  * encountered during processing and a new Enumeratee that may be advanced 
  * further. An Enumeratee signals completion (or the presence of errors) by way 
  * of the status method. Further calls to the step method after an Enumeratee's
  * status method has indicated completion are a noop.
  * 
  * Note1: Enumeratee implementations should not throw exceptions. Instead,
  * exceptions should be returned as issues in the result and the 
  * status of the next Enumeratee set to FATAL_ERROR (should processing stop) or 
  * RECOVERABLE_ERROR (if processing may continue).
  * 
  * @author Lance
  * @param <A> the output type 
  */
 public interface Enumeratee<A> {
   /**
    * Get the status of the enumerator
    * @return status of the enumerator
    */
   StatusCode status();
   
   /**
    * The immutable result of stepping forward an enumeratee.
    * 
    * @param <A> the output type
    */
   public interface Result<A> {
     /**
      * Get the next enumeratee
      * @return next enumeratee
      */
     Enumeratee<A> next();
     /**
      * Get the output items
      * @return output items
      */
     Traversable<A> output();
     /**
      * Get the issues that occurred during processing this result
      * @return the issues that occurred during processing this result
      */
     Traversable<Issue> issues();
   }
   
   /**
    * Step the enumeratee forward, returning an immutable result object that 
    * contains some output, a list of issues that occurred during processing and 
    * the next enumeratee.
    * @return an immutable result object that contains some output, a list of 
    * issues that occurred during processing and the next enumeratee
    */
   Result<A> step();
 }
