 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.Scanner;
 
 class MaxFlow {
   static HashMap<String, Integer> mappings;
   static int k;
 
   public static void main(String args[]) {
       Scanner scanner = new Scanner(System.in);
       int from, to, capacity;
 
       int N = scanner.nextInt();
       mappings = new HashMap<String, Integer>(N);
       k = 0;
       Graph g = new Graph(N);
       while(scanner.hasNext()) {
           from = translateChar(scanner.next());
           to = translateChar(scanner.next());
           capacity = Integer.parseInt(scanner.next());
           g.addEdge(from, to, capacity);
       }
       Vertex source = g.vertices.get(translateChar("A"));
       Vertex target = g.vertices.get(translateChar("Z"));
       System.out.println(maxFlow(g, source, target));
   }
 
   public static int translateChar(String chr) {
      if (mappings.containsKey(chr)) {
          return mappings.get(chr).intValue();
      }
      mappings.put(chr, ++k);
      return k;
   }
 
   public static int maxFlow(Graph g, Vertex source, Vertex target) {
       ArrayList<EdgeNode> path;
       int minResidual;
       path = DfsPath.Dfs(g, source, target, new ArrayList<EdgeNode>(),
               new HashSet<Vertex>());
       while (path != null) {
          minResidual = findMinResidual(path);
          for (EdgeNode edge : path) {
              if (edge.isForward()) {
                  edge.flow = edge.flow + minResidual;
              }
              else {
                 edge.flow = edge.flow - minResidual;
              }
          }
          path = DfsPath.Dfs(g, source, target, new ArrayList<EdgeNode>(),
                  new HashSet<Vertex>());
       }
       return vertexFlow(target);
   }
 
   public static int findMinResidual(ArrayList<EdgeNode> path) {
       int minResidual;
       if (path.size() < 1) {
           return 0;
       }
 
       minResidual = Integer.MAX_VALUE;
       for (EdgeNode edge : path) {
           if (edge.capacity - edge.flow < minResidual) {
               minResidual = edge.capacity - edge.flow;
           }
       }
       return minResidual;
   }
 
   public static int vertexFlow(Vertex vertex) {
       int flow = 0;
       for (EdgeNode edge : vertex.edges) {
           if (edge.isBackward()) {
               flow += edge.forward.flow;
           }
       }
       return flow;
   }
 }
