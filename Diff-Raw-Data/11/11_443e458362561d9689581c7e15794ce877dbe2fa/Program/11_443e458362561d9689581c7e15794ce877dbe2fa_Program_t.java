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
  * This is the main entry point of the program
  * 
  * @author   Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  * @version  1.0
  */
 public class Program
 {
     /**
      * Forbidden constructor
      */
     private Program()
     {
 	assert false : "You may not create instances of this class.";
     }
     
     
     
     /**
      * This is the main entry point of the program
      * 
      * @param  args  Command line arguments
      */
     public static void main(final String... args)
     {
 	final PrintStream stderr = System.err;
 	final PrintStream stdout = System.out;
 	final PrintStream devNull = new PrintStream(new OutputStream() { public void write(int b) { /* do nothing */ } });
 	
 	final String jcbnfFile = args[0];
 	final String parseFile = args[1];
 	final String main      = args[2];
 	
 	InputStream gis = null, fis = null;
 	try
 	{
 	    System.setOut(devNull);
 	    System.setErr(devNull);
 	    
 	    System.out.println("--- Parsing Syntax ---\n\n");
 	    
 	    gis = new BufferedInputStream(new FileInputStream(new File(jcbnfFile)));
 	    final HashMap<String, Definition> defs = GrammarParser.parseGrammar(gis);
 	    for (final Definition def : defs.values())
 	    {
 		printGrammar(def);
 		System.out.println("\n");
 	    }
 	    
 	    System.out.println("--- Parsing code ---\n\n");
 	    
 	    final Parser parser = new Parser(defs, main);
 	    fis = new BufferedInputStream(new FileInputStream(new File(parseFile)));
 	    final ParseTree tree = parser.parse(fis);
 	    System.out.println("\n");
 	    
 	    System.setOut(stdout);
 	    System.setErr(stderr);
 	    
 	    if (tree == null)
 		System.out.println("===### Grammar did not match ###===\n\n");
 	    else
 	    {
 		System.out.println("--- Parsed code ---\n\n");
 		printTree(tree, parser.data);
 		System.out.println("\n");
 	    }
 	}
 	catch (final SyntaxFileError err)
 	{
	    System.setOut(stdout);
	    System.setErr(stderr);
 	    System.err.println("ERROR: " + err.getMessage());
 	    if (err.getCause() != null)
 		err.getCause().printStackTrace(System.err);
 	}
 	catch (final UndefiniedDefinitionException err)
 	{
	    System.setOut(stdout);
	    System.setErr(stderr);
 	    System.err.println("ERROR: " + err.getMessage());
 	}
 	catch (final RuntimeException err)
 	{
	    System.setOut(stdout);
	    System.setErr(stderr);
 	    System.err.print("ERROR: ");
 	    err.printStackTrace(System.err);
 	}
 	catch (final Throwable err)
 	{
	    System.setOut(stdout);
	    System.setErr(stderr);
 	    System.err.println("---SYSTEM ERROR---");
 	    err.printStackTrace(System.err);
 	}
 	finally
 	{
 	    if (gis != null)
 		try
 		{   gis.close();
 		}
 		catch (final Throwable err)
 		{   //Ignore
 		}
 	    if (fis != null)
 		try
 		{   fis.close();
 		}
 		catch (final Throwable err)
 		{   //Ignore
 		}
 	}
     }
     
     
     /**
      * Prints out a parsed tree
      * 
      * @param  tree  The tree
      * @param  data  The parsed data
      * 
      * @throws  Exception  Yay!
      */
     public static void printTree(final ParseTree tree, final int[] data) throws Exception
     {
 	final ArrayDeque<ParseTree> nodes = new ArrayDeque<ParseTree>();
 	final ArrayDeque<String> indents = new ArrayDeque<String>();
 	
 	nodes.add(tree);
 	indents.add("");
 	
 	ParseTree node;
 	while ((node = nodes.pollLast()) != null)
 	{
 	    String indent = indents.pollLast();
 	    
 	    System.err.print(indent);
 	    System.err.print(node.definition.definition);
 	    System.err.print(" :: (");
 	    System.err.print(node.intervalStart);
 	    System.err.print(", ");
 	    System.err.print(node.intervalEnd - node.intervalStart);
 	    System.err.print(", ");
 	    System.err.print(node.intervalEnd);
 	    System.err.print(")  \"\033[32m");
 	    System.err.write(Escaper.escape(data, node.intervalStart, node.intervalEnd - node.intervalStart));
 	    System.err.println("\033[39m\"\033[35m");
 	    System.err.println(indent + "(:: " + node.definition.name + " ::)");
 	    //node.definition.definition.printGrammar(indent + "::= ");
 	    System.err.print("\033[21;33m");
 	    for (final int[] warning : node.definition.warnings)  System.err.println(indent + "w-- " + Util.intArrayToString(warning));
 	    for (final int[] warning : node.definition.uniques)   System.err.println(indent + "w== " + Util.intArrayToString(warning));
 	    System.err.print("\033[31m");
 	    for (final int[] error : node.definition.oopses)      System.err.println(indent + "<-- " + Util.intArrayToString(error));
 	    System.err.print("\033[1m");
 	    for (final int[] error : node.definition.panics)      System.err.println(indent + "<== " + Util.intArrayToString(error));
 	    System.err.print("\033[21;39m");
 	    
 	    indent += "    ";
 	    
 	    for (int i = node.children.size() - 1; i >= 0; i--)
 	    {
 		nodes.offerLast(node.children.get(i));
 		indents.offerLast(indent);
 	    }
 	}
     }
     
     
     /**
      * Prints out a definition
      * 
      * @param  definition  The definition
      */
     @SuppressWarnings({"rawtypes", "unchecked"})
     public static void printGrammar(final Definition definition)
     {
 	System.err.print(definition.toString());
 	
 	System.out.println("(:: " + definition.name + " ::)");
 	
 	if (definition.definition != null)
 	    definition.definition.printGrammar("::= ");
 	
 	if (definition.compiles != null)
 	    definition.compiles.printGrammar("==> ");
     }
     
 }
 
