 package org.kvj.lima1.pg.sync.data;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 public class DAO {
 
 	private static Logger log = LoggerFactory.getLogger(DAO.class);
 	public static final String DATASOURCE_ATTR = "dataSource";
 
 	private static final String ENV_DB_HOST = "OPENSHIFT_DB_HOST";
 	private static final String ENV_DB_PORT = "OPENSHIFT_DB_PORT";
 	private static final String ENV_DB_USERNAME = "OPENSHIFT_DB_USERNAME";
 	private static final String ENV_DB_PASSWORD = "OPENSHIFT_DB_PASSWORD";
	private static final String ENV_DB_URL = "OPENSHIFT_DB_URL";
 	private static final String ENV_DATA_DIR = "OPENSHIFT_DATA_DIR";
 
 	public static DataSource initServet(ServletConfig config) {
 		try {
 			ComboPooledDataSource dataSource = new ComboPooledDataSource();
 			dataSource.setDriverClass("org.postgresql.Driver");
 			String url = String.format("jdbc:postgresql://%s:%s/%s",
 					System.getenv(ENV_DB_HOST), System.getenv(ENV_DB_PORT),
 					System.getenv(ENV_DB_URL));
 			dataSource.setJdbcUrl(url);
 			dataSource.setUser(System.getenv(ENV_DB_USERNAME));
 			dataSource.setPassword(System.getenv(ENV_DB_PASSWORD));
 			dataSource.setMinPoolSize(3);
 			dataSource.setMaxPoolSize(10);
 			dataSource.setAutoCommitOnClose(false);
 			config.getServletContext()
 					.setAttribute(DATASOURCE_ATTR, dataSource);
 			log.info(String.format("DataSource created: %s, %s, %s",
 					dataSource.getJdbcUrl(), dataSource.getUser(),
 					dataSource.getPassword()));
 			return dataSource;
 		} catch (Exception e) {
 			log.error("Error creating datasource", e);
 		}
 		return null;
 	}
 
 	public static void closeConnection(Connection c) {
 		if (null != c) {
 			try {
 				c.close();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	public static long nextID(Connection c) throws SQLException {
 		PreparedStatement st = c.prepareStatement("select nextval('ids')");
 		ResultSet set = st.executeQuery();
 		set.next();
 		return set.getLong(1);
 	}
 
 	public static DataSource getDataSource(ServletContext context) {
 		try {
 			return (DataSource) context.getAttribute(DATASOURCE_ATTR);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static void destroyServlet(HttpServlet servlet) {
 		ComboPooledDataSource dataSource = (ComboPooledDataSource) getDataSource(servlet
 				.getServletContext());
 		if (null != dataSource) {
 			try {
 				dataSource.close();
 				log.info("DataSource closed");
 			} catch (Exception e) {
 				log.error("Error closing datasource", e);
 			}
 		}
 	}
 
 	public static File getUploadFolder(String name) throws IOException {
 		String folderName = System.getenv(ENV_DATA_DIR);
 		if (null == folderName) {
 			folderName = System.getenv("user.home");
 		}
 		File folder = new File(folderName);
 		if (!folder.exists() || !folder.isDirectory()) {
 			throw new IOException("Data folder " + folderName
 					+ " is not accessible");
 		}
 		File dataFolder = new File(folder, name);
 		if (dataFolder.exists()) {
 			if (dataFolder.isDirectory()) {
 				return dataFolder;
 			}
 			throw new IOException("Data folder is not directory");
 		} else {
 			if (dataFolder.mkdir()) {
 				return dataFolder;
 			}
 			throw new IOException("Data folder mkdir failed");
 		}
 	}
 
 	public static void copyStream(InputStream in, OutputStream out)
 			throws IOException {
 		byte[] buffer = new byte[1024];
 		BufferedInputStream bis = new BufferedInputStream(in, buffer.length);
 		int bytes = 0;
 		while ((bytes = bis.read(buffer)) > 0) {
 			out.write(buffer, 0, bytes);
 		}
 	}
 }
