 package org.cloudlet.book.servlet;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import org.cloudlet.book.server.BookImporter;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @Singleton
 public class ImporterServlet extends HttpServlet {
 
   @Inject
   BookImporter importer;
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setCharacterEncoding("UTF-8");
     PrintWriter w = resp.getWriter();
     try {
       importer.setWriter(w);
       importer.importDuoKan();
     } catch (Throwable t) {
       t.printStackTrace(w);
       t.printStackTrace();
     }
     w.println("Complete import data.");
   }
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     doGet(req, resp);
   }
 
 }
