 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.core.model.runtime.listener;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.tcf.services.IProcesses;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelLookupService;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelUpdateService;
 import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.processes.core.interfaces.tracing.ITraceIds;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 
 /**
  * Process service runtime model service listener implementation.
  */
 public class RuntimeModelProcessServiceListener implements IProcesses.ProcessesListener {
 	// Reference to the parent model
 	private final IRuntimeModel model;
 
 	/**
      * Constructor.
      */
     public RuntimeModelProcessServiceListener(IRuntimeModel model) {
     	Assert.isNotNull(model);
     	this.model = model;
     }
 
 	/**
 	 * Returns the parent runtime model.
 	 *
 	 * @return The parent runtime model.
 	 */
 	public final IRuntimeModel getModel() {
 		return model;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.services.IProcesses.ProcessesListener#exited(java.lang.String, int)
 	 */
     @Override
     public void exited(String id, int exit_code) {
     	if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_SERVICE_LISTENER)) {
    		CoreBundleActivator.getTraceHandler().trace("RuntimeModelProcessServiceListener#exited: id = \"" + id + //$NON-NLS-1$
    						"\" exitCode=\"" + exit_code + "\"", //$NON-NLS-1$ //$NON-NLS-2$
    						0, ITraceIds.TRACE_SERVICE_LISTENER,
    						IStatus.INFO, this);
     	}

   		// Find the terminated process
   		IModelNode[] nodes = model.getService(IModelLookupService.class).lkupModelNodesById(id);
   		for (IModelNode node : nodes) {
   			// Remove the terminated nodes from the model
   			model.getService(IModelUpdateService.class).remove(node);
   		}
     }
 }
