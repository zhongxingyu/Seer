 package org.rubypeople.rdt.ui.text;
 
 import java.util.Vector;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IAutoEditStrategy;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.ITextDoubleClickStrategy;
 import org.eclipse.jface.text.ITextHover;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.jface.text.formatter.IContentFormatter;
 import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
 import org.eclipse.jface.text.presentation.IPresentationReconciler;
 import org.eclipse.jface.text.presentation.PresentationReconciler;
 import org.eclipse.jface.text.reconciler.IReconciler;
 import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
 import org.eclipse.jface.text.rules.ITokenScanner;
 import org.eclipse.jface.text.source.IAnnotationHover;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.util.Assert;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
 import org.eclipse.ui.texteditor.ChainedPreferenceStore;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.formatter.DefaultCodeFormatterConstants;
 import org.rubypeople.rdt.internal.corext.util.CodeFormatterUtil;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.rubyeditor.IRubyScriptDocumentProvider;
 import org.rubypeople.rdt.internal.ui.rubyeditor.RubyAbstractEditor;
 import org.rubypeople.rdt.internal.ui.text.HTMLTextPresenter;
 import org.rubypeople.rdt.internal.ui.text.IRubyColorConstants;
 import org.rubypeople.rdt.internal.ui.text.IRubyPartitions;
 import org.rubypeople.rdt.internal.ui.text.PreferencesAdapter;
 import org.rubypeople.rdt.internal.ui.text.RubyAnnotationHover;
 import org.rubypeople.rdt.internal.ui.text.RubyCommentScanner;
 import org.rubypeople.rdt.internal.ui.text.RubyContentAssistPreference;
 import org.rubypeople.rdt.internal.ui.text.RubyDoubleClickSelector;
 import org.rubypeople.rdt.internal.ui.text.RubyPartitionScanner;
 import org.rubypeople.rdt.internal.ui.text.RubyReconciler;
 import org.rubypeople.rdt.internal.ui.text.comment.CommentFormattingStrategy;
 import org.rubypeople.rdt.internal.ui.text.comment.RubyCommentAutoIndentStrategy;
 import org.rubypeople.rdt.internal.ui.text.ruby.AbstractRubyScanner;
 import org.rubypeople.rdt.internal.ui.text.ruby.RubyCodeScanner;
 import org.rubypeople.rdt.internal.ui.text.ruby.RubyCompletionProcessor;
 import org.rubypeople.rdt.internal.ui.text.ruby.RubyFormattingStrategy;
 import org.rubypeople.rdt.internal.ui.text.ruby.RubyReconcilingStrategy;
 import org.rubypeople.rdt.internal.ui.text.ruby.SingleTokenRubyCodeScanner;
 import org.rubypeople.rdt.internal.ui.text.ruby.hover.RubyCodeTextHover;
 
 public class RubySourceViewerConfiguration extends TextSourceViewerConfiguration {
 
     protected RubyTextTools textTools;
     protected ITextEditor fTextEditor;
     private RubyCodeTextHover fRubyTextHover;
 
     /**
      * The document partitioning.
      * 
      * @since 0.8.0
      */
     private String fDocumentPartitioning;
 
     /**
      * The color manager.
      * 
      * @since 0.8.0
      */
     private IColorManager fColorManager;
 
     protected AbstractRubyScanner fCodeScanner;
     protected AbstractRubyScanner fMultilineCommentScanner, fSinglelineCommentScanner,
             fStringScanner;
     private SingleTokenRubyCodeScanner fRegexpScanner;
     private SingleTokenRubyCodeScanner fCommandScanner;
     private RubyDoubleClickSelector fRubyDoubleClickSelector;
 
     /**
      * Creates a new Ruby source viewer configuration for viewers in the given
      * editor using the given preference store, the color manager and the
      * specified document partitioning.
      * <p>
      * Creates a Ruby source viewer configuration in the new setup without text
      * tools. Clients are allowed to call
      * {@link RubySourceViewerConfiguration#handlePropertyChangeEvent(PropertyChangeEvent)}
      * and disallowed to call
      * {@link RubySourceViewerConfiguration#getPreferenceStore()} on the
      * resulting Ruby source viewer configuration.
      * </p>
      * 
      * @param colorManager
      *            the color manager
      * @param preferenceStore
      *            the preference store, can be read-only
      * @param editor
      *            the editor in which the configured viewer(s) will reside, or
      *            <code>null</code> if none
      * @param partitioning
      *            the document partitioning for this configuration, or
      *            <code>null</code> for the default partitioning
      * @since 3.0
      */
     public RubySourceViewerConfiguration(IColorManager colorManager,
             IPreferenceStore preferenceStore, ITextEditor editor, String partitioning) {
         super(preferenceStore);
         fColorManager = colorManager;
         fTextEditor = editor;
         fDocumentPartitioning = partitioning;
         initializeScanners();
     }
     
     /*
      * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
      */
     public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
         final MultiPassContentFormatter formatter= new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
 
         formatter.setMasterStrategy(new RubyFormattingStrategy());
         formatter.setSlaveStrategy(new CommentFormattingStrategy(), IRubyPartitions.RUBY_DOC);
         formatter.setSlaveStrategy(new CommentFormattingStrategy(), IRubyPartitions.RUBY_SINGLE_LINE_COMMENT);
         formatter.setSlaveStrategy(new CommentFormattingStrategy(), IRubyPartitions.RUBY_MULTI_LINE_COMMENT);
 
         return formatter;
     }
 
     /**
      * Determines whether the preference change encoded by the given event
      * changes the behavior of one of its contained components.
      * 
      * @param event
      *            the event to be investigated
      * @return <code>true</code> if event causes a behavioral change
      * @since 0.8.0
      */
     public boolean affectsTextPresentation(PropertyChangeEvent event) {
         return fCodeScanner.affectsBehavior(event)
                 || fMultilineCommentScanner.affectsBehavior(event)
                 || fSinglelineCommentScanner.affectsBehavior(event)
                 || fStringScanner.affectsBehavior(event) || fRegexpScanner.affectsBehavior(event)
                 || fCommandScanner.affectsBehavior(event);
     }
     
     /**
      * Adapts the behavior of the contained components to the change
      * encoded in the given event.
      * <p>
      * Clients are not allowed to call this method if the old setup with
      * text tools is in use.
      * </p>
      *
      * @param event the event to which to adapt
      * @see RubySourceViewerConfiguration#RubySourceViewerConfiguration(IColorManager, IPreferenceStore, ITextEditor, String)
      * @since 0.8.0
      */
     public void handlePropertyChangeEvent(PropertyChangeEvent event) {
         Assert.isTrue(isNewSetup());
         if (fCodeScanner.affectsBehavior(event))
             fCodeScanner.adaptToPreferenceChange(event);
         if (fMultilineCommentScanner.affectsBehavior(event))
             fMultilineCommentScanner.adaptToPreferenceChange(event);
         if (fSinglelineCommentScanner.affectsBehavior(event))
             fSinglelineCommentScanner.adaptToPreferenceChange(event);
         if (fStringScanner.affectsBehavior(event))
             fStringScanner.adaptToPreferenceChange(event);
         if (fRegexpScanner.affectsBehavior(event))
             fRegexpScanner.adaptToPreferenceChange(event);
         if (fCommandScanner.affectsBehavior(event))
             fCommandScanner.adaptToPreferenceChange(event);
     }
 
 
     /**
      * Creates and returns a preference store which combines the preference
      * stores from the text tools and which is read-only.
      * 
      * @param rubyTextTools
      *            the Ruby text tools
      * @return the combined read-only preference store
      * @since 0.8.0
      */
     private static final IPreferenceStore createPreferenceStore(RubyTextTools rubyTextTools) {
         Assert.isNotNull(rubyTextTools);
         IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
         if (rubyTextTools.getCorePreferenceStore() == null)
             return new ChainedPreferenceStore(new IPreferenceStore[] {
                     rubyTextTools.getPreferenceStore(), generalTextStore});
 
         return new ChainedPreferenceStore(new IPreferenceStore[] {
                 rubyTextTools.getPreferenceStore(),
                 new PreferencesAdapter(rubyTextTools.getCorePreferenceStore()), generalTextStore});
     }
 
     /**
      * Initializes the scanners.
      * 
      * @since 3.0
      */
     private void initializeScanners() {
         Assert.isTrue(isNewSetup());
         fCodeScanner = new RubyCodeScanner(getColorManager(), fPreferenceStore);
         fMultilineCommentScanner = new RubyCommentScanner(getColorManager(), fPreferenceStore,
                 IRubyColorConstants.RUBY_MULTI_LINE_COMMENT);
         fSinglelineCommentScanner = new RubyCommentScanner(getColorManager(), fPreferenceStore,
                 IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT);
         fStringScanner = new SingleTokenRubyCodeScanner(getColorManager(), fPreferenceStore,
                 IRubyColorConstants.RUBY_STRING);
         fRegexpScanner = new SingleTokenRubyCodeScanner(getColorManager(), fPreferenceStore,
                 IRubyColorConstants.RUBY_REGEXP);
         fCommandScanner = new SingleTokenRubyCodeScanner(getColorManager(), fPreferenceStore,
                 IRubyColorConstants.RUBY_COMMAND);
     }
 
     /**
      * @return <code>true</code> iff the new setup without text tools is in
      *         use.
      * 
      * @since 3.0
      */
     private boolean isNewSetup() {
         return textTools == null;
     }
 
     /**
      * Returns the color manager for this configuration.
      * 
      * @return the color manager
      */
     protected IColorManager getColorManager() {
         return fColorManager;
     }
 
     public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
         PresentationReconciler reconciler = new PresentationReconciler();
         reconciler.setDocumentPartitioning(IRubyPartitions.RUBY_PARTITIONING);
 
         DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
         reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
         reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
 
         dr = new DefaultDamagerRepairer(getSinglelineCommentScanner());
         reconciler.setDamager(dr, RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT);
         reconciler.setRepairer(dr, RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT);
 
         dr = new DefaultDamagerRepairer(getMultilineCommentScanner());
         reconciler.setDamager(dr, RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT);
         reconciler.setRepairer(dr, RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT);
 
         dr = new DefaultDamagerRepairer(getStringScanner());
         reconciler.setDamager(dr, RubyPartitionScanner.RUBY_STRING);
         reconciler.setRepairer(dr, RubyPartitionScanner.RUBY_STRING);
 
         dr = new DefaultDamagerRepairer(getRegexpScanner());
         reconciler.setDamager(dr, RubyPartitionScanner.RUBY_REGULAR_EXPRESSION);
         reconciler.setRepairer(dr, RubyPartitionScanner.RUBY_REGULAR_EXPRESSION);
 
         dr = new DefaultDamagerRepairer(getCommandScanner());
         reconciler.setDamager(dr, RubyPartitionScanner.RUBY_COMMAND);
         reconciler.setRepairer(dr, RubyPartitionScanner.RUBY_COMMAND);
 
         return reconciler;
     }
 
     private ITokenScanner getCommandScanner() {
         return fCommandScanner;
     }
 
     protected ITokenScanner getCodeScanner() {
         return fCodeScanner;
     }
 
     protected ITokenScanner getMultilineCommentScanner() {
         return fMultilineCommentScanner;
     }
 
     protected ITokenScanner getSinglelineCommentScanner() {
         return fSinglelineCommentScanner;
     }
 
     protected ITokenScanner getStringScanner() {
         return fStringScanner;
     }
 
     protected ITokenScanner getRegexpScanner() {
         return fRegexpScanner;
     }
 
     public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
         return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
                 RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, RubyPartitionScanner.RUBY_STRING,
                 RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT};
     }
     
     /*
      * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
      * @since 0.8.0
      */
     public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
         if (fDocumentPartitioning != null)
             return fDocumentPartitioning;
         return super.getConfiguredDocumentPartitioning(sourceViewer);
     }
 
     public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
         ContentAssistant contentAssistant = new ContentAssistant();
         contentAssistant.setContentAssistProcessor(new RubyCompletionProcessor(getEditor()),
                 IDocument.DEFAULT_CONTENT_TYPE);
 
         contentAssistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_OVERLAY);
         contentAssistant.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_ABOVE);
 
         RubyContentAssistPreference.configure(contentAssistant, getPreferenceStore());
         return contentAssistant;
     }
 
     /*
      * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
      */
     public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
         return new RubyAnnotationHover(RubyAnnotationHover.VERTICAL_RULER_HOVER);
     }
     
     public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
     	String partitioning= getConfiguredDocumentPartitioning(sourceViewer);
 		if (IRubyPartitions.RUBY_SINGLE_LINE_COMMENT.equals(contentType))
 			return new IAutoEditStrategy[] { new RubyCommentAutoIndentStrategy(partitioning) };
 		else
 			return super.getAutoEditStrategies(sourceViewer, contentType);
     }
 
     /*
      * @see SourceViewerConfiguration#getInformationControlCreator(ISourceViewer)
      * @since 2.0
      */
     public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
         return new IInformationControlCreator() {
 
             public IInformationControl createInformationControl(Shell parent) {
                 return new DefaultInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true));
             }
         };
     }
 
     /**
      * Returns the editor in which the configured viewer(s) will reside.
      * 
      * @return the enclosing editor
      */
     protected ITextEditor getEditor() {
         return fTextEditor;
     }
 
     protected IPreferenceStore getPreferenceStore() {
         return RubyPlugin.getDefault().getPreferenceStore();
     }
 
     public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
         return new String[] { "#", ""};
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
      */
     public IReconciler getReconciler(ISourceViewer sourceViewer) {
    	final ITextEditor editor = getEditor();
		if (editor != null && editor.isEditable()) {
			RubyReconciler reconciler = new RubyReconciler(editor,
					new RubyReconcilingStrategy(
							(RubyAbstractEditor) fTextEditor), true);
			reconciler.setIsIncrementalReconciler(false);
			// TODO Uncomment when we move to Eclipse 3.2
			// ECLIPSE 3.2
			// reconciler.setIsAllowedToModifyDocument(false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
     }
 
 	private IRubyProject getProject() {
 		ITextEditor editor= getEditor();
 		if (editor == null)
 			return null;
 
 		IRubyElement element= null;
 		IEditorInput input= editor.getEditorInput();
 		IDocumentProvider provider= editor.getDocumentProvider();
 		if (provider instanceof IRubyScriptDocumentProvider) {
 			IRubyScriptDocumentProvider cudp= (IRubyScriptDocumentProvider) provider;
 			element= cudp.getWorkingCopy(input);
 		}
 
 		if (element == null)
 			return null;
 
 		return element.getRubyProject();
 	}
     
     public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
 
 		Vector vector= new Vector();
 
 		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
 
 		IRubyProject project= getProject();
 		final int tabWidth= CodeFormatterUtil.getTabWidth(project);
 		final int indentWidth= CodeFormatterUtil.getIndentWidth(project);
 		int spaceEquivalents= Math.min(tabWidth, indentWidth);
 		boolean useSpaces;
 		if (project == null)
 			useSpaces= RubyCore.SPACE.equals(RubyCore.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)) || tabWidth > indentWidth;
 		else
 			useSpaces= RubyCore.SPACE.equals(project.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true)) || tabWidth > indentWidth;
 
 		for (int i= 0; i <= spaceEquivalents; i++) {
 		    StringBuffer prefix= new StringBuffer();
 
 			if (useSpaces) {
 			    for (int j= 0; j + i < spaceEquivalents; j++)
 			    	prefix.append(' ');
 
 				if (i != 0)
 		    		prefix.append('\t');
 			} else {
 			    for (int j= 0; j < i; j++)
 			    	prefix.append(' ');
 
 				if (i != spaceEquivalents)
 		    		prefix.append('\t');
 			}
 
 			vector.add(prefix.toString());
 		}
 
 		vector.add(""); //$NON-NLS-1$
 
 		return (String[]) vector.toArray(new String[vector.size()]);
     }
     
 	/*
 	 * @see SourceViewerConfiguration#getTabWidth(ISourceViewer)
 	 */
 	public int getTabWidth(ISourceViewer sourceViewer) {
 		return CodeFormatterUtil.getTabWidth(getProject());
 	}
 
     public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer,
             String contentType) {
         if (fRubyDoubleClickSelector == null) {
             fRubyDoubleClickSelector= new RubyDoubleClickSelector();
         }
         return fRubyDoubleClickSelector;
     }
 
     public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
         if (fRubyTextHover == null) {
             fRubyTextHover = new RubyCodeTextHover();
         }
         return fRubyTextHover;
     }
 
 }
