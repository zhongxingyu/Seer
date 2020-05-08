 /**
  * GameEvolutionContextTester.java
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
 
 import static org.junit.Assert.assertEquals;
 import jmona.EvolutionException;
 import jmona.Population;
 import jmona.game.impl.example.ExampleGame;
 import jmona.game.impl.example.ExampleStrategy;
 import jmona.impl.DefaultPopulation;
 import jmona.impl.selection.FitnessProportionateSelection;
 import jmona.test.Util;
 
 import org.apache.log4j.Logger;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test class for the GameEvolutionContext class.
  * 
  * @author jfinkels
  */
 public class GameEvolutionContextTester {
 
   /** The context under test. */
   private GameEvolutionContext<ExampleStrategy> context = null;
   /** The population in the EvolutionContext. */
   private Population<ExampleStrategy> population = null;
 
   /** Establish a fixture for tests in this class. */
   @Before
   public final void setUp() {
     this.population = new DefaultPopulation<ExampleStrategy>();
     this.population.add(new ExampleStrategy());
     this.population.add(new ExampleStrategy());
 
     this.context = new GameEvolutionContext<ExampleStrategy>(this.population);
     this.context.setGame(new ExampleGame());
     this.context
         .setSelectionFunction(new FitnessProportionateSelection<ExampleStrategy>());
   }
 
   /**
    * Test method for {@link jmona.game.impl.GameEvolutionContext#sanityCheck()}.
    */
   @Test
   public void testSanityCheck() {
     this.context = new GameEvolutionContext<ExampleStrategy>(this.population);
     try {
       this.context.sanityCheck();
       Util.shouldHaveThrownException();
     } catch (final NullPointerException exception) {
       this.context.setGame(new ExampleGame());
     }
 
     try {
       this.context.sanityCheck();
       Util.shouldHaveThrownException();
     } catch (final NullPointerException exception) {
       // selection function has not been set
       this.context
           .setSelectionFunction(new FitnessProportionateSelection<ExampleStrategy>());
     }
 
     try {
       this.context.sanityCheck();
     } catch (final NullPointerException exception) {
       Util.fail(exception);
     }
   }
 
   /**
    * Test method for
    * {@link jmona.game.impl.GameEvolutionContext#setGame(jmona.game.TwoPlayerGame)}
    * .
    */
   @Test
   public void testSetGame() {
     this.context.setGame(new ExampleGame());
   }
 
   /**
    * Test method for
    * {@link jmona.game.impl.GameEvolutionContext#stepGeneration()}.
    */
   @Test
   public void testStepGeneration() {
     
     int beforeSize = this.context.currentPopulation().size();
     
     LOG.debug(this.context.currentPopulation());
     try {
       this.context.stepGeneration();
     } catch (final EvolutionException exception) {
       Util.fail(exception);
     }
     LOG.debug(this.context.currentPopulation());
     
    assertEquals(beforeSize, this.context.currentPopulation());
     
   }
 
   /** The Logger for this class. */
   private static final transient Logger LOG = Logger
       .getLogger(GameEvolutionContextTester.class);
 }
