 /**
  * SingleElementwiseMutationFunctionTester.java
  * 
  * Copyright 2010 Jeffrey Finkelstein
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
 import jmona.DeepCopyableList;
 import jmona.MutationException;
 import jmona.impl.DeepCopyableVector;
 import jmona.impl.example.ExampleIndividual;
 import jmona.impl.example.ExampleMutationFunction;
 import jmona.test.Util;
 
 import org.junit.Test;
 
 /**
  * Test class for the SingleElementwiseMutationFunction class.
  * 
 * @author Jeffrey Finkelstein
  * @since 0.5
  */
 public class SingleElementwiseMutationFunctionTester {
 
   /** Zero. */
   public static final double ZERO_DELTA = 0.0;
 
   /**
    * Test for the
    * {@link jmona.impl.mutation.SingleElementwiseMutationFunction#mutate(DeepCopyableList)}
    * method.
    */
   @Test
   public void testMutate() {
     final SingleElementwiseMutationFunction<ExampleIndividual> function = new SingleElementwiseMutationFunction<ExampleIndividual>();
     function.setElementMutationFunction(new ExampleMutationFunction());
     final DeepCopyableList<ExampleIndividual> list = new DeepCopyableVector<ExampleIndividual>();
     list.add(new ExampleIndividual(1));
     try {
       function.mutate(list);
     } catch (final MutationException exception) {
       Util.fail(exception);
     }
 
     assertEquals(-1, list.get(0).fitness(), ZERO_DELTA);
 
   }
 
 }
