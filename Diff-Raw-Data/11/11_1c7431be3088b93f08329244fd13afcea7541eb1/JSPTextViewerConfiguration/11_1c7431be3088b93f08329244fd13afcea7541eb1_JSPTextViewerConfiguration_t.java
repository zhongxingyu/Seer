 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.text.ITextHover;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jst.jsp.core.text.IJSPPartitions;
 import org.eclipse.jst.jsp.ui.StructuredTextViewerConfigurationJSP;
 import org.eclipse.jst.jsp.ui.internal.contentassist.JSPStructuredContentAssistProcessor;
 import org.eclipse.jst.jsp.ui.internal.style.jspel.LineStyleProviderForJSPEL;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.sse.ui.internal.SSEUIPlugin;
 import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;
 import org.eclipse.wst.sse.ui.internal.taginfo.TextHoverManager;
 import org.jboss.tools.common.text.xml.contentassist.ProposalSorter;
import org.jboss.tools.common.text.xml.xpl.MarkerProblemAnnotationHoverProcessor;
 
 /**
  * @author Igels
  */
 @SuppressWarnings("restriction")
 public class JSPTextViewerConfiguration extends StructuredTextViewerConfigurationJSP implements ITextViewerConfiguration {
 	private static final char[] PROPOSAL_AUTO_ACTIVATION_CHARS = new char[] {
 		'<', '=', '"', '\'', '.', '{'
 	};
 
 	private TextViewerConfigurationDelegate configurationDelegate;
 
 	public JSPTextViewerConfiguration() {
 		super();
 		configurationDelegate = new TextViewerConfigurationDelegate(this);
 	}
 
 
 	/*
 	 * JBIDE-4390:  
 	 * The line style provider for partition type == "org.eclipse.jst.jsp.SCRIPT.JSP_EL2", 
 	 * which is ommitted (forgotten ?) in superclass
 	 */
 	private LineStyleProvider fLineStyleProviderForJSPEL2;
 
 	/*
 	 * JBIDE-4390:  
 	 * The method is overriden to provide a line style provider for partition type == "org.eclipse.jst.jsp.SCRIPT.JSP_EL2", 
 	 * which is ommitted (forgotten ?) in superclass
 	 */
 	private LineStyleProvider getLineStyleProviderForJSPEL2() {
 		if (fLineStyleProviderForJSPEL2 == null) {
 			fLineStyleProviderForJSPEL2 = new LineStyleProviderForJSPEL();
 		}
 		return fLineStyleProviderForJSPEL2;
 	}
 
 	/*
 	 * JBIDE-4390:  
 	 * The method is overriden to provide a line style provider for partition type == "org.eclipse.jst.jsp.SCRIPT.JSP_EL2", 
 	 * which is ommitted (forgotten ?) in superclass
 	 * 
 	 * 
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.jsp.ui.StructuredTextViewerConfigurationJSP#getLineStyleProviders(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
 	 */
 	public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
 		LineStyleProvider[] providers = null;
 
 		if (partitionType == IJSPPartitions.JSP_DEFAULT_EL2) {
 			providers = new LineStyleProvider[]{getLineStyleProviderForJSPEL2()};
 		} 
 		else {
 			providers = super.getLineStyleProviders(sourceViewer, partitionType);
 		}
 
 		return providers;
 	}
 
 	protected IContentAssistProcessor[] getContentAssistProcessors(ISourceViewer sourceViewer, String partitionType) {
 //		IContentAssistProcessor[] superProcessors = super.getContentAssistProcessors(
 //				sourceViewer, partitionType);
 		IContentAssistProcessor superProcessor = new JSPStructuredContentAssistProcessor(
 				this.getContentAssistant(), partitionType, sourceViewer) {
 
 					@SuppressWarnings({ "rawtypes", "unchecked" })
 					@Override
 					protected List filterAndSortProposals(List proposals,
 							IProgressMonitor monitor,
 							CompletionProposalInvocationContext context) {
 						return ProposalSorter.filterAndSortProposals(proposals, monitor, context);
 					}
 
 					@Override
 					public char[] getCompletionProposalAutoActivationCharacters() {
 						char[] superAutoActivationCharacters = super.getCompletionProposalAutoActivationCharacters();
 						if (superAutoActivationCharacters == null)
 							return PROPOSAL_AUTO_ACTIVATION_CHARS;
 						
 						String chars = new String(superAutoActivationCharacters);
 						for (char ch : PROPOSAL_AUTO_ACTIVATION_CHARS) {
 							if (chars.indexOf(ch) == -1) {
 								chars += ch;
 							}
 						}
 						return chars.toCharArray();
 					}
 		};
 		
 		List<IContentAssistProcessor> processors = new ArrayList<IContentAssistProcessor>();
 		processors.addAll(
 				Arrays.asList(
 						configurationDelegate.getContentAssistProcessors(
 								sourceViewer,
 								partitionType)));
 //		processors.addAll(Arrays.asList(superProcessors));
 		processors.add(superProcessor);
 		return processors.toArray(new IContentAssistProcessor[0]);
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
 	 * @since 3.1
 	 */
 	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
 		return configurationDelegate.getHyperlinkDetectors(
 				sourceViewer,
 				fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED));		
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public IContentAssistProcessor[] getContentAssistProcessorsForPartitionType(
 			ISourceViewer sourceViewer, String partitionType) {
 		// TODO Auto-generated method stub
 //		return super.getContentAssistProcessors(sourceViewer, partitionType);
 		return new IContentAssistProcessor[0];
 	}
 	
 	@Override
 	public ITextHover getTextHover(ISourceViewer sourceViewer,	String contentType, int stateMask) {
 		TextHoverManager.TextHoverDescriptor[] hoverDescs = SSEUIPlugin.getDefault().getTextHoverManager().getTextHovers();
 		int i = 0;
 		while (i < hoverDescs.length) {
 			if (hoverDescs[i].isEnabled() && computeStateMask(hoverDescs[i].getModifierString()) == stateMask) {
 				String hoverType = hoverDescs[i].getId();
 				if (TextHoverManager.COMBINATION_HOVER.equalsIgnoreCase(hoverType)){
 					return new MarkerProblemAnnotationHoverProcessor();
 				}
 			}
 			i++;
 		}
 		
 		return super.getTextHover(sourceViewer, contentType, stateMask);
 	}
 
 }
