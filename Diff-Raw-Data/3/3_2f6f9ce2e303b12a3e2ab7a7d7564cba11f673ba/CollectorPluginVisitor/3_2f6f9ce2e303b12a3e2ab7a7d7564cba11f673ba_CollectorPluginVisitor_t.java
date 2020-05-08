 package org.wickedsource.hooked.svn;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
 import org.wickedsource.hooked.plugins.api.collector.CollectorPlugin;
 import org.wickedsource.hooked.plugins.api.collector.CommittedFile;
 import org.wickedsource.hooked.plugins.api.collector.FileMetaData;
 import org.wickedsource.hooked.plugins.api.collector.FileType;
 import org.wickedsource.hooked.plugins.api.notifier.FileMetrics;
 import org.wickedsource.hooked.svn.data.SvnCommitData;
 import org.wickedsource.hooked.svn.data.SvnFileMetaData;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Tom Hombergs <tom.hombergs@gmail.com>
  */
 public class CollectorPluginVisitor {
 
     private static Logger logger = LoggerFactory.getLogger(CollectorPluginVisitor.class);
 
     private final SvnCommitData data;
 
     private final String repositoryRoot;
 
     private final Long revision;
 
     private FileMetrics metrics = new FileMetrics();
 
     public CollectorPluginVisitor(SvnCommitData data, String repositoryRoot, Long revision) {
         this.data = data;
         this.repositoryRoot = repositoryRoot;
         this.revision = revision;
     }
 
     public FileMetrics visitCollectorPlugins() {
         try {
             SVNLookClient lookClient = SVNKitUtil.createSVNLookClient();
             List<CommittedFile> committedFiles = new ArrayList<>();
             for (SvnFileMetaData svnFile : data.getSvnFilesMetaData()) {
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 if(svnFile.getFileType() == FileType.FILE){
                     lookClient.doCat(new File(repositoryRoot), svnFile.getFilePath(), SVNRevision.create(revision), out);
                 }
                 committedFiles.add(new CommittedFile(mapFileMetaData(svnFile), out.toByteArray()));
             }
             visitCollectorPlugins(committedFiles);
             return metrics;
         } catch (SVNException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void visitCollectorPlugins(List<CommittedFile> committedFiles) {
         Set<CollectorPlugin> plugins = PluginRegistry.INSTANCE.getCollectorPlugins();
         for (CollectorPlugin plugin : plugins) {
             try {
                 metrics.join(plugin.analyzeCommittedFiles(committedFiles));
             } catch (Exception e) {
                logger.error("Collector" +
                        " plugin %s could did not execute normally. Skipped plugin execution.",
                         plugin.getClass());
             }
         }
     }
 
     private FileMetaData mapFileMetaData(SvnFileMetaData svnFile) {
         FileMetaData file = new FileMetaData();
         file.setFileType(svnFile.getFileType());
         file.setModificationType(svnFile.getModificationType());
         file.setPath(svnFile.getFilePath());
         return file;
     }
 
 }
