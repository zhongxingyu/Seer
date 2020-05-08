 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.dnd;
 
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 
 /**
  * The drop target listener for the file tree of Target Explorer.
  */
 public class FSDropTargetListener extends ViewerDropAdapter {
 	// The tree viewer that the drop listener attached to.
 	TreeViewer viewer;
 	// The common dnd operation
 	CommonDnD dnd;
 	/**
 	 * Create FSDropTargetListener using the viewer.
 	 * 
 	 * @param viewer The file system tree viewer.
 	 */
 	public FSDropTargetListener(TreeViewer viewer) {
 		super(viewer);
 		this.viewer = viewer;
 		dnd = new CommonDnD();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
 	 */
 	@Override
 	public boolean validateDrop(Object target, int operation, TransferData transferType) {
 		if (target instanceof FSTreeNode) {
 			if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
 				return dnd.validateLocalSelectionDrop(target, operation, transferType);
 			}
 			else if (FileTransfer.getInstance().isSupportedType(transferType)) {
 				return dnd.validateFilesDrop(target, operation, transferType);
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
 	 */
 	@Override
 	public boolean performDrop(Object data) {
 		boolean success = false;
 		TransferData transferType = getCurrentEvent().currentDataType;
 		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
 		    IStructuredSelection selection = (IStructuredSelection) data;
 		    int operations = getCurrentOperation();
 			FSTreeNode target = (FSTreeNode) getCurrentTarget();
 		    success = dnd.dropLocalSelection(target, operations, selection);
 		}
 		else if(FileTransfer.getInstance().isSupportedType(transferType)) {
 			String[] files = (String[]) data;
 		    int operations = getCurrentOperation();
 			FSTreeNode target = (FSTreeNode) getCurrentTarget();
 			success = dnd.dropFiles(viewer, files, operations, target);
 		}
 		return success;
 	}
 }
