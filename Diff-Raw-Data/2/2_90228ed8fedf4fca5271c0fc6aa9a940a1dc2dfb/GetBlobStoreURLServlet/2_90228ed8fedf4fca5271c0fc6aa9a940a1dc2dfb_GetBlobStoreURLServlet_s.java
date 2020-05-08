 package com.histograph.server.controllers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 /**
  * 
  * @author alandonohoe
  * This is solely a wrapper for GAE's blobstore service: 'createUploadUrl' method
  * Returns a one time URL for posting blobs (ie: images) to the GAE blobstore
  * And sets the call back function as phoneImgBlobsCallback - this is called 
  * once the blobstore has persisted img and will provide the img's blobkey
  * to the callback function for adding to the datastore Image entry.
  */
 public class GetBlobStoreURLServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static Logger log = Logger.getLogger(GetBlobStoreURLServlet.class
 			.getName());
 	
	private static final String CALLBACKFUNCTION = "/phoneImgBlobsCallback";
 
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		String blobstoreURL = "";
 		
 		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 		
 		try{
 			// creates new blobstore url with  callback servlet -  uploadimages - to be called when asynx upload of img is complete
 			blobstoreURL = blobstoreService.createUploadUrl(CALLBACKFUNCTION); 
 		
 		}catch(Exception e){
 			log.warning(e.getMessage());
 			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 
 		log.info("blobstoreURL = " + blobstoreURL);
 
 		resp.getWriter().append(blobstoreURL);
 
 	}
 	
 }
