 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  *        or more contributor license agreements.  See the NOTICE file
  *        distributed with this work for additional information
  *        regarding copyright ownership.  The ASF licenses this file
  *        to you under the Apache License, Version 2.0 (the
  *        "License"); you may not use this file except in compliance
  *        with the License.  You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *        Unless required by applicable law or agreed to in writing,
  *        software distributed under the License is distributed on an
  *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *        KIND, either express or implied.  See the License for the
  *        specific language governing permissions and limitations
  *        under the License.
  */
 
 package org.trianacode.TrianaCloud.Broker;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.log4j.Logger;
 import org.trianacode.TrianaCloud.Utils.MD5;
 import org.trianacode.TrianaCloud.Utils.Task;
 import org.trianacode.TrianaCloud.Utils.TaskDAO;
 import org.trianacode.TrianaCloud.Utils.TrianaCloudServlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * @author Kieran David Evans
  */
 public class Broker extends TrianaCloudServlet {
 
     private Logger logger = Logger.getLogger(this.getClass().toString());
 
     private TaskDAO td;
 
     /*
      * Initialises the Broker class.
      * Takes care of initializing Hibernate.
      */
 
     public void init() throws ServletException {
         try {
 
             ///TODO: Hibernate settings, get them from servlet container?
             //factory = (ConnectionFactory) getServletContext().getAttribute("RabbitMQConnectionFactory");
             //if (factory == null) {
             //    logger.fatal("No RabbitMQ factory retrieved from Servlet Context. Cannot go on.");
             //    throw new ServletException("No RabbitMQ factory retrieved from Servlet Context. Cannot go on.");
             //}
             logger.info("Initializing Broker");
 
             td = new TaskDAO();
         } catch (Exception e) {
             ServletException se = new ServletException(e);
             logger.fatal("Something Happened!", se);
             throw se;
         }
     }
 
     /*
      * Inserts a task into the database for deploying to workers.
      * @param t The task to be inserted.
      */
     public void insertTask(Task t) throws IOException {
         ///TODO: Read task metadata to determine which queue to send to (e.g. #.triana)
         String corrId = t.getUUID();
         t.setState(Task.PENDING);
         logger.info("Inserting Task");
         td.create(t);
 
         logger.info("Inserted job " + corrId + " with payload " + t.getName());
     }
 
     /*
      * Provides the web service endpoint to insert tasks.
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         logger.info("Broker received a request.");
         String pathInfo = isolatePath(request);
         String content = "";
         if (!pathInfo.equalsIgnoreCase("")) {
             logger.info("Unknown Endpoint");
             write404Error(response, "Unknown endpoint");
             return;
         }
 
         try {
             byte[] data = null;
             String r_key = "";
             String fname = "";
             String name = "";
             int numTasks = 0;
             StringBuilder s = new StringBuilder();
 
             try {
                 List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                 for (FileItem item : items) {
                     if (item.isFormField()) {
                         // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
                         String fieldname = item.getFieldName();
                         String fieldvalue = item.getString();
                         if (fieldname.equalsIgnoreCase("task")) {
                             s.append(fieldvalue);
                         }
                         if (fieldname.equalsIgnoreCase("routingkey")) {
                             r_key = fieldvalue;
                         }
                         if (fieldname.equalsIgnoreCase("numtasks")) {
                             numTasks = Integer.parseInt(fieldvalue);
                         }
                         if (fieldname.equalsIgnoreCase("name")) {
                             name = fieldvalue;
                         }
                     } else {
                         // Process form file field (input type="file").
                         String fieldname = item.getFieldName();
                         String filename = FilenameUtils.getName(item.getName());
                         // ... (do your job here)
 
                         fname = filename;
 
                         InputStream is = item.getInputStream();
 
                         long length = item.getSize();
 
                         if (length > Integer.MAX_VALUE) {
                             // File is too large
                             throw new Exception("File too large");
                         }
 
                         // Create the byte array to hold the data
                         byte[] bytes = new byte[(int) length];
 
                         // Read in the bytes
                         int offset = 0;
                         int numRead = 0;
                         while (offset < bytes.length
                                 && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                             offset += numRead;
                         }
 
                         // Ensure all the bytes have been read in
                         if (offset < bytes.length) {
                             throw new IOException("Could not completely read file " + length);
                         }
                         data = bytes;
                     }
                 }
             } catch (FileUploadException e) {
                 logger.error("Cannot parse multipart request.");
                 throw new ServletException("Cannot parse multipart request.", e);
             }
 
             List<String> UUIDList = new ArrayList<String>();
 
            log.debug(content);
             for (int i = 0; i < numTasks; i++) {
                 Task t = new Task();
                 t.setData(data);
                 t.setDataMD5(MD5.getMD5Hash(data));
                 t.setDataType("binary");
                 t.setName(name);
                 t.setFileName(fname);
                 t.setOrigin("Broker");
                 t.setDispatchTime(System.currentTimeMillis());
                 t.setRoutingKey(r_key);
                 t.setUUID(UUID.randomUUID().toString());
                 insertTask(t);
                 UUIDList.add(t.getUUID());
             }
 
             StringBuilder sb = new StringBuilder();
             for (String id : UUIDList) {
                 sb.append(id + "\n");
             }
             //String ret = "Ok; ///TODO:do some stuff here
             writeResponse(response, 200, "Success", sb.toString());
 
             List<Task> t = td.list();
 
             for (Task ta : t) {
                 logger.info(ta.getDispatchTime() + "  " + ta.getState());
             }
 
 
         } catch (Exception e) {
             e.printStackTrace();
            log.error(e);
             StringBuffer stack = new StringBuffer("Error: " + e.getMessage() + "<br/>");
             StackTraceElement[] trace = e.getStackTrace();
             for (StackTraceElement element : trace) {
                 stack.append(element.toString()).append("<br/>");
             }
             writeError(response, 500, stack.toString());
         } catch (Throwable t) {
             writeThrowable(response, t);
         }
     }
 }
