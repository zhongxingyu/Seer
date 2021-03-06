 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Oct 10, 2003
  *
  * To change the template for this generated file go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.wst.common.internal.emfworkbench.operation;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.jem.util.UIContextDetermination;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.wst.common.frameworks.internal.operations.IOperationHandler;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel;
 import org.eclipse.wst.common.internal.emfworkbench.EMFWorkbenchContext;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModel;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.IValidateEditContext;
 
 public abstract class EditModelOperation extends WTPOperation {
 	protected EditModel editModel;
 	protected EMFWorkbenchContext emfWorkbenchContext;
 	private CommandStack commandStack;
 
 	public EditModelOperation(EditModelOperationDataModel dataModel) {
 		super(dataModel);
 	}
 
 	public EditModelOperation() {
 		//Default constructor
 	}
 
	protected final void initialize(IProgressMonitor monitor) {
 		EditModelOperationDataModel dataModel = (EditModelOperationDataModel) operationDataModel;
 		emfWorkbenchContext = (EMFWorkbenchContext) WorkbenchResourceHelperBase.createEMFContext(dataModel.getTargetProject(), null);
 		editModel = emfWorkbenchContext.getEditModelForWrite(dataModel.getStringProperty(EditModelOperationDataModel.EDIT_MODEL_ID), this);
 		doInitialize(monitor);
 	}
 
 	protected void doInitialize(IProgressMonitor monitor) {
 		//init
 	}
 
 	protected final void dispose(IProgressMonitor monitor) {
 		try {
 			doDispose(monitor);
 		} finally {
 			saveEditModel(monitor);
 		}
 	}
 
 	private final void saveEditModel(IProgressMonitor monitor) {
 		if (null != editModel) {
 			if (((EditModelOperationDataModel) operationDataModel).getBooleanProperty(EditModelOperationDataModel.PROMPT_ON_SAVE))
 				editModel.saveIfNecessaryWithPrompt(monitor, (IOperationHandler) operationDataModel.getProperty(WTPOperationDataModel.UI_OPERATION_HANLDER), this);
 			else
 				editModel.saveIfNecessary(monitor, this);
 			editModel.releaseAccess(this);
 			editModel = null;
 		}
 		postSaveEditModel(monitor);
 	}
 
 	/**
 	 * @param monitor
 	 */
 	protected void postSaveEditModel(IProgressMonitor monitor) {
 		// do nothing by default
 	}
 
 	protected void doDispose(IProgressMonitor monitor) {
 		//dispose
 	}
 
 	/**
 	 * @return Returns the commandStack.
 	 */
 	public CommandStack getCommandStack() {
 		if (commandStack == null && editModel != null)
 			commandStack = editModel.getCommandStack();
 		return commandStack;
 	}
 
 	/**
 	 * @param commandStack
 	 *            The commandStack to set.
 	 */
 	public void setCommandStack(CommandStack commandStack) {
 		this.commandStack = commandStack;
 	}
 
 	/**
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperation#validateEdit()
 	 */
 	protected boolean validateEdit() {
 		IValidateEditContext validator = (IValidateEditContext) UIContextDetermination.createInstance(IValidateEditContext.CLASS_KEY);
 		return validator.validateState(editModel).isOK();
 	}
 }
