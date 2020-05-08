 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    Bug 336488 - DiagramEditor API
  *    mgorning - Bug 347262 - DirectEditingFeature with TYPE_DIALOG type
  *    mgorning - Bug 377419 - Hide text in underlying GA while DirectEditing is enabled
  *    pjpaulin - Bug 352120 - Now uses IDiagramContainerUI interface
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.parts.directedit;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.gef.editparts.ZoomListener;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.requests.DirectEditRequest;
 import org.eclipse.gef.tools.DirectEditManager;
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.func.IDirectEditing;
 import org.eclipse.graphiti.func.IProposal;
 import org.eclipse.graphiti.func.Proposal;
 import org.eclipse.graphiti.ui.editor.DiagramBehavior;
 import org.eclipse.graphiti.ui.internal.figures.GFMultilineText;
 import org.eclipse.graphiti.ui.internal.figures.GFText;
 import org.eclipse.graphiti.ui.internal.parts.ShapeEditPart;
 import org.eclipse.graphiti.ui.internal.requests.GFDirectEditRequest;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.platform.ICellEditorProvider;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.bindings.keys.KeyStroke;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.part.CellEditorActionHandler;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class GFDirectEditManager extends DirectEditManager implements IDirectEditHolder {
 
 	private IDirectEditingContext directEditingContext;
 
 	private IDirectEditingFeature directEditingFeature;
 
 	private DiagramBehavior diagramBehavior;
 
 	private Font cellEditorFont = null;
 
 	private IActionBars actionBars;
 
 	private CellEditorActionHandler actionHandler;
 
 	private IAction copy, cut, paste, undo, redo, find, selectAll, delete;
 
 	private double cachedZoom = -1.0;
 
 	private ZoomListener zoomListener = new ZoomListener() {
 		public void zoomChanged(double newZoom) {
 			updateScaledFont();
 		}
 	};
 
 	public GFDirectEditManager(ShapeEditPart part, TextCellLocator cellEditorLocator) {
 		super(part, null, cellEditorLocator);
 		diagramBehavior = part.getConfigurationProvider().getDiagramBehavior();
 	}
 
 	public IDirectEditingContext getDirectEditingContext() {
 		return directEditingContext;
 	}
 
 	public IDirectEditingFeature getDirectEditingFeature() {
 		return directEditingFeature;
 	}
 
 	public void setDirectEditingContext(IDirectEditingContext directEditingContext) {
 		this.directEditingContext = directEditingContext;
 	}
 
 	public void setDirectEditingFeature(IDirectEditingFeature directEditingFeature) {
 		this.directEditingFeature = directEditingFeature;
 	}
 
 	public boolean isSimpleMode() {
 		boolean ret = true;
 		if (getDirectEditingFeature() != null) {
 			if (getDirectEditingFeature().getProposalSupport() != null) {
 				ret = false;
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	protected CellEditor createCellEditorOn(Composite composite) {
 		CellEditor ret = null;
 
 		IFigure locatorFigure = getTextLocatorFigure();
 
 		int editingType = directEditingFeature.getEditingType();
 
 		if (editingType == IDirectEditing.TYPE_MULTILINETEXT) {
 			int horizontalAlignment = SWT.LEFT;
 			if (locatorFigure instanceof GFMultilineText) {
 				int textAlignment = ((GFMultilineText) locatorFigure).getHorizontalAligment();
 				if (textAlignment == PositionConstants.CENTER) {
 					horizontalAlignment = SWT.CENTER;
 				} else if (textAlignment == PositionConstants.RIGHT) {
 					horizontalAlignment = SWT.RIGHT;
 				}
 			}
 			ret = new TextCellEditor(composite, SWT.MULTI | SWT.WRAP | horizontalAlignment);
 		} else if (editingType == IDirectEditing.TYPE_TEXT) {
 			int horizontalAlignment = SWT.LEFT;
 			if (locatorFigure instanceof GFText) {
 				int textAlignment = ((GFText) locatorFigure).getLabelAlignment();
 				if (textAlignment == PositionConstants.CENTER) {
 					horizontalAlignment = SWT.CENTER;
 				} else if (textAlignment == PositionConstants.RIGHT) {
 					horizontalAlignment = SWT.RIGHT;
 				}
 			}
 			ret = new TextCellEditor(composite, horizontalAlignment);
 		} else if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_DROPDOWN
 				|| directEditingFeature.getEditingType() == IDirectEditing.TYPE_DROPDOWN_READ_ONLY) {
 			String[] possibleValues = getPossibleValues();
 			ret = new ComboBoxCellEditor(composite, possibleValues);
 		} else if (editingType == IDirectEditing.TYPE_CUSTOM) {
 			if (directEditingFeature instanceof ICellEditorProvider) {
 				ret = ((ICellEditorProvider) directEditingFeature).createCellEditor(composite);
 			} else {
 				throw new UnsupportedOperationException("if custom type the feature must implement ICellEditorProvider"); //$NON-NLS-1$
 			}
 		}
 
 		if (ret != null && editingType != IDirectEditing.TYPE_CUSTOM) {
 			ret.setValidator(new GFCellEditorValidator(this, ret));
 		}
 
 		return ret;
 	}
 
 	private String[] getPossibleValues() {
 		String[] possibleValues;
 		if (isSimpleMode()) {
 			possibleValues = directEditingFeature.getPossibleValues(directEditingContext);
 		} else {
 			IProposal[] proposals = directEditingFeature.getProposalSupport().getPossibleValues(directEditingContext);
 			possibleValues = Proposal.proposalsToTexts(proposals);
 		}
 		return possibleValues;
 	}
 
 	private void disposeCellEditorFont() {
 		if (cellEditorFont != null) {
 			cellEditorFont.dispose();
 			cellEditorFont = null;
 		}
 	}
 
 	private void updateScaledFont() {
 		if (getCellEditor().getControl() instanceof Text) {
 			double zoom = 1.0;
 			ZoomManager zoomMgr = (ZoomManager) getEditPart().getViewer().getProperty(ZoomManager.class.toString());
 			if (zoomMgr != null) {
 				zoom = zoomMgr.getZoom();
 			}
 
 			if (cachedZoom == zoom)
 				return;
 			cachedZoom = zoom;
 
 			disposeCellEditorFont();
 			Font lf = getTextLocatorFigure().getFont();
 			FontData fd = lf.getFontData()[0];
 			fd.setHeight((int) (fd.getHeight() * zoom));
 			cellEditorFont = new Font(lf.getDevice(), fd);
 
 			Text text = (Text) getCellEditor().getControl();
 			text.setForeground(getTextLocatorFigure().getForegroundColor());
 			text.setFont(cellEditorFont);
 		}
 	}
 
 	@Override
 	protected DirectEditRequest createDirectEditRequest() {
 		GFDirectEditRequest req = new GFDirectEditRequest();
 		req.setCellEditor(getCellEditor());
 		req.setDirectEditingContext(this);
 		return req;
 	}
 
 	@Override
 	protected void initCellEditor() {
 		String initialValue = directEditingFeature.getInitialValue(directEditingContext);
 
 		if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_MULTILINETEXT) {
 			if (initialValue != null) {
 				getCellEditor().setValue(initialValue);
 			}
 		} else if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_DROPDOWN
 				|| directEditingFeature.getEditingType() == IDirectEditing.TYPE_DROPDOWN_READ_ONLY) {
 			ComboBoxCellEditor comboBoxCellEditor = (ComboBoxCellEditor) getCellEditor();
 
 			setDirty(true);
 
 			if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_DROPDOWN_READ_ONLY) {
 				CCombo cc = (CCombo) comboBoxCellEditor.getControl();
 				cc.setEditable(false);
 			}
 
 			String[] possibleValues = getPossibleValues();
 			comboBoxCellEditor.setValue(0);
 			for (int x = 0; x < possibleValues.length; x++) {
 				if (possibleValues[x].equals(initialValue))
 					comboBoxCellEditor.setValue(x);
 			}
 		} else if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_TEXT) {
 
 			if (initialValue != null) {
 				getCellEditor().setValue(initialValue);
 			}
 
 			if (directEditingFeature.isCompletionAvailable()) {
 
 				String value = (String) getCellEditor().getValue();
 				if (value == null)
 					value = ""; //$NON-NLS-1$
 
 				TextContentAdapter controlContentAdapter = new TextContentAdapter();
 				IContentProposalProvider contentProposalProvider = new ContentProposalProvider(this);
 
 				// if auto code completion disabled then open code completion on
 				// CTRL+Space
 				KeyStroke keyStroke = directEditingFeature.isAutoCompletionEnabled() ? null : KeyStroke.getInstance(
 						SWT.CTRL, 32);
 
 				ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(
 						getCellEditor().getControl(), controlContentAdapter, contentProposalProvider, keyStroke, null);
 				contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
 				contentProposalAdapter.addContentProposalListener(new ContentProposalListener(
 						(TextCellEditor) getCellEditor()));
 			}
 		} else if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_CUSTOM) {
 			setDirty(true);
 			getCellEditor().setValue(initialValue);
 		}
 
 		// this will force the font to be set
 		cachedZoom = -1.0;
 		updateScaledFont();
 		ZoomManager zoomMgr = (ZoomManager) getEditPart().getViewer().getProperty(ZoomManager.class.toString());
 		if (zoomMgr != null) {
 			zoomMgr.addZoomListener(zoomListener);
 		}
 
 		getLocator().relocate(getCellEditor());
 
 		// Hook the cell editor's copy/paste actions to the actionBars so that
 		// they can
 		// be invoked via keyboard shortcuts.
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (activeEditor != null) {
			actionBars = activeEditor.getEditorSite().getActionBars();
			saveCurrentActions(actionBars);
			actionHandler = new CellEditorActionHandler(actionBars);
			actionHandler.addCellEditor(getCellEditor());
			actionBars.updateActionBars();
		}
 	}
 
 	@Override
 	protected void bringDown() {
 
 		GFMultilineText mt = getMultilineText();
 		if (mt != null) {
 			mt.setSuppressText(false);
 		}
 
 		diagramBehavior.setDirectEditingActive(false);
 
 		ZoomManager zoomMgr = (ZoomManager) getEditPart().getViewer().getProperty(ZoomManager.class.toString());
 		if (zoomMgr != null) {
 			zoomMgr.removeZoomListener(zoomListener);
 		}
 
 		if (actionHandler != null) {
 			actionHandler.dispose();
 			actionHandler = null;
 		}
 		if (actionBars != null) {
 			restoreSavedActions(actionBars);
 			actionBars.updateActionBars();
 			actionBars = null;
 		}
 
 		if (diagramBehavior.isAlive()) {
 			super.bringDown();
 			disposeCellEditorFont();
 			GraphitiUiInternal.getWorkbenchService().getActiveStatusLineManager().setErrorMessage(null);
 		}
 
 		((ShapeEditPart) getEditPart()).delayDirectEditing();
 	}
 
 	@Override
 	public void show() {
 
 		GFMultilineText mt = getMultilineText();
 		if (mt != null) {
 			mt.setSuppressText(true);
 		}
 
 		diagramBehavior.setDirectEditingActive(true);
 		// this is a bugfix
 		// celleditor not shown initially when figure has insets
 		// or mouse is not directly over control
 		super.show();
 		getLocator().relocate(getCellEditor());
 	}
 
 	private void restoreSavedActions(IActionBars actionBars) {
 		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copy);
 		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), paste);
 		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), delete);
 		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAll);
 		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), cut);
 		actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), find);
 		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undo);
 		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redo);
 	}
 
 	private void saveCurrentActions(IActionBars actionBars) {
 		copy = actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
 		paste = actionBars.getGlobalActionHandler(ActionFactory.PASTE.getId());
 		delete = actionBars.getGlobalActionHandler(ActionFactory.DELETE.getId());
 		selectAll = actionBars.getGlobalActionHandler(ActionFactory.SELECT_ALL.getId());
 		cut = actionBars.getGlobalActionHandler(ActionFactory.CUT.getId());
 		find = actionBars.getGlobalActionHandler(ActionFactory.FIND.getId());
 		undo = actionBars.getGlobalActionHandler(ActionFactory.UNDO.getId());
 		redo = actionBars.getGlobalActionHandler(ActionFactory.REDO.getId());
 	}
 
 	private GFMultilineText getMultilineText() {
 		GFMultilineText mt = null;
 		if (getTextLocatorFigure() instanceof GFMultilineText) {
 			mt = (GFMultilineText) getTextLocatorFigure();
 		}
 		return mt;
 	}
 
 	private IFigure getTextLocatorFigure() {
 		return ((TextCellLocator) getLocator()).getFigure();
 	}
 }
