 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.AcceptanceTest;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.TransactionHistory;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 /**
  * 
  * @author Pi 
  * @author Chris
  * @author Brian
  *
  */
 /**
  * @author paul
  * 
  */
 @SuppressWarnings("serial")
 public class EditRequirementPanel extends RequirementPanel {
 	private Requirement requirementBeingEdited;
 	private JButton buttonUpdate = new JButton("Update");
 	private JButton buttonCancel = new JButton("Cancel");
 	private JButton buttonModifyFromParent = new JButton("Add Child Requirement");
 	private JButton buttonClear = new JButton("Undo Changes");
 	private JButton buttonDelete = new JButton("Delete");
 	private JScrollPane historyScrollPane = new JScrollPane();
 	private SubrequirementPanel subRequirementPanel;
 	private boolean readyToClose = false;
 	private JTextArea noteMessage = new JTextArea();
 
 	/**
 	 * Constructor for a new requirement panel
 	 * 
 	 * @param req
 	 *            Model Local requirement model for containing data
 	 */
 	public EditRequirementPanel(Requirement req) {
 		super();
 
 		requirementBeingEdited = this.displayRequirement = req;
 		subRequirementPanel = new SubrequirementPanel(requirementBeingEdited);
 		GridBagLayout layout = new GridBagLayout();
 		contentPanel = new JPanel(layout);
 		GridBagConstraints c = new GridBagConstraints();
 
 		JPanel left = buildLeftPanel();
 		JPanel right = buildRightPanel();
 
 		JTabbedPane tabs = new JTabbedPane();
 		JPanel notes = buildNotePanel();
 		JPanel history = buildHistoryPanel();
 		JPanel tests = buildTestPanel();
 		tabs.add("Notes", notes);
 		tabs.add("Transaction History", history);
 		tabs.add("Acceptance Tests", tests);
 		tabs.add("Subrequirements", subRequirementPanel);
 
 		JPanel bottom = buildBottom();
 		c.gridx = 0; // Column 0
 		c.gridy = 0; // Row 0
 		c.weighty = 1; // Row is elastic
 		c.gridheight = 1;
 		contentPanel.add(left, c); // add left panel
 		
 		c.gridx = 1; // Column 1
 		contentPanel.add(right, c); // add right panel
 
 		c.gridx = 2; // Column 2
 		c.weightx = 1; // Column is elastic
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.BOTH; // Stretch contents
 		contentPanel.add(tabs, c); // add tabs
 
 		
 		c.fill = GridBagConstraints.NONE;
 		c.gridy = 1; // Row 1
 		c.gridx = 2; // Column 1
 		c.weighty = 0; // Row is not elastic
 		c.weightx = 0; // Column is not elastic
 		c.anchor = GridBagConstraints.LINE_END;
 		contentPanel.add(bottom, c); // Add bottom
 
 		
 		contentPanel.setMinimumSize(new Dimension(500, 465));
 		contentPanel.setPreferredSize(new Dimension(500, 465));
 
 		this.setViewportView(contentPanel);
 
 		fillFieldsForRequirement();
 	}
 
 	/**
 	 * Fills the fields of the edit requirement panel based on the current
 	 * settings of the edited requirement.
 	 */
 	@SuppressWarnings("unchecked")
 	private void fillFieldsForRequirement() {
 		getBoxName().setText(getRequirementBeingEdited().getName());
 		getBoxDescription().setText(
 				getRequirementBeingEdited().getDescription());
 		getBoxEstimate().setText(
 				String.valueOf(getRequirementBeingEdited().getEstimate()));
 		getBoxReleaseNum().setText(getRequirementBeingEdited().getRelease());
 		
 		if (getRequirementBeingEdited().getStatus().equals(RequirementStatus.NEW))
 		{
 			getDropdownStatus().removeAllItems();
 			getDropdownStatus().addItem(RequirementStatus.NEW);
 			getDropdownStatus().addItem(RequirementStatus.DELETED);
 		}
 		else if (getRequirementBeingEdited().getStatus().equals(RequirementStatus.INPROGRESS))
 		{
 			getDropdownStatus().removeAllItems();
 			getDropdownStatus().addItem(RequirementStatus.INPROGRESS);
 			getDropdownStatus().addItem(RequirementStatus.COMPLETE);
 			getDropdownStatus().addItem(RequirementStatus.DELETED);
 		}
 		else if (getRequirementBeingEdited().getStatus().equals(RequirementStatus.OPEN))
 		{
 			getDropdownStatus().removeAllItems();
 			getDropdownStatus().addItem(RequirementStatus.OPEN);
 			getDropdownStatus().addItem(RequirementStatus.DELETED);
 		}
 		else if (getRequirementBeingEdited().getStatus().equals(RequirementStatus.COMPLETE)
 				|| getRequirementBeingEdited().getStatus().equals(RequirementStatus.DELETED))
 		{
 			if (getRequirementBeingEdited().getIteration().equals("Backlog"))
 			{
 				getDropdownStatus().removeAllItems();
 				getDropdownStatus().addItem(RequirementStatus.OPEN);
 				getDropdownStatus().addItem(RequirementStatus.COMPLETE);
 				getDropdownStatus().addItem(RequirementStatus.DELETED);
 			}
 			else
 			{
 				getDropdownStatus().removeAllItems();
 				getDropdownStatus().addItem(RequirementStatus.INPROGRESS);
 				getDropdownStatus().addItem(RequirementStatus.COMPLETE);
 				getDropdownStatus().addItem(RequirementStatus.DELETED);
 			}
 		}
 		getDropdownStatus().setSelectedItem(
 				getRequirementBeingEdited().getStatus());
 		
 		if(requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS) getButtonDelete().setEnabled(false);
 		if(requirementBeingEdited.getStatus() == RequirementStatus.DELETED) disableComponents(); 
 		if(requirementBeingEdited.getEstimate() <= 0) getBoxIteration().setEnabled(false);
 		if(getRequirementBeingEdited().getEstimate() <= 0) getBoxIteration().setEnabled(false);
 		
 		getDropdownType()
 				.setSelectedItem(getRequirementBeingEdited().getType());
 		getBoxIteration().setText(
 				getRequirementBeingEdited().getIteration().toString());
 
         this.setPriorityDropdown(getRequirementBeingEdited().getPriority());
 
 		if (getRequirementBeingEdited().getStatus() == RequirementStatus.INPROGRESS
 				|| getRequirementBeingEdited().getStatus() == RequirementStatus.COMPLETE) {
 			getBoxEstimate().setEnabled(false);
 		} else {
 			getBoxEstimate().setEnabled(true);
 		}
 
 		if (requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS)
 			buttonDelete.setEnabled(false);
 		if (requirementBeingEdited.getStatus() == RequirementStatus.DELETED)
 			disableComponents();
 		if (requirementBeingEdited.getStatus() == RequirementStatus.COMPLETE)
 			this.getBoxIteration().setEnabled(false);
 		if (!(requirementBeingEdited.getEstimate() > 0))
 			getBoxIteration().setEnabled(false);
 		if (!(getRequirementBeingEdited().getEstimate() > 0))
 			getBoxIteration().setEnabled(false);
 
 		// reset the error messages.
 		this.getErrorEstimate().setText("");
 		getBoxEstimate().setBorder(defaultBorder);
 		this.getErrorDescription().setText("");
 		getBoxDescription().setBorder(defaultBorder);
 		this.getErrorName().setText("");
 		getBoxName().setBorder(defaultBorder);
 		this.buttonUpdate.setEnabled(false);
 		getButtonClear().setEnabled(false);
 		this.buttonModifyFromParent.setText("Attach To Parent");
 		if(getRequirementBeingEdited().getParentID() != -1)
 		{
 			this.buttonModifyFromParent.setText("Remove From Parent");
 			this.disableNonChildFields();
 		}
 		
 		repaint();
 	}
 
 	/**
 	 * Builds the right panel
 	 */
 	@Override
 	protected JPanel buildRightPanel() {
 		super.buildRightPanel();
 
 		return rightPanel;
 	}
 
 	public JPanel buildBottom() {
 		// setup the buttons
 		JPanel buttonPanel = new JPanel();
 
 		// Construct the add requirement controller and add it to the update
 		// button
 		getButtonUpdate().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (validateFields()) {
 					update();
 					readyToClose = true;
 					ViewEventController.getInstance().removeTab(EditRequirementPanel.this);
 				}
 			}
 		});
 
 		getButtonClear().addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillFieldsForRequirement();
 			}
 
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				readyToClose = true;
 				cancel();
 			}
 		});
 		
 		buttonDelete.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				deleteRequirement();
 			}
 		});
 
 		buttonModifyFromParent.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				if(requirementBeingEdited.getParentID() == -1)
 				{
 					//TODO: add to parent
 					buttonModifyFromParent.setText("Remove From Parent");
 				}
 				else
 				{
 					try {
 						requirementBeingEdited.setParentID(-1);
 						refreshEditPanel();
 						UpdateRequirementController.getInstance().updateRequirement(requirementBeingEdited);
 					} catch (Exception e1) {
 						System.out.println(e1.getMessage());
 					}
 					buttonModifyFromParent.setText("Attach To Parent");
 				}
 			}
 		});
 		buttonPanel.add(getButtonUpdate());
 		buttonPanel.add(getButtonClear());
 		buttonPanel.add(buttonModifyFromParent);
 		
 		buttonPanel.add(buttonDelete);
 		buttonPanel.add(buttonCancel);
 
 		return buttonPanel;
 	}
 
 	/**
 	 * Updates the requirement.
 	 */
 	public void update() {
 		// Extract the name, release number, and description from the GUI fields
 		String stringName = this.getBoxName().getText();
 		String stringReleaseNum = this.getBoxReleaseNum().getText();
 		String stringDescription = this.getBoxDescription().getText();
 		String stringEstimate = this.getBoxEstimate().getText();
 		String stringIteration = this.getBoxIteration().getText();
 
 		if (stringIteration.trim().equals(""))
 			stringIteration = "Backlog";
 
 		RequirementPriority priority;
 		RequirementStatus status;
 		RequirementType type = (RequirementType) getDropdownType()
 				.getSelectedItem();
 
 		int estimate = stringEstimate.trim().length() == 0 ? 0 : Integer
 				.parseInt(stringEstimate);
 		// Extract the status from the GUI
 		status = (RequirementStatus) this.getDropdownStatus().getSelectedItem();
 		// Extract which radio is selected for the priority
 		// If requirement deleted {}
 		// estimate = iteration.getEstimate()- estimate;
 		boolean stateHigh = getPriorityHigh().isSelected();
 		boolean stateMedium = getPriorityMedium().isSelected();
 		boolean stateLow = getPriorityLow().isSelected();
 
 		// Convert the priority string to its corresponding enum
 		if (stateHigh)
 			priority = RequirementPriority.HIGH;
 		else if (stateMedium)
 			priority = RequirementPriority.MEDIUM;
 		else if (stateLow)
 			priority = RequirementPriority.LOW;
 		else
 			priority = RequirementPriority.BLANK;
 
 		// Set to false to indicate the requirement is being newly created
 		boolean created = false;
 
 		// Set the time stamp so that all transaction messages from this update
 		// will have the same time stamp
 		TransactionHistory requirementHistory = requirementBeingEdited
 				.getHistory();
 		requirementHistory.setTimestamp(System.currentTimeMillis());
 
 		// Create a new requirement object based on the extracted info
 		if(getRequirementBeingEdited().getParentID() != -1)
 		{
 			getRequirementBeingEdited().setName(stringName);
 			getRequirementBeingEdited().setDescription(stringDescription);
 			getRequirementBeingEdited().setStatus(status, created);
 			getRequirementBeingEdited().setEstimate(estimate);
 		}
 		else
 		{
 			getRequirementBeingEdited().setName(stringName);
 			getRequirementBeingEdited().setRelease(stringReleaseNum);
 			getRequirementBeingEdited().setDescription(stringDescription);
 			getRequirementBeingEdited().setStatus(status, created);
 			getRequirementBeingEdited().setPriority(priority, created);
 			getRequirementBeingEdited().setEstimate(estimate);
 			getRequirementBeingEdited().setIteration(stringIteration, created);
 			getRequirementBeingEdited().setType(type);
 		}
 		
 		UpdateRequirementController.getInstance().updateRequirement(
 				getRequirementBeingEdited());
 
 		ViewEventController.getInstance().refreshTable();
 	}
 
 	/**
 	 * Constructs a panel with a scolling list of notes for the requirement, as
 	 * well as the elements to add new notes
 	 * 
 	 * @return panel for displaying and creating notes
 	 */
 	private JPanel buildNotePanel() {
 		// Buttons to be added to the bottom of the NotePanel
 		JButton buttonAddNote = new JButton("Add Note");
 		JButton buttonClear = new JButton("Clear");
 
 		// Create text area for note to be added
 		noteMessage.setLineWrap(true); // If right of box is reach, goes down a
 										// line
 		noteMessage.setWrapStyleWord(true); // Doesn't chop off words
 		
 		// Error message label in case no note was included
 		final JLabel errorMsg = new JLabel();
 
 		// Layout manager for entire note panel
 		GridBagLayout layout = new GridBagLayout();
 		JPanel panel = new JPanel(layout);
 		GridBagConstraints c = new GridBagConstraints();
 
 		// Layout manager for panel that contains the buttons
 		GridBagLayout bottomLayout = new GridBagLayout();
 		JPanel bottomPanel = new JPanel(bottomLayout);
 		GridBagConstraints bc = new GridBagConstraints();
 
 		// Create new scroll pane for notes
 		final JScrollPane scroll = new JScrollPane();
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		// Always show scroll bar
 
 		c.fill = GridBagConstraints.BOTH; // Fill grid cell with elements
 		c.weightx = 1; // Fill horizontal space
 		c.weighty = 1; // Fill all the vertical space
 		panel.add(scroll, c);
 
 		c.gridy = 1; // Row 1
 		c.weighty = .2; // Fill 0% of vertical space
 		panel.add(noteMessage,c);
 		
 		bc.anchor = GridBagConstraints.WEST; // Anchor buttons to west of bottom panel
 		bottomPanel.add(buttonAddNote, bc); // Include "Add note" button to bottom panel
 		
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
 		panel.add(bottomPanel, c); // Add buttons to the panel
 
 		// Set scroll pane to display notes associated with edited requirement
 		scroll.setViewportView(NotePanel.createList(this.requirementBeingEdited
 				.getNotes()));
 
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
 
 					// Add note to requirement
 					requirementBeingEdited.getNotes().add(msg);
 
 					// Update panel to show new note
 					scroll.setViewportView(NotePanel
 							.createList(requirementBeingEdited.getNotes()));
 
 					// Update database so requirement stores new note
 					UpdateRequirementController.getInstance()
 							.updateRequirement(requirementBeingEdited);
 				}
 			}
 		});
 
 		// Listener for the Clear button
 		buttonClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Clear all text fields
 				noteMessage.setText("");
 				errorMsg.setText("");
 			}
 		});
 
 		return panel;
 	}
 
 	/**
 	 * Builds the Transaction History Panel
 	 * 
 	 * @return The built panel for the transaction history tab
 	 */
 	private JPanel buildHistoryPanel() {
 		// Layout manager for transaction history panel
 		GridBagLayout layout = new GridBagLayout();
 		JPanel panel = new JPanel(layout);
 		GridBagConstraints c = new GridBagConstraints();
 
 		// Create scroll pane for window, set scroll bar to always be on
 		historyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		c.fill = GridBagConstraints.BOTH; // Fill grid cell with elements
 		c.weightx = 1; // Fill horizontal space
 		c.weighty = 0.8; // Fill 80% of vertical space
 		panel.add(historyScrollPane, c); // Add scroll pane to panel
 
 		// Show the requirement's transaction history in the scroll pane
 		historyScrollPane.setViewportView(HistoryPanel
 				.createList(this.requirementBeingEdited.getHistory()));
 
 		return panel;
 	}
 
 	private JPanel buildTestPanel() {
 		// Button used to add a test and update status
 		JButton buttonAddTest = new JButton("Add Test");
 
 		// Error message field
 		final JLabel error = new JLabel("");
 
 		// Create new scroll pane for notes
 		final JScrollPane scroll = new JScrollPane();
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		// Always show scroll bar
 
 		// Layout manager for acceptance test panel
 		GridBagLayout layout = new GridBagLayout();
 		JPanel panel = new JPanel(layout);
 		GridBagConstraints c = new GridBagConstraints();
 
 		// Layout manager for button panel
 		GridBagLayout bl = new GridBagLayout();
 		JPanel buttons = new JPanel(bl);
 		GridBagConstraints bc = new GridBagConstraints();
 
 		c.fill = GridBagConstraints.BOTH; // Fill grid cell with elements
 		c.anchor = GridBagConstraints.NORTH; // Anchor to top of panel
 		c.weightx = 1; // Fill horizontal space
 		c.weighty = 1; // Fill all the vertical space
 		panel.add(scroll, c); // Add scroll pane to panel
 
 		bc.anchor = GridBagConstraints.WEST; // Anchor to left
 		buttons.add(buttonAddTest, bc);
 
 		bc.gridx = 1; // Column 1
 		buttons.add(buttonUpdate, bc);
 
 		bc.gridx = 2; // Column 2
 		buttons.add(error, bc);
 
 		c.fill = GridBagConstraints.NONE; // Don't fill cell
 		c.anchor = GridBagConstraints.WEST; // Anchor to left of panel
 		c.gridy = 1; // Row 1
 		c.weighty = 0; // Do not stretch vertically
 		panel.add(buttons, c); // Add buttons to panel
 
 		JPanel tests = TestPanel.createList(this.requirementBeingEdited);
 		scroll.setViewportView(tests);
 
 		// Listener for addTest button
 		buttonAddTest.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JTextField title = new JTextField();
 				JTextArea description = new JTextArea(6, 6);
 				description.setWrapStyleWord(true);
 				description.setLineWrap(true);
 				JScrollPane dScroll = new JScrollPane(description);
 				dScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 				int response = 1;
 
 				// Options for dialog box
 				Object[] options = { "OK", "Cancel" };
 
 				final JComponent[] inputs = new JComponent[] {
 						new JLabel("Title"), title, new JLabel("Description"),
 						dScroll };
 				response = JOptionPane.showOptionDialog(null, inputs,
 						"Add Acceptance Test", JOptionPane.OK_CANCEL_OPTION,
 						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
 
 				if (response == 0) {
 					if (title.getText().length() <= 0) 
 					{
 						error.setText(" Title can not be blank");
 					} 
 					else if (title.getText().length() > 100) 
 					{
 						error.setText(" Title is too long: Max 100 characters");
 					} 
 					else 
 					{
 						error.setText("");
 						
 						// Set timestamp for transaction history
 						requirementBeingEdited.getHistory().setTimestamp(System.currentTimeMillis());
 						
 						int maxTestId = 0;
 						for (int i = 0; i < requirementBeingEdited.getTests().size(); i++) {
 							if (requirementBeingEdited.getTests().get(i).getId() > maxTestId) {
 								maxTestId = requirementBeingEdited.getTests().get(i).getId();
 							}
 						}
 						
 						// Add test to requirement
 						AcceptanceTest addTest = new AcceptanceTest(maxTestId + 1, title
 								.getText(), description.getText());
 						requirementBeingEdited.addTest(addTest);
 
 						// Update panel to show new test
 						scroll.setViewportView(TestPanel
 								.createList(requirementBeingEdited));
 						
 						// Update history panel
 						historyScrollPane.setViewportView(HistoryPanel
 								.createList(requirementBeingEdited.getHistory()));
 
 						// Update database so requirement stores new test
 						UpdateRequirementController.getInstance()
 								.updateRequirement(requirementBeingEdited);
 					}
 				}
 			}
 		});
 
 		return panel;
 	}
 
 	/**
 	 * Cancels the editing of the requirement.
 	 */
 	private void cancel() {
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);
 	}
 
 	public JButton getButtonUpdate() {
 		return buttonUpdate;
 	}
 
 	public JButton getButtonClear() {
 		return buttonClear;
 	}
 
 	public Requirement getRequirementBeingEdited() {
 		return requirementBeingEdited;
 	}
 
 	/**
 	 * Deletes the requirement. Sets all fields uneditable, sets status to
 	 * deleted and closes the tab.
 	 */
 	private void deleteRequirement() {
 		if (this.requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS)
 			return;
 
 		this.getDropdownStatus().setSelectedItem(RequirementStatus.DELETED);
 
 		requirementBeingEdited.setStatus(RequirementStatus.DELETED, false);
 
 		UpdateRequirementController.getInstance().updateRequirement(
 				requirementBeingEdited);
 
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);
 	}
 
 	/**
 	 * Disables all the components of the editing panel besides the status
 	 * dropdown.
 	 */
 	private void disableComponents() {
 		this.getBoxName().setEnabled(false);
 		this.getBoxDescription().setEnabled(false);
 		this.getBoxEstimate().setEnabled(false);
 		this.getBoxReleaseNum().setEnabled(false);
 		this.getDropdownType().setEnabled(false);
 		this.getBoxIteration().setEnabled(false);
 		this.getPriorityHigh().setEnabled(false);
 		this.getPriorityMedium().setEnabled(false);
 		this.getPriorityLow().setEnabled(false);
 		this.getPriorityBlank().setEnabled(false);
 		this.getButtonDelete().setEnabled(false);
 	}
 	
 	/**
 	 * Enables all of the components of the editing panel.
 	 */
 	private void enableComponents()
 	{
 		this.getBoxName().setEnabled(true);
 		this.getBoxDescription().setEnabled(true);
 		this.getBoxEstimate().setEnabled(true);
 		this.getBoxReleaseNum().setEnabled(true);
 		this.getDropdownType().setEnabled(true);
 		if(requirementBeingEdited.getEstimate() > 0) this.getBoxIteration().setEnabled(true);
 		this.getPriorityHigh().setEnabled(true);
 		this.getPriorityMedium().setEnabled(true);
 		this.getPriorityLow().setEnabled(true);
 		this.getPriorityBlank().setEnabled(true);
 		this.getButtonDelete().setEnabled(true);		
 	}
 	
 	/**
 	 * Returns whether the panel is ready to be removed or not based on if there are changes that haven't been
 	 * saved.
 	 * 
 	 * @return whether the panel can be removed.
 	 */
 	public boolean readyToRemove()
 	{
 		if(readyToClose) return true;
 		if(anythingChanged())
 		{
 			int result = JOptionPane.showConfirmDialog(this, "Discard unsaved changes and close tab?", "Discard Changes?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 			
 			return result == 0;
 		}
 		else
 		{
 			return true;
 		}
 	}
 	
 	/**
 	 * Returns whether any field in the panel has been changed
 	 */
 	public boolean anythingChanged()
 	{
 		// Check if the user has changed the name
 		if (!(getBoxName().getText().equals(requirementBeingEdited.getName()))){
 			return true;}
 		// Check if the user has changed the description
 		if (!(getBoxDescription().getText().equals(requirementBeingEdited.getDescription()))){
 			return true;}
 		// Check if the user has changed the release number
 		if (!(getBoxReleaseNum().getText().equals(requirementBeingEdited.getRelease()))){
 			return true;}
 		// Check if the user has changed the iteration number
 		if (!(getBoxIteration().getText().equals(requirementBeingEdited.getIteration()))){
 			return true;}
 		// Check if the user has changed the type
 		if (!(((RequirementType)getDropdownType().getSelectedItem()) == requirementBeingEdited.getType())){
 			return true;}
 		// Check if the user has changed the status
 		if (!(((RequirementStatus)getDropdownStatus().getSelectedItem()) == requirementBeingEdited.getStatus())){
 			return true;}
 		// Check if the user has changed the estimate
 		if (!(getBoxEstimate().getText().trim().equals(String.valueOf(requirementBeingEdited.getEstimate())))){
 			return true;}
 
 		RequirementPriority reqPriority = requirementBeingEdited.getPriority();
 		boolean priorityChanged = false;
 		switch(reqPriority)
 		{
 			case BLANK:
 				priorityChanged = !getPriorityBlank().isSelected();
 				break;
 			case LOW:
 				priorityChanged = !getPriorityLow().isSelected();
 				break;
 			case MEDIUM:
 				priorityChanged = !getPriorityMedium().isSelected();
 				break;
 			case HIGH:
 				priorityChanged = !getPriorityHigh().isSelected();
 				break;
 		}
 		if (priorityChanged){
 			return true;
 		}
 		
 		// Check if the user has entered anything into the note panel
 		if (noteMessage.getText().length()>0){
 			return true;}
 		
 		return false;
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {}
 
 	@Override
 	public void keyPressed(KeyEvent e) {}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		this.buttonUpdate.setEnabled(anythingChanged());	
 		this.buttonClear.setEnabled(anythingChanged());
 		
 		
 		//check that estimate is valid to enable iterations.
 		boolean validEstimate = true;
 		
 		try
 		{
 			int estimate = Integer.parseInt(getBoxEstimate().getText().trim());
 			validEstimate = estimate > 0;
 		}
 		catch (Exception ex)
 		{
 			validEstimate = false;
 		}
 		
 		this.getBoxIteration().setEnabled(validEstimate);
 		if(getRequirementBeingEdited().getParentID() != -1) disableNonChildFields();
 
 		this.repaint();		
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		this.buttonUpdate.setEnabled(getBoxName().getText().trim().length() > 0 && getBoxDescription().getText().trim().length() > 0);
 		this.buttonClear.setEnabled(anythingChanged());		
 		
 		if(getDropdownStatus().getSelectedItem() != RequirementStatus.DELETED)
 		{
 			enableComponents();
 		}
 		else
 		{
 			disableComponents();
 		}
 		
 		if(getDropdownStatus().getSelectedItem() == RequirementStatus.COMPLETE || getDropdownStatus().getSelectedItem() == RequirementStatus.DELETED)
 		{
 			this.subRequirementPanel.enableChildren(false);
 			this.buttonModifyFromParent.setEnabled(false);
 		}
 		else
 		{
 			this.subRequirementPanel.enableChildren(true);
 			this.buttonModifyFromParent.setEnabled(true);
 		}
 		
 		if(getRequirementBeingEdited().getParentID() != -1) disableNonChildFields();
 
 		this.repaint();
 	}
 
 	public JButton getButtonCancel() {
 		return buttonCancel;
 	}
 
 	public JButton getButtonDelete() {
 		return buttonDelete;
 	}
 	/**
 	 * Refreshes the the parent when a newChild is found
 	 */
 	public void refreshEditPanel() {
 		if(requirementBeingEdited.getParentID() != -1)
 		{
 			parent.setText("Child of \""+displayRequirement.getParent().getName()+"\"");
			buttonModifyFromParent.setText("Remove From Parent");
 			parent.setVisible(true);
 		}
 		
 		else
 		{
			buttonModifyFromParent.setText("Attach To Parent");
 			parent.setVisible(false);
 		}
 	}
 
 }
