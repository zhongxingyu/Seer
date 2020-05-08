 package com.bna;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 
 public class test extends HttpServlet {
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
         resp.setContentType("text/plain");
        resp.getWriter().println("Hello, world");
     }
 }
