 package com.technophobia.substeps.predicate;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 
 import com.technophobia.eclipse.project.ProjectManager;
 import com.technophobia.substeps.supplier.Predicate;
 
 public class IsFeatureFolderPredicate implements Predicate<IFolder> {
 
     private final ProjectManager projectManager;
 
 
     public IsFeatureFolderPredicate(final ProjectManager projectManager) {
         this.projectManager = projectManager;
     }
 
 
     @Override
     public boolean forModel(final IFolder folder) {
         final IProject project = folder.getProject();
         final IPath featureFolderPath = featureFolderForProject(project);
 
        System.out.println("Feature folder path " + featureFolderPath + ", folder location: " + folder.getLocation());

         return featureFolderPath != null ? isDescendantOf(folder.getLocation(), featureFolderPath) : false;
     }
 
 
     private boolean isDescendantOf(final IPath folder, final IPath featureFolderPath) {
         return featureFolderPath.isPrefixOf(folder);
     }
 
 
     private IPath featureFolderForProject(final IProject project) {
         return projectManager.featureFolderFor(project);
     }
 }
