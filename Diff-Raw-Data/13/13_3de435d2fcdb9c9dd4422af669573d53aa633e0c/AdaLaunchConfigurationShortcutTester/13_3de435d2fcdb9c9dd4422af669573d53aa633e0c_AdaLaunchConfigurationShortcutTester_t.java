 package org.padacore.ui.launch;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.padacore.core.project.AbstractAdaProjectAssociationManager;
 import org.padacore.core.project.AdaProjectNature;
 import org.padacore.core.project.IAdaProject;
 import org.padacore.core.project.PropertiesManager;
 
 public class AdaLaunchConfigurationShortcutTester extends PropertyTester {
 
 	private final static String BELONGS_TO_ADA_PROJECT = "belongsToAdaProject";
 	private final static String IS_EXECUTABLE_ADA_PROJECT = "isExecutableAdaProject";
 	private final static String IS_ADA_EDITOR = "isAdaExecutableEditor";
 
 	@Override
 	public boolean test(Object receiver, String property, Object[] args,
 			Object expectedValue) {
 
 		boolean testPassed = false;
 
 		if (property.equals(BELONGS_TO_ADA_PROJECT)) {
 			Assert.isLegal(receiver instanceof IFile);
 			testPassed = this.isFileAnExecutableInAdaProject((IFile) receiver);
 		} else if (property.equals(IS_EXECUTABLE_ADA_PROJECT)) {
 			Assert.isLegal(receiver instanceof IProject);
 			testPassed = this
 					.isProjectAnExecutableAdaProject((IProject) receiver);
 		} else if (property.equals(IS_ADA_EDITOR)) {
 			Assert.isLegal(receiver instanceof IEditorPart);
 			testPassed = this
 					.doesEditorPointsToAnExecutableInAdaProject((IEditorPart) receiver);
 		}
 
 		return testPassed;
 
 	}
 
 	/**
 	 * Checks if the given editor points to an executable of a project with Ada
 	 * nature.
 	 * 
 	 * @param selectedEditor
 	 *            the selected editor to check.
 	 * @return True if the given editor points to an executable of a project
 	 *         with Ada nature, False otherwise.
 	 */
 	private boolean doesEditorPointsToAnExecutableInAdaProject(
 			IEditorPart selectedEditor) {
 		IEditorInput editorInput = selectedEditor.getEditorInput();
 		boolean testPassed = false;
 
 		if (editorInput instanceof IFileEditorInput) {
 			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
 			IFile editedFile = fileEditorInput.getFile();
 
 			testPassed = this.isFileAnExecutableInAdaProject(editedFile);
 		}
 		return testPassed;
 	}
 
 	/**
	 * Checks if the given project has an Ada nature, is open and is executable.
 	 * 
 	 * @param selectedProject
 	 *            the selected project which is tested.
 	 * @return True if the given project is open, has an Ada nature and is
 	 *         executable, False otherwise.
 	 */
 	private boolean isProjectAnExecutableAdaProject(IProject selectedProject) {
 		boolean hasAdaNatureAndIsOpen = false;
 		boolean isAnExecutableProject = false;
 
 		IAdaProject associatedAdaProject;
 
 		try {
 			if (selectedProject.isOpen()) {
 				hasAdaNatureAndIsOpen = selectedProject
 						.hasNature(AdaProjectNature.NATURE_ID);
 			}
 
 			if (hasAdaNatureAndIsOpen) {
 				associatedAdaProject = AbstractAdaProjectAssociationManager
 						.GetAssociatedAdaProject(selectedProject);
 				isAnExecutableProject = associatedAdaProject.isExecutable();
 			}
 		} catch (CoreException e) {
 			hasAdaNatureAndIsOpen = false;
 		}
 
 		return hasAdaNatureAndIsOpen && isAnExecutableProject;
 	}
 
 	/**
 	 * Checks if the given file correspond to an executable of a project with
 	 * Ada nature.
 	 * 
 	 * @param selectedFile
 	 *            the selected file to check.
 	 * @return True if the given file correspond to an executable of a project
 	 *         with Ada nature, False otherwise.
 	 */
 	private boolean isFileAnExecutableInAdaProject(IFile selectedFile) {
 
 		boolean belongsToExecutableAdaProject = this
 				.isProjectAnExecutableAdaProject(selectedFile.getProject());
 		boolean selectedFileIsAnExecutableOfProject = false;
 
 		if (belongsToExecutableAdaProject) {
			PropertiesManager propertiesManager = new PropertiesManager(
					selectedFile.getProject());
			IAdaProject adaProject = propertiesManager.getAdaProject();

 			selectedFileIsAnExecutableOfProject = adaProject
 					.getExecutableNames().contains(selectedFile.getName())
 					|| adaProject.getExecutableSourceNames().contains(
 							selectedFile.getName());
 		}
 
 		return selectedFileIsAnExecutableOfProject;
 	}
 }
