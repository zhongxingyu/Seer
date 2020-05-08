 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.action.adf;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.jboss.tools.common.model.ui.action.*;
 
 public class AddADFSupportAction extends AbstractModelActionDelegate implements IObjectActionDelegate {
 	AddADFSupportHelper helper = new AddADFSupportHelper();
 	
 	public AddADFSupportAction() {}
 
 	protected void safeSelectionChanged(IAction action, ISelection selection) {
 		if(object == null && action.isEnabled()) action.setEnabled(false);
 		object = getAdapter(selection);
 		helper.setObject(object);
 		action.setEnabled(computeEnabled());
 	}
 
 	protected boolean computeEnabled() {
 		return helper.isEnabled();
 	}
 
	protected void doRun() throws Exception {
 		helper.execute();
 	}
 
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
 	
 
 }
