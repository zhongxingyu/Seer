 package com.epam.lab.intouch.web.util;
 
 import static com.epam.lab.intouch.dao.util.PropertiesReader.getProperty;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 @MultipartConfig
 public class AttachmentProvider {
 	
 
 	private final static Logger LOG = LogManager.getLogger(AttachmentProvider.class);
 
 	private String fullPath = "";
 	private String userPhotoName;
 	private String userPhotoNameExtension;
 	private List<FileItem> uploadedItems;
 
 	public void processRequest(HttpServletRequest request, String userLogin) throws ServletException, IOException {
 
 		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 
 		if (!isMultipart) {
			
 		} else {
 
 			// File repository = new
 			// File(request.getServletContext().getRealPath("/images"));
 
 			// String newFileName = request.getParameter("newFileName");
 			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
 			diskFileItemFactory.setRepository(getRepository()); 
 			
 			diskFileItemFactory.setSizeThreshold(5 * 1024 * 1024); 
 			ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory); 
 			try {
 				uploadedItems = servletFileUpload.parseRequest(request);
 			} catch (FileUploadException e) {
 				LOG.error("Some problems with File Upload", e);
 				e.printStackTrace();
 			}
 
 			for (FileItem fileItem : uploadedItems) {
 				if (!fileItem.isFormField()) {
 					setUserPhotoName(userLogin);
 					fullPath = getRepository().getAbsolutePath() + File.separator + getUserPhotoName();
 					File uploadedFile = new File(fullPath);
 					System.out.println(fullPath);
 					try {
 						fileItem.write(uploadedFile);
 					} catch (Exception e) {
 						LOG.error("Problem with writing file to repository");
 						e.printStackTrace();
 					}
 
 				}
 			}
 		}
 	}
 
 	private void setUserPhotoName(String userLogin) {
 		
 		
 
 		for (FileItem fileItem : uploadedItems) {
 			if (!fileItem.isFormField()) {
 
 				this.userPhotoNameExtension = FilenameUtils.getExtension(fileItem.getName());
 			}
 		}
 
 		this.userPhotoName = userLogin+ '.' + userPhotoNameExtension;
 
 	}
 	
 	public String getUserPhotoNameExtension(){
 		return userPhotoNameExtension;
 		
 	}
 
 	public String getUserPhotoName() {
 		return userPhotoName;
 	}
 
 	private File getRepository() {
 		File repository = new File(getProperty("avatar_storage"));
 		if (!repository.exists()) {
 			repository.mkdir();
 		}
 
 		return repository;
 
 	}
 
 }
