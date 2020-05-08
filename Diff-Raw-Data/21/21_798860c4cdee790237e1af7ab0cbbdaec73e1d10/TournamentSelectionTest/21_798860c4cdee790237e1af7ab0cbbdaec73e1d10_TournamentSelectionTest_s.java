 package org.neurus.evolution;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.neurus.fitness.Fitness;
 import org.neurus.instruction.Program;
 import org.neurus.rng.RandomNumberGenerator;
 
 public class TournamentSelectionTest {
 
   private Population population;
   private RandomNumberGenerator rng;
 
   @Before
   public void setUp() {
     rng = mock(RandomNumberGenerator.class);
     // create a random population, the fifth is the best, the third is the worst
     population = createFakePopulation(10);
     population.get(3).setFitness(new Fitness(4.0));
     population.get(5).setFitness(new Fitness(2.0));
     population.get(8).setFitness(new Fitness(3.0));
   }
 
   @Test
   public void testTournamentSelectionSelectsBestIndividuals() {
     TournamentSelection ts = new TournamentSelection(rng, 3);
     // ensure that individuals 3, 5 and 8 get picked for the tournament
     when(rng.nextInt(population.size())).thenReturn(3, 5, 8);
     // select, the fifth element should win
     assertEquals(5, ts.select(population));
     verify(rng, times(3)).nextInt(population.size());
   }
 
   @Test
   public void testTournamentSelectionSelectsWorstIndividuals() {
     TournamentSelection ts = new TournamentSelection(rng, 3, true);
     // ensure that individuals 3, 5 and 8 get picked for the tournament
     when(rng.nextInt(population.size())).thenReturn(3, 5, 8);
     // select, the third element should win
     assertEquals(3, ts.select(population));
     verify(rng, times(3)).nextInt(population.size());
   }
 
   private Population createFakePopulation(int populationSize) {
     Individual[] individuals = new Individual[populationSize];
     for (int x = 0; x < individuals.length; x++) {
       individuals[x] = new Individual(new Program(new byte[] {}));
     }
     Population population = new Population(individuals);
     return population;
   }
 }
