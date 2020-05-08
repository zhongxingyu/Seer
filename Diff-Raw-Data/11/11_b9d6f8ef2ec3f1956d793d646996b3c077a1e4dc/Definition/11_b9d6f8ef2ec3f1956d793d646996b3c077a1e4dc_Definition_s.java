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
 package se.kth.maandree.jcbnfp;
 import  se.kth.maandree.jcbnfp.elements.*;
 
 import java.util.*;
 
 
 /**
  * JCBNF definition class
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Definition
 {
     /**
      * Constructor
      * 
      * @param  name        The name of the definition
      * @param  definition  The definition
      * @param  compiles    To what the definition compiles
      * @param  oopses      All oopses, non-stopping errors
      * @param  panics      All panics, stopping errors
      * @param  warnings    All warnings
      * @param  uniques     All unique warnings
      * 
      * @throws  SyntaxFileError  If there is something wrong with the JCBNF file
      */
     public Definition(final String name, final int[] definition, final int[] compiles,
 		      final ArrayList<int[]> oopses, final ArrayList<int[]> panics,
 		      final ArrayList<int[]> warnings, final ArrayList<int[]> uniques) throws SyntaxFileError
     {
 	
 	final int[] ref = {0};
 	this.name       = name;
 	this.definition = definition == null ? null : parseGrammar(definition, 0, ref);
 	this.compiles   = compiles == null ? null : parseGrammar(compiles, 0, ref);
 	this.oopses     = oopses;
 	this.panics     = panics;
 	this.warnings   = warnings;
 	this.uniques    = uniques;
     }
     
     
     
     /**
      * The name of the definition
      */
     public final String name;
     
     /**
      * The definition
      */
     public final GrammarElement definition;
     
     /**
      * To what the definition compiles
      */
     public final GrammarElement compiles;
     
     /**
      * All oopses, non-stopping errors
      */
     public final ArrayList<int[]> oopses;
     
     /**
      * All panics, stopping errors
      */
     public final ArrayList<int[]> panics;
     
     /**
      * All warnings
      */
     public final ArrayList<int[]> warnings;
     
     /**
      * All unique warnings
      */
     public final ArrayList<int[]> uniques;
     
     
     
     /**
      * Parse a grammar
      * 
      * @param   grammar  The grammar as character array
      * @param   off      The beginning of the part to parse
      * @param   ref      A ref-array for the end of the parsed part
      * @return           The grammar
      * 
      * @throws  SyntaxFileError  If there is something wrong with the JCBNF file
      */
     private GrammarElement parseGrammar(final int[] grammar, final int off, final int[] ref) throws SyntaxFileError
     {
 	final ArrayList<GrammarElement> elems = new ArrayList<GrammarElement>();
 	final ArrayList<GrammarElement> alts  = new ArrayList<GrammarElement>();
 	
 	
 	int last = 0, i = off;
 	for (int c, n = grammar.length; i < n; i++)
 	{
 	    if (((c = grammar[i]) == ' ') || (c == '\t'))
 		continue;
 	    
 	    if ((c == ')') || (c == ']') || (c == '}') || (c == '>'))
 	    {
 		i++;
 		break;
 	    }
 	    
 	    if (c == '\"')
 	    {
 		final JCBNFString str = new JCBNFString(grammar, i + 1, ref);
 		if (last == '@')
 		    elems.add(new JCBNFCharacters.JCBNFCharacterGroup(str.string));
 		else
 		    elems.add(str);
 		i = ref[0];
 	    }
 	    else if (c == '\'')
 	    {
 		elems.add(new JCBNFWordString(grammar, i + 1, ref));
 		i = ref[0];
 		
 		if (last == -2)
 		{
 		    final GrammarElement min = elems.get(elems.size() - 2);
 		    final GrammarElement max = elems.get(elems.size() - 1);
 		    
 		    if ((min instanceof JCBNFWordString) && (max instanceof JCBNFWordString))
 		    {
 			final int[] minarr = ((JCBNFWordString)min).string;
 			final int[] maxarr = ((JCBNFWordString)max).string;
 			
 			if ((minarr.length == 1) && (maxarr.length == 1))
 			{
 			    elems.remove(elems.size() - 2);
 			    elems.remove(elems.size() - 1);
 			    
 			    elems.add(new JCBNFCharacters.JCBNFCharacterRange(minarr[0], maxarr[0]));
 			}
 		    }
 		}
 	    }
 	    else if (c == '\\')
 	    {
		elems.add(parseEscape(c = grammar[++i], grammar, i, ref));
 		i = ref[0];
 	    }
 	    else if ((c == '$') || (c == '@') || (c == '^'))
 	    {
 		//do nothing
 	    }
 	    else if (c == '.')
 	    {
 		if (last == '.')
 		{
 		    last = -2;
 		    continue;
 		}
 	    }
 	    else if ((('0' <= c) && (c <= '9')) || (('a' <= c) && (c <= 'z')) || (('A' <= c) && (c <= 'Z')))
 	    {
 		final int[] buf = new int[grammar.length];
 		int ptr = 0;
 		
 		buf[ptr++] = c;
 		i++;
 		while (i < n)
 	        {
 		    c = grammar[i++];
 		    if ((('0' <= c) && (c <= '9')) || (('a' <= c) && (c <= 'z')) || (('A' <= c) && (c <= 'Z')))
 			buf[ptr++] = c;
 		    else
 		    {
 			i -= 2;
 			break;
 		    }
 		}
 		
 		final String name = new String(buf, 0, ptr);
 		
 		if (last == '$')
 		{
 		    final JCBNFCharacters.JCBNFCharacterClass.Set set;
 		    if      (name.equalsIgnoreCase("any"))        set = JCBNFCharacters.JCBNFCharacterClass.Set.ANY;
 		    else if (name.equalsIgnoreCase("sub"))        set = JCBNFCharacters.JCBNFCharacterClass.Set.SUB;
 		    else if (name.equalsIgnoreCase("sup"))        set = JCBNFCharacters.JCBNFCharacterClass.Set.SUP;
 		    else if (name.equalsIgnoreCase("letter"))     set = JCBNFCharacters.JCBNFCharacterClass.Set.LETTER;
 		    else if (name.equalsIgnoreCase("letteroid"))  set = JCBNFCharacters.JCBNFCharacterClass.Set.LETTEROID;
 		    else if (name.equalsIgnoreCase("digit"))      set = JCBNFCharacters.JCBNFCharacterClass.Set.DIGIT;
 		    else
 			set = null;
 		    elems.add(new JCBNFCharacters.JCBNFCharacterClass(set));
 		}
 		else
 		    elems.add(new JCBNFDefinition(name));
 	    }
 	    else if (c == '|')
 		if (elems.size() == 1)
 		{
 		    alts.addAll(elems);
 		    elems.clear();
 		}
 		else
 		{
 		    final JCBNFJuxtaposition juxta = new JCBNFJuxtaposition();
 		    juxta.elements.addAll(elems);
 		    alts.add(juxta);
 		    elems.clear();
 		}
 	    else if (c == '(')
 	    {
 		final JCBNFGroup elem = new JCBNFGroup();
 		int s;
 		elem.element = parseGrammar(grammar, s = i + 1, ref);
 		i = ref[0];
 		elems.add(elem);
 		
 		if (elem.element instanceof JCBNFAlternation)
 		{
 		    final JCBNFAlternation e = (JCBNFAlternation)(elem.element);
 		    if (e.elements.get(0) instanceof JCBNFDefinition)
 		    {
 			final String name = ((JCBNFDefinition)(e.elements.get(0))).name;
 			int num = 0;
 			for (int j = 0, cc, m = name.length(); j < m; j++)
 			    if (('0' <= (cc = name.charAt(j))) && (cc <= '9'))
 				num = (num * 10) - (cc & 15);
 			    else
 			    {
 				num = 1;
 				break;
 			    }
 			if (num <= 0)
 			{
 			    num = -num;
 			    e.elements.remove(0);
 			    
 			    boolean pos = false;
 			    boolean neg = false;
 			    boolean cln = false;
 			    
 			    for (int cc, j = s + name.length(), m = i; j < m; j++)
 			    {
 				cc = grammar[i];
 				
 				if      (cc == '+')  pos = true;
 				else if (cc == '-')  neg = true;
 				else if (cc == ':')  cln = true;
 				
 				if (cln || (cc == '|'))
 				    break;
 			    }
 			    
 			    GrammarElement opt = null;
 			    
 			    if (cln)
 			    {
 				opt = e.elements.get(0);
 				e.elements.remove(0);
 				for (;;)
 				    if      (opt instanceof JCBNFGroup)      opt = ((JCBNFGroup)opt).element;
 				    else if (opt instanceof JCBNFOption)     opt = ((JCBNFOption)opt).element;
 				    else if (opt instanceof JCBNFRepeation)  opt = ((JCBNFRepeation)opt).element;
 				    else
 					break;
 			    }
 			    
 			    final GrammarElement repElem = e.elements.size() == 1 ? e.elements.get(0) : e;
 			    
 			    final JCBNFBoundedRepeation bounded;
 			    if (pos == neg)  bounded = new JCBNFBoundedRepeation(num, num);
 			    else if (pos)    bounded = new JCBNFBoundedRepeation(num, -1);
 			    else             bounded = new JCBNFBoundedRepeation(1, num);
 			    bounded.option = opt;
 			    bounded.element = repElem;
 			    elems.remove(elems.size() - 1);
 			    elems.add(bounded);
 			}
 		    }
 		}
 		
 		if (elems.get(elems.size() - 1) instanceof JCBNFGroup)
 		{
 		    final JCBNFGroup g = (JCBNFGroup)(elems.get(elems.size() - 1));
 		    if (g.element != null)
 		    {
 			elems.add(g.element);
 			elems.remove(elems.size() - 2);
 		    }
 		}
 	    }
 	    else if (c == '[')
 	    {
 		final JCBNFOption elem = new JCBNFOption();
 		elem.element = parseGrammar(grammar, i + 1, ref);
 		i = ref[0] - 1;
 		elems.add(elem);
 		if (elem.element instanceof JCBNFOption)
 		    if (((JCBNFOption)(elem.element)).element instanceof JCBNFString)
 		    {
 			final int[] str = ((JCBNFString)(((JCBNFOption)(elem.element)).element)).string;
 			final JCBNFPartialString pstr = new JCBNFPartialString(str);
 			elems.remove(elems.size() - 1);
 			elems.add(pstr);
 		    }
 	    }
 	    else if (c == '{')
 	    {
 		final JCBNFRepeation elem = new JCBNFRepeation();
 		elem.element = parseGrammar(grammar, i + 1, ref);
 		i = ref[0];
 		elems.add(elem);
 	    }
 	    else if (c == '<')
 	    {
 		final int[] buf = new int[grammar.length - i];
 		int ptr = 0;
 		while ((grammar[++i] != ' ') && (grammar[i] != '\t'))
 		    break;
 		int cc;
 		for (;; i++)
 		{
 		    cc = grammar[i];
 		    if ((cc == ' ') || (cc == '\t'))
 			continue;
 		    if (cc == '=')  break;
 		    if (cc == '|')  break;
 		    if (cc == '>')  break;
 		    buf[ptr++] = cc;
 		}
 		final String name = new String(buf, 0, ptr);
 		if (cc == '>')
 		    elems.add(new JCBNFBacktrack(name));
 		else if (cc == '=')
 		{
 		    final JCBNFStore elem;
 		    elems.add(elem = new JCBNFStore(name));
 		    elem.element = parseGrammar(grammar, i + 1, ref);
 		    i = ref[0];
 		}
 		else
 		{
 		    final GrammarElement elem = parseGrammar(grammar, i + 1, ref);
 		    i = ref[0];
 		    if ((elem instanceof JCBNFAlternation) && (((JCBNFAlternation)elem).elements.size() == 2))
 		    {
 			final GrammarElement a = ((JCBNFAlternation)elem).elements.get(0);
 			final GrammarElement b = ((JCBNFAlternation)elem).elements.get(1);
 			if ((a instanceof JCBNFString) && (b instanceof JCBNFString))
 			{
 			    final String replacee = Util.intArrayToString(((JCBNFString)a).string);
 			    final String replacer = Util.intArrayToString(((JCBNFString)b).string);
 			    
 			    elems.add(new JCBNFBacktrack(name, replacee, replacer));
 			}
 		    }
 		}
 	    }
 	    
 	    if ((last == '^') && (elems.get(elems.size() - 2) instanceof JCBNFCharacters))
 	    {
 		if (elems.get(elems.size() - 1) instanceof JCBNFWordString)
 		{
 		    final int[] str = ((JCBNFWordString)(elems.get(elems.size() - 1))).string;
 		    if (str.length == 1)
 		    {
 			elems.remove(elems.size() - 1);
 			elems.add(new JCBNFCharacters.JCBNFCharacter(str[0]));
 		    }
 		}
 		if (elems.get(elems.size() - 1) instanceof JCBNFCharacters)
 		{
 		    ((JCBNFCharacters)(elems.get(elems.size() - 2))).exceptions.add((JCBNFCharacters)(elems.get(elems.size() - 1)));
 		    elems.remove(elems.size() - 1);
 		}
 	    }
 	    
 	    last = c;
 	}
 	
 	ref[0] = i;
 	
 	if ((alts.size() > 0) && (elems.size() > 0))
 	    if (elems.size() == 1)
 	    {
 		alts.addAll(elems);
 		elems.clear();
 	    }
 	    else
 	    {
 		final JCBNFJuxtaposition juxta = new JCBNFJuxtaposition();
 		juxta.elements.addAll(elems);
 		alts.add(juxta);
 		elems.clear();
 	    }
 	
 	if (alts.size() > 0)
 	{
 	    final JCBNFAlternation rc = new JCBNFAlternation();
 	    rc.elements.addAll(alts);
 	    return rc;
 	}
 	if (elems.size() != 1) // != 1 so ==> without grammar is possible
 	{
 	    final JCBNFJuxtaposition rc = new JCBNFJuxtaposition();
 	    rc.elements.addAll(elems);
 	    return rc;
 	}
 	return elems.get(0);
     }
     
     
     /**
      * Parses an escape
      * 
      * @param   c        The escaped character
      * @param   grammar  The grammar
      * @param   off      The beginning of the part to parse
      * @param   ref      A ref-array for the end of the parsed part
      * @retrun           The element the escaped character represents, may be a check
      */
     private GrammarElement parseEscape(final int c, final int[] grammar, final int off, final int[] ref)
     {
 	ref[0] = off;
 	int cc = c;
 	switch (c)
 	{
 	    case 'a':  cc = 007;        break;
 	    case 'b':  cc = (int)'\b';  break;
 	    case 'e':  cc = 033;        break;
 	    case 'f':  cc = (int)'\f';  break;
 	    case 'n':  cc = (int)'\n';  break;
 	    case 'r':  cc = (int)'\r';  break;
 	    case 't':  cc = (int)'\t';  break;
 	    case 'v':  cc = 12;         break;
 	    case 'u':  cc = -10;        break;
 	    case 'U':  cc = -11;        break;
 	    case 'A':  cc = -1;         break;
 	    case 'Z':  cc = -2;         break;
 	    case 'z':  cc = -3;         break;
 	    case 'W':  cc = -4;         break;
 	    case 'w':  cc = -5;         break;
 	}
 	
 	if (cc == -1)  return JCBNFCheck.A;
 	if (cc == -2)  return JCBNFCheck.Z;
 	if (cc == -3)  return JCBNFCheck.z;
         if (cc == -4)  return JCBNFCheck.W;
 	if (cc == -5)  return JCBNFCheck.w;
 	
 	int i = off;
 	if ((cc == -10) || (cc == -11))
 	{
 	    final int m = cc == -10 ? 4 : 6;
 	    int val = 0;
 	    for (int j = 0; j < m; j++)
 	    {
 		final int v = grammar[++i];
 		if ((v & 48) == 48)
 		    val = (val << 4) | (v & 16);
 		else
 		    val = (val << 4) | (9 + (v & 63));
 	    }
 	    cc = val;
 	}
 	ref[0] = i;
 	return new JCBNFCharacters.JCBNFCharacter(cc);
     }
     
 }
 
