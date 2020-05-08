 package com.gmail.at.zhuikov.aleksandr.driveddoc.servlet;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.gmail.at.zhuikov.aleksandr.driveddoc.repository.SignatureContainerDescriptionRepository;
 import com.google.api.client.json.JsonFactory;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.utils.SystemProperty;
 import com.google.drive.samples.dredit.DrEditServlet;
 
 @Singleton
 public class OCSPSignatureContainerUploadURLServlet extends DrEditServlet {
 
 	@Inject
 	public OCSPSignatureContainerUploadURLServlet(JsonFactory jsonFactory) {
 		super(jsonFactory);
 	}
 
 	private static final long serialVersionUID = 1L;
 	
 	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 	SignatureContainerDescriptionRepository signatureContainerDescriptionRepository = 
 			SignatureContainerDescriptionRepository.getInstance();
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
		String url = blobstoreService.createUploadUrl("/api/OCSPSignatureContainer");
 		
 		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
 			url = url.replaceFirst("http://", "https://").replaceFirst("8888", "8443");
 		}
 		
 		sendJson(resp, url);
 	}
 }
