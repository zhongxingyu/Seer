/*******************************************************************************
 * Copyright (c) 2013 WPI-Suite
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
  * 
 * Contributors: Team Rolling Thunder
 ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.overview;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 /**
  * @author Raphael
  *
  */
 public class OverviewTableCellEditor extends DefaultCellEditor {
 
 	/** 
 	 *  Contructor for the OverviewTableCellEditor
 	 *  
 	 *  This alternate editor is used to address text that is entered into the OverviewTable cells
 	 *  in Mutliple Requirement Editing mode
 	 *  
 	 * @param textField
 	 */
 	public OverviewTableCellEditor(JTextField textField) {
 		super(textField);	
 	}
 
 	@Override
 	/**
 	 * Returns the table cell editor so that the table can edit the cells text fields
 	 */
 	public Component getTableCellEditorComponent(JTable table, Object value, final boolean isSelected, int row, int column) {
 		// extract the cell's text field
 		final JTextField c = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
 		
 		c.setBackground(Color.white);
 		
 		// add a key listener to the cell's text field
 		// this will color the text field correctly when the text field is edited
 		c.addKeyListener(new KeyListener() {	
 
 			@Override
 			// fire when a key on the keyboard is released
 			public void keyReleased(KeyEvent e) {
 				// extract the text from the text field, trimming beginning and ending spaces 
 				String cellText = c.getText().trim();
 				boolean formatError = false;
 				int cellInt = 0;
 				// attempt to parse the text to an integer 
 				try {
 					cellInt = Integer.parseInt(cellText);
 				}
 				// set formatError to true if the text was not able to be parsed to an integer and is not null
 				catch (NumberFormatException nfe){
 					if (!cellText.isEmpty()) formatError = true;
 				}
 				// if the text could not be parsed
 				if (formatError) {
 					// highlight the text field in red and add a tool tip
 					c.setBackground(Color.red);	 
 					c.setToolTipText("Estimate must be a valid, non-negative integer.");
 				}
 				// if the text is a valid integer, return the text field background to white and remove the tool tip 
 				else {					
 					c.setBackground(Color.white);
 					c.setToolTipText(null);
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 
 			}
 
 
 		});
 
 		return c;
 
 	}
 
 }
