 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.policy;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.editpolicies.DirectEditPolicy;
 import org.eclipse.gef.requests.DirectEditRequest;
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.func.IDirectEditing;
 import org.eclipse.graphiti.internal.command.DirectEditingFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.ICommand;
 import org.eclipse.graphiti.internal.util.LookManager;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.editor.DiagramEditorInternal;
 import org.eclipse.graphiti.ui.internal.parts.directedit.IDirectEditHolder;
 import org.eclipse.graphiti.ui.internal.requests.GFDirectEditRequest;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.internal.util.DataTypeTransformation;
 import org.eclipse.graphiti.util.IColorConstant;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * An EditPolicy to handle direct-editing of EditParts. Typically not the
  * complete EditPart shall be edited directly, but only one control of it (e.g.
  * one Label). For this control an appropiate CellEditor would usually be shown.
  * 
  * @see org.eclipse.graphiti.ui.internal.policy.IEditPolicyFactory#createDirectEditPolicy()
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class DefaultDirectEditPolicy extends DirectEditPolicy {
 
 	private final IConfigurationProvider configurationProvider;
 
 	public DefaultDirectEditPolicy(IConfigurationProvider configurationProvider) {
 		this.configurationProvider = configurationProvider;
 	}
 
 	/**
 	 * Is called, when the (already opened) cell-editor was closed (but not
 	 * canceled).
 	 * 
 	 * @see org.eclipse.gef.editpolicies.DirectEditPolicy#getDirectEditCommand(org.eclipse.gef.requests.DirectEditRequest)
 	 */
 	@Override
 	protected Command getDirectEditCommand(DirectEditRequest request) {
 
 		if (!(request instanceof GFDirectEditRequest)) {
 			return null;
 		}
 
 		final String message = request.getCellEditor().getErrorMessage();
 		if (message != null && message.length() != 0) {
 			MessageDialog.openError(GraphitiUiInternal.getWorkbenchService().getShell(), Messages.DefaultDirectEditPolicy_0_xmsg, message);
 			return null;
 		}
 
 		final IDirectEditHolder directEditHolder = ((GFDirectEditRequest) request).getDirectEditHolder();
 		final IDirectEditingFeature directEditingFeature = directEditHolder.getDirectEditingFeature();
 		final IDirectEditingContext directEditingContext = directEditHolder.getDirectEditingContext();
 
 		Object valueObject = request.getCellEditor().getValue();
 
 		if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_DROPDOWN
 				|| directEditingFeature.getEditingType() == IDirectEditing.TYPE_DROPDOWN_READ_ONLY) {
 			final int index = ((Integer) valueObject).intValue();
 
 			// TODO: user inserted free value, what to do here?
 			if (index < 0) {
 				final Control control = request.getCellEditor().getControl();
 				if (control instanceof CCombo) {
 					final CCombo cc = (CCombo) control;
 					valueObject = cc.getText();
 				} else {
 					return null;
 				}
 			} else {
 				valueObject = directEditingFeature.getPossibleValues(directEditingContext)[index];
 			}
 
 		}
 
 		final ICommand cmd = new DirectEditingFeatureCommandWithContext(directEditingFeature, directEditingContext, (String) valueObject);
 
 		final IFeatureProvider fp = directEditingFeature.getFeatureProvider();
 		final DiagramEditorInternal diagramEditor = (DiagramEditorInternal) fp.getDiagramTypeProvider().getDiagramEditor();
 		final CommandStack commandStack = diagramEditor.getEditDomain().getCommandStack();
 		commandStack.execute(new GefCommandWrapper(cmd, diagramEditor.getEditingDomain()));
 		// CommandExec.getSingleton().executeCommand(cmd, fp.getConnection());
 
 		return null;
 	}
 
 	/**
 	 * Is called on every change in the cell-editor. For example on every
 	 * key-press in a TextCellEditor. It then updates the Label, which the
 	 * cell-editor edits. Note that this means, that the Label shows the new
 	 * value, before it is committed to the underlying model. However, this can
 	 * be useful, e.g. to adjust the layout of the editpart while changing the
 	 * value in the cell-editor.
 	 * 
 	 * @see org.eclipse.gef.editpolicies.DirectEditPolicy#showCurrentEditValue(org.eclipse.gef.requests.DirectEditRequest)
 	 */
 	@Override
 	protected void showCurrentEditValue(DirectEditRequest request) {
 
 		final Object currentValue = request.getCellEditor().getValue();
 		if (currentValue != null) {
 		}
 		// ((StickyNoteFigure)getHostFigure()).setText(value);
 		// hack to prevent async layout from placing the cell editor twice.
 		getHostFigure().getUpdateManager().performUpdate();
 	}
 
 	@Override
 	protected void showDirectEditFeedback(DirectEditRequest request) {
 		super.showDirectEditFeedback(request);
 		final String errorMessage = request.getCellEditor().getErrorMessage();
 
 		// mark cell editor background
 		final Control control = request.getCellEditor().getControl();
 		if (errorMessage == null) {
 			control.setBackground(ColorConstants.listBackground);
 		} else {
			final IColorConstant cc = LookManager.getLook().getFieldErrorBackgroundColor();
			final int r = cc.getRed();
			final int g = cc.getGreen();
			final int b = cc.getBlue();
			final Color errorColor = DataTypeTransformation.toSwtColor(this.configurationProvider, r, g, b);
			control.setBackground(errorColor);
 		}
 
 		// set status line message
 		GraphitiUiInternal.getWorkbenchService().getActiveStatusLineManager().setErrorMessage(errorMessage);
 	}
 }
