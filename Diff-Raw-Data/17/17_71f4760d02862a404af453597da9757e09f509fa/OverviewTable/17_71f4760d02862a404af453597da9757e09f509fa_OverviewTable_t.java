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
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.GetRequirementsController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.iterationcontroller.GetIterationController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 
 public class OverviewTable extends JTable
 {
 	private DefaultTableModel tableModel = null;
 	private boolean initialized;
 	private boolean isInEditMode;
 	/**
 	 * Sets initial table view
 	 * 
 	 * @param data	Initial data to fill OverviewTable
 	 * @param columnNames	Column headers of OverviewTable
 	 */
 	public OverviewTable(Object[][] data, String[] columnNames)
 	{
 		this.tableModel = new DefaultTableModel(columnNames, 0);
 		this.setModel(tableModel);
 		this.setDefaultRenderer(Object.class, new OverviewTableCellRenderer());
 
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
 				
 				if (e.getClickCount() == 2)
 				{
 					ViewEventController.getInstance().editSelectedRequirement();
 				}
 			}
 		});
 	}
 	
 	/**
 	 * updates OverviewTable with the contents of the requirement model
 	 * 
 	 */
 	public void refresh() {
 		tableModel.setRowCount(0); //clear the table
 		
 		List<Requirement> requirements = RequirementModel.getInstance().getRequirements();
 		for (int i = 0; i < requirements.size(); i++) {
 			Requirement req = requirements.get(i);
 			if (!req.isDeleted()) {
 				tableModel.addRow(new Object[]{ req.getId(), 
 												req,
 												req.getRelease(),
 												req.getIteration(),
 												req.getType(),
 												req.getStatus(),
 												req.getPriority(),
 												req.getEstimate()
 												 });
 			}
 		}
 			
 	}
 	
 	/**
 	 * Overrides the isCellEditable method to ensure no cells are editable.
 	 * 
 	 * @param row	row of OverviewTable cell is located
 	 * @param col	column of OverviewTable cell is located
 	 */
 	@Override
 	public boolean isCellEditable(int row, int col)
 	{
 		// if the column contains the estimate and the table is in edit mode, make the cell editable
 		if ((col == 7) && (isInEditMode == true)) return true;
 		
 		else return false;
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
 	 * @return isInEditMode
 	 */
 	public boolean getEditFlag(){
 		return isInEditMode;
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
 		// retrieve the array of requirements
 		List<Requirement> requirements = RequirementModel.getInstance().getRequirements();
 		
 		// iterate through the array of requirements
 		for (int reqIndex = 0; reqIndex < requirements.size(); reqIndex++) {
 			// retrieve the requirement at the index
 			Requirement req = requirements.get(reqIndex);
 									
 			// update the estimate with the value in the cell at row reqIndex, column 7			
 			String cellEstimateStr = this.tableModel.getValueAt(reqIndex, 7).toString();
			int cellEstimate = req.getEstimate();
			boolean formatError = false;
			try {
				cellEstimate = Integer.parseInt(cellEstimateStr);
			}
			catch (NumberFormatException nfe){
				formatError = true;
			}
			
			if (formatError) {
				cellEstimate = req.getEstimate();
				this.setValueAt(cellEstimate, reqIndex, 7);
			}
			else {
				cellEstimate = Integer.parseInt(cellEstimateStr);
			}
 			req.setEstimate(cellEstimate);
 			
 			// updates requirement on the server
 			UpdateRequirementController.getInstance().updateRequirement(req);
 		}		
 	}
 }
