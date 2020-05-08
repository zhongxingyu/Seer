 package org.agora.server.queries;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.agora.lib.*;
 import org.agora.logging.Log;
 import org.agora.server.*;
 import org.bson.BasicBSONObject;
 
 public class LoginResponder implements QueryResponder {
   
   @Override
   public BasicBSONObject respond(BasicBSONObject query, JAgoraServer server) {
     BasicBSONObject bsonResponse = new BasicBSONObject();
     
     try (DatabaseConnection dbc = server.createDatabaseConnection()) {
       if (dbc == null) {
         Log.error("[LoginResponder] Could not connect to database.");
         bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_FAIL);
         bsonResponse.put(IJAgoraLib.REASON_FIELD, "Server failure.");
         return bsonResponse;
       }
       
       // TODO: password is already hashed?
       String user = query.getString(IJAgoraLib.USER_FIELD);
       String pass = Util.hash(query.getString(IJAgoraLib.PASSWORD_FIELD));
       
       Statement s = dbc.produceStatement();
       if (s == null) {
         Log.error("[LoginResponder] Could not create statement.");
         bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_FAIL);
         bsonResponse.put(IJAgoraLib.REASON_FIELD, "Server failure.");
         return bsonResponse;
       }
       //TODO: input sanitisation
       String strQuery = "SELECT user_ID FROM users WHERE " + 
           "username = '" + user + "' AND " + 
           "password = '" + pass + "';";
       ResultSet rs = s.executeQuery(strQuery);
 
       
       boolean logged = rs.next();
       
       if (logged) {
         UserSession session = server.userLogin(user, rs.getInt("user_ID"));
         bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_OK);
         bsonResponse.put(IJAgoraLib.SESSION_ID_FIELD, session.getSessionID());
         bsonResponse.put(IJAgoraLib.USER_ID_FIELD, session.getUserID());
       } else {
         bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_FAIL);
         bsonResponse.put(IJAgoraLib.REASON_FIELD, "Wrong username/password.");
       }
       return bsonResponse;
     } catch (SQLException e) {
       Log.error("[LoginResponder] Could not execute query ("+e.getMessage()+")");
       bsonResponse.put(IJAgoraLib.RESPONSE_FIELD, IJAgoraLib.SERVER_FAIL);
       bsonResponse.put(IJAgoraLib.REASON_FIELD, "Server failure.");
       return bsonResponse;
     }
   }
 
 }
