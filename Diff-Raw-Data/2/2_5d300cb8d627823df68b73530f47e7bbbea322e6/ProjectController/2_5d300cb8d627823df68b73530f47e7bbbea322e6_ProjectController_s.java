 /*
  * Copyright (C) 2009-2011 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mcculley
  */
 public class ProjectController extends HttpServlet {
 
     private Sarariman sarariman;
 
     @Override
     public void init() throws ServletException {
         super.init();
         sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
     }
 
     private enum Action {
 
         create, update, delete
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
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
 
         String name = request.getParameter("name");
         Action action = Action.valueOf(request.getParameter("action"));
         try {
             long id;
             Project project;
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
             java.sql.Date pop_start;
             java.sql.Date pop_end;
             switch (action) {
                 case create:
                     pop_start = new java.sql.Date(dateFormat.parse(request.getParameter("pop_start")).getTime());
                     pop_end = new java.sql.Date(dateFormat.parse(request.getParameter("pop_start")).getTime());
                     project = Project.create(sarariman, name, Long.parseLong(request.getParameter("customer")), pop_start, pop_end,
                             null, null, new BigDecimal(0), new BigDecimal(0), 0, new BigDecimal(0), true);
                     id = project.getId();
                     response.sendRedirect(response.encodeRedirectURL(MessageFormat.format("project?id={0}", id)));
                     return;
                 case update:
                     id = Long.parseLong(request.getParameter("id"));
                     project = sarariman.getProjects().get(id);
                     pop_start = new java.sql.Date(dateFormat.parse(request.getParameter("pop_start")).getTime());
                     pop_end = new java.sql.Date(dateFormat.parse(request.getParameter("pop_end")).getTime());
                     project.update(name, Long.parseLong(request.getParameter("customer")), pop_start, pop_end,
                             request.getParameter("contract"), request.getParameter("subcontract"),
                             new BigDecimal(request.getParameter("funded")),
                             new BigDecimal(request.getParameter("previously_billed")),
                             Long.parseLong(request.getParameter("terms")),
                            new BigDecimal(request.getParameter("odc_fee")), Boolean.parseBoolean(request.getParameter("active")));
                     response.sendRedirect(response.encodeRedirectURL(MessageFormat.format("project?id={0}", id)));
                     return;
                 case delete:
                     id = Long.parseLong(request.getParameter("id"));
                     project = sarariman.getProjects().get(id);
                     project.delete();
                     response.sendRedirect(response.encodeRedirectURL("projects"));
                     return;
                 default:
                     response.sendError(500);
                     return;
             }
         } catch (Exception e) {
             throw new IOException(e);
         }
     }
 
     /** 
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "performs updates on projects";
     }
 
 }
