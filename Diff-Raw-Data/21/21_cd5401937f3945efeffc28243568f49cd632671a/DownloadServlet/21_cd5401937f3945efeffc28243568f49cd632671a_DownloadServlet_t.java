 /**
  * @author Yi Huang (Celia)
  */
 package benchmark.storage.myblob;
 
 import benchmark.storage.ActionStatus;
 import benchmark.storage.PMF;
 import java.io.IOException;
 import java.util.List;
 import javax.jdo.Query;
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import com.google.appengine.api.datastore.Blob;
 
 /**
  */
 public class DownloadServlet extends HttpServlet {
     protected void HandleRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         long t1 = System.currentTimeMillis();
         PersistenceManager pm = PMF.getManager();
         Query query = pm.newQuery(MyBlobInfo.class);
         try {
             String name = InitServlet.getCachedObjName(request.getParameter("id"));
             query.setFilter("name == blobName");
             query.declareParameters("String blobName");
             List<MyBlobInfo> list = (List<MyBlobInfo>) query.execute(name);
             MyBlobInfo blobInfo = list.get(0);
             long t2 = System.currentTimeMillis();
             Blob blob = blobInfo.getBlob();
             response.setStatus(HttpServletResponse.SC_FOUND);
             long t3 = System.currentTimeMillis();
            response.getWriter().format("myblob download %s %s %d %d %d", new Object[]{
                ActionStatus.SUCCESS, name, t1, t2, t3
             });
         } catch(ArrayIndexOutOfBoundsException e) {
             response.getWriter().format("myblob download %s", new Object[]{
                 ActionStatus.FAILED
             });
         } finally {
             query.closeAll();
             pm.close();
         }
     }
     
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         HandleRequest(request, response);
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         HandleRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "GAEBenchmark MyBlob SimDownload";
     }
 }
