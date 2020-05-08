 /*
  * Copyright (C) 2010-2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.io.IOException;
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
 public class ContactController extends HttpServlet {
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         Employee user = (Employee)request.getAttribute("user");
         if (!user.isAdministrator()) {
             response.sendError(401);
             return;
         }
 
         Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         if (request.getParameter("action").equals("create")) {
             try {
                 Connection connection = sarariman.getDataSource().getConnection();
                 try {
                     PreparedStatement ps = connection.prepareStatement("INSERT INTO contacts (name, title, email, phone, fax, mobile, street, city, state, zip) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                     try {
                         ps.setString(1, request.getParameter("name"));
                         ps.setString(2, request.getParameter("title"));
                         ps.setString(3, request.getParameter("email"));
                         ps.setString(4, request.getParameter("phone"));
                         ps.setString(5, request.getParameter("fax"));
                         ps.setString(6, request.getParameter("mobile"));
                         ps.setString(7, request.getParameter("street"));
                         ps.setString(8, request.getParameter("city"));
                         ps.setString(9, request.getParameter("state"));
                         ps.setString(10, request.getParameter("zip"));
                         ps.executeUpdate();
                         ResultSet rs = ps.getGeneratedKeys();
                         try {
                             rs.next();
                             long id = rs.getLong(1);
                             response.sendRedirect(String.format("contact?id=%d", id));
                         } finally {
                             rs.close();
                         }
                     } finally {
                         ps.close();
                     }
                 } finally {
                     connection.close();
                 }
             } catch (SQLException se) {
                 throw new ServletException(se);
             }
         } else {
             try {
                 Connection connection = sarariman.getDataSource().getConnection();
                 try {
                     PreparedStatement ps = connection.prepareStatement("UPDATE contacts SET name=?, title=?, email=?, phone=?, fax=?, mobile=?, street=?, city=?, state=?, zip=? WHERE id=?");
                     long id = Long.parseLong(request.getParameter("id"));
                     try {
                         ps.setString(1, request.getParameter("name"));
                         ps.setString(2, request.getParameter("title"));
                         ps.setString(3, request.getParameter("email"));
                         ps.setString(4, request.getParameter("phone"));
                         ps.setString(5, request.getParameter("fax"));
                         ps.setString(6, request.getParameter("mobile"));
                         ps.setString(7, request.getParameter("street"));
                         ps.setString(8, request.getParameter("city"));
                         ps.setString(9, request.getParameter("state"));
                         ps.setString(10, request.getParameter("zip"));
                         ps.setLong(11, id);
                         ps.executeUpdate();
                         response.sendRedirect(String.format("contact?id=%d", id));
                     } finally {
                         ps.close();
                        connection.close();
                     }
                 } finally {
                     connection.close();
                 }
             } catch (SQLException se) {
                 throw new ServletException(se);
             }
         }
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Creates or updates a contact";
     }
 
 }
