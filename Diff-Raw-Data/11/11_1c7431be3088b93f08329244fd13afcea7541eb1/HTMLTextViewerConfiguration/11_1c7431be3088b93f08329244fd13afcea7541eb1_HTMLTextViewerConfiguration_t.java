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
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.text.ITextHover;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.formatter.IContentFormatter;
 import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
 import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
 import org.eclipse.jface.text.information.IInformationProvider;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
 import org.eclipse.wst.html.core.text.IHTMLPartitions;
 import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
 import org.eclipse.wst.html.ui.internal.contentassist.HTMLStructuredContentAssistProcessor;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.sse.ui.internal.ExtendedConfigurationBuilder;
 import org.eclipse.wst.sse.ui.internal.SSEUIPlugin;
 import org.eclipse.wst.sse.ui.internal.format.StructuredFormattingStrategy;
 import org.eclipse.wst.sse.ui.internal.taginfo.AnnotationHoverProcessor;
 import org.eclipse.wst.sse.ui.internal.taginfo.ProblemAnnotationHoverProcessor;
 import org.eclipse.wst.sse.ui.internal.taginfo.TextHoverManager;
 import org.jboss.tools.common.text.xml.contentassist.ProposalSorter;
import org.jboss.tools.common.text.xml.xpl.MarkerProblemAnnotationHoverProcessor;
 import org.jboss.tools.jst.jsp.format.HTMLFormatProcessor;
 import org.jboss.tools.jst.jsp.jspeditor.info.ChainTextHover;
 
 @SuppressWarnings("restriction")
 public class HTMLTextViewerConfiguration extends
 		StructuredTextViewerConfigurationHTML implements
 		ITextViewerConfiguration {
 
 	private static final char[] PROPOSAL_AUTO_ACTIVATION_CHARS = new char[] {
 		'<', '=', '"', '\'', '.', '{'
 	};
 
 	TextViewerConfigurationDelegate configurationDelegate;
 
 	private static final String TEMPLATES_CONTENT_ASSISTANT = "org.jboss.tools.jst.jsp.editorContentAssistent"; //$NON-NLS-1$
 	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
 
 	public HTMLTextViewerConfiguration() {
 		super();
 		configurationDelegate = new TextViewerConfigurationDelegate(this);
 	}
 
 	protected IContentAssistProcessor[] getContentAssistProcessors(
 			ISourceViewer sourceViewer, String partitionType) {
 //		IContentAssistProcessor[] superProcessors = super.getContentAssistProcessors(
 //		sourceViewer, partitionType);
 		IContentAssistProcessor superProcessor = new HTMLStructuredContentAssistProcessor(
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
 	 * @see
 	 * org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors
 	 * (org.eclipse.jface.text.source.ISourceViewer)
 	 * 
 	 * @since 3.1
 	 */
 	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
 		return configurationDelegate
 				.getHyperlinkDetectors(
 						sourceViewer,
 						fPreferenceStore
 								.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML#
 	 * getContentFormatter(org.eclipse.jface.text.source.ISourceViewer)
 	 */
 	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
 		MultiPassContentFormatter formatter = new MultiPassContentFormatter(
 				getConfiguredDocumentPartitioning(sourceViewer),
 				IHTMLPartitions.HTML_DEFAULT);
 		formatter.setMasterStrategy(new StructuredFormattingStrategy(
 				new HTMLFormatProcessor()));
 		return formatter;
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public IContentAssistProcessor[] getContentAssistProcessorsForPartitionType(
 			ISourceViewer sourceViewer, String partitionType) {
 //		IContentAssistProcessor[] results = super.getContentAssistProcessors(
 //				sourceViewer, partitionType);
 		// added by Maksim Areshkau
 		// TODO: create a CA computer and move it to 
 		//	org.eclipse.wst.sse.ui.completionProposal extension point
 		if ("org.eclipse.wst.html.HTML_DEFAULT".equalsIgnoreCase(partitionType)) { //$NON-NLS-1$
 			List<IContentAssistProcessor> contAssists = getVpeTestExtensions();
 			return contAssists.toArray(new IContentAssistProcessor[0]);
 		}
 		return new IContentAssistProcessor[0];
 	}
 
 	/**
 	 * Create documentation hovers based on hovers contributed via
 	 * <code>org.eclipse.wst.sse.ui.editorConfiguration</code> extension point
 	 * 
 	 * Copied from
 	 * {@link org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration} because
 	 * of private modifier
 	 * 
 	 * @param partitionType
 	 * @return
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private ITextHover[] createDocumentationHovers(String partitionType) {
 		List extendedTextHover = ExtendedConfigurationBuilder.getInstance()
 				.getConfigurations(
 						ExtendedConfigurationBuilder.DOCUMENTATIONTEXTHOVER,
 						partitionType);
 		ITextHover[] hovers = (ITextHover[]) extendedTextHover
 				.toArray(new ITextHover[extendedTextHover.size()]);
 		return hovers;
 	}
 
 	@Override
 	protected IInformationProvider getInformationProvider(
 			ISourceViewer sourceViewer, String partitionType) {
 
 		ITextHover chainTextHover = new ChainTextHover(
 				createDocumentationHovers(partitionType));
 		return new TextHoverInformationProvider(chainTextHover);
 	}
 
 	@Override
 	public ITextHover getTextHover(ISourceViewer sourceViewer,
 			String contentType, int stateMask) {
 		ITextHover textHover = null;
 
 		/*
 		 * Returns a default problem, annotation, and best match hover depending
 		 * on stateMask
 		 */
 		TextHoverManager.TextHoverDescriptor[] hoverDescs = SSEUIPlugin
 				.getDefault().getTextHoverManager().getTextHovers();
 		int i = 0;
 		while (i < hoverDescs.length && textHover == null) {
 			if (hoverDescs[i].isEnabled()
 					&& computeStateMask(hoverDescs[i].getModifierString()) == stateMask) {
 				String hoverType = hoverDescs[i].getId();
 				if (TextHoverManager.PROBLEM_HOVER.equalsIgnoreCase(hoverType))
 					textHover = new ProblemAnnotationHoverProcessor();
 				else if (TextHoverManager.ANNOTATION_HOVER
 						.equalsIgnoreCase(hoverType))
 					textHover = new AnnotationHoverProcessor();
 				else if (TextHoverManager.COMBINATION_HOVER
 						.equalsIgnoreCase(hoverType))
 					return new MarkerProblemAnnotationHoverProcessor();
 				else if (TextHoverManager.DOCUMENTATION_HOVER
 						.equalsIgnoreCase(hoverType)) {
 					ITextHover[] hovers = createDocumentationHovers(contentType);
 					if (hovers.length > 0) {
 						textHover = hovers[0];
 					}
 				}
 			}
 			i++;
 		}
 		return textHover;
 	}
 
 	/**
 	 * @deprecated
 	 * 
 	 * Returns all extensions of {@value #VPE_TEST_EXTENTION_POINT_ID}
 	 */
 	public List<IContentAssistProcessor> getVpeTestExtensions() {
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = extensionRegistry
 				.getExtensionPoint(TEMPLATES_CONTENT_ASSISTANT);
 		IExtension[] extensions = extensionPoint.getExtensions();
 		List<IContentAssistProcessor> contentAssisteProcessors = new ArrayList<IContentAssistProcessor>();
 		for (IExtension extension : extensions) {
 			IConfigurationElement[] confElements = extension
 					.getConfigurationElements();
 			for (IConfigurationElement configurationElement : confElements) {
 				IContentAssistProcessor contentAssistProcessor;
 				try {
 					contentAssistProcessor = (IContentAssistProcessor) configurationElement
 							.createExecutableExtension(CLASS_ATTRIBUTE);
 					contentAssisteProcessors.add(contentAssistProcessor);
 				} catch (CoreException e) {
 					JspEditorPlugin.getPluginLog().logError(e);
 				}
 			}
 		}
 		return contentAssisteProcessors;
 	}
 }
