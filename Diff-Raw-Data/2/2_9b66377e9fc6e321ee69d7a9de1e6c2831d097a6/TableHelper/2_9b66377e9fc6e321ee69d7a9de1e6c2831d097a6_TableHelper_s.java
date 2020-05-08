 /*
  *  ___  ___   _      ___                 _    
  * |   \/ __| /_\    / __|___ _ _  ___ __(_)___
  * | |) \__ \/ _ \  | (_ / -_) ' \/ -_|_-< (_-<
  * |___/|___/_/ \_\  \___\___|_||_\___/__/_/__/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package dsagenesis.core.sqlite;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Vector;
 
 import jhv.util.debug.logger.ApplicationLogger;
 
 /**
  * static helper class for working with the Genesis Database
  * 
  * the functions are helpers for accessing 
  * CoreDataTableIndex
  * and 
  * TableColumnLabels 
  */
 public class TableHelper 
 {
 	/**
 	 * getDBVersion
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static String getDBVersion() 
 			throws SQLException
 	{
 		String query = 
 				"SELECT ID, ver_major, ver_minor from CoreDataVersion WHERE ID=0";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		if( rs.next() )
 		{
 			return rs.getString("ver_major")
 				+ "."
 				+ rs.getString("ver_minor");
 		}
 		return "0.0";
 	}
 	
 	/**
 	 * getDBLanguage
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static String getDBLanguage() 
 			throws SQLException
 	{
 		String query = 
 				"SELECT ID, ver_language from CoreDataVersion WHERE ID=0";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		if( rs.next() )
 		{
 			return rs.getString("ver_language");
 		}
 		return "";
 	}
 	
 	/**
 	 * getPrefixForTable
 	 * 
 	 * @param tablename
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static String getPrefixForTable(String tablename) 
 			throws SQLException
 	{
 		String query = 
 				"SELECT ti_prefix, ti_table_name from CoreDataTableIndex WHERE ti_table_name='"
 					+ tablename+"'";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		if( rs.next() )
 			return rs.getString("ti_prefix");
 		
 		return "";
 	}
 	
 	/**
 	 * getTableFromPrefix
 	 * 
 	 * @param stringwithprefix (can be an id or column name or only the prefix)
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static String getTableFromPrefix(String stringwithprefix) 
 			throws SQLException
 	{
 		String prefix = stringwithprefix.substring( 0, (stringwithprefix.indexOf("_")+1) );
 		String query = 
 				"SELECT ti_prefix, ti_table_name from CoreDataTableIndex WHERE ti_prefix='"
 					+ prefix+"'";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		if( rs.next() )
 			return rs.getString("ti_table_name");
 		
 		return "";
 	}
 	
 	
 	/**
 	 * getLabelForTable
 	 * 
 	 * @param tablename
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static String getLabelForTable(String tablename) 
 			throws SQLException
 	{
 		String query = 
 				"SELECT ti_label, ti_table_name from CoreDataTableIndex WHERE ti_table_name='"
 						+ tablename	+ "'";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		if( rs.next() )
 			return rs.getString("ti_label");
 		
 		return "";
 	}
 	
 	/**
 	 * getColumnLabelsForTable
 	 * 
 	 * inner Vector fields:
 	 * [0] = tcl_column_name
 	 * [1] = tcl_label
 	 * 
 	 * @param tablename
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static Vector<Vector<String>> getColumnLabelsForTable(String tablename)
 			throws SQLException
 	{
 		String query = 
 				"SELECT tcl_column_name, tcl_label, tcl_table_name from TableColumnLabels WHERE tcl_table_name='"
 						+ tablename	+ "'";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		
 		Vector<Vector<String>> vec = new Vector<Vector<String>>();
 		while( rs.next() )
 		{
 			Vector<String> vec2 = new Vector<String>(2);
 			vec2.add(rs.getString("tcl_column_name"));
 			vec2.add(rs.getString("tcl_label"));
 			vec.add(vec2);
 		}
 		return vec;
 	}
 	
 	/**
 	 * getNoteForTable
 	 * 
 	 * @param tablename
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static String getNoteForTable(String tablename) 
 			throws SQLException
 	{
 		String query = 
 				"SELECT ti_note, ti_table_name from CoreDataTableIndex WHERE ti_table_name='"
 						+ tablename	+ "'";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		if( rs.next() )
 			return rs.getString("ti_note");
 		
 		return "";
 	}
 		
 	/**
 	 * isTableEditable
 	 * 
 	 * @param tablename
 	 * 
 	 * @return
 	 * 
 	 * @throws SQLException
 	 */
 	public static boolean isTableEditable(String tablename) 
 			throws SQLException
 	{
 		String query = 
 				"SELECT ti_editable, ti_table_name from CoreDataTableIndex WHERE ti_table_name='"
 					+ tablename+"'";
 		
 		ResultSet rs = DBConnector.getInstance().executeQuery(query);
 		if( rs.next() )
 			return rs.getBoolean("ti_editable");
 		
 		return false;
 	}
 	
 	/**
 	 * idExists
 	 * 
 	 * returns true if the table contains this id.
 	 * 
 	 * @param id
 	 * @param tablename
 	 * 
 	 * @return
 	 */
 	public static boolean idExists(String id, String tablename)
 	{
 		String query = "SELECT ID FROM " + tablename + " WHERE ID='"+id+"'";
 		
 		try
 		{
 			ResultSet rs = DBConnector.getInstance().executeQuery(query);
 			
 			if( rs.next() )
				if( id.equals(rs.getString(0)) )
 					return true;
 			
 		} catch( Exception e ) {
 			ApplicationLogger.logError(e);
 		}
 		return false;
 	}
 	
 	/**
 	 * createNewID
 	 * 
 	 * @param tablename
 	 * 
 	 * @return
 	 */
 	public static String createNewID(String tablename)
 	{
 		String query = "SELECT ti_last_index_num, ti_prefix "
 				+ "FROM CoreDataTableIndex "
 				+ "WHERE ti_uses_prefix=1 AND ti_table_name='"+tablename+"'";
 		
 		try
 		{
 			ResultSet rs = DBConnector.getInstance().executeQuery(query);
 			
 			if( rs.next() )
 			{
 				int index = rs.getInt("ti_last_index_num");
 				index++;
 				String prefix = rs.getString("ti_prefix");
 				
 				String update = "UPDATE CoreDataTableIndex "
 						+ "SET ti_last_index_num="+index
 						+ " WHERE ti_table_name='"+tablename+"'";
 				
 				DBConnector.getInstance().executeUpdate(update);
 				
 				return prefix + index;
 			}
 		} catch( Exception e ) {
 			ApplicationLogger.logError(e);
 			return "";
 		}
 		// it will be generated by DB
 		return null;
 	}
 	
 }
