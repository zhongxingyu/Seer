 package com.comoyo.jelastic;
 
 import java.io.IOException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 @SuppressWarnings("serial")
 public class Ranking extends HttpServlet {
     @Override
     public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         resp.setContentType("text/html");
        resp.getWriter()println("<h1>Full ranking </h1>");
         resp.getWriter().print(PersistentStorage.getRankingList());
     }
 }
