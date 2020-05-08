 // Copyright (C) 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.util;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * Utility methods for working with directories.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class Directory  {
 
   private final File m_directory;
   private final List m_warnings = new ArrayList();
 
   /**
    * Constructor.
    *
    * @param directory The directory which this <code>Directory</code>
    * operates upon.
    * @exception DirectoryException If <code>directory</code> is not a
    * directory, or if the directory could not be created.
    */
   public Directory(File directory) throws DirectoryException {
     if (directory.exists() && !directory.isDirectory()) {
       throw new DirectoryException(
         "'" + directory.getPath() + "' is not a directory");
     }
 
     m_directory = directory;
   }
 
   /**
    * Create the directory if it doesn't exist.
    *
    * @exception DirectoryException If the directory could not be created.
    */
   public void create() throws DirectoryException {
     if (!m_directory.exists()) {
       if (!m_directory.mkdirs()) {
         throw new DirectoryException(
           "Could not create directory '" + m_directory + "'");
       }
     }
   }
 
   /**
    * Get as a <code>java.io.File</code>.
    *
    * @return The <code>File</code>
    */
   public File getAsFile() {
     return m_directory;
   }
 
   /**
    * List the files in the hierarchy below the directory.
    *
    * @return The list of files. Files are relative to the directory,
    * not absolute. More deeply nested files are later in the list.
    */
   public File[] listContents() {
     return listContents(-1);
   }
 
   /**
    * List the files in the hierarchy below the directory that have
    * been modified after <code>since</code>.
    *
    * @param since Milliseconds since the Epoch. Don't return files
    * that are older than this. Specify <code>-1</code> to return all
    * files.
    * @return The list of files. Files are relative to the directory,
    * not absolute. More deeply nested files are later in the list.
    */
   public File[] listContents(long since) {
     return listContents(false, false, since);
   }
 
   private File[] listContents(boolean includeDirectories,
                               boolean absolutePaths,
                               long since) {
 
     final List resultList = new ArrayList();
     final Set visited = new HashSet();
     final List directoriesToVisit = new ArrayList();
 
     // new File(null, path) is equivalent to new File(path).
     directoriesToVisit.add(null);
 
     while (directoriesToVisit.size() > 0) {
       final File[] directories =
         (File[]) directoriesToVisit.toArray(
           new File[directoriesToVisit.size()]);
 
       directoriesToVisit.clear();
 
       for (int i = 0; i < directories.length; ++i) {
         final File relativeDirectory = directories[i];
         final File absoluteDirectory =
           relativeDirectory != null ?
           new File(getAsFile(), relativeDirectory.getPath()) : getAsFile();
 
         visited.add(relativeDirectory);
 
         // We use list() rather than listFiles() so the results are
         // relative, not absolute.
         final String[] children = absoluteDirectory.list();
 
         if (children == null) {
           // This can happen if the user does not have permision to
           // list the directory.
           m_warnings.add("Could not list '" + absoluteDirectory);
          continue;
         }
 
         for (int j = 0; j < children.length; ++j) {
           final File relativeChild = new File(relativeDirectory, children[j]);
           final File absoluteChild = new File(absoluteDirectory, children[j]);
 
           if (includeDirectories || !absoluteChild.isDirectory()) {
             if (absoluteChild.lastModified() > since) {
               resultList.add(absolutePaths ? absoluteChild : relativeChild);
             }
           }
 
           if (absoluteChild.isDirectory() &&
               !visited.contains(relativeChild)) {
             directoriesToVisit.add(relativeChild);
           }
         }
       }
     }
 
     return (File[])resultList.toArray(new File[resultList.size()]);
   }
 
   /**
    * Delete the contents of the directory.
    *
    * @throws DirectoryException If a file could not be deleted. The
    * contents of the directory are left in an indeterminate state.
    * @see #delete
    */
   public void deleteContents() throws DirectoryException {
     // We rely on the order of the listContents result: more deeply
     // nested files are later in the list.
     final File[] deleteList = listContents(true, true, -1);
 
     for (int i = deleteList.length - 1; i >= 0; --i) {
       if (!deleteList[i].delete()) {
         throw new DirectoryException(
           "Could not delete '" + deleteList[i] + "'");
       }
     }
   }
 
   /**
    * Delete the directory. This will fail if the directory is not
    * empty.
    *
    * @throws DirectoryException If the directory could not be deleted.
    * @see #deleteContents
    */
   public void delete() throws DirectoryException {
     if (!m_directory.delete()) {
       throw new DirectoryException("Could not delete '" + m_directory + "'");
     }
   }
 
   /**
    * Find the given file in the directory and return a File
    * representing its path relative to the root of the directory.
    *
    * @param absoluteFile The file to search for.
    * @return The relatvie file, or <code>null</code> if the file was
    * not found.
    */
   public File getRelativePath(File absoluteFile) {
 
     final File[] contents = listContents();
 
     for (int i = 0; i < contents.length; ++i) {
       if (new File(getAsFile(), contents[i].getPath()).equals(absoluteFile)) {
         return contents[i];
       }
     }
 
     return null;
   }
 
   /**
    * Copy contents of the directory to the target directory.
    *
    * @param target Target directory.
    * @param incremental <code>true</code> => copy newer files to the
    * directory. <code>false</code> => overwrite the target directory.
    * @throws IOException If a file could not be copied. The contents
    * of the target directory are left in an indeterminate state.
    */
   public void copyTo(Directory target, boolean incremental)
     throws IOException {
 
     target.create();
 
     if (!incremental) {
       target.deleteContents();
     }
 
     final File[] files = listContents(true, false, -1);
     final byte[] buffer = new byte[8196];
 
     for (int i = 0; i < files.length; ++i) {
       final String relativePath = files[i].getPath();
       final File source = new File(getAsFile(), relativePath);
       final File destination = new File(target.getAsFile(), relativePath);
 
       if (source.isDirectory()) {
         destination.mkdirs();
       }
       else {
         // Copy file.
         if (!incremental ||
             !destination.exists() ||
             source.lastModified() > destination.lastModified()) {
 
           FileInputStream in = null;
           FileOutputStream out = null;
 
           try {
             in = new FileInputStream(source);
             out = new FileOutputStream(destination);
 
             int n;
 
             while ((n = in.read(buffer)) != -1) {
               out.write(buffer, 0, n);
             }
           }
           finally {
             if (in != null) {
               try {
                 in.close();
               }
               catch (IOException e) {
                 // Ignore;
               }
             }
 
             if (out != null) {
               try {
                 out.close();
               }
               catch (IOException e) {
                 // Ignore;
               }
             }
           }
         }
       }
     }
   }
 
   /**
    * Return a list of warnings that have occured since the last time
    * {@link #getWarnings} was called.
    *
    * @return The list of warnings.
    */
   public String[] getWarnings() {
     try {
       return (String[])m_warnings.toArray(new String[m_warnings.size()]);
     }
     finally {
       m_warnings.clear();
     }
   }
 
   /**
    * An exception type used to report Directory related problems.
    */
   public static final class DirectoryException extends IOException {
     DirectoryException(String message) {
       super(message);
     }
   }
 
   /**
    * Delegate equality to our <code>File</code>.
    *
    * @return The hash code.
    */
   public int hashCode() {
     return getAsFile().hashCode();
   }
 
   /**
    * Delegate equality to our <code>File</code>.
    *
    * @param o Object to compare.
    * @return <code>true</code> => equal.
    */
   public boolean equals(Object o) {
     if (o == this) {
       return true;
     }
 
     if (!(o instanceof Directory)) {
       return false;
     }
 
     return getAsFile().equals(((Directory)o).getAsFile());
   }
 }
