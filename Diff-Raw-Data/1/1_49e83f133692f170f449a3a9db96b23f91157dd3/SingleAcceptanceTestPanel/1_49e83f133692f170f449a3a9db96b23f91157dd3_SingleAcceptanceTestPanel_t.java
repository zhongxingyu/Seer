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
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Iterator;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.Border;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.AcceptanceTest;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.TestStatus;
 
 /**
  * Class that creates a panel that is used to represent an acceptance test
  * 
  * @author Brian Froehlich
  *
  */
 @SuppressWarnings("serial")
 public class SingleAcceptanceTestPanel extends JPanel 
 {
 	
 	private AcceptanceTest test;
 	private Requirement requirement;
 
 	@SuppressWarnings("unchecked")
 	public SingleAcceptanceTestPanel(Requirement req, AcceptanceTest test)
 	{
 		this.requirement = req;
 		this.test = test;
 		
 		// Set border to black
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 		
 		// Create a text area containing the test's description
 		JTextArea description = new JTextArea(test.getDescription());
 		description.setLineWrap(true);
 		description.setWrapStyleWord(true);
 		description.setEditable(false); // Do not allow to be edited
 		
 		// Give the description a black border with 2px padding inside
 		Border b = BorderFactory.createCompoundBorder(
 				BorderFactory.createLineBorder(Color.black), 
 		           BorderFactory.createEmptyBorder(2, 2, 2, 2));
 		description.setBorder(b);
 		
 		// Get the title and place it in a label
 		JLabel testName = new JLabel(" "+test.getName());
 		
 		// Get status and set drop down box to correct status
 		JComboBox dropdownStatus = new JComboBox(TestStatus.values());
 		dropdownStatus.setBackground(Color.WHITE);
 		if (test.getStatus().equals("")) {
 			dropdownStatus.setSelectedItem(TestStatus.STATUS_BLANK);
 		} else if (test.getStatus().equals("Passed")) {
 			dropdownStatus.setSelectedItem(TestStatus.STATUS_PASSED);
 		} else if (test.getStatus().equals("Failed")) {
 			dropdownStatus.setSelectedItem(TestStatus.STATUS_FAILED);
 		}
 
 		final Requirement finalReq = requirement;
 		final AcceptanceTest finalTest = test;
 		ItemListener itemListener = new ItemListener() {
 		      public void itemStateChanged(ItemEvent itemEvent) {
 		    	  updateRequirementTest((TestStatus)itemEvent.getItem());
 		      }
 		    };
 		dropdownStatus.addItemListener(itemListener);
 
 		// Create panel for dropdown status
 		JPanel statusPanel = new JPanel();
 		statusPanel.add(new JLabel("Status: "));
 		statusPanel.add(dropdownStatus);
 		
 		// Create a layout manager for this test panel
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints testConstraints = new GridBagConstraints();
 		
 		testConstraints.anchor = GridBagConstraints.WEST; // Display title in top-left
 		testConstraints.fill = GridBagConstraints.NONE; // Don't fill elements
 		testConstraints.gridy = 0; // Row 0
 		testConstraints.gridx = 0; // Column 0
 		this.add(testName, testConstraints); // Add info to testPanel
 		
 		testConstraints.gridx = 1; // Column 1
 		testConstraints.anchor = GridBagConstraints.NORTHEAST; // Display status in top-right
 		this.add(statusPanel, testConstraints); // Add status to panel
 		
 		testConstraints.fill = GridBagConstraints.HORIZONTAL; // Fill elements horizontally
 		testConstraints.gridx = 0; // Column 0
 		testConstraints.gridy = 1; //Row 1
 		testConstraints.gridwidth = 2; // Fill 2 columns
 		testConstraints.weightx = 1; //Fill the width
 		testConstraints.insets = new Insets(2,2,2,2); //2px margin
 		this.add(description, testConstraints); // Add description to testPanel
 	}
 	
 	private void updateRequirementTest(TestStatus newStatus) {
   	  	requirement.updateTestStatus(test.getId(), newStatus);
   	  	UpdateRequirementController.getInstance().updateRequirement(requirement);
 	}
 	
 	/**
 	 * Creates a panel containing all of the notes passed to it in the list
 	 * @param list List of note used to create panel
 	 * @return Panel containing all of the notes given to the method
 	 */
 	public static JPanel createList(Requirement req)
 	{
 		// Create a panel to hold all of the notes
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.WHITE); // Background color is white
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints(); // Create layout for adding notes
 		c.gridy = GridBagConstraints.RELATIVE; // Make a new row and add it to it
 		c.anchor = GridBagConstraints.NORTH; // Anchor to top of panel
 		c.fill = GridBagConstraints.HORIZONTAL; // Fill elements horizontally
 		c.weightx = 1;//Fill horizontally
 		c.gridy = 0; //Row 0
 		c.insets = new Insets(5,5,5,5); // Creates margins between notes
 		
 		// Get iterator of the list of notes
 		Iterator<AcceptanceTest> itt = req.getTests().iterator();
 		
 		// Add each note to panel individually
 		while(itt.hasNext())
 		{
 			//Create a new NotePanel for each Note and add it to the panel
 			panel.add(new SingleAcceptanceTestPanel(req, itt.next()), c);
 			c.gridy++; //Next Row
 		}
 		
 		//Create a dummy panel to take up space at the bottom
 		c.weighty = 1;
 		JPanel dummy = new JPanel();
 		dummy.setBackground(Color.WHITE);
 		panel.add(dummy,c);
 		
 		return panel;
 	}
 	
 	public AcceptanceTest getTest() {
 		return test;
 	}
 
 	public void setTest(AcceptanceTest test) {
 		this.test = test;
 	}
 }
