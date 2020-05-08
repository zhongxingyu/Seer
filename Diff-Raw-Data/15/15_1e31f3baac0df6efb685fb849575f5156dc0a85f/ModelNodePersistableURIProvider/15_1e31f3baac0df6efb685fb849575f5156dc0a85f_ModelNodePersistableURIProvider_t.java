 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.core.adapters;
 
 import java.net.URI;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.tcf.te.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNodeProvider;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNameProvider;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider;
 
 /**
  * Model node persistable adapter implementation.
  */
 public class ModelNodePersistableURIProvider implements IPersistableURIProvider {
 
 	/**
 	 * Determine the model node from the given context object.
 	 *
 	 * @param context The context object or <code>null</code>.
 	 * @return The model node or <code>null</code>.
 	 */
 	private IModelNode getModelNode(Object context) {
 		IModelNode node = null;
 
 		if (context instanceof IModelNode) {
 			node = (IModelNode)context;
 		}
 		else if (context instanceof IModelNodeProvider) {
 			node = ((IModelNodeProvider)context).getModelNode();
 		}
 
 		return node;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider#getURI(java.lang.Object)
 	 */
 	@Override
 	public URI getURI(Object context) {
 		Assert.isNotNull(context);
 
 		URI uri = null;
 
 		IModelNode node = getModelNode(context);
 
 		// Only model nodes are supported
 		if (node != null) {
 
 			IPath path = null;
 
 			// Try to adapt the node to the IPersistableNameProvider interface first
 			IPersistableNameProvider provider = (IPersistableNameProvider)node.getAdapter(IPersistableNameProvider.class);
 			if (provider != null) {
 				String name = provider.getName(node);
 				if (name != null && !"".equals(name.trim())) { //$NON-NLS-1$
 					path = getRoot().append(name.trim());
 				}
 			}
 
 			if (path == null) {
 				// If the path could not be determined via the IPersistableNameProvider interface, check for the node id
 				if (node.getStringProperty(IModelNode.PROPERTY_ID) != null && !"".equals(node.getStringProperty(IModelNode.PROPERTY_ID).trim())) { //$NON-NLS-1$
 					path = getRoot().append(makeValidFileSystemName(node.getStringProperty(IModelNode.PROPERTY_ID).trim()));
 				}
 				// If the id is not set, check for the node name
 				else if (node.getName() != null && !"".equals(node.getName().trim())) { //$NON-NLS-1$
 					path = getRoot().append(makeValidFileSystemName(node.getName().trim()));
 				}
 				// If the name is not set, check for an URI
 				else if (node.getProperty(IPersistableNodeProperties.PROPERTY_URI) != null) {
 					Object candidate = node.getProperty(IPersistableNodeProperties.PROPERTY_URI);
 					if (candidate instanceof URI) {
 						uri = (URI)candidate;
 					}
 					else if (candidate instanceof String && !"".equals(((String)candidate).trim())) { //$NON-NLS-1$
 						uri = URI.create(((String)candidate).trim());
 					}
 				}
 				// No name and no explicit path is set -> use the UUID
 				else if (node.getUUID() != null) {
 					path = getRoot().append(makeValidFileSystemName(node.getUUID().toString().trim()));
 				}
 			}
 
 			if (path != null) {
 				if (!"ini".equals(path.getFileExtension())) { //$NON-NLS-1$
 					path = path.addFileExtension("ini"); //$NON-NLS-1$
 				}
 				uri = path.toFile().toURI();
 			}
 		}
 
 		return uri;
 	}
 
 	/**
 	 * Make a valid file system name from the given name.
 	 *
 	 * @param name The original name. Must not be <code>null</code>.
 	 * @return The valid file system name.
 	 */
 	protected String makeValidFileSystemName(String name) {
 		Assert.isNotNull(name);
 		return name.replaceAll("\\W", "_"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * Returns the root location.
 	 *
 	 * @return The root location or <code>null</code> if it cannot be determined.
 	 */
 	protected IPath getRoot() {
 		IPath location = null;
 
 		// Try the bundles state location first (not available if launched with -data @none).
 		try {
 			IPath path = Platform.getStateLocation(CoreBundleActivator.getContext().getBundle()).append(".store"); //$NON-NLS-1$
			boolean exists = path.toFile().exists();
			if (!exists) exists = path.toFile().mkdirs();
			if (exists && path.toFile().canRead() && path.toFile().isDirectory()) {
 				location = path;
 			}
 		} catch (IllegalStateException e) {
 			// Workspace less environments (-data @none)
 			// The users local target definition persistence directory is $HOME/.tcf/.store.
 			IPath path = new Path(System.getProperty("user.home")).append(".tcf/.store"); //$NON-NLS-1$ //$NON-NLS-2$
			boolean exists = path.toFile().exists();
			if (!exists) exists = path.toFile().mkdirs();
			if (exists && path.toFile().canRead() && path.toFile().isDirectory()) {
 				location = path;
 			}
 		}
 
 		return location;
 	}
 }
