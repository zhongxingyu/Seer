 import static spark.Spark.*;
 import spark.*;
 import java.sql.*;
 import java.net.*;
 
 public class Aeserv {
 
    static Connection conn;
 
    public static void main(String[] args) {
       
       setPort(Integer.parseInt(System.getenv("PORT")));
       
       get (new Route("/hello") {
          @Override
          public Object handle (Request request, Response response) {
             return "Hello World!";
          }
       });
 
       post (new Route("/new") {
          @Override
          public Object handle (Request request, Response response) {
             try {
                saveMessage(request.queryParams("to"), request.body());
                response.status(200);
                return "OK";
            } else {
                e.printStackTrace();
                response.status(500);
                return "FAIL";
             }
          }
       });
 
       try {
          conn = getConnection();
          createTables();
          // st = conn.createStatement();
          // ResultSet rs = st.executeQuery("SELECT * FROM cities");
          // while (rs.next()) {
          //     System.out.print("Column 1 returned ");
          //     System.out.println(rs.getString(1));
          // }
          // rs.close();
       } catch (Exception e) {
          e.printStackTrace();
          System.exit(1);
       }
 
    }
 
    static void createTables () throws URISyntaxException, SQLException {
       Statement st = conn.createStatement();
       st.execute("CREATE TABLE IF NOT EXISTS messages (user varchar(80), message text);");
    }
 
    static void saveMessage (String user, String message) throws SQLException {
       Statement st = conn.createStatement();
       PreparedStatement ps = conn.prepareStatement("INSERT INTO messages VALUES (?, ?)");
       ps.setString(1, user);
       ps.setString(2, message);
       ps.executeQuery();
    }
 
    static void readMessages (String user) {
 
    }
 
    static Connection getConnection () throws URISyntaxException, SQLException {
       try {
          Class.forName("org.postgresql.Driver");
       } catch (Exception e) {
          System.out.println("FUCKFUCKFUCK");
       }
       URI dbUri = new URI(System.getenv("DATABASE_URL"));
 
       String username = dbUri.getUserInfo().split(":")[0];
       String password = dbUri.getUserInfo().split(":")[1];
       String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + ":" + dbUri.getPort();
 
       return DriverManager.getConnection(dbUrl, username, password);
    }
 
 }
 
