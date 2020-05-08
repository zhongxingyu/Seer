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
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 /**
  * 
  * @author Pi 
  * @author Chris
  * @author Brian
  *
  */
 
 public class NewRequirementPanel extends RequirementPanel 
 {	
 	
 	private JButton buttonUpdate = new JButton("Create");
 	private JButton buttonCancel = new JButton("Cancel");
 	private JButton buttonClear = new JButton("Clear");
 		
 	/**
 	 * Constructor for a new requirement panel with a parent
 	 */
 	public NewRequirementPanel()
 	{
 		this(-1);
 	}
 	
 	/**
 	 * Constructor for a new requirement panel with a parent
 	 */
 	public NewRequirementPanel(int parentRequirement) {
 		contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		
 		this.displayRequirement = new Requirement();
 		try {
 			displayRequirement.setParentID(parentRequirement);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 
 		contentPanel.add(buildLeftPanel()); //add left panel
 		contentPanel.add(buildRightPanel()); //add right panel
 		
 		contentPanel.setMinimumSize(new Dimension(500,465));
 		contentPanel.setPreferredSize(new Dimension(500,465));
 		
 		if(displayRequirement.getParentID() != -1) fillFieldsForParent();
 		
 		this.setViewportView(contentPanel);
 	}
 	
 	private void fillFieldsForParent()
 	{
 		getBoxIteration().setText(displayRequirement.getIteration());
 		getBoxEstimate().setText(String.valueOf(displayRequirement.getEstimate()));
 		getBoxReleaseNum().setText(displayRequirement.getRelease());
		getDropdownStatus().setSelectedItem(displayRequirement.getParent().getStatus());
 		getDropdownType().setSelectedItem(displayRequirement.getType());
 		
 		switch(displayRequirement.getPriority())
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
 		
 		this.disableNonChildFields();
 		this.getDropdownStatus().setEnabled(false);
 
 	}
 	
 	/**
 	 * Builds the left panel
 	 */
 	@Override
 	protected JPanel buildLeftPanel()
 	{
 		super.buildLeftPanel();
 		getBoxIteration().setEnabled(false);
 		getBoxIteration().setBackground(leftPanel.getBackground());
 		return leftPanel;
 	}
 	
 	/**
 	 * Builds the right panel
 	 */
 	@Override
 	protected JPanel buildRightPanel()
 	{
 		super.buildRightPanel();
 
 		getDropdownStatus().setEnabled(false);
 		
 		//setup the buttons
 		JPanel buttonPanel = new JPanel();
 		
 		// Construct the add requirement controller and add it to the update button
 		buttonUpdate.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{				
 				if(validateFields())
 					{
 						update();
 						removeNewReqTab();
 					}
 			}
 		});
 		
 		buttonClear.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				clear();
 				getButtonClear().setEnabled(false);
 			}
 		
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				removeNewReqTab();
 			}
 		});
 
 		buttonPanel.add(buttonUpdate);
 		buttonPanel.add(buttonClear);
 		buttonPanel.add(buttonCancel);
 		
 		SpringLayout rightLayout = (SpringLayout)rightPanel.getLayout();
 		
 		rightLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 15,
 				SpringLayout.SOUTH, getErrorEstimate());
 		rightLayout.putConstraint(SpringLayout.WEST, buttonPanel, 15,
 				SpringLayout.WEST, rightPanel);
 		
 		rightPanel.add(buttonPanel);
 		
 		return rightPanel;
 	}
 	
 	/**
 	 * Updates the requirement.
 	 */
 	public void update()
 	{
 		getNewRequirement().setId(RequirementModel.getInstance().getNextID());
 		// Extract the name, release number, and description from the GUI fields
 		String stringName = this.getBoxName().getText();
 		String stringReleaseNum = this.getBoxReleaseNum().getText();
 		String stringDescription = this.getBoxDescription().getText();
 		String stringIteration = this.getBoxIteration().getText();
 		String stringEstimate = this.getBoxEstimate().getText();
 		
 		RequirementPriority priority;
 		RequirementStatus status;
 		RequirementType type;
 		int estimate = stringEstimate.trim().length() == 0 ? 0 : Integer.parseInt(stringEstimate);
 		
 		// Extract the status from the GUI
 		status = (RequirementStatus)this.getDropdownStatus().getSelectedItem();
 		type = (RequirementType)this.getDropdownType().getSelectedItem();
 		// Extract which radio is selected for the priority
 		boolean stateHigh = getPriorityHigh().isSelected();
 		boolean stateMedium = getPriorityMedium().isSelected();
 		boolean stateLow = getPriorityLow().isSelected();
 		boolean stateBlank = getPriorityBlank().isSelected();
 
 		// Convert the priority string to its corresponding enum
 		if (stateHigh)
 			priority = RequirementPriority.HIGH;
 		else if (stateMedium)
 			priority = RequirementPriority.MEDIUM;
 		else if (stateLow)
 			priority = RequirementPriority.LOW;
 		else 
 			priority = RequirementPriority.BLANK;
 
 
 		// Set to true to indicate the requirement is being newly created
 		boolean created = true;
 				
 		if(getNewRequirement().getParentID() != -1)
 		{
 			getNewRequirement().setName(stringName);
 			getNewRequirement().setDescription(stringDescription);
 			getNewRequirement().setStatus(status, created);
 			getNewRequirement().setEstimate(estimate);
 		}
 		else
 		{
 			// Create a new requirement object based on the extracted info
 			getNewRequirement().setName(stringName);
 			getNewRequirement().setDescription(stringDescription);		
 			getNewRequirement().setRelease(stringReleaseNum);
 			getNewRequirement().setStatus(status, created);
 			getNewRequirement().setPriority(priority, created);
 			getNewRequirement().setType(type);
 			getNewRequirement().setEstimate(estimate);
 			getNewRequirement().setIteration(stringIteration, created);
 		}
 		
 		// Set the time stamp for the transaction for the creation of the requirement
         getNewRequirement().getHistory().setTimestamp(System.currentTimeMillis());
         System.out.println("The Time Stamp is now :" + getNewRequirement().getHistory().getTimestamp());
 		getNewRequirement().getHistory().add("Requirement created");
 
 		RequirementModel.getInstance().addRequirement(getNewRequirement());
 		
 		ViewEventController.getInstance().refreshEditRequirementPanel(RequirementModel.getInstance().getRequirement(getNewRequirement().getParentID()));
 	}
 	
 	private void removeNewReqTab()
 	{
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	/**
 	 * Clears the editing of the requirement.
 	 */
 	private void clear() 
 	{
 		if(getNewRequirement().getParentID() != -1)
 		{
 			getBoxName().setText("");
 			getBoxDescription().setText("");
 			getBoxEstimate().setText("");
 			repaint();
 			return;
 		}
 		getBoxName().setText("");
 		getBoxDescription().setText("");
 		this.getPriorityBlank().setSelected(true);
 		getDropdownType().setSelectedItem(RequirementType.BLANK);
 		getBoxReleaseNum().setText("");
 		getBoxEstimate().setText("");
 		
 		this.getErrorEstimate().setText("");
 		getBoxEstimate().setBorder(defaultBorder);
 		this.getErrorDescription().setText("");
 		getBoxDescription().setBorder(defaultBorder);
 		this.getErrorName().setText("");
 		getBoxName().setBorder(defaultBorder);
 		repaint(); //repaint the entire panel.
 	}
 	
 
 
 	/**
 	 * @return the buttonUpdate
 	 */
 	public JButton getButtonUpdate() {
 		return buttonUpdate;
 	}
 
 
 
 	/**
 	 * @return the buttonCancel
 	 */
 	public JButton getButtonCancel() {
 		return buttonCancel;
 	}
 
 
 
 	/**
 	 * @return the buttonClear
 	 */
 	public JButton getButtonClear() {
 		return buttonClear;
 	}
 
 
 
 	public Requirement getNewRequirement() {
 		return this.displayRequirement;
 	}
 	
 	/**
 	 * Returns whether any field in the panel has been changed
 	 */
 	public boolean anythingChanged()
 	{
 		boolean nameChanged = !(getBoxName().getText().equals(""));
 		boolean descriptionChanged = !(getBoxDescription().getText().equals(""));
 		boolean releaseChanged = !(getBoxReleaseNum().getText().equals(""));
 		//boolean iterationChanged = !(getBoxIteration().getText().equals(""));
 		boolean typeChanged = !(((RequirementType)getDropdownType().getSelectedItem()) == RequirementType.BLANK);
 
 		boolean priorityChanged = !getPriorityBlank().isSelected();
 		
 		boolean estimateChanged = !(getBoxEstimate().getText().equals(""));
 
 		boolean anythingChanged = nameChanged || descriptionChanged || releaseChanged ||  
 				typeChanged || priorityChanged || estimateChanged;
 		
 		return anythingChanged;
 	}
 	
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO change the boolean expression // test case fail because of this
 		this.buttonUpdate.setEnabled(getBoxName().getText().trim().length() > 0 && getBoxDescription().getText().trim().length() > 0);	
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
 		if(getNewRequirement().getParentID() != -1) this.disableNonChildFields();
 		
 		this.repaint();				
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		this.buttonUpdate.setEnabled(getBoxName().getText().trim().length() > 0 && getBoxDescription().getText().trim().length() > 0 );	
 		this.buttonClear.setEnabled(anythingChanged());		
 		this.repaint();				
 	}
 
 	@Override
 	public boolean readyToRemove() {
 		return true;
 	}
 
 }
