 package servlet;
 
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
 
 // @WebServlet(
 //         name = "HelloServlet",
 //         urlPatterns = {"/hello"}
 //     )
 public class HelloServlet extends HttpServlet {
 
     // Database Connection
     private static Connection getConnection() throws URISyntaxException, SQLException {
         URI dbUri = new URI(System.getenv("DATABASE_URL"));
 
         String username = dbUri.getUserInfo().split(":")[0];
         String password = dbUri.getUserInfo().split(":")[1];
         String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
 
         return DriverManager.getConnection(dbUrl, username, password);
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // response.getWriter().print("Hello from Java!\n");
 
         try {
             Connection connection = getConnection();
 
             Statement stmt = connection.createStatement();
             stmt.executeUpdate("DROP TABLE IF EXISTS ticks");
             stmt.executeUpdate("CREATE TABLE ticks (tick timestamp)");
             stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
             ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
             while (rs.next()) {
                request.setAttribute("test_var", rs.getTimestamp("tick"));
             }
         }
         catch (SQLException e) {
             response.getWriter().print("SQLException: " + e.getMessage());
         }
         catch (URISyntaxException e) {
             response.getWriter().print("URISyntaxException: " + e.getMessage());
         }
 
         request.getRequestDispatcher("/hello.jsp").forward(request, response);
     }
 
 }
 
 /*
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.*;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.*;
 
 public class HelloWorld extends HttpServlet {
 
     // Database Connection
     private static Connection getConnection() throws URISyntaxException, SQLException {
         URI dbUri = new URI(System.getenv("DATABASE_URL"));
 
         String username = dbUri.getUserInfo().split(":")[0];
         String password = dbUri.getUserInfo().split(":")[1];
         String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
 
         return DriverManager.getConnection(dbUrl, username, password);
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // response.getWriter().print("Hello from Java!\n");
 
         try {
             Connection connection = getConnection();
 
             Statement stmt = connection.createStatement();
             stmt.executeUpdate("DROP TABLE IF EXISTS ticks");
             stmt.executeUpdate("CREATE TABLE ticks (tick timestamp)");
             stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
             ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
             while (rs.next()) {
                 // response.getWriter().print("Read from DB: " + rs.getTimestamp("tick"));
             }
         }
         catch (SQLException e) {
             response.getWriter().print("SQLException: " + e.getMessage());
         }
         catch (URISyntaxException e) {
             response.getWriter().print("URISyntaxException: " + e.getMessage());
         }
 
         request.getRequestDispatcher("/index.jsp").forward(request, response);
     }
 
     public static void main(String[] args) throws Exception{
         Server server = new Server(Integer.valueOf(System.getenv("PORT")));
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         server.setHandler(context);
         context.addServlet(new ServletHolder(new HelloWorld()),"/*");
         server.start();
         server.join();
     }
 }
 */
 
