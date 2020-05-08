 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.common;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.DirectoryWalker;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.apache.commons.io.filefilter.FalseFileFilter;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.io.filefilter.FileFileFilter;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.lang.SystemUtils;
 import com.google.common.collect.Lists;
 
 /**
  * Utility class for doing things with files.
  *
  * @author Laurent Caillette
  */
 public class FileTools {
 
   public static final Comparator< ? super File >
   ABSOLUTEPATH_COMPARATOR = new Comparator< File >() {
     public int compare( File file1, File file2 ) {
       return file1.getAbsolutePath().compareTo( file2.getAbsolutePath() ) ;
     }
   } ;
 
   private FileTools() { }
 
   /**
    * Returns the first file in given directory, with one of given extensions.
    * Match is done on according to the order of given extensions.
    *
    * @param basedir
    * @param fileNameNoExtension
    * @param fileExtensions
    * @return a non-null object.
    * @throws FileNotFoundException with extensive message listing all names of files
    *     that were looked for.
    */
   public static File load( 
       File basedir,
       String fileNameNoExtension,
       String... fileExtensions
   ) throws FileNotFoundException {
     final StringBuffer buffer = new StringBuffer( "Not found:" ) ;
     for( final String extension : fileExtensions ) {
       final File file = new File( basedir, fileNameNoExtension + "." + extension ) ;
       if( file.exists() ) {
         return file ;
       } else {
         buffer.append( "\n    '" ) ;
         buffer.append( file.getAbsolutePath() ) ;
         buffer.append( "'" ) ;
       }
     }
     throw new FileNotFoundException( buffer.toString() ) ;
   }
 
   /**
    * Return files with given extensions in given directory.
    *
    * @param directory a non-null object.
    * @param extensions a non-null array containing no nulls.
    * @return a non-null object iterating on non-null objects.
    */
   public static List< File > scanFiles(
       File directory,
       String[] extensions
   ) {
     final Collection fileCollection = FileUtils.listFiles(
         directory,
         extensions,
         true
     ) ;
 
     // Workaround: Commons Collection doesn't know about Generics.
     final List< File > files = Lists.newArrayList() ;
     for( Object o : fileCollection ) {
       files.add( ( File ) o ) ;
     }
     return files ;
   }
 
   /**
    * Returns a list of visible directories under a root directory.
    * The root directory is included in the list.
    *
    * @param root a non-null object representing a directory.
    * @return a non-null object containing no nulls.
    */
   public static List< File > scanDirectories( File root ) {
     final List< File > directories = Lists.newArrayList() ;
     try {
       new MyDirectoryWalker().walk( root, directories ) ;
     } catch( IOException e ) {
       throw new RuntimeException( e ) ;
     }
     return directories ;
 
   }
 
   /**
    * For a {@code File} object, returns its path relative to a given directory.
    * <p>
    * Given this code:
    * <pre>
 final File parent = ...
 final File child = new File( parent, "some relative path" ) ;
 final File relative = new File( parent, relativizePath( parent, child ) ) ;
    * </pre>
    * We should have {@code child} and {@code relative} referencing the same file.
    *
    * @param parent a non-null object representing a directory.
    * @param child a non-null {@code File} object that must be a child of {@code base}.
    * @return a non-null, non-empty {@code String} representing the name of {@code child}
    *     relative to {@code parent}.
    *     separator.
    * @throws IllegalArgumentException
    */
   public static String relativizePath( File parent, File child ) throws IllegalArgumentException {
 
     final String baseAbsolutePath = parent.getAbsolutePath() ;
     if( ! parent.isDirectory() ) {
       throw new IllegalArgumentException( "Not a directory: " + baseAbsolutePath ) ;
     }
     final String baseAbsolutePathFixed = normalizePath( baseAbsolutePath ) ;
     final String childAbsolutePath = child.isDirectory() ?
         normalizePath( child.getAbsolutePath() ) : child.getAbsolutePath() ;
     
     if( childAbsolutePath.startsWith( baseAbsolutePathFixed ) ) {
       final String relativePath = childAbsolutePath.substring( baseAbsolutePathFixed.length() ) ;
       if( relativePath.startsWith( SystemUtils.FILE_SEPARATOR ) ) {
         return relativePath.substring( 1 ) ;
       } else {
         return relativePath ;
       }
     } else {
       throw new IllegalArgumentException(
           "No parent-child relationship: '" +  baseAbsolutePathFixed + "' " +
           "not parent of '" + childAbsolutePath + "'"
       ) ;
     }
   }
 
   private static String normalizePath( String path ) {
     return path.endsWith( SystemUtils.FILE_SEPARATOR ) ?
         path :
         path + SystemUtils.FILE_SEPARATOR
     ;
   }
 
   public static boolean isParentOf( File maybeParent, File maybeChild ) {
     final String maybeParentPathName = normalizePath( maybeParent.getAbsolutePath() ) ;
     final String maybeChildPathName = normalizePath( maybeChild.getAbsolutePath() ) ;
     return
         ( maybeParentPathName.length() < maybeChildPathName.length() ) &&
         maybeChildPathName.startsWith( maybeParentPathName )
     ;
   }
 
   private static final IOFileFilter VISIBLE_DIRECTORY_FILTER = new IOFileFilter() {
     public boolean accept( File file ) {
      return file.isDirectory() && ! file.isHidden() ;
     }
     public boolean accept( File dir, String name ) {
       return ! dir.isHidden() /*&& ! name.startsWith( "." )*/ ;
     }
   };
 
   private static class MyDirectoryWalker extends DirectoryWalker {
 
     public MyDirectoryWalker() {
       super(
           VISIBLE_DIRECTORY_FILTER,
           -1
       );
     }
 
     protected boolean handleDirectory( File file, int i, Collection collection )
         throws IOException
     {
       collection.add( file ) ;
       return true ;
     }
 
     public void walk( File root, List< File > results ) throws IOException {
       super.walk( root, results ) ;
     }
   }
 }
