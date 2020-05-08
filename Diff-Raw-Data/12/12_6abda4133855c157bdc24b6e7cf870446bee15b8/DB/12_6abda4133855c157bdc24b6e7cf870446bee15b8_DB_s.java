 package com.nyaruka.db;
 
 import java.io.File;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.nyaruka.json.JSON;
 
 public abstract class DB {
 	
 	public static final int RECORD_ID = 0;
 	public static final int RECORD_COLLECTION_ID = 1;
 	public static final int RECORD_DATA = 2;
 	
 	public static final int COLLECTION_ID = 0;
 	public static final int COLLECTION_NAME = 1;
 	
 	private static HashMap<String, String> OPERATORS = new HashMap<String, String>();
 	
 	public abstract Connection getConnection();
 	
 	public DB() {
 		initOperators();
 	}
 	
 	public static void initOperators(){
 		OPERATORS.put("$gt", ">");
 		OPERATORS.put("$gte", ">=");
 		OPERATORS.put("$lt", "<");
 		OPERATORS.put("$lte", "<=");
 		OPERATORS.put("$ne", "!=");
 	}
 
 	public void open(){
 		try{
 			m_connection = getConnection();
 			m_connection.open(true);
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		}
 	}
 	
 	public void close() {
 	    m_collections = null;
 	    m_connection.dispose();
 	}
 	
 	private void exec(String sql){
 		Connection conn = null;
 		try{
 			conn = m_connection.exec(sql);
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		} finally {
 			if (conn != null){
 				//conn.dispose();
 			}
 		}		
 	}
 	
 	public void clear(){
 		exec("DELETE FROM `collections`;");
 		m_collections.clear();
 	}
 	
 	public void init(){
 		// record types, maybe should be called collections? 
 		exec("CREATE TABLE IF NOT EXISTS `collections` (" +
 			 "`id` integer PRIMARY KEY," +
 			 "`name` varchar(128)," + 
 			 "`int0` varchar(128)," +
 			 "`int1` varchar(128)," +
 			 "`int2` varchar(128)," +
 			 "`int3` varchar(128)," +
 			 "`int4` varchar(128)," +
 			 "`str0` varchar(128)," +
 			 "`str1` varchar(128)," +			 
 			 "`str2` varchar(128)," +			 			 
 			 "`str3` varchar(128)," + 
 			 "`str4` varchar(128)" +			 			 			 			 
 		")");
 		
 		// our actual records
 		exec("CREATE TABLE IF NOT EXISTS `records` (" +
 			  "`id` integer PRIMARY KEY," +
 			  "`collection` integer," +
 			  "`data` text," +			  
 			  "`int0` integer," +
 			  "`int1` integer," +
 			  "`int2` integer," +
 			  "`int3` integer," +
 			  "`int4` integer," +
 			  "`str0` string," +
 			  "`str1` string," +
 			  "`str2` string," +
 			  "`str3` string," +
 			  "`str4` string" +
 		")");
 	}
 	
 	/**
 	 * Clears our in memory data and reloads it from the database.
 	 */
 	public void load(){
 		m_collections = new HashMap<String, Collection>();
 
 		try{
 			Statement st = m_connection.prepare("SELECT * FROM collections");
 			while (st.step()){
 				int id = st.columnInt(0);
 				String name = st.columnString(1);
 				Collection type = new Collection(this, id, name);
 				m_collections.put(name, type);
 				
 				for(int i=0; i<10; i++){
 					if (i < 5){
 						type.setIntIndex(i, st.columnString(i+2));
 					} else {
 						type.setStrIndex(i-5, st.columnString(i+2));						
 					}
 				}
 			}
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		}
 	}
 	
 	public boolean collectionExists(String name) {
 		if (m_collections == null){
 			load();
 		}
 		return m_collections.containsKey(name);
 	}
 
 	public void saveCollection(Collection type){
 		try{
 			Statement st = m_connection.prepare("UPDATE collections SET int0=?, int1=?, int2=?, int3=?, int4=?," +
 											  "                       str0=?, str1=?, str2=?, str3=?, str4=? " + 
 											  " WHERE id=?");
 			// bind our int indexes
 			for(int i=0; i<5; i++){
 				st.bind(i+1, type.getIntIndex(i));
 			}
 			
 			// then our str indexes
 			for(int i=0; i<5; i++){
 				st.bind(i+6, type.getStrIndex(i));
 			}
 			
 			// bind our id
 			st.bind(11, type.getId());
 			st.step();
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		}
 	}
 	
 	public Collection getCollection(String name){
 		if (!collectionExists(name)){
 			throw new RuntimeException("No collection with name " + name);
 		} else {
 			return m_collections.get(name);
 		}
 	}
 	
 	public void deleteCollection(Collection collection){
 		if (collectionExists(collection.getName())){
 			try{
				SQLiteStatement st = m_db.prepare("DELETE FROM collections where id = ?");
 				st.bind(1, collection.getId());
 				st.step();
 				
				st = m_db.prepare("DELETE FROM records where collection = ?");
 				st.bind(1, collection.getId());
 				st.step();
 			} catch (Throwable t){
 				throw new RuntimeException(t);
 			}
 			load();
 		}
 	}
 	
 	public Collection ensureCollection(String name) {
 		if (!collectionExists(name)){
 			// validate the name is only alphanumiric
 			if (!name.matches("^[a-zA-Z0-9_]+$")){
 				throw new RuntimeException("Invalid collection name, collection names must be alphanumeric");
 			}
 			
 			try{
 				Statement st = m_connection.prepare("INSERT INTO collections VALUES(NULL, ?, " +
 												  "NULL, NULL, NULL, NULL, NULL, " +
 												  "NULL, NULL, NULL, NULL, NULL)");
 				st.bind(COLLECTION_NAME, name);
 				st.step();
 			} catch (Throwable t){
 				throw new RuntimeException(t);
 			}
 			load();
 		}
 		return m_collections.get(name);
 	}
 	
 	public ArrayList<Collection> getCollections(){
 		ArrayList<Collection> collections = new ArrayList<Collection>();
 		for (Collection coll : m_collections.values()){
 			collections.add(coll);
 		}
 		return collections;
 	}
 	
 	public ArrayList<String> getCollectionNames(){
 		ArrayList<String> names = new ArrayList<String>();
 		for (Collection coll : m_collections.values()){
 			names.add(coll.getName());
 		}
 		return names;
 	}
 	
 	public static String generateSQL(Collection coll, JSON query, ArrayList<String> params){
 		String whereClause = "collection = " + coll.getId();
 		String delim = " AND ";
 		Iterator keys = query.keys();
 		while (keys.hasNext()){
 			whereClause += delim;			
 			String key = (String) keys.next();
 			String field = coll.getIndexName(key);
 				
 			if (field == null){
 				throw new RuntimeException("No index present for column '" + key + "', call ensureIndex() first.");
 			}
 				
 			// field will now be: str1, int4 or whatever
 			
 			// we now need to figure out if our value for this key is a complex operator (say $gt) or a simple value (5)
 			try{
 				JSON operands = query.getJSON(key);
 				whereClause += buildClause(field, operands, params); 
 			} catch (Throwable t){
 				// ok, this is just simple equality then
 				whereClause += field + " = ?";
 				params.add(query.get(key).toString());
 			}
 		}
 		
 		if (whereClause.length() > 0){
 			whereClause = " WHERE " + whereClause;
 		}
 		
 		return whereClause;
 	}
 	
 	public static String buildClause(String field, JSON operands, ArrayList<String> params){
 		String sql = "";
 		String delim = "";
 		Iterator keys = operands.keys();
 		while(keys.hasNext()){
 			sql += delim;
 			String key = (String) keys.next();
 			
 			// make sure this is a valid operator
 			if (!OPERATORS.containsKey(key)){
 				throw new RuntimeException("Unsupported operator '" + key + "' in clause: " + operands);
 			}
 			
 			//wahoo
 			String val = null;
 			try{
 				val = "" + operands.getInt(key);
 			} catch (Throwable t){
 				try{
 					val = "" + operands.getString(key);
 				} catch (Throwable tt){
 					try{
 						throw new RuntimeException("Unsupported value for operand '" + operands.get(key) + "'");
 					} catch (Throwable ttt){}
 				}
 			}
 			
 			// add it to our clause
 			sql += field + " " + OPERATORS.get(key) + " ?";
 			params.add(val);
 			delim = " AND ";
 		}
 		
 		return sql;
 	}
 	
 	public Record save(Collection collection, JSON data){
 		try{
 			Statement st = null;
 			long id = -1;
 			
 			// this is an update
 			if (data.has("id")){
 				st = m_connection.prepare("UPDATE records SET collection=?, data=?," +
 						"int0=?, int1=?, int2=?, int3=?, int4=?, " +
 						"str0=?, str1=?, str2=?, str3=?, str4=? " + 
 				        " WHERE id=?");
 				id = data.getLong("id");
 				st.bind(13, id);
 				data.getObject().remove("id");
 			} else {
 				st = m_connection.prepare("INSERT INTO records VALUES(NULL, ?, ?," +
 						"?, ?, ?, ?, ?, " +
 						"?, ?, ?, ?, ? " + 
 				")");
 			}
 			
 			st.bind(RECORD_COLLECTION_ID, collection.getId());
 			st.bind(RECORD_DATA, data.toString());
 			
 			// bind our int values
 			for(int i=0; i<Collection.INDEX_COLS; i++){
 				String name = collection.getIntIndex(i);
 				boolean set = false;
 				if (name != null){
 					if (data.has(name)){
 						Object value = data.get(name);
 						if (value != null && value instanceof Integer){
 							st.bind(i+3, (Integer) value);
 							set = true;
 						}
 					}
 					if (!set){
 						st.bindNull(i+3);
 					}
 				}
 			}
 			
 			// then our string values
 			for(int i=0; i<Collection.INDEX_COLS; i++){
 				String name = collection.getStrIndex(i);
 				boolean set = false;
 				if (name != null){
 					if (data.has(name)){
 						st.bind(i+8, data.get(name) + "");
 						set = true;
 					} 
 				} 
 				if (!set){
 					st.bindNull(i+8);
 				}
 			}
 			
 			st.step();
 			if (id == -1){
 				id = m_connection.getLastInsertId();
 			}
 
 			return new Record(id, data);
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		}
 	}
 	
 	public void populateIndex(Collection collection, String name) {
 		Cursor cursor = findRecords(collection, new JSON());
 		
 		// TODO: this could be made much more efficient in that we could only update
 		// the index column that is given to us
 		while(cursor.hasNext()){
 			Record rec = cursor.next();
 			collection.save(rec.toString());
 		}
 	}
 	
 	public Cursor findRecords(Collection coll, JSON query){
 		ArrayList<String> params = new ArrayList<String>();
 		
 		try{
 			String whereClause = generateSQL(coll, query, params);
 			
 			// calculate our # of matches first
 			Statement st = m_connection.prepare("SELECT count(id) " + 
 											  "from records" + whereClause);
 			for (int i=0; i<params.size(); i++){
 				st.bind(i+1, params.get(i));
 			}
 			st.step();
 			
 			int rowCount = st.columnInt(0); 
 			
 			st = m_connection.prepare("SELECT id, collection, data " +
 							  "from records" + whereClause + " ORDER BY id ASC");
 			for (int i=0; i<params.size(); i++){
 				st.bind(i+1, params.get(i));
 			}
 			
 			return new Cursor(st, rowCount);
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		}
 	}
 	
 	public Record getRecord(Collection collection, long id){
 		try{
 			Statement st = m_connection.prepare("SELECT id, collection, data " +
 											  "from records where id=?");
 			st.bind(1, id);
 			
 			Record rec = null;
 			if (st.step()){
 				rec = new Record(st);
 			}
 			return rec;
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		}		
 	}
 	
 	public void deleteRecord(Collection collection, long id) {
 		try{
 			Statement st = m_connection.prepare("DELETE from records where id=?");
 			st.bind(1, id);
 			st.step();
 		} catch (Throwable t){
 			throw new RuntimeException(t);
 		}		
 	}
 
 	private Connection m_connection;
     private HashMap<String, Collection> m_collections = null;
 }
