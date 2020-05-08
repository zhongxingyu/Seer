 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
  * William Chen (Wind River) - [352302]Opening a file in an editor depending on
  *                             the client's permissions.
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.core.model;
 
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IFileSystem;
 import org.eclipse.tcf.services.IFileSystem.DirEntry;
 import org.eclipse.tcf.services.IFileSystem.FileAttrs;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel;
 import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IWindowsFileAttributes;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.UserAccount;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.callbacks.QueryDoneOpenChannel;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.callbacks.RefreshStateDoneOpenChannel;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.NullOpExecutor;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpTargetFileDigest;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpUser;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.testers.TargetPropertyTester;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLConnection;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLStreamHandlerService;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.FileState;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 
 /**
  * Representation of a file system tree node.
  * <p>
  * <b>Note:</b> Node construction and child list access is limited to the TCF
  * event dispatch thread.
  */
 public final class FSTreeNode extends AbstractTreeNode implements Cloneable {
 	// The constant to access the Windows Attributes.
 	private static final String KEY_WIN32_ATTRS = "Win32Attrs"; //$NON-NLS-1$
 
 	/**
 	 * The tree node file system attributes
 	 */
 	public IFileSystem.FileAttrs attr = null;
 
 	/**
 	 * Create a folder node using the specified parent node, the directory entry
 	 * and the flag to indicate if it is a root node.
 	 * 
 	 * @param parentNode The parent node.
 	 * @param entry The directory entry.
 	 * @param entryIsRootNode If this folder is root folder.
 	 */
 	public FSTreeNode(FSTreeNode parentNode, DirEntry entry, boolean entryIsRootNode) {
 		Assert.isNotNull(entry);
 		IFileSystem.FileAttrs attrs = entry.attrs;
 
 		this.attr = attrs;
 		this.name = entry.filename;
 		if (attrs == null || attrs.isDirectory()) {
 			this.type = entryIsRootNode ? "FSRootDirNode" : "FSDirNode"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else if (attrs.isFile()) {
 			this.type = "FSFileNode"; //$NON-NLS-1$
 		}
 		this.parent = parentNode;
 		this.peerNode = parentNode.peerNode;
 		Assert.isTrue(Protocol.isDispatchThread());
 	}
 	
 	/**
 	 * Constructor.
 	 */
 	public FSTreeNode() {
 		Assert.isTrue(Protocol.isDispatchThread());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public Object clone() {
 		if (Protocol.isDispatchThread()) {
 			FSTreeNode clone = new FSTreeNode();
 			clone.childrenQueried = childrenQueried;
 			clone.childrenQueryRunning = childrenQueryRunning;
 			clone.name = name;
 			clone.parent = parent;
 			clone.peerNode = peerNode;
 			clone.type = type;
 			if (attr != null) {
 				Map<String, Object> attributes = new HashMap<String, Object>(attr.attributes);
 				clone.attr = new IFileSystem.FileAttrs(attr.flags, attr.size, attr.uid, attr.gid, attr.permissions, attr.atime, attr.mtime, attributes);
 			} else {
 				clone.attr = null;
 			}
 			return clone;
 		}
 		final Object[] objects = new Object[1];
 		Protocol.invokeAndWait(new Runnable() {
 
 			@Override
 			public void run() {
 				objects[0] = FSTreeNode.this.clone();
 			}
 		});
 		return objects[0];
 	}
 
 	/**
 	 * Change the file/folder's write permission.
 	 * @param b true if the agent is granted with its write permission.
 	 */
 	public void setWritable(boolean b) {
 		UserAccount account = getUserAccount(peerNode);
 		if (account != null && attr != null) {
 			int bit;
 			if (attr.uid == account.getEUID()) {
 				bit = IFileSystem.S_IWUSR;
 			} else if (attr.gid == account.getEGID()) {
 				bit = IFileSystem.S_IWGRP;
 			} else {
 				bit = IFileSystem.S_IWOTH;
 			}
 			int permissions = attr.permissions;
 			setPermissions(b ? (permissions | bit):(permissions & ~ bit));
 		}
     }
 	
 	/**
 	 * Get the user account of the specified TCF peer.
 	 * 
 	 * @param peerNode The peer node of the TCF agent.
 	 * @return The user account that runs the agent.
 	 */
 	private UserAccount getUserAccount(IPeerModel peerNode) {
 		OpUser user = new OpUser(peerNode);
 		new NullOpExecutor().execute(user);
 		return user.getUserAccount();
 	}
 
 	/**
 	 * Set the file's permissions.
 	 * @param permissions The new permissions.
 	 */
 	public void setPermissions(int permissions) {
 		attr = new IFileSystem.FileAttrs(attr.flags, attr.size, attr.uid, attr.gid, permissions, attr.atime, attr.mtime, attr.attributes);
     }
 	
 	/**
 	 * Returns the children outside of TCF thread.
 	 * 
 	 * @return The children list.
 	 */
 	@Override
 	public List<FSTreeNode> getChildren() {
 	    List<FSTreeNode> result = new ArrayList<FSTreeNode>();
 	    synchronized(children) {
 	    	for(AbstractTreeNode child : children) {
 	    		result.add((FSTreeNode)child);
 	    	}
 	    }
 	    return result;
 	}
 
 	/**
 	 * Return if the node is a Windows file/folder node.
 	 *
 	 * @return true if it is a Windows node, or else false.
 	 */
 	public boolean isWindowsNode() {
 		if (attr != null && attr.attributes != null) {
 			return attr.attributes.containsKey(KEY_WIN32_ATTRS);
 		}
 		if (peerNode != null) {
 			String OSName = TargetPropertyTester.getOSName(peerNode);
 			if(OSName != null){
 				return OSName.startsWith("Windows"); //$NON-NLS-1$
 			}
 		}
 		synchronized (children) {
 			if (!children.isEmpty()) {
				for (AbstractTreeNode treeNode : children) {
					FSTreeNode node = (FSTreeNode) treeNode;
					if(node.hasOSInfo()) return node.isWindowsNode();
				}
 			}
 		}
		if(parent != null) {
			return ((FSTreeNode)parent).isWindowsNode();
		}
 		return false;
 	}
	
	/**
	 * If this node has OS information.
	 * 
	 * @return true if it has.
	 */
	private boolean hasOSInfo() {
		return attr != null && attr.attributes != null || 
				peerNode != null && TargetPropertyTester.getOSName(peerNode)!= null;
	}
 
 	/**
 	 * Return if the node is a file.
 	 *
 	 * @return true if it is a file, or else false.
 	 */
 	public boolean isFile() {
 		return attr != null && attr.isFile();
 	}
 
 	/**
 	 * Return if the node is a directory.
 	 *
 	 * @return true if it is a directory, or else false.
 	 */
 	public boolean isDirectory() {
 		return attr != null && attr.isDirectory();
 	}
 
 	/**
 	 * Return if the attribute specified by the mask bit is turned on.
 	 *
 	 * @param bit
 	 *            The attribute's mask bit.
 	 * @return true if it is on, or else false.
 	 */
 	public boolean isWin32AttrOn(int bit) {
 		if (attr != null && attr.attributes.get(KEY_WIN32_ATTRS) instanceof Integer) {
 			Integer win32Attrs = (Integer) attr.attributes.get(KEY_WIN32_ATTRS);
 			return (win32Attrs.intValue() & bit) != 0;
 		}
 		return false;
 	}
 
 	/**
 	 * Set the attribute specified by the mask bit to on or off.
 	 * @param bit The attribute's mask bit.
 	 * @param on The flag if the bit should be turned on or off.
 	 */
 	public void setWin32Attr(int bit, boolean on) {
 		if (attr != null && attr.attributes.get(KEY_WIN32_ATTRS) instanceof Integer) {
 			int win32attr = ((Integer) attr.attributes.get(KEY_WIN32_ATTRS)).intValue();
 			win32attr = on ? (win32attr | bit) : (win32attr & ~bit);
 			attr.attributes.put(KEY_WIN32_ATTRS, Integer.valueOf(win32attr));
 		}
 	}
 
 	/**
 	 * Get the file's win32 attributes.
 	 * @return The file's win32 attributes.
 	 */
 	public int getWin32Attrs() {
 		if (attr != null && attr.attributes.get(KEY_WIN32_ATTRS) instanceof Integer) {
 			return ((Integer) attr.attributes.get(KEY_WIN32_ATTRS)).intValue();
 		}
 	    return 0;
     }
 
 	/**
 	 * Return if this file/folder is hidden.
 	 *
 	 * @return true if it is hidden, or else false.
 	 */
 	public boolean isHidden() {
 		return isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_HIDDEN);
 	}
 
 	/**
 	 * Set the file/folder hidden attribute's value.
 	 * @param hidden The new value.
 	 */
 	public void setHidden(boolean hidden) {
 		setWin32Attr(IWindowsFileAttributes.FILE_ATTRIBUTE_HIDDEN, hidden);
     }
 
 	/**
 	 * Return if this file/folder is read-only.
 	 *
 	 * @return true if it is read-only, or else false.
 	 */
 	public boolean isReadOnly() {
 		return isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_READONLY);
 	}
 
 	/**
 	 * Set the file/folder read-only attribute's value.
 	 * @param readOnly The new value.
 	 */
 	public void setReadOnly(boolean readOnly) {
 		setWin32Attr(IWindowsFileAttributes.FILE_ATTRIBUTE_READONLY, readOnly);
     }
 
 	/**
 	 * Get the location of a file/folder node using the format of the file
 	 * system's platform.
 	 *
 	 * @param parentNode
 	 *            The file/folder node.
 	 * @return The location of the file/folder.
 	 */
 	public String getLocation() {
 		return getLocation(false);
 	}
 
 	/**
 	 * Get the location of a file/folder.
 	 *
 	 * @param cross
 	 *            If the format is cross-platform.
 	 * @return The path to the file/folder.
 	 */
 	public String getLocation(boolean cross) {
 		if(isRoot()) {
 			if(cross) {
 				if(isWindowsNode()) {
 					return name.substring(0, name.length() - 1) + "/"; //$NON-NLS-1$
 				}
 			}
 			return name;
 		}
 		String pLoc = getParent().getLocation(cross);
 		if(getParent().isRoot()) {
 			return pLoc + name;
 		}
 		String pathSep = (!cross && isWindowsNode()) ? "\\" : "/"; //$NON-NLS-1$ //$NON-NLS-2$
 		return pLoc + pathSep + name;
 	}
 
 	/**
 	 * Get the URL of the file or folder. The URL's format is created in the
 	 * following way: tcf:/<TCF_AGENT_ID>/remote/path/to/the/resource... See
 	 * {@link TcfURLConnection#TcfURLConnection(URL)}
 	 *
 	 * @see TcfURLStreamHandlerService#parseURL(URL, String, int, int)
 	 * @see #getLocationURI()
 	 * @return The URL of the file/folder.
 	 */
 	public URL getLocationURL() {
 		try {
 			String id = peerNode.getPeerId();
 			String path = getLocation(true);
 			String location = TcfURLConnection.PROTOCOL_SCHEMA + ":/" + id + (path.startsWith("/") ? path : "/" + path); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			return new URL(location);
 		} catch (MalformedURLException e) {
 			assert false;
 			return null;
 		}
 	}
 	
 	/**
 	 * Get the URI of the file or folder. The URI's format is created in the
 	 * following way: tcf:/<TCF_AGENT_ID>/remote/path/to/the/resource...
 	 *
 	 * @return The URI of the file/folder.
 	 */
 	public URI getLocationURI() {
 		try {
 			String id = peerNode.getPeerId();
 			String path = getEncodedURIPath();
 			String location = TcfURLConnection.PROTOCOL_SCHEMA + ":/" + id + (path.startsWith("/") ? path : "/" + path); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			return new URI(location);
 		}
 		catch (URISyntaxException e) {
 			assert false;
 			return null;
 		}
 	}
 	
 	/**
 	 * Encode each segment of the path to a URI compatible name,
 	 * and get the URI encoded path.
 	 * 
 	 * @return The encoded path which is URI compatible.
 	 */
 	private String getEncodedURIPath() {
 		if(isRoot()) {
 			if(isWindowsNode()) {
 				return name.substring(0, name.length() - 1) + "/"; //$NON-NLS-1$
 			}
 			return name;
 		}
 		final AtomicReference<String> ref = new AtomicReference<String>();
 		SafeRunner.run(new ISafeRunnable(){
 			@Override
             public void handleException(Throwable exception) {
 				// Ignore on purpose
             }
 			@Override
             public void run() throws Exception {
 				ref.set(URLEncoder.encode(name, "UTF-8")); //$NON-NLS-1$
             }});
 		String segment = ref.get();
 		String pLoc = getParent().getEncodedURIPath();
 		if(getParent().isRoot()) {
 			return pLoc + segment;
 		}
 		return pLoc + "/" + segment; //$NON-NLS-1$
 	}
 
 	/**
 	 * If this node is a root node.
 	 *
 	 * @return true if this node is a root node.
 	 */
 	public boolean isRoot() {
 		return type != null && type.equals("FSRootDirNode"); //$NON-NLS-1$
 	}
 
 	/**
 	 * If this node is the system root.
 	 *
 	 * @return true if this node is the system root.
 	 */
 	@Override
 	public boolean isSystemRoot() {
 		return type != null && type.equals("FSRootNode"); //$NON-NLS-1$
 	}
 
 	/**
 	 * If this file is readable.
 	 *
 	 * @return true if it is readable.
 	 */
 	public boolean isReadable() {
 		UserAccount account = getUserAccount(peerNode);
 		if (account != null && attr != null) {
 			if (attr.uid == account.getEUID()) {
 				return (attr.permissions & IFileSystem.S_IRUSR) != 0;
 			} else if (attr.gid == account.getEGID()) {
 				return (attr.permissions & IFileSystem.S_IRGRP) != 0;
 			} else {
 				return (attr.permissions & IFileSystem.S_IROTH) != 0;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * If the agent is the owner of this file/folder.
 	 *
 	 * @return true if the agent is the owner of this file/folder.
 	 */
 	public boolean isAgentOwner() {
 		UserAccount account = getUserAccount(peerNode);
 		if (account != null && attr != null) {
 			return attr.uid == account.getEUID();
 		}
 		return false;
 	}
 
 	/**
 	 * If this file is writable.
 	 *
 	 * @return true if it is writable.
 	 */
 	public boolean isWritable() {
 		UserAccount account = getUserAccount(peerNode);
 		if (account != null && attr != null) {
 			if (attr.uid == account.getEUID()) {
 				return (attr.permissions & IFileSystem.S_IWUSR) != 0;
 			} else if (attr.gid == account.getEGID()) {
 				return (attr.permissions & IFileSystem.S_IWGRP) != 0;
 			} else {
 				return (attr.permissions & IFileSystem.S_IWOTH) != 0;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * If this file is executable.
 	 *
 	 * @return true if it is executable.
 	 */
 	public boolean isExecutable() {
 		UserAccount account = getUserAccount(peerNode);
 		if (account != null && attr != null) {
 			if (attr.uid == account.getEUID()) {
 				return (attr.permissions & IFileSystem.S_IXUSR) != 0;
 			} else if (attr.gid == account.getEGID()) {
 				return (attr.permissions & IFileSystem.S_IXGRP) != 0;
 			} else {
 				return (attr.permissions & IFileSystem.S_IXOTH) != 0;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Test if this file is a windows system file.
 	 *
 	 * @return true if it is a windows system file.
 	 */
 	public boolean isSystem() {
 		return !isRoot() && isWindowsNode() && isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_SYSTEM);
 	}
 
 	/**
 	 * Get the type label of the file for displaying purpose.
 	 *
 	 * @return The type label text.
 	 */
 	public String getFileType() {
 		if (isRoot()) {
 			return Messages.FSTreeNode_TypeLocalDisk;
 		}
 		if (isDirectory()) {
 			return Messages.FSTreeNode_TypeFileFolder;
 		}
 		if (isSystem()) {
 			return Messages.FSTreeNode_TypeSystemFile;
 		}
 		IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(name);
 		if (contentType != null) {
 			return contentType.getName();
 		}
 		int lastDot = name.lastIndexOf("."); //$NON-NLS-1$
 		if (lastDot == -1) {
 			return Messages.FSTreeNode_TypeUnknownFile;
 		}
 		return name.substring(lastDot + 1).toUpperCase() + " " + Messages.FSTreeNode_TypeFile; //$NON-NLS-1$
     }
 
 	/**
 	 * Set the file's attributes and trigger property change event.
 	 * 
 	 * @param attrs The new attributes.
 	 */
 	public void setAttributes(FileAttrs attrs) {
 		FileAttrs oldAttrs = this.attr;
 		this.attr = attrs;
 		if (attrs != oldAttrs) {
 			firePropertyChange(new PropertyChangeEvent(this, "attributes", oldAttrs, attrs)); //$NON-NLS-1$
 		}
 	}
 	
 	/**
 	 * Get the local file's state of the specified tree node. The local file must exist
 	 * before calling this method to get its state.
 	 *
 	 * @param node The tree node whose local file state is going to retrieved.
 	 * @return The tree node's latest cache state.
 	 */
 	public CacheState getCacheState() {
 		File file = CacheManager.getCacheFile(this);
 		if (!file.exists()) return CacheState.consistent;
 		FileState digest = PersistenceManager.getInstance().getFileDigest(this);
 		return digest.getCacheState();
 	}
 
 	/**
 	 * Set the file's new name and trigger property change event.
 	 * 
 	 * @param name The new name.
 	 */
 	public void setName(String name) {
 		String oldName = this.name;
 		this.name = name;
 		if(name == null && oldName != null || name != null && !name.equals(oldName)) {
 			firePropertyChange(new PropertyChangeEvent(this, "name", oldName, name)); //$NON-NLS-1$
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode#doCreateRefreshDoneOpenChannel(org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
     protected DoneOpenChannel doCreateRefreshDoneOpenChannel(final ICallback callback) {
 		final FileState digest = PersistenceManager.getInstance().getFileDigest(this);
 		ICallback cb = new Callback(){
 			@Override
             protected void internalDone(Object caller, IStatus status) {
 				if (status.isOK() && caller instanceof OpTargetFileDigest) {
 					digest.updateTargetDigest(((OpTargetFileDigest) caller).getDigest());
 				}
 				if (callback != null) callback.done(caller, status);
             }
 		};
 		return new RefreshStateDoneOpenChannel(this, cb);
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode#doCreateQueryDoneOpenChannel()
 	 */
 	@Override
     protected DoneOpenChannel doCreateQueryDoneOpenChannel() {
 	    return new QueryDoneOpenChannel(this);
     }
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode#getParent()
 	 */
 	@Override
 	public FSTreeNode getParent() {
 		return (FSTreeNode) parent;
 	}
 }
