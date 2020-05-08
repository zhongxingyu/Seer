 package nl.utwente.ewi.udprelaydb;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.utwente.ewi.udprelay.XMLUtil;
 
 import org.w3c.dom.Element;
 
 /**
  * Reads the db configuration part of the config file and exposes these directives with getters.
  * 
  * @author rein
  */
 public class DbConfiguration {
 
 //  private static final String CONFIG_FILENAME = "dbcp_config.xml";
   private String dbDriverName = null;
   private String dbUser = null;
   private String dbPassword = null;
   private String dbURI = null;
   private static final Logger logger = Logger.getLogger(DbConfiguration.class.getName());
 
   public DbConfiguration(Element database) {
     try {
       dbDriverName = XMLUtil.getSubElementContents(database, "dbDriverName").trim();
       dbUser = XMLUtil.getSubElementContents(database, "dbUser").trim();
       dbPassword = XMLUtil.getSubElementContents(database, "dbPassword");
      dbURI = XMLUtil.getSubElementContents(database, "dbURI");
     } catch (Exception ex) {
     	logger.log(Level.SEVERE, ex.getMessage());
       logger.log(Level.SEVERE, "Problem with loading the db part of the config file.");
       ex.printStackTrace();
       System.exit(1);
     }
 
   }
 
   public String getDbDriverName() {
     return dbDriverName;
   }
 
   public String getDbUser() {
     return dbUser;
   }
 
   public String getDbPassword() {
     return dbPassword;
   }
 
   public String getDbURI() {
     return dbURI;
   }
 
 }
