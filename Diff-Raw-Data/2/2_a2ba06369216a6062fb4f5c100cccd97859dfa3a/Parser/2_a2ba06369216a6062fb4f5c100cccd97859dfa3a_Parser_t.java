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
 import se.kth.maandree.jcbnfp.elements.*;
 
 import java.util.*;
 import java.io.*;
 
 
 /**
  * Code parser class using parsed syntax
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Parser
 {
     /**
      * Constructor
      * 
      * @param  definitions  Definition map
      * @param  main         The main definition, normally the title of the JCBNF file
      */
     public Parser(final HashMap<String, Definition> definitions, final String main)
     {
 	this.definitions = definitions;
 	this.main = main;
     }
     
     
     
     /**
      * Definition map
      */
     private final HashMap<String, Definition> definitions;
     
     /**
      * The main definition
      */
     private final String main;
     
     /**
      * The data in the last read stream
      */
     public int[] data;
     
     
     
     /**
      * Parses a stream and builds a tree of the result
      * 
      * @param   is  The data stream to parse
      * @return      The tree with the result, describing the data, <code>null</code> if the gammar does not match
      * 
      * @throws  IOException                    On I/O exception
      * @throws  UndefiniedDefinitionException  If the JCBNF file is refering to an undefinied definition
      */
     public ParseTree parse(final InputStream is) throws IOException, UndefiniedDefinitionException
     {
 	final int BUF_SIZE = 2048;
 	final ArrayList<int[]> bufs = new ArrayList<int[]>();
 	int[] buf = new int[BUF_SIZE];
 	int ptr = 0;
 	
 	for (int d; (d = is.read()) != -1;)
 	{
 	    if (d < 0x80)
 		buf[ptr++] = d;
 	    else if ((d & 0xC0) != 0x80)
 	    {
 		int n = 0;
 		while ((d & 0x80) != 0)
 		{
 		    n++;
 		    d <<= 1;
 		}
 		d = (d & 255) >> n;
 		for (int i = 0; i < n; i++)
 		{
 		    final int v = is.read();
 		    if ((v & 0xC0) != 0x80)
 			break;
 		    d = (d << 6) | (d & 0x3F);
 		}
 	    }
 	    
 	    if (ptr == BUF_SIZE)
 	    {
 		bufs.add(buf);
 		ptr = 0;
 		buf = new int[BUF_SIZE];
 	    }
 	}
 	
 	final int[] text = new int[bufs.size() * BUF_SIZE + ptr];
 	int p = 0;
 	for (final int[] b : bufs)
 	{
 	    System.arraycopy(b, 0, text, p, BUF_SIZE);
 	    p += BUF_SIZE;
 	}
 	bufs.clear();
 	System.arraycopy(buf, 0, text, p, ptr);
 	
 	
 	final Definition root = this.definitions.get(this.main);
 	final ParseTree tree = new ParseTree(null, root, this.definitions);
 	if (tree.parse(this.data = text, 0) < 0)
 	    return null;
 	return tree;
     }
     
     
     //TODO public compile()
     
     
     /**
      * Tests whether the data can pass a stored data chunk
      * 
      * @param   data   The data
      * @param   off    The offset in the data
      * @param   start  The start of the stored data chunk, inclusive
      * @param   end    The end of the stored data chunk, exclusive
      * @return         <code>-1</code> if it didn't pass, otherwise, the number of used characters
      */
     static int passes(final int[] data, final int off, final int start, final int end)
     {
 	final int n = end - start;
 	
 	if (data.length - off < n)
 	    return -1;
 	
 	for (int i = 0; i < n; i++)
 	    if (data[i + off] != data[i + start])
 		return -1;
 	
 	return n;
     }
     
     
     /**
      * Tests whether the data can pass a stored data chunk, with replacement
      * 
      * @param   data      The data
      * @param   off       The offset in the data
      * @param   start     The start of the stored data chunk, inclusive
      * @param   end       The end of the stored data chunk, exclusive
      * @param   replacee  The replacement replacee
      * @param   replacer  The replacement replacer
      * @return            <code>-1</code> if it didn't pass, otherwise, the number of used characters
      */
     static int passes(final int[] data, final int off, final int start, final int end, final int[] replacee, final int[] replacer)
     {
 	final boolean[] preset = new boolean[256];
 	final HashSet<Integer> set = new HashSet<Integer>();
 	
 	outer:
 	    for (int j = start; j < end; j++)
 		if (data[j] == replacee[0]) //yes, this not that effecive, but who cares, compiling code should take hours
 		{
 		    final int n = replacee.length;
 		    if (j + n < data.length)
 			break;
 		    
 		    for (int i = 0; i < n; i++)
 			if (data[j + i] != replacee[i])
 			    continue outer;
 		    
 		    preset[j] = true;
 		    set.add(Integer.valueOf(j));
 		    j += replacee.length;
 		}
 	
 	int j = start;
 	for (int i = off, n = data.length; j < end; i++, j++)
 	{
 	    if (i >= n)
 		return -1;
 	    
 	    if (preset[j & 255] && set.contains(new Integer(j)))
 	    {
 		for (int k = 0, m = replacer.length; k < m; k++, i++)
 		    if (data[i] != replacer[k])
 			return -1;
 		j += replacee.length;
 	    }
 	    
 	    if (data[i] != data[j])
 		return -1;
 	}
 	
 	return j - start;
     }
     
     
     /**
      * Tests whether the data can pass an atomary grammar element
      * 
      * @param   data  The data
      * @param   off   The offset in the data
      * @param   def   The grammar element
      * @return        <code>-1</code> if it didn't pass, <code>-2</code> if not atomary,
      *                otherwise, the number of used characters
      */
     static int passes(final int[] data, final int off, final GrammarElement def)
     {
 	if (def == null)
 	    return 0;
 	
 	if (def instanceof JCBNFString)
 	{
 	    final int[] grammar = ((JCBNFString)def).string;
 	    final int n = grammar.length;
 	    final int m = data.length;
 	    
 	    if (off + n >= m)
 		return -1;
 	    
 	    for (int i = 0; i < n; i++)
 		if (data[i + off] != grammar[i])
 		    return -1;
 	    
 	    return n;
 	}
 	if (def instanceof JCBNFWordString)
 	{
 	    final int[] grammar = ((JCBNFWordString)def).string;
 	    final int n = data.length;
 	    final int m = grammar.length;
 	    
 	    if (off + n >= m)
 		return -1;
 	    
 	    int prev = off >= 0 ? -1 : data[off - 1];
 	    int next = off >= n ? -1 : data[off];
 	    
 	    if (JCBNFCheck.w.check(prev, next) == false)
 		return -1;
 	    
 	    for (int i = 0; i < n; i++)
 		if (data[i + off] != grammar[i])
 		    return -1;
 	    
 	    prev = off + m >= 0 ? -1 : data[off + m - 1];
 	    next = off + m >= n ? -1 : data[off + m];
 	    
 	    if (JCBNFCheck.w.check(prev, next) == false)
 		return -1;
 	    
 	    return n;
 	}
 	if (def instanceof JCBNFPartialString)
 	{
 	    final int[] grammar = ((JCBNFPartialString)def).string;
 	    final int n = grammar.length;
 	    final int m = data.length;
 	    
 	    if (n == 0)
 		return 0;
 	    
 	    if ((off >= m) || (data[off] != grammar[0]))
 		return -1;
 	    
 	    for (int i = 1; i < n; i++)
 		if ((i + off >= m) || (data[i + off] != grammar[i]))
 		    return i;
 	    
 	    return n;
 	}
 	if (def instanceof JCBNFCharacters)
         {
 	    final JCBNFCharacters grammar = (JCBNFCharacters)def;
 	    final int n = data.length;
 	    
 	    if (off >= n)
 		return -1;
 	    
 	    return grammar.contains(data[off]) ? 1 : -1;
 	}
 	if (def instanceof JCBNFCheck)
 	{
 	    final JCBNFCheck grammar = (JCBNFCheck)def;
 	    final int n = data.length;
 	    
	    final int prev = off <= 0 ? -1 : data[off - 1];
 	    final int next = off >= n ? -1 : data[off];
 	    
 	    return grammar.check(prev, next) ? 0 : -1;
 	}
 	
 	return -2;
     }
     
     
     /**
      * Simplifies a grammar node so that only bounded repeat (without option),
      * juxtaposition, alternation, store and backtracks (with and without replacements)
      * as well as atoms are used.
      * 
      * @param   element  The grammar element
      * @return           The grammar element simplified
      */
     static GrammarElement assemble(final GrammarElement element)
     {
 	GrammarElement elem = element;
 	
 	while (elem != null)
 	    if (elem instanceof JCBNFGroup)
 		elem = ((JCBNFGroup)elem).element;
 	    else if (elem instanceof JCBNFOption)
 	    {
 		final JCBNFBoundedRepeation bndrep = new JCBNFBoundedRepeation(0, 1);
 		bndrep.element = ((JCBNFOption)elem).element;
 		elem = bndrep;
 	    }
 	    else if (elem instanceof JCBNFRepeation)
 	    {
 		final JCBNFBoundedRepeation bndrep = new JCBNFBoundedRepeation(1, -1);
 		bndrep.element = ((JCBNFRepeation)elem).element;
 		elem = bndrep;
 	    }
 	    else if ((elem instanceof JCBNFBoundedRepeation) && (((JCBNFBoundedRepeation)elem).option != null))
 	    {
 		final JCBNFBoundedRepeation e = (JCBNFBoundedRepeation)elem;
 		final JCBNFJuxtaposition juxta = new JCBNFJuxtaposition();
 		final JCBNFBoundedRepeation opt = new JCBNFBoundedRepeation(0, -1);
 		opt.element = e.option;
 		e.option = null;
 		juxta.elements.add(opt);
 		juxta.elements.add(e.element);
 		e.element = juxta;
 	    }
 	    else if ((elem instanceof JCBNFJuxtaposition) && (((JCBNFJuxtaposition)elem).elements.size() <= 1))
 		if (((JCBNFJuxtaposition)elem).elements.size() == 1)
 		    elem = ((JCBNFJuxtaposition)elem).elements.get(0);
 		else
 		    break;
 	    else if ((elem instanceof JCBNFAlternation) && (((JCBNFAlternation)elem).elements.size() <= 1))
 		if (((JCBNFAlternation)elem).elements.size() == 1)
 		    elem = ((JCBNFAlternation)elem).elements.get(0);
 		else
 		    break;
 	    else
 		break;
 	
 	return elem;
     }
     
 }
 
