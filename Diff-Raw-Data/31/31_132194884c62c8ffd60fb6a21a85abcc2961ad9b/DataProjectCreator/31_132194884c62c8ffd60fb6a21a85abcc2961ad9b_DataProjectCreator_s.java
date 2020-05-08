 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.ui.project;
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class DataProjectCreator implements IStartup{
 
 	private static final Logger logger = LoggerFactory.getLogger(DataProjectCreator.class);
 	
 	@Override
 	public void earlyStartup() {
 		
 		final IWorkspaceRoot root         = ResourcesPlugin.getWorkspace().getRoot();
 		final boolean        moreThanOne = root.getProjects()!=null&&root.getProjects().length>1;		
 		
 		if (moreThanOne) return; // They probably are used to projects
 		
 		for (IProject project : root.getProjects()) {
 			if (project.getName().equals("data")) return;
 		}
 		
 		final Job newData = new Job("Create Data Project") {
 			
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
				try {
 					DataProjectUtils.createDataProject("data", root, true, monitor);
 					
 					IWorkspace ws = ResourcesPlugin.getWorkspace();
 					ws.save(false, monitor);
 
 				} catch (Exception e) {
 					logger.error("Cannot create data project!", e);
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		
 		newData.setUser(false);
 		newData.setSystem(true);
 		newData.schedule();
 
 	}
 
 }
