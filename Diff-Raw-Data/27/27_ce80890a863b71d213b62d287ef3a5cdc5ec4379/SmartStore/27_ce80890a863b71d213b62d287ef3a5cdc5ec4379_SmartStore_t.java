 /*
  * Copyright (c) 2011, salesforce.com, inc.
  * All rights reserved.
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  * - Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of salesforce.com, inc. nor the names of its contributors
  * may be used to endorse or promote products derived from this software without
  * specific prior written permission of salesforce.com, inc.
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.salesforce.androidsdk.store;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.text.TextUtils;
 
 
 /**
  * Smart store 
  * 
  * Provides a secure means for SalesforceMobileSDK Container-based applications to store objects in a persistent
  * and searchable manner. Similar in some ways to CouchDB, SmartStore stores documents as JSON values.  
  * SmartStore is inspired by the Apple Newton OS Soup/Store model. 
  * The main challenge here is how to effectively store documents with dynamic fields, and still allow indexing and searching.
  */
 /**
  * @author wmathurin
  *
  */
 public class SmartStore  {
 	// Default
 	public static final int DEFAULT_PAGE_SIZE = 10;
 
 	// Table to keep track of soup names
 	protected static final String SOUP_NAMES_TABLE = "soup_names";
 	
 	// Table to keep track of soup's index specs
 	protected static final String SOUP_INDEX_MAP_TABLE = "soup_index_map";
 	
 	// Columns of the soup index map table
 	protected static final String SOUP_NAME_COL = "soupName";
 	protected static final String PATH_COL = "path";
 	protected static final String COLUMN_NAME_COL = "columnName";
 	protected static final String COLUMN_TYPE_COL = "columnType";
 	
 	// Columns of a soup table
 	protected static final String ID_COL = "id";
 	protected static final String CREATED_COL = "created";
 	protected static final String LAST_MODIFIED_COL = "lastModified";
 	protected static final String SOUP_COL = "soup";
 
 	// JSON fields added to soup element on insert/update 
 	public static final String SOUP_ENTRY_ID = "_soupEntryId";
 	public static final String SOUP_LAST_MODIFIED_DATE = "_soupLastModifiedDate";
 	
 	// Backing database
 	protected Database db;
 	
 	/**
 	 * Create soup index map table to keep track of soups' index specs
 	 * Create soup name map table to keep track of soup name to table name mappings
 	 * Called when the database is first created
 	 * 
 	 * @param db
 	 */
 	public static void createMetaTables(Database db) {
 		// Create soup_index_map table
 		StringBuilder sb = new StringBuilder();
 		sb.append("CREATE TABLE ").append(SOUP_INDEX_MAP_TABLE).append(" (") 
 				  	.append(SOUP_NAME_COL).append(" TEXT")
 				  	.append(",").append(PATH_COL).append(" TEXT")
 				  	.append(",").append(COLUMN_NAME_COL).append(" TEXT")
 				  	.append(",").append(COLUMN_TYPE_COL).append(" TEXT")
 				  	.append(")");
 		db.execSQL(sb.toString());
 		// Add index on soup_name column
 		db.execSQL(String.format("CREATE INDEX %s on %s ( %s )", SOUP_INDEX_MAP_TABLE + "_0", SOUP_INDEX_MAP_TABLE, SOUP_NAME_COL));
 		
 		// Create soup_names table
 		// The table name for the soup will simply be table_<soupId>
 		sb = new StringBuilder();
 		sb.append("CREATE TABLE ").append(SOUP_NAMES_TABLE).append(" (") 
 					.append(ID_COL).append(" INTEGER PRIMARY KEY AUTOINCREMENT") 
 					.append(",").append(SOUP_NAME_COL).append(" TEXT")
 				  	.append(")");
 		db.execSQL(sb.toString());		
 		// Add index on soup_name column
 		db.execSQL(String.format("CREATE INDEX %s on %s ( %s )", SOUP_NAMES_TABLE + "_0", SOUP_NAMES_TABLE, SOUP_NAME_COL));
 	}
 
 	/**
 	 * @param db
 	 */
 	public SmartStore(Database db) {
 		this.db = db;
 	}
 
 	/**
 	 * Start transaction
 	 */
 	public void beginTransaction() {
 		db.beginTransaction();
 	}
 
 	/**
 	 * End transaction (commit or rollback)
 	 */
 	public void endTransaction() {
 		db.endTransaction();
 	}
 	
 	/**
 	 * Mark transaction as successful (next call to endTransaction will be a commit)
 	 */
 	public void setTransactionSuccessful() {
 		db.setTransactionSuccessful();
 	}
 
 	
 	/**
 	 * Register a soup 
 	 * 
 	 * Create table for soupName with a column for the soup itself and columns for paths specified in indexSpecs
 	 * Create indexes on the new table to make lookup faster
 	 * Create rows in soup index map table for indexSpecs
 	 * @param soupName
 	 * @param indexSpecs
 	 */
 	public void registerSoup(String soupName, IndexSpec[] indexSpecs) {
 		if (soupName == null) throw new SmartStoreException("Bogus soup name:" + soupName);
 		if (indexSpecs.length == 0) throw new SmartStoreException("No indexSpecs specified for soup: " + soupName);
 		if (hasSoup(soupName)) return; // soup already exist - do nothing
 
 		// First get a table name
 		String soupTableName = null;
 		ContentValues soupMapValues = new ContentValues();
 		soupMapValues.put(SOUP_NAME_COL, soupName);
 		try {
 			db.beginTransaction();
 			long soupId = db.insert(SOUP_NAMES_TABLE, soupMapValues);
 			soupTableName = getSoupTableName(soupId);
 			db.setTransactionSuccessful();
 		}
 		finally {
 			db.endTransaction();
 		}
 		
 		// Prepare SQL for creating soup table and its indices
 		StringBuilder createTableStmt = new StringBuilder();          // to create new soup table
 		List<String> createIndexStmts = new ArrayList<String>();      // to create indices on new soup table
 		List<ContentValues> soupIndexMapInserts = new ArrayList<ContentValues>();  // to be inserted in soup index map table
 		
 		createTableStmt.append("CREATE TABLE ").append(soupTableName).append(" (")
 						.append(ID_COL).append(" INTEGER PRIMARY KEY AUTOINCREMENT")
 					    .append(", ").append(SOUP_COL).append(" TEXT")
 					    .append(", ").append(CREATED_COL).append(" INTEGER")
 					    .append(", ").append(LAST_MODIFIED_COL).append(" INTEGER");
 		
 		int i = 0;
 		for (IndexSpec indexSpec : indexSpecs) {
 			// for create table
 			String columnName = soupTableName + "_" + i;
 			String columnType = indexSpec.type.getColumnType();
 			createTableStmt.append(", ").append(columnName).append(" ").append(columnType);
 			
 			// for insert
 			ContentValues values = new ContentValues();
 			values.put(SOUP_NAME_COL, soupName);
 			values.put(PATH_COL, indexSpec.path);
 			values.put(COLUMN_NAME_COL, columnName);
 			values.put(COLUMN_TYPE_COL, indexSpec.type.toString());
 			soupIndexMapInserts.add(values);
 			
 			// for create index
 			String indexName = soupTableName + "_" + i + "_idx";
 			createIndexStmts.add(String.format("CREATE INDEX %s on %s ( %s )", indexName, soupTableName, columnName));;
 			
 			i++;
 		}
 		createTableStmt.append(")");
 		
 		// Run SQL for creating soup table and its indices
 		db.execSQL(createTableStmt.toString());
 		for (String createIndexStmt : createIndexStmts) {
 			db.execSQL(createIndexStmt.toString());
 		}
 		
 		try {
 			db.beginTransaction();
 			for (ContentValues values : soupIndexMapInserts) {
 				db.insert(SOUP_INDEX_MAP_TABLE, values);
 			}
 			db.setTransactionSuccessful();
 		}
 		finally {
 			db.endTransaction();
 		}
 		
 	}
 	
 	/**
 	 * Check if soup exists
 	 * 
 	 * @param soupName
 	 * @return true if soup exists, false otherwise
 	 */
 	public boolean hasSoup(String soupName) {
 		return getSoupTableName(soupName) != null;
 	}
 	
 	/**
 	 * 
 	 * @param soupName
 	 * @return table name for a given soup or null if the soup doesn't exist
 	 */
 	public String getSoupTableName(String soupName) {
 		Cursor cursor = null;
 		try {
 			cursor = db.query(SOUP_NAMES_TABLE, new String[] {ID_COL}, null, null, getSoupNamePredicate(), soupName);
 			if (!cursor.moveToFirst()) {
 				return null;
 			}
 			return getSoupTableName(cursor.getLong(cursor.getColumnIndex(ID_COL)));
 		}
 		finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 	
 	/**
 	 * Destroy a soup
 	 * 
 	 * Drop table for soupName 
 	 * Cleanup entries in soup index map table  
 	 * @param soupName
 	 */
 	public void dropSoup(String soupName) {
 		String soupTableName = getSoupTableName(soupName);
 		if (soupTableName != null) {
 			db.execSQL("DROP TABLE IF EXISTS " + soupTableName);
 			try {
 				db.beginTransaction();
 				db.delete(SOUP_NAMES_TABLE, getSoupNamePredicate(), soupName);
 				db.delete(SOUP_INDEX_MAP_TABLE, getSoupNamePredicate(), soupName);
 				db.setTransactionSuccessful();
 			}
 			finally {
 				db.endTransaction();
 			}
 		}
 	}
 
 	/**
 	 * Run a query
 	 * @param soupName
 	 * @param querySpec
 	 * @param pageIndex
 	 * @return
 	 * @throws JSONException 
 	 */
 	public JSONArray querySoup(String soupName, QuerySpec querySpec, int pageIndex) throws JSONException {
 		String soupTableName = getSoupTableName(soupName);
 		if (soupTableName == null) throw new SmartStoreException("Soup: " + soupName + " does not exist");
 		
 		// Page
 		int offsetRows = querySpec.pageSize * pageIndex;
 		int numberRows = querySpec.pageSize;
 		String limit = offsetRows + "," + numberRows;
 		
 		// Get the matching soups
 		Cursor cursor = null;
 		try {
			if (querySpec.path == null) {
				cursor = db.query(soupTableName, new String[] {SOUP_COL}, null, limit, null);				
			}
			else { 
				String columnName = getColumnNameForPath(db, soupName, querySpec.path);
				cursor = db.query(soupTableName, new String[] {SOUP_COL}, querySpec.getOrderBy(columnName), limit, querySpec.getKeyPredicate(columnName), querySpec.getKeyPredicateArgs());
			}
 			
 			JSONArray results = new JSONArray();
 			if (cursor.moveToFirst()) {
 				do {
 					results.put(new JSONObject(cursor.getString(cursor.getColumnIndex(SOUP_COL))));
 				}
 				while (cursor.moveToNext());
 			}
 			
 			return results;			
 		}
 		finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 	
 	/**
 	 * @param soupName
 	 * @param querySpec
 	 * @return count for the given querySpec
 	 * @throws JSONException
 	 */
 	public int countQuerySoup(String soupName, QuerySpec querySpec) throws JSONException {
 		String soupTableName = getSoupTableName(soupName);
 		if (soupTableName == null) throw new SmartStoreException("Soup: " + soupName + " does not exist");
 		
 		Cursor cursor = null;
 		try {
			if (querySpec.path == null) {
				cursor = db.countQuery(soupTableName, null, null);
			}
			else {
				String columnName = getColumnNameForPath(db, soupName, querySpec.path);
				cursor = db.countQuery(soupTableName, querySpec.getKeyPredicate(columnName), querySpec.getKeyPredicateArgs());
			}
 			
 			if (cursor.moveToFirst()) {
 				return cursor.getInt(0);
 			}
 			else {
 				return -1; 
 			}
 		}
 		finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 	
 
 	/**
 	 * Create (and commits) 
 	 * @param soupName
 	 * @param soupElt
 	 * @return soupElt created or null if creation failed
 	 * @throws JSONException 
 	 */
 	public JSONObject create(String soupName, JSONObject soupElt) throws JSONException {
 		return create(soupName, soupElt, true);
 	}
 	
 	/**
 	 * Create
 	 * @param soupName
 	 * @param soupElt
 	 * @return
 	 * @throws JSONException
 	 */
 	public JSONObject create(String soupName, JSONObject soupElt, boolean handleTx) throws JSONException {
 		String soupTableName = getSoupTableName(soupName);
 		if (soupTableName == null) throw new SmartStoreException("Soup: " + soupName + " does not exist");
 		IndexSpec[] indexSpecs = getIndexSpecs(db, soupName);
 		
 		try {
 			if (handleTx) {
 				db.beginTransaction();
 			}
 
 			long now = System.currentTimeMillis();
 			ContentValues contentValues = new ContentValues();
 			contentValues.put(SOUP_COL, ""); 
 			contentValues.put(CREATED_COL, now);
 			contentValues.put(LAST_MODIFIED_COL, now);
 			for (IndexSpec indexSpec : indexSpecs) {
 				contentValues.put(indexSpec.columnName, (String) project(soupElt, indexSpec.path));
 			}
 			long soupEntryId = db.insert(soupTableName, contentValues); // insert without the soup to get the id
 			
 			// Adding fields to soup element
 			// Cloning to not modify the one passed in (inefficient?)
 			JSONObject soupEltCreated = new JSONObject(soupElt.toString());
 			soupEltCreated.put(SOUP_ENTRY_ID, soupEntryId);
 			soupEltCreated.put(SOUP_LAST_MODIFIED_DATE, now);
 			
 			// Updating soup column
 			contentValues = new ContentValues();
 			contentValues.put(SOUP_COL, soupEltCreated.toString());
 			
 			// Updating database
 			boolean success = db.update(soupTableName, contentValues, getSoupEntryIdPredicate(), soupEntryId + "") == 1;
 			
 			// Commit if successful
 			if (success) {
 				if (handleTx) {
 					db.setTransactionSuccessful();
 				}
 				return soupEltCreated;
 			}
 			else {
 				return null;
 			}
 		}
 		finally {
 			if (handleTx) {
 				db.endTransaction();
 			}
 		}
 	}
 
 	/**
 	 * Retrieve
 	 * @param soupName
 	 * @param soupEntryIds
 	 * @return JSONArray of JSONObject's with the given soupEntryIds
 	 * @throws JSONException 
 	 */
 	public JSONArray retrieve(String soupName, Long... soupEntryIds) throws JSONException {
 		String soupTableName = getSoupTableName(soupName);
 		if (soupTableName == null) throw new SmartStoreException("Soup: " + soupName + " does not exist");
 		Cursor cursor = null;
 		try {
 			JSONArray result = new JSONArray();
 			cursor = db.query(soupTableName, new String[] {SOUP_COL}, null, null, getSoupEntryIdsPredicate(soupEntryIds), (String[]) null);
 			if (!cursor.moveToFirst()) {
 				return result;
 			}
 			do {
 				String raw = cursor.getString(cursor.getColumnIndex(SOUP_COL));
 				result.put(new JSONObject(raw));
 			} 
 			while (cursor.moveToNext());
 			
 			return result;
 		}
 		finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 	
 
 	/**
 	 * Update (and commits) 
 	 * @param soupName
 	 * @param soupElt
 	 * @param soupEntryId
 	 * @return soupElt updated or null if update failed
 	 * @throws JSONException 
 	 */
 	public JSONObject update(String soupName, JSONObject soupElt, long soupEntryId) throws JSONException {
 		return update(soupName, soupElt, soupEntryId, true);
 	}
 	
 	/**
 	 * Update
 	 * @param soupName
 	 * @param soupElt
 	 * @param soupEntryId
 	 * @return
 	 * @throws JSONException
 	 */
 	public JSONObject update(String soupName, JSONObject soupElt, long soupEntryId, boolean handleTx) throws JSONException {
 		String soupTableName = getSoupTableName(soupName);
 		if (soupTableName == null) throw new SmartStoreException("Soup: " + soupName + " does not exist");
 		IndexSpec[] indexSpecs = getIndexSpecs(db, soupName);
 		
 		long now = System.currentTimeMillis();
 		
 		// Updating last modified field in soup element
 		// Cloning to not modify the one passed in (inefficient?)
 		JSONObject soupEltUpdated = new JSONObject(soupElt.toString());
 		soupEltUpdated.put(SOUP_LAST_MODIFIED_DATE, now);
 
 		// Preparing data for row
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(SOUP_COL, soupEltUpdated.toString());
 		contentValues.put(LAST_MODIFIED_COL, now);
 		for (IndexSpec indexSpec : indexSpecs) {
 			contentValues.put(indexSpec.columnName, (String) project(soupElt, indexSpec.path));
 		}
 		
 		try {
 			if (handleTx) {
 				db.beginTransaction();
 			}
 			boolean success = db.update(soupTableName, contentValues, getSoupEntryIdPredicate(), soupEntryId + "") == 1;
 			if (success) {
 				if (handleTx) {
 					db.setTransactionSuccessful();
 				}
 				return soupEltUpdated;
 			}
 			else {
 				return null;
 			}
 		}
 		finally {
 			if (handleTx) {
 				db.endTransaction();
 			}
 		}
 	}
 
 	/**
 	 * Upsert (and commits)
 	 * @param soupName
 	 * @param soupElt
 	 * @return soupElt upserted or null if upsert failed
 	 * @throws JSONException 
 	 */
 	public JSONObject upsert(String soupName, JSONObject soupElt) throws JSONException {
 		return upsert(soupName, soupElt, true);
 	}
 	
 	/**
 	 * Upsert
 	 * @param soupName
 	 * @param soupElt
 	 * @param handleTx
 	 * @return
 	 * @throws JSONException
 	 */
 	public JSONObject upsert(String soupName, JSONObject soupElt, boolean handleTx) throws JSONException {
 		if (soupElt.has(SOUP_ENTRY_ID)) {
 			long entryId = soupElt.getLong(SOUP_ENTRY_ID);
 			return update(soupName, soupElt, entryId, handleTx);
 		}
 		else {
 			return create(soupName, soupElt, handleTx);
 		}
 	}
 	
 	
 	/**
 	 * Delete (and commits)
 	 * @param soupName
 	 * @param soupEntryIds
 	 */
 	public void delete(String soupName, Long... soupEntryIds) {
 		delete(soupName, soupEntryIds, true);
 	}
 	
 	/**
 	 * Delete
 	 * @param soupName
 	 * @param soupEntryId
 	 * @param handleTx
 	 */
 	public void delete(String soupName, Long[] soupEntryIds, boolean handleTx) {
 		String soupTableName = getSoupTableName(soupName);
 		if (soupTableName == null) throw new SmartStoreException("Soup: " + soupName + " does not exist");
 		
 		if (handleTx) {
 			db.beginTransaction();
 		}
 
 		try {
 			db.delete(soupTableName, getSoupEntryIdsPredicate(soupEntryIds), (String []) null);
 			if (handleTx) {
 				db.setTransactionSuccessful();
 			}
 		}
 		finally {
 			if (handleTx) {
 				db.endTransaction();
 			}
 		}
 	}
 
 	/**
 	 * Return column name in soup table that holds the soup projection for path
 	 * @param db
 	 * @param soupName
 	 * @param path
 	 * @return
 	 */
 	protected String getColumnNameForPath(Database db, String soupName, String path) {
 		Cursor cursor = null;
 		try {
 			cursor = db.query(SOUP_INDEX_MAP_TABLE, new String[] {COLUMN_NAME_COL}, null, 
 					null, getSoupNamePredicate() + " AND " + getPathPredicate(), soupName, path);
 			
 			if (cursor.moveToFirst()) {
 				return cursor.getString(0);
 			}
 			else {
 				throw new RuntimeException(String.format("%s does not have an index on %s", soupName, path));
 			}
 		}
 		finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 	
 	/**
 	 * Read index specs back from the soup index map table
 	 * @param db
 	 * @param soupName
 	 * @return
 	 */
 	protected IndexSpec[] getIndexSpecs(Database db, String soupName) {
 		Cursor cursor = null;
 		try {
 			cursor = db.query(SOUP_INDEX_MAP_TABLE, new String[] {PATH_COL, COLUMN_NAME_COL, COLUMN_TYPE_COL}, null,
 					null, getSoupNamePredicate(), soupName);
 		
 			if (!cursor.moveToFirst()) {
 				throw new RuntimeException(String.format("%s does not have any indices", soupName));				
 			}
 
 			List<IndexSpec> indexSpecs = new ArrayList<IndexSpec>();
 			do {
 				String path = cursor.getString(cursor.getColumnIndex(PATH_COL));
 				String columnName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_COL));
 				Type columnType = Type.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_COL)));
 				indexSpecs.add(new IndexSpec(path, columnType, columnName));
 			}
 			while (cursor.moveToNext());
 			
 			return indexSpecs.toArray(new IndexSpec[0]);
 		}
 		finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 	
 	/**
 	 * @return predicate to match a soup entry by name
 	 */
 	protected String getSoupNamePredicate() {
 		return SOUP_NAME_COL + " = ?";
 	}
 
 	/**
 	 * @return predicate to match a soup entry by id
 	 */
 	protected String getSoupEntryIdPredicate() {
 		return ID_COL + " = ?";
 	}
 	
 	/**
 	 * @return predicate to match soup entries by id
 	 */
 	protected String getSoupEntryIdsPredicate(Long[] soupEntryIds) {
 		return ID_COL + " IN (" + TextUtils.join(",", soupEntryIds)+ ")";
 	}
 	
 	
 	/**
 	 * @return
 	 */
 	protected String getPathPredicate() {
 		return PATH_COL + " = ?";		
 	}
 	
 	/**
 	 * @param soupId
 	 * @return
 	 */
 	protected String getSoupTableName(long soupId) {
 		return "TABLE_" + soupId;
 	}
 
 
 	/**
 	 * @param soup
 	 * @param path
 	 * @return object at path in soup
 	 */
 	public static Object project(JSONObject soup, String path) {
 		if (soup == null) {
 			return null;
 		}
 		if (path == null || path.equals("")) {
 			return soup;
 		}
 		
 		String[] pathElements = path.split("[.]");
 		Object o = soup;
 		for (String pathElement : pathElements) {
 			o = ((JSONObject) o).opt(pathElement);
 		}
 		return o;
 	}
 
 	/**
 	 * Enum for column type
 	 */
 	public enum Type {
 		string("TEXT"), integer("INTEGER");
 		
 		private String columnType;
 
 		private Type(String columnType) {
 			this.columnType = columnType;
 		}
 		
 		public String getColumnType() {
 			return columnType;
 		}
 	}
 	
 	/**
 	 * Simple class to represent index spec
 	 */
 	public static class IndexSpec {
 		public final String path;
 		public final Type type;
 		public final String columnName;
 		
 		public IndexSpec(String path, Type type) {
 			this.path = path;
 			this.type = type;
 			this.columnName = null; // undefined
 		}
 		
 		public IndexSpec(String path, Type type, String columnName) {
 			this.path = path;
 			this.type = type;
 			this.columnName = columnName;
 		}
 		
 	}
 
 	/**
 	 * Query type enum
 	 */
 	public enum QueryType {
 		all,
 		exact,
 		range,
 		like;
 	}
 	
 	
 	/**
 	 * Simple class to represent a query spec
 	 */
 	public static class QuerySpec {
 		public final String path;
 		public final QueryType queryType;
 		
 		// Exact 
 		public final String matchKey;
 		// Range 
 		public final String beginKey;
 		public final String endKey;
 		// Like
 		public final String likeKey;
 
 		// Order
 		public final Order order;
 		
 		// Page size
 		public final int pageSize;
 		
 		// Private constructor
 		private QuerySpec(String path, QueryType queryType, String matchKey, String beginKey, String endKey, String likeKey, Order order, int pageSize) {
 			this.path = path;
 			this.queryType = queryType;
 			this.matchKey = matchKey;
 			this.beginKey = beginKey;
 			this.endKey = endKey;
 			this.likeKey = likeKey;
 			this.order = order;
 			this.pageSize = pageSize;
 		}
 		
 
 		/**
 		 * Return a query spec for returning all entries
 		 * @param order
 		 * @param pageSize
 		 * @return
 		 */
 		public static QuerySpec buildAllQuerySpec(Order order, int pageSize) {
 			return new QuerySpec(null, QueryType.all, null, null, null, null, order, pageSize);
 		}
 		
 		
 		/**
 		 * Return a query spec for an exact match query
 		 * @param path
 		 * @param exactMatchKey
 		 * @param pageSize
 		 * @return
 		 */
 		public static QuerySpec buildExactQuerySpec(String path, String exactMatchKey, int pageSize) {
 			return new QuerySpec(path, QueryType.exact, exactMatchKey, null, null, null, Order.ascending /* meaningless - all rows will have the same value in the indexed column*/, pageSize);
 		}
 
 		/**
 		 * Return a query spec for a range query
 		 * @param path
 		 * @param beginKey
 		 * @param endKey
 		 * @param order
 		 * @param pageSize
 		 * @return
 		 */
 		public static QuerySpec buildRangeQuerySpec(String path, String beginKey, String endKey, Order order, int pageSize) {
 			return new QuerySpec(path, QueryType.range, null, beginKey, endKey, null, order, pageSize);
 		}
 
 		/**
 		 * Return a query spec for a like query
 		 * @param path
 		 * @param matchKey
 		 * @param order
 		 * @param pageSize
 		 * @return
 		 */
 		public static QuerySpec buildLikeQuerySpec(String path, String likeKey, Order order, int pageSize) {
 			return new QuerySpec(path, QueryType.like, null, null, null, likeKey, order, pageSize);
 		}
 		
 		/**
 		 * @param columnName
 		 * @return string representing sql predicate
 		 */
 		public String getKeyPredicate(String columnName) {
 			switch(queryType) {
 			case all:
 				return null;
 			case exact:
 				return columnName + " = ?";
 			case like:
 				return columnName + " LIKE ?";
 			case range:
 				if (endKey == null)
 					return columnName + " >= ? ";
 				else if (beginKey == null)
 					return columnName + " <= ? ";
 				else
 					return columnName + " >= ? AND " + columnName + " <= ?";
 			default:
 				throw new SmartStoreException("Fell through switch: " + queryType);
 			}
 		}
 		
 		/**
 		 * @return args going with the sql predicate returned by getKeyPredicate
 		 */
 		public String[] getKeyPredicateArgs() {
 			switch(queryType) {
 			case all:
 				return null;
 			case exact:
 				return new String[] {matchKey};
 			case like:
 				return new String[] {likeKey};
 			case range:
 				if (endKey == null)
 					return new String[] {beginKey};
 				else if (beginKey == null)
 					return new String[] {endKey};
 				else
 					return new String[] {beginKey, endKey};
 			default:
 				throw new SmartStoreException("Fell through switch: " + queryType); 
 			}
 		}
 		
 		/**
 		 * @param columnName
 		 * @return sql for order by
 		 */
 		public String getOrderBy(String columnName) {
 			return columnName + " " + order.sql;
 		}
 	}
 
 	/**
 	 * Simple class to represent query order (used by QuerySpec)
 	 */
 	public enum Order {
 		ascending("ASC"), descending("DESC");
 		
 		public final String sql;
 		
 		Order(String sqlOrder) {
 			this.sql = sqlOrder;
 		}
 	}
 	
 	/**
 	 * Exception thrown by smart store
 	 *
 	 */
 	public static class SmartStoreException extends RuntimeException {
 
 		public SmartStoreException(String message) {
 			super(message);
 		}
 
 		private static final long serialVersionUID = -6369452803270075464L;
 		
 	}
 }
