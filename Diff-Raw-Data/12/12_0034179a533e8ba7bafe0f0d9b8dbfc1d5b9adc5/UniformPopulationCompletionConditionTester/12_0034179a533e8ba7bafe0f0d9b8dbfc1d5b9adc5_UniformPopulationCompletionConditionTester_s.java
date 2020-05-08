 /**
  * UniformPopulationCompletionConditionTester.java
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
 package jmona.game.impl;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import jmona.Population;
 import jmona.impl.DefaultPopulation;
 import jmona.impl.example.ExampleEvolutionContext;
 import jmona.impl.example.ExampleIndividual;
 
 import org.junit.Test;
 
 /**
  * Test class for the UniformPopulationCompletionCondition class.
  * 
  * @author jfinkels
  */
 public class UniformPopulationCompletionConditionTester {
 
   /**
    * Test method for
   * {@link jmona.game.impl.UniformPopulationCompletionConditionion#isSatisfied(jmona.EvolutionContext)}
    * .
    */
   @Test
   public void testIsSatisfied() {
     final UniformPopulationCompletionCondition<ExampleIndividual> criteria = new UniformPopulationCompletionCondition<ExampleIndividual>();
 
     final Population<ExampleIndividual> population = new DefaultPopulation<ExampleIndividual>();
     population.add(new ExampleIndividual());
     population.add(new ExampleIndividual());
 
     final ExampleEvolutionContext context = new ExampleEvolutionContext(
         population);
 
     assertTrue(criteria.isSatisfied(context));
     population.add(new ExampleIndividual() {
     });
     assertFalse(criteria.isSatisfied(context));
 
   }
 }
