 package MOOCnet;
 /**
  * Created with IntelliJ IDEA.
  * User: ellscampbell
  * Date: 5/7/13
  * Time: 12:22 PM
  */
 
 import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SparseGraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.graph.util.Pair;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 
 public class Network {
 
     private Random            random = new Random()  ;
     private Graph<Node, Edge> graph                  ;
     private Node[]            nodes                  ;
     private Edge[]            edges                  ;
     private int               restartCounter = 0     ;
     private boolean           g2g            = false ;
     private int               refusalCount   = 0     ;
     private int               edgesNN        = 0     ;
     private int               edgesNP        = 0     ;
     private int               edgesPP        = 0     ;
     private int               edgesPN        = 0     ;
 
 
     public void printResultsToFile() {
         this.printEdgeList("edges" + "_netType_"   + Settings.getInstance().getNetworkType()
                                   + "_assort_"    + String.format("%.2f", Settings.getInstance().getAssortativityTarget())
                                   + "_CV_"        + String.format("%.2f", Settings.getInstance().getTargetCV()
                                   + "_k_"         + String.format("%.1f", Settings.getInstance().getMeanDegree()
                                   + "_refusal_"   + String.format("%.2f", Settings.getInstance().getRefusalCoverage()
                                   + "_nodeCount_" + String.format(".1f", Settings.getInstance().getNumberOfNodes()))))
                                    + "_millis_"    + System.currentTimeMillis());
 
         this.printNodeList("nodes" + "_netType_"   + Settings.getInstance().getNetworkType()
                                   + "_assort_"    + String.format("%.2f", Settings.getInstance().getAssortativityTarget())
                                   + "_CV_"        + String.format("%.2f", Settings.getInstance().getTargetCV()
                                   + "_k_"         + String.format("%.1f", Settings.getInstance().getMeanDegree()
                                   + "_refusal_"   + String.format("%.2f", Settings.getInstance().getRefusalCoverage()
                                   + "_nodeCount_" + String.format(".1f", Settings.getInstance().getNumberOfNodes()))))
                                    + "_millis_"    + System.currentTimeMillis());
     }
 
     public void assignVaccinationSentimentStatus() {
         int    numberOfNodes    = Settings.getInstance().getNumberOfNodes();
         double refusalCovg      = Settings.getInstance().getRefusalCoverage();
         int    numberOfRefusers = (int)Math.round(numberOfNodes * refusalCovg);
 
         while (this.refusalCount < numberOfRefusers) {
             Node randomNode;
             do {
                 randomNode = this.nodes[this.random.nextInt(numberOfNodes)];
             }
             while (randomNode.getStatus() != Node.NEUTRAL);
             if (this.random.nextDouble() < refusalCovg) {
                 randomNode.setStatus(Node.REFUSE);
                 this.refusalCount++;
             }
         }
 
         for (Node node : this.graph.getVertices()) {
             if (node.getStatus() == Node.NEUTRAL) node.setStatus(Node.ACCEPT);
         }
     }
 
     public void runSmallWorld() {
         double targetCV = Settings.getInstance().getTargetCV();
         this.initSmallWorldGraph();
         String fileName = "SW.EdgeList" + String.format("%.3f", this.getCV());
         this.printEdgeList(fileName);
         while (this.getCV() < targetCV) {
             this.rewire();
             System.out.println(this.getMeanDegree() + "\t" + this.getCV());
         }
         fileName = "SW.EdgeList" + String.format(".3f", this.getCV());
         this.printEdgeList(fileName);
 
     }
 
     public void runRandom() {
         System.out.println("THIS MAY TAKE A FEW ATTEMPTS...please hold...");
 
         do {
             this.initRandomGraph();
         }
         while (!this.g2g);
 
         double targetCV = Settings.getInstance().getTargetCV();
         String fileName = "Rnd.EdgeList" + String.format("%.3f", this.getCV());
         this.printEdgeList(fileName);
         while (this.getCV() < targetCV) {
             this.rewire();
             System.out.println(this.getMeanDegree() + "\t" + this.getCV());
         }
         fileName = "Rnd.EdgeList" + String.format(".3f", this.getCV());
         this.printEdgeList(fileName);
     }
 
     public void initRandomGraph() {
         Set components                                                ;
         int numberOfNodes = Settings.getInstance().getNumberOfNodes() ;
         int meanDegree    = Settings.getInstance().getMeanDegree()    ;
         this.nodes        = new Node[numberOfNodes]                   ;
         do {
             this.graph = new SparseGraph<Node, Edge>();
             for (int i = 0; i < numberOfNodes; i++) {
                 Node node          = new Node(i) ;
                      this.nodes[i] = node        ;
                      this.graph.addVertex(node)  ;
             }
 
             for (Node node : this.graph.getVertices()) {
                 while (this.graph.degree(node) < meanDegree) {
                     this.restartCounter++;
                     if (this.restartCounter > 5000) {
                         this.restartCounter = 0;
                         return;
                     }
                     Node node2;
                     do {
                         node2 = this.nodes[this.random.nextInt(this.nodes.length)];
                     }
                     while(node2 == node);
 
                     if (this.graph.getNeighbors(node).contains(node2) || this.graph.degree(node2) == meanDegree) continue;
                     else {
                         Edge newEdge = new Edge();
                         this.graph.addEdge(newEdge, node, node2);
                     }
                 }
             }
             WeakComponentClusterer wcc = new WeakComponentClusterer();
             components = wcc.transform(this.graph);
         }
         while (components.size() > 1) ;
         this.g2g = true               ;
         this.remakeEdgeList()         ;
 
     }
 
     public void initSmallWorldGraph() {
         double rewire        = Settings.getInstance().getSmallWorldRewireProbability() ;
         int    numberOfNodes = Settings.getInstance().getNumberOfNodes()               ;
         int    meanDegree    = Settings.getInstance().getMeanDegree()                  ;
                this.nodes    = new Node[numberOfNodes]                                 ;
         Set    components                                                              ;
 
 
         do {
             this.graph = new SparseGraph<Node, Edge>();
             for (int i = 0; i < numberOfNodes; i++) {
                 Node node = new Node(i);
                 this.nodes[i] = node;
                 this.graph.addVertex(node);
             }
             for (int i = 0; i < numberOfNodes; i++) {
                 for (int ii = 0; ii < meanDegree; ii++) {
                     int diff = ii/2 + 1;
                     if (ii%2 == 1) diff *= -1;
                     int newIndex = i + diff;
                     if (newIndex < 0) newIndex += numberOfNodes;
                     if (newIndex >= numberOfNodes) newIndex -= numberOfNodes;
                     Edge newEdge = new Edge();
                     this.graph.addEdge(newEdge, this.nodes[i], this.nodes[newIndex], EdgeType.UNDIRECTED);
                 }
             }
             for (Edge edge:this.graph.getEdges()) {
                 if (this.random.nextDouble() < rewire) {
                     Node source = this.graph.getEndpoints(edge).getFirst();
                     Node newDestination;
                     do {
                         newDestination = this.nodes[this.random.nextInt(numberOfNodes)];
                     }
                     while (this.graph.isNeighbor(source,newDestination) || source.equals(newDestination));
                     this.graph.removeEdge(edge);
                     Edge newEdge = new Edge();
                     this.graph.addEdge(newEdge, source, newDestination, EdgeType.UNDIRECTED);
                 }
             }
             WeakComponentClusterer wcc = new WeakComponentClusterer();
             components = wcc.transform(this.graph);
         }
         while (components.size() > 1);
         this.remakeEdgeList();
     }
 
     public double getMeanDegree() {
         int sumDegree = 0;
         for (Node node : this.graph.getVertices()) {
             sumDegree += this.graph.degree(node);
         }
         double meanDegree = (1.0*sumDegree) / this.graph.getVertexCount();
         return meanDegree;
     }
 
     public double getStandardDeviation() {
         double standardDeviation;
         double meanDegree = this.getMeanDegree();
         double sumDifSquares = 0;
         for (Node node : this.graph.getVertices()) {
             double dif = this.graph.degree(node) - meanDegree;
             double difSquared = dif*dif;
             sumDifSquares += difSquared;
         }
         standardDeviation = Math.pow(sumDifSquares / this.graph.getVertexCount(), 0.5);
         return standardDeviation;
     }
 
     private double getCV() {
         double stDev = this.getStandardDeviation();
         double meanDeg = this.getMeanDegree();
         return stDev/meanDeg;
     }
 
     private void remakeEdgeList() {
         int i = 0;
         this.edges = new Edge[this.graph.getEdgeCount()];
         for (Edge edge : this.graph.getEdges()) {
             this.edges[i] = edge;
             i++;
         }
     }
 
     private void rewire() {
         double currentCV = this.getCV();
         Edge edge = this.edges[this.random.nextInt(this.edges.length)];
 
         Node source = this.graph.getEndpoints(edge).getFirst();
         Node destination = this.graph.getEndpoints(edge).getSecond();
         if (this.graph.getNeighborCount(destination) == 1) return;
 
         // pick a new destination that is not already connected to the source
         Node newDestination;
         do{
             newDestination = this.nodes[this.random.nextInt(this.nodes.length)];
         }
         while (this.graph.getNeighbors(source).contains(newDestination));
 
         //check for creating a double edge
         if (this.graph.getNeighbors(source).contains(newDestination)) return;
         //check for a self edge
         if (source.getID() == newDestination.getID()) return;
 
         //remove edge HERE (after we've confirmed that we're not about to make a self-edge or double-edge
         this.graph.removeEdge(edge);
 
         //draw the new edge
         Edge newEdge = new Edge();
         this.graph.addEdge(newEdge, source, newDestination);
 
         Set components;
         WeakComponentClusterer wcc = new WeakComponentClusterer();
         components = wcc.transform(this.graph);
 
         // rewires that decrease CV or disconnect the GCC are reversed
         if (this.getCV() < currentCV || components.size()>1) {
             this.graph.removeEdge(newEdge);
             this.graph.addEdge(new Edge(), source, destination);
         }
         //re-make this.edges to account for the changes and move to the next
         this.remakeEdgeList();
     }
 
     private void printEdgeList(String filename) {
 
         PrintWriter out = null;
         try {
             out = new PrintWriter(new java.io.FileWriter(filename));
         } catch (IOException e) {
             e.printStackTrace();
         }
         out.println("source\ttarget");
         for (Edge edge : this.graph.getEdges()) {
             out.println(this.graph.getEndpoints(edge).getFirst().getID() + "\t" + this.graph.getEndpoints(edge).getSecond().getID());
 
         }
         out.close();
     }
 
     private void printNodeList(String filename) {
 
         PrintWriter out = null;
         try {
             out = new PrintWriter(new java.io.FileWriter(filename));
         } catch (IOException e) {
             e.printStackTrace();
         }
         out.println("id\tstatus");
         for (Node node : this.graph.getVertices()) {
             out.println(node.getID() + "\t" + node.getStatus());
 
         }
         out.close();
     }
 
     public void increaseAssortativity() {
         double currentR = this.measureAssortativity();
         double targetR  = Settings.getInstance().getAssortativityTarget();
         int controlCounter = 0;
         if (currentR < targetR) {
             while (currentR < targetR) {
                 currentR = this.assortVaccineRefusal(currentR);
                 controlCounter++;
                 System.out.println(currentR);
             }
         }
     }
     private double measureAssortativity() {
         /**
          * This method counts each type of edge in the network, and returns those values to calculate assortativity
          */
         int numberOfEdges_Neg_Neg = 0;
         int numberOfEdges_Pos_Pos = 0;
         int numberOfEdges_Pos_Neg = 0;
         int numberOfEdges_Neg_Pos = 0;
 
         for (Edge edge : this.graph.getEdges()) {
             Pair pair        =  this.graph.getEndpoints(edge) ;
             Node source      =  (Node)pair.getFirst()   ;
             Node destination =  (Node)pair.getSecond()  ;
             if (source.getStatus() == Node.REFUSE && destination.getStatus() == Node.REFUSE) numberOfEdges_Neg_Neg++ ;
             if (source.getStatus() == Node.ACCEPT && destination.getStatus() == Node.ACCEPT) numberOfEdges_Pos_Pos++ ;
             if (source.getStatus() == Node.ACCEPT && destination.getStatus() == Node.REFUSE) numberOfEdges_Pos_Neg++ ;
             if (source.getStatus() == Node.REFUSE && destination.getStatus() == Node.ACCEPT) numberOfEdges_Neg_Pos++ ;
         }
 
         this.edgesNN = numberOfEdges_Neg_Neg;
         this.edgesPP = numberOfEdges_Pos_Pos;
         this.edgesNP = numberOfEdges_Neg_Pos;
         this.edgesPN = numberOfEdges_Pos_Neg;
 
         return this.getAssortativity(numberOfEdges_Neg_Neg, numberOfEdges_Pos_Pos, numberOfEdges_Pos_Neg, numberOfEdges_Neg_Pos);
     }
 
     public double getAssortativity(int numberOfEdges_Neg_Neg, int numberOfEdges_Pos_Pos, int numberOfEdges_Pos_Neg, int numberOfEdges_Neg_Pos) {
         /**
          * Assortative Mixing calculations for directed networks (see Newman 2003)
          */
         int    numberOfEdges     = numberOfEdges_Neg_Neg + numberOfEdges_Pos_Pos + numberOfEdges_Pos_Neg + numberOfEdges_Neg_Pos ;
         double eII_neg           = (double)numberOfEdges_Neg_Neg / numberOfEdges ;
         double eII_pos           = (double)numberOfEdges_Pos_Pos / numberOfEdges ;
         double eIJ_pos_neg       = (double)numberOfEdges_Pos_Neg / numberOfEdges ;
         double eIJ_neg_pos       = (double)numberOfEdges_Neg_Pos / numberOfEdges ;
 
         double a_pos = (eII_pos + eIJ_neg_pos);
         double a_neg = (eII_neg + eIJ_pos_neg);
 
         double r = ((eII_neg + eII_pos) - (a_pos * a_neg)) / (1 - (a_pos * a_neg)) ;
 
         return r;
     }
 
     private double assortVaccineRefusal(double currentR) {
         /**
          * This method will either increase or decrease assortative mixing, depending on the target assortativity
          * This method will maintain the underlying topology of the network, only modifying node characteristics
          * For each change, assortativity is re-calculated and compared to the target assortativity.
          * If the change is undesired, node characteristics are reverted.
          * Successful or unsuccessful, the method returns the current assortativity (r) of the network.
          */
 
         int deltaNN = 0 ;
         int deltaNP = 0 ;
         int deltaPN = 0 ;
         int deltaPP = 0 ;
         int randomIndex ;
 
         // generate node & edge arrays for index access
         ArrayList<Edge> edges = new ArrayList<Edge>() ;
         ArrayList<Node>     nodes = new ArrayList<Node>() ;
         for (Node node : this.graph.getVertices()) {
             nodes.add(node);
         }
         for (Edge edge : this.graph.getEdges()) {
             edges.add(edge);
         }
 
         // initialize people involved
         Node p1, p2, pSwap;
 
         // pick a random edge that connects nodes of either of the two offset-types  (positive->negative or negative->positive)
         do {
             randomIndex = this.random.nextInt(this.graph.getEdgeCount());
             Edge randomEdge = edges.get(randomIndex);
             Pair pair = this.graph.getEndpoints(randomEdge);
             p1 = (Node)pair.getFirst();
             p2 = (Node)pair.getSecond();
         }
         while(p1.getStatus() == Node.ACCEPT && p2.getStatus() == Node.ACCEPT ||
                 p1.getStatus() == Node.REFUSE && p2.getStatus() == Node.REFUSE);
 
         // first, we'll try swapping out p1's sentiment
         pSwap = p1;
 
         // we only swap negative sentiments, positive sentiments remain unchanged
         if (p1.getStatus() == Node.ACCEPT) pSwap = p2;
 
         // check out pSwap's neighbors
         Collection neighbors = this.graph.getNeighbors(pSwap);
         Iterator neighborsIterator = neighbors.iterator();
 
         // since pSwap is negative by this point, if the neighbor also has a negative
         while(neighborsIterator.hasNext()) {
             Node neighbor = (Node) neighborsIterator.next();
             if (neighbor.getStatus() == Node.REFUSE) {
                 deltaNN--;
                 deltaNP++;
             }
             else {
                 deltaPN--;
                 deltaPP++;
             }
         }
         // swap first node now! otherwise calculations will be wrong if second node
         pSwap.setStatus(Node.ACCEPT);
 
         // now pick a random node that is positive, and that is not connected to a positive node
         Node pRandom;
 
         // first make sure the node is positive
         do {
             randomIndex = this.random.nextInt(this.graph.getVertexCount());
             pRandom = (Node) nodes.get(randomIndex);
         }
         while (pRandom.getStatus() == Node.REFUSE);
 
         // calculate numberOfEdges that change
         neighbors = this.graph.getNeighbors(pRandom);
         neighborsIterator = neighbors.iterator();
         while (neighborsIterator.hasNext()) {
             Node neighbor = (Node) neighborsIterator.next();
             if (neighbor.getStatus() == Node.REFUSE) {
                 deltaPN--;
                 deltaNN++;
             }
             else {
                 deltaPP--;
                 deltaPN++;
             }
         }
         pRandom.setStatus(Node.REFUSE);
 
         double r = this.getAssortativity(this.edgesNN+deltaNN, this.edgesPP+deltaPP, this.edgesPN+deltaPN, this.edgesNP+deltaNP);
 
         if (r < currentR) {
             pSwap.setStatus(Node.REFUSE);
             pRandom.setStatus(Node.ACCEPT);
             return currentR;
         }
         else {
             this.edgesNN += deltaNN;
             this.edgesPP += deltaPP;
             this.edgesPN += deltaPN;
             this.edgesNP += deltaNP;
             return r;
         }
 
     }
 
 
 }
