 /*******************************************************************************
  * Copyright (c) 2012 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.handlers;
 
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.ui.controller.UIDeleteRemoteProjectController;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 
 /**
  * Handler for deleting a remote project.<br/>
  * It is assumed that the user previously has selected a {@link ProjectInfo} instance.
  * 
  * @author ovonwesen
  * @author emueller
  * 
  */
 public class DeleteProjectOnServerHandler extends AbstractEMFStoreHandler {
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.handlers.AbstractEMFStoreHandler#handle()
 	 */
 	@Override
 	public void handle() {
 		ProjectInfo projectInfoSelection = requireSelection(ProjectInfo.class);
 		ServerInfo serverInfo = (ServerInfo) projectInfoSelection.eContainer();
		new UIDeleteRemoteProjectController(getShell(), serverInfo, projectInfoSelection.getProjectId(), false)
			.execute();
 	}
 }
