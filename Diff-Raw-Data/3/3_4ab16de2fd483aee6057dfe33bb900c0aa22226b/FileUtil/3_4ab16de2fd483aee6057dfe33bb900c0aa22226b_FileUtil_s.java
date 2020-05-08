 /*
  * Copyright 2010-2011 Heads Up Development Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.headsupdev.support.java;
 
 import java.io.*;
 import java.net.URL;
 
 /**
  * Various utility methods for working with files
  *
  * @author Andrew Williams
  * @since 1.0
  */
 public class FileUtil
 {
     /**
      * Create a temporary directory similar to the <code>File.createTempFile( String prefix, String suffix )</code> method.
      * For detailed usage see the entry on {@see File#createTempFile(String,String)}.
      *
      * This method provides only part of a temporary-file facility.
      * To arrange for a file created by this method to be deleted automatically,
      * use the <code>File.deleteOnExit( String prefix, String suffix )</code> method.
      *
      * @param prefix The prefix of the temp dir - must be at least 3 characters long
      * @param suffix The suffix of the temp dir - if null is passed then ".tmp" will be used
      * @return a temporary directory in the default temporary storage location
      * @see File#createTempFile(String,String)
      * @throws IOException if the directory could not be created
      */
     public static File createTempDir( String prefix, String suffix )
         throws IOException
     {
         return FileUtil.createTempDir( prefix, suffix, null );
     }
 
     /**
      * Create a temporary directory similar to the <code>File.createTempFile( String prefix, String suffix, File directory )</code> method.
      * For detailed usage see the entry on {@see File#createTempFile(String,String,File)}.
      *
      * This method provides only part of a temporary-file facility.
      * To arrange for a file created by this method to be deleted automatically,
      * use the <code>File.deleteOnExit( String prefix, String suffix, File directory )</code> method.
      *
      * @param prefix The prefix of the temp dir - must be at least 3 characters long
      * @param suffix The suffix of the temp dir - if null is passed then ".tmp" will be used
      * @return a temporary directory in the specified temporary storage location
      * @see File#createTempFile(String,String,File)
      * @throws IOException if the directory could not be created
      */
     public static File createTempDir( String prefix, String suffix, File directory )
         throws IOException
     {
         File ret;
         if ( directory == null )
         {
             ret = File.createTempFile( prefix, suffix );
         }
         else
         {
             if ( !directory.exists() )
             {
                 FileUtil.mkdirs( directory );
             }
 
             ret = File.createTempFile( prefix, suffix, directory );
         }
 
         // delete the temporary file
         FileUtil.delete( ret );
 
         // create a directory with the same name as the temporary file and mark it as temporary
         FileUtil.mkdir( ret );
         return ret;
     }
 
     /**
      * Get the extension from a file's name, if one exists.
      * Returns null if the file is null or there is no extension.
      * The extension is defined as the text following the last '.' in the file name.
      *
      * @param file The file to get the extension of
      * @return The file's extension or null if none
      */
     public static String getExtension( File file )
     {
         return getExtension( file.getName() );
     }
 
     /**
      * Get the extension from a given filename, if one exists.
      * Returns null if the name is null or there is no extension.
      * The extension is defined as the text following the last '.' in the filename.
      *
      * @param fileName The filename to get the extension from
      * @return The file's extension or null if none
      */
     public static String getExtension( String fileName )
     {
         if ( fileName == null )
         {
             return null;
         }
 
         int pos = fileName.lastIndexOf( '.' );
         if ( pos == -1 || pos == fileName.length() - 1 )
         {
             return null;
         }
 
         return fileName.substring( pos + 1 );
     }
 
     /**
      * Make the specified directory, throwing an exception if it could not be created.
      *
      * @param file The directory to create
      * @throws IOException If the directory could not be created or the parent directory did not exist
      */
     public static void mkdir( File file )
         throws IOException
     {
         if ( file.exists() )
         {
             return;
         }
 
         if ( !file.mkdir() )
         {
             throw new IOException( "Unable to create directory: " + file.getPath() );
         }
     }
 
     /**
      * Make the specified directory and any parent directories, throwing an exception if they could not be created.
      *
      * @param file The directory to create
      * @throws IOException If the directory or it's parent could not be created
      */
     public static void mkdirs( File file )
         throws IOException
     {
         if ( file.exists() )
         {
             return;
         }
 
         if ( !file.mkdirs() )
         {
             throw new IOException( "Unable to create directory path: " + file.getPath() );
         }
     }
 
     /**
      * Recursively delete a file or directory of files, throwing an exception if they could not be deleted.
      *
      * @param file The file or directory to delete (recursively)
      * @throws IOException If the file or directory or any of it's child files could not be deleted
      */
     public static void delete( File file )
         throws IOException
     {
         FileUtil.delete( file, true );
     }
 
     /**
      * Delete a file (or if recursing, a directory of files), throwing an exception if they could not be deleted.
      *
      * @param file The file or directory to delete
      * @param recurse Whether or not we should recurse, passing false on a directory with files in it will cause
      *   this method to fail and throw an IOException
      * @throws IOException If the file or directory or any of it's child files (if recursing) could not be deleted
      */
     public static void delete( File file, boolean recurse )
         throws IOException
     {
        if ( !file.exists() )
         {
             return;
         }
 
         if ( file.isDirectory() && recurse )
         {
             for ( File child : file.listFiles() )
             {
                 FileUtil.delete( child, recurse );
             }
         }
 
         if ( !file.delete() )
         {
             throw new IOException( "Unable to delete file: " + file.getPath() );
         }
     }
 
     /**
      * Load a file content to a string.
      * Character conversions will be performed using the UTF-8 character set.
      *
      * @param file The file to read the string content of
      * @return The contents of the named file, parsed using UTF-8
      */
     public static String toString( File file )
     {
         if ( file == null )
         {
             return null;
         }
 
         try
         {
             return IOUtil.toString( new FileInputStream( file ) );
         }
         catch ( FileNotFoundException e )
         {
             // TODO report somehow... (need central logging?)
             e.printStackTrace();
         }
 
         return null;
     }
 
     /**
      * Write a string to the given file. Output will be started at the beginning of the file,
      * removing any previous content.
      *
      * @param string The text to write to the file
      * @param file The file to write the string to
      * @throws IOException If there is an error in writing to the file
      */
     public static void writeToFile( String string, File file )
         throws IOException
     {
         writeToFile( string, file, false );
     }
 
     /**
      * Write a string to the given file. The file will be appended if specified but otherwise will
      * replace any existing content.
      * 
      * @param string The text to write to the file
      * @param file The file to write the string to
      * @param append Whether or not the string should be appended to the end of the file
      * @throws IOException If there is an error in writing to the file
      */
     public static void writeToFile( String string, File file, boolean append )
         throws IOException
     {
         BufferedWriter out = null;
         try
         {
             out = new BufferedWriter( new FileWriter( file, append ) );
 
             out.write( string );
             out.newLine();
         }
         finally
         {
             IOUtil.close( out );
         }
     }
 
     /**
      * Download the contents of a url into a file.
      *
      * @param url The url to download from
      * @param file The file to write into
      * @throws IOException If there was a problem copying the data into the specified file
      */
     public static void downloadToFile( URL url, File file )
         throws IOException
     {
         InputStream in = null;
         OutputStream out = null;
 
         try
         {
             in = url.openStream();
             out = new FileOutputStream( file );
 
             IOUtil.copyStream( in, out );
         }
         finally
         {
             IOUtil.close( in );
             IOUtil.close( out );
         }
     }
 
     /**
      * Lookup a file in the OS's PATH. This method loads the PATH environment variable and
      * attempts to locate the requested filename in each item of the path.
      * If unmatched the method will return null.
      *
      * @param filename The filename that we wish to find on the path
      * @return The full path to the file, if found. If nothing matched then we return null.
      */
     public static File lookupInPath( String filename )
     {
         String path = System.getenv( "PATH" );
         String paths[] = path.split( File.pathSeparator );
 
         for ( String p : paths )
         {
             File ret = new File( p, filename );
             if ( ret.exists() )
             {
                 return ret;
             }
         }
 
         return null;
     }
 
     /**
      * Lookup the parent of a file in the OS's PATH. This method loads the PATH environment
      * variable and attempts to locate the requested filename in each item of the path.
      * If found then the parent directory of this file is returned.
      * If unmatched the method will return null.
      *
      * @param filename The filename that we wish to find the parent of on the path
      * @return The full path to the parent directory, if found. If nothing matched then we return null.
      */
     public static File lookupParentInPath( String filename )
     {
         File file = lookupInPath( filename );
 
         if ( file == null )
         {
             return null;
         }
 
         return file.getParentFile();
     }
 
     /**
      * Lookup the grandparent of a file in the OS's PATH. This method loads the PATH environment
      * variable and attempts to locate the requested filename in each item of the path.
      * If found then the grandparent directory of this file is returned.
      * If unmatched the method will return null.
      *
      * @param filename The filename that we wish to find the grandparent of on the path
      * @return The full path to the grandparent directory, if found. If nothing matched then we return null.
      */
     public static File lookupGrandparentInPath( String filename )
     {
         File file = lookupParentInPath( filename );
 
         if ( file == null )
         {
             return null;
         }
 
         return file.getParentFile();
     }
 }
