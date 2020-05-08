 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package control;
 
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import model.Employee;
 
 /**
  *
  * @author Marc
  */
 public class DBHandler {
 
     private Connection conn;
     private Statement stmt;
     private String user;
     private String pw;
     private String host;
     private String port;
     private String dbName;
     private ArrayList<Employee> employeeList;
 
     public DBHandler(Connection conn, Statement stmt, String user, String pw, String host, String port, String dbName) {
         this.conn = conn;
         this.stmt = stmt;
         this.user = user;
         this.pw = pw;
         this.host = host;
         this.port = port;
         this.dbName = dbName;
 
     }
 
     public DBHandler() {
     }
 
     public DBHandler(String user, String pw, String host, String port, String dbName) throws SQLException {
         this.user = user;
         this.pw = pw;
         this.host = host;
         this.port = port;
         this.dbName = dbName;
         connect();
 
     }
 
     private boolean connect() throws SQLException {
 
         boolean connected = true;
         String connString = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
 
         try {
             conn = (Connection) DriverManager.getConnection(connString, user, pw);
             stmt = (Statement) conn.createStatement();
 
         } catch (Exception ex) {
             connected = false;
         }
         System.out.println("Connected " + connected);
         return connected;
 
     }
 
     public boolean correctPassword(String username, String password) throws SQLException {
         boolean correctPassword = false;
         String query = "SELECT COUNT(*) as total FROM employee WHERE Username = '" + username + "' AND Password = '" + password + "'";
         System.out.println(query);
         ResultSet rs = stmt.executeQuery(query);
 
         while (rs.next()) {
             if (rs.getInt("total") == 1) {
                 correctPassword = true;
             }
         }
         rs.close();
         return correctPassword;
     }
 
     public Employee retrieveUser(String username) throws SQLException {
         Employee user = null;
         String query = "SELECT * FROM employee WHERE Username = username;";
 
         ResultSet rs = stmt.executeQuery(query);
         if (rs.next()) {
             String name = rs.getString("Fullname");
             String pass = rs.getString("password");
             int accessLevel = rs.getInt("accessLevel");
 
             user = new Employee(username, name, pass, accessLevel);
 
         }
 
         return user;
 
     }
   
    public boolean insertEmployee(Employee employee) throws SQLException{
         boolean inserted = false;
         
         String query = "Insert into employee (Username,Fullname, Accesslevel,Password)"
                 + "Values ('"+employee.getUsername()+"', '"+employee.getName()+"','"+employee.getAccessLevel()+"','"+employee.getPassword()+")";
 
         
      int result = stmt.executeUpdate(query);
      if(result != 0){
          inserted = true;
      }
      return inserted;  
         
     }
 
     public ArrayList<Employee> retrieveAllUsers() throws SQLException {
         employeeList = new ArrayList<>();
         String query = "SELECT * FROM employee";
         ResultSet rs = stmt.executeQuery(query);
         while (rs.next()) {
             String name = rs.getString("Fullname");
             String username = rs.getString("Username");
             String password = rs.getString("Password");
             int al = rs.getInt("Accesslevel");
 
             Employee user = new Employee(username, name, password, al);
             System.out.println(name + username);
 
             employeeList.add(user);
         }
         return employeeList;
 
     }
 
     public Connection getConn() {
         return conn;
     }
 
     public void setConn(Connection conn) {
         this.conn = conn;
     }
 
     public Statement getStmt() {
         return stmt;
     }
 
     public void setStmt(Statement stmt) {
         this.stmt = stmt;
     }
 
     public String getUser() {
         return user;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public String getPw() {
         return pw;
     }
 
     public void setPw(String pw) {
         this.pw = pw;
     }
 
     public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public String getPort() {
         return port;
     }
 
     public void setPort(String port) {
         this.port = port;
     }
 
     public String getDbName() {
         return dbName;
     }
 
     public void setDbName(String dbName) {
         this.dbName = dbName;
     }
 }
