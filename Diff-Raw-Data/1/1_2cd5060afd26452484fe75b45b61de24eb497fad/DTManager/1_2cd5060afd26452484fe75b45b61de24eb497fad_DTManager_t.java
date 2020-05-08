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
 package org.eclipse.jst.pagedesigner.dtmanager.internal.provisional;
 
 import org.eclipse.jst.jsf.core.internal.tld.CMUtil;
 import org.eclipse.jst.pagedesigner.converter.ConverterFactoryRegistry;
 import org.eclipse.jst.pagedesigner.converter.IConverterFactory;
 import org.eclipse.jst.pagedesigner.converter.ITagConverter;
 import org.eclipse.jst.pagedesigner.dtmanager.converter.internal.provisional.DTTagConverterFactory;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.w3c.dom.Element;
 
 /**
  * DTManager is the top-level entry point for design-time (DT) services, such
  * as tag converters and design-time information.
  * 
  * @author Ian Trimble - Oracle
  */
 public class DTManager {
 
 	private static DTManager instance;
 	private IDTInfoFactory dtInfoFactory;
 
 	private DTManager() {
        // no external instantiation
 	}
 
 	/**
 	 * Gets the singleton DTManager instance.
 	 * 
 	 * @return The singleton DTManager instance.
 	 */
 	public static synchronized DTManager getInstance() {
 		if (instance == null) {
 			instance = new DTManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * Gets an ITagConverter instance for the specified Element and mode.
 	 * 
 	 * @param element Element instance for which to locate and return an
 	 * ITagConverter instance.
 	 * @param mode Mode falg (use IConverterFactory constants).
 	 * @param document Target IDOMDocument instance.
 	 * @return An ITagConverter instance for the specified Element and mode.
 	 */
 	public ITagConverter getTagConverter(Element element, int mode, IDOMDocument document) {
 		ITagConverter tagConverter = null;
 		String nsURI = CMUtil.getElementNamespaceURI(element);
 		//try MD-driven approach
 		IConverterFactory tagConverterFactory = getTagConverterFactory(nsURI);
 		if (tagConverterFactory != null) {
 			tagConverter = tagConverterFactory.createConverter(element, mode);
 			if (tagConverter != null) {
 				tagConverter.setDestDocument(document);
 			} else {
 				//fallback to contributed (non-MD-driven) approach
 				tagConverter = ConverterFactoryRegistry.getInstance().createTagConverter(element, mode, document);
 			}
 		}
 		return tagConverter;
 	}
 
 	/**
 	 * Gets an IConverterFactory instance for the specified namespace URI.
 	 * 
 	 * @param nsURI Namespace URI.
 	 * @return An IConverterFactory instance for the specified namespace URI.
 	 */
 	protected IConverterFactory getTagConverterFactory(String nsURI) {
 		//TODO: future - expand to first look for registered factories
 		return new DTTagConverterFactory();
 	}
 
 	/**
 	 * Gets an IDTInfo instance for the specified Element.
 	 * 
 	 * @param element Element instance for which to locate and return IDTInfo
 	 * instance.
 	 * @return An IDTInfo instance for the specified Element.
 	 */
 	public IDTInfo getDTInfo(Element element) {
 		IDTInfo dtInfo = null;
 		String nsURI = CMUtil.getElementNamespaceURI(element);
 		IDTInfoFactory dtInfoFactory = getDTInfoFactory(nsURI);
 		if (dtInfoFactory != null) {
 			dtInfo = dtInfoFactory.getDTInfo(element);
 		}
 		return dtInfo;
 	}
 
 	/**
 	 * Gets an IDTInfoFactory instance for the specified namespace URI.
 	 * 
 	 * @param nsURI Namespace URI.
 	 * @return An IDTInfoFactory instance for the specified namespace URI.
 	 */
 	protected IDTInfoFactory getDTInfoFactory(String nsURI) {
 		if (dtInfoFactory == null) {
 			dtInfoFactory = new DefaultDTInfoFactory();
 		}
 		return dtInfoFactory;
 	}
 
 }
