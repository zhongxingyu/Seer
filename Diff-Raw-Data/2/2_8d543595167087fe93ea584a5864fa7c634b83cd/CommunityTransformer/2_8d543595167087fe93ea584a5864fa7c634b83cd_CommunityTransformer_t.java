 package stat.comm;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import model.graph.Edge;
 import model.graph.Graph;
 import stat.IStatTransformer;
 import stat.util.AdjacencyList;
 import Jama.EigenvalueDecomposition;
 import Jama.Matrix;
 
 /**
  * Algorithm reference: (among others; information may be redundant)
  * 		http://arxiv.org/pdf/physics/0605087v3.pdf
  * 		http://vw.indiana.edu/netsci06/conf-slides/conf-mon/netsci-talk-mark-newman.pdf
  * 		http://en.wikipedia.org/wiki/Modularity_(networks)
  * 
 * Note: the runtime on this is pretty terrible (something on the order of O((n^2)log(n)))
  * There is also a greedy version of modularity maximization developed by Clauset, Newman
  * and Moore that runs in O(nlog^2(n)). However, it is much more inaccurate and difficult
  * to implement.
  * 
  * Since our graphs will not usually exceed 100-ish vertices, this method should work. We
  * might have to use another method if our graphs get much bigger.
  */
 public class CommunityTransformer implements IStatTransformer<Dendrogram>{
 	
 	public CommunityTransformer() {
 		
 	}
 	
 	public static void main(String[] args) {
 		
         Graph g = new Graph("Community Test");
         g.addVertex("A");
         g.addVertex("B");
         g.addVertex("C");
         g.addVertex("D");
         g.addVertex("E");
         g.addVertex("F");
         g.addVertex("G");
         g.addVertex("H");
         g.addVertex("I");
         g.addVertex("J");
         g.addVertex("K");
         g.addEdge("A","D",Edge.UNDIRECTED);
         g.addEdge("A","E",Edge.UNDIRECTED);
         g.addEdge("A","H",Edge.UNDIRECTED);
         g.addEdge("B","C",Edge.UNDIRECTED);
         g.addEdge("B","G",Edge.UNDIRECTED);
         g.addEdge("B","I",Edge.UNDIRECTED);
         g.addEdge("C","D",Edge.UNDIRECTED);
         g.addEdge("C","G",Edge.UNDIRECTED);
         g.addEdge("C","I",Edge.UNDIRECTED);
         g.addEdge("D","E",Edge.UNDIRECTED);
         g.addEdge("D","H",Edge.UNDIRECTED);
         g.addEdge("E","F",Edge.UNDIRECTED);
         g.addEdge("E","H",Edge.UNDIRECTED);
         g.addEdge("F","J",Edge.UNDIRECTED);
         g.addEdge("F","K",Edge.UNDIRECTED);
         g.addEdge("G","I",Edge.UNDIRECTED);
         g.addEdge("J","K",Edge.UNDIRECTED);
         
         Dendrogram communities = new CommunityTransformer().transform(g, "3");
         System.out.println(communities);
 	}
 	
 	@Override
 	public Dendrogram transform(Graph g,String...args) {
 		/* The maximum number of iterations for this algorithm */
 		int maxIter;
 		if (args.length > 0) {
 			maxIter = Integer.parseInt(args[0]);
 		} else {
 			/* The default value is effectively an unlimited number of
 			 * partitions. */
 			maxIter = Integer.MAX_VALUE;
 		}
 		/* Adapt the graph to a faster adjacency list format */
 		AdjacencyList adj = g.adaptTo(AdjacencyList.class);
 		/* Compute the modularity matrix from the adj. list */
 		Matrix Q_mat = computeModMatrix(adj);
 		/* Create the dendrogram object with all vertices */
 		Dendrogram root = new Dendrogram(g.vertices());
 		/* Initialize the breadth-first queue */
 		Queue<Dendrogram> queue = new LinkedList<Dendrogram>();
 		/* Add the root to the queue */
 		queue.add(root);
 		/* iterCount will keep track of the number of split operations
 		 * that occur. If the maximum number is reached, the algorithm
 		 * will terminate. */
 		int iterCount = 0;
 		/* This is the main loop of the algorithm. While communities can
 		 * still be divided, perform breadth-first splitting of each comm-
 		 * unity and add its children to the queue if the split was 
 		 * successful. */
 		while(!queue.isEmpty()) {
 			if (iterCount < maxIter) {
 				/* Take the first community from the queue and try to split it */
 				Dendrogram[] children = split(queue.remove(),adj,Q_mat);
 				iterCount++;
 				/* If the split was successful, add the subcommunities to the 
 				 * queue. Otherwise, the community is indivisible */
 				if (children != null) {
 					queue.add(children[0]);
 					queue.add(children[1]);
 				}
 			} else {
 				break;
 			}
 		}		
 		/* Finally, return the binary dendrogram */
 		return root;
 	}
 	
 	private Dendrogram[] split(Dendrogram root, AdjacencyList adj, Matrix Q_mat) {
 		/* You only split nodes that have no children (aka. leaves) */
 		if (root.isLeaf()) {
 			/* First, find the indices of the vertices in root */
 			int[] indices = new int[root.size()];
 			int count = 0;
 			for (String name : root.names) {
 				indices[count] = adj.getIndex(name);
 				count++;
 			}
 			/* Use these indices to obtain a modularity matrix of 
 			 * only the subgraph */
 			Matrix Q_sub = Q_mat.getMatrix(indices, indices);
 			/* Calculate eigenvalues and their corresponding eigenvectors */
 			EigenvalueDecomposition decomp = Q_sub.eig();
 			double[] eigenvalues = decomp.getRealEigenvalues();
 			double[][] eigenvectors = decomp.getV().getArray();
 			/* If the leading eigenvalue is 0 or negative, the index will not
 			 * be reset, and the cluster will be considered as one community */
 			double greatest = 0.001;
 			int indexOfGreatest = -1;
 			/* Search for the leading eigenvalue */
 			for (int i = 0; i < eigenvalues.length; i++) {
 				if (eigenvalues[i] > greatest) {
 					indexOfGreatest = i;
 					greatest = eigenvalues[i];
 				}
 			}
 			/* If the search was successful... */
 			if (indexOfGreatest != -1) {
 				/* These lists will hold the indices (note: NOT the subgraph
 				 * indices) of each child communities's vertices */
 				List<Integer> toFirst = new LinkedList<Integer>();
 				List<Integer> toSecond = new LinkedList<Integer>();
 				/* We walk through the leading eigenvector. Indices with positive
 				 * values are sorted into the first community, and indices with
 				 * negative values are sorted into the second. Indices with values
 				 * close to zero are not partitioned */
 				for (int i = 0; i < indices.length; i++) {
 					double val = eigenvectors[i][indexOfGreatest];
 					if (val > 0.01) {
 						toFirst.add(indices[i]);
 					} else if (val < -0.01){
 						toSecond.add(indices[i]);
 					}
 				}
 				/* If either of the children are empty, only one community
 				 * exists, so the cluster of vertices is indivisible */
 				if (toFirst.isEmpty() || toSecond.isEmpty()) {
 					/* Hence, we return null */
 					return null;
 				/* Two separate sub-communities for this partition, so
 				 * we add their IDs to the children of the root */
 				} else {
 					for (Integer j : toFirst) {
 						root.addFirst(adj.getID(j));
 					}
 					for (Integer j : toSecond) {
 						root.addSecond(adj.getID(j));
 					}
 				}
 			/* No maximum eigenvalue was found, so we consider the cluster of 
 			 * vertices to be indivisible and return null. */
 			} else {
 				return null;
 			}
 		}
 		return root.getChildren();
 	}
 	
 	/* Compute the modularity matrix corresponding to this adjacency list.
 	 * This operation should only have to be performed once per graph. */
 	private Matrix computeModMatrix(AdjacencyList adj) {
 		Matrix B = new Matrix(adj.getVertexCount(),adj.getVertexCount());
 		for (int i = 0; i < adj.getVertexCount(); i++) {
 			for (int c = 0; c <= i; c++ ) {
 				double mod = adj.weightOf(i,c)-((double)adj.getDegree(i)*(double)adj.getDegree(c))/(2.0*adj.getEdgeCount());
 				B.set(i, c, mod);
 				B.set(c, i, mod);
 			}
 		}
 		return B;
 	}
 
 }
