 package main.java.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.*;
 
 public class TestLampServlet extends HttpServlet {
 
     // Database Connection
     private static Connection getConnection() throws URISyntaxException, SQLException {
         URI dbUri = new URI(System.getenv("DATABASE_URL"));
 
         String username = dbUri.getUserInfo().split(":")[0];
         String password = dbUri.getUserInfo().split(":")[1];
 
         // Hardcoded dbUrl:
         // String dbUrl = "jdbc:postgres://ixhixpfgeanclh:p1uyfk5c9yLh1VEWoCOGb4FIEX@ec2-54-225-112-205.compute-1.amazonaws.com:5432/d3lbshfcpi0soa";
         // Heroku dbUrl:
         String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
 
         return DriverManager.getConnection(dbUrl, username, password);
     }
 
     private static String convertIntToStatus(int data_value_int) {
         // Convert int to string
         String status_str = "on";
         if (data_value_int == 0) {
             status_str = "off";
         }
 
         return status_str;
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
             Connection connection = getConnection();
 
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_lamp ORDER BY time DESC LIMIT 1");
             rs.next();
             request.setAttribute("lampStatus", convertIntToStatus(rs.getInt(1)));
             request.setAttribute("lampStatusTime", rs.getString(2));
         }
         catch (SQLException e) {
             request.setAttribute("SQLException", e.getMessage());
         }
         catch (URISyntaxException e) {
             request.setAttribute("URISyntaxException", e.getMessage());
         }
 
         request.getRequestDispatcher("/testlamp-get.jsp").forward(request, response);
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String data_value_str = request.getParameter("data_value");
         data_value_str = data_value_str.toLowerCase();
 
         // Convert string to corresponding int 0-off 1-on
         int data_value_int;
         if (data_value_str == "off") {
             data_value_int = 0;
         }
         else {
             data_value_int = 1;
         }
             
         try {
             Connection connection = getConnection();
 
             // Insert latest test lamp change
             Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO test_lamp VALUES (" + data_value_int + "', now())");
 
             // Return the latest status of the test lamp
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_lamp ORDER BY time DESC LIMIT 1");
             rs.next();
 
             request.setAttribute("lampStatus", convertIntToStatus(rs.getInt(1)));
             request.setAttribute("lampStatusTime", rs.getString(2));
         }
         catch (SQLException e) {
             request.setAttribute("SQLException", e.getMessage());
         }
         catch (URISyntaxException e) {
             request.setAttribute("URISyntaxException", e.getMessage());
         }
 
         request.getRequestDispatcher("/testlamp-post.jsp").forward(request, response);
     }
 
 };
 
