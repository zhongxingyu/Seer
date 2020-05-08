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
 
 package com.jpii.gamekit.debug;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class DebugWindow extends JFrame {
 	private JLabel lblTitle;
 	private JTextPane debugPrinter;
 	private JTextField commandField;
 	private CommandHandler commandHandler;
 	
 	/**
 	 * Constructor for DebugWindow.
 	 */
 	public DebugWindow(String title) {
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		setTitle("Debug Window");	
 		setSize(489,369);
 		getContentPane().setLayout(null);
 
 		commandHandler = new CommandHandler(DefaultCommands.COMMANDS);
 		
 		lblTitle = new JLabel(title + " Debug Mode");
 		JScrollPane scrollPane = new JScrollPane();
 		setDebugPrinter(new JTextPane());
 		commandField = new JTextField();
 		final JButton btnSubmit = new JButton("Submit");
 		
 		lblTitle.setBounds(10, 11, 464, 17);
 		scrollPane.setBounds(10, 35, 464, 255);
 		commandField.setBounds(10, 298, 364, 30);
 		btnSubmit.setBounds(384, 298, 90, 30);
 		
 		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
 		getDebugPrinter().setFont(new Font("Consolas",0,12));
 		getDebugPrinter().setEditable(false);
 		scrollPane.setViewportView(getDebugPrinter());
 		commandField.setColumns(10);
 		btnSubmit.setFocusable(false);
 		getDebugPrinter().setFocusable(false);
 		
 		getContentPane().add(lblTitle);
 		getContentPane().add(scrollPane);
 		getContentPane().add(commandField);
 		getContentPane().add(btnSubmit);
 
 		btnSubmit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(!commandField.getText().isEmpty()) {
 					submitCommand();
 					commandField.setText("");
 				}
 			}
 		});
 		
 		commandField.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
 					if(!commandField.getText().isEmpty()) {
 						submitCommand();
 						commandField.setText("");
 					}
 				}
 			}	
 		});
 		
 		setResizable(false);
 		
 		commandField.grabFocus();
 	}
 
 	/**
 	 * Parses command.
 	 * @param command
 	 */
 	public void submitCommand(String s) {
 		commandHandler.parseCommand(s);
 	}
 	
 	public void submitCommand() {
 		if(!commandField.getText().isEmpty()) {
 			commandHandler.parseCommand(commandField.getText());
 			commandField.setText("");
 		}
 	}
 
 	/**
 	 * Get current JFrame.
 	 * @return JFrame
 	 */
 	public JFrame getFrame() {
 		return this;
 	}
 
 	public JTextPane getDebugPrinter() {
 		return debugPrinter;
 	}
 
 	public void setDebugPrinter(JTextPane debugPrinter) {
 		this.debugPrinter = debugPrinter;
 	}
 	
 	public CommandHandler getCommandHandler() {
 		return commandHandler;
 	}
 }
