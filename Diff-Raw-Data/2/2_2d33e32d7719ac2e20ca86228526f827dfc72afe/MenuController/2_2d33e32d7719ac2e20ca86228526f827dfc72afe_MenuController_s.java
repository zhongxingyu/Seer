 /**
  * 
  */
 package com.arenz.spriteeditor.controller.menu;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.arenz.spriteeditor.controller.MainController;
 import com.arenz.spriteeditor.model.Project;
 
 /**
  * @author Camille
  *
  */
 public class MenuController {
 	private MainController parentController;
 
 	public void createNewProject(File file) {
 		try {
 			String path = file.getCanonicalPath();
 			String title = file.getName();
 			Project newProject = new Project(title, path);
 		} catch (IOException IOE) {
 			IOE.printStackTrace();
			//TODO: call the main controller to display an error
 		}
 	}
 }
