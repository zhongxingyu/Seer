 package net.hlw5a.VidPicLib.Ui;
 
 import javax.swing.SwingUtilities;
 
 public class VPLStarter {
 	public static void main(String[] args) {
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VidPicLib");
 		SwingUtilities.invokeLater(new VPLMainProgram());
 	}
 }
