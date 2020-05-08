 package de.unisiegen.gtitool.ui.utils;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.regex.Pattern;
 
 
 /**
  * This class is used to calculate the current svn version. Note: Do not edit or
  * remove the system output, because it is used by the build script.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public final class SVNVersion
 {
 
   /**
    * The svn pattern.
    */
   private static final Pattern PATTERN = Pattern
       .compile ( "/svn/gtitool/!svn/ver/[0-9]+/trunk/.*" ); //$NON-NLS-1$
 
 
   /**
    * The filename to read.
    */
   private static final String FILE_NAME = "all-wcprops";//$NON-NLS-1$
 
 
   /**
    * The char 0.
    */
   private static final char CHAR_0 = '0';
 
 
   /**
    * The char 1.
    */
   private static final char CHAR_1 = '1';
 
 
   /**
    * The char 2.
    */
   private static final char CHAR_2 = '2';
 
 
   /**
    * The char 3.
    */
   private static final char CHAR_3 = '3';
 
 
   /**
    * The char 4.
    */
   private static final char CHAR_4 = '4';
 
 
   /**
    * The char 5.
    */
   private static final char CHAR_5 = '5';
 
 
   /**
    * The char 6.
    */
   private static final char CHAR_6 = '6';
 
 
   /**
    * The char 7.
    */
   private static final char CHAR_7 = '7';
 
 
   /**
    * The char 8.
    */
   private static final char CHAR_8 = '8';
 
 
   /**
    * The char 9.
    */
   private static final char CHAR_9 = '9';
 
 
   /**
    * Returns the current svn version of all projects.
    * 
    * @return The current svn version of all projects.
    */
   private final static int getVersion ()
   {
     int version = -1;
     int newVersion;
 
     // core
     newVersion = getVersion ( new File ( "../de.unisiegen.gtitool.core" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // ui
     newVersion = getVersion ( new File ( "../de.unisiegen.gtitool.ui" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // htdocs
     newVersion = getVersion ( new File ( "../gtitool.htdocs" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // javacup
     newVersion = getVersion ( new File ( "../gtitool.javacup" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // jflex
     newVersion = getVersion ( new File ( "../gtitool.jflex" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // jgraph
     newVersion = getVersion ( new File ( "../gtitool.jgraph" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // literature
     newVersion = getVersion ( new File ( "../gtitool.literature" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // manual
     newVersion = getVersion ( new File ( "../gtitool.manual" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // presentation
     newVersion = getVersion ( new File ( "../gtitool.presentation" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     // thesis
     newVersion = getVersion ( new File ( "../gtitool.thesis" ) ); //$NON-NLS-1$
     version = newVersion > version ? newVersion : version;
 
     return version;
   }
 
 
   /**
    * Returns the current svn version of the given file and all sub files if the
    * file is a directory.
    * 
    * @param file The input file.
    * @return The current svn version of the given file and all sub files if the
    *         file is a directory.
    */
   private final static int getVersion ( File file )
   {
     int version = -1;
     int newVersion;
     if ( file.isDirectory () )
     {
       File [] files = file.listFiles ();
       for ( int i = 0 ; i < files.length ; i++ )
       {
         newVersion = getVersion ( files [ i ] );
         version = newVersion > version ? newVersion : version;
       }
     }
     else if ( file.getName ().equals ( FILE_NAME ) )
     {
       return getVersionFile ( file );
     }
     return version;
   }
 
 
   /**
    * Returns the current svn version of the given file.
    * 
    * @param file The input file.
    * @return The current svn version of the given file.
    */
   private final static int getVersionFile ( File file )
   {
     int version = -1;
     try
     {
       FileReader fileReader = new FileReader ( file );
       BufferedReader bufferedReader = new BufferedReader ( fileReader );
       String line;
       int newVersion;
       while ( ( line = bufferedReader.readLine () ) != null )
       {
         if ( PATTERN.matcher ( line ).matches () )
         {
           char [] chars = line.toCharArray ();
           for ( int i = 0 ; i < line.length () ; i++ )
           {
             char currentChar = chars [ i ];
             if ( currentChar == CHAR_0 || currentChar == CHAR_1
                 || currentChar == CHAR_2 || currentChar == CHAR_3
                 || currentChar == CHAR_4 || currentChar == CHAR_5
                 || currentChar == CHAR_6 || currentChar == CHAR_7
                 || currentChar == CHAR_8 || currentChar == CHAR_9 )
             {
               String s = String.valueOf ( currentChar );
               for ( int j = i + 1 ; j < line.length () ; j++ )
               {
                 char currentNextChar = chars [ j ];
                 if ( currentNextChar == CHAR_0 || currentNextChar == CHAR_1
                     || currentNextChar == CHAR_2 || currentNextChar == CHAR_3
                     || currentNextChar == CHAR_4 || currentNextChar == CHAR_5
                     || currentNextChar == CHAR_6 || currentNextChar == CHAR_7
                     || currentNextChar == CHAR_8 || currentNextChar == CHAR_9 )
                 {
                   s += String.valueOf ( currentNextChar );
                 }
                 else
                 {
                   break;
                 }
               }
               newVersion = Integer.parseInt ( s );
               version = newVersion > version ? newVersion : version;
             }
           }
         }
       }
     }
     catch ( Exception exc )
     {
       exc.printStackTrace ();
     }
     return version;
   }
 
 
   /**
    * The main method.
    * 
    * @param arguments The arguments.
    */
   public final static void main ( String [] arguments )
   {
     int version = getVersion ();
 
     /*
      * Do not edit or remove the system output, because it is used by the build
      * script.
      */
     System.out.println ( version );
   }
 }
