 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.core.configuration;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 
 /**
  * Wrapper around the Project Nature management APIs.
  * 
  * @author xcoulon
  * 
  */
 public final class ProjectNatureUtils {
 
 	/** The JAX-RS Nature Id. */
 	public static final String JAXRS_NATURE_ID = "org.jboss.tools.ws.jaxrs.nature";
 
 	/**
 	 * Hidden constructor of the utility class.
 	 */
 	private ProjectNatureUtils() {
 
 	}
 
 	/**
 	 * Check if a nature identified by its ID is installed on a given project.
 	 * 
 	 * @param project
 	 *            the project to look into
 	 * @param natureId
 	 *            the nature ID to look up in the project's natures
 	 * @return true if the Nature is installed (ie, declared)
 	 * @throws CoreException
 	 *             in case of exception
 	 */
 	public static boolean isProjectNatureInstalled(final IProject project, final String natureId) throws CoreException {
 		String[] natures = project.getDescription().getNatureIds();
 		for (String nature : natures) {
 			if (nature.equals(natureId)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Adds the nature identified by its ID to the given project.
 	 * 
 	 * @param project
 	 *            the project
 	 * @param natureId
 	 *            the nature id
 	 * @return true if the nature was actually added to the project, false if it
 	 *         was already installed
 	 * @throws CoreException
 	 *             in case of underlying exception (the nature may not be
 	 *             installed)
 	 */
 	public static boolean installProjectNature(final IProject project, final String natureId) throws CoreException {
 		if (isProjectNatureInstalled(project, natureId)) {
 			return false;
 		}
 		IProjectDescription description = project.getDescription();
 		String[] natures = description.getNatureIds();
 		String[] newNatures = new String[natures.length + 1];
 		System.arraycopy(natures, 0, newNatures, 0, natures.length);
 		newNatures[natures.length] = natureId;
 		description.setNatureIds(newNatures);
 		project.setDescription(description, null);
 		return true;
 	}
 
 	/**
 	 * Remove the given nature identified by its ID on the given project.
 	 * 
 	 * @param project
 	 *            the project from which the nature should be removed
 	 * @param natureId
 	 *            the id of the nature to remove
 	 * @return true if the nature was removed
 	 * @throws CoreException
 	 *             in case of exception
 	 */
 	public static boolean uninstallProjectNature(final IProject project, final String natureId) throws CoreException {
 		IProjectDescription desc = project.getDescription();
 		String[] natures = desc.getNatureIds();
 
 		for (int i = 0; i < natures.length; i++) {
 			if (natures[i].equals(natureId)) {
 				// remove builder from project
 				String[] newNatureIds = new String[natures.length - 1];
 				System.arraycopy(natures, 0, newNatureIds, 0, i);
 				System.arraycopy(natures, i + 1, newNatureIds, i, natures.length - i - 1);
 				desc.setNatureIds(newNatureIds);
 				project.setDescription(desc, null);
 				return true;
 			}
 		}
 		return false;
 	}
 }
