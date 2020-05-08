 public class ExtendedBreadthFirstPaths  {
     private int numOfPaths[];
     private int levels[];
     private int mostPathsVertex;
     private int mostPaths;
     private int base;
     private Graph g;
     
     public ExtendedBreadthFirstPaths(Graph G, int s) {
         g = G;
         base = s;
         numOfPaths = new int[G.V()];
         levels = new int[G.V()];
         mostPathsVertex = -1;
         mostPaths = -1;
     }
     
     private void generate(){
         Queue<Integer> q = new Queue<Integer>();
        for (int v = 0; v < g.V(); v++) numOfPaths[v] = -1;
         numOfPaths[base] = 1;
         q.enqueue(base);
         levels[base] = 0;
         
         while (!q.isEmpty()) {
             int v = q.dequeue();
             for (int w : g.adj(v)) {
                if (numOfPaths[w] != -1) {
                    if(levels[w] < levels[v]) numOfPaths[v] += numOfPaths[w];
                 }
                if(numOfPaths[w] == -1) {
                     q.enqueue(w);
                    levels[w] = levels[v]+1;
                    numOfPaths[w] = 0;
                }
             }
            
             
            if(numOfPaths[v] > mostPaths) {
                mostPaths = numOfPaths[v];
                 mostPathsVertex = v;
             }
            if(v == base) numOfPaths[v] = 1;       
         }
         
     }
     
     public int getMostPaths(){
         if(mostPaths == -1){
             generate();
         }
         return mostPaths;
     }
     
     public int getMostPathsVertex(){
         if(mostPathsVertex == -1){
             generate();
         }
         return mostPathsVertex;
     }
 }
