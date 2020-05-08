 /* $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The Netarchive Suite - Software to harvest and preserve websites
  * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
  *   USA
  */
 package dk.statsbiblioteket.mediaplatform.ingest.channelarchivingrequester.web;
 
 
 import dk.statsbiblioteket.mediaplatform.ingest.model.YouSeeChannelMapping;
 import dk.statsbiblioteket.mediaplatform.ingest.model.service.ServiceException;
 import dk.statsbiblioteket.mediaplatform.ingest.model.service.YouSeeChannelMappingService;
 import dk.statsbiblioteket.mediaplatform.ingest.model.service.YouSeeChannelMappingServiceIF;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.undo.CannotRedoException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class YouSeeChannelMappingCRUDServlet extends HttpServlet {
     public static final String ID = "ID";
     public static final String SB_ID = "sb_id";
     public static final String UC_ID = "uc_id";
     public static final String DISPLAY_NAME = "display_name";
     public static final String FROM_DATE = "from_date";
     public static final String TO_DATE = "to_date";
 
     public static final String SUBMIT_ACTION = "action";
 
     public static final String CREATE = "create";
     public static final String UPDATE = "update";
     public static final String DELETE = "delete";
 
     public static final SimpleDateFormat JAVA_DATE_FORMAT =  new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
     public static YouSeeChannelMappingServiceIF service;
 
     @Override
     public void init() throws ServletException {
         super.init();    //To change body of overridden methods use File | Settings | File Templates.
         service = new YouSeeChannelMappingService();
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String action = req.getParameter(SUBMIT_ACTION);
         String id = req.getParameter(ID);
 
         String sbChannelName = req.getParameter(SB_ID);
         String youSeeName = req.getParameter(UC_ID);
         String displayName = req.getParameter(DISPLAY_NAME);
         Date fromDate;
         Date toDate;
         String fromDateS = req.getParameter(FROM_DATE);
         if (fromDateS != null || "".equals(fromDateS)) {
             fromDateS = "2012-05-01 00:00";
         }
         try {
             fromDate = JAVA_DATE_FORMAT.parse(fromDateS);
             fromDate.setSeconds(0);
         } catch (ParseException e) {
             throw new RuntimeException("Could not parse '" +
                     fromDateS + "'");
         }
         String toDateS = req.getParameter(TO_DATE);
         if (toDateS != null || "".equals(toDateS)) {
             toDateS = "3000-01-01 00:00";
         }
         try {
             toDate = JAVA_DATE_FORMAT.parse(toDateS);
             toDate.setSeconds(0);
         } catch (ParseException e) {
             throw new RuntimeException("Could not parse '" +
                     toDateS + "'");
         }
         YouSeeChannelMapping mapping = new YouSeeChannelMapping();
         if (id != null && !"".equals(id)) {
             mapping.setId(Long.parseLong(id));
         }
         mapping.setDisplayName(displayName);
         mapping.setFromDate(fromDate);
         mapping.setToDate(toDate);
         mapping.setSbChannelId(sbChannelName);
         mapping.setYouSeeChannelId(youSeeName);
         if (CREATE.equals(action)) {
             try {
                 service.create(mapping);
             } catch (ServiceException e) {
                 req.setAttribute("error", e);
             }
         } else if (UPDATE.equals(action)) {
               try {
                 service.update(mapping);
             } catch (ServiceException e) {
                 req.setAttribute("error", e);
             }
         } else if (DELETE.equals(action)) {
               try {
                 service.delete(mapping);
             } catch (ServiceException e) {
                 req.setAttribute("error", e);
             }
         }
        req.setAttribute("page_attr", "you_see_channel_mapping.jsp");
         req.getSession().getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
 
     }
 }
