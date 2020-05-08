 //Delete.java
 //Class to delete song from your playlist
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.sql.*;
 
 public class Delete extends HttpServlet {
 
     public void doGet(HttpServletRequest req, HttpServletResponse res)
         throws IOException, ServletException {
         
         res.setContentType("text/html");
         PrintWriter out = res.getWriter();
         
         /* Get Session */
         HttpSession s = req.getSession(true);
         /* Make sure user is logged in */
         if(s.getAttribute("login") == null || (String) s.getAttribute("login") != "go")
         {
             req.getRequestDispatcher("login.jsp").forward(req, res);
         }
 
 
         try{
             String dbuser = this.getServletContext().getInitParameter("dbuser");
             String dbpassword = this.getServletContext().getInitParameter("dbpassword");
             
             Class.forName("com.mysql.jdbc.Driver");
             Connection conn = DriverManager.getConnection ("jdbc:mysql://localhost/project", dbuser, dbpassword);
 
             Statement stmt = conn.createStatement();
            stmt.execute( "DELETE FROM play_lists WHERE user_id = '" + req.getParameter("user_id") + "' AND song_id = '" + req.getParameter("song_id") + "' AND play_list_id = '" + req.getParameter("play_list_id") + "'" );            
 
             stmt.close();
             conn.close();
 
             req.getRequestDispatcher("delete.jsp").forward(req, res); 
 
         }  catch (Exception e) {
             out.println(e.getMessage());
         }
 
     }
 }
