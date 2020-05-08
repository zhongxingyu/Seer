 package com.mrroman.springgames.utils;
 
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 
 public class SwingUtils {
 
 	public void showPicture(String url) {
		final JDialog dialog = new JDialog();
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		
 		JLabel label;
 		try {
 			label = new JLabel(new ImageIcon(new URL(url)));
 		} catch (MalformedURLException e) {
 			label = new JLabel(url);
 		}
				
		dialog.getContentPane().add(BorderLayout.CENTER, label);
 		dialog.pack();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		dialog.setVisible(true);
 	}
 	
 }
