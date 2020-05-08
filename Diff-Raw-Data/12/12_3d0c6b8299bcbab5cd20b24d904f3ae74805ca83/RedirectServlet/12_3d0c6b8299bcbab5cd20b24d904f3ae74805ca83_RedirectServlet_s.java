 /*
  * RedirectServlet.java
  *
  * Copyright (C) 2010 Fingerprints Software
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License along with this library.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fingerprintsoft.servlet;
 
 import java.io.IOException;
import java.net.URLEncoder;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.fingerprintsoft.io.IFileService;
 
 public abstract class RedirectServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     private String redirectBase;
     private IFileService fileService;
 
     public void init(ServletConfig config) throws ServletException {
 
         super.init(config);
         // Get base path (path to get all resources from) as init parameter.
         this.redirectBase = getInitParameter("redirectBase");
         if (redirectBase == null) {
             throw new ServletException(
                     "The Specified redirect path cannot be null.");
         }
 
         String fileServiceClassName = getInitParameter("fileServiceClassName");
         if (fileServiceClassName == null) {
             throw new ServletException(
                     "The Specified fileServiceClassName path cannot be null.");
         }
         try {
             fileService = (IFileService) Class.forName(fileServiceClassName)
                     .newInstance();
         } catch (InstantiationException e) {
             throw new ServletException("Could not instantiate the class: "
                     + fileServiceClassName, e);
         } catch (IllegalAccessException e) {
             throw new ServletException("Could not access the class: "
                     + fileServiceClassName, e);
         } catch (ClassNotFoundException e) {
             throw new ServletException("The Specified class cannot be found."
                     + fileServiceClassName, e);
         }
 
     }
 
     public void doGet(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {

         String filePath = (String) fileService
                 .getFilePath(getSearchParamaters(req));
        String site = URLEncoder.encode(redirectBase + filePath, "UTF-8");
        res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        res.setHeader("Location", site);

     }
 
     protected abstract Map<String, Object> getSearchParamaters(
             HttpServletRequest req) throws ServletException;
 
     public String getRedirectBase() {
         return redirectBase;
     }
 
     public void setRedirectBase(String redirectBase) {
         this.redirectBase = redirectBase;
     }
 
 }
