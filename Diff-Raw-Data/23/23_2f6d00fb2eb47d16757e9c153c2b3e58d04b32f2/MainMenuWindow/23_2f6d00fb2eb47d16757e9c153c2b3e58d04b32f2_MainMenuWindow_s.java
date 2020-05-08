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
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.gui.listeners.*;
 import com.jpii.navalbattle.renderer.Helper;
 
 @SuppressWarnings("serial")
 public class MainMenuWindow extends Window {
 	Timer ticker;
 	//MenuBackground backgrnd;
 	int ticks;
 	
 	public MainMenuWindow() {
 		super();
 		
 		getContentPane().setLayout(null);
 		
 		//backgrnd = new MenuBackground(491,339,2);
 		JLabel lblVersion = new JLabel(Constants.NAVALBATTLE_VERSION_TITLE);
 		JLabel lblNavalBattle = new JLabel("NavalBattle");
 		JButton btnSingleplayer = new JButton("Singleplayer");
 		JButton btnHelp = new JButton("Help");
 		JButton btnRoketGamer = new JButton("RoketGamer");
 		JButton btnQuit = new JButton("Quit");
 		JButton btnCredits = new JButton("Credits");
 		JButton btnMultiplayer = new JButton("Multiplayer");
 		
		lblNavalBattle.setBounds(10, 13, 500, 51);
 		lblVersion.setBounds(10, 276, 238, 14);
		lblNavalBattle.setBounds(158, 13, 193, 51);
 		btnSingleplayer.setBounds(194, 73, 100, 25);
 		btnHelp.setBounds(194, 141, 100, 25);
 		btnRoketGamer.setBounds(194, 175, 100, 25);
 		btnQuit.setBounds(194, 209, 100, 25);
 		btnCredits.setBounds(389, 267, 76, 23);
 		btnMultiplayer.setBounds(194, 107, 100, 25);
 		
 		//backgrnd.setLocation(0, 0);
 		lblNavalBattle.setForeground(Color.blue);
 		lblNavalBattle.setFont(Helper.GUI_MENU_TITLE_FONT);
 		setFont(new Font("RingBearer", Font.BOLD, 35));
 		btnMultiplayer.setEnabled(false);
 		
 	//	setContentPane(backgrnd);
 		getContentPane().add(lblVersion);
 		getContentPane().add(lblNavalBattle);
 		getContentPane().add(btnSingleplayer);
 		getContentPane().add(btnHelp);
 		getContentPane().add(btnRoketGamer);
 		getContentPane().add(btnQuit);
 		getContentPane().add(btnCredits);
 		getContentPane().add(btnMultiplayer);
 		
 		btnSingleplayer.setFocusable(false);
 		btnMultiplayer.setFocusable(false);
 		btnHelp.setFocusable(false);
 		btnRoketGamer.setFocusable(false);
 		btnQuit.setFocusable(false);
 		btnCredits.setFocusable(false);
 		
 		btnSingleplayer.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				dispose();
 				new SPOptions();
 			}
 		});		
 		btnHelp.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				dispose();
 				new HelpWindow();
 			}
 		});
 		if(NavalBattle.getGameState().isOffline()) {
 			btnRoketGamer.setEnabled(false);
 		} else {
 			btnRoketGamer.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					dispose();
 					new RoketGamerWindow();
 				}
 			});
 		}
 		btnQuit.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				WindowCloser.close();
 			}
 		});
 		btnCredits.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				dispose();
 				new CreditsWindow();
 			}
 		});
 		btnMultiplayer.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printWarning("Multiplayer has not been implemented");
 			}
 		});
 		ActionListener listener = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 		//		backgrnd.tick();
 				ticks += 100;
 			//	backgrnd.invalidate();
 				repaint();
 			}
 		};
 		ticker = new Timer(100,listener);
 		ticker.start();
 	}
 }
