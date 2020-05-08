 /*  Copyright (C) 2011  Nicholas Wright
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package io;
 import java.awt.Image;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 /**
  * Class for database communication.
  */
 public class MySQL{
 	private static final HashMap<String, String> prepStmts = new HashMap<String, String>();
 	protected static Logger logger = Logger.getLogger(MySQL.class.getName());
 	protected final String RS_CLOSE_ERR = "Could not close ResultSet: ";
 	protected final String SQL_OP_ERR = "MySQL operation failed: ";
 	protected final ConnectionPool connPool;
 
 	public MySQL(ConnectionPool connPool){
 		this.connPool = connPool;
 	}
 	
 	static{
 		init();
 	}
 
 	/**
 	 * Initialize the class, preparing the statements needed for the methods.
 	 */
 	private static void init(){
 		generateStatements();
 		
 		addPrepStmt("addCache"			, "REPLACE INTO cache SET id=?");
 		addPrepStmt("addThumb"			, "INSERT INTO thumbs (url, filename, thumb) VALUES(?,?,?)");
 		addPrepStmt("getThumb"			, "SELECT thumb FROM thumbs WHERE url = ? ORDER BY filename ASC");
 		addPrepStmt("pending"			, "SELECT count(*) FROM filter WHERE status = 1");
 		addPrepStmt("isCached"			, "SELECT timestamp FROM `cache` WHERE `id` = ?");
 		addPrepStmt("isArchive"			, "SELECT * FROM `archive` WHERE `id` = ?");
 		addPrepStmt("isDnw"				, "SELECT * FROM `dnw` WHERE `id` = ?");
 		addPrepStmt("prune"				, "DELETE FROM `cache` WHERE `timestamp` < ?");
 		addPrepStmt("isHashed"			, "SELECT id FROM `hash` WHERE `id` = ?");
 		addPrepStmt("addHash"			, "INSERT INTO hash (id, dir, filename, size) VALUES (?,?,?,?)");
 		addPrepStmt("deleteHash"		, "DELETE FROM hash WHERE id = ?");
 		addPrepStmt("deleteFilter"		, "DELETE FROM filter WHERE id = ?");
 		addPrepStmt("deleteDnw"			, "DELETE FROM dnw WHERE id = ?");
 		addPrepStmt("deleteBlock"		, "DELETE FROM block WHERE id = ?");
 		addPrepStmt("deleteArchive"		, "DELETE FROM archive WHERE id = ?");
 		addPrepStmt("isBlacklisted"		, "SELECT * FROM `block` WHERE `id` = ?");
 		addPrepStmt("getDirectory"		, "SELECT id FROM dirlist WHERE dirpath = ?");
 		addPrepStmt("getFilename"		, "SELECT id FROM filelist WHERE filename = ?");
 		addPrepStmt("getSetting"		, "SELECT param	FROM settings WHERE name = ?");
 	}
 	
 	private static void generateStatements(){
 		for(MySQLtables table : MySQLtables.values()){
 			addPrepStmt("size"+table.toString(), "SELECT count(*) FROM "+table.toString());
 		}
 	}
 
 	protected Connection getConnection(){
 		try {
 			return connPool.getConnection();
 		} catch (SQLException e) {
 			logger.warning("Failed to get database connection");
 		}
 		
 		return null;
 	}
 
 	protected static void addPrepStmt(String id, String stmt){
 		try {
 			if(prepStmts.containsKey(id))
 				throw new IllegalArgumentException("Key is already present");
 			prepStmts.put(id, stmt);
 		} catch (NullPointerException npe){
 			logger.severe("Prepared Statement could not be created, invalid connection");
 		} catch (IllegalArgumentException iae){
 			logger.severe("Prepared Statement could not be created, "+iae.getMessage());
 		}
 	}
 
 	public boolean batchExecute(String[] statements){
 		Connection cn = getConnection();
 		Statement req = null;
 		
 		try {
 			for(String sql : statements){
 				req = cn.createStatement();
 				req.execute(sql);
 			}
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 			try {
 				if(req != null)
 					req.close();
 			} catch (SQLException e1) {
 				logger.warning("Unable to close statement");
 			}
 			return false;
 		} finally {
 			silentClose(cn, null, null);
 		}
 		
 		return true;
 	}
 
 //	public void addPrepStmt(String id,String stmt,int param1, int param2){
 //		PreparedStatement toAdd = null;
 //		try {
 //			toAdd = cn.prepareStatement(stmt,param1,param2);
 //			prepStmts.put(id, toAdd);
 //		} catch (SQLException e) {
 //			logger.severe("Prepared Statement could not be created,\n"+e.getMessage()+
 //					"\n"+id
 //					+"\n"+stmt);
 //		}
 //	}
 //
 //	public void addPrepStmt(String id,String stmt,int param1){
 //		PreparedStatement toAdd = null;
 //		try {
 //			toAdd = cn.prepareStatement(stmt,param1);
 //			prepStmts.put(id, toAdd);
 //		} catch (SQLException e) {
 //			logger.severe("Prepared Statement could not be created,\n"+e.getMessage()+
 //					"\n"+id
 //					+"\n"+stmt);
 //		} catch (NullPointerException npe) {
 //			logger.severe("Could not add Prepared Statment, invalid connection");
 //		}
 //	}
 
 	protected PreparedStatement getPrepStmt(String command){
 		if(prepStmts.containsKey(command)){
 			Connection cn = getConnection();
 			
 			PreparedStatement prepStmt = null;
 			try {
 				prepStmt = cn.prepareStatement(prepStmts.get(command));
 			} catch (SQLException e) {
 				logger.warning("Failed to create prepared statement for command \""+command+"\"");
 			}
 			return prepStmt;
 		}else{
 			logger.warning("Prepared statment command \""+command+"\" not found.\nHas this object been initialized?");
 			return null;
 		}
 	}
 	
 	protected void silentClose(Connection cn, PreparedStatement ps, ResultSet rs){
 		if(rs != null)
 			try{rs.close();}catch(SQLException e){}
 		if(ps != null)
 			try{ps.close();}catch(SQLException e){}
 		if(cn != null)
 			try{cn.close();}catch(SQLException e){}
 	}
 	
 	protected void closeAll(PreparedStatement ps){
 		Connection cn = null;
 		ResultSet rs = null;
 		
 		if(ps == null)
 			return;
 		
 		try{cn = ps.getConnection();}catch(SQLException e){}
 		try{rs = ps.getResultSet();}catch(SQLException e){}
 		
 		if(rs != null)
 			try{rs.close();}catch(SQLException e){}
 		if(ps != null)
 			try{ps.close();}catch(SQLException e){}
 		if(cn != null)
 			try{cn.close();}catch(SQLException e){}
 	}
 
 	/**
 	 * Add the current URL to the cache, or update it's Timestamp if it
 	 * already exists. Method will return true if the URL is already present,
 	 * otherwise false.
 	 * @param url URL to be added
 	 * @return true if URL is already present else false.
 	 * Retruns true on error.
 	 */
 	public boolean addCache(URL url){
 		String id = url.toString();
 		PreparedStatement ps = getPrepStmt("addCache");
 		try {
 			ps.setString(1, id);
 			int res = ps.executeUpdate();
 	
 			if(res > 1)
 				return true; // entry was present
 			else
 				return false; // entry is new
 	
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		} finally {
 			closeAll(ps);
 		}
 		return true;
 	}
 
 	public void addThumb(String url,String filename, byte[] data){
 		Connection cn = getConnection();
 		Blob blob = null;
 	
 		PreparedStatement ps = getPrepStmt("addThumb");
 		try {
 			blob = cn.createBlob();
 			blob.setBytes(1, data);
 	
 			ps.setString(1, url);
 			ps.setString(2, filename);
 			ps.setBlob(3, blob);
 			ps.executeUpdate();
 	
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		}finally{
 			try {
 				if(blob != null)
 					blob.free();
 			} catch (SQLException e) {
 				logger.severe(e.getMessage());
 			}
 			closeAll(ps);
 			silentClose(cn, ps, null);
 		}
 	}
 
 	public boolean addHash(String hash, String path, long size) {
 		PreparedStatement ps = getPrepStmt("addHash");
 
 		try{
 			int[] pathId = addPath(path);
 
 			if (pathId == null){
 				logger.warning("Invalid path data");
 				return false;
 			}
 
 			ps.setString(1, hash);
 			ps.setInt(2, pathId[0]);
 			ps.setInt(3, pathId[1]);
 			ps.setLong(4, size);
 			ps.execute();
 			return true;
 		} catch(SQLException e){
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		} finally {
 			closeAll(ps);
 		}
 
 		return false;
 	}
 
 
 	/**
 	 * Get the number of pending filter items.
 	 * @return Number of pending items.
 	 */
 	public int getPending(){
 		return simpleIntQuery("pending");
 	}
 
 	public ArrayList<Image> getThumb(String url){
 		Blob blob = null;
 		ArrayList<Image> images = new ArrayList<Image>();
 		InputStream is;
 		String command = "getThumb";
 
 		ResultSet rs = null;
 		PreparedStatement ps = getPrepStmt(command);
 
 		try {
 			ps.setString(1, url);
 			rs = ps.executeQuery();
 
 			while(rs.next()){
 				blob = rs.getBlob(1);
 				is = new BufferedInputStream(blob.getBinaryStream());
 				images.add(ImageIO.read(is));
 				is.close();
 				blob.free();
 			}
 
 			return images;
 
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		} catch (IOException e) {
 			logger.severe(e.getMessage());
 		}finally{
 			closeAll(ps);
 			silentClose(null, ps, rs);
 		}
 		return null;
 	}
 
 	public int size(MySQLtables table){
 		return simpleIntQuery("size"+table.toString());
 	}
 
 	/**
 	 * Check the ID against the cache.
 	 * @param uniqueID ID to check
 	 * @return true if the ID is present otherwise false.
 	 * Returns true on errors.
 	 */
 	public boolean isCached(URL url){
 		return isCached(url.toString());
 	}
 
 	/**
 	 * Check the ID against the cache.
 	 * @param uniqueID ID to check
 	 * @return true if the ID is present otherwise false.
 	 * Returns true on errors.
 	 */
 	public boolean isCached(String uniqueID){
 		return simpleBooleanQuery("isCached", uniqueID, true);
 	}
 
 	public boolean isArchived(String hash){
 		return simpleBooleanQuery("isArchive", hash, true);
 	}
 
 	public boolean isDnw(String hash){
 		return simpleBooleanQuery("isDnw", hash, true);
 	}
 
 	public boolean isHashed(String hash){
 		return simpleBooleanQuery("isHashed", hash, true);
 	}
 
 	public boolean isBlacklisted(String hash){
 		return simpleBooleanQuery("isBlacklisted", hash, false);
 	}
 	
 	private boolean simpleBooleanQuery(String command, String key, Boolean defaultReturn){
 		ResultSet rs = null;
 		PreparedStatement ps = getPrepStmt(command);
 		try {
 			ps.setString(1, key);
 			rs = ps.executeQuery();
 			boolean b = rs.next();
 			return b;
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
 		} finally{
 			closeAll(ps);
 		}
 		
 		return defaultReturn;
 	}
 	
 	private int simpleIntQuery(String command){
 		ResultSet rs = null;
 		PreparedStatement ps = getPrepStmt(command);
 		
 		if(ps == null){
 			logger.warning("Could not carry out query for command \""+command+"\"");
 			return -1;
 		}
 		
 		try {
 			rs = ps.executeQuery();
 
 			rs.next();
 			int intValue = rs.getInt(1);
 			return intValue;
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
 		} finally{
 			closeAll(ps);
 		}
 		
 		return -1;
 	}
 	
 	private String simpleStringQuery(String command){
 		ResultSet rs = null;
 		PreparedStatement ps = getPrepStmt(command);
 		
 		if(ps == null){
 			logger.warning("Could not carry out query for command \""+command+"\"");
 			return null;
 		}
 		
 		try {
 			rs = ps.executeQuery();
 
 			rs.next();
 			String string = rs.getString(1);
 			return string;
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
 		} finally{
 			closeAll(ps);
 			closeResultSet(rs, command);
 		}
 		
 		return null;
 	}
 
 	public void pruneCache(long maxAge){
 		PreparedStatement ps = getPrepStmt("prune");
 
 		try {
 			ps.setTimestamp(1,new Timestamp(maxAge));
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		} finally {
 			closeAll(ps);
 		}
 	}
 
 	public void delete(MySQLtables table, String id){
 		PreparedStatement ps = getPrepStmt("delete"+table.toString());
 		if(ps == null){
 			logger.warning("Could not delete entry "+ id +" for table "+table.toString());
 			return;
 		}
 		
 		try {
 			ps.setString(1, id);
 			ps.executeUpdate();
 		} catch (Exception e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		} finally {
 			closeAll(ps);
 		}
 	}
 
 	public void sendStatement(String sqlStatment){
 		Connection cn = getConnection();
 		Statement req = null;
 		try {
 			req = cn.createStatement();
 			req.execute(sqlStatment);
 			req.close();
 		} catch (SQLException e) {
 			logger.warning("Failed to execute statement id: "+sqlStatment+"\n"+e.getMessage());
 		} finally {
 			if(req != null)
 				try{req.close();} catch (SQLException e){}
 
 			silentClose(cn, null, null);
 		}
 	}
 
 	public String getSetting(DBsettings settingName){
 		String command = "getSetting";
 		ResultSet rs = null;
 		PreparedStatement ps = getPrepStmt(command);
 		
 		try {
 			ps.setString(1, settingName.toString());
 			rs = ps.executeQuery();
 
 			rs.next();
 			String string = rs.getString(1);
 			return string;
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 		} finally{
 			closeAll(ps);
 			silentClose(null, ps, rs);
 		}
 		
 		return null;
 	}
 
 	private int[] addPath(String fullPath){
 		int pathValue;
 		
 		Connection cn = null;
 		try{
 			cn = getConnection();
 			PreparedStatement addDir = cn.prepareStatement("INSERT INTO dirlist (dirpath) VALUES (?)",PreparedStatement.RETURN_GENERATED_KEYS);
 			PreparedStatement addFile = cn.prepareStatement("INSERT INTO filelist (filename) VALUES (?)",PreparedStatement.RETURN_GENERATED_KEYS);
 			
 			int split = fullPath.lastIndexOf("\\")+1;
 			String filename = fullPath.substring(split).toLowerCase(); // bar.txt
 			String path = fullPath.substring(0,split).toLowerCase(); // D:\foo\
 			int[] pathId = new int[2];
 	
 			pathValue = pathLookupQuery("getDirectory", path);
 			pathId[0] = pathAddQuery(addDir, pathValue, path);
 			
 			pathValue = pathLookupQuery("getFilename", filename);
 			pathId[1] = pathAddQuery(addFile, pathValue, filename);
 	
 			return pathId;
 		} catch (SQLException e) {
 			logger.severe(e.getMessage());
 		} finally {
 			silentClose(cn, null, null);
 		}
 	
 		return null;
 	}
 	
 	private int pathLookupQuery(String command, String path) throws SQLException{
 		PreparedStatement ps = getPrepStmt(command);
 		ps.setString(1, path);
 		ResultSet rs = ps.executeQuery();
 		int pathValue = -1;
 
 		try{
 			if(rs.next()){
 				pathValue = rs.getInt(1);
 			}
 		}catch (SQLException e){
 			throw e;
 		}finally{
 			closeAll(ps);
 		}
 
 		return pathValue;
 	}
 	
 	private int pathAddQuery(PreparedStatement ps, int pathLookUp, String path) throws SQLException{
 		int pathValue = -1;
 		ResultSet rs = null;
 
 		try{
 			if(pathLookUp == -1){
 				ps.setString(1, path);
 				ps.execute();
 
 				rs = ps.getGeneratedKeys();
 				rs.next();
 				pathValue = rs.getInt(1); 
 			}
 		}catch (SQLException e){
 			throw e;
 		}finally{
 			closeAll(ps);
 		}
 
 		return pathValue;
 	}
 	
 	private void closeResultSet(ResultSet rs, String command){
 		if(rs != null){
 			try{
 				rs.close();
 			}catch (SQLException e){
 				logger.warning(RS_CLOSE_ERR+e.getMessage()+" for command \""+command+"\"");
 			}
 		}
 	}
 }
