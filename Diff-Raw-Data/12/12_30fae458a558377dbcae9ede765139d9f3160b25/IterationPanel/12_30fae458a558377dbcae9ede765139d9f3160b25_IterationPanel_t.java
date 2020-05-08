 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.iterations;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.IterationDate;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.Month;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 
 /**
  * 
  *
  */
 public class IterationPanel extends JPanel {
 	private JTextField boxName;
 	
 	private JComboBox<Month> monthStart;
 	private JTextField dayStart;
 	private JTextField yearStart;
 	
 	private JComboBox<Month> monthEnd;
 	private JTextField dayEnd;
 	private JTextField yearEnd;
 	
 	private JButton buttonAdd;
 	private JButton buttonCancel;
 	
 	public IterationPanel(){
 		SpringLayout layout = new SpringLayout();
 		this.setLayout(layout);
 		
 		JLabel labelName = new JLabel("Name: ");
 		boxName = new JTextField();
 		boxName.setPreferredSize(new Dimension(200, 20));
 		
 		JLabel labelStart = new JLabel("Start Date: ");
 		JLabel labelEnd = new JLabel("End Date: ");
 		
 		JLabel labelMonthStart = new JLabel("Month: ");
 		monthStart = new JComboBox(Month.values());
 		
 		JLabel labelDayStart = new JLabel("Day: ");
 		dayStart = new JTextField(2);
 		
 		JLabel labelYearStart = new JLabel("Year: ");
 		yearStart = new JTextField(4);
 		
 		JLabel labelMonthEnd = new JLabel("Month: ");
 		monthEnd = new JComboBox(Month.values());
 		
 		JLabel labelDayEnd = new JLabel("Day: ");
 		dayEnd = new JTextField(2);
 		
 		JLabel labelYearEnd = new JLabel("Year: ");
 		yearEnd = new JTextField(4);
 		
 		buttonAdd = new JButton("Add Iteration");
 		buttonCancel = new JButton("Cancel");
 		
 		final IterationPanel thisPanel = this;
 		
 		buttonAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				int error = 0;
				
 				int id = IterationModel.getInstance().getIterations().size();
 				String name = boxName.getText();
				if (name.length() == 0) {
					error = 1;
					System.out.println("Error");
				}
 				IterationDate start = new IterationDate((Month) monthStart.getSelectedItem(),
 						Integer.parseInt(dayStart.getText()),
 						Integer.parseInt(yearStart.getText()));
 				IterationDate end = new IterationDate((Month) monthEnd.getSelectedItem(),
 						Integer.parseInt(dayEnd.getText()),
 						Integer.parseInt(yearEnd.getText()));
 				Iteration iter = new Iteration(id, name, start, end);
 				
				if (error == 0) {
					IterationModel.getInstance().addIteration(iter);
				}
 				ViewEventController.getInstance().removeTab(thisPanel);
 			}
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ViewEventController.getInstance().refreshTable();
 				ViewEventController.getInstance().removeTab(thisPanel);
 			}
 		});
 		
 		layout.putConstraint(SpringLayout.NORTH, labelName, 5, SpringLayout.NORTH, this);
 		layout.putConstraint(SpringLayout.WEST, labelName, 5, SpringLayout.WEST, this);
 		
 		layout.putConstraint(SpringLayout.NORTH, boxName, 5, SpringLayout.NORTH, this);
 		layout.putConstraint(SpringLayout.WEST, boxName, 5, SpringLayout.EAST, labelName);
 		
 		layout.putConstraint(SpringLayout.NORTH, labelStart, 10, SpringLayout.SOUTH, labelName);
 		layout.putConstraint(SpringLayout.WEST, labelStart, 5, SpringLayout.WEST, this);
 		
 		layout.putConstraint(SpringLayout.NORTH, labelMonthStart, 10, SpringLayout.SOUTH, labelStart);
 		layout.putConstraint(SpringLayout.WEST, labelMonthStart, 5, SpringLayout.WEST, this);
 		
 		layout.putConstraint(SpringLayout.NORTH, monthStart, 10, SpringLayout.SOUTH, labelStart);
 		layout.putConstraint(SpringLayout.WEST, monthStart, 5, SpringLayout.EAST, labelMonthStart);
 
 		layout.putConstraint(SpringLayout.NORTH, labelDayStart, 10, SpringLayout.SOUTH, labelStart);
 		layout.putConstraint(SpringLayout.WEST, labelDayStart, 5, SpringLayout.EAST, monthStart);
 		
 		layout.putConstraint(SpringLayout.NORTH, dayStart, 10, SpringLayout.SOUTH, labelStart);
 		layout.putConstraint(SpringLayout.WEST, dayStart, 5, SpringLayout.EAST, labelDayStart);
 		
 		layout.putConstraint(SpringLayout.NORTH, labelYearStart, 10, SpringLayout.SOUTH, labelStart);
 		layout.putConstraint(SpringLayout.WEST, labelYearStart, 5, SpringLayout.EAST, dayStart);
 		
 		layout.putConstraint(SpringLayout.NORTH, yearStart, 10, SpringLayout.SOUTH, labelStart);
 		layout.putConstraint(SpringLayout.WEST, yearStart, 5, SpringLayout.EAST, labelYearStart);
 		
 		layout.putConstraint(SpringLayout.NORTH, labelEnd, 20, SpringLayout.SOUTH, labelMonthStart);
 		layout.putConstraint(SpringLayout.WEST, labelEnd, 5, SpringLayout.WEST, this);
 		
 		layout.putConstraint(SpringLayout.NORTH, labelMonthEnd, 10, SpringLayout.SOUTH, labelEnd);
 		layout.putConstraint(SpringLayout.WEST, labelMonthEnd, 5, SpringLayout.WEST, this);
 		
 		layout.putConstraint(SpringLayout.NORTH, monthEnd, 10, SpringLayout.SOUTH, labelEnd);
 		layout.putConstraint(SpringLayout.WEST, monthEnd, 5, SpringLayout.EAST, labelMonthEnd);
 
 		layout.putConstraint(SpringLayout.NORTH, labelDayEnd, 10, SpringLayout.SOUTH, labelEnd);
 		layout.putConstraint(SpringLayout.WEST, labelDayEnd, 5, SpringLayout.EAST, monthEnd);
 		
 		layout.putConstraint(SpringLayout.NORTH, dayEnd, 10, SpringLayout.SOUTH, labelEnd);
 		layout.putConstraint(SpringLayout.WEST, dayEnd, 5, SpringLayout.EAST, labelDayEnd);
 		
 		layout.putConstraint(SpringLayout.NORTH, labelYearEnd, 10, SpringLayout.SOUTH, labelEnd);
 		layout.putConstraint(SpringLayout.WEST, labelYearEnd, 5, SpringLayout.EAST, dayEnd);
 		
 		layout.putConstraint(SpringLayout.NORTH, yearEnd, 10, SpringLayout.SOUTH, labelEnd);
 		layout.putConstraint(SpringLayout.WEST, yearEnd, 5, SpringLayout.EAST, labelYearEnd);
 		
 		layout.putConstraint(SpringLayout.SOUTH, buttonAdd, -5, SpringLayout.SOUTH, this);
 		layout.putConstraint(SpringLayout.WEST, buttonAdd, 5, SpringLayout.WEST, this);
 		
 		layout.putConstraint(SpringLayout.NORTH, buttonCancel, 0, SpringLayout.NORTH, buttonAdd);
 		layout.putConstraint(SpringLayout.WEST, buttonCancel, 5, SpringLayout.EAST, buttonAdd);
 		
 		this.add(labelName);
 		this.add(boxName);
 		this.add(labelStart);
 		this.add(labelMonthStart);
 		this.add(monthStart);
 		this.add(labelDayStart);
 		this.add(dayStart);
 		this.add(labelYearStart);
 		this.add(yearStart);
 		this.add(labelEnd);
 		this.add(labelMonthEnd);
 		this.add(monthEnd);
 		this.add(labelDayEnd);
 		this.add(dayEnd);
 		this.add(labelYearEnd);
 		this.add(yearEnd);
 		this.add(buttonAdd);
 		this.add(buttonCancel);
 	}
 }
