 package basis.migration.service;
 
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import java.net.URL;
 import java.util.Properties;
 
 
 public class MigrationServiceImpl implements MigrationService
 {
 
     protected static Log log = ExoLogger.getLogger("basis.migration.service.MigrationServiceImpl");
     private String path;
     private String jcrpath;
     private String documentsErrorPath;
     private String documentsMigratePath;
 
     public MigrationServiceImpl() {
         log.debug("basis.migration.service.MigrationServiceImpl");
     }
 
 
     public Properties getPropsFile(String propsFileName )
     {
         Properties prop = new Properties();
         URL myURL = this.getClass().getClassLoader().getResource(propsFileName);
         try
         {
             if (myURL != null)
                 prop.load(myURL.openStream());
         }
         catch(Exception e){
         }
          return prop;
     }
 
 
     @Override
     public void MigrateAll(String BO, String BOCountPattern) {
         //To change body of implemented methods use File | Settings | File Templates.
         Properties prop = getPropsFile("conf/application.properties");
         this.path = prop.getProperty(BO+".basis.documentsPath");
         this.documentsErrorPath = prop.getProperty(BO+".basis.documentsErrorPath");
         this.jcrpath = prop.getProperty(BO+".basis.jcrPath");
         this.documentsMigratePath = prop.getProperty(BO+".basis.documentsMigratedPath");
 
 
         log.info("Starting Migration of fastdoc folder :" + path + " in jcr path :" + jcrpath );
         Session session = MigrationUtil.getSession();
 
 
         Node rootNode = null;
         try {
            rootNode = session.getRootNode().getNode(jcrpath);
 
 
        if (!rootNode.hasNode(BO))
         {
            Node boNode = rootNode.addNode(BO, "basis:basisBO");
             boNode.setProperty("basis:BOCount", "00000000");
             boNode.setProperty("basis:BODisplayRNN", false);
             session.save();
         }
 
         } catch (RepositoryException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         ;
 
         int i =1;
 
         for(String fileName : MigrationUtil.getFileList(path))
         {
 
               log.info("Migrating file : " + fileName + " index : "+ i);
 
               MigrationUtil.addBasisDocument(session, BO, path + "/" + fileName,jcrpath,documentsErrorPath,documentsMigratePath, BOCountPattern);
 
               i++;
 
         }
         session.logout();
 
         log.info("End Migration of fastdoc folder :" + path + " in jcr path :" + jcrpath );
     }
 
     @Override
     public void MigrateDocument(String BO,String fileName) throws Exception {
         //To change body of implemented methods use File | Settings | File Templates.
         log.info("Starting Migration of folder :" + path + "in jcr path :" + jcrpath );
         Session session = MigrationUtil.getSession();
 
         log.info("Migrating file : " + fileName);
         //MigrationUtil.readBasisFile(fileName);
 
     }
 }
