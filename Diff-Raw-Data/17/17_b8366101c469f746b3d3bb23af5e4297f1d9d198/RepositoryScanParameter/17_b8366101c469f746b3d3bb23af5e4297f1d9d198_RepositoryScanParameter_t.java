 package com.soebes.multithreading.cp.supose.scan;
 
 import java.io.File;
 import java.net.URL;
 
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 
 public class RepositoryScanParameter {
 
     private ISVNAuthenticationManager authenticationManager;
     
     private URL url;
     
     private File indexDirectory;
 
     /**
      * @param authenticationManager The authentication manager which is used to authenticate.
     * @param url
     *            The access URL to the repository either <code>file:///</code>, <code>http://</code>,
      *            <code>https://</code> or <code>svn://</code>.
      * 
      * @param indexDirectory
      *            The directory of the index which will be created.
      */
     public RepositoryScanParameter(ISVNAuthenticationManager authenticationManager, URL url, File indexDirectory) {
         super();
         this.authenticationManager = authenticationManager;
         this.url = url;
         this.indexDirectory = indexDirectory;
     }
 
     public ISVNAuthenticationManager getAuthenticationManager() {
         return authenticationManager;
     }
 
     public void setAuthenticationManager(ISVNAuthenticationManager authenticationManager) {
         this.authenticationManager = authenticationManager;
     }
 
     public URL getUrl() {
         return url;
     }
 
     public void setUrl(URL url) {
         this.url = url;
     }
 
     public File getIndexDirectory() {
         return indexDirectory;
     }
 
     public void setIndexDirectory(File indexDirectory) {
         this.indexDirectory = indexDirectory;
     }
 }
