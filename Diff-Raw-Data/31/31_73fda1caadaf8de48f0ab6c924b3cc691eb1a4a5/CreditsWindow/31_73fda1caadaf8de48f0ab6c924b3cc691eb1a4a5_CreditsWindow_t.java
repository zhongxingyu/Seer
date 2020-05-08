 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
 
 @SuppressWarnings("serial")
 public class CreditsWindow extends Window {
 	private JLabel gameTitle;
 	private JLabel licenseNotice;
 	private JButton btnClose;
	private CreditsBackground cb;
 
 	public CreditsWindow() {
 		setSize(800,600);
		cb = new CreditsBackground(800,600);
		setContentPane(cb);
 		getContentPane().setLayout(null);
 		gameTitle = new JLabel("NavalBattle");
 		licenseNotice = new JLabel("NavalBattle is open source under the GNU General Public License v3.");
 		btnClose = new JButton("Close");
 		
 		gameTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
 
 		licenseNotice.setBounds(0, 292, 492, 15);
 		btnClose.setBounds(205, 254, 90, 30);
 		
 		licenseNotice.setHorizontalAlignment(SwingConstants.CENTER);
 		btnClose.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		//hide for now.
 		//getContentPane().add(licenseNotice);
 		//getContentPane().add(btnClose);	
 		
 		
 		btnClose.setFocusable(false);
 		
 		btnClose.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				nextWindow("MainMenuWindow");
 			}
 		});
		
		FocusListener fl = new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				cb.start();
			}
			public void focusLost(FocusEvent arg0) {
				cb.stop();
			}		
		};
		addFocusListener(fl);
 	}
 }
