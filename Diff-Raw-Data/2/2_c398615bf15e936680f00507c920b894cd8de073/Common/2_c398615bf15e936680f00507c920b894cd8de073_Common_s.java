 //Common.java
 //Class to find songs in common with another user
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.sql.*;
 
 public class Common extends HttpServlet {
 
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
             String mySongs = "SELECT song_id FROM play_lists NATURAL JOIN songs NATURAL JOIN users WHERE play_lists.user_id = '" + s.getAttribute("user_id") + "'";
             ResultSet rs = stmt.executeQuery("SELECT * FROM play_lists NATURAL JOIN songs NATURAL JOIN users NATURAL JOIN user_play_lists WHERE play_lists.user_id = '" + req.getParameter("user_id") + "' AND song_id IN (" + mySongs + ") ORDER BY song_name");            
 
             String table = "<table class=\"stripe\"><tr><th>Song</th><th>Artist</th><th>Album</th><th>Genre</th><th>Play List</th><th>Votes</th></tr>";
            String full_name = null;
             int x = 0;
 
             if(!rs.next())
                 req.getRequestDispatcher("common.jsp?error=1").forward(req, res);
             do
             {
                 if(x % 2 == 0) { table = table + "<tr>";}
                 else { table = table + "<tr class=\"alt\">"; }
 
                 table = table + "<td>" + rs.getString("song_name") + "</td>"
                 + "<td>" + rs.getString("artist") + "</td>"
                 + "<td>" + rs.getString("album") + "</td>"
                 + "<td>" + rs.getString("genre") + "</td>"
                 + "<td>" + rs.getString("play_list_name") + "</td>"
                 + "<td>" + rs.getString("votes") + " <a href=\"Vote?song_id=" + rs.getString("song_id") + "\">Vote</a></td></tr>";
                 
                 full_name = rs.getString("full_name");
                 x++;
             } while(rs.next());
             
             table = table + "</table>";
             
             rs.close();
             stmt.close();
             conn.close();
             
             req.setAttribute("table", table);     
             req.setAttribute("full_name", full_name );                 
             req.getRequestDispatcher("common.jsp").forward(req, res);
 
         }  catch (Exception e) {
             out.println(e.getMessage());
         }
 
     }
 }
