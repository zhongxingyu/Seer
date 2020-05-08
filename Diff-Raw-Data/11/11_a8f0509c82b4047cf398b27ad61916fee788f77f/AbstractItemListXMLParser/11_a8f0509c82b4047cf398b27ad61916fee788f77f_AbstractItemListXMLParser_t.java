 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.launch.core.persistence;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.tcf.te.launch.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.persistence.PersistenceManager;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
 import org.osgi.framework.Bundle;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * AbstractItemListXMLParser
  */
 public abstract class AbstractItemListXMLParser<ItemType> extends DefaultHandler {
	private final static int IN_ITEMS_DEFINITION = 1;
	private final static int IN_ITEM_DEFINITION = 2;
 
 	private SAXParser parser;
 
 	private int parseState;
 	private String lastData;
 	private String lastType;
 	private List<ItemType> items;
 
 	private final String containerTag;
 	private final String itemTag;
 
 	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
 
 	/**
 	 * Constructor
 	 */
 	public AbstractItemListXMLParser(String tagName) {
 		super();
 		Assert.isNotNull(tagName);
 
 		containerTag = tagName + "s"; //$NON-NLS-1$
 		itemTag = tagName;
 
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		factory.setNamespaceAware(false);
 		factory.setValidating(false);
 		try {
 			parser = factory.newSAXParser();
 		}
 		catch (ParserConfigurationException e) {
 			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getClass().getName(), e);
 			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 		}
 		catch (SAXException e) {
 			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getClass().getName(), e);
 			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 		}
 	}
 
 	/**
 	 * Returns the associated XML parser instance.
 	 */
 	public SAXParser getXMLReader() {
 		return parser;
 	}
 
 	/**
 	 * Reset the XML parser to a defined start point.
 	 */
 	public void initXMLParser() {
 		parseState = 0;
 		lastData = null;
 		lastType = null;
 		items = null;
 	}
 
 	/**
 	 * Associate the list instance to store the identified contexts.
 	 */
 	public void setItems(List<ItemType> items) {
 		this.items = items;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public void endElement(String uri, String localName, String name) throws SAXException {
 		if (containerTag.equalsIgnoreCase(name) && (parseState & IN_ITEMS_DEFINITION) == IN_ITEMS_DEFINITION) {
 			parseState ^= IN_ITEMS_DEFINITION;
 		}
 		if (itemTag.equalsIgnoreCase(name) && (parseState & IN_ITEM_DEFINITION) == IN_ITEM_DEFINITION) {
 			parseState ^= IN_ITEM_DEFINITION;
 
 			// The item encoded string is in last data
 			if (lastType != null && lastData != null) {
 				Class<IModelNode> clazz = null;
 				try {
 					clazz = (Class<IModelNode>)CoreBundleActivator.getContext().getBundle().loadClass(lastType);
 				} catch (ClassNotFoundException e) {
 					if (Platform.inDebugMode()) {
 						IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
 										"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
 						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 					}
 				}
 
 				// If the class could not be loaded by our own bundle class loader, try to find
 				// the bundle from the class name and try to load the class through the bundle.
 				if (clazz == null) {
 					String bundleId = lastType;
 					Bundle bundle = null;
 					while (bundleId != null && bundle == null) {
 						bundle = Platform.getBundle(bundleId);
 						if (bundle == null) {
 							int i = bundleId.lastIndexOf('.');
 							if (i != -1) {
 								bundleId = bundleId.substring(0, i);
 							} else {
 								bundleId = null;
 							}
 						}
 					}
 
 					if (bundle != null) {
 						try {
 							clazz = (Class<IModelNode>)bundle.loadClass(lastType);
 						} catch (ClassNotFoundException e) {
 							if (Platform.inDebugMode()) {
 								IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
 												"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
 								Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 							}
 						}
 					}
 				}
 
 				if (clazz != null) {
 					IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(clazz, lastData, false);
 					if (delegate != null) {
 						try {
 							ItemType item = (ItemType)delegate.read(getReadClass(), lastData, null);
 							if (!items.contains(item)) {
 								items.add(item);
 							}
 						}
 						catch (IOException e) {
 							if (Platform.inDebugMode()) {
 								IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
 												"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
 								Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	protected abstract Class<?> getReadClass();
 
 	/* (non-Javadoc)
 	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
 	 */
 	@Override
 	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
 		// Each time we start a new element, throw away the lastData content
 		lastData = null;
 
 		if (containerTag.equalsIgnoreCase(name)) {
 			parseState |= IN_ITEMS_DEFINITION;
 		}
 		if (itemTag.equalsIgnoreCase(name) && (parseState & IN_ITEMS_DEFINITION) == IN_ITEMS_DEFINITION) {
 			parseState |= IN_ITEM_DEFINITION;
 			lastType = attributes.getValue(ATTR_TYPE);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
 	 */
 	@Override
 	public void characters(char ch[], int start, int length) {
 		if (lastData == null) {
 			lastData = new String(ch, start, length).trim();
 		}
 		else {
 			lastData += new String(ch, start, length).trim();
 		}
 	}
 }
