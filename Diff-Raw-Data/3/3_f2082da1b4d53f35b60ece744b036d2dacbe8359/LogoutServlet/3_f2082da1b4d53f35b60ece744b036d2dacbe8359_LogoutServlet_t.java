 package no.steria.swhrs;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 public class LogoutServlet extends HttpServlet {
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         HttpSession session = req.getSession();
        if (session == null || session.getAttribute("user") == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No session existed or no user was logged in");
        }
         session.removeAttribute("user");
     }
 }
