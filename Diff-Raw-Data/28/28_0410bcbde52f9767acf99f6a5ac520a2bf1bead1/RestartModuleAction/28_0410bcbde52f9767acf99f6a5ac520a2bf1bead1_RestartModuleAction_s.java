 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.internal.view.servers;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.ServerLabelProvider;
 /**
  * Restart a module on a server.
  */
 public class RestartModuleAction extends Action {
 	protected IServer server;
 	protected IModule[] module;
 
 	public RestartModuleAction(IServer server, IModule[] module) {
 		super();
 		this.server = server;
 		this.module = module;
	
 		int size = module.length;
		setText(module[size - 1].getName());
 		
		ServerLabelProvider slp = (ServerLabelProvider) ServerUICore.getLabelProvider();
		setImageDescriptor(slp.getImageDescriptor(module[size - 1]));
	
 		setEnabled(server.canControlModule(module, null).isOK() && server.getModuleState(module) != IServer.STATE_STOPPED);
 	}
 
 	/**
 	 * Implementation of method defined on <code>IAction</code>.
 	 */
 	public void run() {
 		server.restartModule(module, null);
 	}
 }
