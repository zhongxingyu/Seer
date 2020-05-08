 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.chemistry.webdav;
 
 import com.bradmcevoy.http.Range;
 import com.bradmcevoy.http.ReplaceableResource;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.generationjava.io.xml.PrettyPrinterXmlWriter;
 import com.generationjava.io.xml.SimpleXmlWriter;
 import com.generationjava.io.xml.XmlWriter;
 import org.apache.chemistry.Document;
 import org.apache.chemistry.Property;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.*;
 import java.util.Map;
 
 /**
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class TextDocumentResource extends DocumentResource implements ReplaceableResource {
     private static final Log LOG = LogFactory.getLog(TextDocumentResource.class);
 
     public TextDocumentResource(ChemistryResourceFactory resourceFactory, String path, Document document) {
         super(resourceFactory, path, document);
     }
 
     /**
      * {@inheritDoc}
      */
     public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException {
        // TODO: use StAX XMLStreamWriter instead?
         if (LOG.isDebugEnabled()) {
             LOG.debug("Sending object " + object.getId() + " as XML (range=" + range
                     + ",params=" + params + ", contentType=" + contentType+ ")");
         }
         final XmlWriter xml = new PrettyPrinterXmlWriter(new SimpleXmlWriter(new PrintWriter(out)));
         xml.writeXmlVersion();
 
         xml.writeEntity("document");
         xml.writeAttribute("objectId", object.getId());
         xml.writeAttribute("versionSeriesId", emptyIfNull(object.getVersionSeriesId()));
         xml.writeAttribute("typeId", object.getTypeId());
 
         xml.writeEntity("name").writeCData(object.getName()).endEntity();
         xml.writeEntity("properties");
         for (Map.Entry<String, Property> entry : object.getProperties().entrySet()) {
             xml.writeEntity("property");
             xml.writeAttribute("name", entry.getKey());
             xml.writeAttribute("type", entry.getValue().getDefinition().getType().name());
             
             xml.writeEntity("value");
             final Serializable value = entry.getValue().getValue();
             if (value != null) {
                 if (value instanceof String) {
                     xml.writeCData(value.toString());
                 } else {
                     xml.writeText(emptyIfNull(value));
                 }
             }
             xml.endEntity();    // value
 
             xml.endEntity();    // property
         }
         xml.endEntity();    // properties
 
         xml.endEntity();    // document
         xml.close();
     }
 
     public void replaceContent(InputStream in, Long length) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Replacing content for object " + object.getId());
         }
         processXmlProperties(in);
         object.save();
     }
 
     protected void processXmlProperties(InputStream in) {
         final XMLInputFactory factory = XMLInputFactory.newInstance();
         try {
             final XMLStreamReader parser = factory.createXMLStreamReader(in);
             for (int event = parser.next();
                  event != XMLStreamConstants.END_DOCUMENT;
                  event = parser.next()) {
                 switch (event) {
                     case XMLStreamConstants.START_ELEMENT:
                         if ("property".equals(parser.getLocalName())) {
                             processProperty(parser);
                         } else if ("name".equals(parser.getLocalName())) {
                             processName(parser);
                         }
 
                 }
             }
         } catch (XMLStreamException e) {
             throw new RuntimeException("Failed to replace content: " + e.getMessage(), e);
         }
     }
 
     /**
      * Set the value of a property, stream points to the start of the property tag.
      *
      * @param parser    the XML parser
      * @throws XMLStreamException   on parsing errors
      */
     protected void processProperty(XMLStreamReader parser) throws XMLStreamException {
         int level = 0;
         String name = null;
         for (int i = 0; i < parser.getAttributeCount(); i++) {
             if ("name".equals(parser.getAttributeName(i).getLocalPart())) {
                 name = parser.getAttributeValue(i);
                 break;
             }
         }
         if (name == null) {
             if (LOG.isTraceEnabled()) {
                 LOG.trace("property without name attribute encountered");
             }
             return;
         }
 
         String value = null;
         for (int event = parser.nextTag();
              event != XMLStreamConstants.END_DOCUMENT && level >= 0;
              event = parser.nextTag()) {
             switch (event) {
                 case XMLStreamConstants.START_ELEMENT:
                     if ("value".equals(parser.getLocalName())) {
                         value = parser.getElementText().trim();
                     } else if ("name".equals(parser.getLocalName())) {
                         name = parser.getElementText();
                     } else {
                         level++;
                     }
                     break;
                 case XMLStreamConstants.END_ELEMENT:
                     level--;
                     break;
             }
         }
 
         if (value != null) {
             if (LOG.isTraceEnabled()) {
                 LOG.trace("Setting field " + name + " to " + value);
             }
             try {
                 object.setValue(name, value);
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Failed to set field " + name + " (ignored): " + e.getMessage());
                 }
             }
         }
     }
 
     /**
      * Set a new name for the object, stream points to the start of the name tag.
      *
      * @param parser    the XML parser
      * @throws XMLStreamException   on parsing errors
      */
     protected void processName(XMLStreamReader parser) throws XMLStreamException {
         final String name = parser.getElementText().trim();
         if (!name.equals(object.getName())) {
             if (LOG.isTraceEnabled()) {
                 LOG.trace("Setting new name: " + name);
             }
             object.setName(name);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getContentType(String accepts) {
         return "application/xml";
     }
 
     /**
      * {@inheritDoc}
      */
     public Long getContentLength() {
         return null;
     }
 
     protected String emptyIfNull(Object o) {
         return o == null ? "" : o.toString();
     }
 }
