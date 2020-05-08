 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.editor.tree.columns;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IPendingOperationNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.runtime.IRuntimeModel;
 import org.eclipse.tcf.te.tcf.processes.ui.navigator.runtime.AbstractLabelProviderDelegate;
 
 /**
  * The label provider for the tree column "user".
  */
 public class UserLabelProvider extends AbstractLabelProviderDelegate {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
 	 */
 	@Override
 	public String getText(Object element) {
 		if (element instanceof IRuntimeModel || element instanceof IPendingOperationNode) {
 			return ""; //$NON-NLS-1$
 		}
 
 		if (element instanceof IProcessContextNode) {
 			final IProcessContextNode node = (IProcessContextNode)element;
 
 			final AtomicReference<String> username = new AtomicReference<String>();
 
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
					if (node.getSysMonitorContext() != null)
						username.set(node.getSysMonitorContext().getUserName());
 				}
 			};
 
 			Assert.isTrue(!Protocol.isDispatchThread());
 			Protocol.invokeAndWait(runnable);
 
 			if (username.get() != null) return username.get();
 		}
 
 		return ""; //$NON-NLS-1$
 	}
 }
