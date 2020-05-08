 package com.wickedspiral.jacss;
 
 import com.google.common.io.Closeables;
 import com.google.common.util.concurrent.Uninterruptibles;
 import com.wickedspiral.jacss.lexer.Lexer;
 import com.wickedspiral.jacss.parser.Parser;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Pattern;
 
 /**
  * @author wasche
  * @since 2011.08.05
  */
 public class JACSS implements Callable<Boolean>
 {
     private static final String VERSION = JACSS.class.getPackage().getImplementationVersion();
 
     private static class CLI extends Options
     {
         private static final String REGEX_FROM  = "-gen.css$";
         private static final String REGEX_TO    = "-c.css";
         private static final int    NUM_THREADS = 1;
 
         @SuppressWarnings({ "MismatchedQueryAndUpdateOfCollection" })
         @Argument(required = false, usage = "List of files to compress")
         private List<File> files;
 
         @Option(name = "-r", aliases = { "--regex-from" }, required = false, metaVar = "REGEXFROM",
             usage = "Regex to replace with REGEXTO in new file names (default: " + REGEX_FROM + ")")
         private String regexFrom = REGEX_FROM;
 
         @Option(name = "-t", aliases = { "--regex-to" }, required = false, metaVar = "REGEXTO",
             usage = "Regex to replace REGEXFROM with, uses Java's Matcher.replace (default: " + REGEX_TO + ")")
         private String regexTo = REGEX_TO;
 
         @Option(name="-j", aliases={"--threads"}, required=false, metaVar="THREADS",
                 usage="Number of threads to use (default: " + NUM_THREADS + ")")
         private int numThreads = NUM_THREADS;
 
         @Option(name = "-O", aliases = {"--stdout"}, required = false, usage = "Print to stdout instead of to file")
         private boolean stdout = false;
         
         @Option( name = "--help", usage = "Show this help text.")
         private boolean help = false;
         
         @Option( name = "--version", usage = "Show version information.")
         private boolean version = false;
 
         public Pattern getFromPattern()
         {
             return Pattern.compile(regexFrom == null ? REGEX_FROM : regexFrom);
         }
     }
 
     private static final int EXIT_STATUS_INVALID_ARG = 1;
     private static final int EXIT_STATUS_INVALID_FILE = 2;
     private static final int EXIT_STATUS_TIMEOUT = 3;
     private static final int EXIT_STATUS_COMPRESSION_FAILED = 4;
     
     private final String sourceName;
     private final String targetName;
     private final InputStream source;
     private final OutputStream target;
     private final File targetFile;
     private final Options options;
     private final boolean shouldCompress;
     private final boolean closeSource, closeTarget;
 
     public JACSS( File in, File out, Options options ) throws FileNotFoundException
     {
         this.options = options;
 
         if ( !in.isFile() )
         {
             throw new FileNotFoundException(in.toString());
         }
 
         sourceName = in.getName();
         targetName = out.getName();
         targetFile = out;
 
         shouldCompress = options.force || !(out.exists() && out.lastModified() >= in.lastModified());
 
         source = shouldCompress ? new FileInputStream( in ) : null;
         target = shouldCompress ? new FileOutputStream( out ) : null;
         
         closeSource = true;
         closeTarget = true;
     }
     
     public JACSS( File source, OutputStream target, Options options ) throws FileNotFoundException
     {
         this.options = options;
         
         if ( !source.isFile() )
         {
             throw new FileNotFoundException( source.toString() );
         }
         
         sourceName = source.getName();
         targetName = "<out>";
         targetFile = null;
         
         this.source = new FileInputStream( source );
         this.target = target;
         shouldCompress = true;
         
         closeSource = true;
         closeTarget = false;
     }
 
     public JACSS( InputStream source, OutputStream target, Options options )
     {
         this.sourceName = "<in>";
         this.targetName = "<out>";
         this.source = source;
         this.target = target;
         this.options = options;
         targetFile = null;
         shouldCompress = true;
         closeSource = false;
         closeTarget = false;
     }
 
     public Boolean call()
     {
         boolean ok = false;
         if ( shouldCompress )
         {
             if (options.verbose) System.err.println( "Compressing " + sourceName + " to " + targetName );
 
             try(
                     BufferedInputStream in = new BufferedInputStream( source );
                     PrintStream out = new PrintStream( new BufferedOutputStream( target ) )
             )
             {
                 Parser parser = new Parser( out, options );
                 Lexer lexer = new Lexer();
                 lexer.addTokenListener(parser);
                 lexer.parse( in );
                 // Do explicit flush here because BufferedOutputStream swallows the exception
                 // from flush() on close().
                 out.flush();
                 ok = true;
             }
             catch (Exception e)
             {
                 System.err.println("Compression failed for " + sourceName);
                 e.printStackTrace( System.err );
                 if (targetFile != null && targetFile.exists())
                 {
                     targetFile.delete();
                 }
             }
         }
         else
         {
             if (options.verbose) System.err.println("Skipping " + targetName);
            ok = true;
         }
         
         if (closeSource && source != null)
         {
             Closeables.closeQuietly(source);
         }
         if (closeTarget && target != null)
         {
             Closeables.closeQuietly(target);
         }
         
         return ok;
     }
 
     public static void main(String[] args)
     {
         CLI cli = new CLI();
         CmdLineParser parser = new CmdLineParser(cli);
         try
         {
             parser.parseArgument(args);
         }
         catch (CmdLineException e)
         {
             System.err.println(e.getMessage());
             parser.printUsage(System.err);
             System.exit(EXIT_STATUS_INVALID_ARG);
         }
         if ( cli.help )
         {
             System.err.println( "Usage: java -jar JACSS-<version>.jar [options] [files]\n" );
             System.err.println( "Options:\n" );
             parser.printUsage( System.err );
             System.exit( 0 );
         }
         if ( cli.version )
         {
             System.err.println( "JACSS, version " + VERSION );
             System.exit( 0 );
         }
         
         cli.imply();
 
         Pattern from = cli.getFromPattern();
 
         if (cli.debug) System.err.println("Debug mode enabled.");
 
         ExecutorService pool = Executors.newFixedThreadPool(cli.numThreads);
         List<Future<Boolean>> futures = new ArrayList<>();
         JACSS jacss;
         
         if ( null == cli.files || cli.files.isEmpty() )
         {
             jacss = new JACSS( System.in, System.out, cli );
             pool.submit( jacss );
         }
         else
         {
             for (File file : cli.files)
             {
                 try
                 {
                     if ( cli.stdout )
                     {
                         jacss = new JACSS( file, System.out, cli );
                     }
                     else
                     {
                         File f = new File( from.matcher(file.toString()).replaceAll(cli.regexTo) );
                         jacss = new JACSS(file, f, cli);
                     }
                     futures.add(pool.submit(jacss));
                 }
                 catch (FileNotFoundException e)
                 {
                     System.err.println("Could not find file: " + e.getMessage());
                     pool.shutdownNow();
                     System.exit(EXIT_STATUS_INVALID_FILE);
                 }
             }
         }
 
         int numFailures = 0;
         for (Future<Boolean> future: futures)
         {
             try
             {
                 if (! Uninterruptibles.getUninterruptibly(future))
                 {
                     numFailures++;
                 }
             }
             catch (ExecutionException e)
             {
                 numFailures++;
             }
         }
 
         int remaining = pool.shutdownNow().size();
         if (remaining > 0)
         {
             System.err.println(remaining + "incomplete tasks found");
             numFailures++;
         }
         
         System.exit(numFailures == 0 ? 0 : EXIT_STATUS_COMPRESSION_FAILED);
     }
 }
