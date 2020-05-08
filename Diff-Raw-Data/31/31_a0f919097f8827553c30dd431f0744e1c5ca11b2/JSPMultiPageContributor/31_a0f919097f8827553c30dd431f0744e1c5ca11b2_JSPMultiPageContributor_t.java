 /*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.jspeditor;
 
 import java.util.ResourceBundle;
 
 import org.jboss.tools.common.model.ui.texteditors.AbstractMultiPageContributor;
 import org.eclipse.jem.internal.java.adapters.nls.ResourceHandler;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.ide.IDEActionFactory;
 import org.eclipse.ui.texteditor.IStatusField;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.texteditor.ITextEditorActionConstants;
 import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
 import org.eclipse.ui.texteditor.ITextEditorExtension;
 import org.eclipse.ui.texteditor.RetargetTextEditorAction;
 import org.eclipse.wst.sse.ui.internal.actions.StructuredTextEditorActionConstants;
import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
 
import org.jboss.tools.common.text.xml.xpl.GoToMatchingTagAction;
 import org.jboss.tools.common.text.xml.xpl.ToggleOccurencesMarkUpAction;
 
 /**
  * @author Jeremy
  * 
  */
 public class JSPMultiPageContributor extends AbstractMultiPageContributor {
	private static final String GO_TO_MATCHING_TAG_ID = "org.eclipse.wst.xml.ui.gotoMatchingTag"; //$NON-NLS-1$
 
 	public JSPMultiPageContributor() {
 		fToggleOccurencesMarkUp = new ToggleOccurencesMarkUpAction();

		ResourceBundle resourceBundle = XMLUIMessages.getResourceBundle();
		fGoToMatchingTagAction = new GoToMatchingTagAction(resourceBundle, "gotoMatchingTag_", null); //$NON-NLS-1$
		fGoToMatchingTagAction.setActionDefinitionId(GO_TO_MATCHING_TAG_ID);
		fGoToMatchingTagAction.setId(GO_TO_MATCHING_TAG_ID);
 	}
 
 	public void init(IActionBars bars) {
 		super.init(bars);
 		initEditMenu(bars);
 		ResourceBundle resourceBundle = ResourceHandler.getResourceBundle();
 
 		//TODO-3.3: keep checking if 'quick fix' action appears in WTP
 //		fQuickFix = new RetargetTextEditorAction(resourceBundle,
 //				StructuredTextEditorActionConstants.ACTION_NAME_QUICK_FIX
 //						+ StructuredTextEditorActionConstants.DOT);
 //		fQuickFix.setActionDefinitionId(ActionDefinitionIds.QUICK_FIX);
 	}
 
 	protected void createAssistObjects() {
 		ResourceBundle resourceBundle = ResourceHandler.getResourceBundle();
 
 		if(fContentAssistProposal == null) {
 			fContentAssistProposal = new RetargetTextEditorAction(
 				resourceBundle,
 				StructuredTextEditorActionConstants.ACTION_NAME_CONTENTASSIST_PROPOSALS
 						+ StructuredTextEditorActionConstants.UNDERSCORE);
 			fContentAssistProposal
 				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
 		}
 
 		if(fContentAssistTip == null) {
 			fContentAssistTip = new RetargetTextEditorAction(resourceBundle,
 					ITextEditorActionConstants.SHOW_INFORMATION);
 			fContentAssistTip
 				.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
 		}
 	}
 
 	public void setActiveEditor(IEditorPart part) {
 		IEditorPart activeNestedEditor = null;
 		if (part instanceof JSPMultiPageEditor) {
 			activeNestedEditor = ((JSPMultiPageEditor) part).getActiveEditor();
 		}
 		setActivePage(activeNestedEditor);
 
 		mainPart = part;
 
 		IActionBars actionBars = getActionBars();
 
 		if (actionBars != null) {
 			IStatusLineManager slm = actionBars.getStatusLineManager();
 			if (slm != null) {
 				slm.setErrorMessage(null);
 				slm.setMessage(null);
 			}
 			ITextEditor textEditor= null;
 			if (part instanceof ITextEditor)
 				textEditor= (ITextEditor)part;
 			/** The global actions to be connected with editor actions */
 			IAction action= getAction(textEditor, ITextEditorActionConstants.NEXT);
 			actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, action);
 			action= getAction(textEditor, ITextEditorActionConstants.PREVIOUS);
 			actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, action);
 		}
 	}
 
 	public void setActivePage(IEditorPart part) {
 		if (fActiveEditorPart == part)
 			return;
 		cleanStatusLine();
 		fActiveEditorPart = part;
 		IActionBars actionBars = getActionBars();
 		if (actionBars != null) {
 
 			ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part
 					: null;
 			if (editor != null) {
 
 				actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
 						getAction(editor, ITextEditorActionConstants.DELETE));
 				actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
 						getAction(editor, ITextEditorActionConstants.UNDO));
 				actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
 						getAction(editor, ITextEditorActionConstants.REDO));
 				actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
 						getAction(editor, ITextEditorActionConstants.CUT));
 				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
 						getAction(editor, ITextEditorActionConstants.COPY));
 				actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
 						getAction(editor, ITextEditorActionConstants.PASTE));
 				actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL
 						.getId(), getAction(editor,
 						ITextEditorActionConstants.SELECT_ALL));
 				actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
 						getAction(editor, ITextEditorActionConstants.FIND));
 				actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK
 						.getId(), getAction(editor, IDEActionFactory.BOOKMARK
 						.getId()));
 				actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK
 						.getId(), getAction(editor, IDEActionFactory.ADD_TASK
 						.getId()));
 				actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(),
 						getAction(editor, ITextEditorActionConstants.PRINT));
 				actionBars.setGlobalActionHandler(ActionFactory.REVERT.getId(),
 						getAction(editor, ITextEditorActionConstants.REVERT));
 				actionBars.setGlobalActionHandler(ActionFactory.SAVE.getId(),
 						getAction(editor, ITextEditorActionConstants.SAVE));
 
 				//TODO-3.3: keep checking if 'quick fix' action appears in WTP
                                 //				fQuickFix
 				//		.setAction(getAction(
 				//				editor,
 				//				StructuredTextEditorActionConstants.ACTION_NAME_QUICK_FIX));
 
 
 			}
 
 			// re-register action on key binding service
 			IEditorPart localPart = (part != null) ? part : mainPart;
 			if (localPart != null) {
 				IHandlerService handler = (IHandlerService) localPart
 						.getEditorSite().getService(IHandlerService.class);
 				if (handler == null && mainPart != null) {
 					handler = (IHandlerService) mainPart.getEditorSite()
 							.getService(IHandlerService.class);
 				}
 				if (editor != null && handler != null) {
 					// editor
 //					registerKeyBindings(handler, ACTIONS_2, editor);
 					String[] ACTIONS_3 = {
 //							StructuredTextEditorActionConstants.ACTION_NAME_INFORMATION,
 //							StructuredTextEditorActionConstants.ACTION_NAME_CONTENTASSIST_PROPOSALS,
 // TODO-3.3							StructuredTextEditorActionConstants.ACTION_NAME_QUICK_FIX 
 					};
 //					registerKeyBindings(handler, ACTIONS_3, editor);
 				}
 			}
 			cleanActionBarStatus();
 			actionBars.updateActionBars();
 		}
		
		ITextEditor textEditor = getTextEditor(part);
		
		fToggleOccurencesMarkUp.setEditor(textEditor);
 		fToggleOccurencesMarkUp.update();
 
		fGoToMatchingTagAction.setEditor(textEditor);
		if (textEditor != null) {
			textEditor.setAction(GO_TO_MATCHING_TAG_ID, fGoToMatchingTagAction);
		}

 		updateStatus();
 	}
 
 	protected void updateStatus() {
 		if (fActiveEditorPart instanceof ITextEditor) {
 			ITextEditor textEditor = (ITextEditor) fActiveEditorPart;
 			fContentAssistProposal
 					.setAction(getAction(
 							textEditor,
 							StructuredTextEditorActionConstants.ACTION_NAME_CONTENTASSIST_PROPOSALS));
 			fContentAssistTip
 					.setAction(getAction(
 							textEditor,
 							ITextEditorActionConstants.SHOW_INFORMATION));
 		}
 		if (fActiveEditorPart instanceof ITextEditorExtension) {
 			ITextEditorExtension extension = (ITextEditorExtension) fActiveEditorPart;
 			for (int i = 0; i < STATUSFIELDS.length; i++)
 				extension.setStatusField((IStatusField) fStatusFields
 						.get(STATUSFIELDS[i]), STATUSFIELDS[i]);
 		}
 	}
 
 	public void dispose() {
 		setActiveEditor(null);
 		if (fToggleOccurencesMarkUp != null) {
 			fToggleOccurencesMarkUp.setEditor(null);
 			fToggleOccurencesMarkUp = null;
 		}
 		
 		super.dispose();
 		fActiveEditorPart=null;
 		mainPart=null;
 		fContentAssistProposal=null;
 		fContentAssistTip=null;
 	}
 }
