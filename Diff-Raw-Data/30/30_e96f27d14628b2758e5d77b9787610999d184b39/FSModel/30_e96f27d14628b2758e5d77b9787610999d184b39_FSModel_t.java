 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.core.model;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
 import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 
 /**
  * The file system model implementation.
  */
 public final class FSModel implements ITreeNodeModel {
 	/* default */static final String FSMODEL_KEY = CorePlugin.getUniqueIdentifier() + ".file.system"; //$NON-NLS-1$
 
 	/**
 	 * Get the file system model of the peer model. If it does not
 	 * exist yet, create a new instance and store it.
 	 *
 	 * @param peerModel The peer model
 	 * @return The file system model connected this peer model.
 	 */
 	public static FSModel getFSModel(final IPeerModel peerModel) {
		final AtomicReference<FSModel> model = new AtomicReference<FSModel>();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (peerModel != null) {
					model.set((FSModel) peerModel.getProperty(FSMODEL_KEY));
					if (model.get() == null) {
						model.set(new FSModel(peerModel));
						peerModel.setProperty(FSMODEL_KEY, model.get());
 					}
 				}
			}
		};
 
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);
 
		return model.get();
 	}
 
 	// The root node of the peer model
 	private FSTreeNode root;
 	private IPeerModel peerNode;
 	/**
 	 * Create a File System Model.
 	 */
 	/* default */ FSModel(IPeerModel peerNode) {
 		this.peerNode = peerNode;
 	}
 
 	/**
 	 * Get the root node of the peer model.
 	 *
 	 * @return The root node.
 	 */
 	@Override
 	public FSTreeNode getRoot() {
 		if(root == null) {
 			root = createRoot();
 		}
 		return root;
 	}
 
 	/**
 	 * Create a root node for the specified peer.
 	 *
 	 * @param peerNode The peer.
 	 */
 	/* default */ FSTreeNode createRoot() {
 		if (Protocol.isDispatchThread()) {
 			return createRootNode(peerNode);
 		}
 		else {
 			final AtomicReference<FSTreeNode> ref = new AtomicReference<FSTreeNode>();
 			Protocol.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					ref.set(createRoot());
 				}
 			});
 			return ref.get();
 		}
 	}
 
 	/**
 	 * Create a root node for the peer.
 	 *
 	 * @param peerNode The peer.
 	 * @return The root file system node.
 	 */
 	public static FSTreeNode createRootNode(IPeerModel peerNode) {
 		FSTreeNode node = new FSTreeNode();
 		node.type = "FSRootNode"; //$NON-NLS-1$
 		node.peerNode = peerNode;
 		node.name = Messages.FSTreeNodeContentProvider_rootNode_label;
 	    return node;
     }
 
 	/**
 	 * Create a file node under the folder specified folder using the new name.
 	 *
 	 * @param name The file's name.
 	 * @param folder The parent folder.
 	 * @return The file tree node.
 	 */
 	public static FSTreeNode createFileNode(String name, FSTreeNode folder) {
 		return createTreeNode(name, "FSFileNode", folder); //$NON-NLS-1$
     }
 
 	/**
 	 * Create a folder node under the folder specified folder using the new name.
 	 *
 	 * @param name The folder's name.
 	 * @param folder The parent folder.
 	 * @return The folder tree node.
 	 */
 	public static FSTreeNode createFolderNode(String name, FSTreeNode folder) {
 		return createTreeNode(name, "FSDirNode", folder); //$NON-NLS-1$
     }
 
 	/**
 	 * Create a tree node under the folder specified folder using the new name.
 	 *
 	 * @param name The tree node's name.
 	 * @param type The new node's type.
 	 * @param folder The parent folder.
 	 * @return The tree node.
 	 */
 	private static FSTreeNode createTreeNode(String name, String type, FSTreeNode folder) {
 	    FSTreeNode node = new FSTreeNode();
 		node.name = name;
 		node.parent = folder;
 		node.peerNode = folder.peerNode;
 		node.type = type;
 	    return node;
     }
 }
