 import java.util.*;
 
 /**
  * Extends a HashMap, only rewriting the toString function.
  */
 public class GraphTable extends HashMap<String, Node> {
    public List<Node> allNodes = null;
 
    public String toILOC() {
       String ret = "";
       List<Node> nodes = new LinkedList<Node>();
 
       for (String s : this.keySet()) {
          nodes.addAll(TopoSort.sort(this.get(s)));
       }
 
       for (Node n : nodes) {
          ret += n.toILOC();
       }
 
       return ret;
    }
 
    public List<Node> getAllNodes() {
       if (allNodes == null) {
          topoSort();
       }
 
       return allNodes;
    }
 
    public void topoSort() {
       List<Node> nodes; 
 
       allNodes = new LinkedList<Node>();
       for (String s : this.keySet()) {
          nodes = TopoSort.sort(this.get(s));
          allNodes.addAll(nodes);
       }
    }
 
    public String toSparc() {
       String ret = "";
       List<Node> nodes = new LinkedList<Node>();
 
       // Print out the functions.
       for (String s : this.keySet()) {
          // .align 4
          ret += "\t.align 4\n";
 
          // .global printList
          ret += "\t.global " + s + "\n";
 
          // .type    printList, #function
          ret += "\t.type " + s + ", #function\n";
 
          // WE ARE A FUNCTION!
          this.get(s).function = true;
 
          nodes = TopoSort.sort(this.get(s));
 
          for (Node n : nodes) {
             ret += n.toSparc();
          }
 
          // .size    add, .-add
          ret += "\t.size " + s + ", .-" + s + "\n";
       }
 
       return ret;
    }
 
    public void computeLiveSets() {
       ListIterator<Node> itr;
       boolean liveSetsAreConstant;
       Node check;
          
       do {
          // Start iterator and end of nodes.
          liveSetsAreConstant = true;
          itr = getAllNodes().listIterator(getAllNodes().size());
 
          while (itr.hasPrevious()) {
            liveSetsAreConstant = 
             itr.previous().redoLiveSet() && liveSetsAreConstant;
          }
 
       } while (!liveSetsAreConstant);
    }
 }
