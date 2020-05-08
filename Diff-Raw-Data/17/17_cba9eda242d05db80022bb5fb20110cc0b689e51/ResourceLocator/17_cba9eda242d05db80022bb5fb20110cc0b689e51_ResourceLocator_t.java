 package org.padacore.core.project;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
import org.padacore.core.project.PropertiesManager.ProjectKind;
 
 /**
  * This class enables to get a resource handle from an absolute path.
  * 
  * @author RS
  * 
  */
 public class ResourceLocator {
 
 	private IProject project;
 
 	public ResourceLocator(IProject project) {
 		try {
 			Assert.isLegal(project.hasNature(AdaProjectNature.NATURE_ID));
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 		this.project = project;
 	}
 
 	/**
 	 * Returns a resource handle for the resource identified by the given
 	 * absolute path or null if no such resource exists in project.
 	 * 
 	 * @param absolutePath
 	 *            the absolute path for the resource to find.
 	 * @return the resource handle or null if no such resource exists.
 	 */
 	public IResource findResourceFromPath(IPath absolutePath) {
 		IPath resourceRelativePath = this
 				.getProjectRelativePathFor(absolutePath);
 
 		return this.project.findMember(resourceRelativePath);
 	}
 
 	/**
 	 * Converts the given absolute path to a path relative to the project root
 	 * path.
 	 * 
 	 * @param absolutePath
 	 *            the absolute path to convert
 	 * @return the path relative to the project root path
 	 */
 	private IPath getProjectRelativePathFor(IPath absolutePath) {
 		IAdaProject associatedAdaProject = AbstractAdaProjectAssociationManager
 				.GetAssociatedAdaProject(this.project);
 
 		IPath projectRootPath = associatedAdaProject.getRootPath();
 
 		IPath relativePath = absolutePath.makeRelativeTo(projectRootPath);
 
 		if (this.isProjectAnImportedProject()) {
 			// TOOD change name of derived directory
 			relativePath = new Path("toto").append(relativePath);
 		}
 
 		return relativePath;
 	}
 
 	/**
 	 * Checks if the project corresponds to an imported project (i.e. a project
 	 * which does not reside in workspace but is linked to an existing folder).
 	 * 
 	 * @return True if project is an imported project, False otherwise.
 	 */
 	private boolean isProjectAnImportedProject() {
		PropertiesManager propertiesManager = new PropertiesManager(
				this.project);
		
		return propertiesManager.getProjectKind() == ProjectKind.IMPORTED;
 	}
 }
