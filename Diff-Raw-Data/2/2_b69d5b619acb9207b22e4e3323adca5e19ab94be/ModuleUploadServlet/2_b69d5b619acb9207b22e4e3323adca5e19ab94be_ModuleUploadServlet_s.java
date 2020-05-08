 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.servlets;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.jdesktop.wonderland.modules.Module;
 import org.jdesktop.wonderland.modules.service.ModuleManager;
 import org.jdesktop.wonderland.utils.RunUtil;
 
 /**
  *
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class ModuleUploadServlet extends HttpServlet {
 
     /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         throw new ServletException("Upload servlet only handles post");
     } 
 
     /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         /*
          * Create a factory for disk-base file items to handle the request. Also
          * place the file in add/.
          */
         PrintWriter writer = response.getWriter();
         ModuleManager manager = ModuleManager.getModuleManager();
         Logger logger = ModuleManager.getLogger();
         
         /* Check that we have a file upload request */
         boolean isMultipart = ServletFileUpload.isMultipartContent(request);
         if (isMultipart == false) {
             logger.warning("[MODULE] UPLOAD Bad request");
             writer.println("Unable to recognize upload request. Press the ");
             writer.println("Back button on your browser and try again.<br><br>");
             return;
         }
  
         // Create a new file upload handler
         ServletFileUpload upload = new ServletFileUpload();
 
         // Parse the request
         try {
             FileItemIterator iter = upload.getItemIterator(request);
             while (iter.hasNext() == true) {
                 FileItemStream item = iter.next();
                 String name = item.getFieldName();
                 InputStream stream = item.openStream();
                 if (item.isFormField() == false) {
                     /*
                      * The name given should have a .jar extension. Check this here. If
                      * not, return an error. If so, parse out just the module name.
                      */
                     String moduleJar = item.getName();
                     if (moduleJar.endsWith(".jar") == false) {
                         /* Log an error to the log and write an error message back */
                         logger.warning("[MODULE] UPLOAD File is not a jar file " + moduleJar);
                         writer.println("The file " + moduleJar + " needs to be a jar file. Press the ");
                         writer.println("Back button on your browser and try again.<br><br>");
                         return;
                     }
                     String moduleName = moduleJar.substring(0, moduleJar.length() - 4);
 
                     logger.info("[MODULE] UPLOAD Install module " + moduleName + " with file name " + moduleJar);
                     
                     /*
                      * Write the file a temporary file
                      */
                     File tmpFile = null;
                     try {
                        tmpFile = File.createTempFile(moduleName, ".jar");
                         tmpFile.deleteOnExit();
                         RunUtil.writeToFile(stream, tmpFile);
                         logger.info("[MODULE] UPLOAD Wrote added module to " + tmpFile.getAbsolutePath());
                     } catch (java.lang.Exception excp) {
                         /* Log an error to the log and write an error message back */
                         logger.log(Level.WARNING, "[MODULE] UPLOAD Failed to save file", excp);
                         writer.println("Unable to save the file to the module directory. Press the ");
                         writer.println("Back button on your browser and try again.<br><br>");
                         writer.println(excp.toString());
                         return;
                     }
 
                     /* Add the new module */
                     Collection<File> moduleFiles = new LinkedList<File>();
                     moduleFiles.add(tmpFile);
                     Collection<Module> result = manager.addToInstall(moduleFiles);
                     if (result.isEmpty() == true) {
                         /* Log an error to the log and write an error message back */
                         logger.warning("[MODULE] UPLOAD Failed to install module " + moduleName);
                         writer.println("Unable to install module for some reason. Press the ");
                         writer.println("Back button on your browser and try again.<br><br>");
                         return;
                     }
                 }
             }
         } catch (FileUploadException excp) {
             /* Log an error to the log and write an error message back */
             logger.log(Level.WARNING, "[MODULE] UPLOAD Failed", excp);
             writer.println("Unable to install module for some reason. Press the ");
             writer.println("Back button on your browser and try again.<br><br>");
             return;
         }
  
         /* Install all of the modules that are possible */
         manager.installAll();
         
         /* If we have reached here, then post a simple message */
         logger.info("[MODULE] UPLOAD Added module successfully");
         writer.print("Module added successfully. Press the ");
         writer.print("Back button on your browser and refresh the page to see the updates.<br><br>");
     }
     
     /** 
     * Returns a short description of the servlet.
     */
     @Override
     public String getServletInfo() {
         return "Module Upload Servlet";
     }
 }
