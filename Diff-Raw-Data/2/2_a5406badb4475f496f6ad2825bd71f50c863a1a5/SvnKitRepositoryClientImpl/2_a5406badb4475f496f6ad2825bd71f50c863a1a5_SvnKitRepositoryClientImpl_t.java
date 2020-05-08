 package ch.yarb.service.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import org.springframework.stereotype.Service;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNLogEntryPath;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNDiffClient;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 import ch.yarb.api.to.ChangeType;
 import ch.yarb.api.to.ChangedPath;
 import ch.yarb.api.to.LogEntry;
 import ch.yarb.api.to.RepoConfiguration;
 import ch.yarb.api.to.RevisionRange;
 
 import static org.tmatesoft.svn.core.SVNURL.parseURIEncoded;
 
 /**
  * Repository client implementation for Subversion using theSvnKit library.
  *
  * @author pellaton
  */
 @Service
 public class SvnKitRepositoryClientImpl implements RepositoryClient {
 
   /**
    * Default constructor.
    */
   SvnKitRepositoryClientImpl() {
     // initialize the support for the svn and svn+ssh protocols
     SVNRepositoryFactoryImpl.setup();
     // initialize the support for the http and https protocols
     DAVRepositoryFactory.setup();
     // initialize the support for the local file access
     FSRepositoryFactory.setup();
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public List<LogEntry> getRepositoryLog(RepoConfiguration repoConfiguration, RevisionRange revisionRange,
       String... paths) {
     List<LogEntry> logEntryList = new ArrayList<LogEntry>();
     try {
       SVNRepository repository = SVNRepositoryFactory.create(parseURIEncoded(repoConfiguration.getRepoUrl()));
       ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(
           repoConfiguration.getUserName(), repoConfiguration.getPassword());
       repository.setAuthenticationManager(authManager);
 
       Collection<?> logEntries = repository.log(paths, null, revisionRange.getLowerBound(),
           revisionRange.getUpperBound(), true, true);
       for (Iterator<?> entries = logEntries.iterator(); entries.hasNext();) {
         SVNLogEntry logEntry = (SVNLogEntry) entries.next();
         logEntryList.add(new LogEntry(
             Long.toString(logEntry.getRevision()), logEntry.getDate(), logEntry.getAuthor(),
             logEntry.getMessage(), getChangedPathsList(logEntry.getChangedPaths())));
       }
     } catch (SVNException e) {
       e.printStackTrace();
     }
 
     return logEntryList;
   }
 
   private List<ChangedPath> getChangedPathsList(Map<?, ?> map) {
     List<ChangedPath> changedPathList = new ArrayList<ChangedPath>();
     for (Object path : new TreeSet<Object>(map.keySet())) {
       SVNLogEntryPath changedPath = (SVNLogEntryPath) map.get(path);
       changedPathList.add(new ChangedPath(changedPath.getPath(), decodeChangeType(changedPath.getType())));
     }
     return changedPathList;
   }
 
   private ChangeType decodeChangeType(char type) {
     switch (type) {
       case 'A' : return ChangeType.ADDED;
       case 'D' : return ChangeType.DELETED;
       case 'R' : return ChangeType.REPLACED;
       case 'M' : return ChangeType.MODIFIED;
       default: throw new IllegalArgumentException("unknown change type '" + type + "'");
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public List<String> getDiff(RepoConfiguration repoConfiguration, RevisionRange revisionRange, String path) {
     try {
       SVNURL fileURL = SVNURL.parseURIEncoded(repoConfiguration.getRepoUrl() + path);
       SVNRevision fromRevision = SVNRevision.create(revisionRange.getLowerBound().longValue());
       SVNRevision toRevision = SVNRevision.create(revisionRange.getUpperBound().longValue());
 
       ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(
           repoConfiguration.getUserName(), repoConfiguration.getPassword());
 
       SVNClientManager clientManager = SVNClientManager.newInstance();
       clientManager.setAuthenticationManager(authManager);
 
       SVNDiffClient diffClient = clientManager.getDiffClient();
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       diffClient.doDiff(fileURL, fromRevision, fileURL, toRevision, SVNDepth.INFINITY, false, outputStream);
       return simplifyWhenEmpty(Arrays.asList(outputStream.toString().split("\\n")));
     } catch (SVNException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   private List<String> simplifyWhenEmpty(List<String> diffList) {
     // If there is no diff between revisions, svnkit returns an empty string.
     // In that case, we prefer returning an empty list instead of a list containing a single empty string.
    if (diffList.size() == 1 && "".equals(diffList.get(0))) {
       return Collections.emptyList();
     }
     return diffList;
   }
 
 }
 
