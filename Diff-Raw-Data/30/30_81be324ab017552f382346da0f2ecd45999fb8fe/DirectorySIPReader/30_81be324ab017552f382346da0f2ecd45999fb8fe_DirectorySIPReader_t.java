 package fedora.services.diringest.sip2fox;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.log4j.*;
 
 /**
  * A SIPReader based on a directory of files.
  */
 public class DirectorySIPReader implements SIPReader,
                                            DataResolver {
 
     public static final String METS_ENTRY_PATH = "METS.xml";
 
     private static final Logger _LOG =
             Logger.getLogger(DirectorySIPReader.class.getName());
 
     private File _dir;
 
     private boolean _deleteOnClose;
 
     private METSReader _metsReader;
 
     /**
      * Instantiate the reader, parsing the METS.xml and verifying
      * that the referenced files actually exist in the given directory.
      */
     public DirectorySIPReader(File dir, boolean deleteOnClose) throws Exception {
         _dir = dir;
         _deleteOnClose = deleteOnClose;
        try {
            _metsReader = new METSReader(getInputStream(METS_ENTRY_PATH), this);
        } catch (Exception e) {
            close();
            throw e;
        }
     }
 
     /**
      * Get an <code>InputStream</code> for the given filename in
      * the directory, or throw an <code>IOException</code> if not found.
      */
     private InputStream getInputStream(String path) 
             throws IOException {
         File f = new File(_dir, path);
         if (f.exists() && !f.isDirectory()) {
             return new FileInputStream(f);
         } else {
             throw new IOException("File not found in SIP: " + path);
         }
     }
 
     // Implements SIPReader.getRoot()
     public TreeNode getRoot() {
         return _metsReader.getRoot();
     }
 
     // Implements SIPReader.close()
     public void close() {
        if (_deleteOnClose && _dir != null) {
             Converter.deleteTempDir(_dir);
            _dir = null;
         }
         try {
            if (_metsReader != null) {
                _metsReader.close();
                _metsReader = null;
            }
         } catch (IOException e) {
             _LOG.warn("Could not close METS reader.", e);
         }
     }
 
     // Implements DataResolver.getData(String, String)
     public InputStream getData(String locatorType, String locator) throws IOException {
         return getInputStream(getPath(locator));
     }
 
     // Implements DataResolver.checkResolvability(String, String)
     public void checkResolvability(String locatorType, String locator) throws IOException {
         getInputStream(getPath(locator)).close();
     }
 
     /**
      * Given a file: uri, return the path it must be referring to,
      * relative to the base directory of the SIP.
      */
     private String getPath(String locator) throws IOException {
         if (!locator.startsWith("file:")) {
             throw new IOException("Bad locator syntax: " + locator);
         }
 
         // remove "file:"
         String path = locator.substring(5);
 
         // remove leading slashes
         while (path.startsWith("/")) {
             path = path.substring(1);
         }
 
         //Interpret "+" and "%20" as a space (" "), and ensure that the "/" character
         //is used as a directory seperator
         return path.replaceAll("\\+", " ").replaceAll("%20", " ").replaceAll("\\\\", "/");
     }
 
    public void finalize() {
        close();
    }

 }
