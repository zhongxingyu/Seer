 /******************************************************************************* 
  * Copyright (c) 2010-2011 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.jspeditor.info;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.IOpenable;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.ITypeRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.corext.javadoc.JavaDocLocations;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.internal.ui.text.FastJavaPartitionScanner;
 import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
 import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
 import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
 import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks;
 import org.eclipse.jdt.ui.JavaElementLabels;
 import org.eclipse.jdt.ui.PreferenceConstants;
 import org.eclipse.jdt.ui.text.IJavaPartitions;
 import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
 import org.eclipse.jface.internal.text.html.HTMLPrinter;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.swt.graphics.FontData;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELUtil;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolution;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELSegment;
 import org.jboss.tools.common.el.core.resolver.JavaMemberELSegmentImpl;
 import org.jboss.tools.common.el.ui.ca.ELProposalProcessor;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.common.util.StringUtil;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.contentassist.Utils;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.el.MessagePropertyELSegmentImpl;
 import org.osgi.framework.Bundle;
 
 /**
  * 
  * @author Victor Rubezhny
  *
  */
 @SuppressWarnings("restriction")
 public class JavaStringELInfoHover extends JavadocHover {
 //	private IInformationControlCreator fPresenterControlCreator;
 
 	/*
 	 * @see ITextHover#getHoverRegion(ITextViewer, int)
 	 */
 	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
 		return JavaWordFinder.findWord(textViewer.getDocument(), offset);
 	}
 	
 	/*
 	 * @see JavaElementHover
 	 */
 	public String getHoverInfoDepracated(ITextViewer textViewer, IRegion region) {
 		// find a region of __java_string, if we're in it - use it
 		IDocument document = textViewer == null ? null : textViewer.getDocument();
 		if (document == null)
 			return null;
 		
 		int rangeStart = -1;
 		int rangeLength = 0;
 		IToken rangeToken = null;
 		FastJavaPartitionScanner scanner = new FastJavaPartitionScanner();
 		scanner.setRange(document, 0, document.getLength());
 		while(true) {
 			IToken token = scanner.nextToken();
 			if(token == null || token.isEOF()) break;
 			int start = scanner.getTokenOffset();
 			int length = scanner.getTokenLength();
 			int end = start + length;
 			if(start <= region.getOffset() && end >= region.getOffset()) {
 				rangeStart = start;
 				rangeLength = length;
 				rangeToken = token;
 				break;
 			}
 			if(start > region.getOffset()) break;
 		}
 
 		if (rangeToken == null || rangeStart == -1 || rangeLength <=0 ||
 				!IJavaPartitions.JAVA_STRING.equals(rangeToken.getData()))
 			return null;
 
 		// OK. We've found JAVA_STRING token  
 		// Check that the position is in the EL 
 		if (!checkStartPosition(document, region.getOffset()))
 			return null;
 		
 		// Calculate and prepare KB-query parameters
 		String text = null;
 		try {
 			text = document.get(rangeStart, rangeLength);
 		} catch (BadLocationException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		int inValueOffset = region.getOffset() - rangeStart;
 	
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(text);
 		
 		ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 		if (ie == null) 
 			return null;
 
 		String query = "#{" + ie.getText(); //$NON-NLS-1$
 		
 		KbQuery kbQuery = Utils.createKbQuery(Type.ATTRIBUTE_VALUE, region.getOffset() + region.getLength(), 
 				query, query, "", "", null, null, false); //$NON-NLS-1$ //$NON-NLS-2$
 
 		ITypeRoot input= getEditorInputJavaElement();
 		if (input == null)
 			return null;
 		
 		IFile file = null;
 		
 		try {
 			IResource resource = input.getCorrespondingResource();
 			if (resource instanceof IFile)
 				file = (IFile) resource;
 		} catch (JavaModelException e) {
 			// Ignore. It is probably because of Java element's resource is not found 
 		}
 
 		if(file == null) {
 			return null;
 		}
 
 		ELContext context = PageContextFactory.createPageContext(file, JavaCore.JAVA_SOURCE_CONTENT_TYPE);
 		
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, context);
 		if (proposals == null)
 			return null;
 		
 		for(TextProposal proposal : proposals) {
 			String label = proposal == null ? null : proposal.getLabel();
 			label = (label == null || label.indexOf(':') == -1) ? label : label.substring(0, label.indexOf(':')).trim(); 
 			if (label != null && query.endsWith(label) && 
 					proposal != null && proposal.getContextInfo() != null &&
 					proposal.getContextInfo().trim().length() > 0) {
 				return proposal.getContextInfo();
 			}
 		}
 
 		return null;
 	}
 	
     /*
 	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
 	 * @since 3.4
 	 */
 	public Object getHoverInfo2(ITextViewer textViewer, IRegion region) {
 		// find a region of __java_string, if we're in it - use it
 		IDocument document = textViewer == null ? null : textViewer.getDocument();
 		if (document == null)
 			return null;
 		
 		int rangeStart = -1;
 		int rangeLength = 0;
 		IToken rangeToken = null;
 		FastJavaPartitionScanner scanner = new FastJavaPartitionScanner();
 		scanner.setRange(document, 0, document.getLength());
 		while(true) {
 			IToken token = scanner.nextToken();
 			if(token == null || token.isEOF()) break;
 			int start = scanner.getTokenOffset();
 			int length = scanner.getTokenLength();
 			int end = start + length;
 			if(start <= region.getOffset() && end >= region.getOffset()) {
 				rangeStart = start;
 				rangeLength = length;
 				rangeToken = token;
 				break;
 			}
 			if(start > region.getOffset()) break;
 		}
 
 		if (rangeToken == null || rangeStart == -1 || rangeLength <=0 ||
 				!IJavaPartitions.JAVA_STRING.equals(rangeToken.getData()))
 			return null;
 
 		// OK. We've found JAVA_STRING token  
 		// Check that the position is in the EL 
 		if (!checkStartPosition(document, region.getOffset()))
 			return null;
 		
 		// Calculate and prepare KB-query parameters
 		String text = null;
 		try {
 			text = document.get(rangeStart, rangeLength);
 		} catch (BadLocationException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		int inValueOffset = region.getOffset() - rangeStart;
 	
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(text);
 		
 		ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 		if (ie == null) 
 			return null;
 
 		ITypeRoot input= getEditorInputJavaElement();
 		if (input == null)
 			return null;
 		
 		IResource r = input.getResource();
 		if(!(r instanceof IFile) || !r.exists() || r.getName().endsWith(".jar")) { //$NON-NLS-1$
 			return null;
 		}
 		IFile file = (IFile)r;
 
 		ELContext context = PageContextFactory.createPageContext(file, JavaCore.JAVA_SOURCE_CONTENT_TYPE);
 		
 		ELResolver[] resolvers =  context.getElResolvers();
 		
 		for (int i = 0; resolvers != null && i < resolvers.length; i++) {
 			ELResolution resolution = resolvers[i] == null ? null : resolvers[i].resolve(context, ie, region.getOffset() + region.getLength());
 			if (resolution == null)
 				continue;
 			
 			ELSegment segment = resolution.getLastSegment();
 			if (segment==null || !segment.isResolved()) continue;
 			
 			if(segment instanceof JavaMemberELSegmentImpl) {
 				JavaMemberELSegmentImpl jmSegment = (JavaMemberELSegmentImpl)segment;
 				
 				IJavaElement[] javaElements = jmSegment.getAllJavaElements();
 				if (javaElements == null || javaElements.length == 0) {
 					if (jmSegment.getJavaElement() == null)
 						continue;
 					
 					javaElements = new IJavaElement[] {jmSegment.getJavaElement()};
 				}
 				if (javaElements == null || javaElements.length == 0)
 					continue;
 				
 				return JavaStringELInfoHover.getHoverInfo2Internal(javaElements, true);
 			} else if (segment instanceof MessagePropertyELSegmentImpl) {
 				MessagePropertyELSegmentImpl mpSegment = (MessagePropertyELSegmentImpl)segment;
 				String baseName = mpSegment.getBaseName();
 				String propertyName = mpSegment.isBundle() ? null : StringUtil.trimQuotes(segment.getToken().getText());
 				String hoverText = ELProposalProcessor.getELMessagesHoverInternal(baseName, propertyName, (List<XModelObject>)mpSegment.getObjects());
 				StringBuffer buffer = new StringBuffer(hoverText);
 
 				HTMLPrinter.insertPageProlog(buffer, 0, getStyleSheet());
 				HTMLPrinter.addPageEpilog(buffer);
 				
 				return new ELInfoHoverBrowserInformationControlInput(null, new IJavaElement[0], buffer.toString(), 0);
 			}
 		}
 
 		return null;
 	}
 
 	/*
 	 * Checks if the EL start starting characters are present
 	 * @param viewer
 	 * @param offset
 	 * @return
 	 * @throws BadLocationException
 	 */
 	private boolean checkStartPosition(IDocument document, int offset) {
 		try {
 			while (--offset >= 0) {
 				if ('}' == document.getChar(offset))
 					return false;
 
 
 				if ('"' == document.getChar(offset) &&
 						(offset - 1) >= 0 && '\\' != document.getChar(offset - 1)) {
 					return false;
 				}
 
 
 				if ('{' == document.getChar(offset) &&
 						(offset - 1) >= 0 && 
 						('#' == document.getChar(offset - 1) || 
 								'$' == document.getChar(offset - 1))) {
 					return true;
 				}
 			}
 		} catch (BadLocationException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		return false;
 	}
 	
 	public static ELInfoHoverBrowserInformationControlInput getHoverInfo2Internal(IJavaElement[] elements, boolean useFullHTML) {
 		int nResults= elements.length;
 		StringBuffer buffer= new StringBuffer();
 		boolean hasContents= false;
 		String base= null;
 		IJavaElement element= null;
 
 		int leadingImageWidth= 0;
 
 		if (nResults > 1) {
 			for (int i= 0; i < elements.length; i++) {
 				if (useFullHTML) {
 					HTMLPrinter.startBulletList(buffer);
 				}
 				if (elements[i] == null) continue;
 				if (elements[i] instanceof IMember || 
 						elements[i].getElementType() == IJavaElement.LOCAL_VARIABLE || 
 						elements[i].getElementType() == IJavaElement.TYPE_PARAMETER) {
 					if (useFullHTML) {
 						HTMLPrinter.addBullet(buffer, getInfoText(elements[i], false, useFullHTML));
 					} else {
 						buffer.append('\u002d').append(' ').append(getInfoText(elements[i], false, useFullHTML));
 					}
 					hasContents= true;
 				}
 				if (useFullHTML) {
 					HTMLPrinter.endBulletList(buffer);
 				} else {
 					buffer.append("<br/>"); //$NON-NLS-1$
 				}
 			}
 
 			for (int i=0; i < elements.length; i++) {
 				if (elements[i] == null) continue;
 				if (elements[i] instanceof IMember || 
 						elements[i].getElementType() == IJavaElement.LOCAL_VARIABLE || 
 						elements[i].getElementType() == IJavaElement.TYPE_PARAMETER) {
 					if (!useFullHTML) {
 						buffer.append("<br/>"); //$NON-NLS-1$
 					}
 					base = addFullInfo(buffer, elements[i], useFullHTML);
 					hasContents = true;
 				}
 			}
 		} else {
 			element= elements[0];
 			if (element instanceof IMember ||
 					element.getElementType() == IJavaElement.LOCAL_VARIABLE || 
 					element.getElementType() == IJavaElement.TYPE_PARAMETER) {
 				base = addFullInfo(buffer, element, useFullHTML);
 				hasContents= true;
 			}
 			leadingImageWidth= 20;
 		}
 
 		if (!hasContents)
 			return null;
 
 		if (buffer.length() > 0) {
 			HTMLPrinter.insertPageProlog(buffer, 0, useFullHTML ? getStyleSheet() : null);
 			if (base != null) {
 				int endHeadIdx= buffer.indexOf("</head>"); //$NON-NLS-1$
 				if (endHeadIdx != -1) {
 					buffer.insert(endHeadIdx, "\n<base href='" + base + "'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 			HTMLPrinter.addPageEpilog(buffer);
 			
 			return new ELInfoHoverBrowserInformationControlInput(null, elements, buffer.toString(), leadingImageWidth);
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Adds full information to the hover
 	 * Returns base URL if exists
 	 * 
 	 * @param buffer
 	 * @param element
 	 * @return
 	 */
 	private static String addFullInfo(StringBuffer buffer, IJavaElement element, boolean useFullHTML) {
 		String base= null;
 
 		if (element instanceof IMember) {
 			IMember member= (IMember) element;
 			HTMLPrinter.addSmallHeader(buffer, getInfoText(member, true, useFullHTML));
 			Reader reader;
 			try {
 				String content= JavadocContentAccess2.getHTMLContent(member, true);
 				reader= content == null ? null : new StringReader(content);
 
 				// Provide hint why there's no Javadoc
 				if (reader == null && member.isBinary()) {
 					boolean hasAttachedJavadoc= JavaDocLocations.getJavadocBaseLocation(member) != null;
 					IPackageFragmentRoot root= (IPackageFragmentRoot)member.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
 					boolean hasAttachedSource= root != null && root.getSourceAttachmentPath() != null;
 					IOpenable openable= member.getOpenable();
 					boolean hasSource= openable.getBuffer() != null;
 
 					if (!hasAttachedSource && !hasAttachedJavadoc)
 						reader= new StringReader(ELInfoHoverMessages.ELInfoHover_noAttachments);
 					else if (!hasAttachedJavadoc && !hasSource)
 						reader= new StringReader(ELInfoHoverMessages.ELInfoHover_noAttachedJavadoc);
 					else if (!hasAttachedSource)
 						reader= new StringReader(ELInfoHoverMessages.ELInfoHover_noAttachedJavaSource);
 					else if (!hasSource)
 						reader= new StringReader(ELInfoHoverMessages.ELInfoHover_noInformation);
 
 				} else {
					base= JavaDocLocations.getBaseURL(member, member.isBinary());
 				}
 
 			} catch (JavaModelException ex) {
 				reader= new StringReader(ELInfoHoverMessages.ELInfoHover_error_gettingJavadoc);
 				JavaPlugin.log(ex);
 			}
 
 			if (reader != null) {
 				HTMLPrinter.addParagraph(buffer, reader);
 			}
 
 		} else if (element.getElementType() == IJavaElement.LOCAL_VARIABLE || element.getElementType() == IJavaElement.TYPE_PARAMETER) {
 			HTMLPrinter.addSmallHeader(buffer, getInfoText(element, true, useFullHTML));
 		}
 		return base;
 	}
 	
 	private static final long LABEL_FLAGS=  JavaElementLabels.ALL_FULLY_QUALIFIED
 			| JavaElementLabels.M_PRE_RETURNTYPE | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES | JavaElementLabels.M_EXCEPTIONS
 			| JavaElementLabels.F_PRE_TYPE_SIGNATURE | JavaElementLabels.M_PRE_TYPE_PARAMETERS | JavaElementLabels.T_TYPE_PARAMETERS
 			| JavaElementLabels.USE_RESOLVED;
 	private static final long LOCAL_VARIABLE_FLAGS= LABEL_FLAGS & ~JavaElementLabels.F_FULLY_QUALIFIED | JavaElementLabels.F_POST_QUALIFIED;
 	private static final long TYPE_PARAMETER_FLAGS= LABEL_FLAGS | JavaElementLabels.TP_POST_QUALIFIED;
 
 	private static String getInfoText(IJavaElement element, boolean allowImage, boolean useFullHTML) {
 		long flags;
 		switch (element.getElementType()) {
 			case IJavaElement.LOCAL_VARIABLE:
 				flags= LOCAL_VARIABLE_FLAGS;
 				break;
 			case IJavaElement.TYPE_PARAMETER:
 				flags= TYPE_PARAMETER_FLAGS;
 				break;
 			default:
 				flags= LABEL_FLAGS;
 				break;
 		}
 		StringBuffer label= new StringBuffer(JavaElementLinks.getElementLabel(element, flags));
 
 		String imageName= null;
 		if (allowImage) {
 			URL imageUrl= JavaPlugin.getDefault().getImagesOnFSRegistry().getImageURL(element);
 			if (imageUrl != null) {
 				imageName= imageUrl.toExternalForm();
 			}
 		}
 
 		StringBuffer buf= new StringBuffer();
 		addImageAndLabel(buf, imageName, 16, 16, 2, 2, label.toString(), 2, 2, useFullHTML);
 		return buf.toString();
 	}
 		
 	/**
 	 * The style sheet (css).
 	 * @since 3.4
 	 */
 	private static String fgStyleSheet;
 
 	/**
 	 * Returns the Javadoc hover style sheet with the current Javadoc font from the preferences.
 	 * @return the updated style sheet
 	 * @since 3.4
 	 */
 	private static String getStyleSheet() {
 		if (fgStyleSheet == null)
 			fgStyleSheet= loadStyleSheet();
 		String css= fgStyleSheet;
 		if (css != null) {
 			FontData fontData= JFaceResources.getFontRegistry().getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
 			css= HTMLPrinter.convertTopLevelFont(css, fontData);
 		}
 
 		return css;
 	}
 
 	/**
 	 * Loads and returns the Javadoc hover style sheet.
 	 * @return the style sheet, or <code>null</code> if unable to load
 	 * @since 3.4
 	 */
 	private static String loadStyleSheet() {
 		Bundle bundle= Platform.getBundle(JavaPlugin.getPluginId());
 		URL styleSheetURL= bundle.getEntry("/JavadocHoverStyleSheet.css"); //$NON-NLS-1$
 		if (styleSheetURL != null) {
 			BufferedReader reader= null;
 			try {
 				reader= new BufferedReader(new InputStreamReader(styleSheetURL.openStream()));
 				StringBuffer buffer= new StringBuffer(1500);
 				String line= reader.readLine();
 				while (line != null) {
 					buffer.append(line);
 					buffer.append('\n');
 					line= reader.readLine();
 				}
 				return buffer.toString();
 			} catch (IOException ex) {
 				JavaPlugin.log(ex);
 				return ""; //$NON-NLS-1$
 			} finally {
 				try {
 					if (reader != null)
 						reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return null;
 	}
 
 	public static void addImageAndLabel(StringBuffer buf, String imageName, int imageWidth, int imageHeight, int imageLeft, int imageTop, String label, int labelLeft, int labelTop, boolean useFullHTML) {
 
 		if (imageName != null) {
 			StringBuffer imageStyle= new StringBuffer("position: relative; "); //$NON-NLS-1$
 			imageStyle.append("width: ").append(imageWidth).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
 			imageStyle.append("height: ").append(imageHeight).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
 			if (imageTop != -1)
 				imageStyle.append("top: ").append(imageTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
 			if (imageLeft != -1)
 				imageStyle.append("left: ").append(imageLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
 
 			if (useFullHTML) {
 				buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n"); //$NON-NLS-1$
 				buf.append("<span style=\"").append(imageStyle).append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='").append(imageName).append("')\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				buf.append("<![endif]><![endif]-->\n"); //$NON-NLS-1$
 	
 				buf.append("<!--[if !IE]>-->\n"); //$NON-NLS-1$
 				buf.append("<img style='").append(imageStyle).append("' src='").append(imageName).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				buf.append("<!--<![endif]-->\n"); //$NON-NLS-1$
 				buf.append("<!--[if gte IE 7]>\n"); //$NON-NLS-1$
 				buf.append("<img style='").append(imageStyle).append("' src='").append(imageName).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				buf.append("<![endif]-->\n"); //$NON-NLS-1$
 			} else {
 				buf.append("<img style='").append(imageStyle).append("' src='").append(imageName).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 		}
 
 		buf.append("<span style='word-wrap:break-word;"); //$NON-NLS-1$
 		if (imageName != null) {
 			buf.append("margin-left: ").append(labelLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
 			buf.append("margin-top: ").append(labelTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		buf.append("'>"); //$NON-NLS-1$
 		buf.append(label);
 		buf.append("</span>"); //$NON-NLS-1$
 		if (imageName != null) {
 			buf.append("</span>"); //$NON-NLS-1$
 		}
 	}
 
 }
 
 
 @SuppressWarnings("restriction")
 class ELInfoHoverBrowserInformationControlInput extends BrowserInformationControlInput {
 	private final IJavaElement[] fElements;
 	private final String fHtml;
 	private final int fLeadingImageWidth;
 
 	/**
 	 * Creates a new browser information control input.
 	 *
 	 * @param previous previous input, or <code>null</code> if none available
 	 * @param element the element, or <code>null</code> if none available
 	 * @param html HTML contents, must not be null
 	 * @param leadingImageWidth the indent required for the element image
 	 */
 	public ELInfoHoverBrowserInformationControlInput(BrowserInformationControlInput previous, IJavaElement[] elements, String html, int leadingImageWidth) {
 		super(previous);
 		Assert.isNotNull(html);
 		fElements= elements;
 		fHtml= html;
 		fLeadingImageWidth= leadingImageWidth;
 	}
 
 	/*
 	 * @see org.eclipse.jface.internal.text.html.BrowserInformationControlInput#getLeadingImageWidth()
 	 * @since 3.4
 	 */
 	public int getLeadingImageWidth() {
 		return fLeadingImageWidth;
 	}
 
 	/**
 	 * Returns the Java element.
 	 *
 	 * @return the element or <code>null</code> if none available
 	 */
 	public IJavaElement[] getElements() {
 		return fElements;
 	}
 
 	/*
 	 * @see org.eclipse.jface.internal.text.html.BrowserInput#getHtml()
 	 */
 	public String getHtml() {
 		return fHtml;
 	}
 
 	@Override
 	public Object getInputElement() {
 		return fElements == null ? (Object) fHtml : fElements;
 	}
 
 	private String fInputName = null;
 	@Override
 	public String getInputName() {
 		if (fInputName != null)
 			return fInputName;
 		
 		String inputName = ""; //$NON-NLS-1$
 		for (int i = 0; fElements != null && i < fElements.length; i++) {
 			if (i > 0 && inputName.trim().length() > 0)
 				inputName += " & "; //$NON-NLS-1$
 			inputName += fElements[i].getElementName();
 		}
 		fInputName = inputName;
 		
 		return fInputName;
 	}
 	
 }
