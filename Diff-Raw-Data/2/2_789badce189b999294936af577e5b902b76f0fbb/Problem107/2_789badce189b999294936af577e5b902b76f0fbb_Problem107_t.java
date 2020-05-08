 package euler.level3;
 
 import java.io.IOException;
 
 import euler.Problem;
 import euler.graph.PrimsAlgorithm;
 import euler.graph.WeightedDirectedGraphImpl;
 import euler.graph.WeightedDirectedGraphImpl.Edge;
 import euler.graph.WeightedDirectedGraphImpl.Vertex;
 import euler.input.FileUtils;
 
 public class Problem107 extends Problem<Integer> {
 
     private int getTotalWeight(WeightedDirectedGraphImpl g) {
         int total = 0;
         for (Vertex v : g) {
             for (Edge e : v) {
                 total += e.getWeight();
             }
         }
         return total;
     }
 
     @Override
     public Integer solve() {
         WeightedDirectedGraphImpl input;
         try {
             input = FileUtils.readWeightedGraph(this);
            WeightedDirectedGraphImpl output = PrimsAlgorithm.create(input).run();
             return getTotalWeight(input) / 2 - getTotalWeight(output);
         } catch (IOException e) {
             e.printStackTrace();
             return -1;
         }
     }
 }
