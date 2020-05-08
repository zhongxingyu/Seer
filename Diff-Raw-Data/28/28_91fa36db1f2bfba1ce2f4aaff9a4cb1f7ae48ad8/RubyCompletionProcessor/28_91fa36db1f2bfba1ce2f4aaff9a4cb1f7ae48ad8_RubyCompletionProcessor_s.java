 package org.eclipse.dltk.ruby.internal.ui.text.completion;
 
 import org.eclipse.dltk.core.CompletionContext;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 import org.eclipse.dltk.ui.text.completion.CompletionProposalLabelProvider;
 import org.eclipse.dltk.ui.text.completion.ContentAssistInvocationContext;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProcessor;
 import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
 import org.eclipse.jface.text.contentassist.IContextInformationValidator;
 import org.eclipse.ui.IEditorPart;
 
 public class RubyCompletionProcessor extends ScriptCompletionProcessor {
 	public RubyCompletionProcessor(IEditorPart editor,
 			ContentAssistant assistant, String partition) {
 		super(editor, assistant, partition);
 	}
 
 	protected IPreferenceStore getPreferenceStore() {
 		return RubyUI.getDefault().getPreferenceStore();
 	}
 
 	protected ContentAssistInvocationContext createContext(ITextViewer viewer,
 			int offset) {
 		return new ScriptContentAssistInvocationContext(viewer, offset,
 				fEditor, RubyNature.NATURE_ID) {
 			public CompletionContext getCoreContext() {
						return new CompletionContext();
					}
 
 			protected CompletionProposalLabelProvider createLabelProvider() {
 				return new RubyCompletionProposalLabelProvider();
 			}
 		};
 	}
 
 	protected static class Validator implements IContextInformationValidator,
 			IContextInformationPresenter {
 
 		private int initialOffset;
 
 		public boolean isContextInformationValid(int offset) {
 			return Math.abs(offset - initialOffset) < 5;
 		}
 
 		public void install(IContextInformation info, ITextViewer viewer,
 				int offset) {
 			initialOffset = offset;
 		}
 
 		public boolean updatePresentation(int documentPosition,
 				TextPresentation presentation) {
 			return false;
 		}
 	}
 
 	private IContextInformationValidator validator;
 
 	public IContextInformationValidator getContextInformationValidator() {
 		if (validator == null) {
 			validator = new Validator();
 		}
 		return validator;
 	}
 }
