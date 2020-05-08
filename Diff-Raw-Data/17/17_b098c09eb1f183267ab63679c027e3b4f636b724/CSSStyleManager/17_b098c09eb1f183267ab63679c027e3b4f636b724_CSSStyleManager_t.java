 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.css.common;
 
import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.wst.css.core.internal.provisional.adapters.IStyleSheetAdapter;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSDocument;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSNode;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
 import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
 import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
 import org.jboss.tools.jst.css.dialog.common.CSSConstants;
 import org.jboss.tools.jst.css.dialog.common.Util;
 import org.jboss.tools.jst.jsp.util.Constants;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 import org.w3c.dom.css.CSSStyleRule;
 import org.w3c.dom.css.CSSStyleSheet;
 import org.w3c.dom.css.ElementCSSInlineStyle;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class CSSStyleManager.
  *
  * @author Sergey Dzmitrovich
  */
 public class CSSStyleManager {
 
 	/** The Constant STYLE_TAG_NAME. */
 	public static final String STYLE_TAG_NAME = "style"; //$NON-NLS-1$
 
 	/** The Constant STYLE_ATTRIBUTE_NAME. */
 	public static final String STYLE_ATTRIBUTE_NAME = "style"; //$NON-NLS-1$
 
 	/**
 	 * Recognize css style.
 	 *
 	 * @param node the selected node
 	 * @return the style container
 	 */
 	public static StyleContainer recognizeCSSStyle(Object node) {
 		StyleContainer container = null;
 		// if selected object is node in css file
 		if (node instanceof ICSSNode) {
 			CSSStyleRule styleRule = getStyleRule((ICSSNode) node);
 			if (styleRule != null)
 				container = new CSSStyleRuleContainer(styleRule);
 		} else if ((node instanceof Element)
 				|| (node instanceof Attr)) {
 			Element selectedElement = null;
 			if (node instanceof Attr) {
 				selectedElement = ((Attr) node).getOwnerElement();
 			} else {
 				selectedElement = (Element) node;
 			}
 			if (isSuitableElement(selectedElement)) {
 				container = new StyleAttribyteContainer(selectedElement);
 			}
 		}
 		return container;
 	}
 	
 	/**
 	 * Recognize css style.
 	 *
 	 * @param selection the selection
 	 * @return the style container
 	 */
 	public static StyleContainer recognizeCSSStyle(ISelection selection) {
 		StyleContainer container = null;
 		if (selection instanceof IStructuredSelection) {
 			Object selectedObject = ((IStructuredSelection) selection)
 					.getFirstElement();
 			container = recognizeCSSStyle(selectedObject);
 			/*
 			 * When container was not found and
 			 * the selection is text selection then:
 			 */
 			if ((null == container) && (selectedObject instanceof Text)
 					&& (selection instanceof ITextSelection)) {
 				Text styleText = (Text) selectedObject;
 				Node parentNode = styleText.getParentNode();
 				if ((parentNode != null)
 						&& STYLE_TAG_NAME.equalsIgnoreCase(parentNode
 								.getNodeName())) {
 					int offset = getRelationalOffset(styleText,
 							((ITextSelection) selection).getOffset());
 					CSSStyleSheet sheet = getSheet(parentNode);
 					ICSSNode node = getNode(sheet, offset);
 					CSSStyleRule styleRule = getStyleRule(node);
 					if (styleRule != null) {
 						container = new StyleElementRuleContainer(
 								styleText, styleRule);
 					}
 				}
 			}
 		}
 		return container;
 	}
 
 	/**
 	 * Gets the sheet.
 	 *
 	 * @param styleContainer the style container
 	 * @return the sheet
 	 */
 	private static CSSStyleSheet getSheet(Node styleContainer) {
 
 		if (styleContainer instanceof INodeNotifier) {
 
 			INodeNotifier notifier = (INodeNotifier) styleContainer;
 
 			IStyleSheetAdapter adapter = (IStyleSheetAdapter) notifier
 					.getAdapterFor(IStyleSheetAdapter.class);
 
 			if (adapter != null) {
 
 				return (CSSStyleSheet) adapter.getSheet();
 			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the node.
 	 *
 	 * @param sheet the sheet
 	 * @param offset the offset
 	 * @return the node
 	 */
 	private static ICSSNode getNode(CSSStyleSheet sheet, int offset) {
 
 		ICSSModel model = ((ICSSDocument) sheet).getModel();
 
 		if (model != null)
 
 			return (ICSSNode) model.getIndexedRegion(offset);
 
 		return null;
 
 	}
 
 	/**
 	 * Checks if element has "style" property.
 	 *
 	 * @param element the element
 	 * @return true, if is suitable element
 	 */
 	private static boolean isSuitableElement(Element element) {
 
 		if (element instanceof ElementCSSInlineStyle
 				&& isAttributeAvailable(element, STYLE_TAG_NAME)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Gets the style rule.
 	 *
 	 * @param node the node
 	 * @return the style rule
 	 */
 	private static CSSStyleRule getStyleRule(ICSSNode node) {
 
 		while (node != null) {
 
 			if (node instanceof CSSStyleRule)
 				return (CSSStyleRule) node;
 
 			node = node.getParentNode();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets the relational offset.
 	 *
 	 * @param basicNode the basic node
 	 * @param absoluteOffset the absolute offset
 	 * @return the relational offset
 	 */
 	private static int getRelationalOffset(Node basicNode, int absoluteOffset) {
 
 		return absoluteOffset - ((IndexedRegion) basicNode).getStartOffset();
 	}
 
 	/**
 	 * Checks if attribute is available.
 	 *
 	 * @param element the element
 	 * @param attrName the attr name
 	 * @return true, if is attribute available
 	 */
 	private static boolean isAttributeAvailable(Element element, String attrName) {
 		ModelQuery modelQuery = ModelQueryUtil.getModelQuery(element
 				.getOwnerDocument());
 		if (modelQuery != null) {
 			CMElementDeclaration decl = modelQuery
 					.getCMElementDeclaration(element);
 			if (decl != null) {
 				CMNamedNodeMap map = decl.getAttributes();
 				if ((CMAttributeDeclaration) map.getNamedItem(attrName) != null) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 	
 	
 	/**
 	 * Gets the style attributes.
 	 *
 	 * @param styleString the style string
 	 * @return the style attributes
 	 */
 	public static Map<String, String> getStyleAttributes(String styleString) { 
		Map<String, String> styleMap = Collections.emptyMap();
 		if ((styleString != null) && (styleString.length() > 0)) {
			styleMap = new HashMap<String, String>();
 			String[] styles = styleString.split(Constants.SEMICOLON);
 			for (String styleElement : styles) {
				String[] styleElementParts = styleElement.trim().split(Constants.COLON);
				if (styleElementParts.length == 2 && Util.searchInElement(styleElementParts[0], CSSConstants.CSS_STYLES_MAP)) {
 					styleMap.put(styleElementParts[0], styleElementParts[1]);
 				}
 			}
 		}
 		return styleMap;
 	}
 }
