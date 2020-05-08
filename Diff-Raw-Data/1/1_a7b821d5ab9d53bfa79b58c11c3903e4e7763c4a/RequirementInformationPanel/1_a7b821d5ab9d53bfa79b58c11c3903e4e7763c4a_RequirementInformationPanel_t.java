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
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 
 import net.miginfocom.swing.MigLayout;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.TransactionHistory;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 
 /**
  */
 public class RequirementInformationPanel extends JScrollPane implements KeyListener,
 ItemListener, RequirementPanelListener, RequirementSelectorListener {
 	private Requirement currentRequirement;
 	private ViewMode viewMode;
 	private RequirementPanel parentPanel;
 
 	private JTextField boxName;
 	private JTextField boxReleaseNum;
 	private JTextArea boxDescription;
 	private JComboBox boxIteration;
 	private JTextField boxChildEstimate;
 	private JLabel labelChildEstimate;
 	private JTextField boxTotalEstimate;
 	private JLabel labelTotalEstimate;
 	private final Border defaultBorder = (new JTextField()).getBorder();
 	private final Border errorBorder = BorderFactory
 			.createLineBorder(Color.RED);
 
 	private JLabel currentParent;
 	private JButton editParent;
 	private JButton removeFromParent;
 	private JButton chooseParent;
 	private JPanel noParentInfoPanel;
 	private RequirementSelector parentSelector;
 	private JComboBox<RequirementType> dropdownType;
 	private JComboBox<RequirementStatus> dropdownStatus;
 	private JRadioButton priorityHigh;
 	private JRadioButton priorityMedium;
 	private JRadioButton priorityLow;
 	private JRadioButton priorityBlank;
 	private JTextField boxEstimate;
 	private ButtonGroup group;
 
 	private RequirementStatus lastValidStatus;
 	private boolean fillingFieldsForRequirement = false;
 	
 	private JLabel errorName;
 	private JLabel errorDescription;
 	private JLabel errorEstimate;
 
 	/**
 	 * Constructs the requirement information panel
 	 * @param parentPanel the panel this info panel reports to
 	 * @param mode the current view mode.
 	 * @param curr the requirement being edited/created.
 	 */
 	public RequirementInformationPanel(RequirementPanel parentPanel,
 			ViewMode mode, Requirement curr) {
 		this.currentRequirement = curr;
 		this.parentPanel = parentPanel;
 		this.viewMode = mode;
 		this.setMinimumSize(new Dimension(500,200));
 		this.buildLayout();
 
 		clearInfo();
 	}
 
 	/**
 	 * Builds the layout panel.
 	 * 
 	
 	 */
 	@SuppressWarnings("rawtypes")
 	private void buildLayout() {
 		ScrollablePanel contentPanel = new ScrollablePanel();
 		contentPanel.setLayout(new MigLayout("", "", "shrink"));
 		//instantialize everything.
 		JLabel labelName = new JLabel("Name *");
 		JLabel labelReleaseNum = new JLabel("Release Number");
 		JLabel labelDescription = new JLabel("Description *");
 		JLabel labelIteration = new JLabel("Iteration");
 		labelChildEstimate = new JLabel("Children Estimate");
 		labelTotalEstimate = new JLabel("Total Estimate");
 		JLabel labelType = new JLabel("Type");
 		JLabel labelStatus = new JLabel("Status");
 		JLabel labelPriority = new JLabel("Priority");
 		JLabel labelEstimate = new JLabel("Estimate");
 		JPanel parentInfoPanel = new JPanel();
 		boxName = new JTextField();
 		boxName.addKeyListener(this);
 
 		boxReleaseNum = (new JTextField());
 		boxReleaseNum.addKeyListener(this);
 
 		JScrollPane descrScroll = new JScrollPane();
 		boxDescription = new JTextArea();
 		boxDescription.setLineWrap(true);
 		boxDescription.setBorder(defaultBorder);
 		boxDescription.addKeyListener(this);
 		descrScroll.setViewportView(boxDescription);
 
 		List<Iteration> iterations = IterationModel.getInstance().getIterations();
 		int size = iterations.size();
 		String[] iterationNames = new String[size];
 		for (int i = 0; i < iterations.size(); i++) {
 			Iteration iter = iterations.get(i);
 			iterationNames[i] = iter.getName();
 		}
 		boxIteration = (new JComboBox(iterationNames));
 		boxIteration.addItemListener(this);
 		boxIteration.setBackground(Color.WHITE);
 
 		errorName = (new JLabel());
 		errorDescription = (new JLabel());
 
 		dropdownType = (new JComboBox<RequirementType>(RequirementType.values()));
 		dropdownType.setEditable(false);
 		dropdownType.setBackground(Color.WHITE);
 		dropdownType.addItemListener(this);
 
 		dropdownStatus = (new JComboBox<RequirementStatus>(
 				RequirementStatus.values()));
 		dropdownStatus.setEditable(false);
 		dropdownStatus.setBackground(Color.WHITE);
 		dropdownStatus.addItemListener(this);
 
 		// Radio buttons
 
 		priorityHigh = (new JRadioButton("High"));
 		priorityHigh.addItemListener(this);
 		priorityMedium = (new JRadioButton("Medium"));
 		priorityMedium.addItemListener(this);
 		priorityLow = (new JRadioButton("Low"));
 		priorityLow.addItemListener(this);
 		priorityBlank = (new JRadioButton("None"));
 		priorityBlank.addItemListener(this);
 
 		group = new ButtonGroup();
 		group.add(priorityBlank);
 		group.add(priorityHigh);
 		group.add(priorityMedium);
 		group.add(priorityLow);
 
 		JPanel priorityPanel = new JPanel();
 
 		priorityPanel.add(priorityLow);
 		priorityPanel.add(priorityMedium);
 		priorityPanel.add(priorityHigh);
 		priorityPanel.add(priorityBlank);
 
 		boxEstimate = (new JTextField());
 		boxEstimate.setPreferredSize(new Dimension(50, 20));
 		boxEstimate.addKeyListener(this);
 		boxChildEstimate = (new JTextField());
 		boxChildEstimate.setEnabled(false);
 		boxTotalEstimate = new JTextField();
 		boxTotalEstimate.setEnabled(false);
 		errorEstimate = (new JLabel());
 
 		boolean hasChildren = !currentRequirement.getChildren().isEmpty();
 		labelChildEstimate.setVisible(hasChildren);
 		boxChildEstimate.setVisible(hasChildren);
 
 		labelTotalEstimate.setVisible(hasChildren);
 		boxTotalEstimate.setVisible(hasChildren);
 
 		currentParent = new JLabel();
 		editParent = new JButton("Edit Parent");
 		editParent.setAlignmentX(RIGHT_ALIGNMENT);
 		editParent.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(currentRequirement.getParentID() != -1)
 				{
 					ViewEventController.getInstance().editRequirement(currentRequirement.getParent());
 				}
 			}		
 		});
 		removeFromParent = new JButton("Remove From Parent");
 		removeFromParent.setAlignmentX(RIGHT_ALIGNMENT);
 		removeFromParent.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Requirement oldParent = currentRequirement.getParent();
 				try {
 					currentRequirement.setParentID(-1);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 				UpdateRequirementController.getInstance().updateRequirement(currentRequirement);
 				ViewEventController.getInstance().refreshEditRequirementPanel(currentRequirement);
 				ViewEventController.getInstance().refreshEditRequirementPanel(oldParent);
 			}	
 		});
 		
 		parentSelector = new RequirementSelector(this, currentRequirement, RequirementSelectorMode.POSSIBLE_PARENTS, false);
 
 		chooseParent = new JButton("Choose Parent");
 		chooseParent.setAlignmentX(RIGHT_ALIGNMENT);
 		chooseParent.addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				chooseParent.setVisible(false);
 				parentSelector.setVisible(true);
 				repaint();
 			}
 			
 		});
 		
 
 		parentInfoPanel.setLayout(new BoxLayout(parentInfoPanel, BoxLayout.Y_AXIS));
 		currentParent.setAlignmentX(LEFT_ALIGNMENT);
 		noParentInfoPanel = new JPanel();
 		parentInfoPanel.add(editParent);
 		parentInfoPanel.add(removeFromParent);
 		noParentInfoPanel.add(parentSelector);
 		noParentInfoPanel.add(chooseParent);
 		parentInfoPanel.add(noParentInfoPanel);
 		chooseParent.setVisible(true);
 		parentSelector.setVisible(false);
 		//setup the top.
 
 		contentPanel.add(labelName, "wrap");
 		contentPanel.add(boxName, "growx, pushx, shrinkx, span, wrap");
 
 		contentPanel.add(labelDescription, "wrap");
 		contentPanel.add(descrScroll, "growx, pushx, shrinkx, span, height 200px, wmin 10, wrap");
 
 		//setup columns.
 		JPanel leftColumn = new JPanel(new MigLayout());
 		JPanel rightColumn = new JPanel(new MigLayout());
 		leftColumn.add(labelReleaseNum, "left,wrap");
 		leftColumn.add(boxReleaseNum, "width 100px, left,wrap");
 		leftColumn.add(labelIteration, "left,wrap");
 		leftColumn.add(boxIteration, "width 100px, left,wrap");
 		leftColumn.add(labelEstimate, "left,wrap");
 		leftColumn.add(boxEstimate, "width 50px, left,wrap");
 		leftColumn.add(labelChildEstimate, "left,wrap");
 		leftColumn.add(boxChildEstimate, "width 50px, left, wrap");
 		leftColumn.add(labelTotalEstimate, "left, wrap");
 		leftColumn.add(boxTotalEstimate, "width 50px, left");
 
 		rightColumn.add(labelType, "right, wrap");
 		rightColumn.add(dropdownType, "right, wrap");
 		rightColumn.add(labelStatus, "right, wrap");
 		rightColumn.add(dropdownStatus, "right, wrap");
 		rightColumn.add(labelPriority, "right, wrap");
 		rightColumn.add(priorityPanel, "right, wrap");
 		rightColumn.add(currentParent,"right, wrap");
 		rightColumn.add(parentInfoPanel, "right, span, wrap");
 		
 		contentPanel.add(leftColumn, "left, spany, growy, push");
 		contentPanel.add(rightColumn, "right, spany, growy, push");
 		
 		fireRefresh();
 		this.setViewportView(contentPanel);
 	}
 
 	/**
 	 * Refreshes the information of the requirement.
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireRefresh()
 	 */
 	public void fireRefresh() {
 		parentSelector.refreshList();
 		refreshParentInformation();
 		adjustFieldEnability();
 	}
 	
 	/**
 	 * Refresh information about the parent
 	 */
 	private void refreshParentInformation()
 	{
 		boolean isCreating = viewMode == ViewMode.CREATING;
 		boolean hasChildren = !currentRequirement.getChildren().isEmpty();
 		labelChildEstimate.setVisible(hasChildren);
 		boxChildEstimate.setText(
 				Integer.toString(currentRequirement.getChildEstimate()));
 		boxChildEstimate.setVisible(hasChildren);
 		boxTotalEstimate.setText(Integer.toString(currentRequirement.getTotalEstimate()));
 		boxTotalEstimate.setVisible(hasChildren);
 		labelTotalEstimate.setVisible(hasChildren);
 		
 		if (currentRequirement.getParentID() != -1) {
 			currentParent.setText("Parent: " + currentRequirement.getParent().getName());
 			currentParent.setVisible(true);
 			editParent.setVisible(true && !isCreating);
 			removeFromParent.setVisible(true && !isCreating);
 			noParentInfoPanel.setVisible(false);
 		}
 		else {
 			currentParent.setText("Parent: ");
 			currentParent.setVisible(true && !isCreating);
 			editParent.setVisible(false);
 			removeFromParent.setVisible(false);
 			noParentInfoPanel.setVisible(true);
 		}
 	}
 
 	/**
 	 * Method fireDeleted.
 	 * @param b boolean
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireDeleted(boolean)
 	 */
 	@Override
 	public void fireDeleted(boolean b) {}
 
 	/**
 	 * Method fireValid.
 	 * @param b boolean
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireValid(boolean)
 	 */
 	@Override
 	public void fireValid(boolean b) {}
 
 	/**
 	 * Method fireChanges.
 	 * @param b boolean
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#fireChanges(boolean)
 	 */
 	@Override
 	public void fireChanges(boolean b) {}
 
 	/**
 	 * Fills the fields of the edit requirement panel based on the current
 	 * settings of the edited requirement.
 	 */
 	@SuppressWarnings("unchecked")
 	private void fillFieldsForRequirement() {
 		fillingFieldsForRequirement = true;
 		boxName.setText(currentRequirement.getName());
 		boxDescription.setText(currentRequirement.getDescription());
 		boxEstimate.setText(
 				String.valueOf(currentRequirement.getEstimate()));
 		boxReleaseNum.setText(currentRequirement.getRelease());
 		boxIteration.setSelectedItem(currentRequirement.getIteration());
 
 		if (currentRequirement.getStatus().equals(RequirementStatus.NEW)) {
 			dropdownStatus.removeAllItems();
 			dropdownStatus.addItem(RequirementStatus.NEW);
 			dropdownStatus.addItem(RequirementStatus.DELETED);
 		} else if (currentRequirement.getStatus().equals(
 				RequirementStatus.INPROGRESS)) {
 			dropdownStatus.removeAllItems();
 			dropdownStatus.addItem(RequirementStatus.INPROGRESS);
 			dropdownStatus.addItem(RequirementStatus.COMPLETE);
 		} else if (currentRequirement.getStatus()
 				.equals(RequirementStatus.OPEN)) {
 			dropdownStatus.removeAllItems();
 			dropdownStatus.addItem(RequirementStatus.OPEN);
 			dropdownStatus.addItem(RequirementStatus.DELETED);
 		} else if (currentRequirement.getStatus().equals(
 				RequirementStatus.COMPLETE)
 				|| currentRequirement.getStatus().equals(
 						RequirementStatus.DELETED)) {
 			if (currentRequirement.getIteration().equals("Backlog")) {
 				dropdownStatus.removeAllItems();
 				dropdownStatus.addItem(RequirementStatus.OPEN);
 				dropdownStatus.addItem(RequirementStatus.COMPLETE);
 				dropdownStatus.addItem(RequirementStatus.DELETED);
 			} else {
 				dropdownStatus.removeAllItems();
 				dropdownStatus.addItem(RequirementStatus.INPROGRESS);
 				dropdownStatus.addItem(RequirementStatus.COMPLETE);
 				dropdownStatus.addItem(RequirementStatus.DELETED);
 			}
 		}
 		dropdownStatus.setSelectedItem(currentRequirement.getStatus());
 		lastValidStatus = currentRequirement.getStatus();
 
 		dropdownType.setSelectedItem(currentRequirement.getType());
 
 		this.setPriorityButton(currentRequirement.getPriority());
 
 		// reset the error messages.
 		errorEstimate.setText("");
 		boxEstimate.setBorder(defaultBorder);
 		errorDescription.setText("");
 		boxDescription.setBorder(defaultBorder);
 		errorName.setText("");
 		boxName.setBorder(defaultBorder);
 		boxChildEstimate.setText(Integer.toString(currentRequirement.getChildEstimate()));
 		boxTotalEstimate.setText(Integer.toString(currentRequirement.getTotalEstimate()));
 
 		fireRefresh();
 		fillingFieldsForRequirement = false;
 		repaint();
 	}
 
 	/**
 	 * Validates the values of the fields in the requirement panel to ensure
 	 * they are valid
 	 * @param warn whether to warn the user or not
 	
 	 * @return whether fields are valid. */
 	public boolean validateFields(boolean warn) {
 		boolean isNameValid;
 		boolean isDescriptionValid;
 		boolean isEstimateValid;
 		
 		parentPanel.removeError("Name can be no more than 100 chars.");
 		parentPanel.removeError("Name is required.");
 		parentPanel.removeError("Description is required.");
 		parentPanel.removeError("Estimate must be non-negative integer");	
 		parentPanel.removeError("Cannot have an estimate of 0 and be assigned to an iteration.");	
 		
 		if (getBoxName().getText().length() >= 100) {
 			isNameValid = false;
 			getErrorName().setText("No more than 100 chars");
 			getBoxName().setBorder(errorBorder);
 			getErrorName().setForeground(Color.RED);
 			parentPanel.displayError("Name can be no more than 100 chars.");
 		} else if (getBoxName().getText().trim().length() <= 0) {
 			isNameValid = false;
 			if(warn)
 			{
 				getErrorName().setText("** Name is REQUIRED");
 				getBoxName().setBorder(errorBorder);
 				getErrorName().setForeground(Color.RED);
 			}
 			parentPanel.displayError("Name is required.");
 		} else {
 			if(warn)
 			{
 				getErrorName().setText("");
 				getBoxName().setBorder(defaultBorder);
 			}
 			isNameValid = true;
 
 		}
 		if (getBoxDescription().getText().trim().length() <= 0) {
 			isDescriptionValid = false;
 			if(warn)
 			{
 				getErrorDescription().setText("** Description is REQUIRED");
 				getErrorDescription().setForeground(Color.RED);
 				getBoxDescription().setBorder(errorBorder);
 			}
 			parentPanel.displayError("Description is required.");
 		} else {
 			if(warn)
 			{
 				getErrorDescription().setText("");
 				getBoxDescription().setBorder(defaultBorder);
 			}
 			isDescriptionValid = true;
 		}
 
 		if (getBoxEstimate().getText().trim().length() <= 0) {
 			getBoxEstimate().setText("");
 			getErrorEstimate().setText("");
 			getBoxEstimate().setBorder(defaultBorder);
 			isEstimateValid = true;
 		} else if (!(isInteger(getBoxEstimate().getText()))) {
 			getErrorEstimate()
 			.setText("Estimate must be non-negative integer");
 			getBoxEstimate().setBorder(errorBorder);
 			getBoxEstimate().setBorder((new JTextField()).getBorder());
 			getErrorEstimate().setForeground(Color.RED);
 			isEstimateValid = false;
 		} else if (Integer.parseInt(getBoxEstimate().getText()) < 0) {
 			getErrorEstimate()
 			.setText("Estimate must be non-negative integer");
 			getBoxEstimate().setBorder(errorBorder);
 			getErrorEstimate().setForeground(Color.RED);
 			isEstimateValid = false;
 		} else if (((Integer.parseInt(getBoxEstimate().getText()) == 0) || (getBoxEstimate().getText().trim().length() == 0))
 				&& !(getBoxIteration().getSelectedItem().equals("Backlog") || getBoxIteration()
 						.getSelectedItem().equals(""))) {
 			getErrorEstimate()
 			.setText(
 					"Cannot have an estimate of 0 and be assigned to an iteration.");
 			getBoxEstimate().setBorder(errorBorder);
 			getErrorEstimate().setForeground(Color.RED);
 			isEstimateValid = false;
 		} else {
 			getErrorEstimate().setText("");
 			getBoxEstimate().setBorder(defaultBorder);
 			isEstimateValid = true;
 		}
 		parentPanel.displayError(getErrorEstimate().getText());
 		return isNameValid && isDescriptionValid && isEstimateValid;
 	}
 
 	/**
 	 * Resets the information back to default
 	 */
 	public void clearInfo()
 	{
 		this.fillFieldsForRequirement(); //if editing, revert back to old info
 
 		//no changes have been made, so let the parent know.
 		this.parentPanel.fireChanges(false); 
 	}
 
 	/**
 	 * Updates the requirement/creates the requirement based on the view mode.
 	 */
 	public void update() {
 		updateRequirement(viewMode == ViewMode.CREATING);
 	}
 
 	/**
 	 * Updates the requirement based on whether it is being created or not
 	 * @param created whether the requirement is being created or edited.
 	 */
 	private void updateRequirement(boolean created) {
 		if(created) currentRequirement.setId(RequirementModel.getInstance().getNextID());
 		
 		// Extract the name, release number, and description from the GUI fields
 		String stringName = this.getBoxName().getText();
 		String stringReleaseNum = this.getBoxReleaseNum().getText();
 		String stringDescription = this.getBoxDescription().getText();
 		String stringEstimate = this.getBoxEstimate().getText();
 		String stringIteration = (String) this.getBoxIteration().getSelectedItem().toString();
 
 		if (stringIteration.trim().equals(""))
 			stringIteration = "Backlog";
 
 		RequirementPriority priority;
 		RequirementStatus status = (RequirementStatus) this.getDropdownStatus().getSelectedItem();
 		RequirementType type = (RequirementType) getDropdownType()
 				.getSelectedItem();
 
 		int estimate = stringEstimate.trim().length() == 0 ? 0 : Integer
 				.parseInt(stringEstimate);
 
 		// Extract which radio is selected for the priority
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
 
 		// Set the time stamp so that all transaction messages from this update
 		// will have the same time stamp
 		TransactionHistory requirementHistory = currentRequirement.getHistory();
 		requirementHistory.setTimestamp(System.currentTimeMillis());
 		
 		currentRequirement.setName(stringName);
 		currentRequirement.setRelease(stringReleaseNum);
 		currentRequirement.setDescription(stringDescription);
 		currentRequirement.setStatus(status, created);
 		currentRequirement.setPriority(priority, created);
 		currentRequirement.setEstimate(estimate, created);
 		currentRequirement.setIteration(stringIteration, created);
 		currentRequirement.setType(type);
 		if(created)
 		{
 			// Set the time stamp for the transaction for the creation of the requirement
 			currentRequirement.getHistory().setTimestamp(System.currentTimeMillis());
 			System.out.println("The Time Stamp is now :" + currentRequirement.getHistory().getTimestamp());
 			currentRequirement.getHistory().add("Requirement created");
 
 			RequirementModel.getInstance().addRequirement(currentRequirement);
 		}
 
 		UpdateRequirementController.getInstance().updateRequirement(
 				currentRequirement);
 
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().refreshTree();
 
 		if(currentRequirement.getParentID() != -1)
 		{
 			ViewEventController.getInstance().refreshEditRequirementPanel(currentRequirement.getParent());
 		}
 	}
 	
 	/**
 	 * Enables or disables components as needed
 	 */
 	private void adjustFieldEnability()
 	{
 		boolean allDisabled = getDropdownStatus().getSelectedItem() == RequirementStatus.DELETED;
 		allDisabled |= getDropdownStatus().getSelectedItem() == RequirementStatus.COMPLETE;
 		boolean inProgress = getDropdownStatus().getSelectedItem() == RequirementStatus.INPROGRESS;
 		boolean validEstimate = false;
 		boolean isCreating = viewMode == ViewMode.CREATING;
 		
 		boolean allChildrenDeleted = true;
 		for(Requirement child : currentRequirement.getChildren())
 		{
 			allChildrenDeleted &= child.getStatus() == RequirementStatus.DELETED;
 		}
 
 		try
 		{
 			Integer estimate = new Integer(getBoxEstimate().getText().trim());
 			
 			validEstimate = estimate > 0;
 		}
 		catch(Exception e)
 		{
 			validEstimate = false;
 		}
 		
 		this.getBoxName().setEnabled(!allDisabled);
 		this.getBoxDescription().setEnabled(!allDisabled);
 		this.getBoxEstimate().setEnabled(!inProgress && !allDisabled);
 		this.getBoxReleaseNum().setEnabled(!allDisabled);
 		this.getDropdownType().setEnabled(!allDisabled);
 		this.getDropdownStatus().setEnabled(!isCreating);
 		this.getBoxIteration().setEnabled(validEstimate && !allDisabled);
 		this.getPriorityHigh().setEnabled(!allDisabled);
 		this.getPriorityMedium().setEnabled(!allDisabled);
 		this.getPriorityLow().setEnabled(!allDisabled);
 		this.getPriorityBlank().setEnabled(!allDisabled);
 		this.parentPanel.fireDeleted(allDisabled || inProgress || !allChildrenDeleted);	
 	}
 
 	/**
 	
 	 * @return Returns whether any field in the panel has been changed */
 	public boolean anythingChanged()
 	{
 		if(viewMode == ViewMode.CREATING)
 		{
 			return anythingChangedCreating();
 		}
 		else
 		{
 			return anythingChangedEditing();
 		}
 	}
 
 	/**
 	
 	 * @return Returns whether any fields in the panel have been changed  */
 	private boolean anythingChangedCreating() {
 		// Check if the user has changed the name
 		if (!(getBoxName().getText().equals(""))){
 			return true;}
 		// Check if the user has changed the description
 		if (!(getBoxDescription().getText().equals(""))){
 			return true;}
 		// Check if the user has changed the release number
 		if (!(getBoxReleaseNum().getText().equals(""))){
 			return true;}
 		// Check if the user has changed the iteration number
 		if (!((getBoxIteration().getSelectedItem().toString().equals("")) || getBoxIteration().getSelectedItem().equals("Backlog"))){
 			return true;}
 		// Check if the user has changed the type
 		if (!(((RequirementType)getDropdownType().getSelectedItem()) == RequirementType.BLANK)){
 			return true;}
 		// Check if the user has changed the estimate
 		if (!(getBoxEstimate().getText().trim().equals("") || getBoxEstimate().getText().trim().equals("0"))){
 			return true;}
 
 		if (!getPriorityBlank().isSelected())
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	
 	 * @return whether any fields have been changed. */
 	private boolean anythingChangedEditing() {
 		// Check if the user has changed the name
 		if (!(getBoxName().getText().equals(currentRequirement.getName()))){
 			return true;}
 		// Check if the user has changed the description
 		if (!(getBoxDescription().getText().equals(currentRequirement.getDescription()))){
 			return true;}
 		// Check if the user has changed the release number
 		if (!(getBoxReleaseNum().getText().equals(currentRequirement.getRelease()))){
 			return true;}
 		// Check if the user has changed the iteration number
 		if (!(getBoxIteration().getSelectedItem().toString().equals(currentRequirement.getIteration()))){
 			return true;}
 		// Check if the user has changed the type
 		if (!(((RequirementType)getDropdownType().getSelectedItem()) == currentRequirement.getType())){
 			return true;}
 		// Check if the user has changed the status
 		if (!(((RequirementStatus)getDropdownStatus().getSelectedItem()) == currentRequirement.getStatus())){
 			return true;}
 		// Check if the user has changed the estimate
 		if (!(getBoxEstimate().getText().trim().equals(String.valueOf(currentRequirement.getEstimate())))){
 			return true;}
 
 		RequirementPriority reqPriority = currentRequirement.getPriority();
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
 		if (priorityChanged)
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Returns whether the panel is ready to be removed or not based on if there are changes that haven't been
 	 * saved.
 	 * 
 	
 	 * @return whether the panel can be removed. * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener#readyToRemove()
 	 */
 	public boolean readyToRemove()
 	{	
 		return !anythingChanged();
 	}
 	
 	/**
 	 * Overrides the paintComponent method to retrieve the requirements on the first painting.
 	 * 
 	 * @param g	The component object to paint
 	 */
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		fireRefresh();
 		super.paintComponent(g);
 	}
 
 	/**
 	 * Sets the priority button based on the priority inputted
 	 * @param priority the priority to be set.
 	 */
 	public void setPriorityButton(RequirementPriority priority) {
 		switch (priority) {
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
 	}
 
 	/**
 	 * Method itemStateChanged.
 	 * @param e ItemEvent
 	 * @see java.awt.event.ItemListener#itemStateChanged(ItemEvent)
 	 */
 	@Override
 	public void itemStateChanged(ItemEvent e) {		
 		parentPanel.removeError("Cannot complete unless children are completed.");
 		parentPanel.removeError("Cannot delete when non-deleted children exist.");
 		if(!fillingFieldsForRequirement)
 		{
 			boolean allChildrenCompleted = true;
 			boolean allChildrenDeleted = true;
 			for (Requirement Child: currentRequirement.getChildren()){
 				allChildrenCompleted &= Child.getStatus() == RequirementStatus.COMPLETE;
 				allChildrenDeleted &= Child.getStatus() == RequirementStatus.DELETED;
 			}
 
 			if (!allChildrenCompleted && dropdownStatus.getSelectedItem() == RequirementStatus.COMPLETE)
 			{
 				dropdownStatus.setSelectedItem(lastValidStatus);
 				parentPanel.displayError("Cannot complete unless children are completed.");
 			}
 			else if (!allChildrenDeleted && this.dropdownStatus.getSelectedItem() == RequirementStatus.DELETED)
 			{
 				dropdownStatus.setSelectedItem(lastValidStatus);
 				parentPanel.displayError("Cannot delete when non-deleted children exist.");
 			}
 			else
 			{
 
 				lastValidStatus = (RequirementStatus)this.dropdownStatus.getSelectedItem();
 			}
 		}
 		this.parentPanel.fireValid(validateFields(false));
 		this.parentPanel.fireChanges(anythingChanged());
 		adjustFieldEnability();
 		this.repaint();
 	}
 
 	/**
 	 * Method keyReleased.
 	 * @param e KeyEvent
 	 * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		this.parentPanel.fireValid(validateFields(false));
 		this.parentPanel.fireChanges(anythingChanged());
 		adjustFieldEnability();
 
 
 		this.repaint();
 	}
 
 	/**
 	 * Method keyPressed.
 	 * @param e KeyEvent
 	 * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {}
 
 	/**
 	 * Method keyTyped.
 	 * @param e KeyEvent
 	 * @see java.awt.event.KeyListener#keyTyped(KeyEvent)
 	 */
 	@Override
 	public void keyTyped(KeyEvent e) {}
 
 	/**
 	 * Checks if the input string is an integer
 	 * 
 	 * @param input
 	 *            the string to test
 	
 	 * @return true if input is an integer, false otherwise */
 	public boolean isInteger(String input) {
 		try {
 			Integer.parseInt(input);
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Returns the error name label
 	
 	 * @return error name label */
 	public JLabel getErrorName() {
 		return errorName;
 	}
 
 	/**
 	
 	 * @return the error description label */
 	public JLabel getErrorDescription() {
 		return errorDescription;
 	}
 
 	/**
 	 * 
 	
 	 * @return the error estimate label */
 	public JLabel getErrorEstimate() {
 		return errorEstimate;
 	}
 
 	/**
 	 * 
 	
 	 * @return box name */
 	public JTextField getBoxName() {
 		return boxName;
 	}
 
 	/**
 	 * 
 	
 	 * @return box release num */
 	public JTextField getBoxReleaseNum() {
 		return boxReleaseNum;
 	}
 
 	/**
 	 * 
 	
 	 * @return box description */
 	public JTextArea getBoxDescription() {
 		return boxDescription;
 	}
 
 	/**
 	 * 
 	
 	 * @return box iteration */
 	public JComboBox getBoxIteration() {
 		return boxIteration;
 	}
 
 	/**
 	 * 
 	
 	 * @return box child estimate */
 	public JTextField getBoxChildEstimate() {
 		return boxChildEstimate;
 	}
 
 	/**
 	 * 
 	
 	 * @return box total estimate */
 	public JTextField getBoxTotalEstimate() {
 		return boxTotalEstimate;
 	}
 
 	/**
 	 * 
 	
 	 * @return type dropdown */
 	public JComboBox<RequirementType> getDropdownType() {
 		return dropdownType;
 	}
 
 	/**
 	 * 
 	
 	 * @return status dropdown */
 	public JComboBox<RequirementStatus> getDropdownStatus() {
 		return dropdownStatus;
 	}
 
 	/**
 	 * 
 	
 	 * @return estimate box */
 	public JTextField getBoxEstimate() {
 		return boxEstimate;
 	}
 
 	/**
 	
 	 * @return high priority button */
 	public JRadioButton getPriorityHigh() {
 		return priorityHigh;
 	}
 	/**
 	
 	 * @return medium priority button */
 	public JRadioButton getPriorityMedium() {
 		return priorityMedium;
 	}
 	/**
 	
 	 * @return low priority button */
 	public JRadioButton getPriorityLow() {
 		return priorityLow;
 	}
 	/**
 	
 	 * @return blank priority button */
 	public JRadioButton getPriorityBlank() {
 		return priorityBlank;
 	}
 
 	/**
 	 * Method requirementSelected.
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelectorListener#requirementSelected()
 	 */
 	@Override
 	public void requirementSelected(Object[] requirements) {
 		this.parentSelector.setVisible(false);
 		this.chooseParent.setVisible(true);
 	}
 }
