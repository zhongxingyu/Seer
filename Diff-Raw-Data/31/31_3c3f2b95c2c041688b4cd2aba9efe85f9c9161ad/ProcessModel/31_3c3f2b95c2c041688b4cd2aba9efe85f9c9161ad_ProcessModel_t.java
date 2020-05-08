 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.model;
 
 import java.beans.PropertyChangeEvent;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.core.interfaces.IPropertyChangeProvider;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.tcf.filesystem.core.model.ITreeNodeModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.processes.core.nls.Messages;
 
 /**
  * The process tree model implementation.
  */
 public class ProcessModel implements ITreeNodeModel{
 	/* default */static final String PROCESS_ROOT_KEY = CoreBundleActivator.getUniqueIdentifier() + ".process.root"; //$NON-NLS-1$
 
 	/**
 	 * Get the process model stored in the peer model.
 	 * If there's no process model yet, create a new process model.
 	 *
 	 * @param peerModel The target's peer model.
 	 * @return The process model representing the process.
 	 */
 	public static ProcessModel getProcessModel(final IPeerModel peerModel) {
 		if (peerModel != null) {
 			if (Protocol.isDispatchThread()) {
 				ProcessModel model = (ProcessModel) peerModel.getProperty(PROCESS_ROOT_KEY);
 				if (model == null) {
 					model = new ProcessModel(peerModel);
 					peerModel.setProperty(PROCESS_ROOT_KEY, model);
 				}
 				return model;
 			}
 			final AtomicReference<ProcessModel> reference = new AtomicReference<ProcessModel>();
 			Protocol.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					reference.set(getProcessModel(peerModel));
 				}
 			});
 			return reference.get();
 		}
 		return null;
 	}
 
 	/**
 	 * Create a root process node.
 	 *
 	 * @param peerModel The peer model which this process belongs to.
 	 * @return The root process node.
 	 */
 	static ProcessTreeNode createRootNode(IPeerModel peerModel) {
 		ProcessTreeNode node = new ProcessTreeNode();
 		node.type = "ProcRootNode"; //$NON-NLS-1$
 		node.peerNode = peerModel;
 		node.name = Messages.ProcessLabelProvider_RootNodeLabel;
 		return node;
 	}
 
 	// The root node of the peer model
 	ProcessTreeNode root;
 	// The polling interval in seconds. If it is zero, then stop polling periodically.
 	/* default */int interval;
 	// The timer to schedule polling task.
 	/* default */Timer pollingTimer;
 	// The flag to indicate if the polling has been stopped.
 	/* default */boolean stopped;
 	IPeerModel peerModel;
 	// The periodic refreshing callback.
 	/**
 	 * Create a File System Model.
 	 */
 	ProcessModel(IPeerModel peerModel) {
 		this.peerModel = peerModel;
 		this.stopped = true;
 	}
 
 	/**
 	 * Get the root node of the peer model.
 	 *
 	 * @return The root node.
 	 */
 	@Override
     public ProcessTreeNode getRoot() {
 		if(root == null) {
 			root = createRoot();
 		}
 		return root;
 	}
 
 	/**
 	 * Set the root node of the peer model.
 	 *
 	 * @param root The root node
 	 */
 	ProcessTreeNode createRoot() {
 		return createRootNode(peerModel);
 	}
 
 	/**
 	 * Start the periodical polling.
 	 */
 	void startPolling() {
 	    setStopped(false);
 	    pollingTimer = new Timer();
 		scheduleRefreshing();
     }
 
 	/**
 	 * Set the status of the polling and
 	 * fire a property change event.
 	 *
 	 * @param stopped if the polling should be stopped.
 	 */
 	void setStopped(boolean stopped) {
 		if(this.stopped != stopped) {
 			boolean old = this.stopped;
 			this.stopped = stopped;
 			Boolean oldValue = Boolean.valueOf(old);
 			Boolean newValue = Boolean.valueOf(stopped);
 			PropertyChangeEvent event = new PropertyChangeEvent(getRoot(), "stopped", oldValue, newValue); //$NON-NLS-1$
 			IPropertyChangeProvider provider = (IPropertyChangeProvider) peerModel.getAdapter(IPropertyChangeProvider.class);
 			provider.firePropertyChange(event);
 		}
     }
 
 	/**
 	 * Stop the periodical polling.
 	 */
 	void stopPolling() {
 		setStopped(true);
 	}
 
 	/**
 	 * Schedule the periodical refreshing.
 	 */
 	void scheduleRefreshing() {
		final ICallback refreshCallback = new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				if (!stopped) {
					scheduleRefreshing();
				}
				else {
					if (pollingTimer != null) {
						pollingTimer.cancel();
						pollingTimer = null;
					}
				}
			}
		};
 		TimerTask pollingTask = new TimerTask(){
 			@Override
 	        public void run() {
 				if (root != null && root.childrenQueried && !root.childrenQueryRunning) {
 					root.refresh(refreshCallback);
 				}
 				else {
 					refreshCallback.done(this, Status.OK_STATUS);
 				}
 	        }};
         pollingTimer.schedule(pollingTask, interval * 1000L);
     }
 
 	/**
 	 * Set new interval.
 	 *
 	 * @param interval The new interval.
 	 */
 	public void setInterval(int interval) {
 		Assert.isTrue(interval >= 0);
 		if (this.interval != interval) {
 			if(this.interval == 0) {
 				this.interval = interval;
 				startPolling();
 			} else {
 				this.interval = interval;
 				if(interval == 0) {
 					stopPolling();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get the current interval.
 	 *
 	 * @return the current interval.
 	 */
 	public int getInterval() {
 		return interval;
 	}
 
 	/**
 	 * If the polling has been stopped.
 	 *
 	 * @return true if it is stopped.
 	 */
 	public boolean isRefreshStopped() {
 	    return stopped;
     }
 }
 
