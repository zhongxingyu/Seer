 /*
  *
  */
 package uk.ac.ncl.web;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletContextAttributeEvent;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.io.IOUtils;
 import org.jboss.resteasy.plugins.providers.multipart.InputPart;
 import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
 
 import uk.ac.ncl.model.RuleFilesEnum;
 
 /**
  * The Class UploadFileService.
  *
  * @author <a href="mailto:i.sfyrakis@ncl.ac.uk">Ioannis Sfyrakis</a>
  * @author last edited by: $$Author: gsfyrakis $$
  * @version $$Revision: 5 $$, $$Date: 2012-09-05 23:33:00 +0100 (Wed, 05 Sep 2012) $$
  */
 @Path("/file")
 public class UploadFileService {
 

 	/**
 	 * uploads a file to server instance.
 	 * @uml.property  name="uriInfo"
 	 * @uml.associationEnd  readOnly="true"
 	 */
 	@Context
 	UriInfo uriInfo;
 
	/**
 	 * Upload file.
 	 *
 	 * @param input
 	 *            the input
 	 * @return the response builder
 	 */
 	@POST
 	@Path("/upload")
 	@Consumes("multipart/form-data")
	@Produces({ MediaType.TEXT_HTML })
	public ResponseBuilder uploadFile(MultipartFormDataInput input) {
 
 		String fileName = "";
 
 		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
 		List<InputPart> inputParts = uploadForm.get("uploadedFile");
 
 		for (InputPart inputPart : inputParts) {
 
 			try {
 
 				MultivaluedMap<String, String> header = inputPart.getHeaders();
 				fileName = getFileName(header);
 
 				// convert the uploaded file to inputstream
 				InputStream inputStream = inputPart.getBody(InputStream.class, null);
 
 				byte[] bytes = IOUtils.toByteArray(inputStream);
 
 				// constructs upload file path
 				// fileName = UPLOADED_FILE_PATH + fileName;
 
 				writeFile(bytes, fileName);
 
				System.out.println("Done");

 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 
		UriBuilder ub = uriInfo.getAbsolutePathBuilder();
		// String warName = .getContextPath();
		// TODO improve how we handle paths
		java.net.URI userUri = ub.replacePath("CCCRest/UploadForm.html").build();
		System.out.println("uri: " + userUri);
		return Response.seeOther(userUri);
		// return Response.status(200).entity("").build();
 
 	}
 
 	/**
 	 * Extract filename from header
 	 *
 	 * header sample { Content-Type=[image/png], Content-Disposition=[form-data;
 	 * name="file"; filename="filename.extension"] }
 	 *
 	 * @param header
 	 *            the header
 	 * @return the file name
 	 */
 	private String getFileName(MultivaluedMap<String, String> header) {
 
 		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
 
 		for (String filename : contentDisposition) {
 			if ((filename.trim().startsWith("filename"))) {
 
 				String[] name = filename.split("=");
 
 				String finalFileName = name[1].trim().replaceAll("\"", "");
 				return finalFileName;
 			}
 		}
 		return "unknown";
 	}
 
 	/**
 	 * Save file to a folder in the server's path /drools/upload.
 	 *
 	 * @param content
 	 *            the content
 	 * @param filename
 	 *            the filename
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	private void writeFile(byte[] content, String filename) throws IOException {
 
 		File file = new File(filename);
 
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 
		String uploadDir = RuleFilesEnum.SIMPLE_CONTRACT.getRulesFolderPath();
		System.out.println("writeFile to : " + uploadDir);
 
 		FileOutputStream fop = new FileOutputStream(uploadDir + "/" + file);
 
 		fop.write(content);
 		fop.flush();
 		fop.close();
 
 	}
 }
