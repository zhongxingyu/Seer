 /**
  * InsertionMutationFunctionTester.java
  * 
  * Copyright 2009, 2010 Jeffrey Finkelstein
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
 package jmona.impl.mutation;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import jfcommon.functional.Range;
 import jfcommon.test.TestUtils;
 import jmona.MutationException;
 import jmona.impl.mutable.MutableInteger;
 import jmona.impl.mutable.functional.MutableRange;
 
 import org.junit.Test;
 
 /**
  * Test class for the InsertionMutationFunction class.
  * 
  * @author Jeffrey Finkelstein
  * @since 0.4
  */
 public class InsertionMutationFunctionTester extends
     AbstractListMutationFunctionTester {
 
   /** Instantiate this test class with access to an InsertionMutationFunction. */
   public InsertionMutationFunctionTester() {
    super(new InsertionMutationFunction<MutableInteger, List<MutableInteger>>());
   }
 
   /**
    * Test method for
    * {@link jmona.impl.mutation.InsertionMutationFunction#mutate(List)}.
    */
   @Test
   @Override
   public void testMutate() {
     for (int j = 0; j < NUM_TESTS; ++j) {
      
       this.setUp();
 
       try {
         this.function().mutate(this.list());
       } catch (final MutationException exception) {
         TestUtils.fail(exception);
       }
 
       assertEquals(LENGTH, this.list().size());
       for (final MutableInteger i : new MutableRange(LENGTH)) {
         assertTrue(this.list().contains(i));
       }
 
       int firstChange = 0;
       for (final int i : new Range(this.list().size())) {
         if (this.list().get(i).intValue() != i) {
           firstChange = i;
           break;
         }
       }
 
       int lastChange = 0;
       for (int i = this.list().size() - 1; i >= 0; --i) {
         if (this.list().get(i).intValue() != i) {
           lastChange = i;
           break;
         }
       }
      
      // TODO check the case of final element being moved
 
       // if an element was moved to a location previous to its original location
       // for example:
       //
       // before: 0 1 2 3 4 5 6 7 8 9 10 11 12
       // after : 0 1 2 3 4 10 5 6 7 8 9 11 12
       if (this.list().get(lastChange).intValue() == lastChange - 1) {
         assertEquals(lastChange, this.list().get(firstChange).intValue());
 
         for (final int i : new Range(firstChange + 1, lastChange)) {
           assertEquals(i - 1, this.list().get(i).intValue());
         }
 
         for (int i = (lastChange + 1) % this.list().size(); i > lastChange
             || i < firstChange; i = (i + 1) % this.list().size()) {
           assertEquals(i, this.list().get(i).intValue());
         }
       }
 
       // if an element was moved to a location after its original location
       // for example:
       //
       // before: 0 1 2 3 4 5 6 7 8 9 10 11 12
       // after : 0 1 2 3 4 6 7 8 9 10 5 11 12
       if (this.list().get(firstChange).intValue() == firstChange + 1) {
         assertEquals(firstChange, this.list().get(lastChange).intValue());
 
         for (final int i : new Range(firstChange, lastChange)) {
           assertEquals(i + 1, this.list().get(i).intValue());
         }
 
         final int size = this.list().size();
        for (int i = (lastChange + 1) % size; i > lastChange || i < firstChange; i = (i + 1)
            % size) {
           assertEquals(i, this.list().get(i).intValue());
         }
       }
     }
   }
 }
