 package controller;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
/**
 * Created with IntelliJ IDEA.
 * User: sgomez
 * Date: 2/26/13
 * Time: 7:10 PM
 * To change this template use File | Settings | File Templates.
 */
 @WebServlet("/logout")
 public class LogoutServlet extends HttpServlet {
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // log the user out
         request.logout();
         // make sure the session is not a new session
         HttpSession session = request.getSession(false);
         if(session != null){
             // invalidate the session to log the user out
              session.invalidate();
        }
         // redirect the user back to the login page
        response.sendRedirect(request.getContextPath() + "/login");
     }
 }
