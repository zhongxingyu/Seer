 /*******************************************************************************
  * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.model.runtime;
 
 import java.util.EventObject;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.core.interfaces.IConnectable;
 import org.eclipse.tcf.te.core.interfaces.IFilterable;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
 import org.eclipse.tcf.te.runtime.model.ContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.contexts.AsyncRefreshableCtxAdapter;
 import org.eclipse.tcf.te.runtime.model.factory.Factory;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType;
 import org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactory;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelChannelService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelLookupService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelUpdateService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProperties;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelLookupService;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelRefreshService;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModelUpdateService;
 import org.eclipse.tcf.te.tcf.processes.core.model.runtime.services.RuntimeModelChannelService;
 import org.eclipse.tcf.te.tcf.processes.core.model.runtime.services.RuntimeModelLookupService;
 import org.eclipse.tcf.te.tcf.processes.core.model.runtime.services.RuntimeModelRefreshService;
 import org.eclipse.tcf.te.tcf.processes.core.model.runtime.services.RuntimeModelUpdateService;
 
 
 /**
  * ModelManager implementation dealing with Processes at runtime.
  */
 public final class RuntimeModel extends ContainerModelNode implements IRuntimeModel, IFilterable {
 	// Flag to mark the model disposed
 	private boolean disposed;
 
 	// Reference to the model node factory
 	private IFactory factory = null;
 
 	// Reference to the associated peer model
 	private final IPeerNode peerNode;
 
 	// Reference to the refresh service
 	private final IRuntimeModelRefreshService refreshService = new RuntimeModelRefreshService(this);
 	// Reference to the lookup service
 	private final IRuntimeModelLookupService lookupService = new RuntimeModelLookupService(this);
 	// Reference to the update service
 	private final IRuntimeModelUpdateService updateService = new RuntimeModelUpdateService(this);
 	// Reference to the channel service
 	private final IModelChannelService channelService = new RuntimeModelChannelService(this);
 
 	// The runtime model needs asynchronous refreshes
 	private final IAsyncRefreshableCtx refreshableCtxAdapter = new AsyncRefreshableCtxAdapter();
 
 	// The auto-refresh interval in seconds
 	/* default */ int interval = 0;
 	// The auto-refresh timer
 	/* default */ Timer timer = null;
 
 	private IEventListener listener = null;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param peerNode The peerNode to associated. Must not be <code>null</code>.
 	 */
 	public RuntimeModel(IPeerNode peerNode) {
 		super();
 
 		disposed = false;
 		setChangeEventsEnabled(true);
 		suppressEventsOnNullParent = false;
 
 		Assert.isNotNull(peerNode);
 		this.peerNode = peerNode;
 
 		if (listener == null) {
 			listener = new IEventListener() {
 				@Override
 				public void eventFired(EventObject event) {
 					if (event instanceof ChangeEvent) {
						final ChangeEvent changeEvent = (ChangeEvent)event;
						Protocol.invokeLater(new Runnable() {
							@Override
							public void run() {
								if (getPeerNode() == changeEvent.getSource() && IPeerNodeProperties.PROP_CONNECT_STATE.equals(changeEvent.getEventId())) {
									if (!changeEvent.getNewValue().equals(new Integer(IConnectable.STATE_CONNECTED))) {
										clear();
									}
								}
 							}
						});
 					}
 				}
 			};
 			EventManager.getInstance().addEventListener(listener, ChangeEvent.class);
 		}
 
 		// No initial context query required
 		refreshableCtxAdapter.setQueryState(QueryType.CONTEXT, QueryState.DONE);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.nodes.PropertiesContainer#checkThreadAccess()
 	 */
 	@Override
 	protected boolean checkThreadAccess() {
 		return Protocol.isDispatchThread();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.IModel#dispose()
 	 */
 	@Override
 	public void dispose() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		disposed = true;
 
 		if (listener != null) {
 			EventManager.getInstance().removeEventListener(listener);
 		}
 
 		// Close the active channel (if any)
 		channelService.closeChannel();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.IModel#isDisposed()
 	 */
 	@Override
 	public boolean isDisposed() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		return disposed;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.IModel#getService(java.lang.Class)
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public <V extends IModelService> V getService(Class<V> serviceInterface) {
 		Assert.isNotNull(serviceInterface);
 		return (V)getAdapter(serviceInterface);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (IRuntimeModelRefreshService.class.equals(adapter)) {
 			return refreshService;
 		}
 		if (IModelRefreshService.class.equals(adapter)) {
 			return refreshService;
 		}
 		if (IRuntimeModelLookupService.class.equals(adapter)) {
 			return lookupService;
 		}
 		if (IModelLookupService.class.equals(adapter)) {
 			return lookupService;
 		}
 		if (IRuntimeModelUpdateService.class.equals(adapter)) {
 			return updateService;
 		}
 		if (IModelUpdateService.class.equals(adapter)) {
 			return updateService;
 		}
 		if (IModelChannelService.class.equals(adapter)) {
 			return channelService;
 		}
 		if (IAsyncRefreshableCtx.class.equals(adapter)) {
 			return refreshableCtxAdapter;
 		}
 
 		return super.getAdapter(adapter);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.IModel#setFactory(org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactory)
 	 */
 	@Override
 	public void setFactory(IFactory factory) {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		this.factory = factory;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.IModel#getFactory()
 	 */
 	@Override
 	public IFactory getFactory() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		return factory != null ? factory : Factory.getInstance();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProvider#getPeerModel()
 	 */
 	@Override
 	public IPeerNode getPeerNode() {
 		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
 		return peerNode;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel#setAutoRefreshInterval(int)
 	 */
 	@Override
 	public final void setAutoRefreshInterval(int interval) {
 		// Normalize the interval
 		if (interval < 0) interval = 0;
 		// Remember the old value (for the change event)
 		final int oldInterval = this.interval;
 		// Apply the new interval
 		this.interval = interval;
 		// If the interval has changed, start/stop the auto-refresh and send a change notification
 		if (oldInterval != interval) {
 			// Get the auto-refresh started if not yet scheduled
 			if (interval != 0 && timer == null) {
 				// Create the timer task to schedule
 				TimerTask task = new TimerTask() {
 					@Override
 					public void run() {
 						// Drop out of the interval has been set to 0 in the meanwhile
 						if (RuntimeModel.this.interval == 0) return;
 						// Do the auto refresh
 						doAutoRefresh();
 					}
 				};
 
 				// Create the timer
 				timer = new Timer();
 				timer.schedule(task, this.interval * 1000);
 			} else if (interval == 0 && timer != null) {
 				timer.cancel();
 				timer = null;
 			}
 
 			// Signal the change to the auto-refresh interval
 			fireChangeEvent("autoRefreshInterval", Integer.valueOf(oldInterval), Integer.valueOf(interval)); //$NON-NLS-1$
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel#getAutoRefreshInterval()
 	 */
 	@Override
 	public final int getAutoRefreshInterval() {
 	    return interval;
 	}
 
 	/**
 	 * Execute the auto refresh of the model and reschedule until stopped.
 	 */
 	/* default */ void doAutoRefresh() {
 		Protocol.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				// Refresh the model
 				RuntimeModel.this.getService(IRuntimeModelRefreshService.class).autoRefresh(new Callback() {
 					@Override
 					protected void internalDone(Object caller, IStatus status) {
 						// Re-schedule ourself if the interval is still > 0
 						if (RuntimeModel.this.interval > 0 && timer != null) {
 							// Create the timer task to schedule
 							TimerTask task = new TimerTask() {
 								@Override
 								public void run() {
 									// Drop out of the interval has been set to 0 in the meanwhile
 									if (RuntimeModel.this.interval == 0) return;
 									// Do the auto refresh
 									doAutoRefresh();
 								}
 							};
 
 							timer.schedule(task, RuntimeModel.this.interval * 1000);
 						}
 					}
 				});
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.model.ModelNode#toString()
 	 */
 	@Override
 	public String toString() {
 		if (disposed) {
 			return "*DISPOSED* : " + super.toString(); //$NON-NLS-1$
 		}
 	    return super.toString();
 	}
 }
