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
        for (int v = 0; v < g.V(); v++) numOfPaths[v] = Integer.MAX_VALUE;
         numOfPaths[base] = 1;
         q.enqueue(base);
         levels[base] = 0;
        int level = 0;
         
         while (!q.isEmpty()) {
             int v = q.dequeue();
            int sumPaths = 0;
            level = levels[v];
             for (int w : g.adj(v)) {
                if (numOfPaths[w] != Integer.MAX_VALUE) {
                    if(levels[w] != level)sumPaths += numOfPaths[w];
                 }
                else {
                     q.enqueue(w);
                    levels[w] = level+1;
                }
             }
            
             
            if(sumPaths > mostPaths) {
                mostPaths = sumPaths;
                 mostPathsVertex = v;
             }

            if(v == base) numOfPaths[v] = 1;
            else numOfPaths[v] = sumPaths;       
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
