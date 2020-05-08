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
 
 import com.jpii.gamekit.GameKit;
 import com.jpii.navalbattle.data.Constants;
 import com.roketgamer.RoketGamer;
 
 import java.awt.*;
 import java.awt.event.*;
 
 public class CreditsWindow extends BaseWindow {
 	private static final long serialVersionUID = 1L;
 	private JButton btnClose;
 	
 	private String contributorsText = "Many thanks to our contributors who have contributed to the project:" +
 			"<ul><li><b>Anthony \"abauer\" Bauer</b> - game design lead</li>" +
			"<li><b>Thomas \"TexasGamer\" Gaubert</b> - SCM manager, RoketGamer lead, music implementation</li>" +
 			"<li><b>Max \"maximusvladimir\" Kirkby</b> - grid and rendering lead</li>" +
 			"<li><b>JR \"DarkWarHero\" Vetus</b> - AI lead</li>" +
 			"<li><b>Matt \"Matthis5point0\" Waller</b> - QA and game-play lead</li>" +
			"<li><b>Zach \"smeagle42\" Matthewson</b> - art and music design lead</li>" +
 			"<li><b>Stephen \"szsike\" Ziske - art asset contributions</li>" +
 			"</ul>";
 
 	public CreditsWindow() {
 
 		getContentPane().setLayout(null);
 		btnClose = new JButton("Close");
 		btnClose.setBounds(10, 261, 90, 30);
 		getContentPane().add(btnClose);
 
 		btnClose.setFocusable(false);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(0, 0, 476, 250);
 		getContentPane().add(tabbedPane);
 		
 		JPanel navalbattlePanel = new JPanel();
 		tabbedPane.addTab("NavalBattle", null, navalbattlePanel, null);
 		navalbattlePanel.setLayout(null);
 		
 		JLabel lblNavalbattle = new JLabel("NavalBattle");
 		lblNavalbattle.setHorizontalAlignment(SwingConstants.CENTER);
 		lblNavalbattle.setFont(new Font("Tahoma", Font.PLAIN, 38));
 		lblNavalbattle.setBounds(111, 11, 197, 46);
 		navalbattlePanel.add(lblNavalbattle);
 		
 		JLabel lblNBVersion = new JLabel("Version " + Constants.NAVALBATTLE_VERSION + " (" + Constants.NAVALBATTLE_CODENAME + ")");
 		lblNBVersion.setHorizontalAlignment(SwingConstants.CENTER);
 		lblNBVersion.setBounds(111, 68, 197, 14);
 		navalbattlePanel.add(lblNBVersion);
 		
 		JLabel lblNBLicense = new JLabel("NavalBattle is open source under the GNU General Public License v3.");
 		lblNBLicense.setHorizontalAlignment(SwingConstants.CENTER);
 		lblNBLicense.setBounds(10, 187, 409, 14);
 		navalbattlePanel.add(lblNBLicense);
 		
 		JPanel gamekitPanel = new JPanel();
 		tabbedPane.addTab("GameKit", null, gamekitPanel, null);
 		gamekitPanel.setLayout(null);
 		
 		JLabel lblGameKit = new JLabel("GameKit");
 		lblGameKit.setHorizontalAlignment(SwingConstants.CENTER);
 		lblGameKit.setFont(new Font("Tahoma", Font.PLAIN, 38));
 		lblGameKit.setBounds(111, 11, 197, 46);
 		gamekitPanel.add(lblGameKit);
 		
 		JLabel lblGKVersion = new JLabel("Version " + GameKit.getVersion() + " (" + GameKit.getApiLevel() + ")");
 		lblGKVersion.setHorizontalAlignment(SwingConstants.CENTER);
 		lblGKVersion.setBounds(111, 68, 197, 14);
 		gamekitPanel.add(lblGKVersion);
 		
 		JLabel lblGKLicense = new JLabel("GameKit is open source under the GNU General Public License v3.");
 		lblGKLicense.setHorizontalAlignment(SwingConstants.CENTER);
 		lblGKLicense.setBounds(10, 187, 409, 14);
 		gamekitPanel.add(lblGKLicense);
 		
 		JPanel panel = new JPanel();
 		tabbedPane.addTab("RoketGamer", null, panel, null);
 		panel.setLayout(null);
 		
 		JLabel lblRoketgamer = new JLabel("RoketGamer");
 		lblRoketgamer.setHorizontalAlignment(SwingConstants.CENTER);
 		lblRoketgamer.setFont(new Font("Tahoma", Font.PLAIN, 38));
 		lblRoketgamer.setBounds(92, 11, 241, 46);
 		panel.add(lblRoketgamer);
 		
 		JLabel lblRKVersion = new JLabel("Version " + RoketGamer.VERSION);
 		lblRKVersion.setHorizontalAlignment(SwingConstants.CENTER);
 		lblRKVersion.setBounds(111, 68, 197, 14);
 		panel.add(lblRKVersion);
 		
 		JLabel lblRGLink = new JLabel("http://www.roketgamer.com");
 		lblRGLink.setHorizontalAlignment(SwingConstants.CENTER);
 		lblRGLink.setBounds(10, 187, 409, 14);
 		panel.add(lblRGLink);
 		
 		JPanel contributorPanel = new JPanel();
 		tabbedPane.addTab("Contributors", null, contributorPanel, null);
 		contributorPanel.setLayout(null);
 		
 		JTextPane contributorsTextPane = new JTextPane();
 		contributorsTextPane.setFont(new Font("Arial", Font.PLAIN, 11));
 		contributorsTextPane.setContentType("text/html");
 		contributorsTextPane.setEditable(false);
 		contributorsTextPane.setBounds(0, 0, 473, 222);
 		contributorsTextPane.setText(contributorsText);
 		contributorPanel.add(contributorsTextPane);
 
 		btnClose.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				nextWindow("MainMenuWindow");
 			}
 		});
 	}
 }
