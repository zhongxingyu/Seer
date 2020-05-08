 /*
  * Copyright (c) 2011 Imaginea Technologies Private Ltd.
  * Hyderabad, India
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.imaginea.mongodb.services;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.imaginea.mongodb.common.MongoInstanceProvider;
 import com.imaginea.mongodb.common.SessionMongoInstanceProvider;
 import com.imaginea.mongodb.common.exceptions.CollectionException;
 import com.imaginea.mongodb.common.exceptions.DatabaseException;
 import com.imaginea.mongodb.common.exceptions.DeleteCollectionException;
 import com.imaginea.mongodb.common.exceptions.DuplicateCollectionException;
 import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
 import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
 import com.imaginea.mongodb.common.exceptions.ErrorCodes;
 import com.imaginea.mongodb.common.exceptions.InsertCollectionException;
 import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
 import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
 import com.imaginea.mongodb.common.exceptions.ValidationException;
 import com.mongodb.BasicDBObject;
 import com.mongodb.CommandResult;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 /**
  * Defines services definitions for performing operations like create/drop on
  * collections inside a database present in mongo to which we are connected to.
  * Also provides service to get list of all collections present and Statistics
  * of a particular collection.
  * 
  * @author Rachit Mittal
  * @since 4 July 2011
  * 
  * 
  */
 public class CollectionServiceImpl implements CollectionService {
 
 	/**
 	 * Instance variable used to get a mongo instance after binding to an
 	 * implementation.
 	 */
 	private MongoInstanceProvider mongoInstanceProvider;
 	/**
 	 * Mongo Instance to communicate with mongo
 	 */
 	private Mongo mongoInstance;
 
 	/**
 	 * Creates an instance of MongoInstanceProvider which is used to get a mongo
 	 * instance to perform operations on collections. The instance is created
 	 * based on a userMappingKey which is recieved from the collection request
 	 * dispatcher and is obtained from tokenId of user.
 	 * 
 	 * @param dbInfo
 	 *            A combination of username,mongoHost and mongoPort
 	 */
 	public CollectionServiceImpl(String dbInfo) {
 		mongoInstanceProvider = new SessionMongoInstanceProvider(dbInfo);
 	}
 
 	/**
 	 * Gets the list of collections present in a database in mongo to which user
 	 * is connected to.
 	 * 
 	 * @param dbName
 	 *            Name of database
 	 * @return List of All Collections present in MongoDb
 	 * 
 	 * @exception UndefinedDatabaseException
 	 *                If db is not present
 	 * @exception EmptyDatabaseNameException
 	 *                If Db Name is null
 	 * @exception DatabaseException
 	 *                throw super type of UndefinedDatabaseException
 	 * @exception ValidationException
 	 *                throw super type of EmptyDatabaseNameException
 	 * @exception CollectionException
 	 *                exception while performing get list operation on
 	 *                collection
 	 * 
 	 */
 	public Set<String> getCollList(String dbName) throws ValidationException, DatabaseException, CollectionException {
 
 		mongoInstance = mongoInstanceProvider.getMongoInstance();
 
 		if (dbName == null) {
 			throw new EmptyDatabaseNameException("Database Name Is Null");
 		}
 		if (dbName.equals("")) {
 			throw new EmptyDatabaseNameException("Database Name Empty");
 		}
 
 		Set<String> collList = new HashSet<String>();
 
 		try {
 			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
 				throw new UndefinedDatabaseException(
 
 				"Database with dbName [ " + dbName + "] does not exist");
 			}
 
 			Set<String> collectionList = new HashSet<String>();
 			collectionList = mongoInstance.getDB(dbName).getCollectionNames();
 			Iterator<String> it = collectionList.iterator();
 
 			while (it.hasNext()) {
 				String coll = it.next();
 				if (!coll.contains("system.")) {
 					collList.add(coll);
 				}
 			}
 
 		} catch (MongoException m) {
 			CollectionException e = new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, "GET_COLLECTION_LIST_EXCEPTION", m.getCause());
 			throw e;
 		}
 		return collList;
 
 	}
 
 	/**
 	 * Creates a collection inside a database in mongo to which user is
 	 * connected to.
 	 * 
 	 * @param dbName
 	 *            Name of Database in which to insert a collection
 	 * @param collectionName
 	 *            Name of Collection to be inserted
 	 * @param capped
 	 *            Specify if the collection is capped
 	 * @param size
 	 *            Specify the size of collection
 	 * @param maxDocs
 	 *            specify maximum no of documents in the collection
 	 * @return Success if Insertion is successful else throw exception
 	 * @exception EmptyDatabaseNameException
 	 *                if dbName is null
 	 * @exception EmptyCollectionNameException
 	 *                if collectionName is null
 	 * @exception UndefinedDatabaseException
 	 *                if database is not present
 	 * @exception DuplicateCollectionException
 	 *                if collection is already present
 	 * @exception InsertCollectionException
 	 *                exception while inserting collection
 	 * @exception DatabaseException
 	 *                throw super type of UndefinedDatabaseException
 	 * @exception ValidationException
 	 *                throw super type of
 	 *                EmptyDatabaseNameException,EmptyCollectionNameException
 	 * @exception CollectionException
 	 *                throw super type of
 	 *                DuplicateCollectionException,InsertCollectionException
 	 */
 	public String insertCollection(String dbName, String collectionName, boolean capped, int size, int maxDocs) throws DatabaseException, CollectionException, ValidationException {
 
 		mongoInstance = mongoInstanceProvider.getMongoInstance();
 		if (dbName == null) {
 			throw new EmptyDatabaseNameException("Database name is null");
 
 		}
 		if (dbName.equals("")) {
 			throw new EmptyDatabaseNameException("Database Name Empty");
 		}
 
 		if (collectionName == null) {
 			throw new EmptyCollectionNameException("Collection name is null");
 		}
 		if (collectionName.equals("")) {
 			throw new EmptyCollectionNameException("Collection Name Empty");
 		}
 		try {
 			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
 				throw new UndefinedDatabaseException("Db with name [" + dbName + "] doesn't exist.");
 			}
 			if (mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
 				throw new DuplicateCollectionException("Collection [" + collectionName + "] Already exists in Database [" + dbName + "]");
 			}
 
 			DBObject options = new BasicDBObject();
 			options.put("capped", capped);
 			if (capped) {
 				options.put("size", size);
 				options.put("max", maxDocs);
 			}
 			mongoInstance.getDB(dbName).createCollection(collectionName, options);
 		} catch (MongoException m) {
 			InsertCollectionException e = new InsertCollectionException("COLLECTION_CREATION_EXCEPTION", m.getCause());
 			throw e;
 		}
		String result = "Created Collection [" + collectionName + "] in Database [" + dbName + "]";

 		return result;
 	}
 
 	/**
 	 * Deletes a collection inside a database in mongo to which user is
 	 * connected to.
 	 * 
 	 * @param dbName
 	 *            Name of Database in which to insert a collection
 	 * @param collectionName
 	 *            Name of Collection to be inserted
 	 * @return Success if deletion is successful else throw exception
 	 * @exception EmptyDatabaseNameException
 	 *                if dbName is null
 	 * @exception EmptyCollectionNameException
 	 *                if collectionName is null
 	 * @exception UndefinedDatabaseException
 	 *                if database is not present
 	 * @exception UndefinedCollectionException
 	 *                if collection is not present
 	 * @exception DeleteCollectionException
 	 *                exception while deleting collection
 	 * @exception DatabaseException
 	 *                throw super type of UndefinedDatabaseException
 	 * @exception ValidationException
 	 *                throw super type of
 	 *                EmptyDatabaseNameException,EmptyCollectionNameException
 	 * @exception CollectionException
 	 *                throw super type of
 	 *                UndefinedCollectionException,DeleteCollectionException
 	 */
 
 	public String deleteCollection(String dbName, String collectionName) throws DatabaseException, CollectionException, ValidationException {
 
 		mongoInstance = mongoInstanceProvider.getMongoInstance();
 		if (dbName == null) {
 			throw new EmptyDatabaseNameException("Database name is null");
 
 		}
 		if (dbName.equals("")) {
 			throw new EmptyDatabaseNameException("Database Name Empty");
 		}
 
 		if (collectionName == null) {
 			throw new EmptyCollectionNameException("Collection name is null");
 		}
 		if (collectionName.equals("")) {
 			throw new EmptyCollectionNameException("Collection Name Empty");
 		}
 		try {
 			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
 				throw new UndefinedDatabaseException("DB with name [" + dbName + "]DOES_NOT_EXIST");
 			}
 
 			if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
 				throw new UndefinedCollectionException("Collection with name [" + collectionName + "] DOES NOT EXIST in Database [" + dbName + "]");
 			}
 
 			mongoInstance.getDB(dbName).getCollection(collectionName).drop();
 		} catch (MongoException m) {
 
 			DeleteCollectionException e = new DeleteCollectionException("COLLECTION_DELETION_EXCEPTION", m.getCause());
 			throw e;
 
 		}
 		String result = "Deleted Collection [" + collectionName + "] in Database [" + dbName + "]";
 
 		return result;
 	}
 
 	/**
 	 * Get Statistics of a collection inside a database in mongo to which user
 	 * is connected to.
 	 * 
 	 * @param dbName
 	 *            Name of Database in which to insert a collection
 	 * @param collectionName
 	 *            Name of Collection to be inserted
 	 * @return Array of JSON Objects each containing a key value pair in
 	 *         Collection Stats.
 	 * @exception EmptyDatabaseNameException
 	 *                if dbName is null
 	 * @exception EmptyCollectionNameException
 	 *                if collectionName is null
 	 * @exception UndefinedDatabaseException
 	 *                if database is not present
 	 * @exception UndefinedCollectionException
 	 *                if collection is not present
 	 * @exception DatabaseException
 	 *                throw super type of UndefinedDatabaseException
 	 * @exception ValidationException
 	 *                throw super type of
 	 *                EmptyDatabaseNameException,EmptyCollectionNameException
 	 * @exception CollectionException
 	 *                throw super type of UndefinedCollectionException
 	 * @exception JSONException
 	 *                JSON Exception
 	 */
 
 	public JSONArray getCollStats(String dbName, String collectionName) throws DatabaseException, CollectionException, ValidationException, JSONException {
 		mongoInstance = mongoInstanceProvider.getMongoInstance();
 		if (dbName == null) {
 			throw new EmptyDatabaseNameException("Database name is null");
 
 		}
 		if (dbName.equals("")) {
 			throw new EmptyDatabaseNameException("Database Name Empty");
 		}
 
 		if (collectionName == null) {
 			throw new EmptyCollectionNameException("Collection name is null");
 		}
 		if (collectionName.equals("")) {
 			throw new EmptyCollectionNameException("Collection Name Empty");
 		}
 
 		JSONArray collStats = new JSONArray();
 
 		try {
 			if (!mongoInstance.getDatabaseNames().contains(dbName)) {
 				throw new UndefinedDatabaseException("DB with name [" + dbName + "]DOES_NOT_EXIST");
 			}
 			if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
 				throw new UndefinedCollectionException(
 
 				"Collection with name [" + collectionName + "] DOES NOT EXIST in Database [" + dbName + "]");
 			}
 			CommandResult stats = mongoInstance.getDB(dbName).getCollection(collectionName).getStats();
 
 			Set<String> keys = stats.keySet();
 			Iterator<String> keyIterator = keys.iterator();
 			JSONObject temp = new JSONObject();
 
 			while (keyIterator.hasNext()) {
 				temp = new JSONObject();
 				String key = keyIterator.next().toString();
 				temp.put("Key", key);
 				String value = stats.get(key).toString();
 				temp.put("Value", value);
 				String type = stats.get(key).getClass().toString();
 				temp.put("Type", type.substring(type.lastIndexOf('.') + 1));
 				collStats.put(temp);
 			}
 		} catch (JSONException e) {
 			throw e;
 		} catch (MongoException m) {
 			CollectionException e = new CollectionException(ErrorCodes.GET_COLL_STATS_EXCEPTION, "GET_COLL_STATS_EXCEPTION", m.getCause());
 			throw e;
 		}
 		return collStats;
 	}
 }
