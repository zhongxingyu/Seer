 /*
  * Copyright (c) 2010 Erwin van Eijk <erwin.vaneijk@gmail.com>. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of <copyright holder>.
  */
 
 package nl.minjus.nfi.dt.jhashtools;
 
 import nl.minjus.nfi.dt.jhashtools.exceptions.PersistenceException;
 import nl.minjus.nfi.dt.jhashtools.hashers.ConcurrencyMode;
 import nl.minjus.nfi.dt.jhashtools.hashers.DirectoryHasher;
 import nl.minjus.nfi.dt.jhashtools.hashers.DirectoryHasherCreator;
 import nl.minjus.nfi.dt.jhashtools.hashers.FileHasher;
 import nl.minjus.nfi.dt.jhashtools.persistence.PersistenceStyle;
 import nl.minjus.nfi.dt.jhashtools.utils.Version;
 import org.apache.commons.cli.*;
 
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.security.NoSuchAlgorithmException;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static java.util.logging.Logger.getLogger;
 
 /**
  * Entrypoint of the cli version of the tooling.
  * @author Erwin van Eijk
  */
 public class App
 {
 
     private static final String USAGE = "[options] dir [dir...]";
     private static final String HEADER =
             "hashtree - Creating a list of digests for files and/or directories.\nCopyright (c) 2010, Erwin van Eijk";
     private static final String FOOTER = "";
     private static final Logger LOG = getLogger(App.class.getName());
     private static final int DEFAULT_TERMINAL_WIDTH = 80;
 
     public static void main(String[] arguments)
     {
         CommandLine line = App.getCommandLine(arguments);
         String[] filesToProcess = line.getArgs();
 
         DirectoryHasher directoryHasher = createDirectoryHasher(line);
 
         LOG.log(Level.INFO, "Version: " + Version.getVersion());
 
         if (line.hasOption("i") && line.hasOption("o")) {
             LOG.log(Level.WARNING, "Make up your mind. Cannot do -i and -o at the same time.");
             System.exit(1);
         }
 
         PersistenceStyle persistenceStyle = getPersistenceStyle(line);
         if (line.hasOption("i")) {
             String filename = line.getOptionValue("i");
             processFileAndVerify(directoryHasher, persistenceStyle, line, filename, filesToProcess);
         } else if (line.hasOption("o")) {
             String outputFilename = line.getOptionValue("output");
             boolean forceOverwrite = line.hasOption("force");
 
             processFilesAndWrite(directoryHasher, outputFilename, persistenceStyle, forceOverwrite, filesToProcess);
        } else {
        	LOG.log(Level.WARNING, "You need either -i or -o");
        	System.exit(2);
         }
 
         System.exit(0);
     }
 
     private static PersistenceStyle getPersistenceStyle(CommandLine line)
     {
         PersistenceStyle persistenceStyle;
         if (line.hasOption("style")) {
             persistenceStyle = PersistenceStyle.convert(line.getOptionValue("style"));
         } else {
             persistenceStyle = PersistenceStyle.JSON;
         }
         return persistenceStyle;
     }
 
     private static void processFilesAndWrite(DirectoryHasher directoryHasher,
                                              String outputFile,
                                              PersistenceStyle style,
                                              boolean forceOverwrite,
                                              String[] filesToProcess)
     {
         try {
             DigestOutputCreator outputCreator =
                     new DigestOutputCreator(System.err, directoryHasher, forceOverwrite);
 
             outputCreator.setOutputFile(outputFile);
             outputCreator.setPersistenceStyle(style);
             outputCreator.generate(filesToProcess);
             outputCreator.finish();
         } catch (FileNotFoundException ex) {
             System.err.println("File " + outputFile + " exists or not forced to be overwritten. Stop.");
             System.exit(-1);
         }
     }
 
     private static void processFileAndVerify(DirectoryHasher directoryHasher,
                                              PersistenceStyle persistenceStyle,
                                              CommandLine line,
                                              String filename,
                                              String[] filesToProcess)
     {
         try {
             DirHasherResultVerifier verifier = new DirHasherResultVerifier(directoryHasher, persistenceStyle);
             verifier.setIgnoreCase(line.hasOption("ignorecase"));
             verifier.loadDigestsFromFile(filename);
             verifier.generateDigests(filesToProcess);
             verifier.verify(new PrintWriter(System.out, true));
         } catch (FileNotFoundException ex) {
             LOG.log(Level.SEVERE, "A file could not be found.", ex);
             System.exit(-1);
         } catch (PersistenceException ex) {
             LOG.log(Level.SEVERE, "Could not parse the file.", ex);
             System.exit(-2);
         }
     }
 
     private static DirectoryHasher createDirectoryHasher(CommandLine line)
     {
         DirectoryHasher directoryHasher = null;
         try {
             ConcurrencyMode concurrencyMode =
                     (line.hasOption("single")) ? ConcurrencyMode.SINGLE : ConcurrencyMode.MULTI_THREADING;
 
             directoryHasher = DirectoryHasherCreator.create(Executors.newCachedThreadPool());
             directoryHasher.setVerbose(line.hasOption("verbose"));
 
             if (line.hasOption("all") || line.hasOption("sha-256")) {
                 directoryHasher.addAlgorithm("sha-256");
             }
             if (line.hasOption("all") || line.hasOption("sha-1")) {
                 directoryHasher.addAlgorithm("sha-1");
             }
             if (line.hasOption("all") || line.hasOption("sha-384")) {
                 directoryHasher.addAlgorithm("sha-384");
             }
             if (line.hasOption("all") || line.hasOption("sha-512")) {
                 directoryHasher.addAlgorithm("sha-512");
             }
             if (line.hasOption("all") || line.hasOption("md5")) {
                 directoryHasher.addAlgorithm("md5");
             }
             if (line.hasOption("all") || line.hasOption("md2")) {
                 directoryHasher.addAlgorithm("md2");
             }
         } catch (NoSuchAlgorithmException ex) {
             LOG.log(Level.SEVERE, "Algorithm not found", ex);
         } finally {
             try {
                 if ((directoryHasher != null) && (directoryHasher.getAlgorithms().size() == 0)) {
                     directoryHasher.addAlgorithm(FileHasher.DEFAULT_ALGORITHM);
                 }
             } catch (NoSuchAlgorithmException ex) {
                 LOG.log(Level.SEVERE, "Algorithm is not found", ex);
                 System.exit(1);
             }
         }
 
         return directoryHasher;
     }
 
     @SuppressWarnings({"static-access", "AccessStaticViaInstance"})
     private static CommandLine getCommandLine(final String[] theArguments)
     {
         CommandLineParser parser = new PosixParser();
 
         Options options = new Options();
 
         options.addOption("h", "help", false, "Get help on the supported commandline options");
         options.addOption("1", "sha-1", false, "Output a sha-1 digest");
         options.addOption("2", "sha-256", false, "Output a sha-256 digest (Default if none given)");
         options.addOption(null, "sha-384", false, "Output a sha-384 digest");
         options.addOption(null, "sha-512", false, "Output a sha-512 digest");
         options.addOption(null, "md5", false, "Output a md5 digest");
         options.addOption(null, "md2", false, "Output a md2 digest (should not be used!)");
         options.addOption("a", "all", false, "Include all available digest algorithms");
         options.addOption("n", "ignorecase", false, "Ignore the case on the file, only used when verifying.");
         options.addOption("v", "verbose", false, "Create verbose output");
         options.addOption("f", "force", false, "Force overwriting any previous output");
         options.addOption(null, "single", false, "Only use single threaded execution path");
         final Option outputOption =
                 OptionBuilder
                         .withLongOpt("output")
                         .withDescription("The file the output is written to")
                         .hasArg()
                         .withArgName("outputfile")
                         .create("o");
         options.addOption(outputOption);
         options.addOption(OptionBuilder
                 .withLongOpt("input")
                 .withDescription("The file needed to verify the found digests")
                 .hasArg()
                 .withArgName("inputfile")
                 .create("i"));
         options.addOption(OptionBuilder
                 .withLongOpt("style")
                 .withDescription("The input/output style to use")
                 .hasArg()
                 .withArgName("style")
                 .create("s"));
         CommandLine line;
         try {
             line = parser.parse(options, theArguments);
             if (line.hasOption("help")) {
                 final HelpFormatter formatter = new HelpFormatter();
                 formatter.setWidth(DEFAULT_TERMINAL_WIDTH);
                 formatter.printHelp(USAGE, HEADER, options, FOOTER);
                 System.exit(0);
             }
         } catch (ParseException ex) {
             LOG.log(Level.SEVERE, "Failed at parsing the commandline options.", ex);
             final HelpFormatter helpFormatter = new HelpFormatter();
             helpFormatter.printHelp("hashtree [options] dir [dir...]", options);
             System.exit(-2);
             return null;
         }
         return line;
     }
 }
