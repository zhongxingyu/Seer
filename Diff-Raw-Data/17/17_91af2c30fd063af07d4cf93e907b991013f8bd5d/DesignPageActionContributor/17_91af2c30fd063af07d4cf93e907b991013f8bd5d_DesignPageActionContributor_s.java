 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.editors.actions;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.pagedesigner.IHTMLConstants;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.actions.range.DesignerToolBarAction;
 import org.eclipse.jst.pagedesigner.editors.HTMLEditor;
 import org.eclipse.jst.pagedesigner.editors.SimpleGraphicalEditor;
 import org.eclipse.jst.pagedesigner.viewer.IHTMLGraphicalViewer;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.part.EditorActionBarContributor;
 import org.eclipse.ui.texteditor.ITextEditorActionConstants;
 import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
 
 /**
  * sub action contributor for the designer page.
  * 
  * @author mengbo
  */
 public class DesignPageActionContributor extends EditorActionBarContributor {
 	private static final Logger _log = PDPlugin
 			.getLogger(DesignPageActionContributor.class);
 
 	/**
 	 * paragraph
 	 */
 	public static final String PARAGRAPH_ACTION_ID = "paragraph"; //$NON-NLS-1$
 
 	private IEditorPart _editorPart;
 
 	/**
 	 * @param manager
 	 * @param id
 	 */
 	protected void doRemove(IContributionManager manager, String id) {
 		try {
 			if (manager.find(id) != null) {
 				manager.remove(id);
 			}
 		} catch (Exception e) {
 			_log.info("Error:", e); //$NON-NLS-1$
 		}
 	}
 
 	public void init(IActionBars bars, IWorkbenchPage page) {
 		super.init(bars);
 		init(bars);
 	}
 
 	public void init(IActionBars bars) {
 		IToolBarManager toolbar = bars.getToolBarManager();
 		initToolbar(toolbar);
 	}
 
 	/**
 	 * @param toolbar
 	 */
 	private void initToolbar(IToolBarManager toolbar) {
 		DesignActionBarFactory factory = DesignActionBarFactory.getInstance();
 
 		Action action = factory.getStyleAction(IHTMLConstants.TAG_U);
 		toolbar.add(action);
 
 		action = factory.getStyleAction(IHTMLConstants.TAG_B);
 		toolbar.add(action);
 
 		action = factory.getStyleAction(IHTMLConstants.TAG_I);
 		toolbar.add(action);
 
 		action = factory.getStyleAction(IHTMLConstants.TAG_SMALL);
 		toolbar.add(action);
 
 		action = factory.getStyleAction(IHTMLConstants.TAG_BIG);
 		toolbar.add(action);
 		// action = factory.getStyleAction(PARAGRAPH_ACTION_ID);
 		// toolbar.add(action);
 	}
 
 	/**
 	 * @param menuManager
 	 * @param action
 	 * @param id
 	 */
 	protected void addActionWithId(IMenuManager menuManager, Action action,
 			String id) {
 		action.setId(id);
 		menuManager.add(action);
 	}
 
 	/**
 	 * @param enabled
 	 */
 	public void setViewerSpecificContributionsEnabled(boolean enabled) {
 		HTMLEditor htmlEditor = null;
 		if (_editorPart instanceof HTMLEditor) {
 			htmlEditor = (HTMLEditor) _editorPart;
 		} else if (_editorPart instanceof SimpleGraphicalEditor) {
 			htmlEditor = ((SimpleGraphicalEditor) _editorPart).getHTMLEditor();
 		}
 
 		if (htmlEditor == null)
 			return;
 
 		SimpleGraphicalEditor graphicalEditor = (SimpleGraphicalEditor) htmlEditor
 				.getDesignViewer();
 		IWorkbenchPartSite site = htmlEditor.getSite();
 		if (site instanceof IEditorSite) {
 			IActionBars actionBars = ((IEditorSite) site).getActionBars();
 
 			if (enabled) {
 				// // we always let the text editor to handle UNDO and REDO
 				// actionBars.setGlobalActionHandler(ITextEditorActionConstants.UNDO,
 				// textEditor
 				// .getAction(ITextEditorActionConstants.UNDO));
 				// actionBars.setGlobalActionHandler(ITextEditorActionConstants.REDO,
 				// textEditor
 				// .getAction(ITextEditorActionConstants.REDO));
 				// lium: the above behavior changed, since we now use
 				// DesignerUndoRedoAction.
 				// see comments in DesignerUndoRedoAction
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.UNDO, graphicalEditor
 								.getAction(IWorkbenchActionDefinitionIds.UNDO));
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.REDO, graphicalEditor
 								.getAction(IWorkbenchActionDefinitionIds.REDO));
 
 				// cut/copy/paste is delegated to design actions
 				actionBars
 						.setGlobalActionHandler(
 								ITextEditorActionConstants.DELETE,
 								graphicalEditor
 										.getAction(IWorkbenchActionDefinitionIds.DELETE));
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.CUT, graphicalEditor
 								.getAction(IWorkbenchActionDefinitionIds.CUT));
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.COPY, graphicalEditor
 								.getAction(IWorkbenchActionDefinitionIds.COPY));
 				actionBars
 						.setGlobalActionHandler(
 								ITextEditorActionConstants.PASTE,
 								graphicalEditor
 										.getAction(IWorkbenchActionDefinitionIds.PASTE));
 			} else {
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.UNDO, null);
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.REDO, null);
 
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.DELETE, null);
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.CUT, null);
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.COPY, null);
 				actionBars.setGlobalActionHandler(
 						ITextEditorActionConstants.PASTE, null);
 			}
 		}
 	}
 
 	/**
 	 * The active editor passed in could be the following: HTMLEditor,
 	 * SimpleGraphicalEditor, null.
 	 */
 	public void setActiveEditor(IEditorPart targetEditor) {
 		_editorPart = targetEditor;
 
 		// temp code.
 		if (targetEditor instanceof SimpleGraphicalEditor) {
			IHTMLGraphicalViewer viewer = ((SimpleGraphicalEditor) targetEditor)
					.getGraphicViewer();
			setViewerOnActions(viewer);
 		} else if (targetEditor instanceof HTMLEditor) {
			IHTMLGraphicalViewer viewer = ((HTMLEditor) targetEditor)
					.getDesignViewer().getGraphicViewer();
			setViewerOnActions(viewer);
 		} else {
 			setViewerOnActions(null);
 		}
 
 		// TODO... uncomment this and investigate NPE
 		//
 		// add the cut/copy/paste for text fields
 		// ActionHandlerPlugin.connectPart(editorPart);
 	}
 
 	private void setViewerOnActions(IHTMLGraphicalViewer viewer) {
 		IContributionItem[] items = getActionBars().getToolBarManager()
 				.getItems();
 		if (items != null) {
 			for (int i = 0; i < items.length; i++) {
 				if (items[i] instanceof ActionContributionItem) {
 					IAction action = ((ActionContributionItem) items[i])
 							.getAction();
 					if (action instanceof DesignerToolBarAction) {
 						((DesignerToolBarAction) action).setViewer(viewer);
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorActionBarContributor#dispose()
 	 */
 	public void dispose() {
         // TODO: anything to dispose?
 	}
 }
