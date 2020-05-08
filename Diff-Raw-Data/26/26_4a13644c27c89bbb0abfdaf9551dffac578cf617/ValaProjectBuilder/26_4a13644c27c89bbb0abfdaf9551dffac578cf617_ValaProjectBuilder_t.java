 /* ValaProjectBuilder.java
  *
  * Copyright (C) 2008  Johann Prieur <johann.prieur@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.builder;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 
 import valable.job.ValaBuildJob;
 
 public class ValaProjectBuilder extends IncrementalProjectBuilder {
 	
 	public static final String ID = "valable.builder.ValaProjectBuilder";
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
		IProject[] built = null;
 		if(kind == FULL_BUILD) {
 			fullBuild(monitor);
			built = new IProject[1];
			built[0] = getProject();
 		} else {
			// TODO : incremental or delta build
 		}
		
		if(built == null) built = new IProject[0];
		return built;
 	}
 
 	private void fullBuild(IProgressMonitor monitor) throws CoreException {
 		List<IFile> filesToCompile = new ArrayList<IFile>();
 		
 		monitor.beginTask("ValaProjectBuilding", IProgressMonitor.UNKNOWN);
 		IResource[] members = getProject().members();
 		
 		for(IResource resource : members) {
 			if(resource.getType() == IResource.FILE) {
				if(resource.getName().endsWith(".vala")){
					filesToCompile.add((IFile)resource);
				}
 			} else if(resource.getType() == IResource.FOLDER) {
 				// TODO : grab vala files from sub folders
 			}
 		}
 		
 		ValaBuildJob job = new ValaBuildJob(filesToCompile);
 		job.schedule();
 		
 		monitor.done();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		System.out.println("cleaning...");
 		super.clean(monitor);
 	}
 	
 }
