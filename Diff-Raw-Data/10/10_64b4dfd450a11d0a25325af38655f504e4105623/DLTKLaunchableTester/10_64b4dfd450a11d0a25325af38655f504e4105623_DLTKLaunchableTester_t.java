 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.launching;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ModelException;
 
 
 /**
  * Generalized property tester class to determine enablement of context
  * launching menu artifacts
  * 
 	 *
  */
 public class DLTKLaunchableTester extends PropertyTester {
 	/**
 	 * "is container" property
 	 */
 	private static final String PROPERTY_IS_CONTAINER = "isContainer"; //$NON-NLS-1$
 	
 	/**
 	 * name for the PROPERTY_PROJECT_NATURE property
 	 */
 	private static final String PROPERTY_PROJECT_NATURE = "hasProjectNature"; //$NON-NLS-1$	
 	/**
 	 * name for the PROPERTY_HAS_SWT_ON_PATH property
 	 */
 	private static final String PROPERTY_BUILDPATH_REFERENCE = "buildpathReference"; //$NON-NLS-1$
 	public DLTKLaunchableTester() {
 		if( DLTKCore.VERBOSE ) {
 			System.out.println("DLTKLaunchableTester:initialized..."); //$NON-NLS-1$
 		}
 	}
 	/**
 	 * determines if the project selected has the specified nature
 	 * 
 	 * @param resource
 	 *            the resource to get the project for
 	 * @param ntype
 	 *            the specified nature type
 	 * @return true if the specified nature matches the project, false otherwise
 	 */
 	private boolean hasProjectNature(IModelElement element, String ntype) {
 		try {
 			if (element != null) {
 				IResource resource = element.getResource();
 				if (resource != null) {
 					IProject proj = resource.getProject();
 					return proj.isAccessible() && proj.hasNature(ntype);
 				}
 			}
 			return false;
 		} catch (CoreException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Determines if an item or list of items are found on the buildpath. Once
 	 * any one single items matches though, the method returns true, this method
 	 * is intended to be used in OR like situations, where we do not care if all
 	 * of the items are on the build path, only that one of them is.
 	 * 
 	 * @param element
 	 *            the element whose build path should be checked
 	 * @param args
 	 *            the value(s) to search for on the build path
 	 * @return true if any one of the args is found on the build path
 	 */
 	private boolean hasItemOnBuildPath(IModelElement element, Object[] args) {
 		if (element != null && args != null) {
 			IScriptProject project = element.getScriptProject();
 			Set searched = new HashSet();
 			searched.add(project);
 			return hasItemsOnBuildPath(project, searched, args);
 		}
 		return false;
 	}
 
 	private boolean hasItemsOnBuildPath(IScriptProject project, Set searched, Object[] args) {
 		try {
 			List projects = new ArrayList();
 			if (project != null && project.exists()) {
 				IBuildpathEntry[] entries = project.getResolvedBuildpath(true);
 				for (int i = 0; i < entries.length; i++) {
 					IBuildpathEntry entry = entries[i];
 					IPath path = entry.getPath();
 					String spath = path.toPortableString();
 					for (int j = 0; j < args.length; j++) {
 						if (spath.lastIndexOf((String) args[j]) != -1) {
 							return true;
 						}
 					}
 					if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 						String name = entry.getPath().lastSegment();
 						IProject dep = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 						IScriptProject scriptProject = DLTKCore.create(dep);
 						if (!searched.contains(scriptProject)) {
 							projects.add(scriptProject);
 						}
 					}
 				}
 			}
 			// search referenced projects
 			Iterator iterator = projects.iterator();
 			while (iterator.hasNext()) {
 				IScriptProject jp = (IScriptProject) iterator.next();
 				searched.add(jp);
 				if (hasItemsOnBuildPath(jp, searched, args)) {
 					return true;
 				}
 			}
 		} catch (ModelException e) {
 		}
 		return false;
 	}
 
 	/**
 	 * Method runs the tests defined from extension points for Run As... and
 	 * Debug As... menu items. Currently this test optimisitically considers
 	 * everything not a source file. In this context we consider an optimistic
 	 * approach to mean that the test will always return true.
 	 * 
 	 * There are many reasons for the optimistic choice some of them are
 	 * outlined below.
 	 * <ul>
 	 * <li>Performance (in terms of time neede to display menu) cannot be
 	 * preserved. To know what to allow in any one of the menus we would have to
 	 * search all of the children of the container to determine what it contains
 	 * and what can be launched by what.</li>
 	 * <li>If inspection of children of containers were done, a user might want
 	 * to choose a different launch type, even though our tests filter it out.</li>
 	 * </ul>
 	 * 
 	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
 	 *      java.lang.String, java.lang.Object[], java.lang.Object)
 	 *
 	 * @return true if the specified tests pass, or the context is a container,
 	 *         false otherwise
 	 */
 	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 		if (PROPERTY_IS_CONTAINER.equals(property)) {
 			if (receiver instanceof IAdaptable) {
 				IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
 				if (resource != null) {
 					return resource instanceof IContainer;
 				}
 			}
 			return false;
 		}
 		
 		IModelElement element = null;
 		if (receiver instanceof IAdaptable) {
 			element = (IModelElement) ((IAdaptable) receiver).getAdapter(IModelElement.class);
			if( element == null ) {
				IResource res = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
				if( res != null ) {
					element = DLTKCore.create(res);
				}
			}
 			if (element != null) {
 				if (!element.exists()) {
 					return false;
 				}
 			}
 		}
 		if (PROPERTY_BUILDPATH_REFERENCE.equals(property)) {
 			return hasItemOnBuildPath(element, args);
 		}
 		if (PROPERTY_PROJECT_NATURE.equals(property) && args.length > 0 && args[0]!=null) {
 			return hasProjectNature(element, (String) args[0]);
 		}
 		return false;
 	}
 }// end class
