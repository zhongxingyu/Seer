 package servlet;
 
 import db.RegisterDb;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 
 /*import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.DriverManager;
 import java.util.Properties;*/
 
 import java.io.IOException;
 
 public class RegisterServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -6820932460475477607L;
 
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
                  throws ServletException, IOException {
     String username = request.getParameter("username");
     String password = request.getParameter("password");
     String cpassword = request.getParameter("cpassword");
     if(!password.equals(cpassword)) {
       return; //passwords do not match. fix this
     }
     String email = request.getParameter("email");
     String crowd = request.getParameter("custtype");
    RegisterDb.addUser(email, username, password, crowd.equals(crowd));
     response.sendRedirect("/index.jsp");      
   }
   
 }
