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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 import com.sangupta.nutz.ast.BlockQuoteNode;
 import com.sangupta.nutz.ast.CodeBlockNode;
 import com.sangupta.nutz.ast.HRuleNode;
 import com.sangupta.nutz.ast.HeadingNode;
 import com.sangupta.nutz.ast.Node;
 import com.sangupta.nutz.ast.OrderedListNode;
 import com.sangupta.nutz.ast.ParagraphNode;
 import com.sangupta.nutz.ast.PlainTextNode;
 import com.sangupta.nutz.ast.RootNode;
 import com.sangupta.nutz.ast.UnorderedListNode;
 
 /**
  * Parse the given markup and create an AST (abstract syntax tree),
  * 
  * @author sangupta
  *
  */
 public class Parser {
 
 	/**
 	 * Internal reader that reads line by line from the markup
 	 * provided.
 	 */
 	private BufferedReader reader = null;
 	
 	/**
 	 * Currently read line
 	 */
 	private String line = null;
 	
 	/**
 	 * Reference to the root node of the AST
 	 */
 	private final RootNode ROOT_NODE = new RootNode();
 	
 	/**
 	 * A collector that is used to collect data when we enter
 	 * an iterative function that needs look ahead.
 	 */
 	private StringBuilder collector = new StringBuilder(1024);
 	
 	/**
 	 * Reference to the last node that was added to the AST
 	 */
 	private Node lastNode = null;
 	
 	/**
 	 * Internal reference to the {@link TextNodeParser} that is used to
 	 * parse text nodes recursively.
 	 */
 	private final TextNodeParser textNodeParser = new TextNodeParser();
 	
 	/**
 	 * Maintains the depth of blockquotes nesting
 	 */
 	private int blockDepth = 0;
 	
 	/**
 	 * Public function that parses and creates an AST of the given markup.
 	 * 
 	 * @param markup
 	 * @return
 	 * @throws Exception
 	 */
 	public RootNode parse(String markup) throws Exception {
 		reader = new BufferedReader(new StringReader(markup));
 		readLines(ROOT_NODE);
 		
 		return ROOT_NODE;
 	}
 	
 	private String readLine() throws IOException {
 		String line = reader.readLine();
 		
 		if(line == null) {
 			return null;
 		}
 		
 		if(this.blockDepth > 0) {
 			int length = line.length();
 			int found = 0;
 			for(int index = 0; index < length; index++) {
 				if(line.charAt(index) == Identifiers.HTML_OR_AUTOLINK_END) {
 					found++;
 				}
 				
 				if(found == this.blockDepth) {
 					if(line.charAt(index + 1) == Identifiers.SPACE) {
 						index++;
 					}
 					
 					line = line.substring(index + 1);
 					break;
 				}
 			}
 		}
 		
 		return line;
 	}
 	
 	/**
 	 * Read all lines one-by-one and create the AST.
 	 * 
 	 * @throws Exception
 	 */
 	private void readLines(Node root) throws Exception {
 		boolean readAhead = true;
 		do {
 			if(readAhead) {
 				this.line = reader.readLine();
 			}
 			
 			if(this.line == null) {
 				return;
 			}
 			
 			readAhead = parseLine(root, line);
 		} while(true);
 	}
 
 	/**
 	 * Parse the read line and construct AST
 	 * 
 	 * @param line
 	 * @throws Exception
 	 */
 	private boolean parseLine(Node currentRoot, String line) throws Exception {
 		final int[] spaceTokens = MarkupUtils.findLeadingSpaces(line);
 		final int leadingPosition = spaceTokens[0];
 		final int leadingSpaces = spaceTokens[1];
 		
 		if(leadingSpaces == 0) {
 			if(line.startsWith("#")) {
 				lastNode = parseHeading(currentRoot, line);
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 			
 			// Github Extra: fenced code blocks
 			if(line.startsWith("```")) {
 				lastNode = parseFencedCodeBlock(line, "```");
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 			
 			// PHP Extra: fenced code blocks
 			if(line.startsWith("~~~")) {
 				lastNode = parseFencedCodeBlock(line, "~~~");
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 			
 			if(line.startsWith("===")) {
 				// turn previous text line into a heading of type H1
 				// if present, else parse this as a normal line
 				if(lastNode instanceof ParagraphNode) {
 					boolean broken = ((ParagraphNode) lastNode).breakIntoTextAndHeading(1);
 					if(broken) {
 						return true;
 					}
 				}
 			}
 			
 			if(line.startsWith("---")) {
 				// turn previous text line into a heading of type H2
 				// if present, else convert it into an HRULE
 				if(lastNode instanceof ParagraphNode) {
 					boolean broken = ((ParagraphNode) lastNode).breakIntoTextAndHeading(2);
 					if(broken) {
 						return true;
 					}
 				}
 				
 				lastNode = new HRuleNode();
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 			
 			if(line.startsWith("[")) {
 				// parse an inline link reference
 				boolean found = parseLinkReference(line, leadingPosition);
 				if(found) {
 					lastNode = null;
 					return true;
 				}
 			}
 			
 			if((line.startsWith("* ") || line.startsWith("*\t")) && !MarkupUtils.isOnlySpaceAndCharacter(line, '*')) {
 				// this is a list of data
 				lastNode = parseUnorderedList(currentRoot, line);
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 		} /// leading spaces == 0
 		
 		if(leadingSpaces < 4) {
 			// check for reference links starts with
 			if(line.startsWith("[", leadingPosition)) {
 				boolean found = parseLinkReference(line, leadingPosition);
 				if(found) {
 					lastNode = null;
 					return true;
 				}
 			}
 
 			// check for various forms of HRULE
 			boolean found = checkForVariousHorizontalRules(currentRoot, line, leadingPosition);
 			if(found) {
 				return true;
 			}
 			
 		} // leading spaces < 4
 		
 		// check for leading spaces
 		if(leadingSpaces >= 4) {
 			// this is a verbatimn node
 			Node codeNode = parseVerbatimBlock(line);
 			if(codeNode != null) {
 				lastNode = codeNode;
 				currentRoot.addChild(lastNode);
 				
 				// one more line has just been read
 				// which needs to be parsed again
 //				parseLine(currentRoot, this.line);
 				
 				return false;
 			}
 		}
 		
 		// check for block quotes
 		// this is a block quote - remove the block quote symbol
 		// trim one space after this
 		// and then re-parse the line
 		if(!line.isEmpty() && leadingPosition < line.length() && line.charAt(leadingPosition) == Identifiers.HTML_OR_AUTOLINK_END) {
 			BlockQuoteNode blockQuoteNode;
 			if(currentRoot.hasChild() && (currentRoot.lastNode() instanceof BlockQuoteNode)) {
 				blockQuoteNode = (BlockQuoteNode) currentRoot.lastNode();
 			} else {
 				blockQuoteNode = new BlockQuoteNode();
 				currentRoot.addChild(blockQuoteNode);
 			}
 
 			lastNode = blockQuoteNode;
 
 			// parse the block
 			this.blockDepth++;
 			
 			String temp = line.substring(leadingPosition + 1);
 			if(temp.charAt(0) == Identifiers.SPACE) {
 				temp = temp.substring(1);
 			}
			parseLine(blockQuoteNode, temp);
 			this.blockDepth--;
 			
 			// save the node
			return true;
 		}
 		
 		// try and see if this is an ordered list
 		if(lineStartsWithNumber(line)) {
 			lastNode = parseOrderedList(currentRoot, line);
 			currentRoot.addChild(lastNode);
 			return true;
 		}
 		
 		// check if line is empty
 		// if so, add a new line node
 		if(line.trim().isEmpty()) {
 			lastNode = new PlainTextNode(currentRoot, "\n");
 			currentRoot.addChild(lastNode);
 			return true;
 		}
 		
 		// parse the text line reading ahead as desired
 		lastNode = parseText(currentRoot, line, true);
 		currentRoot.addChild(lastNode);
 		
 		// there may be a new line that would have been read
 		if(this.line != null) {
 			return parseLine(currentRoot, this.line);
 		}
 		
 		return true;
 	}
 
 	private boolean checkForVariousHorizontalRules(Node currentRoot, String line, int leadingPosition) {
 		if(MarkupUtils.isOnlySpaceAndCharacter(line, '-')) {
 			if(line.startsWith("---", leadingPosition)) {
 				lastNode = new HRuleNode();
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 	
 			if(line.startsWith("- - -", leadingPosition)) {
 				lastNode = new HRuleNode();
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 		}
 
 		if(MarkupUtils.isOnlySpaceAndCharacter(line, '*')) {
 			if(line.startsWith("***", leadingPosition)) {
 				lastNode = new HRuleNode();
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 	
 			if(line.startsWith("* * *", leadingPosition)) {
 				lastNode = new HRuleNode();
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 		}
 		
 		if(MarkupUtils.isOnlySpaceAndCharacter(line, '_')) {
 			if(line.startsWith("___", leadingPosition)) {
 				lastNode = new HRuleNode();
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 	
 			if(line.startsWith("_ _ _", leadingPosition)) {
 				lastNode = new HRuleNode();
 				currentRoot.addChild(lastNode);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Create a code block out of the verbatim block that has been created using
 	 * 4 starting spaces.
 	 * 
 	 * @param line
 	 * @return
 	 * @throws IOException
 	 */
 	private CodeBlockNode parseVerbatimBlock(String line) throws IOException {
 		// make sure this is not an empty line
 		if(line.trim().length() == 0) {
 			return null;
 		}
 		
 		String lang = null;
 		boolean firstLine = true;
 		int leadingSpaces = -1;
 		
 		collector.setLength(0);
 		
 		do {
 			if(firstLine) {
 				int index = line.trim().indexOf('!');
 				if(index != -1) {
 					int spaceIndex = line.indexOf(Identifiers.SPACE, index + 1);
 					if(spaceIndex == -1) {
 						lang = line.substring(0, index).trim();
 					}
 				}
 				
 				// no language was detected
 				// let's add it to the code block
 				if(lang == null) {
 					collector.append(line);
 					collector.append('\n');
 				}
 			}
 
 			// append line to collector
 			if(!firstLine) {
 				collector.append(line);
 				collector.append('\n');
 			}
 			
 			// read one more line
 			firstLine = false;
 			line = readLine();
 			if(line == null) {
 				break;
 			}
 			
 			leadingSpaces = MarkupUtils.findLeadingSpaces(line)[1];
 			if(!line.isEmpty() && leadingSpaces < 4) {
 				break;
 			}
 		} while(true);
 		
 		// we need to consume the current pending line
 		this.line = line;
 		
 		CodeBlockNode node = new CodeBlockNode(collector.toString(), lang);
 		return node;
 	}
 
 	/**
 	 * Test if the given line starts with a number
 	 * 
 	 * @param line2
 	 * @return
 	 */
 	private boolean lineStartsWithNumber(String line2) {
 		int dot = line.indexOf('.');
 		if(dot == -1) {
 			return false;
 		}
 		
 		try {
 			Integer.parseInt(line.substring(0, dot));
 			return true;
 		} catch(NumberFormatException e) {
 			// do nothing
 			// we will continue parsing to see
 			// if any of the other tokens can match
 			// this parsing schedule
 		}
 		return false;
 	}
 
 	/**
 	 * Parse the given line and extract the link reference that is present in it
 	 * and add it to the root node.
 	 * 
 	 * @param line
 	 * @return
 	 */
 	private boolean parseLinkReference(String line, int leadingPosition) {
 		int index = line.indexOf(']');
 		if(index == -1) {
 			return false;
 		}
 		
 		if(!(line.charAt(index + 1) == ':')) {
 			return false;
 		}
 		
 		String id = line.substring(leadingPosition + 1, index);
 		String link = line.substring(index + 2).trim();
 		
 		// extract any title if available
 		String[] tokens = MarkupUtils.parseLinkAndTitle(link);
 		
 		ROOT_NODE.addReferenceLink(id, tokens[0].trim(), tokens[1]);
 		return true;
 	}
 
 	/**
 	 * Create an ordered list
 	 * 
 	 * @param line
 	 * @return
 	 * @throws IOException
 	 */
 	private OrderedListNode parseOrderedList(Node currentRoot, String line) throws IOException {
 		OrderedListNode list = new OrderedListNode();
 		
 		do {
 			line = line.substring(1).trim();
 			
 			list.addChild(textNodeParser.parse(currentRoot, line));
 
 			// read a new line
 			line = readLine();
 			
 			if(line == null) {
 				break;
 			}
 			
 			// check for termination of block
 			if(!lineStartsWithNumber(line)) {
 				break;
 			}
 		} while(true);
 		
 		return list;
 	}
 
 	/**
 	 * Parses a list of elements while reading ahead
 	 * 
 	 * @param line
 	 * @return
 	 */
 	private UnorderedListNode parseUnorderedList(Node currentRoot, String line) throws IOException {
 		UnorderedListNode list = new UnorderedListNode();
 		
 		do {
 			line = line.substring(1).trim();
 			
 			list.addChild(textNodeParser.parse(currentRoot, line));
 
 			// read a new line
 			line = readLine();
 			
 			if(line == null) {
 				break;
 			}
 			
 			// check for termination of block
 			if(!line.startsWith("*")) {
 				break;
 			}
 		} while(true);
 		
 		return list;
 	}
 
 	/**
 	 * Read a fenced code block from the line
 	 * 
 	 * @param line
 	 * @return
 	 */
 	private CodeBlockNode parseFencedCodeBlock(String line, String terminator) throws IOException {
 		String language = null;
 		if(line.length() > 3) {
 			language = line.substring(3);
 		}
 		
 		collector.setLength(0);
 		
 		// start reading more lines
 		// till we get an ending fenced code block
 		do {
 			line = reader.readLine();
 			if(line.startsWith(terminator)) {
 				break;
 			}
 			
 			collector.append(line);
 			collector.append("\n");
 		} while(true);
 		
 		CodeBlockNode codeBlock = new CodeBlockNode(collector.toString(), language);
 		return codeBlock;
 	}
 
 	/**
 	 * Parse a heading from this line
 	 * 
 	 * @param line
 	 * @return
 	 * @throws Exception
 	 */
 	private HeadingNode parseHeading(Node currentRoot, String line) throws Exception {
 		int headCount = 1;
 		int index = 1;
 		do {
 			if(line.charAt(index) == '#') {
 				headCount++;
 			} else {
 				break;
 			}
 			
 			index++;
 		} while(true);
 
 		// strip off all hash signs per the last non-hash character
 		line = line.trim();
 		index = line.length();
 		do {
 			if(line.charAt(index - 1) == '#') {
 				// skip
 			} else {
 				break;
 			}
 			
 			index--;
 		} while(true);
 		
 		line = line.substring(headCount, index).trim();
 		
 		Node textNode = parseText(currentRoot, line, false);
 		HeadingNode heading = new HeadingNode(headCount, textNode);
 		return heading;
 	}
 	
 	/**
 	 * Parse text from the given line
 	 * 
 	 * @param readLine
 	 * @param fetchMoreLines indicates if we can read ahead more lines
 	 * @return
 	 * @throws Exception
 	 */
 	private Node parseText(Node currentRoot, String readLine, boolean fetchMoreLines) throws Exception {
 		if(!fetchMoreLines) {
 			return textNodeParser.parse(currentRoot, readLine);
 		}
 		
 		if(readLine.isEmpty()) {
 			this.line = null;
 			return new ParagraphNode(currentRoot, "\n");
 		}
 		
 		collector.setLength(0);
 		do {
 			if(readLine.isEmpty() || readLine.endsWith("  ")) {
 				// this is a break for a new line
 				// exit now
 				break;
 			}
 			
 			collector.append(readLine);
 			collector.append('\n');
 
 			line = readLine();
 			if(line == null || line.isEmpty()) {
 				break;
 			}
 			
 			// this signifies a presence of heading
 			// need to break here
 			if(line.startsWith("===") || line.startsWith("---")) {
 				break;
 			}
 			
 			readLine = line;
 		} while(true);
 		
 		return textNodeParser.parse(currentRoot, collector.toString());
 	}
 	
 }
