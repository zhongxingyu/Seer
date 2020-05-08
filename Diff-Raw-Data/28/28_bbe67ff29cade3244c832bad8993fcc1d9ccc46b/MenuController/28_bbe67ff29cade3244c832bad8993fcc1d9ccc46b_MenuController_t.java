 /**
  * 
  */
 package com.arenz.spriteeditor.controller.menu;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import com.arenz.spriteeditor.controller.AbstractController;
 import com.arenz.spriteeditor.controller.MainController;
 import com.arenz.spriteeditor.model.Project;
 import com.arenz.spriteeditor.ui.Viewable;
 import com.arenz.spriteeditor.ui.dialogs.DialogHelper;
 import com.arenz.spriteeditor.ui.dialogs.NewProjectDialog;
 import com.arenz.spriteeditor.ui.menu.MenuView;
 
 /**
  * @author Camille
  * 
  */
 public class MenuController extends AbstractController {
 	private MainController parentController;
 	private MenuView menuView;
 
 	public MenuController(MainController mainController, MenuView menuView) {
 		this.parentController = mainController;
 		this.menuView = menuView;
 		
 		addListeners();
 	}
 
 	private void addListeners() {
 		menuView.addNewProjectListener(new NewProjectListener());
 		menuView.addSaveProjectListener(new SaveProjectListener());
 		menuView.addLoadProjectListener(new LoadProjectListener());
 		
 		menuView.addQuitProjectListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				System.exit(0);
 			}
 		});
 	}
 
 
 	public class NewProjectListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			NewProjectDialog newProjectDialog = new NewProjectDialog();
 			NewProjectController newProjectController = new NewProjectController(MenuController.this, newProjectDialog);
 			newProjectController.displayView();
 			/*
 			JFileChooser newFileChooser = DialogHelper
 					.createHomeFileChooser();
 			newFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			
 			if (newFileChooser.showDialog(null, "Create") == JFileChooser.APPROVE_OPTION) {
 				createNewProject(newFileChooser.getSelectedFile());
 			}
 			*/
 		}
 	}
 
 	public class SaveProjectListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			DialogHelper.displayNotImplementedDialog();
 		}
 	}
 
 	public class LoadProjectListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			DialogHelper.displayNotImplementedDialog();
 		}
 	}
 
 	public void setProject(Project project) {
 		parentController.setProject(project);
 	}
 
 	@Override
 	public AbstractController getParentController() {
 		return parentController;
 	}
 
 	@Override
	public Viewable getWindow() {
		return parentController.getWindow();
 	}
 }
