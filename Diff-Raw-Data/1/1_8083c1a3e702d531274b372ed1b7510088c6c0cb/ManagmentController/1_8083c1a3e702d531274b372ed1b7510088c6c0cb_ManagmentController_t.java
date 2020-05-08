 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller.managment.controller;
 
 import controller.managment.interfacesGUI.ManagmentMainVue;
 
 /**
  *
  * @author valentin.seitz
  */
 public class ManagmentController {
 
 	private static ManagmentMainVue mainVue;
 
 	public static ManagmentMainVue getMainVue() {
 		return ManagmentController.mainVue;
 	}
 
 	private static void setMainVue(ManagmentMainVue mainVue) {
 		ManagmentController.mainVue = mainVue;
 	}
 
 	public ManagmentController(ManagmentMainVue mainVue) {
 		setMainVue(mainVue);
 	}
 }
