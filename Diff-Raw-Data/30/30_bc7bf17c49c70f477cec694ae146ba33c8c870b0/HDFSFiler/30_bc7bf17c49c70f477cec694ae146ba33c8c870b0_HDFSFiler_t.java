 package eu.scape_project.pt.util.fs;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 
 /**
  * Handles the transportation of files from the local filesystem to HDFS 
  * and vice-versa.
  * 
  * @author Rainer Schmidt [rschmidt13]
  * @author Matthias Rella [myrho]
  * @author Martin Schenck [schenck]
  */
 public class HDFSFiler extends Filer {
     
     private static Log LOG = LogFactory.getLog(HDFSFiler.class);
     
     /**
      * Hadoop Filesystem handle.
      */
     private final FileSystem hdfs;
 
     /**
      * File to handle by this filer
      */
     private final Path file;
 
     HDFSFiler(URI uri) throws IOException {
         this.file = new Path(uri);
         hdfs = file.getFileSystem(new Configuration());
     }
     
     /**
      * Copies a file or directory from the local filesystem to a remote one.
      */
     private void depositDirectoryOrFile(String strSrc, String strDest) throws IOException {
         File source = new File( strSrc );
         if(source.isDirectory()) {
             depositDirectory(strSrc, strDest);
         } else {
             depositFile(strSrc, strDest);
         }
     }
     
     /**
      * Copies a directory from the local filesystem to a remote one.
      */
     private void depositDirectory(String strSrc, String strDest) throws IOException {
         // Get output directory name from strSrc
         File localDir = new File( strSrc );
         
         if(!localDir.isDirectory()) {
             throw new IOException("Could not find correct local output directory: " + localDir );
         }
         
         LOG.debug("Local directory is: " + localDir );
         
         for(File localFile : localDir.listFiles()) {
             depositDirectoryOrFile(localFile.getCanonicalPath(), strDest + File.separator + localFile.getName());
         }
     }
 
     /**
      * Copies a file from the local filesystem to a remote one.
      */
     private void depositFile(String strSrc, String strDest) throws IOException {
         Path src = new Path(strSrc);
         Path dest = new Path(strDest);
         
         LOG.debug("local file name is: "+src+" destination path is:" +dest);
         hdfs.copyFromLocalFile(src, dest);
     }
 
     @Override
     public void localize() throws IOException {
         File fileRef = new File(getAbsoluteFileRef());
         LOG.debug("localize " + fileRef);
         new File(fileRef.getParent()).mkdirs();
         Path localfile = new Path( fileRef.toString() );
         if(hdfs.exists(file)) {
             hdfs.copyToLocalFile(file, localfile);
         }
     }
 
     @Override
     public void delocalize() throws IOException {
         this.depositDirectoryOrFile(getAbsoluteFileRef(), file.toString());
     }
 
     @Override
     public void setWorkingDir(String strDir ) {
         LOG.debug("setDirectory " + strDir );
        File dirFile = new File(strDir);
        if( !dirFile.isAbsolute() ) {
             this.dir = this.getTmpDir() + strDir;
         } else {
             this.dir = strDir;
         }
         LOG.debug("this.dir = " + this.dir );
     }
 
     @Override
     public String getAbsoluteFileRef() {
         return this.getFullDirectory();
     }
 
     @Override
     public String getRelativeFileRef() {
         String path = this.getPath();
         if( path.startsWith(System.getProperty("file.separator")) )
             path = path.substring(1);
         return path;
     }
 
     /**
      * Returns the user defined directory of the file.
      */
     private String getPath() {
         URI uri = this.file.toUri();
         
         String path = uri.getPath();
         LOG.debug("path = " + path);
         String sep = System.getProperty("file.separator");
         return path.replace(Path.SEPARATOR, sep);
     }
 
     /**
      * Returns working space directory with user defined directories.
      */
     private String getFullDirectory() {
         String par = this.getPath();
         return (this.dir.isEmpty() 
                 ? "hdfsfiler_" + file.hashCode()
                 : this.dir) 
                 + par;
     }
 
     @Override
     public InputStream getInputStream() throws IOException {
         return hdfs.open(file);
     }
 
     @Override
     public OutputStream getOutputStream() throws IOException {
         return hdfs.create(file);
     }
 
 }
