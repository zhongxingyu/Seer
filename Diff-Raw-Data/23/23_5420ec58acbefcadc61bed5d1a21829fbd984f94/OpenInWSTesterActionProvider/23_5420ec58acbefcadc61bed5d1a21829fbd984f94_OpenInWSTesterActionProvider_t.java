 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Brian Fitzpatrick - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.ui.cnf.action;
 
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.navigator.CommonActionProvider;
 import org.eclipse.ui.navigator.ICommonActionExtensionSite;
 import org.eclipse.ui.navigator.ICommonMenuConstants;
 import org.eclipse.ui.navigator.ICommonViewerSite;
 import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.jboss.tools.ws.jaxrs.ui.internal.launcher.WSTesterClientDelegate;
import org.jboss.tools.ws.jaxrs.ui.internal.launcher.WSTesterLaunchableAdapterDelegate;
 
 /**
  * @author bfitzpat
 * @deprecated replaced by {@link WSTesterLaunchableAdapterDelegate}
  */
@Deprecated
 public class OpenInWSTesterActionProvider extends CommonActionProvider {
 
 	private OpenInWSTesterAction openInWSTesterAction = null;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator
 	 * .ICommonActionExtensionSite)
 	 */
 	@Override
 	public void init(ICommonActionExtensionSite aSite) {
 		ICommonViewerSite viewSite = aSite.getViewSite();
 		if (viewSite instanceof ICommonViewerWorkbenchSite) {
 			openInWSTesterAction = new OpenInWSTesterAction();
 			openInWSTesterAction.setSelection(aSite.getStructuredViewer().getSelection());
 			aSite.getStructuredViewer().addSelectionChangedListener(openInWSTesterAction);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars
 	 * )
 	 */
 	@Override
 	public void fillActionBars(IActionBars actionBars) {
 		super.fillActionBars(actionBars);
 		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), openInWSTesterAction);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.
 	 * action.IMenuManager)
 	 */
 	@Override
 	public void fillContextMenu(IMenuManager menu) {
 		if (openInWSTesterAction != null && openInWSTesterAction.isEnabled()) {
 			menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, openInWSTesterAction);
 		}
 	}
 
 }
