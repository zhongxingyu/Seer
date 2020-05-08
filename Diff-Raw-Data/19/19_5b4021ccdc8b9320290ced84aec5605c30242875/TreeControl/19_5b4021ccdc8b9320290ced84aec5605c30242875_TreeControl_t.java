 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.trees;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPart;
 
 /**
  * A declarative tree control implementation that extends AbstractTreeControl. 
  */
 public class TreeControl extends AbstractTreeControl {
 	// The tree viewer's id.
 	private String viewerId;
 	// The extension parser that parse the viewer extension.
 	private TreeViewerExtension viewerExtension;
 	// The viewer descriptor parsed from the viewer extensions
 	private ViewerDescriptor viewerDescriptor;
 	
 	/**
 	 * Create an instance of TreeControl with a viewerId.
 	 * 
 	 * @param viewerId The viewer id.
 	 */
 	public TreeControl(String viewerId) {
 	    super();
 	    this.viewerId = viewerId;
 	    initialize();
     }
 
 	/**
 	 * Create an instance of TreeControl with a viewerId in a workbench part.
 	 * 
 	 * @param viewerId The viewer id.
 	 * @param parentPart the workbench part in which the tree control is created.
 	 */
 	public TreeControl(String viewerId, IWorkbenchPart parentPart) {
 	    super(parentPart);
 	    this.viewerId = viewerId;
 	    initialize();
     }
 
 	/**
 	 * Parse and initialze the tree control.
 	 */
 	private void initialize() {
 		viewerExtension = new TreeViewerExtension(viewerId);
 		viewerDescriptor = viewerExtension.parseViewer();
 		Assert.isNotNull(viewerExtension);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewer(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
     protected TreeViewer doCreateTreeViewer(Composite parent) {
 		Assert.isNotNull(parent);
 		IConfigurationElement configuration = viewerDescriptor.getStyleConfig();
 		if(configuration != null) {
 			int style = viewerExtension.parseStyle(configuration);
 			if(style != -1)
 				return new TreeViewer(parent, style);
 		}
 	    return super.doCreateTreeViewer(parent);
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
 	 */
 	@Override
     protected void configureTreeViewer(TreeViewer viewer) {
 	    super.configureTreeViewer(viewer);
 	    IConfigurationElement configuration = viewerDescriptor.getDragConfig();
 	    if(configuration != null) {
 	    	int operations = viewerExtension.parseDnd(configuration);
 	    	Transfer[] transferTypes = viewerExtension.parseTransferTypes(configuration);
 	    	DragSourceListener listener = viewerExtension.parseDragSourceListener(viewer, configuration);
 	    	viewer.addDragSupport(operations, transferTypes, listener);
 	    }
 	    configuration = viewerDescriptor.getDropConfig();
 	    if(configuration != null) {
 	    	int operations = viewerExtension.parseDnd(configuration);
 	    	Transfer[] transferTypes = viewerExtension.parseTransferTypes(configuration);
 	    	DropTargetListener adapter = viewerExtension.parseDropTargetListener(viewer, configuration);
 	    	viewer.addDropSupport(operations, transferTypes, adapter);
 	    }
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getViewerId()
 	 */
 	@Override
 	protected String getViewerId() {
 		return viewerId;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewerContentProvider(org.eclipse.jface.viewers.TreeViewer)
 	 */
 	@Override
 	protected ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer) {
 		return viewerDescriptor.getContentProvider();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
 	 */
 	@Override
 	protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#isStatePersistent()
 	 */
 	@Override
     protected boolean isStatePersistent() {
 	    return viewerDescriptor.isPersistent();
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getHelpId()
 	 */
 	@Override
     protected String getHelpId() {
 	    return viewerDescriptor.getHelpId();
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getAutoExpandLevel()
 	 */
 	@Override
     protected int getAutoExpandLevel() {
 	    return viewerDescriptor.getAutoExpandLevel();
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getDoubleClickCommandId()
 	 */
 	@Override
     protected String getDoubleClickCommandId() {
 	    return viewerDescriptor.getDoubleClickCommand();
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getContextMenuId()
 	 */
 	@Override
 	protected String getContextMenuId() {
		return viewerDescriptor.getContextMenuId();
 	}
 }
