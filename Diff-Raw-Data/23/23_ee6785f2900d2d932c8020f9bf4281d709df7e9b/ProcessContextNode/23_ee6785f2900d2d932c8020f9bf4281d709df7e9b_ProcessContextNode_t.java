 /*******************************************************************************
  * Copyright (c) 2012, 2014 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.model.nodes;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IProcesses;
 import org.eclipse.tcf.services.ISysMonitor;
 import org.eclipse.tcf.services.ISysMonitor.SysMonitorContext;
import org.eclipse.tcf.te.core.interfaces.IConnectable;
 import org.eclipse.tcf.te.core.interfaces.IFilterable;
 import org.eclipse.tcf.te.runtime.model.ContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.contexts.AsyncRefreshableCtxAdapter;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.IModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProvider;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNodeProperties;
 
 /**
  * A process context node implementation.
  */
 public class ProcessContextNode extends ContainerModelNode implements IProcessContextNode, IPeerNodeProvider, IFilterable {
 	// Reference to the agent side process context object
 	private IProcesses.ProcessContext pContext = null;
 	// Reference to the agent side system monitor context object
 	private ISysMonitor.SysMonitorContext sContext = null;
 
 	// The node type
 	private TYPE type = TYPE.Unknown;
 
 	// Context nodes needs asynchronous refreshes
 	private final IAsyncRefreshableCtx refreshableCtxAdapter = new AsyncRefreshableCtxAdapter();
 
 	/**
 	 * Constructor.
 	 */
 	public ProcessContextNode() {
 		super();
 		setChangeEventsEnabled(true);
 
 		// No initial context query required
 		refreshableCtxAdapter.setQueryState(QueryType.CONTEXT, QueryState.DONE);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#checkThreadAccess()
 	 */
 	@Override
 	protected boolean checkThreadAccess() {
 	    return Protocol.isDispatchThread();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.model.ModelNode#getName()
 	 */
 	@Override
 	public String getName() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		// If the node is associated with an agent side context, take the name
 		// from the agent side context first
 		String name = pContext != null ? pContext.getName() : null;
 
 		// If the name is empty or the same as the context ID -> reset
 		if (name != null && ("".equals(name.trim()) || name.equals(pContext.getID()))) { //$NON-NLS-1$
 			name = null;
 		}
 
 		// Take the last part of the image name if available
 		String file = sContext != null ? sContext.getFile() : null;
 		if (file != null && !"".equals(file)) { //$NON-NLS-1$
 			IPath path = new Path(file);
 			// If it is a typical Windows path with a drive letter, take only the
 			// last segment. In all other cases, take "file" as it is
 			if (path.getDevice() != null && path.getDevice().endsWith(":") && path.getDevice().length() == 2) { //$NON-NLS-1$
 				name = path.lastSegment();
 			} else {
 				name = file;
 			}
 		}
 
 		// Fallback to the context ID
 		if (name == null || "".equals(name.trim())) name = pContext != null ? pContext.getID() : null; //$NON-NLS-1$
 
 		// Fallback to the local node properties
 		if (name == null || "".equals(name.trim())) name = getStringProperty(IProcessContextNodeProperties.PROPERTY_NAME); //$NON-NLS-1$
 		if (name == null || "".equals(name.trim())) name = getStringProperty(IProcessContextNodeProperties.PROPERTY_ID); //$NON-NLS-1$
 
 	    return name != null && !"".equals(name.trim()) ? name.trim() : super.getName(); //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode#setType(org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode.TYPE)
 	 */
 	@Override
 	public void setType(TYPE type) {
 		Assert.isNotNull(type);
 		this.type = type;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode#getType()
 	 */
     @Override
     public TYPE getType() {
 	    return type;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode#setContext(org.eclipse.tcf.services.IProcesses.ProcessContext)
 	 */
 	@Override
 	public final void setProcessContext(IProcesses.ProcessContext context) {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		this.pContext = context;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode#getContext()
 	 */
 	@Override
 	public final IProcesses.ProcessContext getProcessContext() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 	    return pContext;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode#setSysMonitorContext(org.eclipse.tcf.services.ISysMonitor.SysMonitorContext)
 	 */
     @Override
     public void setSysMonitorContext(SysMonitorContext context) {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		this.sContext = context;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode#getSysMonitorContext()
 	 */
     @Override
     public SysMonitorContext getSysMonitorContext() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 	    return sContext;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProvider#getPeerModel()
      */
     @Override
     public IPeerNode getPeerNode() {
         return (IPeerNode)getAdapter(IPeerNode.class);
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode#isComplete()
 	 */
 	@Override
 	public boolean isComplete() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		 // Return true if one of the contexts is not null.
 		if (getProcessContext() != null || getSysMonitorContext() != null) {
 			return true;
 		}
 
 		boolean complete = true;
 
 		// ID is mandatory and must not be empty
 		String id = getStringProperty(IProcessContextNodeProperties.PROPERTY_ID);
 		complete &= id != null && !"".equals(id); //$NON-NLS-1$
 
 	    return complete;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(final Class adapter) {
 		// NOTE: The getAdapter(...) method can be invoked from many place and
 		//       many threads where we cannot control the calls. Therefore, this
 		//       method is allowed be called from any thread.
 		final AtomicReference<Object> object = new AtomicReference<Object>();
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				object.set(doGetAdapter(adapter));
 			}
 		};
 
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeAndWait(runnable);
 
 		return object.get() != null ? object.get() : super.getAdapter(adapter);
 	}
 
 	/**
 	 * Returns an object which is an instance of the given class associated with this object.
 	 * Returns <code>null</code> if no such object can be found.
 	 * <p>
 	 * This method must be called within the TCF dispatch thread!
 	 *
 	 * @param adapter The adapter class to look up.
 	 * @return The adapter or <code>null</code>.
 	 */
 	protected Object doGetAdapter(Class<?> adapter) {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		if (IProcesses.ProcessContext.class.isAssignableFrom(adapter)) {
 			return pContext;
 		}
 		if (ISysMonitor.SysMonitorContext.class.isAssignableFrom(adapter)) {
 			return sContext;
 		}
 		if (IPeerNodeProvider.class.isAssignableFrom(adapter)) {
 			IModel model = getParent(IModel.class);
 			if (model instanceof IPeerNodeProvider) return model;
 		}
		if (IPeerNode.class.isAssignableFrom(adapter) || IConnectable.class.isAssignableFrom(adapter)) {
 			IModel model = getParent(IModel.class);
 			if (model instanceof IPeerNodeProvider) return ((IPeerNodeProvider)model).getPeerNode();
 		}
 		if (IAsyncRefreshableCtx.class.isAssignableFrom(adapter)) {
 			return refreshableCtxAdapter;
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.model.ModelNode#toString()
 	 */
 	@Override
 	public String toString() {
 		final AtomicReference<String> toString = new AtomicReference<String>(super.toString());
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				StringBuilder buffer = new StringBuilder(toString.get());
 				buffer.deleteCharAt(buffer.length() - 1);
 				buffer.append(", system monitor properties="); //$NON-NLS-1$
 				buffer.append(getSysMonitorContext() != null ? getSysMonitorContext().toString() : "{}"); //$NON-NLS-1$
 				buffer.append(", process properties="); //$NON-NLS-1$
 				buffer.append(getProcessContext() != null ? getProcessContext().toString() : "{}"); //$NON-NLS-1$
 				buffer.append("}"); //$NON-NLS-1$
 				toString.set(buffer.toString());
 			}
 		};
 
 		if (Protocol.isDispatchThread()) runnable.run();
 		else Protocol.invokeAndWait(runnable);
 
 	    return toString.get();
 	}
 }
