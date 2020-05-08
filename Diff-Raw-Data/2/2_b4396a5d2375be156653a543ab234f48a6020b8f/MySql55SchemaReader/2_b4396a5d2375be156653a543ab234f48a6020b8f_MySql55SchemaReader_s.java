 package net.sf.javascribe.patterns.model.impl;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.patterns.model.DatabaseSchemaReader;
 import net.sf.javascribe.patterns.model.DatabaseTable;
 import net.sf.javascribe.patterns.model.DatabaseTableColumn;
 
 @Scannable
 public class MySql55SchemaReader implements DatabaseSchemaReader {
 
 	@Override
 	public String databaseType() {
 		return "MySQL 5.5";
 	}
 
 	@Override
 	public List<DatabaseTable> readSchema(String url, String username,
 			String password,String catalog, ProcessorContext ctx) throws JavascribeException {
 		List<DatabaseTable> ret = new ArrayList<DatabaseTable>();
 		Connection conn = null;
 		String versionField = null;
 		
 		try {
 			versionField = ctx.getProperty("net.sf.javascribe.patterns.model.EntityManagerComponent.jpaVersionField");
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(url, username, password);
 
 			List<String> tableNames = findTableNames(conn,catalog);
 			for(String s : tableNames) {
 				DatabaseTable tab = readTable(conn,catalog,s,versionField);
 				ret.add(tab);
 			}
 			
 		} catch(SQLException e) {
			throw new JavascribeException("SQLException while reading MySQL schema");
 		} catch(ClassNotFoundException e) {
 			throw new JavascribeException("Could not find MySQL JDBC driver in classpath");
 		} finally {
 			if (conn!=null) {
 				try { conn.close(); } catch(Exception e) { }
 			}
 		}
 
 		return ret;
 	}
 
 	private DatabaseTable readTable(Connection conn,String schema,String tableName,String versionField) throws SQLException {
 		PreparedStatement pstmt = null;
 		ResultSet res = null;
 		DatabaseTable table = new DatabaseTable();
 
 		try {
 			table.setName(tableName);
 			pstmt = conn.prepareStatement("show columns in "+schema+"."+tableName);
 			res = pstmt.executeQuery();
 			while(res.next()) {
 				DatabaseTableColumn col = new DatabaseTableColumn();
 				table.getColumns().add(col);
 				
 				if ((versionField!=null) && (res.getString(1).equals(versionField))) {
 					table.setVersionField(res.getString(1));
 					continue;
 				}
 				col.setName(res.getString(1));
 				String s = res.getString(3);
 				if (s.equals("NO")) col.setNull(true);
 				else col.setNull(false);
 				
 				s = res.getString(2);
 				if (s.indexOf("bigint")>=0) {
 					col.setJavaType("longint");
 				} else if (s.indexOf("int")>=0) {
 					col.setJavaType("integer");
 				} else if (s.indexOf("varchar")==0) {
 					col.setJavaType("string");
 					int end = s.indexOf(')');
 					col.setSize(s.substring(8,end));
 				} else if (s.indexOf("datetime")==0) {
 					col.setJavaType("timestamp");
 				} else if (s.indexOf("date")==0) {
 					col.setJavaType("date");
 				} else if (s.indexOf("text")==0) {
 					col.setJavaType("string");
 				} else {
 					System.out.println("Found no type for "+s);
 				}
 				s = res.getString(4);
 				if ((s!=null) && (s.equals("PRI"))) {
 					table.setPrimaryKeyColumn(col.getName());
 				}
 			}
 		} finally {
 			if (res!=null) {
 				try { res.close(); } catch(Exception e) { }
 			}
 			if (pstmt!=null) {
 				try { pstmt.close(); } catch(Exception e) { }
 			}
 		}
 		return table;
 	}
 
 	private List<String> findTableNames(Connection conn,String catalog) throws SQLException {
 		List<String> ret = new ArrayList<String>();
 		Statement stmt = null;
 		ResultSet res = null;
 
 		try {
 			stmt = conn.createStatement();
 			res = stmt.executeQuery("show tables in "+catalog);
 			while(res.next()) {
 				ret.add(res.getString(1));
 			}
 		} finally {
 			try { res.close(); } catch(Exception e) { }
 			try { stmt.close(); } catch(Exception e) { }
 		}
 
 		return ret;
 	}
 }
