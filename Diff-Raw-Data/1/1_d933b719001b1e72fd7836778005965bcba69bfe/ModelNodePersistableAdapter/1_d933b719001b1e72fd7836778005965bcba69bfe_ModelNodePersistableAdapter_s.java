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
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.core.nls.Messages;
 import org.eclipse.tcf.te.runtime.model.factory.Factory;
 import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.persistence.PersistenceDelegateManager;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.osgi.framework.Bundle;
 
 /**
  * Model node persistable adapter implementation.
  */
 public class ModelNodePersistableAdapter implements IPersistable {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getStorageID()
 	 */
 	@Override
 	public String getStorageID() {
 		return "org.eclipse.tcf.te.runtime.persistence.properties"; //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getURI(java.lang.Object)
 	 */
 	@Override
 	public URI getURI(Object data) {
 		Assert.isNotNull(data);
 
 		URI uri = null;
 
 		// Only model nodes are supported
 		if (data instanceof IModelNode) {
 			IModelNode node = (IModelNode) data;
 
 			IPath path = null;
 
 			// If the persistence node name is set, use it and ignore all other possibilities
 			String persistenceNodeName = node.getStringProperty(IPersistableNodeProperties.PROPERTY_NODE_NAME);
 			if (persistenceNodeName != null && !"".equals(node.getName().trim())) { //$NON-NLS-1$
 				path = getRoot().append(makeValidFileSystemName(persistenceNodeName.trim()));
 			}
 			// If the persistence name not set, check for the node name
 			else if (node.getName() != null && !"".equals(node.getName().trim())) { //$NON-NLS-1$
 				path = getRoot().append(makeValidFileSystemName(node.getName().trim()));
 			}
 			// If the name is not set, check for an URI
 			else if (node.getProperty(IPersistableNodeProperties.PROPERTY_URI) != null) {
 				Object candidate = node.getProperty(IPersistableNodeProperties.PROPERTY_URI);
 				if (candidate instanceof URI) uri = (URI)candidate;
 				else if (candidate instanceof String && !"".equals(((String)candidate).trim())) { //$NON-NLS-1$
 					uri = URI.create(((String)candidate).trim());
 				}
 			}
 			// No name and no explicit path is set -> use the UUID
 			else if (node.getUUID() != null) {
 				path = getRoot().append(makeValidFileSystemName(node.getUUID().toString().trim()));
 			}
 
 			if (path != null) {
 				if (!"ini".equals(path.getFileExtension())) path = path.addFileExtension("ini"); //$NON-NLS-1$ //$NON-NLS-2$
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
 	private String makeValidFileSystemName(String name) {
 		Assert.isNotNull(name);
 		return name.replaceAll("\\W", "_"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * Returns the root location.
 	 *
 	 * @return The root location or <code>null</code> if it cannot be determined.
 	 */
 	public IPath getRoot() {
 		IPath location = null;
 
 		// Try the bundles state location first (not available if launched with -data @none).
 		try {
 			IPath path = Platform.getStateLocation(CoreBundleActivator.getContext().getBundle()).append(".store"); //$NON-NLS-1$
 			if (!path.toFile().exists()) path.toFile().mkdirs();
 			if (path.toFile().canRead() && path.toFile().isDirectory()) {
 				location = path;
 			}
 		} catch (IllegalStateException e) {
 			// Workspace less environments (-data @none)
 			// The users local target definition persistence directory is $HOME/.tcf/.store.
 			IPath path = new Path(System.getProperty("user.home")).append(".tcf/.store"); //$NON-NLS-1$ //$NON-NLS-2$
 			if (!path.toFile().exists()) path.toFile().mkdirs();
 			if (path.toFile().canRead() && path.toFile().isDirectory()) {
 				location = path;
 			}
 		}
 
 		return location;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getInterfaceType(java.lang.Object)
 	 */
 	@SuppressWarnings("restriction")
     @Override
 	public String getInterfaceTypeName(Object data) {
 		if (data instanceof IContainerModelNode) {
 			return org.eclipse.tcf.te.runtime.model.activator.CoreBundleActivator.getUniqueIdentifier() + ":" + IContainerModelNode.class.getName(); //$NON-NLS-1$
 		} else if (data instanceof IModelNode) {
 			return org.eclipse.tcf.te.runtime.model.activator.CoreBundleActivator.getUniqueIdentifier() + ":" + IModelNode.class.getName(); //$NON-NLS-1$
 		}
 	    return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#exportFrom(java.lang.Object)
 	 */
 	@Override
 	public Map<String, Object> exportFrom(Object data) throws IOException {
 		Assert.isNotNull(data);
 
 		// Create a new map instance that will hold the exported properties
 		Map<String, Object> result = new HashMap<String, Object>();
 
 		// Only model nodes are supported
 		if (data instanceof IModelNode && !((IModelNode)data).isEmpty()) {
 			// Get a snapshot of all properties
 			Map<String, Object> properties = ((IModelNode)data).getProperties();
 			// And export the properties to the result map
 			exportFromMap(properties, result);
 		}
 
 		// If the result map is empty, return null
 		return !result.isEmpty() ? result : null;
 	}
 
 	/**
 	 * Exports the properties of a map from the given source into the given
 	 * destination.
 	 *
 	 * @param src The map to export the properties from. Must not be <code>null</code>.
 	 * @param dst The map to write the exported properties to. Must not be <code>null</code>.
 	 *
 	 * @throws IOException - if the operation fails.
 	 */
 	@SuppressWarnings("unchecked")
     protected void exportFromMap(Map<String, Object> src, Map<String, Object> dst) throws IOException {
 		Assert.isNotNull(src);
 		Assert.isNotNull(dst);
 
 		// Loop all properties and check for transient or complex properties
 		for (String key : src.keySet()) {
 			if (key.contains(".transient")) continue; //$NON-NLS-1$
 
 			// Get the property value
 			Object value = src.get(key);
 
 			// If the value is null, no need to go any further
 			if (value == null) continue;
 
 			// For String, Integer, Boolean, etc ... export them as string
 			boolean isSimpleType = value instanceof String || value instanceof Boolean || value instanceof Integer || value instanceof Long
 										|| value instanceof Float || value instanceof Double;
 			if (isSimpleType) {
 				dst.put(key, value.toString());
 				continue;
 			}
 
 			// BigInteger, BigDecimal ... probably needs special handling, for now, export them as string
 			boolean isBigType = value instanceof BigInteger || value instanceof BigDecimal;
 			if (isBigType) {
 				dst.put(key, value.toString());
 				continue;
 			}
 
 			// For Lists and Arrays, do a deepToString
 			boolean isListType = value instanceof List<?> || value instanceof Object[];
 			if (isListType) {
 				dst.put(key, Arrays.deepToString(value instanceof List<?> ? ((List<?>)value).toArray() : (Object[])value));
 				continue;
 			}
 
 			// For Maps, create a new destination map and call ourself
 			boolean isMapType = value instanceof Map<?,?>;
 			if (isMapType) {
 				Map<String, Object> result = new HashMap<String, Object>();
 				exportFromMap((Map<String, Object>)value, result);
 				if (!result.isEmpty()) dst.put(key, result);
 				continue;
 			}
 
 			// For anything remaining, check if the value object type can be adapted to
 			// an IPersistable itself
 			IPersistable persistable = value instanceof IAdaptable ? (IPersistable)((IAdaptable)value).getAdapter(IPersistable.class) : null;
 			if (persistable == null) persistable = (IPersistable)Platform.getAdapterManager().getAdapter(value, IPersistable.class);
 			if (persistable != null) {
 				String storageID = persistable.getStorageID();
 				URI uri = persistable.getURI(value);
 				String interfaceTypeName = persistable.getInterfaceTypeName(value);
 
 				// Check if the persistable returns complete information to create the reference
 				if (storageID == null) {
 					throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_export_invalidPersistable, value.getClass().getCanonicalName(), "storageID")); //$NON-NLS-1$
 				}
 				if (uri == null) {
 					throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_export_invalidPersistable, value.getClass().getCanonicalName(), "uri")); //$NON-NLS-1$
 				}
 				if (interfaceTypeName == null) {
 					throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_export_invalidPersistable, value.getClass().getCanonicalName(), "interfaceTypeName")); //$NON-NLS-1$
 				}
 
 				// Create a reference object
 				Map<String, String> reference = new HashMap<String, String>();
 				reference.put("storageID", storageID); //$NON-NLS-1$
 				reference.put("uri", uri.toString()); //$NON-NLS-1$
 				reference.put("interfaceType", interfaceTypeName); //$NON-NLS-1$
 
 				IPersistenceDelegate delegate = PersistenceDelegateManager.getInstance().getDelegate(storageID, false);
 				if (delegate != null) {
 					delegate.write(uri, persistable.exportFrom(value));
 					dst.put("reference:" + key, reference); //$NON-NLS-1$
 					continue;
 				}
 			}
 
 			// Falling through down here is a problem. We should never end up here,
 			// because it means we have no idea on how to persist an object
 			throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_export_unknownType, value.getClass().getCanonicalName(), key));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#importTo(java.lang.Object, java.util.Map)
 	 */
 	@Override
 	public void importTo(Object data, Map<String, Object> external) throws IOException {
 		Assert.isNotNull(data);
 		Assert.isNotNull(external);
 
 		// Only model nodes are supported
 		if (data instanceof IModelNode) {
 			IModelNode node = (IModelNode) data;
 			for (String key : external.keySet()) {
 				// Get the property value
 				Object value = external.get(key);
 
 				// Check for reference objects
 				if (key.startsWith("reference:") && value instanceof Map<?,?>) { //$NON-NLS-1$
 					// Cut the "reference:" from the key
 					String newKey = key.substring(10);
 
 					@SuppressWarnings("unchecked")
                     Map<String, String> reference = (Map<String, String>)value;
 
 					// Get the storage id and the URI from the reference
 					String storageID = reference.get("storageID"); //$NON-NLS-1$
 					String uriString = reference.get("uri"); //$NON-NLS-1$
 					String interfaceTypeName = reference.get("interfaceType"); //$NON-NLS-1$
 
 					// Check if the reference returns complete information to read the referenced storage
 					if (storageID == null) {
 						throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_import_invalidReference, "storageID")); //$NON-NLS-1$
 					}
 					if (uriString == null) {
 						throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_import_invalidReference, "uri")); //$NON-NLS-1$
 					}
 					if (interfaceTypeName == null) {
 						throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_import_invalidReference, "interfaceType")); //$NON-NLS-1$
 					}
 
 					// Get the persistence delegate
 					IPersistenceDelegate delegate = PersistenceDelegateManager.getInstance().getDelegate(storageID, false);
 					if (delegate != null) {
 						URI uri = URI.create(uriString);
 						Map<String, Object> referenceData = delegate.read(uri);
 						if (referenceData != null && !referenceData.isEmpty()) {
 							try {
 								// Now, we have to recreate the object
 
 								// Separate the bundleId from the interface name
 								String[] pieces = interfaceTypeName.split(":", 2); //$NON-NLS-1$
 								String bundleId = pieces.length > 1 ? pieces[0] : null;
 								if (pieces.length > 1) interfaceTypeName = pieces[1];
 
 								// Determine the bundle to use for loading the class
 								Bundle bundle = bundleId != null && !"".equals(bundleId.trim()) ? Platform.getBundle(bundleId.trim()) : CoreBundleActivator.getContext().getBundle(); //$NON-NLS-1$
 
 								Class<? extends IModelNode> interfaceType = (Class<? extends IModelNode>)bundle.loadClass(interfaceTypeName);
 								IModelNode referenceNode = Factory.getInstance().newInstance(interfaceType);
 
 								IPersistable persistable = (IPersistable)referenceNode.getAdapter(IPersistable.class);
 								if (persistable == null) persistable = (IPersistable)Platform.getAdapterManager().getAdapter(referenceNode, IPersistable.class);
 								if (persistable != null) {
 									persistable.importTo(referenceNode, referenceData);
 									node.setProperty(newKey, referenceNode);
 								}
 							} catch (ClassNotFoundException e) {
 								throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_import_cannotLoadClass, interfaceTypeName), e);
 							}
 						}
 					}
 				}
 				// Not a reference object -> store the object as is to the node
 				else {
 					node.setProperty(key, value);
 				}
 			}
 		}
 	}
 
 }
