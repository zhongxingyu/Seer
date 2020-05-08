 package com.pixelus.servlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 
 /*
 * @author David Mouser
 */
 public class RestfulServlet
     extends HttpServlet {
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
 
     PrintWriter out = resp.getWriter();
     out.println("POST method called");
     out.println("Parameters: " + parameters(req));
     out.println("Headers: " + headers(req));
   }
 
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
 
     PrintWriter out = resp.getWriter();
     out.println("GET method called");
     out.println("Parameters: " + parameters(req));
     out.println("Headers: " + headers(req));
   }
 
   @Override
   protected void doPut(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
 
     PrintWriter out = resp.getWriter();
    out.println("PUT method called");
     out.println("Parameters: " + parameters(req));
     out.println("Headers: " + headers(req));
   }
 
   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
 
     PrintWriter out = resp.getWriter();
     out.println("DELETE method called");
   }
 
   private String parameters(HttpServletRequest req) {
 
     StringBuilder stringBuilder = new StringBuilder();
     for (Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
       String paramName = (String) e.nextElement();
       stringBuilder.append("|" + paramName + "->" + req.getParameter(paramName));
     }
 
     return stringBuilder.toString();
   }
 
   private String headers(HttpServletRequest req) {
 
     StringBuilder stringBuilder = new StringBuilder();
     for (Enumeration e = req.getHeaderNames(); e.hasMoreElements(); ) {
       String headerName = (String) e.nextElement();
       stringBuilder.append("|" + headerName + "->" + req.getHeader(headerName));
     }
 
     return stringBuilder.toString();
   }
 }
