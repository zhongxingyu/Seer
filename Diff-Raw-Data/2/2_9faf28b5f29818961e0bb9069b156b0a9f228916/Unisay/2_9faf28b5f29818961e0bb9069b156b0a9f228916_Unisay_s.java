 /**
  * Unisay — cowsay+ponysay rewritten in Java, with added features and full Unicode(!) support
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
 package se.kth.maandree.unisay;
 
 import java.util.*;
 import java.io.*;
 
 
 /**
  * The main class of the unisay program
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Unisay
 {
     /**
      * Non-constructor
      */
     private Unisay()
     {
 	assert false : "This class [Unisay] is not meant to be instansiated.";
     }
     
     
     
     /**
      * This is the main entry point of the program
      * 
      * @param  args  Startup arguments, start the program with </code>--help</code> for details
      * 
      * @throws  IOException  On I/O exception
      */
     public static void main(final String... args) throws IOException
     {
 	boolean help = false, anyarg = false;
 	boolean random = false, format = false, dash = false, notrunc = false;
 	boolean say = false, icp = false, fcp = false, ocp = false, quote = false;
 	boolean cows = false, ponies = false, all = false;
 	
 	for (final String arg : args)
 	    if      (Util.equalsAny(arg, "--help", "-h"))                                                help    = true;
 	    else if (Util.equalsAny(arg, "--"))                                                 anyarg = dash    = true;
 	    else if (Util.equalsAny(arg, "--random", "-r"))                                     anyarg = random  = true;
 	    else if (Util.equalsAny(arg, "--format", "-p"))                                     anyarg = format  = true;
 	    else if (Util.equalsAny(arg, "--say", "-s"))                                        anyarg = say     = true;
 	    else if (Util.equalsAny(arg, "--pony-quotes", "--ponyquotes", "--quotes", "-q"))    anyarg = quote   = true;
 	    else if (Util.equalsAny(arg, "--no-truncate", "--notruncate", "--notrunc", "-T"))   anyarg = notrunc = true;
 	    else if (Util.equalsAny(arg, "--cows", "-C"))                                       anyarg = cows    = true;
 	    else if (Util.equalsAny(arg, "--ponies", "-P"))                                     anyarg = ponies  = true;
 	    else if (Util.equalsAny(arg, "--all", "-A"))                                        anyarg = all     = true;
 	    else if (Util.equalsAny(arg, "--in-encoding", "--icp", "--ie", "-i"))               anyarg = icp     = true;
 	    else if (Util.equalsAny(arg, "--file-encoding", "--fcp", "--fe", "-f"))             anyarg = fcp     = true;
 	    else if (Util.equalsAny(arg, "--out-encoding", "--ocp", "--oe", "-o"))              anyarg = ocp     = true;
 	
 	final boolean allargs = !anyarg;
 	
 	if (help)
 	{
 	    System.out.println("unisay is a message and fortune displaying program inspired heavily by");
 	    System.out.println("cowsay, and comes with adapted images from cowsay, ponysay (from qponies),");
 	    System.out.println("and qponies. The most important thing about unisay, beside that it cows");
 	    System.out.println("with the 'My Little Ponies' from ponysay is that it supports UCS (\"Unicode\")");
 	    System.out.println("fully in all aspects through UTF-8 and other UTF, which was an issue with");
 	    System.out.println("cowsay and ponysay (wraps cowsay); but it also includes some sugar.");
 	    System.out.println();
 	    System.out.println();
 	    System.out.println("USAGE:");
 	    System.out.println();
 	    System.out.println("  unisay [-pifoTCPA <FILE> <CP> <CP> <CP>] [-s <TEXT> | -q] (-r | [--] <FILES...>)");
 	    System.out.println();
 	    System.out.println("  -p <FILE>, -i <CP>, -f <CP>, -o <CP>, -T, -C, -P and -A");
 	    System.out.println("  are mutally independent and may be included as you see fit.");
 	    System.out.println();
 	    System.out.println("  <FILES...> are whitespace separated files with ponys (or whatever),");
 	    System.out.println("  that are used printed in the output.");
 	    System.out.print("\n\n");
 	    System.out.println("OPTIONS:");
 		
 	    if (dash || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --            Disables parsing any following argument as an option.");
 	    }
 	    if (help || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --help");
 		System.out.println("  -h            Prints a this help message, you get less (than all),");
 		System.out.println("                options included in the message by adding the options");
 		System.out.println("                you want information about.");
 	    }
 	    if (format || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --format <FILE>");
 		System.out.println("  -p <FILE>     Specify the file you want to use thay provides an");
 		System.out.println("                alternative style for the baloon.");
 		System.out.println("                You may add this option multiple times if you");
 		System.out.println("                want one to be picked randomly.");
 	    }
 	    if (random || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --random");
 		System.out.println("  -r");
 		System.out.println("                Use a random, instead of the user's default, pony.");
 		System.out.println("                This options is implied by specifying pony files,");
 		System.out.println("                and by using --pony-quotes.");
 	    }
 	    if (say || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --say <TEXT>");
 		System.out.println("  -s <TEXT>     Specifies what the pony should say or think.");
 		System.out.println("                You may instead feed the text to stdin.  You");
 		System.out.println("                may (if your system allows) install fortune");
 		System.out.println("                (or fortune-mod) and, for a random qoute, run");
 		System.out.println("                    unisay --say `fortune`");
 		System.out.println("                    unisay --say $(fortune)");
 		System.out.println("                    fortune | unisay");
 		System.out.println("                You may add this option multiple times if you");
 		System.out.println("                want one to be picked randomly.");
 	    }
 	    if (quote || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --pony-quotes");
 		System.out.println("  --ponyquotes");
 		System.out.println("  --quotes");
 		System.out.println("  -q            Will use pony quotes!");
 		System.out.println("                You may specify which pony you want a quote from,");
 		System.out.println("                by specify ponies as usual.");
 		System.out.println("                This option cannot be used with --say or stdin input.");
 	    }
 	    if (notrunc || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --no-truncate");
 		System.out.println("  --notruncate");
 		System.out.println("  --notrunc");
 		System.out.println("  -T            Do not truncate the pony to the width of the terminal.");
 		System.out.println("                The width of the terminal is determined by stderr using tput.");
 	    }
 	    if (cows || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --cows");
 		System.out.println("  -C");
 		System.out.println("                Use only (unless another class is specified) cows.");
 	    }
 	    if (ponies || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --ponies");
 		System.out.println("  -P");
 		System.out.println("                Use only (unless another class is specified) ponies.");
 	    }
 	    if (all || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --all");
 		System.out.println("  -A");
 		System.out.println("                Use all images; both cows and ponies.");
 		System.out.println("                This option is default.");
 	    }
 	    if (icp || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --in-encoding <CP>");
 		System.out.println("  --icp <CP>");
 		System.out.println("  --ie <CP>");
 		System.out.println("  -i <CP>       Specifies the encoding for the input from stdin.");
 		System.out.println("                UTF-8 is default.");
 		System.out.println("                <<NOT IMPLEMENTED>>");
 	    }
 	    if (fcp || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --file-encoding <CP>");
 		System.out.println("  --fcp <CP>");
 		System.out.println("  --fe <CP>");
 		System.out.println("  -f <CP>       Specifies the encoding of files.");
 		System.out.println("                UTF-8 is default.");
 		System.out.println("                <<NOT IMPLEMENTED>>");
 	    }
 	    if (ocp || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("  --out-encoding <CP>");
 		System.out.println("  --ocp <CP>");
 		System.out.println("  --oe <CP>");
 		System.out.println("  -o <CP>       Specifies the encoding of the output from Unisay.");
 		System.out.println("                UTF-8 is default.");
 		System.out.println("                <<NOT IMPLEMENTED>>");
 	    }
 	    
 	    
 	    if (format || allargs)
 	    {
 		System.out.print("\n\n");
 		System.out.println("BALOON STYLE FILE FORMAT:");
 		System.out.print("\n\n");
 		System.out.println("  Baloon style files must include ten lines, each starting");
 		System.out.println("  start (uniquely) with one of the following exact beginnings:");
 		System.out.println("      nw:       The upper left corner of the baloon");
 		System.out.println("      n:        The upper edge of the baloon");
 		System.out.println("      ne:       The upper right corner of the baloon");
 		System.out.println("      e:        The right edge of the baloon");
 		System.out.println("      se:       The lower right corner of the baloon");
 		System.out.println("      s:        The lower edge of the baloon");
 		System.out.println("      sw:       The lower left corner of the baloon");
 		System.out.println("      w:        The left edge of the baloon");
 		System.out.println("      \\:        Link line between the baloon and the pony. (\\ direction)");
 		System.out.println("      /:        Link line between the baloon and the pony. (/ direction)");
 		//I have decided not to adopt cowsay's depenceny of the number of lines in the baloon.
 		System.out.println("  Note that the are no spaces; the text followed by the colon on");
 		System.out.println("  such a line sets the attribute's value. Hashes (#) by be used");
 		System.out.println("  as the first character on a line to make it a comment. e:, w:,");
 		System.out.println("  \\: and /: must be single lines; nw, n and ne must, however,");
 		System.out.println("  just consist of the same number of lines, same thing goes for");
 		System.out.println("  se, s and sw; additional lines must start with just a colon.");
 		    
 		    
 		System.out.print("\n\n");
 		System.out.println("THE IMAGE FILE FORMAT:");
 		System.out.print("\n\n");
 		System.out.println("  The image files are text files, as the output of the program");
 		System.out.println("  is text for terminals. ANSI esacape sequences in the text are");
 		System.out.println("  allowed and by be used drawing new ponies.");
 		System.out.println("  The image files describes the entire output of the program,");
 		System.out.println("  except the ballon's and its link's style and the message.");
 		System.out.println("  Anything written in the file will be printed, except things");
 		System.out.println("  surrounded by dollar signs ($), two dollar signes (nothing");
 		System.out.println("  surrounded by dollar sign, i.e. $$, produces one dollar sign.");
 		System.out.println("  additionally, $\\$ produces the \\ directional link line character");
 		System.out.println("  sequence between the ballon and the pony, $/$ produces the /");
 		System.out.println("  directional version of this, while $baloon#$ produces the baloon");
 		System.out.println("  with the message inside it, the hash (#) must be either removed");
 		System.out.println("  or replaced by an integer specifying the minimum allowed width of");
 		System.out.println("  the entire ballon. You may add ,# to the end of the tag (before");
 		System.out.println("  the second $) where # is an integer specifying the minimum allowed");
 		System.out.println("  height of the baloon, this option is independent of the width");
 		System.out.println("  option, and may create additional line in the end of the output,");
 		System.out.println("  the baloon will then be padded into place with blank spaces.");
 		System.out.println("  You may also create your own tags, this is done by adding,");
 		System.out.println("  anywhere before used, a $name=text$ tag, the name of the tag");
 		System.out.println("  is not allowed to be / or \\, start with ballon or include =.");
 	    }
 		
 		
 	    System.out.println("\n\n");
 	    System.out.println("Copyright (C) 2012  Mattias Andrée <maandree@kth.se>");
 	    System.out.println();
 	    System.out.println("This program is free software: you can redistribute it and/or modify");
 	    System.out.println("it under the terms of the GNU General Public License as published by");
 	    System.out.println("the Free Software Foundation, either version 3 of the License, or");
 	    System.out.println("(at your option) any later version.");
 	    System.out.println();
 	    System.out.println("This program is distributed in the hope that it will be useful,");
 	    System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
 	    System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
 	    System.out.println("GNU General Public License for more details.");
 	    System.out.println();
 	    System.out.println("You should have received a copy of the GNU General Public License");
 	    System.out.println("along with this library.  If not, see <http://www.gnu.org/licenses/>.");
 	    System.out.println();
 	    System.out.println();
 	    System.out.println("Neither cowsay or ponysay nor way other program was cannibalised,");
 	    System.out.println("but I did cannibalise ponies and eat some delicous pancake\"let\"s.");
 	    System.out.println();
 	    System.out.println();
 		
 	    return;
 	}
 	
 	final int width = notrunc ? -1 : Util.getWidth();
 	if (width > 15) //sanity
 	{
 	    final OutputStream stdout = new BufferedOutputStream(System.out);
 	    final OutputStream out = new OutputStream()
 		    {
 			/**
 			 * The number of column on the current line
 			 */
 			private int x = 0;
 			
 			/**
 			 * Escape sequence state
 			 */
 			private int esc = 0;
 			
 			/**
 			 * Last bytes as written
 			 */
 			private boolean ok = true;
 			
 			
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public void write(final int b) throws IOException
 			{
 			    if (this.esc == 0)
 			    {
 				if (b == '\n')
 				{
 				    if (x >= width)
 				    {
 					write('\033');
 					write('[');
 					write('4');
 					write('9');
 					write('m');
 				    }
 				    this.x = -1;
 				}
 				else if (b == '\t')
 				{
 				    int nx = 8 - (x & 7);
 				    for (int i = 0; i < nx; i++)
 					write(' ');
 				    return; //(!)
 				}
 				else if (b == '\033')
 				    this.esc = 1;
 			    }
 			    else if (this.esc == 1)
 			    {
 				if      (b == '[')  this.esc = 2;
 				else if (b == ']')  this.esc = 3;
 				else                this.esc = 10;
 			    }
 			    else if (this.esc == 2)
 			    {
 				if ((('a' <= b) && (b <= 'z')) || (('A' <= b) && (b <= 'Z')))
 				    this.esc = 10;
 			    }
 			    else if ((this.esc == 3) && (b == 'P'))
 			    {
 				this.esc = ~0;
 			    }
 			    else if (this.esc < 0)
 			    {
 				this.esc--;
 				if (this.esc == ~7)
 				{
 				    flush();
 				    this.esc = 10;
 				}
 			    }
 			    else
 				this.esc = 10;
 			    
 			    if ((x < width) || (this.esc != 0) || (ok && ((b & 0xC0) == 0x80)))
 			    {
 				stdout.write(b);
 				if (this.esc == 0)
 				    if ((b & 0xC0) != 0x80)
 					x++;
 				ok = true;
 			    }
 			    else
 				ok = false;
 			    if (this.esc == 10)
 				this.esc = 0;
 			}
 			
 			/**
 			 * {@inheritDoc}
 			 */
 			@Override
 			public void flush() throws IOException
 			{
 			    stdout.flush();
 			}
 		    };
 	    
 	    System.setOut(new PrintStream(out));
 	}
 	
 	start(args);
 	System.out.flush();
     }
     
     
     /**
      * The is actually the main method of the program, {@link #main(String...)} just wraps this
      * methods and takes care of the <code>--help</code> argument.
      * 
      * @param  args  Startup arguments, start the program with </code>--help</code> for details
      * 
      * @throws  IOException  On I/O exception
      */
     private static void start(final String... args) throws IOException
     {
 	final boolean linuxvt = Util.getProperty("TERM").equals("linux");
 	
 	final String priv = "~/.local/share/unisay/".replace("~", Util.getProperty("HOME"));
 	final String publ = "/usr/share/unisay/";
 	
 	String nw =  "/-",  n = "-", ne = "-\\",
 	        w =  "|  ",           e = "  |",
 	       sw = "\\-",  s = "-", se = "-/", l = "\\", L = "/";
 	
 	nw += "\n| ";      ne += "\n |";
 	sw = "| \n" + sw;  se = " |\n" + se;
 	n += "\n ";        s = " \n" + s;
 	
 	final ArrayList<String> format = new ArrayList<String>();
 	final ArrayList<String> pony = new ArrayList<String>();
 	final ArrayList<String> say = new ArrayList<String>();
 	
 	boolean quote = false;
 	boolean random = false;
 	boolean dash = false;
 	boolean useCows = false;
 	boolean usePonies = false;
 	
 	for (int i = 0, m = args.length; i < m; i++)
         {
 	    final String arg = args[i];
 	    
 	    if (dash)                                                                                                      pony.add(arg);
 	    else if (Util.equalsAny(arg, "--"))                                                                            dash = true;
 	    else if (Util.equalsAny(arg, "--random", "-r"))                                   if (random)                  System.err.println("--random (-r) should only be used once.");
 		                                                                              else                         random = true;
 	    else if (Util.equalsAny(arg, "--format", "-p"))                                                                format.add(args[++i]);
 	    else if (Util.equalsAny(arg, "--say", "-s"))                                      if (quote)                   System.err.println("--pony-quotes and but be used togather with --say");
 		                                                                              else                         say.add(args[++i]);
 	    else if (Util.equalsAny(arg, "--pony-quotes", "--ponyquotes", "--quotes", "-q"))  if (say.isEmpty() == false)  System.err.println("--pony-quotes and but be used togather with --say");
 		                                                                              else                         quote = true;
 	    else if (Util.equalsAny(arg, "--cows", "-C"))                                                                  useCows = true;
 	    else if (Util.equalsAny(arg, "--ponies", "-P"))                                                                usePonies = true;
 	    else if (Util.equalsAny(arg, "--all", "-A"))                                                                   usePonies = useCows = true;
 	    else if (arg.startsWith("-"))
 	    {
 		if (false == Util.equalsAny(arg, "--no-truncate", "--notruncate", "--notrunc", "-T"))
 		{
 		    System.err.println("Unrecognised option, assuming it is a pony file: " + arg);
 		    pony.add(arg);
 		}
 	    }
 	    else
 		pony.add(arg);
 	}
 	
 	if ((useCows || usePonies) == false)
 	    useCows = usePonies = true;
 	
 	String oneSay;
 	String onePony;
 	String oneFormat;
 	InputStream sayfeed = System.in;
 	
 	if (say.isEmpty() == false)
 	    oneSay = say.get((int)(Math.random() * say.size()));
 	else
 	    oneSay = null; //we will catch it later
 	
 	final String privateDir    = priv + (linuxvt ? "tty" : "") + "pony/";
 	final String  publicDir    = publ + (linuxvt ? "tty" : "") + "pony/";
 	final String privateCowDir = priv + "cow/";
 	final String  publicCowDir = publ + "cow/";
 	
 	if (quote)
 	{
 	    final String privateQuotesDir = priv + "ponyquotes/";
 	    final String  publicQuotesDir = publ + "ponyquotes/";
 	    
 	    final HashMap<String, String> ponymap = new HashMap<String, String>();
 	    final HashSet<String> qset = new HashSet<String>();
 	    final HashSet<String> pset = new HashSet<String>();
 	    
 	    for (final String p : pony)
 		if (p.contains("/"))
 		    pset.add(p.substring(p.lastIndexOf('/') + 1));
 		else
 		    pset.add(p);
 	    
 	    for (final String dir : new String[] {publicQuotesDir, privateQuotesDir, })
 		if ((new File(dir)).exists())
 		    for (final String file : (new File(dir)).list())
 			if (file.endsWith("~") == false)
 			{
 			    final String[] ponies = file.substring(0, file.lastIndexOf('.')).split("\\+");
 			    for (final String p : ponies)
 				qset.add(p);
 			}
 	    
 	    if (usePonies)
 		for (final String dir : new String[] {publicDir, privateDir, })
 		    if ((new File(dir)).exists())
 			for (final String file : (new File(dir)).list())
 			    if ((file.equals("default") == false) && (qset.contains(file)))
 				if (pony.isEmpty() || (pset.contains(file)))
 				    ponymap.put(file, dir + file);
 	    
 	    if (useCows)
 		for (final String dir : new String[] {publicCowDir, privateCowDir, })
 		    if ((new File(dir)).exists())
 			for (final String file : (new File(dir)).list())
 			    if ((file.equals("default") == false) && (qset.contains(file)))
 				if (pony.isEmpty() || (pset.contains(file)))
 				    ponymap.put(file, dir + file);
 	    
 	    final ArrayList<ArrayList<String>> qlist = new ArrayList<ArrayList<String>>();
 	    
 	    for (final String dir : new String[] {publicQuotesDir, privateQuotesDir, })
 		if ((new File(dir)).exists())
 		    for (final String file : (new File(dir)).list())
 			if (file.endsWith("~") == false)
 			{
 			    final ArrayList<String> values = new ArrayList<String>();
 			    values.add(dir + file);
 			    final String[] ponies = file.substring(0, file.lastIndexOf('.')).split("\\+");
 			    for (final String p : ponies)
 				if (ponymap.containsKey(p))
 				    values.add(p);
 			    if (values.size() > 1)
 				qlist.add(values);
 			}
 	    
 	    final ArrayList<String> q = qlist.get(((int)(Math.random() * qlist.size())) % qlist.size());
 	    final String qp = q.get(1 + ((int)(Math.random() * (q.size() - 1))) % (q.size() - 1));
 	    final String qq = q.get(0);
 	    
 	    onePony = ponymap.get(qp);
 	    oneSay = null;
 	    sayfeed = new BufferedInputStream(new FileInputStream(new File(qq)));
 	}
 	else if (pony.isEmpty() == false)
 	{
 	    onePony = pony.get(((int)(Math.random() * pony.size())) % pony.size());
 	    
 	    if ((new File(onePony)).exists() == false)
 	    {
 		final String privatePony    = privateDir    + onePony;
 		final String  publicPony    =  publicDir    + onePony;
 		final String privateCowPony = privateCowDir + onePony;
 		final String  publicCowPony =  publicCowDir + onePony;
 		
 		if      (usePonies && (new File(privatePony   )).exists())  onePony = privatePony;
 		else if (usePonies && (new File( publicPony   )).exists())  onePony =  publicPony;
 		else if (useCows   && (new File(privateCowPony)).exists())  onePony = privateCowPony;
 		else if (useCows   && (new File( publicCowPony)).exists())  onePony =  publicCowPony;
 	    }
 	}
 	else
 	{
 	    final String privateDefault    = privateDir    + "default";
 	    final String  publicDefault    =  publicDir    + "default";
 	    final String privateCowDefault = privateCowDir + "default";
 	    final String  publicCowDefault =  publicCowDir + "default";
 	    
 	    if      (usePonies && (new File(privateDefault   )).exists() && !random)  onePony = privateDefault;
 	    else if (usePonies && (new File( publicDefault   )).exists() && !random)  onePony =  publicDefault;
 	    else if (useCows   && (new File(privateCowDefault)).exists() && !random)  onePony = privateCowDefault;
 	    else if (useCows   && (new File( publicCowDefault)).exists() && !random)  onePony =  publicCowDefault;
 	    else
 	    {
 		pony.clear();
 		
 		if (usePonies)
 		    for (final String dir : new String[] {privateDir, publicDir, })
 			if ((new File(dir)).exists())
 			    for (final String file : (new File(dir)).list())
 				if (file.equals("default") == false)
 				    pony.add(dir + file);
 		
 		if (useCows)
 		    for (final String dir : new String[] {privateCowDir, publicCowDir, })
 			if ((new File(dir)).exists())
 			    for (final String file : (new File(dir)).list())
 				if (file.equals("default") == false)
 				    pony.add(dir + file);
 		
 		if (pony.isEmpty())
 		    onePony = null;
 		else
 		    onePony = pony.get(((int)(Math.random() * pony.size())) % pony.size());
 	    }
 	}
 	
 	if (format.isEmpty() == false)
 	    oneFormat = format.get(((int)(Math.random() * format.size())) % format.size());
 	else
 	    oneFormat = null;
 	
 	if (onePony == null)
 	{
 	    System.err.println("No pony file was specified.");
 	    return;
 	}
 	if ((new File(onePony)).exists() == false)
 	{
 	    System.err.println("The selected (or choosen) pony file does not exist.");
 	    return;
 	}
 	
 	if (oneFormat != null)
 	    if ((new File(oneFormat)).exists() == false)
 	    {
 		final String privateFormatDir = priv + "format/";
 		final String  publicFormatDir = publ + "format/";
 		
 		final String privateFormat = privateFormatDir + oneFormat;
 		final String  publicFormat =  publicFormatDir + oneFormat;
 		
 		if      ((new File(privateFormat)).exists())  oneFormat = privateFormat;
 		else if ((new File( publicFormat)).exists())  oneFormat =  publicFormat;
 		else
 		{
 		    System.err.println("The selected (or choosen) format file does not exist.");
 		    oneFormat = null;
 		}
 	    }
 	
 	final int[][] mne, me, mse, ms, msw, mw, mnw, mn, ml, mL;
 	if (oneFormat == null)
 	{
 	    final String[] nes, ns, nws, sws, ss, ses;
 	    mne = new int[(nes = ne.split("\n")).length][];
 	    mn  = new int[(ns  = n .split("\n")).length][];
 	    mnw = new int[(nws = nw.split("\n")).length][];
 	    msw = new int[(sws = sw.split("\n")).length][];
 	    ms  = new int[(ss  = s .split("\n")).length][];
 	    mse = new int[(ses = se.split("\n")).length][];
 	    me = new int[1][e.length()];
 	    mw = new int[1][w.length()];
 	    ml = new int[1][l.length()];
 	    mL = new int[1][L.length()];
 	    for (int i = 0, m = e.length(); i < m; i++)  me[0][i] = (int)(e.charAt(i));
 	    for (int i = 0, m = w.length(); i < m; i++)  mw[0][i] = (int)(w.charAt(i));
 	    for (int i = 0, m = l.length(); i < m; i++)  ml[0][i] = (int)(l.charAt(i));
 	    for (int i = 0, m = L.length(); i < m; i++)  mL[0][i] = (int)(L.charAt(i));
 	    String t;
 	    for (int i = 0, mi = nes.length; i < mi; i++)    for (int j = 0, mj = (mne[i] = new int[(t = nes[i]).length()]).length; j < mj; j++)    mne[i][j] = (int)(t.charAt(j));
 	    for (int i = 0, mi =  ns.length; i < mi; i++)    for (int j = 0, mj = (mn [i] = new int[(t = ns [i]).length()]).length; j < mj; j++)    mn [i][j] = (int)(t.charAt(j));
 	    for (int i = 0, mi = nws.length; i < mi; i++)    for (int j = 0, mj = (mnw[i] = new int[(t = nws[i]).length()]).length; j < mj; j++)    mnw[i][j] = (int)(t.charAt(j));
 	    for (int i = 0, mi = sws.length; i < mi; i++)    for (int j = 0, mj = (msw[i] = new int[(t = sws[i]).length()]).length; j < mj; j++)    msw[i][j] = (int)(t.charAt(j));
 	    for (int i = 0, mi =  ss.length; i < mi; i++)    for (int j = 0, mj = (ms [i] = new int[(t = ss [i]).length()]).length; j < mj; j++)    ms [i][j] = (int)(t.charAt(j));
 	    for (int i = 0, mi = ses.length; i < mi; i++)    for (int j = 0, mj = (mse[i] = new int[(t = ses[i]).length()]).length; j < mj; j++)    mse[i][j] = (int)(t.charAt(j));
 	}
 	else
 	{
 	    final InputStream is = new BufferedInputStream(new FileInputStream(new File(oneFormat)));
 	    
 	    final int SIZE = 32;
 	    final ArrayList<int[]> front = new ArrayList<int[]>(); //Using List instead of Deque to make it work with Java 5.
 	    int[] buf = new int[SIZE];
 	    int ptr = 0;
 	    int state = 0;
 	    String dir = null;
 	    
 	    final HashMap<String, ArrayList<int[]>> map = new HashMap<String, ArrayList<int[]>>();
 	    map.put("nw:", new ArrayList<int[]>());
 	    map.put("n:",  new ArrayList<int[]>());
 	    map.put("ne:", new ArrayList<int[]>());
 	    map.put( "e:", new ArrayList<int[]>());
 	    map.put("se:", new ArrayList<int[]>());
 	    map.put("s:",  new ArrayList<int[]>());
 	    map.put("sw:", new ArrayList<int[]>());
 	    map.put( "w:", new ArrayList<int[]>());
 	    map.put("\\:", new ArrayList<int[]>());
 	    map.put("/:",  new ArrayList<int[]>());
 	    
 	    for (int d; (d = is.read()) != -1;)
 	    {
 		if ((d & 128) == 128)
 		{
 		    int dn = 0;
 		    while ((d & 128) == 128)
 		    {
 			dn++;
 			d <<= 1;
 		    }
 		    if (dn == 1)
 			continue;
 		    d &= 0xFF;
 		    d >>>= dn;
 		    for (int i = 1; i < dn; i++)
 		    {
 			int pd = is.read();
 			if (pd == -1)
 			    break;
 			d <<= 6;
 			d |= pd & 0x3F;
 		    }
 		}
 		
 		if ((state != 1) || (d != (int)'\n'))
 		    buf[ptr++] = d;
 		if (ptr == SIZE)
 		{
 		    front.add(buf);
 		    buf = new int[SIZE];
 		    ptr = 0;
 		}
 		
 		if ((state == 0) && (d == (int)':'))
 		{
 		    state = 1;
 		    front.add(buf);
 		    buf = front.get(0);
 		    front.clear();
 		    final char[] cbuf = new char[ptr];
 		    for (int i = 0; i < ptr; i++)
 			cbuf[i] = (char)(buf[i]);
 		    String tmp = new String(cbuf);
 		    if (Util.equalsAny(tmp, "nw:", "n:", "ne:", "e:", "se:", "s:", "sw:", "w:", "\\:", "/:"))
 			dir = tmp;
 		    else if (tmp.equals(":") == false)
 		    {
 			dir = null;
 			System.err.println("Unrecognised format part: " + tmp.substring(0, tmp.length() - 1));
 		    }
 		    ptr = 0;
 		}
 		else if ((state == 1) && (d == (int)'\n'))
 		{
 		    state = 0;
 		    final int[] line = new int[front.size() * SIZE + ptr];
 		    for (int i = 0, m = front.size(); i < m; i++)
 			System.arraycopy(front.get(i), 0, line, i * SIZE, SIZE);
 		    front.clear();
 		    System.arraycopy(buf, 0, line, front.size() * SIZE, ptr);
 		    ptr = 0;
 		    map.get(dir).add(line);
 		}
 	    }
 	    
 	    is.close();
 	    
 	    
 	    mne = new int[map.get("ne:").size()][];
 	    mn  = new int[map.get( "n:").size()][];
 	    mnw = new int[map.get("nw:").size()][];
 	    msw = new int[map.get("sw:").size()][];
 	    ms  = new int[map.get( "s:").size()][];
 	    mse = new int[map.get("se:").size()][];
 	    me = new int[1][];
 	    mw = new int[1][];
 	    ml = new int[1][];
 	    mL = new int[1][];
 	    
 	    map.get("nw:").toArray(mnw);  map.get("n:").toArray(mn);  map.get("ne:").toArray(mne);
 	    map.get( "w:").toArray(mw);                               map.get( "e:").toArray(me);
 	    map.get("sw:").toArray(msw);  map.get("s:").toArray(ms);  map.get("se:").toArray(mse);
 	    map.get("\\:").toArray(ml);   map.get("/:").toArray(mL);
 	}
 	
 	final ArrayList<int[]> lens = new ArrayList<int[]>();
 	final ArrayList<ArrayList<byte[]>> lines = new ArrayList<ArrayList<byte[]>>();
 	if (oneSay != null)
 	{
 	    final byte[] expand = new byte[8];
 	    for (int i = 0; i < 8; i++)
 		expand[i] = ' ';
 	    
 	    boolean esc = false;
 	    
 	    for (final String line : oneSay.split("\n"))
 	    {
 		int len = 0;
 		final byte[] bs = line.getBytes("UTF-8");
 		final ArrayList<byte[]> list = new ArrayList<byte[]>();
 		final int[] tabs = new int[bs.length];
 		int tabptr = 0;
 		
 		for (int i = 0, nn = bs.length; i < nn; i++)
 		    if (bs[i] == '\t')
 			tabs[tabptr++] = i;
 		
 		int start = 0;
 		for (int i = 0, nn = tabptr; i < nn; i++)
 		{
 		    final int end = tabs[i], m;
 		    final byte[] part = new byte[m = end - start];
 		    System.arraycopy(bs, start, part, 0, m);
 		    list.add(part);
 		    final byte[] exp = new byte[8 - (m & 7)]; //not associative!
 		    System.arraycopy(expand, 0, exp, 0, exp.length);
 		    list.add(exp);
 		    start = end + 1;
 		    len += (m | 7) + 1;
 		}
 		final int end = bs.length, m;
 		len += m = end - start;
 		final byte[] part = new byte[m];
 		System.arraycopy(bs, start, part, 0, m);
 		list.add(part);
 		
 		for (final byte[] chunk : list)
 		    for (final byte b : chunk)
 			if (b == 033)
 			{
 			    esc = true;
 			    len--;
 			}
 		        else if (b == 8)
 			    len -= 2; //damn fortune cookies!
 			else if (esc)
 			{
 			    len--;
 			    esc = (b != (byte)'m');
 			}
 			else if ((b & 0xC0) == 0x80)
 			    len--;
 		
 		lines.add(list);
 		lens.add(new int[] { len });
 	    }
 	}
 	else
 	{
 	    ArrayList<byte[]> list = new ArrayList<byte[]>();
 	    final int SIZE = 64;
 	    byte[] buf = new byte[SIZE];
 	    int ptr = 0;
 	    int len = 0;
 	    
 	    boolean esc = false;
 	    for (int d; (d = sayfeed.read()) != -1;)
 		if (d == '\n')
 		{
 		    if (ptr > 0)
 		    {
 			byte[] app = new byte[ptr];
 			System.arraycopy(buf, 0, app, 0, ptr);
 			list.add(app);
 		    }
 		    lines.add(list);
 		    list = new ArrayList<byte[]>();
 		    lens.add(new int[] { len });
 		    ptr = 0;
 		    len = 0;
 		}
 		else if (d == '\t')
 		{
 		    final int exp = 8 - (len & 7);
 		    len += exp;
 		    for (int i = 0; i < exp; i++)
 		    {
 			buf[ptr++] = (byte)' ';
 			if (ptr == SIZE)
 			{
 			    list.add(buf);
 			    buf = new byte[SIZE];
 			    ptr = 0;
 			}
 		    }
 		}
 		else
 		{
 		    if ((d & 0xC0) != 0x80)
 			if (d == '\033')
 			    esc = true;
 			else if (!esc)
 			    if (d == 8)
 				len--; //damn fortune cookies!
 			    else
 				len++;
 			else if (d == 'm')
 			    esc = false;
 		    buf[ptr++] = (byte)d;
 		    if (ptr == SIZE)
 		    {
 			list.add(buf);
 			buf = new byte[SIZE];
 			ptr = 0;
 		    }
 		}
 	    
 	    if (sayfeed != System.in)
 		sayfeed.close();
 	    
 	    if (ptr > 0)
 	    {
 		byte[] app = new byte[ptr];
 		System.arraycopy(buf, 0, app, 0, ptr);
 		list.add(app);
 	    }
 	    lens.add(new int[] { len });
 	    lines.add(list);
 	}
 	
 	while ((lines.size() > 1) && (lines.get(lines.size() - 1).isEmpty()))
 	{
 	    lines.remove(lines.size() - 1);
 	    lens.remove(lens.size() - 1);
 	}
 	
 	/*
 	for (int i = 0, m = lines.size(); i < m; i++)
 	{
 	    int dn = 0;
 	    int d = 0;
 	    for (final byte[] chunk : lines.get(i))
 		 for (final byte b : chunk)
 		 {
 		     if ((b & 0xC0) == 0x80)
 		     {
 			 d <<= 6;
 			 d |= b & 0x3F;
 			 dn--;
 		     }
 		     else
 		     {
 			 d = b;
 			 while ((d & 128) == 128)
 			 {
 			     dn++;
 			     d <<= 1;
 			 }
 			 d &= 255;
 			 d >>= dn;
 			 dn--;
 		     }
 		     if (dn == 0)
 		     {
 			 if ((d & 0xDC00) == 0xDC00) //UTF-16 encoded character in UTF-8
 			     lens.get(i)[0]--;
 		     }
 		 }
 	}
 	*/
 	
 	int maxlen = 0;
 	for (final int[] len : lens)
 	    if (maxlen < len[0])
 		maxlen = len[0];
 	
 	final Baloon baloon = new Baloon(lens, maxlen, lines, mnw, mn, mne, me[0], mse, ms, msw, mw[0]);
 	say(onePony, baloon, ml[0], mL[0]);
     }
     
     
     /**
      * Performs the speaking!
      *
      * @param  ponyFile  The image file
      * @parma  baloon    The baloon object
      * @param  l         \ directional link symbol
      * @param  L         / directional link symbol
      * 
      * @throws  IOException  On I/O exception
      */
     private static void say(final String ponyFile, final Baloon baloon, final int[] l, final int[] L) throws IOException
     {
 	final HashMap<String, byte[]> variables = new HashMap<String, byte[]>();
 	final InputStream is = new BufferedInputStream(new FileInputStream(new File(ponyFile)));
 	variables.put("\\", Util.toBytes(l));
 	variables.put("/", Util.toBytes(L));
 	variables.put("", Util.toBytes((int)'$'));
 	int indent = 0;
 	
 	boolean dollar = false;
 	int eq = 0, ptr = 0;
 	byte[] buf = new byte[16];
 	
 	for (int d; (d = is.read()) != -1;)
 	    if (d == '$')
 	    {
 		if ((dollar ^= true) == false)
 		{
 		    String var = new String(buf, 0, eq == 0 ? ptr : eq, "UTF-8");
 		    if (eq != 0)
 		    {
 			final byte[] val = new byte[ptr - eq - 1];
 			System.arraycopy(val, 0, buf, eq + 1, val.length);
 			variables.put(var, val);
 		    }
 		    else if (var.startsWith("baloon"))
 		    {
 			int w = 0;
 			int h = 0;
 			final String props = var.substring("baloon".length());
 			if (props.isEmpty() == false)
 			    if (props.contains(","))
 			    {
 				if (props.startsWith(",") == false)
 				    w = Integer.parseInt(props.substring(0, props.indexOf(",")));
 				h = Integer.parseInt(props.substring(1 + props.indexOf(",")));
 			    }
 			    else
 				w = Integer.parseInt(props);
 			
 			baloon.print(w, h, indent);
 			
 			indent = 0;
 		    }
 		    else
 			System.out.write(variables.get(var));
 		    eq = 0;
 		    ptr = 0;
 		}
 	    }
 	    else if (dollar)
 	    {
 		if ((buf[ptr++] = (byte)d) == (byte)'=')
 		    if (eq == 0)
 			eq = ptr - 1;
 		if (ptr == buf.length)
 		{
 		    final byte[] nbuf = new byte[ptr + 16];
 		    System.arraycopy(nbuf, 0, buf, 0, ptr);
 		    buf = nbuf;
 		}
 	    }
 	    else if (d == '\033')
 	    {
 		System.out.write(d);
 		if ((d = is.read()) == -1)
 		    break;
 		System.out.write(d);
 		
 		if (d == 'c')
 		    indent = 0;
 		else if (d == ']')
 		{
 		    if ((d = is.read()) == -1)
 			break;
 		    System.out.write(d);
 		    if (d == 'P')
 			for (int di = 0; (di < 7) && ((d = is.read()) != -1); di++)
 			    System.out.write(d);
 		}
 		else if (d == '[')
 		    while ((d = is.read()) != -1)
 		    {
 			System.out.write(d);
 			if ((d == '~') || (('a' <= d) && (d <= 'z')) || (('A' <= d) && (d <= 'Z')))
 			    break;
 		    }
 	    }
 	    else
 	    {
 		System.out.write(d);
 		indent++;
 		if (d == '\n')
 		    indent = 0;
 	    }
 	
 	is.close();
     }    
     
 }
