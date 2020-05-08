 package main;
 
import javax.swing.JFrame;
import javax.swing.JOptionPane;

 import gui.Mainwindow;
 import util.LookAndFeelHelper;
 import util.SpeechSynthesizer;
 import de.mikescher.MikesKeyLogger.helper.MKLLibraryLoader;
 
 public class Main {
 	public static boolean DEBUG = true; // DEBUG MODE
 	
 	public static void main(String[] args) {
 		//#########################################################################
 		//############## SUPER WICHTIGER NATIVER INITIALIZATIONSCODE ##############
 		//#########################################################################
		String loadedLib = MKLLibraryLoader.loadLibrary();
 		SpeechSynthesizer.init();
 		LookAndFeelHelper.initLnFManager();
 		//#########################################################################
		if (loadedLib == null) { // catch dem errors
			JOptionPane.showMessageDialog(new JFrame(), "GlobalKeyListener DLL not found", "DLL not found", 0);
		}
		//#########################################################################
 		
 		Mainwindow MainWindow = new Mainwindow();
 		MainWindow.setVisible(true);
 		
 		//#################################################################
 		//############## SUPER WICHTIGER NATIVER DESTROYCODE ##############
 		//#################################################################
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 		    @Override
 		    public void run() {
 		        SpeechSynthesizer.destroy();
 		    }
 		});
 		//#################################################################
 	}
 }
