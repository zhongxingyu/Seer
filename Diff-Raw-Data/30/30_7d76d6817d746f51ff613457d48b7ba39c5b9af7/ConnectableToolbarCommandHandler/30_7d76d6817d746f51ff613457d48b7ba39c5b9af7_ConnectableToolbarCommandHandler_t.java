 /*******************************************************************************
  * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.tcf.ui.handler;
 
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.core.interfaces.IConnectable;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.IDefaultContextService;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.handler.ConnectableCommandHandler;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.IElementUpdater;
 import org.eclipse.ui.menus.UIElement;
 
 /**
  * ConnectableToolbarCommandHandler
  */
 public class ConnectableToolbarCommandHandler extends ConnectableCommandHandler implements IElementUpdater {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.handler.AbstractCommandHandler#getSelection(org.eclipse.core.commands.ExecutionEvent)
 	 */
 	@Override
 	protected IStructuredSelection getSelection(ExecutionEvent event) {
 		IPeerNode defaultContext = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 	    return defaultContext != null ? new StructuredSelection(defaultContext) : new StructuredSelection();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
 	 */
     @Override
     public void updateElement(final UIElement element, Map parameters) {
 		final IPeerNode defaultContext = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
    	PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
		    	if (defaultContext != null && getAction() == IConnectable.ACTION_CONNECT) {
			    	element.setTooltip(NLS.bind(Messages.ConnectableToolbarCommandHandler_tooltip_connect, defaultContext.getName()));
		    	}
		    	else if (defaultContext != null && getAction() == IConnectable.ACTION_DISCONNECT) {
			    	element.setTooltip(NLS.bind(Messages.ConnectableToolbarCommandHandler_tooltip_disconnect, defaultContext.getName()));
		    	}
		    	else {
		    		element.setTooltip(null);
		    	}
			}
		});
     }
 }
