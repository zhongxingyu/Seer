 import java.io.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 
 
 public class SelectExample extends HttpServlet {
 
   String driver = "org.apache.derby.jdbc.EmbeddedDriver";
   String dbName= "dbDemo";
  String connectionURL = "jdbc:derby:Databases/" + dbName + ";create=false";
 
   public void doGet(HttpServletRequest request, HttpServletResponse response) 
     throws IOException, ServletException {
 
     try {
       Class.forName(driver);
     } catch(java.lang.ClassNotFoundException e) {
       System.out.println("failed to load Derby embedded driver " + e.toString());
     }
 
     ResultSet guests;
     PrintWriter out = response.getWriter();    
     
     try {
       Connection conn = DriverManager.getConnection(connectionURL);
       Statement select = conn.createStatement();
       guests = select.executeQuery("select name from Person");
       out.println("<html>");
       out.println("<head>");
       out.println("<body>");
       while (guests.next()){
         out.println(guests.getString(1));
       }
       out.println("</head>");
       out.println("</body>");
       out.println("</html>");
     } catch (Throwable e) {
      e.printStackTrace();
     }
     
     
   }
 
 }
 
 
 
