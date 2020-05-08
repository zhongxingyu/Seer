 package edu.kpi.pzks.core.queue;
 
 import edu.kpi.pzks.core.model.Link;
 import edu.kpi.pzks.core.model.Node;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
import java.util.Set;
 
 /**
  * @author smarx
  */
 public abstract class AbstractFactorEvaluator implements FactorEvaluator {
 
     private Collection<Node> nodes;
     private Collection<Link> links;
 
     public AbstractFactorEvaluator(Collection<Node> nodes) {
         this.nodes = nodes;
     }
 
     protected boolean isOnCriticalPath(){
         return false;
     }
 
     protected int getCriticalPathForGraph() {
 
         int criticalPath;
         int pathWeight = 0;
         for(Node n : nodes) {
             if(n.getOutputNodes().size() == 0) {
                System.out.printf("Calculate critical path for Node #%d (%d)\n", n.getId(), n.getWeight());
                 List<Node> path = findPathToRoot(n);
                for (Node pathNode : path) {
                    System.out.print();
                }
                 int weight = calculateCriticalPath(path);
                 if(weight > pathWeight) {
                     pathWeight = weight;
                 }
             }
         }
 
         return pathWeight;
     }
 
     private int calculateCriticalPath(List<Node> path) {
         int weight = 0;
         for(Node node : path) {
             weight += node.getWeight();
         }
         return weight;
     }
 
     protected List<Node> findPathToRoot(Node n) {
         Collection<Node> inNodes = n.getInputNodes();
         int criticalWeight = 0;
         List<Node> criticalPath = new ArrayList<>();
 
         for(Node current : inNodes) {
             List<Node> currentPath = findPathToRoot(current);
             int weight = calculateCriticalPath(currentPath);
             if(weight > criticalWeight) {
                 criticalPath = currentPath;
             }
         }
 
         criticalPath.add(0, n);
         return criticalPath;
     }
 
     protected int getCriticalPathFromTop(Node n) {
         return 0;
     }
 
     protected int getCriticalPathFromBottom(Node n) {
         return 0;
     }
 
     protected double getWeightedCriticalPathFromTop(Node n) {
         return (getCriticalPathFromTop(n) + .0d)/getCriticalPathForGraph();
     }
 
     protected double getWeightedCriticalPathFromBottom(Node n) {
         return 0;
     }
     
     protected int getCriticalNumberForGraph() {
         return 0;
     }
 
     protected int getCriticalNumberFromTop(Node n) {
         return 0;
     }
 
     protected int getCriticalNumberFromBottom(Node n) {
         return 0;
     }
 
     protected double getWeightedCriticalNumberFromTop(Node n) {
         return (getCriticalNumberFromTop(n) + .0d)/getCriticalNumberForGraph();
     }
 
     protected double getWeightedCriticalNumberFromBottom(Node n) {
         return 0;
     }
 
     /**
      * @param n Node which needs to have it's root node retrieved
      * @return Node which has a simple path to a given node and has no inbound links
      */
     protected Node getRootNodeFor(Node n) {
         return n;
     }
 
 }
