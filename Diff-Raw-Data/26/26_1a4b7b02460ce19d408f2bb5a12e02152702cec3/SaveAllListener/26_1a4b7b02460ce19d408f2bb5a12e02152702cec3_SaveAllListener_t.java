 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * William Chen (Wind River) - [345552] Edit the remote files with a proper editor
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.ui.internal.autosave;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IExecutionListener;
 import org.eclipse.core.commands.NotHandledException;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCacheCommit;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.StateManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.model.FSModel;
 import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
 import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.IOpExecutor;
 import org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.UiExecutor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * The execution listener of command "SAVE ALL", which synchronizes the local
  * file with the one on the target server after it is saved.
  */
 public class SaveAllListener implements IExecutionListener {
 	// Dirty nodes that should be saved and synchronized.
 	List<FSTreeNode> fDirtyNodes;
 	/**
 	 * Create the listener listening to command "SAVE ALL".
 	 */
 	public SaveAllListener() {
 		this.fDirtyNodes = new ArrayList<FSTreeNode>();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.IExecutionListener#postExecuteSuccess(java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public void postExecuteSuccess(String commandId, Object returnValue) {
 		if (!fDirtyNodes.isEmpty()) {
 			if (UIPlugin.isAutoSaving()) {
 				FSTreeNode[] nodes = fDirtyNodes.toArray(new FSTreeNode[fDirtyNodes.size()]);
 				IOpExecutor executor = new UiExecutor();
 				executor.execute(new OpCacheCommit(nodes));
 			}
 			else {
 				SafeRunner.run(new SafeRunnable(){
 					@Override
                     public void handleException(Throwable e) {
 						// Ignore exception
                     }
 					@Override
                     public void run() throws Exception {
 						for (FSTreeNode dirtyNode : fDirtyNodes) {
 							StateManager.getInstance().refreshState(dirtyNode);
 						}
                     }});
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.IExecutionListener#preExecute(java.lang.String, org.eclipse.core.commands.ExecutionEvent)
 	 */
 	@Override
 	public void preExecute(String commandId, ExecutionEvent event) {
 		fDirtyNodes.clear();
 		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
 		IEditorPart[] editors = page.getDirtyEditors();
 		for (IEditorPart editor : editors) {
 			IEditorInput input = editor.getEditorInput();
 			FSTreeNode node = getEditedNode(input);
 			if (node != null) {
 				// If it is a modified node, add it to the dirty node list.
 				fDirtyNodes.add(node);
 			}
 		}
 	}
 
 	/**
 	 * Get the corresponding FSTreeNode from the input.
 	 * If the input has no corresponding FSTreeNode, return null;
 	 * @param input The editor input.
 	 * @return The corresponding FSTreeNode or null if it has not.
 	 */
 	private FSTreeNode getEditedNode(IEditorInput input){
 		if (input instanceof IURIEditorInput) {
 			//Get the file that is being edited.
 			IURIEditorInput fileInput = (IURIEditorInput) input;
 			URI uri = fileInput.getURI();
 			try {
 				IFileStore store = EFS.getStore(uri);
 				File localFile = store.toLocalFile(0, new NullProgressMonitor());
 				if (localFile != null) {
 					// Get the file's mapped FSTreeNode.
					FSTreeNode node = FSModel.getTreeNode(localFile.getCanonicalPath());
 					return node;
 				}
			}catch(Exception e){}
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.IExecutionListener#notHandled(java.lang.String, org.eclipse.core.commands.NotHandledException)
 	 */
 	@Override
 	public void notHandled(String commandId, NotHandledException exception) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.IExecutionListener#postExecuteFailure(java.lang.String, org.eclipse.core.commands.ExecutionException)
 	 */
 	@Override
 	public void postExecuteFailure(String commandId, ExecutionException exception) {
 	}
 }
