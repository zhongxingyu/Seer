 package signature;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Class to reconstruct a graph from a signature string (or a colored tree).
  * 
  * @author maclean
  *
  */
 public abstract class AbstractGraphBuilder {
     
     private Map<Integer, Integer> colorToVertexIndexMap;
     
     private int vertexCount;
     
     public AbstractGraphBuilder() {
         this.colorToVertexIndexMap = new HashMap<Integer, Integer>();
         this.vertexCount = 0;
     }
     
     public void makeFromColoredTree(ColoredTree tree) {
         this.makeGraph();
         ColoredTree.Node root = tree.getRoot();
         this.makeVertex(root.label);
         this.vertexCount = 1;
         for (ColoredTree.Node child : root.children) {
             this.makeFromColoredTreeNode(root, child, 0);
         }
     }
     
     private void makeFromColoredTreeNode(
             ColoredTree.Node parent, ColoredTree.Node node, int parentIndex) {
         int vertexIndex;
         if (node.isColored()) {
             if (this.colorToVertexIndexMap.containsKey(node.color)) {
                 vertexIndex = this.colorToVertexIndexMap.get(node.color);
             } else {
                 this.makeVertex(node.label);
                 this.vertexCount++;
                 vertexIndex = this.vertexCount - 1;
                 this.colorToVertexIndexMap.put(node.color, vertexIndex);
             }
         } else {
             this.makeVertex(node.label);
             this.vertexCount++;
             vertexIndex = this.vertexCount - 1;
         }
         
         this.makeEdge(parentIndex, vertexIndex, parent.label, node.label, node.edgeLabel);
         for (ColoredTree.Node child : node.children) {
             this.makeFromColoredTreeNode(node, child, vertexIndex);
         }
     }
     
     /**
      * Make the initial, empty, graph to be filled. It is up to the 
      * implementing class to store the graph instance.
      */
     public abstract void makeGraph();
     
     /**
      * Make a vertex in the graph with label <code>label</code>.
      *  
      * @param label the string label to use
      */
     public abstract void makeVertex(String label);
     
     /**
      * Make an edge between the two vertices indexed by 
      * <code>vertexIndex1</code> and <code>vertexIndex2</code>.
      * 
      * @param vertexIndex1 the index of the first vertex in the graph
      * @param vertexIndex2 the index of the second vertex in the graph
      */
     public abstract void makeEdge(int vertexIndex1, int vertexIndex2,
             String vertexSymbol1, String vertexSymbol2, String edgeLabel);
 }
