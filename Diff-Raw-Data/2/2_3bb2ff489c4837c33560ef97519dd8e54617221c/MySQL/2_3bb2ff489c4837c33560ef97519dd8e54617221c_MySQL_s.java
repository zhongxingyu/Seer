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
 import java.security.InvalidParameterException;
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
 	HashMap<String, PreparedStatement> prepStmts = new HashMap<String, PreparedStatement>();
 	protected static Logger logger = Logger.getLogger(MySQL.class.getName());
 	Connection cn = null;
 	Properties mySqlProps;
 	int retries = 0;
 	private static int MAX_RETRY = 3; // reconnect attempts
 	private static int RETRY_WAIT = 5000;	// time to wait between reconnect attempts, in milliseconds
 	private static int VALID_CHECK_TIME_OUT = 10; // time to wait for a response when validating connection, in seconds
 
 	public MySQL(Properties mySqlProps){
 		this.mySqlProps = mySqlProps;
 	}
 
 	/**
 	 * Initialize the class, preparing the statements needed for the methods.
 	 */
 	public void init(){
 		addPrepStmt("addCache"			, "REPLACE INTO cache SET id=?");
 		addPrepStmt("addThumb"			, "INSERT INTO thumbs (url, filename, thumb) VALUES(?,?,?)");
 		addPrepStmt("getThumb"			, "SELECT thumb FROM thumbs WHERE url = ? ORDER BY filename ASC");
 		addPrepStmt("sizeCache"			, "SELECT count(*) FROM cache");
 		addPrepStmt("sizeFilter"		, "SELECT count(*) FROM filter");
 		addPrepStmt("pending"			, "SELECT count(*) FROM filter WHERE status = 1");
 		addPrepStmt("isCached"			, "SELECT timestamp FROM `cache` WHERE `id` = ?");
 		addPrepStmt("isArchive"			, "SELECT * FROM `archive` WHERE `hash` = ?");
 		addPrepStmt("isDnw"				, "SELECT * FROM `dnw` WHERE `hash` = ?");
 		addPrepStmt("prune"				, "DELETE FROM `cache` WHERE `timestamp` < ?");
 		addPrepStmt("isHashed"			, "SELECT hash FROM `hash` WHERE `hash` = ?");
 		addPrepStmt("addHash"			, "INSERT INTO hash (hash, dir, filename, size) VALUES (?,?,?,?)");
 		addPrepStmt("deleteHash"		, "DELETE FROM hash WHERE hash = ?");
 		addPrepStmt("deleteFilter"		, "DELETE FROM filter WHERE id = ?");
 		addPrepStmt("deleteDnw"			, "DELETE FROM dnw WHERE hash = ?");
 		addPrepStmt("deleteBlock"		, "DELETE FROM block WHERE hash = ?");
 		addPrepStmt("deleteArchive"		, "DELETE FROM archive WHERE hash = ?");
 		addPrepStmt("isBlacklisted"		, "SELECT * FROM `block` WHERE `hash` = ?");
 		addPrepStmt("addDirectory"		, "INSERT INTO dirlist (dirpath) VALUES (?)",PreparedStatement.RETURN_GENERATED_KEYS);
 		addPrepStmt("getDirectory"		, "SELECT id FROM dirlist WHERE dirpath = ?");
 		addPrepStmt("addFilename"		, "INSERT INTO filelist (filename) VALUES (?)",PreparedStatement.RETURN_GENERATED_KEYS);
 		addPrepStmt("getFilename"		, "SELECT id FROM filelist WHERE filename = ?");
 	}
 
 	public void reconnect(){
 		if(isValid())
 			return;
 	
 		try{
 			Class.forName( "com.mysql.jdbc.Driver" );
 			if(mySqlProps != null){
 				Class.forName( "com.mysql.jdbc.Driver" );
 				cn = DriverManager.getConnection(mySqlProps.getProperty("url"),mySqlProps);
 				logger.info("connection to server established");
 				retries=0;
 			}else{
 				logger.warning("could not connect, connection details missing");
 			}
 		}catch(Exception e){
 			if((e.getMessage().contains("link")) && (retries < MAX_RETRY)){
 				retries++;
 				try{Thread.sleep(RETRY_WAIT);}catch(InterruptedException ie){}
 				logger.info("connect failed, retrying...");
 				reconnect();
 			}else{
 				retries = MAX_RETRY; // Cancel loop
 				logger.severe(e.getMessage());
 			}
 		}
 	}
 
 	public boolean isValid (){
 		if (cn == null){
 			return false;
 		}else{
 			try {
 				return cn.isValid(VALID_CHECK_TIME_OUT);
 			} catch (SQLException e) {
 				logger.severe(e.getMessage());
 				return false;
 			}
 		}
 	}
 
 	public void disconnect(){
 		Iterator<PreparedStatement> ite = prepStmts.values().iterator();
 	
 		while(ite.hasNext()){
 			PreparedStatement ps = ite.next();
 			try { if( null != ps ) ps.close(); } catch( Exception ex ) {}
 		}
 	}
 
 	public void addPrepStmt(String id, String stmt){
 		reconnect();
 
 		PreparedStatement toAdd = null;
 		try {
 			if(prepStmts.containsKey(id))
 				throw new IllegalArgumentException("Key is already present");
 			toAdd = cn.prepareStatement(stmt);
 			prepStmts.put(id, toAdd);
 		} catch (SQLException e) {
 			logger.severe("Prepared Statement could not be created,\n"+e.getMessage()+
 					"\n"+id
 					+"\n"+stmt);
 		} catch (NullPointerException npe){
 			logger.severe("Prepared Statement could not be created, invalid connection");
 		} catch (IllegalArgumentException iae){
 			logger.severe("Prepared Statement could not be created, "+iae.getMessage());
 		}
 	}
 
 	public void addPrepStmt(String id,String stmt,int param1, int param2){
 		PreparedStatement toAdd = null;
 		try {
 			toAdd = cn.prepareStatement(stmt,param1,param2);
 			prepStmts.put(id, toAdd);
 		} catch (SQLException e) {
 			logger.severe(e.getMessage()+
 					"\n"+id
 					+"\n"+stmt);
 		}
 	}
 
 	public void addPrepStmt(String id,String stmt,int param1){
 		PreparedStatement toAdd = null;
 		try {
 			toAdd = cn.prepareStatement(stmt,param1);
 			prepStmts.put(id, toAdd);
 		} catch (SQLException e) {
 			logger.severe(e.getMessage()+
 					"\n"+id
 					+"\n"+stmt);
 		} catch (NullPointerException npe) {
 			logger.severe("Could not add Prepared Statment, invalid connection");
 		}
 	}
 
 	public PreparedStatement getPrepStmt(String command){
 		if(prepStmts.containsKey(command)){
 			return prepStmts.get(command);
 		}else{
 			logger.warning("Prepared statment command \""+command+"\" not found.\nHas this object been initialized?");
 			return null;
 		}
 	}
 
 	public void prepStmtUpdate(String string) throws SQLException{
 		prepStmts.get(string).executeUpdate();
 	}
 
 	public ResultSet prepStmtQuery(String string) throws SQLException{
 		ResultSet res =  prepStmts.get(string).executeQuery();
 		return res;
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
 		reconnect();
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
 			logger.warning("MySql filter update failed: "+e.getMessage());
 		}
 		return true;
 	}
 
 	public void addThumb(String url,String filename, byte[] data){
 		reconnect();
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
 			logger.warning("Unable to add thumbnail: "+e.getMessage());
 		}finally{
 			try {
 				if(blob != null)
 					blob.free();
 			} catch (SQLException e) {
 				logger.severe(e.getMessage());
 			}
 		}
 	}
 
 	public void addHash(String hash, String path, long size) throws SQLException{
 		reconnect();
 	
 		PreparedStatement ps = getPrepStmt("addHash");
 	
 		int[] pathId = addPath(path);
 	
 		if (pathId == null){
 			logger.warning("Invalid path data");
 			pathId = new int[2];
 			pathId[0] = 0;
 			pathId[1] = 1;
 		}
 	
 		ps.setString(1, hash);
 		ps.setInt(2, pathId[0]);
 		ps.setInt(3, pathId[1]);
 		ps.setLong(4, size);
 		ps.execute();
 	}
 
 
 	/**
 	 * Get the number of pending filter items.
 	 * @return Number of pending items.
 	 */
 	public int getPending(){
 		reconnect();
 	
 		ResultSet rs;
 		PreparedStatement ps = getPrepStmt("pending");
 	
 		try {
 			rs = ps.executeQuery();
 			rs.next();
 			int i = rs.getInt(1);
 			rs.close();
 			return i;
 		} catch (SQLException e) {
 			logger.warning("Pending filter lookup failed: "+e.getMessage());
 		}
 		return -1;
 	}
 
 	public ArrayList<Image> getThumb(String url){
 		reconnect();
 		Blob blob = null;
 		ArrayList<Image> images = new ArrayList<Image>();
 		InputStream is;
 
 		ResultSet rs;
 		PreparedStatement ps = getPrepStmt("getThumb");
 
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
 
 			rs.close();
 			return images;
 
 		} catch (SQLException e) {
 			logger.warning("thumbGet failed with "+e.getMessage());
 		} catch (IOException e) {
 			logger.severe(e.getMessage());
 		}
 		return null;
 	}
 
 	public int size(String table){
 		reconnect();
 
 		ResultSet rs;
 		PreparedStatement ps = null;
 		
 		switch(table){
 		case "filter":
 			ps = getPrepStmt("sizeFilter");
 			break;
 			
 		case "cache":
 			ps = getPrepStmt("sizeCache");
 			break;
 		default:
 			throw new InvalidParameterException("Table "+ table +" not supported");
 		}
 
 		try {
 			rs = ps.executeQuery();
 
 			rs.next();
 			int size = rs.getInt(1);
 			return size;
 		} catch (SQLException e) {
 			logger.warning("MySql size lookup failed: "+e.getMessage());
 		}
 
 		return -1;
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
 		reconnect();
 
 		ResultSet rs;
 		PreparedStatement ps = getPrepStmt("isCached");
 
 		try {
 			ps.setString(1, uniqueID);
 			rs = ps.executeQuery();
 			Boolean isKnown = rs.next();
 			rs.close();
 			return isKnown;
 		} catch (SQLException e) {
 			logger.warning("MySql known lookup failed: "+e.getMessage());
 		}
 		return true;
 	}
 
 	public boolean isArchived(String hash){
 		reconnect();
 
 		ResultSet rs;
 		PreparedStatement ps = getPrepStmt("isArchive");
 
 		try {
 			ps.setString(1, hash);
 			rs = ps.executeQuery();
 			boolean b = rs.first();
 			rs.close();
 			return b; 
 		} catch (SQLException e) {
 			logger.warning("MySql Archive lookup failed: "+e.getMessage());
 		}
 
 		return true;
 	}
 
 	public boolean isDnw(String hash){
 		reconnect();
 
 		ResultSet rs;
 		PreparedStatement ps = getPrepStmt("isDnw");
 
 		try {
 			ps.setString(1, hash);
 			rs = ps.executeQuery();
 			boolean b = rs.first();
 			rs.close();
 			return b; 
 		} catch (SQLException e) {
 			logger.warning("MySql DNW lookup failed: "+e.getMessage());
 		}
 
 		return true;
 	}
 
 	public boolean isHashed(String hash){
 		reconnect();
 	
 		ResultSet rs;
 		PreparedStatement ps = getPrepStmt("isHashed");
 	
 		try {
 			ps.setString(1, hash);
 			rs = ps.executeQuery();
 			boolean b =rs.first();
 			rs.close();
 			return b; 
 		} catch (SQLException e) {
 			logger.warning("MySql exists lookup failed: "+e.getMessage());
 		}
 		return true;
 	}
 
 	public boolean isBlacklisted(String hash){
 		reconnect();
 	
 		ResultSet rs;
 		PreparedStatement ps = getPrepStmt("isBlacklisted");
 	
 		try {
 			ps.setString(1, hash);
 			rs = ps.executeQuery();
 			boolean b = rs.first();
 			rs.close();
 			return b;
 		} catch (SQLException e) {
 			logger.warning("MySql block lookup failed: "+e.getMessage());
 		}
 	
 		return false;
 	}
 
 	public void pruneCache(long maxAge){
 		reconnect();
 
 		PreparedStatement ps = getPrepStmt("prune");
 
 		try {
 			ps.setTimestamp(1,new Timestamp(maxAge));
 			ps.executeUpdate();
 		} catch (SQLException e) {
 			logger.warning("MySql known prune failed: "+e.getMessage());
 		}
 	}
 
 	public void delete(String table, String id){
 		reconnect();
 
 		PreparedStatement ps = null;
 
 		switch(table){
 		case "hash": 	
 			ps = getPrepStmt("deleteHash");
 			break;
 		
 		case "dnw" : 	
 			ps = getPrepStmt("deleteDnw");
 			break;
 			
 		case "filter":	
 			ps = getPrepStmt("deleteFilter");
 			break;
 			
 		case "block": 
 			ps = getPrepStmt("deleteBlock");
 			break;
 			
 		case "archive":
 			ps = getPrepStmt("deleteArchive");
 			break;
 		
 		default:
 			throw new InvalidParameterException("Table "+ table +" not supported");
 		}
 
 		try {
 			ps.setString(1, id);
 			ps.executeUpdate();
 		} catch (Exception e) {
 			logger.warning("MySql delete failed: "+e.getMessage());
 		}
 	}
 
 	public void sendStatement(String sqlStatment){
 		reconnect();
 		try {
 			Statement req = cn.createStatement();
 			req.execute(sqlStatment);
 			req.close();
 		} catch (SQLException e) {
 			logger.warning("failed to execute statement id: "+sqlStatment+"\n"+e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	private int[] addPath(String fullPath){
 	
 		ResultSet rs;
 	
 		PreparedStatement pathA = getPrepStmt("addDirectory");
 		PreparedStatement pathG = getPrepStmt("getDirectory");
 		PreparedStatement nameA = getPrepStmt("addFilename");
 		PreparedStatement nameG = getPrepStmt("getFilename");
 	
 	
 		try{
 			int split = fullPath.lastIndexOf("\\")+1;
 			String filename = fullPath.substring(split).toLowerCase(); // bar.txt
 			String path = fullPath.substring(0,split).toLowerCase(); // D:\foo\
 			int[] pathId = new int[2];
 	
 			pathG.setString(1, path);
 			rs = pathG.executeQuery();
 	
 			if(rs.next())
 				pathId[0] = rs.getInt(1);
 			else{
 				pathA.setString(1, path);
 				pathA.execute();
 	
 				rs = pathA.getGeneratedKeys();
 				rs.next();
 				pathId[0] = rs.getInt(1); 
 			}
 			rs.close();
 	
 			nameG.setString(1, filename);
 			rs = nameG.executeQuery();
 	
 			if(rs.next())
 				pathId[1] = rs.getInt(1);
 			else{
 				nameA.setString(1, filename);
 				nameA.execute();
 	
 				rs = nameA.getGeneratedKeys();
 				rs.next();
 				pathId[1] = rs.getInt(1); 
 			}
 	
 			rs.close();
 	
 			return pathId;
 		} catch (SQLException e) {
 			logger.severe(e.getMessage());
 		}
 	
 		return null;
 	}
 }
