 package org.wyona.yarep.core.impl.svn;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import org.apache.log4j.Category;
 
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.wc.SVNStatusType;
//import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.RepositoryException;
 
 /**
 * TODO: add working property support (see commented out code)!
  */
 public class SVNRepositoryOutputStream extends OutputStream {
 
     private static Category log = Category.getInstance(SVNRepositoryOutputStream.class);
 
     private OutputStream out;
 
     private SVNClient svnClient;
 
     private File file;
 
    //private Property[] properties;

     /**
      * 
      */
     public SVNRepositoryOutputStream(final File file, SVNClient svnClient) throws RepositoryException {
    /*    this(file, null, svnClient);
    }

    public SVNRepositoryOutputStream(File file, Property[] properties, SVNClient svnClient) throws RepositoryException {*/
         this.svnClient = svnClient;
         try {
             this.file = file;
 
             File parent = new File(file.getParent());
             if (!parent.exists()) {
                 log.warn("Directory will be created: " + parent);
                 // parent.mkdirs();
                 svnClient.addDirectory(parent); // parent dirs will be created and added (svn add)
             }
 
             log.debug(file.toString());
             if (file.isDirectory()) {
                 out = new OutputStream() {
                     @Override
                     public void write(int arg0) throws IOException {
                         throw new IOException("Cannot write stream data to directory: " + file);
                     }
                 };
                 return;
             }
             out = new FileOutputStream(file);
         } catch (Exception e) {
             log.error(e);
             throw new RepositoryException(e.getMessage(), e);
         }
        //writer.println(properties[i].getName() + "<" + org.wyona.yarep.core.PropertyType.getTypeName(properties[i].getType()) + ">:" + properties[i].getValueAsString());
        //this.properties = properties;
     }
 
     /**
      * 
      */
     public void write(int b) throws IOException {
         out.write(b);
     }
 
     /**
      * 
      */
     public void close() throws IOException {
         out.close();
         save();
     }
 
     private void save() throws IOException {
         try {
            //svnClient.setProperties(file, properties);

             SVNStatusType status = svnClient.getStatus(file);
 
             if (status == SVNStatusType.STATUS_UNVERSIONED) {
                 log.debug("adding new file: " + file.getAbsolutePath());
                 svnClient.addFile(file);
                 ArrayList<File> pathsList = getPathsToCommit(file);
                 File[] paths = new File[pathsList.size()];
                 paths = pathsList.toArray(paths);
                 svnClient.commit(paths, "yarep automated commit");
             } else if (status == SVNStatusType.STATUS_ADDED) {
                 ArrayList<File> pathsList = getPathsToCommit(file);
                 File[] paths = new File[pathsList.size()];
                 paths = pathsList.toArray(paths);
                 svnClient.commit(paths, "yarep automated commit");
             } else if (status == SVNStatusType.STATUS_NORMAL) {
                 // nothing to do, file has already been added and committed
             } else if (status == SVNStatusType.STATUS_MODIFIED) {
                 log.debug("checking in modified file: " + file.getAbsolutePath());
                 svnClient.commit(file, "yarep automated commit");
             } else {
                 throw new IOException("Invalid status of " + file.getAbsolutePath() + ": " 
                         + status);
             }
         } catch (SVNException e) {
             log.error(e);
             throw new IOException("could not commit file " + file.getAbsolutePath());
         }
     }
 
     private ArrayList<File> getPathsToCommit(File file) throws SVNException {
         SVNStatusType status = svnClient.getStatus(file);
         if (status == SVNStatusType.STATUS_ADDED) {
             File parent = file.getParentFile();
             ArrayList<File> list = getPathsToCommit(parent);
             list.add(file);
             return list;
         }
         return new ArrayList<File>();
 
     }
 }
