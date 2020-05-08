 package org.hackystat.sensor.ant.svn;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.dav.http.DefaultHTTPConnectionFactory;
 import org.tmatesoft.svn.core.internal.io.dav.http.IHTTPConnectionFactory;
 import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 /**
  * SVN Repository processor to extract commit information.
  * 
  * @author (Cedric) Qin ZHANG
  * @version $Id$
  */
 public class SVNCommitProcessor {
   private SVNRepository svnRepository;
 
   /**
    * Constructs this instance.
    * 
    * @param repositoryUrl The svn repository url. It can points to a
    * subdirectory in the repository.
    * @param userName The user name. Null is a valid value. If either user name
    * or password is null, then anonymous credential is used to access the svn
    * repository.
    * @param password The password. Null is a valid value.
    * 
    * @throws Exception If there is any error.
    */
   public SVNCommitProcessor(String repositoryUrl, String userName, String password)
     throws Exception {
     if (repositoryUrl.startsWith("http://") || repositoryUrl.startsWith("https://")) {
       IHTTPConnectionFactory factory =
         new DefaultHTTPConnectionFactory(null, true, null);
       DAVRepositoryFactory.setup(factory); 
     }
     else if (repositoryUrl.startsWith("svn://")) {
       SVNRepositoryFactoryImpl.setup();
     }
     else {
       throw new Exception("Repository url must start with http|https|svn.");
     }
 
     this.svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repositoryUrl));
     if (userName != null && password != null) {
       this.svnRepository.setAuthenticationManager(SVNWCUtil
           .createDefaultAuthenticationManager(userName, password));
     }
   }
 
   /**
    * Gets the largest revision number at the date specified.
    * 
    * @param date The date.
    * 
    * @return The revision number.
    * 
    * @throws Exception If there is any error.
    */
   public long getRevisionNumber(Date date) throws Exception {
     // If date is too large to too small, such as new Date(Long.MIN_VALUE)
     // or new Date(Long.MAX_VALUE), then the underlying library JavaSNV
     // SVNRepository.getDatedRevision() call
     // will either raise exception or give an erronous version number.
     // We try to avoid the error here.
 
     long startLogTime = Long.MAX_VALUE;
     long endLogTime = Long.MIN_VALUE;
     SVNLogEntry startLog = null;
     SVNLogEntry endLog = null;
 
     long latestRevision = this.svnRepository.getLatestRevision();
     Collection<?> svnLogEntries = this.svnRepository.log(new String[] { "" }, null, 1,
         latestRevision, true, true);
     for (Object entry : svnLogEntries) {
       SVNLogEntry log = (SVNLogEntry) entry;
       long time = log.getDate().getTime();
       if (time < startLogTime) {
         startLogTime = time;
         startLog = log;
       }
       if (time > endLogTime) {
         endLogTime = time;
         endLog = log;
       }
     }
 
     if (startLog == null && endLog == null) {
       // we cannot determine revision time bounds, give JavaSVN a shot
       return this.svnRepository.getDatedRevision(date);
     }
     else {
       long targetTime = date.getTime();
       if (targetTime < startLogTime) {
         return 0;
       }
       else if (endLogTime < targetTime) {
         return latestRevision;
       }
       else {
         return this.svnRepository.getDatedRevision(date);
       }
     }
   }
 
   /**
    * Gets commit record for a specified revision. Note that if the repository
    * url supplied in the constructor points to a subdirectory of the repository
    * root or a file, then there might not be any commits for the revision. In
    * this case, null is returned.
    * 
    * @param revision The revision number.
    * 
    * @return The commit record or null.
    * 
    * @throws Exception If there is any error.
    */
   public CommitRecord getCommitRecord(long revision) throws Exception {
     Collection<?> svnLogEntries = this.svnRepository.log(new String[] { "" }, null, revision,
         revision, true, true);
 
     // since startRevision and endRevision are the same, we should have at most
     // 1 svn log entry.
     if (svnLogEntries == null || svnLogEntries.isEmpty()) {
       return null;
     }
     else if (svnLogEntries.size() > 1) {
       throw new RuntimeException("Assertion failure. JavaSVN client error?");
     }
     else { // exactly 1 svn log entry
       SVNLogEntry logEntry = (SVNLogEntry) svnLogEntries.iterator().next();
       return new CommitRecord(this.svnRepository, logEntry);
     }
   }
 }
