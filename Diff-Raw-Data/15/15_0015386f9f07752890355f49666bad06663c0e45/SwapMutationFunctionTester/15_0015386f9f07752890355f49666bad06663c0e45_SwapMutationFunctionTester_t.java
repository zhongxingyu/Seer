 /**
  * SwapMutationFunctionTester.java
  * 
  * Copyright 2009 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.example.ga.tsp.mutation;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 import jmona.MutationException;
 import jmona.test.Util;
 
import org.apache.log4j.Logger;
 import org.junit.Test;
 
 /**
  * Test class for the SwapMutationFunction class.
  * 
  * @author jfinkels
  */
 public class SwapMutationFunctionTester extends
     AbstractTSPMutationFunctionTester {
 
   /** Instantiate this test class with access to a SwapMutationFunction. */
   public SwapMutationFunctionTester() {
     super(new SwapMutationFunction());
   }
 
   /**
    * Test method for
    * {@link jmona.example.ga.tsp.mutation.SwapMutationFunction#mutate(jmona.example.ga.tsp.Tour)}
    * .
    */
   @Override
   @Test
   public void testMutate() {
     for (int j = 0; j < NUM_TESTS; ++j) {
 
      this.setUp();
      
       try {
         this.function().mutate(this.tour());
       } catch (final MutationException exception) {
         Util.fail(exception);
       }
 
      assertEquals(LENGTH, this.tour().size());
      for (int i = 0; i < LENGTH; ++i) {
        assertTrue(this.tour().contains(i));
      }

       int changed = 0;
       for (int i = 0; i < this.tour().size(); ++i) {
         if (this.tour().get(i) != i) {
           changed = i;
           break;
         }
       }
 
       final int swappedCity = this.tour().get(changed);
 
      assertEquals(changed, this.tour().get(swappedCity).intValue());
     }
 
   }
 }
