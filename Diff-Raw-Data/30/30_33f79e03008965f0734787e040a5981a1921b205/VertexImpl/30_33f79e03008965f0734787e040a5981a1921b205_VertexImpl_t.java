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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.impl.IncidenceIterableAtVertex;
 import de.uni_koblenz.jgralab.impl.IncidentEdgeIterable;
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.IncidenceType;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 /**
  * Implementation of all methods of the interface {@link Vertex} which are
  * independent of the fields of a specific VertexImpl.
  * 
  * Example: getNextVertex(): local: public Vertex getNextVertex() { long nextId
  * = container.nextVertex[getLocalId(this.elementId)]; return
  * localGraphDb.getVertexObject(nextId); }
  * 
  * remote: public Vertex getNextVertex() { long nextId =
  * remoteDiskStorage.getNextVertex(getLocalId(this.elementId)]; return
  * localGraphDb.getVertexObject(nextId); }
  * 
  * 
  * @author ist@uni-koblenz.de
  */
 public abstract class VertexImpl extends
 		GraphElementImpl<VertexClass, Vertex, Edge> implements Vertex {
 
 	protected VertexContainer container;
 
 	/**
 	 * Creates a new {@link Vertex} instance.
 	 * 
 	 * @param id
 	 *            int the id of the vertex
 	 * @param graph
 	 *            {@link Graph} its corresponding graph
 	 * @throws IOException
 	 */
 	protected VertexImpl(long id, GraphDatabaseBaseImpl localGraphDatabase)
 			throws IOException {
 		super(localGraphDatabase);
 		this.elementId = id;
 		this.container = localGraphDatabase.getLocalDiskStorage()
 				.getVertexContainer(getLocalId());
 	}
 
 	protected VertexImpl(long id, GraphDatabaseBaseImpl localGraphDatabase,
 			VertexContainer container) throws IOException {
 		super(localGraphDatabase);
 		this.elementId = id;
 		this.container = container;
 	}
 
 	/* **********************************************************
 	 * Access id *********************************************************
 	 */
 
 	@Override
 	public final long getGlobalId() {
 		return elementId;
 	}
 
 	/* **********************************************************
 	 * Access next and previous elements in Vseq
 	 * *********************************************************
 	 */
 
 	@Override
 	public Vertex getNextVertex() {
 		assert isValid();
 		return getNextVertex(localGraphDatabase.getTraversalContext());
 	}
 
 	@Override
 	public Vertex getPreviousVertex() {
 		assert isValid();
 		return getPreviousVertex(localGraphDatabase.getTraversalContext());
 	}
 
 	@Override
 	public Vertex getNextVertex(Graph traversalContext) {
 		assert isValid();
 		if (container.nextElementInGraphId[getIdInStorage(elementId)] == 0) {
 			return null;
 		} else if ((traversalContext == null)
 				|| traversalContext
 						.containsVertex(localGraphDatabase
 								.getVertexObject(container.nextElementInGraphId[getIdInStorage(elementId)]))) {
 			return localGraphDatabase
 					.getVertexObject(container.nextElementInGraphId[getIdInStorage(elementId)]);
 		} else {
 			return localGraphDatabase.getVertexObject(
 					container.nextElementInGraphId[getIdInStorage(elementId)])
 					.getNextVertex(traversalContext);
 		}
 	}
 
 	@Override
 	public Vertex getPreviousVertex(Graph traversalContext) {
 		assert isValid();
 		if (container.previousElementInGraphId[getIdInStorage(elementId)] == 0) {
 			return null;
 		} else if ((traversalContext == null)
 				|| traversalContext
 						.containsVertex(localGraphDatabase
 								.getVertexObject(container.previousElementInGraphId[getIdInStorage(elementId)]))) {
 			return localGraphDatabase
 					.getVertexObject(container.previousElementInGraphId[getIdInStorage(elementId)]);
 		} else {
 			return localGraphDatabase
 					.getVertexObject(
 							container.previousElementInGraphId[getIdInStorage(elementId)])
 					.getPreviousVertex(traversalContext);
 		}
 	}
 
 	@Override
 	public <T extends Vertex> T getNextVertex(Class<T> vertexClass) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(localGraphDatabase.getTraversalContext(),
 				vertexClass, false);
 	}
 
 	@Override
 	public <T extends Vertex> T getNextVertex(Class<T> m1VertexClass,
 			boolean noSubclasses) {
 		assert m1VertexClass != null;
 		assert isValid();
 		return getNextVertex(localGraphDatabase.getTraversalContext(),
 				m1VertexClass, noSubclasses);
 	}
 
 	@Override
 	public Vertex getNextVertex(VertexClass vertexClass) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(localGraphDatabase.getTraversalContext(),
 				vertexClass.getM1Class(), false);
 	}
 
 	@Override
 	public Vertex getNextVertex(VertexClass vertexClass, boolean noSubclasses) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(localGraphDatabase.getTraversalContext(),
 				vertexClass.getM1Class(), noSubclasses);
 	}
 
 	@Override
 	public <T extends Vertex> T getNextVertex(Graph traversalContext,
 			Class<T> vertexClass) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(vertexClass, false);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Vertex> T getNextVertex(Graph traversalContext,
 			Class<T> m1VertexClass, boolean noSubclasses) {
 		assert m1VertexClass != null;
 		assert isValid();
 		VertexImpl v = (VertexImpl) getNextVertex();
 		while (v != null) {
 			if (noSubclasses) {
 				if (m1VertexClass == v.getM1Class()) {
 					return (T) v;
 				}
 			} else {
 				if (m1VertexClass.isInstance(v)) {
 					return (T) v;
 				}
 			}
 			v = (VertexImpl) v.getNextVertex();
 		}
 		return null;
 	}
 
 	@Override
 	public Vertex getNextVertex(Graph traversalContext, VertexClass vertexClass) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(vertexClass.getM1Class(), false);
 	}
 
 	@Override
 	public Vertex getNextVertex(Graph traversalContext,
 			VertexClass vertexClass, boolean noSubclasses) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(vertexClass.getM1Class(), noSubclasses);
 	}
 
 	/**
 	 * Puts <code>nextVertex</code> after this {@link Vertex} in the sequence of
 	 * all vertices in the graph.
 	 * 
 	 * @param nextVertex
 	 *            {@link Vertex}which should be put after this {@link Vertex}
 	 */
 	protected void setNextVertex(Vertex nextVertex) {
 		container.nextElementInGraphId[getIdInStorage(elementId)] = nextVertex
 				.getGlobalId();
 	}
 
 	/**
 	 * Puts <code>prevVertex</code> before this {@link Vertex} in the sequence
 	 * of all vertices in the graph.
 	 * 
 	 * @param prevVertex
 	 *            {@link Vertex}which should be put before this {@link Vertex}
 	 */
 	protected void setPreviousVertex(Vertex prevVertex) {
 		container.previousElementInGraphId[getIdInStorage(elementId)] = prevVertex
 				.getGlobalId();
 	}
 
 	@Override
 	public boolean isBefore(Vertex v) {
 		assert v != null;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		if (this == v) {
 			return false;
 		}
 		Vertex prev = v.getPreviousVertex();
 		while ((prev != null) && (prev != this)) {
 			prev = v.getPreviousVertex();
 		}
 		return prev != null;
 	}
 
 	@Override
 	public void putBefore(Vertex v) {
 		assert v != null;
 		assert v != this;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		storingGraphDatabase.putVertexBefore(v.getGlobalId(),
 				this.getGlobalId());
 	}
 
 	@Override
 	public boolean isAfter(Vertex v) {
 		assert v != null;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		if (this == v) {
 			return false;
 		}
 		VertexImpl next = (VertexImpl) v.getNextVertex();
 		while ((next != null) && (next != this)) {
 			next = (VertexImpl) next.getNextVertex();
 		}
 		return next != null;
 	}
 
 	@Override
 	public void putAfter(Vertex v) {
 		assert v != null;
 		assert v != this;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		storingGraphDatabase
 				.putVertexAfter(v.getGlobalId(), this.getGlobalId());
 	}
 
 	/* **********************************************************
 	 * Access first and last element in Lambda_seq
 	 * *********************************************************
 	 */
 
 	@Override
 	public final Incidence getFirstIncidence() {
 		return getFirstIncidence(localGraphDatabase.getTraversalContext());
 	}
 
 	@Override
 	public Incidence getLastIncidence() {
 		return getLastIncidence(localGraphDatabase.getTraversalContext());
 	}
 
 	@Override
 	public Incidence getFirstIncidence(Graph traversalContext) {
 		Incidence firstIncidence = localGraphDatabase
 				.getIncidenceObject(container.firstIncidenceId[getIdInStorage(elementId)]);
 		while ((firstIncidence != null) && (traversalContext != null)
 				&& (!traversalContext.containsEdge(firstIncidence.getEdge()))) {
 			firstIncidence = firstIncidence.getNextIncidenceAtVertex();
 		}
 		return firstIncidence;
 	}
 
 	@Override
 	public Incidence getLastIncidence(Graph traversalContext) {
 		Incidence lastIncidence = localGraphDatabase
 				.getIncidenceObject(container.lastIncidenceId[getIdInStorage(elementId)]);
 		if ((lastIncidence == null) || (traversalContext == null)
 				|| (traversalContext.containsVertex(lastIncidence.getVertex()))) {
 			return lastIncidence;
 		} else {
 			return lastIncidence.getPreviousIncidenceAtVertex(traversalContext);
 		}
 	}
 
 	@Override
 	public final Incidence getFirstIncidence(Direction direction) {
 		assert isValid();
 		return getFirstIncidence(localGraphDatabase.getTraversalContext(),
 				direction);
 	}
 
 	@Override
 	public final Incidence getFirstIncidence(Graph traversalContext,
 			Direction direction) {
 		assert isValid();
 		Incidence i = localGraphDatabase
 				.getIncidenceObject(container.firstIncidenceId[getIdInStorage(elementId)]);
 		if (traversalContext == null) {
 			while (((i != null) && (direction != null)
 					&& (direction != Direction.BOTH) && (direction != i
 						.getDirection()))) {
 				i = i.getNextIncidenceAtVertex();
 			}
 		} else {
 			if ((direction != null) && (direction != Direction.BOTH)) {
 				while ((i != null)
 						&& ((!traversalContext.containsEdge(i.getEdge())) || (direction != i
 								.getDirection()))) {
 					i = i.getNextIncidenceAtVertex();
 				}
 			} else {
 				while ((i != null)
 						&& (!traversalContext.containsEdge(i.getEdge()))) {
 					i = i.getNextIncidenceAtVertex();
 				}
 			}
 
 		}
 		return i;
 	}
 
 	@Override
 	public Incidence getFirstIncidence(boolean thisIncidence,
 			IncidenceType... incidentTypes) {
 		assert isValid();
 		return getFirstIncidence(localGraphDatabase.getTraversalContext(),
 				thisIncidence, incidentTypes);
 	}
 
 	@Override
 	public <T extends Incidence> T getFirstIncidence(Class<T> anIncidenceClass,
 			Direction direction, boolean noSubclasses) {
 		assert anIncidenceClass != null;
 		assert isValid();
 		return getFirstIncidence(localGraphDatabase.getTraversalContext(),
 				anIncidenceClass, direction, noSubclasses);
 	}
 
 	@Override
 	public Incidence getFirstIncidence(Graph traversalContext,
 			boolean thisIncidence, IncidenceType... incidentTypes) {
 		assert isValid();
 		Incidence i = getFirstIncidence();
 		if (incidentTypes.length == 0) {
 			return i;
 		}
 		while (i != null) {
 			for (IncidenceType element : incidentTypes) {
 				if ((thisIncidence ? i.getThisSemantics() : i
 						.getThatSemantics()) == element) {
 					return i;
 				}
 			}
 			i = i.getNextIncidenceAtVertex();
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Incidence> T getFirstIncidence(Graph traversalContext,
 			Class<T> anIncidenceClass, Direction direction, boolean noSubclasses) {
 		assert anIncidenceClass != null;
 		assert isValid();
 		Incidence currentIncidence = getFirstIncidence(traversalContext,
 				direction);
 		while (currentIncidence != null) {
 			if (noSubclasses) {
 				if (anIncidenceClass == currentIncidence.getM1Class()) {
 					return (T) currentIncidence;
 				}
 			} else {
 				if (anIncidenceClass.isInstance(currentIncidence)) {
 					return (T) currentIncidence;
 				}
 			}
 			currentIncidence = currentIncidence.getNextIncidenceAtVertex(
 					traversalContext, direction);
 		}
 		return null;
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences() {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this, direction);
 	}
 
 	@Override
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Class<T> anIncidenceClass) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<T>(this, anIncidenceClass);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(IncidenceClass anIncidenceClass) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this,
 				anIncidenceClass.getM1Class());
 	}
 
 	@Override
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Class<T> anIncidenceClass, Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<T>(this, anIncidenceClass,
 				direction);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(IncidenceClass anIncidenceClass,
 			Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this,
 				anIncidenceClass.getM1Class(), direction);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(Graph traversalContext) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(traversalContext, this);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(Graph traversalContext,
 			Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(traversalContext, this,
 				direction);
 	}
 
 	@Override
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Graph traversalContext, Class<T> anIncidenceClass) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<T>(traversalContext, this,
 				anIncidenceClass);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(Graph traversalContext,
 			IncidenceClass anIncidenceClass) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(traversalContext, this,
 				anIncidenceClass.getM1Class());
 	}
 
 	@Override
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Graph traversalContext, Class<T> anIncidenceClass,
 			Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<T>(traversalContext, this,
 				anIncidenceClass, direction);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(Graph traversalContext,
 			IncidenceClass anIncidenceClass, Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(traversalContext, this,
 				anIncidenceClass.getM1Class(), direction);
 	}
 
 	@Override
 	public Incidence connect(String rolename, Edge elemToConnect) {
 		return elemToConnect.connect(getIncidenceClassForRolename(rolename),
 				this);
 	}
 
 	@Override
 	public Incidence connect(IncidenceClass incidenceClass, Edge elemToConnect) {
 		return elemToConnect.connect(incidenceClass.getM1Class(), this);
 	}
 
 	@Override
 	public <T extends Incidence> T connect(Class<T> incidenceClass,
 			Edge elemToConnect) {
 		return elemToConnect.connect(incidenceClass, this);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Incidence> T connect(Class<T> incidenceClass,
 			Edge elemToConnect, long globalIdOfIncidence) {
 		return (T) localGraphDatabase.getIncidenceObject(storingGraphDatabase
 				.connect(getSchema().getClassId(incidenceClass),
 						this.getGlobalId(), elemToConnect.getGlobalId(),
 						globalIdOfIncidence));
 	}
 

 
 	/* **********************************************************
 	 * Access sigma and kappa information
 	 * *********************************************************
 	 */
 
 	@Override
 	public Graph getSubordinateGraph() {
 		if (subordinateGraphId == 0) {
 			Graph subordinateGraph = localGraphDatabase.getGraphFactory()
 					.createSubordinateGraph(this);
 			subordinateGraphId = subordinateGraph.getGlobalId();
 			return subordinateGraph;
 		} else {
 			return localGraphDatabase.getGraphObject(subordinateGraphId);
 		}
 	}
 
 	@Override
 	public GraphElement<?, ?, ?> getSigma() {
 		long sigmaId = container.sigmaId[getIdInStorage(elementId)];
 		if (sigmaId < 0) {
 			return localGraphDatabase.getEdgeObject(-sigmaId);
 		} else {
 			return localGraphDatabase.getVertexObject(sigmaId);
 		}
 	}
 
 	@Override
 	public void setSigma(GraphElement<?, ?, ?> elem) {
 		long sigmaId = elem.getGlobalId();
 		if (elem instanceof Edge) {
 			container.sigmaId[getIdInStorage(elementId)] = -sigmaId;
 		} else {
 			container.sigmaId[getIdInStorage(elementId)] = sigmaId;
 		}
 	}
 
 	@Override
 	public int getKappa() {
 		return container.kappa[getIdInStorage(elementId)];
 	}
 
 	@Override
 	public void setKappa(int kappa) {
 		assert getType().getAllowedMaxKappa() >= kappa
 				&& getType().getAllowedMinKappa() <= kappa;
 		container.kappa[getIdInStorage(elementId)] = kappa;
 	}
 
 	/* **********************************************************
 	 * Access alpha and omega information
 	 * *********************************************************
 	 */
 
 	@Override
 	public Iterable<Edge> getAlphaEdges() {
 		return new IncidentEdgeIterable<Edge>(this, Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public Iterable<Edge> getAlphaEdges(EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class(),
 				Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getAlphaEdges(Class<T> anEdgeClass) {
 		return new IncidentEdgeIterable<T>(this, anEdgeClass,
 				Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public Iterable<Edge> getAlphaEdges(Graph traversalContext) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this,
 				Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public Iterable<Edge> getAlphaEdges(Graph traversalContext,
 			EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this,
 				anEdgeClass.getM1Class(), Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getAlphaEdges(Graph traversalContext,
 			Class<T> anEdgeClass) {
 		return new IncidentEdgeIterable<T>(traversalContext, this, anEdgeClass,
 				Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public Iterable<Edge> getOmegaEdges() {
 		return new IncidentEdgeIterable<Edge>(this, Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public Iterable<Edge> getOmegaEdges(EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class(),
 				Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getOmegaEdges(Class<T> anEdgeClass) {
 		return new IncidentEdgeIterable<T>(this, anEdgeClass,
 				Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public Iterable<Edge> getOmegaEdges(Graph traversalContext) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this,
 				Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public Iterable<Edge> getOmegaEdges(Graph traversalContext,
 			EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this,
 				anEdgeClass.getM1Class(), Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getOmegaEdges(Graph traversalContext,
 			Class<T> anEdgeClass) {
 		return new IncidentEdgeIterable<T>(traversalContext, this, anEdgeClass,
 				Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges() {
 		return new IncidentEdgeIterable<Edge>(this);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Direction direction) {
 		return new IncidentEdgeIterable<Edge>(this, direction);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class());
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getIncidentEdges(Class<T> anEdgeClass) {
 		return new IncidentEdgeIterable<T>(this, anEdgeClass);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(EdgeClass anEdgeClass,
 			Direction direction) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class(),
 				direction);
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getIncidentEdges(Class<T> anEdgeClass,
 			Direction direction) {
 		return new IncidentEdgeIterable<T>(this, anEdgeClass, direction);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Graph traversalContext) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Graph traversalContext,
 			Direction direction) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this, direction);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Graph traversalContext,
 			EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this,
 				anEdgeClass.getM1Class());
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getIncidentEdges(
 			Graph traversalContext, Class<T> anEdgeClass) {
 		return new IncidentEdgeIterable<T>(traversalContext, this, anEdgeClass);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Graph traversalContext,
 			EdgeClass anEdgeClass, Direction direction) {
 		return new IncidentEdgeIterable<Edge>(traversalContext, this,
 				anEdgeClass.getM1Class(), direction);
 	}
 
 	@Override
 	public <T extends Edge> Iterable<T> getIncidentEdges(
 			Graph traversalContext, Class<T> anEdgeClass, Direction direction) {
 		return new IncidentEdgeIterable<T>(traversalContext, this, anEdgeClass,
 				direction);
 	}
 
 	@Override
 	public boolean isValid() {
 		return localGraphDatabase.containsVertex(this);
 	}
 
 	@Override
 	public int getDegree() {
 		return getDegree(localGraphDatabase.getTraversalContext());
 	}
 
 	@Override
 	public int getDegree(Direction direction) {
 		return getDegree(localGraphDatabase.getTraversalContext(), direction);
 	}
 
 	@Override
 	public int getDegree(IncidenceClass ic, boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		return getDegree(localGraphDatabase.getTraversalContext(), ic,
 				noSubClasses);
 	}
 
 	@Override
 	public int getDegree(Class<? extends Incidence> ic, boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		return getDegree(localGraphDatabase.getTraversalContext(), ic,
 				noSubClasses);
 	}
 
 	@Override
 	public int getDegree(IncidenceClass ic, Direction direction,
 			boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		return getDegree(localGraphDatabase.getTraversalContext(), ic,
 				direction, noSubClasses);
 	}
 
 	@Override
 	public int getDegree(Class<? extends Incidence> ic, Direction direction,
 			boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		return getDegree(localGraphDatabase.getTraversalContext(), ic,
 				direction, noSubClasses);
 	}
 
 	@Override
 	public int getDegree(Graph traversalContext) {
 		int d = 0;
 		Incidence i = getFirstIncidence(traversalContext);
 		while (i != null) {
 			d++;
 			i = i.getNextIncidenceAtVertex(traversalContext);
 		}
 		return d;
 	}
 
 	@Override
 	public int getDegree(Graph traversalContext, Direction direction) {
 		if (direction == Direction.BOTH || (direction == null)) {
 			return getDegree(traversalContext);
 		}
 		int d = 0;
 		Incidence i = getFirstIncidence(traversalContext);
 		while (i != null) {
 			if (direction == Direction.BOTH || i.getDirection() == direction) {
 				d++;
 			}
 			i = i.getNextIncidenceAtVertex(traversalContext);
 		}
 		return d;
 	}
 
 	@Override
 	public int getDegree(Graph traversalContext, IncidenceClass ic,
 			boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(traversalContext, ic, noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(traversalContext, ic, noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public int getDegree(Graph traversalContext, Class<? extends Incidence> ic,
 			boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(traversalContext, ic, noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(traversalContext, ic, noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public int getDegree(Graph traversalContext, IncidenceClass ic,
 			Direction direction, boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(traversalContext, ic, direction,
 				noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(traversalContext, ic, direction,
 					noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public int getDegree(Graph traversalContext, Class<? extends Incidence> ic,
 			Direction direction, boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(traversalContext, ic, direction,
 				noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(traversalContext, ic, direction,
 					noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public String toString() {
 		assert isValid();
 		return "v" + elementId + ": " + getType().getQualifiedName();
 	}
 
 	@Override
 	public int compareTo(Vertex v) {
 		assert isValid() && v.isValid();
 		assert getGraph() == v.getGraph();
 		return (int) (getGlobalId() - v.getGlobalId());
 	}
 
 	@Override
 	public void delete() {
 		assert isValid() : this + " is not valid!";
 		storingGraphDatabase.deleteVertex(this.getGlobalId());
 	}
 
 	@Override
 	public void putIncidenceAfter(Incidence target, Incidence moved) {
 		storingGraphDatabase.putIncidenceIdAfterAtVertexId(
 				target.getGlobalId(), moved.getGlobalId());
 	}
 
 	@Override
 	public void putIncidenceBefore(Incidence target, Incidence moved) {
 		storingGraphDatabase.putIncidenceIdBeforeAtVertexId(
 				target.getGlobalId(), moved.getGlobalId());
 	}
 
 	public void deleteIncidence(Incidence i) {
 		storingGraphDatabase.deleteIncidence(i.getGlobalId());
 	}
 
 	// @Override
 	// //TODO: Move to storing graph database
 	@Override
 	public void sortIncidences(Comparator<Incidence> comp) {
 		throw new RuntimeException("Not yet implemented");
 		// assert isValid();
 		//
 		// if (getFirstIncidence() == null) {
 		// // no sorting required for empty incidence lists
 		// return;
 		// }
 		// class IncidenceList {
 		// IncidenceImpl first;
 		// IncidenceImpl last;
 		//
 		// public void add(IncidenceImpl i) {
 		// if (first == null) {
 		// first = i;
 		// assert (last == null);
 		// last = i;
 		// } else {
 		// i.setPreviousIncidenceAtVertex(last);
 		// last.setNextIncidenceAtVertex(i);
 		// last = i;
 		// }
 		// i.setNextIncidenceAtVertex(null);
 		// }
 		//
 		// public IncidenceImpl remove() {
 		// if (first == null) {
 		// throw new NoSuchElementException();
 		// }
 		// IncidenceImpl out;
 		// if (first == last) {
 		// out = first;
 		// first = null;
 		// last = null;
 		// return out;
 		// }
 		// out = first;
 		// first = (IncidenceImpl) out.getNextIncidenceAtVertex();
 		// first.setPreviousIncidenceAtVertex(null);
 		// return out;
 		// }
 		//
 		// public boolean isEmpty() {
 		// assert ((first == null) == (last == null));
 		// return first == null;
 		// }
 		//
 		// }
 		//
 		// IncidenceList a = new IncidenceList();
 		// IncidenceList b = new IncidenceList();
 		// IncidenceList out = a;
 		//
 		// // split
 		// IncidenceImpl last;
 		// IncidenceList l = new IncidenceList();
 		// l.first = (IncidenceImpl) getFirstIncidence();
 		// l.last = (IncidenceImpl) getLastIncidence();
 		//
 		// out.add(last = l.remove());
 		// while (!l.isEmpty()) {
 		// IncidenceImpl current = l.remove();
 		// if (comp.compare(current, last) < 0) {
 		// out = (out == a) ? b : a;
 		// }
 		// out.add(current);
 		// last = current;
 		// }
 		// if (a.isEmpty() || b.isEmpty()) {
 		// out = a.isEmpty() ? b : a;
 		// storingGraphDatabase.setFirstIncidenceId(elementId,
 		// out.first.getId());
 		// storingGraphDatabase.setLastIncidenceId(elementId, out.last.getId());
 		// return;
 		// }
 		//
 		// while (true) {
 		// if (a.isEmpty() || b.isEmpty()) {
 		// out = a.isEmpty() ? b : a;
 		// storingGraphDatabase.setFirstIncidenceOfVertexId(elementId,
 		// out.first.getId());
 		// storingGraphDatabase.setLastIncidenceId(elementId, out.last.getId());
 		// storingGraphDatabase.incidenceListModified(elementId);
 		// return;
 		// }
 		//
 		// IncidenceList c = new IncidenceList();
 		// IncidenceList d = new IncidenceList();
 		// out = c;
 		//
 		// last = null;
 		// while (!a.isEmpty() && !b.isEmpty()) {
 		// int compareAToLast = last != null ? comp.compare(a.first, last)
 		// : 0;
 		// int compareBToLast = last != null ? comp.compare(b.first, last)
 		// : 0;
 		//
 		// if ((compareAToLast >= 0) && (compareBToLast >= 0)) {
 		// if (comp.compare(a.first, b.first) <= 0) {
 		// out.add(last = a.remove());
 		// } else {
 		// out.add(last = b.remove());
 		// }
 		// } else if ((compareAToLast < 0) && (compareBToLast < 0)) {
 		// out = (out == c) ? d : c;
 		// last = null;
 		// } else if ((compareAToLast < 0) && (compareBToLast >= 0)) {
 		// out.add(last = b.remove());
 		// } else {
 		// out.add(last = a.remove());
 		// }
 		// }
 		//
 		// // copy rest of A
 		// while (!a.isEmpty()) {
 		// IncidenceImpl current = a.remove();
 		// if (comp.compare(current, last) < 0) {
 		// out = (out == c) ? d : c;
 		// }
 		// out.add(current);
 		// last = current;
 		// }
 		//
 		// // copy rest of B
 		// while (!b.isEmpty()) {
 		// IncidenceImpl current = b.remove();
 		// if (comp.compare(current, last) < 0) {
 		// out = (out == c) ? d : c;
 		// }
 		// out.add(current);
 		// last = current;
 		// }
 		//
 		// a = c;
 		// b = d;
 		// }
 		//
 	}
 
 	@Override
 	public List<? extends Vertex> getAdjacences(String role) {
 		return getAdjacences(localGraphDatabase.getTraversalContext(),
 				getIncidenceClassForRolename(role));
 	}
 
 	@Override
 	public List<? extends Vertex> getAdjacences(IncidenceClass ic) {
 		assert ic != null;
 		assert isValid();
 		return getAdjacences(localGraphDatabase.getTraversalContext(), ic);
 	}
 
 	@Override
 	public List<? extends Vertex> getAdjacences(Graph traversalContext,
 			String role) {
 		return getAdjacences(getIncidenceClassForRolename(role));
 	}
 
 	@Override
 	public List<? extends Vertex> getAdjacences(Graph traversalContext,
 			IncidenceClass ic) {
 		assert ic != null;
 		assert isValid();
 		List<Vertex> adjacences = new ArrayList<Vertex>();
 		Class<? extends Edge> ec = ic.getEdgeClass().getM1Class();
 		Direction dir = ic.getDirection();
 		for (Edge e : getIncidentEdges(traversalContext, ec, dir)) {
 			for (Vertex v : e.getIncidentVertices(traversalContext,
 					dir == Direction.EDGE_TO_VERTEX ? Direction.VERTEX_TO_EDGE
 							: Direction.EDGE_TO_VERTEX)) {
 				adjacences.add(v);
 			}
 		}
 		return adjacences;
 	}
 
 	@Override
 	public Edge addAdjacence(String incidentRole, String adjacentRole,
 			Vertex other) {
 		return addAdjacence(getIncidenceClassForRolename(incidentRole),
 				getIncidenceClassForRolename(adjacentRole), other);
 	}
 
 	@Override
 	public Edge addAdjacence(IncidenceClass incidentIc,
 			IncidenceClass adjacentIc, Vertex other) {
 		assert incidentIc != null;
 		assert adjacentIc != null;
 		assert isValid();
 		assert other.isValid();
 		assert getGraph() == other.getGraph();
 
 		EdgeClass entry = incidentIc.getEdgeClass();
 		Class<? extends Edge> ec = entry.getM1Class();
 
 		assert adjacentIc.getEdgeClass() == entry;
 
 		Edge e = getGraph().createEdge(ec);
 		e.connect(incidentIc, this);
 		e.connect(adjacentIc, other);
 
 		localGraphDatabase.edgeListModified();
 		return e;
 	}
 
 	@Override
 	public List<Vertex> removeAdjacences(String role) {
 		return removeAdjacences(getIncidenceClassForRolename(role));
 	}
 
 	@Override
 	public List<Vertex> removeAdjacences(IncidenceClass ic) {
 		// TODO (graph and incidencelists modified)
 		return null;
 		// assert (role != null) && (role.length() > 0);
 		// assert isValid();
 		//
 		// DirectedM1EdgeClass entry = getEdgeForRolename(role);
 		// Class<? extends Edge> ec = entry.getM1Class();
 		// List<Vertex> adjacences = new ArrayList<Vertex>();
 		// List<Edge> deleteList = new ArrayList<Edge>();
 		// Direction dir = entry.getDirection();
 		// for (Edge e : incidences(ec, dir)) {
 		// deleteList.add(e);
 		// adjacences.add(e.getThat());
 		// }
 		// for (Edge e : deleteList) {
 		// e.delete();
 		// }
 		// return adjacences;
 	}
 
 	@Override
 	public void removeAdjacence(String role, Vertex other) {
 		removeAdjacence(getIncidenceClassForRolename(role), other);
 	}
 
 	@Override
 	public void removeAdjacence(IncidenceClass ic, Vertex other) {
 		// TODO (graph and incidencelists modified)
 		// assert (role != null) && (role.length() > 0);
 		// assert isValid();
 		// assert other.isValid();
 		// assert getGraph() == other.getGraph();
 		//
 		// DirectedM1EdgeClass entry = getEdgeForRolename(role);
 		// Class<? extends Edge> ec = entry.getM1Class();
 		// List<Edge> deleteList = new ArrayList<Edge>();
 		// Direction dir = entry.getDirection();
 		// for (Edge e : incidences(ec, dir)) {
 		// if (e.getThat() == other) {
 		// deleteList.add(e);
 		// }
 		// }
 		// for (Edge e : deleteList) {
 		// e.delete();
 		// }
 	}
 
 	@Override
 	protected void addFirstSubordinateEdge(Edge appendix) {
 		return;
 	}
 
 	@Override
 	protected void addFirstSubordinateVertex(Vertex appendix) {
 		appendix.putAfter(this);
 	}
 
 	/**
 	 * @return long the internal incidence list version
 	 * @see #isIncidenceListModified(long)
 	 */
 	@Override
 	public final long getIncidenceListVersion() {
 		assert isValid();
 		return container.incidenceListVersion[getIdInStorage(elementId)];
 	}
 
 }
