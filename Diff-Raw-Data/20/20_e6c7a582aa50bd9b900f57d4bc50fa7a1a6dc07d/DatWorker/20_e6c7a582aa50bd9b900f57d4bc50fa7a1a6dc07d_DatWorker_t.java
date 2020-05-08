 package at.tomtasche.datmoment.engine;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 
 @SuppressWarnings("serial")
 public class DatWorker extends HttpServlet {
 
     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
 	    throws ServletException, IOException {
 	if (request.getParameter("key") == null)
 	    return;
 	BlobKey key = new BlobKey(request.getParameter("key"));
 
 	BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
 	blobStoreService.delete(key);
     }
 }
