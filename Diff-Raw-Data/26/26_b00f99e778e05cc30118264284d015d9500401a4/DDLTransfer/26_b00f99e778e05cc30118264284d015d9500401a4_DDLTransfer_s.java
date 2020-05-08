 package com.teradata.qaf.tset.common.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.teradata.qaf.tset.common.DBConn;
 import com.teradata.qaf.tset.common.Transferable;
 import com.teradata.qaf.tset.utils.SQLReader;
 import com.teradata.qaf.tset.utils.SQLWriter;
 
 public class DDLTransfer implements Transferable {
 
 	private static Logger logger = Logger.getLogger(DDLTransfer.class.getName());
 	private Connection conn;
 	
 	public DDLTransfer() {
 		
 	}
 	
 	public DDLTransfer(Connection conn) {
 		this.conn = conn;
 	}
 	
 	@Override
 	public String getGeneratedSQL() {
 		return this.generateSQL(DBConn.getDatabase());
 	}
 	
 	// generate SQL statement for exporting
 	public String generateSQL(String database) {
		String sql = "select requesttext from dbc.tables order by createtimestamp";
		if (database!=null && !database.equals("")) {
			sql = "select requesttext from dbc.tables where databasename='"+database+"' order by createtimestamp";
		}
 		return sql;
 	}
 	
	// generate import SQL Statements from the exported DDL file
	public List<String> generateImportSQL(String sql) {
		List<String> sqlList = new ArrayList<String>();
		// BUG: if ; exsits in a column name
		sqlList = Arrays.asList(sql.split(";"));
		return sqlList;
	}
 
 	@Override
 	public void doExport() {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement(getGeneratedSQL());
 			logger.info(getGeneratedSQL());
 			rs = ps.executeQuery();
 			List<String> sqlList = new ArrayList<String>();
 			while(rs.next()) {
 				
 				sqlList.add(rs.getString("requesttext"));
 			}
 			//rs.close();
 			//ps.close();
 			// can use multi-thread, fork a new thread to write file
 			SQLWriter.setFileName("TSETInfoTables/" + DBConn.getDatabase() + ".sql");
 			SQLWriter.writeSQL(sqlList);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 		} finally {
 			try {
 				rs.close();
 				ps.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
 		}
 	}
 
 	@Override
 	public void doImport() {
 		PreparedStatement ps = null;
 		boolean res = false;
 		try {
 			//conn.setAutoCommit(false);
			//String sql = SQLReader.readSQL("TSETInfoTables/" + DBConn.getDatabase() + ".sql");
			//List<String> sqlList = this.generateImportSQL(sql);
 			List<String> sqlList = SQLReader.readSQL("TSETInfoTables/" + DBConn.getDatabase() + ".sql");
 			Iterator<String> it = sqlList.iterator();
 			int countAll = 1;
 			conn.setAutoCommit(false);
 			// Firstly, create all the tables
 			while(it.hasNext()) {
 				
 				String sqlTemp = it.next();
 				
 				// empty string 
 				//if(sqlTemp.equals("") || sqlTemp.startsWith("BEGIN LOADING")) continue;
 				//if(sqlTemp.equals("") || sqlTemp.equals(" ")) continue;
 				if(sqlTemp.matches("\\s+") || sqlTemp.toUpperCase().startsWith("BEGIN LOADING")) continue;
 				String []arr = sqlTemp.trim().split("\\s+");
 				//if(sqlTemp.substring(0, 11).equalsIgnoreCase("create table") || sqlTemp.substring(0, 21).equalsIgnoreCase("create multiset table"))
 				if ((arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("table"))
 						|| (arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("multiset") && arr[2].equalsIgnoreCase("table"))
 						|| (arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("set") && arr[2].equalsIgnoreCase("table")))
 				{
 					logger.info("["+countAll+"/"+sqlList.size()+"]" + sqlTemp);
 					//if (countAll < 452) {countAll++; continue;}
 					ps = conn.prepareStatement(sqlTemp);
 					
 					res = ps.execute();
 					conn.commit();
 					++ countAll;
 				}
 			}
 //			
 			conn.commit();
 			logger.info("Totally create table count: " + countAll);
 			
 			// Secondly, create all the other Objects(views, etc.) except tables 
 			ps = null;
 			it = sqlList.iterator();
 			conn.setAutoCommit(false);
 			while(it.hasNext()) {
 				
 				String sqlTemp = it.next();
 				
 				// empty string 
 				if(sqlTemp.matches("\\s+") || sqlTemp.toUpperCase().startsWith("BEGIN LOADING")) {
 					// write only once is ok.
 					logger.info("*["+countAll+"/"+sqlList.size()+"]" + sqlTemp);
 					++ countAll;
 					continue;
 				}
 				String []arr = sqlTemp.trim().split("\\s+");
 				if (!((arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("table"))
 						|| (arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("multiset") && arr[2].equalsIgnoreCase("table"))
 						|| (arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("set") && arr[2].equalsIgnoreCase("table"))))
 				{
 					logger.info("["+countAll+"/"+sqlList.size()+"]" + sqlTemp);
 					//if (countAll < 452) {countAll++; continue;}
 					ps = conn.prepareStatement(sqlTemp);
 					res = ps.execute();
 					conn.commit();
 					++ countAll;
 				}
 			}
 			
 			conn.commit();
 			logger.info("Totally create objects(tables,views,etc.) count: " + countAll);
 			
 			logger.info("return value: " + res);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 			try {
 				conn.rollback();
 				conn.close();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 			System.exit(1);
 		} finally {
 			try {
 				if(ps!=null) ps.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	/**
 	public void doImport() {
 		PreparedStatement ps = null;
 		boolean res = false;
 		try {
 			//conn.setAutoCommit(false);
 			String sql = SQLReader.readSQL("TSETInfoTables/" + DBConn.getDatabase() + ".sql");
 			List<String> sqlList = this.generateImportSQL(sql);
 			Iterator<String> it = sqlList.iterator();
 			int countAll = 1;
 			conn.setAutoCommit(false);
 			// Firstly, create all the tables
 			while(it.hasNext()) {
 				String sqlTemp = it.next();
 				// BEGIN LOADING and empty string 
 				//if(sqlTemp.equals("") || sqlTemp.startsWith("BEGIN LOADING")) continue;
 				//if(sqlTemp.equals("") || sqlTemp.equals(" ")) continue;
 				if(sqlTemp.matches("\\s+")) continue;
 //				if(sqlTemp.startsWith("CREATE VIEW")) 
 //				{
 //					int[] count = ps.executeBatch();
 //					
 //					conn.commit();
 //					countAll += count.length;
 //					logger.info(count.length + " tables was created.");
 //				}
 				String []arr = sqlTemp.trim().split("\\s+");
 				//if(sqlTemp.substring(0, 11).equalsIgnoreCase("create table") || sqlTemp.substring(0, 21).equalsIgnoreCase("create multiset table"))
 				if ((arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("table"))
 						|| (arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("multiset") && arr[1].equalsIgnoreCase("table")))
 				{
 					logger.info("["+countAll+"/"+sqlList.size()+"]" + sqlTemp);
 					if(sqlTemp.toLowerCase().indexOf("with") >= 0) {
 						ps.executeBatch();
 						conn.commit();
 					}
 					ps = conn.prepareStatement(sqlTemp);
 					ps.addBatch();
 					//res = ps.execute();
 					//conn.commit();
 					++ countAll;
 				}
 			}
 //			int[] count = ps.executeBatch();
 //			logger.info(count.length + " tables was created.");
 //			countAll += count.length;
 			ps.executeBatch();
 			conn.commit();
 			logger.info("Totally create table count: " + countAll);
 			
 			// Secondly, create all the other Objects(views, etc.) except tables 
 			ps = null;
 			it = sqlList.iterator();
 			conn.setAutoCommit(false);
 			while(it.hasNext()) {
 				String sqlTemp = it.next();
 				// empty string 
 				if(sqlTemp.matches("\\s+")) continue;
 				String []arr = sqlTemp.split("\\s+");
 				if (!((arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("table"))
 						|| (arr[0].equalsIgnoreCase("create") && arr[1].equalsIgnoreCase("multiset") && arr[1].equalsIgnoreCase("table"))))
 				{
 					logger.info("["+countAll+"/"+sqlList.size()+"]" + sqlTemp);
 					ps = conn.prepareStatement(sqlTemp);
 					ps.addBatch();
 					++ countAll;
 				}
 			}
 			ps.executeBatch();
 			conn.commit();
 			logger.info("Totally create objects(tables,views,etc.) count: " + countAll);
 			
 			logger.info("return value: " + res);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 			try {
 				conn.rollback();
 				conn.close();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 			System.exit(1);
 		} finally {
 			try {
 				if(ps!=null) ps.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 */
 }
