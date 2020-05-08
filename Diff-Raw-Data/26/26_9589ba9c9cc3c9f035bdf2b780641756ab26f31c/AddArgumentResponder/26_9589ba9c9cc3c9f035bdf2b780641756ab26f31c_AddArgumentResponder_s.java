 package org.agora.server.queries;
 
 import java.sql.SQLException;
 
 import org.agora.lib.IJAgoraLib;
 import org.agora.logging.Log;
 import org.agora.server.*;
 import org.agora.server.database.DBAddArgument;
 import org.bson.BasicBSONObject;
 
 public class AddArgumentResponder implements QueryResponder {
   
   @Override
   public BasicBSONObject respond(BasicBSONObject query, JAgoraServer server) {
     BasicBSONObject bsonResponse = new BasicBSONObject();
     
     boolean verified = server.verifySession(query);
     
     if (!verified) {
       bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_FAIL);
       bsonResponse.put(IJAgoraLib.REASON_FIELD, "Invalid session ID.");
       return bsonResponse;
     }
     
     try {
      DBAddArgument.addArgumentToDB(query, server.createDatabaseConnection());
     } catch (SQLException e) {
       Log.error("[AddArgumentResponder] Could not execute add argument query ("+e.getMessage()+")");
       bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_FAIL);
       bsonResponse.put(IJAgoraLib.REASON_FIELD, "Server failure.");
       return bsonResponse;
     }
     
     
     bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_OK);
     
     return bsonResponse;
   }
 
 }
