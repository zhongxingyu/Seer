 /*
  * Shared Folder Chat is a program for chatting via a shared folder in a local area network (LAN).
  * Copyright (C) 2013  Niklas Wenzel <nikwen.developer@gmail.com>
  * 
  * This file is part of Shared Folder Chat.
  *  
  * Shared Folder Chat is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Shared Folder Chat is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License 
  * along with Shared Folder Chat.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.wenzel.niklas.sharedfolderchat;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 import net.miginfocom.swing.MigLayout;
 import javax.swing.JLabel;
 import javax.swing.Box;
 
 import java.awt.Cursor;
 import java.awt.Desktop;
 import java.awt.Font;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 public class AboutDialog extends JDialog {
 
 	private static final long serialVersionUID = -4410499108609020626L;
 
 	public AboutDialog(JFrame parent) {
 		setModal(true);
 		setResizable(false);
 		setTitle("About");
 		setBounds(150, 150, 420, 304);
 		super.setLocationRelativeTo(parent);
 		getContentPane().setLayout(new MigLayout("", "[][29.00][][35][16.00][35][10.00][grow]", "[][11.00][35.00][35.00][35.00][35.00][:35.00:35.00][35.0,grow][]"));
 		
		JLabel lblAbout = new JLabel("School Chat Room");
 		lblAbout.setFont(new Font("Tahoma", Font.PLAIN, 18));
 		getContentPane().add(lblAbout, "cell 0 0 8 1,alignx center");
 		
 		Box horizontalBox = Box.createHorizontalBox();
 		getContentPane().add(horizontalBox, "cell 1 1");
 		
 		getContentPane().add(new JLabel(new ImageIcon(AboutDialog.class.getResource("/de/wenzel/niklas/sharedfolderchat/icon1.png"))), "cell 1 2 5 5");
 		
 		JLabel lblProgrammedByNiklas = new JLabel("Copyright 2013 Niklas Wenzel");
 		getContentPane().add(lblProgrammedByNiklas, "cell 7 2");
 		
 		JLabel lblNewLabel = new JLabel("Version 1.1");
 		getContentPane().add(lblNewLabel, "cell 7 3");
 		
 		JLabel lblReleasedUnderThe = new JLabel("<html>Released under the <a href=\"http://www.gnu.org/licenses/gpl.html\">GPL V3.</a></html>");
 		lblReleasedUnderThe.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				try {
 					final URI licenseURI = new URI("http://www.gnu.org/licenses/gpl.html");
 					if (Desktop.isDesktopSupported()) {
 						try {
 							Desktop.getDesktop().browse(licenseURI);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		lblReleasedUnderThe.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		getContentPane().add(lblReleasedUnderThe, "cell 7 4");
 		
 		JLabel lblSourceCodeAvailable = new JLabel("<html>Source Code available at <br><a href=\"https://github.com/nikwen/SharedFolderChat\">github.com</a></html>");
 		lblSourceCodeAvailable.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				try {
 					final URI licenseURI = new URI("https://github.com/nikwen/SharedFolderChat");
 					if (Desktop.isDesktopSupported()) {
 						try {
 							Desktop.getDesktop().browse(licenseURI);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		lblSourceCodeAvailable.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		getContentPane().add(lblSourceCodeAvailable, "cell 7 5");
 		
 		JLabel lblContactMe = new JLabel("<html>Contact me: <a href=\"mailto:nikwen.developer@gmail.com?subject=Shared%20Folder%20Chat\">nikwen.developer@gmail.com</a></html>");
 		lblContactMe.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Desktop desktop = Desktop.getDesktop();
 				URI uri = URI.create("mailto:nikwen.developer@gmail.com?subject=Shared%20Folder%20Chat");
 				try {
 					desktop.mail(uri);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		lblContactMe.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		getContentPane().add(lblContactMe, "cell 7 6");
 		
 		JButton btnOkButton = new JButton("Ok");
 		btnOkButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				dispose();
 			}
 		});
 		getContentPane().add(btnOkButton, "cell 1 8 7 1,alignx center");
 		btnOkButton.requestFocus();
 		getRootPane().setDefaultButton(btnOkButton);
 	}
 }
