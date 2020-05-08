 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.jive.api.xml;
 
 import static javax.xml.stream.XMLStreamConstants.*;
 
 import java.io.Reader;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.UnhandledException;
 
 public class XmlMapper
 {
 
     /**XML output factory to write xml.
      * */
     private final XMLOutputFactory xmlOutputFactory =
         XMLOutputFactory.newInstance();
 
     /**XML input factory to read xml.
      * */
     private final XMLInputFactory xmlInputFactory =
         XMLInputFactory.newInstance();
     
     /**Deletes an entity.
      * @return The xml response parse in a {@link Map}.
      * @param type The service type used to determine the url for this resource.
      * @param id The id to be added in the url as path parameter.
      * */
     public final void map2xml(final String xmlRootTag, final Map<String, Object> entity, final Writer writer) 
     {
         try 
         {
             final XMLStreamWriter w =
                     xmlOutputFactory.createXMLStreamWriter(writer);
 
             w.writeStartDocument();
 
             w.writeStartElement(xmlRootTag);
 
             writeXML(w, entity);
 
             w.writeEndElement();
 
             w.writeEndDocument();
         } 
         catch (XMLStreamException e) 
         {
             throw new UnhandledException(e);
         }
     }
 
   
 
     ///////////////////////////Private methods//////////////////////////////////
 
     
 
     /**Writes the xml of the internal data.
      * @param w The writer in which it'll write the xml
      * @param model The entity
      * @throws XMLStreamException When fails
      * */
     @SuppressWarnings("unchecked")
     private void writeXML(final XMLStreamWriter w,
                    final Map<String, Object> model) throws XMLStreamException 
                    {
         final Set<Entry<String, Object>> entries = model.entrySet();
         for (final Entry<String, Object> entry : entries) 
         {
             if (List.class.isInstance(entry.getValue())) 
             {
                 for (final String elem : (List<String>) entry.getValue()) 
                 {
                     w.writeStartElement(entry.getKey());
                     w.writeCharacters(elem);
                     w.writeEndElement();
                 }
             }
             else 
             {
                 String key = entry.getKey();
                 if (StringUtils.startsWith(key, "return")) 
                 {
                     key = "return";
                 }
                 w.writeStartElement(key);
                 if (!HashMap.class.isInstance(entry.getValue())) 
                 {
                     w.writeCharacters(entry.getValue().toString());
                 } 
                 else 
                 {
                     writeXML(w, (HashMap<String, Object>) entry.getValue());
                 }
                 w.writeEndElement();
             }
         }
     }
     
     /**Maps an xml from a {@link Reader} to a {@link Map}.
      * @param reader The {@link Reader} with the xml data
      * @return The map with the entity data
      * */
     @SuppressWarnings("unchecked")
     public final Map<String, Object> xml2map(final Reader reader) 
     {
         final Map<String, Object> ret = new HashMap<String, Object>();
         final Stack<Map<String, Object>> maps =
             new Stack<Map<String, Object>>();
         Map<String, Object> current = ret;
 
         try 
         {
             final XMLStreamReader r =
                 xmlInputFactory.createXMLStreamReader(reader);
             StringBuilder lastText = new StringBuilder();
             String currentElement = null;
             int returnCount = 0;
             while (r.hasNext())
             {
                 final int eventType = r.next();
                 if (eventType == CHARACTERS || eventType == CDATA
                         || eventType == SPACE
                         || eventType == ENTITY_REFERENCE) 
                 {
                     lastText.append(r.getText());
                 } 
                 else if (eventType == END_DOCUMENT)
                 {
                     break;
                 }
                 else if (eventType == START_ELEMENT)
                 {
                     if (currentElement != null)
                     {
                         maps.push(current);
                         final Map<String, Object> map =
                             new HashMap<String, Object>();
                         if (StringUtils.startsWith(currentElement, "return")) 
                         {
                             currentElement = currentElement + "--" + String.valueOf(returnCount);
                             returnCount++;
                         }
                         current.put(currentElement, map);
                         current = map;
                     }
                     currentElement = r.getLocalName();
                 } 
                 else if (eventType == END_ELEMENT) 
                 {
                     if (currentElement == null) 
                     {
                         current = maps.pop();
                     } 
                     else 
                     {
                         current.put(currentElement, lastText.toString().trim());
                         currentElement = null;
                         lastText = new StringBuilder();
                     }
                 }
                 else 
                 {
                     throw new XMLStreamException("Unexpected event type "
                         + eventType);
                 }
             }
 
             final Object obj = ret.get(ret.keySet().iterator().next());
             if (obj instanceof String) 
             {
                 Map<String, Object> responseTag = new HashMap<String, Object>();
                 responseTag.put("response",
                     ret.keySet().iterator().next().toString());
                 return responseTag;
             } 
             else 
             {
                 final Map<String, Object> returnXMLElement = (Map<String, Object>)
                 ret.get(ret.keySet().iterator().next());
 
                 if (returnXMLElement.keySet().contains("return--1")) 
                 {
                     return returnXMLElement;
                 }
                return (Map<String, Object>) returnXMLElement.get("return");
             }
             
         } 
         catch (XMLStreamException e) 
         {
             throw new UnhandledException(e);
         }
     }
 
 }
 
 
