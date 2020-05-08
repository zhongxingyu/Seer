 /**
  * 
  */
 package ar.edu.itba.paw.grupo1;
 
import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.itba.paw.grupo1.dao.JDBCUserDao;
 import ar.edu.itba.paw.grupo1.dao.UserDao;
 import ar.edu.itba.paw.grupo1.service.AbstractContainer;
 import ar.edu.itba.paw.grupo1.service.UserService;
 import ar.edu.itba.paw.grupo1.service.UserServiceImpl;
 
 /**
  * Container implementation.
  * 
  * @author jpcivile
  */
 public class ApplicationContainer extends AbstractContainer {
 
 	private static ApplicationContainer self;
 	
	private static Logger logger = Logger.getLogger(ApplicationContainer.class);
	
	private Properties config;
	
 	private ApplicationContainer() {
 		super();
		
		config = new Properties();
		try {
			config.load(getClass().getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			logger.warn("The configuration for ApplicationContainer couldn't be loaded.", e);
			//We just leave config empty, and let the builders deal with it
		}
 	}
 	
 	/**
 	 * Get an instance of the container.
 	 * 
 	 * @return the working instance
 	 */
 	public synchronized static ApplicationContainer getInstance() {
 		
 		if (self == null) {
 			self = new ApplicationContainer();
 		}
 		
 		return self;
 	}
 	
 	/**
 	 * Get an instance for a given interface.
 	 * 
 	 * @param <E> The type of the interface.
 	 * @param cls The interface Class Object.
 	 * 
 	 * @return The instance
 	 */
 	public static <E> E get(Class<E> cls) {
 		return getInstance().getObject(cls);
 	}
 	
 	protected UserService buildUserService(UserDao userDao) {
 		return new UserServiceImpl(userDao);
 	}
 	
 	protected UserDao buildUserDao(Connection conn) {
 		return new JDBCUserDao(conn);
 	}
 	
 	protected Connection buildConnection() {
 		
		if (!config.containsKey("db.url") || !config.containsKey("db.user") || !config.containsKey("db.pass")) {
			logger.error("Database configuration missing. Bailing on the build.");
			return null;
		}
		
 		try {
			return DriverManager.getConnection(config.getProperty("db.url"), config.getProperty("db.user"), config.getProperty("db.pass"));
 		} catch (SQLException e) {
 			Logger.getLogger(ApplicationContainer.class).fatal("Failed to build a connection.", e);
 		}
 		
 		return null;
 	}
 }
