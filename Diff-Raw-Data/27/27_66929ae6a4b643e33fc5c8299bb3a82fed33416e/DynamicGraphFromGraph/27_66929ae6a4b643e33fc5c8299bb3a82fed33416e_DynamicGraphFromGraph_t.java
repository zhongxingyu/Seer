 package org.swows.graph.events;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.hp.hpl.jena.graph.BulkUpdateHandler;
 import com.hp.hpl.jena.graph.Capabilities;
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.GraphEventManager;
 import com.hp.hpl.jena.graph.GraphStatisticsHandler;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Reifier;
 import com.hp.hpl.jena.graph.TransactionHandler;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.graph.TripleMatch;
 import com.hp.hpl.jena.graph.query.QueryHandler;
 import com.hp.hpl.jena.shared.AddDeniedException;
 import com.hp.hpl.jena.shared.DeleteDeniedException;
 import com.hp.hpl.jena.shared.PrefixMapping;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 
 public class DynamicGraphFromGraph implements DynamicGraph {
 	
 	protected Graph baseGraph;
 	protected EventManager eventManager;
 	private SimpleGraphUpdate currGraphUpdate = null;
 			
 	private SimpleGraphUpdate getCurrGraphUpdate() {
 		if (currGraphUpdate == null)
			currGraphUpdate = new SimpleGraphUpdate(baseGraph);
 		return currGraphUpdate;
 	}
 	
 	public DynamicGraphFromGraph(Graph graph) {
 		baseGraph = graph;
 		this.eventManager = new SimpleEventManager(this);
 	}
 
 	public DynamicGraphFromGraph(Graph graph, EventManager eventManager) {
 		baseGraph = graph;
 		this.eventManager = eventManager;
 	}
 
 	@Override
 	public boolean dependsOn(Graph other) {
 		return baseGraph.dependsOn(other);
 	}
 
 	@Override
 	public QueryHandler queryHandler() {
 		return baseGraph.queryHandler();
 	}
 
 	@Override
 	public TransactionHandler getTransactionHandler() {
 		return baseGraph.getTransactionHandler();
 	}
 
 	@Override
 	public BulkUpdateHandler getBulkUpdateHandler() {
 		return new BulkUpdateHandler() {
 			BulkUpdateHandler buh = baseGraph.getBulkUpdateHandler();
 			@Override
 			public void removeAll() {
 				getCurrGraphUpdate().putDeletedTriples(baseGraph);
 				buh.removeAll();
 			}
 			
 			@Override
 			public void remove(Node s, Node p, Node o) {
 				getCurrGraphUpdate().putDeletedTriples(baseGraph.find(s, p, o));
 				buh.remove(s, p, o);
 			}
 			
 			@Override
 			public void delete(Graph g, boolean withReifications) {
 				getCurrGraphUpdate().putDeletedTriples(g);
 				buh.delete(g, withReifications);
 			}
 			
 			@Override
 			public void delete(Graph g) {
 				getCurrGraphUpdate().putDeletedTriples(g);
 				buh.delete(g);
 			}
 			
 			@Override
 			public void delete(Iterator<Triple> it) {
 				getCurrGraphUpdate().putDeletedTriples(it);
 				buh.delete(it);
 			}
 			
 			@Override
 			public void delete(List<Triple> triples) {
 				getCurrGraphUpdate().putDeletedTriples(triples);
 				buh.delete(triples);
 			}
 			
 			@Override
 			public void delete(Triple[] triples) {
 				getCurrGraphUpdate().putDeletedTriples(triples);
 				buh.delete(triples);
 			}
 			
 			@Override
 			public void add(Graph g, boolean withReifications) {
 				getCurrGraphUpdate().putAddedTriples(g);
 				buh.add(g, withReifications);
 			}
 			
 			@Override
 			public void add(Graph g) {
 				getCurrGraphUpdate().putAddedTriples(g);
 				buh.add(g);
 			}
 			
 			@Override
 			public void add(Iterator<Triple> it) {
 				getCurrGraphUpdate().putAddedTriples(it);
 				buh.add(it);
 			}
 			
 			@Override
 			public void add(List<Triple> triples) {
 				getCurrGraphUpdate().putAddedTriples(triples);
 				buh.add(triples);
 			}
 			
 			@Override
 			public void add(Triple[] triples) {
 				getCurrGraphUpdate().putAddedTriples(triples);
 				buh.add(triples);
 			}
 		};
 	}
 
 	@Override
 	public Capabilities getCapabilities() {
 		return baseGraph.getCapabilities();
 	}
 
 	@Override
 	public GraphEventManager getEventManager() {
 		return baseGraph.getEventManager();
 	}
 
 	@Override
 	public GraphStatisticsHandler getStatisticsHandler() {
 		return baseGraph.getStatisticsHandler();
 	}
 
 	@Override
 	public Reifier getReifier() {
 		return baseGraph.getReifier();
 	}
 
 	@Override
 	public PrefixMapping getPrefixMapping() {
 		return baseGraph.getPrefixMapping();
 	}
 
 	@Override
 	public void delete(Triple t) throws DeleteDeniedException {
 		baseGraph.delete(t);
 		getCurrGraphUpdate().putDeletedTriple(t);
 	}
 
 	@Override
 	public ExtendedIterator<Triple> find(TripleMatch m) {
 		return baseGraph.find(m);
 	}
 
 	@Override
 	public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
 		return baseGraph.find(s, p, o);
 	}
 
 	@Override
 	public boolean isIsomorphicWith(Graph g) {
 		return baseGraph.isIsomorphicWith(g);
 	}
 
 	@Override
 	public boolean contains(Node s, Node p, Node o) {
 		return baseGraph.contains(s, p, o);
 	}
 
 	@Override
 	public boolean contains(Triple t) {
 		return baseGraph.contains(t);
 	}
 
 	@Override
 	public void close() {
 		baseGraph.close();
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return baseGraph.isEmpty();
 	}
 
 	@Override
 	public int size() {
 		return baseGraph.size();
 	}
 
 	@Override
 	public boolean isClosed() {
 		return baseGraph.isClosed();
 	}
 
 	@Override
 	public void add(Triple t) throws AddDeniedException {
 		baseGraph.add(t);
 		getCurrGraphUpdate().putAddedTriple(t);
 	}
 
 	@Override
 	public EventManager getEventManager2() {
 		return eventManager;
 	}
 	
 	public synchronized void sendUpdateEvents() {
		if (currGraphUpdate != null && !currGraphUpdate.isEmpty())
			eventManager.notifyUpdate(currGraphUpdate);
 		currGraphUpdate = null;
 	}
 
 }
