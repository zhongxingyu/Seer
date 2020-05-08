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
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.iterationcontroller.UpdateIterationController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.ErrorPanel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanelListener;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.ViewMode;
 
 /**
  *
  * @version $Revision: 1.0 $
  */
 public class IterationPanel extends JPanel implements KeyListener{
 	private final String START_AFTER_END_ERROR = "Start date cannot be after end date.";
 	private final String OVERLAPPING_ERROR = "Iteration dates cannot overlap.";
 	private final String INVALID_NAME_ERROR = "Iteration exists with given name.";
 	private final String EMPTY_NAME_ERROR = "Name is required.";
 	private final String DATES_REQ = "Start and end date required.";
 	private final String PAST_ERROR = "Iteration cannot occur in the past.";
 	
 	private IterationRequirements requirements;
 	private IterationCalendarPanel calPanel;
 	
 	private JTextField boxName;
 	
 	private JPanel buttonPanel;
 	
 	private JFormattedTextField startDateBox;
 	private JFormattedTextField endDateBox;
 	
 	private JButton buttonAdd;
 	private JButton buttonCancel;
 	private JButton buttonUndoChanges;
 	
 	private ErrorPanel errorDisplay;
 	
 	private ViewMode vm;
 	private Iteration displayIteration;
 	
 	private boolean forceRemove = false;
 	
 	/**
 	 * The constructor for the iteration panel when creating an iteration.
 	 */
 	public IterationPanel() {
 		this.vm = ViewMode.CREATING;
 		displayIteration = new Iteration();
 		buildLayout();
 	}
 	
 	/**
 	 * Constructor for the iteration panel when editing an iteration
 	 * @param iter the iteration to edit.
 	 */
 	public IterationPanel(Iteration iter) {
 		this.vm = ViewMode.EDITING;
 		displayIteration = iter;
 		buildLayout();
 		populateInformation();
		validateFields();
 	}
 	
 	/**
 	 * Builds the GUI layout for the iteration panel
 	 */
 	private void buildLayout(){
 		this.setLayout(new BorderLayout());
 		MigLayout layout = new MigLayout();
 
 		JPanel contentPanel = new JPanel(layout);
 
 		JLabel labelName = new JLabel("Name: ");
 
 		boxName = new JTextField();
 		boxName.setPreferredSize(new Dimension(200, 20));
 		boxName.addKeyListener(this);
 		
 		JLabel labelStart = new JLabel("Start Date: ");
 		JLabel labelEnd = new JLabel("End Date: ");
 		
 		startDateBox = new JFormattedTextField();
 		startDateBox.setEnabled(false);
 		startDateBox.setPreferredSize(new Dimension(150, 20));
 		endDateBox = new JFormattedTextField();
 		endDateBox.setEnabled(false);
 		endDateBox.setPreferredSize(new Dimension(150, 20));
 		
 		JLabel dateInstructions = new JLabel("Drag in the calendar below to select dates.");
 		
 		contentPanel.add(labelName, "left");
 		contentPanel.add(boxName, "left");
 		contentPanel.add(labelStart, "left");
 		contentPanel.add(startDateBox, "left");
 		contentPanel.add(labelEnd, "left");
 		contentPanel.add(endDateBox, "left,wrap");
 		contentPanel.add(dateInstructions, "left, span,cell 3 1");
 		
 		String addText = vm == ViewMode.EDITING ? "Update Iteration" : "Add Iteration";
 		
 		buttonAdd = new JButton(addText);
 		buttonAdd.setAlignmentX(LEFT_ALIGNMENT);
 		buttonAdd.setEnabled(false);
 
 		buttonUndoChanges = new JButton("Undo Changes");
 		buttonUndoChanges.setAlignmentX(LEFT_ALIGNMENT);
 		buttonUndoChanges.setEnabled(false);
 		buttonUndoChanges.setVisible(vm == ViewMode.EDITING);
 		
 		buttonCancel = new JButton("Cancel");
 		buttonCancel.setAlignmentX(LEFT_ALIGNMENT);
 		
 		errorDisplay = new ErrorPanel();
 		
 		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
 		buttonPanel.add(buttonAdd);
 		buttonPanel.add(buttonUndoChanges);
 		buttonPanel.add(buttonCancel);
 		buttonPanel.add(errorDisplay);
 		
 		buttonAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				updateIteration();
 			}
 		});
 		
 		buttonUndoChanges.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				populateInformation();
 				buttonUndoChanges.setEnabled(false);
 			}
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ViewEventController.getInstance().refreshTable();
 				ViewEventController.getInstance().refreshTree();
 				ViewEventController.getInstance().removeTab(IterationPanel.this);
 			}
 		});
 		
 		JTabbedPane tabs = new JTabbedPane();
 
 		calPanel = new IterationCalendarPanel(this, vm, displayIteration);
 		tabs.addTab("Iteration Calendar", calPanel);
 		
 		requirements = new IterationRequirements();
 		tabs.addTab("Requirements", requirements);
 
 	
 		this.add(contentPanel, BorderLayout.NORTH);
 		this.add(tabs, BorderLayout.CENTER);
 		this.add(buttonPanel, BorderLayout.SOUTH);
				
		validateFields();
 	}
 	
 	/**
 	 * Updates the display iteration
 	 */
 	private void updateIteration()
 	{
 		String name = boxName.getText();
 		displayIteration.setName(name);
 		displayIteration.setDateInterval((Date)startDateBox.getValue(), (Date)endDateBox.getValue());
 
 		if(vm == ViewMode.CREATING)
 		{
 			int id = IterationModel.getInstance().getNextID();
 			displayIteration.setId(id);
 			IterationModel.getInstance().addIteration(displayIteration);
 			
 		}
 		else
 		{
 			UpdateIterationController.getInstance().updateIteration(displayIteration);
 		}
 		
 		forceRemove = true;
 		ViewEventController.getInstance().removeTab(IterationPanel.this);
 
 	}
 	
 	/**
 	 * Populate the information in the iteration panel for
 	 * the display requirement.
 	 */
 	private void populateInformation()
 	{
 		this.boxName.setText(displayIteration.getName());
 		this.startDateBox.setValue(displayIteration.getStart().getDate());
 		this.endDateBox.setValue(displayIteration.getEnd().getDate());
 		this.calPanel.getIterationCalendar().setSelectionInterval(displayIteration.getStart().getDate(), displayIteration.getEnd().getDate());
 	}
 	
 	/**
 	 * Sets the dates for the iteration panel
 	 * @param startDate the start
 	 * @param endDate the end
 	 */
 	public void setDates(Date startDate, Date endDate)
 	{
 		if(startDate != null) this.startDateBox.setValue(startDate);
 		if(endDate != null) this.endDateBox.setValue(endDate);
 		
 		validateFields();
 		checkForChanges();
 	}
 	
 	/**
 	 * Validates the options of the fields inputted.
 	 */
 	private void validateFields()
 	{
 		errorDisplay.removeAllErrors();
 		Calendar cal = new GregorianCalendar();
 		cal.setTime(Calendar.getInstance().getTime());
 		cal.add(Calendar.DAY_OF_YEAR, -1);
 		Iteration forName = IterationModel.getInstance().getIteration(boxName.getText().trim());
 		if(boxName.getText().trim().length() == 0)
 		{
 			errorDisplay.displayError(EMPTY_NAME_ERROR);
 		}
 		else if(forName != null && forName != displayIteration)
 		{
 			errorDisplay.displayError(INVALID_NAME_ERROR);
 		}
 		
 		if(endDateBox.getText().trim().length() == 0 || endDateBox.getText().trim().length() == 0)
 		{
 			errorDisplay.displayError(DATES_REQ);
 		}
 		else if(((Date)startDateBox.getValue()).after((Date)endDateBox.getValue()))
 		{
 			errorDisplay.displayError(START_AFTER_END_ERROR);
 		}
 		else if(((Date)startDateBox.getValue()).before(cal.getTime()))
 		{
 			errorDisplay.displayError(PAST_ERROR);
 		}
 		else
 		{
 			Iteration conflicting = IterationModel.getInstance().getConflictingIteration((Date)startDateBox.getValue(), (Date)endDateBox.getValue());
 			if(conflicting != null && conflicting != displayIteration)
 			{
 				errorDisplay.displayError(OVERLAPPING_ERROR + " Overlaps with " + conflicting.getName() + ".");
 			}
 		}
 		
 		buttonAdd.setEnabled(!errorDisplay.hasErrors());
 	}
 	
 	/**
 	 * Checks whether anything changed and updates buttons as needed.
 	 */
 	public boolean checkForChanges()
 	{
 		boolean nameChanged = false;
 		boolean startChanged = false;
 		boolean endChanged = false;
 		if(vm == ViewMode.CREATING)
 		{
 			
 			nameChanged = !boxName.getText().trim().equals("");
 			startChanged = !startDateBox.getText().equals("");
 			endChanged = !endDateBox.getText().equals("");
 		}
 		else
 		{
 			nameChanged = !boxName.getText().equals(displayIteration.getName());
 			Date startDate = (Date)startDateBox.getValue();
 			Date endDate = (Date)endDateBox.getValue();
 			
 			startChanged = !startDate.equals(displayIteration.getStart().getDate());
 			endChanged = !endDate.equals(displayIteration.getEnd().getDate());
 		}
 		
 		boolean anythingChanged = nameChanged || startChanged || endChanged;
 		buttonAdd.setEnabled(buttonAdd.isEnabled() && anythingChanged);
 		buttonUndoChanges.setEnabled(anythingChanged);
 		return anythingChanged;
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
		validateFields();
		checkForChanges();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
		validateFields();
		checkForChanges();
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
		validateFields();
		checkForChanges();
 	}
 
 	/**
 	 * 
 	 * @return the display iteration
 	 */
 	public Iteration getDisplayIteration() {
 		return displayIteration;
 	}
 
 	/**
 	
 	 * @return whether the iteration panel as a whole is ready to be removed. */
 	public boolean readyToRemove() {
 		boolean readyToRemove;
 		
 		if(forceRemove) return true;
 		
 		if(checkForChanges())
 		{
 			int result = JOptionPane.showConfirmDialog(this, "Discard unsaved changes and close tab?", "Discard Changes?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 			
 			readyToRemove = result == 0;		
 		}
 		else
 		{
 			readyToRemove = true;
 		}
 		
 		return readyToRemove;
 	}
 	
 	
 }
