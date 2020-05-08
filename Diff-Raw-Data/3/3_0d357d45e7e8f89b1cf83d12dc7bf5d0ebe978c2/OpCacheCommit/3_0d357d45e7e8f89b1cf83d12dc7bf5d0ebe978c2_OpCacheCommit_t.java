 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.StateManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
 /**
  * The operation class that commits the local cache to target file systems. 
  */
 public class OpCacheCommit extends OpUpload {
 	// The files that are to be committed to its target file system.
 	private FSTreeNode[] nodes;
 	// If it should synchronize its status with the target file.
 	private boolean sync;
 	
 	/**
 	 * Create an operation of cache commit with specified nodes.
 	 * 
 	 * @param nodes The nodes to be committed to the target file system.
 	 */
 	public OpCacheCommit(FSTreeNode... nodes) {
 		this(false, nodes);
 	}
 	
 	/**
 	 * Create an operation of cache commit with specified nodes, specifying
 	 * if the status should be synchronized.
 	 * 
 	 * @param sync If the status should be synchronized after commitment.
 	 * @param nodes The nodes to be committed to the target file system.
 	 */
 	public OpCacheCommit(boolean sync, FSTreeNode... nodes) {
 		super(nodes);
 		this.nodes = nodes;
 		this.sync = sync;
     }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
     public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		try {
 			super.run(monitor);
 		}
 		finally {
 			// Once upload is successful, synchronize the modified time.
 			for (int i = 0; i < nodes.length; i++) {
 				final FSTreeNode node = nodes[i];
 				SafeRunner.run(new ISafeRunnable() {
 					@Override
 					public void handleException(Throwable e) {
 						// Ignore exception
 					}
 					@Override
 					public void run() throws Exception {
						StateManager.getInstance().refreshState(node);
 						PersistenceManager.getInstance().setBaseTimestamp(node.getLocationURI(), node.attr.mtime);
 						if (sync) {
 							File file = CacheManager.getInstance().getCacheFile(node);
 							setLastModifiedChecked(file, node.attr.mtime);
 						}
 					}
 				});
 			}
 			monitor.done();
 		}
     }
 }
