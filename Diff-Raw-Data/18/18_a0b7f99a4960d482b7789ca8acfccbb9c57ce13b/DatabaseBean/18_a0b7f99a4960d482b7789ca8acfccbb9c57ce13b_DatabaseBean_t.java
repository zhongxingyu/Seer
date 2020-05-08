 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.service.engine;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import net.wazari.dao.DatabaseFacadeLocal;
 import net.wazari.dao.DatabaseFacadeLocal.DatabaseFacadeLocalException;
 import net.wazari.dao.TagFacadeLocal;
 import net.wazari.dao.ThemeFacadeLocal;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.Tag;
 import net.wazari.dao.entity.Theme;
 import net.wazari.service.DatabaseLocal;
 import net.wazari.service.entity.util.PhotoUtil;
 import net.wazari.service.exchange.Configuration;
 import net.wazari.service.exchange.ViewSession;
 import net.wazari.service.exchange.ViewSession.Action;
 import net.wazari.service.exchange.ViewSession.Special;
 import net.wazari.service.exchange.ViewSessionDatabase;
 import net.wazari.service.exchange.xml.database.XmlDatabaseCheck;
 import net.wazari.service.exchange.xml.database.XmlDatabaseDefault;
 import net.wazari.service.exchange.xml.database.XmlDatabaseExport;
 import net.wazari.service.exchange.xml.database.XmlDatabaseImport;
 import net.wazari.service.exchange.xml.database.XmlDatabaseStats;
 import net.wazari.service.exchange.xml.database.XmlDatabaseStats.XmlDatabaseStatsTheme;
 import net.wazari.service.exchange.xml.database.XmlDatabaseTrunk;
 import net.wazari.service.exchange.xml.tag.XmlTagCloud.XmlTagCloudEntry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class DatabaseBean implements DatabaseLocal {
     private static String getPath(Configuration conf) {
         return conf.getBackupPath();
     }
     private static final Logger log = LoggerFactory.getLogger(DatabaseBean.class.toString());
     @EJB ThemeFacadeLocal themeDAO;
     @EJB TagFacadeLocal tagDAO;
     @EJB DatabaseFacadeLocal databaseDAO;
     @EJB private PhotoUtil photoUtil ;
     
     public XmlDatabaseImport treatIMPORT(ViewSession vSession) {
         XmlDatabaseImport output = new XmlDatabaseImport() ;
         try {
             databaseDAO.treatImportXML(vSession.getConfiguration().wantsProtectDB(), getPath(vSession.getConfiguration()));
             output.message = "Import OK";
         } catch (DatabaseFacadeLocal.DatabaseFacadeLocalException e) {
             output.exception = e.getMessage();
         }
         return output;
     }
 
     public XmlDatabaseExport treatEXPORT(ViewSession vSession) {
         XmlDatabaseExport output = new XmlDatabaseExport() ;
         try {
             databaseDAO.treatExportXML(getPath(vSession.getConfiguration()));
             output.message = "Export OK";
         } catch (DatabaseFacadeLocal.DatabaseFacadeLocalException e) {
             output.exception = e.getMessage();
         }
         return output;
     }
 
     public XmlDatabaseCheck treatCHECK(ViewSession vSession) {
         XmlDatabaseCheck output = new XmlDatabaseCheck() ;
         
         Action action = vSession.getAction() ;
         if (action == null)
             return output;
         
         try {
            Theme enrTheme = themeDAO.find(vSession.getTheme().getId());
             if (enrTheme == null)
                 throw new DatabaseFacadeLocalException("Theme not set");
             if (vSession.getConfiguration().isPathURL()) {
                 throw new DatabaseFacadeLocalException("URL path checking not implemented yet");
             }
             List<Theme> themes = new LinkedList<Theme>();
             if (vSession.isRootSession())
                 //Hibernate exception if direct
                 themes.addAll(themeDAO.findAll());
             else
                 themes.add(enrTheme);
             
             int count = 0;
             for (Theme curEnrTheme : themes) {
                 if (curEnrTheme.getId() == ThemeFacadeLocal.THEME_ROOT_ID)
                     continue ;
                 List<String> images = null;
                 List<String> mini = null;
                 
                 if (action == Action.CHECK_FS) {
                     images = new LinkedList<String>();
                     mini = new LinkedList<String>();
                     String sep = vSession.getConfiguration().getSep() ;
                     List<String> current = images ;
                     String pictpath = vSession.getConfiguration().getImagesPath() ;
                     
                     while (current != null) {    
                         File themeDir = new File(pictpath + sep + curEnrTheme.getNom());
                         for (File year : themeDir.listFiles()) {
                             for (File albums : year.listFiles()) {
                                 for (File image : albums.listFiles()) {
                                     if (image.getName().endsWith(".gpx"))
                                         continue ;
                                     current.add(image.getAbsolutePath());
                                 }
                             }
                         }
                         
                         if (current == images) {
                             current = mini; 
                             pictpath = vSession.getConfiguration().getMiniPath() ;
                         } else {
                             current = null;
                         }
                     }
                 }
                 for (Album enrAlbum : curEnrTheme.getAlbumList()) {
                     log.warn("checking album: {}", enrAlbum.getNom());
                     for (Photo enrPhoto : enrAlbum.getPhotoList()) {
                         count++;
                         List<String> current = images ;
                         for (String filepath : new String[]{photoUtil.getImagePath(vSession, enrPhoto), photoUtil.getMiniPath(vSession, enrPhoto)}) {
                             if (action == Action.CHECK_DB) {
                                 File f = new File(filepath);
                                 if (!f.exists())
                                     output.files.add(filepath+": missing");
                                 else if (!f.canRead())
                                     output.files.add(filepath+": not readable");
                             } else if (action == Action.CHECK_FS) {
                                 
                                 boolean removed = current.remove(filepath);
                                 log.info("checking: {}", filepath);
                                 log.info("found ^: {}", removed);
                                 current = mini ;
                             }
                         }
                     }
                 }
                 if (action == Action.CHECK_FS) {
                     output.files.addAll(images);
                     output.files.addAll(mini);
                 }
             }
             
             output.message = "Check OK. "+count+" photos checked, "+output.files.size() + " problems";
             log.warn(output.message);
         } catch (DatabaseFacadeLocal.DatabaseFacadeLocalException e) {
             output.exception = e.getMessage();
         }
         return output;
     }
 
     public XmlDatabaseTrunk treatTRUNK(ViewSession vSession) {
         XmlDatabaseTrunk output = new XmlDatabaseTrunk() ;
         try {
             databaseDAO.treatTruncateDB(vSession.getConfiguration().wantsProtectDB());
             output.message = "Trunk OK";
         } catch (DatabaseFacadeLocal.DatabaseFacadeLocalException e) {
             output.exception = e.getMessage();
         }
         return output;
     }
 
     public XmlDatabaseStats treatSTATS(ViewSessionDatabase vSession) {
         XmlDatabaseStats output = new XmlDatabaseStats() ;
         Theme enrTheme = vSession.getTheme();
         if (enrTheme == null) {
             output.exception = "theme not set";
             return output;
         }
         List<Theme> themes = new LinkedList<Theme>();
         XmlDatabaseStatsTheme xmlRootTheme = null;
         if (!vSession.isRootSession()) {
             //Hibernate exception if direct
             themes.add(themeDAO.find(enrTheme.getId()));
         } else {
             themes.addAll(themeDAO.findAll());
             xmlRootTheme = new XmlDatabaseStatsTheme("root");
         }
         for (Theme curEnrTheme : themes) {
             if (curEnrTheme.getId() == 1)
                 continue;
             XmlDatabaseStatsTheme xmlTheme = new XmlDatabaseStatsTheme(curEnrTheme.getNom());
             output.theme.add(xmlTheme);
             
             for (Album enrAlbum : curEnrTheme.getAlbumList()) {
                 int photosize = enrAlbum.getPhotoList().size();
                 xmlTheme.albums++;
                 xmlTheme.photos += photosize;
                 if (xmlRootTheme != null) {
                     xmlRootTheme.albums++;
                     xmlRootTheme.photos += photosize;
                 }
             }
             if (xmlRootTheme != null) {
                 vSession.setRootSession(Boolean.FALSE);
                 vSession.setTheme(curEnrTheme);
             }
             xmlTheme.tags = tagDAO.loadVisibleTags(vSession, false).size();
             if (xmlRootTheme != null) {
                 vSession.setRootSession(Boolean.TRUE);
                 vSession.setTheme(enrTheme);
             }
             if (xmlRootTheme == null || xmlRootTheme.tag == null) {
                 Map<Tag, Long> map = tagDAO.queryIDNameCount(vSession);
                 List<XmlTagCloudEntry> lst = new ArrayList<XmlTagCloudEntry>(map.size());
                 for (Tag enrTag : map.keySet()) {
                     XmlTagCloudEntry xmlCloudEntry = new XmlTagCloudEntry();
                     xmlCloudEntry.name = enrTag.getNom();
                     xmlCloudEntry.nb = map.get(enrTag);
                     lst.add(xmlCloudEntry);
                 }
                 if (xmlRootTheme == null)
                     xmlTheme.tag = lst;
                 else
                     xmlRootTheme.tag = lst;
             }
         }
         
         if (xmlRootTheme != null)  {
             xmlRootTheme.tags = tagDAO.findAll().size();
             output.theme.add(xmlRootTheme);
         }
         return output;
     }
     
     public XmlDatabaseDefault treatDEFAULT(ViewSessionDatabase vSession) {
         XmlDatabaseDefault output = new XmlDatabaseDefault() ;
         
         return output;
     }
     
 }
