 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller.terminal.controller;
 
 import controller.terminal.interfacesGUI.TerminalMainVue;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class TerminalController {
 
 	private static TerminalMainVue mainVue;
 	private static boolean doAutoCancel;
 	private static boolean doAlertBeforeAutoCancel;
 
 	public static void setDoAutoCancel(boolean doAutoCancel) {
 		TerminalController.doAutoCancel = doAutoCancel;
 	}
 
 	public static boolean isDoAutoCancel() {
 		return doAutoCancel;
 	}
 
 	public static void setDoAlertBeforeAutoCancel(boolean doAlertBeforeAutoCancel) {
 		TerminalController.doAlertBeforeAutoCancel = doAlertBeforeAutoCancel;
 	}
 
 	public static boolean isDoAlertBeforeAutoCancel() {
 		return doAlertBeforeAutoCancel;
 	}
 
 	private static void setMainVue(TerminalMainVue mainVue) {
 		TerminalController.mainVue = mainVue;
 	}
 
 	public static TerminalMainVue getMainVue() {
 		return TerminalController.mainVue;
 	}
 
 	public TerminalController(TerminalMainVue mainVue) {
 		setMainVue(mainVue);
 		mainVue.displayTerminalWelcome();
 	}
 
 	public static void doExit() {
 		VueStateMachine.doCancel();
 		System.exit(0);
 	}
 }
