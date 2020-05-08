 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.dao.jpa;
 
import java.io.File;
 import java.text.Normalizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import net.wazari.dao.MaintFacadeLocal;
 import net.wazari.dao.PhotoFacadeLocal;
 import net.wazari.dao.entity.Photo;
 import org.hibernate.ejb.EntityManagerImpl;
 import org.hibernate.stat.Statistics;
 
 /**
  *
  * @author kevinpouget
  */
 @Stateless
 public class MaintDAOBean implements MaintFacadeLocal {
 
     private static final Logger log = LoggerFactory.getLogger(MaintDAOBean.class.getName());
     @PersistenceContext(unitName = WebAlbumsDAOBean.PERSISTENCE_UNIT)
     private EntityManager em;
     @EJB
     ImportExporter xml;
     @EJB
     private PhotoFacadeLocal photoDAO;
 
     @Override
     public void treatImportXML(boolean protect, final String path) {
         if (protect || WebAlbumsDAOBean.PERSISTENCE_UNIT == WebAlbumsDAOBean.PERSISTENCE_UNIT_Prod) {
             return;
         }
         xml.importXml(path);
     }
 
     @Override
     public void treatExportXML(String path) {
         xml.exportXml(path);
     }
 
     @Override
     public void treatTruncateDB(boolean protect) {
         if (protect || WebAlbumsDAOBean.PERSISTENCE_UNIT == WebAlbumsDAOBean.PERSISTENCE_UNIT_Prod) {
             return;
         }
         xml.truncateDb();
     }
 
     @Override
     public void treatFullImport(boolean protect, String path) {
         if (protect || WebAlbumsDAOBean.PERSISTENCE_UNIT == WebAlbumsDAOBean.PERSISTENCE_UNIT_Prod) {
             return;
         }
         treatTruncateDB(protect);
         treatImportXML(protect, path);
     }
     public static String sansAccents(String source) {
             return Normalizer.normalize(source, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
     }
     @Override
     public void treatUpdate() {
         int count = 0 ;
         int countsans = 0 ;
         for (Photo enrPhoto : photoDAO.findAll()) {
             String pathSansAccent = sansAccents(enrPhoto.getPath()) ;
             if (pathSansAccent.equals(enrPhoto.getPath())) {
                 countsans++ ;
                 continue ;
             }
 
             log.info("change from {} to {}", enrPhoto.getPath(), pathSansAccent) ;
             enrPhoto.setPath(pathSansAccent) ;
             photoDAO.edit(enrPhoto) ;
             count++ ;
         }
         log.info("count: {}", count);
         log.info("countsans: {}", countsans);
         em.flush();
     }
 
     
     @Override
     public void treatDumpStats() {
         Statistics stats = ((EntityManagerImpl) em.getDelegate()).getSession().getSessionFactory().getStatistics();
 
         stats.logSummary();
         for (String query : stats.getQueries()) {
             //QueryStatistics qStats = stats.getQueryStatistics(query);
             log.info(query);
             //log.log(Level.INFO, "\tgetExecutionCount {}", qStats.getExecutionCount());
             //log.log(Level.INFO, "\tgetExecutionAvgTime {}", qStats.getExecutionAvgTime());
             //log.log(Level.INFO, "\tgetExecutionMaxTime {}", qStats.getExecutionMaxTime());
             //log.log(Level.INFO, "\tgetExecutionRowCount {}", qStats.getExecutionRowCount());
         }
     }
 }
