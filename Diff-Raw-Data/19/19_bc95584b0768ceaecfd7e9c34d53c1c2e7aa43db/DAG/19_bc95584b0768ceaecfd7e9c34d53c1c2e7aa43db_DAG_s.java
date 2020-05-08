 package signature;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A directed acyclic graph that is the core data structure of a signature. It
  * is the DAG that is canonized by sorting its layers of nodes.
  * 
  * @author maclean
  *
  */
 public class DAG implements Iterable<List<DAG.Node>>{
     
     /**
      * The direction up and down the DAG. UP is from leaves to root.
      *
      */
     public enum Direction { UP, DOWN };
 	
 	/**
 	 * A node of the directed acyclic graph
 	 *
 	 */
 	public class Node implements Comparable<Node>, VisitableDAG {
 		
 		public int vertexIndex;
 		
 		public List<Node> parents;
 		
 		public List<Node> children;
 		
 		public int layer;
 		
 		/**
 		 * A label for the nodes to handle bond orders
 		 */
 		public String label;  
 		
 		/**
 		 * The final computed invariant, used for sorting children when printing
 		 */
 		public int invariant;
 		
 		public Node(int vertexIndex, int layer, String label) {
 			this.vertexIndex = vertexIndex;
 			this.layer = layer;
 			this.parents = new ArrayList<Node>();
 			this.children = new ArrayList<Node>();
 			this.label = label;
 		}
 	
 		public void addParent(Node node) {
 			this.parents.add(node);
 		}
 		
 		public void addChild(Node node) {
 			this.children.add(node);
 		}
 		
 		public void accept(DAGVisitor visitor) {
 		    visitor.visit(this);
 		}
 		
 		public String toString() {
 		    StringBuffer parentString = new StringBuffer();
 		    parentString.append('[');
 		    for (Node parent : this.parents) {
 		        parentString.append(parent.vertexIndex).append(',');
 		    }
 		    if (parentString.length() > 1) {
 		        parentString.setCharAt(parentString.length() - 1, ']');
 		    } else {
 		        parentString.append(']');
 		    }
 		    StringBuffer childString = new StringBuffer();
 		    childString.append('[');
             for (Node child : this.children) {
                 childString.append(child.vertexIndex).append(',');
             }
             if (childString.length() > 1) {
                 childString.setCharAt(childString.length() - 1, ']');    
             } else {
                 childString.append(']');
             }
             
             return vertexIndex + " " 
                  + label + " (" + parentString + ", " + childString + ")";
 		}
 
         public int compareTo(Node o) {
             int c = this.label.compareTo(o.label); 
             if (c == 0) {
                 if (this.invariant < o.invariant) {
                     return - 1;
                 } else if (this.invariant > o.invariant) {
                     return 1;
                 } else {
                     return 0;
                 }
             } else {
                 return c;
             }
         }
 	}
 	
 	/**
 	 * An arc of the directed acyclic graph
 	 *
 	 */
 	public class Arc {
 		
 		public int a;
 		
 		public int b;
 		
 		public Arc(int a, int b) {
 			this.a = a;
 			this.b = b;
 		}
 		
 		public boolean equals(Object other) {
 			if (other instanceof Arc) {
 				Arc o = (Arc) other;
 				return (this.a == o.a && this.b == o.b)
 					|| (this.a == o.b && this.b == o.a);
 			} else {
 				return false;
 			}
 		}
 	}
 	
 	/**
 	 * The layers of the DAG
 	 */
 	private List<List<Node>> layers;
 	
 	/**
 	 * The counts of parents for nodes  
 	 */
 	private Map<Integer, Integer> parentCounts;
 	
 	private Map<Integer, String> labels;
 	
 	private Invariants invariants;
 	
 	/**
 	 * Convenience reference to the nodes of the DAG
 	 */
 	private List<DAG.Node> nodes;
 	
 	/**
 	 * A convenience record of the number of vertices
 	 */
 	private int vertexCount;
 	
     /**
      * Create a DAG from a graph, starting at the root vertex.
      * 
      * @param rootVertexIndex the vertex to start from
      * @param vertexCount the number of vertices covered by this DAG
      * @param rootLabel the string label for the root vertex  
      */
 	public DAG(int rootVertexIndex, String rootLabel) {
 		this.layers = new ArrayList<List<Node>>();
 		this.nodes = new ArrayList<Node>();
 		List<Node> rootLayer = new ArrayList<Node>();
 		Node rootNode = new Node(rootVertexIndex, 0, rootLabel);
 		rootLayer.add(rootNode);
 		this.layers.add(rootLayer);
 		this.nodes.add(rootNode);
 		
 		this.labels = new HashMap<Integer, String>();
 		this.labels.put(rootVertexIndex, rootLabel);
 		this.vertexCount = 1;
 		this.parentCounts = new HashMap<Integer, Integer>();
 	}
 	
 	
     /**
      * Reset a DAG starting at another root vertex.
      * 
      * @param rootVertexIndex the vertex to start from
      * @param rootLabel the string label for the root vertex
      */
 	public void resetDAG(int rootVertexIndex, String rootLabel) {
 		this.layers.clear();
 		this.nodes.clear();
 		List<Node> rootLayer = new ArrayList<Node>();
 		Node rootNode = new Node(rootVertexIndex, 0, rootLabel);
 		rootLayer.add(rootNode);
 		this.layers.add(rootLayer);
 		this.nodes.add(rootNode);
 		
 		this.parentCounts.clear();
 		
 	}
 	
 	public Iterator<List<Node>> iterator() {
 		return layers.iterator();
 	}
 	
 	public List<DAG.Node> getRootLayer() {
 	    return this.layers.get(0);
 	}
 	
 	public DAG.Node getRoot() {
 		return this.layers.get(0).get(0);
 	}
 	
 	public Invariants copyInvariants() {
 	    return (Invariants) this.invariants.clone();
 	}
 	
 	public void initialize() {
 	    this.invariants = new Invariants();
 	    this.initializeVertexInvariants();
 	}
 	
 	public void setLabel(int vertexIndex, String label) {
 	    this.labels.put(vertexIndex, label);
 	}
 	
 	public void setColor(int vertexIndex, int color) {
 	    this.invariants.setColor(vertexIndex, color);
 	}
 	
 	public void setInvariants(Invariants invariants) {
 	    this.invariants = invariants;
 	}
 	
 	/**
 	 * Create and return a DAG.Node, while setting some internal references to
 	 * the same data. Does not add the node to a layer.
 	 * 
 	 * @param vertexIndex the index of the vertex in the original graph
 	 * @param layer the index of the layer
 	 * @param label the label of the vertex
 	 * @return the new node 
 	 */
 	public DAG.Node makeNode(int vertexIndex, int layer, String label) {
 	    DAG.Node node = new DAG.Node(vertexIndex, layer, label);
 	    this.labels.put(vertexIndex, label);
 	    this.nodes.add(node);
 	    this.vertexCount++;
 	    return node;
 	}
 	
 	/**
 	 * Create and return a DAG.Node, while setting some internal references to
      * the same data. Note: also adds the node to a layer, creating it if 
      * necessary.
      * 
 	 * @param vertexIndex the index of the vertex in the original graph
      * @param layer the index of the layer
      * @param label the label of the vertex
      * @return the new node
 	 */
 	public DAG.Node makeNodeInLayer(int vertexIndex, int layer, String label) {
         DAG.Node node = this.makeNode(vertexIndex, layer, label);
         if (layers.size() <= layer) {
           this.layers.add(new ArrayList<DAG.Node>());
         }
         this.layers.get(layer).add(node);
         this.vertexCount++;
         return node;
     }
 	
 	public void addRelation(DAG.Node childNode, DAG.Node parentNode) {
 	    childNode.parents.add(parentNode);
 	    if (parentCounts.containsKey(childNode.vertexIndex)) {
     	    int oldCount = this.parentCounts.get(childNode.vertexIndex);
     	    this.parentCounts.put(childNode.vertexIndex, oldCount + 1);
 	    } else {
 	        this.parentCounts.put(childNode.vertexIndex, 1);
 	    }
 	    parentNode.children.add(childNode);
 	}
 	
 	public List<InvariantIntIntPair> getInvariantPairs() {
 	    List<InvariantIntIntPair> pairs = new ArrayList<InvariantIntIntPair>();
 	    for (int i = 0; i < this.vertexCount; i++) {
 	        if (invariants.getColor(i) == 0 
 	                && (parentCounts.containsKey(i) && 
 	                        parentCounts.get(i) >= 2)) {
 	            pairs.add(
 	                    new InvariantIntIntPair(
 	                            invariants.getVertexInvariant(i), i));
 	        }
 	    }
 	    Collections.sort(pairs);
 	    return pairs;
 	}
 	
 	public int colorFor(int vertexIndex) {
 		return this.invariants.getColor(vertexIndex);
 	}
 	
 	public void accept(DAGVisitor visitor) {
 	    this.getRoot().accept(visitor);
 	}
 
 	public void addLayer(List<Node> layer) {
 		this.layers.add(layer);
 	}
 	
 	public void initializeVertexInvariants() {
 	    List<InvariantIntStringPair> pairs = 
 	        new ArrayList<InvariantIntStringPair>();
 	    for (int i = 0; i < this.vertexCount; i++) {
 	        String l = this.labels.get(i);
 	        int p;
 	        if (parentCounts.containsKey(i)) {
 	            p = this.parentCounts.get(i);
 	        } else {
 	            p = 0;
 	        }
 	        pairs.add(new InvariantIntStringPair(l, p, i));
 	    }
 	    Collections.sort(pairs);
 	    
 	    if (pairs.size() == 0) return;
 	    
 	    int order = 1;
 	    this.invariants.setVertexInvariant(pairs.get(0).originalIndex, order);
 	    for (int i = 1; i < pairs.size(); i++) {
 	        InvariantIntStringPair a = pairs.get(i - 1);
 	        InvariantIntStringPair b = pairs.get(i);
 	        if (!a.equals(b)) {
 	            order++;
 	        }
 	        this.invariants.setVertexInvariant(b.originalIndex, order);
 	    }
 	}
 	
 	public List<Integer> createOrbit() {
 	    int maxOrbitSize = 0;
 	    int invariantChoice = 0;
 	    List<Integer> orbit = new ArrayList<Integer>();
 	    
 	    // Vertex invariants range from 1 to the number of vertices.
 	    for (int invariant = 1; invariant <= this.vertexCount; invariant++) {
 	        int orbitSize = 0;
 	        for (int j = 0; j < this.vertexCount; j++) {
 	            if (invariants.getVertexInvariant(j) == invariant 
 	                    && (parentCounts.containsKey(j) 
 	                            && parentCounts.get(j) >= 2)) {
 	                orbitSize++;
 	            }
 	        }
 	    // Pick the orbit with the lowest invariant 
 	    // if there is a tie between orbits.
 	        if (maxOrbitSize < orbitSize) { 
 	            maxOrbitSize = orbitSize;
 	            invariantChoice = invariant;
 	        }
 	    }
 	    
 	    for (int k = 0; k < vertexCount; k++) {
 	        if (invariants.getVertexInvariant(k) == invariantChoice) {
 	            orbit.add(k);
 	        }
 	    }
 	    return orbit;
 	}
 	
 	public void computeVertexInvariants() {
	    int[][] layerInvariants = new int[this.vertexCount][];
 	    for (int i = 0; i < this.nodes.size(); i++) {
 	        DAG.Node node = this.nodes.get(i);
 	        int j = node.vertexIndex;
	        if (layerInvariants[j] == null) {
	            layerInvariants[j] = new int[this.layers.size()];
 	        }
	        layerInvariants[j][node.layer] = invariants.getNodeInvariant(i); 
 	    }
 	    
 	    List<InvariantArray> invariantLists = new ArrayList<InvariantArray>();
	    for (int i = 0; i < this.vertexCount; i++) {
	        InvariantArray invArr = new InvariantArray(layerInvariants[i], i); 
 	        invariantLists.add(invArr);
 	    }
 	    Collections.sort(invariantLists);
 	    
 	    int order = 1;
 	    int first = invariantLists.get(0).originalIndex;
 	    invariants.setVertexInvariant(first, 1);
 	    for (int i = 1; i < invariantLists.size(); i++) {
 	        InvariantArray a = invariantLists.get(i - 1);
 	        InvariantArray b = invariantLists.get(i);
 	        if (!a.equals(b)) {
 	            order++;
 	        }
 	        invariants.setVertexInvariant(b.originalIndex, order);
 	    }
 
 	}
 	
 	public void updateVertexInvariants() {
 	    Map<Integer, Integer> oldInvariants = new HashMap<Integer, Integer>();
 	    boolean invariantSame = true;
 	    while (invariantSame) {
 	        oldInvariants = invariants.getVertexInvariantCopy();
 	        
 	        updateNodeInvariants(Direction.UP); // From the leaves to the root.
 	        computeVertexInvariants(); // This is needed here otherwise there will be cases where a node invariant is reset when the tree is traversed down. This is not mentioned in Faulon's paper.
 	        
 	        updateNodeInvariants(Direction.DOWN); // From the root to the leaves.
 	        computeVertexInvariants();
 	        
 	        invariantSame = 
 	            checkInvariantChange(
 	                    oldInvariants, invariants.getVertexInvariants());
 	        
 	    }
 	    
 	    // finally, copy the node invariants into the nodes, for easy sorting
 	    for (int i = 0; i < this.nodes.size(); i++) {
 	        this.nodes.get(i).invariant = invariants.getNodeInvariant(i);
 	    }
 	}
 	
 	public boolean checkInvariantChange(
 	        Map<Integer, Integer> a, Map<Integer, Integer> b) {
 	    for (int k : a.keySet()) {
 	        if (a.get(k) != b.get(k)) {
 	            return true;
 	        }
         }
 	    return false;
 	}
 	
 	public void updateNodeInvariants(DAG.Direction direction) {
 	    int start, end, increment;
 	    if (direction == Direction.UP) {
 	        start = this.layers.size() - 1; 
 	        end = 0; // The root node is not included but it doesn't matter since it always is alone.
 	        increment = -1;
 	    } else {
 	        start = 0;
 	        end = this.layers.size()-1; // We do not include the leaf layer, perhaps we should. Does it matter?
 	        increment = 1;
 	    }
 	    
         for (int i = start; i != end; i += increment) {
            this.updateLayer(this.layers.get(i), direction);
         }
         
 	}
 	
 	public void updateLayer(List<DAG.Node> layer, DAG.Direction direction) {
 	    List<InvariantList> nodeInvariantList = 
             new ArrayList<InvariantList>();
         for (int i = 0; i < layer.size(); i++) {
             DAG.Node layerNode = layer.get(i);
             int x = layerNode.vertexIndex;
             InvariantList nodeInvariant = new InvariantList(nodes.indexOf(layerNode));
             nodeInvariant.add(this.invariants.getColor(x));
             nodeInvariant.add(this.invariants.getVertexInvariant(x));
             
             List<Integer> relativeInvariants = new ArrayList<Integer>();
 
             // If we go up we should check the children.
             List<DAG.Node> relatives = (direction == Direction.UP) ? 
                     layerNode.children : layerNode.parents;
             for (Node relative : relatives ){
                 int j = this.nodes.indexOf(relative);
             	relativeInvariants.add(this.invariants.getNodeInvariant(j));
             }
             Collections.sort(relativeInvariants);
             nodeInvariant.addAll(relativeInvariants);
             nodeInvariantList.add(nodeInvariant);
         }
         
         Collections.sort(nodeInvariantList);
         
         int order = 1;
         int first = nodeInvariantList.get(0).originalIndex;
         this.invariants.setNodeInvariant(first, order);
         for (int i = 1; i < nodeInvariantList.size(); i++) {
             InvariantList a = nodeInvariantList.get(i - 1);
             InvariantList b = nodeInvariantList.get(i);
             if (!a.equals(b)) {
                 order++;
             }
             this.invariants.setNodeInvariant(b.originalIndex, order);
         }
 	}
 	
 	public String toString() {
 		StringBuffer buffer = new StringBuffer();
 		for (List<Node> layer : this) {
 			buffer.append(layer);
 			buffer.append("\n");
 		}
 		return buffer.toString();
 	}
 }
