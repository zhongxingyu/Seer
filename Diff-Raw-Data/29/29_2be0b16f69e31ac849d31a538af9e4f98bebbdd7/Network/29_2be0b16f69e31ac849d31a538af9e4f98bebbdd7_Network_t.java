 import java.util.Random;
 
/*
 * @Authors: Shawn Craine, Adam Childs (Trevor Cickovski)
 * 
 */

 class TableEntry {
    public int distance;   // The total distance
    public int sourceHost; // The originator
    public boolean mark;   // Am I marked?
 }
 
 public class Network {
 
    // Edges[i][j] contains the distance on the direct connection between
    // host i and host j
    private int[][] edges;
 
    // tables[i] contains the table of shortest paths between host i and all other hosts
    // tables[i][j] contains a TableEntry, which holds (1) the shortest distance so far,
    // (2) the source host, and (3) a mark.
    private TableEntry[][] tables;
 
    // Create a fully connected network of <size> hosts
    public Network(int size) {
       
  
       // Each edge gets a random distance between 0 and 1000
       edges = new int[size][size];
       Random generator = new Random();
       for (int i = 0; i < size; i++) {
          for (int j = 0; j < size; j++) {
             if (i == j)
              edges[i][j] = 0;  // Distance between an edge and itself is zero
             else
              edges[i][j] = generator.nextInt(1000)+1;
          }
       }
 
       // Initialize Table
       tables = new TableEntry[size][size];
       for (int i = 0; i < size; i++) {
           for (int j = 0; j < size; j++) {
              tables[i][j] = new TableEntry();
              if (i == j) {
                 tables[i][j].distance = 0;
                 tables[i][j].sourceHost = i;
                 tables[i][j].mark = true;
              }
              else {
                 tables[i][j].distance = (int)Float.POSITIVE_INFINITY;
                 tables[i][j].sourceHost = -1;
                 tables[i][j].mark = false;
              }
           }
       }
 
       // Run Dijkstra for all hosts
       for (int i = 0; i < size; i++) {
          runDijkstra(i);
       }  
    }
 
 
   // To help you debug
   // At any given time, print the table of shortest paths from i to everywhere else
   private void printTable(int i) {
          System.out.println("FOR HOST "+i+": ");
          for (int j = 0; j < tables.length; j++) {
             System.out.print(tables[i][j].distance+"("+tables[i][j].sourceHost+")");
             if (tables[i][j].mark == true) System.out.print("*");
          }
          System.out.println();
   }
 
   // Run Dijkstra's algorithm for host i
   // NOTE: You should only be modifying tables[i]. 
   public void runDijkstra(int i) {
      boolean allmarked = false;
 
      // 1. Populate the table for all of i's neighbors
      for (int m = 0; m < tables.length; m++) {
     	 TableEntry t = new TableEntry();
     	 t.distance = edges[i][m];
     	 t.sourceHost = i;
     	 t.mark = false;
     	 tables[i][m] = t;
      }
 
      while (!allmarked) { 
         // 2. Find the host with the shortest path (call it k), and mark it
     	 TableEntry k = tables[i][0];
     	 int realK = 0;
     	 for (int m = 0; m < tables.length; m++)
     		 if (tables[i][m].distance < k.distance) {
     			 k = tables[i][m];
     			 tables[i][m].mark = true;
     			 realK = m;
     		 }
  
         // 3. For each umarked neighbor of k (call it j), check if the distance from i through k to j
         //    is less than the current shortest path from i to j -> if it is update the table
     	 TableEntry j = tables[realK][0];
    	 int shortest = tables[i][0].distance; // Set initial shortest
     	 for (int m = 0; m < tables.length; m++)
    	 {
    		 if (!tables[realK][m].mark)
    		 {
     			 int iTk = edges[i][realK];
    			 int kTj = edges[realK][m];
     			 int iTkTj = kTj + iTk;
     			 
    			 if (iTkTj < shortest)
    			 {
    				 shortest = iTkTj;
    				 tables[i][m].distance = shortest;
     			 }
     		 }
    	 }
     			 
         // 4. Check if there are anymore unmarked hosts
         //    If not, set allmarked to true (which stops the loop)
     	 for (int m = 0; m < tables.length; m++) {
     		 for (int z = 0; z < tables.length; z++) {
     			 if (tables[m][z].mark = false)
     				 allmarked = false;
     			 else
     				 allmarked = true;
     		 }
     	 }
      }
 
   }
 
 
 
   // Display a table of each host and distances
   public void display() {
      System.out.print("\t");
      for (int i = 0; i < tables.length; i++) {
          System.out.print(i+"\t");
      }
      System.out.println();
      System.out.println();
      
      for (int i = 0; i < tables.length; i++){
         System.out.print(i+"\t");
         for (int j = 0; j < tables.length; j++) 
            System.out.print(edges[i][j] + "\t");
         System.out.println();
      }
   }
 
 
   // Assuming Dijkstra's algorithm has worked properly,
   // This should return a trace of all hosts on the shortest path between host1 and host2
   public void tracert(int host1, int host2) {
       System.out.println("*********** TRACE FROM "+host1+" TO "+host2+" ***********");
       String path = "";
       TableEntry entry = tables[host1][host2];
       while (entry.sourceHost != host1) {
          path = "Hop from host "+entry.sourceHost+" to "+host2+" for a total distance of "+entry.distance+"\n"+path;
          host2 = entry.sourceHost;
          entry = tables[host1][host2];
       }
       path = "Hop from host "+entry.sourceHost+" to "+host2+" for a total distance of "+entry.distance+"\n"+path;
       System.out.println(path);
   }   
 
    
 
 }
