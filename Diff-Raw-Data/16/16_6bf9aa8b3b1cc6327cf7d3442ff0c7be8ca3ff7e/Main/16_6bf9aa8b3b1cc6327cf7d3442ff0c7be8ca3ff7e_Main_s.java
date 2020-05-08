 package main;
 
 import java.awt.EventQueue;
 
 import javax.swing.UIManager;
 
 import resources.GUIResources;
 import resources.SimulationResources;
//import test.Example;
 import view.mainwindow.MainWindow;
 
 public class Main {
 
 	public static void main(String[] args) {
 
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 
 				try {

 					SimulationResources.initialize();
 					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 					GUIResources.mainFrame = new MainWindow();
 					//new Example();
 					
 				}
 
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 		});
 
 	}
 
 }
