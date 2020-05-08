 /**
  * OnesMutationFunctionTester.java
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
 package jmona.example.ga.ones;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.Vector;
 
 import jmona.MutationException;
 import jmona.example.ga.ones.OnesIndividual;
 import jmona.example.ga.ones.OnesMutationFunction;
 
 import org.junit.Test;
 
 /**
  * Test class for the OnesMutationFunction class.
  * 
  * @author jfinkels
  */
 public class OnesMutationFunctionTester {
 
   /** The length of the genes of the individuals to test. */
   public static final int GENE_LENGTH = 100;
   /** The number of Individuals on which to test mutation. */
   public static final int NUM_INDIVIDUALS = 100;
 
   /**
    * Test method for
    * {@link jmona.example.ga.ones.OnesMutationFunction#mutate(jmona.example.ga.ones.OnesIndividual)}
    * .
    */
   @Test
   public void testMutate() {
     // create the mutator function
     final OnesMutationFunction function = new OnesMutationFunction();
 
     // create a list of all individuals.
     final List<OnesIndividual> allIndividuals = new Vector<OnesIndividual>();
 
     // initialize many genes for many individuals
     for (int i = 0; i < NUM_INDIVIDUALS; ++i) {
 
       // initialize an array as a gene for an individual
       final short[] array = new short[GENE_LENGTH];
       for (int j = 0; j < GENE_LENGTH; ++j) {
         array[j] = 0;
       }
 
       // create an individual with the created gene
       allIndividuals.add(new OnesIndividual(array));
     }
 
     for (final OnesIndividual individual : allIndividuals) {
       // mutate the individual's gene
       try {
         function.mutate(individual);
       } catch (final MutationException exception) {
         exception.printStackTrace(System.err);
         fail(exception.getMessage());
       }
     }
 
     // iterate over all individuals
     double result = 0;
     for (final OnesIndividual individual : allIndividuals) {
 
       // iterate over each bit in the gene of the individual
       double actualMutations = 0;
       for (int i = 0; i < GENE_LENGTH; ++i) {
         // increment the number of mutations each time there is a one
         actualMutations += individual.gene()[i];
       }
 
       // get the total number of mutations over all individuals
       result += actualMutations;
     }
 
     // determine the arithmetic mean of mutations over all individuals
     result /= (double) allIndividuals.size();
 
     // determine the expected average mutations
     final double expectedMutations = OnesMutationFunction.PROB_BITWISE_MUTATION
         * GENE_LENGTH;
 
     // the error tolerance
    final double epsilon = expectedMutations * .10;
 
     // TODO use standard deviation or something more official for epsilon
     assertEquals(expectedMutations, result, epsilon);
   }
 }
