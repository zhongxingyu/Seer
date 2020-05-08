/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 package org.eclipse.jem.internal.proxy.core;
 
 import java.lang.reflect.Array;
 import java.util.*;
 import java.util.regex.Pattern;
 
 
 /**
  * Mapping of container paths (Regular expressions) to configuration elements.
  * <p>
  * It can be instantiated by clients that need to built up a subset of the mapping from the 
  * normal mapping done by {@link ProxyPlugin#getPluginExtensions(String)}.
  * <p>
  * To build one up separately clients would:
  * <pre><code>
  * ContainerPathContributionMapping mapping = new ContainerPathContributionMapping(contributionType);
  * mapping.addContribution(containerid, pattern, contribution);
  * 	...
  * mapping.finalizeMapping();
  * </code></pre>
  * 
  * @since 1.2.0
  */
 public class ContainerPathContributionMapping {
 	
 	/**
 	 * Used in {@link ContainerPathContributionMapping#containerIdToContributions} as the value of the map. 
 	 * This contains a Pattern for a container path to match against to see if the contribution
 	 * should be used.
 	 * <p>
 	 * Normally it would be better to use {@link ContainerPathContributionMapping#getContributors(String, String[])}
 	 * to get all of the contributors for all of the paths of the same container id. 
 	 * 
 	 * @since 1.2.0
 	 */
 	public static class ContainerContributionEntry {
 		
 		private final Object contribution;
 		private final Pattern containerPathPattern;
 	
 		public ContainerContributionEntry(Object contribution, Pattern containerPathPattern) {
 			this.contribution = contribution;
 			this.containerPathPattern = containerPathPattern;
 		}
 		
 		
 		/**
 		 * @return Returns the contribution.
 		 * 
 		 * @since 1.2.0
 		 */
 		public final Object getContribution() {
 			return contribution;
 		}
 		
 		
 		/**
 		 * @return Returns the container pattern for matching.
 		 * 
 		 * @since 1.2.0
 		 */
 		public final Pattern getContainerPathPattern() {
 			return containerPathPattern;
 		}
 	}
 
 	
 	/**
 	 * Map of container ids (String, first segment of container path) to {@link ContainerContributionEntry[]}.
 	 * <p>
 	 * Normally it would be better to use {@link #getContributors(String, String[])}
 	 * to get all of the contributors for all of the paths of the same container id. 
 	 * 
 	 * @since 1.2.0
 	 */
 	public Map containerIdToContributions = Collections.EMPTY_MAP;
 	
 	// TODO When 1.5, this can go away.
 	private final Class contributionType;
 	
 	/**
 	 * Create with contribution type (i.e. the array type returned from {@link #getContributors(String, String[])}.
 	 * <p>
 	 * TODO This will go away with 1.5 because this will be templated.
 	 * 
 	 * @param type
 	 * 
 	 * 
 	 * @since 1.2.0
 	 */
 	public ContainerPathContributionMapping(Class type) {
 		contributionType = type;
 	}
 	
 	
 	/**
 	 * Get the unique array of configuration elements (no duplicates) for the given container id, and
 	 * the set of container paths for that container id. For example, "SWT_CONTAINER" as containerID and
 	 * {"/SWT_CONTAINER/", "/SWT_CONTAINER/PDE/JFACE"} for container paths. This will then return configuration elements
 	 * that match these two paths in the container attribute of the configuration element. The container attribute
 	 * is a regular expression. For example "SWT_CONTAINER" will match both "/SWT_CONTAINER/" and "/SWT_CONTAINER/PDE/JFACE"
 	 * while "SWT_CONTAINER/.* /JFACE" will match "/SWT_CONTAINER/PDE/JFACE". (Note it is actually no space between the "*" and "/" but java comment syntax won't allow it.)   
 	 * @param containerId id of all the containers in the list of paths (the first segment of all of the paths).
 	 * @param containerPaths array of all of the paths to look for contributions for.
 	 * @return array of configuration elements for the given list of paths. They will be in order declared within a plugin within plugin order.
 	 * 
 	 * @since 1.2.0
 	 */
 	public Object[] getContributors(String containerId, String[] containerPaths) {
 		ContainerContributionEntry[] bundleContributions = (ContainerContributionEntry[]) containerIdToContributions.get(containerId);
 		if (bundleContributions == null)
 			return (Object[]) Array.newInstance(contributionType, 0);
 		List contributions = new ArrayList();
 		// Patterns that have been tested. Key is a pattern, value is Boolean. true if this pattern matched any of the container paths.
 		// This way a pattern will only be tested once for the list of paths. If the pattern is found the list again we will know if it
 		// should be selected or not.
 		// The bundleContributions are in order declared within each plugin within plugin order.
 		Map testedPatterns = new HashMap();
 		for (int i = 0; i < bundleContributions.length; i++) {
 			Boolean tested = (Boolean) testedPatterns.get(bundleContributions[i].getContainerPathPattern());
 			if (tested == null) {
 				// Need to test it.
 				// Run through container paths and see if any match.
 				tested = Boolean.FALSE;
 				Pattern pattern = bundleContributions[i].getContainerPathPattern();
 				for (int j = 0; j < containerPaths.length; j++) {
 					if (pattern.matcher(containerPaths[j]).matches()) {
 						tested = Boolean.TRUE;
 						break;
 					}
 				}
 				testedPatterns.put(pattern, tested);
 			}
 			if (tested.booleanValue())
 				contributions.add(bundleContributions[i].getContribution());
 		}
 		return contributions.toArray((Object[]) Array.newInstance(contributionType, contributions.size()));
 	}
 
 	/**
 	 * Add contribution to mapping. This is not normally needed by clients unless the client needs to
 	 * build up a different container path mapping than the one normally created by {@link ProxyPlugin#processContributionExtensionPoint(String)}.
 	 * <p>
 	 * After all contributions have been added {@link #finalizeMapping()} must be called. If this
 	 * is not called then {@link #getContributors(String, String[])} will fail with exceptions.
 
 	 * @param containerId
 	 * @param pattern
 	 * @param contribution
 	 * 
 	 * @since 1.2.0
 	 */
 	public void addContribution(String containerId, Pattern pattern, Object contribution) {
 		if (containerIdToContributions == Collections.EMPTY_MAP)
 			containerIdToContributions = new HashMap();	// This is first call to add something.
 		// We will build as list, but then change to array when done.
 		Object contributions = containerIdToContributions.get(containerId);
 		if (contributions == null) {
 			contributions = new ArrayList(1);
 			containerIdToContributions.put(containerId, contributions);
 		} else if (!(contributions instanceof List)) {
 			// It must be an array, so convert back to list so that we can add to it.
 			List oldContributions = Arrays.asList((Object[]) contribution);
 			contributions = new ArrayList(oldContributions.size());
 			((List) contributions).addAll(oldContributions);
 			containerIdToContributions.put(containerId, contributions);
 		}
 		((List) contributions).add(new ContainerContributionEntry(contribution, pattern));
 
 	}
 	
 	/**
 	 * Finalize the mapping. This is called when clients are done with all {@link #addContribution(String)}.
 	 * It takes the mapping from an internal format that allowed for quicker building into the final format.
 	 * 
 	 * 
 	 * @since 1.2.0
 	 */
 	public void finalizeMapping() {
 		for (Iterator iter = containerIdToContributions.entrySet().iterator(); iter.hasNext();) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			if (entry.getValue() instanceof List)
 				entry.setValue(((List) entry.getValue()).toArray(new ContainerContributionEntry[((List) entry.getValue()).size()]));
 		}
 	}
 }
