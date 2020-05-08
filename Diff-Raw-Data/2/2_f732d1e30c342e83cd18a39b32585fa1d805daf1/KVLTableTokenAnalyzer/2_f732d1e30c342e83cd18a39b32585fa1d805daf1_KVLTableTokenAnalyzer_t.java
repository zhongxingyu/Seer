 //
 // KVLTableTokenAnalyzer.java
 // darkcircle dot 0426 at gmail dot com
 //
 // This source can be distributed under the terms of GNU General Public License version 3
 // which is derived from the license of Manalith bot.
 
 package org.manalith.ircbot.plugin.KVL;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
import org.manalith.ircbot.plugin.KVL.Exceptions.EmptyTokenStreamException;
 
 
 public class KVLTableTokenAnalyzer extends TokenAnalyzer{
 	
 	public KVLTableTokenAnalyzer ()
 	{
 		super ();
 	}
 	
 	public KVLTableTokenAnalyzer ( String newData )
 	{
 		this.setTokenStringData( newData );
 	}
 	
 	public TokenType getTokenType(String tokenString)
 	{
 		TokenType result = TokenType.Unknown;
 		
 		Pattern table_pattern = Pattern.compile("\\<[\\/]?table((\\s)(id|style|class)\\=\\\"(\\s|\\S)+\\\")*\\>"); 
 		// Pattern tbody_pattern = Pattern.compile("\\<[\\/]?tbody\\>");
 		Pattern tr_pattern = Pattern.compile("\\<[\\/]?tr((\\s)align\\=\\\"(\\s|\\S)+\\\")?\\>");
 		Pattern td_pattern = Pattern.compile("\\<[\\/]?td(\\s)?\\>");
 		// Pattern strong_pattern = Pattern.compile("\\<[\\/]?strong\\>");
 		//Pattern a_pattern = Pattern.compile("\\<[\\/]?a((\\s)(href|target|onclick|name)\\=\\\"(\\s|\\S)+\\\")*\\>");
 		
 		Matcher table_match = table_pattern.matcher(tokenString);
 		Matcher tr_match = tr_pattern.matcher(tokenString);
 		Matcher td_match = td_pattern.matcher(tokenString);
 		//Matcher strong_match = strong_pattern.matcher(tokenString);
 		//Matcher a_match = a_pattern.matcher(tokenString);
 		
 		if ( table_match.matches() )
 		{
 			result = TokenType.Table;
 		}
 		else if ( tr_match.matches() )
 		{
 			result = TokenType.TR;
 		}
 		else if ( td_match.matches() )
 		{
 			result = TokenType.TD;
 		}
 		
 		return result;
 	}
 	public TokenSubtype getTokenSubtype(String tokenString, TokenType currentType)
 	{
 		TokenSubtype result;
 		int hashCode = currentType.hashCode();
 		
 		if ( hashCode == TokenType.Table.hashCode() )
 		{
 			if ( tokenString.charAt(1) == '/' )
 				result = TokenSubtype.TableClose;
 			else
 				result = TokenSubtype.TableOpen;
 		}
 		else if ( hashCode == TokenType.TR.hashCode() )
 		{
 			if ( tokenString.charAt(1) == '/' )
 				result = TokenSubtype.TRClose;
 			else 
 				result = TokenSubtype.TROpen;
 		}
 		else if ( hashCode == TokenType.TD.hashCode() )
 		{
 			if ( tokenString.charAt(1) == '/')
 				result = TokenSubtype.TDClose;
 			else
 				result = TokenSubtype.TDOpen;
 		}
 		/*
 		else if ( hashCode == TokenType.STRONG.hashCode() )
 		{
 			if ( tokenString.charAt(1) == '/')
 				result = TokenSubtype.StrongClose;
 			else
 				result = TokenSubtype.StrongOpen;
 		}
 		*/
 		else if ( hashCode == TokenType.TextString.hashCode() )
 			result = TokenSubtype.TextString;
 		else
 			result = TokenSubtype.Unknown;
 		
 		return result;
 	}
 	
 	//
 	// OK! Implementation is completed. 2011/10/19
 	// No more needs to modify something.
 	//
 	public TokenArray analysisTokenStream () throws EmptyTokenStreamException
 	{
 		TokenArray result = new TokenArray();
 		TokenType currentTokenType = TokenType.Unknown;
 		TokenSubtype currentTokenSubtype = TokenSubtype.Unknown;
 		boolean inBoundOfTable = false;
 		
 		int len = this.data.length();
 		if ( len == 0 )
 			throw new EmptyTokenStreamException();
 		
 		int i = 0;
 		
 		String tokenString = "";
 		String tempchar = "";
 		
 		while ( i < len )
 		{
 			tempchar = this.data.substring(i, i+1);
 			i++;
 
 			if ( ( tempchar.equals("\t")  || tempchar.equals("\n") ) || ( tempchar.equals("\r") || tempchar.equals(" ") ) )
 			{
 				continue;
 			}
 			
 			if ( tokenString.equals("") && !tempchar.equals("<") )
 			{
 				tokenString = tempchar;
 				tempchar = this.data.substring(i, i+1);
 				i++;
 
 				while ( !tempchar.equals("<") )
 				{
 					if ( tempchar.equals("\t") || ( tempchar.equals("\r")|| tempchar.equals("\n") ) )
 					{
 						tempchar = this.data.substring(i, i+1);
 						i++;
 						continue;
 					}
 					tokenString += tempchar; 
 					tempchar = this.data.substring(i, i+1);
 					i++;
 				}
 				
 				currentTokenType = TokenType.TextString;
 				currentTokenSubtype = TokenSubtype.TextString;
 				
 				if ( inBoundOfTable )
 				{
 					result.addElement(tokenString, currentTokenType, currentTokenSubtype);
 					tokenString = "";
 					currentTokenType = TokenType.Unknown;
 					currentTokenSubtype = TokenSubtype.Unknown;
 				}
 				
 			}
 			
 			// get a piece of the tag
 			if ( tempchar.equals("<") )
 			{
 				tokenString = tempchar;
 				tempchar = this.data.substring(i, i+1);
 				i++;
 				
 				while ( !tempchar.equals(">") )
 				{
 					tokenString += tempchar;
 					tempchar = this.data.substring(i, i+1);
 					i++;
 				}
 				
 				// I need to check this point;
 				tokenString += tempchar;			
 			} // OK!
 			
 			currentTokenType = this.getTokenType(tokenString);
 			if ( currentTokenType == TokenType.Unknown )
 			{
 				tokenString = "";
 				continue;
 			}
 			else
 			{
 				
 				currentTokenSubtype = this.getTokenSubtype(tokenString, currentTokenType);
 				
 				if ( currentTokenSubtype == TokenSubtype.TableOpen )
 				{
 					if ( tokenString.length() != 7 )
 					{
 						// <table ...>
 						String [] CheckTableOption = tokenString.substring(1, tokenString.length() - 1).split("\\s");
 						for ( int l = 1 ; l < CheckTableOption.length ; l++ )
 						{
 							String [] KeyVal = CheckTableOption[l].split("\\=");
 							if ( KeyVal[0].equals("class") && KeyVal[1].substring(1, KeyVal[1].length() - 1).equals("kver") )
 							{
 								// class="kval"
 								inBoundOfTable = true;
 							}
 						}
 						
 					}
 				}
 				else if ( currentTokenSubtype == TokenSubtype.TableClose && inBoundOfTable )
 				{
 					TokenUnit newTokenUnit = new TokenUnit (tokenString, currentTokenType, currentTokenSubtype);
 					result.addElement(newTokenUnit);
 					break;
 				}
 				
 				TokenUnit newTokenUnit = new TokenUnit (tokenString, currentTokenType, currentTokenSubtype);
 				if ( inBoundOfTable )
 					result.addElement(newTokenUnit);
 				
 				tokenString = "";
 				currentTokenType = TokenType.Unknown;
 				currentTokenSubtype = TokenSubtype.Unknown;
 				
 			}
 		}
 		
 		return result;
 	}
 }
