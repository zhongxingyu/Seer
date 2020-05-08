 package com.semsaas.jsonxml;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.util.LinkedList;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.NamespaceSupport;
 
 
 /**
  * This class processes XJson object documents 
  * 
  * Here is an example xjson XML document
  * <object xmlns="http://www.objectml.org/ns/data/xjson">
  *   <value name="firstname">Benjamin</value>
  *   <array name="children">
  *     <object>
  *       <value name="firstname">Alex</value>
  *     </object>
  *   </array>
  * </object>
  * 
  * 
  * @author benjamin
  *
  */
 public class XMLJsonGenerator extends DefaultHandler {
 	private StringBuffer currentValue = null;
 	private NamespaceSupport prefixes;
 
 	LinkedList<String> nameStack = new LinkedList<String>();
 	JsonFactory jfactory = new JsonFactory();
 	JsonGenerator jGenerator = null;
 	
 	public XMLJsonGenerator() {
 		prefixes = new NamespaceSupport();
 	}
 	
 	public void setOutputStream(OutputStream os) throws IOException {
 		jGenerator = jfactory.createJsonGenerator(os);
 	}
 
 	public void setOutputWriter(Writer w) throws IOException {
 		jGenerator = jfactory.createJsonGenerator(w);
 	}
 	
 	public void startDocument() throws SAXException {
 	}

	int depth = 0;
 	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
 		prefixes.pushContext();
		depth++;
 		
 		try {
 			if(XJSON.XMLNS.equals(uri)) {
 				String fieldName = atts.getValue(XJSON.nameAttrLocalName);
 				nameStack.push(fieldName);
 				
 				// <object>
 				if(XJSON.objectLocalName.equals(localName)) {
					if(fieldName != null && depth > 1) {
 						jGenerator.writeObjectFieldStart(fieldName);
 					} else {
 						jGenerator.writeStartObject();
 					}
 				}
 				
 				// <array>
 				else if(XJSON.arrayLocalName.equals(localName)) {
					if(fieldName != null && depth > 1) {
 						jGenerator.writeArrayFieldStart(fieldName);
 					} else {
 						jGenerator.writeStartArray();
 					}
 				}
 				
 				// <value>
 				else if(XJSON.valueLocalName.equals(localName)) {
 					currentValue = new StringBuffer();
 				}
 				
 				else {
 					throw new InvalidXJsonException("Unknown XJSON element "+localName);
 				}
 			}
 		} catch (JsonGenerationException e) {
 			throw new SAXException(e);
 		} catch (IOException e) {
 			throw new SAXException(e);
 		}
 	}
 	
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		if(currentValue != null) {
 			if(currentValue.length() > 0) {
 				currentValue.append(' ');
 			}
 			currentValue.append(ch,start,length);
 		}
 	}
 
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		try {
 			if(XJSON.XMLNS.equals(uri)) {
 				String fieldName = nameStack.pop();
 				
 				// </object>
 				if(XJSON.objectLocalName.equals(localName)) {
 					jGenerator.writeEndObject();
 				}
 				
 				// </array>
 				else if(XJSON.arrayLocalName.equals(localName)) {
 					jGenerator.writeEndArray();
 				}
 				
 				// </value>
 				else if(XJSON.valueLocalName.equals(localName)) {
					if(fieldName != null && depth > 1) {
 						jGenerator.writeStringField(fieldName, currentValue.toString());
 					} else {
 						jGenerator.writeString(currentValue.toString());
 					}
 					currentValue = new StringBuffer();
 				}
 				
 				else {
 					throw new InvalidXJsonException("Unknown XJSON element "+localName);
 				}
 			}
 			prefixes.popContext();
			depth--;
 		} catch (JsonGenerationException e) {
 			throw new SAXException(e);
 		} catch (IOException e) {
 			throw new SAXException(e);
 		}
 	}
 
 	public void endDocument() throws SAXException {
 		try {
 			jGenerator.close();
 		} catch (IOException e) {
 			throw new SAXException(e);
 		}
 	}
 
 	public void startPrefixMapping(String prefix, String uri) throws SAXException {
 		prefixes.declarePrefix(prefix, uri);
 	}
 }
