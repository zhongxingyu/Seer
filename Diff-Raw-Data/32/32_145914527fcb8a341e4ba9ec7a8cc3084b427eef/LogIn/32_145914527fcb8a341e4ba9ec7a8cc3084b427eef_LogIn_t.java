 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import DataDAO.*;
 import com.mysql.jdbc.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Michelle
  */
 public class LogIn extends HttpServlet {
 
     private static Connection con;
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         String email = request.getParameter("username");
         System.out.println("username:" + email);
         String pw = request.getParameter("password");
         System.out.println("password:" + pw);
         String disasterType = request.getParameter("Disaster");
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (ClassNotFoundException e) {
             throw new AssertionError(e);
         }
         String connectionStr = "jdbc:mysql://localhost/DisasterAssessment";       
         String user = "root";
         String pw_con = "";
         try {
             con = (Connection) DriverManager.getConnection(connectionStr, user, pw_con);
             User UserInstance = new User();
             
             List<String> errors = new ArrayList<String>();
             request.setAttribute("errors", errors);
 
             UserInstance = User.lookup(email, con);
             if (UserInstance == null) {
                errors.add("The e-mail address you entered is not registered. Please enter a valid e-mail address, or use the Register button to create an account.");
 //                String destination = "/index.jsp";
 //                RequestDispatcher red = getServletContext().getRequestDispatcher(destination);
 //                red.forward(request, response);
             } else if (UserInstance.getIsApproved().equals("N")) {
                 //System.out.println(((String)UserInstance.getIsApproved()).equals("N"));
                errors.add("Your account has not been approved yet by an administrator. Please contact your supervisor for more details.");
 //                String destination = "/index.jsp";
 //                RequestDispatcher red = getServletContext().getRequestDispatcher(destination);
 //                red.forward(request, response);
             } else if (!UserInstance.getPassword().equals(pw)) {
                errors.add("The password you entered was incorrect. Please try again.");
                 //request.setAttribute("errors", errors);
             }
 
                 if (errors.size()>0) {
                     for (int i = 0; i < errors.size(); i++) {
                         System.out.println(errors.get(i));
                     }
                     String destination = "/index.jsp";
                     RequestDispatcher red = getServletContext().getRequestDispatcher(destination);
                     red.forward(request, response);
 
                 }
                else
                {
                     //request.setAttribute("email", email);
                     HttpSession session = request.getSession();
                     session.setAttribute("userName", UserInstance.getfName() + " " + UserInstance.getlName());
                    session.setAttribute("volunteerId", Integer.toString(UserInstance.getUserId()));
                    session.setAttribute("loginStatus", "OK");
                    session.setAttribute("adminStatus", UserInstance.getRole());
                     response.sendRedirect("welcome.jsp");
                 }
 
             
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try {
                 con.close();
             } catch (SQLException ex) {
                 Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
 
     }
 }
