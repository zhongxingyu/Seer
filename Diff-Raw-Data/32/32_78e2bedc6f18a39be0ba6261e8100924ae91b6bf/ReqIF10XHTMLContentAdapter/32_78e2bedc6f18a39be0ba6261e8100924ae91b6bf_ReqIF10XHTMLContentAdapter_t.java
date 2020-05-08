 /*******************************************************************************
  * Copyright (c) 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     itemis AG - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.common.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.xerces.impl.Constants;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.XMLSave;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
 import org.eclipse.rmf.reqif10.XhtmlContent;
import org.eclipse.rmf.reqif10.xhtml.XhtmlDivType;
import org.eclipse.rmf.reqif10.xhtml.XhtmlFactory;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlPackage;
 
 public class ReqIF10XHTMLContentAdapter extends EContentAdapter {
 	String xmlString = null;
 	XMLResource resource = null;
 	static final String UTF8 = "UTF-8"; //$NON-NLS-1$
 	XhtmlContent xhtmlContent = null;
 
 	public String getXhtmlString() {
 		if (null == xmlString) {
 			updateXhtmlString();
 		}
 
 		return xmlString;
 	}
 
 	public void setXhtmlString(String xhtmlString) throws IOException {
 		// set object tree in model
 		updateXhtmlObjects(xhtmlString);
 		// the xhtml String is updated automatically from updated xhtml objects
 		// TODO: analyze if it would be sufficient not to recreate the xhtml string from the xhtml object structure
 	}
 
	public void setXhtmlPlainTextString(String plainText) {
		XhtmlDivType xhtmlDivType = XhtmlFactory.eINSTANCE.createXhtmlDivType();
		xhtmlDivType.getMixed().add(FeatureMapUtil.createTextEntry(plainText));
		xhtmlContent.setXhtml(xhtmlDivType);
	}

 	public EList<Diagnostic> getErrors() {
 		return resource.getErrors();
 	}
 
 	public EList<Diagnostic> getWarnings() {
 		return resource.getWarnings();
 	}
 
 	@Override
 	public boolean isAdapterForType(Object type) {
 		return ReqIF10XHTMLContentAdapter.class == type;
 	}
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		super.notifyChanged(notification);
 		Object notifier = notification.getNotifier();
 		if (notifier instanceof XhtmlContent) {
 			updateXhtmlString();
 		}
 	}
 
 	private void updateXhtmlString() {
 		assert null != xhtmlContent;
 		EObject xmlRoot = xhtmlContent.getXhtml();
 
 		if (null != xmlRoot) {
 			try {
 				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 
 				XMLResource resource = getResource();
 				Map<Object, Object> options = new HashMap<Object, Object>();
 				options.put(XMLResource.OPTION_ROOT_OBJECTS, Collections.singletonList(xmlRoot));
 				resource.save(byteArrayOutputStream, options);
 
 				xmlString = new String(byteArrayOutputStream.toByteArray(), UTF8);
 			} catch (IOException ioe) {
 				assert false : "We should always be able to update the xhtml string from the given object model"; //$NON-NLS-1$
 			}
 		}
 	}
 
 	private void updateXhtmlObjects(String xmlString) throws IOException {
 		assert null != xhtmlContent;
 		if (null == xmlString) {
 			// unset the xhtmlContent
 			xhtmlContent.unsetXhtml();
 		} else {
 			// parse the string
 
 			Resource resource = getResource();
 			resource.load(new ByteArrayInputStream(xmlString.getBytes(UTF8)), null);
 			EList<EObject> contents = resource.getContents();
 			if (0 < contents.size()) {
 				EObject rootObject = contents.get(0);
 				xhtmlContent.setXhtml(rootObject);
 			}
 		}
 
 	}
 
 	private XMLResource getResource() {
 		if (null == resource) {
 			resource = new NestedXMLResourceImpl();
 
 		} else {
 			resource.getContents().clear();
 		}
 		return resource;
 	}
 
 	@Override
 	public void setTarget(Notifier target) {
 		super.setTarget(target);
 		if (target instanceof XhtmlContent) {
 			xhtmlContent = (XhtmlContent) target;
 		}
 	}
 
 	private class NestedXMLResourceImpl extends XMLResourceImpl {
 		@Override
 		protected XMLHelper createXMLHelper() {
 			XMLHelper helper = super.createXMLHelper();
 			// make sure that no namespace prefix is created during serialization
 			EMap<String, String> prefixToNamespaceMap = helper.getPrefixToNamespaceMap();
 			prefixToNamespaceMap.put("", XhtmlPackage.eNS_URI); //$NON-NLS-1$
 			helper.setPrefixToNamespaceMap(prefixToNamespaceMap);
 
 			// make sure that missing namespace declaration is mapped to Xhtml
 			helper.pushContext();
 			helper.addPrefix("", XhtmlPackage.eNS_URI);//$NON-NLS-1$
 			return helper;
 		}
 
 		@Override
 		protected XMLSave createXMLSave() {
 			return new XMLSaveImpl(createXMLHelper()) {
 				@Override
 				protected Object writeTopObject(EObject top) {
 					EClass eClass = top.eClass();
 					String name = helper.getQName(eClass);
 					doc.startElement(name);
 					Object mark = doc.mark();
 					root = top;
 					saveElementID(top);
 					return mark;
 				}
 			};
 		}
 
 		@Override
 		protected void init() {
			super.init();
 			// make sure that the xhtml package is registered
 			Object dummy = XhtmlPackage.eINSTANCE;
 			setEncoding(UTF8);
 			Map<Object, Object> loadOptions = getDefaultLoadOptions();
 			loadOptions.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
 			loadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
 			// avoid creation of document root element
 			loadOptions.put(XMLResource.OPTION_SUPPRESS_DOCUMENT_ROOT, Boolean.TRUE);
 			// performance improvement: delays the attachment of adapters until all elements are loaded.
 			loadOptions.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
 
 			Map<String, Object> parserProperties = new HashMap<String, Object>();
 			parserProperties.put(Constants.XERCES_PROPERTY_PREFIX + Constants.BUFFER_SIZE_PROPERTY, 1024);
 
 			Map<String, Boolean> parserFeatures = new HashMap<String, Boolean>();
 			parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE, true);
 			parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACE_PREFIXES_FEATURE, false);
 			loadOptions.put(XMLResource.OPTION_PARSER_FEATURES, parserFeatures);
 			loadOptions.put(XMLResource.OPTION_PARSER_PROPERTIES, parserProperties);
 
 			Map<Object, Object> saveOptions = getDefaultSaveOptions();
 			saveOptions.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
 			saveOptions.put(XMLResource.OPTION_DECLARE_XML, Boolean.FALSE);
 			saveOptions.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.FALSE);
 			saveOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
 			saveOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);

 		}
 	}
 
 }
