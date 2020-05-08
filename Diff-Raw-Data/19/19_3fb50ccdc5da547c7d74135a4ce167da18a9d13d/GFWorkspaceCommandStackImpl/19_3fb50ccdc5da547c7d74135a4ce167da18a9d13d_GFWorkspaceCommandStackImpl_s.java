 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 324859 - Need Undo/Redo support for Non-EMF based domain objects
  *    mwenz - Bug 340627 - Features should be able to indicate cancellation
  *    mwenz - Bug 351053 - Remove the need for WorkspaceCommandStackImpl
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.editor;
 
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.transaction.RollbackException;
 import org.eclipse.emf.workspace.impl.WorkspaceCommandStackImpl;
 import org.eclipse.graphiti.IExecutionInfo;
 import org.eclipse.graphiti.features.ICustomUndoableFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureAndContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.internal.command.DefaultExecutionInfo;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class GFWorkspaceCommandStackImpl extends WorkspaceCommandStackImpl {
 
 	private volatile boolean topLevelCommand = true;
 
 	private Stack<IExecutionInfo> undoStackForExecutionInfo = new Stack<IExecutionInfo>();
 
 	private Stack<IExecutionInfo> redoStackForExecutionInfo = new Stack<IExecutionInfo>();
 
 	public GFWorkspaceCommandStackImpl(IOperationHistory history) {
 		super(history);
 	}
 
 	@Override
	public void execute(Command command, Map<?, ?> options) throws InterruptedException, RollbackException {
 		IExecutionInfo executionInfo = null;
 		/*
 		 * Needed to retrieve an eventually externally created IExecutionInfo
 		 * object (created in GFCommandStack) to the internal execution of a
 		 * feature. Could be created here as well, but then there would
 		 * potentially be different instances of features and contexts.
 		 */
 		if (options != null) {
 			Object object = options.get(GFCommandStack.OPTION_EXECUTION_INFO);
 			if (object instanceof IExecutionInfo) {
 				executionInfo = (IExecutionInfo) object;
 				options.remove(GFCommandStack.OPTION_EXECUTION_INFO);
 			}
 		}
 		if (topLevelCommand) {
 			topLevelCommand = false;
 			try {
 				super.execute(command, options);
 
 				/*
 				 * Add the execution info to the stack; in case it is not
 				 * provided, create it first. Must happen here and not in the
 				 * else branch below because the super.execute call add the EMF
 				 * command to the history and therefore to the command stack
 				 * (thus is the entry relevant for undo)
 				 */
 				if (executionInfo == null) {
 					executionInfo = new DefaultExecutionInfo();
 					GraphitiUiInternal.getCommandService().completeExecutionInfo((DefaultExecutionInfo) executionInfo,
 							GraphitiUiInternal.getCommandService().transformFromEmfToGefCommand(command));
 				}
 				undoStackForExecutionInfo.push(executionInfo);
 			} finally {
 				topLevelCommand = true;
 			}
 		} else {
 			command.execute();
 			if (getMostRecentCommand() != null) {
 				getMostRecentCommand().chain(command);
 			}
 		}
 
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		redoStackForExecutionInfo.clear();
 		redoStackForExecutionInfo = null;
 		undoStackForExecutionInfo.clear();
 		undoStackForExecutionInfo = null;
 	}
 
 	@Override
 	public void flush() {
 		super.flush();
 		undoStackForExecutionInfo.clear();
 		redoStackForExecutionInfo.clear();
 	}
 
 	@Override
 	public void redo() {
 		// Care about EMF redo
 		super.redo();
 
 		// Check if non-EMF redo is needed and care about it
 		if (!redoStackForExecutionInfo.isEmpty()) {
 			IExecutionInfo ei = redoStackForExecutionInfo.pop();
 			undoStackForExecutionInfo.push(ei);
 			IFeatureAndContext[] executionList = ei.getExecutionList();
 			// Traverse operation forwards
 			for (int i = 0; i < executionList.length; i++) {
 				IFeature feature = executionList[i].getFeature();
 				IContext context = executionList[i].getContext();
 				if (feature instanceof ICustomUndoableFeature) {
 					ICustomUndoableFeature undoableFeature = (ICustomUndoableFeature) feature;
 					if (undoableFeature.canRedo(context)) {
 						undoableFeature.redo(context);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void undo() {
 		// Care about EMF undo
 		super.undo();
 
 		// Check if non-EMF undo is needed and care about it
 		if (!undoStackForExecutionInfo.isEmpty()) {
 			IExecutionInfo ei = undoStackForExecutionInfo.pop();
 			redoStackForExecutionInfo.push(ei);
 			IFeatureAndContext[] executionList = ei.getExecutionList();
 			// Traverse operations backwards
 			for (int i = executionList.length - 1; i >= 0; i--) {
 				IFeature feature = executionList[i].getFeature();
 				IContext context = executionList[i].getContext();
 				if (feature instanceof ICustomUndoableFeature) {
 					ICustomUndoableFeature undoableFeature = (ICustomUndoableFeature) feature;
 					if (undoableFeature.canUndo(context)) {
 						undoableFeature.undo(context);
 					}
 				}
 			}
 		}
 	}
 }
