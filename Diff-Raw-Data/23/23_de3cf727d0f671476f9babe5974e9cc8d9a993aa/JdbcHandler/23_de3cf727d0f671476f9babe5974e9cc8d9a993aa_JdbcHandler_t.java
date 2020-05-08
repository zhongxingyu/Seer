 package jdbc.handlers;
 
 import org.vertx.java.core.*;
 import org.vertx.java.core.eventbus.*;
 import org.vertx.java.core.json.*;
 
 import java.sql.*;
 
 import jdbc.Config;
 
 public abstract class JdbcHandler
    implements Handler<Message<JsonObject>> {
 
    private Config config;
 
    public JdbcHandler(Config config){
       this.config = config;
    }
 
    Connection openConnection() 
       throws SQLException {
 
       return DriverManager.getConnection(
          config.url,
          config.username,
          config.password
       );
    }
 
    JsonObject resultSetToJsonObject(ResultSet rs)
       throws SQLException {
 
       /* Retrieve MetaData of ResultSet */
       ResultSetMetaData metadata = rs.getMetaData();
       int columnCount = metadata.getColumnCount();
       int[] types = new int[columnCount];
       String[] names = new String[columnCount];
 
       /* Do note that JDBC columns are 1-based */
       for(int i = 1; i <= columnCount; i++){
          types[i-1] = metadata.getColumnType(i);
          names[i-1] = metadata.getColumnLabel(i);
       }
 
       JsonObject result = new JsonObject();
       JsonArray data = new JsonArray();
 
       while(rs.next()){
          JsonObject row = new JsonObject();
 
          for(int i = 1; i <= columnCount; i++){
             final int type = types[i-1];
             final String name = names[i-1];
 
             switch(types[i-1]){
                
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:
                case Types.BIGINT:
                   row.putNumber(name,rs.getInt(i));
                   break;
 
                case Types.CHAR:
                case Types.NCHAR:
                case Types.VARCHAR:
                case Types.NVARCHAR:
                   row.putString(name,rs.getString(i));
                   break;
             }
          }
 
          data.addObject(row);
             
       }
 
       result.putArray("result", data);
      result.putBoolean("success", true);
 
       return result;
    }


   JsonObject generateErrorReply(Exception e){
      return 
         new JsonObject()
            .putBoolean("success", false)
            .putString("error",e.getMessage());


   }
} 
