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
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.util.URLUtils;
 
 import java.awt.*;
 import java.awt.event.*;
 
 @SuppressWarnings("serial")
 public class HelpWindow extends Window {
 	private JLabel lblTitle;
 
 	public HelpWindow() {
 		getContentPane().setLayout(null);
 		
 		lblTitle = new JLabel("NavalBattle Help");
 		JButton btnClose = new JButton("Close");
 		JLabel lblYoutubeTutorials = new JLabel("YouTube Tutorials");
 		JButton btnVideoOverview = new JButton("Overview");
 		JButton btnVideoNavigation = new JButton("Navigation");
 		JButton btnVideoCombat = new JButton("Combat");
 		JButton btnVideoUpgradeShop = new JButton("Upgrades");
 		JButton btnVideoMultiplayer = new JButton("Multiplayer");
 		JButton btnVideoRoketgamer = new JButton("RoketGamer");
 		
 		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
 		
 		lblTitle.setBounds(10, 11, 121, 23);
 		btnClose.setBounds(10, 137, 82, 23);
 		lblYoutubeTutorials.setBounds(20, 36, 111, 14);
 		btnVideoOverview.setBounds(30, 61, 82, 23);
 		btnVideoNavigation.setBounds(122, 61, 89, 23);
 		btnVideoCombat.setBounds(221, 61, 89, 23);
 		btnVideoUpgradeShop.setBounds(320, 61, 107, 23);
 		btnVideoMultiplayer.setBounds(111, 95, 100, 23);
 		btnVideoRoketgamer.setBounds(221, 95, 100, 23);
 		
 		getContentPane().add(lblTitle);
 		getContentPane().add(btnClose);
 		getContentPane().add(lblYoutubeTutorials);
 		getContentPane().add(btnVideoOverview);
 		getContentPane().add(btnVideoNavigation);
 		getContentPane().add(btnVideoCombat);
 		getContentPane().add(btnVideoUpgradeShop);
 		getContentPane().add(btnVideoMultiplayer);
 		getContentPane().add(btnVideoRoketgamer);
 		
 		btnVideoOverview.setFocusable(false);
 		btnVideoNavigation.setFocusable(false);
 		btnVideoCombat.setFocusable(false);
 		btnVideoUpgradeShop.setFocusable(false);
 		btnVideoMultiplayer.setFocusable(false);
 		btnVideoRoketgamer.setFocusable(false);
 		btnClose.setFocusable(false);
 		
 		btnVideoRoketgamer.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Opening RoketGamer tutorial");
 				URLUtils.openURL("http://www.google.com");
 			}
 		});
 		btnVideoOverview.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Opening overview tutorial");
 				URLUtils.openURL("http://www.google.com");
 			}
 		});
 		btnClose.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				dispose();
 				new MainMenuWindow();
 			}
 		});
 		btnVideoOverview.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		btnVideoNavigation.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Opening navigation tutorial");
 				URLUtils.openURL("http://www.google.com");
 			}
 		});
 		btnVideoCombat.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Opening combat tutorial");
 				URLUtils.openURL("http://www.google.com");
 			}
 		});
 		btnVideoUpgradeShop.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Opening upgrade shop tutorial");
 				URLUtils.openURL("http://www.google.com");
 			}
 		});
 		btnVideoMultiplayer.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Opening Multiplayer tutorial");
 				URLUtils.openURL("http://www.google.com");
 			}
 		});
 	}
 }
