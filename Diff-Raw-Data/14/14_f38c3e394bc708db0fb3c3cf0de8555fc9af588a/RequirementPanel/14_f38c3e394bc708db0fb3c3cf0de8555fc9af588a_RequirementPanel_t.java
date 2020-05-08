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
 
 import java.awt.BorderLayout;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.tabs.RequirementTabsPanel;
 
 /**
  * @author Pi 
  * @author Chris
  * @author Brian
  */
 public class RequirementPanel extends JPanel implements RequirementButtonListener
 {
 	private List<RequirementPanelListener> listeners = new LinkedList<RequirementPanelListener>();
 	private Requirement displayRequirement;
 	private RequirementViewMode viewMode;
 	
 	private RequirementInformationPanel infoPanel;
 	private RequirementTabsPanel tabsPanel;
 	private RequirementButtonPanel buttonPanel;
 	
 	private boolean readyToClose = false;
 	
 	/**
 	 * Constructor for editing a requirement
 	 * @param editingRequirement requirement to edit
 	 */
 	public RequirementPanel(Requirement editingRequirement)
 	{
 		viewMode = (RequirementViewMode.EDITING);
 		
 		displayRequirement = editingRequirement;
 		this.buildLayout();
 	}
 	
 	/**
 	 * Constructor for creating a requirement
 	 * @param parentID the parent id, or -1 if no parent.
 	 */
 	public RequirementPanel(int parentID)
 	{
 		viewMode = (RequirementViewMode.CREATING);
 		
 		displayRequirement = new Requirement();
 		displayRequirement.setId(-2);
 		
 		try 
 		{
 			displayRequirement.setParentID(parentID);
 		} 
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 		}
 		this.buildLayout();
 	}
 	
 	/**
 	 * Builds the layout of the panel.
 	 */
 	private void buildLayout()
 	{
 		buttonPanel = new RequirementButtonPanel(this, viewMode, displayRequirement);
 		listeners.add(buttonPanel);
 		tabsPanel = new RequirementTabsPanel(this, viewMode, displayRequirement);
 		listeners.add(tabsPanel);
 		infoPanel = new RequirementInformationPanel(this, viewMode, displayRequirement);
 		listeners.add(infoPanel);
 		
 		JSplitPane contentPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, infoPanel, tabsPanel);
 		
 		this.setLayout(new BorderLayout());
 		this.add(contentPanel, BorderLayout.CENTER); // Add scroll pane to panel
 		this.add(buttonPanel, BorderLayout.SOUTH);
 	}
 	@Override
 	public void OKPressed() {
 		if (infoPanel.validateFields(true)) {
 			infoPanel.update();
 			readyToClose = true;
 			ViewEventController.getInstance().removeTab(this);
 		}		
 	}
 
 	@Override
 	public void clearPressed() 
 	{
 		infoPanel.clearInfo();
 	}
 
 	@Override
 	public void cancelPressed() 
 	{
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);		
 	}
 	
 	/**
 	 * Deletes the requirement. Sets all fields uneditable, sets status to
 	 * deleted and closes the tab.
 	 */
 	public void deletePressed() 
 	{
 		if (this.displayRequirement.getStatus() == RequirementStatus.INPROGRESS)
 			return;
 
 		displayRequirement.setStatus(RequirementStatus.DELETED, false);
 
 		UpdateRequirementController.getInstance().updateRequirement(displayRequirement);
 		
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);	
 	}	
 
 	/**
 	 * Fires to all listeners whether the requirement has been deleted or not
	 * @param b whether the requirement has been deleted or not.
 	 */
 	public void fireDeleted(boolean b) {	
 		for(RequirementPanelListener listener : listeners)
 		{
 			listener.fireDeleted(b);
 		}
 	}
 
 	/**
 	 * Fires to all listeners whether the requirement is valid or not
 	 * @param b whether the requirement is valid or not.
 	 */
 	public void fireValid(boolean b) {		
 		for(RequirementPanelListener listener : listeners)
 		{
 			listener.fireValid(b);
 		}
 	}
 	
 	/**
 	 * Fires to all listeners whether changes have occured
 	 * @param b whether changes have occured.
 	 */
 	public void fireChanges(boolean b) {	
 		for(RequirementPanelListener listener : listeners)
 		{
 			listener.fireChanges(b);
 		}	
 	}
 	
 	/**
 	 * Fires to all listeners to refresh.
 	 */
 	public void fireRefresh()
 	{
 		for(RequirementPanelListener listener : listeners)
 		{
 			listener.fireRefresh();
 		}	
 	}
 
 	/**
 	 * 		displays the given error message
 	 * @param msg the message to display.
 	 */
 	public void displayError(String msg)
 	{
 		buttonPanel.displayError(msg);
 	}
 	
 	/**
 	 * 		Removes the given error message
 	 * @param msg the message to display.
 	 */
 	public void removeError(String msg)
 	{
 		buttonPanel.removeError(msg);
 	}
 	
 	/**
 	 * @return whether the requirement panel as a whole is ready to be removed.
 	 */
 	public boolean readyToRemove() {
 		if(readyToClose) return true;
 		
 		boolean readyToRemove = true;
 		for(RequirementPanelListener listener : listeners)
 		{
 			readyToRemove &= listener.readyToRemove();
 		}
 		
 		if(readyToRemove)
 		{
 			return true;
 		}
 		else
 		{
 			int result = JOptionPane.showConfirmDialog(this, "Discard unsaved changes and close tab?", "Discard Changes?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 			
 			return result == 0;
 		}
 	}
 	
 	/**
 	 * @return the requirement information panel.
 	 */
 	public RequirementInformationPanel getInfoPanel()
 	{
 		return this.infoPanel;
 	}
 	
 	/**
 	 * @return the button panel
 	 */
 	public RequirementButtonPanel getButtonPanel()
 	{
 		return this.buttonPanel;
 	}
 	
 	/**
 	 * @return the display requirement.
 	 */
 	public Requirement getDisplayRequirement() {
 		return displayRequirement;
 	}
 }
