 package common.persist;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 
 import javax.sql.DataSource;
 
 public class DbUtils {
 	private DbUtils() {
 	}
 	
 	public static int executeAggregate(DataSource dataSource, String query) throws SQLException {
 		return (Integer) executeQuery(dataSource, query, new ResultSetHandler() {
 			@Override
 			public Object handle(ResultSet rst) throws SQLException {
 				if(rst.next()) {
 					return rst.getInt(1);
 				} else {
 					return -1;
 				}
 			}
 		});
 	}
 	
 	public static int executeAggregate(Connection cnn, String query) throws SQLException {
 		return (Integer) executeQuery(cnn, query, new ResultSetHandler() {
 			@Override
 			public Object handle(ResultSet rst) throws SQLException {
 				if(rst.next()) {
 					return rst.getInt(1);
 				} else {
 					return -1;
 				}
 			}
 		});
 	}
 	
 	public static Object executeQuery(DataSource dataSource, String query, ResultSetHandler handler) throws SQLException {
 		Connection cnn = null; 
 		try {
 			cnn = dataSource.getConnection();
 			return executeQuery(cnn, query, handler);
 		} finally {
 			DbUtils.close(cnn);
 		}
 	}
 	
 	public static Object executeQuery(Connection cnn, String query, ResultSetHandler handler) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = cnn.prepareStatement(query);
 			
 			ResultSet rst = null;
 			try {
 				rst = stmt.executeQuery();
 				return handler.handle(rst);
 			} finally {
 				DbUtils.close(rst);
 			}
 			
 		} finally {
 			DbUtils.close(stmt);
 		}
 	}
 
 	public static void executeScripts(DataSource dataSource, String scripts) throws IOException, SQLException {
 		String[] statements = scripts.split(";");
 		Connection cnn = null;
 		try {
 			cnn = dataSource.getConnection();
 			
 			for(String ddlStatement : statements) {
 				Statement stmt = null;
 				try {
 					stmt = cnn.createStatement();
 					stmt.executeUpdate(ddlStatement);
 				} finally {
 					if(stmt != null) {
 						stmt.close();
 					}
 				}
 			}
 		} finally {
 			if(cnn != null) {
 				cnn.close();
 			}
 		}
 	}
 
 	public static void close(Connection cnn) throws SQLException {
 		if(cnn != null) {
 			cnn.close();
 		}
 	}
 
 	public static void close(Statement stmt) throws SQLException {
 		if(stmt != null) {
 			stmt.close();
 		}
 	}
 
 	public static void close(ResultSet rst) throws SQLException {
 		if(rst != null) {
 			rst.close();
 		}
 	}
 	
 	public static void setInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
 		if(value == null) {
 			stmt.setNull(index, Types.INTEGER);
 		} else {
 			stmt.setInt(index, value);
 		}
 	}
 	
 	public static void setDate(PreparedStatement stmt, int index, java.util.Date date) throws SQLException {
 		if(date == null) {
 			stmt.setNull(index, Types.DATE);
 		} else {
 			stmt.setDate(index, new java.sql.Date(date.getTime()));
 		}
 	}
 }
