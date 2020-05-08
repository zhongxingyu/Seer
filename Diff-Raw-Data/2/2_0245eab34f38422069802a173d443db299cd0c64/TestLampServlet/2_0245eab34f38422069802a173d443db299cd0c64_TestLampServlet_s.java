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
             // Return the latest status of the test lamp
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_lamps ORDER BY time DESC LIMIT 1");
             rs.next();
             request.setAttribute("lampAddress", rs.getString(1));
             request.setAttribute("lampStatus", rs.getString(2));
             request.setAttribute("lampStatusTime", rs.getString(3));
             connection.close();
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
         String node_address = request.getParameter("node_address");
         String data_value_str = request.getParameter("data_value");
         int data_value_int = 0;
 
         if (node_address == null) {
             request.setAttribute("error", "No node_address specified.");
         }
 
         if (data_value_str == null) {
             request.setAttribute("error", "No data_value specified.");
         }
         else {
             data_value_str = data_value_str.toLowerCase();
 
             // Convert string to corresponding int 0-off 1-on
             if (data_value_str.contains("off")) {
                 data_value_int = 0;
             }
             else {
                 data_value_int = 1;
             }
         }
 
         try {
             Connection connection = getConnection();
 
             // Insert latest test lamp change
             Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO test_lamps VALUES (" + node_address + ", " + data_value_int + ", now())");
 
             // Return the latest status of the test lamp
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_lamps ORDER BY time DESC LIMIT 1");
             rs.next();
             request.setAttribute("lampAddress", rs.getString(1));
             request.setAttribute("lampStatus", rs.getString(2));
             request.setAttribute("lampStatusTime", rs.getString(3));
             connection.close();
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
 
