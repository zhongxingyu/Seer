 package edu.ppt.impossible.helpers;
 
 import edu.ppt.impossible.model.Graph;
 import edu.ppt.impossible.model.Tree;
 import edu.ppt.impossible.tfind.SpanningTreeFinder;
 
 public class TopologyAnalyserImpl implements TopologyAnalyser {
 	
 	private final SpanningTreeFinder spanningTreeFinder;
 
 	public TopologyAnalyserImpl(SpanningTreeFinder spanningTreeFinder) {
 		this.spanningTreeFinder = spanningTreeFinder;
 	}
 
 	@Override
	public boolean isConnected(Graph copy) {
		Tree spanningTree = spanningTreeFinder.find(copy);
		return copy.getNumNodes() == spanningTree.getNumNodes();
 	}
 }
