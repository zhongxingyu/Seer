 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Feb 2, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.plugin;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ItemProvider;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.viewers.IStructuredSelection;
 
 /**
  * Utility class for retrieving the project from the specified selection
  */
 public class UIProjectUtilities {
 
 	/**
 	 * @param selection
 	 *            The current selection
 	 * @return The first project (regardless of nature) in the selection
 	 */
 	public static IProject getSelectedProject(IStructuredSelection selection) {
 		return getSelectedProject(selection, (String[]) null);
 	}
 
 	/**
 	 * @param selection
 	 *            The current selection
 	 * @param expectedNatureId
 	 *            The required Nature ID
 	 * @return The first project, only if the first project has the given nature
 	 */
 	public static IProject getSelectedProject(IStructuredSelection selection, String expectedNatureId) {
 		return getSelectedProject(selection, new String[] {expectedNatureId});
 	}
 
 	/**
 	 * 
 	 * @param selection
 	 *            The current selection
 	 * @param possibleNatureIds
 	 *            A list of possible nature ids
 	 * @return The first project selected, only if it has AT LEAST ONE of the possible nature ids
 	 */
 	public static IProject getSelectedProject(IStructuredSelection selection, String[] possibleNatureIds) {
 		IProject[] projects = getAllSelectedProjects(selection, possibleNatureIds);
 		if (projects == null || projects.length==0)
 			return null;
 		return projects[0];
 	}
 
 	/**
 	 * 
 	 * @param selection
 	 *            The current selection
 	 * @return All selected Projects, regardless of nature
 	 */
 	public static IProject[] getAllSelectedProjects(IStructuredSelection selection) {
 		return getAllSelectedProjects(selection, (String[]) null);
 	}
 
 	/**
 	 * 
 	 * @param selection
 	 *            The current selection
 	 * @param expectedNatureId
 	 *            The expected nature id
 	 * @return All selected Projects which have the expected nature id
 	 */
 	public static IProject[] getAllSelectedProjects(IStructuredSelection selection, String expectedNatureId) {
 		return getAllSelectedProjects(selection, new String[] {expectedNatureId});
 	}
 
 	/**
 	 * 
 	 * @param selection
 	 *            The current selection
 	 * @param possibleNatureIds
 	 *            a list of possible nature ids
 	 * @return All selected Projects which have AT LEAST ONE of the given nature ids
 	 */
 	public static IProject[] getAllSelectedProjects(IStructuredSelection selection, String[] possibleNatureIds) {
 		if (selection != null && !selection.isEmpty()) {
 			Object obj = null;
 			List projects = new ArrayList();
 			Iterator selectionIterator = selection.iterator();
 			while (selectionIterator.hasNext()) {
 				obj = selectionIterator.next();
 				IProject project = null;
 				if (obj instanceof IProject)
 					project = (IProject) obj;
				else if (obj instanceof IAdaptable)
 					project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
				
 				// Selection may not be adaptable to a project so continue trying to get selected project
 				if (project == null && obj instanceof EObject)
 					project = ProjectUtilities.getProject((EObject) obj);
 				else if (project == null && obj instanceof ItemProvider) {
 					Object temp = ((ItemProvider) obj).getParent(EObject.class);
 					if (temp != null && temp instanceof EObject)
 						project = ProjectUtilities.getProject((EObject) temp);
 				}
 				else if (project == null && obj instanceof IFile) 
 					project = ProjectUtilities.getProject(obj);
 
 				if (project != null && possibleNatureIds != null && possibleNatureIds.length > 0) {
 					try {
 						for (int i = 0; i < possibleNatureIds.length; i++)
 							if (project.hasNature(possibleNatureIds[i]))
 								projects.add(project);
 					} catch (CoreException e) {
 						//Ignore
 					}
				} else
 					projects.add(project);
 			}
 			IProject[] finalProjects = new IProject[projects.size()];
 			projects.toArray(finalProjects);
 			return finalProjects;
 		}
 		return new IProject[0];
 	}
 
 }
