 package com.mrroman.springgames.utils;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 
 public class SwingUtils {
 
 	public void showPicture(String url) {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
	
 		JLabel label;
 		try {
 			label = new JLabel(new ImageIcon(new URL(url)));
 		} catch (MalformedURLException e) {
 			label = new JLabel(url);
 		}
		
		dialog.add(label);
 		dialog.pack();
 		dialog.setVisible(true);
 	}
 	
 }
