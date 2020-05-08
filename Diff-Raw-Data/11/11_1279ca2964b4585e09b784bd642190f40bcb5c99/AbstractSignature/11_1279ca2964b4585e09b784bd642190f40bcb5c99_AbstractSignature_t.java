 package signature;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class AbstractSignature {
 	
 	public static final char START_BRANCH_SYMBOL = '(';
 	
 	public static final char END_BRANCH_SYMBOL = ')';
 	
 	private final String startNodeSymbol;
 	
 	private final String endNodeSymbol;
 	
 	private DAG dag;
 
 	public AbstractSignature() {
 		this.startNodeSymbol = "";
 		this.endNodeSymbol = "";
 	}
 	
 	public AbstractSignature(String startNodeSymbol, String endNodeSymbol) {
 		this.startNodeSymbol = startNodeSymbol;
 		this.endNodeSymbol = endNodeSymbol;
 	}
 	
 	/**
 	 * Get the symbol to use in the output signature string for this vertex of 
 	 * the input graph.
 	 *  
 	 * @param vertexIndex the index of the vertex in the input graph
 	 * @return a String symbol
 	 */
 	public abstract String getSymbol(int vertexIndex);
 	
 	/**
 	 * Get a list of the indices of the vertices connected to the vertex with 
 	 * the supplied index.
 	 * 
 	 * @param vertexIndex the index of the vertex to use
 	 * @return the indices of connected vertices in the input graph
 	 */
 	public abstract int[] getConnected(int vertexIndex);
 	
 	/**
 	 * Get the symbol (if any) for the edge between the vertices with these two
 	 * indices.
 	 * 
 	 * @param vertexIndex the index of one of the vertices in the edge
 	 * @param otherVertexIndex the index of the other vertex in the edge 
 	 * @return a string symbol for this edge
 	 */
 	public abstract String getEdgeSymbol(int vertexIndex, int otherVertexIndex);
 	
 	public String toString() {
		DAG.Node root = this.dag.getRoot();
 		StringBuffer buffer = new StringBuffer();
 		print(buffer, root, null, new ArrayList<DAG.Arc>());
 		return buffer.toString();
 	}
 	
 	/**
 	 * Recursively print the signature into the buffer.
 	 * 
 	 * @param buffer the string buffer to print into
 	 * @param node the current node of the signature
 	 * @param parent the parent node, or null
 	 * @param arcs the list of already visited arcs
 	 */
 	private void print(StringBuffer buffer, DAG.Node node,
 			DAG.Node parent, List<DAG.Arc> arcs) {
 		
 		// print out any symbol for the edge in the input graph
 		if (parent != null) {
 			buffer.append(getEdgeSymbol(node.vertexIndex, parent.vertexIndex));
 		}
 		
 		// print out the text that represents the node itself
 		buffer.append(this.startNodeSymbol);
 		buffer.append(getSymbol(node.vertexIndex));
 		int color = dag.colorFor(node.vertexIndex);
 		if (color != 0) {
 			buffer.append(',').append(color);
 		}
 		buffer.append(this.endNodeSymbol);
 		
 		// now print any children, surrounded by branch symbols
 		boolean addedBranchSymbol = false;
 		for (DAG.Node child : node.children) {
 			DAG.Arc arc = dag.new Arc(parent.vertexIndex, node.vertexIndex);
 			if (arcs.contains(arc)) {
 				continue;
 			} else {
 				if (!addedBranchSymbol) {
 					buffer.append(AbstractSignature.START_BRANCH_SYMBOL);
 					addedBranchSymbol = true;
 				}
 				arcs.add(arc);
 				print(buffer, child, node, arcs);
 			}
 		}
 		if (addedBranchSymbol) {
 			buffer.append(AbstractSignature.END_BRANCH_SYMBOL);
 		}
 	}
 	
 }
