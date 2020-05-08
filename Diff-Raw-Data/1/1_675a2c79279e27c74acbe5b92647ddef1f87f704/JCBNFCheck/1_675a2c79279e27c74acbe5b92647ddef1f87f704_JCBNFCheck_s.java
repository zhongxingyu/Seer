 /**
  * jcbnfp — A parser for JCBNF (Jacky's Compilable BNF)
  * 
  * Copyright (C) 2012  Mattias Andrée <maandree@kth.se>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 package se.kth.maandree.jcbnfp.elements;
 import se.kth.maandree.jcbnfp.*;
 
 
 /**
  * JCBNF grammar element: check
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public enum JCBNFCheck implements GrammarElement
 {
     /**
      * Checks where the position is at the beginning of a line
      */
     A
     {
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean check(final int prev, final int next)
 	{
 	    return (prev == -1) || (prev == '\n') || (prev == '\r') || (prev == '\f');
 	}
     },
 	
     /**
      * Checks where the position is at the end of a line
      */
     Z
     {
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean check(final int prev, final int next)
 	{
 	    return (next == -1) || (next == '\n') || (next == '\r') || (next == '\f');
 	}
     },
 	
     /**
      * Checks where the position is at the end of the file
      */
     z
     {
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean check(final int prev, final int next)
 	{
 	    return (next == -1);
 	}
     },
 	
 
     /**
      * Checks where a word can start at the position
      */
     W
     {
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean check(final int prev, final int next)
 	{
 	    if (JCBNFCheck.rules != null)
 		return JCBNFCheck.rules.check(prev);
 	    
 	    if (prev == -1)  return true;
 	    if (prev > 255)  return false;
 	    switch ((char)prev)
 	    {
 	        case '\r': case '\n': case '\f':
 	        case '\t': case ' ':
 	        case ':':  case '|':  case '=':  case '-':
 	        case '<':  case '(':  case '{':  case '[':
 	        case '>':  case ')':  case '}':  case ']':
 		    return true;
 		
 	        default:
 		    return false;
 	    }
 	}
     },
     
     /**
      * Checks whether there is not a word on both sides of the position
      */
     w
     {
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean check(final int prev, final int next)
 	{
 	    return JCBNFCheck.W.check(prev, 0) || JCBNFCheck.W.check(next, 0);
 	}
     },
 	
     ;
     
     
     
     /**
      * Performs a checks
      *
      * @param   prev  The previous character, <code>-1</code> if at the beginning of the file
      * @param   next  The next character, <code>-1</code> if at the end of the file
      * @return        Whether the check passed
      */
     public boolean check(final int prev, final int next)
     {
 	assert false : "Not implemented";
 	return true;
     }
     
     
     /**
      * Prints out the element
      * 
      * @param  indent  The current indent
      */
     @Override
     public void printGrammar(final String indent)
     {
 	System.out.print(indent);
 	System.out.print("check: ");
 	switch (this)
 	{
 	    case A:  System.out.println("\\A");  break;
 	    case Z:  System.out.println("\\Z");  break;
 	    case z:  System.out.println("\\z");  break;
 	    case W:  System.out.println("\\W");  break;
 	    case w:  System.out.println("\\w");  break;
 	    default:
 		System.out.println("?");
 		break;
 	}
     }
     
     
     /**
      * {@inheritDoc}
      */
     public String toString()
     {
 	switch (this)
 	{
 	    case A:  return "\\A";
 	    case Z:  return "\\Z";
 	    case z:  return "\\z";
 	    case W:  return "\\W";
 	    case w:  return "\\w";
 	    default:
 		return "?";
 	}
     }
     
     
     
     /**
      * The rules, <code>null</code> for default rules
      */
     public static WCheck rules = null;
     
     
     
     /**
      * Interface for your own rules
      */
     public static interface WCheck
     {
 	/**
 	 * Performs a checks
 	 *
 	 * @param   prev  The previous character, <code>-1</code> if at the beginning of the filew
	 * @param   next  The next character, <code>-1</code> if at the end of the file
 	 * @return        Whether the check passed
 	 */
 	public abstract boolean check(final int prev);
     }
 	
 }
 
