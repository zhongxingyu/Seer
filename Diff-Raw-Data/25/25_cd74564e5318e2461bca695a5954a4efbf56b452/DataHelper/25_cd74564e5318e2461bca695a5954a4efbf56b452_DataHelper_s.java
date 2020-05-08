 package edu.msergey.jalg.graphs.kosarajus_two_pass;
 
 import edu.msergey.jalg.ResourceDataReader;
 
 import java.util.ArrayList;
 import java.util.List;
 
 class DataHelper {
     protected static List<List<Integer>> loadData(String filename) {
        ResourceDataReader resourceReader = new ResourceDataReader(
                String.format("/graphs/kosarajus_two_pass/%s", filename));
        List<List<Integer>> verticesToConnections = new ArrayList<List<Integer>>();
        String line;
        while ((line = resourceReader.readLine()) != null) {
            List<Integer> connections = new ArrayList<Integer>(2); // always only two integers
            verticesToConnections.add(connections);
            String[] words = line.split("\\s+");
            for (String word : words) {
                connections.add(Integer.parseInt(word.trim()));
             }
         }
        return verticesToConnections;
     }
 }
