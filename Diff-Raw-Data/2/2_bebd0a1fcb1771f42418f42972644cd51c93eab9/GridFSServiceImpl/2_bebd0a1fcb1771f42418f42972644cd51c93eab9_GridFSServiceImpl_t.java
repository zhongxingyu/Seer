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
 
 import com.imaginea.mongodb.common.MongoInstanceProvider;
 import com.imaginea.mongodb.common.SessionMongoInstanceProvider;
 import com.imaginea.mongodb.common.exceptions.*;
 import com.mongodb.*;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSDBFile;
 import com.mongodb.gridfs.GridFSInputFile;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import org.bson.types.ObjectId;
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
  * @since Dec 3, 2008
  */
 public class GridFSServiceImpl implements GridFSService {
 
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
      * instance to perform operations on files. The instance is created
      * based on a userMappingKey which is received from the file request
      * dispatcher and is obtained from tokenId of user.
      *
      * @param dbInfo A combination of username,mongoHost and mongoPort
      */
     public GridFSServiceImpl(String dbInfo) {
         mongoInstanceProvider = new SessionMongoInstanceProvider(dbInfo);
     }
 
     /**
      * Service implementation for creating GridFS store in the specified database.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @returns Status message.
      */
     public String createStore(String dbName, String bucketName) throws EmptyDatabaseNameException, EmptyCollectionNameException {
         mongoInstance = mongoInstanceProvider.getMongoInstance();
 
         if (dbName == null) {
             throw new EmptyDatabaseNameException("Database Name Is Null");
         }
         if (dbName.equals("")) {
             throw new EmptyDatabaseNameException("Database Name Empty");
         }
         if (bucketName == null) {
             throw new EmptyCollectionNameException("Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new EmptyCollectionNameException("Bucket Name Empty");
         }
 
         new GridFS(mongoInstance.getDB(dbName), bucketName);
 
        return "GridFS bucket [" + bucketName + "] added to database [" + dbName + "].";
     }
 
     /**
      * Service implementation for getting the list of files stored in GridFS of specified database.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @returns JSON representation of list of all files as a String.
      */
     public ArrayList<DBObject> getFileList(String dbName, String bucketName) throws ValidationException, DatabaseException, CollectionException {
 
         mongoInstance = mongoInstanceProvider.getMongoInstance();
 
         if (dbName == null) {
             throw new EmptyDatabaseNameException("Database Name Is Null");
         }
         if (dbName.equals("")) {
             throw new EmptyDatabaseNameException("Database Name Empty");
         }
 
         ArrayList<DBObject> fileList = new ArrayList<DBObject>();
 
         try {
             if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                 throw new UndefinedDatabaseException(
 
                     "Database with dbName [ " + dbName + "] does not exist");
             }
 
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
             Field field = GridFS.class.getDeclaredField("_filesCollection");
             field.setAccessible(true);
             DBCollection filesCollection = (DBCollection) field.get(gridFS);
             DBCursor cursor = filesCollection.find().sort(new BasicDBObject("uploadDate", -1));
             Iterator<DBObject> it = cursor.iterator();
 
             while (it.hasNext()) {
                 fileList.add(it.next());
             }
 
         } catch (Exception m) {
             CollectionException e = new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, "GET_FILES_LIST_EXCEPTION", m.getCause());
             throw e;
         }
         return fileList;
 
     }
 
     /**
      * Service implementation for retrieving the specified file stored in GridFS.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @param id         ObjectId of the file to be retrieved
      * @returns Requested multipartfile for viewing or download based on 'download' param.
      */
     public File getFile(String dbName, String bucketName, String id) throws ValidationException, DatabaseException, CollectionException {
         mongoInstance = mongoInstanceProvider.getMongoInstance();
 
         if (dbName == null) {
             throw new EmptyDatabaseNameException("Database Name Is Null");
         }
         if (dbName.equals("")) {
             throw new EmptyDatabaseNameException("Database Name Empty");
         }
         File tempFile = null;
         try {
             if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                 throw new UndefinedDatabaseException(
 
                     "Database with dbName [ " + dbName + "] does not exist");
             }
 
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
             GridFSDBFile gridFSDBFile = gridFS.findOne(new ObjectId(id));
             String tempDir = System.getProperty("java.io.tmpdir");
             tempFile = new File(tempDir + "/" + gridFSDBFile.getFilename());
             gridFSDBFile.writeTo(tempFile);
 
         } catch (MongoException m) {
             CollectionException e = new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, "GET_FILE_EXCEPTION", m.getCause());
             throw e;
         } catch (IOException e) {
             CollectionException ce = new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, "GET_FILE_EXCEPTION", e.getCause());
             throw ce;
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
      * @param dbInfo      Mongo Db Configuration provided by user to connect to.
      * @returns Success message with additional file details such as name, size,
      * download url & deletion url as JSON Array string.
      */
     public JSONArray insertFile(String dbName, String bucketName, String dbInfo, InputStream inputStream, FormDataBodyPart formData) throws DatabaseException, CollectionException, DocumentException, ValidationException {
         mongoInstance = mongoInstanceProvider.getMongoInstance();
         if (dbName == null) {
             throw new EmptyDatabaseNameException("Database name is null");
 
         }
         if (dbName.equals("")) {
             throw new EmptyDatabaseNameException("Database Name Empty");
         }
 
         if (bucketName == null) {
             throw new EmptyCollectionNameException("Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new EmptyCollectionNameException("Bucket Name Empty");
         }
 
         JSONArray result = new JSONArray();
         FormDataContentDisposition fileData = formData.getFormDataContentDisposition();
         try {
             if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                 throw new UndefinedDatabaseException("DB [" + dbName + "] DOES NOT EXIST");
             }
 
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
             GridFSInputFile fsInputFile = gridFS.createFile(inputStream, fileData.getFileName());
             fsInputFile.setContentType(formData.getMediaType().toString());
             fsInputFile.save();
             JSONObject obj = new JSONObject();
             obj.put("name", fsInputFile.getFilename());
             obj.put("size", fsInputFile.getLength());
             obj.put("url", String.format("services/%s/%s/gridfs/getfile?id=%s&download=%s&dbInfo=%s&ts=%s", dbName, bucketName, fsInputFile.getId().toString(), false, dbInfo, new Date()));
             obj.put("delete_url", String.format("services/%s/%s/gridfs/dropfile?id=%s&dbInfo=%s&ts=%s", dbName, bucketName, fsInputFile.getId().toString(), dbInfo, new Date().getTime()));
             obj.put("delete_type", "GET");
             result.put(obj);
 
         } catch (Exception e) {
             CollectionException ce = new CollectionException(ErrorCodes.UPLOAD_FILE_EXCEPTION, "UPLOAD_FILE_EXCEPTION", e.getCause());
             throw ce;
         }
         return result;
     }
 
     /**
      * Service implementation for dropping a file from GridFS.
      *
      * @param dbName     Name of Database
      * @param bucketName Name of GridFS Bucket
      * @param id         Object id of file to be deleted
      * @returns Status message.
      */
     public String deleteFile(String dbName, String bucketName, ObjectId id) throws DatabaseException, DocumentException, ValidationException {
         mongoInstance = mongoInstanceProvider.getMongoInstance();
         if (dbName == null) {
             throw new EmptyDatabaseNameException("Database name is null");
 
         }
         if (dbName.equals("")) {
             throw new EmptyDatabaseNameException("Database Name Empty");
         }
 
         if (bucketName == null) {
             throw new EmptyCollectionNameException("Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new EmptyCollectionNameException("Bucket Name Empty");
         }
 
         String result = null;
         GridFSDBFile gridFSDBFile = null;
         try {
             if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                 throw new UndefinedDatabaseException("DB [" + dbName + "] DOES NOT EXIST");
             }
             if (id == null) {
                 throw new EmptyDocumentDataException("File is empty");
             }
 
             GridFS gridFS = new GridFS(mongoInstance.getDB(dbName), bucketName);
 
             gridFSDBFile = gridFS.findOne(id);
 
             if (gridFSDBFile == null) {
                 throw new UndefinedDocumentException("DOCUMENT_DOES_NOT_EXIST");
             }
 
             gridFS.remove(id);
 
         } catch (MongoException e) {
             throw new DeleteDocumentException("FILE_DELETION_EXCEPTION");
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
     public String dropBucket(String dbName, String bucketName) throws DatabaseException, DocumentException, ValidationException {
         mongoInstance = mongoInstanceProvider.getMongoInstance();
         if (dbName == null) {
             throw new EmptyDatabaseNameException("Database name is null");
 
         }
         if (dbName.equals("")) {
             throw new EmptyDatabaseNameException("Database Name Empty");
         }
 
         if (bucketName == null) {
             throw new EmptyCollectionNameException("Bucket name is null");
         }
         if (bucketName.equals("")) {
             throw new EmptyCollectionNameException("Bucket Name Empty");
         }
 
         String result = null;
         try {
             if (!mongoInstance.getDatabaseNames().contains(dbName)) {
                 throw new UndefinedDatabaseException("DB [" + dbName + "] DOES NOT EXIST");
             }
 
             mongoInstance.getDB(dbName).getCollection(bucketName + ".files").drop();
             mongoInstance.getDB(dbName).getCollection(bucketName + ".chunks").drop();
 
         } catch (MongoException e) {
             throw new DeleteDocumentException("FILES_DELETION_EXCEPTION");
         }
         result = "Bucket [" + bucketName + "] has been deleted from Database [" + dbName + "].";
         return result;
     }
 }
