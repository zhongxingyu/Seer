 /**
  * DoubleIdentityFunctionTester.java
  * 
  * Copyright 2009, 2010 Jeffrey Finkelstein
  * 
  * This file is part of jfcommon-functional.
  * 
  * jfcommon-functional is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * jfcommon-functional is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jfcommon-functional. If not, see <http://www.gnu.org/licenses/>.
  */
 package jfcommon.functional;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 /**
  * Test class for the {@link DoubleIdentityFunction} class.
  * 
  * @author Jeffrey Finkelstein
  * @since 0.1
  */
 public class DoubleIdentityFunctionTest {
 
   /** The value by which to increment the input to the function. */
   public static final double INCREMENT = 0.1;
   /** The maximum value of the input to the function. */
   public static final double MAX_VALUE = 100.0;
   /** The minimum value of the input to the function. */
   public static final double MIN_VALUE = 0.0;
   /** Zero. */
   public static final double ZERO_DELTA = 0.0;
 
   /**
    * Test method for
   * {@link jfcommon.functional.DoubleIdentityFunction#newInstance()} .
    */
   @Test
   public void testNewInstance() {
     final IdentityFunction<Double> function = DoubleIdentityFunction
         .newInstance();
 
     for (double x = MIN_VALUE; x < MAX_VALUE; x += INCREMENT) {
       assertEquals(x, function.execute(x), ZERO_DELTA);
     }
   }
 
 }
