 package users;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Singleton connection
  * usage:
  * java.sql.Connection conn = Conn.getInstance().getConnection();
  * 
  * Need to add actual exception handling and a way to get the username
  * and password instead of hardcoding it.
  * 
  * @author Mitch
  */
 public class Conn {
   private static Conn instance;
   private Connection conn;
   
   /**
    * Accessor for the Conn
    * @return 
    */
   public static Conn getInstance()
   {
     return instance;
   }
   
   /**
    * Accessor for the jdbc connection
    * @return 
    */
   public Connection getConnection()
   {
     return conn;
   }
   
   
   static
   {
     instance = new Conn();
   }
   
   /**
    * Constructor
    */
   private Conn()
   {
       try 
       {
 	// Load the Oracle JDBC driver
 	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
       }
       catch (SQLException e)
       {
         // handle couldn't find driver
         System.out.println("Couldn't load the driver: " + e.getMessage());
       }
       // somehow get the username and password
       // get rid of the hard code
       String username = "ora_c7e8";
       String password = "a84148014";
       connect(username, password);
   }
   
   private void connect(String username, String password)
   {
       String connectURL = "jdbc:oracle:thin:@localhost:1521:ug";
    try 
    {
      conn = DriverManager.getConnection(connectURL, username, password);
    } 
    catch (SQLException ex) 
    {
      // handle couldn't connect
      // temporary handler
      System.out.println("could not connect: " + ex.getMessage());
    }
   }
   
   
   
   /**
    * For testing
    * @param args 
    */
   public static void main(String[] args) {
     Connection conn = Conn.getInstance().getConnection();
     try
     {
       PreparedStatement stat = conn.prepareStatement("SELECT * FROM Borrower");
       ResultSet rs = stat.executeQuery();
       while (rs.next())
       {
         System.out.println("bid = " + rs.getString(1));
       }
     }
     catch (SQLException e)
     {
       // do nothing
       System.out.println(e.getMessage());
     }
   }
 }
