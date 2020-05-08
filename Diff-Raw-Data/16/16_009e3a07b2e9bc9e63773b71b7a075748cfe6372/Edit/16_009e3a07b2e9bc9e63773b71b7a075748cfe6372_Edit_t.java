 //Edit.java
 //Class to display songs in your playlist you may want to delete
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.sql.*;
 
 public class Edit extends HttpServlet {
 
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
             ResultSet rs = stmt.executeQuery("SELECT * FROM play_lists NATURAL JOIN songs NATURAL JOIN users NATURAL JOIN user_play_lists WHERE play_lists.user_id = '" + s.getAttribute("user_id") + "' ORDER BY song_name");            
 
             String table = "<table class=\"display\" id=\"zebra\"><thead><tr><th>Song</th><th>Artist</th><th>Album</th><th>Genre</th><th>Play List</th><th>Delete</th></tr></thead>";
             while(rs.next() )
             {
                 table = table + "<tr>";
                 
                 table = table + "<td width=\"25%\">" + rs.getString("song_name") + "</td>"
                 + "<td width=\"25%\">" + rs.getString("artist") + "</td>"
                 + "<td width=\"25%\">" + rs.getString("album") + "</td>"
                 + "<td width=\"15%\">" + rs.getString("genre") + "</td>"
                 + "<td width=\"15%\">" + rs.getString("play_list_name") + "</td>"
                + "<td width=\"10%\"><a href=\"Delete?song_id=" + rs.getString("play_lists.song_id") + "&play_list_id=" + rs.getString("play_lists.play_list_id") + "&user_id=" + rs.getString("play_lists.user_id") + "\">Delete</a></td></tr>";
                 
             }
             table = table + "</table>";
 
             rs.close();
             stmt.close();
             conn.close();
                         
             req.setAttribute("table", table);                
             req.getRequestDispatcher("edit.jsp").forward(req, res);
 
         }  catch (Exception e) {
             out.println(e.getMessage());
         }
 
     }
 }
