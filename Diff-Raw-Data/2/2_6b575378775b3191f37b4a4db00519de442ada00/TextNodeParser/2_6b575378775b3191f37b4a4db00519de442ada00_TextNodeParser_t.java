 /**
  *
  * nutz - Markdown processor for JVM
  * Copyright (c) 2012, Sandeep Gupta
  * 
  * http://www.sangupta/projects/nutz
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.nutz;
 
 import java.util.Arrays;
 
 import com.sangupta.nutz.ast.AnchorNode;
 import com.sangupta.nutz.ast.EmailNode;
 import com.sangupta.nutz.ast.EmphasisNode;
 import com.sangupta.nutz.ast.ImageNode;
 import com.sangupta.nutz.ast.InlineCodeNode;
 import com.sangupta.nutz.ast.Node;
 import com.sangupta.nutz.ast.ParagraphNode;
 import com.sangupta.nutz.ast.PlainTextNode;
 import com.sangupta.nutz.ast.SpecialCharacterNode;
 import com.sangupta.nutz.ast.StrongNode;
 import com.sangupta.nutz.ast.TextNode;
 import com.sangupta.nutz.ast.XmlNode;
 
 /**
  * 
  * @author sangupta
  *
  */
 public class TextNodeParser implements Identifiers {
 	
 	private String line;
 	
 	private int lastConverted;
 	
 	private int pos;
 	
 	private int length;
 	
 	private ParagraphNode root = null;
 
 	/**
 	 * Parse the given markup line and convert it into
 	 * many text nodes.
 	 * 
 	 * @param line
 	 * @return
 	 */
 	public TextNode parse(Node parent, String line) {
 		this.line = line;
 		this.length = line.length();
 		this.pos = 0;
 		this.lastConverted = 0;
 		this.root = new ParagraphNode(parent);
 		
 		// parse into tokens
 		parse();
 		
 		return root;
 	}
 	
 	/**
 	 * Recursively parse the entire string and convert it into various text nodes
 	 * 
 	 */
 	private void parse() {
 		do {
 			if(charAt(pos, ESCAPE_CHARACTER)) {
 				clearPending();
 				pos++;
 				lastConverted++;
 			}
 			
 			if(charAt(pos, HTML_OR_AUTOLINK_START)) {
 				clearPending();
 				parseHtmlOrAutoLinkBlock();
 			}
 			
 			if(charAt(pos, CODE_MARKER) && !charAt(pos + 1, CODE_MARKER) && !charAt(pos - 1, ESCAPE_CHARACTER)) {
 				clearPending();
 				parseCodeBlock();
 			}
 			
 			if(charAt(pos, EXCLAIMATION) && charAt(pos + 1, LINK_START)) {
 				clearPending();
 				parseImageBlock();
 			}
 			
 			if(charAt(pos, LINK_START)) {
 				clearPending();
 				parseLink();
 			}
 			
 			if(charAt(pos, ITALIC_OR_BOLD) && !charAt(pos - 1, ESCAPE_CHARACTER)) {
 				clearPending();
 				parseItalicOrBoldBlock(ITALIC_OR_BOLD);
 			}
 			
 			if(charAt(pos, ITALIC_OR_BOLD_UNDERSCORE) && !charAt(pos - 1, ESCAPE_CHARACTER)) {
 				clearPending();
 				parseItalicOrBoldBlock(ITALIC_OR_BOLD_UNDERSCORE);
 			}
 
 			handleSpecialCharacters();
 			
 			pos++;
 		} while(pos < line.length());
 		
 		clearPending();
 	}
 	
 	/**
 	 * Handle conversion of special characters that need to be escaped in HTML.
 	 * This is needed to make sure that we write the correct set of letters
 	 * and also to keep the speed intact.
 	 */
 	private void handleSpecialCharacters() {
 		if(this.pos >= this.length) {
 			return;
 		}
 		
 		final char character = line.charAt(pos);
 		boolean conversion = false;
 		
 		switch(character) {
 			case AMPERSAND:
 				if(line.length() < pos + 5) {
 					break;
 				}
 				
 				if(!(line.substring(pos + 1, pos + 5).equals("amp;"))) {
 					conversion = true;
 				}
 				break;
 				
 			case HTML_OR_AUTOLINK_START:
 			case HTML_OR_AUTOLINK_END:
 				conversion = true;
 				break;
 		}
 		
 		if(conversion) {
 			// remove the current character and replace
 			clearPending();
 			root.addChild(new SpecialCharacterNode(root, character));
 			pos++;
 			lastConverted = pos;
 		}
 	}
 	
 	/**
 	 * Parse a block of autolinking URL or an email address. If not,
 	 * then see if this is an HTML block. If yes, escape till the end
 	 * of this HTML block so that we can continue working with 
 	 * markdown markup.
 	 */
 	private void parseHtmlOrAutoLinkBlock() {
 		int index = line.indexOf(HTML_OR_AUTOLINK_END, pos + 1);
 		
 		if(index == -1) {
 			// nothing more to do
 			return;
 		}
 		
 		String markup = line.substring(pos + 1, index);
 
 		// hyperlink
 		if(MarkupUtils.isHyperLink(markup)) {
 			root.addChild(new AnchorNode(root, markup));
 			
 			// reset
 			pos = index + 1;
 			lastConverted = pos;
 			
 			return;
 		}
 		
 		// email
 		if(MarkupUtils.isEmail(markup)) {
 			root.addChild(new EmailNode(markup));
 
 			// reset
 			pos = index + 1;
 			lastConverted = pos;
 			
 			return;
 		}
 		
 		// check if this is an XML
 		// extract the tag name from the line
 		String tagName = extractTagName(markup);
 		
 		String end = "</" + tagName + ">";
 		index = line.indexOf(end, pos);
 		if(index == -1) {
 			// no idea what this is
 			// ignore
 			return;
 		}
 		
 		// add an XML block
 		root.addChild(new XmlNode(line.substring(pos, index + end.length())));
 		
 		// reset
 		pos = index + end.length() + 1;
 		lastConverted = pos;
 	}
 
 	private String extractTagName(String markup) {
 		int index = markup.indexOf(SPACE);
 		if(index == -1) {
 			return markup;
 		}
 		
 		return markup.substring(0, index);
 	}
 
 	/**
 	 * Parse and create an image block out of the code just found.
 	 * 
 	 */
 	private void parseImageBlock() {
 		// TODO Auto-generated method stub
 		int index = line.indexOf(LINK_END, pos + 2); // 2 because we have just matched two characters
 		if(index == -1) {
 			// not an image
 			return;
 		}
 		
 		if(!charAt(index + 1, HREF_START)) {
 			// no image url present
 			return;
 		}
 		
 		String title = line.substring(pos + 2, index);
 		pos = index + 1;
 		
 		index = line.indexOf(HREF_END, index + 2);
 		if(index == -1) {
 			return;
 		}
 		
 		String link = line.substring(pos + 1, index);
 		
 		// create the node
 		root.addChild(new ImageNode(link, title));
 		
 		// reset
 		pos = index + 1;
 		lastConverted = pos;
 	}
 
 	/**
 	 * Parse a hyperlink that may have been specified. A hyperlink
 	 * may have more markup inside.
 	 * 
 	 */
 	private void parseLink() {
 		int index = line.indexOf(LINK_END, pos + 1);
 		
 		if(index == -1) {
 			// this is not a hyperlink
 			return;
 		}
 		
 		// check if we do not have an image embedded in it
 		int imageIndex = line.indexOf(IMAGE_IDENTIFIER, pos + 1);
 		if(imageIndex != -1) {
 			// see if this is less than found ending point
 			if(imageIndex < index) {
 				// find next index
 				index = line.indexOf(LINK_END, index + 1);
 			}
 		}
 		
 		if(index == -1) {
 			// this is not a hyperlink
 			return;
 		}
 		
 		// this is the text
 		String linkText = line.substring(pos + 1, index);
 		pos = ++index;
 		
 		// either a URL would be specified
 		// or a reference to another hyperlink
 		// would be provided
 		char ch = line.charAt(index++);
 		while(ch == SPACE) {
 			ch = line.charAt(index++);
 		}
 		
 		if(ch != HREF_START && ch != LINK_START) {
 			// this is not a hyperlink
 			// just plainly exit the loop
 			return;
 		}
 		
 		if(ch == HREF_START) {
 			// extract the URL
 			index = line.indexOf(HREF_END, index + 1);
 			if(index == -1) {
 				// not a hyperlink
 				return;
 			}
 			
 			// extract the actual URL
 			String link = line.substring(pos + 1, index);
 			
 			// see if we have some title in it or not
 			String[] tokens = MarkupUtils.parseLinkAndTitle(link);
 	
 			// create the node
 			AnchorNode anchorNode = new AnchorNode(root, linkText.trim(), tokens[0].trim(), tokens[1], false);
 			root.addChild(anchorNode);
 		} else if(ch == LINK_START) {
 			// this is the text
 			index = line.indexOf(LINK_END, index + 1);
 			if(index == -1) {
 				// not a reference link
 				return;
 			}
 			
 			// extract the identifier
 			String identifier = line.substring(pos + 2, index);
 			AnchorNode anchorNode = new AnchorNode(root, linkText.trim(), identifier, null, true);
 			root.addChild(anchorNode);
 		}
 		
 		// final settlement
 		pos = index + 1;
 		lastConverted = pos;
 	}
 
 	private void parseItalicOrBoldBlock(char terminator) {
 		int count = 1;
 
 		// count the total number of terminators available together
 		int index = 1;
 		do {
 			if(line.charAt(pos + index) == terminator) {
 				index++;
 				count++;
 			} else {
 				break;
 			}
 		} while(true);
 		
 		// now we need to find the total number of terminators after a non-terminator
 		// character
 		index = line.indexOf(terminator, pos + count);
 		if(index == -1) {
 			// none available 
 			// we need to break only the available ones
 			// into nodes
 			convertBoldOrUnderlineTerminatorsToNode(count, terminator);
 			
 			// reset
 			pos = pos + count;
 			lastConverted = pos;
 			
 			return;
 		}
 		
 		// this means that we found out another set of stars
 		// let's find the total number of counts available here
 		int endCount = 1;
 		int checkIndex = 0;
 		do {
 			checkIndex = index + endCount;
 			if(checkIndex == this.length) {
 				break;
 			}
 			
 			if(line.charAt(checkIndex) == terminator) {
 				endCount++;
 			} else {
 				break;
 			}
 		} while(true);
 		
 		String text = line.substring(pos + count, index);
 
 		// now we have the text that is between these starting and ending
 		// terminator string. convert this to markup
 		convertBoldOrUnderlineTerminatedToNode(count, endCount, text);
 		
 		// reset
		pos = index + endCount;
 		lastConverted = pos;
 	}
 
 	private void convertBoldOrUnderlineTerminatedToNode(int startCount, int endCount, String text) {
 		if(startCount == endCount) {
 			switch(startCount) {
 				case 1:
 					// create an italic text node
 					root.addChild(new EmphasisNode(root, text));
 					return;
 					
 				case 2:
 					// create a strong node
 					root.addChild(new StrongNode(root, text));
 					return;
 
 				case 3:
 					StrongNode strongNode = new StrongNode(root);
 					EmphasisNode emphasisNode = new EmphasisNode(strongNode, text);
 					strongNode.setTextNode(emphasisNode);
 					root.addChild(strongNode);
 					return;
 			}
 		}
 		
 		System.out.println("*** Unhandled: " + text + ":" + startCount + ", " + endCount);
 	}
 
 	/**
 	 * Convert a single continuous string or bold or underline
 	 * terminators namely, the star '*' and the underscore '_'
 	 * into a valid markup node object.
 	 * 
 	 * @param count
 	 */
 	private void convertBoldOrUnderlineTerminatorsToNode(int count, char terminator) {
 		if(count <= 2) {
 			char[] array = new char[count];
 			Arrays.fill(array, terminator);
 			root.addChild(new PlainTextNode(root, String.valueOf(array)));
 		}
 		
 		// TODO: we still need to convert these terminators
 	}
 
 	/**
 	 * Clear any pending conversion and make
 	 * it into a text node
 	 * 
 	 */
 	private void clearPending() {
 		if(pos > this.length) {
 			pos = this.length;
 		}
 		
 		if((lastConverted + 1) < pos) {
 			root.addChild(new PlainTextNode(root, line.substring(lastConverted, pos)));
 		}
 		lastConverted = pos;
 	}
 	
 	/**
 	 * Parse an inline code block
 	 * 
 	 */
 	private void parseCodeBlock() {
 		// this is a code block
 		int index = pos;
 		do {
 			index = line.indexOf(CODE_MARKER, index + 1);
 			if(index == -1) {
 				break;
 			}
 			
 			if(!charAt(index + 1, CODE_MARKER)) {
 				break;
 			} else {
 				index++;
 			}
 		} while(true);
 		
 		if(index == -1) {
 			index = line.length() - 1;
 		}
 		
 		// create the node
 		root.addChild(new InlineCodeNode(root, line.substring(pos + 1, index)));
 
 		// reset
 		pos = index + 1;
 		lastConverted = pos;
 	}
 	
 	private boolean charAt(int pos, char character) {
 		if(pos < 0) {
 			return false;
 		}
 		
 		if(pos < line.length()) {
 			if(line.charAt(pos) == character) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 }
