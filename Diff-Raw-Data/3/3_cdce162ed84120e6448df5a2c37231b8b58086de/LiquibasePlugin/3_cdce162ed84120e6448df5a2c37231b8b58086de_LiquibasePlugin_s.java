 package play.modules.liquibase;
 
import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import liquibase.ClassLoaderFileOpener;
 import liquibase.Liquibase;
 import liquibase.exception.LiquibaseException;
 import play.Logger;
 import play.Play;
 import play.PlayPlugin;
 import play.db.DB;
 import play.utils.Properties;
 
 public class LiquibasePlugin extends PlayPlugin {
 
 	@Override
 	public void onApplicationStart() {
 		
 		String autoupdate = Play.configuration.getProperty("liquibase.active");
 		String mainchangelogpath = Play.configuration.getProperty("liquibase.changelog", "mainchangelog.xml");
 		String propertiespath = Play.configuration.getProperty("liquibase.properties", "liquibase.properties");
 		String contexts = Play.configuration.getProperty("liquibase.contexts");
		Boolean keepdump = Boolean.valueOf(Play.configuration.getProperty("liquibase.keepfile"));
 		
 		if (null != autoupdate && "true".equals(autoupdate)) {
 			Logger.info("Auto update flag found and positive => let's get on with changelog update");
 			try {
 				Connection cnx = DB.datasource.getConnection();
 				cnx.setAutoCommit(false);
 				Liquibase liquibase = new Liquibase(mainchangelogpath, new ClassLoaderFileOpener(), cnx);
 				InputStream stream = Play.classloader.getResourceAsStream(propertiespath);
 				
 				if (null != stream) {
 					Properties props = new Properties();
 					props.load(stream);
 					
 					for (String key:props.keySet()) {
 						String val = props.get(key);
 						Logger.info("found parameter [%1$s] / [%2$s] for liquibase update", key, val);
 						liquibase.setChangeLogParameterValue(key, val);
 					}
 				} else {
 					Logger.warn("Could not find properties file [%s]", propertiespath);
 				}
 				
 				Logger.info("Ready for database diff generation");
 				liquibase.changeLogSync(contexts);
 				Logger.info("Changelog Execution performed");
 			} catch (SQLException sqle) {
 				throw new LiquibaseUpdateException(sqle.getMessage());
 			} catch (LiquibaseException e) { 
 				throw new LiquibaseUpdateException(e.getMessage());
 			} catch (IOException ioe) {
 				throw new LiquibaseUpdateException(ioe.getMessage());				
 			}
 		} else {
 			Logger.info("Auto update flag set to false or not available => skipping structural update");
 		}	}
 }
