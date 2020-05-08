 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class LoginServlet extends HttpServlet {
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String username = request.getParameter("username");
         String password = request.getParameter("password");
         if (username != null) {
            request.getSession().setAttribute("user", username); // Put user in session.
            response.sendRedirect(request.getContextPath() + "/"); // Go to some start page.
         } else {
            request.setAttribute("error", "Unknown login, try again"); // Set error msg for ${error}
            request.getRequestDispatcher(request.getContextPath() + "/login.jsp").forward(request, response); // Go back to login page.
         }
     }
 }
