 package org.manalith.ircbot.plugin.Calc;
 // CalcTokenAnalyzer.java 
 //
 // This class is for generating token stream array from user input string.
 //
// This program can be distributed under the terms of GNU GPL v3 or later.
 // darkcircle.0426@gmail.com
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.manalith.ircbot.plugin.Calc.Exceptions.EmptyTokenStreamException;
 import org.manalith.ircbot.plugin.Calc.Exceptions.TokenAnalysisException;
 
 
 public class CalcTokenAnalyzer {
 	
 	private String tokenStream;
 	
 	public CalcTokenAnalyzer ()
 	{
 		setTokenStream ( "" );
 	}
 	public CalcTokenAnalyzer (String MathExpr)
 	{
 		setTokenStream ( MathExpr );
 	}
 
 	public void setTokenStream (String MathExpr)
 	{
 		this.tokenStream = MathExpr;
 	}
 	public String getTokenStream ( )
 	{
 		return this.tokenStream;
 	}
 
 	public TokenType getTokenType (String tokenString)
 	{
 		TokenType type = TokenType.Unknown;
 		
 		// Regex patterns for recognizing integer
 		Pattern binary = Pattern.compile("(0|(0|1)+)(B|b)");
 		Pattern octal = Pattern.compile("0[1-7][0-7]*");
 		Pattern decimal = Pattern.compile("(0|-?[1-9][0-9]*)");
 		Pattern hexadec = Pattern.compile("0x([0-9a-fA-F]*)");
 		
 		// Regex pattern for recognizing floating point
 		Pattern fpoint = Pattern.compile("(-?[0-9]+)(\\.[0-9]+)?(([Ee](-?[1-9][0-9]*))|f)?");
 		
 		// Regex pattern for recognizing operator
 		Pattern operat = Pattern.compile("\\+|\\-|\\*|\\/|\\%|\\^|\\!");
 		
 		// Regex patterns for recognizing parenthesis to change priority of calculation
 		Pattern lparen = Pattern.compile("\\(");
 		Pattern rparen = Pattern.compile("\\)");
 		
 		// Regex patterns for recognizing parentheses to preprocess for the function
 		Pattern triangleFunc = Pattern.compile("sin|cos|tan|arcsin|arccos|arctan");
 		Pattern convertBase = Pattern.compile("to(bin|oct|dec|hex)");
 		Pattern mathematFunc = Pattern.compile("sqrt");
 		
 		Matcher bin_match = binary.matcher(tokenString);
 		Matcher oct_match = octal.matcher(tokenString);
 		Matcher dec_match = decimal.matcher(tokenString);
 		Matcher hex_match = hexadec.matcher(tokenString);
 		
 		Matcher op_match = operat.matcher(tokenString);
 		
 		Matcher fp_match = fpoint.matcher(tokenString);
 		
 		Matcher lparen_match = lparen.matcher(tokenString);
 		Matcher rparen_match = rparen.matcher(tokenString);
 		
 		Matcher triFunc_match = triangleFunc.matcher(tokenString);
 		Matcher convBase_match = convertBase.matcher(tokenString);
 		Matcher mathemat_match = mathematFunc.matcher(tokenString);
 		
 		if ( ( bin_match.matches() || oct_match.matches() ) || ( dec_match.matches() || hex_match.matches() ) )
 			type = TokenType.Integer;
 		else if ( fp_match.matches() )
 			type = TokenType.FlPoint;
 		else if ( op_match.matches() )
 			type = TokenType.Operatr;
 		else if ( lparen_match.matches() || rparen_match.matches() )
 			type = TokenType.Parents;
 		else if ( triFunc_match.matches() )
 			type = TokenType.TriangleFunc;
 		else if ( convBase_match.matches() )
 			type = TokenType.BaseConvFunc;
 		else if ( mathemat_match.matches() )
 			type = TokenType.MathematFunc;
 		return type;
 	}
 	public TokenSubtype getTokenSubtype (String tokenString, TokenType type)
 	{
 		TokenSubtype result = TokenSubtype.Unknown;
 		if ( type.equals(TokenType.Integer) )
 		{
 			// Regex pattern for recognizing integer
 			Pattern binary = Pattern.compile("(0|(0|1)+)(B|b)");
 			Pattern octal = Pattern.compile("0[1-7][0-7]*");
 			Pattern decimal = Pattern.compile("(0|-?[1-9][0-9]*)");
 			Pattern hexadec = Pattern.compile("0x([0-9a-fA-F]*)");
 			
 			Matcher bin_match = binary.matcher(tokenString);
 			Matcher oct_match = octal.matcher(tokenString);
 			Matcher dec_match = decimal.matcher(tokenString);
 			Matcher hex_match = hexadec.matcher(tokenString);
 			
 			if ( oct_match.matches() ) 
 				result = TokenSubtype.Octal;
 			else if ( dec_match.matches() )
 				result = TokenSubtype.Decimal;
 			else if ( hex_match.matches() )
 				result = TokenSubtype.Hexadec;
 			else if ( bin_match.matches() )
 				result = TokenSubtype.Binary;
 			else
 				result = TokenSubtype.Unknown;
 		}
 		else if ( type.equals(TokenType.FlPoint) )
 		{
 			// Regex pattern for recognizing floating point
 			Pattern spfpoint = Pattern.compile("(-?[0-9]+)(\\.[0-9]+)?f");
 			Pattern dpfpoint = Pattern.compile("(-?[0-9]+)(\\.[0-9]+)");
 			Pattern expfpoint = Pattern.compile("(-?[0-9]+)(\\.[0-9]+)?([Ee](-?[1-9][0-9]*))");
 			
 			Matcher spf_match = spfpoint.matcher(tokenString);
 			Matcher dpf_match = dpfpoint.matcher(tokenString);
 			Matcher expf_match = expfpoint.matcher(tokenString);
 			
 			if ( spf_match.matches() )
 				result = TokenSubtype.SpFltPoint;
 			else if ( dpf_match.matches() )
 				result = TokenSubtype.DpFltPoint;
 			else if ( expf_match.matches() )
 				result = TokenSubtype.ExpFltPoint;
 			else
 				result = TokenSubtype.Unknown;
 		}
 		else if ( type.equals(TokenType.Parents) )
 		{
 			if ( tokenString.equals("(") )
 				result = TokenSubtype.Left_Parenthesis;
 			else if ( tokenString.equals(")") )
 				result = TokenSubtype.Righ_Parenthesis;
 			else 
 				result = TokenSubtype.Unknown;
 		}
 		else if ( type.equals(TokenType.Operatr) )
 		{
 			if ( tokenString.equals("+"))
 				result = TokenSubtype.Plus;
 			else if ( tokenString.equals("-") )
 				result = TokenSubtype.Minus;
 			else if ( tokenString.equals("*") )
 				result = TokenSubtype.Times;
 			else if ( tokenString.equals("/") )
 				result = TokenSubtype.Divide;
 			else if ( tokenString.equals("%") )
 				result = TokenSubtype.Modulus;
 			else if ( tokenString.equals("^") )
 				result = TokenSubtype.Power;
 			else if ( tokenString.equals("!") )
 				result = TokenSubtype.Factorial;
 			else 
 				result = TokenSubtype.Unknown;
 		}
 		else if ( type.equals(TokenType.TriangleFunc) )
 		{
 			//throw new NotImplementedException();
 			//*
 			if ( tokenString.equals("sin") )
 			{
 				result = TokenSubtype.Sine;
 			}
 			else if ( tokenString.equals("cos") )
 			{
 				result = TokenSubtype.Cosine;
 			}
 			else if ( tokenString.equals("tan") )
 			{
 				result = TokenSubtype.Tangent;
 			}
 			else if ( tokenString.equals("arcsin") )
 			{
 				result = TokenSubtype.ArcSine;
 			}
 			else if ( tokenString.equals("arccos") )
 			{
 				result = TokenSubtype.ArcCosine;
 			}
 			else if ( tokenString.equals("arctan") )
 			{
 				result = TokenSubtype.ArcTangent;
 			}
 		}
 		else if ( type.equals(TokenType.BaseConvFunc) )
 		{
 		   if ( tokenString.equals("tobin") )
 			{
 				result = TokenSubtype.ToBin;
 			}
 			else if ( tokenString.equals("tooct") )
 			{
 				result = TokenSubtype.ToOct;
 			}
 			else if ( tokenString.equals("todec") )
 			{
 				result = TokenSubtype.ToDec;
 			}
 			else if ( tokenString.equals("tohex") )
 			{
 				result = TokenSubtype.ToHex;
 			}
 		}
 		else if ( type.equals(TokenType.MathematFunc) )
 		{
 			if ( tokenString.equals("sqrt") )
 			{
 				result = TokenSubtype.Sqrt;
 			}
 		}
 		else
 			result = TokenSubtype.Unknown;
 		
 		return result;
 	}
 	
 	public TokenArray getTokenArray () throws TokenAnalysisException, EmptyTokenStreamException
 	{
 		TokenArray result = new TokenArray();
 		
 		int stringLength = tokenStream.length();
 		
 		if ( stringLength == 0 )
 			throw new EmptyTokenStreamException ();
 		
 		String temp = "";
 		
 		TokenType currentType = TokenType.Unknown;
 		TokenType checkedType = TokenType.Unknown; // init.
 		
 		for ( int i=0 ; i < stringLength ; i++ )
 		{
 			// x"."
 			if ( tokenStream.charAt(i) == '.' )
 			{
 				checkedType = TokenType.FlPoint;
 				currentType = TokenType.FlPoint;
 				temp = temp.concat( tokenStream.substring( i , i+1 ) );
 				continue;
 			}
 			
 			// 0.001 "e" or 0xnnnne
 			if ( ( tokenStream.charAt(i) == 'e' || tokenStream.charAt(i) == 'E' ) )
 			{
 				if ( this.getTokenSubtype( temp, currentType ) == TokenSubtype.Decimal )
 				{
 					currentType = TokenType.FlPoint;
 					
 					temp = temp.concat ( tokenStream.substring( i , i+1 ) );
 					continue;
 				}
 				else
 				{
 					// hexa decimal, ExpFltPoint or function
 					temp = temp.concat ( tokenStream.substring( i , i+1 ) );
 					continue;
 				}
 			}
 			
 			// " 0x "
 			if ( ( tokenStream.charAt(i) == 'x') && currentType == TokenType.Integer )
 			{
 				temp = temp.concat ( tokenStream.substring( i , i+1 ) );
 				continue;
 			}
 			
 			if ( tokenStream.charAt(i) == '-' )
 			{
 				if ( temp.length() == 0 && i == 0 )
 				{
 					temp = temp.concat ( tokenStream.substring( i, i+1 ) );
 					currentType = this.getTokenType(temp);
 					checkedType = currentType;
 					
 					continue;
 				}
 				if ( temp.length() == 0 && result.getToken(result.getSize()-1).getTokenType() != TokenType.Operatr )
 				{
 					// operator
 					temp = temp.concat( tokenStream.substring ( i, i+1 ) );
 					currentType = this.getTokenType(temp);
 					TokenSubtype tsType = this.getTokenSubtype( temp, currentType );
 					TokenUnit newUnit = new TokenUnit( currentType, tsType, temp );
 					result.addToken( newUnit );
 					
 					currentType = TokenType.Unknown;
 					temp = "";
 					
 					continue;
 				}
 				else if ( currentType == TokenType.FlPoint && 
 						( temp.charAt( temp.length() - 1 ) == 'e' || temp.charAt( temp.length() - 1) == 'E' ) )
 				{
 					// unary mark for exponential
 					temp = temp.concat( tokenStream.substring ( i , i+1 ) );
 					continue;
 				}
 			}
 			
 			if ( tokenStream.charAt(i) == ' ')
 			{
 				// add token if current point meets separator(white space)
 				currentType = this.getTokenType(temp);
 				TokenSubtype tsType = this.getTokenSubtype( temp, currentType );
 				TokenUnit newUnit = new TokenUnit( currentType, tsType, temp );
 				result.addToken( newUnit );
 				
 				// reset
 				temp = "";
 				checkedType = TokenType.Unknown;
 				currentType = TokenType.Unknown;
 				
 				// and ignore white space
 				continue;
 			}
 			
 			
 			temp = temp.concat( tokenStream.substring( i , i+1 ) );
 			
 			if ( i == 0 )
 			{
 				// unary operator
 				if ( temp.equals("-") )
 				{
 					continue;
 				}
 				
 				currentType = this.getTokenType(temp);
 				checkedType = currentType;
 			}
 			else if ( i == 1 )
 			{
 				
 				checkedType = this.getTokenType(temp);
 				
 				if ( currentType != checkedType )
 				{
 					if ( checkedType == TokenType.Unknown )
 					{
 						temp = temp.substring( 0, temp.length() - 1 );
 						
 						// if selected string is empty.
 						if ( temp.equals("") )
 							throw new TokenAnalysisException();
 						
 						TokenSubtype tsType = this.getTokenSubtype( temp, currentType );
 						
 						// if selected string is unknown type of token.
 						if ( currentType == TokenType.Unknown && tsType == TokenSubtype.Unknown )
 							throw new TokenAnalysisException();
 						
 						TokenUnit newUnit = new TokenUnit( currentType, tsType, temp );
 						result.addToken( newUnit );
 					
 						currentType = TokenType.Unknown;
 						temp = "";
 					
 						i--;
 					}
 					else
 					{
 						currentType = checkedType;
 					}
 				}
 			}
 			else 
 			{
 				checkedType = this.getTokenType(temp);
 				
 				if ( currentType != checkedType )
 				{
 					if ( checkedType == TokenType.Unknown )
 					{
 						temp = temp.substring( 0, temp.length() - 1 );
 						
 						// if selected string is empty
 						if ( temp.equals("") )
 							throw new TokenAnalysisException();
 						
 						TokenSubtype tsType = this.getTokenSubtype( temp, currentType );
 						
 						// if selected string is unknown type of token.
 						if ( currentType == TokenType.Unknown && tsType == TokenSubtype.Unknown )
 							throw new TokenAnalysisException();
 						
 						TokenUnit newUnit = new TokenUnit( currentType, tsType, temp );
 						result.addToken( newUnit );
 						
 						temp = "";
 						currentType = TokenType.Unknown;
 						i--;
 					}
 					else
 					{
 						currentType = checkedType;
 					}
 				}
 			}
 		}
 		
 		TokenSubtype tsType = this.getTokenSubtype( temp, currentType );
 		
 		// if selected string is unknown type of token.
 		if ( currentType == TokenType.Unknown && tsType == TokenSubtype.Unknown )
 			throw new TokenAnalysisException();
 		
 		TokenUnit newUnit = new TokenUnit( currentType, tsType, temp );
 		result.addToken( newUnit );
 		
 		return result;
 	}
 
 }
