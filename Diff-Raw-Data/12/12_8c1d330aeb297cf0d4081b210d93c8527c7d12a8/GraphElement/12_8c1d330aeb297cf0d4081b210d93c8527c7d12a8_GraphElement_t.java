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
 
 package de.uni_koblenz.jgralab;
 
 import java.rmi.RemoteException;
 import java.util.List;
 
 import de.uni_koblenz.jgralab.impl.mem.GraphElementImpl;
 import de.uni_koblenz.jgralab.schema.GraphElementClass;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.IncidenceType;
 
 /**
  * Aggregates vertices and edges.
  * 
  * @author ist@uni-koblenz.de
  * 
  * @param <OwnType>
  *            This parameter must be either {@link Vertex} in case of a vertex
  *            or {@link Edge} in case of an edge.
  * @param <DualType>
  *            If <code>&lt;OwnType&gt;</code> is {@link Vertex} this parameter
  *            must be {@link Edge}. Otherwise it has to be {@link Vertex}.
  */
 
 public interface GraphElement<OwnTypeClass extends GraphElementClass<OwnTypeClass, OwnType>, OwnType extends GraphElement<OwnTypeClass, OwnType, DualType>, DualType extends GraphElement<?, DualType, OwnType>>
 		extends AttributedElement<OwnTypeClass, OwnType> {
 
 	/**
 	 * Returns <code>true</code> if this {@link GraphElement} is still present
 	 * in the {@link Graph} (i.e. not deleted). This check is equivalent to
 	 * <code>getGraph().containsVertex(this)</code> or
 	 * <code>getGraph().containsEdge(this)</code>.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isValid();
 
 	/**
 	 * Returns the graph containing this {@link GraphElement}. TODO: This method
 	 * should be sensitive to the current traversal context
 	 * 
 	 * @return {@link Graph} containing this {@link GraphElement}
 	 */
 	public Graph getGraph();
 
 	/**
 	 * Returns the local partial graph this element belongs to either directly
 	 * or as a member of a subordinate graph TODO: Needs to be implemented
 	 * 
 	 * @return
 	 */
 	public Graph getLocalGraph();
 
 	/**
 	 * Returns the graph directly containing this graph elements, e.g. the
 	 * subordinate or partial one
 	 * 
 	 * @return
 	 */
 	public Graph getContainingGraph();
 
 	/**
 	 * Returns the {@link GraphElement} in which this {@link GraphElement} is
 	 * nested. If such a {@link GraphElement} does not exist, <code>null</code>
 	 * is returned.
 	 * 
 	 * @return {@link GraphElement}
 	 */
 	public GraphElement<?, ?, ?> getSigma();
 
 	/**
 	 * Returns a {@link Graph} which contains all elements which are nested in
 	 * this {@link GraphElement}.
 	 * 
 	 * @return {@link Graph}
 	 */
 	public Graph getSubordinateGraph();
 
 	/**
 	 * Returns the kappa value of this {@link GraphElement}.
 	 * 
 	 * @see {@link GraphElementImpl#kappa}
 	 * @return int
 	 */
 	public int getKappa();
 
 	/**
 	 * Checks if this {@link GraphElement} is visible at a kappa value of
 	 * <code>kappa</code>.
 	 * 
 	 * @param kappa
 	 *            int
 	 * @return boolean <code>true</code> if this GraphElement is visible
 	 */
 	public boolean isVisible(int kappa);
 
 	/**
 	 * Checks if this {@link GraphElement} contains <code>element</code>.
 	 * 
 	 * @param element
 	 *            {@link GraphElement}
 	 * @return boolean <code>true</code> if <code>element</code> is contained in
 	 *         this {@link GraphElement}
 	 */
 	public boolean containsElement(GraphElement<?, ?, ?> element);
 
 	public long getIncidenceListVersion();
 
	public boolean isIncidenceListModified(long incidenceListVersion);

 	/**
 	 * Returns the first {@link Incidence} of this {@link GraphElement}. If such
 	 * an {@link Incidence} does not exist, <code>null</code> is returned.
 	 * 
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence();
 
 	/**
 	 * Returns the first {@link Incidence} of this {@link GraphElement} with
 	 * direction <code>direction</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param direction
 	 *            {@link Direction} of connected {@link Incidence}s.
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Direction direction);
 
 	/**
 	 * Get the first {@link Incidence} which has one of the aggregation
 	 * semantics given by <code>incidentTypes</code> at either this
 	 * {@link Incidence} (<code>thisIncidence == true</code>) or that
 	 * {@link Incidence} (<code>thisIncidence == false</code>). If there are no
 	 * <code>incidentTypes</code> given, it simply returns the first
 	 * {@link Incidence}. If such an {@link Incidence} does not exist,
 	 * <code>null</code> is returned.<br/>
 	 * <br/>
 	 * For example, this returns the first {@link Incidence} to a parent
 	 * {@link GraphElement} in the containment hierarchy.
 	 * 
 	 * <pre>
 	 * ge.getFirstIncidence(true, IncidenceType.AGGREGATION, IncidenceType.COMPOSITION)
 	 * </pre>
 	 * 
 	 * And this returns the first {@link Incidence} to a child
 	 * {@link GraphElement} in the containment hierarchy.
 	 * 
 	 * <pre>
 	 * v.getFirstIncidence(false, IncidenceType.AGGREGATION, IncidenceType.COMPOSITION)
 	 * </pre>
 	 * 
 	 * @param thisIncidence
 	 *            boolean if true, <code>incidentTypes</code> has to match the
 	 *            {@link Incidence} at this {@link GraphElement}, else it has to
 	 *            match the opposite {@link Incidence}
 	 * @param incidentTypes
 	 *            {@link IncidenceType}...
 	 * 
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(boolean thisIncidence,
 			IncidenceType... incidentTypes);
 
 	/**
 	 * Returns the first incidence in lambda-seq where the corresponding
 	 * {@link IncidenceClass} is of class <code>anIncidenceClass</code>. If such
 	 * an {@link Incidence} does not exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(IncidenceClass anIncidenceClass);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If such an
 	 * {@link Incidence} does not exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link Class} the {@link Incidence} class to search for
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Class<T> anIncidenceClass);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code> and the
 	 * direction is <code>direction</code>. If such an {@link Incidence} does
 	 * not exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(IncidenceClass anIncidenceClass,
 			Direction direction);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code> and the
 	 * direction is <code>direction</code>. If such an {@link Incidence} does
 	 * not exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link Class} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Class<T> anIncidenceClass,
 			Direction direction);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(IncidenceClass anIncidenceClass,
 			boolean noSubclasses);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link Class} to search for
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Class<T> anIncidenceClass,
 			boolean noSubclasses);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. The {@link Incidence} must have the
 	 * direction <code>direction</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(IncidenceClass anIncidenceClass,
 			Direction direction, boolean noSubclasses);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. The {@link Incidence} must have the
 	 * direction <code>direction</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link Class} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Class<T> anIncidenceClass,
 			Direction direction, boolean noSubclasses);
 
 	/**
 	 * Returns the first {@link Incidence} of this {@link GraphElement}. If such
 	 * an {@link Incidence} does not exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Graph traversalContext);
 
 	/**
 	 * Returns the first {@link Incidence} of this {@link GraphElement} with
 	 * direction <code>direction</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param direction
 	 *            {@link Direction} of connected {@link Incidence}s.
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Graph traversalContext,
 			Direction direction);
 
 	/**
 	 * Get the first {@link Incidence} which has one of the aggregation
 	 * semantics given by <code>incidentTypes</code> at either this
 	 * {@link Incidence} (<code>thisIncidence == true</code>) or that
 	 * {@link Incidence} (<code>thisIncidence == false</code>). If there are no
 	 * <code>incidentTypes</code> given, it simply returns the first
 	 * {@link Incidence}. If such an {@link Incidence} does not exist,
 	 * <code>null</code> is returned.<br/>
 	 * <br/>
 	 * For example, this returns the first {@link Incidence} to a parent
 	 * {@link GraphElement} in the containment hierarchy.
 	 * 
 	 * <pre>
 	 * ge.getFirstIncidence(true, IncidenceType.AGGREGATION, IncidenceType.COMPOSITION)
 	 * </pre>
 	 * 
 	 * And this returns the first {@link Incidence} to a child
 	 * {@link GraphElement} in the containment hierarchy.
 	 * 
 	 * <pre>
 	 * v.getFirstIncidence(false, IncidenceType.AGGREGATION, IncidenceType.COMPOSITION)
 	 * </pre>
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param thisIncidence
 	 *            boolean if true, <code>incidentTypes</code> has to match the
 	 *            {@link Incidence} at this {@link GraphElement}, else it has to
 	 *            match the opposite {@link Incidence}
 	 * @param incidentTypes
 	 *            {@link IncidenceType}...
 	 * 
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Graph traversalContext,
 			boolean thisIncidence, IncidenceType... incidentTypes);
 
 	/**
 	 * Returns the first incidence in lambda-seq where the corresponding
 	 * {@link IncidenceClass} is of class <code>anIncidenceClass</code>. If such
 	 * an {@link Incidence} does not exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Graph traversalContext,
 			IncidenceClass anIncidenceClass);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If such an
 	 * {@link Incidence} does not exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link Class} the {@link Incidence} class to search for
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Graph traversalContext,
 			Class<T> anIncidenceClass);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code> and the
 	 * direction is <code>direction</code>. If such an {@link Incidence} does
 	 * not exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Graph traversalContext,
 			IncidenceClass anIncidenceClass, Direction direction);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code> and the
 	 * direction is <code>direction</code>. If such an {@link Incidence} does
 	 * not exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link Class} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Graph traversalContext,
 			Class<T> anIncidenceClass, Direction direction);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Graph traversalContext,
 			IncidenceClass anIncidenceClass, boolean noSubclasses);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link Class} to search for
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Graph traversalContext,
 			Class<T> anIncidenceClass, boolean noSubclasses);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. The {@link Incidence} must have the
 	 * direction <code>direction</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public Incidence getFirstIncidence(Graph traversalContext,
 			IncidenceClass anIncidenceClass, Direction direction,
 			boolean noSubclasses);
 
 	/**
 	 * Returns the first {@link Incidence} in lambda-seq where the corresponding
 	 * {@link Incidence} is of class <code>anIncidenceClass</code>. If
 	 * <code>noSubclasses</code> is set to <code>true</code> the
 	 * {@link Incidence} must not be an instance of a subclass of
 	 * <code>anIncidenceClass</code>. The {@link Incidence} must have the
 	 * direction <code>direction</code>. If such an {@link Incidence} does not
 	 * exist, <code>null</code> is returned.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @param anIncidenceClass
 	 *            {@link Class} to search for
 	 * @param direction
 	 *            {@link Direction} of the {@link Incidence}s.
 	 * @param noSubclasses
 	 *            boolean
 	 * @return {@link Incidence}
 	 */
 	public <T extends Incidence> T getFirstIncidence(Graph traversalContext,
 			Class<T> anIncidenceClass, Direction direction, boolean noSubclasses);
 
 	/**
 	 * Returns the last {@link Incidence} of this {@link GraphElement}.
 	 * 
 	 * @return {@link Incidence}
 	 */
 	public Incidence getLastIncidence();
 
 	/**
 	 * Returns the last {@link Incidence} of this {@link GraphElement}.
 	 * 
 	 * @param traversalContext
 	 *            {@Link Graph}
 	 * @return {@link Incidence}
 	 */
 	public Incidence getLastIncidence(Graph traversalContext);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement}.
 	 * 
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences();
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which have the direction specified by
 	 * <code>direction</code>.
 	 * 
 	 * @param direction
 	 *            {@link Direction} specifies the direction of the requested
 	 *            {@link Incidence}s.
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences(Direction direction);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link Class}
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Class<T> anIncidenceClass);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass}
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences(IncidenceClass anIncidenceClass);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which have the direction specified by
 	 * <code>direction</code> and is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction} specifies the direction of the requested
 	 *            {@link Incidence}s.
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Class<T> anIncidenceClass, Direction direction);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which have the direction specified by
 	 * <code>direction</code> and is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param anIncidenceClass
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction} specifies the direction of the requested
 	 *            {@link Incidence}s.
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences(IncidenceClass anIncidenceClass,
 			Direction direction);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement}.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences(Graph traversalContext);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which have the direction specified by
 	 * <code>direction</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param direction
 	 *            {@link Direction} specifies the direction of the requested
 	 *            {@link Incidence}s.
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences(Graph traversalContext,
 			Direction direction);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param anIncidenceClass
 	 *            {@link Class}
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Graph traversalContext, Class<T> anIncidenceClass);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param anIncidenceClass
 	 *            {@link IncidenceClass}
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences(Graph traversalContext,
 			IncidenceClass anIncidenceClass);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which have the direction specified by
 	 * <code>direction</code> and is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param anIncidenceClass
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction} specifies the direction of the requested
 	 *            {@link Incidence}s.
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public <T extends Incidence> Iterable<T> getIncidences(
 			Graph traversalContext, Class<T> anIncidenceClass,
 			Direction direction);
 
 	/**
 	 * Returns an {@link Iterable} over all {@link Incidence}s at this
 	 * {@link GraphElement} which have the direction specified by
 	 * <code>direction</code> and is an instance of
 	 * <code>anIncidenceClass</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param anIncidenceClass
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction} specifies the direction of the requested
 	 *            {@link Incidence}s.
 	 * @return {@link Iterable}&lt;{@link Incidence}&gt;
 	 */
 	public Iterable<Incidence> getIncidences(Graph traversalContext,
 			IncidenceClass anIncidenceClass, Direction direction);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} .
 	 * 
 	 * @return int
 	 */
 	public int getDegree();
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code>.
 	 * 
 	 * @param direction
 	 *            {@link Direction}
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         connected to this GraphElement
 	 */
 	public int getDegree(Direction direction);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(IncidenceClass ic);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link Class}
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(Class<? extends Incidence> ic);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(IncidenceClass ic, boolean noSubClasses);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link Class}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(Class<? extends Incidence> ic, boolean noSubClasses);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @param direction
 	 *            {@link Direction}
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(IncidenceClass ic, Direction direction);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction}
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(Class<? extends Incidence> ic, Direction direction);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @param direction
 	 *            {@link Direction}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(IncidenceClass ic, Direction direction,
 			boolean noSubClasses);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param ic
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(Class<? extends Incidence> ic, Direction direction,
 			boolean noSubClasses);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} .
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @return int
 	 */
 	public int getDegree(Graph traversalContext);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param direction
 	 *            {@link Direction}
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         connected to this GraphElement
 	 */
 	public int getDegree(Graph traversalContext, Direction direction);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, IncidenceClass ic);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link Class}
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, Class<? extends Incidence> ic);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, IncidenceClass ic,
 			boolean noSubClasses);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link Class}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s which are an instance of
 	 *         <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, Class<? extends Incidence> ic,
 			boolean noSubClasses);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @param direction
 	 *            {@link Direction}
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, IncidenceClass ic,
 			Direction direction);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction}
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, Class<? extends Incidence> ic,
 			Direction direction);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @param direction
 	 *            {@link Direction}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, IncidenceClass ic,
 			Direction direction, boolean noSubClasses);
 
 	/**
 	 * Returns the number of connected {@link Incidence}s to this
 	 * {@link GraphElement} which have the {@link Direction} specified by
 	 * <code>direction</code> and are a subclass of <code>ic</code>.
 	 * 
 	 * @param traversalContext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link Class}
 	 * @param direction
 	 *            {@link Direction}
 	 * @param noSubClasses
 	 *            boolean if set to <code>true</code>, subclasses of
 	 *            <code>ic</code> are not counted
 	 * @return int number of {@link Incidence}s of desired direction which are
 	 *         an instance of <code>ic</code>
 	 */
 	public int getDegree(Graph traversalContext, Class<? extends Incidence> ic,
 			Direction direction, boolean noSubClasses);
 
 	/**
 	 * Returns <code>true</code> if this {@link GraphElement} is before
 	 * <code>g</code> in the sequence of incident {@link GraphElement}s.
 	 * 
 	 * @param g
 	 * @return boolean
 	 */
 	public boolean isBefore(OwnType g);
 
 	/**
 	 * TODO This method does not check, if this {@link GraphElement} is moved
 	 * into a subordinate graph.<br>
 	 * Puts this {@link GraphElement} immediately before <code>g</code> in the
 	 * sequence of incident {@link GraphElement}s.
 	 * 
 	 * @param g
 	 */
 	public void putBefore(OwnType g);
 
 	/**
 	 * Returns <code>true</code> if this {@link GraphElement} is after
 	 * <code>g</code> in the sequence of incident {@link GraphElement}s.
 	 * 
 	 * @param g
 	 * @return boolean
 	 */
 	public boolean isAfter(OwnType g);
 
 	/**
 	 * TODO This method does not check, if this {@link GraphElement} is moved
 	 * into a subordinate graph.<br>
 	 * Puts this {@link GraphElement} immediately after <code>g</code> in the
 	 * sequence of incident {@link GraphElement}s.
 	 * 
 	 * @param g
 	 */
 	public void putAfter(OwnType g);
 
 	/**
 	 * Returns a {@link List} of <code>OwnType</code>s, which are adjacent to
 	 * this {@link GraphElement}. The connected <code>DualType</code> object is
 	 * connected to this {@link GraphElement} via an {@link IncidenceClass} of
 	 * name <code>role</code>.
 	 * 
 	 * @see GraphElement
 	 * @param role
 	 *            {@link String}
 	 * @return {@link List} of <code>OwnType</code> objects
 	 */
 	public List<? extends OwnType> getAdjacences(String role);
 
 	/**
 	 * Returns a {@link List} of <code>OwnType</code>s, which are adjacent to
 	 * this {@link GraphElement}. The connected <code>DualType</code> object is
 	 * connected to this {@link GraphElement} via an {@link Incidence} of type
 	 * <code>ic</code>.
 	 * 
 	 * @see GraphElement
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @return {@link List} of <code>OwnType</code> objects
 	 */
 	public List<? extends OwnType> getAdjacences(IncidenceClass ic);
 
 	/**
 	 * Returns a {@link List} of <code>OwnType</code>s, which are adjacent to
 	 * this {@link GraphElement}. The connected <code>DualType</code> object is
 	 * connected to this {@link GraphElement} via an {@link IncidenceClass} of
 	 * name <code>role</code>.
 	 * 
 	 * @see GraphElement
 	 * @param traversalConext
 	 *            {@link Graph}
 	 * @param role
 	 *            {@link String}
 	 * @return {@link List} of <code>OwnType</code> objects
 	 */
 	public List<? extends OwnType> getAdjacences(Graph traversalConext,
 			String role);
 
 	/**
 	 * Returns a {@link List} of <code>OwnType</code>s, which are adjacent to
 	 * this {@link GraphElement}. The connected <code>DualType</code> object is
 	 * connected to this {@link GraphElement} via an {@link Incidence} of type
 	 * <code>ic</code>.
 	 * 
 	 * @see GraphElement
 	 * @param traversalConext
 	 *            {@link Graph}
 	 * @param ic
 	 *            {@link IncidenceClass}
 	 * @return {@link List} of <code>OwnType</code> objects
 	 */
 	public List<? extends OwnType> getAdjacences(Graph traversalConext,
 			IncidenceClass ic);
 
 	/**
 	 * Creates and returns a new instance of <code>DualType</code> connected to
 	 * <code>this</code> and <code>other</code> via instances of
 	 * {@link IncidenceClass}es which correspond to <code>incidentRole</code>
 	 * and <code>adjacentRole</code>.
 	 * 
 	 * @see #addAdjacence(IncidenceClass, IncidenceClass, Object)
 	 * @see #getIncidenceClassForRolename(String)
 	 * @param incidentRole
 	 *            {@link String}
 	 * @param adjacentRole
 	 *            {@link String}
 	 * @param other
 	 *            <code>OwnType</code>
 	 * @return <code>DualType</code>
 	 * @throws RemoteException
 	 */
 	public DualType addAdjacence(String incidentRole, String adjacentRole,
 			OwnType other);
 
 	/**
 	 * Creates and returns a new <code>DualType</code> instance, which is
 	 * connected to <code>this</code> via an instance of <code>incidentIc</code>
 	 * and to <code>other</code> via an instance of <code>adjacentIc</code>.
 	 * 
 	 * @param incidentIc
 	 *            {@link IncidenceClass}
 	 * @param adjacentIc
 	 *            {@link IncidenceClass}
 	 * @param other
 	 *            <code>OwnType</code>
 	 * @return <code>DualType</code>
 	 */
 	public DualType addAdjacence(IncidenceClass incidentIc,
 			IncidenceClass adjacentIc, OwnType other);
 
 	/**
 	 * Manipulation operations are not sensitive for a subgraph but affect the
 	 * complete graph the above method should add the newly created element to
 	 * the subgraph both connected elements belong to
 	 */
 	// public DualType addAdjacence(Graph traversalContext, String incidentRole,
 	// String adjacentRole, OwnType other);
 
 	/**
 	 * Manipulation operations are not sensitive for a subgraph but affect the
 	 * complete graph the above method should add the newly created element to
 	 * the subgraph both connected elements belong to
 	 */
 	// public DualType addAdjacence(Graph traversalContext,
 	// IncidenceClass incidentIc, IncidenceClass adjacentIc, OwnType other);
 
 	/**
 	 * TODO
 	 * 
 	 * @param role
 	 * @return
 	 */
 	public List<OwnType> removeAdjacences(String role);
 
 	/**
 	 * TODO
 	 * 
 	 * @param ic
 	 * @return
 	 */
 	public List<OwnType> removeAdjacences(IncidenceClass ic);
 
 	/**
 	 * Manipulation operations are not sensitive for a subgraph but affect the
 	 * complete graph
 	 */
 	// public List<OwnType> removeAdjacences(Graph traversalContext, String
 	// role);
 
 	/**
 	 * Manipulation operations are not sensitive for a subgraph but affect the
 	 * complete graph
 	 */
 	// public List<OwnType> removeAdjacences(Graph traversalContext,
 	// IncidenceClass ic);
 
 	/**
 	 * TODO
 	 * 
 	 * @param role
 	 * @param other
 	 */
 	public void removeAdjacence(String role, OwnType other);
 
 	/**
 	 * TODO
 	 * 
 	 * @param ic
 	 * @param other
 	 */
 	public void removeAdjacence(IncidenceClass ic, OwnType other);
 
 	/**
 	 * Manipulation operations are not sensitive for a subgraph but affect the
 	 * complete graph
 	 */
 	// public void removeAdjacence(Graph traversalContext, String role, OwnType
 	// other);
 
 	/**
 	 * Manipulation operations are not sensitive for a subgraph but affect the
 	 * complete graph
 	 * 
 	 * @param traversalContext
 	 * @param ic
 	 * @param other
 	 */
 	// public void removeAdjacence(Graph traversalContext, IncidenceClass ic,
 	// OwnType other);
 
 	/**
 	 * Returns the {@link IncidenceClass} corresponding to <code>rolename</code>
 	 * .
 	 * 
 	 * @param rolename
 	 *            {@link String}
 	 * @return {@link IncidenceClass}
 	 */
 	public IncidenceClass getIncidenceClassForRolename(String rolename);
 
 	/**
 	 * Removes this {@link GraphElement} from the corresponding sequence and
 	 * erases its attributes.
 	 */
 	public void delete();
 
 	/**
 	 * Tests if the <code>DualType</code> <code>graphElement</code> may start at
 	 * this GraphElement
 	 * 
 	 * @see GraphElement
 	 * @param graphElement
 	 *            <code>DualType</code>
 	 * @return <code>true</code> iff <code>graphElement</code> may start at this
 	 *         {@link GraphElement}
 	 */
 	// public boolean isValidAlpha(DualType graphElement);
 
 	/**
 	 * Tests if the <code>DualType</code> <code>graphElement</code> may end at
 	 * this GraphElement
 	 * 
 	 * @see GraphElement
 	 * @param graphElement
 	 *            <code>DualType</code>
 	 * @return <code>true</code> iff <code>graphElement</code> may end at this
 	 *         {@link GraphElement}
 	 */
 	// public boolean isValidOmega(DualType graphElement);
 
 	/**
 	 * Connects this {@link GraphElement} with <code>elemToConnect</code> via an
 	 * {@link Incidence} which corresponds to <code>rolename</code>.
 	 * 
 	 * @see GraphElement
 	 * @param rolename
 	 *            {@link String} the rolename of the {@link IncidenceClass}
 	 *            which should be used for the connection
 	 * @param elemToConnect
 	 *            <code>DualType</code> to which this GraphElement should be
 	 *            connected
 	 * @return
 	 * @throws RemoteException
 	 */
 	public Incidence connect(String rolename, DualType elemToConnect);
 
 	/**
 	 * Connects this {@link GraphElement} with <code>elemToConnect</code> via an
 	 * {@link Incidence} of type <code>incidenceClass</code>.
 	 * 
 	 * @see GraphElement
 	 * @param incidenceClass
 	 *            {@link IncidenceClass} which should be used for the connection
 	 * @param elemToConnect
 	 *            <code>DualType</code> to which this GraphElement should be
 	 *            connected
 	 */
 	public Incidence connect(IncidenceClass incidenceClass,
 			DualType elemToConnect);
 
 	/**
 	 * Connects this {@link GraphElement} with <code>elemToConnect</code> via an
 	 * {@link Incidence} of type <code>incidenceClass</code>.
 	 * 
 	 * @see GraphElement
 	 * @param incidenceClass
 	 *            {@link Class} which should be used for the connection
 	 * @param elemToConnect
 	 *            <code>DualType</code> to which this GraphElement should be
 	 *            connected
 	 */
 	public <T extends Incidence> T connect(Class<T> incidenceClass,
 			DualType elemToConnect);
 
 	/**
 	 * Adds <code>appendix</code> to the subordinate graph of this
 	 * {@link GraphElement}.
 	 * 
 	 * @param appendix
 	 *            {@link Vertex}
 	 */
 	public void addSubordinateElement(Vertex appendix);
 
 	/**
 	 * Adds <code>appendix</code> to the subordinate graph of this
 	 * {@link GraphElement}.
 	 * 
 	 * @param appendix
 	 *            {@link Edge}
 	 */
 	public void addSubordinateElement(Edge appendix);
 
 	void setSigma(GraphElement<?, ?, ?> elem);
 
 	void setKappa(int kappa);
 
 }
