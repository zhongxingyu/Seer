 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ini4j.InvalidFileFormatException;
 import org.ini4j.Wini;
 
 import toctep.skynet.backend.Main;
 import toctep.skynet.backend.dal.dao.BoundingBoxDao;
 import toctep.skynet.backend.dal.dao.BoundingBoxTypeDao;
 import toctep.skynet.backend.dal.dao.CountryDao;
 import toctep.skynet.backend.dal.dao.GeoDao;
 import toctep.skynet.backend.dal.dao.GeoTypeDao;
 import toctep.skynet.backend.dal.dao.HashtagDao;
 import toctep.skynet.backend.dal.dao.KeywordDao;
 import toctep.skynet.backend.dal.dao.LanguageDao;
 import toctep.skynet.backend.dal.dao.PlaceDao;
 import toctep.skynet.backend.dal.dao.PlaceTypeDao;
 import toctep.skynet.backend.dal.dao.SourceTypeDao;
 import toctep.skynet.backend.dal.dao.TimeZoneDao;
 import toctep.skynet.backend.dal.dao.TweetContributorDao;
 import toctep.skynet.backend.dal.dao.TweetDao;
 import toctep.skynet.backend.dal.dao.TweetHashtagDao;
 import toctep.skynet.backend.dal.dao.TweetKeywordDao;
 import toctep.skynet.backend.dal.dao.TweetMentionDao;
 import toctep.skynet.backend.dal.dao.TweetUrlDao;
 import toctep.skynet.backend.dal.dao.UrlDao;
 import toctep.skynet.backend.dal.dao.UserDao;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 
 public final class MySqlUtil {
 
 	private static MySqlUtil instance;
 		
 	public static MySqlUtil getInstance(String properties) {
 		if (instance == null) {
 			instance = new MySqlUtil(properties);
 		}
 		return instance;
 	}
 	
 	public static MySqlUtil getInstance() {
 		return MySqlUtil.getInstance(Main.DB_PROPERTIES);
 	}
 	
 	private String properties;
 	
 	private Wini ini;
 	
 	private String driver;
 	private String host;
 	private String name;
 	private String user;
 	private String pass;
 	
 	private Connection conn;
 	
 	private MySqlUtil(String properties) {
 		this.properties = properties;
 		
 		try {
 			initialize();
 			connect();
 		} catch (InvalidFileFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void initialize() throws InvalidFileFormatException, IOException {
 		ini = new Wini(new File(properties));
 		
 		driver = getIniValue("driver");
         host = getIniValue("host");
         name = getIniValue("name");
         user = getIniValue("user");
         pass = getIniValue("pass");
 	}
 	
 	private String getIniValue(String optionName) {
 		return ini.get("jdbc", optionName, String.class);
 	}
 	
 	private void connect() throws SQLException {
 		String url = "jdbc:" + driver + "://" + host + "/" + name;
 		conn = (Connection) DriverManager.getConnection(url, user, pass);
 	}
 	
 	public Connection getConnection() {
 		return conn;
 	}
 	
 	public int query(String query, Param[] params) {
 		int result = 0;
 		
 		PreparedStatement pstmt = null;
 		
 		try {
 			pstmt = conn.prepareStatement(query);
 			
 			for (int i = 0; i < params.length; i++) {
 				pstmt.setObject(i + 1, params[i].getValue(), params[i].getType());
 			}
 			
 			result = pstmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				pstmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return result;
 	}
 	
 	public int insert(String query, Param[] params) {
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		int id = 0;
 		
 		try {
 			pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
 			
 			for (int i = 0; i < params.length; i++) {
 				pstmt.setObject(i + 1, params[i].getValue(), params[i].getType());
 			}
 			
 			pstmt.executeUpdate();		
 			
 			rs = pstmt.getGeneratedKeys();
 			if (rs.first()) {
 				id = rs.getInt(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				rs.close();
 				pstmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return id;
 	}
 	
 	public List<Object> selectRecord(String query, Param[] params) {
 		List<Object> record = new ArrayList<Object>();
 		
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		
 		try {
 			pstmt = conn.prepareStatement(query);
 			
 			for (int i = 0; i < params.length; i++) {
 				pstmt.setObject(i + 1, params[i].getValue(), params[i].getType());
 			}
 			
 			rs = pstmt.executeQuery();
 			
 			rs.first();
 			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                 Object value = rs.getObject(i);
                 record.add(value);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				rs.close();
 				pstmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return record;
 	}
 	
 	public List<List<Object>> select(String query, Param[] params) {
 		List<List<Object>> records = new ArrayList<List<Object>>();
 		
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		
 		try {
 			pstmt = conn.prepareStatement(query);
 			
 			for (int i = 0; i < params.length; i++) {
 				pstmt.setObject(i + 1, params[i].getValue(), params[i].getType());
 			}
 			
 			rs = pstmt.executeQuery();
 			
 			rs.beforeFirst();
 			
 			while (rs.next()) {
 				List<Object> record = new ArrayList<Object>();
 				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
 	                Object value = rs.getObject(i);
 	                record.add(value);
 				}
 				records.add(record);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				rs.close();
 				pstmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return records;
 	}
 	
 	public int update(String query, Param[] params) {
 		return this.query(query, params);
 	}
 	
 	public int delete(String query, Param[] params) {
 		return this.query(query, params);
 	}
 	
 	public boolean exists(String tableName, String where) {
 		boolean exists = false;
 		
 		Statement stmt = null;
 		ResultSet rs = null;
 		
 		try {
 			stmt = (Statement) conn.createStatement();
 			rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE " + where);
 			
 			int counter = 0;
 			while (rs.next()) {
 				counter++;
 			}
 			if (counter > 0) {
 				exists = true;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 				rs.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return exists;
 	}
 	
 	public int count(String tableName) {
 		int count = 0;
 
 		Statement stmt = null;
 		ResultSet rs = null;
 
 		try {
 			stmt = (Statement) conn.createStatement();
 			rs = stmt.executeQuery("SELECT COUNT(*) as rows FROM " + tableName);
 			rs.next();
 			count = rs.getInt(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 				rs.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return count;
 	}
 	
 	public void truncateDatabase() {
 		Statement stmt = null;
 		try {
 			stmt = (Statement) conn.createStatement();
 			stmt.executeQuery("set foreign_key_checks=0");
 			stmt.executeQuery("TRUNCATE TABLE " + TweetKeywordDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + TweetDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + UserDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + PlaceDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + CountryDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + GeoDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + GeoTypeDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + HashtagDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + LanguageDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + PlaceTypeDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + SourceTypeDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + TimeZoneDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + TweetContributorDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + TweetHashtagDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + TweetMentionDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + TweetUrlDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + UrlDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + KeywordDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + BoundingBoxDao.TABLE_NAME);
 			stmt.executeQuery("TRUNCATE TABLE " + BoundingBoxTypeDao.TABLE_NAME);
 			stmt.executeQuery("set foreign_key_checks=1");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static String escape(Object str) {
 		if(str instanceof String) {
 			str = "\"" + (str != null ? ((String) str).replace("\"", "\\\"") : "") + "\"";
 		}
 		return (String) str;
 	}
 
 }
