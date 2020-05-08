 package ru.korpse.screenshots.core.controllers;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import ru.korpse.screenshots.core.dao.ShotDao;
 import ru.korpse.screenshots.core.services.ClientService;
 import ru.korpse.screenshots.core.services.SecureKeyService;
 import ru.korpse.screenshots.entities.Shot;
 import ru.korpse.screenshots.exceptions.MaxHitCountExceededException;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.blobstore.UploadOptions;
 
 @Controller
 @RequestMapping
 public class FileUploadController {
 
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 	
 	@Autowired
 	private ShotDao dao;
 	
 	@Autowired
 	private ClientService clientService;
 	
 	@Autowired
 	private SecureKeyService secureKeyService;
 	
 	private UploadOptions options = UploadOptions.Builder.withMaxUploadSizeBytes(4 * 1024 * 1024);
 
 	@RequestMapping(value = "/upload", method = RequestMethod.POST)
 	@ResponseBody
     public Map<String, Object> doPost(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException {
 
 		Map<String, Object> result = new HashMap<String, Object>();
 
         @SuppressWarnings("deprecation")
 		Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
         BlobKey blobKey = blobs.get("fileUpload");
         String secureKey = blobstoreService.getFileInfos(req).get("fileUpload").get(0).getFilename();
         String addr = secureKeyService.getAddrByKey(secureKey);
         if (blobKey == null || addr == null) {
 			res.setStatus(500);
 			result.put("error", "File not loaded");
 			return result;
         }
         Shot shot = new Shot();
         shot.setBlobKey(blobKey.getKeyString());
         shot.setCreated(new Date());
         
         try {
 			clientService.hit(addr);
 	        dao.save(shot);
 	    	result.put("filename", shot.getKey());
 		} catch (MaxHitCountExceededException e) {
 			res.setStatus(429);
 			result.put("error", "Maximum hits count per day has been exceeded");
 			blobstoreService.delete(blobKey);
 		}
 
         return result;
     }
 	
 	@RequestMapping(value = "/getuploadurl", method = RequestMethod.GET)
 	@ResponseBody
 	public Map<String, Object> getUploadURL(HttpServletRequest req) {
 	    String uploadUrl = blobstoreService.createUploadUrl("/upload", options);
 		String secureKey = secureKeyService.getSecureKey(req.getRemoteAddr(),
 				uploadUrl);
 		secureKeyService.save(secureKey, req.getRemoteAddr());
 
 		Map<String, Object> result = new HashMap<String, Object>();
 	    result.put("uploadUrl", uploadUrl);
 	    result.put("secureKey", secureKey);
 
 	    return result;
 	}
 }
