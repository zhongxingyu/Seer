 package org.agora.server.database;
 
 import java.io.ByteArrayInputStream;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
import org.agora.lib.IJAgoraLib;
 import org.agora.server.DatabaseConnection;
 import org.agora.server.Options;
 import org.bson.BSONEncoder;
 import org.bson.BSONObject;
 import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;
 
 public class DBAddArgument {
 
   protected static String ADD_QUERY = "INSERT INTO arguments (source_ID_attacker, thread_ID, user_ID, content) VALUES (?, ?, ?, ?);";
   
  public static boolean addArgumentToDB(BasicBSONObject request, DatabaseConnection dbc) throws SQLException {
    BSONObject content = (BSONObject)request.get(IJAgoraLib.CONTENT_FIELD);
    int threadID = request.getInt(IJAgoraLib.THREAD_ID_FIELD);
    int userID = request.getInt(IJAgoraLib.USER_ID_FIELD);
     
     PreparedStatement ps = dbc.prepareStatement(ADD_QUERY);
     ps.setString(1, Options.SERVER_URL);
     ps.setInt(2, threadID);
     ps.setInt(3, userID);
     
     BSONEncoder benc = new BasicBSONEncoder();
     byte[] b = benc.encode(content); 
     
     ps.setBinaryStream(4, new ByteArrayInputStream(b));
     
     ps.addBatch();
     
     int[] res = ps.executeBatch();
     
    // TODO: needed?
    // dbc.commit(); // This gives an error.
     dbc.close();
 
     return res[0] != 0;
   }
 }
