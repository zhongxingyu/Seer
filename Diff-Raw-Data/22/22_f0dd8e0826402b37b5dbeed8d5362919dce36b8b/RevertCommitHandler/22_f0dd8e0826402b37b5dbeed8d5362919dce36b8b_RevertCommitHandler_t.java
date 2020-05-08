 /*******************************************************************************
  * Copyright (c) 2012 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
 * Otto von Wesendonk
 * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.handlers;
 
 import org.eclipse.emf.emfstore.internal.client.ui.controller.UIRevertCommitController;
 import org.eclipse.emf.emfstore.internal.client.ui.views.historybrowserview.HistoryBrowserView;
import org.eclipse.emf.emfstore.internal.server.model.versioning.HistoryInfo;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Handler for forcing the revert of a commit.
  * 
  * @author ovonwesen
  * @author emueller
  * 
  */
 public class RevertCommitHandler extends AbstractEMFStoreHandler {
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.handlers.AbstractEMFStoreHandler#handle()
 	 */
 	@Override
 	public void handle() {
 
 		// TODO: remove HistoryBrowserView, switch to API class
 		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
 
 		if (activePage == null || !(activePage.getActivePart() instanceof HistoryBrowserView)) {
 			return;
 		}
 
 		HistoryBrowserView view = (HistoryBrowserView) activePage.getActivePart();
 
 		new UIRevertCommitController(
 			getShell(),
			requireSelection(HistoryInfo.class).toAPI().getPrimarySpec(),
			view.getProjectSpace().toAPI()).execute();
 	}
 
 }
