 /**
  * Copyright (c) 2004 Grad-Soft Ltd, Kiev, Ukraine
  * http://www.gradsoft.ua
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package org.gridsphere.servlets;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gridsphere.layout.PortletLayoutEngine;
 import org.gridsphere.portlet.impl.SportletProperties;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.services.core.registry.PortletManagerService;
 import org.gridsphere.services.core.persistence.PersistenceManagerService;
 import org.gridsphere.services.core.persistence.PersistenceManagerRdbms;
 import org.gridsphere.services.core.security.role.PortletRole;
 import org.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.hibernate.StaleObjectStateException;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.File;
 import java.util.StringTokenizer;
 import java.util.Enumeration;
 
 /**
  * GridSphereFilter is used for first time portal initialization including portlets
  */
 public class GridSphereFilter implements Filter {
 
     private static Boolean firstDoGet = Boolean.TRUE;
 
     private Log log = LogFactory.getLog(GridSphereFilter.class);
     private RoleManagerService roleService = null;
 
     private ServletContext context = null;
 
     public void init(FilterConfig filterConfig) {
         context = filterConfig.getServletContext();
     }
 
     public void destroy() {
     }
 
     public void doFilter(ServletRequest request, ServletResponse response,
                          FilterChain chain)
             throws IOException, ServletException {
 
         log.info("START");
         if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
             HttpServletRequest req = (HttpServletRequest)request;
             HttpServletResponse res = (HttpServletResponse)response;
 
             //PersistenceManagerService pms = null;
 
             // If first time being called, instantiate all portlets
 
             if (firstDoGet.equals(Boolean.TRUE)) {
 
 
                 // check if database file exists
                 String release = SportletProperties.getInstance().getProperty("gridsphere.release");
                 int idx = release.lastIndexOf(" ");
                 String gsversion = release.substring(idx+1);
 
                 //System.err.println("gsversion=" + gsversion);
 
                 String dbpath = context.getRealPath("/WEB-INF/CustomPortal/database/GS_" + gsversion);
 
                 File dbfile = new File(dbpath);
 
                 if (!dbfile.exists()) {
                     request.setAttribute("setup", "true");
                     RequestDispatcher rd = request.getRequestDispatcher("/setup");
                     rd.forward(request, response);
                     return;
                 }
 
                 PersistenceManagerService pms = (PersistenceManagerService)PortletServiceFactory.createPortletService(PersistenceManagerService.class, true);
                 PersistenceManagerRdbms pm = null;
                 boolean noAdmin = true;
                 try {
                     log.info("Starting a database transaction");
                     pm = pms.createGridSphereRdbms();
                     pm.beginTransaction();
 
                     roleService = (RoleManagerService) PortletServiceFactory.createPortletService(RoleManagerService.class, true);
                     noAdmin = roleService.getUsersInRole(PortletRole.ADMIN).isEmpty();
 
                     pm.endTransaction();
                 } catch (StaleObjectStateException staleEx) {
                     log.error("This interceptor does not implement optimistic concurrency control!");
                     log.error("Your application will not work until you add compensation actions!");
                 } catch (Throwable ex) {
                     ex.printStackTrace();
                     pm.endTransaction();
                     try {
                         pm.rollbackTransaction();
                     } catch (Throwable rbEx) {
                         log.error("Could not rollback transaction after exception!", rbEx);
                     }
                 }
 
                 if (noAdmin) {
                     request.setAttribute("setup", "true");
                     RequestDispatcher rd = request.getRequestDispatcher("/setup");
                     rd.forward(request, response);
                     return;
                 }
 
                 System.err.println("Initializing portlets!!!");
                 log.info("Initializing portlets");
                 try {
                     // initialize all portlets
                     PortletManagerService portletManager = (PortletManagerService)PortletServiceFactory.createPortletService(PortletManagerService.class, true);
                     portletManager.initAllPortletWebApplications(req, res);
                     firstDoGet = Boolean.FALSE;
                 } catch (Exception e) {
                     log.error("GridSphere initialization failed!", e);
                     RequestDispatcher rd = req.getRequestDispatcher("/jsp/errors/init_error.jsp");
                     req.setAttribute("error", e);
                     rd.forward(req, res);
                     return;
                 }
 
             }
 
             String pathInfo = req.getPathInfo();
             StringBuffer requestURL = req.getRequestURL();
             String requestURI = req.getRequestURI();
             String query = req.getQueryString();
             log.info("\ncontext path = " + req.getContextPath() + " servlet path=" + req.getServletPath());
             log.info("\n pathInfo= " + pathInfo + " query= " + query);
             log.info(" requestURL= " + requestURL + " requestURI= " + requestURI + "\n");
             
             String extraInfo = "";
 
             // use the servlet path to determine where to forward
             // expect servlet path = /servletpath/XXXX
 
             String path = req.getServletPath();
             int start = path.indexOf("/", 1);
 
             if ((start > 0) && (path.length()-1) > start) {
 
                 String parsePath = path.substring(start+1);
                 //System.err.println(parsePath);
                 extraInfo = "?";
 
                 StringTokenizer st = new StringTokenizer(parsePath, "/");
 
                 if (st.hasMoreTokens()) {
                     String layoutId = (String)st.nextElement();
                     extraInfo += SportletProperties.LAYOUT_PAGE_PARAM + "=" + layoutId;
                 }
                 if (st.hasMoreTokens()) {
                     String cid = (String)st.nextElement();
                     extraInfo += "&" + SportletProperties.COMPONENT_ID+ "=" + cid;
                 }
                 if (st.hasMoreTokens()) {
                     String action = (String)st.nextElement();
                     extraInfo += "&" + SportletProperties.DEFAULT_PORTLET_ACTION + "=" + action;
                 }
                 if (query != null) {
                     extraInfo += "&" + query;
                 }
                 //String ctxPath = "/" + configService.getProperty("gridsphere.context");
             }
 
 
             //chain.doFilter(request, response);
 
             String ctxPath = "/gs";
 
             log.info("forwarded URL: " + ctxPath + extraInfo);
 
             context.getRequestDispatcher(ctxPath + extraInfo).forward(req, res);
 
             log.info("END");
 
         }
 
     }
 
 
 
 }
