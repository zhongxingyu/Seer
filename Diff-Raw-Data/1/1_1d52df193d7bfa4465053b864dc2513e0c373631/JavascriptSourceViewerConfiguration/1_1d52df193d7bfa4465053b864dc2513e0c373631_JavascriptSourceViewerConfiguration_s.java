 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.text;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.internal.ui.editor.ScriptSourceViewer;
 import org.eclipse.dltk.javascript.internal.ui.text.completion.JavaScriptCompletionProcessor;
 import org.eclipse.dltk.javascript.internal.ui.text.completion.JavaScriptContentAssistPreference;
 import org.eclipse.dltk.javascript.ui.text.IJavaScriptPartitions;
 import org.eclipse.dltk.ui.CodeFormatterConstants;
 import org.eclipse.dltk.ui.text.AbstractScriptScanner;
 import org.eclipse.dltk.ui.text.IColorManager;
 import org.eclipse.dltk.ui.text.ScriptPresentationReconciler;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.dltk.ui.text.SingleTokenScriptScanner;
 import org.eclipse.dltk.ui.text.completion.ContentAssistProcessor;
 import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IAutoEditStrategy;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.jface.text.formatter.IContentFormatter;
 import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
 import org.eclipse.jface.text.information.IInformationPresenter;
 import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
 import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
 import org.eclipse.jface.text.rules.RuleBasedScanner;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 
 public class JavascriptSourceViewerConfiguration extends
 		ScriptSourceViewerConfiguration {
 
 	private JavascriptTextTools fTextTools;
 
 	private JavascriptCodeScanner fCodeScanner;
 
 	private AbstractScriptScanner fStringScanner;
 
 	private AbstractScriptScanner fCommentScanner;
 
 	public JavascriptSourceViewerConfiguration(IColorManager colorManager,
 			IPreferenceStore preferenceStore, ITextEditor editor,
 			String partitioning) {
 		super(colorManager, preferenceStore, editor, partitioning);
 	}
 
 	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
 		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
 				IJavaScriptPartitions.JS_STRING, IJavaScriptPartitions.JS_COMMENT};
 	}
 	
 	public String[] getDefaultPrefixes(ISourceViewer sourceViewer,
 			String contentType) {
 		return new String[] { "//", "" }; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	public String[] getIndentPrefixes(ISourceViewer sourceViewer,
 			String contentType) {
 		// XXX: what happens here?.. why " " ?
 		return new String[] { "\t", "    " }; 
 	}
 	
 	/*
 	 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
 	 */
 	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
 		final MultiPassContentFormatter formatter= new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
 
 		formatter.setMasterStrategy(new JavaScriptFormattingStrategy());
 //		formatter.setSlaveStrategy(new CommentFormattingStrategy(), IJavaPartitions.JAVA_DOC);
 //		formatter.setSlaveStrategy(new CommentFormattingStrategy(), IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
 //		formatter.setSlaveStrategy(new CommentFormattingStrategy(), IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
 
 		return formatter;
 	}
 	
 	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
 
 		if (getEditor() != null) {
 
 			ContentAssistant assistant= new ContentAssistant();
 			assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
 
 			assistant.setRestoreCompletionProposalSize(getSettings("completion_proposal_size")); //$NON-NLS-1$
 
 			IContentAssistProcessor scriptProcessor= new JavaScriptCompletionProcessor(getEditor(), assistant, IDocument.DEFAULT_CONTENT_TYPE);
 			assistant.setContentAssistProcessor(scriptProcessor, IDocument.DEFAULT_CONTENT_TYPE);
 
 			ContentAssistProcessor singleLineProcessor= new JavaScriptCompletionProcessor(getEditor(), assistant, IJavaScriptPartitions.JS_COMMENT);
 			assistant.setContentAssistProcessor(singleLineProcessor, IJavaScriptPartitions.JS_COMMENT);
 
 			ContentAssistProcessor stringProcessor= new JavaScriptCompletionProcessor(getEditor(), assistant, IJavaScriptPartitions.JS_STRING);
 			assistant.setContentAssistProcessor(stringProcessor, IJavaScriptPartitions.JS_STRING);
 
 			JavaScriptContentAssistPreference.getDefault().configure(assistant, fPreferenceStore);
 
 			assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
 			assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
 			
 			return assistant;
 		}
 
 		return null;
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
 	 */
 	public int getTabWidth(ISourceViewer sourceViewer) {
 		if (fPreferenceStore == null)
 			return super.getTabWidth(sourceViewer);
 		return fPreferenceStore.getInt(CodeFormatterConstants.FORMATTER_TAB_SIZE);
 	}
 
 	protected void initializeScanners() {
 		Assert.isTrue(isNewSetup());
 		fCodeScanner = new JavascriptCodeScanner(getColorManager(),
 				fPreferenceStore);
 		fStringScanner = new JavascriptStringScanner(getColorManager(),
 				fPreferenceStore);
 		fCommentScanner = new SingleTokenScriptScanner(getColorManager(),
 				fPreferenceStore,
 				JavascriptColorConstants.JS_SINGLE_LINE_COMMENT);
 	}
 
 	/**
 	 * @return <code>true</code> iff the new setup without text tools is in
 	 *         use.
 	 */
 	private boolean isNewSetup() {
 		return fTextTools == null;
 	}
 
 	/**
 	 * Returns the Javascript string scanner for this configuration.
 	 * 
 	 * @return the Javascript string scanner
 	 */
 	protected RuleBasedScanner getStringScanner() {
 		return fStringScanner;
 	}
 
 	/**
 	 * Returns the Javascript comment scanner for this configuration.
 	 * 
 	 * @return the Javascript comment scanner
 	 */	
 	protected RuleBasedScanner getCommentScanner() { return fCommentScanner; }
 	
 	public IPresentationReconciler getPresentationReconciler(
 			ISourceViewer sourceViewer) {
 		ScriptPresentationReconciler reconciler = new ScriptPresentationReconciler();
 		reconciler
 				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
 
 		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(
 				this.fCodeScanner);
 		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
 		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
 
 		dr = new DefaultDamagerRepairer(getStringScanner());
 		reconciler.setDamager(dr, IJavaScriptPartitions.JS_STRING);
 		reconciler.setRepairer(dr, IJavaScriptPartitions.JS_STRING);
 
 		
 		dr = new DefaultDamagerRepairer(getCommentScanner());
 		reconciler.setDamager(dr, IJavaScriptPartitions.JS_COMMENT);
 		reconciler.setRepairer(dr, IJavaScriptPartitions.JS_COMMENT);
 
 
 		return reconciler;
 	}
 
 	/**
 	 * Adapts the behavior of the contained components to the change encoded in
 	 * the given event.
 	 * <p>
 	 * Clients are not allowed to call this method if the old setup with text
 	 * tools is in use.
 	 * </p>
 	 * 
 	 * @param event
 	 *            the event to which to adapt
 	 * @see JavascriptSourceViewerConfiguration#ScriptSourceViewerConfiguration(IColorManager,
 	 *      IPreferenceStore, ITextEditor, String)
 	 */
 	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
 		Assert.isTrue(isNewSetup());
 		if (fCodeScanner.affectsBehavior(event))
 			fCodeScanner.adaptToPreferenceChange(event);
 		if (fStringScanner.affectsBehavior(event))
 			fStringScanner.adaptToPreferenceChange(event);
 	}
 
 	/**
 	 * Determines whether the preference change encoded by the given event
 	 * changes the behavior of one of its contained components.
 	 * 
 	 * @param event
 	 *            the event to be investigated
 	 * @return <code>true</code> if event causes a behavioral change
 	 * 
 	 */
 	public boolean affectsTextPresentation(PropertyChangeEvent event) {
 		return fCodeScanner.affectsBehavior(event)
 				|| fStringScanner.affectsBehavior(event);
 	}
 
 	public IInformationPresenter getHierarchyPresenter(
 			ScriptSourceViewer viewer, boolean b) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IInformationPresenter getOutlinePresenter(ScriptSourceViewer viewer,
 			boolean b) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public IAutoEditStrategy[] getAutoEditStrategies(
 			ISourceViewer sourceViewer, String contentType) {
 		// TODO: check contentType. think, do we really need it? :)
 		String partitioning = getConfiguredDocumentPartitioning(sourceViewer);
 		return new IAutoEditStrategy[] { new JavascriptAutoEditStrategy(
 				partitioning,null) };
 	}
 	
 	public IInformationControlCreator getInformationControlCreator(
 			ISourceViewer sourceViewer) {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				return new DefaultInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true));
 			}
 		};
 	}
 	
 	protected IInformationControlCreator getOutlinePresenterControlCreator(ISourceViewer sourceViewer, final String commandId) {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				int shellStyle= SWT.RESIZE;
 				int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
 				return new JavaScriptOutlineInformationControl(parent, shellStyle, treeStyle, commandId);
 			}
 		};
 	}
 }
