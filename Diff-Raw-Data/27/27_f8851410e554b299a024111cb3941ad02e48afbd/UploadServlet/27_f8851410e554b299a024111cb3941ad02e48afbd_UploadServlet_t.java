 package org.kvj.lima1.gae.sync.rest;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.amber.oauth2.common.OAuth;
 import org.codehaus.jettison.json.JSONObject;
 import org.kvj.lima1.gae.sync.data.FileStorage;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 
 public class UploadServlet extends OAuthSecuredServlet {
 
 	BlobstoreService blobstoreService = BlobstoreServiceFactory
 			.getBlobstoreService();
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String name = req.getParameter("name");
 		String app = req.getParameter("app");
 		Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
 		BlobKey file = blobs.get("file");
		log.info("Upload: " + name + " - " + file);
 		if (null != name && null != file) {
 			String result = FileStorage.upload(app,
 					(String) req.getAttribute(OAuth.OAUTH_CLIENT_ID), file,
 					name);
 			if (null != result) {
 				throw new ServletException(result);
 			}
 			writeJSON(new JSONObject(), resp);
 		} else {
 			throw new ServletException("Invalid parameters");
 		}
 	}
 
 	@Override
 	protected JSONObject get(HttpServletRequest req) throws Exception {
 		JSONObject result = new JSONObject();
 		result.put(
 				"u",
 				blobstoreService.createUploadUrl("/rest/file/upload?name="
 						+ req.getParameter("name") + "&oauth_token="
 						+ req.getAttribute(OAuth.OAUTH_TOKEN) + "&app="
 						+ req.getParameter("app")));
 		return result;
 	}
 }
