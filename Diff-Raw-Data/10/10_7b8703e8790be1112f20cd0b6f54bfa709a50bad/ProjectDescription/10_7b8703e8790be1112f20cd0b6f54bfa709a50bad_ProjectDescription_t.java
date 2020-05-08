 /*******************************************************************************
  * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Martin Oberhuber (Wind River) - [245937] setLinkLocation() detects non-change
  *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
  * Markus Schorn (Wind River) - [306575] Save snapshot location with project
  * Broadcom Corporation - build configurations and references
  *******************************************************************************/
 package org.eclipse.core.internal.resources;
 
 import java.net.URI;
 import java.util.*;
 import java.util.Map.Entry;
 import org.eclipse.core.filesystem.URIUtil;
 import org.eclipse.core.internal.events.BuildCommand;
 import org.eclipse.core.internal.utils.FileUtil;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 
 public class ProjectDescription extends ModelObject implements IProjectDescription {
 	// constants
 	private static final BuildConfiguration[] EMPTY_BUILD_CONFIGS = new BuildConfiguration[0];
 	private static final IBuildConfiguration[] EMPTY_BUILD_CONFIG_REFERENCE_ARRAY = new IBuildConfiguration[0];
 	private static final ICommand[] EMPTY_COMMAND_ARRAY = new ICommand[0];
 	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
 	private static final String[] EMPTY_STRING_ARRAY = new String[0];
 	private static final String EMPTY_STR = ""; //$NON-NLS-1$
 
 	protected static boolean isReading = false;
 
 	//flags to indicate when we are in the middle of reading or writing a
 	// workspace description
 	//these can be static because only one description can be read at once.
 	protected static boolean isWriting = false;
 	protected ICommand[] buildSpec = EMPTY_COMMAND_ARRAY;
 	protected String comment = EMPTY_STR;
 
 	// Build configuration + References state
 	/** Id of the currently active build configuration */
 	protected String activeConfigurationId = IBuildConfiguration.DEFAULT_CONFIG_ID;
 	/** The 'real' build configurations set on this project. 
 	 *  This doesn't contain the generated 'default' build configuration added by IProject
 	 *  no build configurations have been defined. */
 	protected BuildConfiguration[] buildConfigs = EMPTY_BUILD_CONFIGS;
 	/** Map from config id in this project -> build configurations in other projects */
 	protected IProject[] staticRefs = EMPTY_PROJECT_ARRAY;
 	protected IProject[] dynamicRefs = EMPTY_PROJECT_ARRAY;
 	protected HashMap/*<String, IBuildConfiguration[]>*/ dynamicConfigRefs = new HashMap(1);
 
 	// Cached build configuration references. Not persisted.
 	protected HashMap/*<String, IBuildConfiguration[]>*/ cachedConfigRefs = new HashMap(1);
 	// Cached project level references.
 	protected IProject[] cachedRefs = null;
 
 	/**
 	 * Map of (IPath -> LinkDescription) pairs for each linked resource
 	 * in this project, where IPath is the project relative path of the resource.
 	 */
 	protected HashMap linkDescriptions = null;
 	
 	/**
 	 * Map of (IPath -> LinkedList<FilterDescription>) pairs for each filtered resource
 	 * in this project, where IPath is the project relative path of the resource.
 	 */
 	protected HashMap filterDescriptions = null;
 
 	/**
 	 * Map of (String -> VariableDescription) pairs for each variable in this
 	 * project, where String is the name of the variable.
 	 */
 	protected HashMap variableDescriptions = null;
 
 	// fields
 	protected URI location = null;
 	protected String[] natures = EMPTY_STRING_ARRAY;
 	protected URI snapshotLocation= null;
 
 	public ProjectDescription() {
 		super();
 	}
 
 	public Object clone() {
 		ProjectDescription clone = (ProjectDescription) super.clone();
 		//don't want the clone to have access to our internal link locations table or builders
 		clone.linkDescriptions = null;
 		clone.filterDescriptions = null;
 		if (variableDescriptions != null)
 			clone.variableDescriptions = (HashMap) variableDescriptions.clone();
 		clone.dynamicConfigRefs = (HashMap) dynamicConfigRefs.clone();
 		clone.clearCachedReferences(null);
 		clone.buildSpec = getBuildSpec(true);
 		return clone;
 	}
 
 	/**
 	 * Returns a copy of the given array of build configs with all duplicates removed
 	 */
 	private IBuildConfiguration[] copyAndRemoveDuplicates(IBuildConfiguration[] values) {
 		Set set = new LinkedHashSet();
 		set.addAll(Arrays.asList(values));
 		return (IBuildConfiguration[]) set.toArray(new IBuildConfiguration[set.size()]);
 	}
 
 	/**
 	 * Returns a copy of the given array with all duplicates removed
 	 */
     private IProject[] copyAndRemoveDuplicates(IProject[] projects) {
         IProject[] result = new IProject[projects.length];
         int count = 0;
         next: for (int i = 0; i < projects.length; i++) {
                 IProject project = projects[i];
                 // scan to see if there are any other projects by the same name
                 for (int j = 0; j < count; j++)
                         if (project.equals(result[j]))
                                 continue next;
                 // not found
                 result[count++] = project;
         }
         if (count < projects.length) {
                 //shrink array
                 IProject[] reduced = new IProject[count];
                 System.arraycopy(result, 0, reduced, 0, count);
                 return reduced;
         }
         return result;
     }
 
 	/**
 	 * Helper to turn an array of projects into an array of {@link IBuildConfiguration} to the
 	 * projects' active configuration
 	 * Order is preserved - the buildConfigs appear for each project in the order
 	 * that the projects were specified.
 	 * @param projects projects to get the active configuration from
 	 * @return collection of build config references
 	 */
 	private Collection getBuildConfigReferencesFromProjects(IProject[] projects) {
 		List refs = new ArrayList(projects.length);
 		for (int i = 0; i < projects.length; i++)
 			refs.add(new BuildConfiguration(projects[i], null));
 		return refs;
 	}
 
 	/**
 	 * Helper to fetch projects from an array of build configuration references
 	 * @param refs
 	 * @return List<IProject>
 	 */
 	private Collection getProjectsFromBuildConfigRefs(IBuildConfiguration[] refs) {
 		LinkedHashSet projects = new LinkedHashSet(refs.length);
 		for (int i = 0; i < refs.length; i++)
 			projects.add(refs[i].getProject());
 		return projects;
 	}
 
 	/**
 	 * Returns the union of the description's static and dynamic project references,
 	 * with duplicates omitted. The calculation is optimized by caching the result
 	 * Call the configuration based implementation.
 	 * @see #getAllBuildConfigReferences(String, boolean)
 	 */
 	public IProject[] getAllReferences(boolean makeCopy) {
 		if (cachedRefs == null) {
 			IBuildConfiguration[] refs;
 			if (hasBuildConfig(activeConfigurationId))
 				refs = getAllBuildConfigReferences(activeConfigurationId, false);
 			else if (buildConfigs.length > 0)
 				refs = getAllBuildConfigReferences(buildConfigs[0].getConfigurationId(), false);
 			else // No build configuration => fall-back to default
 				refs = getAllBuildConfigReferences(IBuildConfiguration.DEFAULT_CONFIG_ID, false);
 			Collection l = getProjectsFromBuildConfigRefs(refs);
 			cachedRefs = (IProject[])l.toArray(new IProject[l.size()]);
 		}
 		//still need to copy the result to prevent tampering with the cache
 		return makeCopy ? (IProject[]) cachedRefs.clone() : cachedRefs;
 	}
 
 	/**
 	 * Returns the union of the description's static and dynamic build config references,
 	 * for the config with the given name, with duplicates omitted. The calculation is
 	 * optimized by caching the result.
 	 * Note that these BuildConfiguration referenes may have <code>null</code> id.  They must
 	 * be resolved using {@link BuildConfiguration#getBuildConfiguration()} before use.
 	 * Returns an empty array if the given configId does not exist in the description.
 	 */
 	public IBuildConfiguration[] getAllBuildConfigReferences(String configId, boolean makeCopy) {
 		if (!hasBuildConfig(configId))
 			return EMPTY_BUILD_CONFIG_REFERENCE_ARRAY;
 		if (!cachedConfigRefs.containsKey(configId)) {
 			Set references = new LinkedHashSet();
 			Collection statik = getBuildConfigReferencesFromProjects(staticRefs);
 			IBuildConfiguration[] dynamicBuildConfigs = dynamicConfigRefs.containsKey(configId) ? 
 														(IBuildConfiguration[])dynamicConfigRefs.get(configId) : EMPTY_BUILD_CONFIG_REFERENCE_ARRAY;
 			Collection dynamic = getBuildConfigReferencesFromProjects(dynamicRefs);
 
 			// Combine all references:
 			// We preserve the previous order of static project references before dynamic project references
 			// However as build config references only come in dynamic form, those trump all others.
 			references.addAll(Arrays.asList(dynamicBuildConfigs));
 			references.addAll(statik);
 			references.addAll(dynamic);
 			cachedConfigRefs.put(configId, references.toArray(new IBuildConfiguration[references.size()]));
 		}
 		//still need to copy the result to prevent tampering with the cache
 		IBuildConfiguration[] result = (IBuildConfiguration[]) cachedConfigRefs.get(configId);
 		return makeCopy ? (IBuildConfiguration[]) result.clone() : result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getBuildSpec()
 	 */
 	public ICommand[] getBuildSpec() {
 		return getBuildSpec(true);
 	}
 
 	public ICommand[] getBuildSpec(boolean makeCopy) {
 		//thread safety: copy reference in case of concurrent write
 		ICommand[] oldCommands = this.buildSpec;
 		if (oldCommands == null)
 			return EMPTY_COMMAND_ARRAY;
 		if (!makeCopy)
 			return oldCommands;
 		ICommand[] result = new ICommand[oldCommands.length];
 		for (int i = 0; i < result.length; i++)
 			result[i] = (ICommand) ((BuildCommand) oldCommands[i]).clone();
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getComment()
 	 */
 	public String getComment() {
 		return comment;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getDynamicReferences()
 	 */
 	public IProject[] getDynamicReferences() {
 		return getDynamicReferences(true);
 	}
 
 	/*
 	 * Get project level dynamic references.  Contains a union of Configuration level
 	 * references and project level references.  Configuration level references take
 	 * precedence.
 	 */
 	public IProject[] getDynamicReferences(boolean makeCopy) {
 		// Need to create union of dynamicConfigRefs & dyamicRefs
 		// The former has priority.
 		Set projects = new LinkedHashSet();
 
 		// Add dynamic references from the active configuration first.
 		if (dynamicConfigRefs.containsKey(activeConfigurationId))
 			projects.addAll(getProjectsFromBuildConfigRefs((IBuildConfiguration[])dynamicConfigRefs.get(activeConfigurationId)));
 
 		// Add all the other build configuration references
 		int i = 0;
 		do {
 			String id;
 			if (buildConfigs.length == 0)
 				id = IBuildConfiguration.DEFAULT_CONFIG_ID;
 			else
 				id = buildConfigs[i].getConfigurationId();
 
 			if (!dynamicConfigRefs.containsKey(id))
 				continue;
 			projects.addAll(getProjectsFromBuildConfigRefs((IBuildConfiguration[])dynamicConfigRefs.get(id)));
 		} while (++i < buildConfigs.length);
 
 		// Finally add all dynamic project references
 		projects.addAll(Arrays.asList(dynamicRefs));
 
 		return (IProject[])projects.toArray(new IProject[projects.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getDynamicConfigReferences(String)
 	 */
 	public IBuildConfiguration[] getDynamicConfigReferences(String configId) {
 		return getDynamicConfigReferences(configId, true);
 	}
 
 	public IBuildConfiguration[] getDynamicConfigReferences(String configId, boolean makeCopy) {
 		if (!hasBuildConfig(configId))
 			return EMPTY_BUILD_CONFIG_REFERENCE_ARRAY;
 		if (!dynamicConfigRefs.containsKey(configId) && dynamicRefs.length == 0)
 			return EMPTY_BUILD_CONFIG_REFERENCE_ARRAY;
 
 		LinkedHashSet refs = new LinkedHashSet();
 		if (dynamicConfigRefs.containsKey(configId))
 			refs.addAll(Arrays.asList((IBuildConfiguration[])dynamicConfigRefs.get(configId)));
 		refs.addAll(getBuildConfigReferencesFromProjects(dynamicRefs));
 		return (IBuildConfiguration[])refs.toArray(new IBuildConfiguration[refs.size()]);
 	}
 
 	/**
 	 * Returns the link location for the given resource name. Returns null if
 	 * no such link exists.
 	 */
 	public URI getLinkLocationURI(IPath aPath) {
 		if (linkDescriptions == null)
 			return null;
 		LinkDescription desc = (LinkDescription) linkDescriptions.get(aPath);
 		return desc == null ? null : desc.getLocationURI();
 	}
 
 	/**
 	 * Returns the filter for the given resource name. Returns null if
 	 * no such filter exists.
 	 */
 	synchronized public LinkedList/*<FilterDescription>*/ getFilter(IPath aPath) {
 		if (filterDescriptions == null)
 			return null;
 		return (LinkedList /*<FilterDescription> */) filterDescriptions.get(aPath);
 	}
 
 	/**
 	 * Returns the map of link descriptions (IPath (project relative path) -> LinkDescription).
 	 * Since this method is only used internally, it never creates a copy.
 	 * Returns null if the project does not have any linked resources.
 	 */
 	public HashMap getLinks() {
 		return linkDescriptions;
 	}
 
 	/**
 	 * Returns the map of filter descriptions (IPath (project relative path) -> LinkedList<FilterDescription>).
 	 * Since this method is only used internally, it never creates a copy.
 	 * Returns null if the project does not have any filtered resources.
 	 */
 	public HashMap getFilters() {
 		return filterDescriptions;
 	}
 
 	/**
 	 * Returns the map of variable descriptions (String (variable name) ->
 	 * VariableDescription). Since this method is only used internally, it never
 	 * creates a copy. Returns null if the project does not have any variables.
 	 */
 	public HashMap getVariables() {
 		return variableDescriptions;
 	}
 
 	/**
 	 * @see IProjectDescription#getLocation()
 	 * @deprecated
 	 */
 	public IPath getLocation() {
 		if (location == null)
 			return null;
 		return FileUtil.toPath(location);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getLocationURI()
 	 */
 	public URI getLocationURI() {
 		return location;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getNatureIds()
 	 */
 	public String[] getNatureIds() {
 		return getNatureIds(true);
 	}
 
 	public String[] getNatureIds(boolean makeCopy) {
 		if (natures == null)
 			return EMPTY_STRING_ARRAY;
 		return makeCopy ? (String[]) natures.clone() : natures;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getReferencedProjects()
 	 */
 	public IProject[] getReferencedProjects() {
 		return getReferencedProjects(true);
 	}
 
 	public IProject[] getReferencedProjects(boolean makeCopy) {
 		if (staticRefs == null)
 			return EMPTY_PROJECT_ARRAY;
 		return makeCopy ? (IProject[]) staticRefs.clone() : staticRefs;
 	}
 
 	/** 
 	 * Returns the URI to load a resource snapshot from.
 	 * May return <code>null</code> if no snapshot is set.
 	 * <p>
 	 * <strong>EXPERIMENTAL</strong>. This constant has been added as
 	 * part of a work in progress. There is no guarantee that this API will
 	 * work or that it will remain the same. Please do not use this API without
 	 * consulting with the Platform Core team.
 	 * </p>
 	 * @return the snapshot location URI,
 	 *   or <code>null</code>.
 	 * @see IProject#loadSnapshot(int, URI, IProgressMonitor)
 	 * @see #setSnapshotLocationURI(URI)
 	 * @since 3.6
 	 */
 	public URI getSnapshotLocationURI() {
 		return snapshotLocation;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#hasNature(String)
 	 */
 	public boolean hasNature(String natureID) {
 		String[] natureIDs = getNatureIds(false);
 		for (int i = 0; i < natureIDs.length; ++i)
 			if (natureIDs[i].equals(natureID))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Helper method to compare two maps of Configuration ID -> IBuildConfigurationReference[]
 	 * @return boolean indicating if there are differences between the two maps
 	 */
 	static boolean configRefsHaveChanges(Map m1, Map m2) {
 		if (m1.size() != m2.size())
 			return true;
 		for (Iterator it = m1.entrySet().iterator(); it.hasNext();) {
 			Entry e = (Entry)it.next();
 			if (!m2.containsKey(e.getKey()))
 				return true;
 			if (!Arrays.equals((IBuildConfiguration[])e.getValue(), 
 					(IBuildConfiguration[])m2.get(e.getKey())))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Updates the references based on the passed in project description.
 	 * @param description to get references from
 	 * @return boolean indicating any change
 	 */
 	public boolean updateReferences(ProjectDescription description) {
 		boolean changed = false;
 		if (!Arrays.equals(staticRefs, description.staticRefs)) {
 			changed = true;
 			setReferencedProjects(description.staticRefs);
 		}
 		if (!Arrays.equals(dynamicRefs, description.dynamicRefs)) {
 			changed = true;
 			setDynamicReferences(description.dynamicRefs);
 		}
 		if (configRefsHaveChanges(dynamicConfigRefs, description.dynamicConfigRefs)) {
 			changed = true;
 			dynamicConfigRefs = new HashMap(description.dynamicConfigRefs);
 			clearCachedReferences(null);
 		}
 		return changed;
 	}
 
 	/**
 	 * Returns true if any private attributes of the description have changed.
 	 * Private attributes are those that are not stored in the project description
 	 * file (.project).
 	 */
 	public boolean hasPrivateChanges(ProjectDescription description) {
 		if (!Arrays.equals(dynamicRefs, description.getDynamicReferences(false)))
 			return true;
 		if (configRefsHaveChanges(dynamicConfigRefs, description.dynamicConfigRefs))
 			return true;
 		if (!activeConfigurationId.equals(description.activeConfigurationId))
 			return true;
 		IPath otherLocation = description.getLocation();
 		if (location == null)
 			return otherLocation != null;
 		return !location.equals(otherLocation);
 	}
 
 	/**
 	 * Returns true if any public attributes of the description have changed.
 	 * Public attributes are those that are stored in the project description
 	 * file (.project).
 	 */
 	public boolean hasPublicChanges(ProjectDescription description) {
 		if (!getName().equals(description.getName()))
 			return true;
 		if (!comment.equals(description.getComment()))
 			return true;
 		//don't bother optimizing if the order has changed
 		if (!Arrays.equals(buildSpec, description.getBuildSpec(false)))
 			return true;
 		if (!Arrays.equals(staticRefs, description.getReferencedProjects(false)))
 			return true;
 		if (!Arrays.equals(natures, description.getNatureIds(false)))
 			return true;
 		if (!Arrays.equals(buildConfigs, description.buildConfigs))
 			return true;
 
 		HashMap otherFilters = description.getFilters();
 		if ((filterDescriptions == null) && (otherFilters != null))
 			return otherFilters != null;
 		if ((filterDescriptions != null) && !filterDescriptions.equals(otherFilters))
 			return true;
 
 		HashMap otherVariables = description.getVariables();
 		if ((variableDescriptions == null) && (otherVariables != null))
 			return true;
 		if ((variableDescriptions != null) && !variableDescriptions.equals(otherVariables))
 			return true;
 
 		final HashMap otherLinks = description.getLinks();
 		if (linkDescriptions != otherLinks) { 
 			if (linkDescriptions == null || !linkDescriptions.equals(otherLinks))
 				return true;
 		}
 		
 		final URI otherSnapshotLoc= description.getSnapshotLocationURI();
 		if (snapshotLocation != otherSnapshotLoc) {
 			if (snapshotLocation == null || !snapshotLocation.equals(otherSnapshotLoc))
 				return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#newCommand()
 	 */
 	public ICommand newCommand() {
 		return new BuildCommand();
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setBuildSpec(ICommand[])
 	 */
 	public void setBuildSpec(ICommand[] value) {
 		Assert.isLegal(value != null);
 		//perform a deep copy in case clients perform further changes to the command
 		ICommand[] result = new ICommand[value.length];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = (ICommand) ((BuildCommand) value[i]).clone();
 			//copy the reference to any builder instance from the old build spec
 			//to preserve builder states if possible.
 			for (int j = 0; j < buildSpec.length; j++) {
 				if (result[i].equals(buildSpec[j])) {
 					((BuildCommand) result[i]).setBuilders(((BuildCommand) buildSpec[j]).getBuilders());
 					break;
 				}
 			}
 		}
 		buildSpec = result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setComment(String)
 	 */
 	public void setComment(String value) {
 		comment = value;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setDynamicReferences(IProject[])
 	 */
 	public void setDynamicReferences(IProject[] value) {
 		Assert.isLegal(value != null);
 		dynamicRefs = copyAndRemoveDuplicates(value);
 		clearCachedReferences(null);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setDynamicConfigReferences(String, IBuildConfiguration[])
 	 */
 	public void setDynamicConfigReferences(String configId, IBuildConfiguration[] references) {
 		Assert.isLegal(configId != null);
 		Assert.isLegal(references != null);
 		if (!hasBuildConfig(configId))
 			return;
 		dynamicConfigRefs.put(configId, copyAndRemoveDuplicates(references));
 		clearCachedReferences(configId);
 	}
 
 	/**
 	 * Sets the map of link descriptions (String name -> LinkDescription).
 	 * Since this method is only used internally, it never creates a copy. May
 	 * pass null if this project does not have any linked resources
 	 */
 	public void setLinkDescriptions(HashMap linkDescriptions) {
 		this.linkDescriptions = linkDescriptions;
 	}
 
 	/**
 	 * Sets the map of filter descriptions (String name -> LinkedList<LinkDescription>).
 	 * Since this method is only used internally, it never creates a copy. May
 	 * pass null if this project does not have any filtered resources
 	 */
 	public void setFilterDescriptions(HashMap filterDescriptions) {
 		this.filterDescriptions = filterDescriptions;
 	}
 
 	/**
 	 * Sets the map of variable descriptions (String name ->
 	 * VariableDescription). Since this method is only used internally, it never
 	 * creates a copy. May pass null if this project does not have any variables
 	 */
 	public void setVariableDescriptions(HashMap variableDescriptions) {
 		this.variableDescriptions = variableDescriptions;
 	}
 
 	/**
 	 * Sets the description of a link. Setting to a description of null will
 	 * remove the link from the project description.
 	 * @return <code>true</code> if the description was actually changed,
 	 *     <code>false</code> otherwise.
 	 * @since 3.5 returns boolean (was void before)
 	 */
 	public boolean setLinkLocation(IPath path, LinkDescription description) {
 		HashMap tempMap = linkDescriptions;
 		if (description != null) {
 			//addition or modification
 			if (tempMap == null)
 				tempMap = new HashMap(10);
 			else 
 				//copy on write to protect against concurrent read
 				tempMap = (HashMap) tempMap.clone();
 			Object oldValue = tempMap.put(path, description);
 			if (oldValue!=null && description.equals(oldValue)) {
 				//not actually changed anything
 				return false;
 			}
 			linkDescriptions = tempMap;
 		} else {
 			//removal
 			if (tempMap == null)
 				return false;
 			//copy on write to protect against concurrent access
 			HashMap newMap = (HashMap) tempMap.clone();
 			Object oldValue = newMap.remove(path);
 			if (oldValue == null) {
 				//not actually changed anything
 				return false;
 			}
 			linkDescriptions = newMap.size() == 0 ? null : newMap;
 		}
 		return true;
 	}
 
 	/**
 	 * Add the description of a filter. Setting to a description of null will
 	 * remove the filter from the project description.
 	 */
 	synchronized public void addFilter(IPath path, FilterDescription description) {
 		Assert.isNotNull(description);
 		if (filterDescriptions == null)
 			filterDescriptions = new HashMap(10);
 		LinkedList/*<FilterDescription>*/ descList = (LinkedList /*<FilterDescription> */) filterDescriptions.get(path);
 		if (descList == null) {
 			descList = new LinkedList/*<FilterDescription>*/();
 			filterDescriptions.put(path, descList);
 		}
 		descList.add(description);
 	}
 	
 	/**
 	 * Add the description of a filter. Setting to a description of null will
 	 * remove the filter from the project description.
 	 */
 	synchronized public void removeFilter(IPath path, FilterDescription description) {
 		if (filterDescriptions != null) {
 			LinkedList/*<FilterDescription>*/ descList = (LinkedList /*<FilterDescription> */) filterDescriptions.get(path);
 			if (descList != null) {
 				descList.remove(description);
 				if (descList.size() == 0) {
 					filterDescriptions.remove(path);
 					if (filterDescriptions.size() == 0)
 						filterDescriptions = null;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the description of a variable. Setting to a description of null will
 	 * remove the variable from the project description.
 	 * @return <code>true</code> if the description was actually changed,
 	 *     <code>false</code> otherwise.
 	 * @since 3.5
 	 */
 	public boolean setVariableDescription(String name,
 			VariableDescription description) {
 		HashMap tempMap = variableDescriptions;
 		if (description != null) {
 			// addition or modification
 			if (tempMap == null)
 				tempMap = new HashMap(10);
 			else
 				// copy on write to protect against concurrent read
 				tempMap = (HashMap) tempMap.clone();
 			Object oldValue = tempMap.put(name, description);
 			if (oldValue!=null && description.equals(oldValue)) {
 				//not actually changed anything
 				return false;
 			}
 			variableDescriptions = tempMap;
 		} else {
 			// removal
 			if (tempMap == null)
 				return false;
 			// copy on write to protect against concurrent access
 			HashMap newMap = (HashMap) tempMap.clone();
 			Object oldValue = newMap.remove(name);
 			if (oldValue == null) {
 				//not actually changed anything
 				return false;
 			}
 			variableDescriptions = newMap.size() == 0 ? null : newMap;
 		}
 		return true;
 	}
 
 	/**
 	 * set the filters for a given resource. Setting to a description of null will
 	 * remove the filter from the project description.
 	 * @return <code>true</code> if the description was actually changed,
 	 *     <code>false</code> otherwise.
 	 */
 	synchronized public boolean setFilters(IPath path, LinkedList/*<FilterDescription>*/ descriptions) {
 		if (descriptions != null) {
 			// addition
 			if (filterDescriptions == null)
 				filterDescriptions = new HashMap(10);
 			Object oldValue = filterDescriptions.put(path, descriptions);
 			if (oldValue!=null && descriptions.equals(oldValue)) {
 				//not actually changed anything
 				return false;
 			}
 		}
 		else { 
 			// removal
 			if (filterDescriptions == null)
 				return false;
 			
 			Object oldValue = filterDescriptions.remove(path);
 			if (oldValue == null) {
 				//not actually changed anything
 				return false;
 			}
 			if (filterDescriptions.size() == 0)
 				filterDescriptions = null;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setLocation(IPath)
 	 */
 	public void setLocation(IPath path) {
 		this.location = path == null ? null : URIUtil.toURI(path);
 	}
 
 	public void setLocationURI(URI location) {
 		this.location = location;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setName(String)
 	 */
 	public void setName(String value) {
 		super.setName(value);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setNatureIds(String[])
 	 */
 	public void setNatureIds(String[] value) {
 		natures = (String[]) value.clone();
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setReferencedProjects(IProject[])
 	 */
 	public void setReferencedProjects(IProject[] value) {
 		Assert.isLegal(value != null);
 		staticRefs = copyAndRemoveDuplicates(value);
 		clearCachedReferences(null);
 	}
 
 	/**
 	 * Sets the location URI for a project snapshot that may be
 	 * loaded automatically when the project is created in a workspace.
 	 * <p>
 	 * <strong>EXPERIMENTAL</strong>. This method has been added as
 	 * part of a work in progress. There is no guarantee that this API will
 	 * work or that it will remain the same. Please do not use this API without
 	 * consulting with the Platform Core team.
 	 * </p>
 	 * @param snapshotLocation the location URI or
 	 *    <code>null</code> to clear the setting 
 	 * @see IProject#loadSnapshot(int, URI, IProgressMonitor)
 	 * @see #getSnapshotLocationURI()
 	 * @since 3.6 
 	 */
 	public void setSnapshotLocationURI(URI snapshotLocation) {
 		this.snapshotLocation = snapshotLocation;
 	}
 
 	public URI getGroupLocationURI(IPath projectRelativePath) {
 		return LinkDescription.VIRTUAL_LOCATION;
 	}
 
 	public String getActiveBuildConfigurationId() {
 		return activeConfigurationId;
 	}
 
 	/**
 	 * Update the build configurations to point at the passed in project
 	 * @param project that owns the build configurations
 	 */
 	void updateBuildConfigurations(IProject project) {
 		for (int i = 0; i < buildConfigs.length; i++)
 			if (!project.equals(buildConfigs[i].getProject()))
 				buildConfigs[i] = new BuildConfiguration(buildConfigs[i], project);
 	}
 
 	public void internalSetDynamicBuildConfigReferences(HashMap refs) {
 		dynamicConfigRefs = refs;
 		clearCachedReferences(null);
 	}
 
 	public IProject[] internalGetDynamicReferences() {
 		return dynamicRefs;
 	}
 
 	public HashMap internalGetDynamicBuildConfigReferences() {
 		return dynamicConfigRefs;
 	}
 
 	public void setActiveBuildConfiguration(String configurationId) {
 		Assert.isNotNull(configurationId);
 		if (!configurationId.equals(activeConfigurationId))
 			clearCachedReferences(null);
 		activeConfigurationId = configurationId;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setBuildConfigurations(IBuildConfiguration[])
 	 */
 	public void setBuildConfigurations(IBuildConfiguration[] value) {
		// Remove references for deleted buildConfigs
		Set buildConfigIds = new HashSet(Arrays.asList(buildConfigs));

 		if (value == null || value.length == 0)
 			buildConfigs = EMPTY_BUILD_CONFIGS;
 		else {
 
 			// Filter out duplicates
 			Set filtered = new LinkedHashSet(value.length);
 			for (int i = 0; i < value.length; i++) {
 				BuildConfiguration config = (BuildConfiguration)((BuildConfiguration) value[i]).clone();
 				Assert.isLegal(config.getConfigurationId() != null);
 				filtered.add(config);
				buildConfigIds.add(config.getConfigurationId());
 			}
 
			if (filtered.isEmpty() ||
 					(filtered.size() ==1 && ((BuildConfiguration)(filtered.iterator().next())).isDefault()))
 				buildConfigs = EMPTY_BUILD_CONFIGS;
 			else {
 				buildConfigs = new BuildConfiguration[filtered.size()];
 				filtered.toArray(buildConfigs);
 			}
 		}
 
 		// Remove references for deleted buildConfigs
 		if (buildConfigIds.isEmpty())
 			buildConfigIds.add(IBuildConfiguration.DEFAULT_CONFIG_ID);
 		boolean modified = false;
 		modified |= dynamicConfigRefs.keySet().retainAll(buildConfigIds);
 		if (modified)
 			clearCachedReferences(null);
 	}
 
 	/**
 	 * Used by Project to get the buildConfigs on the description.
 	 * @return the project configurations of an empty array if none exist.
 	 */
 	public IBuildConfiguration[] internalGetBuildConfigs(boolean makeCopy) {
 		if (buildConfigs.length == 0)
 			return EMPTY_BUILD_CONFIGS;
 		return makeCopy ? copyBuildConfigs(buildConfigs) : buildConfigs;
 	}
     // Deep clone() the build configurations array
 	private IBuildConfiguration[] copyBuildConfigs(BuildConfiguration[] pvars) {
 		IBuildConfiguration[] result = new BuildConfiguration[buildConfigs.length];
 		for (int i = 0; i < buildConfigs.length; i++)
 			result[i] = (BuildConfiguration) buildConfigs[i].clone();
 		return result;
 	}
 
 	/**
 	 * Internal method to check if the description has a given build configuration.
 	 */
 	private boolean hasBuildConfig(String buildConfigId) {
 		Assert.isNotNull(buildConfigId);
 		if (buildConfigs.length == 0)
 			return IBuildConfiguration.DEFAULT_CONFIG_ID.equals(buildConfigId);
 		for (int i = 0; i < buildConfigs.length; i++)
 			if (buildConfigs[i].getConfigurationId().equals(buildConfigId))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Clear cached references for the specified build config Id
 	 * or all if configId is null.
 	 */
 	void clearCachedReferences(String configId)	{
 		if (configId == null)
 			cachedConfigRefs.clear();
 		else
 			cachedConfigRefs.remove(configId);
 		cachedRefs = null;
 	}
 }
