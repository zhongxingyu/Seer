 package nz.ac.auckland.netlogin.gui;
 
 import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
 
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
 
		message.append("nz.ac.auckland.netlogin.NetLogin Client");
		if (version != null) message.append(" Version ").append(version);
		message.append("\n");
		message.append("Copyright(C) 2001-2011 The University of Auckland.\n");
		message.append("Released under terms of the GNU GPL.\n");
 
 		return message.toString();
 	}
 
 	public String readVersion() {
		InputStream manifestIn = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
		if (manifestIn == null) return null;
		try {
			Manifest manifest = new Manifest(manifestIn);
			return manifest.getMainAttributes().getValue("Implementation-Version");
		} catch (IOException e) {
			// if we cannot read the version, proceed without
			return null;
		}
 	}
 
 }
