 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.pagedesigner.dtmanager.converter.operations.internal.provisional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.ITagConverterContext;
 import org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.ITransformOperation;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Abstract ITransformOperation implementation. Maintains ITagConverterContext
  * instance.
  * 
  * @author Ian Trimble - Oracle
  */
 public abstract class AbstractTransformOperation implements ITransformOperation {
 
 	/**
 	 * ITagConverterContext instance.
 	 */
 	protected ITagConverterContext tagConverterContext;
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.ITransformOperation#transform(org.w3c.dom.Element, org.w3c.dom.Element)
 	 */
 	public abstract Element transform(Element srcElement, Element curElement);
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.ITransformOperation#setTagConverterContext(org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.ITagConverterContext)
 	 */
 	public void setTagConverterContext(ITagConverterContext tagConverterContext) {
 		this.tagConverterContext = tagConverterContext;
 	}
 
 	/**
 	 * Creates a new Element.
 	 * 
 	 * @param tagName Name of Element to be created.
 	 * @return New Element instance.
 	 */
 	protected Element createElement(String tagName) {
 		ITransformOperation operation = new CreateElementOperation(tagName);
 		operation.setTagConverterContext(tagConverterContext);
 		return operation.transform(null, null);
 	}
 
 	/**
 	 * Creates and appends a new child Element.
 	 * 
 	 * @param tagName Name of child Element to be created.
 	 * @param parentElement Element instance to which to append the new
 	 * Element.
 	 * @return New Element instance.
 	 */
 	protected Element appendChildElement(String tagName, Element parentElement) {
 		ITransformOperation operation = new AppendChildElementOperation(tagName);
 		operation.setTagConverterContext(tagConverterContext);
 		return operation.transform(null, parentElement);
 	}
 
 	/**
 	 * Creates and appends a new child Text.
 	 * 
 	 * @param content Content of new child Text.
 	 * @param parentElement Element instance to which to append the new Text.
 	 */
 	protected void appendChildText(String content, Element parentElement) {
 		ITransformOperation operation = new AppendChildTextOperation(content);
 		operation.setTagConverterContext(tagConverterContext);
 		operation.transform(null, parentElement);
 	}
 
 	/**
 	 * Gets collection of child Element instances who's local name matches
 	 * specified tag name.
 	 * 
 	 * @param srcNode Source Node instance.
 	 * @param tagName Tag local name.
 	 * @return Collection of child Element instances who's local name matches
 	 * specified tag name.
 	 */
 	protected List getChildElements(Node srcNode, String tagName) {
 		List childElements = new ArrayList();
 		NodeList childNodes = srcNode.getChildNodes();
 		for (int i = 0; i < childNodes.getLength(); i++) {
 			Node curNode = childNodes.item(i);
 			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
 				String curNodeName = curNode.getLocalName();
 				if (curNode != null && curNodeName.equals(tagName)) {
					childElements.add((Element)curNode);
 				}
 			}
 		}
 		return childElements;
 	}
 
 }
