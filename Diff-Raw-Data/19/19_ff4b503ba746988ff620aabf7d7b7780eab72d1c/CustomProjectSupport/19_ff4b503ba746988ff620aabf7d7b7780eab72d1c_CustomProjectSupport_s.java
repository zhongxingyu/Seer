 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.customprojects.rcp.wizards;
 
 import java.net.URI;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.customprojects.rcp.natures.SingleLevelProjectNature;
 
 
 public class CustomProjectSupport {
 	
 	private static final Logger logger = LoggerFactory.getLogger(CustomProjectSupport.class);
 
 	
     public static IProject createProject(String projectName, URI location) {
         Assert.isNotNull(projectName);
         Assert.isTrue(projectName.trim().length() > 0 );
         
         IProject project = createBaseProject(projectName, location);
         try {
             logger.debug("adding nature");
         	addNature(project);
             
             //String[] paths = { "parent/child1-1/child2", "parent/child1-2/child2/child3" }; //$NON-NLS-1$ //$NON-NLS-2$
             //addToProjectStructure(project, paths);        	
         	
         } catch (CoreException e) {
             e.printStackTrace();
             project = null;
         }
 
         return project;
     }
 
     /**
      * Just do the basics: create a basic project.
      *
      * @param location
      * @param projectName
      */
     private static IProject createBaseProject(String projectName, URI location) {
     	logger.debug("projectName= " + projectName + "  location= " + location);
         
    	
     	// it is acceptable to use the ResourcesPlugin class
         IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 
         if (!newProject.exists()) {
             URI projectLocation = location;
 
             IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
             if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
 				projectLocation = null;
 			}
                        
             if(projectLocation != null){
             desc.setLocationURI(projectLocation);
             }
             
            logger.debug("projectLocation: " + projectLocation.getPath());
             try {
                 newProject.create(desc, null);
                 
                 if (!newProject.isOpen()) {
                     newProject.open(null);
                 }
             } catch (CoreException e) {
                 e.printStackTrace();
             }
         }
         
         return newProject;
     }
 
 //    private static void createFolder(IFolder folder) throws CoreException {
 //        IContainer parent = folder.getParent();
 //        if (parent instanceof IFolder) {
 //            createFolder((IFolder) parent);
 //        }
 //        if (!folder.exists()) {
 //            folder.create(false, true, null);
 //        }
 //    }
 
    /**
     * Create a folder structure with a parent root, overlay, and a few child
     * folders.
     *
     * @param newProject
     * @param paths
     * @throws CoreException
     */
 //    private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
 //        for (String path : paths) {
 //            IFolder etcFolders = newProject.getFolder(path);
 //            createFolder(etcFolders);
 //        }
 //    }
 
     private static void addNature(IProject project) throws CoreException {
         if (!project.hasNature(SingleLevelProjectNature.NATURE_ID)) {
             IProjectDescription description = project.getDescription();
             String[] prevNatures = description.getNatureIds();
             String[] newNatures = new String[prevNatures.length + 1];
             System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
             newNatures[prevNatures.length] = SingleLevelProjectNature.NATURE_ID;
             description.setNatureIds(newNatures);
 
             IProgressMonitor monitor = null;
             project.setDescription(description, monitor);
         }
     }
 
 }
