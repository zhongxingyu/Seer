 package de.uni_koblenz.jgralab.algolib;
 
 import de.uni_koblenz.jgralab.Graph;
 
 public class CountCentralAlgorithm extends CentralAlgorithmImpl {
 
	public CountCentralAlgorithm(Graph partialGraph, boolean dfs) {
		super(partialGraph, dfs);
 	}
 
 	private int eCount = 0;
 
 	private int vCount = 0;
 
 	@Override
 	public void handleRoot(Long rootId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void handleVertex(Long vertexId) {
 		vCount++;
 	}
 
 	@Override
 	public void handleEdge(Long edgeId) {
 		eCount++;
 	}
 
 	@Override
 	public void handleTreeIncidence(Long incidenceId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void handleCrossIncidence(Long incidenceId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public long getVertexCount() {
 		return vCount;
 	}
 
 	public long getEdgeCount() {
 		return eCount;
 	}
 
 }
