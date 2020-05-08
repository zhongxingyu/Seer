 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.handler;
 
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.tcf.te.core.interfaces.IConnectable;
 import org.eclipse.tcf.te.core.utils.ConnectStateHelper;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * Connectable command handler implementation.
  */
 public class ConnectableCommandHandler extends AbstractEditorCommandHandler {
 
 	protected static final String PARAM_ACTION = "action"; //$NON-NLS-1$
 
 	protected int action = IConnectable.STATE_UNKNOWN;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.handler.AbstractEditorCommandHandler#internalExecute(org.eclipse.core.commands.ExecutionEvent)
 	 */
 	@Override
 	public Object internalExecute(ExecutionEvent event) throws ExecutionException {
 		Assert.isTrue(action >= 0);
 
		Object element = null;
		if ("org.eclipse.tcf.te.ui.views.Editor".equals(HandlerUtil.getActiveEditorId(event))) { //$NON-NLS-1$
			element = HandlerUtil.getActiveEditorInput(event);
		} else {
			element = HandlerUtil.getCurrentSelection(event);
			if (element instanceof IStructuredSelection) element = ((IStructuredSelection)element).getFirstElement();
		}
 		IConnectable connectable = null;
 		if (element instanceof IConnectable) {
 			connectable = (IConnectable)element;
 		}
 		else if (element instanceof IAdaptable) {
 			connectable = (IConnectable)((IAdaptable)element).getAdapter(IConnectable.class);
 		}
 		if (connectable == null) {
 			connectable = (IConnectable)Platform.getAdapterManager().getAdapter(element, IConnectable.class);
 		}
 		if (connectable != null) {
 			if (connectable.isConnectStateChangeAllowed(action)) {
 				connectable.changeConnectState(action, null, null);
 			}
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
 		super.setInitializationData(config, propertyName, data);
 		if (data instanceof Map) {
 			Map<?,?> dataMap = (Map<?,?>)data;
 			if (dataMap.get(PARAM_ACTION) instanceof String) {
 				String stateStr = dataMap.get(PARAM_ACTION).toString().trim();
 				this.action = ConnectStateHelper.getConnectAction(stateStr);
 			}
 		}
 	}
 }
