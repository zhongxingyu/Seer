 package org.synyx.opencms.solr.indexing;
 
 import org.opencms.file.CmsObject;
 import org.opencms.search.CmsIndexException;
 import org.opencms.search.CmsIndexingThreadManager;
 import org.opencms.search.CmsSearchIndex;
 import org.opencms.search.CmsVfsIndexer;
 import org.opencms.search.I_CmsIndexWriter;
 import org.opencms.search.I_CmsIndexer;
 import org.opencms.search.Messages;
 import org.opencms.report.I_CmsReport;
 import org.opencms.main.CmsLog;
 import org.opencms.file.CmsResource;
 import org.apache.commons.logging.Log;
 
 public class ExpirationUnawareCmsVfsIndexer extends CmsVfsIndexer {
 
     private static final Log LOG = CmsLog.getLog(CmsVfsIndexer.class);
 
     @Override
     public I_CmsIndexer newInstance(CmsObject cms, I_CmsReport report, CmsSearchIndex index) {
         ExpirationUnawareCmsVfsIndexer indexer = new ExpirationUnawareCmsVfsIndexer();
         indexer.m_cms = cms;
         indexer.m_report = report;
         indexer.m_index = index;
         return indexer;
     }
 
     @Override
     protected void updateResource(I_CmsIndexWriter writer, CmsIndexingThreadManager threadManager, CmsResource resource)
             throws CmsIndexException {
         if (resource.isInternal() || resource.isFolder()) {
             // don't index internal resources or folders
             return;
         }
         // no check for folder resources, this must be taken care of before calling this method
         try {
 
             if (m_report != null) {
                 m_report.print(org.opencms.report.Messages.get().container(
                         org.opencms.report.Messages.RPT_SUCCESSION_1,
                         String.valueOf(threadManager.getCounter() + 1)), I_CmsReport.FORMAT_NOTE);
                 m_report.print(
                         Messages.get().container(Messages.RPT_SEARCH_INDEXING_FILE_BEGIN_0),
                         I_CmsReport.FORMAT_NOTE);
                 m_report.print(org.opencms.report.Messages.get().container(
                         org.opencms.report.Messages.RPT_ARGUMENT_1,
                         m_report.removeSiteRoot(resource.getRootPath())));
                 m_report.print(
                         org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                         I_CmsReport.FORMAT_DEFAULT);
             }
 
             threadManager.createIndexingThread(m_cms, writer, resource, m_index, m_report);
 
         } catch (Exception e) {
 
             if (m_report != null) {
                 m_report.println(
                         Messages.get().container(Messages.RPT_SEARCH_INDEXING_FAILED_0),
                         I_CmsReport.FORMAT_WARNING);
             }
             if (LOG.isWarnEnabled()) {
                 LOG.warn(Messages.get().getBundle().key(
                         Messages.ERR_INDEX_RESOURCE_FAILED_2,
                         resource.getRootPath(),
                         m_index.getName()), e);
             }
             throw new CmsIndexException(Messages.get().container(
                     Messages.ERR_INDEX_RESOURCE_FAILED_2,
                     resource.getRootPath(),
                     m_index.getName()));
         }
     }
 }
