 package jdbc.handlers;
 
 import org.vertx.java.core.*;
 import org.vertx.java.core.eventbus.*;
 import org.vertx.java.core.json.*;
 
 import java.sql.*;
 
 import jdbc.Config;
 
 public class SelectHandler extends JdbcHandler {
 
    public SelectHandler(Config config){
       super(config);
    }
 
    public void handle(Message<JsonObject> message){
 
       System.out.println("Message Received: " + message.body);
 
       try(
          final Connection conn = super.openConnection()
       ){
          final String query = message.body.getString("query"); 
          final JsonArray params = message.body.getArray("params");
 
          final PreparedStatement stmt = conn.prepareStatement(
             query
          );
 
          int count = 0;
          for(Object param : params){
             count ++;
             setParameter(stmt,(JsonObject)param,count);
          }
 
          final ResultSet rs = stmt.executeQuery();
          final JsonObject result = resultSetToJsonObject(rs);
 
          message.reply(result);
       }catch(SQLException e){
          e.printStackTrace();
       }
    }
 
 
    private void setParameter(
       PreparedStatement stmt,
       JsonObject param,
       int index
    ) throws SQLException {
       final String type = param.getString("type");
 
 
       /* Information about mapping SQL Types to Java Types can 
        * be found here
        * http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/
             getstart/mapping.html 
        **/
       switch(type){
          /* TINYINT and SMALLINT map to Short */
          case "TINYINT":
          case "SMALLINT":
             stmt.setShort(index,param.getNumber("value").shortValue());
             break;
 
          case "INTEGER":
             stmt.setInt(index,param.getNumber("value").intValue());
             break;
 
          case "BIGINT":
             stmt.setLong(index,param.getNumber("value").longValue());
             break;
 
          /* String Types*/
          case "CHAR":
          case "NCHAR":
          case "VARCHAR":
          case "NVARCHAR":
             stmt.setString(index,param.getString("value"));
             break;
       }
    }
 }
