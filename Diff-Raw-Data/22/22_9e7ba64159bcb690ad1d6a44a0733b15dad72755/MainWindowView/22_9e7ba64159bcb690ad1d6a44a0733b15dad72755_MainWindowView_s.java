 /**
  * 
  */
 package com.arenz.spriteeditor.ui;
 
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
import javax.swing.JMenuBar;
 
 import com.arenz.spriteeditor.controller.MainController;
 import com.arenz.spriteeditor.ui.menu.MenuView;
 
 /**
  * @author Camille
  *
  */
 public class MainWindowView {
 	
 	private JFrame window = new JFrame();
 	private MenuView menu;
 	
 	private MainController controller;
 
 	public MainWindowView(MainController controller, String title) {
 		window.setTitle(title);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setMinimumSize(new Dimension(500, 500));
 		
 		window.setVisible(true);
 	}
 	
 	public void setMenu(MenuView menu) {
 		window.setJMenuBar(menu.getMenuBar());
 	}
 }
