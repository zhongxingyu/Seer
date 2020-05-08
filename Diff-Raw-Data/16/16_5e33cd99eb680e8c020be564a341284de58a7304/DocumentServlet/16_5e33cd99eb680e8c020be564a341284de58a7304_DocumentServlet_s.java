 package com.alcshare.docs.servlets;
 
 import com.alcshare.docs.util.AddOnFiles;
 import com.alcshare.docs.util.Logging;
 import org.apache.commons.io.IOUtils;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 /**
  *
  */
 public class DocumentServlet extends HttpServlet {
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
         File docFile = getDocFile(req.getPathInfo());
         String mime = getMimeType(getExtension(docFile));
         resp.setContentType(mime);
 
         try {
             ServletOutputStream outputStream = resp.getOutputStream();
             IOUtils.copy(new FileInputStream(docFile), outputStream);
             outputStream.flush();
         } catch (IOException e) {
             Logging.println("Error copying file from '"+req.getPathInfo()+"'", e);
         }
     }
 
     private static String getExtension(File docFile) {
         String name = docFile.getName();
         int dotIndex = name.lastIndexOf(".");
         if (dotIndex >= 0) {
            return name.substring(dotIndex);
         } else {
             return "";
         }
     }
 
     private static String getMimeType(String extension) {
         if (extension.equalsIgnoreCase("pdf")) {
             return "application/pdf";
         } else if (extension.equalsIgnoreCase("html")) {
             return "text/html";
         } else if (extension.equalsIgnoreCase("css")) {
             return "text/css";
         } else if (extension.equalsIgnoreCase("doc")) {
             return "application/msword";
         }
         return "text/plain";
     }
 
     private static File getDocFile(String docPath) {
         File docBaseFile = AddOnFiles.getDocDirectory();
         return new File(docBaseFile,docPath);
     }
 
 
 
 }
