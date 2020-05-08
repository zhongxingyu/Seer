 package org.neurus.evolution;
 
 import org.neurus.rng.RandomNumberGenerator;
 
 import com.google.common.base.Preconditions;
 
 public class TournamentSelection implements SelectionMethod {
 
   private RandomNumberGenerator rng;
   private int tournamentSize;
   private boolean selectWorst;
 
   public TournamentSelection(RandomNumberGenerator rng, int tournamentSize) {
     this(rng, tournamentSize, false);
   }
 
   public TournamentSelection(RandomNumberGenerator rng, int tournamentSize, boolean selectWorst) {
     super();
     Preconditions.checkArgument(tournamentSize > 1);
     this.rng = rng;
     this.tournamentSize = tournamentSize;
     this.selectWorst = selectWorst;
   }
 
   @Override
   public int select(Population population) {
    double bestFitness = selectWorst ? Double.MIN_VALUE : Double.MAX_VALUE;
     int winner = -1;
     for (int x = 0; x < tournamentSize; x++) {
       int individualIndex = rng.nextInt(population.size());
       double fit = population.get(individualIndex).getFitness().getValue();
       if ((!selectWorst && fit <= bestFitness) || (selectWorst && fit >= bestFitness)) {
         bestFitness = fit;
         winner = individualIndex;
       }
     }
     return winner;
   }
 }
