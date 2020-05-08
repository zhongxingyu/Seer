 import java.io.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 
 public class InsertExample extends HttpServlet {
 
   String driver = "org.apache.derby.jdbc.EmbeddedDriver";
   String dbName= "dbDemo";
  String connectionURL = "jdbc:derby:Databases/" + dbName + ";create=false";
 
   public void doGet(HttpServletRequest request, HttpServletResponse response) 
     throws IOException, ServletException {
 
     PrintWriter out = response.getWriter();    
     
     out.println("<html>");
     out.println("<head>");
     out.println("<body>");
     out.println("<form method=post action=\"/insert\">");
     out.println("What's your name? <input type=test name=guestname size=20><br>");
     out.println("<input type=submit value=Submit>");
     out.println("</head>");
     out.println("</body>");
     out.println("</html>");
   }
 
   public void doPost(HttpServletRequest request, HttpServletResponse response) 
     throws IOException, ServletException {
     
     PrintWriter out = response.getWriter();    
    
     String guestname = request.getParameter("guestname");
    
     out.println("<html>");
     out.println("<body>");
     out.println("Hello " + guestname + "! You have been inserted into the database!");
     out.println("</body>");
     out.println("</html>");
 
     try {
       Class.forName(driver);
     } catch(java.lang.ClassNotFoundException e) {
       System.out.println("failed to load Derby embedded driver " + e.toString());
     }
 
     try {
       Connection conn = DriverManager.getConnection(connectionURL);
       PreparedStatement psInsert = conn.prepareStatement("insert into Person(name) values '" + guestname + "'");
       psInsert.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
     }
   }
 }
 
 
 
