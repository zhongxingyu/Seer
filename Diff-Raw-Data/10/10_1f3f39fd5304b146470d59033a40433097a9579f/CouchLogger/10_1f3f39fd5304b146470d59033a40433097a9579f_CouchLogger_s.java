 package ca.couchware.wezzle2d.util;
 
 import ca.couchware.wezzle2d.manager.*;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Calendar;
 import org.lwjgl.Sys;
 
 /**
  * A class to manage writing out to a log file.
  * @author Kevin
  *
  */
 public class CouchLogger
 {
     // ---------------------------------------------------------------------------
     // Static Attributes
     // ---------------------------------------------------------------------------
 
     /** The only instance of the log manager. */
     final private static CouchLogger SINGLE = new CouchLogger();
 
     /**
      * Write out to the log. If this is set to true, all messages and
      * errors will be written out to the log file.
      */
     private final boolean OUTPUT_TO_FILE = true;
 
     /**
      * Is the writer opened?
      */
     private boolean opened = false;
 
     /**
      * The file path to the log file.
      */
     private File logFile;
 
     /**
      * A string buffer to store the log information in before writing it.
      */
     private StringBuilder buffer = new StringBuilder();
 
     /**
      * The file writer to write to the log.
      */
     private BufferedWriter writer;
 
     // ---------------------------------------------------------------------------
     // Constructor
     // ---------------------------------------------------------------------------
     private CouchLogger()
     {
     }
 
     /**
      * Get the single instance of log manager.
      * @return
      */
     public static CouchLogger get()
     {
         return SINGLE;
     }
 
     // ---------------------------------------------------------------------------
     // Static Methods
     // ---------------------------------------------------------------------------
     /**
      * A method to append a string of text to the log file.
      * The text is written and the buffer is flushes so that
      * errors can be logged and read in real time while the
      * program is open.
      *
      * @param text The text to append.
      */
     private void append(String text)
     {
         buffer.append( text + Settings.getLineSeparator() );
     }
 
     /**
      * A method to write the contexts of the buffer to the log file.
      */
     public void write()
     {
         try
         {
             // Make sure file is opened.
             if ( !opened )
             {
                 open();
             }
 
             // Write the buffer.
             writer.write( buffer.toString() );
             writer.flush();
 
             // Clear the buffer.
             buffer = new StringBuilder();
 
             // Close the file.
             close();
         }
         catch ( Exception e )
         {
             recordException( this.getClass(), e );
         }
     }
 
     private void open()
     {
         // See if we already opened it.
         if ( opened )
         {
             return;
         }
 
         // Set the opened variable.
         this.opened = true;
 
         // Check if the directory exists.
         File dir = new File( Settings.getLogPath() );
 
         // If the directory doesn't exist. Create it.
         if ( dir.isDirectory() == false )
         {
             dir.mkdirs();
         }
 
         // Create the file.
         logFile = new File( Settings.getLogFilePath() );
 
         try
         {
             // If the file does not exist, create it.
             if ( logFile.exists() == false )
             {
                 logFile.createNewFile();
             }
 
             // Create the writer.
             writer = new BufferedWriter( new FileWriter( logFile, true ) );
         }
         catch ( Exception e )
         {
             recordException( this.getClass(), e );
         }
     }
 
     /**
      * A method to close the log. This method closes the bufferedWriter
      * and flushes the buffer.
      */
     private void close()
     {
         try
         {
             writer.close();
             opened = false;
         }
         catch ( Exception e )
         {
             recordException( this.getClass(), e );
         }
     }
 
     /**
      * Prints an error to standard error, dumps the stack, and then terminates
      * the program.
      * 
      * @param message The error message.
      * @param t The current thread, usually Thread.currentThread().
      */
     public void recordException(Class cls, Exception e)
     {
         if ( cls == null )
         {
            throw new IllegalArgumentException( "cls must not be null." );
         }
 
         String method = extractClassName( cls );
         StringWriter out = new StringWriter();
         String exceptionString = "E. (" + getTimeStamp() + ") " + method + " - \"" + e.
                 getMessage() + "\"" + Settings.getLineSeparator();
 
         out.write( exceptionString );
 
        Sys.alert("Error", exceptionString);


 
         e.printStackTrace( new PrintWriter( out, true ) );
         System.err.println( out.toString() );
 
         if ( OUTPUT_TO_FILE )
         {
             append( out.toString() );
         }
     }
 
     /**
      * Prints a warning to standard error.  Does not dump the stack.
      * @param message The error message.
      * @param t The current thread, usually Thread.currentThread().
      */
     public void recordWarning(Class cls, String message)
     {
         if ( cls == null )
         {
             throw new IllegalArgumentException( "cls must not be null." );
         }
 
         String method = extractClassName( cls );
         String output = "W. (" + getTimeStamp() + ") " + method + " - \"" + message + "\"";
         System.err.println( output );
 
         if ( OUTPUT_TO_FILE )
         {
             append( output );
         }
     }
 
     /**
      * Prints a message to standard out.  Does not dump the stack.
      * @param message The error message.
      * @param t The current thread, usually Thread.currentThread().
      */
     public void recordMessage(Class cls, String message)
     {
         if ( cls == null )
         {
             throw new IllegalArgumentException( "cls must not be null." );
         }
 
         String method = extractClassName( cls );
         String output = "M. (" + getTimeStamp() + ") " + method + " - \"" + message + "\"";
         System.out.println( output );
 
         if ( OUTPUT_TO_FILE == true )
         {
             append( output );
         }
     }
 
     private static String extractClassName(Class cls)
     {
         if (cls.isAnonymousClass())
         {
             return cls.getEnclosingClass().getSimpleName();
         }
         else
         {
             return cls.getSimpleName();
         }
     }
 
     private static String getTimeStamp()
     {
         Calendar now = Calendar.getInstance();
         return String.format( "%02d:%02d:%02d.%03d", now.get( Calendar.HOUR_OF_DAY ),
                 now.get( Calendar.MINUTE ),
                 now.get( Calendar.SECOND ),
                 now.get( Calendar.MILLISECOND ) );
     }
 
 }
