 /*
  * Copyright (C) 2009-2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.stackframe.sarariman.projects.Project;
 import static com.stackframe.sql.SQLUtilities.convert;
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
         // FIXME: Break this into checking cost manager or manager.
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
                     pop_start = convert(dateFormat.parse(request.getParameter("pop_start")));
                    pop_end = convert(dateFormat.parse(request.getParameter("pop_end")));
                     project = sarariman.getProjects().create(name, Long.parseLong(request.getParameter("customer")), pop_start, pop_end,
                             null, null, new BigDecimal(0), new BigDecimal(0), 0, new BigDecimal(0), true, null);
                     id = project.getId();
                     response.sendRedirect(response.encodeRedirectURL(MessageFormat.format("project?id={0}", id)));
                     return;
                 case update:
                     id = Long.parseLong(request.getParameter("id"));
                     project = sarariman.getProjects().getMap().get(id);
                     pop_start = convert(dateFormat.parse(request.getParameter("pop_start")));
                     pop_end = convert(dateFormat.parse(request.getParameter("pop_end")));
                     project.setName(name);
                     project.setClient(sarariman.getClients().get(Integer.parseInt(request.getParameter("customer"))));
                     project.setPoP(new PeriodOfPerformance(pop_start, pop_end));
                     project.setContract(request.getParameter("contract"));
                     project.setSubcontract(request.getParameter("subcontract"));
                     project.setFunded(new BigDecimal(request.getParameter("funded")));
                     project.setPreviouslyBilled(new BigDecimal(request.getParameter("previously_billed")));
                     project.setODCFee(new BigDecimal(request.getParameter("odc_fee")));
                     project.setTerms(Integer.parseInt(request.getParameter("terms")));
                     project.setActive("on".equals(request.getParameter("active")));
                     // FIMXE: Add setInvoiceText() and add it to form.
                     response.sendRedirect(response.encodeRedirectURL(MessageFormat.format("project?id={0}", id)));
                     return;
                 case delete:
                     id = Long.parseLong(request.getParameter("id"));
                     project = sarariman.getProjects().getMap().get(id);
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
