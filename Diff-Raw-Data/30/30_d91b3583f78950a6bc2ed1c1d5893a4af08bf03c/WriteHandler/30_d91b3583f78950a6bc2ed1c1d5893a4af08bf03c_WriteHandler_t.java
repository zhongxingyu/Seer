 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.stax.handler;
 
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * @author ??
  */
 public abstract class WriteHandler extends DefaultHandler {
 
     private static final Pattern SPLIT_PATTERN = Pattern.compile(":");
 
     private XMLStreamWriter writer;
 
     // must be initialized if an instance is created, see creation in
     // MultipleExtractor
     private Map<String, List> nsuris;
 
     private int deepLevel;
 
     protected XMLStreamWriter getWriter() {
         return this.writer;
     }
 
     protected void setWriter(final XMLStreamWriter writer) {
         this.writer = writer;
     }
 
     protected Map<String, List> getNsuris() {
         return this.nsuris;
     }
 
     protected void setNsuris(final Map<String, List> nsuris) {
         this.nsuris = nsuris;
     }
 
     protected int getDeepLevel() {
         return this.deepLevel;
     }
 
     protected void increaseDeepLevel() {
         this.deepLevel++;
     }
 
     protected void decreaseDeepLevel() {
         this.deepLevel--;
     }
 
     protected void writeElement(final StartElement element) throws XMLStreamException {
 
         final String name = element.getLocalName();
         final String uri = element.getNamespace();
         String prefix = element.getPrefix();
 
         if (uri != null) {
             if (nsuris.containsKey(uri)) {
                 final List namespaceTrace = nsuris.get(uri);
                 final Integer deepLevelInMAp = (Integer) namespaceTrace.get(0);
                 final String prefixTrace = (String) namespaceTrace.get(2);
                 if (prefixTrace == null || !prefixTrace.equals(prefix)) {
                     prefix = prefixTrace;
                 }
                 if (deepLevelInMAp >= this.deepLevel) {
                     writer.writeStartElement(prefix, name, uri);
                     writer.writeNamespace(prefix, uri);
                 }
                 else {
                     writer.writeStartElement(prefix, name, uri);
                 }
             }
             else {
                 final List namespaceTrace = new ArrayList();
                 namespaceTrace.add(this.deepLevel);
                 namespaceTrace.add(name);
                 namespaceTrace.add(prefix);
                 nsuris.put(uri, namespaceTrace);
                 writer.writeStartElement(prefix, name, uri);
                 writer.writeNamespace(prefix, uri);
             }
         }
         else {
             writer.writeStartElement(name);
         }
 
     }
 
     protected void writeAttribute(
         String uri, final String elementName, final String attributeName, final String attributeValue, String prefix,
         final NamespaceContext nscontext) throws XMLStreamException {
         if (uri != null) {
            if (uri.isEmpty()) {
                 uri = nscontext.getNamespaceURI(prefix);
             }
             if (nsuris.containsKey(uri)) {
                 final List namespaceTrace = nsuris.get(uri);
                 final String prefixTrace = (String) namespaceTrace.get(2);
                 if (!prefixTrace.equals(prefix)) {
                     prefix = prefixTrace;
                 }
                 // Integer deepLevelInMAp = (Integer) namespaceTrace.get(0);
                 // String nameTrace = (String) namespaceTrace.get(1);
                 // if ( (deepLevelInMAp.intValue() >= deepLevel)) {
                 // writer.writeNamespace(prefix, uri);
                 // }
             }
             else {
                 final List namespaceTrace = new ArrayList<String>();
                 namespaceTrace.add(this.deepLevel);
                 namespaceTrace.add(elementName);
                 namespaceTrace.add(prefix);
                 nsuris.put(uri, namespaceTrace);
                 writer.writeNamespace(prefix, uri);
             }
         }
         if (prefix != null) {
             final String nameUri = nscontext.getNamespaceURI(prefix);
             if (nameUri != null && nameUri.equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)) {
 
                 final String[] attributeValueArray = SPLIT_PATTERN.split(attributeValue);
                 if (attributeValueArray.length == 2) {
                     final int index = attributeValueArray[1].indexOf('/');
                     if (index == -1) {
                         final String prefixValue = attributeValueArray[0];
                         if (prefixValue != null) {
                             final String valueUri = nscontext.getNamespaceURI(prefixValue);
                             if (valueUri != null && !nsuris.containsKey(valueUri)) {
                                 final List namespaceTrace = new ArrayList();
                                 namespaceTrace.add(this.deepLevel);
                                 namespaceTrace.add(elementName);
                                 namespaceTrace.add(prefixValue);
                                 nsuris.put(valueUri, namespaceTrace);
 
                                 writer.writeNamespace(prefixValue, valueUri);
                             }
                         }
                     }
                 }
             }
         }
         writer.writeAttribute(prefix, uri, attributeName, attributeValue);
 
     }
 
     protected void handleAttributeInInsideElement(
         final Attribute attribute, final NamespaceContext nscontext, final String theName) throws XMLStreamException {
         final String attLocalName = attribute.getLocalName();
         final String attrNameSpace = attribute.getNamespace();
         final String attrPrefix = attribute.getPrefix();
         final String attValue = attribute.getValue();
         writeAttribute(attrNameSpace, theName, attLocalName, attValue, attrPrefix, nscontext);
 
     }
 
     protected String handleAttributeInOutsideElement(
         final Attribute attribute, final NamespaceContext nscontext, final String theName, final String attributeName)
         throws XMLStreamException {
         String attributeValue = null;
         final String attLocalName = attribute.getLocalName();
         final String attrNameSpace = attribute.getNamespace();
         final String attrPrefix = attribute.getPrefix();
         final String attValue = attribute.getValue();
         if (attributeName != null && attLocalName.equals(attributeName)) {
             attributeValue = attValue;
         }
         if (!"md-record".equals(theName) && !"admin-descriptor".equals(theName)) {
             writeAttribute(attrNameSpace, theName, attLocalName, attValue, attrPrefix, nscontext);
         }
         return attributeValue;
     }
 }
