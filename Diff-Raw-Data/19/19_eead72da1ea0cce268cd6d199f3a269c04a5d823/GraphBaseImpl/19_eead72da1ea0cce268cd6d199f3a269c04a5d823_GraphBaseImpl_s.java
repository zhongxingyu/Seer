 /*
  * JGraLab - The Java Graph Laboratory
  * 
  * Copyright (C) 2006-2010 Institute for Software Technology
  *                         University of Koblenz-Landau, Germany
  *                         ist@uni-koblenz.de
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * Additional permission under GNU GPL version 3 section 7
  * 
  * If you modify this Program, or any covered work, by linking or combining
  * it with Eclipse (or a modified version of that program or an Eclipse
  * plugin), containing parts covered by the terms of the Eclipse Public
  * License (EPL), the licensors of this Program grant you additional
  * permission to convey the resulting work.  Corresponding Source for a
  * non-source form of such a combination shall include the source code for
  * the parts of JGraLab used as well as that of the covered work.
  */
 
 package de.uni_koblenz.jgralab.impl.disk;
 
 import java.lang.ref.WeakReference;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import de.uni_koblenz.jgralab.AttributedElement;
 import de.uni_koblenz.jgralab.BinaryEdge;
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.GraphException;
 import de.uni_koblenz.jgralab.GraphFactory;
 import de.uni_koblenz.jgralab.GraphIO;
 import de.uni_koblenz.jgralab.GraphIOException;
 import de.uni_koblenz.jgralab.GraphStructureChangedListener;
 import de.uni_koblenz.jgralab.GraphStructureChangedListenerWithAutoRemove;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.impl.EdgeIterable;
 import de.uni_koblenz.jgralab.impl.VertexIterable;
 import de.uni_koblenz.jgralab.schema.Attribute;
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 import de.uni_koblenz.jgralab.schema.GraphClass;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 /**
  * Base class for all graph implementations
  * 
  * 
  * @author ist@uni-koblenz.de
  */
 public abstract class GraphBaseImpl implements Graph {
 
 	/**
 	 * the graph database that stores this graph and manages all connections to
 	 * all partial graphs
 	 */
 	protected final GraphDatabaseBaseImpl localGraphDatabase;
 
 	/**
 	 * The property access providing direct access to the data of this graph,
 	 * either it is the local disk storage of the local graph database or the
 	 * remote graph database
 	 */
 	protected final RemoteGraphDatabaseAccess storingGraphDatabase;
 
 	/**
 	 * The id of this complete or partial graph identifying it in the complete
 	 * graph
 	 */
 	protected final long globalSubgraphId;
 
 	/**
 	 * Creates a graph of the given GraphClass with the given id
 	 * 
 	 * @param id
 	 *            this Graph's id
 	 * @param cls
 	 *            the GraphClass of this Graph
 	 */
 	protected GraphBaseImpl(long globalSubgraphId,
 			GraphDatabaseBaseImpl localGraphDatabase,
 			RemoteGraphDatabaseAccess storingGraphDatabase) {
 		this.globalSubgraphId = globalSubgraphId;
 		this.localGraphDatabase = localGraphDatabase;
 		this.storingGraphDatabase = storingGraphDatabase;
 	}
 
 	@Override
 	public int compareTo(Graph arg0) {
 		int comVal = (int) (globalSubgraphId - arg0.getGlobalId());
 		if (comVal == 0) {
 			return getUniqueGraphId().compareTo(arg0.getUniqueGraphId());
 		}
 		return comVal;
 		// if (getCompleteGraph() == arg0) {
 		// // each graph is smaller than the complete graph
 		// return -1;
 		// } else if (arg0.getParentGraphOrElement() != null) {
 		// // this is a SubordinateGraphImpl
 		// GraphElement<?, ?, ?> ce = (GraphElement<?, ?, ?>)
 		// arg0.getParentGraphOrElement();
 		// boolean isArg0Vertex = ce instanceof Vertex;
 		// boolean isThisVertex = getParentGraphOrElement() instanceof Vertex;
 		// if (isArg0Vertex && isThisVertex) {
 		// // both are vertices
 		// return ((Vertex) getParentGraphOrElement()).compareTo((Vertex) ce);
 		// } else if (!isArg0Vertex && !isThisVertex) {
 		// // both are edges
 		// return ((Edge) getParentGraphOrElement()).compareTo((Edge) ce);
 		// } else {
 		// // the subordinate graph of a vertex is greater
 		// return isThisVertex ? 1 : -1;
 		// }
 		// } else {
 		// // this is a ViewGraphImpl or PartialGraphImpl
 		// return -arg0.compareTo(this);
 		// }
 	}
 
 	// ============================================================================
 	// Methods to manage the current traversal context
 	// ============================================================================
 
 	@Override
 	public Graph getTraversalContext() {
 		return localGraphDatabase.getTraversalContext();
 	}
 
 	@Override
 	public void useAsTraversalContext() {
 		localGraphDatabase.setTraversalContext(this);
 	}
 
 	@Override
 	public void releaseTraversalContext() {
 		localGraphDatabase.releaseTraversalContext();
 	}
 
 	// ============================================================================
 	// Methods to access hierarchy and distribution
 	//
 	// - General methods
 	// - Nesting hierarchy
 	// - Visibility layering
 	// - Distribution
 	// - Graph IDs
 	// ============================================================================
 
 	@Override
 	public abstract Graph getCompleteGraph();
 
 	@Override
 	public abstract Graph getLocalPartialGraph();
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public abstract AttributedElement getParentGraphOrElement();
 
 	@Override
 	public abstract Graph getParentGraph();
 
 	@Override
 	public abstract boolean isPartOfGraph(Graph other);
 
 	@Override
 	public abstract Graph getView(int kappa);
 
 	@Override
 	public abstract Graph getViewedGraph();
 
 	@Override
 	public Graph createPartialGraphInGraph(String hostnameOfPartialGraph) {
 		long pgId;
 		try {
 			pgId = storingGraphDatabase.createPartialGraphInGraph(
 					getGlobalId(), hostnameOfPartialGraph);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 		return localGraphDatabase.getGraphObject(pgId);
 	}
 
 	@Override
 	public List<Graph> getPartialGraphs() {
 		LinkedList<Graph> list = new LinkedList<Graph>();
 		System.out.println("Graph db: " + getGraphDatabase());
 		System.out.println("Global graph id: " + globalSubgraphId);
 		System.out.println("Partial graphs: "
 				+ getGraphDatabase().getPartialGraphIds(getGlobalId()));
 		for (Integer i : getGraphDatabase().getPartialGraphIds(getGlobalId())) {
			Graph p = getGraphDatabase().getGraphObject(i);
 			list.add(p);
 			// list.addAll(p.getPartialGraphs());
 		}
 		return list;
 	}
 
 	@Override
 	public Graph getPartialGraph(int partialGraphId) {
 		return getGraphDatabase().getGraphObject(
 				GraphDatabaseBaseImpl
 						.getToplevelGraphForPartialGraphId(partialGraphId));
 	}
 
 	/**
 	 * Saves the partial graphs of this graph
 	 * 
 	 * @param graphIO
 	 */
 	@Deprecated
 	@Override
 	public void savePartialGraphs(GraphIO graphIO) {
 		throw new RuntimeException("Operation not yet implemented");
 	}
 
 	// ============================================================================
 	// Methods to access ids
 	// ============================================================================
 
 	@Override
 	public String getUniqueGraphId() {
 		return localGraphDatabase.getUniqueGraphId();
 	}
 
 	@Override
 	public long getGlobalId() {
 		return globalSubgraphId;
 	}
 
 	@Override
 	public int getLocalId() {
 		return GraphDatabaseBaseImpl.convertToLocalId(globalSubgraphId);
 	}
 
 	@Override
 	public int getPartialGraphId() {
 		return GraphDatabaseBaseImpl.getPartialGraphId(globalSubgraphId);
 	}
 
 	@Override
 	public boolean isLocalElementId(long id) {
 		try {
 			return GraphDatabaseElementaryMethods.getPartialGraphId(id) == storingGraphDatabase
 					.getLocalPartialGraphId();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// ============================================================================
 	// Methods to access vertices and edges of the graph
 	// ============================================================================
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Vertex> T createVertex(Class<T> cls) {
 		try {
 			return (T) getGraphFactory().createVertex(cls, 0, this);
 		} catch (Exception ex) {
 			if (ex instanceof GraphException) {
 				throw (GraphException) ex;
 			}
 			throw new GraphException("Error creating vertex of class "
 					+ cls.getName(), ex);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Edge> T createEdge(Class<T> cls) {
 		try {
 			return (T) localGraphDatabase.getEdgeObject(storingGraphDatabase
 					.createEdge(getSchema().getClassId(cls)));
 		} catch (Exception exception) {
 			if (exception instanceof GraphException) {
 				throw (GraphException) exception;
 			} else {
 				throw new GraphException("Error creating edge of class "
 						+ cls.getName(), exception);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends BinaryEdge> T createEdge(Class<T> cls, Vertex alpha,
 			Vertex omega) {
 		try {
 			T edge = (T) localGraphDatabase.getEdgeObject(storingGraphDatabase
 					.createEdge(getSchema().getClassId(cls)));
 			IncidenceClass fromClass = null;
 			IncidenceClass toClass = null;
 			EdgeClass metaClass = edge.getType();
 			assert (metaClass.isBinary());
 			Set<IncidenceClass> incClasses = metaClass.getAllIncidenceClasses();
 			for (IncidenceClass ic : incClasses) {
 				if ((!ic.isAbstract())
 						&& (ic.getDirection() == Direction.VERTEX_TO_EDGE)) {
 					fromClass = ic;
 				}
 				if ((!ic.isAbstract())
 						&& (ic.getDirection() == Direction.EDGE_TO_VERTEX)) {
 					toClass = ic;
 				}
 			}
 			connect(fromClass.getM1Class(), alpha, edge);
 			connect(toClass.getM1Class(), omega, edge);
 			return edge;
 		} catch (Exception exception) {
 			if (exception instanceof GraphException) {
 				throw (GraphException) exception;
 			} else {
 				throw new GraphException("Error creating edge of class "
 						+ cls.getName(), exception);
 			}
 		}
 	}
 
 	@Override
 	public <T extends Incidence> T connect(Class<T> cls, Vertex vertex,
 			Edge edge) {
 		T newIncidence = vertex.connect(cls, edge);
 		return newIncidence;
 	}
 
 	@Override
 	public boolean containsEdge(Edge e) {
 		if (containsEdgeLocally(e)) {
 			return true;
 		}
 		return e.getContainingGraph().isPartOfGraph(this);
 	}
 
 	@Override
 	public boolean containsVertex(Vertex v) {
 		if (containsVertexLocally(v)) {
 			return true;
 		}
 		return v.getContainingGraph().isPartOfGraph(this);
 	}
 
 	@Override
 	public boolean containsElement(
 			@SuppressWarnings("rawtypes") GraphElement elem) {
 		if (elem instanceof Edge) {
 			return containsEdge((Edge) elem);
 		} else {
 			return containsVertex((Vertex) elem);
 		}
 	}
 
 	@Override
 	public void deleteVertex(Vertex v) {
 		assert (v != null) && v.isValid() && containsVertex(v);
 		try {
 			storingGraphDatabase.deleteVertex(v.getGlobalId());
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void deleteEdge(Edge e) {
 		assert (e != null) && e.isValid() && containsEdge(e);
 		try {
 			storingGraphDatabase.deleteEdge(e.getGlobalId());
 		} catch (RemoteException e1) {
 			throw new RuntimeException(e1);
 		}
 	}
 
 	@Override
 	public Vertex getFirstVertex() {
 		try {
 			return localGraphDatabase.getVertexObject(storingGraphDatabase
 					.getFirstVertexId(globalSubgraphId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public Vertex getLastVertex() {
 		try {
 			return localGraphDatabase.getVertexObject(storingGraphDatabase
 					.getLastVertexId(globalSubgraphId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public Vertex getFirstVertex(VertexClass vertexClass) {
 		assert vertexClass != null;
 		return getFirstVertex(vertexClass, false);
 	}
 
 	@Override
 	public Vertex getFirstVertex(VertexClass vertexClass, boolean noSubclasses) {
 		assert vertexClass != null;
 		return getFirstVertex(vertexClass.getM1Class(), noSubclasses);
 	}
 
 	@Override
 	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass) {
 		assert vertexClass != null;
 		return getFirstVertex(vertexClass, false);
 	}
 
 	@Override
 	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass,
 			boolean noSubclasses) {
 		assert vertexClass != null;
 		Vertex firstVertex = getFirstVertex();
 		if (firstVertex == null) {
 			return null;
 		}
 		if (noSubclasses) {
 			if (vertexClass == firstVertex.getM1Class()) {
 				return firstVertex;
 			}
 		} else {
 			if (vertexClass.isInstance(firstVertex)) {
 				return firstVertex;
 			}
 		}
 		return firstVertex.getNextVertex(this, vertexClass, noSubclasses);
 	}
 
 	// ------------- EDGE LIST VARIABLES -------------
 
 	@Override
 	public Edge getFirstEdge() {
 		try {
 			return localGraphDatabase.getEdgeObject(storingGraphDatabase
 					.getFirstEdgeId(globalSubgraphId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public Edge getLastEdge() {
 		try {
 			return localGraphDatabase.getEdgeObject(storingGraphDatabase
 					.getLastEdgeId(globalSubgraphId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public Edge getFirstEdge(EdgeClass edgeClass) {
 		assert edgeClass != null;
 		return getFirstEdge(edgeClass.getM1Class(), false);
 	}
 
 	@Override
 	public Edge getFirstEdge(EdgeClass edgeClass, boolean noSubclasses) {
 		assert edgeClass != null;
 		return getFirstEdge(edgeClass.getM1Class(), noSubclasses);
 	}
 
 	@Override
 	public Edge getFirstEdge(Class<? extends Edge> edgeClass) {
 		assert edgeClass != null;
 		return getFirstEdge(edgeClass, false);
 	}
 
 	@Override
 	public Edge getFirstEdge(Class<? extends Edge> edgeClass,
 			boolean noSubclasses) {
 		assert edgeClass != null;
 		Edge currentEdge = getFirstEdge();
 		while (currentEdge != null) {
 			if (noSubclasses) {
 				if (edgeClass == currentEdge.getM1Class()) {
 					return currentEdge;
 				}
 			} else {
 				if (edgeClass.isInstance(currentEdge)) {
 					return currentEdge;
 				}
 			}
 			currentEdge = currentEdge.getNextEdge(this);
 		}
 		return null;
 	}
 
 	@Override
 	public Vertex getVertex(long vId) {
 		assert (vId > 0) : "The vertex id must be > 0, given was " + vId;
 		return localGraphDatabase.getVertexObject(vId);
 	}
 
 	@Override
 	public Edge getEdge(long eId) {
 		assert eId != 0 : "The edge id must be != 0, given was " + eId;
 		return localGraphDatabase.getEdgeObject(eId);
 	}
 
 	@Override
 	public long getMaxVCount() {
 		try {
 			return storingGraphDatabase.getMaxVCount();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public long getMaxECount() {
 		try {
 			return storingGraphDatabase.getMaxECount();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public long getMaxICount() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public long getVCount() {
 		try {
 			return storingGraphDatabase.getVCount(globalSubgraphId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public long getECount() {
 		try {
 			return storingGraphDatabase.getECount(globalSubgraphId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public long getICount() {
 		return localGraphDatabase.getICount(globalSubgraphId);
 	}
 
 	@Override
 	public Iterable<Vertex> getVertices() {
 		return new VertexIterable<Vertex>(this);
 	}
 
 	@Override
 	public Iterable<Vertex> getVertices(Class<? extends Vertex> vertexClass) {
 		return new VertexIterable<Vertex>(this, vertexClass);
 	}
 
 	@Override
 	public Iterable<Vertex> getVertices(VertexClass vertexClass) {
 		return new VertexIterable<Vertex>(this, vertexClass.getM1Class());
 	}
 
 	@Override
 	public Iterable<Edge> getEdges() {
 		return new EdgeIterable<Edge>(this);
 	}
 
 	@Override
 	public Iterable<Edge> getEdges(EdgeClass edgeClass) {
 		return new EdgeIterable<Edge>(this, edgeClass.getM1Class());
 	}
 
 	@Override
 	public Iterable<Edge> getEdges(Class<? extends Edge> edgeClass) {
 		return new EdgeIterable<Edge>(this, edgeClass);
 	}
 
 	// ============================================================================
 	// Methods to sort vertices and edges of the graph
 	// ============================================================================
 
 	// sort vertices
 	@Override
 	public void sortVertices(Comparator<Vertex> comp) {
 
 		if (getFirstVertex() == null) {
 			// no sorting required for empty vertex lists
 			return;
 		}
 		class VertexList {
 			VertexImpl first;
 			VertexImpl last;
 
 			public void add(VertexImpl v) {
 				if (first == null) {
 					first = v;
 					assert (last == null);
 					last = v;
 				} else {
 					// if (!hasSavememSupport()) {
 					v.setPreviousVertex(last);
 					// }
 					last.setNextVertex(v);
 					last = v;
 				}
 				v.setNextVertex(null);
 			}
 
 			public VertexImpl remove() {
 				if (first == null) {
 					throw new NoSuchElementException();
 				}
 				VertexImpl out;
 				if (first == last) {
 					out = first;
 					first = null;
 					last = null;
 					return out;
 				}
 				out = first;
 				first = (VertexImpl) out.getNextVertex();
 				// if (!hasSavememSupport()) {
 				first.setPreviousVertex(null);
 				// }
 				return out;
 			}
 
 			public boolean isEmpty() {
 				assert ((first == null) == (last == null));
 				return first == null;
 			}
 
 		}
 
 		VertexList a = new VertexList();
 		VertexList b = new VertexList();
 		VertexList out = a;
 
 		// split
 		VertexImpl last;
 		VertexList l = new VertexList();
 		l.first = (VertexImpl) getFirstVertex();
 		l.last = (VertexImpl) getLastVertex();
 
 		out.add(last = l.remove());
 		while (!l.isEmpty()) {
 			VertexImpl current = l.remove();
 			if (comp.compare(current, last) < 0) {
 				out = (out == a) ? b : a;
 			}
 			out.add(current);
 			last = current;
 		}
 		if (a.isEmpty() || b.isEmpty()) {
 			out = a.isEmpty() ? b : a;
 			try {
 				storingGraphDatabase.setFirstVertexId(globalSubgraphId,
 						out.first.getGlobalId());
 				storingGraphDatabase.setLastVertexId(globalSubgraphId,
 						out.last.getGlobalId());
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 			return;
 		}
 
 		while (true) {
 			if (a.isEmpty() || b.isEmpty()) {
 				out = a.isEmpty() ? b : a;
 				try {
 					storingGraphDatabase.setFirstVertexId(globalSubgraphId,
 							out.first.getGlobalId());
 					storingGraphDatabase.setLastVertexId(globalSubgraphId,
 							out.last.getGlobalId());
 				} catch (RemoteException e) {
 					throw new RuntimeException(e);
 				}
 				edgeListModified();
 				return;
 			}
 
 			VertexList c = new VertexList();
 			VertexList d = new VertexList();
 			out = c;
 
 			last = null;
 			while (!a.isEmpty() && !b.isEmpty()) {
 				int compareAToLast = last != null ? comp.compare(a.first, last)
 						: 0;
 				int compareBToLast = last != null ? comp.compare(b.first, last)
 						: 0;
 
 				if ((compareAToLast >= 0) && (compareBToLast >= 0)) {
 					if (comp.compare(a.first, b.first) <= 0) {
 						out.add(last = a.remove());
 					} else {
 						out.add(last = b.remove());
 					}
 				} else if ((compareAToLast < 0) && (compareBToLast < 0)) {
 					out = (out == c) ? d : c;
 					last = null;
 				} else if ((compareAToLast < 0) && (compareBToLast >= 0)) {
 					out.add(last = b.remove());
 				} else {
 					out.add(last = a.remove());
 				}
 			}
 
 			// copy rest of A
 			while (!a.isEmpty()) {
 				VertexImpl current = a.remove();
 				if (comp.compare(current, last) < 0) {
 					out = (out == c) ? d : c;
 				}
 				out.add(current);
 				last = current;
 			}
 
 			// copy rest of B
 			while (!b.isEmpty()) {
 				VertexImpl current = b.remove();
 				if (comp.compare(current, last) < 0) {
 					out = (out == c) ? d : c;
 				}
 				out.add(current);
 				last = current;
 			}
 
 			a = c;
 			b = d;
 		}
 
 	}
 
 	// sort edges
 	@Override
 	public void sortEdges(Comparator<Edge> comp) {
 
 		if (getFirstEdge() == null) {
 			// no sorting required for empty edge lists
 			return;
 		}
 		class EdgeList {
 			EdgeImpl first;
 			EdgeImpl last;
 
 			public void add(EdgeImpl e) {
 				if (first == null) {
 					first = e;
 					assert (last == null);
 					last = e;
 				} else {
 					// if (!hasSavememSupport()) {
 					e.setPreviousEdge(last);
 					// }
 					last.setNextEdge(e);
 					last = e;
 				}
 				e.setNextEdge(null);
 			}
 
 			public EdgeImpl remove() {
 				if (first == null) {
 					throw new NoSuchElementException();
 				}
 				EdgeImpl out;
 				if (first == last) {
 					out = first;
 					first = null;
 					last = null;
 					return out;
 				}
 				out = first;
 				first = (EdgeImpl) out.getNextEdge();
 				// if (!hasSavememSupport()) {
 				first.setPreviousEdge(null);
 				// }
 				return out;
 			}
 
 			public boolean isEmpty() {
 				assert ((first == null) == (last == null));
 				return first == null;
 			}
 
 		}
 
 		EdgeList a = new EdgeList();
 		EdgeList b = new EdgeList();
 		EdgeList out = a;
 
 		// split
 		EdgeImpl last;
 		EdgeList l = new EdgeList();
 		l.first = (EdgeImpl) getFirstEdge();
 		l.last = (EdgeImpl) getLastEdge();
 
 		out.add(last = l.remove());
 		while (!l.isEmpty()) {
 			EdgeImpl current = l.remove();
 			if (comp.compare(current, last) < 0) {
 				out = (out == a) ? b : a;
 			}
 			out.add(current);
 			last = current;
 		}
 		if (a.isEmpty() || b.isEmpty()) {
 			out = a.isEmpty() ? b : a;
 			try {
 				storingGraphDatabase.setFirstEdgeId(globalSubgraphId,
 						out.first.getGlobalId());
 				storingGraphDatabase.setLastEdgeId(globalSubgraphId,
 						out.last.getGlobalId());
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 			return;
 		}
 
 		while (true) {
 			if (a.isEmpty() || b.isEmpty()) {
 				out = a.isEmpty() ? b : a;
 				try {
 					storingGraphDatabase.setFirstEdgeId(globalSubgraphId,
 							out.first.getGlobalId());
 					storingGraphDatabase.setLastEdgeId(globalSubgraphId,
 							out.last.getGlobalId());
 				} catch (RemoteException e) {
 					throw new RuntimeException(e);
 				}
 				edgeListModified();
 				return;
 			}
 
 			EdgeList c = new EdgeList();
 			EdgeList d = new EdgeList();
 			out = c;
 
 			last = null;
 			while (!a.isEmpty() && !b.isEmpty()) {
 				int compareAToLast = last != null ? comp.compare(a.first, last)
 						: 0;
 				int compareBToLast = last != null ? comp.compare(b.first, last)
 						: 0;
 
 				if ((compareAToLast >= 0) && (compareBToLast >= 0)) {
 					if (comp.compare(a.first, b.first) <= 0) {
 						out.add(last = a.remove());
 					} else {
 						out.add(last = b.remove());
 					}
 				} else if ((compareAToLast < 0) && (compareBToLast < 0)) {
 					out = (out == c) ? d : c;
 					last = null;
 				} else if ((compareAToLast < 0) && (compareBToLast >= 0)) {
 					out.add(last = b.remove());
 				} else {
 					out.add(last = a.remove());
 				}
 			}
 
 			// copy rest of A
 			while (!a.isEmpty()) {
 				EdgeImpl current = a.remove();
 				if (comp.compare(current, last) < 0) {
 					out = (out == c) ? d : c;
 				}
 				out.add(current);
 				last = current;
 			}
 
 			// copy rest of B
 			while (!b.isEmpty()) {
 				EdgeImpl current = b.remove();
 				if (comp.compare(current, last) < 0) {
 					out = (out == c) ? d : c;
 				}
 				out.add(current);
 				last = current;
 			}
 
 			a = c;
 			b = d;
 		}
 
 	}
 
 	// ============================================================================
 	// Methods to handle graph listeners
 	// ============================================================================
 
 	/**
 	 * A list of all registered <code>GraphStructureChangedListener</code> as
 	 * <i>WeakReference</i>s.
 	 */
 	protected List<WeakReference<GraphStructureChangedListener>> graphStructureChangedListenersWithAutoRemoval;
 	protected List<GraphStructureChangedListener> graphStructureChangedListeners;
 	{
 		graphStructureChangedListenersWithAutoRemoval = null;
 		graphStructureChangedListeners = new ArrayList<GraphStructureChangedListener>();
 	}
 
 	private void lazyCreateGraphStructureChangedListenersWithAutoRemoval() {
 		if (graphStructureChangedListenersWithAutoRemoval == null) {
 			graphStructureChangedListenersWithAutoRemoval = new LinkedList<WeakReference<GraphStructureChangedListener>>();
 		}
 	}
 
 	@Override
 	public void addGraphStructureChangedListener(
 			GraphStructureChangedListener newListener) {
 		assert newListener != null;
 		if (newListener instanceof GraphStructureChangedListenerWithAutoRemove) {
 			lazyCreateGraphStructureChangedListenersWithAutoRemoval();
 			graphStructureChangedListenersWithAutoRemoval
 					.add(new WeakReference<GraphStructureChangedListener>(
 							newListener));
 		} else {
 			graphStructureChangedListeners.add(newListener);
 		}
 	}
 
 	@Override
 	public void removeGraphStructureChangedListener(
 			GraphStructureChangedListener listener) {
 		assert listener != null;
 		if (listener instanceof GraphStructureChangedListenerWithAutoRemove) {
 			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
 			while (iterator.hasNext()) {
 				GraphStructureChangedListener currentListener = iterator.next()
 						.get();
 				if ((currentListener == null) || (currentListener == listener)) {
 					iterator.remove();
 				}
 			}
 		} else {
 			Iterator<GraphStructureChangedListener> iterator = getListenerListIterator();
 			while (iterator.hasNext()) {
 				GraphStructureChangedListener currentListener = iterator.next();
 				if (currentListener == listener) {
 					iterator.remove();
 				}
 			}
 		}
 	}
 
 	protected void setAutoListenerListToNullIfEmpty() {
 		if (graphStructureChangedListenersWithAutoRemoval.isEmpty()) {
 			graphStructureChangedListenersWithAutoRemoval = null;
 		}
 	}
 
 	@Override
 	public void removeAllGraphStructureChangedListeners() {
 		graphStructureChangedListenersWithAutoRemoval = null;
 		graphStructureChangedListeners.clear();
 	}
 
 	@Override
 	public int getGraphStructureChangedListenerCount() {
 		return graphStructureChangedListenersWithAutoRemoval == null ? graphStructureChangedListeners
 				.size() : graphStructureChangedListenersWithAutoRemoval.size()
 				+ graphStructureChangedListeners.size();
 	}
 
 	protected Iterator<WeakReference<GraphStructureChangedListener>> getListenerListIteratorForAutoRemove() {
 		return graphStructureChangedListenersWithAutoRemoval.iterator();
 	}
 
 	private Iterator<GraphStructureChangedListener> getListenerListIterator() {
 		return graphStructureChangedListeners.iterator();
 	}
 
 	/**
 	 * Notifies all registered <code>GraphStructureChangedListener</code> that
 	 * the given vertex <code>v</code> is about to be deleted. All invalid
 	 * <code>WeakReference</code>s are deleted automatically from the internal
 	 * listener list.
 	 * 
 	 * @param v
 	 *            the vertex that is about to be deleted.
 	 */
 	protected void notifyVertexDeleted(Vertex v) {
 		assert (v != null) && v.isValid() && containsVertex(v);
 		if (graphStructureChangedListenersWithAutoRemoval != null) {
 			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
 			while (iterator.hasNext()) {
 				GraphStructureChangedListener currentListener = iterator.next()
 						.get();
 				if (currentListener == null) {
 					iterator.remove();
 				} else {
 					currentListener.vertexDeleted(v);
 				}
 			}
 			setAutoListenerListToNullIfEmpty();
 		}
 		int n = graphStructureChangedListeners.size();
 		for (int i = 0; i < n; i++) {
 			graphStructureChangedListeners.get(i).vertexDeleted(v);
 		}
 	}
 
 	/**
 	 * Notifies all registered <code>GraphStructureChangedListener</code> that
 	 * the given vertex <code>v</code> has been created. All invalid
 	 * <code>WeakReference</code>s are deleted automatically from the internal
 	 * listener list.
 	 * 
 	 * @param v
 	 *            the vertex that has been created.
 	 */
 	protected void notifyVertexAdded(Vertex v) {
 		assert (v != null) && v.isValid() && containsVertex(v);
 		if (graphStructureChangedListenersWithAutoRemoval != null) {
 			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
 			while (iterator.hasNext()) {
 				GraphStructureChangedListener currentListener = iterator.next()
 						.get();
 				if (currentListener == null) {
 					iterator.remove();
 				} else {
 					currentListener.vertexAdded(v);
 				}
 			}
 			setAutoListenerListToNullIfEmpty();
 		}
 		int n = graphStructureChangedListeners.size();
 		for (int i = 0; i < n; i++) {
 			graphStructureChangedListeners.get(i).vertexAdded(v);
 		}
 	}
 
 	/**
 	 * Notifies all registered <code>GraphStructureChangedListener</code> that
 	 * the given incidence <code>i</code> has been created. All invalid
 	 * <code>WeakReference</code>s are deleted automatically from the internal
 	 * listener list.
 	 * 
 	 * @param i
 	 *            the incidence that has been created.
 	 */
 	protected void notifyIncidenceAdded(Incidence i) {
 		if (graphStructureChangedListenersWithAutoRemoval != null) {
 			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
 			while (iterator.hasNext()) {
 				GraphStructureChangedListener currentListener = iterator.next()
 						.get();
 				if (currentListener == null) {
 					iterator.remove();
 				} else {
 					currentListener.incidenceAdded(i);
 				}
 			}
 			setAutoListenerListToNullIfEmpty();
 		}
 		int n = graphStructureChangedListeners.size();
 		for (int j = 0; j < n; j++) {
 			graphStructureChangedListeners.get(j).incidenceAdded(i);
 		}
 	}
 
 	/**
 	 * Notifies all registered <code>GraphStructureChangedListener</code> that
 	 * the given edge <code>e</code> is about to be deleted. All invalid
 	 * <code>WeakReference</code>s are deleted automatically from the internal
 	 * listener list.
 	 * 
 	 * @param e
 	 *            the edge that is about to be deleted.
 	 */
 	protected void notifyEdgeDeleted(Edge e) {
 		assert (e != null) && e.isValid() && containsEdge(e);
 		if (graphStructureChangedListenersWithAutoRemoval != null) {
 			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
 			while (iterator.hasNext()) {
 				GraphStructureChangedListener currentListener = iterator.next()
 						.get();
 				if (currentListener == null) {
 					iterator.remove();
 				} else {
 					currentListener.edgeDeleted(e);
 				}
 			}
 			setAutoListenerListToNullIfEmpty();
 		}
 		int n = graphStructureChangedListeners.size();
 		for (int i = 0; i < n; i++) {
 			graphStructureChangedListeners.get(i).edgeDeleted(e);
 		}
 	}
 
 	/**
 	 * Notifies all registered <code>GraphStructureChangedListener</code> that
 	 * the given edge <code>e</code> has been created. All invalid
 	 * <code>WeakReference</code>s are deleted automatically from the internal
 	 * listener list.
 	 * 
 	 * @param e
 	 *            the edge that has been created.
 	 */
 	protected void notifyEdgeAdded(Edge e) {
 		assert (e != null) && e.isValid() && containsEdge(e);
 		if (graphStructureChangedListenersWithAutoRemoval != null) {
 			Iterator<WeakReference<GraphStructureChangedListener>> iterator = getListenerListIteratorForAutoRemove();
 			while (iterator.hasNext()) {
 				GraphStructureChangedListener currentListener = iterator.next()
 						.get();
 				if (currentListener == null) {
 					iterator.remove();
 				} else {
 					currentListener.edgeAdded(e);
 				}
 			}
 			setAutoListenerListToNullIfEmpty();
 		}
 		int n = graphStructureChangedListeners.size();
 		for (int i = 0; i < n; i++) {
 			graphStructureChangedListeners.get(i).edgeAdded(e);
 		}
 	}
 
 	// ============================================================================
 	// Methods to access graph state and version (loading etc.)
 	// ============================================================================
 
 	@Override
 	public void initializeAttributesWithDefaultValues() {
 		for (Attribute attr : getType().getAttributeList()) {
 			try {
 				if ((attr.getDefaultValueAsString() != null)
 						&& !attr.getDefaultValueAsString().isEmpty()) {
 					internalSetDefaultValue(attr);
 				}
 			} catch (GraphIOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param attr
 	 * @throws GraphIOException
 	 */
 	protected void internalSetDefaultValue(Attribute attr)
 			throws GraphIOException {
 		attr.setDefaultValue(this);
 	}
 
 	protected void moveToSubordinateGraph(GraphElement<?, ?, ?> parent,
 			GraphElement<?, ?, ?> child) {
 		try {
 			parent.addSubordinateElement((Vertex) child);
 		} catch (ClassCastException e) {
 			parent.addSubordinateElement((Edge) child);
 		}
 	}
 
 	/**
 	 * Changes this graph's version. graphModified() is called whenever the
 	 * graph is changed, all changes like adding, creating and reordering of
 	 * edges and vertices or changes of attributes of the graph, an edge or a
 	 * vertex are treated as a change.
 	 */
 	public abstract void graphModified();
 
 	@Override
 	public boolean isGraphModified(long previousVersion) {
 		return getGraphVersion() != previousVersion;
 	}
 
 	/**
 	 * Changes the vertex list version of this graph. vertexListModified() is
 	 * called whenever the vertices of a graph are changes, all changes like
 	 * adding, creating and reordering of vertices are treated as a change.
 	 */
 	protected abstract void vertexListModified();
 
 	@Override
 	public boolean isVertexListModified(long previousVersion) {
 		return getVertexListVersion() != previousVersion;
 	}
 
 	@Override
 	abstract public long getVertexListVersion();
 
 	/**
 	 * Changes the edge list version of this graph. edgeListModified() is called
 	 * whenever the edges of a graph are changes, all changes like adding,
 	 * creating and reordering of edges are treated as a change.
 	 */
 	protected abstract void edgeListModified();
 
 	@Override
 	public boolean isEdgeListModified(long edgeListVersion) {
 		return getEdgeListVersion() != edgeListVersion;
 	}
 
 	@Override
 	abstract public long getEdgeListVersion();
 
 	@Override
 	public GraphFactory getGraphFactory() {
 		return localGraphDatabase.getGraphFactory();
 	}
 
 	@Override
 	public GraphDatabaseBaseImpl getGraphDatabase() {
 		return localGraphDatabase;
 	}
 
 	@Override
 	public GraphClass getGraphClass() {
 		return getType();
 	}
 
 	/**
 	 * checks if the vertex v is contained directly in this graph, ant not as a
 	 * member of one of its partial graphs
 	 */
 	public abstract boolean containsVertexLocally(Vertex v);
 
 	/**
 	 * checks if the edge e is contained directly in this graph, ant not as a
 	 * member of one of its partial graphs
 	 */
 	public abstract boolean containsEdgeLocally(Edge e);
 
 	/**
 	 * Retrieves the Vertex object representing the vertex with the id vid that
 	 * is part of the global graph this graph belongs to.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	Vertex getVertexObjectForId(int vid) {
 		return getGraphDatabase().getVertexObject(vid);
 	}
 
 	/**
 	 * Retrieves the Edge object representing the edge with the id eid that is
 	 * part of the global graph this graph belongs to.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	Edge getEdgeObjectForId(int eid) {
 		return getGraphDatabase().getEdgeObject(eid);
 	}
 
 }
