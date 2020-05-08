 package org.eclipse.dltk.console.ui;
 
import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.ITextHover;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
 
 public class ScriptConsoleSourceViewerConfiguration extends
 		SourceViewerConfiguration {
 	private static final int DEFAULT_TAB_WIDTH = 4;
 
 	private static final int DEFAULT_CA_DELAY = 50;
 
 	private static final String PARTITION_TYPE = IDocument.DEFAULT_CONTENT_TYPE;
 
 	private IContentAssistProcessor processor;
 
 	private ITextHover hover;
 
 	public ScriptConsoleSourceViewerConfiguration(
 			IContentAssistProcessor processor, ITextHover hover) {
 		this.processor = processor;
 		this.hover = hover;
 	}
 
 	public int getTabWidth(ISourceViewer sourceViewer) {
 		return DEFAULT_TAB_WIDTH;
 	}
 
 	public ITextHover getTextHover(ISourceViewer sv, String contentType) {
 		return hover;
 	}
 
 	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
 		return new String[] { PARTITION_TYPE };		
 	}
 
 	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant ca = new ContentAssistant();
		ca.setContentAssistProcessor(processor, PARTITION_TYPE);

		ca.enableAutoActivation(true);
		ca.enableAutoInsert(false);
		ca.setAutoActivationDelay(DEFAULT_CA_DELAY);
				
		return ca;
 	}
 }
