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
 
 package com.jpii.navalbattle.debug;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.gui.KeyboardListener;
 
 public class DebugWindow {
 	JFrame f;
 	private JLabel lblNavalBattle;
 	private JLabel lblDebugMode;
 	private JTextPane debugPrinter;
 	private JTextField commandField;
 
 	public DebugWindow() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 		}
 
 		f = new JFrame();
 		f.setResizable(false);
 		f.setTitle("NavalBattle");
 		f.getContentPane().setLayout(null);
 
 		lblNavalBattle = new JLabel("NavalBattle");
 		lblNavalBattle.setBounds(10, 11, 86, 14);
 		lblNavalBattle.setFont(new Font("Tahoma", Font.BOLD, 14));
 		f.getContentPane().add(lblNavalBattle);
 
 		lblDebugMode = new JLabel("Debug Mode");
 		lblDebugMode.setBounds(95, 13, 66, 14);
 		f.getContentPane().add(lblDebugMode);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(10, 35, 439, 255);
 		f.getContentPane().add(scrollPane);
 
 		debugPrinter = new JTextPane();
 		debugPrinter.setEditable(false);
 		scrollPane.setViewportView(debugPrinter);
 		
 		commandField = new JTextField();
 		commandField.setBounds(10, 301, 337, 23);
 		f.getContentPane().add(commandField);
 		commandField.setColumns(10);
 		
 		final JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(!commandField.getText().isEmpty()) {
 					submitCommand(commandField.getText());
 					commandField.setText("");
 				}
 			}
 		});
 		btnSubmit.setBounds(357, 301, 89, 23);
 		f.getContentPane().add(btnSubmit);
 
 		if(Constants.DEBUG_MODE) {
 			f.setSize(465,365);
 			f.setVisible(true);
 			f.setLocation(0,0);
 		} else {
 			f.setSize(475,340);
 			f.setVisible(false);
 			f.setLocation(0,0);
 		}
 
 		f.setFocusable(true);
 		f.addKeyListener(new KeyboardListener(this));
 		commandField.addKeyListener(new KeyboardListener(this));
 
 		f.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent we) {
 				NavalBattle.close();
 			}
 		});
 	}
 
 	/**
 	 * Prints a message without a tag. Next message does not go to a new line.
 	 * 
 	 * @param message
 	 */
 	public void print(String message) {
 		debugPrinter.setText(debugPrinter.getText() + message);
 	}
 
 	/**
 	 * Prints a message without a tag. Next message goes to a new line.
 	 * 
 	 * @param message
 	 */
 	public void println(String message) {
 		debugPrinter.setText(debugPrinter.getText() + message + "\n");
 	}
 
 	/**
 	 * Prints a message with an [INFO] tag.
 	 * 
 	 * @param message
 	 */
 	public void printInfo(String message) {
 		debugPrinter.setText(debugPrinter.getText() + "[INFO] " + message + "\n");
 	}
 
 	/**
 	 * Prints a message with a [WARN] tag.
 	 * 
 	 * @param message
 	 */
 	public void printWarning(String message) {
 		debugPrinter.setText(debugPrinter.getText() + "[WARN] " + message + "\n");
 	}
 
 	/**
 	 * Prints a message with an [ERROR] tag.
 	 * 
 	 * @param message
 	 */
 	public void printError(String message) {
 		debugPrinter.setText(debugPrinter.getText() + "[ERROR] " + message + "\n");
 	}
 
 	/**
 	 * Prints a message with a [OTHER] tag.
 	 * 
 	 * @param message
 	 */
 	public void printOther(String message) {
 		debugPrinter.setText(debugPrinter.getText() + "[OTHER] " + message + "\n");
 	}
 
 	/**
 	 * Get current JFrame.
 	 * 
 	 * @return JFrame
 	 */
 	public JFrame getFrame() {
 		return f;
 	}
 	
 	/**
 	 * Parses command.
 	 * 
 	 * @param command
 	 */
 	public void submitCommand(String command) {
 		NavalBattle.getCommandHandler().parseCommand(command);
 	}
 	
 	/**
 	 * Remotely grabs entered in <code>commandField</code> and parses.
 	 */
 	public void submitCommandRemote() {
 		if(!commandField.getText().isEmpty()) {
 			submitCommand(commandField.getText());
 			commandField.setText("");
 		}
 	}
 }
