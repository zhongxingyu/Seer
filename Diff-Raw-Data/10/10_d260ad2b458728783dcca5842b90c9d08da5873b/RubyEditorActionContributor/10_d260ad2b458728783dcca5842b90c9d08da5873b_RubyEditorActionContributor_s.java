 package org.rubypeople.rdt.internal.ui.rubyeditor;
 
 import java.util.ResourceBundle;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.texteditor.ITextEditorActionConstants;
 import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
 import org.eclipse.ui.texteditor.RetargetTextEditorAction;
 import org.rubypeople.rdt.internal.ui.RubyUIMessages;
 import org.rubypeople.rdt.internal.ui.actions.FoldingActionGroup;
 import org.rubypeople.rdt.ui.actions.IRubyEditorActionDefinitionIds;
 import org.rubypeople.rdt.ui.actions.RubyActionIds;
 
 public class RubyEditorActionContributor extends BasicTextEditorActionContributor {
 
     protected RetargetTextEditorAction contentAssistProposal;
     private RetargetTextEditorAction fGotoMatchingBracket;
 
     public RubyEditorActionContributor() {
         super();
         ResourceBundle b = RubyEditorMessages.getBundleForConstructedKeys();
 
         contentAssistProposal = new RetargetTextEditorAction(RubyUIMessages.getResourceBundle(),
                 "ContentAssistProposal.");
         fGotoMatchingBracket = new RetargetTextEditorAction(b, "GotoMatchingBracket."); //$NON-NLS-1$
         fGotoMatchingBracket
                 .setActionDefinitionId(IRubyEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
 
     }
 
     public void contributeToMenu(IMenuManager menuManager) {
         IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
         if (editMenu != null) {
             editMenu.add(new Separator());
             editMenu.add(contentAssistProposal);
         }
         
         IMenuManager gotoMenu= menuManager.findMenuUsingPath("navigate/goTo"); //$NON-NLS-1$
         if (gotoMenu != null) {
             gotoMenu.add(new Separator("additions2"));  //$NON-NLS-1$
             gotoMenu.appendToGroup("additions2", fGotoMatchingBracket); //$NON-NLS-1$
         }
     }
 
     public void setActiveEditor(IEditorPart part) {
         super.setActiveEditor(part);
 
         ITextEditor textEditor = null;
         if (part instanceof ITextEditor) textEditor = (ITextEditor) part;
 
         contentAssistProposal.setAction(getAction(textEditor, "ContentAssistProposal"));
         fGotoMatchingBracket.setAction(getAction(textEditor,
                 GotoMatchingBracketAction.GOTO_MATCHING_BRACKET));
 
         
         if (part instanceof RubyEditor) {
         		RubyEditor javaEditor= (RubyEditor) part;
 			javaEditor.getActionGroup().fillActionBars(getActionBars());
 			FoldingActionGroup foldingActions= javaEditor.getFoldingActionGroup();
 			if (foldingActions != null)
 				foldingActions.updateActionBars();
 		}
         
         IActionBars actionBars = getActionBars();
         actionBars.setGlobalActionHandler(RubyActionIds.COMMENT, getAction(textEditor, "Comment"));
         actionBars.setGlobalActionHandler(RubyActionIds.UNCOMMENT, getAction(textEditor,
                 "Uncomment"));
 
         /** The global actions to be connected with editor actions */
         IAction action = getAction(textEditor, ITextEditorActionConstants.NEXT);
         actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION,
                 action);
         actionBars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, action);
         action = getAction(textEditor, ITextEditorActionConstants.PREVIOUS);
         actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION,
                 action);
         actionBars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, action);
     }
 }
