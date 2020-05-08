 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2005  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.cli;
 
 import net.sf.clirr.core.Checker;
 import net.sf.clirr.core.CheckerException;
 import net.sf.clirr.core.ClassSelector;
 import net.sf.clirr.core.PlainDiffListener;
 import net.sf.clirr.core.XmlDiffListener;
 import net.sf.clirr.core.DiffListener;
 import net.sf.clirr.core.internal.asm.AsmTypeArrayBuilder;
 import net.sf.clirr.core.spi.JavaType;
 import net.sf.clirr.core.spi.Scope;
 import net.sf.clirr.core.spi.TypeArrayBuilder;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 
 /**
  * Commandline interface for generating a difference report or checking
  * for binary compatibility between two versions of the same application.
  */
 
 public class Clirr
 {
 
     public static void main(String[] args)
     {
         new Clirr().run(args);
     }
 
     // ===================================================================
 
     private void run(String[] args)
     {
         Options options = defineOptions();
 
         CommandLine cmdline = parseCommandLine(args, options);
 
         String oldPath = cmdline.getOptionValue('o');
         String newPath = cmdline.getOptionValue('n');
         String oldClassPath = cmdline.getOptionValue("ocp");
         String newClassPath = cmdline.getOptionValue("ncp");
         String style = cmdline.getOptionValue('s', "text");
         String outputFileName = cmdline.getOptionValue('f');
         String[] includePkgs = cmdline.getOptionValues('i');
         boolean showAll = cmdline.hasOption('a');
         boolean showPkg = cmdline.hasOption('p');
 
         if ((oldPath == null) || (newPath == null))
         {
             usage(options);
             System.exit(-1);
         }
 
         Checker checker = new Checker();
         if (showAll)
         {
             checker.getScopeSelector().setScope(Scope.PRIVATE);
         }
         else if (showPkg)
         {
             checker.getScopeSelector().setScope(Scope.PACKAGE);
         }
 
         ClassSelector classSelector;
         if ((includePkgs != null) && (includePkgs.length > 0))
         {
             classSelector = new ClassSelector(ClassSelector.MODE_IF);
             for (int i = 0; i < includePkgs.length; ++i)
             {
                 classSelector.addPackageTree(includePkgs[i]);
             }
         }
         else
         {
             // a selector that selects everything
             classSelector = new ClassSelector(ClassSelector.MODE_UNLESS);
         }
 
         DiffListener diffListener = null;
         if (style.equals("text"))
         {
             try
             {
                 diffListener = new PlainDiffListener(outputFileName);
             }
             catch (IOException ex)
             {
                 System.err.println("Invalid output file name.");
             }
         }
         else if (style.equals("xml"))
         {
             try
             {
                 diffListener = new XmlDiffListener(outputFileName);
             }
             catch (IOException ex)
             {
                 System.err.println("Invalid output file name.");
             }
         }
         else
         {
             System.err.println("Invalid style option. Must be one of 'text', 'xml'.");
             usage(options);
             System.exit(-1);
         }
 
 
         File[] origJars = pathToFileArray(oldPath);
         File[] newJars = pathToFileArray(newPath);
 
         checker.addDiffListener(diffListener);
 
         try
         {
             ClassLoader loader1 = new URLClassLoader(convertFilesToURLs(pathToFileArray(oldClassPath)));
             ClassLoader loader2 = new URLClassLoader(convertFilesToURLs(pathToFileArray(newClassPath)));
 
             TypeArrayBuilder tab1 = new AsmTypeArrayBuilder();
             TypeArrayBuilder tab2 = new AsmTypeArrayBuilder();
 
             final JavaType[] origClasses =
                 tab1.createClassSet(origJars, loader1, classSelector);
             
             final JavaType[] newClasses =
                 tab2.createClassSet(newJars, loader2, classSelector);
             
             checker.reportDiffs(origClasses, newClasses);
             
             System.exit(0);
         }
         catch (CheckerException ex)
         {
             System.err.println("Unable to complete checks:" + ex.getMessage());
             System.exit(1);
         }
         catch (MalformedURLException ex)
         {
             System.err.println("Unable to create classloader for 3rd party classes:" + ex.getMessage());
             System.err.println("old classpath: " + oldClassPath);
             System.err.println("new classpath: " + newClassPath);
             System.exit(1);
         }
     }
 
     /**
      * @param args
      * @param parser
      * @param options
      * @return
      */
     private CommandLine parseCommandLine(String[] args, Options options) 
     {
         BasicParser parser = new BasicParser();
         CommandLine cmdline = null;
         try
         {
             cmdline = parser.parse(options, args);
         }
         catch (ParseException ex)
         {
             System.err.println("Invalid command line arguments.");
             usage(options);
             System.exit(-1);
         }
         return cmdline;
     }
 
     /**
      * @return
      */
     private Options defineOptions() {
         Options options = new Options();
         options.addOption("o", "old-version", true, "jar files of old version");
         options.addOption("n", "new-version", true, "jar files of new version");
         options.addOption("ocp", "orig-classpath", true, "3rd party classpath that is referenced by old-version");
         options.addOption("ncp", "new-classpath", true, "3rd party classpath that is referenced by new-version");
         options.addOption("s", "style", true, "output style [text|xml]");
         options.addOption("i", "include-pkg", true,
             "include only classes from this package and its subpackages");
         options.addOption("p", "show-pkg-scope", false,
             "show package scope classes");
         options.addOption("a", "show-all-scopes", false,
             "show private and package classes");
         options.addOption("f", "output-file", true, "output file name");
         return options;
     }
 
     private void usage(Options options)
     {
         HelpFormatter hf = new HelpFormatter();
         PrintWriter out = new PrintWriter(System.err);
         hf.printHelp(
             75,
             "java " + getClass().getName() + " -o path -n path [options]",
             null, options, null);
     }
 
     private File[] pathToFileArray(String path)
     {
         if (path == null)
         {
             return new File[0];            
         }
         
         ArrayList files = new ArrayList();
 
         int pos = 0;
         while (pos < path.length())
         {
            int colonPos = path.indexOf(File.pathSeparatorChar, pos);
             if (colonPos == -1)
             {
                 files.add(new File(path.substring(pos)));
                 break;
             }
 
             files.add(new File(path.substring(pos, colonPos)));
             pos = colonPos + 1;
         }
 
         return (File[]) files.toArray(new File[files.size()]);
     }
     
     private URL[] convertFilesToURLs(File[] files) throws MalformedURLException
     {
         URL[] ret = new URL[files.length];
         for (int i = 0; i < files.length; i++) {
             ret[i] = files[i].toURL();
         }
         return ret;
     }
 }
