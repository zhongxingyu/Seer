 package com.filemanager.model;
 
 import java.io.IOException;
 
 import org.bson.types.ObjectId;
 
 import com.database.connector.Database;
 import com.indexer.model.Indexer;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.WriteResult;
 
 public class FileManagerModel {
 	public DBObject userObj;
 	
 	public FileManagerModel() throws Exception{
 		Database.getInstance().connect();
 		this.userObj = null;
 	}
 
 	public DBObject get(String uid) {
 		DBCollection userColl = Database.getInstance().getCollection("user");
 		BasicDBObject query = new BasicDBObject();
 		query.put("_id", new ObjectId(uid));
 		
 		DBCursor cursor = userColl.find(query);
 		
 		while(cursor.hasNext()) {
 			this.userObj = cursor.next();
 		}
 		return this.userObj;
 	}
 	
 	public String delete(String bookId, String uid) throws IOException {
 		
 		String result = "";
 		//first delete from pages
 		
 		DBCollection pageColl = Database.getInstance().getCollection("pages");
 		BasicDBObject pages = new BasicDBObject();
 		pages.put("bookId", bookId);
 		WriteResult pageDelResult = pageColl.remove(pages);
 		System.out.println("Page deletion result: "+pageDelResult.getError());
 		
 		//and then delete from user record
 		
 		DBCollection userColl = Database.getInstance().getCollection("user");
 		
 		BasicDBObject exists = new BasicDBObject();
 	    exists.put("$exists", "true");
 	    
 	    BasicDBObject updateQuery = new BasicDBObject();
 	    updateQuery.put("books", exists);
 	    
 	    BasicDBObject id = new BasicDBObject("id", bookId);
 	    BasicDBObject books = new BasicDBObject("books", id);
 	    
 	    BasicDBObject updateCommand = new BasicDBObject();
 	    updateCommand.put("$pull", books);
 	    System.out.println(updateQuery + " " + updateCommand);
 	    WriteResult bookDelResult = userColl.update( updateQuery, updateCommand, false, true );
 	    
 	    System.out.println("Book deletion result: "+bookDelResult.getError());
 	    
 	    if(pageDelResult.getError() == null && bookDelResult.getError() == null) {
 	    	Indexer indexer = new Indexer("/home/irmak/index/cakmaki@itu.edu.tr");
 	    	result = "Success";
 	    	indexer.close();
 	    }
 	    //db.user.update({"books":{$exists:true}}, {$pull:{"books":{"id":"4f0ff3a744ae8c994da19e36"}}},false,true)
 
 	    return result;
 
 	}
 
 }
