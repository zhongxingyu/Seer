 /**
  * Copyright 2011-2012 Alexandre Dutra
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package fr.dutra.confluence2wordpress.util;
 
 import static com.atlassian.confluence.content.render.xhtml.XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_ALTERNATE_PREFIX;
 import static com.atlassian.confluence.content.render.xhtml.XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_PREFIX;
 import static com.atlassian.confluence.content.render.xhtml.XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_URI;
 import static com.atlassian.confluence.content.render.xhtml.XhtmlConstants.RESOURCE_IDENTIFIER_NAMESPACE_PREFIX;
 import static com.atlassian.confluence.content.render.xhtml.XhtmlConstants.RESOURCE_IDENTIFIER_NAMESPACE_URI;
 import static com.atlassian.confluence.content.render.xhtml.XhtmlConstants.XHTML_NAMESPACE_URI;
 
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.commons.io.IOUtils;
 
 import com.atlassian.confluence.core.ContentEntityObject;
 
 
 public class StaxUtils {
 
 	/**
 	 * @see com.atlassian.confluence.content.render.xhtml.DefaultXmlEventReaderFactory
 	 */
     private static final XMLInputFactory INPUT_FACTORY;
     
     static {
     	INPUT_FACTORY = XMLInputFactory.newInstance();
     	INPUT_FACTORY.setProperty("javax.xml.stream.supportDTD", Boolean.TRUE);
     	INPUT_FACTORY.setProperty("javax.xml.stream.isCoalescing", Boolean.FALSE);
     	INPUT_FACTORY.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
     	INPUT_FACTORY.setProperty("com.ctc.wstx.normalizeAttrValues", Boolean.FALSE);
     	//javax.xml.stream.resolver
     }
 
     private static final String XHTML_LAT1_DTD;
     private static final String XHTML_SPECIAL_DTD;
     private static final String XHTML_SYMBOL_DTD;
     
     static {
     	String xhtml_lat1 = null;
     	try {
 			xhtml_lat1 = IOUtils.toString(StaxUtils.class.getResourceAsStream("/dtd/xhtml-lat1.ent"));
 		} catch (IOException e) {
 		}
     	XHTML_LAT1_DTD = xhtml_lat1;
     	String xhtml_special = null;
     	try {
     		xhtml_special = IOUtils.toString(StaxUtils.class.getResourceAsStream("/dtd/xhtml-special.ent"));
 		} catch (IOException e) {
 		}
     	XHTML_SPECIAL_DTD = xhtml_special;
     	String xhtml_symbol = null;
     	try {
    		xhtml_symbol = IOUtils.toString(StaxUtils.class.getResourceAsStream("/dtd/xhtml-special.ent"));
 		} catch (IOException e) {
 		}
     	XHTML_SYMBOL_DTD = xhtml_symbol;
     }
 
     private static final String XML_START = 
     	"<!DOCTYPE storage [" +
 		XHTML_LAT1_DTD +
 		XHTML_SPECIAL_DTD +
 		XHTML_SYMBOL_DTD +
     	"]>" +
         "<xml " +
         "xmlns=\"" + XHTML_NAMESPACE_URI + "\" " +
         "xmlns:"+CONFLUENCE_XHTML_NAMESPACE_PREFIX+"=\""+CONFLUENCE_XHTML_NAMESPACE_URI+"\" "+
         "xmlns:"+CONFLUENCE_XHTML_NAMESPACE_ALTERNATE_PREFIX+"=\""+CONFLUENCE_XHTML_NAMESPACE_URI+"\" "+
         "xmlns:"+RESOURCE_IDENTIFIER_NAMESPACE_PREFIX+"=\""+RESOURCE_IDENTIFIER_NAMESPACE_URI+"\" " +
         ">" ;
     
     private static final String XML_END = 
         "</xml>" ;
 
     public static XMLEventReader getReader(ContentEntityObject page) throws XMLStreamException{
         return getXMLEventReader(page.getBodyAsString());
     }
     
     public static XMLEventReader getXMLEventReader(String storage) throws XMLStreamException {
         return INPUT_FACTORY.createXMLEventReader(getReader(storage));
     }
 
     public static StringReader getReader(String storage) throws XMLStreamException {
         return new StringReader(XML_START + storage + XML_END);
     }
 }
