 package eu.choreos.vv.common;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import eu.choreos.vv.clientgenerator.Item;
 import eu.choreos.vv.exceptions.ParserException;
 
 /**
  * This class supports the Item (complex types) building
  * 
  * @author Felipe Besson
  *
  */
 public class ItemBuilder {
 	private static SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 	private SAXParser parser;
 	private StringBuilder outputBuilder = new StringBuilder();
 	private final String REGEXP =  "<!--.* or more repetitions:-->\\s*(<(\\w*:*\\w+)>.*<\\/\\2>)";
 
 	private class RequestParserHandler extends DefaultHandler {
 		
 		private Integer currentLevel = 0;
 
 		private Item currentItem;
 		private Item item;
 		
 		private boolean foundRoot = false;
 		
 		private class TreeItem {
 			public Integer level = 0;
 			public HashMap<String, List<Item>> itens = new HashMap<String, List<Item>>();
 
 			public TreeItem(Integer level){
 				this.level = level;
 			}
 		}
 		
 		private Stack<TreeItem> itemStack = new Stack<TreeItem>();
 
 		public RequestParserHandler(Item item){
 			super();
 			this.item = item;
 		}
 		
 		/**
 		 * @param ch     - The characters.
 		 * @param start  - The start position in the character array.
 		 * @param length - The number of characters to use from the character array.
 		 */
 		@Override
 		public void characters(char[] ch, int start, int lenght) 
 		throws SAXException {
 
 			String trimmed = new String(ch, start, lenght).trim();
 
 			if (!trimmed.isEmpty()){
 				if(trimmed.equals("?")){
 					if(currentItem == null || currentItem.getContent() == null){
 						throw new SAXException("Expected content for \"" + currentItem.getName() + "\", but found none.");
 					} else {
 						outputBuilder.append(currentItem.getContent());
 					}
 				} else {
 					outputBuilder.append(trimmed);
 				}
 			}
 		}
 
 		/**
 		 * Adds the children of the <code>currentItem</code> to the <code>itemStack</code>
 		 * as a <code>TreeItem</code>
 		 */
 		private void addChildrenToStack(){
 			// inserts all children in the stack as a "bag"
 			TreeItem bag = new TreeItem(currentLevel + 1);
 			for(Item child : currentItem.getChildren()){
 				List<Item> children = bag.itens.get(child.getName());
 
 				if(children == null){
 					children = new LinkedList<Item>();
 					bag.itens.put(child.getName(), children);
 				}
 
 				children.add(child);
 				
 			}
 			
 			if(!bag.itens.isEmpty()){
 				itemStack.push(bag);
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
 			outputBuilder.append("</" + qName + ">");
 			currentLevel--;
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
 	
 			if(itemStack.empty() && name.equals(item.getName())){
 				// root found for the first time
 				foundRoot = true;
 				
 				currentItem = item;
 				addChildrenToStack();
 				
 			} else if(!itemStack.empty() && itemStack.peek().itens.containsKey(name)){
 				if(currentLevel == itemStack.peek().level){
 
 					// Remove from the stack bag and update current item
 					List<Item> itens = itemStack.peek().itens.get(name);
 					currentItem = itens.get(0);
 					itens.remove(0);
 					
 					// if List is empty, remove key from hash
 					if(itens.isEmpty()){
 						itemStack.peek().itens.remove(name);
 					}
 					
 					// Remove bag from stack if it's empty
 					if(itemStack.peek().itens.isEmpty()){
 						itemStack.pop();
 					}
 					
 					addChildrenToStack();
 					
 				} else {
 					throw new SAXException("Found item with wrong hierarchy level. Found \"" 
 							+ currentItem.getName() + "\" on level [" + itemStack.peek().level 
 							+ "], expected \"" + name + "\" on level [" + currentLevel + "]");
 				}
 			} else if(!itemStack.empty() || foundRoot){
 				throw new SAXException("Expected \"" + name + "\" on level [" + currentLevel
 						+ "] but found none.");
 			}
 
 			outputBuilder.append("<" + qName);
 			
 			int numAttributes = attributes.getLength();
 			for(int i = 0; i < numAttributes; i++){
 				String attrName = attributes.getQName(i);
 				String value = attributes.getValue(i);
 				
 				outputBuilder.append(" " + attrName + "=\"" + value + "\"");
 			}
 			
 			outputBuilder.append(">");
 
 			currentLevel++;
 		}
 
 		private String getNameWithoutNamespace(String qName) {
 			String[] names = qName.split(":");                        
 
 			return names[names.length  - 1];
 		}
 	}
 
 	public String buildItem(String baseXml, Item root) throws ParserException {
 		
 		if(root == null){
 			// FIXME: What if there should be parameters?
 			return baseXml;
 		}
 		
 		
 		try {
 			baseXml = addListEntriesToXml(baseXml, root);
 			InputStream is = new ByteArrayInputStream(baseXml.getBytes("UTF-8"));
 			parser = parserFactory.newSAXParser();
 			parser.parse(is, new RequestParserHandler(root));
 		}  
 		catch (Exception e) {
 			throw new ParserException(e);
 		}
 				
 		return outputBuilder.toString();
 	}
 
 	private String addListEntriesToXml(String baseXml, Item root) {
 		String result = baseXml;
 		Pattern LIST_ENTRY = Pattern.compile(REGEXP);
 		Matcher m = LIST_ENTRY.matcher(baseXml);
 		
 		while (m.find()) {
 			String tagName = m.group(2);
 			String formattedTagName = new RequestParserHandler(null).getNameWithoutNamespace(m.group(2));
 			
 			int listSize = root.getListSizeFromItem(formattedTagName);
 			String repeatedEntry = m.group(1).replace(tagName, tagName);
 			
 			String listOfTagName = multiplyString(repeatedEntry, listSize);
			result = result.replaceFirst(REGEXP, listOfTagName);
 		}
 		
 	        return result;
         }
 
 	private String multiplyString(String group, int listSize) {
 	        String resultString = "";
 	        for (int i = 0; i < listSize; i++)
 	        	resultString += group;
 	        
 	        return resultString;
         }
 
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
