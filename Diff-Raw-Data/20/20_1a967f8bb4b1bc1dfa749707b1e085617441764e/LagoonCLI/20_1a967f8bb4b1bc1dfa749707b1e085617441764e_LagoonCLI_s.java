 /*
 * Copyright (c) 2001, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lagoon;
 
 import java.io.*;
 import java.util.Properties;
 
 import org.xml.sax.SAXException;
 
 import nu.staldal.xtree.*;
 
 import nu.staldal.lagoon.core.*;
 
 
 /**
  * A command line interface to LagoonProcessor
  *
  * @see nu.staldal.lagoon.core.LagoonProcessor
  */
 public class LagoonCLI
 {
 	private static boolean DEBUG = false;	
 	
     private static final String syntaxMsg =
         "Syntax:\n"
 	  + "nu.staldal.lagoon.LagoonCLI <property_file> [<interval>|build|force]\n"
 	  + "nu.staldal.lagoon.LagoonCLI <sitemap_file> [<interval>|build|force]";
   
     private static Properties properties;
 
 
     /**
      * The application main method
      */
 	public static void main(String[] args)
 	{
         boolean force = false;
         boolean build = false;
         long interval = 0;
 
         if (args.length < 1)
         {
             System.out.println(syntaxMsg);
             return;
         }
         else if ((args.length > 1) && args[1].equals("build"))
             build = true;
         else if ((args.length > 1) && args[1].equals("force"))
             force = true;
         else if (args.length > 1)
         {
             try {
                 interval = 1000*Integer.parseInt(args[1]);
             }
             catch (NumberFormatException e)
             {
                 System.out.println(syntaxMsg);
                 return;
             }
             if (interval < 1)
             {
                 System.out.println(syntaxMsg);
                 return;
             }
         }
 
         LagoonProcessor processor;
         try {
             System.out.println("Initializing Lagoon...");
 			
 			String targetURL;
 			File sitemapFile;
 			File sourceDir;
 			String password;
 			
 			if (args[0].endsWith(".xml") || args[0].endsWith(".sitemap"))
 			{
 				targetURL = System.getProperty("user.dir");
 				sourceDir = new File(targetURL);
 				sitemapFile = new File(args[0]);
 				password = null;
 			}
 			else
 			{
 				File propertyFile = new File(args[0]);
 			
 				properties = new Properties();
 				FileInputStream fis = new FileInputStream(propertyFile);
 				properties.load(fis);
 				fis.close();
 	
 				targetURL = getProperty("targetURL");
 				sitemapFile = new File(getProperty("sitemapFile"));
 				sourceDir = new File(getProperty("sourceDir"));			
                 password = properties.getProperty("password");
 			}
 			
 			Element sitemapTree;
 			try {
 				sitemapTree = TreeBuilder.parseXML(
 					TreeBuilder.fileToInputSource(sitemapFile), false);
 			}
 			catch (SAXException e)
 			{
 				Exception ee = e.getException();
 				if (ee == null)
 				{
 					e.printStackTrace();
 					throw new LagoonException(e.getMessage());
 				}
 				else if (ee instanceof java.io.IOException)
 				{
 					throw (java.io.IOException)ee;
 				}
 				else
 				{
 					ee.printStackTrace();
 					throw new LagoonException(ee.getMessage());
 				}
 			}				
 
             processor = 
 				new LagoonProcessor(
 					targetURL, sitemapTree, sourceDir, password, 
 					new PrintWriter(System.out, true), 
 					new PrintWriter(System.err, true));
         }
         catch (AuthenticationMissingException e)
         {
             System.err.println("Error while initializing Lagoon:");
             System.err.println("Password is required but not specified");
             return;
         }
         catch (AuthenticationException e)
         {
             System.err.println("Error while initializing Lagoon:");
             System.err.println("Incorrect password");
             return;
         }
         catch (FileNotFoundException e)
         {
             System.err.println("Error while initializing Lagoon:");
             System.err.println("File not found: " + e.getMessage());
             return;
         }
         catch (IOException e)
         {
             System.err.println("Error while initializing Lagoon:");
            System.err.println("I/O error: " + e.getMessage());
             return;
         }
         catch (LagoonException e)
         {
             System.err.println("Error while initializing Lagoon:");
             System.err.println(e.getMessage());
             return;
         }
 					
         System.out.println("Lagoon initialized successfully");
 
         try {
             if (build)
             {
                 System.out.println("Building website...");
                 long timeBefore = System.currentTimeMillis();
                 if (!processor.build(false))
 					System.out.println("...with errors...");
                 long timeElapsed = System.currentTimeMillis()-timeBefore;
                 showTime(timeElapsed);
             }
             else if (force)
             {
                 System.out.println("Force building website...");
                 long timeBefore = System.currentTimeMillis();
                 if (!processor.build(true))
 					System.out.println("...with errors...");
                 long timeElapsed = System.currentTimeMillis()-timeBefore;
                 showTime(timeElapsed);
             }
             else if (interval > 0)
             {
                 while (true)
                 {
                     System.out.println("Building website...");
                     long timeBefore = System.currentTimeMillis();
     	            if (!processor.build(false))
 						System.out.println("...with errors...");
                     long timeElapsed = System.currentTimeMillis()-timeBefore;
 	                showTime(timeElapsed);
                     if (timeElapsed < interval)
                     try { Thread.sleep(interval-timeElapsed); } catch (InterruptedException e) {}
                 }
             }
             else
             {
                 BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                 while (true)
                 {
                     System.out.println("Ready");
                     String yn = in.readLine();
 					if (yn.length() < 1) break;
                     if (yn.charAt(0) == 'b')
                     {
                         System.out.println("Building website...");
                         long timeBefore = System.currentTimeMillis();
 	    	            if (!processor.build(false))
 							System.out.println("...with errors...");
                         long timeElapsed = System.currentTimeMillis()-timeBefore;
 	                	showTime(timeElapsed);
                     }
                     else if (yn.charAt(0) == 'f')
                     {
                         System.out.println("Force building website...");
                         long timeBefore = System.currentTimeMillis();
 	    	            if (!processor.build(true))
 							System.out.println("...with errors...");
                         long timeElapsed = System.currentTimeMillis()-timeBefore;
 	                	showTime(timeElapsed);
                     }
                     else break;
                 }
             }
 
             System.out.println("Exiting Lagoon...");
             processor.destroy();
 			System.exit(0);
         }
         catch (FileNotFoundException e)
         {
             System.err.println("File not found: " + e.getMessage());
         }
         catch (IOException e)
         {
 			if (DEBUG) e.printStackTrace();
            System.err.println("I/O error: " + e.getMessage());
         }
 	}
 
     private static String getProperty(String name)
         throws LagoonException
     {
         String value = properties.getProperty(name);
         if (value == null)
             throw new LagoonException("Property " + name + " not specified");
 
         return value.trim();
     }
 
 	private static void showTime(long ms)
 	{
 		if (ms < 10000)
 			System.out.println("in " + ms + " ms");
 		else
 			System.out.println("in " + ms/1000 + " s");
 	}
 }
 
