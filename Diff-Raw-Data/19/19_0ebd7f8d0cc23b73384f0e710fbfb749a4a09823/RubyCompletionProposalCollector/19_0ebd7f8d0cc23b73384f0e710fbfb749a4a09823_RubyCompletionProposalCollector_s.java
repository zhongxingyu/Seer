 package org.eclipse.dltk.ruby.internal.ui.text.completion;
 
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.ui.text.completion.CompletionProposalCollector;
 import org.eclipse.dltk.ui.text.completion.CompletionProposalLabelProvider;
 import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
 import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
 import org.eclipse.swt.graphics.Image;
 
 public class RubyCompletionProposalCollector extends
 		CompletionProposalCollector {
 
 	protected final static char[] VAR_TRIGGER = new char[] { '\t', ' ', '=',
 			';', '.' };
 
 	public RubyCompletionProposalCollector(ISourceModule module) {
 		super(module);
 	}
 
 	protected CompletionProposalLabelProvider createLabelProvider() {
 		return new RubyCompletionProposalLabelProvider();
 	}
 	
 	
 
 	protected ScriptCompletionProposal createScriptCompletionProposal(
 			String completion, int replaceStart, int length, Image image,
 			String displayString, int i) {
		return new RubyCompletionProposal(displayString, replaceStart,
 				length, image, displayString, i);
 	}
 
 	protected ScriptCompletionProposal createScriptCompletionProposal(
 			String completion, int replaceStart, int length, Image image,
 			String displayString, int i, boolean isInDoc) {
		return new RubyCompletionProposal(displayString, replaceStart,
 				length, image, displayString, i, isInDoc);
 	}
 	
 	
 
 	protected char[] getVarTrigger() {
 		return VAR_TRIGGER;
 	}
 
 	protected ScriptCompletionProposal createOverrideCompletionProposal(
 			IDLTKProject dltkProject, ISourceModule compilationUnit,
 			String name, String[] paramTypes, int start, int length,
 			String displayName, String completionProposal) {
 		return new RubyOverrideCompletionProposal(dltkProject, compilationUnit,
 				name, paramTypes, start, length, displayName,
 				completionProposal);
 	}
 
 	protected ScriptContentAssistInvocationContext createScriptContentAssistInvocationContext(
 			ISourceModule sourceModule) {
 		return new ScriptContentAssistInvocationContext(sourceModule) {
 			protected CompletionProposalLabelProvider createLabelProvider() {
 				return new RubyCompletionProposalLabelProvider();
 			}
 		};
 	}
 	
 
 	protected IScriptCompletionProposal createKeywordProposal(
 			CompletionProposal proposal) {
 		String completion = String.valueOf(proposal.getCompletion());
 		int start = proposal.getReplaceStart();
 		int length = getLength(proposal);
 		String label = getLabelProvider().createSimpleLabel(proposal);
 		Image img = getImage(getLabelProvider().createMethodImageDescriptor(
 				proposal));
 		int relevance = computeRelevance(proposal);
 		return createScriptCompletionProposal(completion, start, length, img,
 				label, relevance);
 	}
 
 	protected IScriptCompletionProposal createScriptCompletionProposal(CompletionProposal proposal) {
 		
 		return super.createScriptCompletionProposal(proposal);
 	}
 	
 	
 	
 }
