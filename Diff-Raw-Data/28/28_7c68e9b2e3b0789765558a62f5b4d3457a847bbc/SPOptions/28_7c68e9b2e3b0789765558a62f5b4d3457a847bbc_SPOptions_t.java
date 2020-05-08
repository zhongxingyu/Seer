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
 import com.jpii.navalbattle.game.*;
 
 
 @SuppressWarnings("serial")
 public class SPOptions extends Window{
 	JRadioButton radioButton;
 	JRadioButton radioButton_1;
 	JRadioButton radioButton_2;
 	JRadioButton radioButton_3;
 	JRadioButton radioButton_4;
 	JRadioButton radioButton_5;
 	JRadioButton radioButton_6;
 	JRadioButton radioButton_7;
 	JRadioButton radioButton_8;
 	JRadioButton radioButton_9;
 	JRadioButton rdbtnWater;
 	JRadioButton rdbtnWater_1;
 	JRadioButton rdbtnWater_2;
 	
 	public SPOptions() {
 		getContentPane().setLayout(null);
 		
 		JButton btnOpenSave = new JButton("Open Save");
 		JButton btnBack = new JButton("Back");
 		JButton btnAdvancedOptions = new JButton("Advanced Options");
 		
		btnOpenSave.setBounds(201, 7, 90, 30);
		btnAdvancedOptions.setBounds(331, 7, 150, 30);
		btnBack.setBounds(33, 7, 90, 30);
		
		btnOpenSave.setFocusable(false);
		btnBack.setFocusable(false);
		btnAdvancedOptions.setFocusable(false);
 		
 		getContentPane().add(btnOpenSave);
 		getContentPane().add(btnBack);
 		getContentPane().add(btnAdvancedOptions);
 		
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		JSeparator separator = new JSeparator();
 		JSeparator separator_1 = new JSeparator();
 		JSeparator separator_2 = new JSeparator();
 		JSeparator separator_3 = new JSeparator();
 		JSeparator separator_4 = new JSeparator();
 		JSeparator separator_5 = new JSeparator();
 		JSeparator separator_6 = new JSeparator();
 		JLabel lblNewGame = new JLabel("New Game");
 		
 		separator.setBounds(0, 42, 492, 5);
 		separator_1.setBounds(0, 43, 492, 5);
 		separator_2.setBounds(0, 44, 492, 5);
 		separator_3.setBounds(0, 45, 492, 5);
 		separator_4.setBounds(0, 239, 492, 5);
 		separator_5.setBounds(164, 44, 5, 197);
 		separator_6.setBounds(328, 43, 5, 197);
 		lblNewGame.setBounds(196, 120, 100, 35);
 		
 		lblNewGame.setAlignmentX(CENTER_ALIGNMENT);
 		separator_5.setOrientation(SwingConstants.VERTICAL);
 		separator_6.setOrientation(SwingConstants.VERTICAL);
 		lblNewGame.setFont(new Font("Tahoma", Font.PLAIN, 20));
 		
 		getContentPane().add(separator);
 		getContentPane().add(separator_1);
 		getContentPane().add(separator_2);
 		getContentPane().add(separator_3);
 		getContentPane().add(separator_4);
 		getContentPane().add(separator_5);
 		getContentPane().add(separator_6);
 		getContentPane().add(lblNewGame);
 		
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		ButtonGroup water = new ButtonGroup();
 		
 		rdbtnWater = new JRadioButton("60% Water");
 		rdbtnWater_1 = new JRadioButton("70% Water");
 		rdbtnWater_2 = new JRadioButton("80% Water");
 		
 		rdbtnWater.setBounds(36, 114, 84, 23);
 		rdbtnWater_1.setBounds(36, 155, 109, 23);
 		rdbtnWater_2.setBounds(36, 193, 109, 23);
 		
 		water.add(rdbtnWater);
 		water.add(rdbtnWater_1);
 		water.add(rdbtnWater_2);
 		
 		getContentPane().add(rdbtnWater);
 		getContentPane().add(rdbtnWater_1);
 		getContentPane().add(rdbtnWater_2);
 		
 		rdbtnWater.setSelected(true);
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		ButtonGroup difficulty = new ButtonGroup();
 		
 		radioButton = new JRadioButton("");
 		radioButton_1 = new JRadioButton("");
 		radioButton_2 = new JRadioButton("");
 		radioButton_3 = new JRadioButton("");
 		radioButton_4 = new JRadioButton("");
 		radioButton_5 = new JRadioButton("");
 		
 		radioButton.setBounds(75, 255, 60, 23);
 		radioButton_1.setBounds(130, 255, 60, 23);
 		radioButton_2.setBounds(180, 255, 60, 23);
 		radioButton_3.setBounds(235, 255, 60, 23);
 		radioButton_4.setBounds(290, 255, 60, 23);
 		radioButton_5.setBounds(355, 255, 60, 23);
 		
 		radioButton.setHorizontalAlignment(SwingConstants.CENTER);
 		radioButton_1.setHorizontalAlignment(SwingConstants.CENTER);
 		radioButton_2.setHorizontalAlignment(SwingConstants.CENTER);
 		radioButton_3.setHorizontalAlignment(SwingConstants.CENTER);
 		radioButton_4.setHorizontalAlignment(SwingConstants.CENTER);
 		radioButton_5.setHorizontalAlignment(SwingConstants.CENTER);	
 		
 		difficulty.add(radioButton);
 		difficulty.add(radioButton_1);
 		difficulty.add(radioButton_2);
 		difficulty.add(radioButton_3);
 		difficulty.add(radioButton_4);
 		difficulty.add(radioButton_5);
 		
 		getContentPane().add(radioButton);
 		getContentPane().add(radioButton_1);
 		getContentPane().add(radioButton_2);
 		getContentPane().add(radioButton_3);
 		getContentPane().add(radioButton_4);
 		getContentPane().add(radioButton_5);
 		
 		radioButton.setSelected(true);
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		JLabel lblPioneer = new JLabel("Pioneer");
 		JLabel lblSeawolf = new JLabel("Seawolf");
 		JLabel lblSentry = new JLabel("Sentry");
 		JLabel lblFirebolt = new JLabel("Firebolt");
 		JLabel lblDefender = new JLabel("Defender");
 		JLabel lblBattleship = new JLabel("Battleship");
 		JLabel lblDifficulty = new JLabel("Difficulty");
 		JLabel lblEasiest = new JLabel("Easiest");
 		JLabel lblHardest = new JLabel("Hardest");
 		JLabel lblAmmountOf = new JLabel("Ammount of");
 		JLabel lblLandmass = new JLabel("Landmass");
 		JLabel lblNumberOf = new JLabel("Number of");
 		JLabel lblOpponets = new JLabel("Opponents");
 		
 		lblPioneer.setHorizontalAlignment(SwingConstants.CENTER);
 		lblSeawolf.setHorizontalAlignment(SwingConstants.CENTER);
 		lblSentry.setHorizontalAlignment(SwingConstants.CENTER);
 		lblFirebolt.setHorizontalAlignment(SwingConstants.CENTER);
 		lblDefender.setHorizontalAlignment(SwingConstants.CENTER);
 		lblBattleship.setHorizontalAlignment(SwingConstants.CENTER);
 		lblDifficulty.setHorizontalAlignment(SwingConstants.CENTER);
 		lblEasiest.setHorizontalAlignment(SwingConstants.CENTER);
 		lblHardest.setHorizontalAlignment(SwingConstants.CENTER);
 		lblAmmountOf.setHorizontalAlignment(SwingConstants.CENTER);
 		lblLandmass.setHorizontalAlignment(SwingConstants.CENTER);
 		lblNumberOf.setHorizontalAlignment(SwingConstants.CENTER);
 		lblOpponets.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		lblPioneer.setBounds(75, 276, 60, 14);
 		lblSeawolf.setBounds(130, 276, 60, 14);
 		lblSentry.setBounds(180, 276, 60, 14);
 		lblFirebolt.setBounds(235, 276, 60, 14);
 		lblDefender.setBounds(290, 276, 60, 14);
 		lblBattleship.setBounds(355, 276, 60, 14);
 		lblDifficulty.setBounds(216, 240, 60, 14);
 		lblEasiest.setBounds(20, 270, 60, 14);
 		lblHardest.setBounds(412, 270, 60, 14);
 		lblAmmountOf.setBounds(28, 59, 100, 14);
 		lblLandmass.setBounds(40, 82, 76, 14);
 		lblNumberOf.setBounds(371, 58, 70, 14);
 		lblOpponets.setBounds(369, 81, 76, 16);
 		
 		lblAmmountOf.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblLandmass.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblNumberOf.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		lblOpponets.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		
 		getContentPane().add(lblPioneer);
 		getContentPane().add(lblSeawolf);
 		getContentPane().add(lblSentry);
 		getContentPane().add(lblFirebolt);
 		getContentPane().add(lblDefender);
 		getContentPane().add(lblBattleship);
 		getContentPane().add(lblDifficulty);
 		getContentPane().add(lblEasiest);
 		getContentPane().add(lblHardest);
 		getContentPane().add(lblAmmountOf);
 		getContentPane().add(lblLandmass);
 		getContentPane().add(lblNumberOf);
 		getContentPane().add(lblOpponets);
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		ButtonGroup opponents = new ButtonGroup();
 		
 		radioButton_6 = new JRadioButton("3");
 		radioButton_7 = new JRadioButton("4");
 		radioButton_8 = new JRadioButton("5");
 		radioButton_9 = new JRadioButton("6");
 		JButton btnNewButton = new JButton("Start");
 		
 		radioButton_6.setBounds(386, 116, 40, 25);
 		radioButton_7.setBounds(386, 142, 40, 25);
 		radioButton_8.setBounds(386, 167, 40, 25);
 		radioButton_9.setBounds(386, 193, 40, 25);
		btnNewButton.setBounds(201, 155, 90, 30);
 		
 		opponents.add(radioButton_6);
 		opponents.add(radioButton_7);
 		opponents.add(radioButton_8);
 		opponents.add(radioButton_9);
 		
		radioButton_6.setSelected(true);
		btnNewButton.setFocusable(false);
		
 		getContentPane().add(radioButton_6);
 		getContentPane().add(radioButton_7);
 		getContentPane().add(radioButton_8);
 		getContentPane().add(radioButton_9);
		getContentPane().add(btnNewButton);		
 		
 		btnNewButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				saveOptions();
 				NavalBattle.getDebugWindow().printInfo("Saving Data...");
 				NavalBattle.getDebugWindow().printInfo("NumShips: " + NavalBattle.getGameState().getNumShips());
 				NavalBattle.getDebugWindow().printInfo("WaterLevel: " + NavalBattle.getGameState().getWaterLevel());
 				NavalBattle.getDebugWindow().printInfo("Difficulty: " + NavalBattle.getGameState().getDifficulty());
 				
 				dispose();
 				NavalBattle.getDebugWindow().printInfo("Opening SinglePlayerGame");
 				new SinglePlayerGame();
 			}
 		});
 		
 		
 		btnBack.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				nextWindow("MainMenuWindow");
 			}
 		});
 		btnAdvancedOptions.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 		});
 		
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
 	}
 	
 	private void saveOptions() {
 
 		if(rdbtnWater.isSelected()) {
 			NavalBattle.getGameState().setWaterLevel(60);
 		}
 		else if(rdbtnWater_1.isSelected()) {
 			NavalBattle.getGameState().setWaterLevel(70);
 		}
 		else if(rdbtnWater_2.isSelected()) {
 			NavalBattle.getGameState().setWaterLevel(80);
 		}
 		else {
 			NavalBattle.getDebugWindow().printError("No water level selected");
 		}
 		
 		if(radioButton_6.isSelected()) {
 			NavalBattle.getGameState().setNumShips(Integer.parseInt(radioButton_6.getText()));
 		}
 		else if(radioButton_7.isSelected()) {
 			NavalBattle.getGameState().setNumShips(Integer.parseInt(radioButton_7.getText()));
 		}
 		else if(radioButton_8.isSelected()) {
 			NavalBattle.getGameState().setNumShips(Integer.parseInt(radioButton_8.getText()));
 		}
 		else if(radioButton_9.isSelected()) {
 			NavalBattle.getGameState().setNumShips(Integer.parseInt(radioButton_9.getText()));
 		}
 		else {
 			NavalBattle.getDebugWindow().printError("No number of Ships selected");
 		}
 		
 		if(radioButton.isSelected()) {
 			NavalBattle.getGameState().setDifficulty(1);
 		}
 		else if(radioButton_1.isSelected()) {
 			NavalBattle.getGameState().setDifficulty(2);
 		}
 		else if(radioButton_2.isSelected()) {
 			NavalBattle.getGameState().setDifficulty(3);
 		}
 		else if(radioButton_3.isSelected()) {
 			NavalBattle.getGameState().setDifficulty(4);
 		}
 		else if(radioButton_4.isSelected()) {
 			NavalBattle.getGameState().setDifficulty(5);
 		}
 		else if(radioButton_5.isSelected()) {
 			NavalBattle.getGameState().setDifficulty(6);
 		}
 		
 	}
 }
