 /**
 Copyright (c) 2012 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.delcyon.capo.parsers;
 
 import java.util.Arrays;
 import java.util.Vector;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.parsers.ParseTree.TermType;
 import com.delcyon.capo.parsers.Tokenizer.TokenType;
 
 /**
  * @author jeremiah
  *
  */
 public class ParseRule
 {
 
 	private String name;
 	private String[][] expressions;
 	private ParseTree parseTree;
 
 	/**
 	 * 
 	 * @param name
 	 * @param expressions each expression must be a list of terms w/o any alterations. ie each expression is a separate choice
 	 */
 	public ParseRule(String name, String[]... expressions)
 	{
 		this.name = name;
 		this.expressions = expressions;		
 	}
 
 	public void setParseTree(ParseTree parseTree)
 	{
 		this.parseTree = parseTree;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	private void printPathMessage(Node element, String message)
 	{
//	    StringBuilder stringBuilder = new StringBuilder();
//	    while(element != null)
//	    {
//	        stringBuilder.insert(0, element.getLocalName()+"/");	        
//	        element = (Node) element.getParentNode();
//	    }
//	    System.out.println(stringBuilder+":"+message);
 	}
 	
 	public boolean parse(Element originalParseNode, ParseTape parseTape) throws Exception
 	{
 	    //System.out.println("\n\n");
 	    printPathMessage(originalParseNode, "STARTING:"+this);
 	    
 		Vector<MatchItem> matchItemVector = new Vector<MatchItem>();
 		boolean foundExpressionMatch = false;
 		int initialTapePosition = parseTape.getPosition();
 		
 		Element peerParseNode = (Element) originalParseNode.cloneNode(true);
 		
 		expressions:
 		for (int currentExpression = 0; currentExpression < expressions.length; currentExpression++)
 		{
 			if(foundExpressionMatch == true)
 			{
 				matchItemVector.add(new MatchItem(peerParseNode,parseTape.getPosition()));
 			}
 			//check to see if we need to try something else, if we're out of tape, and we have a match, then we don't
 			if(parseTape.hasMore() == false && foundExpressionMatch)
 			{
 				break;
 			}
 			
 			peerParseNode = (Element) originalParseNode.cloneNode(true);
 			
 			foundExpressionMatch = true;
 			
 			//backup. set list pointer to parse entry position
 			parseTape.setPosition(initialTapePosition);
 			
 			String[] expression = expressions[currentExpression];
 			printPathMessage(peerParseNode, "starting parse with "+Arrays.toString(expression));
 			for (int currentTerm = 0; currentTerm < expression.length; currentTerm++)
 			{
 				String term = expression[currentTerm];
 				
 				//====================================REGEX CODE============================================
 				String parsedRegex = null;
 				String parsedReplacement = null;
 				
 				//check to see if we're dealing with a regex
 				if(term.startsWith("~") && term.matches("~.*/.+(/.*)?"))
 				{
 				    String originalTerm = term;
 				    int firstSlash = originalTerm.indexOf('/');
 				    int lastSlash = originalTerm.lastIndexOf('/');
 				    boolean defaultedLength = false;
 				    if(lastSlash == firstSlash)
 				    {
 				        defaultedLength = true;
 				        lastSlash = originalTerm.length();
 				    }
 				    term = originalTerm.substring(1,firstSlash);
 				    parsedRegex = originalTerm.substring(firstSlash+1,lastSlash);				    
 				    if ( firstSlash != lastSlash)
 				    {
 				        if(defaultedLength == false)
 				        {
 				            lastSlash++;
 				        }
 				        parsedReplacement = originalTerm.substring(lastSlash);
 				        if(parsedReplacement.isEmpty())
 				        {
 				            parsedReplacement = null;
 				        }
 				    }
 				}
 				
 				//=====================================QUANTIFICATION CODE==============================================
 				boolean useQuantification = false;
 				boolean inQuantificationLoop = false;
 				int quantifier = 0;
 				int minimumQuantity = 0;
 				int maximumQuantity = Integer.MAX_VALUE;
 				
 				if(term.endsWith("+"))
 				{
 				    useQuantification = true;
 				    inQuantificationLoop = true;
 				    term = term.substring(0, term.length()-1);
 				    minimumQuantity = 1;
 				}
 				else if(term.endsWith("?"))
                 {
                     useQuantification = true;
                     inQuantificationLoop = true;
                     term = term.substring(0, term.length()-1);
                     maximumQuantity = 1;
                 }
 				else if(term.endsWith("*"))
 				{
 				    useQuantification = true;
                     inQuantificationLoop = true;
                     term = term.substring(0, term.length()-1);
                     
 				}
 				else if(term.matches(".+\\{\\d+\\}"))
                 {
 				    String originalTerm = term;
                     useQuantification = true;
                     inQuantificationLoop = true;
                     term = originalTerm.replaceFirst("(.+)\\{\\d+\\}", "$1");
                     maximumQuantity = Integer.parseInt(originalTerm.replaceFirst(".+\\{(\\d+)\\}", "$1"));
                     minimumQuantity = maximumQuantity;
                 }
 				else if(term.matches(".+\\{\\d*,\\d+\\}"))
                 {
                     String originalTerm = term;
                     useQuantification = true;
                     inQuantificationLoop = true;
                     term = originalTerm.replaceFirst("(.+)\\{\\d*,\\d+\\}", "$1");
                     maximumQuantity = Integer.parseInt(originalTerm.replaceFirst(".+\\{\\d*,(\\d+)\\}", "$1"));
                     String minString = originalTerm.replaceFirst(".+\\{(\\d*),\\d+\\}", "$1");
                     if(minString.isEmpty() == false)
                     {
                         minimumQuantity = Integer.parseInt(minString);
                     }
                 }
 				
 				//=====================START MAIN DO WHILE LOOP====================================================
 				do
 				{				    
 				    if(parseTape.next() == null && currentTerm < expression.length-1)
 				    {					
 				        parseTape.pushBack();
 				        foundExpressionMatch = false;
 				        if(inQuantificationLoop == false)
 				        {
 				            continue expressions;
 				        }
 				    }
 
 				    ParseToken token = parseTape.getCurrent();
 				    if(token == null )
 				    {
 				        token = new ParseToken(null, TokenType.EOL);
 				    }
 				
 				//figure out what to do with the current term
 				    TermType termType = parseTree.getTermType(term); 
 					
 				    if(term.isEmpty() && parsedRegex != null)
 				    {
 				        termType = TermType.LITERAL;
 				    }
 				    
 				    printPathMessage(peerParseNode, "Checking "+term+"["+termType+"] against "+token);
 				    
 				    //==========================MATCHING TERM TO TOKEN CODE=======================================================
 				    //do the full term type checking
 					switch (termType)
 					{
 						case RULE:
 							//drill down into new rule
 							parseTape.pushBack();
 							Element parseNode = parseTree.createElement(originalParseNode,parseTree.getRule(term).getName());
 							peerParseNode.appendChild(parseNode);
 							if (parseTree.getRule(term).parse(parseNode, parseTape) == false)
 							{
 								peerParseNode.removeChild(parseNode);
 								foundExpressionMatch = false;
 								printPathMessage(peerParseNode, "FAILURE "+term+"["+termType+"] against "+token);
 								if(inQuantificationLoop == false)
 		                        {
 		                            continue expressions;
 		                        }
 							}							
 							break;
 						case LITERAL:
 						    //Literals must be tokens
 						    if(token.getTokenType() != TokenType.TOKEN)
                             {
                                 parseTape.pushBack();
                                 foundExpressionMatch = false;
                                 printPathMessage(peerParseNode, "FAILURE "+parsedRegex+"["+termType+"] against "+token);
                                 if(inQuantificationLoop == false)
                                 {
                                     continue expressions;
                                 }
                                 break;
                             }
 						    else if(parsedRegex != null && token.getValue().matches(parsedRegex))
 						    {
 						        if(parseTree.isIncludeLiterals() == true)
 						        {
 						            Element cElement = parseTree.createElement(originalParseNode,"LITERAL");
 						            cElement.setAttribute(parseTree.getLiteralType(token.getValue()).toString(), token.getValue());
 						            peerParseNode.appendChild(cElement);
 						        }
 						    }
 						    else if(parseTree.getLiteralValue(term).equals(token.getValue()))
 							{
 						        if(parseTree.isIncludeLiterals() == true)
 						        {
 						            Element cElement = parseTree.createElement(originalParseNode,"LITERAL");
 						            cElement.setAttribute(parseTree.getLiteralType(token.getValue()).toString(), token.getValue());
 						            peerParseNode.appendChild(cElement);
 						        }
 							}
 							else
 							{
 								parseTape.pushBack();
 								foundExpressionMatch = false;
 								printPathMessage(peerParseNode, "FAILURE "+term+"["+termType+"] against "+token);
 								if(inQuantificationLoop == false)
 		                        {
 		                            continue expressions;
 		                        }
 							}
 							break;
 						case SYMBOL:
 						    //Symbols must be tokens 
 						    if(token.getTokenType() != TokenType.TOKEN)
                             {
                                 parseTape.pushBack();
                                 foundExpressionMatch = false;
                                 printPathMessage(peerParseNode, "FAILURE "+parsedRegex+"["+termType+"] against "+token);
                                 if(inQuantificationLoop == false)
                                 {
                                     continue expressions;
                                 }
                                 break;
                             }
 						    
 						    String value = token.getValue();
 						    TermType valueTermType = parseTree.getTermType(value);
 						    //named rules can't actually be referred to the INPUT, so change this to a symbol
 						    if(valueTermType == TermType.RULE)
 						    {
 						        valueTermType = TermType.SYMBOL;
 						    }
 						    
 						    //do a simple regex check against the value before we start to get fancier later. 
 						    //esp after we've tried to run a replacement regex against the value.
 						    if(parsedRegex != null && value.matches(parsedRegex) == false)
 						    {						        
                                 parseTape.pushBack();
                                 foundExpressionMatch = false;
                                 printPathMessage(peerParseNode, "FAILURE "+parsedRegex+"["+termType+"] against "+token);
                                 if(inQuantificationLoop == false)
                                 {
                                     continue expressions;
                                 }
                                 break;
 						    }
 						    
 						    //see if we need to do some sort of regex replacement
 						    if(parsedRegex != null && parsedReplacement != null)
 						    {
 						        value = value.replaceAll(parsedRegex, parsedReplacement);
 						        //if we've modified this, don't let it turn into some other TermType. it IS a symbol.
 						        valueTermType = TermType.SYMBOL;
 						    }
 						    //this is the basic literal check for symbols. but converted regex's are never marked as literals
 						    else if(valueTermType == TermType.SYMBOL && parseTree.isLiteral(value))
 						    {
 						        valueTermType = TermType.LITERAL;
 						    }
 							//check to see if this is an escaped Literal, if so, it's a symbol. 
 						    //This can happen where the input includes a token wrapped in literal indicators, that was ALSO 
 						    //wrapped in literal indicators in the grammar. Such as '|' in a new notation and the Default Grammar.  
 							if(valueTermType == TermType.LITERAL && parseTree.getLiteralValue(value).length() != value.length())
                             {
                                 valueTermType = TermType.SYMBOL;                                
                             }
 							//check to see if it's a misplaced DELIMITER. DELIMITER's will never also be tokens
 							if(valueTermType == TermType.DELIMITER && token.getTokenType() == TokenType.TOKEN)
 							{
 							    valueTermType = TermType.SYMBOL;
 							}
 							//delimiters and literals should never be treated as symbols
 							if(valueTermType == TermType.DELIMITER || valueTermType == TermType.LITERAL)
 							{
 							    //System.err.println(token+"<=="+valueTermType);
 								parseTape.pushBack();
 								foundExpressionMatch = false;
 								printPathMessage(peerParseNode, "FAILURE "+term+"["+termType+"] against "+token+"["+valueTermType+"]");
 								if(inQuantificationLoop == false)
 		                        {
 		                            continue expressions;
 		                        }
 							}
 							//overlap with RULE names should be ignored as something we're parsing can't refer to a rule name
 							//overlap with Literals should be ignored as a literal can be a SYMBOL_NAME							
 							else
 							{
 							    //if we already have this attribute, then just append the values together
 							    if(peerParseNode.hasAttribute(term))
 							    {
 							        value = peerParseNode.getAttribute(term) +" "+ value;
 							    }
 								peerParseNode.setAttribute(term, value);
 							}
 							break;
 						case DELIMITER:
 						    //DELIMITER must NOT be tokens
 							if(token.getTokenType() != TokenType.TOKEN)
 							{
 								//comsume it, and do nothing
 							}
 							else
 							{
 							    //System.err.println(token+"<=="+parseTree.getTermType(token.getValue()));
 								parseTape.pushBack();
 								foundExpressionMatch = false;
 								printPathMessage(peerParseNode, "FAILURE "+term+"["+termType+"] against "+token);
 								if(inQuantificationLoop == false)
 		                        {
 		                            continue expressions;
 		                        }
 							}
 							break;
 						default:
 							System.err.println("unknown term:"+term);
 							foundExpressionMatch = false;
 							break;
 					}
 					
 					//=============================MORE QUANTIFICATION PROCESSING FOR CHECKING OF LOOP CONTINUATION================
 					if(useQuantification == true && inQuantificationLoop == true)
 					{
 					    if(foundExpressionMatch)
 					    {
 					        quantifier++;
 					        if(quantifier > maximumQuantity)
 					        {
 					            printPathMessage(peerParseNode, "QUANTITY FAILURE "+term+"["+termType+"] against "+token);
 					            foundExpressionMatch = false;
                                 continue expressions;
 					        }
 					    }
 					    else
 					    {
 					        inQuantificationLoop = false;
 					        if(quantifier >= minimumQuantity && quantifier <= maximumQuantity)
 					        {
 					            foundExpressionMatch = true;
 					        }
 					        else
 					        {
 					            printPathMessage(peerParseNode, "QUANTITY FAILURE "+term+"["+termType+"] against "+token);
 					            foundExpressionMatch = false; //redundant
 					            continue expressions;
 					        }
 					    }
 					}					
 				}//============================END DO WHILE LOOP==============================================
 				while(useQuantification == true && inQuantificationLoop == true);
 			}
 		}
 		
 		if(foundExpressionMatch == true)
 		{
 			matchItemVector.add(new MatchItem(peerParseNode,parseTape.getPosition()));
 		}
 		
 		if(matchItemVector.size() > 0)
 		{
 			
 			MatchItem matchItem = null;
 			switch(parseTree.getParseOrderPreference())
 			{				
 				case LEFT:
 					matchItem = matchItemVector.firstElement();
 					break;
 				case RIGHT:
 					matchItem = matchItemVector.lastElement();
 					break;
 				case MAX_LENGTH:
 					int matchItemPos = -1;
 					for (int index = 0 ; index < matchItemVector.size(); index++)
 					{
 						if(matchItemVector.get(index).endTapePosition > matchItemPos)
 						{
 							matchItem = matchItemVector.get(index);
 							matchItemPos = matchItem.endTapePosition;
 						}
 					}
 					break;
 			}
 			parseTape.setPosition(matchItem.endTapePosition);
 			NodeList childrenNodeList = matchItem.parseNode.getChildNodes();
 			for(int index = 0; index < childrenNodeList.getLength();)
 			{			    
 				originalParseNode.appendChild(childrenNodeList.item(index));
 				
 			}
 			
 			NamedNodeMap namedNodeMap = matchItem.parseNode.getAttributes();
 			while(namedNodeMap.getLength() > 0)
 			{
 			    originalParseNode.setAttributeNode((Attr) namedNodeMap.removeNamedItem(namedNodeMap.item(0).getNodeName()));    
 			}
 			
 			
 			
 			//XPath.dumpNode(peerParseNode, System.out);
 			
 			printPathMessage(peerParseNode, "finished parse with TRUE");
 			return true;
 		}
 		printPathMessage(peerParseNode, "finished parse with FAILURE");
 		return false;
 	}
 
 	public String[][] getExpressions()
 	{
 		return expressions;
 	}
 	
 	@Override
 	public String toString()
 	{
 	    StringBuilder stringBuilder = new StringBuilder();
 	    stringBuilder.append("[");
 	    for (String[] expression : expressions)
         {
             stringBuilder.append(Arrays.toString(expression));
         }
 	    stringBuilder.append("]");
 	   return getName()+""+stringBuilder;
 	}
 	
 	private class MatchItem
 	{
 		
 		Element parseNode = null;
 		int endTapePosition = -1;
 
 		public MatchItem(Element parseNode, int position)
 		{
 			this.parseNode = parseNode;
 			this.endTapePosition = position;
 		}
 	}
 	
 }
