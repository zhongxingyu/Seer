 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cvrp;
 
 import cvrp.classes.PrintableSolution;
 import cvrp.abstracts.TerminationCriteria;
 import cvrp.classes.Instance;
 import cvrp.classes.Neighbor;
 import cvrp.classes.Solution;
 import cvrp.exceptions.*;
 import cvrp.interfaces.Tabu;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tamerdark
  */
 public class CVRP {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws IOException, NoSuchTabuTypeException, 
             TerminationCriteriaNotStartedException {
         // TODO code application logic here
         // Instance instance = new Instance(args[0],args[1]);
        Instance instance = new Instance("instanciasCVRP/minimo.txt","settings"); 
         instance.loadEverything();
         run(instance);
     }
 
     /** The TabuSearch Metaheuristic. Implemented.
      * 
      * @param i
      * @throws NoSuchTabuTypeException 
      */
     private static void run(Instance i) throws NoSuchTabuTypeException, TerminationCriteriaNotStartedException {
         List<Tabu> tabuList = new ArrayList<Tabu>();
         Solution current = generateFirstSolution(i);
         PrintableSolution best = current.getPrintableSolution();
         TerminationCriteria tc = i.getTerminationCriteria();
         tc.start();
         while(!tc.timeToFinish(current)){
             try {
                 List<Neighbor> neighbors = i.getNeighborgoodGenerator().generateNeighborhood(current, tabuList);
                 Neighbor neighbor = i.getNeighborSelector().selectNeighbor(neighbors, current);
                 tabuList.addAll(neighbor.getTabus());
                 current.applyMoves(neighbor.getMove());
                 if( current.getDuration() < best.getDuration() ){
                     best = current.getPrintableSolution();
                     tc.recordBest(current);                
                 }
             }
             catch (TabuListFullException ex) {
                 try{
                     tabuList.remove(tabuList.size() - 1);
                 } catch (ArrayIndexOutOfBoundsException a ) {
                     break;
                 }
             }
         }
         tc.finish();
         printSolution(best, tc);
     }
 
     private static void printSolution(PrintableSolution solution, TerminationCriteria tc) {
         BufferedWriter out = null;
         try {
             out = new BufferedWriter(new FileWriter(new File("stat.")));
             out.write("Cost: " + solution.getDuration());
             out.newLine();
             out.write("Iteration until best found: " + tc.getBestFoundIteration());
             out.newLine();
             out.write("Total iterations: " + tc.getCurrentIteration());
             out.newLine();
             out.write("Time until best found: " + tc.getTimeToBest());
             out.newLine();
             out.write("Total time elapsed: " + tc.getTotalTime());
             out.newLine();
             out.write(solution.toString());
         } catch (IOException ex) {
             Logger.getLogger(CVRP.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 out.close();
             } catch (IOException ex) {
                 Logger.getLogger(CVRP.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private static Solution generateFirstSolution(Instance i) {
         Solution s = null;
         try {
             s = new Solution(i);
         } catch (MaxCapacityExceededException ex) {
             System.err.println("La instancia no tiene solución factible.");
             System.exit(1);
         } catch (MaxDurationExceededException ex) {
             System.err.println("La instancia no tiene solución factible.");
             System.exit(1);
         }
         return s;
     }
 
     private Neighbor selectNeighbor(Solution solution, List<Tabu> tabu_list) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 }
