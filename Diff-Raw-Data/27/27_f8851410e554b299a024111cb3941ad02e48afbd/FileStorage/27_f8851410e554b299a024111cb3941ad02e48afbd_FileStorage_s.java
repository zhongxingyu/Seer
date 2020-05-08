 package org.kvj.lima1.gae.sync.data;
 
 import org.codehaus.jettison.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Transaction;
 
 public class FileStorage {
 
 	private static Logger log = LoggerFactory.getLogger(FileStorage.class);
 
 	public static String upload(String app, String user, BlobKey file,
 			String name) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		Transaction txn = datastore.beginTransaction();
 		try {
 			JSONObject schema = SchemaStorage.getInstance().getSchema(app);
 			if (null == schema) {
 				throw new Exception("Schema not found");
 			}
 			Query existing = new Query("User").addFilter("username",
 					FilterOperator.EQUAL, user);
 			Entity userEntity = datastore.prepare(existing).asSingleEntity();
 			if (null == userEntity) {
 				return "User not found";
 			}
 			Entity fileEntity = new Entity("File", userEntity.getKey());
 			fileEntity.setProperty("app", app);
 			fileEntity.setProperty("user", userEntity.getKey());
 			fileEntity.setProperty("file", file);
 			fileEntity.setProperty("name", name);
 			datastore.put(txn, fileEntity);
 			txn.commit();
 			return null;
 		} catch (Exception e) {
 			log.error("Save error", e);
 			return "Database error";
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 			}
 		}
 	}
 
 	public static BlobKey getFile(String app, String user, String name) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		try {
 			JSONObject schema = SchemaStorage.getInstance().getSchema(app);
 			if (null == schema) {
 				throw new Exception("Schema not found");
 			}
 			Query existing = new Query("User").addFilter("username",
 					FilterOperator.EQUAL, user);
 			Entity userEntity = datastore.prepare(existing).asSingleEntity();
 			if (null == userEntity) {
 				throw new Exception("User not found");
 			}
 			Query file = new Query("File")
 					.addFilter("user", FilterOperator.EQUAL,
 							userEntity.getKey())
 					.addFilter("app", FilterOperator.EQUAL, app)
 					.addFilter("name", FilterOperator.EQUAL, name);
 			Entity fileEntity = datastore.prepare(file).asSingleEntity();
 			if (null == fileEntity) {
 				throw new Exception("File not found");
 			}
 			return (BlobKey) fileEntity.getProperty("file");
 		} catch (Exception e) {
			log.error("Save error", e);
 			return null;
 		}
 	}
 
 	public static String removeFile(String app, String user, String name) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		Transaction txn = datastore.beginTransaction();
 		try {
 			JSONObject schema = SchemaStorage.getInstance().getSchema(app);
 			if (null == schema) {
 				throw new Exception("Schema not found");
 			}
 			Query existing = new Query("User").addFilter("username",
 					FilterOperator.EQUAL, user);
 			Entity userEntity = datastore.prepare(existing).asSingleEntity();
 			if (null == userEntity) {
 				return "User not found";
 			}
 			Query file = new Query("File")
 					.addFilter("user", FilterOperator.EQUAL,
 							userEntity.getKey())
 					.addFilter("app", FilterOperator.EQUAL, app)
 					.addFilter("name", FilterOperator.EQUAL, name);
 			Entity fileEntity = datastore.prepare(file).asSingleEntity();
 			if (null == fileEntity) {
 				return null;
 			}
 			datastore.delete(txn, fileEntity.getKey());
 			txn.commit();
 			return null;
 		} catch (Exception e) {
 			log.error("Save error", e);
 			return "Database error";
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 			}
 		}
 	}
 }
