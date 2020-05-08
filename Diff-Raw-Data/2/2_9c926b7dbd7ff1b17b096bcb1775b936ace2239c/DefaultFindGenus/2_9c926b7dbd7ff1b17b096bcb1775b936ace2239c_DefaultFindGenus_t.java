 package genus;
 import java.util.Set;
 
 /** The default implementation of the FindGenus interface.
  */
 public class DefaultFindGenus implements FindGenus
 {
     /** Constructor.
      */
     public DefaultFindGenus()
     {
     }
 
     @Override
     public int findGenus(Graph graph)
     {
         return findGenus(new DefaultGraph(graph));
     }
 
     /** Find the genus of a DefaultGraph.
      *  @param graph Graph to find the genus for.
      *  @return The graph genus.
      */
     public int findGenus(DefaultGraph graph)
     {
        if(graph.isCompleteGraph() && graph.getNumberOfVertices() >= 3) {
             int tmp = (graph.getNumberOfVertices() - 3) *
                     (graph.getNumberOfVertices() - 4);
             return (tmp + 11) / 12;
         }
 
         return findGenus(graph, findFaces(graph));
     }
 
     /** Find the genus of a DefaultGraph.
      *  @param graph Graph to find the genus for.
      *  @param faces Maximum number of faces.
      *  @return The graph genus.
      */
     public int findGenus(DefaultGraph graph, int faces)
     {
         return 1 - (graph.getNumberOfVertices() + faces -
                 graph.getNumberOfEdges() / 2 ) / 2;
     }
 
     /** Find the maximum number of faces in a graph.
      *  @param graph Graph to determine the number of faces for.
      *  @return The maximum number of faces in the graph.
      */
     public int findFaces(DefaultGraph graph)
     {
         onRecursionStart(graph);
         int faces = findFaces(graph, -1, -1, -1, -1,
                 0, graph.getNumberOfEdges(), 0);
         afterRecursion();
         return faces;
     }
 
     /** The main recursing method used to find the number of faces in
      *  a graph.
      *  @param graph Graph to find the number of faces for.
      *  @param cycleStart Starting vertex of the current cycle.
      *  @param cycleSecond Second vertex of the current cycle.
      *  @param lastVertex Last vertex visited.
      *  @param current Current location in the graph.
      *  @param currentFaces Number of faces currently found.
      *  @param edgesLeft Edges left in the graph.
      *  @param edgesInCurrentCycle Number of edges used in the cumber cycle.
      *  @return the maximum number of faces in the graph.
      */
     public int findFaces(DefaultGraph graph, int cycleStart,
             int cycleSecond, int lastVertex, int current, int currentFaces,
             int edgesLeft, int edgesInCurrentCycle)
     {
         if(!onRecurse(graph, cycleStart, cycleSecond, lastVertex, current,
                 currentFaces, edgesLeft, edgesInCurrentCycle)) {
             return 0;
         }
 
         /* We need to start a new cycle. */
         if(current < 0) {
             /* Get a random vertex with outbound edges left, and a next
              * candidate. */
             int vertex = graph.getVertexWithCandidates();
 
             /* End of recursion. */
             if(vertex < 0) {
                 onRecursionEnd(currentFaces);
                 return currentFaces;
             }
 
             /* Continue with the random vertex. */
             int candidate = graph.getVertex(vertex).getCandidate();
             return findFaces(graph, vertex, candidate, vertex,
                     candidate, currentFaces, edgesLeft - 1, 1);
         }
 
         Vertex currentVertex = graph.getVertex(current);
 
         /* Create a branch for every candidate. */
         int max = 0, result = 0;
         for(int candidate: currentVertex.getNeighbours()) {
 
             /* Check that the candidate is, well, a candidate. */
             if(currentVertex.isCandidate(lastVertex, candidate)) {
 
                 /* Connect the edge if this fails, we made an illegal move
                  * somewhere * in our algorithm, so we backtrack. To check: we
                  * can assume it * will always connect, so we can safely skip
                  * this check. */
                 if(!graph.connect(lastVertex, current, candidate)) {
                     System.out.println("Illegal move.");
                     return 0;
                 }
 
                 /* Check if we created a circle, which is the case if we are
                  * back in our starting point and we can legally connect the
                  * cycle without breaking any previous permutations. */
                 if(candidate == cycleStart &&
                         graph.connect(current, cycleStart, cycleSecond)) {
 
                     /* Recurse with one more face found. */
                     result = findFaces(graph, -1, -1, -1, -1,
                             currentFaces + 1, edgesLeft - 1, 0);
 
                     /* Disconnect the cycle again. */
                     graph.split(current, cycleStart, cycleSecond);
 
                 /* We did not create a cycle, so we just continue. */
                 } else {
                     result = findFaces(graph, cycleStart,
                             cycleSecond < 0 ? candidate : cycleSecond,
                             current, candidate, currentFaces,
                             edgesLeft - 1, edgesInCurrentCycle + 1);
                 }
 
                 /* We're only interested in the maximum number of faces we can
                  * find in the graph. */
                 if(result > max)
                     max = result;
 
                 /* Disconnect the cycle again. */
                 graph.split(lastVertex, current, candidate);
             }
         }
 
         /* Return the solution with the most cycles. */
         return max;
     }
 
     /** Called before recursion starts.
      *  @param graph The graph.
      */
     public void onRecursionStart(DefaultGraph graph)
     {
     }
 
     /** Called when a recursion starts.
      *  @param graph Graph to find the number of faces for.
      *  @param cycleStart Starting vertex of the current cycle.
      *  @param cycleSecond Second vertex of the current cycle.
      *  @param lastVertex Last vertex visited.
      *  @param current Current location in the graph.
      *  @param currentFaces Number of faces currently found.
      *  @param edgesLeft Edges left in the graph.
      *  @param edgesInCurrentCycle Number of edges used in the cumber cycle.
      *  @return If the function should continue.
      */
     public boolean onRecurse(DefaultGraph graph, int cycleStart,
             int cycleSecond, int lastVertex, int current, int currentFaces,
             int edgesLeft, int edgesInCurrentCycle)
     {
         return true;
     }
 
     /** End of recursion hook. This gets called when the algorithm reaches
      *  a leaf in the search tree.
      *  @param faces Number of faces found.
      */
     public void onRecursionEnd(int faces)
     {
     }
 
     /** After all recursions hook. Can be overriden.
      */
     public void afterRecursion()
     {
     }
 }
