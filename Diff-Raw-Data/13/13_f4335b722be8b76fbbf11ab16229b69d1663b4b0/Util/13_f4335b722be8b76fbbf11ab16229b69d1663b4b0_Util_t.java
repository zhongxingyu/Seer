 package com.github.wolfie.bob;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.github.wolfie.bob.exception.BobRuntimeException;
import com.github.wolfie.bob.exception.NotAReadableDirectoryException;
 
 public class Util {
   public interface FilePredicate {
     boolean accept(File file);
   }
   
   public static final FilePredicate JAVA_SOURCE_FILE = new FilePredicate() {
     @Override
     public boolean accept(final File file) {
       return file.getName().endsWith(".java");
     }
   };
   
   public static final FilePredicate JAVA_CLASS_FILE = new FilePredicate() {
     @Override
     public boolean accept(final File file) {
       return file.getName().endsWith(".class");
     }
   };
   
   public static String implode(String glue, final Object... bits) {
     if (glue == null) {
       glue = "";
     }
     
     final StringBuilder builder = new StringBuilder();
     for (final Object bit : bits) {
       builder.append(bit.toString()).append(glue);
     }
     
     // remove the last bit
     builder.delete(builder.length() - glue.length(), builder.length());
     
     return builder.toString();
   }
   
   public static String implode(final String glue, final Collection<?> bits) {
     return implode(glue, bits.toArray());
   }
   
   public static File getTemporaryDirectory() throws IOException {
     final File tempDir = File.createTempFile("bob", null);
     
     // Delete autocreated temporary file, and try to recreate it as a
     // directory instead.
     if (!tempDir.delete() || !tempDir.mkdir()) {
       throw new IOException("could not create temporary directory "
           + tempDir.getAbsolutePath());
     }
     
     return tempDir;
   }
   
   /**
    * Verifies that no argument is <code>null</code>
    * 
    * @param args
    *          the arguments to check
    * @throws NullPointerException
    *           if any element in <tt>args</tt> is <code>null</code>.
    */
   public static void checkNulls(final Object... args) {
     for (int i = 0; i < args.length; i++) {
       if (args[i] == null) {
         throw new NullPointerException("Argument with index " + i + " was null");
       }
     }
   }
   
   /**
    * Gets all files underneath a directory matching a given predicate
    * 
    * @param baseDir
    *          The base directory in which to find the files.
    * @param predicate
    *          The predicate to fulfill.
    * @return All {@link File}s that match <tt>predicate</tt> and are in, or
    *         under, <tt>baseDir</tt>.
    */
   public static Collection<File> getFilesRecursively(final File baseDir,
       final FilePredicate predicate) {
     final Set<File> files = new HashSet<File>();
     
     for (final File file : baseDir.listFiles()) {
       if (file.isDirectory()) {
         files.addAll(getFilesRecursively(file, predicate));
       } else if (predicate.accept(file)) {
         files.add(file);
       }
     }
     
     return files;
   }
   
   public static Collection<File> normalizeFilePaths(final File basePath,
       final Collection<File> files) {
     final List<File> result = new ArrayList<File>(files.size());
     final String basePathName = basePath.getPath();
     
     for (final File file : files) {
       if (file.getPath().startsWith(basePathName)) {
         String filename = file.getPath().replace(basePathName, "");
         if (filename.startsWith("/")) {
           filename = filename.substring(1);
         }
         result.add(new File(basePath, filename));
       } else {
         result.add(file);
       }
     }
     
     return result;
   }
   
   /**
    * Verifies that the argument is a readable directory.
    * 
    * @param directory
    *          The {@link File} to check for.
    * @return <tt>directory</tt>
    * @throws NotAReadableDirectoryException
    *           if <tt>directory</tt> is not a directory or it is not readable.
    */
   public static File checkedDirectory(final File directory) {
     if (directory.isDirectory() && directory.canRead()) {
       return directory;
     } else {
       throw new NotAReadableDirectoryException(directory);
     }
   }
   
   /**
    * 
    * @param directory
    */
   public static void createDir(final File directory) {
     if (!directory.isDirectory()) {
       Log.finer("Creating directory " + directory.getAbsolutePath());
       final boolean success = directory.mkdir();
       
       if (!success) {
         throw new BobRuntimeException("Could not create directory "
             + directory.getAbsolutePath());
       }
     } else {
       if (directory.isFile()) {
         throw new BobRuntimeException("Could not create directory "
             + directory.getAbsolutePath()
             + " since it is an existing file.");
       } else {
         Log.info("Not creating " + directory.getAbsolutePath()
             + " since it already exists.");
       }
     }
   }
   
   /**
    * Delete a {@link File}. If it is a normal file, it will simply be deleted.
    * If it is a directory, it, and everything underneath, will recursively be
    * removed.
    * 
    * @param file
    *          The file to remove.
    */
   public static void delete(final File file) {
     if (file.exists()) {
       
       if (file.isDirectory()) {
         for (final File fileEntry : file.listFiles()) {
           delete(fileEntry);
         }
       }
       
       Log.finer("Deleting " + file.getAbsolutePath());
       final boolean success = file.delete();
       if (!success) {
         throw new BobRuntimeException("Could not delete "
             + file.getAbsolutePath());
       }
     } else {
       Log.info("Not removing " + file.getAbsolutePath()
           + " since it does not exist.");
     }
   }
 }
