 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.team.core.RepositoryProvider;
//import org.fedoraproject.eclipse.packager.cvs.FpCVSProjectBits;
//import org.fedoraproject.eclipse.packager.git.FpGitProjectBits;
 
 /**
  * Enriched IResource with type. Use adapters to get instance. See
  * IResourceAdapterFactory.
  * 
  * @author Red Hat Inc.
  *
  */
 public class FpProject {
 	
 	private IResource project; // the underlying project
 	private ProjectType myType; // type of this project
 	private static final String GIT_REPOSITORY = "org.eclipse.egit.core.GitProvider"; //$NON-NLS-1$
 	private static final String CVS_REPOSITORY = "org.eclipse.team.cvs.core.cvsnature"; //$NON-NLS-1$
 	private IFpProjectBits vcsProjectBits; // VCS specific bits
 	
 	
 	public static enum ProjectType { GIT, CVS, UNKNOWN } // available project types
 	
 	/**
 	 * Constructs a FpProject of appropriate type. Instance should be created
 	 * by using FpProject.doAdapt() on a project.
 	 * 
 	 * @param res
 	 */
 	private FpProject(IResource res, ProjectType type) {
 		this.project = res;
 		this.myType = type;
 		// According to project types do branch-parsing accordingly
//		if (type == ProjectType.CVS) {
//			vcsProjectBits = new FpCVSProjectBits(res);
//		} else if (type == ProjectType.GIT) {
//			vcsProjectBits = new FpGitProjectBits(res);
//		}
 	}
 	
 	/**
 	 * @return the project
 	 */
 	public IResource getProject() {
 		return project;
 	}
 
 	/**
 	 * Get project type of this FpProject instance
 	 * 
 	 * @return The type of this instance.
 	 */
 	public ProjectType getProjectType() {
 		return this.myType;
 	}
 	
 	/**
 	 * Try to adapt an IResource to FpProject. Return null if it fails.
 	 * 
 	 * @param res The resource to adapt
 	 * @return The adapted resource or null.
 	 */
 	public static FpProject doAdapt(IResource res) throws CoreException {
 		if (res instanceof IProject) {
 			IProject project = res.getProject();
 			// open project if not already open
 			if (!project.isOpen()) {
 				project.open(null);
 			}
 			RepositoryProvider provider = RepositoryProvider.getProvider(project);
 			if (provider != null) {
 				if (provider.getID().equals(GIT_REPOSITORY)) {
 					// Git project, adapt to FpProject with type GIT
 					return new FpProject(res, ProjectType.GIT);
 				}
 				if (provider.getID().equals(CVS_REPOSITORY)) {
 					// Git project, adapt to FpProject with type GIT
 					return new FpProject(res, ProjectType.CVS);
 				}
 			}
 		}
 		return null;
 	}
 }
