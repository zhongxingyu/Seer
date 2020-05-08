 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Xottab
  * Date: 09.07.13
  * Time: 21:35
  * To change this template use File | Settings | File Templates.
  */
 public class GettingAllRequest extends Request {
     public GettingAllRequest(String[] parameters, RequestType type) throws ProcessingException {
         super(parameters, type);
     }
 
     @Override
     void executeHelper() throws SQLException {
     }
 
     List<String> getResults() throws ProcessingException {
         synchronized (connect) {
             try {
                 List<String> list = new ArrayList<String>();
                 Statement stmt = connect.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE TRUE");
                 while (rs.next()) {
                     list.add(rs.getString("username"));
                 }
                 return list;
             } catch (SQLException e) {
                 throw new ProcessingException("Database error");
             }
         }
     }
 }
