 package connectivity;
 
 import java.sql.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import roosterprogramma.Utils;
 
 /**
  *
  * @author UDP
  */
 public class Dbmanager {
 
     /**
      * The constant for all JDBC exceptions
      */
     public static final String JDBC_EXCEPTION = "JDBC Exception: ";
     /**
      * The constant for all SQL exceptions
      */
     public static final String SQL_EXCEPTION = "SQL Exception: ";
     private static final String sqlexpress_instance = "ZAANSMUSEUM", sqlexpress_address = "zm-server11", sqlexpress_user = "sa", sqlexpress_pass = "";
     private static final String mysql_port = "3306", mysql_address = "127.0.0.1", mysql_user = "root", mysql_pass = "mangos";
     private static final String databasename = "Roosterprogramma";
     /**
      * The actual connection
      */
    private Connection connection;
     private static boolean mysql = false;
 
     /**
      * Open database connection
      */
     public void openConnection() {
         try {
             String url;
             if (mysql) {
                 Class.forName("com.mysql.jdbc.Driver");
                 url = "jdbc:mysql://" + mysql_address
                         + ":" + mysql_port
                         + "/" + databasename
                         + "?user=" + mysql_user
                         + "&password=" + mysql_pass;
             } else {
                 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                 url = "jdbc:sqlserver://" + sqlexpress_address
                         + "\\" + sqlexpress_instance
                         + ";databaseName=" + databasename
                         + ";userName=" + sqlexpress_user
                         + ";password=" + sqlexpress_pass;
             }
             DriverManager.setLoginTimeout(15);
             connection = DriverManager.getConnection(url);
         } catch (Exception ex) {
            Utils.showMessage("Kon geen verbinding met de database maken...\nProbeer het later nog eens of neem contact op met Joke Terol.", "Database fout.", ex.getMessage(), true);
         }
     }
 
     /**
      * Close database connection
      */
     public void closeConnection() {
         try {
             connection.close();
         } catch (SQLException ex) {
             Logger.getLogger(Dbmanager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
     * Executes a query without result. Use for UPDATE queries.
      *
      * @param query
      * @throws SQLException
      */
     public void executeUpdate(String query) throws SQLException {
         connection.createStatement().executeUpdate(query);
     }
 
     /**
      * Executes a query with result.
      *
      * @param query
      * @return
      * @throws SQLException
      */
     public ResultSet doQuery(String query) throws SQLException {
         return connection.createStatement().executeQuery(query);
     }
 
     /**
      * Executes a query with result.
      *
      * @param query
      * @return
      * @throws SQLException
      */
     public ResultSet insertQuery(String query) throws SQLException {
         Statement statement = connection.createStatement();
         statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
         return statement.getGeneratedKeys();
     }
 }
