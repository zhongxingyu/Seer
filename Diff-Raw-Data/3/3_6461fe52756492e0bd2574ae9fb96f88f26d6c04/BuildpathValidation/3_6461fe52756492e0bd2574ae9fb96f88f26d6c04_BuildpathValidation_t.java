 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.ModelException;
 
 
 /*
  * Validates the raw buildpath format and the resolved buildpath of this project,
  * updating markers if necessary.
  */
 public class BuildpathValidation {
 	
 	private ScriptProject project;
 	
 	public BuildpathValidation(ScriptProject project) {
 		this.project = project;
 	}
 	
 	public void validate() {
 		ModelManager.PerProjectInfo perProjectInfo;		
 		try {
 			perProjectInfo = this.project.getPerProjectInfo();
 		} catch (ModelException e) {
 			// project doesn't exist
 			IProject resource = this.project.getProject();
 			if (resource.isAccessible()) {
 				this.project.flushBuildpathProblemMarkers(true/*flush cycle markers*/, true/*flush buildpath format markers*/);				
 					
 				// remove problems and tasks created  by the builder
 				if (DLTKCore.DEBUG) {
 					System.err.println("TODO: BuildpathValidation.java remove problems and tasks created  by the builder"); //$NON-NLS-1$
 				}
 				//ScriptBuilder.removeProblemsAndTasksFor(resource);
 			}
 			return;
 		}
 		
 		// use synchronized block to ensure consistency
 		IBuildpathEntry[] rawBuildpath;		
 		IModelStatus status;
 		synchronized (perProjectInfo) {
 			rawBuildpath = perProjectInfo.rawBuildpath;						
 			status = perProjectInfo.rawBuildpathStatus; // status has been set during POST_CHANGE
 		}
 		
 		// update buildpath format problems
 		this.project.flushBuildpathProblemMarkers(false/*cycle*/, true/*format*/);
 		if (status != null && !status.isOK())
 			this.project.createBuildpathProblemMarker(status);	
 		
 		// update resolved buildpath problems
 		this.project.flushBuildpathProblemMarkers(false/*cycle*/, false/*format*/);
 		
		if (rawBuildpath != ScriptProject.INVALID_BUILDPATH
				&& rawBuildpath != null) {
 		 	for (int i = 0; i < rawBuildpath.length; i++) {
 				status = BuildpathEntry.validateBuildpathEntry(this.project, rawBuildpath[i],  true /*recurse in container*/);
 				if (!status.isOK()) {
 					if (status.getCode() == IModelStatusConstants.INVALID_BUILDPATH && ((BuildpathEntry) rawBuildpath[i]).isOptional() )
 						continue; // ignore this entry
 					this.project.createBuildpathProblemMarker(status);	
 				}
 			 }
 			status = BuildpathEntry.validateBuildpath(this.project, rawBuildpath);
 			if (!status.isOK()) 
 				this.project.createBuildpathProblemMarker(status);
 		 }
 	}
 
 }
