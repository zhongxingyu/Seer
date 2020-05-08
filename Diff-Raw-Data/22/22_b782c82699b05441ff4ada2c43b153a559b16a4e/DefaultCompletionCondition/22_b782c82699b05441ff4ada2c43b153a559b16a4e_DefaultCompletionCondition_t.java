 /**
  * DefaultCompletionCondition.java
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
 import jmona.Individual;
 import jmona.MaxFitnessCompletionCondition;
 import jmona.MaxGenerationCompletionCondition;
 
 /**
  * ACompletionConditionon which tests for a maximum number of generations passed
  * or an Individual in the current population of the EvolutionContext with the
  * maximum fitness.
  * 
  * @param <T>
  *          The type of Individual in the EvolutionContext which will be tested
  *          for completion.
  * @author jfinkels
  */
 public class DefaultCompletionCondition<T extends Individual> implements
     MaxFitnessCompletionCondition<T>, MaxGenerationCompletionCondition<T> {
 
   /** ThCompletionConditionion for maximum fitness in the EvolutionContext. */
   private final MaxFitnessCompletionCondition<T> maxFitnessCompletionCondition = new DefaultMaxFitnessCompletionCondition<T>();
   /** TCompletionConditiontion for maximum number of generations passed. */
   private final MaxGenerationCompletionCondition<T> maxGenerationCompletionCondition = new DefaultMaxGenerationCompletionCondition<T>();
 
   /**
    * Whether the maximum number of generations has passed, or whether the
    * current population of the specified EvolutionContext contains an Individual
    * with the maximum fitness.
    * 
    * @param context
    *          {@inheritDoc}
   * @see jmona.CompletionCondition#isSatisfied(jmona.EvolutionContext)
    */
   @Override
   public boolean isSatisfied(final EvolutionContext<T> context)
       throws CompletionException {
     return (this.maxFitnessCompletionCondition.isSatisfied(context) || this.maxGenerationCompletionCondition
         .isSatisfied(context));
   }
 
   /**
    * {@inheritDoc}
    * 
    * @param newMaxFitness
    *          {@inheritDoc}
   * @see jmona.MaxFitnessCompletionCondition#setMaxFitness(double)
    */
   @Override
   public void setMaxFitness(final double newMaxFitness) {
     this.maxFitnessCompletionCondition.setMaxFitness(newMaxFitness);
   }
 
   /**
    * {@inheritDoc}
    * 
    * @param newMaxGenerations
    *          {@inheritDoc}
    * @see jmona.MaxGenerationCompletionCondition#setMaxGenerations(int)
    */
   @Override
   public void setMaxGenerations(final int newMaxGenerations) {
     this.maxGenerationCompletionCondition.setMaxGenerations(newMaxGenerations);
   }
 
 }
