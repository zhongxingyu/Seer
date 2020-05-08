 package eu.choreos.vv;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Stack;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import eu.choreos.vv.exceptions.ParserException;
 
 
 /**
  * Utility class to parse the Soap XML response of a Web Service operation
  * Obs: it's a package visibility class, since it's not used by the client
  * 
  * @author Leonardo Leite, Guilherme Nogueira
  *
  */
 class ResponseParser {
 
 	private static SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 	private SAXParser parser;
 	private ItemImpl result;
 
 
 	private class ResponseParserHandler extends DefaultHandler {
 		private Stack<ItemImpl> tagStack = new Stack<ItemImpl>();
 
 		/**
 		 * @param ch     - The characters.
 		 * @param start  - The start position in the character array.
 		 * @param length - The number of characters to use from the character array.
 		 */
 		@Override
 		public void characters(char[] ch, int start, int lenght) 
 		throws SAXException {
 
 			String trimmed = new String(ch, start, lenght).trim();
 
 			if (!trimmed.isEmpty() && !tagStack.empty()){
 				tagStack.peek().setContent(trimmed);			
 			}
 		}
 
 		/**
 		 * @param uri       - The Namespace URI.
 		 * @param localName - The local name (without prefix).
 		 * @param qName     - The qualified name (with prefix).
 		 */
 		@Override
 		public void endElement(String uri, String localName, String qName)
 		throws SAXException {
 
 			if (!tagStack.empty()) {
 				ItemImpl poped = tagStack.pop();
 				if (!tagStack.empty()){
 					ItemImpl father = tagStack.peek();
 					father.addChild(poped);
 				} else {
 					result = poped;
 				}
 			}
 
 		}
 
 		/**
 		 *	@param uri        - The Namespace URI.
 		 *	@param localName  - The local name (without prefix).
 		 *	@param qName      - The qualified name (with prefix).
 		 *	@param attributes - The attributes attached to the element.
 		 */
 		@Override
 		public void startElement(String uri, String localName, String qName, Attributes attributes) 
 		throws SAXException {
 
 			String name = getNameWithoutNamespace(qName);
 
 			if (name.endsWith("Response") || !tagStack.empty()) {
 				HashMap<String, String> parameters = new HashMap<String, String>();
 
 				for(int i=0; i< attributes.getLength(); i++)
 					parameters.put(attributes.getQName(i), attributes.getValue(i));
 
 				result = new ItemImpl(name, parameters);
 				tagStack.push(result);
 			}
 		}
 
 		private String getNameWithoutNamespace(String qName) {
 			String[] names = qName.split(":");                        
 
 			return names[names.length  - 1];
 		}
 	}
 
 	public Item parse(String xml) throws ParserException {
 		try {
			if  (xml == null)
				return null;
			
 			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
 			parser = parserFactory.newSAXParser();
 			parser.parse(is, new ResponseParserHandler());
 		}  
 		catch (Exception e) {
 			throw new ParserException(e);
 		}
 
 		return result;
 	}
 
 }
