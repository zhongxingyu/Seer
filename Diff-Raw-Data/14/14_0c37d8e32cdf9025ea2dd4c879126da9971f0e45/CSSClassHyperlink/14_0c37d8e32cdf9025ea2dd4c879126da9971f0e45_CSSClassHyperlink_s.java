 /*******************************************************************************
  * Copyright (c) 2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.text.ext.hyperlink;
 
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleRule;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleSheet;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.text.ext.ExtensionsPlugin;
 import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
 import org.jboss.tools.common.text.ext.hyperlink.xpl.Messages;
 import org.jboss.tools.common.text.ext.util.StructuredModelWrapper;
 import org.jboss.tools.common.text.ext.util.StructuredSelectionHelper;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.jst.web.kb.ICSSContainerSupport;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.PageContextFactory.CSSStyleSheetDescriptor;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.css.CSSRule;
 import org.w3c.dom.css.CSSRuleList;
 
 /**
  * @author Jeremy
  */
 @SuppressWarnings("restriction")
 public class CSSClassHyperlink extends AbstractHyperlink {
 
 	public static final String[] STYLE_TAGS = new String[] { "style", "link" }; //$NON-NLS-1$//$NON-NLS-2$
 	public static final String LINK_TAG = "link"; //$NON-NLS-1$
 	public static final String HREF_ATTRIBUTE = "href"; //$NON-NLS-1$
 	public static final String COMPARE_CLASS_REGEX_PREFIX = "([A-Za-z_][A-Za-z_0-9\\-]*)*[\\.]?"; //$NON-NLS-1$
 	public static final String CONTEXT_PATH_EXPRESSION = "^\\s*(\\#|\\$)\\{facesContext.externalContext.requestContextPath\\}"; //$NON-NLS-1$
 	
 	@Override
 	protected void doHyperlink(IRegion region) {
 		ICSSContainerSupport cssContainerSupport = null;
 		ELContext context = PageContextFactory.createPageContext(getFile());
 		if (!(context instanceof ICSSContainerSupport)) {
 			openFileFailed();
 			return;
 		}
 		cssContainerSupport = (ICSSContainerSupport)context;
 		List<CSSStyleSheetDescriptor> descrs = cssContainerSupport.getCSSStyleSheetDescriptors();
 
 		for (int i = (descrs == null) ? -1 : descrs.size() - 1; descrs != null && i >= 0; i--) {
 			CSSStyleSheetDescriptor descr = descrs.get(i);
 			CSSRuleList rules = descr.sheet.getCssRules();
 			for (int r = 0; rules != null && r < rules.getLength(); r++) {
 				if (isRuleMatch(rules.item(r), getStyleName(region))) {
 					CSSRule rule = rules.item(r);
 					IFile file = findFileForCSSStyleSheet(descr.getFilePath());
 					if (file != null) {
 						int startOffset = 0;
 						if (descr.sheet.getOwnerNode() != null) {
 							Node node = descr.sheet.getOwnerNode().getFirstChild();
 							if (node instanceof IndexedRegion) {
 								startOffset = ((IndexedRegion)node).getStartOffset();
 							}
 						}
 						showRegion(
 								file, 
 								new Region(startOffset + ((IndexedRegion)rule).getStartOffset(), ((IndexedRegion)rule).getLength()));
 						return;
 					}
 				}
 			}
 		}
 		openFileFailed();
 	}
 
 	/*
 	 * Finds a file representing the specified stylesheet
 	 * 
 	 * Three kinds of filePath values are tested:
 	 * - workspace related full path (Comes for a stylesheet defined in STYLE tag. Actually it is a path to the page where the STYLE tag is used)
 	 * - full file path to the CSS-file within the project
 	 * - relative file path
 	 * 
 	 * @param filePath
 	 * @return
 	 */
 	private IFile findFileForCSSStyleSheet(String filePath) {
 		// First try to find a file by WS-related path (because it's the most longest path)
 		IFile file = ResourcesPlugin.getWorkspace().getRoot()
 		.getFileForLocation(
 				ResourcesPlugin.getWorkspace().getRoot().
 					getLocation().append(filePath));
 		
 		return file != null ? file : PageContextFactory.getFileFromProject(filePath, getFile());
 
 	}
 	
 	/**
 	 * 
 	 * @param cssRule
 	 * @param styleName
 	 * @return
 	 */
 	protected boolean isRuleMatch(CSSRule cssRule, String styleName) {
 
 		// get selector text
 		String selectorText = ((ICSSStyleRule) cssRule).getSelectorText();
 
 		if (selectorText != null) {
 			String styles[] = selectorText.trim().split(","); //$NON-NLS-1$
 			for (String styleText : styles) {
 				String[] styleWords = styleText.trim().split(" ");  //$NON-NLS-1$
 				if (styleWords != null) {
 					int searchIndex = Arrays.binarySearch(styleWords, styleName,
 							new Comparator<String>() {
 		
 								public int compare(String o1, String o2) {
 									Matcher matcher = Pattern.compile(
 											COMPARE_CLASS_REGEX_PREFIX + o2)
 											.matcher(o1);
 									
 									return matcher.matches() ? 0 : 1;
 								}
 		
 							});
 					if (searchIndex >= 0)
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param cssRule
 	 * @return
 	 */
 	protected Region getRegion(CSSRule cssRule) {
 
 		int offset = ((IndexedRegion) cssRule).getStartOffset();
 
 		// if css rule is contained in style tag so it is require to take into
 		// account offset of this tag
 		ICSSStyleSheet document = (ICSSStyleSheet) ((ICSSStyleRule) cssRule)
 				.getOwnerDocument();
 
 		ICSSModel model = document.getModel();
 
 		// get style tag
 		Node node = model.getOwnerDOMNode();
 
 		// increase offset
 		if (node instanceof IDOMElement)
 			offset += ((IDOMElement) node).getStartEndOffset();
 
 		return new Region(offset, 0);
 
 	}
 
 	/**
 	 * 
 	 * @param styleRegion
 	 */
 	protected void showRegion(IFile file, IRegion region) {
 
 		IWorkbenchPage workbenchPage = ExtensionsPlugin.getDefault()
 				.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IEditorPart part = null;
 		if (file != null) {
 			try {
 				part = IDE.openEditor(workbenchPage, file, true);
 			} catch (PartInitException e) {
 				e.printStackTrace();
 			}
 		}
 		if (part == null) {
 			openFileFailed();
 			return;
 		}
 		StructuredSelectionHelper.setSelectionAndReveal(part,
 				region);
 	}
 	
 	/**
 	 * 
 	 * @param region
 	 * @return
 	 */
 	protected String getStyleName(IRegion region) {
		try {
			return getDocument().get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return null;
 		}
 	}
 
 	protected IRegion fLastRegion = null;
 
 	/**
 	 * @see com.ibm.sse.editor.AbstractHyperlink#doGetHyperlinkRegion(int)
 	 */
 	protected IRegion doGetHyperlinkRegion(int offset) {
 		fLastRegion = getRegion(offset);
 		return fLastRegion;
 	}
 
 	/**
 	 * TODO research method
 	 * 
 	 * @param offset
 	 * @return
 	 */
 	protected IRegion getRegion(int offset) {
 		StructuredModelWrapper smw = new StructuredModelWrapper();
 		try {
 			smw.init(getDocument());
 			Document xmlDocument = smw.getDocument();
 			if (xmlDocument == null)
 				return null;
 
 			Node n = Utils.findNodeForOffset(xmlDocument, offset);
 
 			if (n == null || !(n instanceof Attr))
 				return null;
 			int start = Utils.getValueStart(n);
 			int end = Utils.getValueEnd(n);
 			if (start > offset)
 				return null;
 
 			String attrText = getDocument().get(start, end - start);
 
 			StringBuffer sb = new StringBuffer(attrText);
 			// find start of bean property
 			int bStart = offset - start;
 			while (bStart >= 0) {
 				if (!Character.isJavaIdentifierPart(sb.charAt(bStart))
 						&& sb.charAt(bStart) != '_' && sb.charAt(bStart) != '-'
 						&& sb.charAt(bStart) != '.') {
 					bStart++;
 					break;
 				}
 
 				if (bStart == 0)
 					break;
 				bStart--;
 			}
 			// find end of bean property
 			int bEnd = offset - start;
 			while (bEnd < sb.length()) {
 				if (!Character.isJavaIdentifierPart(sb.charAt(bEnd))
 						&& sb.charAt(bEnd) != '_' && sb.charAt(bEnd) != '-'
 						&& sb.charAt(bEnd) != '.')
 					break;
 				bEnd++;
 			}
 
 			final int propStart = bStart + start;
 			final int propLength = bEnd - bStart;
 
 			if (propStart > offset || propStart + propLength < offset)
 				return null;
 			return new Region(propStart, propLength);
 		} catch (BadLocationException x) {
 			// ignore
 			return null;
 		} finally {
 			smw.dispose();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IHyperlink#getHyperlinkText()
 	 */
 	public String getHyperlinkText() {
 		String styleName = getStyleName(fLastRegion);
 		if (styleName == null)
 			return MessageFormat.format(Messages.OpenA, Messages.CSSStyle);
 
 		return MessageFormat.format(Messages.OpenCSSStyle, styleName);
 	}
 
 	@Override
 	protected String findAndReplaceElVariable(String fileName) {
 		if (fileName != null)
 			fileName = fileName.replaceFirst(CONTEXT_PATH_EXPRESSION,""); //$NON-NLS-1$
 		
 		return super.findAndReplaceElVariable(fileName);
 	}
 }
