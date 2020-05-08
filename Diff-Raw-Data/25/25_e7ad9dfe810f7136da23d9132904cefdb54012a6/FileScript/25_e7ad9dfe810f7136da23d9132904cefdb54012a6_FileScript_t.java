 /**
  * 
  */
 package cello.alt.servlet.scripting;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Script;
 
 /**
  * A simple File-based implementation of {@link JavaScript}.  It uses the file
  *  modification time to decide whether it has been updated or not.  
  *  Efficiency note: Every time the script is used (directly or indirectly), 
  *  this will make a call to {@link java.io.File#lastModified()}.
  * 
  * @author Marcello
  *
  */
 public class FileScript extends AbstractJavaScript {
     private File file;
     private long lastModified = 0;
     
     /**
      * Constructs a new FileScript object based on a File object.
      * @param scriptName the name of this script
      * @param file  the file
      * @param loader the loader that loaded this script
      * @throws IOException  if the file cannot be read
      */
     public FileScript(String scriptName, File file, ScriptLoader loader) throws IOException {
         super(scriptName, loader);
         
         if (!file.canRead())
             throw new IOException("Cannot read "+file);
         
         this.file = file;
     }
     
     /**
      * Reads and compiles the script
      * @param cx the javascript Context
      * @return the javascript Script object
      * @throws IOException if there was a problem reading the file
      */
     @Override
     protected Script compile(Context cx) throws IOException {
         // update last modified
         lastModified = file.lastModified();
         
         // Compile file
         FileReader reader = new FileReader(file);
        try {
            return cx.compileReader(reader, file.getPath(),1,null);
        } finally {
            reader.close();
        }
         
     }
     /**
      * Returns whether or not this file has been modified.
      * @return true if the file is modified
      * @see cello.alt.servlet.scripting.AbstractJavaScript#isModified()
      */
     @Override
     protected boolean isModified() {
         return lastModified != file.lastModified();
     }
     /**
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         return file.hashCode();
     }
     /**
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object o) {
         return o instanceof FileScript &&
                 file.equals(((FileScript)o).file);
     }
     /**
      * Returns a string representation of this object
      * @return the String representation
      */
     @Override
     public String toString() {
         return "FileScript["+file+"]";
     }
 }
