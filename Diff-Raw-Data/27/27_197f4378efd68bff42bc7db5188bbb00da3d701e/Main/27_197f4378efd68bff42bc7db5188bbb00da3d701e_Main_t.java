 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main;
 
 import controller.terminal.controller.TerminalController;
 import java.net.URL;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import resource.Resource;
 import resource.config.Configuration;
 import resource.log.ProjectLogger;
 import vue.terminal.TerminalMainFrame;
 import vue.terminal.multiview.TerminalMultiMainVue;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class Main {
 
 	public static void main(String[] args) {
 		int nbFrames;
 		TerminalMultiMainVue mainVue;
 		TerminalMainFrame mainFrame;
 		URL iconUrl;
 		ImageIcon icon;
 		TerminalController controller;
 
 		//Configure Logger following tha configuration
 		ProjectLogger.setUniqueLog(
 				Configuration.getParam(
 				Configuration.CONFIGSECTION_LOG,
 				Configuration.CONFIGPARAM_LOG_UNIQUE)
 				.equals(Configuration.BooleanTrue));
 
 		//Set system look and feel
 		try {
 			UIManager.setLookAndFeel(
 					UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 		} catch (InstantiationException e) {
 		} catch (IllegalAccessException e) {
 		} catch (UnsupportedLookAndFeelException e) {
 		}
 
 		nbFrames = selectNbFrames();
 
 		//Realy instanciate program
 		mainVue = new TerminalMultiMainVue();
 		for (int i = 0; i < nbFrames; i++) {
 			mainFrame = new TerminalMainFrame();
 			{
 				iconUrl = Resource.getResource(Resource.IMAGE_LOGO);
 				if (iconUrl != null) {
 					icon = new ImageIcon(iconUrl);
 					//		/!\ icon can be null if the resource iconUrl doesn't exit! /!\
 					if (icon != null) {
 						mainFrame.setIconImage(icon.getImage());
 					}
 				}
 				//Setting minimum size of frame
 				mainFrame.setMinimumSize(mainFrame.getPreferredSize());
 				mainFrame.pack();
 				//Centering the frame
 				mainFrame.setLocationRelativeTo(null);
 			}
 			mainVue.add(mainFrame);
 		}
 		controller = new TerminalController(mainVue);
 		TerminalController.setDoAutoCancel(true);
 		TerminalController.setDoAlertBeforeAutoCancel(true);
 
 		mainVue.setVisible(true);
 	}
 
 	private static int selectNbFrames() {
 		int nbFrames;
 
 		Object[] possibilities = {"1", "2", "3", "4"};
 		String s = (String) JOptionPane.showInputDialog(
 				null,
 				"Nombre de fenêtres ?",
 				"Paramètrage d'interface",
 				JOptionPane.PLAIN_MESSAGE,
 				null,
 				possibilities,
 				"1");
 		if ((s != null) && (s.length() > 0)) {
 			nbFrames = Integer.parseInt(s);
 		} else {
			nbFrames = 1;
 		}
 		return nbFrames;
 	}
 }
