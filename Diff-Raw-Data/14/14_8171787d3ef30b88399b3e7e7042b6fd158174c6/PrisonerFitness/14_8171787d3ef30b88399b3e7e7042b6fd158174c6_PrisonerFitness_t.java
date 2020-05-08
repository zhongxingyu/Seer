 package evolution.prisoner.evolution;
 
 import evolution.FitnessFunction;
 import evolution.prisoner.Result;
 import evolution.prisoner.Strategy;
 import evolution.prisoner.Strategy.Move;
 import evolution.individuals.Individual;
 import evolution.individuals.IntegerIndividual;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class PrisonerFitness implements FitnessFunction {
 
     ArrayList<Strategy> strategies;
     Random rnd = new Random();
 
     public PrisonerFitness(ArrayList<Strategy> strategies) {
         this.strategies = strategies;
     }
 
     public static Move[] toMovesArray(Individual ind) {
 
         IntegerIndividual iind = (IntegerIndividual) ind;
 
         Move[] moves = new Move[iind.length()];
 
         for (int i = 0; i < iind.length(); i++) {
             moves[i] = Move.values()[(Integer) iind.get(i)];
         }
 
         return moves;
     }
 
     public double evaluate(Individual aSubject) {
 
         EvolvedStrategy es = new EvolvedStrategy(toMovesArray(aSubject));
 
         int score = 0;
 
         for (Strategy s : strategies) {
 
            s.reset();
 
            int sc1 = 0;
 
             int iters = 150 + rnd.nextInt(100);
 
             for (int m = 0; m < iters; m++) {
 
                 Move s1Move;
                 Move s2Move;
 
                 s1Move = es.nextMove();
                 s2Move = s.nextMove();
 
 
                 Result r1 = new Result(s1Move, s2Move);
                 Result r2 = new Result(s2Move, s1Move);
 
                 sc1 += r1.getMyScore();
 
                 try {
                     es.reward(r1);
                 } catch (Exception e) {
                     sc1 = 0;
                 }
                 try {
                     s.reward(r2);
                 } catch (Exception e) {
                 }
 
             }
 
             score += sc1;
 
         }
 
         aSubject.setObjectiveValue(score);
         return score;
     }
 }
