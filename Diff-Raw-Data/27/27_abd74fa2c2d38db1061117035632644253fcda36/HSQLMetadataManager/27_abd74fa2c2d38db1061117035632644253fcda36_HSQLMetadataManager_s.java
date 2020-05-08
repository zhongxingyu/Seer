 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw_mannheim.cloudraid.metadatamgr.impl;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.config.exceptions.ConfigException;
 import de.dhbw_mannheim.cloudraid.config.exceptions.InvalidConfigValueException;
 import de.dhbw_mannheim.cloudraid.metadatamgr.IMetadataManager;
 
 /**
  * An implementation of the {@link IMetadataManager} for the HSQL database
  * system.
  * 
  * @author Florian Bausch, Markus Holtermann
  * 
  */
 public class HSQLMetadataManager implements IMetadataManager {
 
 	/**
 	 * 
 	 */
 	private PreparedStatement addUserStatement = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement authUserStatement = null;
 
 	/**
 	 * 
 	 */
 	private Connection con;
 
 	private ICloudRAIDConfig config = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement fileAddStmnt = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement fileByIdStmnt = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement fileDeleteStmnt = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement fileGetStmnt = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement fileUpdateStatusStmnt = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement fileUpdateStmnt = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement findNameStatement = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement getUserSaltStatement = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement changeUserPwdStatement = null;
 
 	/**
 	 * 
 	 */
 	private PreparedStatement listFiles = null;
 
 	/**
 	 * 
 	 */
 	private Statement statement = null;
 
 	@Override
 	public synchronized boolean addUser(String username, String password) {
 		try {
			if (!this.config.getBoolean("allowUserAdd", true)) {
 				return false;
 			}
 			this.getUserSaltStatement.setString(1, username);
 			this.getUserSaltStatement.execute();
 			ResultSet rs = this.getUserSaltStatement.getResultSet();
 			if (rs == null || !rs.next()) {
 				Random rnd = new Random(System.nanoTime());
 				byte[] salt = new byte[8];
 				byte[] pwdigest;
 				rnd.nextBytes(salt);
 				try {
 					MessageDigest digest = MessageDigest.getInstance("SHA-256");
 					digest.reset();
 					digest.update(salt);
 					pwdigest = digest.digest(password.getBytes("UTF-8"));
 				} catch (NoSuchAlgorithmException e) {
 					e.printStackTrace();
 					return false;
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 					return false;
 				}
 				this.addUserStatement.setString(1, username);
 				this.addUserStatement.setBytes(2, pwdigest);
 				this.addUserStatement.setBytes(3, salt);
 				this.addUserStatement.execute();
 				return true;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			try {
 				this.con.rollback();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		} catch (ConfigException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized int authUser(String username, String password) {
 		try {
 			this.getUserSaltStatement.setString(1, username);
 			this.getUserSaltStatement.execute();
 			ResultSet rs = this.getUserSaltStatement.getResultSet();
 			if (rs != null && rs.next()) {
 				byte[] salt = rs.getBytes("salt");
 				MessageDigest digest = MessageDigest.getInstance("SHA-256");
 				digest.reset();
 				digest.update(salt);
 				byte[] pwdigest = digest.digest(password.getBytes("UTF-8"));
 				this.authUserStatement.setString(1, username);
 				this.authUserStatement.setBytes(2, pwdigest);
 				this.authUserStatement.execute();
 				rs = this.authUserStatement.getResultSet();
 				if (rs != null && rs.next()) {
 					return rs.getInt("id");
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized boolean changeUserPwd(String username, String password,
 			int userId) {
 		Random rnd = new Random(System.nanoTime());
 		byte[] salt = new byte[8];
 		byte[] pwdigest;
 		rnd.nextBytes(salt);
 		try {
 			MessageDigest digest = MessageDigest.getInstance("SHA-256");
 			digest.reset();
 			digest.update(salt);
 			pwdigest = digest.digest(password.getBytes("UTF-8"));
 			this.changeUserPwdStatement.setBytes(1, pwdigest);
 			this.changeUserPwdStatement.setBytes(2, salt);
 			this.changeUserPwdStatement.setString(3, username);
 			this.changeUserPwdStatement.setInt(4, userId);
 			this.changeUserPwdStatement.execute();
 			if (this.changeUserPwdStatement.getUpdateCount() == 0) {
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			try {
 				this.con.rollback();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 			return false;
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			return false;
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public synchronized boolean connect(String database, String username,
 			String password) {
 		try {
 			Class.forName("org.hsqldb.jdbcDriver");
 			this.con = DriverManager.getConnection("jdbc:hsqldb:file:"
 					+ database + ";shutdown=true", username, password);
 			this.con.setAutoCommit(true);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public synchronized boolean disconnect() {
 		try {
 			if (this.statement != null) {
 				this.statement.execute("SHUTDOWN COMPACT;");
 			}
 			if (this.con != null) {
 				this.con.commit();
 			}
 		} catch (SQLException e) {
 		}
 
 		this.statement = null;
 		this.addUserStatement = null;
 		this.authUserStatement = null;
 
 		this.fileAddStmnt = null;
 		this.fileDeleteStmnt = null;
 		this.fileGetStmnt = null;
 		this.fileUpdateStatusStmnt = null;
 		this.fileUpdateStmnt = null;
 
 		this.findNameStatement = null;
 
 		this.getUserSaltStatement = null;
 
 		this.listFiles = null;
 
 		try {
 			if (this.con != null) {
 				this.con.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public ResultSet fileById(int fileId) {
 		try {
 			this.fileByIdStmnt.setInt(1, fileId);
 			this.fileByIdStmnt.execute();
 			ResultSet rs = this.fileByIdStmnt.getResultSet();
 			if (rs != null && rs.next()) {
 				return rs;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public synchronized int fileDelete(int fileId) {
 		try {
 			this.fileDeleteStmnt.setInt(1, fileId);
 			this.fileDeleteStmnt.execute();
 			return this.fileDeleteStmnt.getUpdateCount();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return -1;
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 	@Override
 	public synchronized ResultSet fileGet(String path, int userId) {
 		try {
 			this.fileGetStmnt.setString(1, path);
 			this.fileGetStmnt.setInt(2, userId);
 			this.fileGetStmnt.execute();
 			ResultSet rs = this.fileGetStmnt.getResultSet();
 			if (rs != null && rs.next()) {
 				return rs;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public synchronized ResultSet fileList(int userId) {
 		try {
 			this.listFiles.setInt(1, userId);
 			this.listFiles.execute();
 			return this.listFiles.getResultSet();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public int fileNew(String path, String hash, long lastMod, int userId) {
 		try {
 			this.fileAddStmnt.setString(1, path);
 			this.fileAddStmnt.setString(2, hash);
 			this.fileAddStmnt.setTimestamp(3, new Timestamp(lastMod));
 			this.fileAddStmnt.setString(4,
 					IMetadataManager.FILE_STATUS.UPLOADING.toString());
 			this.fileAddStmnt.setInt(5, userId);
 			this.fileAddStmnt.execute();
 			ResultSet rs = this.fileAddStmnt.getGeneratedKeys();
 			if (rs != null && rs.next()) {
 				return rs.getInt(1); // The generated file ID
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			try {
 				this.con.rollback();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	@Override
 	public boolean fileUpdate(int id, String path, String hash, long lastMod,
 			int userId) {
 		try {
 			this.fileUpdateStmnt.setString(1, path);
 			this.fileUpdateStmnt.setString(2, hash);
 			this.fileUpdateStmnt.setTimestamp(3, new Timestamp(lastMod));
 			this.fileUpdateStmnt.setString(4,
 					IMetadataManager.FILE_STATUS.UPLOADING.toString());
 			this.fileUpdateStmnt.setInt(5, userId);
 			this.fileUpdateStmnt.setInt(6, id);
 			this.fileUpdateStmnt.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			try {
 				this.con.rollback();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	@Override
 	public boolean fileUpdateState(int id, FILE_STATUS state) {
 		try {
 			this.fileUpdateStatusStmnt.setString(1, state.toString());
 			this.fileUpdateStatusStmnt.setInt(2, id);
 			this.fileUpdateStatusStmnt.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized String getHash(String path, int userId) {
 		try {
 			this.fileGetStmnt.setString(1, path);
 			this.fileGetStmnt.setInt(2, userId);
 			this.fileGetStmnt.execute();
 			ResultSet rs = this.fileGetStmnt.getResultSet();
 			if (rs != null && rs.next()) {
 				return rs.getString("hash_name");
 			}
 			return null;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public synchronized long getLastMod(String path, int userId) {
 		try {
 			this.fileGetStmnt.setString(1, path);
 			this.fileGetStmnt.setInt(2, userId);
 			this.fileGetStmnt.execute();
 			ResultSet rs = this.fileGetStmnt.getResultSet();
 			if (rs != null && rs.next()) {
 				return rs.getTimestamp("last_mod").getTime();
 			}
 			return -1L;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return -1L;
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			return -1L;
 		}
 	}
 
 	@Override
 	public synchronized String getName(String hash, int userId) {
 		try {
 			this.findNameStatement.setString(1, hash);
 			this.findNameStatement.setInt(2, userId);
 			this.findNameStatement.execute();
 			ResultSet rs = this.findNameStatement.getResultSet();
 			if (rs != null && rs.next()) {
 				return rs.getString("path_name");
 			}
 			return null;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public synchronized boolean initialize() {
 		try {
 			this.statement = this.con.createStatement();
 			String createTable = "CREATE CACHED TABLE IF NOT EXISTS cloudraid_users ("
 					+ "id INTEGER IDENTITY, "
 					+ "username VARCHAR(32) NOT NULL, "
 					+ "password BLOB(32) NOT NULL, "
 					+ "salt BLOB(8) NOT NULL"
 					+ ");";
 			this.statement.execute(createTable);
 
 			createTable = "CREATE CACHED TABLE IF NOT EXISTS cloudraid_files ("
 					+ "id INTEGER IDENTITY, "
 					+ "path_name VARCHAR(512) NOT NULL, "
 					+ "hash_name VARCHAR(256) NOT NULL, "
 					+ "last_mod TIMESTAMP NOT NULL, "
 					+ "status VARCHAR(32) NOT NULL, "
 					+ "user_id INTEGER NULL, "
 					+ "FOREIGN KEY ( user_id ) REFERENCES cloudraid_users ( id ),"
 					+ "UNIQUE ( user_id, path_name ) );";
 			this.statement.execute(createTable);
 
 			this.addUserStatement = this.con
 					.prepareStatement("INSERT INTO cloudraid_users VALUES (NULL, ?, ?, ? );");
 			this.authUserStatement = this.con
 					.prepareStatement("SELECT * FROM cloudraid_users WHERE username = ? AND password = ?");
 
 			this.fileAddStmnt = this.con
 					.prepareStatement(
 							"INSERT INTO cloudraid_files VALUES (NULL, ?, ?, ?, ?, ? );",
 							Statement.RETURN_GENERATED_KEYS);
 			this.fileByIdStmnt = this.con
 					.prepareStatement("SELECT * FROM cloudraid_files WHERE id = ? ;");
 			this.fileDeleteStmnt = this.con
 					.prepareStatement("DELETE FROM cloudraid_files WHERE id = ? ;");
 			this.fileGetStmnt = this.con
 					.prepareStatement("SELECT * FROM cloudraid_files WHERE path_name = ? AND user_id = ? ;");
 			this.fileUpdateStatusStmnt = this.con
 					.prepareStatement("UPDATE cloudraid_files SET status = ? WHERE id = ? ;");
 			this.fileUpdateStmnt = this.con
 					.prepareStatement("UPDATE cloudraid_files SET path_name = ? , hash_name = ? , last_mod = ? , status = ? , user_id = ? WHERE id = ? ;");
 
 			this.findNameStatement = this.con
 					.prepareStatement("SELECT * FROM cloudraid_files WHERE hash_name = ? AND user_id = ?;");
 
 			this.getUserSaltStatement = this.con
 					.prepareCall("SELECT salt FROM cloudraid_users WHERE username = ?");
 
 			this.changeUserPwdStatement = this.con
 					.prepareStatement("UPDATE cloudraid_users SET password = ?, salt = ? WHERE username = ? AND id = ? ;");
 
 			this.listFiles = this.con
 					.prepareStatement("SELECT * FROM cloudraid_files WHERE user_id = ?;");
 
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			try {
 				this.con.rollback();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 			return false;
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * @param config
 	 */
	protected synchronized void setConfig(ICloudRAIDConfig config) {
 		System.out.println("HSQLMetadataManager: setConfig: begin");
 		this.config = config;
 		System.out.println("HSQLMetadataManager: setConfig: " + this.config);
 		System.out.println("HSQLMetadataManager: setConfig: end");
 	}
 
 	/**
 	 * 
 	 */
 	protected void shutdown() {
 		System.out.println("HSQLMetadataManager: shutdown: begin");
 		this.disconnect();
 		System.out.println("HSQLMetadataManager: shutdown: end");
 	}
 
 	/**
 	 * 
 	 */
 	protected void startup() {
 		System.out.println("HSQLMetadataManager: startup: begin");
 		try {
 			String database = this.config.getString("database.name", null);
 			if (database != null) {
 				String username = this.config.getString("database.username",
 						"SA");
 				String password = this.config
 						.getString("database.password", "");
 				this.connect(database, username, password);
 				this.initialize();
 			} else {
 				System.err
 						.println("No database specified. You need to set database.name");
 			}
 		} catch (NoSuchElementException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigValueException e) {
 			e.printStackTrace();
 		}
 		System.out.println("HSQLMetadataManager: startup: end");
 	}
 
 	/**
 	 * @param config
 	 */
	protected synchronized void unsetConfig(ICloudRAIDConfig config) {
 		System.out.println("CloudRAIDService: unsetConfig: begin");
 		System.out.println("CloudRAIDService: unsetConfig: " + config);
 		this.config = null;
 		System.out.println("CloudRAIDService: unsetConfig: " + this.config);
 		System.out.println("CloudRAIDService: unsetConfig: end");
 	}
 
 }
