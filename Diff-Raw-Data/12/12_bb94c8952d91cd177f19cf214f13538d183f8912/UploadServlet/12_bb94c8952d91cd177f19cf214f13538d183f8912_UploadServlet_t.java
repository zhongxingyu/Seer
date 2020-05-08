 package com.imaginea.javatooling.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import com.imaginea.javatooling.helpers.JavaDiff;
 
 /**
  * Servlet implementation class UploadServlet
  */
 @WebServlet("/UploadServlet")
 public class UploadServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String UPLOAD_DIRECTORY = "upload";
 	private static final int THRESHOLD_SIZE = 1024 * 1024 * 3; // 3MB
 	private static final int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
 	private static final int REQUEST_SIZE = 1024 * 1024 * 50; // 50MB
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// checks if the request actually contains upload file
 		if (!ServletFileUpload.isMultipartContent(request)) {
 			// if not, we stop here
 			return;
 		}
 
 		// configures some settings
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		factory.setSizeThreshold(THRESHOLD_SIZE);
 		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
 
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		upload.setFileSizeMax(MAX_FILE_SIZE);
 		upload.setSizeMax(REQUEST_SIZE);
 
 		// constructs the directory path to store upload file
 		String uploadPath = getServletContext().getRealPath("")
 				+ File.separator + UPLOAD_DIRECTORY;
 
 		// creates the directory if it does not exist
 		File uploadDir = new File(uploadPath);
 		if (!uploadDir.exists()) {
 			uploadDir.mkdir();
 		}
 		JavaDiff diff = null;
 		try {
 			// parses the request's content to extract file data
 			List formItems = upload.parseRequest(request);
 			Iterator iter = formItems.iterator();
 			List<String> filePaths = new ArrayList<String>(2);
 			// iterates over form's fields
 			while (iter.hasNext()) {
 				FileItem item = (FileItem) iter.next();
 				// processes only fields that are not form fields
 				if (!item.isFormField()) {
 					String fileName = new File(item.getName()).getName();
 					String filePath = uploadPath + File.separator + fileName;
 					System.out.println(filePath);
 					filePaths.add(filePath);
 					File storeFile = new File(filePath);
 					// saves the file on disk
 					item.write(storeFile);
 				}
 			}
 			diff = new JavaDiff(filePaths.get(0), filePaths.get(1));
 			// diff.compare();
 			// request.setAttribute("response", diff.compare());
 			response.sendRedirect("http://" + request.getServerName() + ":"
 					+ request.getServerPort() + "/" + request.getContextPath()
					+ "/rest/diffservice/differences?d=" + diff.compare());
 		} catch (Exception ex) {
 			request.setAttribute("message",
 					"There was an error: " + ex.getMessage());
 		}
 		getServletContext().getRequestDispatcher("/message.jsp").forward(
 				request, response);
 	}
 }
