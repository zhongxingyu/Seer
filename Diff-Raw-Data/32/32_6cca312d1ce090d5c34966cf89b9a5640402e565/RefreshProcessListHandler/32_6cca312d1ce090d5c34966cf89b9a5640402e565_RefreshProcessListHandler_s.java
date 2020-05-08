 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.handler;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.IModel;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.processes.core.model.ModelManager;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * The handler to refresh the process list.
  */
 public class RefreshProcessListHandler extends AbstractHandler {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
 	 */
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IEditorInput editorInput = HandlerUtil.getActiveEditorInputChecked(event);
		IPeerModel peer = (IPeerModel) editorInput.getAdapter(IPeerModel.class);
 		if (peer != null) {
			IModel model = ModelManager.getRuntimeModel(peer);
			Assert.isNotNull(model);
			model.getService(IModelRefreshService.class).refresh(null);
 		}
 		return null;
 	}
 }
