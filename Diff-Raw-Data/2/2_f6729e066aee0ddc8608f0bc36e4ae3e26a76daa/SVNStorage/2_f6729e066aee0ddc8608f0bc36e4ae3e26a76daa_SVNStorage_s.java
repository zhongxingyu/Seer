 package org.wyona.yarep.core.impl.svn;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Date;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.log4j.Category;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.wyona.commons.io.FileUtil;
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.Storage;
 import org.wyona.yarep.core.UID;
 
 /**
  * Subversion based storage implementation.
  *  
  * Configuration parameters: 
  *   - src: URL of subversion repository (including repository path) 
  *   - workingdir: directory where the working copy of the repository will be checked out 
  *   - username: svn username 
  *   - password: svn password
  * 
  * When the storage is started, the working copy will be updated or checked out.
  * The working dir will be created automatically in case it does not exist. In
  * the current implementation, read requests don't do an update before reading for
  * performance reasons. 
  * Write requests are committed when the streams are being closed.
  * Locking is not implemented yet.
  */
 public class SVNStorage implements Storage {
 
     private static Category log = Category.getInstance(SVNStorage.class);
 
     protected SVNClient svnClient;
 
     protected SVNURL svnRepoUrl;
 
     protected File svnWorkingDir;
 
     /**
      * Reads repository configuration and checks out / updates the local working
      * copy. 
      * TODO: checkout/update should be moved to a separate init() method.
      */
     public void readConfig(Configuration storageConfig, File repoConfigFile) throws RepositoryException {
         try {
             Configuration contentConfig = storageConfig.getChild("content", false);
             svnRepoUrl = SVNURL.parseURIEncoded(contentConfig.getAttribute("src"));
             svnWorkingDir = new File(contentConfig.getAttribute("workdir"));
             if (!svnWorkingDir.isAbsolute()) {
                 svnWorkingDir = FileUtil.file(repoConfigFile.getParent(), svnWorkingDir.toString());
             }
             String username = contentConfig.getAttribute("username");
             String password = contentConfig.getAttribute("password");
 
             log.debug("SVN host URL: " + svnRepoUrl.toString());
             log.debug("SVN working dir: " + svnWorkingDir.getAbsolutePath());
 
             if (!svnWorkingDir.isDirectory()) {
                 svnWorkingDir.mkdirs();
             }
 
             svnClient = new SVNClient(username, password);
 
             // check out or update repository:
             if (svnWorkingDir.listFiles().length == 0) {
                 log.info("checking out repository " + svnRepoUrl + " to " + svnWorkingDir);
                 long rev = svnClient.checkout(svnRepoUrl, svnWorkingDir);
                 log.info("checked out revision " + rev);
             } else {
                 log.info("updating " + svnWorkingDir);
                 long rev = svnClient.update(svnWorkingDir, SVNRevision.HEAD, true);
                 svnClient.checkStatus(svnWorkingDir);
                 log.info("updated to revison " + rev);
             }
         } catch (ConfigurationException e) {
             log.error(e);
             throw new RepositoryException("Could not load repository configuration: " + repoConfigFile + ": " 
                     + e.getMessage(), e);
         } catch (SVNException e) {
             log.error(e);
             throw new RepositoryException("Could not checkout/update svn repository: " + repoConfigFile + ": " 
                     + e.getMessage(), e);
         }
     }
 
     /**
      * 
      */
     public OutputStream getOutputStream(UID uid, Path path) throws RepositoryException {
         File file = getFile(uid);
         return new SVNRepositoryOutputStream(file, svnClient);
     }
 
     /**
      * 
      */
     public InputStream getInputStream(UID uid, Path path) throws RepositoryException {
         File file = getFile(uid);
         return new SVNRepositoryInputStream(file);
     }
 
     /**
      * 
      */
     public long getLastModified(UID uid, Path path) throws RepositoryException {
         File file = getFile(uid);
         try {
             Date date = svnClient.getCommittedDate(file);
             return date.getTime();
         } catch (SVNException e) {
             log.error(e);
             throw new RepositoryException("Could not get committed date of " + file.getAbsolutePath()
                     + ": " + e.getMessage(), e);
         }
     }
 
     /**
      * 
      */
     public boolean delete(UID uid, Path path) throws RepositoryException {
         File file = getFile(uid);
         try {
             svnClient.delete(file);
             svnClient.commit(file, "yarep automated commit");
             return true;
         } catch (SVNException e) {
             log.error(e);
             //throw new RepositoryException("Could not delete " + file.getAbsolutePath()
             //        + ": " + e.getMessage(), e);
             return false;    
         }
     }
 
     /**
      * 
      */
     public String[] getRevisions(UID uid, Path path) throws RepositoryException {
         File file = getFile(uid);
         try {
             //long[] revNumbers = svnClient.getRevisionNumbers(file);
             //String[] revisions = new String[revNumbers.length];
             //for (int i=0; i<revNumbers.length; i++) revisions[i] = String.valueOf(revNumbers[i]);
             //return revisions;
             return svnClient.getRevisionStrings(file);
         } catch (SVNException e) {
             log.error(e);
             throw new RepositoryException("Could not get revisions of " + file.getAbsolutePath()
                     + ": " + e.getMessage(), e);
         }
     }
 
     /**
      * @deprecated
      */
     public Writer getWriter(UID uid, Path path) {
         return null;
     }
 
     /**
      * @deprecated
      */
     public Reader getReader(UID uid, Path path) throws NoSuchNodeException {
         return null;
     }
 
     protected File getFile(UID uid) {
         return new File(svnWorkingDir.getAbsolutePath() + File.separator + uid.toString());
     }
     
 }
