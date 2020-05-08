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
 
 import java.awt.Cursor;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DragSource;
 import java.io.IOException;
 
 import javax.activation.DataHandler;
 import javax.swing.JComponent;
 import javax.swing.JTree;
 import javax.swing.TransferHandler;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 
 public class IterationTransferHandler extends TransferHandler {
 	DataFlavor nodesFlavor;
 
 	public IterationTransferHandler() {
 		nodesFlavor = new DataFlavor(Requirement.class, "Requirement");
 	}
 
 	public boolean canImport(TransferHandler.TransferSupport support) {
 		support.getComponent().setCursor(DragSource.DefaultMoveNoDrop);
 		
 		if(!support.isDrop()) {
 			return false;
 		}
 		
 		support.setShowDropLocation(true);
 		
 		if(!support.isDataFlavorSupported(nodesFlavor)) {
 			return false;
 		}
 		
 		//make sure the drop location is valid.
 		JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
 		
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode)dl.getPath().getLastPathComponent();
 		Object nodeObject = node.getUserObject();
 		
 		//make sure the drop location is an iteration.
 		if(!(nodeObject instanceof Iteration)) return false;
 		
 		support.getComponent().setCursor(DragSource.DefaultMoveDrop);
 		
 		return true;
 	}
 
 	protected Transferable createTransferable(JComponent c) {
 		JTree tree = (JTree)c;
 		TreePath path = tree.getSelectionPath();
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
 				
 		if(node.getUserObject() instanceof Requirement)
 		{
 			Requirement req = (Requirement)node.getUserObject();
 			if(req.getEstimate() > 0 && req.getStatus() != RequirementStatus.COMPLETE && req.getStatus() != RequirementStatus.DELETED)
 			{
 				return new DataHandler(req, nodesFlavor.getMimeType());
 			}
 		}
 		
 		return null;
 	}
 
 	@Override
 	protected void exportDone(JComponent c, Transferable t, int act) {
 		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 	}
 	
 	public int getSourceActions(JComponent c) {
 		return MOVE;
 	}
 
 	public boolean importData(TransferHandler.TransferSupport support) {
 		if(!canImport(support)) {
 			return false;
 		}
 		
 		// Get drop location info.
 		JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
 		TreePath dest = dl.getPath();
 		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)dest.getLastPathComponent();
 
 		try {
 			Requirement child = (Requirement)support.getTransferable().getTransferData(nodesFlavor);
 			
 			if(parent.getUserObject() instanceof Iteration)
 			{
 				Iteration newIteration = (Iteration)parent.getUserObject();
			
 				child.setIteration(newIteration.getName());
 				UpdateRequirementController.getInstance().updateRequirement(child);
 				ViewEventController.getInstance().refreshTable();
 				ViewEventController.getInstance().refreshTree();
 				
 				return true;
 			}
 			
 		} catch (UnsupportedFlavorException e) {
 			System.err.println("Unsupported data exception with dragging.");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("IO Exception with dragging");
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 }
