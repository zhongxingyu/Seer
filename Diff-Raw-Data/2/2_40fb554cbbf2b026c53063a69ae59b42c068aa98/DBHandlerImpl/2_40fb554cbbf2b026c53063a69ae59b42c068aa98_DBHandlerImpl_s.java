 package org.orp.collection.utils;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class DBHandlerImpl implements DBHandler{
 	Connection c;
 	Statement stmt;
 	
 	private DBHandlerImpl(Connection c){ 
 		
 		this.c = c;
 		try{
 			stmt = c.createStatement();
 		}catch(SQLException se){
 			se.printStackTrace();
 		}
 	}
 	
 	public static DBHandlerImpl newHandler(String dbinfo){
 		String dbType = dbinfo.split(":")[1];
 		Connection conn = null;
 		try{
 			if(dbType.equals("sqlite"))
 				Class.forName("org.sqlite.JDBC");
 			else{
 				throw new RuntimeException("Not support " + dbType);
 			}
 			 conn = DriverManager.getConnection(dbinfo);
 		}catch(SQLException se){
 			se.printStackTrace();
 		}catch(ClassNotFoundException ce){
 			ce.printStackTrace();
 		}
 		
 		return new DBHandlerImpl(conn);
 	}
 	public boolean exist(String tabName){
 		try {
 			return c.getMetaData().getTables(null, null, tabName, null).next() ? true : false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	public void createTable(String schema){
 		try {
 			stmt.executeUpdate(schema);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	public Set<Map<String, Object>> selectAll(String tabName){
 		Set<Map<String, Object>> result = null;
 		try{
 			 ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabName);
 			 result = toResultSet(rs);
 			 clean();
 		}catch(SQLException se){
 			se.printStackTrace();
 		}
 		return result;
 	}
 	
 	public Map<String, Object> selectAllById(String tabName, String id){
 		Map<String, Object> cond = new HashMap<String, Object>();
 		cond.put("id", id);
 		Map<String, Object> result = null;
		for(Map<String,Object> key : select("COLLECTION", cond))
 			result = key;
 		return result;
 	}
 	
 	public Set<Map<String, Object>> select(String tabName, Map<String, Object> conditions){
 		StringBuilder query = new StringBuilder("SELECT * FROM " + tabName + " WHERE ");
 		Map<Integer, Object> orderMap = new HashMap<Integer, Object>();
 		int count = 1;
 		for(String key : conditions.keySet()){
 			query.append(key.toUpperCase() + "=? AND ");
 			orderMap.put(count ++, conditions.get(key));
 		}
 		query.replace(query.length() - 5, query.length(), "");
 		Set<Map<String, Object>> result = null;
 		try {
 			result = toResultSet(
 					setPreparedParams(query.toString(), tabName, orderMap).executeQuery());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 	
 	public void insert(String tabName, Map<String, Object> values){
 		try {
 			StringBuilder query = new StringBuilder(
 					"INSERT INTO " + tabName + "(");
 			Map<Integer, Object> orderMap = new HashMap<Integer, Object>();
 			int count = 1;
 			for(String key : values.keySet()){
 				query.append(key.toUpperCase() + ",");
 				orderMap.put(count ++, values.get(key));
 			}
 			query.replace(query.length() - 1, query.length(), ") VALUES(");
 			for(int i = 0; i < values.size(); i ++)
 				query.append("?,");
 			query.replace(query.length() - 1, query.length(), ")");
 			setPreparedParams(query.toString(), tabName, orderMap).executeUpdate();
 		} catch(SQLException se) {
 			se.printStackTrace();
 		}
 		
 	}
 	
 	public void updateById(String tabName, Map<String, Object> values, String id){
 		Map<String, Object> cond = new HashMap<String, Object>(1);
 		cond.put("id", id);
 		update(tabName, values, cond);
 	}
 	
 	public void update(String tabName, Map<String, Object> values, Map<String, Object> conds){
 		StringBuilder query = new StringBuilder("UPDATE " + tabName + " SET ");
 		Map<Integer, Object> orderValues = new HashMap<Integer, Object>();
 		int count = 1;
 		for(String key : values.keySet()){
 			query.append(key + "=?, ");
 			orderValues.put(count ++, values.get(key));
 		}
 		query.replace(query.length() - 2, query.length(), " WHERE ");
 		for(String key : conds.keySet()){
 			query.append(key + "=? AND ");
 			orderValues.put(count ++, conds.get(key));
 		}
 		query.replace(query.length() - 4, query.length(), "");
 		try{
 			setPreparedParams(query.toString(),tabName, orderValues).executeUpdate();
 		}catch(SQLException se){
 			se.printStackTrace();
 		}
 	}
 	
 	public void deleteById(String tabName, String id){
 		Map<String, Object> cond = new HashMap<String, Object>();
 		cond.put("id", id);
 		delete(tabName, cond);
 	}
 	
 	public void delete(String tabName, Map<String, Object> conditions){
 		try{	
 			StringBuilder query = new StringBuilder(
 					"DELETE FROM " + tabName + " WHERE ");
 			Map<Integer, Object> orderMap = new HashMap<Integer, Object>();
 			int count = 1;
 			for(String key : conditions.keySet()){
 				query.append(key.toUpperCase() + "=? AND ");
 				orderMap.put(count ++, conditions.get(key));
 			}
 			query.replace(query.length() - 5, query.length(), "");
 			setPreparedParams(query.toString(), tabName, orderMap).executeUpdate();
 		}catch(SQLException se){
 			se.printStackTrace();
 		}
 	}
 	
 	public Map<String, Integer> getTableInfo(String tabName) 
 			throws SQLException{
 		return getFieldsTypes(stmt.executeQuery("SELECT * FROM " + tabName));
 	}
 	
 	public Map<String, Integer> getFieldsTypes(ResultSet rs) 
 			throws SQLException{
 		 ResultSetMetaData rsinfo = rs.getMetaData();
 		 Map<String, Integer> schema = new HashMap<String, Integer>();
 		 for(int i = 1; i <= rsinfo.getColumnCount(); i ++)
 			 schema.put(rsinfo.getColumnName(i).toLowerCase(), rsinfo.getColumnType(i));
 		 return schema;
 	}
 	
 	public void clean(){
 		try {
 			stmt.close();
 			c.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public PreparedStatement setPreparedParams(String query, 
 			String tabName, Map<Integer, Object> orderValues) 
 			throws SQLException{
 		PreparedStatement pstmt = c.prepareStatement(query);
 		for(int i = 1; i <= orderValues.size(); i ++){
 			Object value = orderValues.get(i);
 			if(value instanceof String)
 				pstmt.setString(i, (String)value);
 			else if(value instanceof Integer)
 				pstmt.setInt(i, (Integer)value);
 			else if(value instanceof Float)
 				pstmt.setFloat(i, (Float)value);
 			else if(value instanceof Double)
 				pstmt.setDouble(i, (Double)value);
 			else if(value instanceof Date){
 				DateFormat pattern = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 				pstmt.setString(i, pattern.format((Date)value));
 			}
 		}
 		return pstmt;
 	}
 	
 	public Set<Map<String, Object>> toResultSet(ResultSet rs) 
 			throws SQLException{
 		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
 		Map<String, Integer> schema = getFieldsTypes(rs);
 		while(rs.next()){
 			Map<String, Object> row = new HashMap<String, Object>();
 			for(String col : schema.keySet()){
 				int type = schema.get(col);
 				switch(type){
 					case Types.VARCHAR:
 					case Types.CHAR:
 					case Types.LONGVARCHAR:
 					case Types.LONGNVARCHAR: 
 					case Types.DATE:
 						row.put(col, rs.getString(col));break;
 					case Types.INTEGER:
 					case Types.BIGINT:
 						row.put(col, rs.getInt(col)); break;
 					case Types.FLOAT:
 						row.put(col, rs.getFloat(col)); break;
 					case Types.DOUBLE:
 					case Types.DECIMAL:
 						row.put(col, rs.getDouble(col)); break;
 					case Types.NULL:
 						row.put(col, null);break;
 					default:
 						row = null;
 						throw new SQLException("Unsupported type: " + type);
 				}
 			}
 			result.add(row);
 		}
 		
 		return result;
 	} 
 	
 	public String removeSpecialChars(String tabName) {
 		return null;
 	}
 
 	public String escapeSpecialChars(String tabName) {
 		return null;
 	}
 }
