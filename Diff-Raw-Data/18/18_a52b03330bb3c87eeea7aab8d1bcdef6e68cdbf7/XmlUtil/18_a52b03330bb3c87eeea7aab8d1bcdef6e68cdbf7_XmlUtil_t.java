 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.rockagen.commons.util;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 import com.thoughtworks.xstream.XStream;
 
 /**
  * XML UTIL
  * 
  * @author AGEN
  * @since JDK1.6
  * @since DOM4J 1.6.1
  * @since XSTREAM 1.2.2
  */
 public class XmlUtil {
 
 	// ~ Instance fields ==================================================
 
 	/**
 	 * 
 	 */
 	private static final Log log = LogFactory.getLog(XmlUtil.class);
 	/**
 	 * Default Encoding
 	 */
 	private static final String ENCODING = "UTF-8";
 
 	// ~ Constructors ==================================================
 
 	/**
 	 */
 	private XmlUtil() {
 
 	}
 
 	// ~ Methods ==================================================
 
 	/**
 	 * Format xml {@link OutputFormat#createPrettyPrint()}
 	 * 
 	 * @param xmlStr
 	 * @param enc
 	 * @return pretty String
 	 */
 	public static String formatPretty(String xmlStr, String enc) {
 		return formatPretty(xmlStr,enc,false);
 	}
 	/**
 	 * Format xml {@link OutputFormat#createPrettyPrint()}
 	 * 
 	 * @param xmlStr
 	 * @param enc
 	 * @param isSuppressDeclaration
 	 * @return pretty String
 	 */
 	public static String formatPretty(String xmlStr, String enc,boolean isSuppressDeclaration) {
 		
 		if (CommUtil.isBlank(xmlStr))
 			return xmlStr;
 		
 		if (enc == null)
 			enc = ENCODING;
 		
 		OutputFormat formater = OutputFormat.createPrettyPrint();
 		formater.setEncoding(enc);
 		formater.setSuppressDeclaration(isSuppressDeclaration);
 		return format(xmlStr, formater, enc);
 	}
 
 	/**
 	 * Format xml {@link OutputFormat#createCompactFormat()}
 	 * 
 	 * @param xmlStr
 	 * @param enc
 	 * @return Compact xml String
 	 */
 	public static String formatCompact(String xmlStr, String enc) {
 		return formatCompact(xmlStr,enc,false);
 	}
 	/**
 	 * Format xml {@link OutputFormat#createCompactFormat()}
 	 * 
 	 * @param xmlStr
 	 * @param enc
 	 * @param isSuppressDeclaration
 	 * @return Compact xml String
 	 */
 	public static String formatCompact(String xmlStr, String enc,boolean isSuppressDeclaration) {
 		
 		if (CommUtil.isBlank(xmlStr))
 			return xmlStr;
 		
 		if (enc == null)
 			enc = ENCODING;
 		
 		OutputFormat formater = OutputFormat.createCompactFormat();
 		formater.setEncoding(enc);
 		formater.setSuppressDeclaration(isSuppressDeclaration);
 		return format(xmlStr, formater, enc);
 	}
 
 	/**
 	 * Format xml
 	 * 
 	 * @param xmlStr
 	 * @param formater
 	 *            {@link OutputFormat}
 	 * @param enc
	 * @return format String
 	 */
 	public static String format(String xmlStr, OutputFormat formater, String enc) {
 		if (CommUtil.isBlank(xmlStr))
 			return xmlStr;
 
 		if (enc == null)
 			enc = ENCODING;
 
 		SAXReader reader = new SAXReader();
 		StringReader sr = new StringReader(xmlStr);
 
 		Document doc = null;
 		XMLWriter writer = null;
 		StringWriter sw=new StringWriter();
 		try {
 			doc = reader.read(sr);
 			writer = new XMLWriter(sw,formater);
 			writer.write(doc);
 			return sw.toString();
 		} catch (DocumentException e) {
 			log.error(e);
 		} catch (IOException e) {
 			log.error(e);
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 				}
 			}
 
 		}
 
 		return xmlStr;
 	}
 
 	/**
 	 * Format xml, use xml file encoding value as OutputFormat encoding ,if not
 	 * exist encoding attribute use UTF-8 by default
 	 * {@link OutputFormat#createPrettyPrint()}
 	 * 
 	 * @param xmlStr
 	 * @return format xml string
 	 */
 	public static String formatPretty(String xmlStr) {
 		return formatPretty(xmlStr, getEncoding(xmlStr));
 	}
 
 	/**
 	 * Format xml, use xml file encoding value as OutputFormat encoding,if not
 	 * exist encoding attribute use UTF-8 by default
 	 * {@link OutputFormat#createPrettyPrint()}
 	 * 
 	 * @param xmlStr
 	 * @return format xml string
 	 */
 	public static String formatCompact(String xmlStr) {
 		return formatCompact(xmlStr, getEncoding(xmlStr));
 	}
 
 	/**
 	 * Obtain xml file encoding attribute
 	 * 
	 * @param xmlStr
 	 * @return encoding value
 	 */
	public static String getEncoding(String xmlStr) {
 		String result = null;
 
		String xml = xmlStr.trim();
 
 		if (xml.startsWith("<?xml")) {
 			int end = xml.indexOf("?>");
 			int encIndex = xml.indexOf("encoding=");
 			if (encIndex != -1) {
 				String sub = xml.substring(encIndex + 9, end);
 				result = CommUtil.substringBetween(sub, "\"", "\"");
 				return result;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * <P>
 	 * Bean to xml.
 	 * <P>
 	 * <P>
 	 * Using XStream library to serialize objects to XML.
 	 * </P>
 	 * <p>example:</p>
 	 * <code><pre>
 	 * 
 	 *  XAlias[] xa={new XAlias("Foo",Foo.class)};
 	 *  XAliasField[] xaf={new XAliasField("Bar",Bar.class,"bar")};
 	 *  XAliasAttribute[] xaa={new XAliasAttribute("Name",User.class,"name")};
 	 *  
 	 *  then,
 	 *  
 	 * 	toXml(bean,xa,xaf,xaa);
 	 * </pre></code>
 	 * <b>Note: XStream Mode is {@link XStream#NO_REFERENCES}
 	 * @param obj
 	 *            bean
 	 * @param xAlias
 	 *            Alias a Class to a shorter name to be used in XML elements.
 	 * @param xAliasFields
 	 *            Create an alias for a field name.
 	 * @param xAliasAttributes
 	 *            Create an alias for an attribute.
 	 * @since XStream 1.2.2
 	 * 
 	 * @return xml String
 	 */
 	public static <T> String toXml(T obj, XAlias[] xAlias,
 			XAliasField[] xAliasFields, XAliasAttribute[] xAliasAttributes) {
 
 		if (obj == null)
 			return null;
 
 		XStream xstream = new XStream();
 		xstream.setMode(XStream.NO_REFERENCES);
 		aliasXstream(xstream,xAlias,xAliasFields,xAliasAttributes);
 
 		return xstream.toXML(obj);
 	}
 	/**
 	 * <P>
 	 * Bean to xml.
 	 * <P>
 	 * <P>
 	 * Using XStream library to serialize objects to XML.
 	 * </P>
 	 * <p>example:</p>
 	 * <code><pre>
 	 * 
 	 *  XAlias[] xa={new XAlias("Foo",Foo.class)};
 	 *  XAliasField[] xaf={new XAliasField("Bar",Bar.class,"bar")};
 	 *  XAliasAttribute[] xaa={new XAliasAttribute("Name",User.class,"name")};
 	 *  
 	 *  then,
 	 *  
 	 * 	toBean(xmlStr,xa,xaf,xaa);
 	 * </pre></code>
 	 * <b>Note: XStream Mode is {@link XStream#NO_REFERENCES}
 	 * @param xmlStr
 	 *            xml String
 	 * @param xAlias
 	 *            Alias a Class to a shorter name to be used in XML elements.
 	 * @param xAliasFields
 	 *            Create an alias for a field name.
 	 * @param xAliasAttributes
 	 *            Create an alias for an attribute.
 	 * @since XStream 1.2.2
 	 * 
 	 * @return Object
 	 */
 	public static Object toBean(String xmlStr, XAlias[] xAlias,
 			XAliasField[] xAliasFields, XAliasAttribute[] xAliasAttributes) {
 		
 		if(CommUtil.isBlank(xmlStr)) return null;
 		
 		XStream xstream = new XStream();
 		xstream.setMode(XStream.NO_REFERENCES);
 		aliasXstream(xstream,xAlias,xAliasFields,xAliasAttributes);
 		
 		return xstream.fromXML(xmlStr);
 	}
 	
 	
 	
 	/**
 	 * XStream Alias 
 	 * @param xstream
 	 * @param xAlias
 	 * @param xAliasFields
 	 * @param xAliasAttributes
 	 */
 	protected static void aliasXstream(XStream xstream,XAlias[] xAlias,
 	XAliasField[] xAliasFields, XAliasAttribute[] xAliasAttributes){
 		if (xAlias != null) {
 			for (XAlias xa : xAlias) {
 				xstream.alias(xa.aliasName, xa.classType);
 			}
 		}
 		if (xAliasFields != null) {
 			for (XAliasField xaf : xAliasFields) {
 				xstream.aliasField(xaf.aliasName, xaf.fieldType, xaf.fieldName);
 			}
 		}
 		if (xAliasAttributes != null) {
 			for (XAliasAttribute xaa : xAliasAttributes) {
 				xstream.useAttributeFor(xaa.attributeType, xaa.attributeName);
 				xstream.aliasAttribute(xaa.attributeType, xaa.attributeName,
 						xaa.aliasName);
 			}
 		}
 	}
 
 	/**
 	 * Alias a Class to a shorter name to be used in XML elements. see
 	 * {@link XStream#alias(String, Class, Class)}
 	 */
 	public static class XAlias {
 		private String aliasName;
 		private Class<?> classType;
 
 		public XAlias() {
 		}
 
 		public XAlias(String aliasName, Class<?> classType) {
 			this.aliasName = aliasName;
 			this.classType = classType;
 		}
 
 		public String getAliasName() {
 			return aliasName;
 		}
 
 		public void setAliasName(String aliasName) {
 			this.aliasName = aliasName;
 		}
 
 		public Class<?> getClassType() {
 			return classType;
 		}
 
 		public void setClassType(Class<?> classType) {
 			this.classType = classType;
 		}
 	}
 
 	/**
 	 * Create an alias for a field name. see
 	 * {@link XStream#aliasField(String, Class, String)}
 	 */
 	public static class XAliasField {
 
 		private String aliasName;
 		private Class<?> fieldType;
 		private String fieldName;
 
 		public XAliasField() {
 		}
 
 		public XAliasField(String aliasName, Class<?> fieldType,
 				String fieldName) {
 			this.aliasName = aliasName;
 			this.fieldType = fieldType;
 			this.fieldName = fieldName;
 		}
 
 		public String getAliasName() {
 			return aliasName;
 		}
 
 		public void setAliasName(String aliasName) {
 			this.aliasName = aliasName;
 		}
 
 		public Class<?> getFieldType() {
 			return fieldType;
 		}
 
 		public void setFieldType(Class<?> fieldType) {
 			this.fieldType = fieldType;
 		}
 
 		public String getFieldName() {
 			return fieldName;
 		}
 
 		public void setFieldName(String fieldName) {
 			this.fieldName = fieldName;
 		}
 
 	}
 
 	/**
 	 * Create an alias for an attribute. see
 	 * {@link XStream#aliasAttribute(Class, String, String)}
 	 */
 	public static class XAliasAttribute {
 
 		private String aliasName;
 		private Class<?> attributeType;
 		private String attributeName;
 
 		public XAliasAttribute() {
 		}
 
 		public XAliasAttribute(String aliasName, Class<?> attributeType,
 				String attributeName) {
 			this.aliasName = aliasName;
 			this.attributeType = attributeType;
 			this.attributeName = attributeName;
 		}
 
 		public String getAliasName() {
 			return aliasName;
 		}
 
 		public void setAliasName(String aliasName) {
 			this.aliasName = aliasName;
 		}
 
 		public Class<?> getAttributeType() {
 			return attributeType;
 		}
 
 		public void setAttributeType(Class<?> attributeType) {
 			this.attributeType = attributeType;
 		}
 
 		public String getAttributeName() {
 			return attributeName;
 		}
 
 		public void setAttributeName(String attributeName) {
 			this.attributeName = attributeName;
 		}
 
 	}
 
 }
