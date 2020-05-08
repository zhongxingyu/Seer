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
 package org.eclipse.jst.pagedesigner.dtmanager.converter.operations;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jst.pagedesigner.dtmanager.converter.ITagConverterContext;
 import org.eclipse.jst.pagedesigner.dtmanager.converter.ITransformOperation;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Abstract ITransformOperation implementation. Maintains ITagConverterContext
  * instance and collection of child ITransformOperation instances.
  * 
  * <p><b>Provisional API - subject to change</b></p>
  * 
  * @author Ian Trimble - Oracle
  */
 public abstract class AbstractTransformOperation implements ITransformOperation {
 
 	/**
 	 * ITagConverterContext instance.
 	 */
 	protected ITagConverterContext tagConverterContext;
 
 	/**
 	 * Collection of child ITransformOperation instances.
 	 */
 	protected List childOperations;
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.ITransformOperation#transform(org.w3c.dom.Element, org.w3c.dom.Element)
 	 */
 	public abstract Element transform(Element srcElement, Element curElement);
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.ITransformOperation#setTagConverterContext(org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.ITagConverterContext)
 	 */
 	public void setTagConverterContext(ITagConverterContext tagConverterContext) {
         // API: this should really be set on construction since other methods
         // cannot be called until it is set.
 		this.tagConverterContext = tagConverterContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.ITransformOperation#appendChildOperation(org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.ITransformOperation)
 	 */
 	public void appendChildOperation(ITransformOperation operation) {
 		if (operation != null) {
 			if (childOperations == null) {
 				childOperations = new ArrayList();
 			}
 			operation.setTagConverterContext(tagConverterContext);
 			childOperations.add(operation);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.ITransformOperation#getChildOperations()
 	 */
 	public List getChildOperations() {
 		return childOperations;
 	}
 
 	/**
 	 * Convenience method to execute child ITransformOperation instances.
 	 * 
 	 * @param srcElement Source Element instance.
 	 * @param curElement Current Element instance (that is being transformed).
 	 * @return New current Element instance.
 	 */
 	protected Element executeChildOperations(Element srcElement, Element curElement) {
 		Element retElement = curElement;
 		if (childOperations != null && childOperations.size() > 0) {
 			Iterator itChildOperations = childOperations.iterator();
 			while (itChildOperations.hasNext()) {
 				ITransformOperation childOperation = (ITransformOperation)itChildOperations.next();
 				retElement = childOperation.transform(srcElement, retElement);
 			}
 		}
 		return retElement;
 	}
 
 	/**
 	 * Creates a new Element.
 	 * 
 	 * @param tagName Name of Element to be created.
 	 * @return New Element instance.
 	 */
 	protected Element createElement(String tagName) {
 		ITransformOperation operation =
 			TransformOperationFactory.getInstance().getTransformOperation(
 					TransformOperationFactory.OP_CreateElementOperation,
 					new String[]{tagName});
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
 		ITransformOperation operation =
 			TransformOperationFactory.getInstance().getTransformOperation(
 					TransformOperationFactory.OP_AppendChildElementOperation,
 					new String[]{tagName});
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
 		ITransformOperation operation =
 			TransformOperationFactory.getInstance().getTransformOperation(
 					TransformOperationFactory.OP_AppendChildTextOperation,
 					new String[]{content});
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
				if (curNodeName != null && curNodeName.equals(tagName)) {
 					childElements.add(curNode);
 				}
 			}
 		}
 		return childElements;
 	}
 
 }
