 package org.openflexo;
 
 import java.io.File;
 import java.util.Properties;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
 
 import org.h2.Driver;
 import org.hibernate.cfg.AvailableSettings;
 import org.hibernate.dialect.H2Dialect;
 import org.openflexo.toolbox.FileUtils;
 
 public class LocalDBAccess {
 
 	private static final String DB_NAME = "openflexo-prefs";
 
 	private static final LocalDBAccess instance = new LocalDBAccess();
 
 	private EntityManagerFactory factory;
 
 	private LocalDBAccess() {
 		Properties properties = new Properties();
 		properties.put(AvailableSettings.DIALECT, H2Dialect.class.getName());
 		properties.put(AvailableSettings.HBM2DDL_AUTO, "update");
 		properties.put("javax.persistence.jdbc.user", "sa");
 		properties.put("javax.persistence.jdbc.password", "");
 		properties.put("javax.persistence.jdbc.driver", Driver.class.getName());
 		properties.put("javax.persistence.jdbc.url",
 				"jdbc:h2:" + new File(FileUtils.getApplicationDataDirectory(), DB_NAME).getAbsolutePath()
 						+ ";AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE");
		factory = Persistence.createEntityManagerFactory("local-db", properties);
 	}
 
 	public static LocalDBAccess getInstance() {
 		return instance;
 	}
 
 	public EntityManagerFactory getFactory() {
 		return factory;
 	}
 
 	public EntityManager getEntityManager() {
 		return factory.createEntityManager();
 	}
 }
