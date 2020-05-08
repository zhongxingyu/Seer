 package org.padacore.wizards;
 
 import java.net.URI;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.padacore.AdaProjectNature;
 
 public class NewAdaProject {
 
 	private IProject            project;
 	private IProjectDescription description;
 	
 	private static final String[] NATURES = {AdaProjectNature.NATURE_ID}; 
 	
 	/**
 	 * Create a new NewAdaProject instance.
 	 * 
 	 * @param project
 	 *            Project handle on the project to create.
 	 * @param location
 	 *            Project location.
 	 */
 	public NewAdaProject (IProject project, URI location) {
 		
 		this.project     = project;
 		this.description = project.getWorkspace().newProjectDescription(project.getName());
 		
 		this.description.setNatureIds(NATURES);
		this.description.setLocationURI(location);		 
 	}
 	
 	/**
 	 * Create and return an Ada project (IProject) or return existing one if it has been
 	 * already created.
 	 */
 	public IProject Create() {
 
 		if (!project.exists()) {
 			try {
 				project.create(description, null);
 				project.open(null);
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	
 		
 		return project;
 	}
 }
