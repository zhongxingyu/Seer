 package org.graphstream.graph.filtered;
 
 import java.util.AbstractCollection;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.graphstream.graph.BreadthFirstIterator;
 import org.graphstream.graph.DepthFirstIterator;
 import org.graphstream.graph.Edge;
 import org.graphstream.graph.Graph;
 import org.graphstream.graph.Node;
 import org.graphstream.util.FilteredEdgeIterator;
 import org.graphstream.util.FilteredNodeIterator;
 
 public class FilteredNode extends FilteredElement<Node> implements Node {
 
 	FilteredGraph graph;
 	int iDegree, oDegree, ioDegree;
 
 	public FilteredNode(Node target, FilteredGraph g) {
 		super(target);
 
 		graph = g;
 		iDegree = oDegree = ioDegree = 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.filtered.FilteredElement#getFilteredElement()
 	 */
 	public Node getFilteredElement() {
 		return element;
 	}
 
 	void register(Edge e) {
 		if (e.isDirected()) {
 			if (e.getSourceNode().getId().equals(getId()))
 				oDegree++;
 			else
 				iDegree++;
 		}
 
 		ioDegree++;
 	}
 
 	void unregister(Edge e) {
 		if (e.isDirected()) {
 			if (e.getSourceNode().getId().equals(getId()))
 				oDegree--;
 			else
 				iDegree--;
 		}
 
 		ioDegree--;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getBreadthFirstIterator()
 	 */
 	public <T extends Node> Iterator<T> getBreadthFirstIterator() {
 		return new BreadthFirstIterator<T>(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getBreadthFirstIterator(boolean)
 	 */
 	public <T extends Node> Iterator<T> getBreadthFirstIterator(boolean directed) {
 		return new BreadthFirstIterator<T>(this, directed);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getDegree()
 	 */
 	public int getDegree() {
 		return ioDegree;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getDepthFirstIterator()
 	 */
 	public <T extends Node> Iterator<T> getDepthFirstIterator() {
 		return new DepthFirstIterator<T>(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getDepthFirstIterator(boolean)
 	 */
 	public <T extends Node> Iterator<T> getDepthFirstIterator(boolean directed) {
 		return new DepthFirstIterator<T>(this, directed);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEachEdge()
 	 */
 	public <T extends Edge> Iterable<T> getEachEdge() {
 		return new Iterable<T>() {
 			public Iterator<T> iterator() {
 				return getEdgeIterator();
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEachEnteringEdge()
 	 */
 	public <T extends Edge> Iterable<T> getEachEnteringEdge() {
 		return new Iterable<T>() {
 			public Iterator<T> iterator() {
 				return getEnteringEdgeIterator();
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEachLeavingEdge()
 	 */
 	public <T extends Edge> Iterable<T> getEachLeavingEdge() {
 		return new Iterable<T>() {
 			public Iterator<T> iterator() {
 				return getLeavingEdgeIterator();
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdge(int)
 	 */
 	public <T extends Edge> T getEdge(int i) {
 		int j = 0;
 
 		while (j < ioDegree) {
 			Edge e = element.getEdge(j);
 
 			if (graph.getEdgeFilter().isAvailable(e)) {
 				if (i == 0)
 					return graph.getEdge(e.getId());
 				else
 					i--;
 			}
 
 			j++;
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeBetween(java.lang.String)
 	 */
 	public <T extends Edge> T getEdgeBetween(String id) {
 		Edge e = element.getEdgeBetween(id);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.graphstream.graph.Node#getEdgeBetween(org.graphstream.graph.Node)
 	 */
 	public <T extends Edge> T getEdgeBetween(Node node) {
		if (node instanceof FilteredElement<?>)
			node = (Node) ((FilteredElement<?>) node).getFilteredElement();
		
 		Edge e = element.getEdgeBetween(node);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeBetween(int)
 	 */
 	public <T extends Edge> T getEdgeBetween(int index)
 			throws IndexOutOfBoundsException {
 		Edge e = element.getEdgeBetween(index);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeFrom(java.lang.String)
 	 */
 	public <T extends Edge> T getEdgeFrom(String id) {
 		Edge e = element.getEdgeFrom(id);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeFrom(org.graphstream.graph.Node)
 	 */
 	public <T extends Edge> T getEdgeFrom(Node node) {
		if (node instanceof FilteredElement<?>)
			node = (Node) ((FilteredElement<?>) node).getFilteredElement();
		
 		Edge e = element.getEdgeFrom(node);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeFrom(int)
 	 */
 	public <T extends Edge> T getEdgeFrom(int index)
 			throws IndexOutOfBoundsException {
 		Edge e = element.getEdgeFrom(index);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeIterator()
 	 */
 	public <T extends Edge> Iterator<T> getEdgeIterator() {
 		Iterator<Edge> org = element.getEdgeIterator();
 		return graph.newFilteredEdgeIterator(new FilteredEdgeIterator<Edge>(
 				org, graph.edgeFilter));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeSet()
 	 */
 	public <T extends Edge> Collection<T> getEdgeSet() {
 		return new AbstractCollection<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return getEdgeIterator();
 			}
 
 			@Override
 			public int size() {
 				return getDegree();
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeToward(java.lang.String)
 	 */
 	public <T extends Edge> T getEdgeToward(String id) {
 		Edge e = element.getEdgeToward(id);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeToward(org.graphstream.graph.Node)
 	 */
 	public <T extends Edge> T getEdgeToward(Node node) {
		if (node instanceof FilteredElement<?>)
			node = (Node) ((FilteredElement<?>) node).getFilteredElement();
		
 		Edge e = element.getEdgeToward(node);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEdgeToward(int)
 	 */
 	public <T extends Edge> T getEdgeToward(int index)
 			throws IndexOutOfBoundsException {
 		Edge e = element.getEdgeToward(index);
 
 		if (graph.getEdgeFilter().isAvailable(e))
 			return graph.getEdge(e.getId());
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEnteringEdge(int)
 	 */
 	public <T extends Edge> T getEnteringEdge(int i) {
 		int j = 0;
 
 		while (j < iDegree) {
 			Edge e = element.getEnteringEdge(j);
 
 			if (graph.getEdgeFilter().isAvailable(e)) {
 				if (i == 0)
 					return graph.getEdge(e.getId());
 				else
 					i--;
 			}
 
 			j++;
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEnteringEdgeIterator()
 	 */
 	public <T extends Edge> Iterator<T> getEnteringEdgeIterator() {
 		Iterator<Edge> org = element.getEnteringEdgeIterator();
 		return graph.newFilteredEdgeIterator(new FilteredEdgeIterator<Edge>(
 				org, graph.edgeFilter));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getEnteringEdgeSet()
 	 */
 	public <T extends Edge> Collection<T> getEnteringEdgeSet() {
 		return new AbstractCollection<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return getEnteringEdgeIterator();
 			}
 
 			@Override
 			public int size() {
 				return getInDegree();
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getGraph()
 	 */
 	public Graph getGraph() {
 		return graph;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getInDegree()
 	 */
 	public int getInDegree() {
 		return iDegree;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getLeavingEdge(int)
 	 */
 	public <T extends Edge> T getLeavingEdge(int i) {
 		int j = 0;
 
 		while (j < oDegree) {
 			Edge e = element.getLeavingEdge(j);
 
 			if (graph.getEdgeFilter().isAvailable(e)) {
 				if (i == 0)
 					return graph.getEdge(e.getId());
 				else
 					i--;
 			}
 
 			j++;
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getLeavingEdgeIterator()
 	 */
 	public <T extends Edge> Iterator<T> getLeavingEdgeIterator() {
 		Iterator<Edge> org = element.getLeavingEdgeIterator();
 		return graph.newFilteredEdgeIterator(new FilteredEdgeIterator<Edge>(
 				org, graph.edgeFilter));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getLeavingEdgeSet()
 	 */
 	public <T extends Edge> Collection<T> getLeavingEdgeSet() {
 		return new AbstractCollection<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return getLeavingEdgeIterator();
 			}
 
 			@Override
 			public int size() {
 				return getOutDegree();
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getNeighborNodeIterator()
 	 */
 	public <T extends Node> Iterator<T> getNeighborNodeIterator() {
 		Iterator<Node> org = element.getNeighborNodeIterator();
 		return graph.newFilteredNodeIterator(new FilteredNodeIterator<Node>(
 				org, graph.nodeFilter));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#getOutDegree()
 	 */
 	public int getOutDegree() {
 		return oDegree;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeBetween(java.lang.String)
 	 */
 	public boolean hasEdgeBetween(String id) {
 		return getEdgeBetween(id) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.graphstream.graph.Node#hasEdgeBetween(org.graphstream.graph.Node)
 	 */
 	public boolean hasEdgeBetween(Node node) {
 		return getEdgeBetween(node) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeBetween(int)
 	 */
 	public boolean hasEdgeBetween(int index) throws IndexOutOfBoundsException {
 		return getEdgeBetween(index) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeFrom(java.lang.String)
 	 */
 	public boolean hasEdgeFrom(String id) {
 		return getEdgeFrom(id) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeFrom(org.graphstream.graph.Node)
 	 */
 	public boolean hasEdgeFrom(Node node) {
 		return getEdgeFrom(node) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeFrom(int)
 	 */
 	public boolean hasEdgeFrom(int index) throws IndexOutOfBoundsException {
 		return getEdgeFrom(index) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeToward(java.lang.String)
 	 */
 	public boolean hasEdgeToward(String id) {
 		return getEdgeToward(id) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeToward(org.graphstream.graph.Node)
 	 */
 	public boolean hasEdgeToward(Node node) {
 		return getEdgeToward(node) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.graphstream.graph.Node#hasEdgeToward(int)
 	 */
 	public boolean hasEdgeToward(int index) throws IndexOutOfBoundsException {
 		return getEdgeToward(index) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Iterable#iterator()
 	 */
 	public Iterator<Edge> iterator() {
 		Iterator<Edge> org = element.iterator();
 		return graph.newFilteredEdgeIterator(new FilteredEdgeIterator<Edge>(
 				org, graph.edgeFilter));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return getId();
 	}
 }
