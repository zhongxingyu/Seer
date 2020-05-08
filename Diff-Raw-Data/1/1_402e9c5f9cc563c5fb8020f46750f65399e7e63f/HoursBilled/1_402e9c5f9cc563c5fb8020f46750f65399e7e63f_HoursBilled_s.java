 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.statusboard;
 
 import com.stackframe.sarariman.Sarariman;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mcculley
  */
 public class HoursBilled extends HttpServlet {
 
     private Sarariman sarariman;
 
     @Override
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
             Connection connection = sarariman.openConnection();
             try {
                 PreparedStatement s = connection.prepareStatement(
                         "SELECT SUM(duration) AS total, date " +
                         "FROM hours " +
                         "JOIN tasks ON hours.task = tasks.id " +
                         "WHERE date > DATE_SUB(NOW(), INTERVAL 30 DAY) AND billable = TRUE " +
                         "GROUP BY date " +
                         "ORDER BY date");
                 try {
                     ResultSet r = s.executeQuery();
                     try {
                         while (r.next()) {
                             String date = r.getString("date");
                             String total = r.getString("total");
                             out.println(String.format("%s,%s", date, total));
                         }
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException e) {
             throw new ServletException(e);
         } finally {
             out.close();
         }
     }
 
 }
