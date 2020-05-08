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
 
 import java.io.StreamTokenizer;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import com.delcyon.capo.parsers.GrammerParser.SymbolType;
 import com.delcyon.capo.xml.cdom.CDocument;
 import com.delcyon.capo.xml.cdom.CElement;
 
 /**
  * @author jeremiah
  *
  */
 public class ParseTree extends CDocument
 {
 
 	public enum ParseOrderPreference
 	{
 		LEFT,
 		RIGHT,
 		MAX_LENGTH
 	}
 	
 	public enum TermType
 	{
 		RULE,
 		SYMBOL,
 		DELIMITER,
 		LITERAL
 	}
 	
 	private Vector<ParseRule> parseRuleVector = new Vector<ParseRule>();
 	private HashMap<String, ParseRule> parseRuleHashMap = new HashMap<String, ParseRule>();
 	
 	private HashMap<String, String[]> symbolHashMap = new HashMap<String, String[]>();	
 	private HashMap<String, SymbolType> symbolTypeHashMap = new HashMap<String, SymbolType>();
 	private HashMap<String, String> notationHashMap = new HashMap<String, String>();
 	
	private ParseOrderPreference parseOrderPreference = ParseOrderPreference.MAX_LENGTH;
 	
 	public void addRule(ParseRule parseRule)
 	{
 		parseRuleVector.add(parseRule);
 		parseRuleHashMap.put(parseRule.getName(), parseRule);
 		//find any literals in the expression, and mark them as a literal
 		String[][] expressions = parseRule.getExpressions();
 		for (String[] expresssion : expressions)
 		{
 			for (String term : expresssion)
 			{
 				String[] patterns = symbolHashMap.get(SymbolType.LITERAL.toString());			
 				if (patterns != null)
 				{
 					for (String literalPattern : patterns)
 					{
 						if(term.matches(literalPattern))
 						{
 							symbolTypeHashMap.put(term.replaceAll(literalPattern, "$1"), SymbolType.LITERAL);
 						}
 					}
 				}
 			}
 		}
 		parseRule.setParseTree(this);
 	}
 
 	public void parse(StreamTokenizer streamTokenizer) throws Exception
 	{
 		ParseTape parseTape = new ParseTape(streamTokenizer);
 		CElement parseNode = new CElement(parseRuleVector.firstElement().getName());
 		appendChild(parseNode);
 		if(parseRuleVector.firstElement().parse(parseNode,parseTape))
 		{
 			//appendChild(parseNode);
 		}
 		
 	}
 
 	public boolean isRule(String term)
 	{
 		return parseRuleHashMap.containsKey(term);
 	}
 
 	public void setSymbolHashMap(HashMap<String, String[]> symbolHashMap)
 	{
 		this.symbolHashMap = symbolHashMap;
 		Set<Entry<String, String[]>> symbolEntrySet = symbolHashMap.entrySet();
 		for (Entry<String, String[]> entry : symbolEntrySet)
 		{
 			String[] symbols = entry.getValue();
 			for (String symbol : symbols)
 			{
 				symbolTypeHashMap.put(symbol, SymbolType.valueOf(entry.getKey()));
 			}
 		}
 	}
 
 	public ParseRule getRuleNode(String term)
 	{
 		return parseRuleHashMap.get(term);
 	}
 
 	
 	public TermType getTermType(String term)
 	{
 		if (symbolTypeHashMap.containsKey(term) )
 		{
 			if(symbolTypeHashMap.get(term) == SymbolType.LITERAL)
 			{
 				return TermType.LITERAL;
 			}
 			else
 			{
 				return TermType.DELIMITER;
 			}
 		}
 		else if (parseRuleHashMap.containsKey(term))
 		{
 			return TermType.RULE;
 		}
 		else
 		{
 			String[] patterns = symbolHashMap.get(SymbolType.LITERAL.toString());			
 			if (patterns != null)
 			{
 				for (String literalPattern : patterns)
 				{
 					if(term.matches(literalPattern))
 					{
 						return TermType.LITERAL;
 					}
 				}
 			}
 			return TermType.SYMBOL;
 		}
 	}
 	
 	public String getLiteralValue(String term)
 	{
 		String[] patterns = symbolHashMap.get(SymbolType.LITERAL.toString());		
 		if (patterns != null)
 		{
 			for (String literalPattern : patterns)
 			{
 				if(term.matches(literalPattern))
 				{					
 					System.out.println(term + "\t ==>\t LITERAL");
 					return term.replaceAll(literalPattern, "$1");
 				}
 			}
 		}
 		return term;
 	}
 
 	public ParseOrderPreference getParseOrderPreference()
 	{
 		return parseOrderPreference;
 	}
 	
 	public void setParseOrderPreference(ParseOrderPreference parseOrderPreference)
 	{
 		this.parseOrderPreference = parseOrderPreference;
 	}
 	
 	
 }
