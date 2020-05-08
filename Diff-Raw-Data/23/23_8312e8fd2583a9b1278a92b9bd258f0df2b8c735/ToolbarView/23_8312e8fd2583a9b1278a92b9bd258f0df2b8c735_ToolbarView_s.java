 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 import javax.swing.border.EtchedBorder;
 
 /**
  * Sets up upper toolbar of RequirementManager tab
  * 
  * @author Arianna
  *
  */
 public class ToolbarView extends JPanel {
 	final JButton createEditButton = new JButton("Edit Estimates");
 	final JButton createCancelButton = new JButton("Cancel Changes");
 	final JButton createIterationButton = new JButton("Create Iteration");
 	/**
 	 * Creates and positions option buttons in upper toolbar
 	 */
 	public ToolbarView(boolean visible) {
 		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED)); // add a border so you can see the panel
 		
 		SpringLayout toolbarLayout = new SpringLayout();
 		this.setLayout(toolbarLayout);
 		
 		// initialize the main view toolbar buttons
 		JButton createButton = new JButton("Create Requirement");
 		
 		createCancelButton.setVisible(false);
 
 		// the action listener for the Create Requirement Button
 		createButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// bring up a create requirement pane if not in Multiple Requirement Editing Mode
 				if (!ViewEventController.getInstance().getOverviewTable().getEditFlag()) {
 					ViewEventController.getInstance().createRequirement();
 				}
 			}
 		});		
 		
 		this.createEditButton.setVisible(visible);
 		// the action listener for the Edit Estimates button
 		createEditButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// check to see if any other tab is currently open
 				if (ViewEventController.getInstance().getMainView().getTabCount() == 1) {
 					// toggle the editing overview table mode
 					ViewEventController.getInstance().toggleEditingTable(false);
 					// edits the Edit Button text based on whether in editing overview table mode or not
 					if (ViewEventController.getInstance().getOverviewTable().getEditFlag()) {
 						createEditButton.setText("Save Changes");
 						createCancelButton.setVisible(true);
 					}	
 					else {
 						createEditButton.setText("Edit Estimates");
 						createCancelButton.setVisible(false);
 					}
 				}
 			}
 		});
 		
 		createCancelButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// toggle the editing overview table mode
 				ViewEventController.getInstance().toggleEditingTable(true);
 
 				createEditButton.setText("Edit Estimates");
 				createCancelButton.setVisible(false);
 
 			}
 		});
 		JButton pieChart = new JButton("Pie Chart");
 		
 		pieChart.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
				ViewEventController.getInstance().createPieChart("Status");
				
				
				
				
 			}
 		});
 		
 		// the action listener for the Create Requirement Button
 		createIterationButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// bring up a create requirement pane if not in Multiple Requirement Editing Mode
 				if (!ViewEventController.getInstance().getOverviewTable().getEditFlag()) {
 					ViewEventController.getInstance().createIteration();
 				}
 			}
 		});
 		
 		toolbarLayout.putConstraint(SpringLayout.NORTH, createButton, 25,SpringLayout.NORTH, this);
 		toolbarLayout.putConstraint(SpringLayout.WEST, createButton, 50, SpringLayout.WEST, this);
 		this.add(createButton);
 		
 		toolbarLayout.putConstraint(SpringLayout.NORTH, createEditButton, 5,SpringLayout.NORTH, this);
 		toolbarLayout.putConstraint(SpringLayout.WEST, createEditButton, 200, SpringLayout.WEST, this);
 		this.add(createEditButton);
 		
 		toolbarLayout.putConstraint(SpringLayout.NORTH, createCancelButton, 5,SpringLayout.SOUTH, createEditButton);
 		toolbarLayout.putConstraint(SpringLayout.WEST, createCancelButton, 200, SpringLayout.WEST, this);
 		this.add(createCancelButton);
 		
 		toolbarLayout.putConstraint(SpringLayout.NORTH, createIterationButton, 5, SpringLayout.NORTH, this);
 		toolbarLayout.putConstraint(SpringLayout.EAST, createIterationButton, -5, SpringLayout.EAST, this);
 		this.add(createIterationButton);
 		
 		this.add(pieChart);
 
 	}
 	
 	public JButton getEditButton() {
 		return this.createEditButton;
 	}
 }
