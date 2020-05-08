 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.homework.hw2;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Ets
  */
 public class HomePage extends HttpServlet {
     //Constant
     public static final String SESSION_ATTRIBUTE = "param";
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         Object url_attribute = request.getParameter(SESSION_ATTRIBUTE);
         request.getSession().setAttribute(SESSION_ATTRIBUTE, url_attribute);
         String session_value = (String)request.getSession().getAttribute(SESSION_ATTRIBUTE);
         
         String sessionId = request.getSession().getId();
         response.getWriter().println("id is " + sessionId);
        response.getWriter().println("attribute is" + url_attribute);       
         
     }
 
 }
