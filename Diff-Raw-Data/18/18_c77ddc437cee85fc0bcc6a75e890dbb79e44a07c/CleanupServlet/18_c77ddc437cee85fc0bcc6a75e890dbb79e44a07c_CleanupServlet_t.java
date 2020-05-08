 /*
  * Copyright (C) 2013 Helsingfors Segelklubb ry
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fi.hoski.web.forms;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Link;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Transaction;
 import fi.hoski.datastore.repository.Attachment;
 import fi.hoski.datastore.repository.Attachment.Type;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Timo Vesalainen
  */
 public class CleanupServlet extends HttpServlet {
 
   /**
    * Processes requests for both HTTP
    * <code>GET</code> and
    * <code>POST</code> methods.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
     response.setContentType("text/html;charset=UTF-8");
     PrintWriter out = response.getWriter();
     try {
       out.println("<html>");
       out.println("<head>");
       out.println("<title>Servlet CleanupServlet</title>");
       out.println("</head>");
       out.println("<body>");
       out.println("<h1>Servlet CleanupServlet at " + request.getContextPath() + "</h1>");
       log("Starting cleanup");
       DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
       Calendar cal = Calendar.getInstance();
       long thisYear = cal.get(Calendar.YEAR);
       Set<Key> preservedKeys = new HashSet<>();
       Transaction tr = datastore.beginTransaction();
       try {
         Query yearQuery = new Query("Year");
         PreparedQuery p1 = datastore.prepare(yearQuery);
         for (Entity year : p1.asIterable()) {
           Key yearKey = year.getKey();
           if (yearKey.getId() < thisYear) {
             Query attachmentQuery = new Query("Attachment");
             attachmentQuery.setAncestor(yearKey);
             PreparedQuery p2 = datastore.prepare(attachmentQuery);
             for (Entity attachment : p2.asIterable(FetchOptions.Builder.withChunkSize(500))) {
               Long typeOrdinal = (Long) attachment.getProperty(Attachment.TYPE);
               if (typeOrdinal == null) {
                 log(attachment + " has null type!!!");
               } else {
                 Type type = Type.values()[typeOrdinal.intValue()];
                 switch (type) {
                   case PICS:
                   case RESULT:
                     Key key = attachment.getKey();
                     while (key != null) {
                       preservedKeys.add(key);
                       key = key.getParent();
                     }
                     break;
                   default:
                     Link link = (Link) attachment.getProperty(Attachment.URL);
                     String url = link.getValue();
                     int idx = url.indexOf("blob-key=");
                    if (idx != -1) {
                      String bks = url.substring(idx + 9);
                      BlobKey bk = new BlobKey(bks);
                      blobstoreService.delete(bk);
                      log("removing " + bk);
                      out.println("<p>removing " + bk.toString().replace("<", "&lt;").replace(">", "&gt;"));
                    }
                     break;
                 }
               }
             }
            for (Key k : preservedKeys) {
              out.println("<p>preserving " + k);
             }
             Query query = new Query();
             query.setAncestor(yearKey);
             query.setKeysOnly();
             PreparedQuery p3 = datastore.prepare(query);
             for (Entity e : p3.asIterable(FetchOptions.Builder.withChunkSize(500))) {
               if (!preservedKeys.contains(e.getKey())) {
                 log("removing " + e.getKey());
                 out.println("<p>removing " + e.getKey());
                 datastore.delete(e.getKey());
               }
             }
           }
         }
         tr.commit();
         log("Cleanup ready");
       } finally {
         if (tr.isActive()) {
           tr.rollback();
           log("rollbacked");
         }
       }
       out.println("</body>");
       out.println("</html>");
     } finally {
       out.close();
     }
   }
 
   // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
   /**
    * Handles the HTTP
    * <code>GET</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
     processRequest(request, response);
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
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
     processRequest(request, response);
   }
 
   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
     return "Short description";
   }// </editor-fold>
 }
