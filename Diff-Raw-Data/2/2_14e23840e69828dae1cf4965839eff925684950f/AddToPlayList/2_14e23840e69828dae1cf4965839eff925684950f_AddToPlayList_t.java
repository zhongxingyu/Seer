 //AddToPlayList.java
 //Class to add song to play list
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.sql.*;
 
 public class AddToPlayList extends HttpServlet {
 
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
         
         String select_box = "";
         // fetch user's playlists
         try{
             String dbuser = this.getServletContext().getInitParameter("dbuser");
             String dbpassword = this.getServletContext().getInitParameter("dbpassword");
             
             Class.forName("com.mysql.jdbc.Driver");
             Connection conn = DriverManager.getConnection ("jdbc:mysql://localhost/project", dbuser, dbpassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM user_play_lists WHERE user_id = '" + req.getParameter("user_id") + "'ORDER BY play_list_name");
 
             while(rs.next() )
             {
                 select_box += "<option value=" + rs.getString("play_list_id") + ">" + rs.getString("play_list_name") + "</option>";
             }
             
             rs.close();
             stmt.close();
             conn.close();
 
         }  catch (Exception e) {
             out.println(e.getMessage());
         }
 
 
         req.setAttribute("select_box", select_box);     
         req.setAttribute("song_id", req.getParameter("song_id") );  
         req.setAttribute("user_id", req.getParameter("user_id") );                
         req.getRequestDispatcher("add_to_play_list_select.jsp").forward(req, res);
         
         }
 
     public void doPost(HttpServletRequest req, HttpServletResponse res)
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
 
         String dbuser = this.getServletContext().getInitParameter("dbuser");
         String dbpassword = this.getServletContext().getInitParameter("dbpassword");
         String user_play_list_id = "";
         try{
             Class.forName("com.mysql.jdbc.Driver");
             //they are adding a new playlist
             if(req.getParameter("create") != "")
             {
                 Connection conn = DriverManager.getConnection ("jdbc:mysql://localhost/project", dbuser, dbpassword);
 
                 Statement stmt = conn.createStatement();
                stmt.executeUpdate( "INSERT INTO user_play_lists VALUES(null, '" + req.getParameter("user_id") + "', '" + req.getParameter("create") + "')"  );
 
                 //silly Java trick to get id of last inserted record, hooray for Python+App Engine!
                 ResultSet rsid = stmt.getGeneratedKeys(); 
                 if (rsid != null && rsid.next()) { 
                   user_play_list_id = Integer.toString(rsid.getInt(1)); 
                 }
                 
                 rsid.close();
                 stmt.close();
                 conn.close();            
             }
             else
             {
                 user_play_list_id = req.getParameter("existing");
             }
 
             Connection conn2 = DriverManager.getConnection ("jdbc:mysql://localhost/project", dbuser, dbpassword);
 
             Statement stmt2 = conn2.createStatement();
             stmt2.execute( "INSERT INTO play_lists VALUES('" + req.getParameter("user_id") + "', '" + req.getParameter("song_id") + "', '" + user_play_list_id + "')" );            
             
             req.getRequestDispatcher("add_to_play_list.jsp").forward(req, res); 
             
             stmt2.close();
             conn2.close();
 
         }  catch (Exception e) {
             //req.getRequestDispatcher("add_error.jsp").forward(req, res); 
             out.println(e.getMessage());
         }
 
     }
 }
