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
  *******************************************************************************/
 package org.eclipse.core.internal.resources;
 
 import org.eclipse.core.runtime.CoreException;
 
 import org.eclipse.core.runtime.IPath;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectVariant;
 
 import java.net.URI;
 import java.util.*;
 import org.eclipse.core.filesystem.URIUtil;
 import org.eclipse.core.internal.events.BuildCommand;
 import org.eclipse.core.internal.utils.FileUtil;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 
 public class ProjectDescription extends ModelObject implements IProjectDescription {
 	private static final ICommand[] EMPTY_COMMAND_ARRAY = new ICommand[0];
 	// constants
 	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
 	private static final IProjectVariant[] EMPTY_PROJECT_VARIANT_ARRAY = new IProjectVariant[0];
 	private static final String[] EMPTY_STRING_ARRAY = new String[0];
 	private static final String EMPTY_STR = ""; //$NON-NLS-1$
 	protected static boolean isReading = false;
 
 	//flags to indicate when we are in the middle of reading or writing a
 	// workspace description
 	//these can be static because only one description can be read at once.
 	protected static boolean isWriting = false;
 	protected ICommand[] buildSpec = EMPTY_COMMAND_ARRAY;
 	/*
 	 * Cached union of static and dynamic project references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected IProjectVariant[] cachedRefs = null;
 	/*
 	 * Cached union of static and dynamic project variant references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected IProject[] cachedProjectRefs = null;
 	/*
 	 * Cached dynamic project references, generated from project variant references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected IProject[] cachedDynamicProjectRefs = EMPTY_PROJECT_ARRAY;
 	/*
 	 * Cached static project references, generated from project variant references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected IProject[] cachedStaticProjectRefs = EMPTY_PROJECT_ARRAY;
 
 	protected String comment = EMPTY_STR;
 	
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
 	protected String[] variants = EMPTY_STRING_ARRAY;
 	protected IProjectVariant[] staticRefs = EMPTY_PROJECT_VARIANT_ARRAY;
 	protected IProjectVariant[] dynamicRefs = EMPTY_PROJECT_VARIANT_ARRAY;
 	protected URI snapshotLocation= null;
 
 	public ProjectDescription() {
 		super();
 		//TODO: ALEX Create a default variant name
 	}
 
 	public Object clone() {
 		ProjectDescription clone = (ProjectDescription) super.clone();
 		//don't want the clone to have access to our internal link locations table or builders
 		clone.linkDescriptions = null;
 		clone.filterDescriptions = null;
 		if (variableDescriptions != null)
 			clone.variableDescriptions = (HashMap) variableDescriptions.clone();
 		clone.buildSpec = getBuildSpec(true);
 		return clone;
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
 	 * Returns true if any private attributes of the description have changed.
 	 * Private attributes are those that are not stored in the project description
 	 * file (.project).
 	 */
 	public boolean hasPrivateChanges(ProjectDescription description) {
 		if (!Arrays.equals(dynamicRefs, description.getDynamicVariantReferences(false)))
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
 		if (!Arrays.equals(staticRefs, description.getReferencedProjectVariants(false)))
 			return true;
 		if (!Arrays.equals(natures, description.getNatureIds(false)))
 			return true;
 		if (!Arrays.equals(variants, description.getVariants(false)))
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
 					((BuildCommand) result[i]).setBuilder(((BuildCommand) buildSpec[j]).getBuilder());
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
 
 	/**
 	 * Sets the map of link descriptions (String name -> LinkDescription).
 	 * Since this method is only used internally, it never creates a copy. May
 	 * pass null if this project does not have any linked resources
 	 */
 	public void setLinkDescriptions(HashMap linkDescriptions) {
 		this.linkDescriptions = linkDescriptions;
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
 
 	/**
 	 * Returns the union of the description's static and dynamic project variant references,
 	 * with duplicates omitted. The calculation is optimized by caching the result
 	 */
 	public IProjectVariant[] getAllVariantReferences(boolean makeCopy) {
 		if (cachedRefs == null) {
 			IProjectVariant[] statik = getReferencedProjectVariants(false);
 			IProjectVariant[] dynamic = getDynamicVariantReferences(false);
 			if (dynamic.length == 0) {
 				cachedRefs = statik;
 			} else if (statik.length == 0) {
 				cachedRefs = dynamic;
 			} else {
 				//combine all references
 				IProjectVariant[] result = new IProjectVariant[dynamic.length + statik.length];
 				System.arraycopy(statik, 0, result, 0, statik.length);
 				System.arraycopy(dynamic, 0, result, statik.length, dynamic.length);
 				cachedRefs = copyAndRemoveDuplicates(result);
 			}
 		}
 		//still need to copy the result to prevent tampering with the cache
 		return makeCopy ? (IProjectVariant[]) cachedRefs.clone() : cachedRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getReferencedVariants()
 	 */
 	public IProjectVariant[] getReferencedProjectVariants() {
 		return getReferencedProjectVariants(true);
 	}
 
 	public IProjectVariant[] getReferencedProjectVariants(boolean makeCopy) {
 		if (staticRefs == null)
 			return EMPTY_PROJECT_VARIANT_ARRAY;
 		return makeCopy ? (IProjectVariant[]) staticRefs.clone() : staticRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setReferencedVariants(IProjectVariant[])
 	 */
 	public void setReferencedProjectVariants(IProjectVariant[] value) {
 		Assert.isLegal(value != null);
 		staticRefs = copyAndRemoveDuplicates(value);
 		clearCachedReferences();
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getDynamicVariantReferences()
 	 */
 	public IProjectVariant[] getDynamicVariantReferences() {
 		return getDynamicVariantReferences(true);
 	}
 
 	public IProjectVariant[] getDynamicVariantReferences(boolean makeCopy) {
 		if (dynamicRefs == null)
 			return EMPTY_PROJECT_VARIANT_ARRAY;
 		return makeCopy ? (IProjectVariant[]) dynamicRefs.clone() : dynamicRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setDynamicVariantReferences()
 	 */
 	public void setDynamicVariantReferences(IProjectVariant[] value) {
 		Assert.isLegal(value != null);
 		dynamicRefs = copyAndRemoveDuplicates(value);
 		clearCachedReferences();
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getVariants()
 	 */
 	public String[] getVariants() {
 		return getVariants(true);
 	}
 
 	public String[] getVariants(boolean makeCopy) {
 		Assert.isNotNull(variants);
 		Assert.isTrue(variants.length > 0);
 		Assert.isTrue(variants[0] != null);
 		return makeCopy ? (String[]) variants.clone() : variants;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setVariants(String[])
 	 */
 	public void setVariants(String[] variants) {
 		Assert.isLegal(variants != null && variants.length > 0 && variants[0] != null);
 		variants = (String[]) variants.clone();
 	}
 
 	private void clearCachedReferences()
 	{
 		cachedRefs = null;
 		cachedProjectRefs = null;
 		cachedStaticProjectRefs = null;
 		cachedDynamicProjectRefs = null;
 	}
 
 	/**
 	 * Returns a copy of the given array of project variants with all duplicates removed
 	 */
 	private IProjectVariant[] copyAndRemoveDuplicates(IProjectVariant[] values) {
 		Set set = new LinkedHashSet();
 		set.addAll(Arrays.asList(values));
 		return (IProjectVariant[]) set.toArray(new IProjectVariant[set.size()]);
 	}
 
 	/**
 	 * Returns the union of the description's static and dynamic project references,
 	 * with duplicates omitted. The calculation is optimized by caching the result
 	 * @see #getAllVariantReferences(boolean)
 	 * @deprecated
 	 */
 	public IProject[] getAllReferences(boolean makeCopy) {
 		if (cachedProjectRefs == null) {
 			IProject[] statik = getReferencedProjects(false);
 			IProject[] dynamic = getDynamicReferences(false);
 			if (dynamic.length == 0) {
 				cachedProjectRefs = statik;
 			} else if (statik.length == 0) {
 				cachedProjectRefs = dynamic;
 			} else {
 				//combine all references
 				IProject[] result = new IProject[dynamic.length + statik.length];
 				System.arraycopy(statik, 0, result, 0, statik.length);
 				System.arraycopy(dynamic, 0, result, statik.length, dynamic.length);
 				cachedProjectRefs = copyProjectsAndRemoveDuplicates(result);
 			}
 		}
 		//still need to copy the result to prevent tampering with the cache
 		return makeCopy ? (IProject[]) cachedProjectRefs.clone() : cachedProjectRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getReferencedProjects()
 	 */
 	/**
 	 * @deprecated
 	 * @see #getReferencedProjectVariants()
 	 */
 	public IProject[] getReferencedProjects() {
 		return getReferencedProjects(true);
 	}
 
 	/**
 	 * @deprecated
 	 * @see #getReferencedProjectVariants(boolean)
 	 */
 	public IProject[] getReferencedProjects(boolean makeCopy) {
 		if (staticRefs == null)
 			return EMPTY_PROJECT_ARRAY;
 		// Generate project references from project variants references
 		if (cachedStaticProjectRefs == null) {
 			cachedStaticProjectRefs = getProjectsFromProjectVariants(staticRefs);
 		}
 		//still need to copy the result to prevent tampering with the cache
 		return makeCopy ? (IProject[]) cachedStaticProjectRefs.clone() : cachedStaticProjectRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setReferencedProjects(IProject[])
 	 */
 	/**
 	 * @see #setReferencedProjectVariants(IProjectVariant[])
 	 * @deprecated
 	 */
 	public void setReferencedProjects(IProject[] projects) {
 		Assert.isLegal(projects != null);
 		// Add all variants in each of the projects as a reference
 		setReferencedProjectVariants(getProjectVariantsFromProjects(projects));
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getDynamicReferences()
 	 */
 	/**
 	 * @deprecated
 	 * @see #getDynamicVariantReferences()
 	 */
 	public IProject[] getDynamicReferences() {
 		return getDynamicReferences(true);
 	}
 
 	/**
 	 * @deprecated
 	 * @see #getDynamicVariantReferences(boolean)
 	 */
 	public IProject[] getDynamicReferences(boolean makeCopy) {
 		if (dynamicRefs == null)
 			return EMPTY_PROJECT_ARRAY;
 		// Generate dynamic project references from dynamic project variants references
 		if (cachedDynamicProjectRefs == null) {
 			cachedDynamicProjectRefs = getProjectsFromProjectVariants(dynamicRefs);
 		}
 		return makeCopy ? (IProject[]) cachedDynamicProjectRefs.clone() : cachedDynamicProjectRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setDynamicReferences(IProject[])
 	 */
 	/**
 	 * @deprecated
 	 * @see #setDynamicVariantReferences(IProjectVariant[])
 	 */
 	public void setDynamicReferences(IProject[] projects) {
 		Assert.isLegal(projects != null);
 		setDynamicVariantReferences(getProjectVariantsFromProjects(projects));
 	}
 
 	/**
 	 * Returns a copy of the given array of projects with all duplicates removed
 	 * @deprecated
 	 */
 	private IProject[] copyProjectsAndRemoveDuplicates(IProject[] values) {
 		Set set = new LinkedHashSet();
 		set.addAll(Arrays.asList(values));
 		return (IProject[]) set.toArray(new IProject[set.size()]);
 	}
 
 	/**
 	 * Get a list of projects, without duplicates, from a list of project variants.
 	 * Order is preserved, and is according to the first occurence of a project in the
 	 * array of project variants.
 	 * @param projectVariants project variants to get the projects from
 	 * @return list of projects
 	 * @deprecated
 	 */
 	private IProject[] getProjectsFromProjectVariants(IProjectVariant[] projectVariants) {
 		Set projects = new LinkedHashSet();
 		for (int i = 0; i < dynamicRefs.length; i++) {
 			projects.add(dynamicRefs[i].getProject());
 		}
 		return (IProject[]) projects.toArray(new Project[projects.size()]);
 	}
 
 	/**
 	 * Get a list of project variants, without duplicates, from a list of projects.
 	 * All the variants for each of the projects is added to the resulting list.
 	 * Order is preserved - the variants appear for each project in the order
 	 * that the projects were specified.
 	 * @param projects projects to get the project variants from
 	 * @return list of project variants
 	 * @deprecated
 	 */
 	private IProjectVariant[] getProjectVariantsFromProjects(IProject[] projects) {
 		List projectVariants = new ArrayList();
 		for (int i = 0; i < projects.length; i++) {
 			IProjectDescription desc;
 			try {
 				desc = projects[i].getDescription();
 			} catch (CoreException e) {
 				continue;
 			}
 			if (desc == null)
 				continue;
 			String[] variantNames = desc.getVariants();
 			for (int j = 0; j < variantNames.length; j++) {
 				projectVariants.add(new ProjectVariant(projects[i], variantNames[j]));
 			}
 		}
 		return (IProjectVariant[]) projectVariants.toArray(new IProjectVariant[projectVariants.size()]);
 	}
 }
