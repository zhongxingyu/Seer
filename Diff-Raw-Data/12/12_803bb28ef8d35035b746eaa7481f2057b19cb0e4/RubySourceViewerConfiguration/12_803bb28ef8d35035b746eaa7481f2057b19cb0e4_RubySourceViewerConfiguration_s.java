 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *  Contributors:
  *     xored software, Inc. - initial API and implementation
  *     xored software, Inc. - fix tab handling (Bug# 200024) (Alex Panchenko) 
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.text;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.internal.ui.editor.ScriptSourceViewer;
 import org.eclipse.dltk.internal.ui.text.HTMLTextPresenter;
 import org.eclipse.dltk.internal.ui.text.ScriptElementProvider;
 import org.eclipse.dltk.ruby.internal.ui.text.completion.RubyCompletionProcessor;
 import org.eclipse.dltk.ruby.internal.ui.text.completion.RubyContentAssistPreference;
 import org.eclipse.dltk.ruby.internal.ui.typehierarchy.RubyHierarchyInformationControl;
 import org.eclipse.dltk.ui.CodeFormatterConstants;
 import org.eclipse.dltk.ui.text.AbstractScriptScanner;
 import org.eclipse.dltk.ui.text.IColorManager;
 import org.eclipse.dltk.ui.text.ScriptPresentationReconciler;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.dltk.ui.text.SingleTokenScriptScanner;
 import org.eclipse.dltk.ui.text.completion.ContentAssistPreference;
 import org.eclipse.dltk.ui.text.util.AutoEditUtils;
 import org.eclipse.dltk.ui.text.util.TabStyle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.AbstractInformationControlManager;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IAutoEditStrategy;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.information.IInformationPresenter;
 import org.eclipse.jface.text.information.IInformationProvider;
 import org.eclipse.jface.text.information.InformationPresenter;
 import org.eclipse.jface.text.presentation.IPresentationReconciler;
 import org.eclipse.jface.text.presentation.PresentationReconciler;
 import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
 import org.eclipse.jface.text.rules.ITokenScanner;
 import org.eclipse.jface.text.rules.RuleBasedScanner;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class RubySourceViewerConfiguration extends
 		ScriptSourceViewerConfiguration {
 
 	private RubyTextTools fTextTools;
 
 	private RubyCodeScanner fCodeScanner;
 
 	private AbstractScriptScanner fStringScanner;
 
 	private AbstractScriptScanner fSingleQuoteStringScanner;
 
 	private AbstractScriptScanner fCommentScanner;
 
 	private AbstractScriptScanner fDocScanner;
 
 	public RubySourceViewerConfiguration(IColorManager colorManager,
 			IPreferenceStore preferenceStore, ITextEditor editor,
 			String partitioning) {
 		super(colorManager, preferenceStore, editor, partitioning);
 	}
 
 	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
 		return IRubyPartitions.RUBY_PARTITION_TYPES;
 	}
 
 	/*
 	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
 	 */
 	public String[] getIndentPrefixes(ISourceViewer sourceViewer,
 			String contentType) {
 		if (fPreferenceStore == null) {
 			return super.getIndentPrefixes(sourceViewer, contentType);
 		}
 		final RubyPreferenceInterpreter prefs = new RubyPreferenceInterpreter(
 				fPreferenceStore);
 		final int tabWidth = prefs.getTabSize();
 		final int indentWidth = prefs.getIndentSize();
 		if (indentWidth < tabWidth) {
 			return new String[] { AutoEditUtils.getNSpaces(indentWidth), "" }; //$NON-NLS-1$
 		} else if (prefs.getTabStyle() == TabStyle.TAB) {
 			return getIndentPrefixesForTab(tabWidth);
 		} else {
 			return getIndentPrefixesForSpaces(tabWidth);
 		}
 	}
 
 	/**
 	 * Computes and returns the indent prefixes for space indentation and the
 	 * given <code>tabWidth</code>.
 	 * 
 	 * @param tabWidth
 	 *            the display tab width
 	 * @return the indent prefixes
 	 * @see #getIndentPrefixes(ISourceViewer, String)
 	 */
 	private static String[] getIndentPrefixesForSpaces(int tabWidth) {
 		final String[] indentPrefixes = new String[tabWidth + 2];
 		indentPrefixes[0] = AutoEditUtils.getNSpaces(tabWidth);
 		for (int i = 0; i < tabWidth; i++) {
 			indentPrefixes[i + 1] = AutoEditUtils.getNSpaces(i) + '\t';
 		}
 		indentPrefixes[tabWidth + 1] = ""; //$NON-NLS-1$
 		return indentPrefixes;
 	}
 
 	public int getTabWidth(ISourceViewer sourceViewer) {
 		if (fPreferenceStore == null)
 			return super.getTabWidth(sourceViewer);
 		return fPreferenceStore
 				.getInt(CodeFormatterConstants.FORMATTER_TAB_SIZE);
 	}
 
 	protected void initializeScanners() {
 		Assert.isTrue(isNewSetup());
 		fCodeScanner = new RubyCodeScanner(getColorManager(), fPreferenceStore);
 		fStringScanner = new RubyStringScanner(getColorManager(),
 				fPreferenceStore);
 		fSingleQuoteStringScanner = new RubySingleQuoteStringScanner(
 				getColorManager(), fPreferenceStore);
 		fCommentScanner = new SingleTokenScriptScanner(getColorManager(),
 				fPreferenceStore, IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT);
 
 		fDocScanner = new RubyDocScanner(getColorManager(), fPreferenceStore);
 	}
 
 	/**
	 * @return <code>true</code> iff the new setup without text tools is in
	 *         use.
 	 */
 	private boolean isNewSetup() {
 		return fTextTools == null;
 	}
 
 	protected RuleBasedScanner getStringScanner() {
 		return fStringScanner;
 	}
 
 	protected RuleBasedScanner getCommentScanner() {
 		return fCommentScanner;
 	}
 
 	public IPresentationReconciler getPresentationReconciler(
 			ISourceViewer sourceViewer) {
 		PresentationReconciler reconciler = new ScriptPresentationReconciler();
 		reconciler
 				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
 
 		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fCodeScanner);
 		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
 		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
 
 		dr = new DefaultDamagerRepairer(getStringScanner());
 		reconciler.setDamager(dr, IRubyPartitions.RUBY_STRING);
 		reconciler.setRepairer(dr, IRubyPartitions.RUBY_STRING);
 
 		dr = new DefaultDamagerRepairer(getSingleQuoteStringScanner());
 		reconciler.setDamager(dr, IRubyPartitions.RUBY_SINGLE_QUOTE_STRING);
 		reconciler.setRepairer(dr, IRubyPartitions.RUBY_SINGLE_QUOTE_STRING);
 
 		dr = new DefaultDamagerRepairer(getDocScanner());
 		reconciler.setDamager(dr, IRubyPartitions.RUBY_DOC);
 		reconciler.setRepairer(dr, IRubyPartitions.RUBY_DOC);
 
 		dr = new DefaultDamagerRepairer(getCommentScanner());
 		reconciler.setDamager(dr, IRubyPartitions.RUBY_COMMENT);
 		reconciler.setRepairer(dr, IRubyPartitions.RUBY_COMMENT);
 
 		return reconciler;
 	}
 
 	private ITokenScanner getSingleQuoteStringScanner() {
 		return fSingleQuoteStringScanner;
 	}
 
 	private ITokenScanner getDocScanner() {
 		return fDocScanner;
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
 	 * @see RubySourceViewerConfiguration#ScriptSourceViewerConfiguration(IColorManager,
 	 *      IPreferenceStore, ITextEditor, String)
 	 */
 	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
 		Assert.isTrue(isNewSetup());
 		if (fCodeScanner.affectsBehavior(event))
 			fCodeScanner.adaptToPreferenceChange(event);
 		if (fStringScanner.affectsBehavior(event))
 			fStringScanner.adaptToPreferenceChange(event);
 		if (fSingleQuoteStringScanner.affectsBehavior(event))
 			fSingleQuoteStringScanner.adaptToPreferenceChange(event);
 		if (fDocScanner.affectsBehavior(event))
 			fDocScanner.adaptToPreferenceChange(event);
		if (fCommentScanner.affectsBehavior(event)) 
 			fCommentScanner.adaptToPreferenceChange(event);
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
 				|| fStringScanner.affectsBehavior(event)
 				|| fSingleQuoteStringScanner.affectsBehavior(event)
 				|| fDocScanner.affectsBehavior(event);
 	}
 
 	private IInformationControlCreator getHierarchyPresenterControlCreator(
 			ISourceViewer sourceViewer) {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				int shellStyle = SWT.RESIZE;
 				int treeStyle = SWT.V_SCROLL | SWT.H_SCROLL;
 				return new RubyHierarchyInformationControl(parent, shellStyle,
 						treeStyle);
 			}
 		};
 	}
 
 	public IInformationPresenter getHierarchyPresenter(
 			ScriptSourceViewer sourceViewer, boolean doCodeResolve) {
 		// Do not create hierarchy presenter if there's no CU.
 		if (getEditor() != null
 				&& getEditor().getEditorInput() != null
 				&& EditorUtility.getEditorInputModelElement(getEditor(), true) == null)
 			return null;
 
 		InformationPresenter presenter = new InformationPresenter(
 				getHierarchyPresenterControlCreator(sourceViewer));
 		presenter
 				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
 		presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
 		IInformationProvider provider = new ScriptElementProvider(getEditor(),
 				doCodeResolve);
 		presenter.setInformationProvider(provider,
 				IDocument.DEFAULT_CONTENT_TYPE);
 
 		presenter.setSizeConstraints(50, 20, true, false);
 		return presenter;
 	}
 
 	protected boolean isNewLine(IDocument doc, String text) {
 		String[] delims = doc.getLegalLineDelimiters();
 		for (int i = 0; i < delims.length; ++i) {
 			if (delims[i].equals(text)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public IAutoEditStrategy[] getAutoEditStrategies(
 			ISourceViewer sourceViewer, String contentType) {
 		// // TODO: check contentType. think, do we really need it? :)
 		String partitioning = getConfiguredDocumentPartitioning(sourceViewer);
 		return new IAutoEditStrategy[] { new RubyAutoEditStrategy(partitioning) };
 	}
 
 	protected IInformationControlCreator getOutlinePresenterControlCreator(
 			ISourceViewer sourceViewer, final String commandId) {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				int shellStyle = SWT.RESIZE;
 				int treeStyle = SWT.V_SCROLL | SWT.H_SCROLL;
 				return new RubyOutlineInformationControl(parent, shellStyle,
 						treeStyle, commandId);
 			}
 		};
 	}
 
 	protected void alterContentAssistant(ContentAssistant assistant) {
 		IContentAssistProcessor scriptProcessor = new RubyCompletionProcessor(
 				getEditor(), assistant, IDocument.DEFAULT_CONTENT_TYPE);
 		assistant.setContentAssistProcessor(scriptProcessor,
 				IDocument.DEFAULT_CONTENT_TYPE);
 	}
 
 	protected ContentAssistPreference getContentAssistPreference() {
 		return RubyContentAssistPreference.getDefault();
 	}
 
 	public IInformationControlCreator getInformationControlCreator(
 			ISourceViewer sourceViewer) {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				return new DefaultInformationControl(parent, SWT.NONE,
 						new HTMLTextPresenter(true), ""); //$NON-NLS-1$
 			}
 		};
 	}
 
 	protected void initializeQuickOutlineContexts(
 			InformationPresenter presenter, IInformationProvider provider) {
 		presenter
 				.setInformationProvider(provider, IRubyPartitions.RUBY_COMMENT);
 		presenter.setInformationProvider(provider, IRubyPartitions.RUBY_DOC);
 		presenter.setInformationProvider(provider, IRubyPartitions.RUBY_STRING);
 		presenter.setInformationProvider(provider,
 				IRubyPartitions.RUBY_SINGLE_QUOTE_STRING);
 	}
 
 	protected Map getHyperlinkDetectorTargets(final ISourceViewer sourceViewer) {
 		final Map targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("org.eclipse.dltk.ruby.ode", getEditor()); //$NON-NLS-1$
 		return targets;
 	}
 
 }
