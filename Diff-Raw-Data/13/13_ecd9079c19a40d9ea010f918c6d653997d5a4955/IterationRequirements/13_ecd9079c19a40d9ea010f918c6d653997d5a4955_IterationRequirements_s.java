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
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelector;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelectorListener;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelectorMode;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.ViewMode;
 
 /**
  * 
  *
  * @author justinhess
  * @version $Revision: 1.0 $
  */
 public class IterationRequirements extends JPanel implements RequirementSelectorListener{
 	private JButton removeButton;
 	private JTable requirementTable;
 	private DefaultTableModel tableModel;
 	private IterationPanel parentPanel;
 	private ViewMode viewMode;
 	
 	private Iteration activeIteration;
 	
 	private RequirementSelector reqSelector;
 
 	/**
 	 * Constructor for IterationRequirements.
 	 * @param displayIteration Iteration
 	 */
 	public IterationRequirements(IterationPanel parent, ViewMode vm,  Iteration displayIteration) {
 	
 		activeIteration = displayIteration;
 		parentPanel = parent;
 		viewMode = vm;
 		
 		this.setLayout(new BorderLayout());
 		
 		reqSelector = new RequirementSelector(this, null, RequirementSelectorMode.ITERATION, false);
 		
 		JScrollPane scroll = new JScrollPane();
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		// Always show scroll bar
 		
 		removeButton = new JButton("Remove");
 		removeButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int[] selectedObjects = requirementTable.getSelectedRows();
 				
 				for(int i = 0; i < selectedObjects.length; i++)
 				{
 					Requirement toBeRemoved = (Requirement)requirementTable.getValueAt(selectedObjects[i], 1);
 					toBeRemoved.setIteration("Backlog", false);
 					
 					UpdateRequirementController.getInstance().updateRequirement(toBeRemoved);
 				}
 				refreshTable();
 				ViewEventController.getInstance().refreshTree();
 				ViewEventController.getInstance().refreshTable();
 				reqSelector.refreshList();
 				parentPanel.refreshEstimate();
 			}
 		});
 		removeButton.setEnabled(false);
 		
 		this.add(scroll, BorderLayout.CENTER); // Add scroll pane to panel
 		
 		reqSelector.addButton(0,removeButton);
 		this.add(reqSelector,BorderLayout.SOUTH);
 		
 		requirementTable = buildTable();
 		scroll.setViewportView(requirementTable);
 		
		removeButton.setEnabled(vm == ViewMode.EDITING);
		reqSelector.enableChildren(vm == ViewMode.EDITING);
 		
 		this.refreshTable();
 	}
 	
 	/**
 	 * Method buildTable.
 	 * @return JTable
 	 */
 	private JTable buildTable()
 	{
 		requirementTable = new JTable()
 		{
 			/**
 			 * Overrides the isCellEditable method to ensure no cells are editable.
 			 * 
 			 * @param row	row of OverviewTable cell is located
 			 * @param col	column of OverviewTable cell is located
 			 */
 			@Override
 			public boolean isCellEditable(int row, int col)
 			{
 				return false;
 			}
 		};
 
 		String[] columnNames = {"ID", "Name", "Description", "Release #", "Type", "Status", "Priority", "Estimate"}; 		
 		tableModel = new DefaultTableModel(columnNames, 0);
 		requirementTable.setModel(tableModel);
 
 		requirementTable.getTableHeader().setReorderingAllowed(false);
 		requirementTable.setAutoCreateRowSorter(true);
 		requirementTable.setFillsViewportHeight(true);
 		
 		/* Create double-click event listener */
 		requirementTable.addMouseListener(new MouseAdapter(){
 			public void mouseClicked(MouseEvent e){
 				
 				if(requirementTable.getRowCount() > 0)
 				{
 					int mouseY = e.getY();
 					Rectangle lastRow = requirementTable.getCellRect(requirementTable.getRowCount() - 1, 0, true);
 					int lastRowY = lastRow.y + lastRow.height;
 
 					if(mouseY > lastRowY) 
 					{
 						requirementTable.getSelectionModel().clearSelection();
 						repaint();
 					}
 				}
 				
 				if (e.getClickCount() == 2)
 				{
 					int[] selection = requirementTable.getSelectedRows();
 
 					if(selection.length != 1) return;
 					Requirement toEdit = (Requirement)requirementTable.getValueAt(selection[0], 1);
 					ViewEventController.getInstance().editRequirement(toEdit);
 				}
 			}
 		});
 		
 		requirementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
				removeButton.setEnabled(requirementTable.getSelectedRowCount() > 0);
 			}		
 		});
 		
 		return requirementTable;
 	}
 	
 	private void refreshTable()
 	{
 		if(viewMode == ViewMode.CREATING) return; 
 		tableModel.setRowCount(0); //clear the table
 
 		List<Requirement> requirements = activeIteration.getRequirements();
 
 		for (int i = 0; i < requirements.size(); i++) {
 			Requirement req = requirements.get(i);
 			if (!req.isDeleted()) {
 				tableModel.addRow(new Object[]{ req.getId(),
 						req,
 						req.getDescription(),
 						req.getRelease(),
 						req.getType(),
 						req.getStatus(),
 						req.getPriority(),
 						req.getEstimate()
 				});
 			}
 		}
 	}
 
 	/**
 	 * Method requirementSelected.
 	 * @param requirements Object[]
 	 * @see edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementSelectorListener#requirementSelected(Object[])
 	 */
 	@Override
 	public void requirementSelected(Object[] requirements) {
 		for (Object obj : requirements) {
 			Requirement req = (Requirement) obj;
 			req.setIteration(activeIteration.getName(), false);
 			UpdateRequirementController.getInstance().updateRequirement(req);
 		}
 		
 		refreshTable();
 		ViewEventController.getInstance().refreshTree();
 		ViewEventController.getInstance().refreshTable();
 		parentPanel.refreshEstimate();
 	}
 	
 	/**
 	* Overrides the paintComponent method to retrieve the requirements on the first painting.
 	* 
 	 * @param g	The component object to paint
 	 */
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		refreshTable();
 		reqSelector.refreshList();
 		super.paintComponent(g);
 	}
 }
