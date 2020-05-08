 //Takes in a text file containing Euclidean Vertex information
 //and creates a Vertex array from it
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class TwoDimParser {
 
     EuclideanVertex[] vertices;
 
     //main method for testing running time of the algorithms
     /*
         Usage: java TwoDimParser input_file.txt numtrials
     */
     public static void main(String[] args){
        String input_file = args[0];
        int numtrials = Integer.parseInt(args[1]);
 
 
         TwoDimParser hello = new TwoDimParser(input_file);
         hello.printEverything();
         Graph g = new Graph(hello.allVertices());
 
         testGreedy(numtrials, g);
         testSim(numtrials, g);
         testTwoOpt(numtrials, g);
         testGenetic(numtrials, g);
         testChrist(numtrials, g);
 
     }
 
     //initializes the TwoDimParser with the file name and the number of vertices
     public TwoDimParser(String f)
     {
         ArrayList<EuclideanVertex> vlist = new ArrayList<EuclideanVertex>();
         try
         {
             BufferedReader br = new BufferedReader(new FileReader(f));
             String sCurrentLine;
             String[] xy;
             int i = 0;
             while ((sCurrentLine = br.readLine()) != null) {
                 System.out.println(sCurrentLine);
                 xy = sCurrentLine.trim().split("\\s+");
                 double[] coords = {Double.parseDouble(xy[1]), Double.parseDouble(xy[2])};
                 vlist.add(new EuclideanVertex(i++, coords));
             }
 
         } catch (IOException e) {
             e.printStackTrace();
         }
         vertices = new EuclideanVertex[vlist.size()];
         for (int i = 0; i < vlist.size(); i++) {
             vertices[i] = vlist.get(i);
         }
     }
 
     static void testGreedy(int numtrials, Graph g) {
         long startTime;
         long endTime;
         double duration;
 
        int toursum = 0;
        int timesum = 0;
 
         Greedy greedisgood = new Greedy();
         for(int i = 0; i < numtrials; i++)
         {
             startTime = System.nanoTime();
             Tour bestgreedy = greedisgood.findShortestPath(g);
             endTime = System.nanoTime();
             duration = (endTime - startTime)/1000000000.0;
             toursum+=bestgreedy.getLength();
             timesum+=duration;
         }
 
         System.out.printf("Greedy Average over %d trials: %.2f in %.5f s\n ",
                 numtrials, toursum/numtrials, timesum/numtrials);
         //bestgreedy.printGraphToFile("Greedybest.txt");
     }
 
     static void testSim(int numtrials, Graph g) {
         long startTime;
         long endTime;
         double duration;
 
        int toursum = 0;
        int timesum = 0;
 
         SimulatedAnnealing sim = new SimulatedAnnealing(2000000, 1, 0.999);
         for(int i = 0; i < numtrials; i++)
         {
             startTime = System.nanoTime();
             Tour bestsim = sim.findShortestPath(g);
             endTime = System.nanoTime();
             duration = (endTime - startTime)/1000000000.0;
             timesum+=duration;
             toursum+=bestsim.getLength();
         }
 
         System.out.printf("Simulated Annealing Average over %d trials: %.2f in %.5f s\n ",
                 numtrials, toursum/numtrials, timesum/numtrials);
         //bestsim.printGraphToFile("simbest.txt");
     }
 
     static void testChrist(int numtrials, Graph g) {
         long startTime;
         long endTime;
         double duration;
 
        int toursum = 0;
        int timesum = 0;
 
         Christofides christofides = new Christofides();
         for (int i = 0; i < numtrials; i++) {
             startTime = System.nanoTime();
             Tour bestchrist = christofides.findShortestPath(g);
             endTime = System.nanoTime();
             duration = (endTime-startTime)/1000000000.0;
             toursum += bestchrist.getLength();
             timesum += duration;
         }
 
         System.out.printf("Christofides Average over %d trials: : %.2f in %.5f s\n",
                 numtrials, toursum/numtrials, timesum/numtrials);
         //bestchrist.printGraphToFile("christbest.txt");
     }
 
     static void testGenetic(int numtrials, Graph g) {
         long startTime;
         long endTime;
         double duration;
 
        int toursum = 0;
        int timesum = 0;
 
         Genetic genetic = new Genetic(100,30000);
         for(int i = 0; i < numtrials; i++)
         {
             startTime = System.nanoTime();
             Tour bestgenetic = genetic.findShortestPath(g);
             endTime = System.nanoTime();
             duration = (endTime - startTime)/1000000000.0;
             toursum+=bestgenetic.getLength();
             timesum+=duration;
         }
         System.out.printf("Genetic Average over %d trials: %.2f in %.5f s\n ",
                 numtrials, toursum/numtrials, timesum/numtrials);
         //bestgenetic.printGraphToFile("Geneticbest.txt");         */
     }
 
     static void testTwoOpt(int numtrials, Graph g) {
         long startTime;
         long endTime;
         double duration;
 
        int toursum = 0;
        int timesum = 0;
 
         TwoOpt twoopt = new TwoOpt();
         for(int i = 0; i < numtrials; i++)
         {
             startTime = System.nanoTime();
             Tour besttwoopt = twoopt.findShortestPath(g);
             endTime = System.nanoTime();
             duration = (endTime - startTime)/1000000000.0;
             timesum+=duration;
             toursum+=besttwoopt.getLength();
         }
 
         System.out.printf("Two Opt Average over %d trials: %.2f in %.5f s\n ",
                 numtrials, toursum/numtrials, timesum/numtrials);
         //besttwoopt.printGraphToFile("twooptbest.txt");
     }
 
     //returns the parsed vertex array
     EuclideanVertex[] allVertices()
     {
         return vertices;
     }
 
     //Prints out the parsed vertex array
     void printEverything()
     {
         StringBuilder builder = new StringBuilder();
         for(int i = 0; i < vertices.length; i++)
         {
           builder.append(vertices[i].toString() + "\n");
         }
 
         System.out.print(builder.toString());
     }
 }
