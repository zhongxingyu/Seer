 package Analyzer;
 
 import tsp.*;
 
 /**
  *
  * @author david
  */
 public class Analyzer {
 
     private final AnalysesType currentAnalyses = AnalysesType.RANDOM;
 //    private final AnalysesType currentAnalyses = AnalysesType.FILE_LOADED;
 
     /*
      * Constants related to real instance TSP
      */
    private final String GRAPH_FILE = "bayg29.tsp";
     /*
      * Constants related to random analyses
      */
     private final int INTERACTIONS = 100;
     private final int MAX_NODES = 100;
     private final int MIN_NODES = 4;
     private final int MAX_EDGE_VALUE = 100;
     private final int MIN_EDGE_VALUE = 1;
     /*
      * Constants related to compare() method
      */
     private final boolean PRINT_MATRIX = false;
     private final boolean GENERATE_REPORT = true;
     /*
      * Analyses' essencial attributes
      */
     Tsp currentProblem;
     TspFileHandler inout;
     double[] algorithmsCosts;
     int[] algorithmsCount;
     Algorithms[] algorithmsCompared;
     int currInter;
 
     public void run() {
         inout = new TspFileHandler(GRAPH_FILE);
 
         // Alter the comparing Algorithms here
         algorithmsCompared = new Algorithms[]{
            //            Algorithms.NEARESTNEIGHBOR,
             Algorithms.TWICEAROUND,
            //            Algorithms.TWICEAROUNDDIJK,
            Algorithms.EDGESCORE,};
 
         algorithmsCosts = new double[algorithmsCompared.length];
         algorithmsCount = new int[algorithmsCompared.length];
 
         for (int i = 0; i < algorithmsCount.length; i++) {
             algorithmsCount[i] = 0;
         }
 
         try {
 
             if (currentAnalyses == AnalysesType.FILE_LOADED) {
 
                 double m[][] = inout.read();
                 currentProblem = new Tsp(m);
 
                 if (PRINT_MATRIX) {
                     currentProblem.print();
                 }
 
                 compare();
 
             } else {
                 // for each defined interaction
                 for (currInter = 0; currInter < INTERACTIONS; currInter++) {
 
                     int tNodes = (int) (Math.random() * MAX_NODES);
 
                     if (tNodes < MIN_NODES) {
                         tNodes += MIN_NODES;
 
                         if (tNodes > MAX_NODES) {
                             tNodes = MAX_NODES;
                         }
                     }
 
                     // randomly generates a instance of TSP
                     currentProblem = new Tsp(tNodes, MAX_EDGE_VALUE, MIN_EDGE_VALUE);
 
                     if (PRINT_MATRIX) {
                         currentProblem.print();
                     }
 
                     // scoreup the best method
                     algorithmsCount[compare()]++;
                 }
             }
 
             if (GENERATE_REPORT && currentAnalyses == AnalysesType.RANDOM) {
                 inout.append("Conclusion - shortest circuit count:");
 
                 for (int i = 0; i < algorithmsCompared.length; i++) {
                     inout.append(algorithmsCompared[i] + ": " + algorithmsCount[i]);
                 }
 
                 inout.commit();
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private int compare() throws Exception {
 
         if (GENERATE_REPORT) {
             inout.append("> inst " + currInter + '\n');
             inout.append("Number of cities: " + currentProblem.getNodes());
         }
 
         for (int i = 0; i < algorithmsCompared.length; i++) {
 
             if (algorithmsCompared[i] == Algorithms.NEARESTNEIGHBOR) {
                 algorithmsCosts[i] = currentProblem.NearestNeighbor();
 
             } else if (algorithmsCompared[i] == Algorithms.TWICEAROUND) {
                 algorithmsCosts[i] = currentProblem.TwiceAround();
 
             } else if (algorithmsCompared[i] == Algorithms.TWICEAROUNDDIJK) {
                 algorithmsCosts[i] = currentProblem.TwiceAroundDijkstra();
 
             } else if (algorithmsCompared[i] == Algorithms.EDGESCORE) {
                 algorithmsCosts[i] = currentProblem.EdgeScore();
             }
         }
 
         int ret = 0;
         for (int i = 0; i < algorithmsCompared.length; i++) {
 //            System.out.println(algorithmsCompared[i] + ": " + algorithmsCosts[i]);
 
             if (GENERATE_REPORT) {
                 inout.append(algorithmsCompared[i] + ": " + algorithmsCosts[i]);
             }
 
             if (algorithmsCosts[i] < algorithmsCosts[ret]) {
                 ret = i;
             }
         }
 
         //System.out.println("\nBest result: " + algorithmsCompared[ret] + "\n");
 
         if (GENERATE_REPORT) {
             inout.append("\nBest result: " + algorithmsCompared[ret].toString() + "\n");
         }
 
         // returns the method which gave the shortest solution
         return ret;
     }
 }
