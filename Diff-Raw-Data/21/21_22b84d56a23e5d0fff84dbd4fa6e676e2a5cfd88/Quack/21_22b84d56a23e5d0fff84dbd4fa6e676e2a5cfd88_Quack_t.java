 /**
  * nox-util — Utility programs for headless environments
  *
  * Copyright © 2012  Mattias Andrée (maandree@kth.se)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.kth.maandree.noxutil;
 
 import java.io.*;
 import java.util.*;
 
 
 /**
  * This main class of the quack program
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Quack
 {
     /**
      * Non-constructor
      */
     private Quack()
     {
 	assert false : "This class [Quack] is not meant to be instansiated.";
     }
     
     
     
     /**
      * This is the main entry point of the program
      *
      * @param   args       Search pattern
      * @throws  Exception  On any exception
      */
     public static void main(final String... args) throws Exception
     {
 	try
         {
 	    ttyin  = (new File("/dev/stdin" )).getCanonicalPath();
 	    ttyout = (new File("/dev/stdout")).getCanonicalPath();
 	    ttyerr = (new File("/dev/stderr")).getCanonicalPath();
 	    
 	    final String[] words = new String[args.length];
 	    System.arraycopy(args, 0, words, 0, args.length);
 	    
 	    for (int i = 0, n = words.length; i < n; i++)
 	    {
 		final StringBuilder buf = new StringBuilder();
 		String word = words[i];
 		if (word.contains(" "))
 		    word = '"' + word + '"';
 		for (int j = 0, m = word.length(); j < m; j++)
 		{
 		    final char c = word.charAt(j);
 		    if      (('0' <= c) && (c <= '9'))  buf.append(c);
 		    else if (('A' <= c) && (c <= 'Z'))  buf.append(c);
 		    else if (('a' <= c) && (c <= 'z'))  buf.append(c);
 		    else if (('_' == c) || (c == '-'))  buf.append(c);
 		    else
 		    {
 			final byte[] bs = Character.toString(c).getBytes("UTF-8");
 		        for (final byte b : bs)
 			{
 			    buf.append('%');
 			    buf.append("0123456789ABCDEF".charAt((b >> 4) & 15));
 			    buf.append("0123456789ABCDEF".charAt((b >> 0) & 15));
 			}
 		    }
 		}
 		words[i] = buf.toString();
 	    }
 	    
 	    final StringBuilder address = new StringBuilder("https://www.duckduckgo.com/?");
 	    if (words.length > 0)
 	    {
 		address.append("q=");
 		for (int i = 0, n = words.length; i < n; i++)
 		{
 		    if (i > 0)
 			address.append('+');
 		    address.append(words[i]);
 		}
 		address.append('&');
 	    }
 	    address.append("kh=1&kj=2b&kp=-1");
 	    
 	    exec("lynx '" + address.toString() + '\'');
 	}
 	catch (final Exception err)
 	{
 	    System.err.println("\033[31mFatal exception:\033[m\n");
 	    throw err;
 	}
     }    
     
     
     
     /**
      * The stdin
      */
     private static String ttyin;
     
     /**
      * The stdout
      */
     private static String ttyout;
     
     /**
      * The stderr
      */
     private static String ttyerr;
     
     
     
     /**
      * Executes a program pipeline in the same terminal
      *
      * @param   cmds         The program commands
      * @throws  IOException  On I/O exception
      */
     public static void exec(final String... cmds) throws IOException
     {
 	final StringBuilder tot = new StringBuilder();
 	for (int i = 0, n = cmds.length - 1; i <= n; i++)
 	{
 	    tot.append(cmds[i]);
 	    tot.append(" 2> ");
 	    tot.append(ttyerr);
 	    if (i == 0)
 	    {
 		tot.append(" < ");
 		tot.append(ttyin);
 	    }
 	    if (i == n)
 	    {
 		tot.append(" > ");
 		tot.append(ttyout);
 	    }
 	    else
 		tot.append(" | ");
 	}
 	final Process process = (new ProcessBuilder("/bin/sh", "-c", tot.toString())).start();
 	final InputStream stream = process.getInputStream();
 	for (;;)
 	    if (stream.read() == -1)
 		break;
 	try
 	{
 	    stream.close();
 	}
 	catch (final Throwable err)
 	{
 	    //Ignore
 	}
     }
 }
 
