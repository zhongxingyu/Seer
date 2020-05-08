 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.navigator.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jst.j2ee.internal.actions.OpenJ2EEResourceAction;
 import org.eclipse.jst.j2ee.internal.provider.J2EEUtilityJarItemProvider;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.actions.ActionContext;
 import org.eclipse.ui.navigator.CommonActionProvider;
 import org.eclipse.ui.navigator.ICommonActionConstants;
 import org.eclipse.ui.navigator.ICommonActionExtensionSite;
 import org.eclipse.ui.navigator.ICommonMenuConstants;
 
 public class J2EEActionProvider extends CommonActionProvider  {
 
 	private OpenJ2EEResourceAction openAction; 
 	
 	/**
 	 * 
 	 */
 	public J2EEActionProvider() {
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.actions.ICommonActionProvider#init(org.eclipse.ui.IViewPart,
 	 *      org.eclipse.jface.viewers.StructuredViewer,
 	 *      org.eclipse.wst.common.navigator.internal.views.extensions.NavigatorContentService)
 	 */
 	public void init(ICommonActionExtensionSite aConfig) { 
 		openAction = new OpenJ2EEResourceAction(); 
 		
 //		renameAction = new J2EERenameAction(aConfig.getViewSite(), aConfig.getViewSite().getShell());
 
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.actions.ICommonActionProvider#setActionContext(org.eclipse.ui.actions.ActionContext)
 	 */
 	public void setContext(ActionContext aContext) {
 		
 		if (aContext != null && aContext.getSelection() instanceof IStructuredSelection) {
 			IStructuredSelection selection = (IStructuredSelection) aContext.getSelection();
 
 			openAction.selectionChanged(selection);
 //			renameAction.selectionChanged(selection);
 
 			Object[] array = selection.toArray();
 			if (isUtilityJars(array)) {
 				J2EEUtilityJarItemProvider utilityJarItemProvider = null;
 				List newSelection = new ArrayList();
 				for (int i = 0; i < array.length; i++) {
 					utilityJarItemProvider = (J2EEUtilityJarItemProvider) array[i];
 					newSelection.addAll(utilityJarItemProvider.getChildren(null));
 				}
 				selection = new StructuredSelection(newSelection);
 			}
 			//genericDelete.selectionChanged(genericDelete, selection);
 		}
 		super.setContext(aContext);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.actions.ICommonActionProvider#fillActionBars(org.eclipse.ui.IActionBars)
 	 */
 	public void fillActionBars(IActionBars theActionBars) {
 //		if (genericDelete.isEnabled())
 //			theActionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), genericDelete);
 		if(openAction.isEnabled()) 
 			theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
 		return;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.actions.ICommonActionProvider#fillContextMenu(org.eclipse.jface.action.IMenuManager)
 	 */
 	public void fillContextMenu(IMenuManager aMenu) {
 		if (getContext()==null || getContext().getSelection().isEmpty())
 			return;
 		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		openAction.selectionChanged(selection);
		if (openAction.isEnabled())
			aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openAction);
 	}
 
 	private boolean isUtilityJars(Object[] items) {
 		if (items != null) {
 			for (int i = 0; i < items.length; i++) {
 				if (items[i] == null || items[i].getClass() != J2EEUtilityJarItemProvider.class)
 					return false;
 			}
 			return true;
 		}
 		return false;
 	} 
 	 
 }
