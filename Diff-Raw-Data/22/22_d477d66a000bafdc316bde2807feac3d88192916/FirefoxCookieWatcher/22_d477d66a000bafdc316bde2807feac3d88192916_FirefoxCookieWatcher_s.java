 package com.socgen.maven.proxy.utils;
 
 import java.io.File;
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.TimerTask;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.dbutils.DbUtils;
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.handlers.AbstractListHandler;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sqlite.SQLiteDataSource;
 
 import com.socgen.maven.proxy.Context;
 
 public class FirefoxCookieWatcher extends TimerTask {
 	public static final String COOKIES_SQLITE = "cookies.sqlite";
 	public static final Log LOGGER = LogFactory.getLog(FirefoxCookieWatcher.class);
 	public static final String SQL = "SELECT host, expiry, name, value FROM moz_cookies WHERE host='.arpege.socgen'";
 	public static final String FAKE_SQL = "SELECT * FROM moz_cookies WHERE host='.247realmedia.com'";
 	private String sqliteFile = COOKIES_SQLITE;
 	private long lastModification;
 	
 	public FirefoxCookieWatcher() {
 		getCookiesSqlitePath();
		lastModification = new File(sqliteFile).lastModified();
 	}
 	
 	@Override
 	public void run() {
 		db();
 	}
 	
 	public static void main(String[] args) {
//		new FirefoxCookieWatcher().db();
 	}
 
 	
 	
 	public void getCookiesSqlitePath(){
 		final String osName = System.getProperty("os.name");
 		if (osName.toLowerCase().indexOf("windows") != -1){
 			final StringBuilder pathToFirefox = new StringBuilder(System.getProperty("user.home"));
 			pathToFirefox.append("/Application Data/Mozilla/Firefox/Profiles/");
 			File file = new File(pathToFirefox.toString());
 			if (file.exists() && file.isDirectory()){
 				if (file.list().length == 1){
					file = new File(file.list()[0]);
 				}else{
 					String tmp = file.list()[0];
 					for (String of : file.list()){
 						if (new File(of).lastModified() > new File(tmp).lastModified()){
 							tmp = of;
 						}
 					}
					file = new File(tmp);
 				}
 			}
 			if (file.exists() && file.isDirectory()){
 				final StringBuilder sb = new StringBuilder(file.getAbsolutePath());
 				sb.append(File.separator);
 				sb.append(COOKIES_SQLITE);
 				sqliteFile = sb.toString();
 			}
 		}
 	}
 	
 	public void db(){
 		if (lastModification < new File(sqliteFile).lastModified()){
 			final boolean loadDriver = DbUtils.loadDriver(org.sqlite.JDBC.class.getName());
 			if (loadDriver){
 				Connection connection = null; 
 				try {
 					final QueryRunner runner = new QueryRunner(getDataSource());
 					final List<Cookie> cookies = runner.query(FAKE_SQL, new CookieHandler());
 					if (!cookies.isEmpty() && cookies.size()==1){
 						ContextThreadLocal.unset();
 						final Context context = new Context();
 						context.setCookie(cookies.get(0));
 						ContextThreadLocal.set(context);
 					}
 				} catch (SQLException e) {
 					LOGGER.error(e);
 				} finally{
 					if (null != connection){
 						try {
 							DbUtils.close(connection);
 						} catch (SQLException e) {
 							LOGGER.error(e);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public DataSource getDataSource() {
 		final SQLiteDataSource dataSource = new SQLiteDataSource();
 		getCookiesSqlitePath();
 		dataSource.setUrl("jdbc:sqlite:" + sqliteFile);
 		return dataSource;
 	}
 
 	public class Cookie implements Serializable {
 		private static final long serialVersionUID = 1L;
 		private int id; // = 1336244053046000 - integer
 		private String name; // = dloadday - text
 		private String value; // = 81.56.11.60.1336244027149768 - text
 		private String host; // = .mozilla.org - text
 		private String path; // = / - text
 		private int expiry; // = Fri Jan 16 20:56:20 CET 1970 - integer
 		private int lastAccessed; // = Wed Jan 21 22:49:08 CET 1970 - integer
 		private int isSecure; // = 0 - integer
 		private int isHttpOnly; // = 0 - integer
 		private String baseDomain; // = mozilla.org - text
 		
 		public int getId() {
 			return id;
 		}
 		public void setId(int id) {
 			this.id = id;
 		}
 		public String getName() {
 			return name;
 		}
 		public void setName(String name) {
 			this.name = name;
 		}
 		public String getValue() {
 			return value;
 		}
 		public void setValue(String value) {
 			this.value = value;
 		}
 		public String getHost() {
 			return host;
 		}
 		public void setHost(String host) {
 			this.host = host;
 		}
 		public String getPath() {
 			return path;
 		}
 		public void setPath(String path) {
 			this.path = path;
 		}
 		public int getExpiry() {
 			return expiry;
 		}
 		public void setExpiry(int expiry) {
 			this.expiry = expiry;
 		}
 		public int getLastAccessed() {
 			return lastAccessed;
 		}
 		public void setLastAccessed(int lastAccessed) {
 			this.lastAccessed = lastAccessed;
 		}
 		public int getIsSecure() {
 			return isSecure;
 		}
 		public void setIsSecure(int isSecure) {
 			this.isSecure = isSecure;
 		}
 		public int getIsHttpOnly() {
 			return isHttpOnly;
 		}
 		public void setIsHttpOnly(int isHttpOnly) {
 			this.isHttpOnly = isHttpOnly;
 		}
 		public String getBaseDomain() {
 			return baseDomain;
 		}
 		public void setBaseDomain(String baseDomain) {
 			this.baseDomain = baseDomain;
 		}
 		@Override
 		public String toString() {
 			return ToStringBuilder.reflectionToString(this);
 		}
 		
 	}
 	public class CookieHandler extends AbstractListHandler<Cookie>{
 
 		@Override
 		protected Cookie handleRow(ResultSet rs) throws SQLException {
 			Cookie cookie = new Cookie();
 			
 			for (int i=1; i<rs.getMetaData().getColumnCount();i++){
 				final String columnName = rs.getMetaData().getColumnName(i);
 				final Object value = rs.getObject(columnName);
 				try {
 					BeanUtils.setProperty(cookie, columnName, value);
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				}
 			}
 			
 //			for(Field field : Cookie.class.getDeclaredFields()){
 //				final String fieldName = field.getName();
 //				final Object value = rs.getObject(fieldName);
 //				try {
 //					BeanUtils.setProperty(cookie, fieldName, value);
 //				} catch (IllegalAccessException e) {
 //					e.printStackTrace();
 //				} catch (InvocationTargetException e) {
 //					e.printStackTrace();
 //				}
 //			}
 			
 			return cookie;
 		}
 		
 	}
 }
