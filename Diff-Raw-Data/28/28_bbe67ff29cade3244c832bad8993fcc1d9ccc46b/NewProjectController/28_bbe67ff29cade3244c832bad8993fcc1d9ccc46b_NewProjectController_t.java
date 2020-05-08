 /**
  * 
  */
 package com.arenz.spriteeditor.controller.menu;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import com.arenz.spriteeditor.controller.AbstractController;
 import com.arenz.spriteeditor.model.Project;
 import com.arenz.spriteeditor.ui.Viewable;
 import com.arenz.spriteeditor.ui.dialogs.DialogHelper;
 import com.arenz.spriteeditor.ui.dialogs.NewProjectDialog;
 
 /**
  * @author Camille
  *
  */
 public class NewProjectController extends AbstractController {
 	private NewProjectDialog view;
 	private MenuController parentController;
 	
 	public NewProjectController(MenuController parentController, NewProjectDialog newProjectView) {
 		this.parentController = parentController;
 		this.view = newProjectView;
 		
 		addListeners();
 	}
 
 	private void addListeners() {
 		view.addOkButtonListener(new OkButtonListener());
 		view.addCancelButtonListener(new CancelButtonListener());
 		view.addBrowseButtonListener(new CancelButtonListener());
 	}
 
 	public void displayErrorMessage(String title, String text) {
 		DialogHelper.displayErrorMessage(view, title, text);
 	}
 	
 	private class OkButtonListener implements ActionListener {
 		private File rootFolder;
 		private String projectName;
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			projectName = view.getProjectName();
 			String projectRootFolder= view.getProjectRootFolder();
 			
 			boolean areValid = areDataValid(projectRootFolder);
 			
 			if (areValid) {
 				Project project = createNewProject();
 				parentController.setProject(project);
 				closeView();
 			} else {
 				displayErrorMessage(
 						"Invalid data",
 						"Please enter valid data.");
 			}
 		}
 		
 		private boolean areDataValid(String projectRootFolder) {
 			boolean areValid = false;
 			
 			if (!projectName.isEmpty() && !projectRootFolder.isEmpty()) {
 				areValid = false;
 			}
 			
 			rootFolder = new File(projectRootFolder);
 			if (rootFolder.exists() && rootFolder.isDirectory()) {
 				areValid = false;
 			}
 			
 			return areValid;
 		}
 
 		private Project createNewProject() {
 			Project newProject = new Project(projectName, rootFolder);
 			return newProject;
 		}
 	}
 	
 	private class CancelButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			closeView();
 		}
 	}
 	
 	private void closeView() {
 		view.setVisible(false);
 	}
 	
 	public void displayView() {
 		view.pack();
 		view.setVisible(true);
 	}
 
 	@Override
 	public AbstractController getParentController() {
 		return parentController;
 	}
 
 	@Override
	public Viewable getWindow() {
 		return view;
 	}
 }
