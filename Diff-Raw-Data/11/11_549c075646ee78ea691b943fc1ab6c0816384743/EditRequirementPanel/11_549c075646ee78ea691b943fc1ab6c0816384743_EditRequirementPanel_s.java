 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.Note;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.NoteList;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.Transaction;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.TransactionHistory;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
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
 public class EditRequirementPanel extends RequirementPanel 
 {	
 	private Requirement requirementBeingEdited;
 	private JButton buttonUpdate = new JButton("Update");
 	private JButton buttonCancel = new JButton("Cancel");
 	private JButton buttonAddChild = new JButton("Add Child Requirement");
 	private JButton buttonClear = new JButton("Undo Changes");
 	private JButton buttonDelete = new JButton("Delete");
 	
 	/**
 	 * Constructor for a new requirement panel
 	 * @param req	Model Local requirement model for containing data
 	 */
 	public EditRequirementPanel(Requirement req) {
 		super();
 
 		requirementBeingEdited = req;
 		GridBagLayout layout = new GridBagLayout();
 		contentPanel = new JPanel(layout);
 		GridBagConstraints c = new GridBagConstraints();
 		
 		
 		JPanel left = buildLeftPanel();
 		JPanel right = buildRightPanel();
 		
 		JTabbedPane tabs = new JTabbedPane();
 		JPanel notes = buildNotePanel();
 		JPanel history = buildHistoryPanel();
 		tabs.add("Notes", notes);
 		tabs.add("Transaction History", history);
 		
 		JPanel bottom = buildBottom();
 		
 		c.gridx = 0; // Column 0
 		c.gridy = 0; // Row 0
 		c.weighty = 1; // Row is elastic
 		c.gridheight = 2;
 		contentPanel.add(left,c); //add left panel
 		
 		c.gridx = 1; // Column 1
 		contentPanel.add(right,c); //add right panel
 		
 		c.gridx = 2; //Column 2
 		c.weightx = 1; //Column is elastic
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.BOTH; // Stretch contents
 		contentPanel.add(tabs,c); // add tabs
 		
 		c.fill = GridBagConstraints.NONE;
 		c.gridy = 1; // Row 1
 		c.gridx = 2; // Column 1
 		c.weighty = 0; // Row is not elastic
 		c.weightx = 0; // Column is not elastic
 		c.anchor = GridBagConstraints.LINE_END;
 		contentPanel.add(bottom,c); // Add bottom
 		
 		contentPanel.setMinimumSize(new Dimension(500,465));
 		contentPanel.setPreferredSize(new Dimension(500,465));
 
 		this.setViewportView(contentPanel);
 		
 		fillFieldsForRequirement();
 	}
 	
 	/**
 	 * Fills the fields of the edit requirement panel based on the current settings of the
 	 * edited requirement.
 	 */
 	private void fillFieldsForRequirement()
 	{
 		getBoxName().setText(getRequirementBeingEdited().getName());
 		getBoxDescription().setText(getRequirementBeingEdited().getDescription());
 		getBoxEstimate().setText(String.valueOf(getRequirementBeingEdited().getEstimate()));
 		getBoxReleaseNum().setText(getRequirementBeingEdited().getRelease());
 		getDropdownStatus().setSelectedItem(getRequirementBeingEdited().getStatus());
 		getDropdownType().setSelectedItem(getRequirementBeingEdited().getType());
 		getBoxIteration().setText(getRequirementBeingEdited().getIteration().toString());
 		
 		switch(getRequirementBeingEdited().getPriority())
 		{
 		case BLANK:
 			getPriorityBlank().setSelected(true);
 			break;
 		case LOW:
 			getPriorityLow().setSelected(true);
 			break;
 		case MEDIUM:
 			getPriorityMedium().setSelected(true);
 			break;
 		case HIGH:
 			getPriorityHigh().setSelected(true);
 			break;
 		}
 		
 		if(getRequirementBeingEdited().getStatus() == RequirementStatus.INPROGRESS || getRequirementBeingEdited().getStatus() == RequirementStatus.COMPLETE)
 		{
 			getBoxEstimate().setEnabled(false);
 		}
 		else
 		{
 			getBoxEstimate().setEnabled(true);
 		}
 		
 		if(requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS) buttonDelete.setEnabled(false);
 		if(requirementBeingEdited.getStatus() == RequirementStatus.DELETED) disableComponents(); 
 		if(!(requirementBeingEdited.getEstimate() > 0)) getBoxIteration().setEnabled(false);
 		if(!(getRequirementBeingEdited().getEstimate() > 0)) getBoxIteration().setEnabled(false);
 		
 		//reset the error messages.
 		this.getErrorEstimate().setText("");
 		getBoxEstimate().setBorder(defaultBorder);
 		this.getErrorDescription().setText("");
 		getBoxDescription().setBorder(defaultBorder);
 		this.getErrorName().setText("");
 		getBoxName().setBorder(defaultBorder);
 		
 		
 		repaint();
 	}
 	
 	/**
 	 * Builds the right panel
 	 */
 	@Override
 	protected JPanel buildRightPanel()
 	{
 		super.buildRightPanel();
 		
 		return rightPanel;
 	}
 	public JPanel buildBottom()
 	{
 		//setup the buttons
 		JPanel buttonPanel = new JPanel();
 	
 		// Construct the add requirement controller and add it to the update button
 		getButtonUpdate().addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				if(validateFields())
 				{
 					update();
 					cancel();
 				}
 			}
 		});
 		
 		getButtonClear().addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillFieldsForRequirement();
 			}
 		
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancel();
 			}
 		});
 		
 		buttonDelete.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				deleteRequirement();
 			}
 		});
 
 		buttonAddChild.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
				
 			}
 		});
 		buttonPanel.add(getButtonUpdate());
 		buttonPanel.add(getButtonClear());
 		buttonPanel.add(buttonAddChild);
 		buttonPanel.add(buttonDelete);
 		buttonPanel.add(buttonCancel);
 		
 		return buttonPanel;
 	}
 	
 	/**
 	 * Updates the requirement.
 	 */
 	public void update()
 	{
 		// Extract the name, release number, and description from the GUI fields
 		String stringName = this.getBoxName().getText();
 		String stringReleaseNum = this.getBoxReleaseNum().getText();
 		String stringDescription = this.getBoxDescription().getText();
 		String stringEstimate = this.getBoxEstimate().getText();
 		String stringIteration = this.getBoxIteration().getText();
 		
 		if(stringIteration.trim().equals("")) stringIteration = "Backlog";
 
 
 		RequirementPriority priority;
 		RequirementStatus status;
 		RequirementType type = (RequirementType)getDropdownType().getSelectedItem();
 		
 		int estimate = stringEstimate.trim().length() == 0 ? 0 : Integer.parseInt(stringEstimate);
 		// Extract the status from the GUI
 		status = (RequirementStatus)this.getDropdownStatus().getSelectedItem();
 		// Extract which radio is selected for the priority
 		//		If requirement deleted {}		
 		//		estimate = iteration.getEstimate()- estimate;
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
 		TransactionHistory requirementHistory = requirementBeingEdited.getHistory();	
 		requirementHistory.setTimestamp(System.currentTimeMillis());
 				
 		// Create a new requirement object based on the extracted info
 		getRequirementBeingEdited().setName(stringName);
 		getRequirementBeingEdited().setRelease(stringReleaseNum);
 		getRequirementBeingEdited().setDescription(stringDescription);
 		getRequirementBeingEdited().setStatus(status, created);
 		getRequirementBeingEdited().setPriority(priority, created);
 		getRequirementBeingEdited().setEstimate(estimate);
 		getRequirementBeingEdited().setIteration(stringIteration, created);
 		getRequirementBeingEdited().setType(type);
 		UpdateRequirementController.getInstance().updateRequirement(getRequirementBeingEdited());
 		
 	}
 	
 	/**
 	 * Constructs a panel with a scolling list of notes for the requirement, as well as the elements to add new notes
 	 * @return panel for displaying and creating notes
 	 */
 	private JPanel buildNotePanel()
 	{
 		// Buttons to be added to the bottom of the NotePanel
 		JButton buttonAddNote = new JButton("Add Note");
 		JButton buttonClear = new JButton("Clear");
 		
 		// Create text area for note to be added
 		final JTextArea noteMessage = new JTextArea();
 		noteMessage.setLineWrap(true); // If right of box is reach, goes down a line
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
 		panel.add(scroll,c);
 		
 		c.gridy = 1; // Row 1
 		c.weighty = 0; // Fill 0% of vertical space
 		panel.add(noteMessage,c);
 		
 		bc.anchor = GridBagConstraints.WEST; // Anchor buttons to west of bottom panel
 		bottomPanel.add(buttonAddNote, bc); // Include "Add note" button to bottom panel
 		
 		bc.gridx = 1; // Column 1
 		bottomPanel.add(buttonClear, bc); // Include "Clear" button to bottom panel
 		
 		bc.gridx = 2; // Column 2
 		bottomPanel.add(errorMsg, bc); // Add error message label to bottom panel
 		
 		c.weighty = 0; // Do not stretch
 		c.gridy = 2; // Row 2
 		c.fill = GridBagConstraints.NONE; // Do not fill cell
 		c.anchor = GridBagConstraints.WEST; // Anchor buttons to west of panel
 		panel.add(bottomPanel,c); // Add buttons to the panel
 		
 		// Set scroll pane to display notes associated with edited requirement
 		scroll.setViewportView(NotePanel.createList(this.requirementBeingEdited.getNotes()));
 		
 		// Listener for add note button
 		buttonAddNote.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				// Display error message if there is no text in noteMessage
 				if(noteMessage.getText().length() <= 0) 
 				{
 					errorMsg.setText(" Error: Must add text to create note.");
 				}
 				else
 				{
 					
 					String msg = noteMessage.getText(); // Get text from noteMessage
 					
 					// Clear all text areas
 					noteMessage.setText("");
 					errorMsg.setText("");
 					
 					// Add note to requirement
 					requirementBeingEdited.getNotes().add(msg);
 					
 					// Update panel to show new note
 					scroll.setViewportView(NotePanel.createList(requirementBeingEdited.getNotes()));
 					
 					// Update database so requirement stores new note
 					UpdateRequirementController.getInstance().updateRequirement(requirementBeingEdited);
 				}
 			}
 		});
 		
 		// Listener for the Clear button
 		buttonClear.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
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
 	private JPanel buildHistoryPanel()
 	{
 		// Layout manager for transaction history panel
 		GridBagLayout layout = new GridBagLayout();
 		JPanel panel = new JPanel(layout);
 		GridBagConstraints c = new GridBagConstraints();
 		
 		// Create scroll pane for window, set scroll bar to always be on
 		JScrollPane scroll = new JScrollPane();
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		c.fill = GridBagConstraints.BOTH; // Fill grid cell with elements
 		c.weightx = 1; // Fill horizontal space
 		c.weighty = 0.8; // Fill 80% of vertical space
 		panel.add(scroll,c); // Add scroll pane to panel
 		
 		// Show the requirement's transaction history in the scroll pane
 		scroll.setViewportView(HistoryPanel.createList(this.requirementBeingEdited.getHistory()));
 
 		return panel;
 	}
 	
 	/**
 	 * Cancels the editing of the requirement.
 	 */
 	private void cancel()
 	{
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
 	 * Deletes the requirement.  Sets all fields uneditable, sets status to deleted and closes the tab.
 	 */
 	private void deleteRequirement()
 	{
 		if(this.requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS) return;
 		
 		this.getDropdownStatus().setSelectedItem(RequirementStatus.DELETED);
 		
 		requirementBeingEdited.setStatus(RequirementStatus.DELETED, false);	
 		
 		
 		UpdateRequirementController.getInstance().updateRequirement(requirementBeingEdited);
 		
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	
 	/**
 	 * Disables all the components of the editing panel besides the status dropdown.
 	 */
 	private void disableComponents()
 	{
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
 		this.buttonAddChild.setEnabled(false);
 		this.buttonDelete.setEnabled(false);
 	}
 	
 }
