 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.overview;
 
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.DropMode;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.GetRequirementsController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.iterationcontroller.GetIterationController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 
 /**
  * @author justinhess
  * @version $Revision: 1.0 $
  */
 public class OverviewTable extends JTable
 {
 	private DefaultTableModel tableModel = null;
 	private boolean initialized;
 	private boolean isInEditMode;
	private boolean changedByRefresh = false;
 	/**
 	 * Sets initial table view
 	 * 
 	 * @param data	Initial data to fill OverviewTable
 	 * @param columnNames	Column headers of OverviewTable
 	 */
 	public OverviewTable(Object[][] data, String[] columnNames)
 	{
 		this.tableModel = new DefaultTableModel(data, columnNames);
 		this.setModel(tableModel);
 		this.setDefaultRenderer(Object.class, new OverviewTableCellRenderer());
 		this.setDefaultEditor(Object.class, new OverviewTableEstimateCellEditor(new JTextField()));
 		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		this.setDragEnabled(true);
         this.setDropMode(DropMode.ON);
         this.setTransferHandler(new OverviewTableTransferHandler(this));
 
 		this.getTableHeader().setReorderingAllowed(false);
 		this.setAutoCreateRowSorter(true);
 		setFillsViewportHeight(true);
 		isInEditMode = false;
 
 		ViewEventController.getInstance().setOverviewTable(this);
 		initialized = false;
 
 		/* Create double-click event listener */
 		this.addMouseListener(new MouseAdapter(){
 			public void mouseClicked(MouseEvent e){
 				
 				if(getRowCount() > 0)
 				{
 					int mouseY = e.getY();
 					Rectangle lastRow = getCellRect(getRowCount() - 1, 0, true);
 					int lastRowY = lastRow.y + lastRow.height;
 
 					if(mouseY > lastRowY) 
 					{
 						getSelectionModel().clearSelection();
 						repaint();
 					}
 				}
 				
 				// only allow edit requirement panel to pop up outside of Multiple Requirement Editing Mode
 				if ((e.getClickCount() == 2) && !isInEditMode)
 				{
 					ViewEventController.getInstance().editSelectedRequirement();
 				}
 			}
 		});
 		 System.out.println("finished constructing the table");
 	}
 	
 	/**
 	 * updates OverviewTable with the contents of the requirement model	 * 
 	 */
 	public void refresh() {
 		List<Requirement> requirements = RequirementModel.getInstance().getRequirements();
 		
 		String[] pastEst = new String[requirements.size()];
 		
 		if (isInEditMode){
 			// store all the estimates currently in the table if in Mult. Req. Editing mode
 			for (int i = 0; i < this.getRowCount(); i++) {
 				pastEst[i] = String.valueOf(this.tableModel.getValueAt(i, 7));
 			}
 			
 			pastEst[requirements.size() - 1] = String.valueOf(requirements.get(requirements.size() - 1).getEstimate());
 			
 			// indicate that refresh is about to affect the table
 			setChangedByRefresh(true);
 		}		
 				
 		// clear the table
 		tableModel.setRowCount(0);		
 		
 		for (int i = 0; i < requirements.size(); i++) {
 			Requirement req = requirements.get(i);			
 			String currEst = String.valueOf(req.getEstimate());
 			
 			// re-enter the value last in the cell if in Mult. Req. Editing mode and req estimate not just edited
 			if (isInEditMode && (currEst != pastEst[i]) && (!req.getEstimateEdited())) currEst = pastEst[i];			
 			
 			tableModel.addRow(new Object[]{ req.getId(), 
 					req,
 					req.getRelease(),
 					req.getIteration(),
 					req.getType(),
 					req.getStatus(),
 					req.getPriority(),
 					currEst
 			});	
 			req.setEstimateEdited(false);
 		}
 		// indicate that refresh is no longer affecting the table
 		setChangedByRefresh(false);
 		
 		System.out.println("finished refreshing the table");		
 	}
 	
 	/**
 	 * Overrides the isCellEditable method to ensure no cells are editable.
 	 * 
 	 * @param row	row of OverviewTable cell is located
 	 * @param col	column of OverviewTable cell is located
 	
 	 * @return boolean */
 	@Override
 	public boolean isCellEditable(int row, int col)
 	{
 		// extract the ID number displayed in the row
     	String rowIDstr = this.getValueAt(row, 0).toString();
     	int rowID = Integer.parseInt(rowIDstr);
     	// retrieve the requirement with ID rowID and the requirement's estimate 
     	Requirement req = RequirementModel.getInstance().getRequirement(rowID);
     	   	
 		// if the column contains the estimate, the requirement is not deleted, in progress or completed,
     	// and the table is in Multiple Requirement Editing mode, make the cell editable
 		if ((col == 7) && (isInEditMode) && (!req.isDeleted())
 										 &&	(req.getStatus() != RequirementStatus.COMPLETE)
 										 &&	(req.getStatus() != RequirementStatus.INPROGRESS)) {
 			return true;
 		}	
 		
 		return false;
 	}
 	
 	/**
 	 * Used to toggle the isInEditMode to indicate whether the requirements in the Overview table are 
 	 * being edited or not 
 	 * 
 	 * @param beingEdited
 	 */
 	public void setEditFlag(boolean beingEdited) {
 		isInEditMode = beingEdited;
 	}
 	
 	
 	/**
 	
 	 * @return isInEditMode */
 	public boolean getEditFlag(){
 		return isInEditMode;
 	}
 	
 	
 	/**
 	
 	 * @return the changedByRefresh */
 	public boolean wasChangedByRefresh() {
 		return changedByRefresh;
 	}
 
 	/**
 	 * @param changedByRefresh the changedByRefresh to set
 	 */
 	public void setChangedByRefresh(boolean changedByRefresh) {
 		this.changedByRefresh = changedByRefresh;
 	}
 
 	/**
 	 * Overrides the paintComponent method to retrieve the requirements on the first painting.
 	 * 
 	 * @param g	The component object to paint
 	 */
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		if(!initialized)
 		{
 			try 
 			{
 				GetRequirementsController.getInstance().retrieveRequirements();
 				GetIterationController.getInstance().retrieveIterations();
 				initialized = true;
 			}
 			catch (Exception e)
 			{
 
 			}
 		}
 
 		super.paintComponent(g);
 	}
 	
 	/**
 	 * saves the changes made to the Overview Table
 	 */
 	public void saveChanges() {
 		// Set time stamp for transaction history
 		long timestamp = System.currentTimeMillis();	
 		
 		// iterate through the rows of the overview table
 		for (int row = 0; row < this.tableModel.getRowCount(); row++) {
 			
 			// extract the ID number displayed in the row
 			String rowIDstr = this.tableModel.getValueAt(row, 0).toString();
 			int rowID = Integer.parseInt(rowIDstr);
 			
 			// use the ID number in the row to retrieve the requirement represented by the row
 			Requirement req = RequirementModel.getInstance().getRequirement(rowID);
 			
 			// indicate that the requirement were not just created
 			req.setWasCreated(false);
 									
 			// Set the time stamp for the transaction for the creation of the requirement
 			req.getHistory().setTimestamp(timestamp);
 			
 			// update the estimate with the value in the cell at row, column 7			
 			String cellEstimateStr = this.tableModel.getValueAt(row, 7).toString();
 			int cellEstimate = req.getEstimate();
 			boolean formatError = false;
 			// make sure the value in the cell is a valid integer
 			try {
 				cellEstimate = Integer.parseInt(cellEstimateStr);
 			}
 			catch (NumberFormatException nfe){
 				formatError = true;
 			}
 			
 			if (formatError) {
 				cellEstimate = req.getEstimate();
 				this.setValueAt(cellEstimate, row, 7);
 			}
 			else {
 				cellEstimate = Integer.parseInt(cellEstimateStr);
 			}
 			req.setEstimate(cellEstimate);
 			
 			// updates requirement on the server
 			UpdateRequirementController.getInstance().updateRequirement(req);			
 		}			
 		
 		// refresh table to get rid of cell highlights
 		this.refresh();
 	}
 
 	/**	
 	
 	 * @return true if there are unsaved, saveable changes in the Overview Table   */
 	public boolean hasChanges() {
 				
 		// iterate through the rows of the overview table
 		for (int row = 0; row < this.tableModel.getRowCount(); row++) {
 
 			// extract the ID number displayed in the row
 			String rowIDstr = this.tableModel.getValueAt(row, 0).toString();
 			int rowID = Integer.parseInt(rowIDstr);
 
 			// use the ID number in the row to retrieve the requirement represented by the row
 			Requirement req = RequirementModel.getInstance().getRequirement(rowID);
 
 			// extract the string from the estimate column			
 			String cellEstimateStr = this.tableModel.getValueAt(row, 7).toString();
 
 			boolean formatError = false;
 			int cellEstimate = 0;
 			
 			// check to see if the value in the cell is a valid integer
 			try {
 				cellEstimate = Integer.parseInt(cellEstimateStr);
 			}
 			catch (NumberFormatException nfe){
 				formatError = true;
 			}
 			
 			if (!formatError) {
 				// if the valid cell estimate is not equal to the requirement estimate,
 				// indicate that a change has been found by returning true
 				if (cellEstimate != req.getEstimate()) return true;						
 			}
 		}
 
 		// indicate that no changes were found by returning false
 		return false;
 	}
 }
