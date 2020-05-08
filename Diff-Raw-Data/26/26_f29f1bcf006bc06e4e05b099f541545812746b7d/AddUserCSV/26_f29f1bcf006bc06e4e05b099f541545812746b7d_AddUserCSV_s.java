 package automaatnehindaja;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 @WebServlet("/AddCSV")
 public class AddUserCSV extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static PasswordGeneratorAndMailer generator = new PasswordGeneratorAndMailer();
 	private static Logger logger = Logger.getLogger(AddUserCSV.class);
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		boolean isMultipartContent = ServletFileUpload
 				.isMultipartContent(request);
 		if (request.isUserInRole("admin")
 				|| request.isUserInRole("responsible")) {
 
 			if (!isMultipartContent) {
 				response.sendRedirect("error.html");
 				logger.warn("request is not multipart, unexpected. Request by: "
 						+ request.getRemoteUser());
 				return;
 			}
 			FileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			try {
 				List<FileItem> fields = upload.parseRequest(request);
 				Iterator<FileItem> it = fields.iterator();
 				if (fields.size() != 2) {
 					response.sendRedirect("addusers.html?success=false");
 					logger.warn("Fields size not 2, unexpected. Request by: "
 							+ request.getRemoteUser());
 					return;
 				}
 				FileItem fileitem = it.next();
 				if (fileitem.getSize() > 1024 * 1024) {
 					response.sendRedirect("addusers.html?success=false");
 					logger.warn("File larger than 1MB. Request by: "
 							+ request.getRemoteUser());
 					return;
 				}
 				if (!fileitem.getName().endsWith(".csv")) {
 					response.sendRedirect("addusers.html?success=false");
 					logger.warn("Incorrect file type. Request by: "
 							+ request.getRemoteUser());
 					return;
 				}
 
 				String course = it.next().getString();
 				logger.info("Starting aading users via CSV. Request by: " + request.getRemoteUser());
 				if (AddUsersToSql(fileitem.getInputStream(), course)) {
 					logger.info("User addation succeeded");
 				}
 
 			} catch (FileUploadException e) {
 				logger.error("File upload exception. Request by: " + request.getRemoteUser(), e);
 				return;
 			}
 			response.sendRedirect("addusers.html?success=true");
 		}
 		else{
 			logger.warn("Unauthorized access by: " + request.getRemoteUser());
 			response.sendRedirect("logout");
 			return;
 		}
 	}
 	
 
 	protected boolean AddUsersToSql(InputStream stream, String course) {
 		PreparedStatement stmt = null;
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			Connection c = DriverManager.getConnection(
 					"jdbc:mysql://localhost:3306/automaatnehindaja",
 					"ahindaja", "k1rven2gu");
 			File tempFile = File.createTempFile("temporary", "csv");
 			FileOutputStream out = new FileOutputStream(tempFile);
 			IOUtils.copy(stream, out);
 			CSVReader csvReader = new CSVReader(new FileReader(tempFile), ';');
 			List<String[]> result = csvReader.readAll();
 
 			csvReader.close();
 			tempFile.delete();
 
 			try {
 				Integer.parseInt(result.get(0)[0]);
 			} catch (NumberFormatException e) {
 				result.remove(0);
 			}
 
 			String statement = "INSERT IGNORE INTO users VALUES (?,?,?,?);";
 			String newPassword = null;
 			String email;
 			List<String> emails = new ArrayList<String>();
 			List<String> passwords = new ArrayList<String>();
 
 			for (int i = 0; i < result.size(); i++) {
 				stmt = c.prepareStatement(statement);
 				newPassword = generator.generatePassword();
 				email = result.get(i)[5];
 				stmt.setString(1, email);
				stmt.setString(2, newPassword);
 				stmt.setString(3, result.get(i)[2]);
 				stmt.setString(4, result.get(i)[1]);
 				if (stmt.executeUpdate() != 0) {
 					emails.add(email);
 					passwords.add(newPassword);
 				}
 				stmt.executeUpdate();
 				stmt.close();
 			}
 
 			statement = "INSERT IGNORE INTO users_courses VALUES (?,?);";
 			stmt = c.prepareStatement(statement);
 
 			for (int i = 0; i < result.size(); i++) {
 
 				stmt.setString(1, result.get(i)[5]);
 				stmt.setString(2, course);
 				stmt.addBatch();
 			}
 
 			stmt.executeBatch();
 			stmt.close();
 
 			statement = "INSERT IGNORE INTO users_roles VALUES (?,?);";
 			stmt = c.prepareStatement(statement);
 
 			for (int i = 0; i < result.size(); i++) {
 
 				stmt.setString(1, result.get(i)[5]);
 				stmt.setString(2, "tudeng");
 				stmt.addBatch();
 			}
 
 			stmt.executeBatch();
 			stmt.close();
 
 			c.close();
 
 			generator.sendAsyncEmails(emails, passwords);
 
 		} catch (IOException | ClassNotFoundException | SQLException e) {
 			logger.error("Error during adding users via CSV.", e);
 			return false;
 		}
 		return true;
 	}
 }
