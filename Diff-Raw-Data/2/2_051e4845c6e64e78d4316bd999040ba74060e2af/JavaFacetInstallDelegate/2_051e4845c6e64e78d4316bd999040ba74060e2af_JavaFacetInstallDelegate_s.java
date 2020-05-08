 /******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.jst.common.project.facet;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.common.project.facet.core.ClasspathHelper;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IDelegate;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 /**
  * @author <a href="mailto:kosta@bea.com">Konstantin Komissarchik</a>
  */
 
 public final class JavaFacetInstallDelegate implements IDelegate {
 
 	public void execute(final IProject project, final IProjectFacetVersion fv, final Object cfg, final IProgressMonitor monitor) throws CoreException {
 		if (monitor != null) {
 			monitor.beginTask("", 1); //$NON-NLS-1$
 		}
 
 		try {
 			IDataModel model = (IDataModel) cfg;
 
 			// Create the source and the output directories.
 
 			final IWorkspace ws = ResourcesPlugin.getWorkspace();
 
 			final IPath pjpath = project.getFullPath();
 			IJavaProject jproject = null;
 			if( project.exists()){
 				 jproject = JavaCore.create(project);
 			}
 			
 			if( !jproject.exists()){
 				String srcFolderName = model.getStringProperty(IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME);
 				// final IPath srcdir = pjpath.append( "src" );
 				final IPath srcdir = pjpath.append(srcFolderName);
 	
 				final IPath outdir = pjpath.append("build/classes"); //$NON-NLS-1$
 	
 				ws.getRoot().getFolder(srcdir).getLocation().toFile().mkdirs();
 				ws.getRoot().getFolder(outdir).getLocation().toFile().mkdirs();
 				project.refreshLocal(IResource.DEPTH_INFINITE, null);
 	
 				// Add the java nature. This will automatically add the builder.
 	
 				final IProjectDescription desc = project.getDescription();
 				final String[] current = desc.getNatureIds();
 				final String[] replacement = new String[current.length + 1];
 				System.arraycopy(current, 0, replacement, 0, current.length);
 				replacement[current.length] = JavaCore.NATURE_ID;
 				desc.setNatureIds(replacement);
 				project.setDescription(desc, null);
 	
 				// Set up the sourcepath and the output directory.
 	
 				final IJavaProject jproj = JavaCore.create(project);
 				final IClasspathEntry[] cp = {JavaCore.newSourceEntry(srcdir)};
 	
 				jproj.setRawClasspath(cp, outdir, null);
 				jproj.save(null, true);
 	
 				// Setup the classpath.
 	
 				ClasspathHelper.removeClasspathEntries(project, fv);
 	
 				if (!ClasspathHelper.addClasspathEntries(project, fv)) {
 					// TODO: Support the no runtime case.
 					// ClasspathHelper.addClasspathEntries( project, fv, <something> );
 				}
 			}
 			// Set the compiler comliance level for the project. Ignore whether
 			// this might already be set so at the workspace level in case
 			// workspace settings change later or the project is included in a
 			// different workspace.
 			
			//JavaFacetUtils.setCompilerLevel(project, fv);
 
 			if (monitor != null) {
 				monitor.worked(1);
 			}
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 }
