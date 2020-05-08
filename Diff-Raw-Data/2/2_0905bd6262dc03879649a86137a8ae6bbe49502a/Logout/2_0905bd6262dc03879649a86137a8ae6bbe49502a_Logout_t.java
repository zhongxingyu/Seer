 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.stackframe.sarariman.logincookies.LoginCookie;
 import com.stackframe.sarariman.logincookies.LoginCookies;
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author mcculley
  */
 public class Logout extends HttpServlet {
 
     private LoginCookies loginCookies;
 
     @Override
     public void init() throws ServletException {
         super.init();
         Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         loginCookies = sarariman.getLoginCookies();
     }
 
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession();
         session.setAttribute("user", null);
         LoginCookie loginCookie = loginCookies.findLoginCookie(request);
        if (loginCookie != null) {
             loginCookie.delete();
         }
 
         response.addCookie(loginCookies.makeDeleteCookie());
         response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/login"));
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Handler for logging out of a session";
     }
 
 }
