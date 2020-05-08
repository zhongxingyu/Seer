 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.snaps.core.internal.webapp.config;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.virgo.snaps.core.internal.webapp.config.MutableWebXml.AbstractWebComponentDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.MutableWebXml.MutableFilterDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.MutableWebXml.MutableServletDefinition;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 final class WebXmlParser {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
     
     private static final String ELEMENT_PARAM_VALUE = "param-value";
 
     private static final String ELEMENT_PARAM_NAME = "param-name";
 
     private static final String ELEMENT_INIT_PARAM = "init-param";
 
     private static final String ELEMENT_URL_PATTERN = "url-pattern";
 
     private static final String ELEMENT_SERVLET_MAPPING = "servlet-mapping";
 
     private static final String ELEMENT_SERVLET_CLASS = "servlet-class";
 
     private static final String ELEMENT_SERVLET_NAME = "servlet-name";
 
     private static final String ELEMENT_SERVLET = "servlet";
     
     private static final String ELEMENT_FILTER = "filter";
     
     private static final String ELEMENT_FILTER_CLASS = "filter-class";
 
     private static final String ELEMENT_FILTER_NAME = "filter-name";
     
     private static final String ELEMENT_FILTER_MAPPING = "filter-mapping";
     
     private static final String ELEMENT_DISPATCHER = "dispatcher";
 
     public void parse(InputStream resource, MutableWebXml webXml) {
         InputStream parseResource = new BufferedInputStream(resource);
         Document document = loadDocument(parseResource);
         
         parseServletDefinitions(document, webXml);
         parseServletMappingDefinitions(document, webXml);
         
         parseFilterDefinitions(document, webXml);
         parseFilterMappingDefinitions(document, webXml);
     }    
 
     private void parseServletDefinitions(Document document, MutableWebXml webXml) {
         NodeList nodes = document.getElementsByTagName(ELEMENT_SERVLET);
         for (int x = 0; x < nodes.getLength(); x++) {
             Node e = nodes.item(x);
             String servletName = extractChildNodeValue(e, ELEMENT_SERVLET_NAME);
             String servletClassName = extractChildNodeValue(e, ELEMENT_SERVLET_CLASS);
             MutableServletDefinition servletDefinition = webXml.addServletDefinition(servletName, servletClassName);
             parseInitParameters(e, servletDefinition);
         }
     }
       
     private void parseInitParameters(Node servletOrFilterNode, AbstractWebComponentDefinition definition) {
         NodeList childNodes = servletOrFilterNode.getChildNodes();
         for (int x = 0; x < childNodes.getLength(); x++) {
             Node node = childNodes.item(x);
             if (ELEMENT_INIT_PARAM.equals(node.getNodeName())) {
                 String paramName = extractChildNodeValue(node, ELEMENT_PARAM_NAME);
                 String paramValue = extractChildNodeValue(node, ELEMENT_PARAM_VALUE);
                 definition.addInitParameter(paramName, paramValue);
             }            
         }
     }
 
     private void parseServletMappingDefinitions(Document document, MutableWebXml webXml) {
         NodeList nodes = document.getElementsByTagName(ELEMENT_SERVLET_MAPPING);
         for (int x = 0; x < nodes.getLength(); x++) {
             Node e = nodes.item(x);
             String servletName = extractChildNodeValue(e, ELEMENT_SERVLET_NAME);
            String[] urlPatterns = extractChildNodesValues(e, ELEMENT_URL_PATTERN);
            if (urlPatterns.length == 0) {
                throw new WebXmlParseException("Missing '" + ELEMENT_URL_PATTERN + "' under '" + e.getNodeName() + "' for servlet name '" + servletName +"'");
            }
            for (String urlPattern : urlPatterns) {
                webXml.addServletMappingDefinition(servletName, urlPattern);
            }
         }
     }
     
     private void parseFilterDefinitions(Document document, MutableWebXml webXml) {
         NodeList nodes = document.getElementsByTagName(ELEMENT_FILTER);
         for (int x = 0; x < nodes.getLength(); x++) {
             Node e = nodes.item(x);
             String filterName = extractChildNodeValue(e, ELEMENT_FILTER_NAME);
             String filterClassName = extractChildNodeValue(e, ELEMENT_FILTER_CLASS);
             MutableFilterDefinition filterDefinition = webXml.addFilterDefinition(filterName, filterClassName);
             parseInitParameters(e, filterDefinition);
         }
     }
     
     private void parseFilterMappingDefinitions(Document document, MutableWebXml webXml) {
         NodeList nodes = document.getElementsByTagName(ELEMENT_FILTER_MAPPING);
         for (int x = 0; x < nodes.getLength(); x++) {
             Node e = nodes.item(x);
             
             String filterName = extractChildNodeValue(e, ELEMENT_FILTER_NAME);
             
             String[] servletNames = extractChildNodesValues(e, ELEMENT_SERVLET_NAME);
             String[] urlPatterns = extractChildNodesValues(e, ELEMENT_URL_PATTERN);
             
             Set<FilterDispatcherType> dispatcherTypes = getConfiguredDispatcherTypes(e);
             
             if (servletNames.length == 0 && urlPatterns.length == 0) {
                 throw new WebXmlParseException("Missing '" + ELEMENT_SERVLET_NAME + "' or '" + ELEMENT_URL_PATTERN + "' under '" + e.getNodeName() + "'");
             }
             
             for (String servletName : servletNames) {
                 webXml.addServletNameFilterMappingDefinition(filterName, servletName, dispatcherTypes);
             }
             
             for (String urlPattern : urlPatterns) {
                 webXml.addUrlPatternFilterMappingDefinition(filterName, urlPattern, dispatcherTypes);
             }      
         }
     }
 
     private Set<FilterDispatcherType> getConfiguredDispatcherTypes(Node e) {
         String[] values = extractChildNodesValues(e, ELEMENT_DISPATCHER);
         
         Set<FilterDispatcherType> dispatcherTypes = new HashSet<FilterDispatcherType>();
         
         for (String dispatcherType : values) {
             dispatcherTypes.add(FilterDispatcherType.valueOf(dispatcherType));
         }
         
         if (dispatcherTypes.isEmpty()) {
             dispatcherTypes.add(FilterDispatcherType.REQUEST);
         }
         return dispatcherTypes;
     }
     
 
     private String extractChildNodeValue(Node node, String childName) {
         NodeList childNodes = node.getChildNodes();
         for (int x = 0; x < childNodes.getLength(); x++) {
             Node child = childNodes.item(x);
             if (childName.equals(child.getNodeName())) {
                 return child.getTextContent().trim();
             }
         }        
         throw new WebXmlParseException("Missing '" + childName + "' under '" + node.getNodeName() + "'");        
     }    
     
     private String[] extractChildNodesValues(Node node, String childName) {
         List<String> values = new ArrayList<String>();
         
         NodeList childNodes = node.getChildNodes();
         for (int x = 0; x < childNodes.getLength(); x++) {
             Node child = childNodes.item(x);
             if (childName.equals(child.getNodeName())) {
                 values.add(child.getTextContent().trim());
             }
         }
         
         return values.toArray(new String[values.size()]);                
     }
 
     private Document loadDocument(InputStream resource) {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         try {
             return factory.newDocumentBuilder().parse(resource);
         } catch (SAXException e) {
             logger.error("Error parsing web.xml file", e);
             throw new WebXmlParseException("Error parsing web.xml file.", e);
         } catch (IOException e) {
             logger.error("Unable to read web.xml file", e);
             throw new WebXmlParseException("Unable to read web.xml file.", e);
         } catch (ParserConfigurationException e) {
             logger.error("Unable to create XML parser", e);
             throw new WebXmlParseException("Unable to create XML parser.", e);
         }
     }
 }
