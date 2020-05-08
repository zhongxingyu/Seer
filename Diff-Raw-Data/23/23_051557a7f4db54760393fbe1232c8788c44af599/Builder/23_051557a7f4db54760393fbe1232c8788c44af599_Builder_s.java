 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright � 2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: Builder.java,v 1.36 2006-10-10 17:01:28 gasiunas Exp $
  */
 
 package org.caesarj.ui.builder;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.caesarj.ui.CaesarPlugin;
 import org.caesarj.ui.editor.CaesarJContentOutlinePage;
 import org.caesarj.ui.marker.AdviceMarker;
 import org.caesarj.ui.marker.AdviceMarkerGenerator;
 import org.caesarj.ui.preferences.CaesarJPreferences;
 import org.caesarj.ui.util.ProjectProperties;
 import org.caesarj.ui.views.CaesarHierarchyView;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Builder is responsible for building a caesar project. Steps done are: 1)
  * collect project properties 2) calling caesar compiler 3) displaying errors 4)
  * refreshing the outlineview
  * 
  * TODO [build] Incremental build? Is there support in caesarj compiler?
  * 
  * @author Ivica Aracic
  */
 
 public class Builder extends IncrementalProjectBuilder {
 
 	private static Logger log = Logger.getLogger(Builder.class);
 
 	private ProjectProperties projectProperties = null;
 
 	private IProject currentProject = null;
 	
 	private Collection errors = new ArrayList();
 
 
 	/**
 	 * @see IncrementalProjectBuilder#build(int, Map, IProgressMonitor) kind is
 	 *      one of: FULL_BUILD, INCREMENTAL_BUILD or AUTO_BUILD currently we do
 	 *      a full build in every case!
 	 */
 	protected IProject[] build(int kind, Map args, IProgressMonitor progressMonitor) {
 		try {
 		    JavaCore.getClasspathVariable(CaesarPlugin.CAESAR_HOME);
 		    
 		    // Get the current project
 			this.currentProject = getProject();
 
 			this.errors.clear();
 
 			// Get this project's properties
 			this.projectProperties = ProjectProperties.create(currentProject);
 
 			// Refresh the properties
 			this.projectProperties.refresh();
 			
 			log.debug("Building to '" + this.projectProperties.getOutputPath() + "'"); //$NON-NLS-1$//$NON-NLS-2$
 			
 			log.debug("kind: " + kind); //$NON-NLS-1$
 
 			log.debug("----\n" + this.projectProperties.toString() + "----\n"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			// Clean the destination directory always, since there is no incremental building
 			cleanOutputFolder(projectProperties.getProjectLocation() +
 					File.separator +
 					projectProperties.getOutputPath());
 			
 			// Create a caesar adapter to compile the project
 			CaesarAdapter caesarAdapter = new CaesarAdapter(
 					this.projectProperties.getProjectLocation(),
 					this.projectProperties.getWorked(),
 					CaesarJPreferences.isRunWeaver());
 			
 			long begin = System.currentTimeMillis();
 			
 			// Compile the Project
 			caesarAdapter.compile(this.projectProperties.getSourceFiles(),
 					this.projectProperties.getClassPath(),
 					this.projectProperties.getOutputPath().substring(1), this.errors,
 					progressMonitor);
 			
 			long end = System.currentTimeMillis();
 			
 			System.out.println("Project compiled in " + (end - begin) + " miliseconds");
 			
 			// Copy non-java resources
 			copyResources(projectProperties.getProjectLocation(), 
 					projectProperties.getSourcePaths(), 
 					projectProperties.getOutputPath());
 			
 			// Set the project's structure model
 			this.projectProperties.setAsmManager(caesarAdapter.getAsmManager());
 			
 			// Set the project's kjcEnvironment
 			this.projectProperties.setKjcEnvironment(caesarAdapter.getKjcEnvironment());
 			
 			// Remember how much it worked for the next time
 			this.projectProperties.setWorked(caesarAdapter.getWorked());
 		} 
 		catch (Throwable t) {
			t.printStackTrace();
 			errors.add("internal compiler error: " + t.toString());
 		}
 		
 		// update markers, show errors
 		deleteOldErrors();
 		showErrors();
 		
 		try {
 			if (this.projectProperties.getAsmManager() != null) {
 				new AdviceMarkerGenerator().generateMarkers(
 						this.currentProject,
 						this.projectProperties.getAsmManager().getHierarchy());
 			}
 		} 
 		catch (Throwable t) {
 			t.printStackTrace();
 		}
 		
 		try {
 			/* ensure that the generated class files are recognized */
 			currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
 			
 			// update has to be executed from Workbenchs Thread
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					// Tell the outline pages and hierarchy view that they must be updated
 					CaesarJContentOutlinePage.updateAll(projectProperties);
 					CaesarHierarchyView.updateAll(projectProperties);
 				}
 			});
 		} 
 		catch (Throwable t) {
 			t.printStackTrace();
 		}
 
 		IProject[] requiredResourceDeltasOnNextInvocation = null;
 		return requiredResourceDeltasOnNextInvocation;		
 	}
 	
 	private void deleteOldErrors() {
 		try {
 			/* delete unpositioned errors */
 			currentProject.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 			
 			/* delete positioned errors */
 			Collection sourceFiles = this.projectProperties.getSourceFiles();
 			for (Iterator it = sourceFiles.iterator(); it.hasNext();) {
 
 				String sourcePath = this.projectProperties.getProjectLocation()
 						+ it.next().toString();
 
 				IResource resource = ProjectProperties.findResource(sourcePath,
 						this.projectProperties.getProject());
 				
 				if (resource != null) {
 					resource.deleteMarkers(IMarker.PROBLEM, true,
 							IResource.DEPTH_INFINITE);
 					resource.deleteMarkers(AdviceMarker.ADVICEMARKER, true,
 							IResource.DEPTH_INFINITE);
 				}
 			}
 		} 
 		catch (Exception e) {
			e.printStackTrace();
 		}
 	}
 
 	// TODO [optimize] make it efficient
 	// TODO [feature] warnings are missing
 	private void showErrors() {
 		for (Iterator it = this.errors.iterator(); it.hasNext();) {
 			try {
 				Object err = it.next();
 				
 				/* errors can be represented by PositionedError, UnpositionedError or String */
 				if (err instanceof PositionedError) {
 					PositionedError error = (PositionedError)err;
 					TokenReference token = error.getTokenReference();
 	
 				    log.debug("file: " + token.getFile() + ", " + "line: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 							+ token.getLine() + ", " + "path: " //$NON-NLS-1$//$NON-NLS-2$
 							+ token.getPath());
 
 					IResource resource = ProjectProperties.findResource(token
 							.getPath().getAbsolutePath(), currentProject);
 					
 					IMarker marker = null;
 					if (resource != null) {
 						marker = resource.createMarker(IMarker.PROBLEM);
 						if (token.getLine() > 0) {
 							marker.setAttribute(IMarker.LINE_NUMBER, token.getLine());
 						}
 					}
 					else {
 						// for the cases, when error refers to a generated piece of code
 						marker = currentProject.createMarker(IMarker.PROBLEM);
 					}
 					marker.setAttribute(IMarker.MESSAGE, error
 							.getFormattedMessage().getMessage());
 					marker.setAttribute(IMarker.SEVERITY, new Integer(
 							IMarker.SEVERITY_ERROR));
 				}
 				else { /* create unpositioned error at the scope of the project */					
 					String msg;
 					if (err instanceof UnpositionedError) {
 						msg = ((UnpositionedError)err).getFormattedMessage().getMessage();
 					}
 					else {
 						msg = (String)err; // for internal errors
 					}
 					
 					IMarker marker = currentProject.createMarker(IMarker.PROBLEM);
 					marker.setAttribute(IMarker.MESSAGE, msg);
 					marker.setAttribute(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
 				}
 			} 
 			catch (Exception e) {
				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Gets an folder and removes all class files recursivelly
 	 * 
 	 * @param output The absolute path to a folder.
 	 */
 	private void cleanOutputFolder(String output) {
 		File[] files = new File[1];
 		files[0] = new File(output);
 		cleanFolder(files);
 	}
 	
 	/**
 	 * Removes all the files that terminate in .class
 	 * 
 	 * @param files
 	 * @throws CoreException
 	 */
 	private void cleanFolder(File[] files) {
 		
 		if (files != null) {
 			// Iterate the files
 			for (int i = 0; i < files.length; i++) {
 				File f = files[i];
 				// If the file is a directory, remove its contents recursively
 				if (f.isDirectory()) {
 					cleanFolder(f.listFiles());
 				}
 				// Remove the file if it is a class file
 				if (f.getName().endsWith(".class"))
 					f.delete();
 			}
 		}
 	}
 	
 	
 	private void copyResources(String base, String[] sources, String output) {
 		
 		if (sources != null) {
 			// Iterate the sources
 			for (String source : sources) {
 				copyResources(base, source, output);
 			}
 		}
 	}
 	
 	private void copyResources(String base, String source, String output) {
 		
 		File sourceDir = new File(base + File.separator + source);
 		File outputDir = new File(base + File.separator + output);
 		
 		if (sourceDir.isDirectory()) {
 			
 			for(File f : sourceDir.listFiles()) {
 
 				String name = f.getName();
 				
 				if (f.isDirectory()) {
 					// If it is a directory, copy its contents recursivelly
 					copyResources(
 							base, 
 							source + File.separator + name, 
 							output + File.separator + name);
 				} else {
 					// If it is not a directory, check if it is a resource
 					if (isResource(name)) {
 						File dest = new File(outputDir, File.separator + name);
 						if (outputDir.exists() || outputDir.mkdirs()) {
 							copy(f, dest);
 						}
 					}	
 				}
 			}
 		}
 	}
 	
 	protected boolean copy(File src, File dest) {
 		FileChannel source, destination;
 
 		try {
 			source = new FileInputStream(src).getChannel();
 			destination = new FileOutputStream(dest).getChannel();
 			source.transferTo(0, source.size(), destination);
 			source.close();
 			destination.close();
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	protected boolean isResource(String name) {
 		return 
 			! name.endsWith(".class") && 
 			! name.endsWith(".cj") && 
 			! name.endsWith(".java") &&
 			! name.endsWith("CVS");
 	}
 }
