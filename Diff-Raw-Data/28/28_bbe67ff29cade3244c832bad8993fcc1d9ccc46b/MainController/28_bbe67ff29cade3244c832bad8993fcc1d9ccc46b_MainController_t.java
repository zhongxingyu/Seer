 package com.arenz.spriteeditor.controller;
 
 import javax.swing.JFrame;
 
 import com.arenz.spriteeditor.controller.menu.MenuController;
 import com.arenz.spriteeditor.model.Project;
 import com.arenz.spriteeditor.ui.MainWindowView;
 import com.arenz.spriteeditor.ui.Viewable;
 import com.arenz.spriteeditor.ui.dialogs.DialogHelper;
 import com.arenz.spriteeditor.ui.menu.MenuView;
 
 public class MainController extends AbstractController {
 
 	private MainWindowView mainView;
 	
 	private MenuController menuController;
 	
 	private Project project;
 	
 	public MainController() {
 		mainView = new MainWindowView(this, "Sprite Editor");
 		
 		createsMenu();
 	}
 	
 	private void createsMenu() {
 		MenuView menuView = new MenuView();
 		menuController = new MenuController(this, menuView);
 		mainView.setMenu(menuView);
 	}
 
 	public void setProject(Project newProject) {
 		this.project = newProject;
 		mainView.updateTitle(project.getTitle());
 	}
 
 	@Override
 	public AbstractController getParentController() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
	public Viewable getWindow() {
 		return mainView;
 	}
 }
