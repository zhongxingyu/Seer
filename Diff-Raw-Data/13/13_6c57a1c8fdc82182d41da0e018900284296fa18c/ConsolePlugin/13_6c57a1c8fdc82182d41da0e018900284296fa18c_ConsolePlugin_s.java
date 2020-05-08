 /*
  * Jalopy Java Source Code Formatter
  *
  * Copyright (c) 2002, Marco Hunsicker. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 package de.hunsicker.jalopy.plugin.console;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 
 import de.hunsicker.io.DirectoryScanner;
 import de.hunsicker.io.ExtensionFilter;
 import de.hunsicker.io.FileFormat;
 import de.hunsicker.jalopy.Jalopy;
 import de.hunsicker.jalopy.VersionMismatchException;
 import de.hunsicker.jalopy.language.ClassRepository;
 import de.hunsicker.jalopy.storage.Convention;
 import de.hunsicker.jalopy.storage.ConventionDefaults;
 import de.hunsicker.jalopy.storage.ConventionKeys;
 import de.hunsicker.jalopy.storage.History;
 import de.hunsicker.jalopy.storage.Loggers;
 import de.hunsicker.util.StringHelper;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import org.apache.oro.io.Perl5FilenameFilter;
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.Perl5Compiler;
 
 
 /**
  * The console Plug-in provides a powerful command line interface for the Jalopy engine.
 * 
  * <p>
  * Refer to the online manual for the list of valid command line options. You can find
  * the most recent version of the manual on the official Jalopy homepage: <a
  * href="http://jalopy.sf.net/plugin-console-usage.html">http://jalopy.sf.net/</a>
  * </p>
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 public final class ConsolePlugin
 {
     //~ Static variables/initializers ----------------------------------------------------
 
     /** Represents the --nobackup option. */
     private static final int OPT_NOBACKUP = -10;
 
     /** Represents the --force option. */
     private static final int OPT_FORCE = -100;
 
     /** Represents the --disclaimer option. */
     private static final int OPT_DISCLAIMER = -1000;
 
     /** Represents the --classpath option. */
     private static final int OPT_CLASSPATH = -10000;
 
     /** Indicates that we use a file as input source. */
     private static final int IO_FILE = 1;
 
     /** Indicates that we use standard input (System.in) as input source. */
     private static final int IO_SYSTEM = 2;
 
     /** Used to store a single argument string. */
     private static String _argString;
 
     /** Used as a monitor to synchronize processes. */
     private static final Object _lock = new Object();
 
     /** Logging. */
     private static final Logger _logger = Loggers.IO;
 
     /** Holds the number of files processed. */
     private static volatile int _numFiles;
 
     /** Scanner to search for Java source files. */
     private static DirectoryScanner _scanner = new DirectoryScanner();
 
     /** Number of running formatting threads. */
     private static int _threadCount;
 
     /** Number of parser threads to use. */
     private static int _threads = 1;
 
     /** Holds the longoptions to use. */
     private static final LongOpt[] LONG_OPTIONS =
         {
             new LongOpt("convention" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, 'c'),
             new LongOpt(
                 "classpath" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, OPT_CLASSPATH),
             new LongOpt("destination" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, 'd'),
             new LongOpt(
                 "disclaimer" /* NOI18N */, LongOpt.NO_ARGUMENT, null, OPT_DISCLAIMER),
             new LongOpt("encoding" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, 'e'),
             new LongOpt("format" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, 'f'),
             new LongOpt("force" /* NOI18N */, LongOpt.NO_ARGUMENT, null, OPT_FORCE),
             new LongOpt("loglevel" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, 'l'),
             new LongOpt("nobackup" /* NOI18N */, LongOpt.NO_ARGUMENT, null, OPT_NOBACKUP),
             new LongOpt("thread" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, 't'),
             new LongOpt("recursive" /* NOI18N */, LongOpt.OPTIONAL_ARGUMENT, null, 'r'),
             new LongOpt("style" /* NOI18N */, LongOpt.REQUIRED_ARGUMENT, null, 's'),
             new LongOpt("version" /* NOI18N */, LongOpt.NO_ARGUMENT, null, 'v'),
             new LongOpt("help" /* NOI18N */, LongOpt.NO_ARGUMENT, null, 'h')
         };
 
     /** Bundle with localized message strings. */
     private static final ResourceBundle BUNDLE =
         ResourceBundle.getBundle(
             "de.hunsicker.jalopy.plugin.console.Bundle" /* NOI18N */,
             Convention.getInstance().getLocale());
 
    private static String CONSOLE_APPENDER_NAME = "ConsoleAppender";            
            
     //~ Instance variables ---------------------------------------------------------------
 
     /** Holds the name of the destination directory, if any. */
     private File _destDir;
 
     /** The file format we use to write files. */
     private FileFormat _fileFormat = FileFormat.AUTO;
 
     /** The level to control logging output. */
     private Level _loglevel;
 
     /** File to use load code convention from. */
     private String _convention;
 
     /** The encoding to use to read files. */
     private String _encoding;
 
     /** Should formatting be forced? */
     private boolean _force;
 
     /** Indicates whether no backups should be kept. */
     private boolean _noBackup;
 
     /** Exit code of the run. */
     private int _exitCode;
 
     /** I/O mode to use. Either {@link #IO_FILE} or {@link #IO_SYSTEM}. */
     private int _ioMode = IO_FILE;
 
     //~ Constructors ---------------------------------------------------------------------
 
     /**
      * Creates a new ConsolePlugin object.
      */
     private ConsolePlugin()
     {
     }
 
     //~ Methods --------------------------------------------------------------------------
 
     /**
      * Returns the version information of this Plug-in.
      *
      * @return version information of this Plug-in.
      */
     public static String getVersion()
     {
         return Package.getPackage("de.hunsicker.jalopy.plugin.console" /* NOI18N */)
                       .getImplementationVersion();
     }
 
 
     /**
      * Starts Jalopy from the command line.
      *
      * @param argv command line arguments.  Refer to the online manual for the list of
      *        valid command line options. You can find the most recent version of the
      *        manual on the official Jalopy homepage: <a
      *        href="http://jalopy.sf.net/plugin-console-usage.html">http://jalopy.sf.net/</a>
      */
     public static void main(String[] argv)
     {
         long start = System.currentTimeMillis();
         ConsolePlugin console = new ConsolePlugin();
         console.parseArgs(argv);
         console.initializeLogging();
 
         if ((console._ioMode != IO_SYSTEM) && !console.scan()) // no files found
         {
             if (_argString != null)
             {
                 Object[] args = { _argString };
                 System.err.println(
                     MessageFormat.format(
                         BUNDLE.getString("NON_MATCHING_EXPRESSION" /* NOI18N */), args));
 
                 System.exit(1);
             }
         }
         else
         {
             try
             {
                 Jalopy.checkCompatibility(
                     StringHelper.getPackageName(console.getClass().getName()));
             }
             catch (VersionMismatchException ex)
             {
                 Object[] args = { ex.getExpected(), ex.getFound() };
                 System.err.println(
                     MessageFormat.format(
                         BUNDLE.getString("VERSION_MISMATCH" /* NOI18N */), args));
 
                 System.exit(1);
             }
 
             if (_threads == 1)
             {
                 console.format();
             }
             else
             {
                 _threadCount = _threads;
 
                 // span the formatting threads
                 for (int i = 0; i < _threads; i++)
                 {
                     new FormatThread(console).start();
                 }
 
                 try
                 {
                     // wait until all formatting threads finished
                     synchronized (_lock)
                     {
                         while (_threadCount > 0)
                         {
                             _lock.wait();
                         }
                     }
                 }
                 catch (InterruptedException ignored)
                 {
                     ;
                 }
             }
 
             Object[] args =
                 {
                     new Integer(console._numFiles), new Integer(console._numFiles),
                     console.getRuntime(System.currentTimeMillis() - start)
                 };
             _logger.l7dlog(Level.INFO, "RUN_INFO" /* NOI18N */, args, null);
         }
     }
 
 
     /**
      * Sets the exit code of the program.
      *
      * @param code exit code to use.
      */
     private void setExitCode(int code)
     {
         _exitCode = code;
     }
 
 
     /**
      * Returns the exit code.
      *
      * @return exit code.
      */
     private int getExitCode()
     {
         return _exitCode;
     }
 
 
     /**
      * Returns an array of objects describing the long command line options (those
      * options normally of the form <code>--option</code>).
      *
      * @return objects containing all the necessary info about the long command line
      *         options.
      */
     private LongOpt[] getLongOptions()
     {
         return LONG_OPTIONS;
     }
 
 
     /**
      * Returns a string containing a list of the short-option characters for short
      * command line options.
      *
      * @return <code>c:d:e:f:hl:s:r::t:v</code>
      */
     private String getOptString()
     {
         return "c:d:e:f:hl:s:r::t:v" /* NOI18N */;
     }
 
 
     /**
      * Gets a formatted string with runtime info for the given time.
      *
      * @param time processing time.
      *
      * @return formatted string.
      */
     private String getRuntime(long time)
     {
         StringBuffer buf = new StringBuffer(8);
 
         long hour = time / 3600000;
         time = time - (hour * 3600000);
 
         long min = time / 60000;
         time = time - (min * 60000);
 
         long sec = time / 1000;
         time = time - (sec * 1000);
 
         long msec = time;
 
         if (hour > 0)
         {
             if (hour < 10)
             {
                 buf.append('0');
             }
 
             buf.append(hour);
             buf.append(':');
         }
 
         if (min > 0)
         {
             if (min < 10)
             {
                 buf.append('0');
             }
 
             buf.append(min);
 
             if (hour == 0)
             {
                 buf.append(':');
             }
         }
 
         if ((hour == 0) && (sec > 0))
         {
             if ((min > 0) && (sec < 10))
             {
                 buf.append('0');
             }
 
             buf.append(sec);
 
             if ((min == 0) && (sec < 2))
             {
                 buf.append('.');
             }
         }
 
         if ((min == 0) && (sec < 2))
         {
             if (msec < 100)
             {
                 buf.append('0');
             }
 
             if (msec < 10)
             {
                 buf.append('0');
             }
 
             buf.append(msec);
         }
 
         if (hour > 0)
         {
             buf.append(" hours" /* NOI18N */);
         }
         else if (min > 0)
         {
             buf.append(" min" /* NOI18N */);
         }
         else if (sec > 0)
         {
             buf.append(" sec" /* NOI18N */);
         }
         else
         {
             buf.append(" msec" /* NOI18N */);
         }
 
         return buf.toString();
     }
 
 
     /**
      * Indicates whether the given encoding is supported on the used platform.
      *
      * @param enc encoding name.
      *
      * @return <code>true</code> if <em>enc</em> is a valid encoding.
      */
     private boolean isValidEncoding(String enc)
     {
         try
         {
             new String(new byte[0], enc);
 
             return true;
         }
         catch (UnsupportedEncodingException ex)
         {
             return false;
         }
     }
 
 
     /**
      * Displays the copyright header on standard output.
      */
     private void displayCopyright()
     {
         System.out.println(
             " Jalopy Java Source Code Formatter " + Jalopy.getVersion()
             + " Console Plug-in " /* NOI18N */ + getVersion());
         System.out.println(
             " Copyright (c) " /* NOI18N */
             + BUNDLE.getString("COPYRIGHT_YEAR" /* NOI18N */) + " "
             + BUNDLE.getString("AUTHOR.1" /* NOI18N */));
         System.out.println();
         System.out.println(" Jalopy comes with ABSOLUTELY NO WARRANTY");
         System.out.println(" Use '--disclaimer' to show the disclaimer");
     }
 
 
     /**
      * Displays help information on standard output and exits.
      */
     private void displayHelp()
     {
         displayCopyright();
         System.out.println();
         displayUsage();
 
         System.exit(getExitCode());
     }
 
 
     /**
      * Displays a hint how to invoke the usage help on standard output and exits.
      */
     private void displayHint()
     {
         Object[] args = { "'java " + BUNDLE.getString("PROGRAM_CMD") + " -h'" };
         System.out.println(MessageFormat.format(BUNDLE.getString("HINT"), args));
         System.out.println();
 
         System.exit(getExitCode());
     }
 
 
     /**
      * Displays the software licence on standard output and exits.
      */
     private void displayLicence()
     {
         System.out.println(
             " Jalopy Java Source Code Formatter " + Jalopy.getVersion()
             + " Console Plug-in " /* NOI18N */ + getVersion());
         System.out.println(
             " Copyright (c) " /* NOI18N */
             + BUNDLE.getString("COPYRIGHT_YEAR" /* NOI18N */) + " "
             + BUNDLE.getString("AUTHOR.1" /* NOI18N */));
         System.out.println();
         System.out.println(
             " This program is free software; you can redistribute it and/or modify\n"
             + " it under the terms of the GNU General Public License as published by\n"
             + " the Free Software Foundation; either version 2 of the License, or\n"
             + " (at your option) any later version.\n\n"
             + " This program is distributed in the hope that it will be useful,\n"
             + " but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
             + " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
             + " GNU General Public License for more details.\n\n"
             + " You should have received a copy of the GNU General Public License\n"
             + " along with this program; if not, write to the Free Software Foundation,\n"
             + " Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.");
         System.out.println();
         System.exit(getExitCode());
     }
 
 
     /**
      * Displays the usage help on standard output and exits.
      */
     private void displayUsage()
     {
         System.out.println(
             "Usage: java " + BUNDLE.getString("PROGRAM_CMD") + " [-options] [args...]");
         System.out.println();
         System.out.println("   or  java -jar jalopy-<version>.jar [-options] [args...]");
         System.out.println();
         System.out.println("where options include:");
         System.out.println("  -c, --convention=FILE   load code convention from FILE");
         System.out.println("  -d, --dest=DIR          use DIR as base output directory");
         System.out.println("      --disclaimer        print software disclaimer");
         System.out.println(
             "  -e, --encoding=WORD     assume WORD as encoding of input files where WORD");
         System.out.println(
             "                          describes one of the JDK supported encodings");
         System.out.println(
             "                          (defaults to "
             + System.getProperty("file.encoding") + " if omitted)");
         System.out.println(
             "  -f, --format=WORD       use WORD as output file format where WORD can be");
         System.out.println(
             "                          either UNIX, DOS, MAC, AUTO (the default) or DEFAULT");
         System.out.println("                          (all case-insensitive)");
         System.out.println(
             "      --force             force formatting even if file up-to-date");
         System.out.println("  -h, --help              display this help");
         System.out.println(
             "  -l, --loglevel=WORD     specifies the logging level where WORD can be");
         System.out.println(
             "                          either DEBUG, INFO (the default), WARN or DEBUG");
         System.out.println("                          (all case-insensitive)");
         System.out.println(
             "      --nobackup          indicates that no backups should be kept");
         System.out.println(
             "  -r, --recursive{=NUM}   recurse into directories, up to NUM levels;");
         System.out.println(
             "                          if NUM is omitted, recurses indefinitely");
         System.out.println("  -t, --thread=NUM        use NUM processing threads");
         System.out.println("  -v, --version           print product version and exit");
         System.out.println();
 
         System.exit(getExitCode());
     }
 
 
     /**
      * Displays the software version on standard output and exits.
      */
     private void displayVersion()
     {
         displayCopyright();
         System.out.println();
 
         System.exit(getExitCode());
     }
 
 
     /**
      * Does the actual formatting.
      */
     private void format()
     {
         Jalopy jalopy = new Jalopy();
 
         if (_convention != null)
         {
             try
             {
                 jalopy.setConvention(_convention);
             }
             catch (IOException ex)
             {
                 /**
                  * @todo use logger
                  */
                 ex.printStackTrace();
 
                 return;
             }
         }
 
         jalopy.setFileFormat(_fileFormat);
 
         if (_encoding != null)
         {
             // this is safe, we've checked the encoding on startup
             jalopy.setEncoding(_encoding);
         }
 
         Convention settings = Convention.getInstance();
         jalopy.setInspect(settings.getBoolean(ConventionKeys.INSPECTOR, false));
 
         if (_ioMode == IO_SYSTEM)
         {
             jalopy.setForce(true);
 
             Writer out = new BufferedWriter(new OutputStreamWriter(System.out));
 
             try
             {
                 jalopy.setInput(System.in, "System.in" /* NOI18N */);
                 jalopy.setOutput(out);
                 jalopy.format();
 
                 if (jalopy.getState() == Jalopy.State.ERROR)
                 {
                     System.exit(1);
                 }
             }
             finally
             {
                 try
                 {
                     if (System.in != null)
                     {
                         System.in.close();
                     }
                 }
                 catch (IOException ignored)
                 {
                     ;
                 }
 
                 try
                 {
                     if (out != null)
                     {
                         out.close();
                     }
                 }
                 catch (IOException ignored)
                 {
                     ;
                 }
             }
         }
         else
         {
             int backupLevel =
                 settings.getInt(
                     ConventionKeys.BACKUP_LEVEL, ConventionDefaults.BACKUP_LEVEL);
 
             if (_noBackup)
             {
                 jalopy.setBackup(false);
             }
             else
             {
                 // if backup level == 0, no backups are kept
                 jalopy.setBackup(backupLevel > 0);
                 jalopy.setBackupLevel(backupLevel);
             }
 
             jalopy.setInspect(
                 settings.getBoolean(
                     ConventionKeys.INSPECTOR, ConventionDefaults.INSPECTOR));
             jalopy.setHistoryPolicy(
                 History.Policy.valueOf(
                     settings.get(
                         ConventionKeys.HISTORY_POLICY, ConventionDefaults.HISTORY_POLICY)));
             jalopy.setHistoryMethod(
                 History.Method.valueOf(
                     settings.get(
                         ConventionKeys.HISTORY_METHOD, ConventionDefaults.HISTORY_METHOD)));
 
             if (_force)
             {
                 jalopy.setForce(_force);
             }
             else
             {
                 jalopy.setForce(
                     settings.getBoolean(
                         ConventionKeys.FORCE_FORMATTING,
                         ConventionDefaults.FORCE_FORMATTING));
             }
 
             jalopy.setBackupDirectory(
                 settings.get(
                     ConventionKeys.BACKUP_DIRECTORY,
                     Convention.getBackupDirectory().getAbsolutePath()));
 
             if (_destDir != null)
             {
                 jalopy.setDestination(_destDir);
             }
 
             while (!_scanner.isFinished() || !_scanner.isEmpty())
             {
                 File source = null;
 
                 try
                 {
                     // blocking take
                     source = _scanner.take();
                     jalopy.setInput(source);
                     jalopy.setOutput(source);
 
                     boolean success = jalopy.format();
 
                     if (jalopy.getState() == Jalopy.State.ERROR)
                     {
                         /**
                          * @todo user configurable FAIL_ON_ERROR
                          */
                         System.exit(1);
                     }
 
                     if (success)
                     {
                         _numFiles++;
                     }
                 }
                 catch (IOException ex)
                 {
                     /**
                      * @todo use logger
                      */
                     ex.printStackTrace();
                 }
                 catch (InterruptedException ex)
                 {
                     return;
                 }
                 catch (Throwable ex)
                 {
                     /**
                      * @todo use logger
                      */
                     ex.printStackTrace();
                 }
             }
         }
 
         jalopy.cleanupBackupDirectory();
 
         if (_logger.isDebugEnabled())
         {
             _logger.debug(jalopy.getProfileTimes());
         }
     }
 
     /**
      * Initializes the given logger.
     * 
      * @param logger the logger to initialize.
      * @param appender the appender the logger should be added to (if it weren't already).
      * @param level the new level of the logger.
      */
     private void initializeLogger(
         Logger   logger,
         Appender appender,
         Level    level)
     {
         Object currentAppender = logger.getAppender(CONSOLE_APPENDER_NAME);
 
         if (currentAppender == null)
         {
             logger.addAppender(appender);
         }
 
         logger.setLevel(level);
     }
 
 
     /**
      * Program initialization.
      */
     private void initializeLogging()
     {
         Appender appender =
             new ConsoleAppender(
                 new PatternLayout("[%p] %m\n" /* NOI18N */),
                 (_ioMode == IO_FILE) ? "System.out" /* NOI18N */
                                      : "System.err" /* NOI18N */);
         appender.setName(CONSOLE_APPENDER_NAME);
 
         if (_loglevel == null)
         {
             Loggers.initialize(appender);
             initializeLogger(Loggers.IO, appender, Level.INFO);
         }
         else
         {
             Loggers.ALL.removeAllAppenders();
 
             ResourceBundle bundle =
                 ResourceBundle.getBundle(
                     "de.hunsicker.jalopy.storage.Bundle" /* NOI18N */,
                     Convention.getInstance().getLocale());
 
             Loggers.ALL.setResourceBundle(bundle);
 
             initializeLogger(Loggers.IO, appender, _loglevel);
             initializeLogger(Loggers.PARSER, appender, _loglevel);
             initializeLogger(Loggers.PRINTER, appender, _loglevel);
             initializeLogger(Loggers.TRANSFORM, appender, _loglevel);
             initializeLogger(Loggers.PARSER_JAVADOC, appender, _loglevel);
             initializeLogger(Loggers.PRINTER_JAVADOC, appender, _loglevel);
         }
     }
 
 
     /**
      * Loads the repository.
      *
      * @param files the serialized repository entries to load.
      */
     private void loadRepository(List files)
     {
         ClassRepository repository = ClassRepository.getInstance();
 
         try
         {
             repository.loadAll(files);
         }
         catch (Exception ex)
         {
             // make sure the repository is empty so that import transformation
             // will be disabled
             if ((repository != null) && !repository.isEmpty())
             {
                 try
                 {
                     repository.unloadAll(files);
                 }
                 catch (Exception e)
                 {
                     _logger.error("Error cleaning up repository", e);
                 }
 
                 _logger.warn("Could not load the import repository", ex);
             }
         }
     }
 
 
     /**
      * Parses the given command line arguments and decodes the options.
      *
      * @param argv command line arguments.
      */
     private void parseArgs(String[] argv)
     {
         _scanner = new DirectoryScanner();
 
         Getopt g =
             new Getopt("Jalopy" /* NOI18N */, argv, getOptString(), getLongOptions());
         int c = -1;
 
         while ((c = g.getopt()) != -1)
         {
             switch (c)
             {
                 case 'd' :
                     _destDir = new File(g.getOptarg());
 
                     if (!_destDir.exists())
                     {
                         if (!_destDir.mkdirs())
                         {
                             Object[] args = { _destDir };
                             System.err.println(
                                 MessageFormat.format(
                                     BUNDLE.getString(
                                         "ERROR_CREATING_DESTINATION_DIRECTORY" /* NOI18N */),
                                     args));
                             System.exit(1);
                         }
                     }
                     else if (_destDir.isFile())
                     {
                         Object[] args = { _destDir };
                         System.err.println(
                             MessageFormat.format(
                                 BUNDLE.getString(
                                     "INVALID_DESTINATION_DIRECTORY" /* NOI18N */), args));
                         System.exit(1);
                     }
 
                     break;
 
                 case 'c' :
                     _convention = g.getOptarg();
 
                     break;
 
                 case 's' :
                     _convention = g.getOptarg();
 
                     break;
 
                 case 'e' :
 
                     if (isValidEncoding(g.getOptarg()))
                     {
                         _encoding = g.getOptarg();
                     }
                     else
                     {
                         Object[] args = { g.getOptarg() };
                         System.err.println(
                             MessageFormat.format(
                                 BUNDLE.getString("INVALID_ENCODING" /* NOI18N */), args));
                         System.exit(1);
                     }
 
                     break;
 
                 case 'f' :
 
                     String format = g.getOptarg().trim().toLowerCase();
 
                     if (
                         format.equals("dos" /* NOI18N */)
                         || format.equals(FileFormat.DOS.getLineSeparator()))
                     {
                         _fileFormat = FileFormat.DOS;
                     }
                     else if (
                         format.equals("default" /* NOI18N */)
                         || format.equals(FileFormat.DEFAULT.toString()))
                     {
                         _fileFormat = FileFormat.DEFAULT;
                     }
                     else if (
                         format.equals("unix" /* NOI18N */)
                         || format.equals(FileFormat.UNIX.getLineSeparator()))
                     {
                         _fileFormat = FileFormat.UNIX;
                     }
                     else if (
                         format.equals("mac" /* NOI18N */)
                         || format.equals(FileFormat.MAC.getLineSeparator()))
                     {
                         _fileFormat = FileFormat.MAC;
                     }
                     else if (
                         format.equals("auto" /* NOI18N */)
                         || format.equals(FileFormat.AUTO.toString()))
                     {
                         _fileFormat = FileFormat.AUTO;
                     }
                     else
                     {
                         Object[] args = { format };
                         System.err.println(
                             MessageFormat.format(
                                 BUNDLE.getString("INVALID_FILE_FORMAT" /* NOI18N */), args));
                         setExitCode(1);
                         displayHint();
                     }
 
                     break;
 
                 case 'l' :
 
                     String level = g.getOptarg().trim().toUpperCase();
 
                     if (level.equals("INFO" /* NOI18N */))
                     {
                         _loglevel = Level.INFO;
                     }
                     else if (level.equals("DEBUG" /* NOI18N */))
                     {
                         _loglevel = Level.DEBUG;
                     }
                     else if (level.equals("WARN" /* NOI18N */))
                     {
                         _loglevel = Level.WARN;
                     }
                     else if (level.equals("ERROR" /* NOI18N */))
                     {
                         _loglevel = Level.ERROR;
                     }
                     else
                     {
                         Object[] args = { level };
 
                        System.err(
                             MessageFormat.format(
                                 BUNDLE.getString("INVALID_LOGLEVEL" /* NOI18N */), args));
                         System.exit(1);
                     }
 
                     break;
 
                 case OPT_FORCE :
                     _force = true;
 
                     break;
 
                 case OPT_DISCLAIMER :
                     displayLicence();
 
                     break;
 
                 case OPT_CLASSPATH :
 
                     List files = new ArrayList();
 
                     for (
                         StringTokenizer tokens = new StringTokenizer(g.getOptarg(), ";");
                         tokens.hasMoreElements();)
                     {
                         File file = new File(tokens.nextToken()).getAbsoluteFile();
 
                         if (!file.exists())
                         {
                             Object[] args = { file };
                             System.err.println(
                                 MessageFormat.format(
                                     BUNDLE.getString("INVALID_CLASSPATH" /* NOI18N */),
                                     args));
                             System.exit(1);
                         }
 
                         files.add(file);
                     }
 
                     loadRepository(files);
 
                     break;
 
                 case 'h' :
                     displayHelp();
 
                     break;
 
                 case 'r' :
 
                     String depth = g.getOptarg();
 
                     if (depth == null)
                     {
                         // recurse indefinitely
                         _scanner.setMaxLevels(Integer.MAX_VALUE);
                     }
                     else
                     {
                         try
                         {
                             int number = Integer.parseInt(depth);
 
                             if (number > 0)
                             {
                                 _scanner.setMaxLevels(number);
                             }
                             else
                             {
                                 Object[] args = { depth };
                                 System.err.println(
                                     MessageFormat.format(
                                         BUNDLE.getString(
                                             "INVALID_RECURSION_NUMBER" /* NOI18N */), args));
                                 setExitCode(1);
                                 displayHint();
                             }
                         }
                         catch (NumberFormatException ex)
                         {
                             Object[] args = { depth };
                             System.err.println(
                                 MessageFormat.format(
                                     BUNDLE.getString(
                                         "INVALID_RECURSION_LEVEL" /* NOI18N */), args));
                             setExitCode(1);
                             displayHint();
                         }
                     }
 
                     break;
 
                 case 't' :
 
                     try
                     {
                         int threads = Integer.parseInt(g.getOptarg());
 
                         if (threads > 0)
                         {
                             _threads = threads;
                         }
                         else
                         {
                             Object[] args = { new Integer(threads) };
                             System.err.println(
                                 MessageFormat.format(
                                     BUNDLE.getString(
                                         "INVALID_THREAD_NUMBER" /* NOI18N */), args));
                             setExitCode(1);
                             displayHint();
                         }
                     }
                     catch (NumberFormatException ex)
                     {
                         Object[] args = { g.getOptarg() };
                         System.err.println(
                             MessageFormat.format(
                                 BUNDLE.getString("INVALID_THREAD_NUMBER" /* NOI18N */),
                                 args));
                         setExitCode(1);
                         displayHint();
                     }
 
                     break;
 
                 case 'v' :
                     displayVersion();
 
                     break;
 
                 case OPT_NOBACKUP :
                     _noBackup = true;
 
                     break;
 
                 case '?' :
                     setExitCode(1);
                     displayUsage();
 
                     break;
             }
         }
 
         PatternCompiler compiler = new Perl5Compiler();
 
         // handle the non-option args - our targets (files, dirs
         // and filter expressions)
         if ((argv.length > 0) && (g.getOptind() != argv.length))
         {
             List targets = new ArrayList(5);
 
             for (int i = g.getOptind(); i < argv.length; i++)
             {
                 File target = new File(argv[i]);
 
                 if (target.exists())
                 {
                     targets.add(target);
                 }
                 else // check for pattern expression
                 {
                     try
                     {
                         _argString = argv[i];
 
                         if (argv[i].indexOf(File.separatorChar) > -1)
                         {
                             String path =
                                 argv[i].substring(
                                     0, argv[i].lastIndexOf(File.separatorChar) + 1);
                             target = new File(path);
 
                             if (target.exists())
                             {
                                 String pattern = argv[i].substring(path.length());
                                 compiler.compile(argv[i]);
                                 _scanner.addFilter(new Perl5FilenameFilter(pattern));
                                 targets.add(target);
 
                                 continue;
                             }
                         }
                         else
                         {
                             compiler.compile(argv[i]);
                             _scanner.addFilter(new Perl5FilenameFilter(argv[i]));
 
                             // no path was given, so use current directory
                             targets.add(new File("." /* NOI18N */).getAbsolutePath());
 
                             continue;
                         }
                     }
                     catch (Throwable ex)
                     {
                         Object[] args = { ex.getMessage(), argv[i] };
                         System.err.println(
                             MessageFormat.format(
                                 BUNDLE.getString("MALFORMED_EXPRESSION" /* NOI18N */),
                                 args));
                         System.exit(1);
                     }
 
                     Object[] args = { argv[i] };
                     System.err.println(
                         MessageFormat.format(
                             BUNDLE.getString("INPUT_SOURCE_NOT_EXIST" /* NOI18N */), args));
                 }
             }
 
             _scanner.setTargets(targets);
         }
         else
         {
             try
             {
                 if (System.in.available() == 0)
                 {
                     displayUsage();
                 }
             }
             catch (IOException ignored)
             {
                 ;
             }
 
             System.err.println(BUNDLE.getString("LISTEN_ON_STDIN" /* NOI18N */));
 
             // no non-option string given; listen on STD_IN, write
             // to STD_OUT, messages goes to standard STD_ERR
             _ioMode = IO_SYSTEM;
             _numFiles = 1;
         }
     }
 
 
     /**
      * Scans the given targets. This method must be called <strong>AFTER</strong> the
      * parsing of the command line arguments.
      *
      * @return <code>true</code> if any matching file(s) could be found.
      *
      * @see #parseArgs
      */
     private boolean scan()
     {
         _scanner.addFilter(new ExtensionFilter(".java" /* NOI18N */));
 
         /**
          * @todo start async if multi-threaded
          */
         _scanner.run();
 
         return !_scanner.isEmpty();
     }
 
     //~ Inner Classes --------------------------------------------------------------------
 
     private static class FormatThread
         extends Thread
     {
         ConsolePlugin console;
 
         public FormatThread(ConsolePlugin console)
         {
             this.console = console;
         }
 
         public void run()
         {
             try
             {
                 console.format();
             }
             finally
             {
                 synchronized (_lock)
                 {
                     _threadCount--;
                     _lock.notify();
                 }
             }
         }
     }
 }
