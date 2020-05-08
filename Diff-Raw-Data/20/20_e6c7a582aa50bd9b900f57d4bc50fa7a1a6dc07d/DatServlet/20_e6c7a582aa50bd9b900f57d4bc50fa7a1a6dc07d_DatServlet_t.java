 package at.tomtasche.datmoment.engine;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.channels.Channels;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.files.AppEngineFile;
 import com.google.appengine.api.files.FileService;
 import com.google.appengine.api.files.FileServiceFactory;
 import com.google.appengine.api.files.FileWriteChannel;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 @SuppressWarnings("serial")
 public class DatServlet extends HttpServlet {
     private static final Gson GSON = new Gson();
 
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 	BlobKey key = new BlobKey(request.getParameter("key"));
 
 	BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
 	blobStoreService.serve(key, response);
     }
 
     public void doPost(HttpServletRequest request, HttpServletResponse response)
 	    throws ServletException, IOException {
 	try {
 	    ServletFileUpload upload = new ServletFileUpload();
 	    response.setContentType("text/plain");
 
 	    FileItemIterator iterator = upload.getItemIterator(request);
 	    while (iterator.hasNext()) {
 		FileItemStream item = iterator.next();
 		InputStream stream = item.openStream();
 
		if (!item.isFormField()) {
 		    FileService fileService = FileServiceFactory.getFileService();
 		    AppEngineFile file = fileService.createNewBlobFile("image/jpg");
 
 		    FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);
 		    OutputStream output = Channels.newOutputStream(writeChannel);
 
 		    int len;
 		    byte[] buffer = new byte[8192];
 		    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
 			output.write(buffer, 0, len);
 		    }
 
 		    output.close();
 		    writeChannel.closeFinally();
 
 		    BlobKey key = fileService.getBlobKey(file);
 		    Queue queue = QueueFactory.getDefaultQueue();
 		    queue.add(TaskOptions.Builder.withUrl("/worker")
 			    .param("key", key.getKeyString()).countdownMillis(600000));
 
 		    JsonObject container = new JsonObject();
 		    container.addProperty("key", key.getKeyString());
 		    GSON.toJson(container, response.getWriter());
 		}
 	    }
 	} catch (Exception ex) {
 	    throw new ServletException(ex);
 	}
     }
 }
