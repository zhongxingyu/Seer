 /**
  * InsertionMutationFunctionTester.java
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
 import jmona.MutationException;
 import jmona.test.Util;
 
 import org.junit.Test;
 
 /**
  * Test class for the InsertionMutationFunction class.
  * 
  * @author jfinkels
  */
 public class InsertionMutationFunctionTester extends
     AbstractTSPMutationFunctionTester {
 
   /** Instantiate this test class with access to an InsertionMutationFunction. */
   public InsertionMutationFunctionTester() {
     super(new InsertionMutationFunction());
   }
 
   /**
    * Test method for
    * {@link jmona.example.ga.tsp.mutation.InsertionMutationFunction#mutate(jmona.example.ga.tsp.Tour)}
    * .
    */
   @Test
   public void testMutate() {
     for (int j = 0; j < NUM_TESTS; ++j) {
 
       try {
         this.function().mutate(this.tour());
       } catch (final MutationException exception) {
         Util.fail(exception);
       }
 
      assertEquals(AbstractTSPMutationFunctionTester.LENGTH, this.tour().size());
 
       int firstChange = 0;
       for (int i = 0; i < this.tour().size(); ++i) {
         if (!this.tour().get(i).equals(i)) {
           firstChange = i;
           break;
         }
       }
 
       int lastChange = 0;
       for (int i = this.tour().size() - 1; i >= 0; --i) {
         if (!this.tour().get(i).equals(i)) {
           lastChange = i;
           break;
         }
       }
 
       // if a city was moved to a location previous to its original location
       // for example:
       //
       // before: 0 1 2 3 4 5 6 7 8 9 10 11 12
       // after : 0 1 2 3 4 10 5 6 7 8 9 11 12
       if (this.tour().get(lastChange) == lastChange - 1) {
         assertEquals(lastChange, this.tour().get(firstChange).intValue());
 
         for (int i = firstChange + 1; i <= lastChange; ++i) {
          assertEquals(i + 1, this.tour().get(i).intValue());
         }
 
        for (int i = lastChange + 1; i > lastChange || i < firstChange; i = (i + 1)
            % this.tour().size()) {
           assertEquals(i, this.tour().get(i).intValue());
         }
       }
 
       // if a city was moved to an location after its original location
       // for example:
       //
       // before: 0 1 2 3 4 5 6 7 8 9 10 11 12
       // after : 0 1 2 3 4 6 7 8 9 10 5 11 12
       if (this.tour().get(firstChange) == firstChange + 1) {
         assertEquals(firstChange, this.tour().get(lastChange).intValue());
 
         for (int i = firstChange; i < lastChange; ++i) {
           assertEquals(i + 1, this.tour().get(i).intValue());
         }
 
         for (int i = lastChange + 1; i > lastChange || i < firstChange; i = (i + 1)
             % this.tour().size()) {
           assertEquals(i, this.tour().get(i).intValue());
         }
       }
     }
   }
}
