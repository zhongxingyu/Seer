 //Signup.java
 //Class to handle initial signup
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.sql.*;
 
 public class Signup extends HttpServlet {
 
     public void doGet(HttpServletRequest req, HttpServletResponse res)
         throws IOException, ServletException {
             req.getRequestDispatcher("signup.jsp").forward(req, res); 
         }
         
     public void doPost(HttpServletRequest req, HttpServletResponse res)
         throws IOException, ServletException {
         
         res.setContentType("text/html");
         PrintWriter out = res.getWriter();
         
         /* Get Session */
         HttpSession s = req.getSession(true);
 
 
         try{
             String dbuser = this.getServletContext().getInitParameter("dbuser");
             String dbpassword = this.getServletContext().getInitParameter("dbpassword");
             
             Class.forName("com.mysql.jdbc.Driver");
             Connection conn = DriverManager.getConnection ("jdbc:mysql://localhost/project", dbuser, dbpassword);
 
            Statement stmt = conn.createStatement();
            //stmt.execute( "INSERT INTO users VALUES(null, '" + req.getParameter("user") + "', '" + req.getParameter("password") + "', '" + req.getParameter("full_name") + "',0)" );            
            stmt.execute( "INSERT INTO users VALUES(null, '" + req.getParameter("user") + "', MD5('" + req.getParameter("password") + "'), '" + req.getParameter("full_name") + "',0)" );            
             
            stmt.close();
            conn.close();
             
            req.getRequestDispatcher("signup_success.jsp").forward(req, res); 
                       
         }  catch (Exception e) {
             out.println(e.getMessage());
         }
 
     }
 }
