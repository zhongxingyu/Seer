 package com.readytalk.olive.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import com.google.gson.Gson;
 import com.readytalk.olive.json.DeleteProjectRequest;
 import com.readytalk.olive.logic.HttpSenderReceiver;
 import com.readytalk.olive.logic.OliveDatabaseApi;
 import com.readytalk.olive.logic.S3Uploader;
 import com.readytalk.olive.logic.Security;
 import com.readytalk.olive.model.Project;
 import com.readytalk.olive.model.User;
 import com.readytalk.olive.util.Attribute;
 import com.readytalk.olive.util.InvalidFileSizeException;
 
 public class OliveServlet extends HttpServlet {
 	// Don't store anything as a member variable in the Servlet.
 	// private Object dontDoThis;
 
 	// Generated using Eclipse's "Add generated serial version ID" refactoring.
 	private static final long serialVersionUID = -6820792513104430238L;
 	// Static variables are okay, though, because they don't change across instances.
 	private static Logger log = Logger.getLogger(OliveServlet.class.getName());
 	private static final String TMP_DIR_PATH = "/temp/";
 	private static File tmpDir;
 	private static final String DESTINATION_DIR_PATH = "/temp/";
 	private static File destinationDir;
 
 	// Modified from: http://www.jsptube.com/servlet-tutorials/servlet-file-upload-example.html
 	// Also see: http://stackoverflow.com/questions/4101960/storing-image-using-htm-input-type-file
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		String realPathTmp = getServletContext().getRealPath(TMP_DIR_PATH);
 		tmpDir = new File(realPathTmp);
 		// tmpDir = new File(TMP_DIR_PATH);
 		log.info(realPathTmp);
 		if (!tmpDir.isDirectory()) {
 			throw new ServletException(TMP_DIR_PATH + " is not a directory");
 		}
 		String realPathDest = getServletContext().getRealPath(
 				DESTINATION_DIR_PATH);
 		destinationDir = new File(realPathDest);
 		// destinationDir = new File(DESTINATION_DIR_PATH);
 		log.info(realPathDest);
 		if (!destinationDir.isDirectory()) {
 			throw new ServletException(DESTINATION_DIR_PATH
 					+ " is not a directory");
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		if (request.getContentType().contains(
 				"application/x-www-form-urlencoded")) { // Full value: "application/x-www-form-urlencoded"
 			// This is a regular text form.
 			String id = request.getParameter("FormName");
 			log.info("The servlet is responding to an "
 					+ "HTTP POST request from form: " + id);
 			if (id.equals("LoginUser")) {
 				handleLogin(request, response, session);
 			} else if (id.equals("EditUser")) {
 				handleEditUser(request, response, session);
 			} else if (id.equals("AddUser")) {
 				handleAddUser(request, response, session);
 			} else if (id.equals("AddProject")) {
 				handleAddProject(request, response, session);
 			} else if (id.equals("SplitVideo")) {
 				handleSplitVideo(request, response, session);
 			} else {
 				log.severe("HTTP POST request coming from unknown form: " + id);
 			}
 		} else if (request.getContentType().contains("multipart/form-data")) { // Full value: "multipart/form-data; boundary=----WebKitFormBoundaryjAGjLWGWeI3ltfBe"
 			// This is a file upload form.
 			log.info("The servlet is responding to an "
 					+ "HTTP POST request from a file upload form");
 			handleUploadVideo(request, response, session);
 		} else if (request.getContentType().contains("application/json")) {
 			// This is not a form, but a custom POST request with JSON in it.
 			log.info("responding to a custom POST");
 			log.info("The servlet is responding to an "
 					+ "HTTP POST request in JSON format");
 			handleJsonPostRequest(request, response, session);
 		} else {
 			log.severe("Unknown content type");
 		}
 	}
 
 	// http://www.apl.jhu.edu/~hall/java/Servlet-Tutorial/Servlet-Tutorial-Form-Data.html
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		log.info("The servlet is responding to an HTTP GET request");
 		response.setContentType("text/html");
 		HttpSession session = request.getSession();
 		String projectName = request.getParameter("projectName");
 		int accountId = OliveDatabaseApi.getAccountId((String) session
 				.getAttribute(Attribute.USERNAME.toString()));
 		if (projectName != null && Security.isSafeProjectName(projectName)
 				&& OliveDatabaseApi.projectExists(projectName, accountId)) { // Short-circuiting
 			session.setAttribute(Attribute.PROJECT_NAME.toString(), projectName);
 			response.sendRedirect("editor.jsp");
 		} else {
 			response.sendRedirect("projects.jsp");
 		}
 		PrintWriter out = response.getWriter();
 		out.println("File uploaded. Please close this window and refresh the editor page.");
 		out.close();
 	}
 
 	private void handleLogin(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		Boolean isAuthorized;
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		if (Security.isSafeUsername(username)
 				&& Security.isSafePassword(password)) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 			isAuthorized = OliveDatabaseApi.isAuthorized(username, password);
 			session.setAttribute(Attribute.IS_AUTHORIZED.toString(),
 					isAuthorized);
 			if (isAuthorized) { // Take the user to the projects page.
 				int accountId = OliveDatabaseApi.getAccountId(username);
 				session.setAttribute(Attribute.USERNAME.toString(),
 						OliveDatabaseApi.getUsername(accountId));
 				session.setAttribute(Attribute.PASSWORD.toString(), password);
 				session.setAttribute(Attribute.EMAIL.toString(),
 						OliveDatabaseApi.getEmail(accountId));
 				session.setAttribute(Attribute.NAME.toString(),
 						OliveDatabaseApi.getAccountName(accountId));
 				session.removeAttribute(Attribute.IS_SAFE.toString()); // Cleared so as to not interfere with any other form.
 				response.sendRedirect("projects.jsp");
 			} else {
 				response.sendRedirect("index.jsp"); // Keep the user on the same page.
 			}
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 			session.setAttribute(Attribute.IS_AUTHORIZED.toString(), false);
 			response.sendRedirect("index.jsp");
 		}
 	}
 
 	private void handleEditUser(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		String username = (String) session.getAttribute(Attribute.USERNAME
 				.toString());
 		String newName = request.getParameter("new-name");
 		String newEmail = request.getParameter("new-email");
 		String newPassword = request.getParameter("new-password");
 		String confirmNewPassword = request
 				.getParameter("confirm-new-password");
 		if (Security.isSafeName(newName) && Security.isSafeEmail(newEmail)
 				&& Security.isSafePassword(newPassword)
 				&& Security.isSafePassword(confirmNewPassword)) {
 			if (newPassword.equals(confirmNewPassword)) {
 				User updateUser = new User(username, newPassword, newName,
 						newEmail);
 				Boolean editSuccessfully = OliveDatabaseApi
 						.editAccount(updateUser);
 				session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(),
 						editSuccessfully);
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(), true);
 				session.setAttribute(Attribute.PASSWORD.toString(), newPassword);
 				session.setAttribute(Attribute.EMAIL.toString(), newEmail);
 				session.setAttribute(Attribute.NAME.toString(), newName);
 			} else {
 				session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(),
 						false);
 				session.setAttribute(Attribute.PASSWORDS_MATCH.toString(),
 						false);
 			}
 		} else {
 			session.setAttribute(Attribute.EDIT_SUCCESSFULLY.toString(), false);
 		}
 		response.sendRedirect("account.jsp");
 	}
 
 	private void handleAddUser(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		// The jQuery regex should catch malicious input, but sanitize just to
 		// be safe.
 		String username = Security.stripOutIllegalCharacters(request
 				.getParameter("name"));
 		String password = Security.stripOutIllegalCharacters(request
 				.getParameter("password"));
 		String email = Security.stripOutIllegalCharacters(request
 				.getParameter("email"));
 		User newUser = new User(username, password, "", email);
 		Boolean addSuccessfully = OliveDatabaseApi.AddAccount(newUser);
 		if (addSuccessfully) {
 			session.setAttribute(Attribute.IS_AUTHORIZED.toString(), true);
 			session.setAttribute(Attribute.USERNAME.toString(), username);
 			session.setAttribute(Attribute.PASSWORD.toString(), password);
 			session.setAttribute(Attribute.EMAIL.toString(), email);
 			response.sendRedirect("projects.jsp");
 		} else {
 			response.sendRedirect("index.jsp");
 			// TODO Add error message here
 		}
 	}
 
 	private void handleAddProject(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws UnsupportedEncodingException, IOException {
 		String projectName = request.getParameter("ProjectName");
 		if (Security.isSafeProjectName(projectName)) {
 			session.setAttribute(Attribute.IS_SAFE.toString(), true);
 
 			String sessionUsername = (String) session
 					.getAttribute(Attribute.USERNAME.toString());
 			int accountId = OliveDatabaseApi.getAccountId(sessionUsername);
 			String icon = ""; // TODO Get this from user input.
 			Project project = new Project(projectName, accountId, icon);
 			OliveDatabaseApi.AddProject(project);
 		} else {
 			session.setAttribute(Attribute.IS_SAFE.toString(), false);
 		}
 		response.sendRedirect("new-project-form.jsp");
 	}
 
 	private static void handleUploadVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		PrintWriter out = response.getWriter();
 		out.println("Uploading file...");
 
 		response.setContentType("text/plain");
 
 		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
 		// Set the size threshold, above which content will be stored on disk.
 		fileItemFactory.setSizeThreshold(1 * 1024 * 1024); // 1 MB
 
 		// Set the temporary directory to store the uploaded files of size above threshold.
 		fileItemFactory.setRepository(tmpDir);
 
 		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
 		try {
 			/*
 			 * Parse the request
 			 */
 			List items = uploadHandler.parseRequest(request);
 			Iterator itr = items.iterator();
 			while (itr.hasNext()) {
 				FileItem item = (FileItem) itr.next();
 				/*
 				 * Handle Form Fields.
 				 */
 				if (item.isFormField()) {
 					log.info("Form Name = \"" + item.getFieldName()
 							+ "\", Value = \"" + item.getString() + "\"");
 				} else {
 					// Handle Uploaded files.
 					log.info("Field Name = \"" + item.getFieldName()
 							+ "\", File Name = \"" + item.getName()
 							+ "\", Content type = \"" + item.getContentType()
 							+ "\", File Size (bytes) = \"" + item.getSize()
 							+ "\"");
 					/*
 					 * Write file to the ultimate location.
 					 */
 					File file = new File(destinationDir, item.getName()); // Allocate the space
 					item.write(file); // Save the file to the allocated space
 
 					String sessionUsername = (String) session
 							.getAttribute(Attribute.USERNAME.toString());
 					int accountId = OliveDatabaseApi
 							.getAccountId(sessionUsername);
 					String projectName = (String) session
 							.getAttribute(Attribute.PROJECT_NAME.toString());
 					int projectId = OliveDatabaseApi.getProjectId(projectName,
 							accountId);
 					String videoName = file.getName().split("[.]")[0]; // Strip extensions
 					if (Security.isSafeVideoName(videoName)
 							&& S3Uploader.uploadFile(file)) { // Short-circuiting for efficiency
 						String icon = ""; // TODO Obtain this from S3.
 						OliveDatabaseApi.AddVideo(videoName, "http", projectId,
 								icon);
 					}
 
 					file.delete();
 				}
 			}
 			out.println("File uploaded. Please close this window and refresh the editor page.");
 			out.println();
 		} catch (FileUploadException e) {
 			log.severe("Error encountered while parsing the request in the upload handler");
 			out.println("Upload failed.");
 			e.printStackTrace();
 		} catch (InvalidFileSizeException e) {
 			log.severe("Invalid file size");
 			out.println("Upload failed (invalid file size)");
 			e.printStackTrace();
 		} catch (Exception e) {
 			log.severe("Unknown error encountered while uploading file");
 			out.println("Upload failed (unknown reason).");
 			e.printStackTrace();
 		} finally {
 			out.close();
 		}
 	}
 
 	private void handleSplitVideo(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		HttpSenderReceiver.split();
 		response.sendRedirect("editor.jsp");
 	}
 
 	// Gson help: http://code.google.com/p/google-gson/
 	// http://stackoverflow.com/questions/338586/a-better-java-json-library
 	// http://stackoverflow.com/questions/1688099/converting-json-to-java/1688182#1688182
 	private void handleJsonPostRequest(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 
 		// TODO Make this more general (not just a deleteProjectRequest).
 		DeleteProjectRequest deleteProjectRequest = new Gson().fromJson(
 				request.getReader(), DeleteProjectRequest.class);
 
 		if (!deleteProjectRequest.command.equals("deleteProject")) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		}
 
 		response.setContentType("text/plain");
 		// response.setStatus(HttpServletResponse.SC_OK); // Unnecessary
 
 		PrintWriter out = response.getWriter();
 
 		out.println("{\"command\":\"" + deleteProjectRequest.command
 				+ "\",\"arguments\":[{\"project\":\""
 				+ deleteProjectRequest.arguments[0].project + "\"}]}");
 
 		String sessionUsername = (String) session
 				.getAttribute(Attribute.USERNAME.toString());
 		int accountId = OliveDatabaseApi.getAccountId(sessionUsername);
 		String firstProjectToDelete = deleteProjectRequest.arguments[0].project;
 		int projectId = OliveDatabaseApi.getProjectId(firstProjectToDelete,
 				accountId);
 		OliveDatabaseApi.deleteProject(projectId);
 
 		out.println(deleteProjectRequest.arguments[0].project
 				+ " deleted successfully.");
 		out.close();
 	}
 }
