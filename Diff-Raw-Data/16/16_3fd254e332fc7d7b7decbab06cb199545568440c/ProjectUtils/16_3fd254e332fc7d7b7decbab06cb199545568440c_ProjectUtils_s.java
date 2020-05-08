 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.ui.utils;
 
 import java.io.File;
 import java.util.List;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 
 import uk.ac.gda.common.rcp.CommonRCPActivator;
 
 public class ProjectUtils {
 	/**
 	 * 
 	 * @param projectName
 	 * @param folderName
 	 * @param importFolder
 	 * @param natureId
 	 * @param resourceFilterWrappers
 	 * @param monitor
 	 * @return IProject created
 	 * @throws CoreException
 	 */
 	public static IProject createImportProjectAndFolder(final String projectName, final String folderName,
 			final String importFolder, final String natureId, final List<ResourceFilterWrapper> resourceFilterWrappers,
 			IProgressMonitor monitor) throws CoreException {
 
 		File file = new File(importFolder);
		if(!file.exists())
			throw new CoreException(new Status(IStatus.ERROR, CommonRCPActivator.PLUGIN_ID, 
					"Unable to create project folder " + projectName + "." + folderName + " as folder " + importFolder + " does not exist "));
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 			@Override
 			public void run(IProgressMonitor monitor) throws CoreException {
 
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IWorkspaceRoot root = workspace.getRoot();
 
 				IProject project = root.getProject(projectName);
 				if (!project.exists()) {
 					monitor.subTask("Creating project :" + projectName);
 					project.create(monitor);
 					if (natureId != null) {
 						project.open(monitor);
 						IProjectDescription description = project.getDescription();
 						description.setNatureIds(new String[] { natureId });
 						project.setDescription(description, monitor);
 					}
 				}
 
 				project.open(monitor);
 				if (project.findMember(folderName) == null) {
 					final IFolder src = project.getFolder(folderName);
					src.createLink(new Path(importFolder), IResource.BACKGROUND_REFRESH,
 							monitor);
 
 					if (resourceFilterWrappers != null) {
 						for (ResourceFilterWrapper wrapper : resourceFilterWrappers) {
 							src.createFilter(wrapper.type, wrapper.fileInfoMatcherDescription,
 									IResource.BACKGROUND_REFRESH, monitor);
 						}
 					}
 				}
 			}
 		};
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IWorkspaceRoot root = workspace.getRoot();
 		workspace.run(runnable, workspace.getRuleFactory().modifyRule(root), IResource.NONE, monitor);
 		return root.getProject(projectName);
 	}
 
 	public static void addRemoveNature(IProject project, IProgressMonitor monitor, boolean add, String natureId) throws CoreException{
 		IProjectDescription description = project.getDescription();
 		boolean hasNature = project.hasNature(natureId);
 		String [] newNatures=null;
 		if( add ){
 			if( !hasNature){
 				String[] natures = description.getNatureIds();
 				newNatures = new String[natures.length + 1];
 				System.arraycopy(natures, 0, newNatures, 0, natures.length);
 				newNatures[natures.length] = natureId;
 			}
 		} else {
 			if( hasNature){
 				String[] natures = description.getNatureIds();
 				Vector<String> v_newNatures= new  Vector<String>();
 				for(int i=0; i< natures.length; i++){
 					if( !natures[i].equals(natureId))
 						v_newNatures.add(natures[i]);
 				}
 				newNatures = v_newNatures.toArray(new String[0]);
 			}
 		}
 		if( newNatures != null){
 			description.setNatureIds(newNatures);
 			project.setDescription(description, monitor);
 		}
 	}
 	
 }
