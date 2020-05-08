 package cello.alt.servlet.scripting;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Script;
 
 /**
  * A ScriptLoader object that uses a compressed archive (.jar/.zip) to look for
  *  resources.  The current implementation opens and closes the file every time
  *  it is needed (once for each file).  This allows for hot-swapping of 
  *  compressed files, at the expense of overhead.  Once the scripts are loaded
  *  and compiled, the file will not be accessed unless it is modified.  This 
  *  could be improved by a time-lapse closing (potentially all the necessary
  *  files will be loaded within a certain timeframe of each other).
  * 
  * @author Marcello
  */
 
 public class JarScriptLoader extends ScriptLoader {
     
     private File file;
     private ZipFile zipFile;
     
     private long lastModified = 0;
 
     /**
      * Constructs a new JarScriptLoader
      * @param file
      * @throws IOException
      */
     public JarScriptLoader(File file) throws IOException {
         this(null,file);
     }
     /**
      * Constructs a new JarScriptLoader with a parent ScriptLoader
      * @param parent parent ScriptLoader
      * @param file
      * @throws IOException
      */
     public JarScriptLoader(ScriptLoader parent, File file) throws IOException {
         super(parent);
         this.file = file;
         openZip();
     }
     
     /**
      * Loads the zip file for reading if it needs to be.
      * @throws IOException
      */
     private void openZip() throws IOException {
         if (zipFile!=null && !isModified()) return;
         lastModified = file.lastModified();
         this.zipFile = new ZipFile(file);
     }
     /**
      * Closes the zip file from reading (releases locks and whatnot).
      * @throws IOException
      */
     private void closeZip() throws IOException {
         if (zipFile!=null)
             zipFile.close();
         zipFile = null;
     }
     
     /**
      * Returns whether ot not the file has been modified.
      * @return  true if the file was modified
      */
     private boolean isModified() {
         return lastModified != file.lastModified();
     }
 
     /**
      * Returns a specified script from the jar/zip file.
      * @param name the name of the script
      * @return the JavaScript associated with this path
      * @throws ScriptNotFoundException if the script does not exist or cannot be 
      *  loaded
      */
     @Override
     public JavaScript findScript(String name) throws ScriptNotFoundException {
         try {
             String path = getPath(name);
             openZip();
             ZipEntry ze = zipFile.getEntry(path);
             if (ze == null)
                 throw new ScriptNotFoundException("Cannot find ZipEntry");
             return new JarScript(name, path);
         } catch (IOException ex) {
             throw new ScriptNotFoundException("Cannot load script "+name, ex);
         }
     }
     
     
 
     /**
      * @see cello.alt.servlet.scripting.ScriptLoader#getResource(java.lang.String)
      */
     @Override
     public URL getResource(String name) {
         // TODO implement JarScriptLoader.getResource
         return null;
     }
 
     /**
      * Returns a string representation of this JarScriptLoader.
      * @return the string representation
      */
     @Override
     public String toString() {
         return "JarScriptLoader["+file+"]";
     }
 
     /**
      * A JavaScript wrapper for this JarScriptLoader.
      *   
      * @author Marcello
      *
      */
     private class JarScript extends AbstractJavaScript {
         private String path;
         private long lastModified = 0;
         
         /**
          * Constructs a new JarScript with a given path
          * @param scriptName the name of this script
          * @param path
          */
         private JarScript(String scriptName, String path) {
             super(scriptName, JarScriptLoader.this);
             this.path = path;
         }
         /**
          * Evaluates the script from within the jar file.
          * @param cx the javascript Context
          * @return the compiled Script
          * @throws IOException if there was an error reading the zip
          */
         @Override
         protected Script compile(Context cx) throws IOException {
             // update last modified
             openZip();
             lastModified = JarScriptLoader.this.lastModified;
             
             // evaluate
            try {
                InputStream is = zipFile.getInputStream(zipFile.getEntry(path));
                return cx.compileReader(new InputStreamReader(is),file.getPath()+"!"+path,1,null);
            } finally {
                closeZip();
            }
         }
 
         /**
          * Returns whether or not this JarScript has been modified.
          * @return true if the script is modified
          */
         @Override
         protected boolean isModified() {
             return lastModified != file.lastModified();
         }
         /**
          * Returns a string representation of this JarScript
          * @return the string representation
          */
         @Override
         public String toString() {
             return "JarScript["+file+"!"+path+"]";
         }
         
     }
 
 }
