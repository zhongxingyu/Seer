 package org.kvj.lima1.gae.sync.rest;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.amber.oauth2.common.OAuth;
 import org.kvj.lima1.gae.sync.data.FileStorage;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.images.Image;
 import com.google.appengine.api.images.ImagesService;
 import com.google.appengine.api.images.ImagesService.OutputEncoding;
 import com.google.appengine.api.images.ImagesServiceFactory;
 import com.google.appengine.api.images.InputSettings;
 import com.google.appengine.api.images.InputSettings.OrientationCorrection;
 import com.google.appengine.api.images.OutputSettings;
 import com.google.appengine.api.images.Transform;
 
 public class DownloadServlet extends OAuthSecuredServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String app = req.getParameter("app");
 		String name = req.getParameter("name");
 		BlobKey file = FileStorage.getFile(app,
 				(String) req.getAttribute(OAuth.OAUTH_CLIENT_ID), name);
 		if (null == file) {
 			throw new ServletException("File not found");
 		} else {
 			BlobstoreService blobstoreService = BlobstoreServiceFactory
 					.getBlobstoreService();
 			String width = req.getParameter("width");
 			if (null != "width") {
 				ImagesService imagesService = ImagesServiceFactory
 						.getImagesService();
 				Image fromBlob = ImagesServiceFactory.makeImageFromBlob(file);
 				InputSettings inputSettings = new InputSettings();
 				inputSettings
 						.setOrientationCorrection(OrientationCorrection.CORRECT_ORIENTATION);
 				OutputSettings outputSettings = new OutputSettings(
 						OutputEncoding.JPEG);
 				fromBlob = imagesService.applyTransform(
 						ImagesServiceFactory.makeRotate(0), fromBlob,
 						inputSettings, outputSettings);
 				double mul = Double.parseDouble(width) / fromBlob.getWidth();
				log.info("Transform image (with rotate) " + fromBlob.getWidth()
						+ fromBlob.getHeight() + ", " + mul);
 				Transform transform = ImagesServiceFactory.makeResize(
 						(int) Math.ceil(fromBlob.getWidth() * mul),
 						(int) Math.ceil(fromBlob.getHeight() * mul));
 				Image outImage = imagesService.applyTransform(transform,
 						fromBlob, inputSettings, outputSettings);
 				resp.getOutputStream().write(outImage.getImageData());
 				resp.flushBuffer();
 				return;
 			}
 			blobstoreService.serve(file, resp);
 		}
 	}
 }
