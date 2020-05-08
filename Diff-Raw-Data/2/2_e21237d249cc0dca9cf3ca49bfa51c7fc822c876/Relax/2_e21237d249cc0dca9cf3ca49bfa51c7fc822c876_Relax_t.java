 package edu.aa12;
 
 /**
 * Implementation of the ILP relaxation lower bound
  */
 public class Relax {
 
   public static void main(String[] args) throws Exception {
     Graph g = new Instance2();
 
     long start = System.nanoTime();
     BnBNode n = new RelaxBNB(g).solve();
     long end = System.nanoTime();
     System.out.printf("Took %.2fms\n",(end-start)/1000000.0);
     Visualization.visualizeSolution(g, n);
   }
 }
