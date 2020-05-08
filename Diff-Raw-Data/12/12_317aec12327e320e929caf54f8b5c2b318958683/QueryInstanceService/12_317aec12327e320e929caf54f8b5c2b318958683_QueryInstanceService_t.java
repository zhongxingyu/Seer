 package gov.nih.nci.cagrid.portal.service;
 
 import gov.nih.nci.cagrid.portal.dao.QueryInstanceDao;
 import gov.nih.nci.cagrid.portal.domain.dataservice.QueryInstance;
 import gov.nih.nci.cagrid.portal.domain.table.QueryResultTable;
import gov.nih.nci.cagrid.portal.util.BeanUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 @Transactional
 public class QueryInstanceService {
     private QueryInstanceDao queryInstanceDao;
     private PortalFileService portalFileService;
     private Log log = LogFactory.getLog(getClass());
 
 
     public void delete(int instanceId) {
         QueryInstance instance = getQueryInstanceDao().getById(instanceId);
         QueryResultTable table = instance.getQueryResultTable();
        String fileName = BeanUtils.traverse(table,"data.fileName");
        if (fileName!=null) {
                 log.debug("Will delete query results file");
                boolean deleted = getPortalFileService().delete(fileName);
                 if (!deleted)
                     log.warn("Could not delete file on disk. Filename " + table.getData().getFileName());
             HibernateTemplate templ = queryInstanceDao.getHibernateTemplate();
             templ.delete(table.getData());
             templ.delete(table);
         }
         getQueryInstanceDao().delete(instance);
     }
 
 
     public QueryInstanceDao getQueryInstanceDao() {
         return queryInstanceDao;
     }
 
     public void setQueryInstanceDao(QueryInstanceDao queryInstanceDao) {
         this.queryInstanceDao = queryInstanceDao;
     }
 
     public PortalFileService getPortalFileService() {
         return portalFileService;
     }
 
     public void setPortalFileService(PortalFileService portalFileService) {
         this.portalFileService = portalFileService;
     }
 }
