 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.callbacks;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.services.IFileSystem;
 import org.eclipse.tcf.services.IFileSystem.DirEntry;
 import org.eclipse.tcf.services.IFileSystem.DoneClose;
 import org.eclipse.tcf.services.IFileSystem.DoneReadDir;
 import org.eclipse.tcf.services.IFileSystem.FileSystemException;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 
 /**
  * The callback handler that handles the event when a directory is read.
  */
 public class QueryDoneReadDir implements DoneReadDir {
 	// The tcf channel.
 	IChannel channel;
 	// The file system service.
 	IFileSystem service;
 	// The file handle of the parent directory.
 	IFileSystem.IFileHandle handle;
 	// The parent node being queried.
 	FSTreeNode parentNode;
 	// The callback object.
 	ICallback callback;
 	/**
 	 * Create an instance with parameters to initialize the fields.
 	 * 
 	 * @param channel The tcf channel.
 	 * @param service The file system service.
 	 * @param handle The directory's file handle.
 	 * @param parentNode The parent directory.
 	 */
 	public QueryDoneReadDir(ICallback callback, IChannel channel, IFileSystem service, IFileSystem.IFileHandle handle, FSTreeNode parentNode) {
 		this.callback = callback;
 		this.channel = channel;
 		this.service = service;
 		this.handle = handle;
 		this.parentNode = parentNode;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.services.IFileSystem.DoneReadDir#doneReadDir(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.DirEntry[], boolean)
 	 */
 	@Override
 	public void doneReadDir(IToken token, FileSystemException error, DirEntry[] entries, boolean eof) {
 		// Process the returned data
 		if (error == null) {
 			if (entries != null && entries.length > 0) {
 				for (DirEntry entry : entries) {
 					FSTreeNode node = new FSTreeNode(parentNode, entry, false);
 					parentNode.addChild(node);
 				}
 			}
 
 			if (eof) {
 				// Close the handle and channel if EOF is signaled or an error occurred.
 				service.close(handle, new DoneClose() {
 					@Override
                     public void doneClose(IToken token, FileSystemException error) {
 						if(callback != null) {
 							IStatus status = error == null ? Status.OK_STATUS : new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), error.getLocalizedMessage(), error);
 							callback.done(this, status);
 						}
                     }});
 			}
 			else {
 				// And invoke ourself again
 				service.readdir(handle, new QueryDoneReadDir(callback, channel, service, handle, parentNode));
 			}
 		} else if(callback != null) {
 			Status status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), error.getLocalizedMessage(), error);
 			callback.done(this, status);
 		}
 	}
 }
