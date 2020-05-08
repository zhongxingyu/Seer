 package dna.updates.generators.evolvingNetworks;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Random;
 
 import dna.graph.Graph;
 import dna.graph.IElement;
 import dna.graph.nodes.DirectedNode;
 import dna.graph.nodes.Node;
 import dna.graph.nodes.UndirectedNode;
 import dna.updates.batch.Batch;
 import dna.updates.generators.BatchGenerator;
 import dna.updates.update.EdgeAddition;
 import dna.updates.update.NodeAddition;
 import dna.util.Rand;
 import dna.util.parameters.DoubleParameter;
 import dna.util.parameters.IntParameter;
 
 
 /**
  * Implements the Positive-feedback Preference network model for rich-club networks.
  * Follows the algorithm in: <br>
  *  The Positive-Feedback Preference Model of the AS-level Internet Topology (Zhou, Mondragon @ ICC'05)    
  * @author Tim
  *
  */
 public class PositiveFeedbackPreferenceBatch extends BatchGenerator {
 
 	private int nodes;
 
 	// PFP probabilities
 	private double deltaPFP;
 	private double pPFP;
 	private double qPFP;
 
 	/**
 	 * Adds <b>nodes</b> to the graph <br>
 	 * initialized with the standard, in the paper mentioned parameters: <br>
 	 * <b>p</b>=0.3
 	 * <b>q</b>=0.1
 	 * <b>delta</b>=0.048
 	 * @param nodes number of nodes added with this batch
 	 */
 	public PositiveFeedbackPreferenceBatch(int nodes) {
 		super("PositiveFeedbackPreferenceBatch", 
 				new IntParameter("nodes", nodes), 
 				new DoubleParameter("delta", 0.048), 
 				new DoubleParameter("p", 0.3), 
 				new DoubleParameter("q", 0.1));
 		this.nodes = nodes;
 		pPFP = 0.3;
 		qPFP = 0.1;
 		deltaPFP = 0.048;
 	}
 	
 	/**
 	 * Adds <b>nodes</b> to the graph <br>
 	 * @param nodes number of nodes added with this batch
 	 * @param delta parameter from paper 
 	 * @param p parameter from paper
 	 * @param q parameter from paper
 	 */
 	public PositiveFeedbackPreferenceBatch(int nodes, double delta, double p, double q) {
 		super("PositiveFeedbackPreferenceBatch", 
 				new IntParameter("nodes", nodes),
 				new DoubleParameter("delta", delta), 
 				new DoubleParameter("p", p), 
 				new DoubleParameter("q", q));
 		this.nodes = nodes;
 		this.deltaPFP = delta;
 		this.pPFP = p;
 		this.qPFP = q;
 	}
 
 	@Override
 	public Batch generate(Graph g) {
 		Batch b = new Batch(g.getGraphDatastructures(), g.getTimestamp(),
 				g.getTimestamp() + 1, this.nodes, 0, 0, this.nodes, 0,
 				0);
 		
 		growGraph(g, b);
 
 		return b;
 	}
 
 	private void growGraph(Graph g, Batch b) {
 		// new nodes can be targets for later nodes
 		Node[] newNodes = new Node[this.nodes];
 		int[] newDegrees = new int[this.nodes];
 		Arrays.fill(newDegrees, 0);
 		
 		// to avoid the recurring casts and degree computation
 		int[] oldDegrees = collectOldDegrees(g);
 		int maxOldId = g.getMaxNodeIndex();
 		
 
 		// add node by node
 		for (int i = 0; i < this.nodes; i++) {
 			// add new node
 			newNodes[i] = g.getGraphDatastructures().newNodeInstance(
 					g.getMaxNodeIndex() + 1 + i);
 			b.add(new NodeAddition(newNodes[i]));
 
 			
 			// add links of the new node
 			double pi = Rand.rand.nextDouble();			
 			int noIgnoredNode = -1;			
 			if (pi <= 1 - pPFP - qPFP){
 				// new node: 2 links to host nodes
 				// 1 of the host nodes: 1 link to a peer
 				
 				// calculate destination 1 - no need to ignore a node
 				Node to1 = getNPPNode(g, oldDegrees, newNodes, newDegrees, 
 						maxOldId, newNodes[i].getIndex(), noIgnoredNode, newNodes[i].getIndex());
 				
 				// calculate destination 2 - ignore destination 1
 				Node to2 = getNPPNode(g, oldDegrees, newNodes, newDegrees, 
 						maxOldId, newNodes[i].getIndex(), to1.getIndex(), newNodes[i].getIndex());
 				
 				// choose destination 1 or destination 2 randomly to be the source of the third edge
 				Node host = (Rand.rand.nextDouble() < 0.5) ? to1 : to2;
 				// calculate destination 3, ignore only the host/chosen destination/2
 				Node peer = getNPPNode(g, oldDegrees, newNodes, newDegrees, maxOldId, 
 						newNodes[i].getIndex(), host.getIndex(), host.getIndex());
 				
 				// add EdgeAdditions to batch
 				b.add(new EdgeAddition(g.getGraphDatastructures().newEdgeInstance(newNodes[i], to1)));
 				b.add(new EdgeAddition(g.getGraphDatastructures().newEdgeInstance(newNodes[i], to2)));
 				b.add(new EdgeAddition(g.getGraphDatastructures().newEdgeInstance(host, peer)));
 				
 				// increase degree of chosen nodes
 				increaseDegree(oldDegrees, newDegrees, maxOldId, new Node[]{to1, to2, host, peer, newNodes[i]});
 			} else if (pi <= 1 - pPFP){
 				// new node: 1 link to a host node
 				// host node: 2 links to peers
 				
 				// calulate destination - no need to ignore a node
 				Node to = getNPPNode(g, oldDegrees, newNodes, newDegrees, 
 						maxOldId, newNodes[i].getIndex(), noIgnoredNode, newNodes[i].getIndex());
 				b.add(new EdgeAddition(g.getGraphDatastructures().newEdgeInstance(newNodes[i], to)));
 				
 				
 				// calculate the two peers to connect the first destination with.
 				Node peer1 = null;
 				Node peer2 = null;
 				for(int j=0; j<2; j++){
 					Node peer = getNPPNode(g, oldDegrees, newNodes, newDegrees, 
 							maxOldId, newNodes[i].getIndex(), to.getIndex(), to.getIndex());
 					if(j==0)
 						peer1 = peer;
 					else
 						peer2 = peer;
 					
 					// add edge only if:
 					// - first edge is created
 					// - second edge is created and second target is different from first one
 					if(peer2 != null && peer2.deepEquals(peer1)){
 						j--;
 					} else {
 						b.add(new EdgeAddition(g.getGraphDatastructures().newEdgeInstance(to, peer)));
 					}
 				}
 				
 				// increase degree of chosen nodes
 				increaseDegree(oldDegrees, newDegrees, maxOldId, new Node[]{to, peer1, peer2, newNodes[i]});
 			} else if (pi <= 1){
 				// new node: 1 link to a host node
 				// host node: 1 link to a peer
 				
 				// calculate destination node - no need to ignore a node
 				Node to = getNPPNode(g, oldDegrees, newNodes, newDegrees, 
 						maxOldId, newNodes[i].getIndex(), noIgnoredNode, newNodes[i].getIndex());
 				b.add(new EdgeAddition(g.getGraphDatastructures().newEdgeInstance(newNodes[i], to)));
 				
 				// calculate the peer to connect the destination with
 				Node peer = getNPPNode(g, oldDegrees, newNodes, newDegrees,
 						maxOldId, newNodes[i].getIndex(), to.getIndex(), to.getIndex());
 				b.add(new EdgeAddition(g.getGraphDatastructures().newEdgeInstance(to, peer)));
 				
 				// increase degree of chosen node
 				increaseDegree(oldDegrees, newDegrees, maxOldId, new Node[]{to, peer, newNodes[i]});
 			}
 		}
 	}
 	
 	/**
 	 * increase the degree of the provided nodes by one
 	 * @param oldDegrees array with degrees of the in g contained nodes
 	 * @param newDegrees array with degrees of the in this batch added nodes
 	 * @param maxOldIndex maximum index of nodes in g (to get the switch to the newDegree array)
 	 * @param nodes nodes with changed degree
 	 */
 	private void increaseDegree(int[] oldDegrees, int[] newDegrees, int maxOldIndex,
 			Node[] nodes) {
 		for(Node n : nodes){
 			if(n.getIndex() <= maxOldIndex) {
 				oldDegrees[n.getIndex()] += 1;
 			} else {
 				newDegrees[n.getIndex()-maxOldIndex-1] +=1; 
 			}
 		}
 		
 	}
 
 	/**
 	 * Iterate over all nodes and calculate a valid one.
 	 * Probability formula is given by the paper:
 	 * 
 	 * @param g	Graph to retrieve the old nodes
 	 * @param oldDegrees	degrees of the old nodes
 	 * @param newNodes		array with new nodes
 	 * @param newDegrees	degrees of the new nodes
 	 * @param maxOldId		last old node index, nodes with higher index are new nodes
 	 * @param maxId			id of the last added nodes (highest id)
 	 * @param ignoreNode	node, that should be ignored as destination for this edge
 	 * @param srcNode		source node of this edge
 	 * @return				destination node
 	 */
 	private Node getNPPNode(Graph g, int[] oldDegrees, Node[] newNodes, int[] newDegrees, int maxOldId, int maxId,
 			int ignoreNode, int srcNode) {
 		
 		int npp = -1;		
 		double sumK = 0;
 		
 		// sum on old nodes
 		for (int j = 0; j <= maxOldId; j++){ 
 			int kj = oldDegrees[j];
			sumK += Math.pow(kj,  calcExponent(kj));
 		}
 		// sum on new nodes
 		for(int j = 0; j < (maxId-maxOldId-1); j++){
 			int kj = newDegrees[j];
 			sumK += Math.pow(kj, calcExponent(kj));
 		}
 		
 		double sumK2 = 0;
 		
 		
 		// get the source node, if srcNode id is > maxOldId, then is the src a new node and has to be retrieved from the new node array
 		Node src = (srcNode <= maxOldId) ? g.getNode(srcNode) : newNodes[srcNode-maxOldId-1];
 		
 		// iterate over all nodes ("old" and in this batch added) to find a destination for this edge
 		while(npp <0){
 			double takeNode = Rand.rand.nextDouble();
 			// check old nodes for valid destination
 			for(int k = 0; k <= maxOldId; k++){
 				int kj = oldDegrees[k];
				sumK2 += Math.pow(kj, calcExponent(kj)) / sumK;
 				
 				Node dst = g.getNode(k);
 				if(sumK2 > takeNode && 
 						k != srcNode && 
 						k != ignoreNode && 
 						!g.containsEdge(g.getGraphDatastructures().newEdgeInstance(src, dst))){
 					return dst;
 				}
 			}
 			// check new nodes for valid destination
 			for(int k = 0; k <= maxId-maxOldId-1; k++){
 				int kj = newDegrees[k];
				sumK2 += Math.pow(kj, calcExponent(kj)) / sumK;
 				
 				Node dst = newNodes[k];
 				
 				if(sumK2 > takeNode && 
 						k+maxOldId+1 != srcNode &&
 						k+maxOldId+1 != ignoreNode && 
 						!g.containsEdge(g.getGraphDatastructures().newEdgeInstance(src, dst))){
 					return dst;
 				}
 			}
 		}
 		// should not be reachable!
 		return null;
 	}
 
 	/**
 	 * calculates the exponent of the possibility equation
 	 * @param kj
 	 * @return
 	 */
 	private double calcExponent(int kj) {
		if(kj==0)	// TODO not correct! how to handle isolated nodes?
			return 0;
		
 		double exp;
 		
 		double logi = Math.log10(kj);
 		
 		exp = 1 + (deltaPFP * logi);
 		
 		return exp;
 	}
 
 	/**
 	 * collects the degrees of the nodes in g
 	 * @param g	current graph
 	 * @return	array of node-degrees
 	 */
 	private int[] collectOldDegrees(Graph g) {
 		int[] oldDegrees = new int[g.getNodeCount()];
 		for(IElement n : g.getNodes()){
 			Node node = (Node) n;
 			if(node instanceof DirectedNode){
 				oldDegrees[node.getIndex()] = ((DirectedNode) node).getDegree();
 			} else if (node instanceof UndirectedNode) {
 				oldDegrees[node.getIndex()] = ((UndirectedNode) node).getDegree();
 			}
 		}
 		return oldDegrees;
 	}
 
 	@Override
 	public void reset() {
 	}
 
 	@Override
 	public boolean isFurtherBatchPossible(Graph g) {
 		return true;
 	}
 
 }
