 /*******************************************************************************
  * Copyright (c) 2010 Broadcom Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Broadcom Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.internal.events;
 
 import org.eclipse.core.resources.IBuildConfiguration;
 
 import java.util.*;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 
 /**
 * Concrete implementation of a build context
  */
 public class BuildContext implements IBuildContext {
 	/** The build configuration for which this context applies. */
 	private final IBuildConfiguration buildConfiguration;
 	/** The build order for the build that this context is part of. */
 	private final IBuildConfiguration[] buildOrder;
 	/** The position in the build order array that this project configuration is. */
 	private final int buildOrderPosition;
 
 	private static final IBuildConfiguration[] EMPTY_BUILD_CONFIGURATION_ARRAY = new IBuildConfiguration[0];
 
 	/** Cached lists of referenced and referencing projects and project configurations */
 	private IProject[] cachedReferencedProjects = null;
 	private IBuildConfiguration[] cachedReferencedBuildConfigurations = null;
 	private IProject[] cachedReferencingProjects = null;
 	private IBuildConfiguration[] cachedReferencingBuildConfigurations = null;
 
 	/**
 	 * Create an empty build context for the given project configuration.
 	 * @param buildConfiguration the project configuration being built, that we need the context for
 	 */
 	public BuildContext(IBuildConfiguration buildConfiguration) {
 		this.buildConfiguration = buildConfiguration;
 		buildOrder = EMPTY_BUILD_CONFIGURATION_ARRAY;
 		buildOrderPosition = -1;
 	}
 
 	/**
 	 * Create a build context for the given project configuration.
 	 * @param buildConfiguration the project configuration being built, that we need the context for
 	 * @param buildOrder the build order for the entire build, indicating how cycles etc. have been resolved
 	 */
 	public BuildContext(IBuildConfiguration buildConfiguration, IBuildConfiguration[] buildOrder) {
 		this.buildConfiguration = buildConfiguration;
 		this.buildOrder = buildOrder;
 		int position = -1;
 		for (int i = 0; i < buildOrder.length; i++) {
 			if (buildOrder[i].equals(buildConfiguration))
 			{
 				position = i;
 				break;
 			}
 		}
 		buildOrderPosition = position;
 		Assert.isTrue(-1 <= buildOrderPosition && buildOrderPosition < buildOrder.length);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedProjects()
 	 */
 	public IProject[] getAllReferencedProjects() {
 		if (cachedReferencedBuildConfigurations == null)
 			cachedReferencedBuildConfigurations = computeReferenced();
 		if (cachedReferencedProjects == null)
 			cachedReferencedProjects = projectConfigurationsToProjects(cachedReferencedBuildConfigurations);
 		return (IProject[]) cachedReferencedProjects.clone();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencedBuildConfigurations()
 	 */
 	public IBuildConfiguration[] getAllReferencedBuildConfigurations() {
 		if (cachedReferencedBuildConfigurations == null)
 			cachedReferencedBuildConfigurations = computeReferenced();
 		return (IBuildConfiguration[]) cachedReferencedBuildConfigurations.clone();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingProjects()
 	 */
 	public IProject[] getAllReferencingProjects() {
 		if (cachedReferencingBuildConfigurations == null)
 			cachedReferencingBuildConfigurations = computeReferencing();
 		if (cachedReferencingProjects == null)
 			cachedReferencingProjects = projectConfigurationsToProjects(cachedReferencingBuildConfigurations);
 		return (IProject[]) cachedReferencingProjects.clone();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IBuildContext#getAllReferencingBuildConfigurations()
 	 */
 	public IBuildConfiguration[] getAllReferencingBuildConfigurations() {
 		if (cachedReferencingBuildConfigurations == null)
 			cachedReferencingBuildConfigurations = computeReferencing();
 		return (IBuildConfiguration[]) cachedReferencingBuildConfigurations.clone();
 	}
 
 	private IBuildConfiguration[] computeReferenced() {
 		Set previousConfigs = new HashSet();
 		for (int i = 0; i < buildOrderPosition; i++)
 			previousConfigs.add(buildOrder[i]);
 
 		// Do a depth first search of the project configuration's references to construct
 		// the references graph.
 		return computeReachable(buildConfiguration, previousConfigs, new GetChildrenFunctor() {
 			public IBuildConfiguration[] run(IBuildConfiguration configuration) {
 				try {
 					return configuration.getProject().getReferencedBuildConfigurations(configuration);
 				} catch (CoreException e) {
 					return EMPTY_BUILD_CONFIGURATION_ARRAY;
 				}
 			}
 		});
 	}
 
 	private IBuildConfiguration[] computeReferencing() {
 		Set subsequentConfigs = new HashSet();
 		for (int i = buildOrderPosition+1; i < buildOrder.length; i++)
 			subsequentConfigs.add(buildOrder[i]);
 
 		// Do a depth first search of the project configuration's referencing configurations
 		// to construct the referencing graph.
 		return computeReachable(buildConfiguration, subsequentConfigs, new GetChildrenFunctor() {
 			public IBuildConfiguration[] run(IBuildConfiguration configuration) {
 				return configuration.getProject().getReferencingBuildConfigurations(configuration);
 			}
 		});
 	}
 
 	private static interface GetChildrenFunctor {
 		IBuildConfiguration[] run(IBuildConfiguration configuration);
 	}
 
 	/**
 	 * Perform a depth first search from the given root using the a functor to
 	 * get the children for each item.
 	 * @returns A set containing all the reachable project configurations from the given root.
 	 */
 	private IBuildConfiguration[] computeReachable(IBuildConfiguration root, Set filter, GetChildrenFunctor getChildren) {
 		Set result = new HashSet();
 		Stack stack = new Stack();
 		stack.push(root);
 		Set visited = new HashSet();
 
 		if (filter.contains(root))
 			result.add(root);
 
 		while (!stack.isEmpty()) {
 			IBuildConfiguration configuration = (IBuildConfiguration) stack.pop();
 			visited.add(configuration);
 			IBuildConfiguration[] refs = getChildren.run(configuration);
 			for (int i = 0; i < refs.length; i++) {
 				IBuildConfiguration ref = refs[i];
 
 				if (!filter.contains(ref))
 					continue;
 
 				// Avoid exploring cycles
 				if (visited.contains(ref))
 					continue;
 
 				result.add(ref);
 				stack.push(ref);
 			}
 		}
 
 		return (IBuildConfiguration[]) result.toArray(new IBuildConfiguration[result.size()]);
 	}
 
 	/**
 	 * Convert a list of project configurations to projects, while removing duplicates.
 	 */
 	private IProject[] projectConfigurationsToProjects(IBuildConfiguration[] configs) {
 		Set set = new HashSet();
 		for (int i = 0; i < configs.length; i++)
 			set.add(configs[i].getProject());
 		return (IProject[]) set.toArray(new IProject[set.size()]);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see Object#hashCode()
 	 */
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + buildOrderPosition;
 		result = prime * result + buildConfiguration.hashCode();
 		for (int i = 0; i < buildOrder.length; i++)
 			result = prime * result + buildOrder[i].hashCode();
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see Object#equals(Object)
 	 */
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		BuildContext context = (BuildContext) obj;
 		if (buildOrderPosition != context.buildOrderPosition)
 			return false;
 		if (!buildConfiguration.equals(context.buildConfiguration))
 			return false;
 		if (!Arrays.equals(buildOrder, context.buildOrder))
 			return false;
 		return true;
 	}
 }
