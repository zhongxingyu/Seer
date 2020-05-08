 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.tabs;
 
 import java.awt.Color;
import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.Border;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.ViewMode;
 
 /**
  */
 public class RequirementNotePanel extends JPanel implements RequirementPanelListener {
 	private final ViewMode viewMode;
 	private final Requirement currentRequirement;
 	private int notesAdded;
 	private final JTextArea noteMessage = new JTextArea();
 	private final JScrollPane noteScroll;
 	private final JButton buttonAddNote;
 	private final JButton buttonClear;
 	private final JLabel errorMsg;
 	
 	/**
 	 * Constructor for the requirement note panel
 	 * @param parent parent panel
 	 * @param vm view mode
 	 * @param current current requirement
 	 */
 	public RequirementNotePanel(RequirementTabsPanel parent, ViewMode vm, Requirement current) {
 		currentRequirement = current;
 		viewMode = vm;
 		notesAdded = 0;
 		
		Component noteField = this.buildNoteField();
 		noteScroll = new JScrollPane();
 
 		// Buttons to be added to the bottom of the NotePanel
 		buttonAddNote = new JButton("Add Note");
 		buttonClear = new JButton("Clear");
 		buttonAddNote.setEnabled(false);
 		buttonClear.setEnabled(false);
 
 		// Error message label in case no note was included
 		errorMsg = new JLabel();
 
 		// Layout manager for entire note panel
 		final GridBagLayout layout = new GridBagLayout();
 		this.setLayout(layout);
 		final GridBagConstraints c = new GridBagConstraints();
 
 		// Layout manager for panel that contains the buttons
 		final GridBagLayout bottomLayout = new GridBagLayout();
 		JPanel bottomPanel = new JPanel(bottomLayout);
 		final GridBagConstraints bc = new GridBagConstraints();
 
 		// Create new scroll pane for notes
 		noteScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		// Always show scroll bar
 		noteScroll.setMinimumSize(new Dimension(100,100));
 
 		c.fill = GridBagConstraints.BOTH; // Fill grid cell with elements
 		c.weightx = 1; // Fill horizontal space
 		c.weighty = 1; // Fill all the vertical space
 		this.add(noteScroll, c);
 
 		c.gridy = 1; // Row 1
 		c.weighty = 0; // Fill 0% of vertical space
		this.add(noteField, c);
 
 		bc.anchor = GridBagConstraints.WEST; // Anchor buttons to west of bottom
 												// panel
 		bottomPanel.add(buttonAddNote, bc); // Include "Add note" button to
 											// bottom panel
 
 		bc.gridx = 1; // Column 1
 		bottomPanel.add(buttonClear, bc); // Include "Clear" button to bottom
 											// panel
 
 		bc.gridx = 2; // Column 2
 		bottomPanel.add(errorMsg, bc); // Add error message label to bottom
 										// panel
 
 		c.weighty = 0; // Do not stretch
 		c.gridy = 2; // Row 2
 		c.fill = GridBagConstraints.NONE; // Do not fill cell
 		c.anchor = GridBagConstraints.WEST; // Anchor buttons to west of panel
 		this.add(bottomPanel, c); // Add buttons to the panel
 		
 		this.setupListeners();
 		this.refresh();
 	}
 	
 	/**
 	 * Refreshes the note panel
 	 */
 	private void refresh()
 	{
 		noteScroll.setViewportView(SingleNotePanel.createList(currentRequirement.getNotes()));
 	}
 	
 	/**
 	 * Sets up the listeners 
 	 */
 	private void setupListeners()
 	{
 		// Listener for add note button
 		buttonAddNote.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Display error message if there is no text in noteMessage
 				if (noteMessage.getText().length() <= 0) {
 					errorMsg.setText(" Error: Must add text to create note.");
 				} else {
 
 					String msg = noteMessage.getText(); // Get text from
 														// noteMessage
 
 					// Clear all text areas
 					noteMessage.setText("");
 					errorMsg.setText("");
 					buttonClear.setEnabled(false);
 					buttonAddNote.setEnabled(false);
 
 					// Add note to requirement
 					currentRequirement.getNotes().add(msg);
 
 					refresh();
 					notesAdded++;
 					// Update database so requirement stores new note
 					UpdateRequirementController.getInstance()
 							.updateRequirement(currentRequirement);
 				}
 			}
 		});
 
 		// Listener for the Clear button
 		buttonClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Clear all text fields
 				noteMessage.setText("");
 				errorMsg.setText("");
 				buttonClear.setEnabled(false);
 				buttonAddNote.setEnabled(false);
 			}
 		});
 	}
 
 	/**
 	 * Method buildNoteMessage.
 	 * @return JTextArea
 	 */
	private Component buildNoteField(){		
 		noteMessage.addKeyListener(new KeyAdapter()
 		{
 			@Override
 			public void keyReleased(KeyEvent e)
 			{
 				boolean enabledButtons = !noteMessage.getText().trim().isEmpty();
 				buttonAddNote.setEnabled(enabledButtons);
 				buttonClear.setEnabled(enabledButtons);
 			}
 		});
 
 		// Create text area for note to be added
 		noteMessage.setLineWrap(true); // If right of box is reach, goes down a
 										// line
 		noteMessage.setWrapStyleWord(true); // Doesn't chop off words
 		noteMessage.setMinimumSize(new Dimension(50,50));
 		noteMessage.setSize(new Dimension(100,100));
 		Border b = BorderFactory.createCompoundBorder(
 				BorderFactory.createLineBorder(Color.GRAY), 
 	            BorderFactory.createEmptyBorder(4, 4, 4, 4));
 		noteMessage.setBorder(b);
 		
		JScrollPane scroller = new JScrollPane(noteMessage);
		scroller.setMinimumSize(new Dimension(50,50));
		scroller.setPreferredSize(new Dimension(60,60));
		
		return scroller;
 	}
 
 	/**
 	 * Method readyToRemove.
 	 * @return boolean
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#readyToRemove()
 	 */
 	@Override
 	public boolean readyToRemove() {
 		return noteMessage.getText().length() == 0 && (notesAdded == 0 || viewMode == ViewMode.EDITING);
 	}
 
 	/**
 	 * Method fireDeleted.
 	 * @param b boolean
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireDeleted(boolean)
 	 */
 	@Override
 	public void fireDeleted(boolean b) {
 	}
 
 	/**
 	 * Method fireValid.
 	 * @param b boolean
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireValid(boolean)
 	 */
 	@Override
 	public void fireValid(boolean b) {		
 	}
 
 	/**
 	 * Method fireChanges.
 	 * @param b boolean
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireChanges(boolean)
 	 */
 	@Override
 	public void fireChanges(boolean b) {		
 	}
 
 	/**
 	 * Method fireRefresh.
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireRefresh()
 	 */
 	@Override
 	public void fireRefresh() {
 		this.refresh();
 	}
 	
 	/**
 	
 	 * @return the note message text area */
 	public JTextArea getNoteMessage() {
 		return noteMessage;
 	}
 	
 	/**
 	
 	 * @return the button for adding a note */
 	public JButton getAddNoteButton() {
 		return buttonAddNote;
 	}
 	
 	/**
 	
 	 * @return the requirement this note panel is in */
 	public Requirement getRequirement() {
 		return currentRequirement;
 	}
 	
 	/**
 	
 	 * @return the number of notes added */
 	public int getNotesAdded() {
 		return notesAdded;
 	}
 
 	/**
 	 * Method getClearButton.
 	 * @return JButton
 	 */
 	public JButton getClearButton() {
 		return buttonClear;
 	}
 
 	/**
 	 * Method getErrorMsg.
 	 * @return JLabel
 	 */
 	public JLabel getErrorMsg() {
 		return errorMsg;
 	}
 }
