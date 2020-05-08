 package com.teradata.qaf.tset.common.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.apache.log4j.Logger;
 
 import com.teradata.qaf.tset.common.CommonConfig;
 import com.teradata.qaf.tset.common.DBConn;
 import com.teradata.qaf.tset.common.Transferable;
 import com.teradata.qaf.tset.utils.SQLReader;
 import com.teradata.qaf.tset.utils.SQLWriter;
 import com.teradata.tset2.pgsql.dao.DDL_KVDAO;
 import com.teradata.tset2.pgsql.pojo.DDL_KV;
 
 public class DDLTransfer implements Transferable {
 
 	private static Logger logger = Logger.getLogger(DDLTransfer.class.getName());
 	private Connection conn;
 	private List<String> sqlListDrop;
 	private final String System_id = "00001";
 	
 	public DDLTransfer(Connection conn) {
 		this.conn = conn;
 		this.sqlListDrop = new ArrayList<String>();
 	}
 	
 	@Override
 	public String getGeneratedSQL() {
 		return this.generateSQL(DBConn.getDatabase());
 	}
 	
 	// generate SQL statement for exporting
 	public String generateSQL(String database) {
 		String sql = CommonConfig.sqlQueryDDL(database);;
 		return sql;
 	}
 	
 	// Execute show table to get DDLs(CREATE and DROP)
 	/**
 	 * 
 	 * @param TableName : The name of Production DB's table
 	 * @param TableKind : The kind of table 
 	 * @return
 	 * @throws SQLException
 	 */
 	private String showTable(String TableName, String TableKind) 
 			throws SQLException {
 		String sqlCREATE = "";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String sql = null;
 		switch(TableKind) {
 		case "T":
 			sql = CommonConfig.sqlShowTable + "\"" + TableName + "\"";
 			this.sqlListDrop.add(CommonConfig.sqlDropTable + 
 					"\"" + TableName + "\"");
 			break;
 		case "V":
 			sql = CommonConfig.sqlShowView + "\"" + TableName + "\"";
 			this.sqlListDrop.add(CommonConfig.sqlDropView + 
 					"\"" + TableName + "\"");
 			break;
 		case "M":
 			sql = CommonConfig.sqlShowMacro + "\"" + TableName + "\"";
 			this.sqlListDrop.add(CommonConfig.sqlDropMacro + 
 					"\"" + TableName + "\"");
 			break;
 		case "P":
 		case "E":
 			sql = CommonConfig.sqlShowProcedure + "\"" + TableName + "\"";
 			this.sqlListDrop.add(CommonConfig.sqlDropProcedure + 
 					"\"" + TableName + "\"");
 			break;
 		case "D":
 			break;
 		case "R":
 		case "F":
 			sql = CommonConfig.sqlShowFunction + "\"" + TableName + "\"";
 			this.sqlListDrop.add(CommonConfig.sqlDropFunction + 
 					"\"" + TableName + "\"");
 			break;
 		default:
 			break;
 		}
 		if(sql == null) return null;
 		ps = conn.prepareStatement(sql);
		logger.info(sql);
 		rs = ps.executeQuery();
 		while(rs.next()) {
			sqlCREATE = rs.getString(1);
 		}
 		
 		// Remove the DB name in the CREATE statement
 		sqlCREATE = sqlCREATE.replace(DBConn.getDatabase() + ".", "");
 		sqlCREATE = sqlCREATE.replace(DBConn.getDatabase().toUpperCase() + ".", "");
 		sqlCREATE = sqlCREATE.replace("\"" + DBConn.getDatabase() + "\".", "");
 		sqlCREATE = sqlCREATE.replace("\"" + DBConn.getDatabase().toUpperCase() + "\".", "");
 		rs.close();
 		ps.close();
 		return sqlCREATE;
 	}
 
 	/**
 	 * Reverse the tables sequence to generate DROP statements.
 	 * @param list
 	 * @return the reversed list
 	 */
 	private List<String> reverseList(List<String> list) {
 		List<String> li = new ArrayList<String>();
 		ListIterator<String> iter = list.listIterator(list.size());
 		while(iter.hasPrevious()) {
 			li.add(iter.previous());
 		}
 		return li;
 	}
 	
 	/**
 	 * Write into local files and PostgreSQL on the same time
 	 */
 	@Override
 	public void doExport() throws Exception{
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement(getGeneratedSQL());
 			logger.info(getGeneratedSQL());
 			rs = ps.executeQuery();
 			List<String> sqlList = new ArrayList<String>();
 			List<DDL_KV> ddlkvList = new ArrayList<DDL_KV>();
 			while(rs.next()) {
 				//sqlList.add(rs.getString("requesttext"));
 				String sql = this.showTable(rs.getString("TableName").trim(), 
 						rs.getString("TableKind").trim());
 //				sqlList.add(this.showTable(rs.getString("TableName").trim(), 
 //						rs.getString("TableKind").trim()));
 				sqlList.add(sql);
 				
 				DDL_KV ddlkv = new DDL_KV();
 				ddlkv.setKey(this.System_id + System.currentTimeMillis());
 				ddlkv.setDdl_createTimestamp(rs.getTimestamp("CreateTimestamp"));
 				//logger.info(rs.getTimestamp("CreateTimestamp"));
 				//logger.info(ddlkv.getDdl_createTimestamp());
 				ddlkv.setDdl_txt(sql);
 				ddlkvList.add(ddlkv);
 				
 			}
 			// can use multi-thread, fork a new thread to write file
 			SQLWriter.setFileName(CommonConfig.path() + "CREATE.sql");
 					//DBConn.getDatabase() + ".sql");
 			SQLWriter.writeSQL(sqlList);
 			SQLWriter.setFileName(CommonConfig.path() + "DROP.sql");
 			SQLWriter.writeSQL(this.reverseList(this.sqlListDrop));
 			
 			DDL_KVDAO ddlkvDao = new DDL_KVDAO();
 			ddlkvDao.insert(ddlkvList);
 			ddlkvDao.closeConn();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 			logger.error("ERROR while exporting DDLs, ROLLBACK automatically " +
 					"and handle the exception outside.");
 			throw new SQLException();
 			//System.exit(-1);
 		} finally {
 			try {
 				if(!rs.isClosed()) rs.close();
 				if(!ps.isClosed()) ps.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				logger.error(e.getMessage());
 			}
 			
 		}
 	}
 
 	/**
 	 * Create all the tables, views and etc. 
 	 */
 	@Override
 	public void doImport() throws SQLException {
 		PreparedStatement ps = null;
 		try {
 			
 			List<String> sqlList = SQLReader.readSQL(CommonConfig.path("CREATE.sql"));
 					//DBConn.getDatabase() + ".sql");
 			Iterator<String> it = sqlList.iterator();
 			int countAll = 1;
 			
 			while(it.hasNext()) {
 				
 				String sqlTemp = it.next();
 				
 				logger.info("["+countAll+"/"+sqlList.size()+"]" + sqlTemp);
 				ps = conn.prepareStatement(sqlTemp);
 					
 				ps.execute();
 				++ countAll;
 			}
 			
 			logger.info("Totally create table(view, etc) count: " + countAll);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 			logger.error("ERROR while importing DDLs, ROLLBACK automatically " +
 					"and handle the exception outside.");
 			throw new SQLException();
 			
 		} finally {
 			try {
 				if(ps!=null) ps.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				logger.error(e.getMessage());
 			}
 		}
 		
 	}
 	
 }
