 /*
  * JGraLab - The Java Graph Laboratory
  * 
  * Copyright (C) 2006-2011 Institute for Software Technology
  *                         University of Koblenz-Landau, Germany
  *                         ist@uni-koblenz.de
  * 
  * For bug reports, documentation and further information, visit
  * 
  *                         http://jgralab.uni-koblenz.de
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
 
 package de.uni_koblenz.jgralab.greql2.evaluator.fa;
 
 import java.rmi.RemoteException;
 import java.util.Set;
 
 
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.graphmarker.ObjectGraphMarker;
 import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ThisEdgeEvaluator;
 import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
 import de.uni_koblenz.jgralab.greql2.schema.IncDirection;
 import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
 import de.uni_koblenz.jgralab.greql2.types.TypeCollection;
 import de.uni_koblenz.jgralab.schema.TypedElementClass;
 
 /**
  * This transition is used only in the GReQL 2 Java Code Generator as long 
  * as the SimpleIncidenceTransition developed by Jon Theegarten in his
  * diploma thesis is not finished.
  * 
  */
 public class SimpleIncidenceTransition_Db extends Transition {
 
 	protected VertexEvaluator predicateEvaluator;
 	
 	public VertexEvaluator getPredicateEvaluator() {
 		return predicateEvaluator;
 	}
 
 	protected ThisEdgeEvaluator thisEdgeEvaluator;
 
 	/**
 	 * The collection of types that are accepted by this transition
 	 */
 	protected TypeCollection typeCollection;
 
 	public TypeCollection getTypeCollection() {
 		return typeCollection;
 	}
 	
 	/**
 	 * this transition may accept edges in direction in, out or any
 	 */
 	protected IncDirection validDirection;
 	
 	
 	public IncDirection getAllowedDirection() {
 		return validDirection;
 	}
 
 	/**
 	 * returns a string which describes the edge
 	 */
 	@Override
 	public String edgeString() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * greql2.evaluator.fa.Transition#equalSymbol(greql2.evaluator.fa.EdgeTransition
 	 * )
 	 */
 	@Override
 	public boolean equalSymbol(Transition t) {
 		if (t instanceof SimpleIncidenceTransition_Db) {
 			SimpleIncidenceTransition_Db st = (SimpleIncidenceTransition_Db) t;
 			if ((validDirection == st.getAllowedDirection()) && (typeCollection == st.getTypeCollection()) && (predicateEvaluator == st.getPredicateEvaluator()))
 					return true;
 		}			
 		return  false;
 	}
 
 	/**
 	 * Copy-constructor, creates a copy of the given transition
 	 */
 	protected SimpleIncidenceTransition_Db(SimpleIncidenceTransition_Db t, boolean addToStates) {
 		super(t, addToStates);
 		validDirection = t.validDirection;
 		typeCollection = new TypeCollection(t.typeCollection);
 		predicateEvaluator = t.predicateEvaluator;
 		thisEdgeEvaluator = t.thisEdgeEvaluator;
 	}
 
 	/**
 	 * returns a copy of this transition
 	 */
 	@Override
 	public Transition copy(boolean addToStates) {
 		return new SimpleIncidenceTransition_Db(this, addToStates);
 	}
 
 	/**
 	 * Creates a new transition from start state to end state. The Transition
 	 * accepts all edges that have the right direction, role, startVertexType,
 	 * endVertexType, edgeType and even it's possible to define a specific edge.
 	 * 
 	 * @param start
 	 *            The state where this transition starts
 	 * @param end
 	 *            The state where this transition ends
 	 * @param dir
 	 *            The direction of the accepted edges, may be EdeDirection.IN,
 	 *            EdgeDirection.OUT or EdgeDirection.ANY
 	 */
 	public SimpleIncidenceTransition_Db(State start, State end, IncDirection dir, TypeCollection typeColl) {
 		super(start, end);
 		validDirection = dir;
 		typeCollection = typeColl;
 		
 	}
 	
 	public SimpleIncidenceTransition_Db(State start, State end, IncDirection dir) {
 		super(start, end);
 		validDirection = dir;
 		typeCollection = null;
 		
 	}
 
 	/**
 	 * Creates a new transition from start state to end state. The Transition
 	 * accepts all edges that have the right direction, role, startVertexType,
 	 * endVertexType, edgeType and even it's possible to define a specific edge.
 	 * This constructor creates a transition to accept a simplePathDescription
 	 * 
 	 * @param start
 	 *            The state where this transition starts
 	 * @param end
 	 *            The state where this transition ends
 	 * @param dir
 	 *            The direction of the accepted edges, may be EdeDirection.IN,
 	 *            EdgeDirection.OUT or EdgeDirection.ANY
 	 * @param typeCollection
 	 *            The types which restrict the possible edges
 	 * @param roles
 	 *            The set of accepted edge role names, or null if any role is
 	 *            accepted
 	 */
 	public SimpleIncidenceTransition_Db(State start, State end, IncDirection dir,
 			TypeCollection typeCollection, Set<String> roles,
 			VertexEvaluator predicateEvaluator,
 			ObjectGraphMarker<Vertex, VertexEvaluator> graphMarker) {
 		super(start, end);
 		validDirection = dir;
 
 		this.typeCollection = typeCollection;
 		this.predicateEvaluator = predicateEvaluator;
 		Vertex v;
 		try {
 			v = graphMarker.getGraph().getFirstVertex(ThisEdge.class);
 		} catch (RemoteException ex) {
 			throw new RuntimeException(ex);
 		}
 		if (v != null) {
 			thisEdgeEvaluator = (ThisEdgeEvaluator) graphMarker.getMark(v);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see greql2.evaluator.fa.Transition#reverse()
 	 */
 	@Override
 	public void reverse() {
 		super.reverse();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see greql2.evaluator.fa.Transition#isEpsilon()
 	 */
 	@Override
 	public boolean isEpsilon() {
 		return false;
 	}
 
 
 	@Deprecated
 	public boolean accepts(GraphElement elem, Incidence inc) {
 		return false;
 	}
 
 	/**
 	 * returns the vertex of the datagraph which can be visited after this
 	 * transition has fired. This is the vertex at the end of the edge
 	 */
 	@Override
 	public Vertex getNextElement(GraphElement elem, Incidence inc) {
 		return inc.getThat();
 	}
 
 	@Override
 	public String prettyPrint() {
 		return "!";
 	}
 
 	@Override
 	public boolean consumesIncidence() {
 		return true;
 	}
 
 	@Override
 	public boolean accepts(Vertex v, Incidence i) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean accepts(Edge e, Incidence i) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
