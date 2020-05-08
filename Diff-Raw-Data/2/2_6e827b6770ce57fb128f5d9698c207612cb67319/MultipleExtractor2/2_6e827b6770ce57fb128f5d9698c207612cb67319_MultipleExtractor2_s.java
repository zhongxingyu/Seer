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
 
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.EndElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class MultipleExtractor2 extends DefaultHandler {
 
     private boolean inside;
 
     private final Map<String, String> paths;
 
     private XMLStreamWriter writer;
 
     private int insideLevel;
 
     private Map<String, OutputStream> metadata;
 
     private Map<String, Map> components;
 
     private final Map<String, Object> outputStreams = new HashMap<String, Object>();
 
     private String componentId;
 
     private boolean inComponent;
 
     private final StaxParser parser;
 
     private Map<String, String> namespaceMap;
 
     private int number;
 
     private List<String> pids;
 
     /**
      * @param namespaceMap Map from namespace to prefix.
      * @param extractPathes
      * @param parser
      */
     public MultipleExtractor2(final Map<String, String> namespaceMap, final Map<String, String> extractPathes,
         final StaxParser parser) {
 
         // FIXME: as parameter
         this.namespaceMap = namespaceMap;
 
         this.parser = parser;
         this.paths = extractPathes;
     }
 
     public MultipleExtractor2(final Map<String, String> extractPathes, final StaxParser parser) {
 
         this(null, extractPathes, parser);
     }
 
     public MultipleExtractor2(final String extractPath, final String extractAtt, final StaxParser parser) {
 
         this.parser = parser;
         this.paths = new HashMap<String, String>();
         this.paths.put(extractPath, extractAtt);
     }
 
     public Map<String, Object> getOutputStreams() {
 
         return this.outputStreams;
     }
 
     public void setPids(final List<String> pids) {
 
         this.pids = pids;
     }
 
     @Override
     public StartElement startElement(final StartElement element) throws XMLStreamException {
         final String elementName = element.getLocalName();
         if ("component".equals(elementName)) {
             this.inComponent = true;
             if (this.pids != null) {
                 this.componentId = pids.get(this.number);
                 this.number++;
             }
             else {
                 final int index = element.indexOfAttribute(null, "objid");
                 if (index != -1) {
                     final String value = element.getAttribute(index).getValue();
                     if (value != null && value.length() > 0) {
                         this.componentId = value;
                     }
                 }
 
             }
         }
 
         if (!this.inside) {
             final String currentPath = parser.getCurPath();
             if (paths.containsKey(currentPath)) {
                 if (this.insideLevel != 0) {
                     throw new XMLStreamException("insideLevel != 0: " + this.insideLevel);
                 }
 
                 this.inside = true;
                 final ByteArrayOutputStream out = new ByteArrayOutputStream();
                 this.writer = newInitializedWriter(out);
 
                 final String attributeName = paths.get(currentPath);
                 if (this.inComponent) {
                     if (this.components == null) {
                         this.components = new HashMap<String, Map>();
                         outputStreams.put("components", this.components);
                     }
                     final Map<String, Object> component;
                     if (components.containsKey(this.componentId)) {
                         component = (HashMap) components.get(this.componentId);
                     }
                     else {
                         component = new HashMap<String, Object>();
                         components.put(this.componentId, component);
                     }
 
                     if (attributeName == null) {
                         component.put(elementName, out);
                     }
                     else {
                         final String attributeValue = getAttributeValue(element, null, attributeName);
                         if ("md-record".equals(elementName)) {
                            Map<String, OutputStream> mdRecords = components.get(this.components);
                             if (mdRecords == null) {
                                 mdRecords = new HashMap<String, OutputStream>();
                                 component.put("md-records", mdRecords);
                             }
                             mdRecords.put(attributeValue, out);
                         }
                         else {
                             component.put(attributeValue, out);
                         }
                     }
                 }
                 else {
                     if (attributeName == null) {
                         outputStreams.put(elementName, out);
                     }
                     else {
                         final String attributeValue = getAttributeValue(element, null, attributeName);
                         if ("md-record".equals(elementName)) {
                             if (this.metadata == null) {
                                 this.metadata = new HashMap<String, OutputStream>();
                                 outputStreams.put("md-records", this.metadata);
                             }
                             metadata.put(attributeValue, out);
                         }
                         else {
                             outputStreams.put(attributeValue, out);
                         }
                     }
                 }
             }
         }
 
         // write start element with attributes (and implicit neccessary
         // namespace declarations due to the repairing xml writer
         if (this.inside) {
             String namespace = element.getNamespace();
             if (namespace != null && !namespaceMap.containsKey(namespace)) {
                 final String prefix = element.getPrefix();
                 if (prefix != null) {
                     writer.setPrefix(prefix, element.getNamespace());
                 }
                 else {
                     writer.setDefaultNamespace(element.getNamespace());
                 }
             }
 
             if (!("md-record".equals(elementName) && paths.containsKey(parser.getCurPath()))) {
                 writer.writeStartElement(element.getNamespace(), elementName);
             }
             final int attCount = element.getAttributeCount();
             for (int i = 0; i < attCount; i++) {
                 final Attribute curAtt = element.getAttribute(i);
                 namespace = curAtt.getNamespace();
                 if (namespace != null && !namespaceMap.containsKey(namespace)) {
                     // Prefix is not null. (FRS)
                     writer.setPrefix(curAtt.getPrefix(), namespace);
                 }
                 if (!("md-record".equals(elementName) && paths.containsKey(parser.getCurPath()))) {
                     writer.writeAttribute(namespace, curAtt.getLocalName(), curAtt.getValue());
                 }
             }
             this.insideLevel++;
         }
 
         // this has to be the last handler
         return element;
     }
 
     @Override
     public EndElement endElement(final EndElement element) throws XMLStreamException {
         final String theName = element.getLocalName();
 
         if ("component".equals(theName)) {
             if (this.componentId == null) {
                 final Map components = (Map) outputStreams.get("components");
                 components.remove(this.componentId);
             }
             this.inComponent = false;
             this.componentId = null;
 
         }
         if (this.inside) {
             this.insideLevel--;
             if (this.insideLevel > 0 || this.insideLevel == 0 && !"md-record".equals(theName)) {
                 writer.writeEndElement();
             }
 
             if (this.insideLevel == 0) {
                 this.inside = false;
                 writer.flush();
                 writer.close();
             }
         }
 
         return element;
     }
 
     @Override
     public String characters(final String data, final StartElement element) throws XMLStreamException {
 
         if (this.inside) {
             writer.writeCharacters(data);
         }
         return data;
     }
 
     /**
      * Creates a new initialized writer.<br/> The writer's prefixes are initialized to the values of the prefixMap.
      *
      * @param out
      * @return Returns the initialized <code>XmlStreamWriter</code>X instance.
      * @throws javax.xml.stream.XMLStreamException
      */
     private XMLStreamWriter newInitializedWriter(final ByteArrayOutputStream out) throws XMLStreamException {
 
         final XMLStreamWriter writer = XmlUtility.createXmlStreamWriterNamespaceRepairing(out);
         if (this.namespaceMap != null && !namespaceMap.isEmpty()) {
             for (final Entry<String, String> stringStringEntry : namespaceMap.entrySet()) {
                 final String prefix = stringStringEntry.getValue();
                 if (prefix != null) {
                     writer.setPrefix(prefix, stringStringEntry.getKey());
                 }
                 else {
                     writer.setDefaultNamespace(stringStringEntry.getKey());
                 }
             }
         }
         return writer;
     }
 
 }
