 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.operations;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.services.IFileSystem;
 import org.eclipse.tcf.services.IFileSystem.DoneRename;
 import org.eclipse.tcf.services.IFileSystem.FileSystemException;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.filesystem.dialogs.TimeTriggeredProgressMonitorDialog;
 import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
 import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
 import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
 import org.eclipse.tcf.te.tcf.filesystem.internal.utils.Rendezvous;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * FSMove moves specified tree nodes to a destination folder.
  */
 public class FSMove extends FSOperation {
 	// The file/folder nodes to be moved.
 	List<FSTreeNode> nodes;
 	// The destination folder to be moved to.
 	FSTreeNode dest;
 
 	/**
 	 * Create a move operation to move the specified nodes to the destination folder.
 	 *
 	 * @param nodes The nodes to be moved.
 	 * @param dest the destination folder to move to.
 	 */
 	public FSMove(List<FSTreeNode> nodes, FSTreeNode dest) {
 		this.nodes = getTopNodes(nodes);
 		this.dest = dest;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
 	 */
 	@Override
 	public boolean doit() {
		// Remove its self from the clipped nodes.
		nodes.remove(dest);
		if(nodes.isEmpty()) {
			// Clear the clip board.
			UIPlugin.getDefault().getClipboard().clear();
			// Refresh the file system tree.
			FSModel.getInstance().fireNodeStateChanged(null);
			return true;
		}
 		IRunnableWithProgress runnable = new IRunnableWithProgress() {
 			@Override
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				FSTreeNode head = nodes.get(0);
 				IChannel channel = null;
 				try {
 					channel = openChannel(head.peerNode.getPeer());
 					if (channel != null) {
 						IFileSystem service = channel.getRemoteService(IFileSystem.class);
 						if (service != null) {
 							monitor.beginTask(Messages.FSMove_PrepareToMove, IProgressMonitor.UNKNOWN);
 							monitor.worked(1);
 							monitor.beginTask(Messages.FSMove_MovingFile, nodes.size());
 							for (FSTreeNode node : nodes) {
 								// Move each node.
 								moveNode(monitor, service, node, dest);
 							}
 						}
 						else {
 							String message = NLS.bind(Messages.FSOperation_NoFileSystemError, head.peerNode.getPeer().getID());
 							throw new TCFFileSystemException(message);
 						}
 					}
 				}
 				catch (TCFException e) {
 					throw new InvocationTargetException(e);
 				}
 				finally {
 					// Clear the clip board.
 					UIPlugin.getDefault().getClipboard().clear();
 					if (channel != null) Tcf.getChannelManager().closeChannel(channel);
 					// Refresh the file system tree.
 					FSModel.getInstance().fireNodeStateChanged(null);
 					monitor.done();
 				}
 			}
 		};
 		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(parent, 250);
 		dialog.setCancelable(true);
 		try {
 			dialog.run(true, true, runnable);
 		}
 		catch (InvocationTargetException e) {
 			// Display the error reported during moving.
 			MessageDialog.openError(parent, Messages.FSMove_MoveFileFolderTitle, e.getLocalizedMessage());
 		}
 		catch (InterruptedException e) {
 			// It is canceled.
 		}
 		return true;
 	}
 
 	/**
 	 * Move the file/folder to the destination folder using the specified file system service.
 	 *
 	 * @param monitor The monitor used to report the moving progress.
 	 * @param service The file system service used to move the remote files.
 	 * @param node The file/folder node to be moved.
 	 * @param dest The destination folder.
 	 * @throws TCFFileSystemException The exception thrown during moving.
 	 * @throws InterruptedException Thrown when the operation is canceled.
 	 */
 	void moveNode(IProgressMonitor monitor, IFileSystem service, final FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
 		if (monitor.isCanceled()) throw new InterruptedException();
 		monitor.subTask(NLS.bind(Messages.FSMove_Moving, node.name));
 		FSTreeNode copy = findChild(service, dest, node.name);
 		if (copy == null || !copy.equals(node) && confirmReplace(node)) {
 			if (copy != null && copy.isDirectory() && node.isDirectory()) {
 				List<FSTreeNode> children = new ArrayList<FSTreeNode>(getChildren(node, service));
 				for (FSTreeNode child : children) {
 					moveNode(monitor, service, child, copy);
 				}
 				removeFolder(node, service);
 				monitor.worked(1);
 			}
 			else if (copy != null && copy.isFile() && node.isDirectory()) {
 				String error = NLS.bind(Messages.FSMove_FileExistsError, copy.name);
 				throw new TCFFileSystemException(error);
 			}
 			else if (copy != null && copy.isDirectory() && node.isFile()) {
 				String error = NLS.bind(Messages.FSMove_FolderExistsError, copy.name);
 				throw new TCFFileSystemException(error);
 			}
 			else {
 				if (copy != null && copy.isFile() && node.isFile()) {
 					removeFile(copy, service);
 				}
 				else if (copy == null) {
 					copy = (FSTreeNode) node.clone();
 				}
 				addChild(service, dest, copy);
 				String dst_path = copy.getLocation(true);
 				String src_path = node.getLocation(true);
 				final FSTreeNode copyNode = copy;
 				final TCFFileSystemException[] errors = new TCFFileSystemException[1];
 				final Rendezvous rendezvous = new Rendezvous();
 				service.rename(src_path, dst_path, new DoneRename() {
 					@Override
 					public void doneRename(IToken token, FileSystemException error) {
 						if (error != null) {
 							String message = NLS.bind(Messages.FSMove_CannotMove, node.name, error);
 							errors[0] = new TCFFileSystemException(message, error);
 						}
 						else {
 							cleanUpNode(node, copyNode);
 						}
 						rendezvous.arrive();
 					}
 				});
 				try {
 					rendezvous.waiting(5000L);
 				}
 				catch (InterruptedException e) {
 					String message = NLS.bind(Messages.FSMove_CannotMove, node.name, Messages.FSOperation_TimedOutWhenOpening);
 					errors[0] = new TCFFileSystemException(message);
 				}
 				if (errors[0] != null) {
 					removeChild(service, dest, copy);
 					throw errors[0];
 				}
 				monitor.worked(1);
 			}
 		}
 	}
 
 	/**
 	 * Clean up the node after successful moving.
 	 *
 	 * @param node The node being moved.
 	 * @param copyNode The target node that is moved to.
 	 */
 	void cleanUpNode(FSTreeNode node, FSTreeNode copyNode) {
 		if (node.isFile()) {
 			super.cleanUpFile(node);
 		}
 		else if (node.isDirectory()) {
 			super.cleanUpFolder(node);
 			List<FSTreeNode> children = new ArrayList<FSTreeNode>(getCurrentChildren(node));
 			getCurrentChildren(copyNode).addAll(children);
 			for (FSTreeNode child : children) {
 				child.parent = copyNode;
 			}
 		}
 	}
 }
