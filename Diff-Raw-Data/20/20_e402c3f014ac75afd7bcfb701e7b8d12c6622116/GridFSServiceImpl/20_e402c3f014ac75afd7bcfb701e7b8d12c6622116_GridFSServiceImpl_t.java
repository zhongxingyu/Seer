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
 
 import com.imaginea.mongodb.exceptions.*;
 import com.imaginea.mongodb.services.AuthService;
 import com.imaginea.mongodb.services.DatabaseService;
 import com.imaginea.mongodb.services.GridFSService;
 import com.imaginea.mongodb.utils.JSON;
 import com.mongodb.*;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSDBFile;
 import com.mongodb.gridfs.GridFSInputFile;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 /**
  * Defines services definitions for performing operations like create/drop on
  * collections inside a database present in mongo to which we are connected to.
  * Also provides service to get list of all collections present and Statistics
  * of a particular file.
  *
  * @author Srinath Anantha
  */
 public class GridFSServiceImpl implements GridFSService {
 
     /**
      * Mongo Instance to communicate with mongo
      */
     private Mongo mongoInstance;
 
     private DatabaseService databaseService;
 
     private static final AuthService AUTH_SERVICE = AuthServiceImpl.getInstance();
 
     /**
      * Creates an instance of MongoInstanceProvider which is used to get a mongo
      * instance to perform operations on files. The instance is created
      * based on a userMappingKey which is received from the file request
      * dispatcher and is obtained from tokenId of user.
      *
      * @param connectionId A combination of username,mongoHost and mongoPort
      */
     public GridFSServiceImpl(String connectionId) throws ApplicationException {
         mongoInstance = AUTH_SERVICE.getMongoInstance(connectionId);
         databaseService = new DatabaseServiceImpl(connectionId);
     }
 
     /**
      * Service implementation for creating GridFS store in the specified database.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @returns Status message.
      */
     public String createStore(String dbName, String bucketName) throws DatabaseException, CollectionException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Is Null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
         if (bucketName == null) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket Name Empty");
         }
 
         new GridFS(mongoInstance.getDB(dbName), bucketName);
 
         return "GridFS bucket [" + bucketName + "] added to database [" + dbName + "].";
     }
 
     /**
      * Service implementation for getting the list of files stored in GridFS of specified database.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @param query
      * @param fields
      * @param skip
      * @param limit      @returns JSON representation of list of all files as a String.
      * @param sortBy
      */
     public JSONObject getFileList(String dbName, String bucketName, String query, String fields, String skip, String limit, String sortBy) throws ValidationException, DatabaseException, CollectionException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Is Null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
         if (bucketName == null) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket Name Empty");
         }
 
 
         JSONObject result = new JSONObject();
         try {
             if (!databaseService.getDbList().contains(dbName)) {
                 throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS,
                         "Database with dbName [ " + dbName + "] does not exist");
             }
 
             DBObject queryObj = (DBObject) JSON.parse(query);
             int filesLimit = Integer.parseInt(limit);
             int filesSkip = Integer.parseInt(skip);
             DBObject sortObj = (DBObject) JSON.parse(sortBy);
 
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
             Field field = GridFS.class.getDeclaredField("_filesCollection");
             field.setAccessible(true);
             DBCollection filesCollection = (DBCollection) field.get(gridFS);
             // Partial Keys cant be fetched for a file
             DBCursor cursor = filesCollection.find(queryObj, null).sort(sortObj).skip(filesSkip).limit(filesLimit);
 
             Iterator<DBObject> it = cursor.iterator();
 
             ArrayList<DBObject> fileList = new ArrayList<DBObject>();
             while (it.hasNext()) {
                 fileList.add(it.next());
             }
 
             long count = filesCollection.count(queryObj);
             result.put("documents", fileList);
             result.put("editable", true);
             result.put("count", count);
         } catch (Exception m) {
             throw new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, m.getMessage());
         }
         return result;
 
     }
 
     /**
      * Service implementation for retrieving the specified file stored in GridFS.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @param _id        ObjectId of the file to be retrieved
      * @returns Requested multipartfile for viewing or download based on 'download' param.
      */
     public File getFile(String dbName, String bucketName, String _id) throws ValidationException, DatabaseException, CollectionException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Is Null");
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
         File tempFile = null;
         try {
             if (!databaseService.getDbList().contains(dbName)) {
                 throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS,
 
                         "Database with dbName [ " + dbName + "] does not exist");
             }
             Object docId = JSON.parse(_id);
             BasicDBObject objectId = new BasicDBObject("_id", docId);
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
             GridFSDBFile gridFSDBFile = gridFS.findOne(objectId);
             String tempDir = System.getProperty("java.io.tmpdir");
             tempFile = new File(tempDir + "/" + gridFSDBFile.getFilename());
             gridFSDBFile.writeTo(tempFile);
 
         } catch (MongoException m) {
             throw new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, m.getMessage());
         } catch (IOException e) {
             throw new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, e.getMessage());
         }
         return tempFile;
     }
 
     /**
      * Service implementation for uploading a file to GridFS.
      *
      * @param dbName      Name of Database
      * @param bucketName  Name of GridFS Bucket
      * @param formData    formDataBodyPart of the uploaded file
      * @param inputStream inputStream of the uploaded file
     * @param connectionId ConnectionId of the connection
      * @returns Success message with additional file details such as name, size,
      * download url & deletion url as JSON Array string.
      */
    public JSONArray insertFile(String dbName, String bucketName, String connectionId, InputStream inputStream, FormDataBodyPart formData) throws DatabaseException, CollectionException, DocumentException, ValidationException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
 
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
 
         if (bucketName == null) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket Name Empty");
         }
 
         JSONArray result = new JSONArray();
         FormDataContentDisposition fileData = formData.getFormDataContentDisposition();
         try {
             if (!databaseService.getDbList().contains(dbName)) {
                 throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB [" + dbName + "] DOES NOT EXIST");
             }
 
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
             GridFSInputFile fsInputFile = gridFS.createFile(inputStream, fileData.getFileName());
             fsInputFile.setContentType(formData.getMediaType().toString());
             fsInputFile.save();
             String objectId = JSON.serialize(fsInputFile.getId());
             JSONObject obj = new JSONObject();
             obj.put("name", fsInputFile.getFilename());
             obj.put("size", fsInputFile.getLength());
            obj.put("url", String.format("services/%s/%s/gridfs/getfile?id=%s&download=%s&connectionId=%s&ts=%s", dbName, bucketName, objectId, false, connectionId, new Date()));
            obj.put("delete_url", String.format("services/%s/%s/gridfs/dropfile?id=%s&connectionId=%s&ts=%s", dbName, bucketName, objectId, connectionId, new Date().getTime()));
             obj.put("delete_type", "GET");
             result.put(obj);
 
         } catch (Exception e) {
             throw new CollectionException(ErrorCodes.UPLOAD_FILE_EXCEPTION, e.getMessage());
         }
         return result;
     }
 
     /**
      * Service implementation for dropping a file from GridFS.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @param _id        Object id of file to be deleted
      * @returns Status message.
      */
     public String deleteFile(String dbName, String bucketName, String _id) throws DatabaseException, DocumentException, CollectionException, ValidationException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
 
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
 
         if (bucketName == null) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket Name Empty");
         }
 
         String result = null;
         GridFSDBFile gridFSDBFile = null;
         try {
             if (!databaseService.getDbList().contains(dbName)) {
                 throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB [" + dbName + "] DOES NOT EXIST");
             }
             if (_id == null) {
                 throw new DocumentException(ErrorCodes.DOCUMENT_EMPTY, "File is empty");
             }
 
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
             Object docId = JSON.parse(_id);
             BasicDBObject objectId = new BasicDBObject("_id", docId);
             gridFSDBFile = gridFS.findOne(objectId);
 
             if (gridFSDBFile == null) {
                 throw new DocumentException(ErrorCodes.DOCUMENT_DOES_NOT_EXIST, "Document does not exist !");
             }
 
             gridFS.remove(objectId);
 
         } catch (MongoException e) {
             throw new DocumentException(ErrorCodes.DOCUMENT_DELETION_EXCEPTION, e.getMessage());
         }
         result = "File [" + gridFSDBFile.getFilename() + "] has been deleted.";
         return result;
     }
 
     /**
      * Service implementation for dropping all files from a GridFS bucket.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @returns Status message.
      */
     public String dropBucket(String dbName, String bucketName) throws DatabaseException, DocumentException, CollectionException, ValidationException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
 
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
 
         if (bucketName == null) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket Name Empty");
         }
 
         String result = null;
         try {
             if (!databaseService.getDbList().contains(dbName)) {
                 throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB [" + dbName + "] DOES NOT EXIST");
             }
 
             mongoInstance.getDB(dbName).getCollection(bucketName + ".files").drop();
             mongoInstance.getDB(dbName).getCollection(bucketName + ".chunks").drop();
 
         } catch (MongoException e) {
             throw new DocumentException(ErrorCodes.DOCUMENT_DELETION_EXCEPTION, e.getMessage());
         }
         result = "Bucket [" + bucketName + "] has been deleted from Database [" + dbName + "].";
         return result;
     }
 
     /**
      * Service handler for getting count of all files in a GridFS bucket.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @returns Status message.
      */
     public JSONObject getCount(String dbName, String bucketName) throws DatabaseException, DocumentException, ValidationException, CollectionException {
         if (dbName == null) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database name is null");
 
         }
         if (dbName.equals("")) {
             throw new DatabaseException(ErrorCodes.DB_NAME_EMPTY, "Database Name Empty");
         }
 
         if (bucketName == null) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new CollectionException(ErrorCodes.COLLECTION_NAME_EMPTY, "Bucket Name Empty");
         }
 
         JSONObject result = new JSONObject();
         try {
             if (!databaseService.getDbList().contains(dbName)) {
                 throw new DatabaseException(ErrorCodes.DB_DOES_NOT_EXISTS, "DB [" + dbName + "] DOES NOT EXIST");
             }
 
             long count = mongoInstance.getDB(dbName).getCollection(bucketName + ".files").count();
             result.put("count", count);
 
         } catch (Exception e) {
             throw new DocumentException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, e.getMessage());
         }
         return result;
     }
 }
