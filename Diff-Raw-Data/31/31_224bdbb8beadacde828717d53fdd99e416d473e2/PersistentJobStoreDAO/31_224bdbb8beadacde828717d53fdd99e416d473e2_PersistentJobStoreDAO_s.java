 /*
  * $Id$
  *
  * Authors:
  *      Jeff Buchbinder <jeff@freemedsoftware.org>
  *
  * FreeMED Electronic Medical Record / Practice Management System
  * Copyright (C) 1999-2010 FreeMED Software Foundation
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package org.freemedsoftware.device;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.tmatesoft.sqljet.core.SqlJetException;
 import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
 import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
 import org.tmatesoft.sqljet.core.table.ISqlJetTable;
 import org.tmatesoft.sqljet.core.table.SqlJetDb;
 
 import com.google.gson.Gson;
 
 public class PersistentJobStoreDAO {
 
 	static final Logger log = Logger.getLogger(PersistentJobStoreDAO.class);
 
 	protected static SqlJetDb db = null;
 
 	private static final String TABLE_NAME = "jobstore";
 
 	private static final String JOB_STORE_CREATE_SQL = "CREATE TABLE "
 			+ TABLE_NAME + " ( " + " id INTEGER NOT NULL PRIMARY KEY"
 			+ " , device TEXT NOT NULL " + " , status TEXT NOT NULL "
 			+ " , displayText TEXT " + " , printTemplate TEXT "
 			+ " , printParameters TEXT "
 			+ " , printCount INT NOT NULL DEFAULT 1 " + " , sigraw BLOB "
 			+ " , sigimage BLOB " + " ) ; ";
 
 	private static final String JOB_STORE_INDEX_ID_SQL = "CREATE INDEX id_index ON "
 			+ TABLE_NAME + "(id)";
 
 	private static final String JOB_STORE_INDEX_STATUS_SQL = "CREATE INDEX status_index ON "
 			+ TABLE_NAME + "(status)";
 
 	public static void open(String fileName) throws SqlJetException {
 		db = SqlJetDb.open(new File(fileName), true);
 		db.beginTransaction(SqlJetTransactionMode.WRITE);
 		try {
 			db.getOptions().setUserVersion(1);
 		} catch (Throwable t) {
 			log.error(t);
 			throw new SqlJetException(t);
 		} finally {
 			db.commit();
 		}
 	}
 
 	public static void create(String fileName) throws SqlJetException {
 		File dbFile = new File(fileName);
 		dbFile.delete();
 
 		db = SqlJetDb.open(dbFile, true);
 		db.getOptions().setAutovacuum(true);
 		db.beginTransaction(SqlJetTransactionMode.WRITE);
 		try {
 			db.getOptions().setUserVersion(1);
 		} catch (Throwable t) {
 			log.error(t);
 			throw new SqlJetException(t);
 		} finally {
 			db.commit();
 		}
 
 		db.beginTransaction(SqlJetTransactionMode.WRITE);
 		try {
 			db.createTable(JOB_STORE_CREATE_SQL);
 			db.createIndex(JOB_STORE_INDEX_ID_SQL);
 			db.createIndex(JOB_STORE_INDEX_STATUS_SQL);
 		} catch (Throwable t) {
 			log.error(t);
 			throw new SqlJetException(t);
 		} finally {
 			db.commit();
 		}
 	}
 
 	public static void delete(Integer id) throws SqlJetException {
 		synchronized (db) {
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			try {
 				ISqlJetTable table = db.getTable(TABLE_NAME);
 				ISqlJetCursor deleteCursor = table.scope(table
 						.getPrimaryKeyIndexName(), new Object[] { id },
 						new Object[] { id });
 				while (!deleteCursor.eof()) {
 					if (deleteCursor.getInteger("id") == id) {
 						deleteCursor.delete();
 					}
 				}
 				deleteCursor.close();
 			} catch (Throwable t) {
 				log.error(t);
 				throw new SqlJetException(t);
 			} finally {
 				db.commit();
 			}
 		}
 	}
 
 	public static JobStoreItem get(Integer id) throws SqlJetException {
 		synchronized (db) {
 			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 			try {
 				ISqlJetTable table = db.getTable(TABLE_NAME);
 				ISqlJetCursor cursor = table.lookup("id_index", id);
 
 				JobStoreItem item = new JobStoreItem();
 				item.setId(id);
 				item.setDevice(cursor.getString("device"));
 				item.setStatus(cursor.getString("status"));
 				item.setDisplayText(cursor.getString("displayText"));
 				item.setPrintTemplate(cursor.getString("printTemplate"));
 				item.setPrintParameters(deserializeMap(cursor
 						.getString("printParameters")));
 				item.setPrintCount((int) cursor.getInteger("printCount"));
 				item.setSignatureRaw(cursor.getBlobAsArray("sigraw"));
 				item.setSignatureImage(cursor.getBlobAsArray("sigimage"));
 
 				cursor.close();
 
 				return item;
 			} catch (Throwable t) {
 				log.error(t);
 				throw new SqlJetException(t);
 			} finally {
 				db.commit();
 			}
 		}
 	}
 
 	public static List<JobStoreItem> unassignedJobs() throws SqlJetException {
 		synchronized (db) {
 			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 			try {
 				ISqlJetTable table = db.getTable(TABLE_NAME);
 				ISqlJetCursor cursor = table.lookup("status_index",
 						JobStoreItem.STATUS_NEW);
 
 				List<JobStoreItem> items = new ArrayList<JobStoreItem>();
 				if (cursor.getRowCount() < 1) {
 					return items;
 				}
 
 				do {
 					JobStoreItem item = new JobStoreItem();
 					item.setId((int) cursor.getInteger("id"));
 					item.setDevice(cursor.getString("device"));
 					item.setStatus(cursor.getString("status"));
 					item.setDisplayText(cursor.getString("displayText"));
 					item.setPrintTemplate(cursor.getString("printTemplate"));
 					item.setPrintParameters(deserializeMap(cursor
 							.getString("printParameters")));
 					item.setPrintCount((int) cursor.getInteger("printCount"));
 					item.setSignatureRaw(cursor.getBlobAsArray("sigraw"));
 					item.setSignatureImage(cursor.getBlobAsArray("sigimage"));
 					items.add(item);
 				} while (cursor.next());
 
 				cursor.close();
 
 				return items;
 			} catch (Throwable t) {
 				log.debug(t);
 				throw new SqlJetException(t);
 			} finally {
 				db.commit();
 			}
 		}
 	}
 
 	public static int insert(JobStoreItem i) throws SqlJetException {
 		synchronized (db) {
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			try {
 				ISqlJetTable table = db.getTable(TABLE_NAME);
 				return (int) table.insert(i.getId(), i.getDevice(), i
 						.getStatus(), i.getDisplayText(), i.getPrintTemplate(),
 						serializeMap(i.getPrintParameters()),
 						i.getPrintCount(), i.getSignatureRaw(), i
 								.getSignatureImage());
 			} catch (Exception t) {
 				log.error(t);
 				throw new SqlJetException(t);
 			} finally {
 				db.commit();
 			}
 		}
 	}
 
 	public static void update(JobStoreItem i) throws SqlJetException {
 		synchronized (db) {
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			try {
 				ISqlJetTable table = db.getTable(TABLE_NAME);
 				ISqlJetCursor updateCursor = table.scope(table
 						.getPrimaryKeyIndexName(), new Object[] { i.getId() },
 						new Object[] { i.getId() });
 				do {
 					if (updateCursor.getInteger("id") == i.getId()) {
 						updateCursor.update(i.getId(), i.getDevice(), i
 								.getStatus(), i.getDisplayText(), i
 								.getPrintTemplate(), serializeMap(i
 								.getPrintParameters()), i.getPrintCount(), i
 								.getSignatureRaw(), i.getSignatureImage());
 					}
 				} while (updateCursor.next());
 				updateCursor.close();
 			} catch (Throwable t) {
 				log.error(t);
 				throw new SqlJetException(t);
 			} finally {
 				db.commit();
 			}
 		}
 	}
 
 	public static void close() {
 		if (db != null) {
 			try {
 				db.close();
 			} catch (SqlJetException e) {
 				log.error(e);
 			}
 		}
 	}
 
 	private static String serializeMap(HashMap<String, String> map) {
 		try {
 			return new Gson().toJson(map);
 		} catch (Exception ex) {
			log.trace(ex);
 			return "{}";
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private static HashMap<String, String> deserializeMap(String raw) {
 		try {
 			if (raw == null || raw == "" || raw == "{}") {
 				return (HashMap<String, String>) null;
 			}
 			return (HashMap<String, String>) new Gson().fromJson(raw,
					HashMap.class);
 		} catch (Exception e) {
			log.trace(e);
 			return (HashMap<String, String>) null;
 		}
 	}
 
 }
