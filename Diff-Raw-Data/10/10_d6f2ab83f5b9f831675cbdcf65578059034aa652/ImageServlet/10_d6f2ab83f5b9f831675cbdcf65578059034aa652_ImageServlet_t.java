 package com.cs1530.group4.addendum.server;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 
 @SuppressWarnings("serial")
 public class ImageServlet extends HttpServlet
 {
 	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
 	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		if(req.getParameter("key") != null)
 		{
 			//the mime type is set automatically if known, otherwise it's set as application/octet-stream
 			BlobKey blobKey = new BlobKey(req.getParameter("key"));
			BlobInfoFactory blobInfoFactory = new BlobInfoFactory(DatastoreServiceFactory.getDatastoreService());
			BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
			resp.setContentLength(new Long(blobInfo.getSize()).intValue());
			resp.setHeader("content-type", blobInfo.getContentType());
			resp.setHeader("content-disposition", "attachment;filename=" + blobInfo.getFilename());
 	        blobstoreService.serve(blobKey, resp);
	        
 	        return;
 		}
 		
 		String username = req.getParameter("username");
 		Entity user = null;
 		
 		if(username == null)
 			user = getUserEntity("default");
 		
 		user = getUserEntity(username);
 
 		if(user == null)
 			user = getUserEntity("default");
 		
 		try
 		{
 			Blob imageBlob = null;
 			if(user.hasProperty("profileImage"))
 				imageBlob = (Blob) user.getProperty("profileImage");
 			else
 			{
 				user = getUserEntity("default");
 				imageBlob = (Blob) user.getProperty("profileImage");
 			}
 			
 			OutputStream out = resp.getOutputStream();
 			out.write(imageBlob.getBytes());
 			out.close();
 		}
 		catch(ClassCastException ex)
 		{
 			BlobKey blobKey = new BlobKey(user.getProperty("profileImage").toString());
 	        blobstoreService.serve(blobKey, resp);
 		}
 	}
 
 	private Entity getUserEntity(String username)
 	{
 		Entity user = null;
 		if(memcache.contains("user_" + username))
 			user = ((Entity) memcache.get("user_" + username));
 		else
 		{
 			try
 			{
 				user = datastore.get(KeyFactory.createKey("User", username));
 			}
 			catch(EntityNotFoundException ex)
 			{
 				ex.printStackTrace();
 			}
 		}
 		return user;
 	}
 }
