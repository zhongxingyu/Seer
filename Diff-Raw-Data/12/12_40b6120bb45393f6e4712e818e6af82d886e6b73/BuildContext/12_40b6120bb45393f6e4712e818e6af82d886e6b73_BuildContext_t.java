 /*******************************************************************************
  * Copyright (c) 2010 Broadcom Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Alex Collins (Broadcom Corp.) - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.internal.events;
 
 import java.util.*;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 
 public class BuildContext implements IBuildContext {
 	/** The project variant for which this context applies. */
 	private final IProjectVariant projectVariant;
 	/** The build order for the build that this context is part of. */
 	private final IProjectVariant[] buildOrder;
 	/** The position in the build order array that this project variant is. */
 	private final int buildOrderPosition;
 
 	static final IProjectVariant[] EMPTY_PROJECT_VARIANT_ARRAY = new IProjectVariant[0];
 	/**
 	 * A directed acyclic graph of the project variant's referenced project variants
 	 * that were built before this project variant.
 	 * 
 	 * This is computed on demand, when the client requests the information, and is
 	 * subsequently cached.
 	 */
 	private DAG referencesGraph = null;
 	/**
 	 * A directed acyclic graph of the project variants referencing this project variant
 	 * that will be built after this project variant.
 	 * 
 	 * This is computed on demand, when the client requests the information, and is
 	 * subsequently cached.
 	 */
 	private DAG referencingGraph = null;
 
 	/** Cached lists of referenced and referencing projects and project variants */
 	private IProject[] cachedReferencedProjects = null;
 	private IProjectVariant[] cachedReferencedProjectVariants = null;
 	private IProject[] cachedReferencingProjects = null;
 	private IProjectVariant[] cachedReferencingProjectVariants = null;
 
 	/**
 	 * A lightweight implementation of a directed acyclic graph
 	 * This implementation does not check for the acyclic nature of the
 	 * graph, it relies on the caller to supply a set of edges that does not
 	 * create a cycle.
 	 */
 	private static class DAG {
 		private static class Vertex {
 			Set children = new HashSet();
 			Vertex() {
 				// Empty ctor
 			}
 		}
 
 		private Map vertexMap = new HashMap();
 		private Set vertices = new HashSet();
 
 		DAG() {
 			// Empty ctor
 		}
 
 		public Set getVertices() {
 			return vertexMap.keySet();
 		}
 
 //		public boolean hasVertex(Object id) {
 //			return vertexMap.containsKey(id);
 //		}
 //
 //		public List getChildren(Object id) {
 //			if (vertexMap.containsKey(id))
 //				return ((Vertex) vertexMap.get(id)).children;
 //			return null;
 //		}
 
 		void addVertex(Object id) {
 			Vertex vertex = new Vertex();
 			if (!vertices.contains(vertex)) {
 				vertices.add(vertex);
 				vertexMap.put(id, vertex);
 			}
 		}
 
 		void addEdge(Object from, Object to) {
 			Vertex fromVertex = (Vertex) vertexMap.get(from);
 			Vertex toVertex = (Vertex) vertexMap.get(to);
 			if (!vertices.contains(fromVertex))
 				return;
 			if (!vertices.contains(toVertex))
 				return;
 			fromVertex.children.add(toVertex);
 		}
 	}
 
 	/**
 	 * Create an empty build context for the given project variant.
 	 * @param projectVariant the project variant being built, that we need the context for
 	 */
 	public BuildContext(IProjectVariant projectVariant) {
 		this.projectVariant = projectVariant;
 		buildOrder = EMPTY_PROJECT_VARIANT_ARRAY;
 		buildOrderPosition = -1;
 
 		referencesGraph = new DAG();
 		referencesGraph.addVertex(projectVariant);
 	}
 
 	/**
 	 * Create a build context for the given project variant.
 	 * @param projectVariant the project variant being built, that we need the context for
 	 * @param buildOrder the build order for the entire build, indicating how cycles etc. have been resolved
 	 */
 	public BuildContext(IProjectVariant projectVariant, IProjectVariant[] buildOrder) {
 		this.projectVariant = projectVariant;
 		this.buildOrder = buildOrder;
 		int position = -1;
 		for (int i = 0; i < buildOrder.length; i++) {
 			if (buildOrder[i].equals(projectVariant))
 			{
 				position = i;
 				break;
 			}
 		}
 		buildOrderPosition = position;
 		Assert.isTrue(0 <= buildOrderPosition && buildOrderPosition < buildOrder.length);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedProjects()
 	 */
 	public IProject[] getAllReferencedProjects() {
 		if (referencesGraph == null)
 			computeReferencesGraph();
 		if (cachedReferencedProjects == null) {
 			Set set = new HashSet();
			for (Iterator it = referencesGraph.getVertices().iterator(); it.hasNext();)
 				set.add(((IProjectVariant) it.next()).getProject());
 			cachedReferencedProjects = new IProject[set.size()];
 			set.toArray(cachedReferencedProjects);
 		}
 		return (IProject[]) cachedReferencedProjects.clone();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedProjectVariants()
 	 */
 	public IProjectVariant[] getAllReferencedProjectVariants() {
 		if (referencesGraph == null)
 			computeReferencesGraph();
 		if (cachedReferencedProjectVariants == null) {
 			cachedReferencedProjectVariants = new IProjectVariant[referencesGraph.getVertices().size()];
 			referencesGraph.getVertices().toArray(cachedReferencedProjectVariants);
 		}
 		return (IProjectVariant[]) cachedReferencedProjectVariants.clone();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingProjects()
 	 */
 	public IProject[] getAllReferencingProjects() {
 		if (referencingGraph == null)
 			computeReferencingGraph();
 		if (cachedReferencingProjects == null) {
			Set set = new HashSet();
			for (Iterator it = referencingGraph.getVertices().iterator(); it.hasNext();)
				set.add(((IProjectVariant) it.next()).getProject());
			cachedReferencingProjects = new IProject[set.size()];
			set.toArray(cachedReferencingProjects);
 		}
 		return (IProject[]) cachedReferencingProjects.clone();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingProjectVariants()
 	 */
 	public IProjectVariant[] getAllReferencingProjectVariants() {
 		if (referencingGraph == null)
 			computeReferencingGraph();
 		if (cachedReferencingProjectVariants == null) {
 			cachedReferencingProjectVariants = new IProjectVariant[referencingGraph.getVertices().size()];
 			referencingGraph.getVertices().toArray(cachedReferencingProjectVariants);
 		}
 		return (IProjectVariant[]) cachedReferencingProjectVariants.clone();
 	}
 
 	private void computeReferencesGraph() {
 		referencesGraph = new DAG();
 
 		Set previousVariants = new HashSet();
 		for (int i = 0; i < buildOrderPosition; i++)
 			previousVariants.add(buildOrder[i]);
 
 		// Do a depth first search of the project variant's references to construct
 		// the references graph.
 		computeGraph(referencesGraph, projectVariant, previousVariants, new GetChildrenFunctor() {
 			public IProjectVariant[] run(IProjectVariant variant) {
 				try {
 					return variant.getProject().getReferencedProjectVariants(variant.getVariant());
 				} catch (CoreException e) {
 					return EMPTY_PROJECT_VARIANT_ARRAY;
 				}
 			}
 		});
 	}
 
 	private void computeReferencingGraph() {
 		referencingGraph = new DAG();
 
 		Set subsequentVariants = new HashSet();
 		for (int i = buildOrderPosition+1; i < buildOrder.length; i++)
 			subsequentVariants.add(buildOrder[i]);
 
 		// Do a depth first search of the project variant's referencing variants
 		// to construct the referencing graph.
 		computeGraph(referencingGraph, projectVariant, subsequentVariants, new GetChildrenFunctor() {
 			public IProjectVariant[] run(IProjectVariant variant) {
 				return variant.getProject().getReferencingProjectVariants(variant.getVariant());
 			}
 		});
 	}
 
 	private static interface GetChildrenFunctor {
 		IProjectVariant[] run(IProjectVariant variant);
 	}
 
 	private void computeGraph(DAG graph, IProjectVariant root, Set filter, GetChildrenFunctor getChildren) {
 		Stack stack = new Stack();
 		stack.push(root);
 		Set visited = new HashSet();
 
 		if (filter.contains(root))
 			graph.addVertex(root);
 
 		while (!stack.isEmpty()) {
 			IProjectVariant variant = (IProjectVariant) stack.pop();
 			visited.add(variant);
 			IProjectVariant[] refs = getChildren.run(variant);
 			for (int i = 0; i < refs.length; i++) {
 				IProjectVariant ref = refs[i];
 	
 				if (!filter.contains(ref))
 					continue;
 
 				// Avoid exploring cycles
 				if (visited.contains(ref))
 					continue;
 
 				graph.addVertex(ref);
 				if (!variant.equals(root))
 					graph.addEdge(variant, ref);
 				stack.push(ref);
 			}
 		}
 	}
 }
