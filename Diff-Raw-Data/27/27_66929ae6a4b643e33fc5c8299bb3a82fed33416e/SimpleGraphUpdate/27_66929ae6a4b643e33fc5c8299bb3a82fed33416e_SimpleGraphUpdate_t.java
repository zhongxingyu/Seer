 package org.swows.graph.events;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.sparql.graph.GraphFactory;
 
 public class SimpleGraphUpdate implements GraphUpdate {
 	
	private Graph baseGraph;
 	private Graph addedGraph = GraphFactory.createGraphMem();
 	private Graph deletedGraph = GraphFactory.createGraphMem();
	
	public SimpleGraphUpdate() {
		this(null);
	}

	public SimpleGraphUpdate(Graph baseGraph) {
		this.baseGraph = baseGraph;
	}
 
 	public void putAddedTriple(Triple triple) {
		if (baseGraph != null && baseGraph.contains(triple))
			return;
 		if (deletedGraph.contains(triple))
 			deletedGraph.delete(triple);
 		else
 			addedGraph.add(triple);
 	}
 	
 	public void putDeletedTriple(Triple triple) {
		if (baseGraph != null && !baseGraph.contains(triple))
			return;
 		if (addedGraph.contains(triple))
 			addedGraph.delete(triple);
 		else
 			deletedGraph.add(triple);
 	}
 	
 	public void putAddedTriple(Node s, Node p, Node o) {
 		putAddedTriple(new Triple(s, p, o));
 	}
 	
 	public void putDeletedTriple(Node s, Node p, Node o) {
 		putDeletedTriple(new Triple(s, p, o));
 	}
 	
 	public void putAddedTriples(Triple[] triples) {
 		for (Triple t: triples)
 			putAddedTriple(t);
 	}
 
 	public void putDeletedTriples(Triple[] triples) {
 		for (Triple t: triples)
 			putDeletedTriple(t);
 	}
 
 	public void putAddedTriples(Iterator<Triple> triples) {
 		while (triples.hasNext())
 			putAddedTriple(triples.next());
 	}
 
 	public void putDeletedTriples(Iterator<Triple> triples) {
 		while (triples.hasNext())
 			putDeletedTriple(triples.next());
 	}
 
 	public void putAddedTriples(List<Triple> triples) {
 		putAddedTriples(triples.iterator());
 	}
 
 	public void putDeletedTriples(List<Triple> triples) {
 		putDeletedTriples(triples.iterator());
 	}
 
 	public void putAddedTriples(Graph graph) {
 		putAddedTriples(graph.find(Node.ANY, Node.ANY, Node.ANY));
 	}
 
 	public void putDeletedTriples(Graph graph) {
 		putDeletedTriples(graph.find(Node.ANY, Node.ANY, Node.ANY));
 	}
 
 	public boolean isEmpty() {
 		return addedGraph.isEmpty() && deletedGraph.isEmpty();
 	}
 	
 	@Override
 	public Graph getAddedGraph() {
 		return addedGraph;
 	}
 
 	@Override
 	public Graph getDeletedGraph() {
 		return deletedGraph;
 	}
 
 }
