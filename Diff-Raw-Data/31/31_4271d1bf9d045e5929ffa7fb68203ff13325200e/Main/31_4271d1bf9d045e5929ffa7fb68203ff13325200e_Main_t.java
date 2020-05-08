 package main;
 
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
//		MKLLibraryLoader.loadLibrary(); TODO FIX IT
//		SpeechSynthesizer.init(); TODO FIX IT
 		LookAndFeelHelper.initLnFManager();
 		//#########################################################################
 		
 		Mainwindow MainWindow = new Mainwindow();
 		MainWindow.setVisible(true);
 		
 		//#################################################################
 		//############## SUPER WICHTIGER NATIVER DESTROYCODE ##############
 		//#################################################################
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 		    @Override
 		    public void run() {
//		        SpeechSynthesizer.destroy(); TODO FIX IT
 		    }
 		});
 		//#################################################################
 	}
 }
