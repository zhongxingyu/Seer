 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.statusboard;
 
 import com.stackframe.sarariman.Employee;
 import com.stackframe.sarariman.Sarariman;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mcculley
  */
 public class SystemStatus extends HttpServlet {
 
     private Sarariman sarariman;
 
     public void init() throws ServletException {
         super.init();
         sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
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
         response.setContentType("text/csv;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             out.println(String.format("Hits,%d", sarariman.getAccessLog().getHitCount()));
             out.println(String.format("Average Time,%d", (int)sarariman.getAccessLog().getAverageTime()));
            out.println(String.format("Errors,%d", sarariman.getErrors().getAll().size()));
         } finally {
             out.close();
         }
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Provide status for the user in CSV suitable for Status Board";
     }
 
 }
