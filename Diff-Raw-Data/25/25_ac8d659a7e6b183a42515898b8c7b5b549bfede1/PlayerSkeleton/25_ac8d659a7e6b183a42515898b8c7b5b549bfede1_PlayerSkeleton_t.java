import java.util.*;
 
 public class PlayerSkeleton {
 
     //implement this function to have a working system
     public int pickMove(State s, int[][] legalMoves) {
         //Chromosome c = Learner.GetBestCandidate();
         //Chromosome c = new SimpleChromosome(-25.3, -40.4, -12.3, -14.9);
         Chromosome c = new SimpleChromosome(-27.6119, -47.503, -49.41, -2.52505);
 
         System.out.println(c.pickMove(s));
         return c.pickMove(s);
     }
 
     public static void main(String[] args) {
 
 
         /* UNCOMMENT FOR LEARNING
         List<Chromosome> initialPop = new ArrayList<Chromosome>();
         for (int x = 0; x < 100; x++) { // <------------modify population size here
         initialPop.add(new SimpleChromosome());
         }
         SimpleGA ga = new SimpleGA(initialPop);
         Learner learner = new Learner(ga);
 
         System.out.println("Learner Made");
 
         // Number of generations
         learner.Run(10);
          */
 
 
         // Remember to remove top part
         State s = new State();
         new TFrame(s);
         PlayerSkeleton p = new PlayerSkeleton();
         while (!s.hasLost()) {
             System.out.println("------------------------------------------------------------------------------------------");
             s.makeMove(p.pickMove(s, s.legalMoves()));
             s.draw();
             s.drawNext(0, 0);
 
             try {
                 Thread.sleep(300);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
         System.out.println("You have completed " + s.getRowsCleared() + " rows.");
     }
 }
 
