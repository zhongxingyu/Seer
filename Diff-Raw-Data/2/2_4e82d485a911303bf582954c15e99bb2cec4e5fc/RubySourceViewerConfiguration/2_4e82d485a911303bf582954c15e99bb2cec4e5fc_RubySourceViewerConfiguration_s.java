 package org.rubypeople.rdt.internal.ui.text;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.jface.text.presentation.IPresentationReconciler;
 import org.eclipse.jface.text.presentation.PresentationReconciler;
 import org.eclipse.jface.text.reconciler.IReconciler;
 import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
 import org.eclipse.jface.text.rules.ITokenScanner;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.rubypeople.rdt.internal.ui.RdtUiPlugin;
 import org.rubypeople.rdt.internal.ui.rubyeditor.RubyAbstractEditor;
 import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditor;
 import org.rubypeople.rdt.internal.ui.text.ruby.RubyCompletionProcessor;
 
 public class RubySourceViewerConfiguration extends SourceViewerConfiguration {
 
 	protected RubyTextTools textTools;
 	protected RubyAbstractEditor textEditor;
 
 	public RubySourceViewerConfiguration(RubyTextTools theTextTools, RubyAbstractEditor theTextEditor) {
 		super();
 		textEditor = theTextEditor;
 		textTools = theTextTools;
 	}
 
 	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
 		PresentationReconciler reconciler = new PresentationReconciler();
 
 		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
 		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
 		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
 
 		dr = new DefaultDamagerRepairer(getSinglelineCommentScanner());
 		reconciler.setDamager(dr, RubyPartitionScanner.SINGLE_LINE_COMMENT);
 		reconciler.setRepairer(dr, RubyPartitionScanner.SINGLE_LINE_COMMENT);
 		
 		dr = new DefaultDamagerRepairer(getMultilineCommentScanner());
 		reconciler.setDamager(dr, RubyPartitionScanner.MULTI_LINE_COMMENT);
 		reconciler.setRepairer(dr, RubyPartitionScanner.MULTI_LINE_COMMENT);
 
 		dr = new DefaultDamagerRepairer(getStringScanner());
 		reconciler.setDamager(dr, RubyPartitionScanner.STRING);
 		reconciler.setRepairer(dr, RubyPartitionScanner.STRING);
 
 		return reconciler;
 	}
 
 	protected ITokenScanner getCodeScanner() {
 		return textTools.getCodeScanner();
 	}
 
 	protected ITokenScanner getMultilineCommentScanner() {
 		return textTools.getMultilineCommentScanner();
 	}
 
 	protected ITokenScanner getSinglelineCommentScanner() {
 		return textTools.getSinglelineCommentScanner();
 	}
 
 	protected ITokenScanner getStringScanner() {
 		return textTools.getStringScanner();
 	}
 
 	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, RubyPartitionScanner.MULTI_LINE_COMMENT, RubyPartitionScanner.STRING};
 	}
 
 	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
 		ContentAssistant contentAssistant = new ContentAssistant();
 		contentAssistant.setContentAssistProcessor(new RubyCompletionProcessor(textTools), IDocument.DEFAULT_CONTENT_TYPE);
 
 		contentAssistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_OVERLAY);
 		contentAssistant.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_ABOVE);
 
 		RubyContentAssistPreference.configure(contentAssistant, getPreferenceStore());
 		return contentAssistant;
 	}
 
 	protected IPreferenceStore getPreferenceStore() {
 		return RdtUiPlugin.getDefault().getPreferenceStore();
 	}
 
 	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
 		return new String[] { "#", ""};
 	}
 
 	 /* (non-Javadoc)
      * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
      */
     public IReconciler getReconciler(ISourceViewer sourceViewer) {
 	    NotifyingReconciler reconciler= new NotifyingReconciler(new RubyReconcilingStrategy(textEditor), true);
 	    reconciler.setDelay(RubyReconcilingStrategy.DELAY);
 	    return reconciler;
     }
     
 	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
 		if (!(textEditor instanceof RubyEditor)) {
 			return super.getIndentPrefixes(sourceViewer, contentType) ;
 		}
 		RubyEditor rubyEditor = (RubyEditor) textEditor ;
 		if (rubyEditor.isTabReplacing()) {
 			return new String[] { rubyEditor.getTabReplaceString(), "\t", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		else {
 			return super.getIndentPrefixes(sourceViewer, contentType); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 	}    
 	
 }
