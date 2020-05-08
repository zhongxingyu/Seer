 package edu.wheaton.hymnal.data;
 
 import java.sql.SQLException;
 
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcConnectionSource;
 
 public class H2Db extends Db {
 	private static final Db INSTANCE = new H2Db();
 
 	private static final String DB_FILENAME = "hymnal.h2";
 
 	private H2Db() {
 		super();
 
 		/*
 		 * The path to the environment in which the code is running. If running
 		 * in the jar, this is the jar's path. If in development, this is the 
 		 * base path for the directory containing resources (like the db). This
 		 * is necessary, because the path to the db needs to be configured
 		 * differently if the code is running in the development environment or
 		 * the packaged jar. 
 		 */
 		String environment = this.getClass().getProtectionDomain()
 				.getCodeSource().getLocation().getPath();
 		String dburl = "jdbc:h2:";
 		
 		// are we in the packaged jar?
 		if (environment.substring(environment.length() - 4).equalsIgnoreCase(
 				".jar")) {
 			dburl += "zip:" + environment + "!/" + DB_FILENAME;
 		} else { // we must be in the development environment
 			/*
 			 * NOTE: in this case, rather than using the environment variable, 
 			 * we could do the following.
 			 * dburl += this.getClass().getResource("/" + DB_FILENAME).getPath(); 
 			 */
 			dburl += "file:" + environment + DB_FILENAME;
 		}
 
 		try {
 			// create a connection source to our database
 			connectionSource = new JdbcConnectionSource(dburl);
 
 			// instantiate the Dao's
			textDao = DaoManager.createDao(connectionSource, Text.class);
			tuneDao = DaoManager.createDao(connectionSource, Tune.class);
			meterDao = DaoManager.createDao(connectionSource, Meter.class);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static Db getInstance() {
 		return INSTANCE;
 	}
 }
