 package nz.ac.auckland.netlogin.gui;
 
 import nz.ac.auckland.netlogin.NetLogin;
 import javax.swing.*;
 
 public class AboutDialog {
 
 	private String version;
 
 	public AboutDialog() {
 		version = readVersion();
 	}
 
 	public void open() {
 		JOptionPane.showMessageDialog(null, getMessage());
 	}
 
 	public String getMessage() {
 		StringBuilder message = new StringBuilder();
 
 		message.append("NetLogin Client\n");
 		if (version != null) message.append("Version: ").append(version).append("\n");
         message.append("\n");
		message.append("© 2001 The University of Auckland.\n");
         message.append("Released under terms of the GNU GPL.\n");
 
 		return message.toString();
 	}
 
 	public String readVersion() {
         Package pkg = NetLogin.class.getPackage();
         if (pkg == null) return null;
         return pkg.getImplementationVersion();
 	}
 
 }
