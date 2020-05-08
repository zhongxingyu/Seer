 /**
  * Main entry point for the script compiler
  *
  * @author steele@osteele.com
  * @author ptw@openlaszlo.org
  */
 
 
 package org.openlaszlo.compiler;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.sc.ScriptCompiler;
 import java.io.*;
 import java.util.*;
 import org.apache.log4j.*;
 import org.apache.log4j.spi.*;
 import org.apache.log4j.varia.NullAppender;
 
 public class Main {
     private static final String[] USAGE = {
         "Usage: lzc [OPTION]... FILE...",
         "",
         "Options:",
         "-D<name>=<value>",
         "  Set the name/var property to value (See Compiler.getProperties).",
         "-D<name>",
         "  Short for -Dname=true.",
         "-v",
         "  Write progress information to standard output.",
         "--mcache on|off",
         "  Turns on/off media cache.  Default is off.",
         "--onerror [throw|warn]",
         "  Action to take on compilation errors.  Defaults to warn.",
         "--help",
         "  Prints this message.",
         "--flush-script-cache",
         "  Doesn't flush script cache before compiling.",
         "--script-cache-dir directory",
         "  Location of script cache directory (default <lps>/work/scache)",
         "--media-cache-dir directory",
         "  Location of media cache directory (default <lps>/work/cache/cmcache)",
         "",
         "Output options:",
         "--runtime=[swf7|swf8|swf9|dhtml|j2me|svg|null]",
         "  Compile to swf7, swf8, swf9, dhtml, j2me, svg, null",
         "--dir outputdir",
         "  Output directory.",
         "-c | --compile",
         "  Compile and assemble, but do not link",
         "-g1 | --debug",
         "  Add debugging support into the output object.",
         "-g | -g2 | --backtrace",
         "  Add debugging and backtrace support into the output object.",
         "-o <file> | --output <file>",
         "  Put output into given filename.",
         "-p | --profile",
         "  Add profiling information into the output object.",
         "",
         "Logging options:",
         "-l<loglevel>",
         "  Logging level (See org.apache.log4j.Level)",
         "-l<loggerName>=<loglevel>",
         "  Logging level (See org.apache.log4j.Level)",
         "-lp file",
         "  Log4j properties files",
         "--log logfile",
         "  Specify logfile (output still goes to console as well)",
         "--schema",
         "  Writes the schema to standard output.",
         "--script",
         "  Writes JavaScript to standard output."
     };
 
     private final static String MORE_HELP =
         "Try `lzc --help' for more information.";
 
 
     /** Compiles each file base.ext to the output file base.swf (or .js),
      * writing progress information to standard output.  This method
      * is intended for testing the compiler.
      *
      * <p>See the usage string or execute <code>lzc --help</code>
      * to see a list of options.
      *
      * @param args the command line arguments
      */
     public static void main(String args[])
         throws IOException
     {
       System.exit(lzc(args, null, null, null));
     }
 
     /** This method implements the behavior described in main
      * but also returns an integer error code.
      */
     public static int lzc(String args[], String logFile, 
             String outFileName, String outDir)
         throws IOException
     {
         Logger logger = Logger.getRootLogger();
         List files = new Vector();
         // Configure logging
         logger.setLevel(Level.ERROR);
         PatternLayout layout = new PatternLayout("%m%n");
         logger.removeAllAppenders();
         if (logFile == null) {
             logger.addAppender(new ConsoleAppender(layout));
         } else {
             logger.addAppender(new FileAppender(layout, logFile, false));
         }
     
         Compiler compiler = new Compiler();

        String tmpdirstr = System.getProperty("java.io.tmpdir");
        String cachetmpdirstr = tmpdirstr + File.separator + "lzccache";
        (new File(cachetmpdirstr)).mkdirs();

        String mediaCacheDir = cachetmpdirstr + File.separator + "cmcache";
        String scriptCacheDir = cachetmpdirstr + File.separator + "scache";
         // Set default runtime to compiler.runtime.default
         compiler.setProperty(CompilationEnvironment.RUNTIME_PROPERTY,
                              LPS.getProperty("compiler.runtime.default",
                                              LPS.getRuntimeDefault()));
         boolean flushScriptCache = true;
         Boolean forceTransCode = null;
         String outFileArg = null;
 
         for (int i = 0; i < args.length; i++) {
             String arg = args[i].intern();
             if (arg.startsWith("-")) {
                 if (arg == "-v") {
                     logger.setLevel(Level.ALL);
                 } else if (arg == "-lp") {
                     String value = safeArg("-lp", args, ++i);
                     if (value == null) {
                         return 1;
                     }
                     PropertyConfigurator.configure(value);
                     String lhome = System.getProperty("LPS_HOME");
                     if (lhome == null || lhome.equals("")) {
                         lhome = System.getenv("LPS_HOME");
                     }
                     LPS.setHome(lhome);
                 } else if (arg == "--schema") {
                     compiler.SchemaLogger.setLevel(Level.ALL);
                 } else if (arg == "--keepscriptcache") {
                     flushScriptCache = false;
                     System.err.println("--keepscriptcache is deprecated.  This is now the default behavior.");
                 } else if (arg == "--flush-script-cache") {
                     flushScriptCache = true;
                 } else if (arg == "-o" || arg == "--output") {
                     outFileArg = safeArg("-o or --output", args, ++i);
                     if (outFileArg == null) {
                         return 1;
                     }
                 } else if (arg == "--onerror") {
                     String value = safeArg("--onerror", args, ++i);
                     if (value == null) {
                         return 1;
                     }
                     CompilationError.ThrowCompilationErrors =
                         "throw".equals(value);
                 } else if (arg.startsWith("--runtime=")) {
                     String value = arg.substring("--runtime=".length());
                     if ((new HashSet(Compiler.KNOWN_RUNTIMES)).contains(value)) {
                       compiler.setProperty(CompilationEnvironment.RUNTIME_PROPERTY, value);
                     } else {
                       System.err.println("Invalid value for --runtime");
                       System.err.println(MORE_HELP);
                       return 1;
                     }
                 } else if (arg == "-S" || arg == "--script") {
                     // TODO: [2007-05-25 ptw] This is bogus -- should write out a .lzs file
                     Logger.getLogger(org.openlaszlo.sc.ScriptCompiler.class)
                         .setLevel(Level.ALL);
                 } else if (arg == "--script-cache-dir") {
                     scriptCacheDir = safeArg("--script-cache-dir", args, ++i);
                     if (scriptCacheDir == null) {
                         return 1;
                     } else if (!(new File(scriptCacheDir)).isDirectory()) {
                         System.err.println("lzc: --script-cache-dir " + scriptCacheDir + ": directory must exist");
                         return 1;
                     }
                 } else if (arg == "--media-cache-dir") {
                     mediaCacheDir = safeArg("--media-cache-dir", args, ++i);
                     if (mediaCacheDir == null) {
                         return 1;
                     } else if (!(new File(mediaCacheDir)).isDirectory()) {
                         System.err.println("lzc: --media-cache-dir " + mediaCacheDir + ": directory must exist");
                         return 1;
                     }
                 } else if (arg == "-mcache" || arg == "--mcache") {
                     String value = safeArg("-mcache", args, ++i);
                     if (value == null) {
                         return 1;
                     } else if (value.equals("on")) {
                         forceTransCode = Boolean.FALSE;
                     } else if (value.equals("off")) {
                         forceTransCode = Boolean.TRUE;
                     } else {
                         System.out.println("Invalid value for --mcache");
                         System.err.println(MORE_HELP);
                         return 1;
                     }
                 } else if (arg == "-log" || arg == "--log") {
                     String log = safeArg("-log or --log", args, ++i);
                     if (log == null) {
                         return 1;
                     }
                     logger.removeAllAppenders();
                     logger.addAppender(new FileAppender(layout, log, false));
                 } else if (arg == "-dir" || arg == "--dir") {
                     outDir = safeArg("-dir or --dir", args, ++i);
                     if (outDir == null) {
                         return 1;
                     }
                 } else if (arg.startsWith("-D")) {
                     String key = arg.substring(2);
                     String value = "true";
                     int offset = key.indexOf('=');
                     if (offset >= 0) {
                         value = key.substring(offset + 1).intern();
                         key = key.substring(0, offset);
                     }
                     compiler.setProperty(key, value);
                     LPS.setProperty(key, value);
                 } else if (arg.startsWith("-l")) {
                     Logger thisLogger = logger;
                     String level = arg.substring(2);
                     if (level.indexOf('=') > -1) {
                         String key = level.substring(0, level.indexOf('='));
                         level = level.substring(level.indexOf('=')+1);
                         thisLogger = Logger.getLogger(key);
                     }
                     if (level != "" && level != null) {
                         thisLogger.setLevel(Level.toLevel(level));
                     }
                 } else if (arg == "-g1" || arg == "--debug") {
                     compiler.setProperty(CompilationEnvironment.DEBUG_PROPERTY, "true");
                 } else if (arg == "-g" || arg == "-g2" || arg == "--backtrace") {
                     compiler.setProperty(CompilationEnvironment.DEBUG_PROPERTY, "true");
                     compiler.setProperty(CompilationEnvironment.BACKTRACE_PROPERTY, "true");
                 } else if (arg == "-p" || arg == "--profile") {
                     compiler.setProperty(CompilationEnvironment.PROFILE_PROPERTY, "true");
                 } else if (arg == "-c" || arg == "--compile") {
                   compiler.setProperty(CompilationEnvironment.LINK_PROPERTY, "false");
                 } else if (arg == "--help") {
                     for (int j = 0; j < USAGE.length; j++) {
                         System.err.println(USAGE[j]);
                     }
                     return 0;
                 } else {
                     System.err.println("Usage: lzc [OPTION]... file...");
                     System.err.println(MORE_HELP);
                     return 1;
                 }
                 continue;
             }
             String sourceName = args[i];
             files.add(sourceName);
         }
 
         CompilerMediaCache cache = new CompilerMediaCache(new File(mediaCacheDir), new Properties());
         if (forceTransCode != null) {
             cache.getProperties().setProperty("forcetranscode", forceTransCode.toString());
         }
         compiler.setMediaCache(cache);
 
         ScriptCompiler.initScriptCompilerCache(new File(scriptCacheDir), new Properties());
 
         if (flushScriptCache) {
             ScriptCompiler.clearCacheStatic();
         }
 
         LPS.initialize();
 
         if (files.size() == 0) {
             System.err.println("lzc: no input files");
             System.err.println(MORE_HELP);
             return 1;
         }
         if (outFileArg != null) {
             outFileName = outFileArg;
             if (files.size() > 1) {
                 System.err.println("lzc: -o or --output can only be used with one input file");
                 System.err.println(MORE_HELP);
                 return 1;
             }
         }
         int status = 0;
         for (Iterator iter = files.iterator(); iter.hasNext(); ) {
             String sourceName = (String) iter.next();
             if (files.size() > 1)
                 System.err.println("Compiling " + sourceName);
             status += compile(compiler, logger, sourceName, outFileName, outDir);
         }
         return status;
     }
 
     /**
      * Utility function to produce a reasonable message for incorrect
      * invocations that end with a arg expecting more, like "lzc -mcache".
      * @return the next arg if available, and null not.
      *      When null is returned, an error message already produced
      * @param argname the name of the argument, e.g. "-mcache"
      * @param the args array
      * @param offset the offset into the args array.
      */
     public static String safeArg(String argname, String args[], int offset)
     {
         if (offset >= args.length) {
             System.err.println("lzc: expected argument for " + argname);
             System.err.println(MORE_HELP);
             return null;
         } else {
             return args[offset];
         }
     }
 
     static private int compile(Compiler compiler,
                                 Logger logger,
                                 String sourcePath,
                                 String outName,
                                 String outDir)
     {
         File sourceFile = new File(sourcePath);
         String objExtension = null;
         String finalExtension = null;
         String finalName = null;
         if ("false".equals(compiler.getProperty(CompilationEnvironment.LINK_PROPERTY))) {
           objExtension = ".gz";
           finalExtension = ".lzo";
         } else {
           String runtime = compiler.getProperty(CompilationEnvironment.RUNTIME_PROPERTY);
           objExtension = compiler.getObjectFileExtensionForRuntime(runtime);
         }
         if (outName == null) {
           String baseName = FileUtils.getBase(sourceFile.getName());
           outName = baseName + objExtension;
           if (finalExtension != null) {
             finalName = baseName + finalExtension;
           }
         }
         if (outDir == null) {
           outDir = sourceFile.getParent();
         }
         File objectFile = new File(outDir, outName);
         try {
           System.err.println("Compiling: " + sourceFile + " to " + objectFile);
             compiler.compile(sourceFile, objectFile, new Properties());
             if (finalName != null) {
               File finalFile = new File(outDir, finalName);
               if (finalFile.exists()) {
                 finalFile.delete();
               }
               if (! objectFile.renameTo(finalFile)) {
                 throw new CompilationError("Could not rename " + objectFile + " to " + finalFile);
               }
             }
         } catch (CompilationError e) {
             logger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Compilation errors occurred:"
  */
                 org.openlaszlo.i18n.LaszloMessages.getMessage(
                     Main.class.getName(),"051018-249")
                 );
             logger.error(e.getMessage());
             return 2;
         } catch (IOException e) {
             logger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="IO exception: " + p[0]
  */
                 org.openlaszlo.i18n.LaszloMessages.getMessage(
                     Main.class.getName(),"051018-259", new Object[] {e.getMessage()})
                 );
             return 3;
         }
         return 0;
     }
 }
 
 /**
  * @copyright Copyright 2001-2008 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
 
 
