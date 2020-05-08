 package org.agora.server.database;
 
 import java.io.ByteArrayInputStream;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import org.agora.server.DatabaseConnection;
 import org.agora.server.Options;
 import org.bson.BSONEncoder;
 import org.bson.BSONObject;
 import org.bson.BasicBSONEncoder;
 
 public class DBAddArgument {
 
   protected static String ADD_QUERY = "INSERT INTO arguments (source_ID_attacker, thread_ID, user_ID, content) VALUES (?, ?, ?, ?);";
   
  public static boolean addArgumentToDB(BSONObject content, int threadID, int userID, DatabaseConnection dbc) throws SQLException {
     
     PreparedStatement ps = dbc.prepareStatement(ADD_QUERY);
     ps.setString(1, Options.SERVER_URL);
     ps.setInt(2, threadID);
     ps.setInt(3, userID);
     
     BSONEncoder benc = new BasicBSONEncoder();
     byte[] b = benc.encode(content); 
     
     ps.setBinaryStream(4, new ByteArrayInputStream(b));
     
     ps.addBatch();
     
     int[] res = ps.executeBatch();
     
    ps.close();
     dbc.close();
 
     return res[0] != 0;
   }
 }
