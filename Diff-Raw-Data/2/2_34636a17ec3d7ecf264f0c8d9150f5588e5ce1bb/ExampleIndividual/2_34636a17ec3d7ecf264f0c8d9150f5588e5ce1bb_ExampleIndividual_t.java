 /**
  * ExampleIndividual.java
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
 package jmona.impl.example;
 
 import jmona.DeepCopyable;
 
 /**
  * A basic implementation of an Individual which has a built-in fitness
  * property.
  * 
  * @author jfinkels
  */
 public class ExampleIndividual implements DeepCopyable<ExampleIndividual> {
   /** The fitness of this Individual. */
   private double fitness = 0.0;
 
   /** Instantiate this Individual with the default fitness. */
   public ExampleIndividual() {
     // intentionally unimplemented
   }
 
   /**
    * Instantiate this Individual with the specified initial fitness.
    * 
    * @param initialFitness
    *          The fitness of this Individual.
    */
   public ExampleIndividual(final double initialFitness) {
     this.fitness = initialFitness;
   }
 
   /**
    * {@inheritDoc}
    * 
    * @return {@inheritDoc}
    * @see jmona.Individual#deepCopy()
    */
   @Override
   public ExampleIndividual deepCopy() {
    return new ExampleIndividual(this.fitness);
   }
 
   /**
    * Get the fitness of this Individual.
    * 
    * @return The fitness of this Individual.
    */
   public double fitness() {
     return this.fitness;
   }
 
   /**
    * Set the fitness of this Individual.
    * 
    * @param newFitness
    *          The fitness of this Individual.
    */
   public void setFitness(final double newFitness) {
     this.fitness = newFitness;
   }
 }
