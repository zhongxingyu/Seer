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
 
import scala.collection.Traversable;
 
 /**
  * An interface that represents the immutable state of a translator that can
  * translate a buffer of input items into a buffer of output items. The apply
  * method can be used to translate a buffer of input items into an immutable 
  * result object that contains a buffer of output items, a list of issues that
  * occurred during processing and the next Translatee.
  * 
  * Note1: Translatee implementations should not throw exceptions. Instead,
  * exceptions should be returned as issues in the result and the 
  * status of the next Translatee set to FATAL_ERROR (should processing stop) or 
  * RECOVERABLE_ERROR (if processing may continue).
  * 
  * @author Lance
  * @param <A> the input type
  * @param <B> the output type
  */
 public interface Translatee<A,B> {
   StatusCode status();
   
   public interface Result<A,B> {
     Translatee<A,B> next();
     Traversable<B> output();
     Traversable<Issue> issues();
   }
   
   Result<A,B> apply(Traversable<A> input);
 }
