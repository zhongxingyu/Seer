 /*
  * Copyright (c) 2000
  *      Jon Schewe.  All rights reserved
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
  */
 package net.mtu.eggplant.dbc;
 
 import antlr.Parser;
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 import antlr.TokenStreamSelector;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  * <p>Class that starts everything off.</p>
  *
  * <p><a name="commandline_doc"><h2>Commandline options</h2></a>
  * <ul>
  *   <li>-f, --force  force instrumentation, regardless of file modification time</li>
  *   <li>-d, --destination &lt;dir&gt; the destination directory (default: instrumented)</li>
  *   <li>-s, --sourceExtension &lt;ext&gt; the extension on the source files (default: java)</li>
  *   <li>-i, --instrumentedExtension &lt;ext&gt; the extension on the source files (default: java)</li>
  *   <li>--source &lt;release&gt; Provide source compatibility with specified release (just like javac</li>
  *   <li>--prettyOutput put in carriage returns in the generated code.  This makes the output easier to read, but screws up line numbers</li>
  *   <li>--disableExit Disable System.exit during instrumentation.  This is useful for use with ant when not forking.  The exit code can be retrieved with JonsAssert.getExitCode()
  *   <li>files all other arguments are taken to be files or directories to be parsed</li>
  * </ul></p>
  *
  * <p>Classes can also be instrumented by calling
  * {@link #instrument(Configuration, Collection) instrument} with a Configuration
  * object and a Collection of files.</p>
  *
 * @version $Revision: 1.14 $
  */
 public final class JonsAssert {
 
   private JonsAssert() {}
   
   private static final Log LOG = LogFactory.getLog(JonsAssert.class);
   
   /**
    * Parses the command line then calls {@link #instrument(Configuration, Collection) instrument}.
    *
    * @param args see <a href="#commandline_doc">commandline options</a>
    */
   public static void main(final String[] args) {
     final Configuration config = new Configuration();
 
     //Setup all of the options
     final Options options = new Options();
     options.addOption("f", "force", false, "force instrumentation");
     options.addOption("d", "destination", true, "<dir> the destination directory (default: instrumented)");
     options.addOption("s", "sourceExtension", true, "<ext> the extension of the source files (default: java)");
     options.addOption("i", "instrumentedExtension", true, "<ext> the extension used for the instrumented files (default: java)");
     options.addOption("source", "source", true, "<release> Provide source compatibility with specified release (just like javac)");
     options.addOption("debugLexer", false, "");
     options.addOption("debug", false, "");
     options.addOption("prettyOutput", "prettyOutput", false, "put in carriage returns in the generated code.  This makes the output easier to read, but screws up line numbers");
     options.addOption("disableExit", "disableExit", false, "Disable System.exit during instrumentation");
     options.addOption("v", "verbose", false, "Should we be verbose?");
 
     //list to hold files/directories to instrument
     final Collection files = new LinkedList();
 
     //parse options
     try {
       final CommandLineParser parser = new PosixParser();
       final CommandLine cmd = parser.parse(options, args);
 
       if(cmd.hasOption("f")) {
         LOG.debug("Overwriting all files");
         config.setIgnoreTimeStamp(true);
       }
       if(cmd.hasOption("d")) {
         LOG.debug("Setting destination directory to: " + cmd.getOptionValue("d"));
         config.setDestinationDirectory(cmd.getOptionValue("d"));
       }
       if(cmd.hasOption("s")) {
         config.setSourceExtension(cmd.getOptionValue("s"));
       }
       if(cmd.hasOption("i")) {
         config.setInstrumentedExtension(cmd.getOptionValue("i"));
       }
       if(cmd.hasOption("debugLexer")) {
         _debugLexer = true;
       }
       if(cmd.hasOption("debug")) {
         Logger.getRootLogger().setLevel(Level.DEBUG);
       }
       if(cmd.hasOption("source")) {
         final String sourceCompatibility = cmd.getOptionValue("source");
         if(Configuration.JAVA_1_4.getName().equals(sourceCompatibility)) {
           config.setSourceCompatibility(Configuration.JAVA_1_4);
         } else if(Configuration.JAVA_1_3.getName().equals(sourceCompatibility)) {
           config.setSourceCompatibility(Configuration.JAVA_1_3);
         } else {
           System.err.println("Invalid source release: " + sourceCompatibility);
           usage(options);
           return;
         }
       }
       if(cmd.hasOption("prettyOutput")) {
         LOG.debug("Pretty output turned on");
         config.setPrettyOutput(true);
       }
       if(cmd.hasOption("disableExit")) {
         _disableExit = true;
       }
       if(cmd.hasOption("v")) {
         config.setVerbose(true);
       }
       
       final Iterator iter = cmd.getArgList().iterator();
       while(iter.hasNext()) {
         final String obj = (String)iter.next();
         final File file = new File((String)obj);
         if(!file.exists() || !file.canRead()) {
           System.err.println("Invalid option: " + obj);
           usage(options);
           return;
         } else {
           files.add(file);
         }
       }
       
     } catch(final ParseException pe) {
      System.err.println(pe.getMessage());
       usage(options);
       _exitCode = 1;
       if(!_disableExit) {
         System.exit(1);
       } else {
         return;
       }
     }
 
     //Set the exit status based on errors
     _exitCode = instrument(config, files) ? 0 : 1;
     if(!_disableExit) {
       System.exit(_exitCode);
     } else {
       return;
     }
   }
 
   /**
    * @pre (options != null)
    */
   private static void usage(final Options options) {
     final HelpFormatter formatter = new HelpFormatter();
     formatter.printHelp("JonsAssert", options);
     
 //     final StringBuffer sb = new StringBuffer(256);
 //     sb.append("Usage: JonsAssert [options] files");
 //     sb.append(System.getProperty("line.separator"));
 
 //     final Iterator iter = options.getOptions().iterator();
 //     while(iter.hasNext()) {
 //       final Option option = (Option)iter.next();
 
 //       //only short options that are characters should be shown
 //       if(Character.isLetter(option.getOpt())) {
 //         sb.append('-');
 //         sb.append(option.getOpt());
 //         if(option.hasLongOpt()) {
 //           sb.append(", ");
 //         }
 //       } else {
 //         sb.append("    ");
 //       }
       
 //       if(option.hasLongOpt()) {
 //         sb.append("--");
 //         sb.append(option.getLongOpt());
 //       }
       
 //       sb.append("  ");
 //       if(option.getDescription() != null) {
 //         sb.append(option.getDescription());
 //       }
 //       sb.append(System.getProperty("line.separator"));
 //     }
 //     System.err.print(sb.toString());
   }
 
   /**
    * Entry point.  Starts up the parser and instruments all files.  This is
    * the method to call if one wants to invoke the application from another
    * Java program.
    *
    * @param config the {@link Configuration configuration} object
    * @param files Collection of {@link java.io.File files/directories} to
    * parse.  Directories are parsed recursively.
    * @return true if everything went ok, false otherwise
    *
    * @pre (config != null)
    * @pre (files != null && net.mtu.eggplant.util.CollectionUtils.checkInstanceOf(files, File.class))
    *
    */
   public static boolean instrument(final Configuration config,
                                    final Collection files) {
     _symtab = new Symtab(config);
 
     boolean success = true;
     // if we have at least one file to parse
     if(!files.isEmpty()) {
       if(getSymtab().getConfiguration().isVerbose()) {
         System.out.println("Instrumenting...");
       }
       // for each directory/file specified on the command line
       final Iterator iter = files.iterator();
       while(iter.hasNext()) {
         final File file = (File)iter.next();
         success &= doFile(file); // parse it
       }
     } else {
       if(getSymtab().getConfiguration().isVerbose()) {
         System.out.println("Parsing testcases...");
       }
       config.setIgnoreTimeStamp(true);
       //Test case
       success &= doFile(new File("net/mtu/eggplant/assert/testcases/")); // parse it
     }
 
     return success;
   }
   
   /**
    * This method decides what action to take based on the type of file we are
    * looking at.
    *
    * @param f the file/directory to parse, if a directory recursively look
    * through the directory for files that match the source extension and parse
    * them.
    * @return true for success
    */
   private static boolean doFile(final File f) {
     boolean success = true; //did the run pass?
     boolean writeFile = false; //do we need to write the file 
     if (f.isDirectory()) {
       // If this is a directory, walk each file/dir in that directory      
       final String[] files = f.list();
       for(int i=0; i < files.length; i++) {
         success &= doFile(new File(f, files[i]));
       }
     } else if(f.getName().endsWith("." + getSymtab().getConfiguration().getSourceExtension())) {
       // otherwise, this is a java file, parse it!
       if(getSymtab().getConfiguration().isVerbose()) {
         System.out.println(f.getName()); //let the user know where we are
       }
       
       if(getSymtab().startFile(f)) {
         try {
           parseFile(new FileInputStream(f));
           success = true;
           writeFile = true;
         } catch(final IOException ioe) {
           if(LOG.isDebugEnabled()) {
             LOG.debug("Caught exception getting file input stream", ioe);
           }
           success = false;
           writeFile = false;
         } catch(final FileAlreadyParsedException fape) {
           LOG.debug("Source file is older than instrumented file, skipping: " + f.getName());
           success = true;
           writeFile = false;
         } catch (final TokenStreamException tse) {
           System.err.println(f.getAbsolutePath() + ": " + tse);
           if(LOG.isDebugEnabled()) {
             LOG.debug(tse);
           }
           success = false;
           writeFile = false;
         } catch (final RecognitionException re) {
           System.err.println(f.getAbsolutePath() + ":" + re.getLine() + ": " + re.getMessage());
           if(LOG.isDebugEnabled()) {
             LOG.debug(re);
           }
           success = false;
           writeFile = false;
         } finally {
           getSymtab().finishFile(writeFile);
         }
       }
     }
     return success;
   }
 
   /**
    * Actually do the work of parsing the file here.
    *
    * @param s a stream to parse
    */
   private static void parseFile(final InputStream s) throws TokenStreamException, RecognitionException {
     // Create a scanner that reads from the input stream passed to us
     _javaLexer = new JavaLexer(s);
     _assertLexer = new AssertLexer(_javaLexer.getInputState());
       
     _selector.addInputStream(_javaLexer, "java");
     _selector.addInputStream(_assertLexer, "assert");
     _selector.select(_javaLexer);
 
     // Create a parser that reads from the scanner
     final Configuration.SourceCompatibilityEnum sourceCompatibility = getSymtab().getConfiguration().getSourceCompatibility();
     if(Configuration.JAVA_1_4 == sourceCompatibility) {
       final Java14Recognizer parser = new Java14Recognizer(_selector);
       if(_debugLexer) {
         debugLexer(parser);
       } else {
         parser.setSymtab(getSymtab());
         parser.unit();
       }
     } else if(Configuration.JAVA_1_3 == sourceCompatibility) {
       final JavaRecognizer parser = new JavaRecognizer(_selector);
       if(_debugLexer) {
         debugLexer(parser);
       } else {
         parser.setSymtab(getSymtab());
         parser.unit();
       }
     } else {
       AssertTools.internalError("Invalid source compatibility: " + sourceCompatibility);
     }
   }
 
   private static Symtab _symtab;
   /**
    * Get the symbol table.
    */
   public static Symtab getSymtab() {
     return _symtab;
   }
 
   /**
    * Just print out the tokens as they're found.
    */
   private static void debugLexer(final Parser parser) throws TokenStreamException {
     antlr.Token tok = _selector.nextToken();
     while(null != tok.getText()) {
       System.out.print("JonsAssert: " + tok);
       System.out.println(" name=" + parser.getTokenName(tok.getType()));
       tok = _selector.nextToken();
     }
   }
 
   private static boolean _disableExit = false;
   private static int _exitCode = 0;
   /**
    * Get the exit code.
    */
   public static int getExitCode() { return _exitCode; }
 
   private static TokenStreamSelector _selector = new TokenStreamSelector();
   /*package*/ static TokenStreamSelector getSelector() { return _selector; }
   
   private static JavaLexer _javaLexer;
   
   private static AssertLexer _assertLexer;
   /*package*/ static AssertLexer getAssertLexer() { return _assertLexer; }
   
   private static boolean _debugLexer = false;
   
 }
