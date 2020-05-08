 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.editor;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.ModelManager;
 import org.eclipse.tcf.te.tcf.processes.ui.navigator.events.TreeViewerListener;
 import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.trees.TreeControl;
 import org.eclipse.tcf.te.ui.views.editor.pages.TreeViewerExplorerEditorPage;
 
 /**
  * The editor page for Process Monitor.
  */
 public class ProcessMonitorEditorPage extends TreeViewerExplorerEditorPage {
 	// The decorator used to decorate the title bar.
 	private ILabelDecorator decorator = new ProcessMonitorTitleDecorator();
 	// The event listener instance
 	private ProcessMonitorEventListener listener = null;
 	// Reference to the tree listener
 	/* default */ ITreeViewerListener treeListener = null;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.TreeViewerExplorerEditorPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (listener != null) {
 			EventManager.getInstance().removeEventListener(listener);
 			listener = null;
 		}
 	    super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.TreeViewerExplorerEditorPage#getViewerId()
 	 */
 	@Override
 	protected String getViewerId() {
 		return "org.eclipse.tcf.te.ui.controls.viewer.processes"; //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormTitle()
 	 */
 	@Override
     protected String getFormTitle() {
 	    return Messages.ProcessMonitorEditorPage_PageTitle;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getContextHelpId()
 	 */
 	@Override
     protected String getContextHelpId() {
 	    return "org.eclipse.tcf.te.tcf.processes.ui.ProcessExplorerEditorPage"; //$NON-NLS-1$
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.TreeViewerExplorerEditorPage#getTitleBarDecorator()
 	 */
 	@Override
     protected ILabelDecorator getTitleBarDecorator() {
 	    return decorator;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.TreeViewerExplorerEditorPage#doCreateTreeControl()
 	 */
 	@Override
 	protected TreeControl doCreateTreeControl() {
 	    TreeControl treeControl = super.doCreateTreeControl();
 	    Assert.isNotNull(treeControl);
 
 	    if (listener == null) {
 	    	listener = new ProcessMonitorEventListener(treeControl);
 	    	EventManager.getInstance().addEventListener(listener, ChangeEvent.class);
 	    }
 
 		if (listener == null && treeControl.getViewer() instanceof TreeViewer) {
 			final TreeViewer treeViewer = (TreeViewer) treeControl.getViewer();
 
 			treeListener = new TreeViewerListener();
 			treeViewer.addTreeListener(treeListener);
 			treeViewer.getTree().addDisposeListener(new DisposeListener() {
 				@Override
 				public void widgetDisposed(DisposeEvent e) {
 					if (treeListener != null) {
 						treeViewer.removeTreeListener(treeListener);
 						treeListener = null;
 					}
 				}
 			});
 		}
 
 	    return treeControl;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.TreeViewerExplorerEditorPage#getViewerInput()
 	 */
 	@Override
     protected Object getViewerInput() {
		IPeerModel peerModel = (IPeerModel) getEditorInputNode();
		return ModelManager.getRuntimeModel(peerModel);
     }
 }
