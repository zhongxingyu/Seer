 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Jorn Bettin
  * ***** END LICENSE BLOCK ***** */
 
 package org.s23m.cell.core;
 
 import static org.s23m.cell.S23MKernel.coreGraphs;
 import static org.s23m.cell.S23MKernel.coreSets;
 import static org.s23m.cell.core.F_Instantiation.identityFactory;
 
 import java.util.List;
 import java.util.ListIterator;
 
 import org.s23m.cell.Identity;
 import org.s23m.cell.Set;
 import org.s23m.cell.api.Query;
 import org.s23m.cell.api.VisitorFunction;
 import org.s23m.cell.api.models.S23MSemanticDomains;
 import org.s23m.cell.api.models.SemanticDomain;
 import org.s23m.cell.api.models2.Visualization;
 
 public class Graph extends OrderedPair implements Set {
 
 	/* Reify the S23M Graph concept */
 	protected static final Graph graph = new Graph();
 	protected static final OrderedSet inMemorySets = new OrderedSet();
 	protected static final OrderedSet inMemorySemanticIdentities = new OrderedSet();
 	protected static final OrderedSet changedSets = new OrderedSet();
 
 	private boolean orderedSetsInitialized;
 	private boolean executableFunctionsInitialized;
 
 	private OrderedSet containedEdges;
 	private OrderedSet allContainedArrows;
 	private OrderedSet containedSuperSetReferences;
 	private OrderedSet containedVertices;
 	private OrderedSet containedVisibilities;
 	private OrderedSet allContainedInstances;
 
 	private OrderedSet variables;
 	private OrderedSet values;
 	private OrderedSet commands;
 	private OrderedSet queries;
 	private OrderedSet executableCommands;
 	private OrderedSet executableQueries;
 
 	/* modification state */
 	private boolean containsDecommissionedSets;
 	private boolean containsNewSets;
 	private boolean hasDecommissionedPayload;
 	private boolean hasNewPayload;
 	private boolean isDecommissioned;
 	private boolean isNewInstance;
 	private boolean hasNewName;
 	private boolean hasNewPluralName;
 
 	protected Graph(final Identity semanticIdentity, final Set categoryOfElement) {
 		super(semanticIdentity, categoryOfElement);
 	}

 	protected Graph(final Identity semanticIdentity) {
 		super(semanticIdentity);
 	}

 	private Graph() {
 		super(identityFactory.graph());
 		this.addToValues(coreSets.orderedPair);
 		this.addToVariables(coreSets.maxCardinality);
 		this.addToVariables(coreSets.isAbstract);
 		this.addToValues(coreSets.isAbstract_TRUE);
 		this.addProperClassQueries();
 		this.addProperClassCommands();
 		this.containsDecommissionedSets = false;
 		this.containsNewSets = false;
 		this.isDecommissioned = false;
 		if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 			this.isNewInstance = true;
 		} else {
 			this.isNewInstance = false;
 		}
 		this.hasNewName = false;
 		this.hasNewPluralName =false;
 		this.hasNewPayload = false;
 		this.hasDecommissionedPayload = false;
 	}

 	public static void addSetToInMemorySets(final Set e) {
 		if (!F_SemanticStateOfInMemoryModel.isDebugModeOn()) {
 			Graph.inMemorySets.add(e);
 			if (F_SemanticStateOfInMemoryModel.semanticDomainIsInitialized()) {
 				if (SemanticDomain.semanticdomain.isEqualTo(e.container().category())) {
 					Graph.inMemorySemanticIdentities.add(e);
 				}
 			}
 		} else {
 			if (Graph.inMemorySets.containsRepresentation(e)) {
 				F_Instantiation.raiseError(identityFactory.semanticErr_ASetWithThisIdentityAndRepresentationIsAlreadyLoaded(), coreSets.semanticErr);
 			}
 			else {
 				Graph.inMemorySets.add(e);
 				if (F_SemanticStateOfInMemoryModel.semanticDomainIsInitialized()) {
 					if (SemanticDomain.semanticdomain.isEqualTo(e.container().category())) {
 						Graph.inMemorySemanticIdentities.add(e);
 					}
 				}
 			}
 		}
 
 	}

 	public static void addSetToChangedSets(final Set e) {
 		Graph.changedSets.add(e);
 	}

 	@Override
 	public String toString() {
 		return this.localVisualRecognitionText();
 	}
 
 	@Override
 	public Graph category() {
 		if (F_SemanticStateOfInMemoryModel.cellKernelSemanticDomainIsInitialized()) {
 			/*
 			 * orderedPairCategory() duplicates category() but is essential to break recursive loop in Graph
 			 * This is the only class/method in S23M where orderedPairCategory() must be used
 			 * All other code must only use category()
 			 */
 			if (this.orderedPairCategory().isEqualTo(coreSets.semanticErr)) {return ((Graph) S23MSemanticDomains.semanticErr);}
 			if (this.orderedPairCategory().isEqualTo(coreSets.kernelDefect)) {return ((Graph) S23MSemanticDomains.kernelDefect);}
 		}
 		return ((Graph) category);
 	}
 
 	@Override
 	public Set variables() {
 		this.ensureInitializedOrderedSets();
 		return variables;
 	}

 	@Override
 	public Set addToVariables(final Set set) {
 		this.ensureInitializedOrderedSets();
 		if (this.identity().isPartOfKernel()) {
 			if (!this.variables.containsSemanticMatch(set)) {
 				this.variables.add(set);
 				if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 					this.containsNewSets = true;
 					Graph.addSetToChangedSets(this);
 				}
 			}
 			return coreSets.successful;
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set values() {
 		this.ensureInitializedOrderedSets();
 		return values;
 	}
 
 	private void setValue(final Set value) {
 		if (F_SemanticStateOfInMemoryModel.cellKernelSemanticDomainIsInitialized()) {
 			if (!this.values.containsSemanticMatch(value)) {
 				this.values.add(value);
 				if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 					this.containsNewSets = true;
 					Graph.addSetToChangedSets(this);
 				}
 			}
 		} else {
 			this.values.add(value);
 			if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 				this.containsNewSets = true;
 				Graph.addSetToChangedSets(this);
 			}
 		}
 	}
 
 	@Override
 	public Set addToValues(final Set anElement) {
 		// a value can only be set once, and can never be modified!
 		this.ensureInitializedOrderedSets();
 		if ((this.identity().isPartOfKernel() && !this.values.containsSemanticMatch(anElement))
 				|| (
 						(this.category().properClass().variables().containsSemanticMatch(((OrderedPair)anElement).category())
 								|| this.category().variables().containsSemanticMatch(((OrderedPair)anElement).category())
 								|| anElement.category().isEqualTo(coreSets.isAbstract)
 								|| anElement.category().isEqualTo(coreSets.maxCardinality)
 								)
 								&& (!this.values.containsSemanticMatch(anElement))
 						)
 				) {
 			this.setValue(anElement);
 			return coreSets.successful;
 		} else {
 			if (F_SemanticStateOfInMemoryModel.cellKernelSemanticDomainIsInitialized()) {
 				final Set theSemanticIdentity = anElement.semanticIdentity();
 				for (final Set variable : this.category().properClass().variables()) {
 					if (theSemanticIdentity.isElementOf(variable.semanticIdentity()).isEqualTo(coreSets.is_TRUE)) {
 						this.setValue(theSemanticIdentity);
 						return S23MSemanticDomains.successful;
 					}
 				}
 				for (final Set variable : this.category().variables()) {
 					if (theSemanticIdentity.isElementOf(S23MSemanticDomains.isAbstract).isEqualTo(coreSets.is_TRUE)
 							|| theSemanticIdentity.isElementOf(S23MSemanticDomains.maxCardinality).isEqualTo(coreSets.is_TRUE)
 							|| theSemanticIdentity.isElementOf(variable.semanticIdentity()).isEqualTo(coreSets.is_TRUE)) {
 						this.setValue(theSemanticIdentity);
 						return S23MSemanticDomains.successful;
 					}
 				}
 				return F_InstantiationImpl.raiseError(coreSets.semanticErr_ValueIsNotAnInstanceOfVariableOfCategoryOfInstance.identity(), coreSets.semanticErr);
 			} else {
 				return F_InstantiationImpl.raiseError(coreSets.semanticErr_ValueIsNotAnInstanceOfVariableOfCategoryOfInstance.identity(), coreSets.semanticErr);
 			}
 		}
 	}
 
 	@Override
 	public Set value(final Set variable) {
 		this.ensureInitializedOrderedSets();
 		for (final Set value : values) {
 			if (F_SemanticStateOfInMemoryModel.cellKernelSemanticDomainIsInitialized()) {
 				if (value.semanticIdentity().isElementOf(variable.semanticIdentity()).isEqualTo(coreSets.is_TRUE)) {
 					return value;
 				}
 			} else {
 				if (value.category().isEqualTo(variable)) {
 					return value;
 				}
 			}
 		}
 		// logic for max cardinality constraint for physical containment of graphs
 		if (variable.isEqualTo(coreSets.maxCardinality)) {
 			return coreSets.maxCardinality_n;
 		} else {
 			return coreSets.semanticErr_ValueIsNotAssigned;
 		}
 	}
 
 	@Override
 	public Set commands() {
 		this.ensureInitializedOrderedSets();
 		return commands;
 	}
 	@Override
 	public Set executableCommands() {
 		this.ensureInitializedOrderedSets();
 		this.ensureInitializedExecutableFunctions();
 		return executableCommands;
 	}
 
 
 	@Override
 	public Set addToCommands(final Set anElement) {
 		this.ensureInitializedOrderedSets();
 		if (!this.commands.containsSemanticMatch(anElement)) {
 			this.commands.add(anElement);
 			if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 				this.containsNewSets = true;
 				Graph.addSetToChangedSets(this);
 			}
 		}
 		return coreSets.successful;
 	}
 
 
 	@Override
 	public Set removeFromCommands(final Set anElement) {
 		this.ensureInitializedOrderedSets();
 		if (!(this.container().isEqualTo(Graph.graph))) {
 			this.commands.remove(anElement);
 			if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 				this.containsNewSets = true;
 				Graph.addSetToChangedSets(this);
 			}
 			return coreSets.successful;
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_GraphGraphCantBeModified.identity(), coreSets.semanticErr);
 		}
 	}
 	@Override
 	public Set queries() {
 		this.ensureInitializedOrderedSets();
 		return queries;
 	}
 
 	private void ensureInitializedExecutableFunctions() {
 		if (!executableFunctionsInitialized) {
 			if (!isEqualTo(Graph.graph)) {
 				// ensure that all properClass-level queries are included in the result
 				addProperClassQueries();
 				addProperClassCommands();
 			}
 			final Set superClasses = this.category().container().filterPolymorphic(this.category().container().localRootSuperSetOf(this.category()));
 			for (final Set superClass : superClasses) {
 				executableQueries = ((OrderedSet) executableQueries.union(superClass.queries()));
 				executableCommands = ((OrderedSet) executableCommands.union(superClass.commands()));
 			}
 			executableFunctionsInitialized = true;
 		}
 	}
 	@Override
 	public Set executableQueries() {
 		this.ensureInitializedOrderedSets();
 		this.ensureInitializedExecutableFunctions();
 		return executableQueries;
 	}
 
 	@Override
 	public Set addToQueries(final Set anElement) {
 		this.ensureInitializedOrderedSets();
 		if (!this.queries.containsSemanticMatch(anElement)) {
 			this.queries.add(anElement);
 			if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 				this.containsNewSets = true;
 				Graph.addSetToChangedSets(this);
 			}
 		}
 		return coreSets.successful;
 	}
 
 	protected Set addToExecutableQueries(final Set anElement) {
 		this.ensureInitializedOrderedSets();
 		if (!this.executableQueries.containsSemanticMatch(anElement)) {
 			this.executableQueries.add(anElement);
 		}
 		return coreSets.successful;
 	}
 	protected Set addToExecutableCommands(final Set anElement) {
 		this.ensureInitializedOrderedSets();
 		if (!this.executableCommands.containsSemanticMatch(anElement)) {
 			this.executableCommands.add(anElement);
 		}
 		return coreSets.successful;
 	}
 
 	@Override
 	public Set removeFromQueries(final Set anElement) {
 		this.ensureInitializedOrderedSets();
 		if (!(this.container().isEqualTo(Graph.graph))) {
 			this.queries.remove(anElement);
 			if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 				this.containsNewSets = true;
 				Graph.addSetToChangedSets(this);
 			}
 			return coreSets.successful;
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_GraphGraphCantBeModified.identity(), coreSets.semanticErr);
 		}
 	}
 
 	private void ensureInitializedOrderedSets() {
 		if (!orderedSetsInitialized) {
 			values = new OrderedSet(identityFactory.createAnonymousIdentity());
 			variables = new OrderedSet(identityFactory.createAnonymousIdentity());
 			allContainedInstances = new OrderedSet(identityFactory.createAnonymousIdentity());
 			containedVertices = new OrderedSet(identityFactory.createAnonymousIdentity());
 			containedEdges = new OrderedSet(identityFactory.createAnonymousIdentity());
 			allContainedArrows = new OrderedSet(identityFactory.createAnonymousIdentity());
 			containedSuperSetReferences = new OrderedSet(identityFactory.createAnonymousIdentity());
 			containedVisibilities = new OrderedSet(identityFactory.createAnonymousIdentity());
 			commands = new OrderedSet(identityFactory.createAnonymousIdentity());
 			queries = new OrderedSet(identityFactory.createAnonymousIdentity());
 			executableCommands = new OrderedSet(identityFactory.createAnonymousIdentity());
 			executableQueries = new OrderedSet(identityFactory.createAnonymousIdentity());
 
 			orderedSetsInitialized = true;
 		}
 	}
 
 	protected Set getVertices() {
 		this.ensureInitializedOrderedSets();
 		return containedVertices;
 	}
 
 	protected void addToVertices(final Vertex anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedVertices.add(anElement);
 		this.allContainedInstances.add(anElement);
 	}
 
 	protected void removeFromVertices(final Vertex anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedVertices.remove(anElement);
 		this.allContainedInstances.remove(anElement);
 	}
 
 	protected Set getEdges() {
 		this.ensureInitializedOrderedSets();
 		return containedEdges;
 	}
 
 	protected void addToEdges(final Edge anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedEdges.add(anElement);
 		this.allContainedArrows.add(anElement);
 		this.allContainedInstances.add(anElement);
 	}
 
 	protected void removeFromEdges(final Edge anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedEdges.remove(anElement);
 		this.allContainedArrows.remove(anElement);
 		this.allContainedInstances.remove(anElement);
 	}
 
 	protected Set getSuperSetReferences() {
 		this.ensureInitializedOrderedSets();
 		return containedSuperSetReferences;
 	}
 
 	protected void addToSuperSetReferences(final SuperSetReference anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedSuperSetReferences.add(anElement);
 		this.allContainedArrows.add(anElement);
 		this.allContainedInstances.add(anElement);
 	}
 
 	protected void removeFromGeneralizationReferences(final SuperSetReference anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedSuperSetReferences.remove(anElement);
 		this.allContainedArrows.remove(anElement);
 		this.allContainedInstances.remove(anElement);
 	}
 
 	protected Set getVisibilities() {
 		this.ensureInitializedOrderedSets();
 		return containedVisibilities;
 	}
 
 	protected void addToVisibilities(final Visibility anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedVisibilities.add(anElement);
 		this.allContainedArrows.add(anElement);
 		this.allContainedInstances.add(anElement);
 	}
 
 	protected void removeFromVisibilities(final Visibility anElement) {
 		this.ensureInitializedOrderedSets();
 		this.containedVisibilities.remove(anElement);
 		this.allContainedArrows.remove(anElement);
 		this.allContainedInstances.remove(anElement);
 	}
 
 	@Override
 	public Set container() {
 		if (this.properClass().isEqualTo(Vertex.vertex)) {
 			return ((Vertex) this).container();
 		}
 		else if (this.properClass().isEqualTo(Edge.edge)) {
 			return ((Edge) this).container();
 		}
 		else if (this.properClass().isEqualTo(Visibility.visibility)) {
 			return ((Visibility) this).container();
 		}
 		else if (this.properClass().isEqualTo(SuperSetReference.superSetReference)) {
 			return ((SuperSetReference) this).container();
 		}
 		else if (this.properClass().isEqualTo(Arrow.arrow)) {
 			return ((Arrow) this).container();
 		}
 		else if (this.isEqualTo(Graph.graph)) {
 			return Graph.graph;
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.kernelDefect_KernelHasReachedAnIllegalState.identity(), coreSets.kernelDefect);
 		}
 	}
 
 	@Override
 	public Set isSuperSetOf(final Set orderedPair) {
 		if (this.isEqualToRepresentation(orderedPair)) {return coreSets.is_TRUE;}
 		// only for performance optimisation
 		if (F_SemanticStateOfInMemoryModel.cellKernelSemanticDomainIsInitialized()) {
 			if (this.isEqualTo(S23MSemanticDomains.semanticIdentity)
 					&& (((OrderedPair)orderedPair.category()).isASemanticIdentity())) {return S23MSemanticDomains.is_TRUE;}
 		}
 
 		final Set a = this;
 		final Set aSuper = a.container().directSuperSetOf(a);
 		final boolean aHasTrueSuper = !aSuper.isEqualToRepresentation(a);
 
 		final Set b = orderedPair;
 		final Set bSuper = b.container().directSuperSetOf(b);
 		final boolean bHasTrueSuper = !bSuper.isEqualToRepresentation(b);
 
 		if (aHasTrueSuper && aSuper.isEqualToRepresentation(b)) {return coreSets.is_FALSE;}
 		if (bHasTrueSuper && bSuper.isEqualToRepresentation(a)) {return coreSets.is_TRUE;}
 		if (bHasTrueSuper) {return a.isSuperSetOf(bSuper);}
 		if (aHasTrueSuper) {return aSuper.isSuperSetOf(b);}
 		return coreSets.is_FALSE;
 
 	}
 
 	@Override
 	public Set localRootSuperSetOf(final Set orderedPair) {
 		Set temp = this.directSuperSetOf(orderedPair);
 		if (!(temp.isEqualToRepresentation(orderedPair))) {
 			if (this.allContainedInstances.containsRepresentation(temp)) {
 				temp = this.localRootSuperSetOf(temp);} else {
 					return temp;
 				}
 		}
 		return temp;
 	}
 
 	@Override
 	public Set isLocalSuperSetOf(final Set orderedPair) {
 		if (this.isEqualToRepresentation(orderedPair)) {
 			return coreSets.is_TRUE;
 		}
 		else {
 			if (this.allContainedInstances.containsRepresentation(orderedPair)) {
 				Set temp = this.directSuperSetOf(orderedPair);
 				while ((!(temp.isEqualToRepresentation(this))) && (!(temp.isEqualToRepresentation(orderedPair)))) {
 					temp = this.directSuperSetOf(temp);
 				}
 				if (temp.isEqualToRepresentation(this)) {
 					return coreSets.is_TRUE;
 				} else {
 					return coreSets.is_FALSE;
 				}
 			} else {
 				return coreSets.is_FALSE;
 			}
 		}
 	}
 
 	@Override
 	public Set directSuperSetOf(final Set orderedPair) {
 		if (this.allContainedInstances.containsRepresentation(orderedPair)) {
 			for (final Set superSetReference : this.getSuperSetReferences()) {
 				if (superSetReference.from().isEqualToRepresentation(orderedPair)) {
 					return ((SuperSetReference)superSetReference).getSuperSet();
 				}
 			}
 			return orderedPair;
 		} else {
 			if (this.isEqualTo(graph) && (
 					(orderedPair.isEqualTo(graph)
 							||	(orderedPair.isEqualTo(Arrow.arrow))
 							||	(orderedPair.isEqualTo(Vertex.vertex))
 							))) {
 				return graph;
 			} else {
 				if (this.isEqualTo(graph) && (
 						(orderedPair.isEqualTo(Edge.edge)
 								||	(orderedPair.isEqualTo(SuperSetReference.superSetReference))
 								||	(orderedPair.isEqualTo(Visibility.visibility))
 
 								))) {
 					return graph;
 				}
 			}}
 		return orderedPair;
 		// TODO : verify that there is no use case where the error condition below adds value
 		// return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 	}
 
 	@Override
 	public Set visibleInstancesForSubGraph(final Set subGraph) {
 		final OrderedSet visibleGraphs = new OrderedSet(identityFactory.createAnonymousIdentity());
 		final OrderedSet visibleChildren = new OrderedSet(identityFactory.createAnonymousIdentity());
 
 		// add the universally visible core instances
 		visibleGraphs.add(coreGraphs.vertex);
 		visibleGraphs.add(coreGraphs.graph);
 		for (final Set coreInstance : coreGraphs.vertex.filterProperClass(coreGraphs.vertex)) {
 			visibleGraphs.add(coreInstance);
 		}
 
 		for (final Set visibleSet : this.getVisibilities()) {
 			final Visibility iVisibility = (Visibility) visibleSet;
 			if (iVisibility.from().isEqualTo(subGraph)) {
 				visibleGraphs.add(((iVisibility.to())));
 				// collect all the Graph content of the toSubGraph
 				for (final Set visibleChild : iVisibility.to().filterInstances()) {
 					visibleChildren.add(visibleChild);
 				}
 			}
 		}
 
 		// also add all the Graph content of the toSubGraph to the visibility
 		for (final Set visibleChild : visibleChildren) {
 			visibleGraphs.add(visibleChild);
 		}
 
 		return visibleGraphs;
 	}
 
 	@Override
 	public Set hasVisibilityOf(final Set target) {
 		if (	(this.container().isEqualTo(target.container()))
 				|| (this.isEqualTo(target.container()))
 				|| (this.category().identity().isPartOfUniversalCellConcept())
 				)	{
 			return coreSets.is_TRUE;
 		} else {
 			final Set viz = this.container().container().visibleInstancesForSubGraph(this.container());
 			if (viz.containsSemanticMatch(target)) {
 				return coreSets.is_TRUE;
 			} else {
 				if (F_SemanticStateOfInMemoryModel.semanticDomainIsInitialized()) {
 					if (target.category().container().isEqualTo(SemanticDomain.semanticdomain) && viz.containsSemanticMatch(target.container())
 							|| (this.isEqualTo(Visualization.symbol))	) {
 						return coreSets.is_TRUE;
 					}
 				}
 				return coreSets.is_FALSE;
 			}
 		}
 	}
 
 	private Set getCategoryOfVisibility(final Graph target) {
 		if (	(this.container().isEqualTo(target.container()))
 				//				||	(this.isEqualTo(target.container())))	{
 				||	(this.isEqualTo(target.container()))
 				||	(this.category().identity().isPartOfUniversalCellConcept()))	{
 			return Visibility.visibility;
 		} else {
 			final Set viz = this.container().container().filterProperClass(coreGraphs.visibility);
 			for (final Set visibility : viz) {
 				if (   visibility.from().isEqualTo(this.container())
 						&& (visibility.to().isEqualTo(target.container())
 								|| (visibility.to().isEqualTo(target)	))
 						) {
 					return visibility;
 				}
 			}
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_TargetIsNotWithinVisibility.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set filterInstances() {
 		this.ensureInitializedOrderedSets();
 		return this.allContainedInstances;
 	}
 
 	@Override
 	public Set filterArrows() {
 		this.ensureInitializedOrderedSets();
 		return this.allContainedArrows;
 	}
 
 	@Override
 	public Set filterArrows(final Set properClassOrCategory, final Set fromSet, final Set toSet) {
 		if (properClassOrCategory.isInformation().is_TRUE()) {
 			if (fromSet.isInformation().is_FALSE()
 					&& toSet.isInformation().is_FALSE()) {
 				if (((OrderedPair)properClassOrCategory).isArrow()) {
 					return this.filterProperClass(properClassOrCategory);
 				} else {
 					return this.filterArrows().filter(properClassOrCategory);
 				}
 			}
 			if (((OrderedPair)properClassOrCategory).isArrow()) {
 				return this.filterProperClass(properClassOrCategory).filterByLinkedFromAndTo(fromSet, toSet);
 			} else {
 				return this.filterArrows().filter(properClassOrCategory).filterByLinkedFromAndTo(fromSet, toSet);
 			}
 		} else {
 			return this.filterArrows().filterByLinkedFromAndTo(fromSet, toSet);
 		}
 	}
 
 	@Override
 	public Set filterProperClass(final Set properClass) {
 		this.ensureInitializedOrderedSets();
 		if (properClass.isEqualTo(coreGraphs.vertex)) {
 			return this.containedVertices;
 		} else if (properClass.isEqualTo(coreGraphs.arrow) && this.isEqualTo(Graph.graph)) {
 			return Arrow.arrow;
 		} else if (properClass.isEqualTo(coreGraphs.arrow)) {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		} else if (properClass.isEqualTo(coreGraphs.edge)) {
 			return this.containedEdges;
 		} else if (properClass.isEqualTo(coreGraphs.visibility)) {
 			return this.containedVisibilities;
 		} else if (properClass.isEqualTo(coreGraphs.superSetReference)) {
 			return this.containedSuperSetReferences;
 		} else if (properClass.isEqualTo(coreGraphs.edgeEnd)) {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		} else if (properClass.isEqualTo(coreGraphs.graph) && this.isEqualTo(Graph.graph)) {
 			return this.allContainedInstances;
 		} else if (properClass.isEqualTo(coreGraphs.graph)) {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.kernelDefect_KernelHasReachedAnIllegalState.identity(), coreSets.kernelDefect);
 		}
 	}
 
 	@Override
 	public Set filter(final Set category) {
 		this.ensureInitializedOrderedSets();
 		final OrderedSet resultSet = new OrderedSet(identityFactory.createAnonymousIdentity());
 		for (final Set instance : this.allContainedInstances) {
 			if (instance.category().isEqualTo(category)) {
 				resultSet.add(instance);
 			}
 		}
 		return resultSet;
 	}
 
 	@Override
 	public Set filterPolymorphic(final Set category) {
 		this.ensureInitializedOrderedSets();
 		final OrderedSet resultSet = new OrderedSet(identityFactory.createAnonymousIdentity());
 		for (final Set instance : this.allContainedInstances) {
 			if (category.isSuperSetOf(instance.category()).isEqualTo(coreSets.is_TRUE)
 					&& category.isSuperSetOf(instance).isEqualTo(coreSets.is_FALSE)) {
 				resultSet.add(instance);
 			}
 		}
 		return resultSet;
 	}
 
 	@Override
 	public Set categoryOfVisibility(final Set target) {
 		return this.getCategoryOfVisibility((Graph)target);
 	}
 
 	@Override
 	public Set containsEdgeTo (final Set anElement) {
 		for (final Set edge : this.getEdges()) {
 			if (	(edge.from().isEqualTo(anElement))
 					|| 	(edge.to().isEqualTo(anElement)) ) {
 				return coreSets.is_TRUE;
 			}
 		}
 		return coreSets.is_FALSE;
 	}
 
 	@Override
 	public Set from() {
 		return F_InstantiationImpl.raiseError(coreSets.semanticErr_OnlyEdgeEndInstancesHaveEdgeEndVertex.identity(), coreSets.semanticErr);
 	}
 
 	@Override
 	public Set to() {
 		return F_InstantiationImpl.raiseError(coreSets.semanticErr_OnlyEdgeEndInstancesHaveEdgeEndVertex.identity(), coreSets.semanticErr);
 	}
 
 	@Override
 	public Set fromEdgeEnd() {
 		return F_InstantiationImpl.raiseError(coreSets.semanticErr_OnlyEdgeInstancesHaveConnectedRoles.identity(), coreSets.semanticErr);
 	}
 
 	@Override
 	public Set toEdgeEnd() {
 		return F_InstantiationImpl.raiseError(coreSets.semanticErr_OnlyEdgeInstancesHaveConnectedRoles.identity(), coreSets.semanticErr);
 	}
 
 	@Override
 	public Set edgeEnds() {
 		return F_InstantiationImpl.raiseError(coreSets.semanticErr_OnlyEdgeInstancesHaveConnectedRoles.identity(), coreSets.semanticErr);
 	}
 
 	@Override
 	public Set properClass() {
 		return Graph.graph;
 	}
 
 	public Set addConcrete(final Set category, final Identity semanticIdentity) {
 		if (category.properClass().isEqualTo(coreGraphs.vertex)) {
 			return (F_Instantiation.addConcreteVertex(this, semanticIdentity, category));
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_AddConcreteIsOnlyValidForConcreteVertex.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set addConcrete(final Set category, final Set semanticIdentity) {
 		if (category.properClass().isEqualTo(coreGraphs.vertex)) {
 			return (F_Instantiation.addConcreteVertex(this, identityFactory.reuseSemanticIdentity(semanticIdentity.identity()), category));
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_AddConcreteIsOnlyValidForConcreteVertex.identity(), coreSets.semanticErr);
 		}
 	}
 
 	public Set addAbstract(final Set category, final Identity semanticIdentity) {
 		if (category.properClass().isEqualTo(coreGraphs.vertex)) {
 			return (F_Instantiation.addAbstractVertex(this, semanticIdentity, category));
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_AddAbstractIsOnlyValidForAbstractVertex.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set addAbstract(final Set category, final Set semanticIdentity) {
 		if (category.properClass().isEqualTo(coreGraphs.vertex)) {
 			return (F_Instantiation.addAbstractVertex(this, identityFactory.reuseSemanticIdentity(semanticIdentity.identity()), category));
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_AddAbstractIsOnlyValidForAbstractVertex.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set decommission() {
 		if (F_SemanticStateOfInMemoryModel.cellKernelSemanticDomainIsInitialized()) {
 			final Set dependentSets = Query.findDependentSets(this);
 			if (dependentSets.size() == 0) {
 				this.isDecommissioned = true;
 				this.isNewInstance = false;
 				Graph.addSetToChangedSets(this);
 				((Graph) this.container()).setContainsDecommissionedSets(true);
 				Graph.addSetToChangedSets(this.container());
 				return coreSets.successful;
 			} else {
 				return dependentSets;
 			}
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		}
 	}
 
 	public Set delete(final Set set) {
 		if (set.category().properClass().isEqualTo(coreGraphs.vertex)) {
 			this.removeFromVertices((Vertex)set);
 			return set;
 		} else {
 			if (set.category().properClass().isEqualTo(coreGraphs.edge)) {
 				this.removeFromEdges((Edge)set);
 				return set;
 			} else {
 
 				if (set.category().properClass().isEqualTo(coreGraphs.superSetReference)) {
 					this.removeFromGeneralizationReferences((SuperSetReference)set);
 					return set;
 				} else {
 					if (set.category().properClass().isEqualTo(coreGraphs.visibility)) {
 						this.removeFromVisibilities((Visibility)set);
 						return set;
 					} else {
 						return F_InstantiationImpl.raiseError(coreSets.semanticErr_GraphsCantBeDecommissioned.identity(), coreSets.semanticErr);
 					}
 				}
 
 			}
 		}
 	}
 
 	protected void setContainsNewSets(final boolean value) {
 		this.containsNewSets = value;
 	}
 
 	protected void setContainsDecommissionedSets(final boolean value) {
 		this.containsDecommissionedSets = value;
 	}
 
 	protected void setHasNewPayload(final boolean value) {
 		this.hasNewPayload = value;
 	}
 
 	protected void setHasDecommissionedPayload(final boolean value) {
 		this.hasDecommissionedPayload = value;
 	}
 
 	@Override
 	public Set isExternal() {
 		if (  	this.properClass().isEqualTo(coreGraphs.edge)
 				||	this.properClass().isEqualTo(coreGraphs.superSetReference)
 				||	this.properClass().isEqualTo(coreGraphs.visibility)
 				) {
 			return F_InstantiationImpl.raiseError(coreSets.kernelDefect_KernelHasReachedAnIllegalState.identity(), coreSets.kernelDefect); // result must be overriden by specialization!
 		} else {
 			return coreSets.is_FALSE;
 		}
 	}
 
 	/**
 	 * Graph queries
 	 */
 	protected void addProperClassQueries() {
 		this.addToExecutableQueries(coreSets.identity);
 		this.addToExecutableQueries(coreSets.container);
 		this.addToExecutableQueries(coreSets.filter);
 		this.addToExecutableQueries(coreSets.containsEdgeFromOrTo);
 		this.addToExecutableQueries(coreSets.filterProperClass);
 		this.addToExecutableQueries(coreSets.hasVisibilityOf);
 		this.addToExecutableQueries(coreSets.filterInstances);
 		this.addToExecutableQueries(coreSets.isLocalSuperSetOf);
 		this.addToExecutableQueries(coreSets.isSuperSetOf);
 		this.addToExecutableQueries(coreSets.filterArrows);
 		this.addToExecutableQueries(coreSets.localRootSuperSetOf);
 		this.addToExecutableQueries(coreSets.directSuperSetOf);
 		this.addToExecutableQueries(coreSets.category);
 		this.addToExecutableQueries(coreSets.containerCategory);
 		this.addToExecutableQueries(coreSets.variables);
 		this.addToExecutableQueries(coreSets.value);
 		this.addToExecutableQueries(coreSets.values);
 		this.addToExecutableQueries(coreSets.visibleInstancesForSubGraph);
 		this.addToExecutableQueries(coreSets.allowableEdgeCategories);
 		this.addToExecutableQueries(coreSets.filterPolymorphic);
 		this.addToExecutableQueries(coreSets.semanticIdentity);
 		this.addToExecutableQueries(coreSets.commands);
 		this.addToExecutableQueries(coreSets.queries);
 		this.addToExecutableQueries(coreSets.executableCommands);
 		this.addToExecutableQueries(coreSets.executableQueries);
 	}
 
 	/**
 	 * Graph commands
 	 */
 	protected void addProperClassCommands() {
 		this.addToExecutableCommands(coreSets.addAbstract);
 		this.addToExecutableCommands(coreSets.addConcrete);
 	}
 
 	@Override
 	public Set containsDecommissionedSets() {
 		if (containsDecommissionedSets) {
 			return coreSets.is_TRUE;
 		} else {
 			return coreSets.is_FALSE;
 		}
 	}
 
 	@Override
 	public Set containsNewSets() {
 		if (containsNewSets) {
 			return coreSets.is_TRUE;
 		} else {
 			return coreSets.is_FALSE;
 		}
 	}
 
 	@Override
 	public Set hasNewPayload() {
 		if (hasNewPayload) {
 			return coreSets.is_TRUE;
 		} else {
 			return coreSets.is_FALSE;
 		}
 	}
 
 	@Override
 	public Set hasDecommissionedPayload() {
 		if (hasDecommissionedPayload) {
 			return coreSets.is_TRUE;
 		} else {
 			return coreSets.is_FALSE;
 		}
 	}
 
 	@Override
 	public Set hasNewName() {
 		if (SemanticDomain.semanticIdentity.isSuperSetOf(this.category()).isEqualTo(coreSets.is_TRUE)) {
 			if (hasNewName) {
 				return coreSets.is_TRUE;
 			} else {
 				return coreSets.is_FALSE;
 			}
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set hasNewPluralName() {
 		if (SemanticDomain.semanticIdentity.isSuperSetOf(this.category()).isEqualTo(coreSets.is_TRUE)) {
 			if (hasNewPluralName) {
 				return coreSets.is_TRUE;
 			} else {
 				return coreSets.is_FALSE;
 			}
 		} else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set isDecommissioned() {
 		if (isDecommissioned) {
 			return coreSets.is_TRUE;
 		} else {
 			return coreSets.is_FALSE;
 		}
 	}
 
 	@Override
 	public Set isNewInstance() {
 		if (isNewInstance) {
 			return coreSets.is_TRUE;
 		} else {
 			return coreSets.is_FALSE;
 		}
 	}
 
 	@Override
 	public Set assignNewName(final String newName) {
 		if (SemanticDomain.semanticIdentity.isSuperSetOf(this.category()).isEqualTo(coreSets.is_TRUE)) {
 			((IdentityImpl) this.identity()).assignNewName(newName);
 			if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 				this.hasNewName = true;
 				Graph.addSetToChangedSets(this);
 			}
 			return coreSets.successful;}
 		else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set assignNewPluralName(final String newPluralName) {
 		if (SemanticDomain.semanticIdentity.isSuperSetOf(this.category()).isEqualTo(coreSets.is_TRUE)) {
 			((IdentityImpl) this.identity()).assignNewPluralName(newPluralName);
 			if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 				this.hasNewPluralName = true;
 				Graph.addSetToChangedSets(this);
 			}
 			return coreSets.successful;}
 		else {
 			return F_InstantiationImpl.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance.identity(), coreSets.semanticErr);
 		}
 	}
 
 	@Override
 	public Set assignNewPayload(final String newPayload) {
 		this.identity().setPayload(newPayload);
 		if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 			this.setHasNewPayload(true);
 			this.setHasDecommissionedPayload(false);
 			Graph.addSetToChangedSets(this);
 		}
 		return coreSets.successful;
 	}
 
 	@Override
 	public Set decommissionPayload() {
 		this.identity().setPayload(null);
 		if (F_SemanticStateOfInMemoryModel.cellEditorIsLive()) {
 			this.setHasDecommissionedPayload(true);
 			this.setHasNewPayload(false);
 			Graph.addSetToChangedSets(this);
 		}
 		return coreSets.successful;
 	}
 
 	public Set clearModificationState() {
 		isNewInstance = false;
 		isDecommissioned = false;
 		containsNewSets = false;
 		containsDecommissionedSets = false;
 		hasNewName = false;
 		hasNewPluralName = false;
 		hasNewPayload = false;
 		hasDecommissionedPayload = false;
 		return coreSets.successful;
 	}
 
 	@Override
 	public Set allowableEdgeCategories(final Set orderedPair) {
 		final OrderedSet result = new OrderedSet(identityFactory.createAnonymousIdentity());
 		for (final Set edge : this.category().container().filterProperClass(Edge.edge)) {
 			if (((edge.from().isSuperSetOf(this.category()).isEqualTo(coreSets.is_TRUE)))
 					&& (ArrowConstraints.isAllowableEdgeCategory(edge, this, orderedPair).isEqualTo(coreSets.is_TRUE))) {
 				result.add(edge);
 			}
 			if (this.category().container().isEqualTo(graph)) {
 				result.add(Edge.edge);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public Set processEvent(final Set event) {
 		return event;
 	}
 
 	@Override
 	public Set unionOfconnectingArrows(final Set instance) {
 		final OrderedSet result = new OrderedSet(identityFactory.createAnonymousIdentity());
 		for (final Set arrow : instance.container().filterArrows()) {
 			if ((arrow.from().isEqualToRepresentation(instance) && (arrow.to().isEqualToRepresentation(this)))
 					|| ((arrow.from().isEqualToRepresentation(this) && arrow.to().isEqualToRepresentation(instance)))) {
 				result.add(arrow);
 			}
 		}
 		for (final Set arrow : this.container().filterArrows()) {
 			if ((arrow.from().isEqualToRepresentation(instance) && (arrow.to().isEqualToRepresentation(this)))
 					|| ((arrow.from().isEqualToRepresentation(this) && arrow.to().isEqualToRepresentation(instance)))) {
 				result.add(arrow);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public String localVisualRecognitionText() {
 		return this.identity().name() + " : " + this.category().identity().name();
 	}
 	@Override
 	public String visualRecognitionText() {
 		if (this.category().isEqualTo(this)) {
 			return this.identity().name();
 		} else {
 			return this.identity().name() + "." + this.container().visualRecognitionText();
 		}
 	}
 	@Override
 	public String fullVisualRecognitionText() {
 		return this.visualRecognitionText() + " : " + this.category().visualRecognitionText();
 	}
 
 	/**
 	 * Support for Information Quality Logic
 	 */
 
 	@Override
 	public Set not() {
 		return F_IqLogic.not(this);
 	}
 
 	@Override
 	public Set and(final Set b) {
 		return F_IqLogic.and(this, b);
 	}
 	@Override
 	public Set or(final Set b) {
 		return F_IqLogic.or(this, b);
 	}
 
 	@Override
 	public Set isQuality() {
 		return F_IqLogic.isQuality(this);
 	}
 
 	@Override
 	public Set isInformation() {
 		return F_IqLogic.isInformation(this);
 	}
 	@Override
 	public  boolean is_NOTAPPLICABLE() {
 		return this.isEqualTo(S23MSemanticDomains.is_NOTAPPLICABLE);
 	}
 	@Override
 	public  boolean is_FALSE() {
 		return this.isEqualTo(S23MSemanticDomains.is_FALSE);
 	}
 	@Override
 	public  boolean is_UNKNOWN() {
 		return this.isEqualTo(S23MSemanticDomains.is_UNKNOWN);
 	}
 	@Override
 	public  boolean is_TRUE() {
 		return this.isEqualTo(S23MSemanticDomains.is_TRUE);
 	}
 	@Override
 	public Set includesValue(final Set value, final Set equivalenceClass) {
 		return F_IqLogic.includesValue(this, value, equivalenceClass);
 	}
 	@Override
 	public Set isEqualTo(final Set set, final Set equivalenceClass) {
 		return F_IqLogic.isEqualTo(set, equivalenceClass);
 	}
 
 	/**
 	 * support of Set Algebra
 	 **/
 
 
 	/**
 	 * the union of this and semanticIdentity
 	 */
 	@Override
 	public Set union(final Set semanticIdentity) {
 		return F_SetAlgebra.union(this, semanticIdentity);
 	}
 	@Override
 	public Set wrapInOrderedSet() {
 		return F_SetAlgebra.wrapInOrderedSet(this);
 	}
 
 	/**
 	 * the intersection of this and semanticIdentity
 	 */
 	@Override
 	public Set intersection(final Set set) {
 		return F_SetAlgebra.intersection(this, set);
 	}
 	/**
 	 * the complement of this and semanticIdentity
 	 */
 	@Override
 	public Set complement(final Set set) {
 		return F_SetAlgebra.complement(this, set);
 	}
 	@Override
 	public Set isElementOf(final Set semanticIdentity){
 		return F_SetAlgebra.isElementOf(this, semanticIdentity);
 	}
 	@Override
 	public Set transformToOrderedSetOfSemanticIdentities() {
 		return F_SetAlgebra.transformToOrderedSetOfSemanticIdentities(this.filterInstances());
 	}
 
 	/**
 	 * Establish symmetry between graphs and ordered set
 	 * Delegate to the instanceSet
 	 */
 
 	@Override
 	public Set filterByLinkedTo(final Set toSet) {
 		return this.filterArrows().filterByLinkedTo(toSet);
 	}
 	@Override
 	public Set filterByLinkedFrom(final Set fromSet) {
 		return this.filterArrows().filterByLinkedFrom(fromSet);
 	}
 	@Override
 	public Set filterByLinkedFromAndTo(final Set fromSet, final Set toSet) {
 		return this.filterArrows().filterByLinkedFromAndTo(toSet, fromSet);
 	}
 	@Override
 	public Set filterByLinkedToVia(final Set toEdgeEnd) {
 		return this.filterArrows().filterByLinkedToVia(toEdgeEnd);
 	}
 	@Override
 	public Set filterByLinkedFromVia(final Set fromEdgeEnd) {
 		return this.filterArrows().filterByLinkedFromVia(fromEdgeEnd);
 	}
 	@Override
 	public Set filterByLinkedFromAndToVia(final Set fromEdgeEnd, final Set toEdgeEnd) {
 		return this.filterArrows().filterByLinkedFromAndToVia(fromEdgeEnd, toEdgeEnd );
 	}
 	@Override
 	public Set filterFrom() {
 		return this.filterArrows().filterFrom();
 	}
 	@Override
 	public Set filterTo() {
 		return this.filterArrows().filterTo();
 	}
 	@Override
 	public Set filterFromAndTo() {
 		return this.filterArrows().filterFromAndTo();
 	}
 	@Override
 	public Set filterByLinkedToSemanticRole(final Set toSetReferencedSemanticRole) {
 		return this.filterArrows().filterByLinkedToSemanticRole(toSetReferencedSemanticRole);
 	}
 	@Override
 	public Set filterByLinkedFromSemanticRole(final Set fromSetReferencedSemanticRole) {
 		return this.filterArrows().filterByLinkedFromSemanticRole(fromSetReferencedSemanticRole);
 	}
 	@Override
 	public Set filterByLinkedFromAndToSemanticRole(final Set fromSetReferencedSemanticRole, final Set toSetReferencedSemanticRole) {
 		return this.filterArrows().filterByLinkedFromAndToSemanticRole(fromSetReferencedSemanticRole, toSetReferencedSemanticRole);
 	}
 	@Override
 	public Set filterBySemanticIdentity(final Set set) {
 		return this.filterInstances().filterBySemanticIdentity(set);
 	}
 	@Override
 	public Set filterByEquivalenceClass(final Set set) {
 		return this.filterInstances().filterByEquivalenceClass(set);
 	}
 	@Override
 	public Set extractUniqueMatch(final Set set) {
 		return this.filterInstances().extractUniqueMatch(set);
 	}
 	@Override
 	public Set extractUniqueMatch(final Identity identity) {
 		return this.filterInstances().extractUniqueMatch(identity);
 	}
 	@Override
 	public Set extractUniqueMatch(final String uuidAsString) {
 		return this.filterInstances().extractUniqueMatch(uuidAsString);
 	}
 	@Override
 	public Set extractFirst() {
 		return this.filterInstances().extractFirst();
 	}
 	@Override
 	public Set extractSecond() {
 		return this.filterInstances().extractSecond();
 	}
 	@Override
 	public Set extractLast() {
 		return this.filterInstances().extractLast();
 	}
 	@Override
 	public Set extractNext(final Set element) {
 		return this.filterInstances().extractNext(element);
 	}
 	@Override
 	public Set extractPrevious(final Set element) {
 		return this.filterInstances().extractPrevious(element);
 	}
 	/**
 	 * See {@link java.util.List#contains(Object)}
 	 */
 	@Override
 	public boolean containsSemanticMatch(final Set set) {
 		return this.filterInstances().containsSemanticMatch(set);
 	}
 	@Override
 	public boolean containsRepresentation(final Set set) {
 		return this.filterInstances().containsSemanticMatch(set);
 	}
 	@Override
 	public boolean containsSemanticMatchesForAll(final Set set) {
 		return this.filterInstances().containsSemanticMatchesForAll(set);
 	}
 	@Override
 	public boolean containsAllRepresentations(final Set set) {
 		return this.filterInstances().containsAllRepresentations(set);
 	}
 	@Override
 	public boolean isEmpty() {
 		return this.filterInstances().isEmpty();
 	}
 	@Override
 	public ListIterator<Set> listIterator() {
 		return this.filterInstances().listIterator();
 	}
 	@Override
 	public ListIterator<Set> listIterator(final int index) {
 		return this.filterInstances().listIterator(index);
 	}
 	@Override
 	public int size() {
 		return this.filterInstances().size();
 	}
 	@Override
 	public Set[] toArray() {
 		return this.filterInstances().toArray();
 	}
 	@Override
 	public Set[] toArray(final Set[] a) {
 		return this.filterInstances().toArray(a);
 	}
 	@Override
 	public List<Set> asList() {
 		return this.filterInstances().asList();
 	}
 	@Override
 	public Set and() {
 		return this.filterInstances().and();
 	}
 	@Override
 	public Set or() {
 		return this.filterInstances().or();
 	}
 	@Override
 	public  Set initializeWalk(final VisitorFunction visitorFunction) {
 		return visitorFunction.initialize(this);
 	}
 
 	@Override
 	public Set walkDownThenRight(final VisitorFunction visitorFunction) {
 		final Set content = this.filterInstances();
 		for (final Set element : content) {
 			element.walkDownThenRight(visitorFunction);
 		}
 		return visitorFunction.compute(this);
 	}
 
 	@Override
 	public Set walkDownThenLeft(final VisitorFunction visitorFunction) {
 		final Set content = this.filterInstances();
 		Set element = content.extractLast();
 		for (int i = 0; i < content.size(); i++) {
 			element.walkDownThenLeft(visitorFunction);
 			element = content.extractPrevious(element);
 		}
 		return visitorFunction.compute(this);
 	}
 
 	@Override
 	public Set walkRightThenDown(final VisitorFunction visitorFunction) {
 		final Set content = this.filterInstances();
 		for (final Set element : content) {
 			visitorFunction.compute(element);
 		}
		return this.walkRightThenDown(visitorFunction);
 	}
 	@Override
 	public Set walkLeftThenDown(final VisitorFunction visitorFunction) {
 		final Set content = this.filterInstances();
 		Set element = content.extractLast();
 		visitorFunction.compute(element);
 		for (int i = 0; i < content.size(); i++) {
 			element = content.extractPrevious(element);
 			visitorFunction.compute(element);
 		}
		this.walkLeftThenDown(visitorFunction);
 		return visitorFunction.compute(this);
 	}
 }
