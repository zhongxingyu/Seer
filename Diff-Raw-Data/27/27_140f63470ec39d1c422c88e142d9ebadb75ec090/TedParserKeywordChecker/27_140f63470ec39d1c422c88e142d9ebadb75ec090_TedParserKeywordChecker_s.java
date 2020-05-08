 package ted;
 
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * The tedkeywordchecker checks a string against a keyword string from the user
  * 
  * @author Roel
  * @author Joost
  *
  * ted License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  *
  */
 public class TedParserKeywordChecker 
 {
 
 	/****************************************************
 	 * LOCAL METHODS
 	 ****************************************************/
 	/**
 	 * Checks a keyword expression against a string
 	 * @param parseTekst Keyword Expression
 	 * @param name String we have to match
 	 * @return Are all keywords in the string?
 	 */
 	private boolean ExpressieChecker ( String parseTekst, String name )
 	{
 		// if the user has not set any keywords in the serie
 		if (parseTekst == null || parseTekst.equals(""))
 		{
 			return true;
 		}
 		
 		// remove all spaces from keywords and name
 		parseTekst = parseTekst.replace(" ", "");
 		name = name.replace(" ", "");
 		
 		String operator, partje;
 		String links = " ";
 		String rechts = " ";
 		int aantalHaakjes;
 		aantalHaakjes = 0;
 		boolean leftnot = false;
 		boolean rightnot = false;
 
 		TedKeywordTokenizer stringTok = new TedKeywordTokenizer (parseTekst);
 		
 		if (stringTok.hasMoreTokens())
 		{
 			partje = stringTok.nextToken();
 		}
 		else
 		{
 			return true;
 		}
 				
 		// when we encounter a not, this means that the entire left side has to be negated
 		if (partje.equals("!"))
 		{
 			leftnot = true;
 			partje = stringTok.nextToken();
 		}
 		// left side of operator
 		if (partje.equals("("))
 		{
 			aantalHaakjes ++;
 			while ( aantalHaakjes > 0 )
 			{
 				partje = stringTok.nextToken();
 				
 				if (partje.equals("("))
 				{
 					aantalHaakjes ++;
 				}
 				else if (partje.equals(")"))
 				{
 					aantalHaakjes --;
 				}
 				if (aantalHaakjes != 0)
 				{
 					links = links + " " + partje;
 				}
 			}
 			
 			// operator
 			operator = stringTok.nextToken();
 			
 			if (!operator.equals("|") && !operator.equals("&") && !operator.equals(","))
 			{
 				// no operator so not necessary to check right side
 				aantalHaakjes -= 1;
 			}
 			
 			// right side of expression
 			partje = stringTok.nextToken();
 			if (partje.equals("!"))
 			{
 				rightnot = true;
 				partje = stringTok.nextToken();
 			}
 			if (partje.equals("("))
 			{
 				aantalHaakjes ++;
 			}
 			while ( aantalHaakjes > 0 )
 			{
 				partje = stringTok.nextToken();
 				
 				if (partje.equals("("))
 				{
 					aantalHaakjes ++;
 				}
 				else if (partje.equals(")"))
 				{
 					aantalHaakjes --;
 				}
 				if (aantalHaakjes != 0)
 				{
 					rechts = rechts + " " + partje;
 				}
 			}
 			
 			// recursion to check value of leftside
 			boolean leftside = ExpressieChecker ( links, name );
 			if (leftnot)
 			{
 				leftside = !leftside;
 			}
 			
 			// combine left and rightside with the operator
 			if ( operator.equals("|") )
 			{
 				boolean rightside = ExpressieChecker ( rechts, name );
 				if (rightnot)
 				{
 					rightside = !rightside;
 				}
 				return ( leftside || rightside );
 			}
 			else if ( operator.equals("&") || operator.equals(",") )
 			{
 				boolean rightside = ExpressieChecker ( rechts, name );
 				if (rightnot)
 				{
 					rightside = !rightside;
 				}
 				return ( leftside && rightside );
 			}
 			else
 			{
 				return leftside;
 			}
 		}
 		
 		// something without brackets		
 		else
 		{
 			boolean result;
 			if (leftnot)
 			{
 				result = !checkWord(partje, name);
 			}
 			else
 			{
 				result = checkWord(partje, name);
 			}
 			String nextpartje;
 			
 			// walk through all the words untill we dont have any
 			while (stringTok.hasMoreTokens())
 			{
 				partje = stringTok.nextToken();
 				nextpartje = stringTok.nextToken();
 				boolean nextpartjeresult;
 				if (nextpartje.equals("!"))
 				{
 					nextpartje = stringTok.nextToken();
 					nextpartjeresult = !checkWord(nextpartje, name);
 				}
 				else
 				{
 					nextpartjeresult = checkWord(nextpartje, name);
 				}
 				
 				
 				if (partje.equals("&") || partje.equals(","))
 				{
 					result = result && nextpartjeresult;
 				}
 				else if (partje.equals("|"))
 				{
 					result = result || nextpartjeresult;
 				}
 				
 			}
 			
 			return result;
 		}	
 	}
 
 	/**
 	 * Checks if one word is in the string
 	 * @param partje Word
 	 * @param name String
 	 * @return If the word is in the string
 	 */
 	private boolean checkWord(String partje, String name) 
 	{
 		int match = name.indexOf(partje);
 		if (match != -1)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	
 	/****************************************************
 	 * PUBLIC METHODS
 	 ****************************************************/
 	/**
 	 * @param name String we have to check
 	 * @param keywords Keywords that have to be in the string
 	 * @return If the keywords match with the string
 	 */
 	public boolean checkKeywords(String name, String keywords)
 	{		
 		return this.ExpressieChecker(keywords, name);
 	}
 	
 	
 }
