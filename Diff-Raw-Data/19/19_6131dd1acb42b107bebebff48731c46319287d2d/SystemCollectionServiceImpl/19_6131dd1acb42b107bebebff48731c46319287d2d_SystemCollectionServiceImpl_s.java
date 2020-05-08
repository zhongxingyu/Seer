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
 
 
 package com.imaginea.mongodb.services.impl;
 
 /**
  * Defines services for performing operations like create/drop on indexes and users which
  * are part of system name space system.users & system.indexes collections
  * inside a database present in mongo to which we are connected to.
  * @author Sanjay Chaluvadi
  */
 
 
import com.imaginea.mongodb.domain.ConnectionDetails;
 import com.imaginea.mongodb.domain.MongoConnectionDetails;
 import com.imaginea.mongodb.exceptions.ApplicationException;
 import com.imaginea.mongodb.exceptions.DatabaseException;
 import com.imaginea.mongodb.exceptions.ErrorCodes;
 import com.imaginea.mongodb.services.AuthService;
 import com.imaginea.mongodb.services.SystemCollectionService;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 
 import java.util.Set;
 
 
 public class SystemCollectionServiceImpl implements SystemCollectionService {
     /**
      * Mongo Instance to communicate with mongo
      */
     private Mongo mongoInstance;
     private static final AuthService AUTH_SERVICE = AuthServiceImpl.getInstance();
 
 
     /**
      * Creates an instance of MongoInstanceProvider which is used to get a mongo
      * instance to perform operations on collections. The instance is created
      * based on a userMappingKey which is recieved from the collection request
      * dispatcher and is obtained from tokenId of user.
      *
      * @param connectionId A combination of username,mongoHost and mongoPort
      */
     public SystemCollectionServiceImpl(String connectionId) throws ApplicationException {
         MongoConnectionDetails mongoConnectionDetails = AUTH_SERVICE.getMongoConnectionDetails(connectionId);
         mongoInstance = mongoConnectionDetails.getMongo();
     }
 
     /**
      * Adds a user to the given database
      *
      * @param dbName   Name of the database
      * @param username Username of the user to be added
      * @param password Password of the usre to be added
      * @param readOnly optional attribute for creating the user
      * @return Returns the success message that should be shown to the user
      * @throws DatabaseException throw super type of UndefinedDatabaseException
      */
 
 
     @Override
     public String addUser(String dbName, String username, String password, boolean readOnly) throws DatabaseException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
         if (username == null) {
             throw new DatabaseException(ErrorCodes.USERNAME_IS_EMPTY, "Username is null");
         }
         if (username.equals("")) {
             throw new DatabaseException(ErrorCodes.USERNAME_IS_EMPTY, "Username is empty");
         }
         if (password == null) {
             throw new DatabaseException(ErrorCodes.PASSWORD_IS_EMPTY, "Password is null");
         }
         if (password.equals("")) {
             throw new DatabaseException(ErrorCodes.PASSWORD_IS_EMPTY, "Password is empty");
         }
 
         mongoInstance.getDB(dbName).addUser(username, password.toCharArray(), readOnly);
         return "User: " + username + " has been added to the DB: " + dbName;
     }
 
     /**
      * Drops the user from the given mongo db based on the username
      *
      * @param dbName   Name of the database
      * @param username Username of the user to be deleted/dropped
      * @return Returns the success message that shown to the user
      * @throws DatabaseException throwsuper type of UndefinedDatabaseException
      */
     @Override
     public String removeUser(String dbName, String username) throws DatabaseException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
         if (username == null) {
             throw new DatabaseException(ErrorCodes.USERNAME_IS_EMPTY, "Username is null");
         }
         if (username.equals("")) {
             throw new DatabaseException(ErrorCodes.USERNAME_IS_EMPTY, "Username is empty");
         }
         mongoInstance.getDB(dbName).removeUser(username);
        return "User: " + username + " is deleted from the DB: " + dbName;
     }
 
     /**
      * Drops all the users from the given mongo db
      *
      * @param dbName Name of the database
      * @return Returns the success message that shown to the user
      * @throws DatabaseException throw super type of UndefinedDatabaseException
      */
 
     @Override
     public String removeAllUsers(String dbName) throws DatabaseException {


         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
         DBCursor users = mongoInstance.getDB(dbName).getCollection("system.users").find();
         if (users.size() != 0) {
             for (DBObject user : users) {
                 mongoInstance.getDB(dbName).getCollection("system.users").remove(user);
             }
             return "All users are dropped from the DB: " + dbName;
         } else {
            return "No users to drop from the DB: " + dbName;
         }
     }
 
     /**
      * Adds an index for a given colleciton in a mongo db
      *
      * @param dbName         Name of the database the index should be added
      * @param collectionName Name of the collection to which the index is to be added
      * @param keys           The keys with the which the index is created
      * @return Returns the success message that shown to the user
      * @throws DatabaseException throw super type of UndefinedDatabaseException
      */
 
     @Override
     public String addIndex(String dbName, String collectionName, DBObject keys) throws DatabaseException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
 
         if (collectionName == null) {
             throw new DatabaseException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is null");
         }
         if (collectionName.equals("")) {
             throw new DatabaseException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is Empty");
         }
         if (keys == null) {
             throw new DatabaseException(ErrorCodes.KEYS_EMPTY, "Index Keys are null");
         }
         if (keys.equals("")) {
             throw new DatabaseException(ErrorCodes.KEYS_EMPTY, "Index keys are Empty");
         }
         mongoInstance.getDB(dbName).getCollection(collectionName).ensureIndex(keys);
        return "New index is successfully added for the collection: " + collectionName + " in the DB: " + dbName;
     }
 
     /**
      * Removes all the indexes from all the collections in a given mongo db
      *
      * @param dbName Name of the database
      * @return Returns the success message that shown to the user
      * @throws DatabaseException throw super type of UndefinedDatabaseException
      */
 
     @Override
     public String removeIndexes(String dbName) throws DatabaseException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
 
         Set<String> collectionNames = mongoInstance.getDB(dbName).getCollectionNames();
         for (String collection : collectionNames) {
             mongoInstance.getDB(dbName).getCollection(collection).dropIndexes();
         }
        return "Indexes are dropped on all collections from DB: " + dbName;
     }
 
     /**
      * Removes an index from the collection based on the index name
      *
      * @param dbName         Name of the database from which the index should be dropped/removed
      * @param collectionName Name of the collection from which the index should be dropped
      * @param indexName      Name of the index that should be deleted
      * @return Returns the success message that shown to the user
      * @throws DatabaseException throw super type of UndefinedDatabaseException
      */
 
     @Override
     public String removeIndex(String dbName, String collectionName, String indexName) throws DatabaseException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
         if (collectionName == null) {
             throw new DatabaseException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is null");
         }
         if (collectionName.equals("")) {
             throw new DatabaseException(ErrorCodes.COLLECTION_NAME_EMPTY, "Collection name is Empty");
         }
         if (indexName == null) {
             throw new DatabaseException(ErrorCodes.INDEX_EMPTY, "Index name is null");
         }
         if (indexName.equals("")) {
             throw new DatabaseException(ErrorCodes.INDEX_EMPTY, "Index name is Empty");
         }
         mongoInstance.getDB(dbName).getCollection(collectionName).dropIndexes(indexName);
 
        return "Index: " + indexName + " is dropped from the collection: " + collectionName + " and DB: " + dbName;
     }
 }
