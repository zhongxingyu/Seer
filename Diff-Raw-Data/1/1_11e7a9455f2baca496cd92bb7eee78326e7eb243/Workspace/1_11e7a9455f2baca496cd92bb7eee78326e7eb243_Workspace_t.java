 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.kbase.psrest.resources;
 
 import com.mongodb.WriteResult;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import com.mongodb.Mongo;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.util.JSON;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 import java.util.Set;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PUT;
 import javax.ws.rs.PathParam;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import us.kbase.psrest.util.MongoConnection;
 import us.kbase.psrest.util.SystemProperties;
 import us.kbase.psrest.util.Tokens;
 
 /**
  *
  * @author qvh
  */
 @Path("/ps")
 public class Workspace {
     
      Mongo m = MongoConnection.getMongo();
      
      /**
       * given a workspaceID - return all metadata about that workspace.
       * @param workspaceID
       * @return 
       */
      @GET
      @Path("/workspace/{workspaceid}")
      @Produces("application/json")
      public String getWorkspaceJSON(@PathParam("workspaceid") String workspaceID) {
          BasicDBObject dbo = new BasicDBObject(); 
          DB db = m.getDB( Tokens.WORKSPACE_DATABASE );
          DBCollection coll = db.getCollection(Tokens.METADATA_COLLECTION);
          BasicDBObject query = new BasicDBObject(); 
          BasicDBObject bo = (BasicDBObject) JSON.parse("{ key :\"" + workspaceID + "\" }"); //JSON2BasicDBObject
          DBCursor dbc = coll.find(bo); 
          return dbc.next().toString(); //need to catch exceptions and so on...
      }
      
      /**
       * save a document to the workspace provided
       * @param workspaceID
       * @return 
       */     
      @PUT
      @Path("/document/{workspaceid}")
      //@Consumes("application/json")
      @Produces("application/json")
      public String saveDocument(@PathParam("workspaceid") String workspaceID, String jsonString) { //, String jsonString
          //System.out.println(jsonString);
          //System.out.println(workspaceID);
          //System.out.println(jsonString);
          DB db = m.getDB( Tokens.WORKSPACE_DATABASE );
          DBCollection coll = db.getCollection(workspaceID);
          BasicDBObject bo = (BasicDBObject) JSON.parse(jsonString);
          WriteResult save = coll.save(bo);
         //System.out.println(workspaceID);
          return save.toString();
      }
      
      /**
       * save a document to the workspace provided
       * @param workspaceID
       * @return 
       */     
      @GET
      @Path("/document/find/{workspaceid}")
      @Consumes("application/json")
      @Produces("application/json")
      public String findDocument(@PathParam("workspaceid") String workspaceID, String jsonString) { //, String jsonString
          String ret = "{\n";
          //System.out.println(jsonString);
          //System.out.println(workspaceID);
          //System.out.println(jsonString);
          int counter = 0;
          DB db = m.getDB( Tokens.WORKSPACE_DATABASE );
          DBCollection coll = db.getCollection(workspaceID);
          BasicDBObject bo = (BasicDBObject) JSON.parse(jsonString);
          DBCursor find = coll.find(bo);
          Iterator<DBObject> iter = find.iterator();
          while(iter.hasNext()){
              counter++;
             if(counter > 1) ret += ",";
              DBObject next = iter.next();
              ret+= "\"kbid" + counter + "\" : "; 
              ret+= next.toString();
          }
          ret += "\n}\n";
         //System.out.println(workspaceID);
          return ret;
      }
      
      @GET
      @Path("/documents/{workspaceid}")
      @Produces("application/json")
      public JSONObject getDocIDs(@PathParam("workspaceid") String workspaceID) {
          
          DB db = m.getDB( Tokens.WORKSPACE_DATABASE );
          
          DBCollection coll = db.getCollection(workspaceID);
          DBCursor documents = coll.find();
          JSONObject docList = new JSONObject();
          int counter = 0;
          try {
              while(documents.hasNext()){
                     DBObject next = documents.next();
                     System.out.println(next.get("_id"));
                     docList.put(new Integer(counter).toString(), next.get("_id"));
                     counter++;
              }
          } catch (JSONException ex) {
             Logger.getLogger(Workspace.class.getName()).log(Level.SEVERE, null, ex);
          }
          System.err.println(workspaceID);
          return docList;
      }
      
 //     //ps/store/<key> 
 //     @PUT
 //     @Path("/store/{workspaceid}")
 //     @Produces("application/json")
 //     @Consumes("application/json")
 //     public JSONObject storeDocument( String message, @PathParam("workspaceid") String workspaceID){
 //         DBCollection coll = db.getCollection(workspaceID);
 //         BasicDBObject bo = (BasicDBObject) JSON.parse(message);
 //         WriteResult save = coll.save(bo);
 //         JSONObject response = new JSONObject();
 //         try {
 //             System.out.println(workspaceID);
 //             System.out.println(message);
 //             response.put("status", "success");
 //             response.put("mongoresponse", save.toString());
 //             response.put("content", message);
 //         } catch (JSONException ex) {
 //            Logger.getLogger(Workspace.class.getName()).log(Level.SEVERE, null, ex);
 //         }
 //         return response;
 //     }
     
 }
 
