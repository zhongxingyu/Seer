 package com.arenz.spriteeditor.controller;
 
 import com.arenz.spriteeditor.controller.menu.MenuController;
 import com.arenz.spriteeditor.ui.MainWindowView;
 import com.arenz.spriteeditor.ui.menu.MenuView;
 
 public class MainController {
 
 	private MainWindowView mainView;
 	
 	private MenuController menuController;
 	
 	public MainController() {
 		mainView = new MainWindowView(this, "Sprite Editor");
 		
 		createsMenu();
 	}
	
	public void displayErrorMessage(String title, String text) {
		mainView.displayErrorMessage(title, text);
	}
 
 	private void createsMenu() {
 		menuController = new MenuController();
 		MenuView menuView = new MenuView(menuController);
 		mainView.setMenu(menuView);
 	}
 }
