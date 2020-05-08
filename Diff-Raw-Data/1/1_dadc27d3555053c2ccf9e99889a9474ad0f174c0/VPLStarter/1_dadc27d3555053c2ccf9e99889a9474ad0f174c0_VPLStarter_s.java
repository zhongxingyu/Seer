 package net.hlw5a.VidPicLib.Ui;
 
 import javax.swing.SwingUtilities;
 
 public class VPLStarter {
 	public static void main(String[] args) {
		int i = 0;
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VidPicLib");
 		SwingUtilities.invokeLater(new VPLMainProgram());
 	}
 }
