 package play.modules.liquibase;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import liquibase.Liquibase;
 import liquibase.database.Database;
 import liquibase.database.DatabaseFactory;
 import liquibase.database.jvm.JdbcConnection;
 import liquibase.exception.DatabaseException;
 import liquibase.exception.LiquibaseException;
 import liquibase.exception.ValidationFailedException;
 import liquibase.lockservice.LockService;
 import liquibase.resource.ClassLoaderResourceAccessor;
 import liquibase.resource.FileSystemResourceAccessor;
 import liquibase.resource.ResourceAccessor;
 
 import org.hibernate.JDBCException;
 
 import play.Logger;
 import play.Play;
 import play.PlayPlugin;
 import play.db.DB;
 import play.utils.Properties;
 
 public class LiquibasePlugin extends PlayPlugin {
 
 	@Override
 	public void onApplicationStart() {
 		
 		String autoupdate = Play.configuration.getProperty("liquibase.active","false");
 		String mainchangelogpath = Play.configuration.getProperty("liquibase.changelog", "mainchangelog.xml");
 		String propertiespath = Play.configuration.getProperty("liquibase.properties", "liquibase.properties");
 		String scanner = Play.configuration.getProperty("liquibase.scanner", "jar");
 		String contexts = Play.configuration.getProperty("liquibase.contexts",null);
 		contexts = (null != contexts && !contexts.trim().isEmpty()) ? contexts : null;
 		String actions = Play.configuration.getProperty("liquibase.actions");
 		
 		if (null == actions) {
 			throw new LiquibaseUpdateException("No valid action found for liquibase operation");
 		}
 		
 		ResourceAccessor accessor = null;
 		if ("jar".equals(scanner)) {
 			accessor = new ClassLoaderResourceAccessor(Play.classloader);
 		} else if ("src".equals(scanner)) {
 			accessor = 	new FileSystemResourceAccessor(Play.applicationPath.getAbsolutePath());
 		} else {
 			throw new LiquibaseUpdateException("No valid scanner found liquibase operation " + scanner);
 		}
 		
 		List<LiquibaseAction> acts = new ArrayList<LiquibaseAction>();
 		
 		for (String action : actions.split(",")) {
 			LiquibaseAction op = LiquibaseAction.valueOf(action.toUpperCase());
 			acts.add(op);
 		}
 		
 		Database db = null;
 		
 		if (true == Boolean.valueOf(autoupdate)) {
 			
 			Logger.info("Auto update flag found and positive => let's get on with changelog update");
 			InputStream pstream = null;
 			InputStream clstream = null;
 			
 			try {
 				
 				Connection cnx = DB.datasource.getConnection();
 
 				Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(cnx));
 				
 				final Liquibase liquibase = new Liquibase(mainchangelogpath, accessor, database);
				
 				if ("jar".equals(scanner))  {
 					pstream = Play.classloader.getResourceAsStream(propertiespath);
 				} else {
 					pstream =  new FileInputStream(Play.getFile(propertiespath));
 				}
 
 				if (null != pstream) {
 					Properties props = new Properties();
 					props.load(pstream);
 					
 					for (String key:props.keySet()) {
 						String val = props.get(key);
 						Logger.info("found parameter [%1$s] / [%2$s] for liquibase update", key, val);
 						liquibase.setChangeLogParameter(key, val);
 					}
 				} else {
 					Logger.warn("Could not find properties file [%s]", propertiespath);
 				}
 				
 				db = liquibase.getDatabase();
 				for (LiquibaseAction op: acts) {
 					Logger.info("Dealing with op [%s]", op);
 					
 					switch (op) {
 						case LISTLOCKS:
 							liquibase.reportLocks(System.out);
 							break;
 						case RELEASELOCKS :
 							LockService.getInstance(db).forceReleaseLock();
 							break;
 						case SYNC :
 							liquibase.changeLogSync(contexts);					
 							break;
 						case STATUS:
 							File tmp = Play.tmpDir.createTempFile("liquibase", ".status");
 							liquibase.reportStatus(true, contexts, new FileWriter(tmp));
 							Logger.info("status dumped into file [%s]", tmp);
 							break;
 						case UPDATE:
 							liquibase.update(contexts);
 							break;
 						case CLEARCHECKSUMS:
 							liquibase.clearCheckSums();
 							break;
 						case VALIDATE:
 							try {
 			                    liquibase.validate();
 			                } catch (ValidationFailedException e) {
 			                    Logger.error(e,"liquibase validation");
 			                }
 						default:
 							break;
 					}
 					Logger.info("op [%s] performed",op);
 				}
 			} catch (SQLException sqe) { 
 				throw new LiquibaseUpdateException(sqe.getMessage());				
 			} catch (LiquibaseException e) { 
 				throw new LiquibaseUpdateException(e.getMessage());
 			} catch (IOException ioe) {
 				throw new LiquibaseUpdateException(ioe.getMessage());				
 			} finally {
 				if (null != db) {
 					try {						
 						db.close();
 					} catch (DatabaseException e) {
 						Logger.warn(e,"problem closing connection");
 					} catch (JDBCException jdbce) {
 						Logger.warn(jdbce,"problem closing connection");
 					}
 				}
 				if (null != pstream) {
 					try {
 						pstream.close();
 					} catch (Exception e) {
 						
 					}
 				}
 				if (null != clstream) {
 					try {
 						clstream.close();
 					} catch (Exception e) {
 						
 					}					
 				}				
 			}
 
 		} else {
 			Logger.info("Auto update flag [%s] != true  => skipping structural update", autoupdate);
 		}
 	}
 }
