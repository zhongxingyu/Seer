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
 package org.eclipse.wst.server.core.util;
 
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.IServerListener;
 /**
  * Helper class which implements the IServerListener interface
  * with empty methods.
  * 
  * @see org.eclipse.wst.server.core.IServerListener
  * @since 1.0
  */
public abstract class ServerAdapter implements IServerListener {
 	public void configurationSyncStateChange(IServer server) {
 		// do nothing
 	}
 
 	public void restartStateChange(IServer server) {
 		// do nothing
 	}
 
 	public void serverStateChange(IServer server) {
 		// do nothing
 	}
 
 	public void modulesChanged(IServer server) {
 		// do nothing
 	}
 	
 	public void moduleStateChange(IServer server, IModule[] parents, IModule module) {
 		// do nothing
 	}
 }
