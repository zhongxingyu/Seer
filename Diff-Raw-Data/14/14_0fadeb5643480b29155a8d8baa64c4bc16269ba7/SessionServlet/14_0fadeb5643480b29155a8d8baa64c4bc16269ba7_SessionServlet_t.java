 package servlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 @WebServlet(
         name = "SessionServlet",
         urlPatterns = {"/go"}
 )
 public class SessionServlet extends HttpServlet {
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         Long last = (Long) req.getSession().getAttribute("last");
        Integer count = (Integer) req.getSession().getAttribute("count");
        count = count == null ? 1 : count + 1;
         Long current = System.currentTimeMillis();
         req.getSession().setAttribute("last", current);
        req.getSession().setAttribute("count", count);
         resp.setContentType("text/plain");
         PrintWriter writer = resp.getWriter();
         writer.println("Last:" + last);
         writer.println("Current:" + current);
        writer.println("Count:" + count);
        writer.close();
     }
 
 }
