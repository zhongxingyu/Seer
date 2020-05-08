 /*******************************************************************************
  * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.model.runtime.services;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService;
 import org.eclipse.tcf.te.tcf.core.model.services.AbstractModelService;
import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNodeProperties;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelLookupService;
 
 /**
  * Runtime model lookup service implementation.
  */
 public class RuntimeModelLookupService extends AbstractModelService<IRuntimeModel> implements IRuntimeModelLookupService {
 
 	/**
 	 * Constructor.
 	 *
 	 * @param model The parent model. Must not be <code>null</code>.
 	 */
 	public RuntimeModelLookupService(IRuntimeModel model) {
 		super(model);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelLookupService#lkupModelNodeByUUID(java.util.UUID)
 	 */
 	@Override
 	public IModelNode lkupModelNodeByUUID(UUID uuid) {
 	    return getModel().find(uuid);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelLookupService#lkupModelNodesById(java.lang.String)
 	 */
 	@Override
 	public IModelNode[] lkupModelNodesById(String id) {
 		Assert.isNotNull(id);
 
 		List<IModelNode> nodes = new ArrayList<IModelNode>();
 		nodes.addAll(findInContainerByIdRecursively(getModel(), id));
 
 		return nodes.toArray(new IModelNode[nodes.size()]);
 	}
 
 	/**
 	 * Search the given container recursively and returns all nodes matching the given id.
 	 *
 	 * @param container The container. Must not be <code>null</code<.
 	 * @param id The id to match. Must not be <code>null</code>.
 	 *
 	 * @return The list of matching nodes, or an empty list.
 	 */
 	private List<IModelNode> findInContainerByIdRecursively(IContainerModelNode container, String id) {
 		Assert.isNotNull(container);
 		Assert.isNotNull(id);
 
 		List<IModelNode> nodes = new ArrayList<IModelNode>();
 		List<IModelNode> candidates = container.getChildren(IModelNode.class);
 		for (IModelNode candidate : candidates) {
 			if (id.equals(candidate.getStringProperty(IProcessContextNodeProperties.PROPERTY_ID)) && !nodes.contains(candidate)) {
 				nodes.add(candidate);
 			}
 			if (candidate instanceof IContainerModelNode) nodes.addAll(findInContainerByIdRecursively((IContainerModelNode)candidate, id));
 		}
 
 		return nodes;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelLookupService#lkupModelNodesByName(java.lang.String)
 	 */
 	@Override
     public IModelNode[] lkupModelNodesByName(String name) {
 		Assert.isNotNull(name);
 
 		List<IModelNode> nodes = new ArrayList<IModelNode>();
 		nodes.addAll(findInContainerByNameRecursively(getModel(), name));
 
 		return nodes.toArray(new IModelNode[nodes.size()]);
 	}
 
 	/**
 	 * Search the given container recursively and returns all nodes matching the given name.
 	 *
 	 * @param container The container. Must not be <code>null</code<.
 	 * @param name The name to match. Must not be <code>null</code>.
 	 *
 	 * @return The list of matching nodes, or an empty list.
 	 */
 	private List<IModelNode> findInContainerByNameRecursively(IContainerModelNode container, String name) {
 		Assert.isNotNull(container);
 		Assert.isNotNull(name);
 
 		List<IModelNode> nodes = new ArrayList<IModelNode>();
 		List<IModelNode> candidates = container.getChildren(IModelNode.class);
 		for (IModelNode candidate : candidates) {
 			if (name.equals(candidate.getName()) && !nodes.contains(candidate)) {
 				nodes.add(candidate);
 			}
 			if (candidate instanceof IContainerModelNode) nodes.addAll(findInContainerByNameRecursively((IContainerModelNode)candidate, name));
 		}
 
 		return nodes;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelLookupService#lkupModelNodeByCapability(java.lang.String[], org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void lkupModelNodeByCapability(final String[] capabilities, final ICallback callback) {
 		Assert.isNotNull(capabilities);
 		Assert.isTrue(capabilities.length > 0);
 		Assert.isNotNull(callback);
 
 		final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)getModel().getAdapter(IAsyncRefreshableCtx.class);
 		if (refreshable != null && refreshable.getQueryState(QueryType.CHILD_LIST) != QueryState.DONE) {
 			// The model needs a refresh
 			getModel().getService(IModelRefreshService.class).refresh(new Callback() {
 				@Override
 				protected void internalDone(Object caller, IStatus status) {
 					callback.setResult(findInContainerByCapabilitiesRecursively(getModel(), capabilities));
 					callback.done(RuntimeModelLookupService.this, Status.OK_STATUS);
 				}
 			});
 		} else {
 			callback.setResult(findInContainerByCapabilitiesRecursively(getModel(), capabilities));
 			callback.done(RuntimeModelLookupService.this, Status.OK_STATUS);
 		}
 	}
 
 	/**
 	 * Search the given container recursively and returns all nodes matching the given capabilities.
 	 *
 	 * @param container The container. Must not be <code>null</code<.
 	 * @param capabilities The capabilities to match. Must not be <code>null</code>.
 	 *
 	 * @return The list of matching nodes, or an empty list.
 	 */
 	protected IProcessContextNode findInContainerByCapabilitiesRecursively(IContainerModelNode container, String[] capabilities) {
 		Assert.isNotNull(container);
 		Assert.isNotNull(capabilities);
 
		if (Boolean.getBoolean("sm.trace.rootnodelkup")) { //$NON-NLS-1$
			String message = "RuntimeModelLookupServer: findInContainerByCapabilitiesRecursively: container=" + container + ", capabilities=" + Arrays.deepToString(capabilities); //$NON-NLS-1$ //$NON-NLS-2$
			IStatus s = new Status(IStatus.INFO, CoreBundleActivator.getUniqueIdentifier(), message);
			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(s);
		}

 		IProcessContextNode node = null;
 		List<IProcessContextNode> candidates = container.getChildren(IProcessContextNode.class);
 		for (IProcessContextNode candidate : candidates) {
 			Map<String, Object> caps = (Map<String, Object>)candidate.getProperty(IProcessContextNodeProperties.PROPERTY_CAPABILITIES);

			if (Boolean.getBoolean("sm.trace.rootnodelkup")) { //$NON-NLS-1$
				String message = "RuntimeModelLookupServer: findInContainerByCapabilitiesRecursively:        candidate=" + candidate + ", capabilities=" + caps.keySet(); //$NON-NLS-1$ //$NON-NLS-2$
				IStatus s = new Status(IStatus.INFO, CoreBundleActivator.getUniqueIdentifier(), message);
				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(s);
			}

 			if (caps != null) {
 				boolean allFound = true;
 				for (String capability : capabilities) {
 					if (!caps.containsKey(capability) || !Boolean.parseBoolean(caps.get(capability).toString())) {
 						allFound = false;
 						break;
 					}
 				}
 
 				if (allFound) {
 					node = candidate;
 					break;
 				}
 			}
 		}
 
 		if (node == null) {
 			for (IProcessContextNode candidate : candidates) {
 				node = findInContainerByCapabilitiesRecursively(candidate, capabilities);
 				if (node != null) {
 					break;
 				}
 			}
 		}
 
		if (Boolean.getBoolean("sm.trace.rootnodelkup")) { //$NON-NLS-1$
			String message = "RuntimeModelLookupServer: findInContainerByCapabilitiesRecursively:        node=" + node; //$NON-NLS-1$
			IStatus s = new Status(IStatus.INFO, CoreBundleActivator.getUniqueIdentifier(), message);
			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(s);
		}

 		return node;
 	}
 }
