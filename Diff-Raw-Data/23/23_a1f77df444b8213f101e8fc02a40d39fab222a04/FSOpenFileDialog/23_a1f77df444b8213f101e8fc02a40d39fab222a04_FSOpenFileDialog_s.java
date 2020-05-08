 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.dialogs;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.filesystem.controls.FSTreeContentProvider;
 import org.eclipse.tcf.te.tcf.filesystem.controls.FSTreeViewerSorter;
 import org.eclipse.tcf.te.tcf.filesystem.interfaces.IUIConstants;
 import org.eclipse.tcf.te.tcf.filesystem.internal.columns.FSTreeElementLabelProvider;
 import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 import org.eclipse.tcf.te.ui.trees.FilterDescriptor;
 import org.eclipse.tcf.te.ui.trees.ViewerStateManager;
 import org.eclipse.ui.IDecoratorManager;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 
 
 /**
  * File system open file dialog.
  */
 public class FSOpenFileDialog extends ElementTreeSelectionDialog {
 	// Label provider used by the file system tree.
 	private FSTreeElementLabelProvider labelProvider;
 
 	/**
 	 * Create an FSFolderSelectionDialog using the specified shell as the parent.
 	 * 
 	 * @param parentShell The parent shell.
 	 */
 	public FSOpenFileDialog(Shell parentShell) {
		this(parentShell, new FSTreeElementLabelProvider(), new FSTreeContentProvider());
 	}
 
 	/**
 	 * Create an FSFolderSelectionDialog using the specified shell, an FSTreeLabelProvider, and a
 	 * content provider that provides the tree nodes.
 	 * 
 	 * @param parentShell The parent shell.
 	 * @param labelProvider The label provider.
 	 * @param contentProvider The content provider.
 	 */
 	private FSOpenFileDialog(Shell parentShell, FSTreeElementLabelProvider labelProvider, ITreeContentProvider contentProvider) {
 		super(parentShell, createDecoratingLabelProvider(labelProvider), contentProvider);
 		this.labelProvider = labelProvider;
 		setTitle(Messages.FSOpenFileDialog_title);
 		setMessage(Messages.FSOpenFileDialog_message);
 		this.setAllowMultiple(false);
 		this.setStatusLineAboveButtons(false);
 		this.setComparator(new FSTreeViewerSorter());
 		this.setValidator(new ISelectionStatusValidator() {
 			@Override
 			public IStatus validate(Object[] selection) {
 				return isValidSelection(selection);
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.dialogs.ElementTreeSelectionDialog#setInput(java.lang.Object)
 	 */
 	@Override
     public void setInput(Object input) {
 		super.setInput(input);
 		FilterDescriptor[] filterDescriptors = ViewerStateManager.getInstance()
 		                .getFilterDescriptors(IUIConstants.ID_TREE_VIEWER_FS, input);
 		if (filterDescriptors != null) {
 			for(FilterDescriptor descriptor : filterDescriptors) {
 				if(descriptor.isEnabled()) addFilter(descriptor.getFilter());
 			}
 		}
 	}
 
 	/**
 	 * Create a decorating label provider using the specified label provider.
 	 * 
 	 * @param labelProvider The label provider that actually provides labels and images.
 	 * @return The decorating label provider.
 	 */
 	private static ILabelProvider createDecoratingLabelProvider(ILabelProvider labelProvider) {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		IDecoratorManager manager = workbench.getDecoratorManager();
 		ILabelDecorator decorator = manager.getLabelDecorator();
 		return new DecoratingLabelProvider(labelProvider,decorator);
 	}
 
 	/**
 	 * Create the tree viewer and set it to the label provider.
 	 */
 	@Override
 	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
 		TreeViewer viewer = super.doCreateTreeViewer(parent, style);
 		viewer.getTree().setLinesVisible(false);
 		labelProvider.setViewer(viewer);
 		return viewer;
 	}
 
 	/**
 	 * If the specified selection is a valid folder to be selected.
 	 * 
 	 * @param selection The selected folders.
 	 * @return An error status if it is invalid or an OK status indicating it is valid.
 	 */
 	IStatus isValidSelection(Object[] selection) {
 		String pluginId = UIPlugin.getUniqueIdentifier();
 		IStatus error = new Status(IStatus.ERROR, pluginId, null);
 		if (selection == null || selection.length == 0) {
 			return error;
 		}
 		if (!(selection[0] instanceof FSTreeNode)) {
 			return error;
 		}
 		FSTreeNode target = (FSTreeNode) selection[0];
 		if(!target.isFile()) {
 			return error;
 		}
 		return new Status(IStatus.OK, pluginId, null);
 	}
 }
