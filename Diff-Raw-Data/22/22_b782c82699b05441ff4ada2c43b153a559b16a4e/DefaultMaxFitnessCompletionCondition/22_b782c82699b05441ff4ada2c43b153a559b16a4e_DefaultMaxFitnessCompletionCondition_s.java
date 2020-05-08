 /**
  * DefaultMaxFitnessCompletionCondition.java
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
 package jmona.impl;
 
 import jmona.CompletionException;
 import jmona.EvolutionContext;
 import jmona.FitnessException;
 import jmona.FitnessFunction;
 import jmona.Individual;
 import jmona.MaxFitnessCompletionCondition;
 
 /**
  * CompletionCondition used to determine whether an EvolutionContext contains an
  * Individual with a maximum fitness value.
  * 
  * @param <T>
  *          The type of Individual in the EvolutionContext to test for
  *          completion.
  * @author jfinkels
  */
 public class DefaultMaxFitnessCompletionCondition<T extends Individual>
     implements MaxFitnessCompletionCondition<T> {
 
   /** The default maximum fitness of an Individual. */
   public static final double DEFAULT_MAX_FITNESS = Double.MAX_VALUE;
   /** The maximum fitness of an Individual. */
   private double maxFitness = DEFAULT_MAX_FITNESS;
 
   /**
    * Determine whether an Individual with the maximum fitness exists in the
    * current population of the specified EvolutionContext.
    * 
    * This method uses the FitnessFunction from
    * {@link EvolutionContext#fitnessFunction()} to measure the fitness of
    * Individuals.
    * 
    * @param context
    *          {@inheritDoc}
    * @throws CompletionException
    *           If the fitness function of the EvolutionContext throws a
    *           FitnessException.
   * @see CompletionConditionndition#isSatisfied(jmona.EvolutionContext)
    */
   @Override
   public boolean isSatisfied(final EvolutionContext<T> context)
       throws CompletionException {
     final FitnessFunction<T> fitnessFunction = context.fitnessFunction();
 
     if (fitnessFunction == null) {
       throw new CompletionException(
           "No FitnessFunction has been set on the EvolutionContext, so no fitnesses can be determined.");
     }
 
     try {
       for (final T individual : context.currentPopulation()) {
         if (fitnessFunction.fitness(individual) >= this.maxFitness) {
           return true;
         }
       }
     } catch (final FitnessException exception) {
       throw new CompletionException(
           "Could not determine fitness of an individual.", exception);
     }
     return false;
   }
 
   /**
    * {@inheritDoc}
    * 
    * @param newMaxFitness
    *          {@inheritDoc}
   * @see jMaxFitnessCompletionConditiondition#setMaxFitness(double)
    */
   @Override
   public void setMaxFitness(final double newMaxFitness) {
     this.maxFitness = newMaxFitness;
   }
 }
