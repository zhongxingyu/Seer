 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.css2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jst.pagedesigner.IHTMLConstants;
 import org.eclipse.wst.css.core.internal.provisional.adapters.IStyleSheetAdapter;
 import org.eclipse.wst.css.core.internal.provisional.adapters.IStyleSheetListAdapter;
 import org.eclipse.wst.html.core.internal.provisional.HTML40Namespace;
 import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.stylesheets.StyleSheet;
 import org.w3c.dom.stylesheets.StyleSheetList;
 
 /**
  * @author mengbo
  * @version 1.5
  */
 public class CSSTempUtil {
 	/**
 	 * the default implementation of (IStyleSheetListAdapter)
 	 * docnotifier.getAdapterFor(IStyleSheetListAdapter.class) will only get
 	 * those style tags under certain tags like "html", "head", etc.
 	 * 
 	 * But datawindow is generating style tag and is not putting them into
 	 * "head". So we can't handle them using the default SSE mechanism.
 	 * 
 	 * We have another problem: currently the converted element is also using
 	 * the original document, not generating another document, and these
 	 * elements is not really adding into the document, so they can't be reached
 	 * from the document.
 	 * 
 	 * @param ele
 	 * @return
 	 * @see org.eclipse.wst.html.core.htmlcss.HTMLDocumentAdapter#addStyleSheet(org.w3c.dom.Element)
 	 */
 	public static List getStyleSheets(Element element) {
 		List styleSheets = new ArrayList();
 		INodeNotifier docnotifier = (INodeNotifier) element.getOwnerDocument();
 		IStyleSheetListAdapter adapter = (IStyleSheetListAdapter) docnotifier
 				.getAdapterFor(IStyleSheetListAdapter.class);
 
 		StyleSheetList ssl = (adapter == null ? null : adapter.getStyleSheets());
 
 		if (ssl != null) {
 			for (int i = 0, numStyles = ssl.getLength(); i < numStyles; i++) {
 				// loop for styles (<style> and <link>)
 				org.w3c.dom.stylesheets.StyleSheet ss = ssl.item(i);
 				styleSheets.add(ss);
 			}
 		}
 
 		// now is our work-around part for support datawindow.
 		Node parent = element.getParentNode();
 		Element rootEle = element;
 		while (parent != null && parent instanceof Element) {
 			rootEle = (Element) parent;
 			parent = parent.getParentNode();
 		}
 		addStyleSheet(rootEle, styleSheets);
 		return styleSheets;
 	}
 
 	/**
 	 */
 	private static void addStyleSheet(Element node, List result) {
 		IDOMElement element = (IDOMElement) node;
 		String tagName = element.getTagName();
 		if (tagName == null) {
 			return;
 		}
 		boolean isContainer = false;
 
 		if (element.isCommentTag()) {
 			Node parent = element.getParentNode();
 			if (parent == element.getOwnerDocument()) {
 				// This condition is too severe, actually do not work for JSF
 				// template.
 				// But above (! globalTag() && isContainer()) cover JSF template
 				// + tpl template
 				isContainer = true;
 			} else if (parent.getNodeType() == Node.ELEMENT_NODE) {
 				tagName = ((Element) parent).getTagName();
 				if (tagName != null
 						&& tagName
 								.equalsIgnoreCase(HTML40Namespace.ElementName.HEAD)) {
 					isContainer = true;
 				}
 			}
 		} else {
 			INodeNotifier notifier = element;
 
 			// (lium) Increase performance: since this method is called tooooo
 			// many times,
 			// and getAdapterFor() is slow, so add a check on the tagName to
 			// filter
 			// those stylesheet stuff first.
 			if (IHTMLConstants.TAG_LINK.equalsIgnoreCase(tagName)
 					|| IHTMLConstants.TAG_STYLE.equalsIgnoreCase(tagName)) {
 				INodeAdapter adapter = notifier
 						.getAdapterFor(IStyleSheetAdapter.class);
				if (adapter instanceof IStyleSheetAdapter) {
                    // XXX: see bug 171740.  This is a (hopefully) temporary
                    // workaround that avoids having the call to getSheet throw an NPE
                    IStyleSheetAdapter sheetAdapter = (IStyleSheetAdapter) adapter;
                    if (sheetAdapter.getElement() != null
                            && "text/css".equals(sheetAdapter.getElement().getAttribute(HTML40Namespace.ATTR_NAME_TYPE)))

                    {
    					StyleSheet sheet = ((IStyleSheetAdapter) adapter)
    							.getSheet();
                        
                        if (sheet != null)
                        {
                            result.add(sheet);
                        }
                    }
 				}
 			}
 
 			isContainer = true;
 		}
 		if (isContainer) {
 			for (Node child = element.getFirstChild(); child != null; child = child
 					.getNextSibling()) {
 				if (child.getNodeType() != Node.ELEMENT_NODE)
 					continue;
 				addStyleSheet((Element) child, result);
 			}
 		}
 	}
 }
