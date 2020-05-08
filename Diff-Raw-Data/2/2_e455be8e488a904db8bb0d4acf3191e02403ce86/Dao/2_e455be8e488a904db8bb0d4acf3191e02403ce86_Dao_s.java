 package com.xu3352.jdbc;
 
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mysql.jdbc.Connection;
 import com.xu3352.config.DbConfig;
 import com.xu3352.config.SetupConfig;
 import com.xu3352.core.Column;
 import com.xu3352.util.StringUtil;
 
 /**
  * MySQL database Dao
  * @author xuyl
  * @date 2013-1-7
  */
 public class Dao {
 	private static String driverName = ""; 	// Load the JDBC driver
 	private static String url = ""; 		// a JDBC url
 	private static String username = "";
 	private static String password = "";
 	
 	// init config
 	static {
 		DbConfig dbConfig = SetupConfig.getInstance().getDbConfig();
 		driverName = dbConfig.getDriverClass();
 		url = dbConfig.getUrl();
 		username = dbConfig.getUsername();
 		password = dbConfig.getPassword();
 	}
 	
 	/**
 	 * query all table name
 	 * @author xuyl
 	 * @date 2013-1-7
 	 * @return
 	 */
 	public List<String> getAllTableName() {
 		List<String> list = new ArrayList<String>();
 		try {
 			checkDriver();
 			Connection conn = getConn();
 			ResultSet rs = createQuary(conn, "show tables");
 			while (rs.next()) {
 				list.add(rs.getString(1));
 			}
 			rs.close();
 			conn.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 	/**
 	 * 根据数据库类型获取字段<br/>
 	 * 默认:支持所有数据库,但没有注释
 	 * @param tableName
 	 * @return
 	 */
 	public List<Column> getGenericColumns(String tableName) {
 		if (driverName.contains("mysql")) {		
 			return getMysqlColumns(tableName);
 		}
 		return getColumns(tableName);	// 默认调用通用接口
 	}
 	
 	/**
 	 * 通用接口:获取表所有列信息,JDBC查询的结果集处理<br/>
 	 * SQL:select * from  <b>表名</b> where 1!=1
 	 * @param tableName
 	 * @return List
 	 */
 	private List<Column> getColumns(String tableName) {
 		List<Column> list = new ArrayList<Column>();
 		try {
 			checkDriver();
 			Connection conn = getConn();
 			ResultSet rs = createQuary(conn, "select * from " + tableName + " where 1!=1");
 			ResultSetMetaData md = rs.getMetaData();
 			int count = md.getColumnCount();
 			for (int i = 1; i <= count; i++) {
 				String name = md.getColumnName(i);
 				String type = typesConvert2Java(md.getColumnType(i));
 				list.add(new Column(type, name, StringUtil.javaStyle(name)));
 			}
 			rs.close();
 			conn.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 	
 	/**
 	 * Mysql获取字段所有信息<br/>
 	 * SQL:show full fields from <b>表名</b>
 	 * @param tableName
 	 * @return
 	 */
 	private List<Column> getMysqlColumns(String tableName) {
 		List<Column> list = new ArrayList<Column>();
 		try {
 			checkDriver();
 			Connection conn = getConn();
 			ResultSet rs = createQuary(conn, "show full fields from " + tableName);
 			while (rs.next()) {
 				final int columnIndex = 9;
 				String type = typesConvertOfMysql2Java(rs.getString(2));
 				String javaStyle = StringUtil.javaStyle(rs.getString(1));
 				list.add(new Column(type, rs.getString(1), javaStyle, rs.getString(columnIndex)));
 			}
 			rs.close();
 			conn.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 	
 	/**
 	 * 数据列类型转换成Java类型
 	 * @param jdbcType
 	 * @return String
 	 * @throws SQLException
 	 */
 	private String typesConvert2Java(int jdbcType) throws SQLException {
 		switch (jdbcType) {
 		case Types.BIGINT:
 		case Types.INTEGER:
 			return "Integer";
 		case Types.CHAR:
 		case Types.NCHAR:
 		case Types.VARCHAR:
 		case Types.NVARCHAR:
 			return "String";
 		case Types.DATE:
 			return "Date";
 		case Types.TIMESTAMP:	// 时间
 			return "Date";
 		case Types.REAL:
 		case Types.DOUBLE:
 			return "Double";
 		default :
 			return jdbcType + "";
 		}
 	}
 	
 	/**
 	 * 数据列类型转换成Java类型<br/>
 	 * data:int(11),varchar(60)
 	 * @param jdbcType
 	 * @return String
 	 * @throws SQLException
 	 */
 	private String typesConvertOfMysql2Java(String mysqlType) throws SQLException {
		if (mysqlType.startsWith("varchar")) {
 			return "String";
 		} else if (mysqlType.startsWith("int") || mysqlType.startsWith("bigint")) {
 			return "Integer";
 		} else if (mysqlType.startsWith("double")) {
 			return "Double";
 		} else if (mysqlType.startsWith("date")) {
 			return "Date";
 		} 
 		return mysqlType;
 	}
 	
 	/**
 	 * 结果集ResultSet
 	 * @param conn
 	 * @param sql
 	 * @return Statement
 	 * @throws SQLException
 	 */
 	private ResultSet createQuary(Connection conn, String sql) throws SQLException {
 		return conn.createStatement().executeQuery(sql);
 	}
 	
 	/**
 	 * 数据库连接Connection
 	 * @return Connection
 	 * @throws SQLException
 	 */
 	private Connection getConn() throws SQLException {
 		return (Connection) DriverManager.getConnection(url, username, password);
 	}
 	
 	/**
 	 * 检查是否有驱动
 	 */
 	private void checkDriver() {
 		try {
 			Class.forName(driverName);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 测试入口
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Dao dao = new Dao();
 		//List<String> list = dao.getAllTableName();
 		List<Column> list = dao.getMysqlColumns("scss_device");
 		for (int i = 0; i < list.size(); i++) {
 			System.out.println(list.get(i));
 		}
 	}
 }
