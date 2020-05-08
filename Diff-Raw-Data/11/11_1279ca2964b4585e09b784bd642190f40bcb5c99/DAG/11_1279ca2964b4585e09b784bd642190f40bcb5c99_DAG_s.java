 package signature;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A directed acyclic graph that is the core data structure of a signature. It
  * is the DAG that is canonized by sorting its layers of nodes.
  * 
  * @author maclean
  *
  */
 public class DAG implements Iterable<List<DAG.Node>>{
 	
 	/**
 	 * A node of the directed acyclic graph
 	 *
 	 */
 	public class Node {
 		
 		public int vertexIndex;
 		
 		public ISignatureVertex vertex;
 		
 		public List<Node> parents;
 		
 		public List<Node> children;
 		
 		public Node(ISignatureVertex vertex) {
 			this.vertex = vertex;
 			this.parents = new ArrayList<Node>();
 			this.children = new ArrayList<Node>();
 		}
 		
 		public ISignatureVertex getVertex() {
 			return this.vertex;
 		}
 		
 		public void addParent(Node node) {
 			this.parents.add(node);
 		}
 		
 		public void addChild(Node node) {
 			this.children.add(node);
 		}
 	}
 	
 	/**
 	 * An arc of the directed acyclic graph
 	 *
 	 */
 	public class Arc {
 		
 		public int vertexIndexA;
 		
 		public int vertexIndexB;
 		
 		public Arc(int a, int b) {
 			this.vertexIndexA = a;
 			this.vertexIndexB = b;
 		}
 	}
 	
 	/**
 	 * The graph that the DAG is built from
 	 */
 	private ISignatureGraph graph;
 	
 	/**
 	 * The layers of the DAG
 	 */
 	private List<List<Node>> layers;
 	
 	/**
 	 * Create a DAG from a graph, starting at the root vertex.
 	 * @param graph the signature graph wrapper instance
 	 * @param rootVertex the vertex to start from
 	 */
 	public DAG(ISignatureGraph graph, ISignatureVertex rootVertex) {
 		this.graph = graph;
 		this.layers = new ArrayList<List<Node>>();
 		List<Node> rootLayer = new ArrayList<Node>();
 		rootLayer.add(new Node(rootVertex));
 		buildLayer(rootLayer, new ArrayList<Arc>());
 	}
 	
 	public Iterator<List<Node>> iterator() {
 		return layers.iterator();
 	}
 	
 	public int colorFor(int vertexIndex) {
 		return 0;	//TODO
 	}
 
 	private void buildLayer(List<Node> previousLayer, List<Arc> usedArcs) {
 		List<Node> nextLayer = new ArrayList<Node>();
 		List<Arc> layerArcs = new ArrayList<Arc>();
 		for (Node node : previousLayer) {
 			ISignatureVertex vertex = graph.getVertex(node);
 			for (ISignatureVertex connectedVertex : graph.getConnected(vertex)) {
 				addNode(node, connectedVertex, layerArcs, usedArcs, nextLayer);
 			}
 		}
 		if (nextLayer.isEmpty()) {
 			return;
 		} else {
 			layers.add(nextLayer);
 		}
 	}
 
 	private void addNode(Node node, ISignatureVertex vertex,
 			List<Arc> layerArcs, List<Arc> usedArcs, List<Node> nextLayer) {
 		Arc arc = new Arc(node.vertexIndex, -1);	//TODO
 		if (usedArcs.contains(arc)) return;
 		Node existingNode = null;
 		for (Node otherNode : nextLayer) {
 			if (otherNode.getVertex() == vertex) {
 				existingNode = otherNode;
 				break;
 			}
 		}
 		if (existingNode == null) {
 			existingNode = new Node(vertex);
 			nextLayer.add(existingNode);
 		}
 		node.addChild(existingNode);
 		existingNode.addParent(node);
 		layerArcs.add(arc);
 	}
 }
