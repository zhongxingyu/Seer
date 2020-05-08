 package uk.org.sappho.code.heatmap.scm;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.tigris.subversion.javahl.ChangePath;
 import org.tigris.subversion.javahl.ClientException;
 import org.tigris.subversion.javahl.Info2;
 import org.tigris.subversion.javahl.LogMessageCallback;
 import org.tigris.subversion.javahl.NodeKind;
 import org.tigris.subversion.javahl.Revision;
 import org.tigris.subversion.javahl.RevisionRange;
 import org.tigris.subversion.javahl.SVNClient;
 
 import com.google.inject.Inject;
 
 import uk.org.sappho.code.heatmap.config.Configuration;
 import uk.org.sappho.code.heatmap.engine.Change;
 import uk.org.sappho.code.heatmap.engine.Filename;
 import uk.org.sappho.code.heatmap.engine.HeatMapCollection;
 import uk.org.sappho.code.heatmap.issues.IssueManagement;
 import uk.org.sappho.code.heatmap.issues.IssueWrapper;
 
 public class Subversion implements SCM {
 
     private final SVNClient svnClient = new SVNClient();
     private final Configuration config;
     private final IssueManagement issueManagement;
     private static final Logger LOG = Logger.getLogger(Subversion.class);
 
     @Inject
     public Subversion(Configuration config, IssueManagement issueManagement) {
 
         LOG.info("Using Subversion SCM plugin");
         this.config = config;
         this.issueManagement = issueManagement;
     }
 
     private class SubversionRevision {
 
         private final ChangePath[] changedPaths;
         private final long revision;
         @SuppressWarnings("unchecked")
         private final Map revprops;
 
         @SuppressWarnings("unchecked")
         public SubversionRevision(ChangePath[] changedPaths, long revision, Map revprops) {
             this.changedPaths = changedPaths;
             this.revision = revision;
             this.revprops = revprops;
         }
 
         public ChangePath[] getChangedPaths() {
             return changedPaths;
         }
 
         public long getRevision() {
             return revision;
         }
 
         @SuppressWarnings("unchecked")
         public Map getRevprops() {
             return revprops;
         }
     }
 
     private class LogMessageProcessor implements LogMessageCallback {
 
         private final List<SubversionRevision> revisions;
 
         public LogMessageProcessor(List<SubversionRevision> revisions) {
 
             this.revisions = revisions;
         }
 
         @SuppressWarnings("unchecked")
         public void singleMessage(ChangePath[] changedPaths, long revision, Map revprops, boolean hasChildren) {
 
             if (revision != Revision.SVN_INVALID_REVNUM) {
                 revisions.add(new SubversionRevision(changedPaths, revision, revprops));
             }
         }
     }
 
     public void processChanges(HeatMapCollection heatMapCollection) throws SCMException {
 
         String errorMessage = "Unable to find Subversion session parameters";
         try {
             String url = config.getProperty("svn.url");
             String basePath = config.getProperty("svn.path");
             long endRevision = Long.parseLong(config.getProperty("svn.end.rev", "-1"));
             if (endRevision < 0) {
                 try {
                     Info2[] info = svnClient.info2(url + basePath, Revision.HEAD, Revision.HEAD, false);
                     endRevision = info[0].getLastChangedRev();
                     LOG.debug("Using HEAD revision because svn.end.rev property requires it");
                 } catch (ClientException e) {
                     LOG.error("Unable to determine head revision of " + url + basePath, e);
                 }
             }
             long startRevision = Long
                     .parseLong(config.getProperty("svn.start.rev", Long.toString(endRevision - 49)));
             errorMessage = "Unable to read Subversion history for " + url + basePath + " from rev. " + startRevision
                     + " to rev. " + endRevision;
             LOG.debug("Subversion history scan parameters:");
             LOG.debug("url:           " + url);
             LOG.debug("basePath:      " + basePath);
             LOG.debug("startRevision: " + startRevision);
             LOG.debug("endRevision:   " + endRevision);
             List<SubversionRevision> revisions = new Vector<SubversionRevision>();
             LOG.info("Reading Subversion history for " + url + basePath + " from rev. " + startRevision
                     + " to rev. " + endRevision);
             RevisionRange[] revisionRange = new RevisionRange[] { new RevisionRange(
                     Revision.getInstance(startRevision),
                     Revision.getInstance(endRevision)) };
             String[] revProps = new String[] { "svn:log" };
             svnClient.logMessages(url + basePath, Revision.getInstance(endRevision), revisionRange,
                    false, true, true, revProps, 0, new LogMessageProcessor(revisions));
             LOG.info("Processing Subversion history");
             for (SubversionRevision revision : revisions) {
                 String commitComment = (String) revision.getRevprops().get("svn:log");
                 LOG.debug("Processing rev. " + revision.getRevision() + " " + commitComment);
                 List<Filename> changedFiles = new Vector<Filename>();
                 for (ChangePath changePath : revision.getChangedPaths()) {
                     String filename = changePath.getPath();
                     try {
                         Revision revisionId = Revision.getInstance(revision.getRevision());
                         Info2[] info = svnClient.info2(url + filename, revisionId, revisionId, false);
                         if (info.length == 1 && info[0].getKind() == NodeKind.file) {
                             LOG.debug("Processing changed file " + filename);
                             changedFiles.add(new Filename(filename));
                         } else {
                             LOG.debug("Presuming " + filename + " is a directory");
                         }
                     } catch (ClientException e) {
                         LOG.debug("Unable to determine type of " + filename + " so presuming it deleted");
                     }
                 }
                 IssueWrapper issue = issueManagement.getIssue(commitComment);
                 if (issue != null) {
                     heatMapCollection.update(new Change(Long.toString(revision.getRevision()), commitComment, issue,
                             changedFiles));
                 } else {
                     LOG.debug("No issue found in commit comment: " + commitComment);
                 }
             }
         } catch (Throwable t) {
             throw new SCMException(errorMessage, t);
         }
     }
 }
