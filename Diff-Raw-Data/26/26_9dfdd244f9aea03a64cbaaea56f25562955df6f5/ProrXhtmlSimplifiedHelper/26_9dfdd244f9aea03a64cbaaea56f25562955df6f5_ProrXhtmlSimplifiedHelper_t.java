 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Lukas Ladenberger - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.agilegrid;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.eclipse.rmf.reqif10.XhtmlContent;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlDivType;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlFactory;
 
 /**
  * @author Lukas Ladenberger
  */
 public class ProrXhtmlSimplifiedHelper {
 
 	private static String newLine = System.getProperty("line.separator");
 
 	/**
 	 * This static method returns the XML representation as string.
 	 */
	public static String generateXMLString(EObject eobject) throws IOException {
 		StringWriter sw = new StringWriter();
 		Map<Object, Object> options = new HashMap<Object, Object>();
 		options.put(XMLResource.OPTION_ROOT_OBJECTS,
 				Collections.singletonList(eobject));
 		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
 		options.put(XMLResource.OPTION_DECLARE_XML, Boolean.TRUE);
 		options.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.FALSE);
 		options.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE,
 				Boolean.FALSE);
 		XMLResourceImpl ri = new XMLResourceImpl();
 		ri.save(sw, options);
 		return sw.toString();
 	}
 
 	/**
 	 * This static method returns the simplified version of the
 	 * {@link XhtmlContent}.
 	 */
 	public static String xhtmlToSimplifiedString(XhtmlContent xhtmlContent) {
 		String stringValue = "";
 		try {
 			stringValue = generateXMLString(xhtmlContent);
 			stringValue = stringValue.replaceAll(newLine, "");
 			stringValue = stringValue.replaceAll(" +", " ");
 			stringValue = stringValue.replaceAll("\\<xhtml:br/\\>", newLine);
 			stringValue = stringValue.replaceAll("\\</xhtml:p\\>", newLine
 					+ newLine);
 			stringValue = stringValue.replaceAll("\\<xhtml:li\\>", "- ");
 			stringValue = stringValue.replaceAll("\\<xhtml:ul\\>", newLine);
 			stringValue = stringValue.replaceAll("\\</xhtml:ul\\>", newLine);
 			stringValue = stringValue.replaceAll("\\<xhtml:ol\\>", newLine);
 			stringValue = stringValue.replaceAll("\\</xhtml:ol\\>", newLine);
 			stringValue = stringValue.replaceAll("\\</xhtml:li\\>", newLine);
 			stringValue = stringValue.replaceAll("\\</xhtml:div\\>", newLine);
 			stringValue = stringValue.replaceAll("\\<.*?\\>", " ");
 			stringValue = stringValue.trim();
 		} catch (IOException e) {
 		}
 		return stringValue;
 	}
 	
 	/**
 	 * This static method returns the simplified version of the
 	 * {@link XhtmlContent}.
 	 */
 	public static XhtmlDivType stringToSimplifiedXhtml(String str) {
 		XhtmlDivType div = XhtmlFactory.eINSTANCE.createXhtmlDivType();
 		String[] split = str.split("\\r\\n|\\r|\\n");
 		for (String br : split) {
 			FeatureMapUtil.addText(div.getMixed(), br);
 			div.getBr().add(XhtmlFactory.eINSTANCE.createXhtmlBrType());
 		}
 		return div;
 	}
 
 }
