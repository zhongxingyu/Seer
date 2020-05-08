 package com.walmart.hackday.service;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataParam;
  
 @Path("/file")
 public class UploadFileService {
 	private static Logger logger = Logger.getLogger(UploadFileService.class);
 	@POST
 	@Path("/upload")
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Response uploadFile(
 		@FormDataParam("file") InputStream uploadedInputStream,
 		@FormDataParam("file") FormDataContentDisposition fileDisposition) {
 		logger.debug("uploadFile(): I am in.." + fileDisposition.getFileName());
 		logger.debug("uploadFile():" + System.getProperty("catalina.base"));
 		String uploadedFileLocation = System.getProperty("catalina.base") + "/webapps/ROOT/images/" + fileDisposition.getFileName();
		//String uploadedFileLocation = "/Users/vpalani/Documents/workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/ROOT/images" + fileDisposition.getFileName();
  
 		// save it
 		writeToFile(uploadedInputStream, uploadedFileLocation);
  
 		String output = "File uploaded to : " + uploadedFileLocation;
  
 		return Response.status(200).entity(output).build();
  
 	}
  
 	// save uploaded file to new location
 	private void writeToFile(InputStream uploadedInputStream,
 		String uploadedFileLocation) {
  
 		try {
 			OutputStream out = new FileOutputStream(new File(
 					uploadedFileLocation));
 			int read = 0;
 			byte[] bytes = new byte[1024];
  
 			out = new FileOutputStream(new File(uploadedFileLocation));
 			while ((read = uploadedInputStream.read(bytes)) != -1) {
 				out.write(bytes, 0, read);
 			}
 			out.flush();
 			out.close();
 		} catch (IOException e) {
  
 			e.printStackTrace();
 		}
  
 	}
  
 }
