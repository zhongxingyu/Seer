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
     @SuppressWarnings("hiding")
     public Parser(final HashMap<String, Definition> definitions, final String main)
     {
 	this.definitions = definitions;
 	this.main = main;
     }
     
     
     
     /**
      * Definition map
      */
     final HashMap<String, Definition> definitions;
     
     /**
      * The main definition
      */
     private final String main;
     
     
     
     /**
      * Tree describing parsed data, as well as parsing it
      * 
      * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
      */
     public class ParseTree
     {
 	/**
 	 * Inside a bounded repeat clause, which can be assembled from repeat clauses
 	 */
 	private static final byte REPEAT = 1;
 	
 	/**
 	 * Inside a optional clause, which is actually assembled to an [0..1] bounded repeat clause
 	 */
 	private static final byte OPTION = 2;
 	
 	
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param  parent      The parent node, <code>null</code> if none
 	 * @param  definition  The current definition, includes name, grammar &amp;c
 	 */
 	public ParseTree(final ParseTree parent, final Definition definition)
 	{
 	    this.parent = parent;
 	    this.definition = definition;
 	}
 	
 	
 	
 	/**
 	 * The parent node, <code>null</code> if none
 	 */
 	public final ParseTree parent;
 	
 	/**
 	 * The current definition, includes name, grammar &amp;c
 	 */
 	public final Definition definition;
 	
 	/**
 	 * The node's children
 	 */
 	public final ArrayList<ParseTree> children = new ArrayList<ParseTree>();
 	
 	/**
 	 * The subtree's named capture storage, may be <code>null</code>
 	 */
 	public HashMap<String, ArrayList<int[]>> storage = null;
 	
 	
 	
 	/**
 	 * Return data class for some parsing methods
 	 * 
 	 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 	 */
 	class Return
 	{
 	    //Has default constructor
 	    
 	    
 	    
 	    /**
 	     * The subtree's named capture storage, may be <code>null</code>
 	     */
 	    public HashMap<String, ArrayList<int[]>> storage = null;
 	    
 	    /**
 	     * The number of read elements in the named capture storage for a given name
 	     */
 	    public final HashMap<String, int[]> storageRead = new HashMap<String, int[]>();
 	    
 	    /**
 	     * The amount of read data
 	     */
 	    public int read = 0;
 	    
 	    
 	    
 	    /**
 	     * Concatenates data from another instance
 	     * 
 	     * @param  obj  The object with which to concatenate
 	     */
 	    private void cat(final Return obj)
 	    {
 		{
 		    HashMap<String, ArrayList<int[]>> xx = this.storage;
 		    final HashMap<String, ArrayList<int[]>> xy = obj.storage;
 		    
 		    if (xx == null)
 			this.storage = xy;
 		    else
 			for (final Map.Entry<String, ArrayList<int[]>> entry : xy.entrySet())
 			{
 			    final String key = entry.getKey();
 			    final ArrayList<int[]> values = entry.getValue();
 			    final ArrayList<int[]> vs;
 			    
 			    if ((vs = xx.get(key)) != null)
 				vs.addAll(values);
 			    else
 				xx.put(key, values);
 			}
 		}
 		{
 		    final HashMap<String, int[]> xx = this.storageRead;
 		    final HashMap<String, int[]> xy = obj.storageRead;
 		    
 		    for (final Map.Entry<String, int[]> entry : xy.entrySet())
 		    {
 			final String key = entry.getKey();
 			final int[] value = entry.getValue();
 			final int[] v;
 			
 			if ((v = xx.get(key)) != null)
 			    v[0] += value[0];
 			else
 			    xx.put(key, value);
 		    }
 		}
 	    }
 	}
 	
 	
 	
 	/**
 	 * Parses the tree and stores all data
 	 * 
 	 * @param   data  The data
 	 * @param   off   The offset in the data
 	 * @return        The amount of read data
 	 */
 	public int parse(final int[] data, final int off)
 	{
 	    @SuppressWarnings({"rawtypes", "unchecked"})
 	    final HashMap<String, ArrayList<int[]>>[] storages = (HashMap<String, ArrayList<int[]>>[])(new HashMap[32]);
 	    @SuppressWarnings({"rawtypes", "unchecked"})
	    final HashMap<String, ArrayList<int[]>>[] reads = (HashMap<String, ArrayList<int[]>>[])(new HashMap[32]);
	    final Return r = parse(data, off, this.definition.definition, storages, 0, reads, 0, 0);
 	    this.storage = r.storage;
 	    return r == null ? -1 : r.read;
 	}
 	
 	
 	/**
 	 * Find a named capture
 	 * 
 	 * @param   name            The name of the capture
 	 * @param   storages        Named capture storage stack
 	 * @param   storagePtr      Named capture storage stack pointer
 	 * @param   reads           Named capture read stack
 	 * @param   readPtr         Named capture read stack pointer
 	 * @parma   elementalState  Grammar element state
 	 * @return                  The capture's span, <code>null</code> if not found, otherwise, {start, end}
 	 */
 	private int[] backtrack(final String name, final HashMap<String, ArrayList<int[]>>[] storages, final int storagePtr,
 				final HashMap<String, int[]>[] reads, final int readPtr, final byte elementalState)
 	{
 	    ParseTree tree = this;
 	    for (;;)
 	    {
 		if (tree == null)
 		    return null;
 		
 		final HashMap<String, ArrayList<int[]>> s = this.storage;
 		if (s == null)
 		{
 		    tree = tree.parent;
 		    break;
 		}
 		
 		final ArrayList<int[]> a = s.get(name);
 		if (a == null)
 		{
 		    tree = tree.parent;
 		    break;
 		}
 		
 		//TODO ########################################################################################
 	    }
 	}
 	
 	
 	/**
 	 * Parses a subtree
 	 * 
 	 * @param   data            The data
 	 * @param   off             The offset in the data
 	 * @param   def             The grammar element to parse
 	 * @param   storages        Named capture storage stack
 	 * @param   storagePtr      Named capture storage stack pointer
 	 * @param   reads           Named capture read stack
 	 * @param   readPtr         Named capture read stack pointer
 	 * @param   elementalState  Grammar element state
 	 * @return                  Parsing subtree data
 	 */
 	@SuppressWarnings("unchecked")
 	private Return parse(final int[] data, final int off, final GrammarElement def, final HashMap<String, ArrayList<int[]>>[] storages,
 			     final int storagePtr, final HashMap<String, int[]>[] reads, final int readPtr, final byte elementalState)
 	{
 	    Return rc = new Return();
 	    final GrammarElement grammar = assemble(def);
 	    final int atom = passes(data, off, grammar);
 	    
 	    if (atom == -1) rc.read = -1;
 	    if (atom >= 0)  rc.read = off + atom;
 	    if (atom >= -1)
 		return rc;
 	    
 	    if (grammar instanceof JCBNFBacktrack)
 	    {
 		final int[] start_end = backtrack(name, storages, storagePtr, reads, readPtr, elementalState);
 		if (start_end == null)
 		    return null;
 		
 		final int start = start_end[0];
 		final int end = start_end[1];
 		
 		if (((JCBNFBacktrack)grammar).replacee != null)
 		    rc.read = passes(data, off, start, end);
 		else
 		{
 		    final int[] replacee = stringToIntArray(((JCBNFBacktrack)grammar).replacee);
 		    final int[] replacer = stringToIntArray(((JCBNFBacktrack)grammar).replacer);
 		    
 		    rc.read = passes(data, off, start, end, replacee, replacer);
 		}
 		
 		return rc.read < 0 ? null : rc;
 	    }
 	    if (grammar instanceof JCBNFStore)
 	    {
 		final String name = ((JCBNFStore)grammar).name;
 		final GrammarElement g = ((JCBNFStore)grammar).element;
 		final Return r = parse(data, off, g, storages, storagePtr, reads, readPtr, elementalState);
 		if (r.read < 0)
 		    return null;
 		
 		if (r.storage == null)
 		    r.storage = new HashMap<String, ArrayList<int[]>>();
 		
 		ArrayList<int[]> list = r.storage.get(name);
 		if (list == null)
 		    r.storage.put(name, list = new ArrayList<int[]>());
 		
 		list.add(0, new int[] {start, start + ref[0], }); // I have arbitrarly choosen to add items in parse (definition) order,
 		                                                  // rather than parsed (complete) order; however storing effected by
 		return r;                                         // this choice [for example <a=x <a=y> z>] is strongly disencouraged.
 	    }
 	    if (grammar instanceof JCBNFBoundedRepeation) //TODO ###################################################################################### reads
 	    {
 		Return r;
 		final int min = ((JCBNFBoundedRepeation)grammar).minCount;
 		final int max = ((JCBNFBoundedRepeation)grammar).maxCount;
 		final GrammarElement g = ((JCBNFBoundedRepeation)grammar).element;
 		HashMap<String, ArrayList<int[]>>[] nstorages = storages;
 		if (storagePtr == storages.length)
 		{
 		    @SuppressWarnings({"rawtypes", "unchecked"})
 		    final HashMap<String, ArrayList<int[]>>[] nnstorages = (HashMap<String, ArrayList<int[]>>[])(new HashMap[storagePtr << 1]);
 		    System.arraycopy(storages, 0, nnstorages, 0, storagePtr);
 		    nstorages = nnstorages;
 		}
 		nstorages[storagePtr] = null;
 		
 		int es = elementalState;
 		es |= min == 0 ? OPTION : 0;
 		es |= max != 1 ? REPEAT : 0;
 		
 		int offset = off;
 		for (int i = 0; i < min; i++)
 		{
		    r = parse(data, offset, g, nstorages, storagePtr + 1, es);
 		    if (r.read < 0)
 			return null;
 		    offset += r.read;
 		    rc.cat(r);
 		    if (nstorages[storagePtr] == null)
 			nstorages[storagePtr] = rc.storage;
 		}
 		for (int i = min; i != max; i++) //infinity is -1, so 'i < max' would fail
 		{
		    r = parse(data, offset, g, nstorages, storagePtr + 1, es);
 		    if (r.read < 0)
 			break;
 		    offset += r.read;
 		    rc.cat(r);
 		    if (nstorages[storagePtr] == null)
 			nstorages[storagePtr] = rc.storage;
 		}
 		
 		rc.read = offset - off;
 		return rc;
 	    }
 	    if (grammar instanceof JCBNFJuxtaposition) //TODO ###################################################################################### reads
 	    {
 		Return r;
 		int offset = off;
 		HashMap<String, ArrayList<int[]>>[] nstorages = storages;
 		if (storagePtr == storages.length)
 		{
 		    @SuppressWarnings({"rawtypes", "unchecked"})
 		    final HashMap<String, ArrayList<int[]>>[] nnstorages = (HashMap<String, ArrayList<int[]>>[])(new HashMap[storagePtr << 1]);
 		    System.arraycopy(storages, 0, nnstorages, 0, storagePtr);
 		    nstorages = nnstorages;
 		}
 		nstorages[storagePtr] = null;
 		
 		
 		for (final GrammarElement g : ((JCBNFJuxtaposition)grammar).elements)
 		{
		    r = parse(data, offset, g, nstorages, storagePtr + 1, elementalState);
 		    if (r.read < 0)
 			return null;
 		    offset += r.read;
 		    rc.cat(r);
 		    if (nstorages[storagePtr] == null)
 			nstorages[storagePtr] = rc.storage;
 		}
 		
 		rc.read = offset - off;
 		return rc;
 	    }
 	    if (grammar instanceof JCBNFAlternation)
 	    {
 		for (final GrammarElement g : ((JCBNFAlternation)grammar).elements)
 		{
 		    rc = parse(data, off, g, storages, storagePtr, reads, readPtr, elementalState);
 		    if (rc.read >= 0)
 			break;
 		    rc = null;
 		}
 		
 		return rc;
 	    }
 	    if (grammar instanceof JCBNFDefinition)
 	    {
 		final String name = ((JCBNFDefinition)grammar).name;
		final ParseTree child = new ParseTree(this, parser.this.definitions.get(name));
 		this.children.add(child);
 		final int r = child.parse(data, off);
 		rc.read = r;
 		return rc;
 	    }
 	    
 	    assert false : "Unrecognised grammar used!";
 	    return null;
 	}
 	
 	
 	/**
 	 * Creates an UTF-32 integer array from an UTF-16 string
 	 * 
 	 * @param   The string
 	 * @return  The integer array
 	 */
 	private int[] stringToIntArray(final String string)
 	{
 	    final int[] rcc = new int[string.length()];
 	    int ptr = 0;
 	    
 	    for (int i = 0, n = string.length(); i < n; i++)
 	    {
 		final char c = string.charAt(i);
 		if ((0xD800 <= c) && (c < 0xDC00))
 		{
 		    final char cc = string.charAt(++i);
 		    final int hi = c  & 0x3FF;
 		    final int lo = cc & 0x3FF;
 		    rcc[ptr++] = ((hi << 10) | lo) + 0x10000;
 		}
 		else if ((0xDC00 <= c) && (c < 0xE000))
 		{
 		    final char cc = string.charAt(++i);
 		    final int lo = c  & 0x3FF;
 		    final int hi = cc & 0x3FF;
 		    rcc[ptr++] = ((hi << 10) | lo) + 0x10000;
 		}
 		else
 		    rcc[ptr++] = c;
 	    }
 	    
 	    final int[] rc = new int[ptr];
 	    System.arraycopy(rcc, 0, rc, 0, ptr);
 	    return rc;
 	}
 	
 	
 	/**
 	 * Simplifies a grammar node so that only bounded repeat (without option),
 	 * juxtaposition, alternation, store and backtracks (with and without replacements)
 	 * as well as atoms are used.
 	 * 
 	 * @param   element  The grammar element
 	 * @return           The grammar element simplified
 	 */
 	private GrammarElement assemble(final GrammarElement element)
 	{
 	    GrammarElement elem = element;
 	    
 	    while (elem != null)
 		if (elem instanceof JCBNFGroup)
 		    elem = ((JCBNFGroup)elem).element;
 		else if (elem instanceof JCBNFOption)
 		{
 		    final JCBNFBoundedRepeation bndrep = new JCBNFBoundedRepeation(0, 1);
 		    bndrep.element = elem;
 		    elem = bndrep;
 		}
 		else if (elem instanceof JCBNFRepeation)
 		{
 		    final JCBNFBoundedRepeation bndrep = new JCBNFBoundedRepeation(1, -1);
 		    bndrep.element = elem;
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
 			elem = null;
 		else if ((elem instanceof JCBNFAlternation) && (((JCBNFAlternation)elem).elements.size() <= 1))
 		    if (((JCBNFAlternation)elem).elements.size() == 1)
 			elem = ((JCBNFAlternation)elem).elements.get(0);
 		    else
 			elem = null;
 	    
 	    return elem;
 	}
 	
 	
 	/**
 	 * Tests whether the data can pass a stored data chunk
 	 * 
 	 * @param   data   The data
 	 * @param   off    The offset in the data
 	 * @param   start  The start of the stored data chunk, inclusive
 	 * @param   end    The end of the stored data chunk, exclusive
 	 * @return         <code>-1</code> if it didn't pass, otherwise, the number of used characters
 	 */
 	private int passes(final int[] data, final int off, final int start, final int end)
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
 	private int passes(final int[] data, final int off, final int start, final int end, final int[] replacee, final int[] replacer)
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
 	    
	    for (int i = off, j = start, n = data.length; j < end; i++, j++)
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
 	    
	    return n;
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
 	private int passes(final int[] data, final int off, final GrammarElement def)
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
 		
 		return m;
 	    }
 	    if (def instanceof JCBNFWordString)
 	    {
 		final int[] grammar = ((JCBNFWordString)def).string;
 		final int n = data.length;
 		final int m = grammar.length;
 		
 		if (off + n >= m)
 		    return -1;
 		
 		int prev = off == 0 ? -1 : data[off - 1];
 		int next = off == n ? -1 : data[off];
 		
 		if (JCBNFCheck.w.check(prev, next) == false)
 		    return -1;
 		
 		for (int i = 0; i < n; i++)
 		    if (data[i + off] != grammar[i])
 			return -1;
 		
 		prev = off + m == 0 ? -1 : data[off + m - 1];
 		next = off + m == n ? -1 : data[off + m];
 		
 		if (JCBNFCheck.w.check(prev, next) == false)
 		    return -1;
 		
 		return m;
 	    }
 	    if (def instanceof JCBNFPartialString)
 	    {
 		final int[] grammar = ((JCBNFPartialString)def).string;
 		final int n = grammar.length;
 		final int m = data.length;
 		
 		if (n == 0)
 		    return 0;
 		
 		if ((off == m) || (data[off] != grammar[0]))
 		    return -1;
 		
 		for (int i = 1; i < n; i++)
 		    if ((i + off == m) || (data[i + off] != grammar[i]))
 			return i;
 		
 		return n;
 	    }
 	    if (def instanceof JCBNFCharacters)
 	    {
 		final JCBNFCharacters grammar = (JCBNFCharacters)def;
 		final int n = data.length;
 		
 		if (off == n)
 		    return -1;
 		
 		return grammar.contains(data[off]) ? 1 : -1;
 	    }
 	    if (def instanceof JCBNFCheck)
 	    {
 		final JCBNFCheck grammar = (JCBNFCheck)def;
 		final int n = data.length;
 		
 		final int prev = off == 0 ? -1 : data[off - 1];
 		final int next = off == n ? -1 : data[off];
 		
 		return grammar.check(prev, next) ? 0 : -1;
 	    }
 	    
 	    return -2;
 	}
     }
     
     
     /**
      * Parses a stream and builds a tree of the result
      * 
      * @param   is  The data stream to parse
      * @return      The tree with the result, describing the data
      * 
      * @throws  IOException  On I/O exception
      */
     public ParseTree parse(final InputStream is) throws IOException
     {
 	final int BUF_SIZE = 2048;
 	final ArrayList<int[]> bufs = new ArrayList<int[]>();
 	int[] buf = new int[BUF_SIZE];
 	int ptr = 0;
 	
 	for (int d; (d = is.read()) != -1;)
 	{
 	    if (d < 128)
 		buf[ptr++] = d;
 	    else if ((d & 192) != 128)
 	    {
 		int n = 0;
 		while ((d & 128) != 0)
 		{
 		    n++;
 		    d <<= 1;
 		}
 		d = (d & 255) >> n;
 		for (int i = 0; i < n; i++)
 		{
 		    final int v = is.read();
 		    if ((v & 192) != 128)
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
 	final ParseTree tree = new ParseTree(null, root);
	tree.parse(text);
 	return tree;
     }
     
 }
 
